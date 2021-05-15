package org.xbill.DNS;

import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import java.util.HashMap;

public final class Type {
    public static final int A = 1;
    public static final int A6 = 38;
    public static final int AAAA = 28;
    public static final int AFSDB = 18;
    public static final int ANY = 255;
    public static final int APL = 42;
    public static final int ATMA = 34;
    public static final int AXFR = 252;
    public static final int CAA = 257;
    public static final int CERT = 37;
    public static final int CNAME = 5;
    public static final int DHCID = 49;
    public static final int DLV = 32769;
    public static final int DNAME = 39;
    public static final int DNSKEY = 48;
    public static final int DS = 43;
    public static final int EID = 31;
    public static final int GPOS = 27;
    public static final int HINFO = 13;
    public static final int IPSECKEY = 45;
    public static final int ISDN = 20;
    public static final int IXFR = 251;
    public static final int KEY = 25;
    public static final int KX = 36;
    public static final int LOC = 29;
    public static final int MAILA = 254;
    public static final int MAILB = 253;
    public static final int MB = 7;
    public static final int MD = 3;
    public static final int MF = 4;
    public static final int MG = 8;
    public static final int MINFO = 14;
    public static final int MR = 9;
    public static final int MX = 15;
    public static final int NAPTR = 35;
    public static final int NIMLOC = 32;
    public static final int NS = 2;
    public static final int NSAP = 22;
    public static final int NSAP_PTR = 23;
    public static final int NSEC = 47;
    public static final int NSEC3 = 50;
    public static final int NSEC3PARAM = 51;
    public static final int NULL = 10;
    public static final int NXT = 30;
    public static final int OPENPGPKEY = 61;
    public static final int OPT = 41;
    public static final int PTR = 12;
    public static final int PX = 26;
    public static final int RP = 17;
    public static final int RRSIG = 46;
    public static final int RT = 21;
    public static final int SIG = 24;
    public static final int SMIMEA = 53;
    public static final int SOA = 6;
    public static final int SPF = 99;
    public static final int SRV = 33;
    public static final int SSHFP = 44;
    public static final int TKEY = 249;
    public static final int TLSA = 52;
    public static final int TSIG = 250;
    public static final int TXT = 16;
    public static final int URI = 256;
    public static final int WKS = 11;
    public static final int X25 = 19;
    private static TypeMnemonic types;

    private static class TypeMnemonic extends Mnemonic {
        private HashMap objects = new HashMap();

        public TypeMnemonic() {
            super("Type", 2);
            setPrefix("TYPE");
        }

        public void add(int val, String str, Record proto) {
            super.add(val, str);
            this.objects.put(Mnemonic.toInteger(val), proto);
        }

        public void check(int val) {
            Type.check(val);
        }

        public Record getProto(int val) {
            check(val);
            return (Record) this.objects.get(toInteger(val));
        }
    }

    static {
        TypeMnemonic typeMnemonic = new TypeMnemonic();
        types = typeMnemonic;
        typeMnemonic.add(1, "A", new ARecord());
        types.add(2, "NS", new NSRecord());
        types.add(3, "MD", new MDRecord());
        types.add(4, "MF", new MFRecord());
        types.add(5, "CNAME", new CNAMERecord());
        types.add(6, "SOA", new SOARecord());
        types.add(7, "MB", new MBRecord());
        types.add(8, "MG", new MGRecord());
        types.add(9, "MR", new MRRecord());
        types.add(10, "NULL", new NULLRecord());
        types.add(11, "WKS", new WKSRecord());
        types.add(12, "PTR", new PTRRecord());
        types.add(13, "HINFO", new HINFORecord());
        types.add(14, "MINFO", new MINFORecord());
        types.add(15, "MX", new MXRecord());
        types.add(16, "TXT", new TXTRecord());
        types.add(17, "RP", new RPRecord());
        types.add(18, "AFSDB", new AFSDBRecord());
        types.add(19, "X25", new X25Record());
        types.add(20, "ISDN", new ISDNRecord());
        types.add(21, "RT", new RTRecord());
        types.add(22, "NSAP", new NSAPRecord());
        types.add(23, "NSAP-PTR", new NSAP_PTRRecord());
        types.add(24, "SIG", new SIGRecord());
        types.add(25, "KEY", new KEYRecord());
        types.add(26, "PX", new PXRecord());
        types.add(27, "GPOS", new GPOSRecord());
        types.add(28, "AAAA", new AAAARecord());
        types.add(29, "LOC", new LOCRecord());
        types.add(30, "NXT", new NXTRecord());
        types.add(31, "EID");
        types.add(32, "NIMLOC");
        types.add(33, "SRV", new SRVRecord());
        types.add(34, "ATMA");
        types.add(35, "NAPTR", new NAPTRRecord());
        types.add(36, "KX", new KXRecord());
        types.add(37, "CERT", new CERTRecord());
        types.add(38, "A6", new A6Record());
        types.add(39, "DNAME", new DNAMERecord());
        types.add(41, "OPT", new OPTRecord());
        types.add(42, "APL", new APLRecord());
        types.add(43, "DS", new DSRecord());
        types.add(44, "SSHFP", new SSHFPRecord());
        types.add(45, "IPSECKEY", new IPSECKEYRecord());
        types.add(46, "RRSIG", new RRSIGRecord());
        types.add(47, "NSEC", new NSECRecord());
        types.add(48, "DNSKEY", new DNSKEYRecord());
        types.add(49, "DHCID", new DHCIDRecord());
        types.add(50, "NSEC3", new NSEC3Record());
        types.add(51, "NSEC3PARAM", new NSEC3PARAMRecord());
        types.add(52, "TLSA", new TLSARecord());
        types.add(53, "SMIMEA", new SMIMEARecord());
        types.add(61, "OPENPGPKEY", new OPENPGPKEYRecord());
        types.add(99, "SPF", new SPFRecord());
        types.add(TKEY, "TKEY", new TKEYRecord());
        types.add(TSIG, "TSIG", new TSIGRecord());
        types.add(IXFR, "IXFR");
        types.add(252, "AXFR");
        types.add(253, "MAILB");
        types.add(254, "MAILA");
        types.add(255, "ANY");
        types.add(256, Constants.SIG_PROPERTY_URI_NAME, new URIRecord());
        types.add(CAA, "CAA", new CAARecord());
        types.add(DLV, "DLV", new DLVRecord());
    }

    private Type() {
    }

    public static void check(int val) {
        if (val < 0 || val > 65535) {
            throw new InvalidTypeException(val);
        }
    }

    public static String string(int val) {
        return types.getText(val);
    }

    public static int value(String s, boolean numberok) {
        int val = types.getValue(s);
        if (val != -1 || !numberok) {
            return val;
        }
        TypeMnemonic typeMnemonic = types;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("TYPE");
        stringBuffer.append(s);
        return typeMnemonic.getValue(stringBuffer.toString());
    }

    public static int value(String s) {
        return value(s, false);
    }

    static Record getProto(int val) {
        return types.getProto(val);
    }

    public static boolean isRR(int type) {
        if (type == 41) {
            return false;
        }
        switch (type) {
            case TKEY /*249*/:
            case TSIG /*250*/:
            case IXFR /*251*/:
            case 252:
            case 253:
            case 254:
            case 255:
                return false;
            default:
                return true;
        }
    }
}
