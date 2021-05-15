package org.xbill.DNS;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.xbill.DNS.utils.base64;

public class TSIG {
    public static final short FUDGE = 300;
    public static final Name HMAC;
    public static final Name HMAC_MD5;
    public static final Name HMAC_SHA1 = Name.fromConstantString("hmac-sha1.");
    public static final Name HMAC_SHA224 = Name.fromConstantString("hmac-sha224.");
    public static final Name HMAC_SHA256 = Name.fromConstantString("hmac-sha256.");
    public static final Name HMAC_SHA384 = Name.fromConstantString("hmac-sha384.");
    public static final Name HMAC_SHA512 = Name.fromConstantString("hmac-sha512.");
    private static Map algMap;
    /* access modifiers changed from: private */
    public Name alg;
    /* access modifiers changed from: private */
    public Mac hmac;
    /* access modifiers changed from: private */
    public Name name;

    static {
        Name fromConstantString = Name.fromConstantString("HMAC-MD5.SIG-ALG.REG.INT.");
        HMAC_MD5 = fromConstantString;
        HMAC = fromConstantString;
        Map out = new HashMap();
        out.put(HMAC_MD5, "HmacMD5");
        out.put(HMAC_SHA1, "HmacSHA1");
        out.put(HMAC_SHA224, "HmacSHA224");
        out.put(HMAC_SHA256, "HmacSHA256");
        out.put(HMAC_SHA384, "HmacSHA384");
        out.put(HMAC_SHA512, "HmacSHA512");
        algMap = Collections.unmodifiableMap(out);
    }

    public static Name algorithmToName(String alg2) {
        for (Map.Entry entry : algMap.entrySet()) {
            if (alg2.equalsIgnoreCase((String) entry.getValue())) {
                return (Name) entry.getKey();
            }
        }
        throw new IllegalArgumentException("Unknown algorithm");
    }

    public static String nameToAlgorithm(Name name2) {
        String alg2 = (String) algMap.get(name2);
        if (alg2 != null) {
            return alg2;
        }
        throw new IllegalArgumentException("Unknown algorithm");
    }

    /* access modifiers changed from: private */
    public static boolean verify(Mac mac, byte[] signature) {
        return verify(mac, signature, false);
    }

    private static boolean verify(Mac mac, byte[] signature, boolean truncation_ok) {
        byte[] expected = mac.doFinal();
        if (truncation_ok && signature.length < expected.length) {
            byte[] truncated = new byte[signature.length];
            System.arraycopy(expected, 0, truncated, 0, truncated.length);
            expected = truncated;
        }
        return Arrays.equals(signature, expected);
    }

    private void init_hmac(String macAlgorithm, SecretKey key) {
        try {
            Mac instance = Mac.getInstance(macAlgorithm);
            this.hmac = instance;
            instance.init(key);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Caught security exception setting up HMAC.");
        }
    }

    public TSIG(Name algorithm, Name name2, byte[] keyBytes) {
        this.name = name2;
        this.alg = algorithm;
        String macAlgorithm = nameToAlgorithm(algorithm);
        init_hmac(macAlgorithm, new SecretKeySpec(keyBytes, macAlgorithm));
    }

    public TSIG(Name algorithm, Name name2, SecretKey key) {
        this.name = name2;
        this.alg = algorithm;
        init_hmac(nameToAlgorithm(algorithm), key);
    }

    public TSIG(Mac mac, Name name2) {
        this.name = name2;
        this.hmac = mac;
        this.alg = algorithmToName(mac.getAlgorithm());
    }

    public TSIG(Name name2, byte[] key) {
        this(HMAC_MD5, name2, key);
    }

    public TSIG(Name algorithm, String name2, String key) {
        byte[] keyBytes = base64.fromString(key);
        if (keyBytes != null) {
            try {
                this.name = Name.fromString(name2, Name.root);
                this.alg = algorithm;
                String macAlgorithm = nameToAlgorithm(algorithm);
                init_hmac(macAlgorithm, new SecretKeySpec(keyBytes, macAlgorithm));
            } catch (TextParseException e) {
                throw new IllegalArgumentException("Invalid TSIG key name");
            }
        } else {
            throw new IllegalArgumentException("Invalid TSIG key string");
        }
    }

    public TSIG(String algorithm, String name2, String key) {
        this(algorithmToName(algorithm), name2, key);
    }

    public TSIG(String name2, String key) {
        this(HMAC_MD5, name2, key);
    }

    public static TSIG fromString(String str) {
        String[] parts = str.split("[:/]", 3);
        if (parts.length >= 2) {
            if (parts.length == 3) {
                try {
                    return new TSIG(parts[0], parts[1], parts[2]);
                } catch (IllegalArgumentException e) {
                    parts = str.split("[:/]", 2);
                }
            }
            return new TSIG(HMAC_MD5, parts[0], parts[1]);
        }
        throw new IllegalArgumentException("Invalid TSIG key specification");
    }

