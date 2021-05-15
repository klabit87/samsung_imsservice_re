package javax.mail;

public class MessagingException extends Exception {
    private static final long serialVersionUID = -7569192289819959253L;
    private Exception next;

    public MessagingException() {
        initCause((Throwable) null);
    }

    public MessagingException(String s) {
        super(s);
        initCause((Throwable) null);
    }

    public MessagingException(String s, Exception e) {
        super(s);
        this.next = e;
        initCause((Throwable) null);
    }

    public synchronized Exception getNextException() {
        return this.next;
    }

    public synchronized Throwable getCause() {
        return this.next;
    }

    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean setNextException(java.lang.Exception r3) {
        /*
            r2 = this;
            monitor-enter(r2)
            r0 = r2
        L_0x0003:
            boolean r1 = r0 instanceof javax.mail.MessagingException     // Catch:{ all -> 0x0025 }
            if (r1 == 0) goto L_0x0016
            r1 = r0
            javax.mail.MessagingException r1 = (javax.mail.MessagingException) r1     // Catch:{ all -> 0x0025 }
            java.lang.Exception r1 = r1.next     // Catch:{ all -> 0x0025 }
            if (r1 != 0) goto L_0x000f
            goto L_0x0016
        L_0x000f:
            r1 = r0
            javax.mail.MessagingException r1 = (javax.mail.MessagingException) r1     // Catch:{ all -> 0x0025 }
            java.lang.Exception r1 = r1.next     // Catch:{ all -> 0x0025 }
            r0 = r1
            goto L_0x0003
        L_0x0016:
            boolean r1 = r0 instanceof javax.mail.MessagingException     // Catch:{ all -> 0x0025 }
            if (r1 == 0) goto L_0x0022
            r1 = r0
            javax.mail.MessagingException r1 = (javax.mail.MessagingException) r1     // Catch:{ all -> 0x0025 }
            r1.next = r3     // Catch:{ all -> 0x0025 }
            r1 = 1
            monitor-exit(r2)
            return r1
        L_0x0022:
            r1 = 0
            monitor-exit(r2)
            return r1
        L_0x0025:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.MessagingException.setNextException(java.lang.Exception):boolean");
    }

    public synchronized String toString() {
        String s = super.toString();
        Exception n = this.next;
        if (n == null) {
            return s;
        }
        StringBuffer sb = new StringBuffer(s == null ? "" : s);
        while (n != null) {
            sb.append(";\n  nested exception is:\n\t");
            if (n instanceof MessagingException) {
                MessagingException mex = (MessagingException) n;
                sb.append(mex.superToString());
                n = mex.next;
            } else {
                sb.append(n.toString());
                n = null;
            }
        }
        return sb.toString();
    }

    private final String superToString() {
        return super.toString();
    }
}
