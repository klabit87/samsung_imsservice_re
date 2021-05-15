package com.sun.mail.pop3;

import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sun.mail.util.LineInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;

public class POP3Folder extends Folder {
    private boolean doneUidl = false;
    private boolean exists = false;
    private Vector message_cache;
    private String name;
    private boolean opened = false;
    private Protocol port;
    private int size;
    private int total;

    POP3Folder(POP3Store store, String name2) {
        super(store);
        this.name = name2;
        if (name2.equalsIgnoreCase("INBOX")) {
            this.exists = true;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getFullName() {
        return this.name;
    }

    public Folder getParent() {
        return new DefaultFolder((POP3Store) this.store);
    }

    public boolean exists() {
        return this.exists;
    }

    public Folder[] list(String pattern) throws MessagingException {
        throw new MessagingException("not a directory");
    }

    public char getSeparator() {
        return 0;
    }

    public int getType() {
        return 1;
    }

    public boolean create(int type) throws MessagingException {
        return false;
    }

    public boolean hasNewMessages() throws MessagingException {
        return false;
    }

    public Folder getFolder(String name2) throws MessagingException {
        throw new MessagingException("not a directory");
    }

    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException(SoftphoneContract.SoftphoneAddress.DELETE);
    }

    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("renameTo");
    }