    public TSIGRecord generate(Message m, byte[] b, int error, TSIGRecord old) {
        Date timeSigned;
        boolean signing;
        int fudge;
        byte[] signature;
        byte[] other;
        int i = error;
        if (i != 18) {
            timeSigned = new Date();
        } else {
            timeSigned = old.getTimeSigned();
        }
        if (i == 0 || i == 18) {
            this.hmac.reset();
            signing = true;
        } else {
            signing = false;
        }
        int fudge2 = Options.intValue("tsigfudge");
        if (fudge2 < 0 || fudge2 > 32767) {
            fudge = 300;
        } else {
            fudge = fudge2;
        }
        if (old != null) {
            DNSOutput out = new DNSOutput();
            out.writeU16(old.getSignature().length);
            if (signing) {
                this.hmac.update(out.toByteArray());
                this.hmac.update(old.getSignature());
            }
        }
        if (signing) {
            this.hmac.update(b);
        } else {
            byte[] bArr = b;
        }
        DNSOutput out2 = new DNSOutput();
        this.name.toWireCanonical(out2);
        out2.writeU16(255);
        out2.writeU32(0);
        this.alg.toWireCanonical(out2);
        long time = timeSigned.getTime() / 1000;
        int timeHigh = (int) (time >> 32);
        long timeLow = time & 4294967295L;
        out2.writeU16(timeHigh);
        out2.writeU32(timeLow);
        out2.writeU16(fudge);
        out2.writeU16(i);
        out2.writeU16(0);
        if (signing) {
            this.hmac.update(out2.toByteArray());
        }
        if (signing) {
            signature = this.hmac.doFinal();
        } else {
            signature = new byte[0];
        }
        if (i == 18) {
            DNSOutput out3 = new DNSOutput();
            long time2 = new Date().getTime() / 1000;
            int timeHigh2 = (int) (time2 >> 32);
            long timeLow2 = time2 & 4294967295L;
            out3.writeU16(timeHigh2);
            out3.writeU32(timeLow2);
            DNSOutput dNSOutput = out3;
            long j = time2;
            long j2 = timeLow2;
            other = out3.toByteArray();
            int i2 = timeHigh2;
        } else {
            DNSOutput dNSOutput2 = out2;
            long j3 = time;
            long j4 = timeLow;
            other = null;
            int i3 = timeHigh;
        }
        int i4 = fudge;
        return new TSIGRecord(this.name, 255, 0, this.alg, timeSigned, fudge, signature, m.getHeader().getID(), error, other);
    }

    public void apply(Message m, int error, TSIGRecord old) {
        m.addRecord(generate(m, m.toWire(), error, old), 3);
        m.tsigState = 3;
    }

    public void apply(Message m, TSIGRecord old) {
        apply(m, 0, old);
    }

    public void applyStream(Message m, TSIGRecord old, boolean first) {
        int fudge;
        Message message = m;
        if (first) {
            apply(m, old);
            return;
        }
        Date timeSigned = new Date();
        this.hmac.reset();
        int fudge2 = Options.intValue("tsigfudge");
        if (fudge2 < 0 || fudge2 > 32767) {
            fudge = 300;
        } else {
            fudge = fudge2;
        }
        DNSOutput out = new DNSOutput();
        out.writeU16(old.getSignature().length);
        this.hmac.update(out.toByteArray());
        this.hmac.update(old.getSignature());
        this.hmac.update(m.toWire());
        DNSOutput out2 = new DNSOutput();
        long time = timeSigned.getTime() / 1000;
        int timeHigh = (int) (time >> 32);
        long timeLow = time & 4294967295L;
        out2.writeU16(timeHigh);
        out2.writeU32(timeLow);
        out2.writeU16(fudge);
        this.hmac.update(out2.toByteArray());
        long j = timeLow;
        int i = timeHigh;
        message.addRecord(new TSIGRecord(this.name, 255, 0, this.alg, timeSigned, fudge, this.hmac.doFinal(), m.getHeader().getID(), 0, (byte[]) null), 3);
        message.tsigState = 3;
    }

