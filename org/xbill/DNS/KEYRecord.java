package org.xbill.DNS;

import java.io.IOException;
import java.util.StringTokenizer;
import org.xbill.DNS.DNSSEC;

public class KEYRecord extends KEYBase {
    public static final int FLAG_NOAUTH = 32768;
    public static final int FLAG_NOCONF = 16384;
    public static final int FLAG_NOKEY = 49152;
    public static final int OWNER_HOST = 512;
    public static final int OWNER_USER = 0;
    public static final int OWNER_ZONE = 256;
    public static final int PROTOCOL_ANY = 255;
    public static final int PROTOCOL_DNSSEC = 3;
    public static final int PROTOCOL_EMAIL = 2;
    public static final int PROTOCOL_IPSEC = 4;
    public static final int PROTOCOL_TLS = 1;
    private static final long serialVersionUID = 6385613447571488906L;

    public static class Protocol {
        public static final int ANY = 255;
        public static final int DNSSEC = 3;
        public static final int EMAIL = 2;
        public static final int IPSEC = 4;
        public static final int NONE = 0;
        public static final int TLS = 1;
        private static Mnemonic protocols;

        private Protocol() {
        }

        static {
            Mnemonic mnemonic = new Mnemonic("KEY protocol", 2);
            protocols = mnemonic;
            mnemonic.setMaximum(255);
            protocols.setNumericAllowed(true);
            protocols.add(0, "NONE");
            protocols.add(1, "TLS");
            protocols.add(2, "EMAIL");
            protocols.add(3, "DNSSEC");
            protocols.add(4, "IPSEC");
            protocols.add(255, "ANY");
        }

        public static String string(int type) {
            return protocols.getText(type);
        }

        public static int value(String s) {
            return protocols.getValue(s);
        }
    }

    public static class Flags {
        public static final int EXTEND = 4096;
        public static final int FLAG10 = 32;
        public static final int FLAG11 = 16;
        public static final int FLAG2 = 8192;
        public static final int FLAG4 = 2048;
        public static final int FLAG5 = 1024;
        public static final int FLAG8 = 128;
        public static final int FLAG9 = 64;
        public static final int HOST = 512;
        public static final int NOAUTH = 32768;
        public static final int NOCONF = 16384;
        public static final int NOKEY = 49152;
        public static final int NTYP3 = 768;
        public static final int OWNER_MASK = 768;
        public static final int SIG0 = 0;
        public static final int SIG1 = 1;
        public static final int SIG10 = 10;
        public static final int SIG11 = 11;
        public static final int SIG12 = 12;
        public static final int SIG13 = 13;
        public static final int SIG14 = 14;
        public static final int SIG15 = 15;
        public static final int SIG2 = 2;
        public static final int SIG3 = 3;
        public static final int SIG4 = 4;
        public static final int SIG5 = 5;
        public static final int SIG6 = 6;
        public static final int SIG7 = 7;
        public static final int SIG8 = 8;
        public static final int SIG9 = 9;
        public static final int USER = 0;
        public static final int USE_MASK = 49152;
        public static final int ZONE = 256;
        private static Mnemonic flags;

        private Flags() {
        }

        static {
            Mnemonic mnemonic = new Mnemonic("KEY flags", 2);
            flags = mnemonic;
            mnemonic.setMaximum(Message.MAXLENGTH);
            flags.setNumericAllowed(false);
            flags.add(16384, "NOCONF");
            flags.add(32768, "NOAUTH");
            flags.add(49152, "NOKEY");
            flags.add(FLAG2, "FLAG2");
            flags.add(EXTEND, "EXTEND");
            flags.add(FLAG4, "FLAG4");
            flags.add(1024, "FLAG5");
            flags.add(0, "USER");
            flags.add(256, "ZONE");
            flags.add(512, "HOST");
            flags.add(768, "NTYP3");
            flags.add(128, "FLAG8");
            flags.add(64, "FLAG9");
            flags.add(32, "FLAG10");
            flags.add(16, "FLAG11");
            flags.add(0, "SIG0");
            flags.add(1, "SIG1");
            flags.add(2, "SIG2");
            flags.add(3, "SIG3");
            flags.add(4, "SIG4");
            flags.add(5, "SIG5");
            flags.add(6, "SIG6");
            flags.add(7, "SIG7");
            flags.add(8, "SIG8");
            flags.add(9, "SIG9");
            flags.add(10, "SIG10");
            flags.add(11, "SIG11");
            flags.add(12, "SIG12");
            flags.add(13, "SIG13");
            flags.add(14, "SIG14");
            flags.add(15, "SIG15");
        }

        public static int value(String s) {
            try {
                int value = Integer.parseInt(s);
                if (value < 0 || value > 65535) {
                    return -1;
                }
                return value;
            } catch (NumberFormatException e) {
                StringTokenizer st = new StringTokenizer(s, "|");
                int value2 = 0;
                while (st.hasMoreTokens()) {
                    int val = flags.getValue(st.nextToken());
                    if (val < 0) {
                        return -1;
                    }
                    value2 |= val;
                }
                return value2;
            }
        }
    }

    KEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new KEYRecord();
    }

    public KEYRecord(Name name, int dclass, long ttl, int flags, int proto, int alg, byte[] key) {
        super(name, 25, dclass, ttl, flags, proto, alg, key);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public KEYRecord(org.xbill.DNS.Name r13, int r14, long r15, int r17, int r18, int r19, java.security.PublicKey r20) throws org.xbill.DNS.DNSSEC.DNSSECException {
        /*
            r12 = this;
            r0 = r20
            r11 = r19
            byte[] r10 = org.xbill.DNS.DNSSEC.fromPublicKey(r0, r11)
            r3 = 25
            r1 = r12
            r2 = r13
            r4 = r14
            r5 = r15
            r7 = r17
            r8 = r18
            r9 = r19
            r1.<init>(r2, r3, r4, r5, r7, r8, r9, r10)
            r1.publicKey = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.KEYRecord.<init>(org.xbill.DNS.Name, int, long, int, int, int, java.security.PublicKey):void");
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        String flagString = st.getIdentifier();
        this.flags = Flags.value(flagString);
        if (this.flags >= 0) {
            String protoString = st.getIdentifier();
            this.proto = Protocol.value(protoString);
            if (this.proto >= 0) {
                String algString = st.getIdentifier();
                this.alg = DNSSEC.Algorithm.value(algString);
                if (this.alg < 0) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("Invalid algorithm: ");
                    stringBuffer.append(algString);
                    throw st.exception(stringBuffer.toString());
                } else if ((this.flags & 49152) == 49152) {
                    this.key = null;
                } else {
                    this.key = st.getBase64();
                }
            } else {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("Invalid protocol: ");
                stringBuffer2.append(protoString);
                throw st.exception(stringBuffer2.toString());
            }
        } else {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("Invalid flags: ");
            stringBuffer3.append(flagString);
            throw st.exception(stringBuffer3.toString());
        }
    }
}
