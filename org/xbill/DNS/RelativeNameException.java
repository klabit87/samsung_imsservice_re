package org.xbill.DNS;

public class RelativeNameException extends IllegalArgumentException {
    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public RelativeNameException(org.xbill.DNS.Name r3) {
        /*
            r2 = this;
            java.lang.StringBuffer r0 = new java.lang.StringBuffer
            r0.<init>()
            java.lang.String r1 = "'"
            r0.append(r1)
            r0.append(r3)
            java.lang.String r1 = "' is not an absolute name"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r2.<init>(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.RelativeNameException.<init>(org.xbill.DNS.Name):void");
    }

    public RelativeNameException(String s) {
        super(s);
    }
}
