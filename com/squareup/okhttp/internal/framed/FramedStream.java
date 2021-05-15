package com.squareup.okhttp.internal.framed;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import okio.AsyncTimeout;
import okio.Buffer;
import okio.BufferedSource;
import okio.Sink;
import okio.Source;
import okio.Timeout;

public final class FramedStream {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    long bytesLeftInWriteWindow;
    /* access modifiers changed from: private */
    public final FramedConnection connection;
    /* access modifiers changed from: private */
    public ErrorCode errorCode = null;
    /* access modifiers changed from: private */
    public final int id;
    /* access modifiers changed from: private */
    public final StreamTimeout readTimeout = new StreamTimeout();
    private final List<Header> requestHeaders;
    private List<Header> responseHeaders;
    final FramedDataSink sink;
    private final FramedDataSource source;
    long unacknowledgedBytesRead = 0;
    /* access modifiers changed from: private */
    public final StreamTimeout writeTimeout = new StreamTimeout();

    FramedStream(int id2, FramedConnection connection2, boolean outFinished, boolean inFinished, List<Header> requestHeaders2) {
        if (connection2 == null) {
            throw new NullPointerException("connection == null");
        } else if (requestHeaders2 != null) {
            this.id = id2;
            this.connection = connection2;
            this.bytesLeftInWriteWindow = (long) connection2.peerSettings.getInitialWindowSize(65536);
            this.source = new FramedDataSource((long) connection2.okHttpSettings.getInitialWindowSize(65536));
            this.sink = new FramedDataSink();
            boolean unused = this.source.finished = inFinished;
            boolean unused2 = this.sink.finished = outFinished;
            this.requestHeaders = requestHeaders2;
        } else {
            throw new NullPointerException("requestHeaders == null");
        }
    }

    public int getId() {
        return this.id;
    }

    /* Debug info: failed to restart local var, previous not found, register: 2 */
    public synchronized boolean isOpen() {
        if (this.errorCode != null) {
            return false;
        }
        if ((this.source.finished || this.source.closed) && ((this.sink.finished || this.sink.closed) && this.responseHeaders != null)) {
            return false;
        }
        return true;
    }

    public boolean isLocallyInitiated() {
        if (this.connection.client == ((this.id & 1) == 1)) {
            return true;
        }
        return false;
    }

    public FramedConnection getConnection() {
        return this.connection;
    }

