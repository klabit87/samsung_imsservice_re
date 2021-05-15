package com.squareup.okhttp.internal;

import com.squareup.okhttp.internal.io.FileSystem;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
import okio.Timeout;

public final class DiskLruCache implements Closeable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final long ANY_SEQUENCE_NUMBER = -1;
    private static final String CLEAN = "CLEAN";
    private static final String DIRTY = "DIRTY";
    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_BACKUP = "journal.bkp";
    static final String JOURNAL_FILE_TEMP = "journal.tmp";
    static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");
    static final String MAGIC = "libcore.io.DiskLruCache";
    /* access modifiers changed from: private */
    public static final Sink NULL_SINK = new Sink() {
        public void write(Buffer source, long byteCount) throws IOException {
            source.skip(byteCount);
        }

        public void flush() throws IOException {
        }

        public Timeout timeout() {
            return Timeout.NONE;
        }

        public void close() throws IOException {
        }
    };
    private static final String READ = "READ";
    private static final String REMOVE = "REMOVE";
    static final String VERSION_1 = "1";
    private final int appVersion;
    private final Runnable cleanupRunnable = new Runnable() {
        /* Debug info: failed to restart local var, previous not found, register: 4 */
        public void run() {
            synchronized (DiskLruCache.this) {
                if (!(!DiskLruCache.this.initialized) && !DiskLruCache.this.closed) {
                    try {
                        DiskLruCache.this.trimToSize();
                        if (DiskLruCache.this.journalRebuildRequired()) {
                            DiskLruCache.this.rebuildJournal();
                            int unused = DiskLruCache.this.redundantOpCount = 0;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean closed;
    /* access modifiers changed from: private */
    public final File directory;
    private final Executor executor;
    /* access modifiers changed from: private */
    public final FileSystem fileSystem;
    /* access modifiers changed from: private */
    public boolean hasJournalErrors;
    /* access modifiers changed from: private */
    public boolean initialized;
    private final File journalFile;
    private final File journalFileBackup;
    private final File journalFileTmp;
    private BufferedSink journalWriter;
    /* access modifiers changed from: private */
    public final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<>(0, 0.75f, true);
    private long maxSize;
    private long nextSequenceNumber = 0;
    /* access modifiers changed from: private */
    public int redundantOpCount;
    private long size = 0;
    /* access modifiers changed from: private */
    public final int valueCount;

    DiskLruCache(FileSystem fileSystem2, File directory2, int appVersion2, int valueCount2, long maxSize2, Executor executor2) {
        this.fileSystem = fileSystem2;
        this.directory = directory2;
        this.appVersion = appVersion2;
        this.journalFile = new File(directory2, JOURNAL_FILE);
        this.journalFileTmp = new File(directory2, JOURNAL_FILE_TEMP);
        this.journalFileBackup = new File(directory2, JOURNAL_FILE_BACKUP);
        this.valueCount = valueCount2;
        this.maxSize = maxSize2;
        this.executor = executor2;
    }

    public synchronized void initialize() throws IOException {
        if (!this.initialized) {
            if (this.fileSystem.exists(this.journalFileBackup)) {
                if (this.fileSystem.exists(this.journalFile)) {
                    this.fileSystem.delete(this.journalFileBackup);
                } else {
                    this.fileSystem.rename(this.journalFileBackup, this.journalFile);
                }
            }
            if (this.fileSystem.exists(this.journalFile)) {
                try {
                    readJournal();
                    processJournal();
                    this.initialized = true;
                    return;
                } catch (IOException journalIsCorrupt) {
                    Platform platform = Platform.get();
                    platform.logW("DiskLruCache " + this.directory + " is corrupt: " + journalIsCorrupt.getMessage() + ", removing");
                    delete();
                    this.closed = false;
                }
            }
            rebuildJournal();
            this.initialized = true;
        }
    }

    public static DiskLruCache create(FileSystem fileSystem2, File directory2, int appVersion2, int valueCount2, long maxSize2) {
        if (maxSize2 <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        } else if (valueCount2 > 0) {
            return new DiskLruCache(fileSystem2, directory2, appVersion2, valueCount2, maxSize2, new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory("OkHttp DiskLruCache", true)));
        } else {
            throw new IllegalArgumentException("valueCount <= 0");
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    private void readJournal() throws IOException {
        int lineCount;
        BufferedSource source = Okio.buffer(this.fileSystem.source(this.journalFile));
        try {
            String magic = source.readUtf8LineStrict();
            String version = source.readUtf8LineStrict();
            String appVersionString = source.readUtf8LineStrict();
            String valueCountString = source.readUtf8LineStrict();
            String blank = source.readUtf8LineStrict();
            if (!MAGIC.equals(magic) || !"1".equals(version) || !Integer.toString(this.appVersion).equals(appVersionString) || !Integer.toString(this.valueCount).equals(valueCountString) || !"".equals(blank)) {
                throw new IOException("unexpected journal header: [" + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
            }
            lineCount = 0;
            while (true) {
                readJournalLine(source.readUtf8LineStrict());
                lineCount++;
            }
        } catch (EOFException e) {
            this.redundantOpCount = lineCount - this.lruEntries.size();
            if (!source.exhausted()) {
                rebuildJournal();
            } else {
                this.journalWriter = newJournalWriter();
            }
            Util.closeQuietly((Closeable) source);
        } catch (Throwable th) {
            Util.closeQuietly((Closeable) source);
            throw th;
        }
    }

    private BufferedSink newJournalWriter() throws FileNotFoundException {
        return Okio.buffer(new FaultHidingSink(this.fileSystem.appendingSink(this.journalFile)) {
            static final /* synthetic */ boolean $assertionsDisabled = false;

            static {
                Class<DiskLruCache> cls = DiskLruCache.class;
            }

            /* access modifiers changed from: protected */
            public void onException(IOException e) {
                boolean unused = DiskLruCache.this.hasJournalErrors = true;
            }
        });
    }

    private void readJournalLine(String line) throws IOException {
        String key;
        int firstSpace = line.indexOf(32);
        if (firstSpace != -1) {
            int keyBegin = firstSpace + 1;
            int secondSpace = line.indexOf(32, keyBegin);
            if (secondSpace == -1) {
                key = line.substring(keyBegin);
                if (firstSpace == REMOVE.length() && line.startsWith(REMOVE)) {
                    this.lruEntries.remove(key);
                    return;
                }
            } else {
                key = line.substring(keyBegin, secondSpace);
            }
            Entry entry = this.lruEntries.get(key);
            if (entry == null) {
                entry = new Entry(key);
                this.lruEntries.put(key, entry);
            }
            if (secondSpace != -1 && firstSpace == CLEAN.length() && line.startsWith(CLEAN)) {
                String[] parts = line.substring(secondSpace + 1).split(" ");
                boolean unused = entry.readable = true;
                Editor unused2 = entry.currentEditor = null;
                entry.setLengths(parts);
            } else if (secondSpace == -1 && firstSpace == DIRTY.length() && line.startsWith(DIRTY)) {
                Editor unused3 = entry.currentEditor = new Editor(entry);
            } else if (secondSpace != -1 || firstSpace != READ.length() || !line.startsWith(READ)) {
                throw new IOException("unexpected journal line: " + line);
            }
        } else {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    private void processJournal() throws IOException {
        this.fileSystem.delete(this.journalFileTmp);
        Iterator<Entry> i = this.lruEntries.values().iterator();
        while (i.hasNext()) {
            Entry entry = i.next();
            if (entry.currentEditor == null) {
                for (int t = 0; t < this.valueCount; t++) {
                    this.size += entry.lengths[t];
                }
            } else {
                Editor unused = entry.currentEditor = null;
                for (int t2 = 0; t2 < this.valueCount; t2++) {
                    this.fileSystem.delete(entry.cleanFiles[t2]);
                    this.fileSystem.delete(entry.dirtyFiles[t2]);
                }
                i.remove();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public synchronized void rebuildJournal() throws IOException {
        if (this.journalWriter != null) {
            this.journalWriter.close();
        }
        BufferedSink writer = Okio.buffer(this.fileSystem.sink(this.journalFileTmp));
        try {
            writer.writeUtf8(MAGIC).writeByte(10);
            writer.writeUtf8("1").writeByte(10);
            writer.writeDecimalLong((long) this.appVersion).writeByte(10);
            writer.writeDecimalLong((long) this.valueCount).writeByte(10);
            writer.writeByte(10);
            for (Entry entry : this.lruEntries.values()) {
                if (entry.currentEditor != null) {
                    writer.writeUtf8(DIRTY).writeByte(32);
                    writer.writeUtf8(entry.key);
                    writer.writeByte(10);
                } else {
                    writer.writeUtf8(CLEAN).writeByte(32);
                    writer.writeUtf8(entry.key);
                    entry.writeLengths(writer);
                    writer.writeByte(10);
                }
            }
            writer.close();
            if (this.fileSystem.exists(this.journalFile)) {
                this.fileSystem.rename(this.journalFile, this.journalFileBackup);
            }
            this.fileSystem.rename(this.journalFileTmp, this.journalFile);
            this.fileSystem.delete(this.journalFileBackup);
            this.journalWriter = newJournalWriter();
            this.hasJournalErrors = false;
        } catch (Throwable th) {
            writer.close();
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004f, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0051, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.squareup.okhttp.internal.DiskLruCache.Snapshot get(java.lang.String r5) throws java.io.IOException {
        /*
            r4 = this;
            monitor-enter(r4)
            r4.initialize()     // Catch:{ all -> 0x0052 }
            r4.checkNotClosed()     // Catch:{ all -> 0x0052 }
            r4.validateKey(r5)     // Catch:{ all -> 0x0052 }
            java.util.LinkedHashMap<java.lang.String, com.squareup.okhttp.internal.DiskLruCache$Entry> r0 = r4.lruEntries     // Catch:{ all -> 0x0052 }
            java.lang.Object r0 = r0.get(r5)     // Catch:{ all -> 0x0052 }
            com.squareup.okhttp.internal.DiskLruCache$Entry r0 = (com.squareup.okhttp.internal.DiskLruCache.Entry) r0     // Catch:{ all -> 0x0052 }
            r1 = 0
            if (r0 == 0) goto L_0x0050
            boolean r2 = r0.readable     // Catch:{ all -> 0x0052 }
            if (r2 != 0) goto L_0x001c
            goto L_0x0050
        L_0x001c:
            com.squareup.okhttp.internal.DiskLruCache$Snapshot r2 = r0.snapshot()     // Catch:{ all -> 0x0052 }
            if (r2 != 0) goto L_0x0024
            monitor-exit(r4)
            return r1
        L_0x0024:
            int r1 = r4.redundantOpCount     // Catch:{ all -> 0x0052 }
            int r1 = r1 + 1
            r4.redundantOpCount = r1     // Catch:{ all -> 0x0052 }
            okio.BufferedSink r1 = r4.journalWriter     // Catch:{ all -> 0x0052 }
            java.lang.String r3 = "READ"
            okio.BufferedSink r1 = r1.writeUtf8(r3)     // Catch:{ all -> 0x0052 }
            r3 = 32
            okio.BufferedSink r1 = r1.writeByte(r3)     // Catch:{ all -> 0x0052 }
            okio.BufferedSink r1 = r1.writeUtf8(r5)     // Catch:{ all -> 0x0052 }
            r3 = 10
            r1.writeByte(r3)     // Catch:{ all -> 0x0052 }
            boolean r1 = r4.journalRebuildRequired()     // Catch:{ all -> 0x0052 }
            if (r1 == 0) goto L_0x004e
            java.util.concurrent.Executor r1 = r4.executor     // Catch:{ all -> 0x0052 }
            java.lang.Runnable r3 = r4.cleanupRunnable     // Catch:{ all -> 0x0052 }
            r1.execute(r3)     // Catch:{ all -> 0x0052 }
        L_0x004e:
            monitor-exit(r4)
            return r2
        L_0x0050:
            monitor-exit(r4)
            return r1
        L_0x0052:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.DiskLruCache.get(java.lang.String):com.squareup.okhttp.internal.DiskLruCache$Snapshot");
    }

    public Editor edit(String key) throws IOException {
        return edit(key, -1);
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0024, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.squareup.okhttp.internal.DiskLruCache.Editor edit(java.lang.String r6, long r7) throws java.io.IOException {
        /*
            r5 = this;
            monitor-enter(r5)
            r5.initialize()     // Catch:{ all -> 0x0068 }
            r5.checkNotClosed()     // Catch:{ all -> 0x0068 }
            r5.validateKey(r6)     // Catch:{ all -> 0x0068 }
            java.util.LinkedHashMap<java.lang.String, com.squareup.okhttp.internal.DiskLruCache$Entry> r0 = r5.lruEntries     // Catch:{ all -> 0x0068 }
            java.lang.Object r0 = r0.get(r6)     // Catch:{ all -> 0x0068 }
            com.squareup.okhttp.internal.DiskLruCache$Entry r0 = (com.squareup.okhttp.internal.DiskLruCache.Entry) r0     // Catch:{ all -> 0x0068 }
            r1 = -1
            int r1 = (r7 > r1 ? 1 : (r7 == r1 ? 0 : -1))
            r2 = 0
            if (r1 == 0) goto L_0x0025
            if (r0 == 0) goto L_0x0023
            long r3 = r0.sequenceNumber     // Catch:{ all -> 0x0068 }
            int r1 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1))
            if (r1 == 0) goto L_0x0025
        L_0x0023:
            monitor-exit(r5)
            return r2
        L_0x0025:
            if (r0 == 0) goto L_0x002f
            com.squareup.okhttp.internal.DiskLruCache$Editor r1 = r0.currentEditor     // Catch:{ all -> 0x0068 }
            if (r1 == 0) goto L_0x002f
            monitor-exit(r5)
            return r2
        L_0x002f:
            okio.BufferedSink r1 = r5.journalWriter     // Catch:{ all -> 0x0068 }
            java.lang.String r3 = "DIRTY"
            okio.BufferedSink r1 = r1.writeUtf8(r3)     // Catch:{ all -> 0x0068 }
            r3 = 32
            okio.BufferedSink r1 = r1.writeByte(r3)     // Catch:{ all -> 0x0068 }
            okio.BufferedSink r1 = r1.writeUtf8(r6)     // Catch:{ all -> 0x0068 }
            r3 = 10
            r1.writeByte(r3)     // Catch:{ all -> 0x0068 }
            okio.BufferedSink r1 = r5.journalWriter     // Catch:{ all -> 0x0068 }
            r1.flush()     // Catch:{ all -> 0x0068 }
            boolean r1 = r5.hasJournalErrors     // Catch:{ all -> 0x0068 }
            if (r1 == 0) goto L_0x0051
            monitor-exit(r5)
            return r2
        L_0x0051:
            if (r0 != 0) goto L_0x005e
            com.squareup.okhttp.internal.DiskLruCache$Entry r1 = new com.squareup.okhttp.internal.DiskLruCache$Entry     // Catch:{ all -> 0x0068 }
            r1.<init>(r6)     // Catch:{ all -> 0x0068 }
            r0 = r1
            java.util.LinkedHashMap<java.lang.String, com.squareup.okhttp.internal.DiskLruCache$Entry> r1 = r5.lruEntries     // Catch:{ all -> 0x0068 }
            r1.put(r6, r0)     // Catch:{ all -> 0x0068 }
        L_0x005e:
            com.squareup.okhttp.internal.DiskLruCache$Editor r1 = new com.squareup.okhttp.internal.DiskLruCache$Editor     // Catch:{ all -> 0x0068 }
            r1.<init>(r0)     // Catch:{ all -> 0x0068 }
            com.squareup.okhttp.internal.DiskLruCache.Editor unused = r0.currentEditor = r1     // Catch:{ all -> 0x0068 }
            monitor-exit(r5)
            return r1
        L_0x0068:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.DiskLruCache.edit(java.lang.String, long):com.squareup.okhttp.internal.DiskLruCache$Editor");
    }

    public File getDirectory() {
        return this.directory;
    }

    public synchronized long getMaxSize() {
        return this.maxSize;
    }

    public synchronized void setMaxSize(long maxSize2) {
        this.maxSize = maxSize2;
        if (this.initialized) {
            this.executor.execute(this.cleanupRunnable);
        }
    }

    public synchronized long size() throws IOException {
        initialize();
        return this.size;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0111, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void completeEdit(com.squareup.okhttp.internal.DiskLruCache.Editor r11, boolean r12) throws java.io.IOException {
        /*
            r10 = this;
            monitor-enter(r10)
            com.squareup.okhttp.internal.DiskLruCache$Entry r0 = r11.entry     // Catch:{ all -> 0x0118 }
            com.squareup.okhttp.internal.DiskLruCache$Editor r1 = r0.currentEditor     // Catch:{ all -> 0x0118 }
            if (r1 != r11) goto L_0x0112
            if (r12 == 0) goto L_0x0050
            boolean r1 = r0.readable     // Catch:{ all -> 0x0118 }
            if (r1 != 0) goto L_0x0050
            r1 = 0
        L_0x0014:
            int r2 = r10.valueCount     // Catch:{ all -> 0x0118 }
            if (r1 >= r2) goto L_0x0050
            boolean[] r2 = r11.written     // Catch:{ all -> 0x0118 }
            boolean r2 = r2[r1]     // Catch:{ all -> 0x0118 }
            if (r2 == 0) goto L_0x0036
            com.squareup.okhttp.internal.io.FileSystem r2 = r10.fileSystem     // Catch:{ all -> 0x0118 }
            java.io.File[] r3 = r0.dirtyFiles     // Catch:{ all -> 0x0118 }
            r3 = r3[r1]     // Catch:{ all -> 0x0118 }
            boolean r2 = r2.exists(r3)     // Catch:{ all -> 0x0118 }
            if (r2 != 0) goto L_0x0033
            r11.abort()     // Catch:{ all -> 0x0118 }
            monitor-exit(r10)
            return
        L_0x0033:
            int r1 = r1 + 1
            goto L_0x0014
        L_0x0036:
            r11.abort()     // Catch:{ all -> 0x0118 }
            java.lang.IllegalStateException r2 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0118 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0118 }
            r3.<init>()     // Catch:{ all -> 0x0118 }
            java.lang.String r4 = "Newly created entry didn't create value for index "
            r3.append(r4)     // Catch:{ all -> 0x0118 }
            r3.append(r1)     // Catch:{ all -> 0x0118 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0118 }
            r2.<init>(r3)     // Catch:{ all -> 0x0118 }
            throw r2     // Catch:{ all -> 0x0118 }
        L_0x0050:
            r1 = 0
        L_0x0051:
            int r2 = r10.valueCount     // Catch:{ all -> 0x0118 }
            if (r1 >= r2) goto L_0x0091
            java.io.File[] r2 = r0.dirtyFiles     // Catch:{ all -> 0x0118 }
            r2 = r2[r1]     // Catch:{ all -> 0x0118 }
            if (r12 == 0) goto L_0x0089
            com.squareup.okhttp.internal.io.FileSystem r3 = r10.fileSystem     // Catch:{ all -> 0x0118 }
            boolean r3 = r3.exists(r2)     // Catch:{ all -> 0x0118 }
            if (r3 == 0) goto L_0x008e
            java.io.File[] r3 = r0.cleanFiles     // Catch:{ all -> 0x0118 }
            r3 = r3[r1]     // Catch:{ all -> 0x0118 }
            com.squareup.okhttp.internal.io.FileSystem r4 = r10.fileSystem     // Catch:{ all -> 0x0118 }
            r4.rename(r2, r3)     // Catch:{ all -> 0x0118 }
            long[] r4 = r0.lengths     // Catch:{ all -> 0x0118 }
            r4 = r4[r1]     // Catch:{ all -> 0x0118 }
            com.squareup.okhttp.internal.io.FileSystem r6 = r10.fileSystem     // Catch:{ all -> 0x0118 }
            long r6 = r6.size(r3)     // Catch:{ all -> 0x0118 }
            long[] r8 = r0.lengths     // Catch:{ all -> 0x0118 }
            r8[r1] = r6     // Catch:{ all -> 0x0118 }
            long r8 = r10.size     // Catch:{ all -> 0x0118 }
            long r8 = r8 - r4
            long r8 = r8 + r6
            r10.size = r8     // Catch:{ all -> 0x0118 }
            goto L_0x008e
        L_0x0089:
            com.squareup.okhttp.internal.io.FileSystem r3 = r10.fileSystem     // Catch:{ all -> 0x0118 }
            r3.delete(r2)     // Catch:{ all -> 0x0118 }
        L_0x008e:
            int r1 = r1 + 1
            goto L_0x0051
        L_0x0091:
            int r1 = r10.redundantOpCount     // Catch:{ all -> 0x0118 }
            r2 = 1
            int r1 = r1 + r2
            r10.redundantOpCount = r1     // Catch:{ all -> 0x0118 }
            r1 = 0
            com.squareup.okhttp.internal.DiskLruCache.Editor unused = r0.currentEditor = r1     // Catch:{ all -> 0x0118 }
            boolean r1 = r0.readable     // Catch:{ all -> 0x0118 }
            r1 = r1 | r12
            r3 = 10
            r4 = 32
            if (r1 == 0) goto L_0x00d4
            boolean unused = r0.readable = r2     // Catch:{ all -> 0x0118 }
            okio.BufferedSink r1 = r10.journalWriter     // Catch:{ all -> 0x0118 }
            java.lang.String r2 = "CLEAN"
            okio.BufferedSink r1 = r1.writeUtf8(r2)     // Catch:{ all -> 0x0118 }
            r1.writeByte(r4)     // Catch:{ all -> 0x0118 }
            okio.BufferedSink r1 = r10.journalWriter     // Catch:{ all -> 0x0118 }
            java.lang.String r2 = r0.key     // Catch:{ all -> 0x0118 }
            r1.writeUtf8(r2)     // Catch:{ all -> 0x0118 }
            okio.BufferedSink r1 = r10.journalWriter     // Catch:{ all -> 0x0118 }
            r0.writeLengths(r1)     // Catch:{ all -> 0x0118 }
            okio.BufferedSink r1 = r10.journalWriter     // Catch:{ all -> 0x0118 }
            r1.writeByte(r3)     // Catch:{ all -> 0x0118 }
            if (r12 == 0) goto L_0x00f6
            long r1 = r10.nextSequenceNumber     // Catch:{ all -> 0x0118 }
            r3 = 1
            long r3 = r3 + r1
            r10.nextSequenceNumber = r3     // Catch:{ all -> 0x0118 }
            long unused = r0.sequenceNumber = r1     // Catch:{ all -> 0x0118 }
            goto L_0x00f6
        L_0x00d4:
            java.util.LinkedHashMap<java.lang.String, com.squareup.okhttp.internal.DiskLruCache$Entry> r1 = r10.lruEntries     // Catch:{ all -> 0x0118 }
            java.lang.String r2 = r0.key     // Catch:{ all -> 0x0118 }
            r1.remove(r2)     // Catch:{ all -> 0x0118 }
            okio.BufferedSink r1 = r10.journalWriter     // Catch:{ all -> 0x0118 }
            java.lang.String r2 = "REMOVE"
            okio.BufferedSink r1 = r1.writeUtf8(r2)     // Catch:{ all -> 0x0118 }
            r1.writeByte(r4)     // Catch:{ all -> 0x0118 }
            okio.BufferedSink r1 = r10.journalWriter     // Catch:{ all -> 0x0118 }
            java.lang.String r2 = r0.key     // Catch:{ all -> 0x0118 }
            r1.writeUtf8(r2)     // Catch:{ all -> 0x0118 }
            okio.BufferedSink r1 = r10.journalWriter     // Catch:{ all -> 0x0118 }
            r1.writeByte(r3)     // Catch:{ all -> 0x0118 }
        L_0x00f6:
            okio.BufferedSink r1 = r10.journalWriter     // Catch:{ all -> 0x0118 }
            r1.flush()     // Catch:{ all -> 0x0118 }
            long r1 = r10.size     // Catch:{ all -> 0x0118 }
            long r3 = r10.maxSize     // Catch:{ all -> 0x0118 }
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 > 0) goto L_0x0109
            boolean r1 = r10.journalRebuildRequired()     // Catch:{ all -> 0x0118 }
            if (r1 == 0) goto L_0x0110
        L_0x0109:
            java.util.concurrent.Executor r1 = r10.executor     // Catch:{ all -> 0x0118 }
            java.lang.Runnable r2 = r10.cleanupRunnable     // Catch:{ all -> 0x0118 }
            r1.execute(r2)     // Catch:{ all -> 0x0118 }
        L_0x0110:
            monitor-exit(r10)
            return
        L_0x0112:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0118 }
            r1.<init>()     // Catch:{ all -> 0x0118 }
            throw r1     // Catch:{ all -> 0x0118 }
        L_0x0118:
            r11 = move-exception
            monitor-exit(r10)
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.DiskLruCache.completeEdit(com.squareup.okhttp.internal.DiskLruCache$Editor, boolean):void");
    }

    /* access modifiers changed from: private */
    public boolean journalRebuildRequired() {
        int i = this.redundantOpCount;
        return i >= 2000 && i >= this.lruEntries.size();
    }

    public synchronized boolean remove(String key) throws IOException {
        initialize();
        checkNotClosed();
        validateKey(key);
        Entry entry = this.lruEntries.get(key);
        if (entry == null) {
            return false;
        }
        return removeEntry(entry);
    }

    /* access modifiers changed from: private */
    public boolean removeEntry(Entry entry) throws IOException {
        if (entry.currentEditor != null) {
            boolean unused = entry.currentEditor.hasErrors = true;
        }
        for (int i = 0; i < this.valueCount; i++) {
            this.fileSystem.delete(entry.cleanFiles[i]);
            this.size -= entry.lengths[i];
            entry.lengths[i] = 0;
        }
        this.redundantOpCount++;
        this.journalWriter.writeUtf8(REMOVE).writeByte(32).writeUtf8(entry.key).writeByte(10);
        this.lruEntries.remove(entry.key);
        if (journalRebuildRequired()) {
            this.executor.execute(this.cleanupRunnable);
        }
        return true;
    }

    public synchronized boolean isClosed() {
        return this.closed;
    }

    private synchronized void checkNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException("cache is closed");
        }
    }

    public synchronized void flush() throws IOException {
        if (this.initialized) {
            checkNotClosed();
            trimToSize();
            this.journalWriter.flush();
        }
    }

    public synchronized void close() throws IOException {
        if (this.initialized) {
            if (!this.closed) {
                for (Entry entry : (Entry[]) this.lruEntries.values().toArray(new Entry[this.lruEntries.size()])) {
                    if (entry.currentEditor != null) {
                        entry.currentEditor.abort();
                    }
                }
                trimToSize();
                this.journalWriter.close();
                this.journalWriter = null;
                this.closed = true;
                return;
            }
        }
        this.closed = true;
    }

    /* access modifiers changed from: private */
    public void trimToSize() throws IOException {
        while (this.size > this.maxSize) {
            removeEntry(this.lruEntries.values().iterator().next());
        }
    }

    public void delete() throws IOException {
        close();
        this.fileSystem.deleteContents(this.directory);
    }

    public synchronized void evictAll() throws IOException {
        initialize();
        for (Entry entry : (Entry[]) this.lruEntries.values().toArray(new Entry[this.lruEntries.size()])) {
            removeEntry(entry);
        }
    }

    private void validateKey(String key) {
        if (!LEGAL_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,120}: \"" + key + "\"");
        }
    }

    public synchronized Iterator<Snapshot> snapshots() throws IOException {
        initialize();
        return new Iterator<Snapshot>() {
            final Iterator<Entry> delegate = new ArrayList(DiskLruCache.this.lruEntries.values()).iterator();
            Snapshot nextSnapshot;
            Snapshot removeSnapshot;

            public boolean hasNext() {
                if (this.nextSnapshot != null) {
                    return true;
                }
                synchronized (DiskLruCache.this) {
                    if (DiskLruCache.this.closed) {
                        return false;
                    }
                    while (this.delegate.hasNext()) {
                        Snapshot snapshot = this.delegate.next().snapshot();
                        if (snapshot != null) {
                            this.nextSnapshot = snapshot;
                            return true;
                        }
                    }
                    return false;
                }
            }

            public Snapshot next() {
                if (hasNext()) {
                    Snapshot snapshot = this.nextSnapshot;
                    this.removeSnapshot = snapshot;
                    this.nextSnapshot = null;
                    return snapshot;
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                Snapshot snapshot = this.removeSnapshot;
                if (snapshot != null) {
                    try {
                        DiskLruCache.this.remove(snapshot.key);
                    } catch (IOException e) {
                    } catch (Throwable th) {
                        this.removeSnapshot = null;
                        throw th;
                    }
                    this.removeSnapshot = null;
                    return;
                }
                throw new IllegalStateException("remove() before next()");
            }
        };
    }

    public final class Snapshot implements Closeable {
        /* access modifiers changed from: private */
        public final String key;
        private final long[] lengths;
        private final long sequenceNumber;
        private final Source[] sources;

        private Snapshot(String key2, long sequenceNumber2, Source[] sources2, long[] lengths2) {
            this.key = key2;
            this.sequenceNumber = sequenceNumber2;
            this.sources = sources2;
            this.lengths = lengths2;
        }

        public String key() {
            return this.key;
        }

        public Editor edit() throws IOException {
            return DiskLruCache.this.edit(this.key, this.sequenceNumber);
        }

        public Source getSource(int index) {
            return this.sources[index];
        }

        public long getLength(int index) {
            return this.lengths[index];
        }

        public void close() {
            for (Source in : this.sources) {
                Util.closeQuietly((Closeable) in);
            }
        }
    }

    public final class Editor {
        private boolean committed;
        /* access modifiers changed from: private */
        public final Entry entry;
        /* access modifiers changed from: private */
        public boolean hasErrors;
        /* access modifiers changed from: private */
        public final boolean[] written;

        private Editor(Entry entry2) {
            this.entry = entry2;
            this.written = entry2.readable ? null : new boolean[DiskLruCache.this.valueCount];
        }

        /* Debug info: failed to restart local var, previous not found, register: 4 */
        public Source newSource(int index) throws IOException {
            synchronized (DiskLruCache.this) {
                if (this.entry.currentEditor != this) {
                    throw new IllegalStateException();
                } else if (!this.entry.readable) {
                    return null;
                } else {
                    try {
                        Source source = DiskLruCache.this.fileSystem.source(this.entry.cleanFiles[index]);
                        return source;
                    } catch (FileNotFoundException e) {
                        return null;
                    }
                }
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 4 */
        public Sink newSink(int index) throws IOException {
            AnonymousClass1 r3;
            synchronized (DiskLruCache.this) {
                if (this.entry.currentEditor == this) {
                    if (!this.entry.readable) {
                        this.written[index] = true;
                    }
                    try {
                        r3 = new FaultHidingSink(DiskLruCache.this.fileSystem.sink(this.entry.dirtyFiles[index])) {
                            /* access modifiers changed from: protected */
                            public void onException(IOException e) {
                                synchronized (DiskLruCache.this) {
                                    boolean unused = Editor.this.hasErrors = true;
                                }
                            }
                        };
                    } catch (FileNotFoundException e) {
                        return DiskLruCache.NULL_SINK;
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
            return r3;
        }

        public void commit() throws IOException {
            synchronized (DiskLruCache.this) {
                if (this.hasErrors) {
                    DiskLruCache.this.completeEdit(this, false);
                    boolean unused = DiskLruCache.this.removeEntry(this.entry);
                } else {
                    DiskLruCache.this.completeEdit(this, true);
                }
                this.committed = true;
            }
        }

        public void abort() throws IOException {
            synchronized (DiskLruCache.this) {
                DiskLruCache.this.completeEdit(this, false);
            }
        }

        public void abortUnlessCommitted() {
            synchronized (DiskLruCache.this) {
                if (!this.committed) {
                    try {
                        DiskLruCache.this.completeEdit(this, false);
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private final class Entry {
        /* access modifiers changed from: private */
        public final File[] cleanFiles;
        /* access modifiers changed from: private */
        public Editor currentEditor;
        /* access modifiers changed from: private */
        public final File[] dirtyFiles;
        /* access modifiers changed from: private */
        public final String key;
        /* access modifiers changed from: private */
        public final long[] lengths;
        /* access modifiers changed from: private */
        public boolean readable;
        /* access modifiers changed from: private */
        public long sequenceNumber;

        private Entry(String key2) {
            this.key = key2;
            this.lengths = new long[DiskLruCache.this.valueCount];
            this.cleanFiles = new File[DiskLruCache.this.valueCount];
            this.dirtyFiles = new File[DiskLruCache.this.valueCount];
            StringBuilder fileBuilder = new StringBuilder(key2).append('.');
            int truncateTo = fileBuilder.length();
            for (int i = 0; i < DiskLruCache.this.valueCount; i++) {
                fileBuilder.append(i);
                this.cleanFiles[i] = new File(DiskLruCache.this.directory, fileBuilder.toString());
                fileBuilder.append(".tmp");
                this.dirtyFiles[i] = new File(DiskLruCache.this.directory, fileBuilder.toString());
                fileBuilder.setLength(truncateTo);
            }
        }

        /* access modifiers changed from: private */
        public void setLengths(String[] strings) throws IOException {
            if (strings.length == DiskLruCache.this.valueCount) {
                int i = 0;
                while (i < strings.length) {
                    try {
                        this.lengths[i] = Long.parseLong(strings[i]);
                        i++;
                    } catch (NumberFormatException e) {
                        throw invalidLengths(strings);
                    }
                }
                return;
            }
            throw invalidLengths(strings);
        }

        /* access modifiers changed from: package-private */
        public void writeLengths(BufferedSink writer) throws IOException {
            for (long length : this.lengths) {
                writer.writeByte(32).writeDecimalLong(length);
            }
        }

        private IOException invalidLengths(String[] strings) throws IOException {
            throw new IOException("unexpected journal line: " + Arrays.toString(strings));
        }

        /* access modifiers changed from: package-private */
        public Snapshot snapshot() {
            if (Thread.holdsLock(DiskLruCache.this)) {
                Source[] sources = new Source[DiskLruCache.this.valueCount];
                long[] lengths2 = (long[]) this.lengths.clone();
                int i = 0;
                while (i < DiskLruCache.this.valueCount) {
                    try {
                        sources[i] = DiskLruCache.this.fileSystem.source(this.cleanFiles[i]);
                        i++;
                    } catch (FileNotFoundException e) {
                        int i2 = 0;
                        while (i2 < DiskLruCache.this.valueCount && sources[i2] != null) {
                            Util.closeQuietly((Closeable) sources[i2]);
                            i2++;
                        }
                        return null;
                    }
                }
                return new Snapshot(this.key, this.sequenceNumber, sources, lengths2);
            }
            throw new AssertionError();
        }
    }
}
