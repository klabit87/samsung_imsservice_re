package javax.mail.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import javax.mail.internet.SharedInputStream;
import org.xbill.DNS.KEYRecord;

public class SharedFileInputStream extends BufferedInputStream implements SharedInputStream {
    private static int defaultBufferSize = KEYRecord.Flags.FLAG4;
    protected long bufpos;
    protected int bufsize;
    protected long datalen;
    protected RandomAccessFile in;
    private boolean master;
    private SharedFile sf;
    protected long start;

    static class SharedFile {
        private int cnt;
        private RandomAccessFile in;

        SharedFile(String file) throws IOException {
            this.in = new RandomAccessFile(file, "r");
        }

        SharedFile(File file) throws IOException {
            this.in = new RandomAccessFile(file, "r");
        }

        public RandomAccessFile open() {
            this.cnt++;
            return this.in;
        }

        public synchronized void close() throws IOException {
            if (this.cnt > 0) {
                int i = this.cnt - 1;
                this.cnt = i;
                if (i <= 0) {
                    this.in.close();
                }
            }
        }

        public synchronized void forceClose() throws IOException {
            if (this.cnt > 0) {
                this.cnt = 0;
                this.in.close();
            } else {
                try {
                    this.in.close();
                } catch (IOException e) {
                }
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            super.finalize();
            this.in.close();
        }
    }

