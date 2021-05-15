package com.sun.mail.pop3;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.IllegalWriteException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.SharedInputStream;

public class POP3Message extends MimeMessage {
    static final String UNKNOWN = "UNKNOWN";
    private POP3Folder folder;
    private int hdrSize = -1;
    private int msgSize = -1;
    String uid = "UNKNOWN";

    public POP3Message(Folder folder2, int msgno) throws MessagingException {
        super(folder2, msgno);
        this.folder = (POP3Folder) folder2;
    }

    public void setFlags(Flags newFlags, boolean set) throws MessagingException {
        super.setFlags(newFlags, set);
        if (!this.flags.equals((Flags) this.flags.clone())) {
            this.folder.notifyMessageChangedListeners(1, this);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public int getSize() throws MessagingException {
        try {
            synchronized (this) {
                if (this.msgSize >= 0) {
                    int i = this.msgSize;
                    return i;
                }
                if (this.msgSize < 0) {
                    if (this.headers == null) {
                        loadHeaders();
                    }
                    if (this.contentStream != null) {
                        this.msgSize = this.contentStream.available();
                    } else {
                        this.msgSize = this.folder.getProtocol().list(this.msgnum) - this.hdrSize;
                    }
                }
                int i2 = this.msgSize;
                return i2;
            }
        } catch (EOFException eex) {
            this.folder.close(false);
            throw new FolderClosedException(this.folder, eex.toString());
        } catch (IOException ex) {
            throw new MessagingException("error getting size", ex);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* access modifiers changed from: protected */
    public InputStream getContentStream() throws MessagingException {
        try {
            synchronized (this) {
                if (this.contentStream == null) {
                    InputStream rawcontent = this.folder.getProtocol().retr(this.msgnum, this.msgSize > 0 ? this.msgSize + this.hdrSize : 0);
                    if (rawcontent != null) {
                        if (this.headers != null) {
                            if (!((POP3Store) this.folder.getStore()).forgetTopHeaders) {
                                while (true) {
                                    int len = 0;
                                    while (true) {
                                        int read = rawcontent.read();
                                        int c1 = read;
                                        if (read < 0) {
                                            break;
                                        } else if (c1 == 10) {
                                            break;
                                        } else if (c1 != 13) {
                                            len++;
                                        } else if (rawcontent.available() > 0) {
                                            rawcontent.mark(1);
                                            if (rawcontent.read() != 10) {
                                                rawcontent.reset();
                                            }
                                        }
                                    }
                                    if (rawcontent.available() != 0) {
                                        if (len == 0) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                this.hdrSize = (int) ((SharedInputStream) rawcontent).getPosition();
                                this.contentStream = ((SharedInputStream) rawcontent).newStream((long) this.hdrSize, -1);
                            }
                        }
                        this.headers = new InternetHeaders(rawcontent);
                        this.hdrSize = (int) ((SharedInputStream) rawcontent).getPosition();
                        this.contentStream = ((SharedInputStream) rawcontent).newStream((long) this.hdrSize, -1);
                    } else {
                        this.expunged = true;
                        throw new MessageRemovedException();
                    }
                }
            }
            return super.getContentStream();
        } catch (EOFException eex) {
            this.folder.close(false);
            throw new FolderClosedException(this.folder, eex.toString());
        } catch (IOException ex) {
            throw new MessagingException("error fetching POP3 content", ex);
        }
    }

    public synchronized void invalidate(boolean invalidateHeaders) {
        this.content = null;
        this.contentStream = null;
        this.msgSize = -1;
        if (invalidateHeaders) {
            this.headers = null;
            this.hdrSize = -1;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public InputStream top(int n) throws MessagingException {
        InputStream pVar;
        try {
            synchronized (this) {
                pVar = this.folder.getProtocol().top(this.msgnum, n);
            }
            return pVar;
        } catch (EOFException eex) {
            this.folder.close(false);
            throw new FolderClosedException(this.folder, eex.toString());
        } catch (IOException ex) {
            throw new MessagingException("error getting size", ex);
        }
    }

    public String[] getHeader(String name) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getHeader(name, delimiter);
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public Enumeration getAllHeaders() throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getNonMatchingHeaderLines(names);
    }

    public void saveChanges() throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadHeaders() throws javax.mail.MessagingException {
        /*
            r4 = this;
            r0 = 0
            monitor-enter(r4)     // Catch:{ EOFException -> 0x0048, IOException -> 0x003f }
            javax.mail.internet.InternetHeaders r1 = r4.headers     // Catch:{ all -> 0x003c }
            if (r1 == 0) goto L_0x0008
            monitor-exit(r4)     // Catch:{ all -> 0x003c }
            return
        L_0x0008:
            r1 = 0
            com.sun.mail.pop3.POP3Folder r2 = r4.folder     // Catch:{ all -> 0x003c }
            javax.mail.Store r2 = r2.getStore()     // Catch:{ all -> 0x003c }
            com.sun.mail.pop3.POP3Store r2 = (com.sun.mail.pop3.POP3Store) r2     // Catch:{ all -> 0x003c }
            boolean r2 = r2.disableTop     // Catch:{ all -> 0x003c }
            if (r2 != 0) goto L_0x0033
            com.sun.mail.pop3.POP3Folder r2 = r4.folder     // Catch:{ all -> 0x003c }
            com.sun.mail.pop3.Protocol r2 = r2.getProtocol()     // Catch:{ all -> 0x003c }
            int r3 = r4.msgnum     // Catch:{ all -> 0x003c }
            java.io.InputStream r2 = r2.top(r3, r0)     // Catch:{ all -> 0x003c }
            r1 = r2
            if (r2 != 0) goto L_0x0025
            goto L_0x0033
        L_0x0025:
            int r2 = r1.available()     // Catch:{ all -> 0x003c }
            r4.hdrSize = r2     // Catch:{ all -> 0x003c }
            javax.mail.internet.InternetHeaders r2 = new javax.mail.internet.InternetHeaders     // Catch:{ all -> 0x003c }
            r2.<init>(r1)     // Catch:{ all -> 0x003c }
            r4.headers = r2     // Catch:{ all -> 0x003c }
            goto L_0x003a
        L_0x0033:
            java.io.InputStream r2 = r4.getContentStream()     // Catch:{ all -> 0x003c }
            r2.close()     // Catch:{ all -> 0x003c }
        L_0x003a:
            monitor-exit(r4)     // Catch:{ all -> 0x003c }
            return
        L_0x003c:
            r1 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x003c }
            throw r1     // Catch:{ EOFException -> 0x0048, IOException -> 0x003f }
        L_0x003f:
            r0 = move-exception
            javax.mail.MessagingException r1 = new javax.mail.MessagingException
            java.lang.String r2 = "error loading POP3 headers"
            r1.<init>(r2, r0)
            throw r1
        L_0x0048:
            r1 = move-exception
            com.sun.mail.pop3.POP3Folder r2 = r4.folder
            r2.close(r0)
            javax.mail.FolderClosedException r0 = new javax.mail.FolderClosedException
            com.sun.mail.pop3.POP3Folder r2 = r4.folder
            java.lang.String r3 = r1.toString()
            r0.<init>(r2, r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Message.loadHeaders():void");
    }
}
