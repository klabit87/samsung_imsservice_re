package org.xbill.DNS;

import java.security.PrivateKey;
import java.util.Date;
import org.xbill.DNS.DNSSEC;

public class SIG0 {
    private static final short VALIDITY = 300;

    private SIG0() {
    }

    public static void signMessage(Message message, KEYRecord key, PrivateKey privkey, SIGRecord previous) throws DNSSEC.DNSSECException {
        int validity = Options.intValue("sig0validity");
        if (validity < 0) {
            validity = 300;
        }
        long now = System.currentTimeMillis();
        message.addRecord(DNSSEC.signMessage(message, previous, key, privkey, new Date(now), new Date(((long) (validity * 1000)) + now)), 3);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: org.xbill.DNS.Record[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: org.xbill.DNS.WKSRecord} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object[]} */
    /* JADX WARNING: type inference failed for: r3v6 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void verifyMessage(org.xbill.DNS.Message r5, byte[] r6, org.xbill.DNS.KEYRecord r7, org.xbill.DNS.SIGRecord r8) throws org.xbill.DNS.DNSSEC.DNSSECException {
        /*
            r0 = 0
            r1 = 3
            org.xbill.DNS.Record[] r1 = r5.getSectionArray(r1)
            r2 = 0
        L_0x0007:
            int r3 = r1.length
            if (r2 >= r3) goto L_0x0028
            r3 = r1[r2]
            int r3 = r3.getType()
            r4 = 24
            if (r3 == r4) goto L_0x0015
            goto L_0x0020
        L_0x0015:
            r3 = r1[r2]
            org.xbill.DNS.SIGRecord r3 = (org.xbill.DNS.SIGRecord) r3
            int r3 = r3.getTypeCovered()
            if (r3 == 0) goto L_0x0023
        L_0x0020:
            int r2 = r2 + 1
            goto L_0x0007
        L_0x0023:
            r3 = r1[r2]
            r0 = r3
            org.xbill.DNS.SIGRecord r0 = (org.xbill.DNS.SIGRecord) r0
        L_0x0028:
            org.xbill.DNS.DNSSEC.verifyMessage(r5, r6, r0, r8, r7)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.SIG0.verifyMessage(org.xbill.DNS.Message, byte[], org.xbill.DNS.KEYRecord, org.xbill.DNS.SIGRecord):void");
    }
}