    public synchronized void open(int mode) throws MessagingException {
        POP3Store pOP3Store;
        checkClosed();
        if (this.exists) {
            try {
                Protocol port2 = ((POP3Store) this.store).getPort(this);
                this.port = port2;
                Status s = port2.stat();
                this.total = s.total;
                this.size = s.size;
                this.mode = mode;
                this.opened = true;
                Vector vector = new Vector(this.total);
                this.message_cache = vector;
                vector.setSize(this.total);
                this.doneUidl = false;
                notifyConnectionListeners(1);
            } catch (IOException e) {
                this.port = null;
                pOP3Store = (POP3Store) this.store;
            } catch (IOException ioex) {
                if (this.port != null) {
                    this.port.quit();
                }
                this.port = null;
                pOP3Store = (POP3Store) this.store;
            } catch (Throwable th) {
                this.port = null;
                ((POP3Store) this.store).closePort(this);
                throw th;
            }
        } else {
            throw new FolderNotFoundException((Folder) this, "folder is not INBOX");
        }
        return;
        pOP3Store.closePort(this);
        throw new MessagingException("Open failed", ioex);
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        r3 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0015 A[Catch:{ IOException -> 0x0045, all -> 0x0015, IOException -> 0x0017, all -> 0x0015 }, ExcHandler: all (th java.lang.Throwable), Splitter:B:7:0x000f] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void close(boolean r9) throws javax.mail.MessagingException {
        /*
            r8 = this;
            monitor-enter(r8)
            r8.checkOpen()     // Catch:{ all -> 0x008d }
            r0 = 3
            r1 = 0
            r2 = 0
            javax.mail.Store r3 = r8.store     // Catch:{ IOException -> 0x007a, all -> 0x0068 }
            com.sun.mail.pop3.POP3Store r3 = (com.sun.mail.pop3.POP3Store) r3     // Catch:{ IOException -> 0x007a, all -> 0x0068 }
            boolean r3 = r3.rsetBeforeQuit     // Catch:{ IOException -> 0x007a, all -> 0x0068 }
            if (r3 == 0) goto L_0x0019
            com.sun.mail.pop3.Protocol r3 = r8.port     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            r3.rset()     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            goto L_0x0019
        L_0x0015:
            r3 = move-exception
            goto L_0x0069
        L_0x0017:
            r3 = move-exception
            goto L_0x007b
        L_0x0019:
            if (r9 == 0) goto L_0x0052
            int r3 = r8.mode     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            r4 = 2
            if (r3 != r4) goto L_0x0052
            r3 = 0
        L_0x0021:
            java.util.Vector r4 = r8.message_cache     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            int r4 = r4.size()     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            if (r3 < r4) goto L_0x002a
            goto L_0x0052
        L_0x002a:
            java.util.Vector r4 = r8.message_cache     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            java.lang.Object r4 = r4.elementAt(r3)     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            com.sun.mail.pop3.POP3Message r4 = (com.sun.mail.pop3.POP3Message) r4     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            r5 = r4
            if (r4 == 0) goto L_0x004f
            javax.mail.Flags$Flag r4 = javax.mail.Flags.Flag.DELETED     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            boolean r4 = r5.isSet(r4)     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            if (r4 == 0) goto L_0x004f
            com.sun.mail.pop3.Protocol r4 = r8.port     // Catch:{ IOException -> 0x0045, all -> 0x0015 }
            int r6 = r3 + 1
            r4.dele(r6)     // Catch:{ IOException -> 0x0045, all -> 0x0015 }
            goto L_0x004f
        L_0x0045:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            java.lang.String r7 = "Exception deleting messages during close"
            r6.<init>(r7, r4)     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
            throw r6     // Catch:{ IOException -> 0x0017, all -> 0x0015 }
        L_0x004f:
            int r3 = r3 + 1
            goto L_0x0021
        L_0x0052:
            com.sun.mail.pop3.Protocol r3 = r8.port     // Catch:{ IOException -> 0x007a, all -> 0x0068 }
            r3.quit()     // Catch:{ IOException -> 0x007a, all -> 0x0068 }
            r8.port = r2     // Catch:{ all -> 0x008d }
            javax.mail.Store r3 = r8.store     // Catch:{ all -> 0x008d }
            com.sun.mail.pop3.POP3Store r3 = (com.sun.mail.pop3.POP3Store) r3     // Catch:{ all -> 0x008d }
            r3.closePort(r8)     // Catch:{ all -> 0x008d }
            r8.message_cache = r2     // Catch:{ all -> 0x008d }
            r8.opened = r1     // Catch:{ all -> 0x008d }
            r8.notifyConnectionListeners(r0)     // Catch:{ all -> 0x008d }
            goto L_0x008b
        L_0x0068:
            r3 = move-exception
        L_0x0069:
            r8.port = r2     // Catch:{ all -> 0x008d }
            javax.mail.Store r4 = r8.store     // Catch:{ all -> 0x008d }
            com.sun.mail.pop3.POP3Store r4 = (com.sun.mail.pop3.POP3Store) r4     // Catch:{ all -> 0x008d }
            r4.closePort(r8)     // Catch:{ all -> 0x008d }
            r8.message_cache = r2     // Catch:{ all -> 0x008d }
            r8.opened = r1     // Catch:{ all -> 0x008d }
            r8.notifyConnectionListeners(r0)     // Catch:{ all -> 0x008d }
            throw r3     // Catch:{ all -> 0x008d }
        L_0x007a:
            r3 = move-exception
        L_0x007b:
            r8.port = r2     // Catch:{ all -> 0x008d }
            javax.mail.Store r3 = r8.store     // Catch:{ all -> 0x008d }
            com.sun.mail.pop3.POP3Store r3 = (com.sun.mail.pop3.POP3Store) r3     // Catch:{ all -> 0x008d }
            r3.closePort(r8)     // Catch:{ all -> 0x008d }
            r8.message_cache = r2     // Catch:{ all -> 0x008d }
            r8.opened = r1     // Catch:{ all -> 0x008d }
            r8.notifyConnectionListeners(r0)     // Catch:{ all -> 0x008d }
        L_0x008b:
            monitor-exit(r8)
            return
        L_0x008d:
            r9 = move-exception
            monitor-exit(r8)
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Folder.close(boolean):void");
    }

    public boolean isOpen() {
        if (!this.opened) {
            return false;
        }
        if (this.store.isConnected()) {
            return true;
        }
        try {
            close(false);
        } catch (MessagingException e) {
        }
        return false;
    }

    public Flags getPermanentFlags() {
        return new Flags();
    }

    public synchronized int getMessageCount() throws MessagingException {
        if (!this.opened) {
            return -1;
        }
        checkReadable();
        return this.total;
    }

    public synchronized Message getMessage(int msgno) throws MessagingException {
        POP3Message m;
        checkOpen();
        POP3Message pOP3Message = (POP3Message) this.message_cache.elementAt(msgno - 1);
        m = pOP3Message;
        if (pOP3Message == null) {
            m = createMessage(this, msgno);
            this.message_cache.setElementAt(m, msgno - 1);
        }
        return m;
    }

    /* access modifiers changed from: protected */
    public POP3Message createMessage(Folder f, int msgno) throws MessagingException {
        POP3Message m = null;
        Constructor cons = ((POP3Store) this.store).messageConstructor;
        if (cons != null) {
            try {
                m = (POP3Message) cons.newInstance(new Object[]{this, new Integer(msgno)});
            } catch (Exception e) {
            }
        }
        if (m == null) {
            return new POP3Message(this, msgno);
        }
        return m;
    }

    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Append not supported");
    }

    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("Expunge not supported");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0075, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void fetch(javax.mail.Message[] r5, javax.mail.FetchProfile r6) throws javax.mail.MessagingException {
        /*
            r4 = this;
            monitor-enter(r4)
            r4.checkReadable()     // Catch:{ all -> 0x0076 }
            boolean r0 = r4.doneUidl     // Catch:{ all -> 0x0076 }
            if (r0 != 0) goto L_0x0056
            javax.mail.UIDFolder$FetchProfileItem r0 = javax.mail.UIDFolder.FetchProfileItem.UID     // Catch:{ all -> 0x0076 }
            boolean r0 = r6.contains((javax.mail.FetchProfile.Item) r0)     // Catch:{ all -> 0x0076 }
            if (r0 == 0) goto L_0x0056
            java.util.Vector r0 = r4.message_cache     // Catch:{ all -> 0x0076 }
            int r0 = r0.size()     // Catch:{ all -> 0x0076 }
            java.lang.String[] r0 = new java.lang.String[r0]     // Catch:{ all -> 0x0076 }
            com.sun.mail.pop3.Protocol r1 = r4.port     // Catch:{ EOFException -> 0x0047, IOException -> 0x003e }
            boolean r1 = r1.uidl((java.lang.String[]) r0)     // Catch:{ EOFException -> 0x0047, IOException -> 0x003e }
            if (r1 != 0) goto L_0x0022
            monitor-exit(r4)
            return
        L_0x0022:
            r1 = 0
        L_0x0023:
            int r2 = r0.length     // Catch:{ all -> 0x0076 }
            if (r1 < r2) goto L_0x002a
            r1 = 1
            r4.doneUidl = r1     // Catch:{ all -> 0x0076 }
            goto L_0x0056
        L_0x002a:
            r2 = r0[r1]     // Catch:{ all -> 0x0076 }
            if (r2 != 0) goto L_0x002f
            goto L_0x003b
        L_0x002f:
            int r2 = r1 + 1
            javax.mail.Message r2 = r4.getMessage(r2)     // Catch:{ all -> 0x0076 }
            com.sun.mail.pop3.POP3Message r2 = (com.sun.mail.pop3.POP3Message) r2     // Catch:{ all -> 0x0076 }
            r3 = r0[r1]     // Catch:{ all -> 0x0076 }
            r2.uid = r3     // Catch:{ all -> 0x0076 }
        L_0x003b:
            int r1 = r1 + 1
            goto L_0x0023
        L_0x003e:
            r1 = move-exception
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x0076 }
            java.lang.String r3 = "error getting UIDL"
            r2.<init>(r3, r1)     // Catch:{ all -> 0x0076 }
            throw r2     // Catch:{ all -> 0x0076 }
        L_0x0047:
            r1 = move-exception
            r2 = 0
            r4.close(r2)     // Catch:{ all -> 0x0076 }
            javax.mail.FolderClosedException r2 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0076 }
            java.lang.String r3 = r1.toString()     // Catch:{ all -> 0x0076 }
            r2.<init>(r4, r3)     // Catch:{ all -> 0x0076 }
            throw r2     // Catch:{ all -> 0x0076 }
        L_0x0056:
            javax.mail.FetchProfile$Item r0 = javax.mail.FetchProfile.Item.ENVELOPE     // Catch:{ all -> 0x0076 }
            boolean r0 = r6.contains((javax.mail.FetchProfile.Item) r0)     // Catch:{ all -> 0x0076 }
            if (r0 == 0) goto L_0x0074
            r0 = 0
        L_0x005f:
            int r1 = r5.length     // Catch:{ all -> 0x0076 }
            if (r0 < r1) goto L_0x0063
            goto L_0x0074
        L_0x0063:
            r1 = r5[r0]     // Catch:{ MessageRemovedException -> 0x0070 }
            com.sun.mail.pop3.POP3Message r1 = (com.sun.mail.pop3.POP3Message) r1     // Catch:{ MessageRemovedException -> 0x0070 }
            java.lang.String r2 = ""
            r1.getHeader(r2)     // Catch:{ MessageRemovedException -> 0x0070 }
            r1.getSize()     // Catch:{ MessageRemovedException -> 0x0070 }
            goto L_0x0071
        L_0x0070:
            r1 = move-exception
        L_0x0071:
            int r0 = r0 + 1
            goto L_0x005f
        L_0x0074:
            monitor-exit(r4)
            return
        L_0x0076:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Folder.fetch(javax.mail.Message[], javax.mail.FetchProfile):void");
    }

    public synchronized String getUID(Message msg) throws MessagingException {
        POP3Message m;
        checkOpen();
        m = (POP3Message) msg;
        try {
            if (m.uid == "UNKNOWN") {
                m.uid = this.port.uidl(m.getMessageNumber());
            }
        } catch (EOFException eex) {
            close(false);
            throw new FolderClosedException(this, eex.toString());
        } catch (IOException ex) {
            throw new MessagingException("error getting UIDL", ex);
        }
        return m.uid;
    }

    public synchronized int getSize() throws MessagingException {
        checkOpen();
        return this.size;
    }

    public synchronized int[] getSizes() throws MessagingException {
        int[] sizes;
        checkOpen();
        sizes = new int[this.total];
        InputStream is = null;
        LineInputStream lis = null;
        try {
            InputStream is2 = this.port.list();
            LineInputStream lis2 = new LineInputStream(is2);
            while (true) {
                String readLine = lis2.readLine();
                String line = readLine;
                if (readLine == null) {
                    try {
                        break;
                    } catch (IOException e) {
                    }
                } else {
                    try {
                        StringTokenizer st = new StringTokenizer(line);
                        int msgnum = Integer.parseInt(st.nextToken());
                        int size2 = Integer.parseInt(st.nextToken());
                        if (msgnum > 0 && msgnum <= this.total) {
                            sizes[msgnum - 1] = size2;
                        }
                    } catch (Exception e2) {
                    }
                }
            }
            lis2.close();
            if (is2 != null) {
                try {
                    is2.close();
                } catch (IOException e3) {
                }
            }
        } catch (IOException e4) {
            if (lis != null) {
                try {
                    lis.close();
                } catch (IOException e5) {
                }
            }
            if (is != null) {
                is.close();
            }
        } catch (Throwable th) {
            if (lis != null) {
                try {
                    lis.close();
                } catch (IOException e6) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e7) {
                }
            }
            throw th;
        }
        return sizes;
    }

    public synchronized InputStream listCommand() throws MessagingException, IOException {
        checkOpen();
        return this.port.list();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        close(false);
    }

    /* access modifiers changed from: package-private */
    public void checkOpen() throws IllegalStateException {
        if (!this.opened) {
            throw new IllegalStateException("Folder is not Open");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkClosed() throws IllegalStateException {
        if (this.opened) {
            throw new IllegalStateException("Folder is Open");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkReadable() throws IllegalStateException {
        if (!this.opened || !(this.mode == 1 || this.mode == 2)) {
            throw new IllegalStateException("Folder is not Readable");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkWritable() throws IllegalStateException {
        if (!this.opened || this.mode != 2) {
            throw new IllegalStateException("Folder is not Writable");
        }
    }

    /* access modifiers changed from: package-private */
    public Protocol getProtocol() throws MessagingException {
        checkOpen();
        return this.port;
    }

    /* access modifiers changed from: protected */
    public void notifyMessageChangedListeners(int type, Message m) {
        super.notifyMessageChangedListeners(type, m);
    }
}