    public List<Header> getRequestHeaders() {
        return this.requestHeaders;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
        r0 = th;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.util.List<com.squareup.okhttp.internal.framed.Header> getResponseHeaders() throws java.io.IOException {
        /*
            r3 = this;
            monitor-enter(r3)
            com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r0 = r3.readTimeout     // Catch:{ all -> 0x0043 }
            r0.enter()     // Catch:{ all -> 0x0043 }
        L_0x0006:
            java.util.List<com.squareup.okhttp.internal.framed.Header> r0 = r3.responseHeaders     // Catch:{ all -> 0x003c }
            if (r0 != 0) goto L_0x0014
            com.squareup.okhttp.internal.framed.ErrorCode r0 = r3.errorCode     // Catch:{ all -> 0x0012 }
            if (r0 != 0) goto L_0x0014
            r3.waitForIo()     // Catch:{ all -> 0x0012 }
            goto L_0x0006
        L_0x0012:
            r0 = move-exception
            goto L_0x003d
        L_0x0014:
            com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r0 = r3.readTimeout     // Catch:{ all -> 0x0043 }
            r0.exitAndThrowIfTimedOut()     // Catch:{ all -> 0x0043 }
            java.util.List<com.squareup.okhttp.internal.framed.Header> r0 = r3.responseHeaders     // Catch:{ all -> 0x0043 }
            if (r0 == 0) goto L_0x0022
            java.util.List<com.squareup.okhttp.internal.framed.Header> r0 = r3.responseHeaders     // Catch:{ all -> 0x0043 }
            monitor-exit(r3)
            return r0
        L_0x0022:
            java.io.IOException r0 = new java.io.IOException     // Catch:{ all -> 0x0043 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0043 }
            r1.<init>()     // Catch:{ all -> 0x0043 }
            java.lang.String r2 = "stream was reset: "
            r1.append(r2)     // Catch:{ all -> 0x0043 }
            com.squareup.okhttp.internal.framed.ErrorCode r2 = r3.errorCode     // Catch:{ all -> 0x0043 }
            r1.append(r2)     // Catch:{ all -> 0x0043 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0043 }
            r0.<init>(r1)     // Catch:{ all -> 0x0043 }
            throw r0     // Catch:{ all -> 0x0043 }
        L_0x003c:
            r0 = move-exception
        L_0x003d:
            com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r1 = r3.readTimeout     // Catch:{ all -> 0x0043 }
            r1.exitAndThrowIfTimedOut()     // Catch:{ all -> 0x0043 }
            throw r0     // Catch:{ all -> 0x0043 }
        L_0x0043:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedStream.getResponseHeaders():java.util.List");
    }

    public synchronized ErrorCode getErrorCode() {
        return this.errorCode;
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public void reply(List<Header> responseHeaders2, boolean out) throws IOException {
        boolean outFinished = false;
        synchronized (this) {
            if (responseHeaders2 != null) {
                try {
                    if (this.responseHeaders == null) {
                        this.responseHeaders = responseHeaders2;
                        if (!out) {
                            boolean unused = this.sink.finished = true;
                            outFinished = true;
                        }
                    } else {
                        throw new IllegalStateException("reply already sent");
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new NullPointerException("responseHeaders == null");
            }
        }
        this.connection.writeSynReply(this.id, outFinished, responseHeaders2);
        if (outFinished) {
            this.connection.flush();
        }
    }

    public Timeout readTimeout() {
        return this.readTimeout;
    }

    public Timeout writeTimeout() {
        return this.writeTimeout;
    }

    public Source getSource() {
        return this.source;
    }

    /* Debug info: failed to restart local var, previous not found, register: 2 */
    public Sink getSink() {
        synchronized (this) {
            if (this.responseHeaders == null) {
                if (!isLocallyInitiated()) {
                    throw new IllegalStateException("reply before requesting the sink");
                }
            }
        }
        return this.sink;
    }

    public void close(ErrorCode rstStatusCode) throws IOException {
        if (closeInternal(rstStatusCode)) {
            this.connection.writeSynReset(this.id, rstStatusCode);
        }
    }

    public void closeLater(ErrorCode errorCode2) {
        if (closeInternal(errorCode2)) {
            this.connection.writeSynResetLater(this.id, errorCode2);
        }
    }

    private boolean closeInternal(ErrorCode errorCode2) {
        synchronized (this) {
            if (this.errorCode != null) {
                return false;
            }
            if (this.source.finished && this.sink.finished) {
                return false;
            }
            this.errorCode = errorCode2;
            notifyAll();
            this.connection.removeStream(this.id);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void receiveHeaders(List<Header> headers, HeadersMode headersMode) {
        ErrorCode errorCode2 = null;
        boolean open = true;
        synchronized (this) {
            if (this.responseHeaders == null) {
                if (headersMode.failIfHeadersAbsent()) {
                    errorCode2 = ErrorCode.PROTOCOL_ERROR;
                } else {
                    this.responseHeaders = headers;
                    open = isOpen();
                    notifyAll();
                }
            } else if (headersMode.failIfHeadersPresent()) {
                errorCode2 = ErrorCode.STREAM_IN_USE;
            } else {
                List<Header> newHeaders = new ArrayList<>();
                newHeaders.addAll(this.responseHeaders);
                newHeaders.addAll(headers);
                this.responseHeaders = newHeaders;
            }
        }
        if (errorCode2 != null) {
            closeLater(errorCode2);
        } else if (!open) {
            this.connection.removeStream(this.id);
        }
    }

    /* access modifiers changed from: package-private */
    public void receiveData(BufferedSource in, int length) throws IOException {
        this.source.receive(in, (long) length);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0011, code lost:
        if (r0 != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0013, code lost:
        r3.connection.removeStream(r3.id);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void receiveFin() {
        /*
            r3 = this;
            monitor-enter(r3)
            r0 = 0
            com.squareup.okhttp.internal.framed.FramedStream$FramedDataSource r1 = r3.source     // Catch:{ all -> 0x001b }
            r2 = 1
            boolean unused = r1.finished = r2     // Catch:{ all -> 0x001b }
            boolean r0 = r3.isOpen()     // Catch:{ all -> 0x001b }
            r3.notifyAll()     // Catch:{ all -> 0x001e }
            monitor-exit(r3)     // Catch:{ all -> 0x001e }
            if (r0 != 0) goto L_0x001a
            com.squareup.okhttp.internal.framed.FramedConnection r1 = r3.connection
            int r2 = r3.id
            r1.removeStream(r2)
        L_0x001a:
            return
        L_0x001b:
            r1 = move-exception
        L_0x001c:
            monitor-exit(r3)     // Catch:{ all -> 0x001e }
            throw r1
        L_0x001e:
            r1 = move-exception
            goto L_0x001c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedStream.receiveFin():void");
    }

    /* access modifiers changed from: package-private */
    public synchronized void receiveRstStream(ErrorCode errorCode2) {
        if (this.errorCode == null) {
            this.errorCode = errorCode2;
            notifyAll();
        }
    }

    private final class FramedDataSource implements Source {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        /* access modifiers changed from: private */
        public boolean closed;
        /* access modifiers changed from: private */
        public boolean finished;
        private final long maxByteCount;
        private final Buffer readBuffer;
        private final Buffer receiveBuffer;

        static {
            Class<FramedStream> cls = FramedStream.class;
        }

        private FramedDataSource(long maxByteCount2) {
            this.receiveBuffer = new Buffer();
            this.readBuffer = new Buffer();
            this.maxByteCount = maxByteCount2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x006a, code lost:
            r10 = com.squareup.okhttp.internal.framed.FramedStream.access$500(r1.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0070, code lost:
            monitor-enter(r10);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
            com.squareup.okhttp.internal.framed.FramedStream.access$500(r1.this$0).unacknowledgedBytesRead += r7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0095, code lost:
            if (com.squareup.okhttp.internal.framed.FramedStream.access$500(r1.this$0).unacknowledgedBytesRead < ((long) (com.squareup.okhttp.internal.framed.FramedStream.access$500(r1.this$0).okHttpSettings.getInitialWindowSize(65536) / 2))) goto L_0x00b1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0097, code lost:
            com.squareup.okhttp.internal.framed.FramedStream.access$500(r1.this$0).writeWindowUpdateLater(0, com.squareup.okhttp.internal.framed.FramedStream.access$500(r1.this$0).unacknowledgedBytesRead);
            com.squareup.okhttp.internal.framed.FramedStream.access$500(r1.this$0).unacknowledgedBytesRead = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x00b1, code lost:
            monitor-exit(r10);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x00b2, code lost:
            return r7;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long read(okio.Buffer r16, long r17) throws java.io.IOException {
            /*
                r15 = this;
                r1 = r15
                r2 = r17
                r4 = 0
                int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
                if (r0 < 0) goto L_0x00c2
                com.squareup.okhttp.internal.framed.FramedStream r6 = com.squareup.okhttp.internal.framed.FramedStream.this
                monitor-enter(r6)
                r15.waitUntilReadable()     // Catch:{ all -> 0x00bb }
                r15.checkNotClosed()     // Catch:{ all -> 0x00bb }
                okio.Buffer r0 = r1.readBuffer     // Catch:{ all -> 0x00bb }
                long r7 = r0.size()     // Catch:{ all -> 0x00bb }
                int r0 = (r7 > r4 ? 1 : (r7 == r4 ? 0 : -1))
                if (r0 != 0) goto L_0x0020
                r7 = -1
                monitor-exit(r6)     // Catch:{ all -> 0x00bb }
                return r7
            L_0x0020:
                okio.Buffer r0 = r1.readBuffer     // Catch:{ all -> 0x00bb }
                okio.Buffer r7 = r1.readBuffer     // Catch:{ all -> 0x00bb }
                long r7 = r7.size()     // Catch:{ all -> 0x00bb }
                long r7 = java.lang.Math.min(r2, r7)     // Catch:{ all -> 0x00bb }
                r9 = r16
                long r7 = r0.read(r9, r7)     // Catch:{ all -> 0x00b9 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b6 }
                long r10 = r0.unacknowledgedBytesRead     // Catch:{ all -> 0x00b6 }
                long r10 = r10 + r7
                r0.unacknowledgedBytesRead = r10     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b6 }
                long r10 = r0.unacknowledgedBytesRead     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.FramedConnection r0 = r0.connection     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.Settings r0 = r0.okHttpSettings     // Catch:{ all -> 0x00b6 }
                r12 = 65536(0x10000, float:9.18355E-41)
                int r0 = r0.getInitialWindowSize(r12)     // Catch:{ all -> 0x00b6 }
                int r0 = r0 / 2
                long r13 = (long) r0     // Catch:{ all -> 0x00b6 }
                int r0 = (r10 > r13 ? 1 : (r10 == r13 ? 0 : -1))
                if (r0 < 0) goto L_0x0069
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.FramedConnection r0 = r0.connection     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.FramedStream r10 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b6 }
                int r10 = r10.id     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.FramedStream r11 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b6 }
                long r13 = r11.unacknowledgedBytesRead     // Catch:{ all -> 0x00b6 }
                r0.writeWindowUpdateLater(r10, r13)     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b6 }
                r0.unacknowledgedBytesRead = r4     // Catch:{ all -> 0x00b6 }
            L_0x0069:
                monitor-exit(r6)     // Catch:{ all -> 0x00b6 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                com.squareup.okhttp.internal.framed.FramedConnection r10 = r0.connection
                monitor-enter(r10)
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r0 = r0.connection     // Catch:{ all -> 0x00b3 }
                long r13 = r0.unacknowledgedBytesRead     // Catch:{ all -> 0x00b3 }
                long r13 = r13 + r7
                r0.unacknowledgedBytesRead = r13     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r0 = r0.connection     // Catch:{ all -> 0x00b3 }
                long r13 = r0.unacknowledgedBytesRead     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r0 = r0.connection     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.Settings r0 = r0.okHttpSettings     // Catch:{ all -> 0x00b3 }
                int r0 = r0.getInitialWindowSize(r12)     // Catch:{ all -> 0x00b3 }
                int r0 = r0 / 2
                long r11 = (long) r0     // Catch:{ all -> 0x00b3 }
                int r0 = (r13 > r11 ? 1 : (r13 == r11 ? 0 : -1))
                if (r0 < 0) goto L_0x00b1
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r0 = r0.connection     // Catch:{ all -> 0x00b3 }
                r6 = 0
                com.squareup.okhttp.internal.framed.FramedStream r11 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r11 = r11.connection     // Catch:{ all -> 0x00b3 }
                long r11 = r11.unacknowledgedBytesRead     // Catch:{ all -> 0x00b3 }
                r0.writeWindowUpdateLater(r6, r11)     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r0 = r0.connection     // Catch:{ all -> 0x00b3 }
                r0.unacknowledgedBytesRead = r4     // Catch:{ all -> 0x00b3 }
            L_0x00b1:
                monitor-exit(r10)     // Catch:{ all -> 0x00b3 }
                return r7
            L_0x00b3:
                r0 = move-exception
                monitor-exit(r10)     // Catch:{ all -> 0x00b3 }
                throw r0
            L_0x00b6:
                r0 = move-exception
                r4 = r7
                goto L_0x00be
            L_0x00b9:
                r0 = move-exception
                goto L_0x00be
            L_0x00bb:
                r0 = move-exception
                r9 = r16
            L_0x00be:
                monitor-exit(r6)     // Catch:{ all -> 0x00c0 }
                throw r0
            L_0x00c0:
                r0 = move-exception
                goto L_0x00be
            L_0x00c2:
                r9 = r16
                java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                java.lang.String r5 = "byteCount < 0: "
                r4.append(r5)
                r4.append(r2)
                java.lang.String r4 = r4.toString()
                r0.<init>(r4)
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedStream.FramedDataSource.read(okio.Buffer, long):long");
        }

        private void waitUntilReadable() throws IOException {
            FramedStream.this.readTimeout.enter();
            while (this.readBuffer.size() == 0 && !this.finished && !this.closed && FramedStream.this.errorCode == null) {
                try {
                    FramedStream.this.waitForIo();
                } finally {
                    FramedStream.this.readTimeout.exitAndThrowIfTimedOut();
                }
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
            if (r2 == false) goto L_0x002e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0023, code lost:
            r12.skip(r13);
            r11.this$0.closeLater(com.squareup.okhttp.internal.framed.ErrorCode.FLOW_CONTROL_ERROR);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x002e, code lost:
            if (r1 == false) goto L_0x0034;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0030, code lost:
            r12.skip(r13);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0033, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0034, code lost:
            r7 = r12.read(r11.receiveBuffer, r13);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x003e, code lost:
            if (r7 == -1) goto L_0x0068;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0040, code lost:
            r9 = r13 - r7;
            r5 = r11.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0044, code lost:
            monitor-enter(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x004d, code lost:
            if (r11.readBuffer.size() != 0) goto L_0x0050;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0050, code lost:
            r6 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
            r13 = r6;
            r11.readBuffer.writeAll(r11.receiveBuffer);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0059, code lost:
            if (r13 == false) goto L_0x0060;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            r11.this$0.notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
            monitor-exit(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x0061, code lost:
            r13 = r9;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0063, code lost:
            r13 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
            monitor-exit(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0065, code lost:
            throw r13;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x0066, code lost:
            r13 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x006d, code lost:
            throw new java.io.EOFException();
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void receive(okio.BufferedSource r12, long r13) throws java.io.IOException {
            /*
                r11 = this;
                r0 = 0
                r1 = r0
                r2 = r1
            L_0x0004:
                r3 = 0
                int r5 = (r13 > r3 ? 1 : (r13 == r3 ? 0 : -1))
                if (r5 <= 0) goto L_0x0075
                com.squareup.okhttp.internal.framed.FramedStream r5 = com.squareup.okhttp.internal.framed.FramedStream.this
                monitor-enter(r5)
                boolean r1 = r11.finished     // Catch:{ all -> 0x0070 }
                okio.Buffer r6 = r11.readBuffer     // Catch:{ all -> 0x006e }
                long r6 = r6.size()     // Catch:{ all -> 0x006e }
                long r6 = r6 + r13
                long r8 = r11.maxByteCount     // Catch:{ all -> 0x006e }
                int r2 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
                r6 = 1
                if (r2 <= 0) goto L_0x001f
                r2 = r6
                goto L_0x0020
            L_0x001f:
                r2 = r0
            L_0x0020:
                monitor-exit(r5)     // Catch:{ all -> 0x0073 }
                if (r2 == 0) goto L_0x002e
                r12.skip(r13)
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                com.squareup.okhttp.internal.framed.ErrorCode r3 = com.squareup.okhttp.internal.framed.ErrorCode.FLOW_CONTROL_ERROR
                r0.closeLater(r3)
                return
            L_0x002e:
                if (r1 == 0) goto L_0x0034
                r12.skip(r13)
                return
            L_0x0034:
                okio.Buffer r5 = r11.receiveBuffer
                long r7 = r12.read(r5, r13)
                r9 = -1
                int r5 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
                if (r5 == 0) goto L_0x0068
                long r9 = r13 - r7
                com.squareup.okhttp.internal.framed.FramedStream r5 = com.squareup.okhttp.internal.framed.FramedStream.this
                monitor-enter(r5)
                okio.Buffer r13 = r11.readBuffer     // Catch:{ all -> 0x0063 }
                long r13 = r13.size()     // Catch:{ all -> 0x0063 }
                int r13 = (r13 > r3 ? 1 : (r13 == r3 ? 0 : -1))
                if (r13 != 0) goto L_0x0050
                goto L_0x0051
            L_0x0050:
                r6 = r0
            L_0x0051:
                r13 = r6
                okio.Buffer r14 = r11.readBuffer     // Catch:{ all -> 0x0063 }
                okio.Buffer r3 = r11.receiveBuffer     // Catch:{ all -> 0x0063 }
                r14.writeAll(r3)     // Catch:{ all -> 0x0063 }
                if (r13 == 0) goto L_0x0060
                com.squareup.okhttp.internal.framed.FramedStream r14 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x0066 }
                r14.notifyAll()     // Catch:{ all -> 0x0066 }
            L_0x0060:
                monitor-exit(r5)     // Catch:{ all -> 0x0063 }
                r13 = r9
                goto L_0x0004
            L_0x0063:
                r13 = move-exception
            L_0x0064:
                monitor-exit(r5)     // Catch:{ all -> 0x0066 }
                throw r13
            L_0x0066:
                r13 = move-exception
                goto L_0x0064
            L_0x0068:
                java.io.EOFException r0 = new java.io.EOFException
                r0.<init>()
                throw r0
            L_0x006e:
                r0 = move-exception
                goto L_0x0071
            L_0x0070:
                r0 = move-exception
            L_0x0071:
                monitor-exit(r5)     // Catch:{ all -> 0x0073 }
                throw r0
            L_0x0073:
                r0 = move-exception
                goto L_0x0071
            L_0x0075:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedStream.FramedDataSource.receive(okio.BufferedSource, long):void");
        }

        public Timeout timeout() {
            return FramedStream.this.readTimeout;
        }

        public void close() throws IOException {
            synchronized (FramedStream.this) {
                this.closed = true;
                this.readBuffer.clear();
                FramedStream.this.notifyAll();
            }
            FramedStream.this.cancelStreamIfNecessary();
        }

        private void checkNotClosed() throws IOException {
            if (this.closed) {
                throw new IOException("stream closed");
            } else if (FramedStream.this.errorCode != null) {
                throw new IOException("stream was reset: " + FramedStream.this.errorCode);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002c, code lost:
        if (r2 == false) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002e, code lost:
        close(com.squareup.okhttp.internal.framed.ErrorCode.CANCEL);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0034, code lost:
        if (r0 != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0036, code lost:
        r4.connection.removeStream(r4.id);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancelStreamIfNecessary() throws java.io.IOException {
        /*
            r4 = this;
            monitor-enter(r4)
            r0 = 0
            com.squareup.okhttp.internal.framed.FramedStream$FramedDataSource r1 = r4.source     // Catch:{ all -> 0x0040 }
            boolean r1 = r1.finished     // Catch:{ all -> 0x0040 }
            if (r1 != 0) goto L_0x0025
            com.squareup.okhttp.internal.framed.FramedStream$FramedDataSource r1 = r4.source     // Catch:{ all -> 0x0040 }
            boolean r1 = r1.closed     // Catch:{ all -> 0x0040 }
            if (r1 == 0) goto L_0x0025
            com.squareup.okhttp.internal.framed.FramedStream$FramedDataSink r1 = r4.sink     // Catch:{ all -> 0x0040 }
            boolean r1 = r1.finished     // Catch:{ all -> 0x0040 }
            if (r1 != 0) goto L_0x0023
            com.squareup.okhttp.internal.framed.FramedStream$FramedDataSink r1 = r4.sink     // Catch:{ all -> 0x0040 }
            boolean r1 = r1.closed     // Catch:{ all -> 0x0040 }
            if (r1 == 0) goto L_0x0025
        L_0x0023:
            r1 = 1
            goto L_0x0026
        L_0x0025:
            r1 = r0
        L_0x0026:
            r2 = r1
            boolean r0 = r4.isOpen()     // Catch:{ all -> 0x003e }
            monitor-exit(r4)     // Catch:{ all -> 0x0044 }
            if (r2 == 0) goto L_0x0034
            com.squareup.okhttp.internal.framed.ErrorCode r1 = com.squareup.okhttp.internal.framed.ErrorCode.CANCEL
            r4.close(r1)
            goto L_0x003d
        L_0x0034:
            if (r0 != 0) goto L_0x003d
            com.squareup.okhttp.internal.framed.FramedConnection r1 = r4.connection
            int r3 = r4.id
            r1.removeStream(r3)
        L_0x003d:
            return
        L_0x003e:
            r1 = move-exception
            goto L_0x0042
        L_0x0040:
            r1 = move-exception
            r2 = r0
        L_0x0042:
            monitor-exit(r4)     // Catch:{ all -> 0x0044 }
            throw r1
        L_0x0044:
            r1 = move-exception
            goto L_0x0042
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedStream.cancelStreamIfNecessary():void");
    }

    final class FramedDataSink implements Sink {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final long EMIT_BUFFER_SIZE = 16384;
        /* access modifiers changed from: private */
        public boolean closed;
        /* access modifiers changed from: private */
        public boolean finished;
        private final Buffer sendBuffer = new Buffer();

        static {
            Class<FramedStream> cls = FramedStream.class;
        }

        FramedDataSink() {
        }

        public void write(Buffer source, long byteCount) throws IOException {
            this.sendBuffer.write(source, byteCount);
            while (this.sendBuffer.size() >= EMIT_BUFFER_SIZE) {
                emitDataFrame(false);
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 9 */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0051, code lost:
            com.squareup.okhttp.internal.framed.FramedStream.access$1100(r9.this$0).enter();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
            r3 = com.squareup.okhttp.internal.framed.FramedStream.access$500(r9.this$0);
            r4 = com.squareup.okhttp.internal.framed.FramedStream.access$600(r9.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0066, code lost:
            if (r10 == false) goto L_0x0074;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0070, code lost:
            if (r1 != r9.sendBuffer.size()) goto L_0x0074;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0072, code lost:
            r0 = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0074, code lost:
            r0 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0075, code lost:
            r3.writeData(r4, r0, r9.sendBuffer, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0086, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0087, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0088, code lost:
            com.squareup.okhttp.internal.framed.FramedStream.access$1100(r9.this$0).exitAndThrowIfTimedOut();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0091, code lost:
            throw r0;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void emitDataFrame(boolean r10) throws java.io.IOException {
            /*
                r9 = this;
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                monitor-enter(r0)
                r1 = 0
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x009d }
                com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r3 = r3.writeTimeout     // Catch:{ all -> 0x009d }
                r3.enter()     // Catch:{ all -> 0x009d }
            L_0x000e:
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x0092 }
                long r3 = r3.bytesLeftInWriteWindow     // Catch:{ all -> 0x0092 }
                int r3 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1))
                if (r3 > 0) goto L_0x002c
                boolean r3 = r9.finished     // Catch:{ all -> 0x0092 }
                if (r3 != 0) goto L_0x002c
                boolean r3 = r9.closed     // Catch:{ all -> 0x0092 }
                if (r3 != 0) goto L_0x002c
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x0092 }
                com.squareup.okhttp.internal.framed.ErrorCode r3 = r3.errorCode     // Catch:{ all -> 0x0092 }
                if (r3 != 0) goto L_0x002c
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x0092 }
                r3.waitForIo()     // Catch:{ all -> 0x0092 }
                goto L_0x000e
            L_0x002c:
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x009d }
                com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r3 = r3.writeTimeout     // Catch:{ all -> 0x009d }
                r3.exitAndThrowIfTimedOut()     // Catch:{ all -> 0x009d }
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x009d }
                r3.checkOutNotClosed()     // Catch:{ all -> 0x009d }
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x009d }
                long r3 = r3.bytesLeftInWriteWindow     // Catch:{ all -> 0x009d }
                okio.Buffer r5 = r9.sendBuffer     // Catch:{ all -> 0x009d }
                long r5 = r5.size()     // Catch:{ all -> 0x009d }
                long r1 = java.lang.Math.min(r3, r5)     // Catch:{ all -> 0x009d }
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x00a0 }
                long r4 = r3.bytesLeftInWriteWindow     // Catch:{ all -> 0x00a0 }
                long r4 = r4 - r1
                r3.bytesLeftInWriteWindow = r4     // Catch:{ all -> 0x00a0 }
                monitor-exit(r0)     // Catch:{ all -> 0x00a0 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r0 = r0.writeTimeout
                r0.enter()
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x0087 }
                com.squareup.okhttp.internal.framed.FramedConnection r3 = r0.connection     // Catch:{ all -> 0x0087 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x0087 }
                int r4 = r0.id     // Catch:{ all -> 0x0087 }
                if (r10 == 0) goto L_0x0074
                okio.Buffer r0 = r9.sendBuffer     // Catch:{ all -> 0x0087 }
                long r5 = r0.size()     // Catch:{ all -> 0x0087 }
                int r0 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
                if (r0 != 0) goto L_0x0074
                r0 = 1
                goto L_0x0075
            L_0x0074:
                r0 = 0
            L_0x0075:
                r5 = r0
                okio.Buffer r6 = r9.sendBuffer     // Catch:{ all -> 0x0087 }
                r7 = r1
                r3.writeData(r4, r5, r6, r7)     // Catch:{ all -> 0x0087 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r0 = r0.writeTimeout
                r0.exitAndThrowIfTimedOut()
                return
            L_0x0087:
                r0 = move-exception
                com.squareup.okhttp.internal.framed.FramedStream r3 = com.squareup.okhttp.internal.framed.FramedStream.this
                com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r3 = r3.writeTimeout
                r3.exitAndThrowIfTimedOut()
                throw r0
            L_0x0092:
                r3 = move-exception
                com.squareup.okhttp.internal.framed.FramedStream r4 = com.squareup.okhttp.internal.framed.FramedStream.this     // Catch:{ all -> 0x009d }
                com.squareup.okhttp.internal.framed.FramedStream$StreamTimeout r4 = r4.writeTimeout     // Catch:{ all -> 0x009d }
                r4.exitAndThrowIfTimedOut()     // Catch:{ all -> 0x009d }
                throw r3     // Catch:{ all -> 0x009d }
            L_0x009d:
                r3 = move-exception
            L_0x009e:
                monitor-exit(r0)     // Catch:{ all -> 0x00a0 }
                throw r3
            L_0x00a0:
                r3 = move-exception
                goto L_0x009e
            */
            throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedStream.FramedDataSink.emitDataFrame(boolean):void");
        }

        public void flush() throws IOException {
            synchronized (FramedStream.this) {
                FramedStream.this.checkOutNotClosed();
            }
            while (this.sendBuffer.size() > 0) {
                emitDataFrame(false);
                FramedStream.this.connection.flush();
            }
        }

        public Timeout timeout() {
            return FramedStream.this.writeTimeout;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001e, code lost:
            if (r8.sendBuffer.size() <= 0) goto L_0x002e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0028, code lost:
            if (r8.sendBuffer.size() <= 0) goto L_0x0041;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
            emitDataFrame(true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
            com.squareup.okhttp.internal.framed.FramedStream.access$500(r8.this$0).writeData(com.squareup.okhttp.internal.framed.FramedStream.access$600(r8.this$0), true, (okio.Buffer) null, 0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0041, code lost:
            r2 = r8.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0043, code lost:
            monitor-enter(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            r8.closed = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0046, code lost:
            monitor-exit(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0047, code lost:
            com.squareup.okhttp.internal.framed.FramedStream.access$500(r8.this$0).flush();
            com.squareup.okhttp.internal.framed.FramedStream.access$1000(r8.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0055, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
            if (r8.this$0.sink.finished != false) goto L_0x0041;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void close() throws java.io.IOException {
            /*
                r8 = this;
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                monitor-enter(r0)
                boolean r1 = r8.closed     // Catch:{ all -> 0x0059 }
                if (r1 == 0) goto L_0x000a
                monitor-exit(r0)     // Catch:{ all -> 0x0059 }
                return
            L_0x000a:
                monitor-exit(r0)     // Catch:{ all -> 0x0059 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                com.squareup.okhttp.internal.framed.FramedStream$FramedDataSink r0 = r0.sink
                boolean r0 = r0.finished
                r1 = 1
                if (r0 != 0) goto L_0x0041
                okio.Buffer r0 = r8.sendBuffer
                long r2 = r0.size()
                r4 = 0
                int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
                if (r0 <= 0) goto L_0x002e
            L_0x0020:
                okio.Buffer r0 = r8.sendBuffer
                long r2 = r0.size()
                int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
                if (r0 <= 0) goto L_0x0041
                r8.emitDataFrame(r1)
                goto L_0x0020
            L_0x002e:
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                com.squareup.okhttp.internal.framed.FramedConnection r2 = r0.connection
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                int r3 = r0.id
                r4 = 1
                r5 = 0
                r6 = 0
                r2.writeData(r3, r4, r5, r6)
            L_0x0041:
                com.squareup.okhttp.internal.framed.FramedStream r2 = com.squareup.okhttp.internal.framed.FramedStream.this
                monitor-enter(r2)
                r8.closed = r1     // Catch:{ all -> 0x0056 }
                monitor-exit(r2)     // Catch:{ all -> 0x0056 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                com.squareup.okhttp.internal.framed.FramedConnection r0 = r0.connection
                r0.flush()
                com.squareup.okhttp.internal.framed.FramedStream r0 = com.squareup.okhttp.internal.framed.FramedStream.this
                r0.cancelStreamIfNecessary()
                return
            L_0x0056:
                r0 = move-exception
                monitor-exit(r2)     // Catch:{ all -> 0x0056 }
                throw r0
            L_0x0059:
                r1 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0059 }
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedStream.FramedDataSink.close():void");
        }
    }

    /* access modifiers changed from: package-private */
    public void addBytesToWriteWindow(long delta) {
        this.bytesLeftInWriteWindow += delta;
        if (delta > 0) {
            notifyAll();
        }
    }

    /* access modifiers changed from: private */
    public void checkOutNotClosed() throws IOException {
        if (this.sink.closed) {
            throw new IOException("stream closed");
        } else if (this.sink.finished) {
            throw new IOException("stream finished");
        } else if (this.errorCode != null) {
            throw new IOException("stream was reset: " + this.errorCode);
        }
    }

    /* access modifiers changed from: private */
    public void waitForIo() throws InterruptedIOException {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }

    class StreamTimeout extends AsyncTimeout {
        StreamTimeout() {
        }

        /* access modifiers changed from: protected */
        public void timedOut() {
            FramedStream.this.closeLater(ErrorCode.CANCEL);
        }

        /* access modifiers changed from: protected */
        public IOException newTimeoutException(IOException cause) {
            SocketTimeoutException socketTimeoutException = new SocketTimeoutException(EucTestIntent.Extras.TIMEOUT);
            if (cause != null) {
                socketTimeoutException.initCause(cause);
            }
            return socketTimeoutException;
        }

        public void exitAndThrowIfTimedOut() throws IOException {
            if (exit()) {
                throw newTimeoutException((IOException) null);
            }
        }
    }
}