    private void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException("Stream closed");
        }
    }

    public SharedFileInputStream(File file) throws IOException {
        this(file, defaultBufferSize);
    }

    public SharedFileInputStream(String file) throws IOException {
        this(file, defaultBufferSize);
    }

    public SharedFileInputStream(File file, int size) throws IOException {
        super((InputStream) null);
        this.start = 0;
        this.master = true;
        if (size > 0) {
            init(new SharedFile(file), size);
            return;
        }
        throw new IllegalArgumentException("Buffer size <= 0");
    }

    public SharedFileInputStream(String file, int size) throws IOException {
        super((InputStream) null);
        this.start = 0;
        this.master = true;
        if (size > 0) {
            init(new SharedFile(file), size);
            return;
        }
        throw new IllegalArgumentException("Buffer size <= 0");
    }

    private void init(SharedFile sf2, int size) throws IOException {
        this.sf = sf2;
        RandomAccessFile open = sf2.open();
        this.in = open;
        this.start = 0;
        this.datalen = open.length();
        this.bufsize = size;
        this.buf = new byte[size];
    }

    private SharedFileInputStream(SharedFile sf2, long start2, long len, int bufsize2) {
        super((InputStream) null);
        this.start = 0;
        this.master = true;
        this.master = false;
        this.sf = sf2;
        this.in = sf2.open();
        this.start = start2;
        this.bufpos = start2;
        this.datalen = len;
        this.bufsize = bufsize2;
        this.buf = new byte[bufsize2];
    }

    private void fill() throws IOException {
        if (this.markpos < 0) {
            this.pos = 0;
            this.bufpos += (long) this.count;
        } else if (this.pos >= this.buf.length) {
            if (this.markpos > 0) {
                int sz = this.pos - this.markpos;
                System.arraycopy(this.buf, this.markpos, this.buf, 0, sz);
                this.pos = sz;
                this.bufpos += (long) this.markpos;
                this.markpos = 0;
            } else if (this.buf.length >= this.marklimit) {
                this.markpos = -1;
                this.pos = 0;
                this.bufpos += (long) this.count;
            } else {
                int nsz = this.pos * 2;
                if (nsz > this.marklimit) {
                    nsz = this.marklimit;
                }
                byte[] nbuf = new byte[nsz];
                System.arraycopy(this.buf, 0, nbuf, 0, this.pos);
                this.buf = nbuf;
            }
        }
        this.count = this.pos;
        this.in.seek(this.bufpos + ((long) this.pos));
        int len = this.buf.length - this.pos;
        long j = (this.bufpos - this.start) + ((long) this.pos) + ((long) len);
        long j2 = this.datalen;
        if (j > j2) {
            len = (int) (j2 - ((this.bufpos - this.start) + ((long) this.pos)));
        }
        int n = this.in.read(this.buf, this.pos, len);
        if (n > 0) {
            this.count = this.pos + n;
        }
    }

    public synchronized int read() throws IOException {
        ensureOpen();
        if (this.pos >= this.count) {
            fill();
            if (this.pos >= this.count) {
                return -1;
            }
        }
        byte[] bArr = this.buf;
        int i = this.pos;
        this.pos = i + 1;
        return bArr[i] & 255;
    }

    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = this.count - this.pos;
        if (avail <= 0) {
            fill();
            avail = this.count - this.pos;
            if (avail <= 0) {
                return -1;
            }
        }
        int cnt = avail < len ? avail : len;
        System.arraycopy(this.buf, this.pos, b, off, cnt);
        this.pos += cnt;
        return cnt;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002b, code lost:
        return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int read(byte[] r4, int r5, int r6) throws java.io.IOException {
        /*
            r3 = this;
            monitor-enter(r3)
            r3.ensureOpen()     // Catch:{ all -> 0x0034 }
            r0 = r5 | r6
            int r1 = r5 + r6
            r0 = r0 | r1
            int r1 = r4.length     // Catch:{ all -> 0x0034 }
            int r2 = r5 + r6
            int r1 = r1 - r2
            r0 = r0 | r1
            if (r0 < 0) goto L_0x002e
            if (r6 != 0) goto L_0x0015
            r0 = 0
            monitor-exit(r3)
            return r0
        L_0x0015:
            int r0 = r3.read1(r4, r5, r6)     // Catch:{ all -> 0x0034 }
            if (r0 > 0) goto L_0x001d
            monitor-exit(r3)
            return r0
        L_0x001d:
            if (r0 < r6) goto L_0x0020
            goto L_0x002a
        L_0x0020:
            int r1 = r5 + r0
            int r2 = r6 - r0
            int r1 = r3.read1(r4, r1, r2)     // Catch:{ all -> 0x0034 }
            if (r1 > 0) goto L_0x002c
        L_0x002a:
            monitor-exit(r3)
            return r0
        L_0x002c:
            int r0 = r0 + r1
            goto L_0x001d
        L_0x002e:
            java.lang.IndexOutOfBoundsException r0 = new java.lang.IndexOutOfBoundsException     // Catch:{ all -> 0x0034 }
            r0.<init>()     // Catch:{ all -> 0x0034 }
            throw r0     // Catch:{ all -> 0x0034 }
        L_0x0034:
            r4 = move-exception
            monitor-exit(r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.util.SharedFileInputStream.read(byte[], int, int):int");
    }

    public synchronized long skip(long n) throws IOException {
        ensureOpen();
        if (n <= 0) {
            return 0;
        }
        long avail = (long) (this.count - this.pos);
        if (avail <= 0) {
            fill();
            avail = (long) (this.count - this.pos);
            if (avail <= 0) {
                return 0;
            }
        }
        long skipped = avail < n ? avail : n;
        this.pos = (int) (((long) this.pos) + skipped);
        return skipped;
    }

    public synchronized int available() throws IOException {
        ensureOpen();
        return (this.count - this.pos) + in_available();
    }

    private int in_available() throws IOException {
        return (int) ((this.start + this.datalen) - (this.bufpos + ((long) this.count)));
    }

    public synchronized void mark(int readlimit) {
        this.marklimit = readlimit;
        this.markpos = this.pos;
    }

    public synchronized void reset() throws IOException {
        ensureOpen();
        if (this.markpos >= 0) {
            this.pos = this.markpos;
        } else {
            throw new IOException("Resetting to invalid mark");
        }
    }

    public boolean markSupported() {
        return true;
    }

    public void close() throws IOException {
        if (this.in != null) {
            try {
                if (this.master) {
                    this.sf.forceClose();
                } else {
                    this.sf.close();
                }
            } finally {
                this.sf = null;
                this.in = null;
                this.buf = null;
            }
        }
    }

    public long getPosition() {
        if (this.in != null) {
            return (this.bufpos + ((long) this.pos)) - this.start;
        }
        throw new RuntimeException("Stream closed");
    }

    public InputStream newStream(long start2, long end) {
        if (this.in == null) {
            throw new RuntimeException("Stream closed");
        } else if (start2 >= 0) {
            if (end == -1) {
                end = this.datalen;
            }
            return new SharedFileInputStream(this.sf, this.start + ((long) ((int) start2)), (long) ((int) (end - start2)), this.bufsize);
        } else {
            throw new IllegalArgumentException("start < 0");
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