    public byte verify(Message m, byte[] b, int length, TSIGRecord old) {
        byte b2;
        int minDigestLength;
        Message message = m;
        message.tsigState = 4;
        TSIGRecord tsig = m.getTSIG();
        this.hmac.reset();
        if (tsig == null) {
            return 1;
        }
        if (!tsig.getName().equals(this.name)) {
            b2 = 17;
        } else if (!tsig.getAlgorithm().equals(this.alg)) {
            TSIGRecord tSIGRecord = tsig;
            b2 = 17;
        } else {
            long now = System.currentTimeMillis();
            if (Math.abs(now - tsig.getTimeSigned().getTime()) <= ((long) tsig.getFudge()) * 1000) {
                if (!(old == null || tsig.getError() == 17 || tsig.getError() == 16)) {
                    DNSOutput out = new DNSOutput();
                    out.writeU16(old.getSignature().length);
                    this.hmac.update(out.toByteArray());
                    this.hmac.update(old.getSignature());
                }
                m.getHeader().decCount(3);
                byte[] header = m.getHeader().toWire();
                m.getHeader().incCount(3);
                this.hmac.update(header);
                byte[] bArr = header;
                this.hmac.update(b, header.length, message.tsigstart - header.length);
                DNSOutput out2 = new DNSOutput();
                tsig.getName().toWireCanonical(out2);
                out2.writeU16(tsig.dclass);
                out2.writeU32(tsig.ttl);
                tsig.getAlgorithm().toWireCanonical(out2);
                long time = tsig.getTimeSigned().getTime() / 1000;
                long j = now;
                out2.writeU16((int) (time >> 32));
                out2.writeU32(time & 4294967295L);
                out2.writeU16(tsig.getFudge());
                out2.writeU16(tsig.getError());
                long j2 = time;
                if (tsig.getOther() != null) {
                    out2.writeU16(tsig.getOther().length);
                    out2.writeByteArray(tsig.getOther());
                } else {
                    out2.writeU16(0);
                }
                this.hmac.update(out2.toByteArray());
                byte[] signature = tsig.getSignature();
                int digestLength = this.hmac.getMacLength();
                TSIGRecord tSIGRecord2 = tsig;
                if (this.hmac.getAlgorithm().toLowerCase().contains("md5")) {
                    minDigestLength = 10;
                } else {
                    minDigestLength = digestLength / 2;
                }
                if (signature.length <= digestLength) {
                    int i = digestLength;
                    if (signature.length < minDigestLength) {
                        if (!Options.check("verbose")) {
                            return 16;
                        }
                        System.err.println("BADSIG: signature too short");
                        return 16;
                    } else if (verify(this.hmac, signature, true)) {
                        message.tsigState = 1;
                        return 0;
                    } else if (!Options.check("verbose")) {
                        return 16;
                    } else {
                        System.err.println("BADSIG: signature verification");
                        return 16;
                    }
                } else if (!Options.check("verbose")) {
                    return 16;
                } else {
                    System.err.println("BADSIG: signature too long");
                    return 16;
                }
            } else if (!Options.check("verbose")) {
                return 18;
            } else {
                System.err.println("BADTIME failure");
                return 18;
            }
        }
        if (Options.check("verbose")) {
            System.err.println("BADKEY failure");
        }
        return b2;
    }

    public int verify(Message m, byte[] b, TSIGRecord old) {
        return verify(m, b, b.length, old);
    }

    public int recordLength() {
        return this.name.length() + 10 + this.alg.length() + 8 + 18 + 4 + 8;
    }

    public static class StreamVerifier {
        private TSIG key;
        private TSIGRecord lastTSIG;
        private int lastsigned;
        private int nresponses = 0;
        private Mac verifier;

        public StreamVerifier(TSIG tsig, TSIGRecord old) {
            this.key = tsig;
            this.verifier = tsig.hmac;
            this.lastTSIG = old;
        }

        public int verify(Message m, byte[] b) {
            int len;
            Message message = m;
            byte[] bArr = b;
            TSIGRecord tsig = m.getTSIG();
            int i = this.nresponses + 1;
            this.nresponses = i;
            if (i == 1) {
                int result = this.key.verify(message, bArr, this.lastTSIG);
                if (result == 0) {
                    byte[] signature = tsig.getSignature();
                    DNSOutput out = new DNSOutput();
                    out.writeU16(signature.length);
                    this.verifier.update(out.toByteArray());
                    this.verifier.update(signature);
                }
                this.lastTSIG = tsig;
                return result;
            }
            if (tsig != null) {
                m.getHeader().decCount(3);
            }
            byte[] header = m.getHeader().toWire();
            if (tsig != null) {
                m.getHeader().incCount(3);
            }
            this.verifier.update(header);
            if (tsig == null) {
                len = bArr.length - header.length;
            } else {
                len = message.tsigstart - header.length;
            }
            this.verifier.update(bArr, header.length, len);
            if (tsig != null) {
                this.lastsigned = this.nresponses;
                this.lastTSIG = tsig;
                if (!tsig.getName().equals(this.key.name) || !tsig.getAlgorithm().equals(this.key.alg)) {
                    if (Options.check("verbose")) {
                        System.err.println("BADKEY failure");
                    }
                    message.tsigState = 4;
                    return 17;
                }
                DNSOutput out2 = new DNSOutput();
                long time = tsig.getTimeSigned().getTime() / 1000;
                out2.writeU16((int) (time >> 32));
                out2.writeU32(4294967295L & time);
                out2.writeU16(tsig.getFudge());
                this.verifier.update(out2.toByteArray());
                if (!TSIG.verify(this.verifier, tsig.getSignature())) {
                    if (Options.check("verbose")) {
                        System.err.println("BADSIG failure");
                    }
                    message.tsigState = 4;
                    return 16;
                }
                this.verifier.reset();
                DNSOutput out3 = new DNSOutput();
                out3.writeU16(tsig.getSignature().length);
                this.verifier.update(out3.toByteArray());
                this.verifier.update(tsig.getSignature());
                message.tsigState = 1;
                return 0;
            }
            if (this.nresponses - this.lastsigned >= 100) {
                message.tsigState = 4;
                return 1;
            }
            message.tsigState = 2;
            return 0;
        }
    }
}
