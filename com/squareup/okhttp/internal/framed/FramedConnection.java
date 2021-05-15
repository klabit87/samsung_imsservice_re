package com.squareup.okhttp.internal.framed;

import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.internal.NamedRunnable;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.framed.FrameReader;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import org.xbill.DNS.Message;

public final class FramedConnection implements Closeable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int OKHTTP_CLIENT_WINDOW_SIZE = 16777216;
    /* access modifiers changed from: private */
    public static final ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp FramedConnection", true));
    long bytesLeftInWriteWindow;
    final boolean client;
    /* access modifiers changed from: private */
    public final Set<Integer> currentPushRequests;
    final FrameWriter frameWriter;
    /* access modifiers changed from: private */
    public final String hostName;
    private long idleStartTimeNs;
    /* access modifiers changed from: private */
    public int lastGoodStreamId;
    /* access modifiers changed from: private */
    public final Listener listener;
    private int nextPingId;
    /* access modifiers changed from: private */
    public int nextStreamId;
    Settings okHttpSettings;
    final Settings peerSettings;
    private Map<Integer, Ping> pings;
    final Protocol protocol;
    private final ExecutorService pushExecutor;
    /* access modifiers changed from: private */
    public final PushObserver pushObserver;
    final Reader readerRunnable;
    /* access modifiers changed from: private */
    public boolean receivedInitialPeerSettings;
    /* access modifiers changed from: private */
    public boolean shutdown;
    final Socket socket;
    /* access modifiers changed from: private */
    public final Map<Integer, FramedStream> streams;
    long unacknowledgedBytesRead;
    final Variant variant;

    private FramedConnection(Builder builder) throws IOException {
        this.streams = new HashMap();
        this.idleStartTimeNs = System.nanoTime();
        this.unacknowledgedBytesRead = 0;
        this.okHttpSettings = new Settings();
        this.peerSettings = new Settings();
        this.receivedInitialPeerSettings = false;
        this.currentPushRequests = new LinkedHashSet();
        this.protocol = builder.protocol;
        this.pushObserver = builder.pushObserver;
        this.client = builder.client;
        this.listener = builder.listener;
        int i = 2;
        this.nextStreamId = builder.client ? 1 : 2;
        if (builder.client && this.protocol == Protocol.HTTP_2) {
            this.nextStreamId += 2;
        }
        this.nextPingId = builder.client ? 1 : i;
        if (builder.client) {
            this.okHttpSettings.set(7, 0, OKHTTP_CLIENT_WINDOW_SIZE);
        }
        this.hostName = builder.hostName;
        if (this.protocol == Protocol.HTTP_2) {
            this.variant = new Http2();
            this.pushExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory(String.format("OkHttp %s Push Observer", new Object[]{this.hostName}), true));
            this.peerSettings.set(7, 0, Message.MAXLENGTH);
            this.peerSettings.set(5, 0, 16384);
        } else if (this.protocol == Protocol.SPDY_3) {
            this.variant = new Spdy3();
            this.pushExecutor = null;
        } else {
            throw new AssertionError(this.protocol);
        }
        this.bytesLeftInWriteWindow = (long) this.peerSettings.getInitialWindowSize(65536);
        this.socket = builder.socket;
        this.frameWriter = this.variant.newWriter(builder.sink, this.client);
        this.readerRunnable = new Reader(this.variant.newReader(builder.source, this.client));
        new Thread(this.readerRunnable).start();
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public synchronized int openStreamCount() {
        return this.streams.size();
    }

    /* access modifiers changed from: package-private */
    public synchronized FramedStream getStream(int id) {
        return this.streams.get(Integer.valueOf(id));
    }

    /* access modifiers changed from: package-private */
    public synchronized FramedStream removeStream(int streamId) {
        FramedStream stream;
        stream = this.streams.remove(Integer.valueOf(streamId));
        if (stream != null && this.streams.isEmpty()) {
            setIdle(true);
        }
        notifyAll();
        return stream;
    }

    private synchronized void setIdle(boolean value) {
        long j;
        if (value) {
            try {
                j = System.nanoTime();
            } catch (Throwable th) {
                throw th;
            }
        } else {
            j = Long.MAX_VALUE;
        }
        this.idleStartTimeNs = j;
    }

    public synchronized boolean isIdle() {
        return this.idleStartTimeNs != Long.MAX_VALUE;
    }

    public synchronized int maxConcurrentStreams() {
        return this.peerSettings.getMaxConcurrentStreams(Integer.MAX_VALUE);
    }

    public synchronized long getIdleStartTimeNs() {
        return this.idleStartTimeNs;
    }

    public FramedStream pushStream(int associatedStreamId, List<Header> requestHeaders, boolean out) throws IOException {
        if (this.client) {
            throw new IllegalStateException("Client cannot push requests.");
        } else if (this.protocol == Protocol.HTTP_2) {
            return newStream(associatedStreamId, requestHeaders, out, false);
        } else {
            throw new IllegalStateException("protocol != HTTP_2");
        }
    }

    public FramedStream newStream(List<Header> requestHeaders, boolean out, boolean in) throws IOException {
        return newStream(0, requestHeaders, out, in);
    }

    /* Debug info: failed to restart local var, previous not found, register: 16 */
    /* JADX WARNING: type inference failed for: r2v9, types: [java.lang.Object, java.lang.Integer] */
    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    /* JADX WARNING: Multi-variable type inference failed */
    private com.squareup.okhttp.internal.framed.FramedStream newStream(int r17, java.util.List<com.squareup.okhttp.internal.framed.Header> r18, boolean r19, boolean r20) throws java.io.IOException {
        /*
            r16 = this;
            r7 = r16
            r14 = r17
            r4 = r19 ^ 1
            r5 = r20 ^ 1
            com.squareup.okhttp.internal.framed.FrameWriter r15 = r7.frameWriter
            monitor-enter(r15)
            r8 = 0
            r9 = 0
            monitor-enter(r16)     // Catch:{ all -> 0x00a0 }
            boolean r0 = r7.shutdown     // Catch:{ all -> 0x0097 }
            if (r0 != 0) goto L_0x0088
            int r0 = r7.nextStreamId     // Catch:{ all -> 0x0097 }
            r13 = r0
            int r0 = r7.nextStreamId     // Catch:{ all -> 0x0082 }
            int r0 = r0 + 2
            r7.nextStreamId = r0     // Catch:{ all -> 0x0082 }
            com.squareup.okhttp.internal.framed.FramedStream r0 = new com.squareup.okhttp.internal.framed.FramedStream     // Catch:{ all -> 0x0082 }
            r1 = r0
            r2 = r13
            r3 = r16
            r6 = r18
            r1.<init>(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0082 }
            r1 = r0
            boolean r0 = r1.isOpen()     // Catch:{ all -> 0x007d }
            if (r0 == 0) goto L_0x003f
            java.util.Map<java.lang.Integer, com.squareup.okhttp.internal.framed.FramedStream> r0 = r7.streams     // Catch:{ all -> 0x003a }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r13)     // Catch:{ all -> 0x003a }
            r0.put(r2, r1)     // Catch:{ all -> 0x003a }
            r7.setIdle(r8)     // Catch:{ all -> 0x003a }
            goto L_0x003f
        L_0x003a:
            r0 = move-exception
            r3 = r18
            goto L_0x009c
        L_0x003f:
            monitor-exit(r16)     // Catch:{ all -> 0x007d }
            if (r14 != 0) goto L_0x0057
            com.squareup.okhttp.internal.framed.FrameWriter r8 = r7.frameWriter     // Catch:{ all -> 0x0052 }
            r9 = r4
            r10 = r5
            r11 = r13
            r12 = r17
            r2 = r13
            r13 = r18
            r8.synStream(r9, r10, r11, r12, r13)     // Catch:{ all -> 0x0078 }
            r3 = r18
            goto L_0x0063
        L_0x0052:
            r0 = move-exception
            r2 = r13
            r3 = r18
            goto L_0x00a5
        L_0x0057:
            r2 = r13
            boolean r0 = r7.client     // Catch:{ all -> 0x0078 }
            if (r0 != 0) goto L_0x006c
            com.squareup.okhttp.internal.framed.FrameWriter r0 = r7.frameWriter     // Catch:{ all -> 0x0078 }
            r3 = r18
            r0.pushPromise(r14, r2, r3)     // Catch:{ all -> 0x0076 }
        L_0x0063:
            monitor-exit(r15)     // Catch:{ all -> 0x0076 }
            if (r19 != 0) goto L_0x006b
            com.squareup.okhttp.internal.framed.FrameWriter r0 = r7.frameWriter
            r0.flush()
        L_0x006b:
            return r1
        L_0x006c:
            r3 = r18
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x0076 }
            java.lang.String r6 = "client streams shouldn't have associated stream IDs"
            r0.<init>(r6)     // Catch:{ all -> 0x0076 }
            throw r0     // Catch:{ all -> 0x0076 }
        L_0x0076:
            r0 = move-exception
            goto L_0x007b
        L_0x0078:
            r0 = move-exception
            r3 = r18
        L_0x007b:
            r13 = r2
            goto L_0x00a5
        L_0x007d:
            r0 = move-exception
            r3 = r18
            r2 = r13
            goto L_0x009c
        L_0x0082:
            r0 = move-exception
            r3 = r18
            r2 = r13
            r1 = r9
            goto L_0x009c
        L_0x0088:
            r3 = r18
            java.io.IOException r0 = new java.io.IOException     // Catch:{ all -> 0x0093 }
            java.lang.String r1 = "shutdown"
            r0.<init>(r1)     // Catch:{ all -> 0x0093 }
            throw r0     // Catch:{ all -> 0x0093 }
        L_0x0093:
            r0 = move-exception
            r13 = r8
            r1 = r9
            goto L_0x009c
        L_0x0097:
            r0 = move-exception
            r3 = r18
            r13 = r8
            r1 = r9
        L_0x009c:
            monitor-exit(r16)     // Catch:{ all -> 0x009e }
            throw r0     // Catch:{ all -> 0x00a7 }
        L_0x009e:
            r0 = move-exception
            goto L_0x009c
        L_0x00a0:
            r0 = move-exception
            r3 = r18
            r13 = r8
            r1 = r9
        L_0x00a5:
            monitor-exit(r15)     // Catch:{ all -> 0x00a7 }
            throw r0
        L_0x00a7:
            r0 = move-exception
            goto L_0x00a5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedConnection.newStream(int, java.util.List, boolean, boolean):com.squareup.okhttp.internal.framed.FramedStream");
    }

    /* access modifiers changed from: package-private */
    public void writeSynReply(int streamId, boolean outFinished, List<Header> alternating) throws IOException {
        this.frameWriter.synReply(outFinished, streamId, alternating);
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004c, code lost:
        r12 = r12 - ((long) r2);
        r4 = r8.frameWriter;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        if (r10 == false) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0054, code lost:
        if (r12 != 0) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0056, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0058, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0059, code lost:
        r4.data(r5, r9, r11, r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeData(int r9, boolean r10, okio.Buffer r11, long r12) throws java.io.IOException {
        /*
            r8 = this;
            r0 = 0
            int r2 = (r12 > r0 ? 1 : (r12 == r0 ? 0 : -1))
            r3 = 0
            if (r2 != 0) goto L_0x000d
            com.squareup.okhttp.internal.framed.FrameWriter r0 = r8.frameWriter
            r0.data(r10, r9, r11, r3)
            return
        L_0x000d:
            r2 = r3
        L_0x000e:
            int r4 = (r12 > r0 ? 1 : (r12 == r0 ? 0 : -1))
            if (r4 <= 0) goto L_0x006e
            monitor-enter(r8)
        L_0x0013:
            long r4 = r8.bytesLeftInWriteWindow     // Catch:{ InterruptedException -> 0x0063 }
            int r4 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1))
            if (r4 > 0) goto L_0x0032
            java.util.Map<java.lang.Integer, com.squareup.okhttp.internal.framed.FramedStream> r4 = r8.streams     // Catch:{ InterruptedException -> 0x0063 }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r9)     // Catch:{ InterruptedException -> 0x0063 }
            boolean r4 = r4.containsKey(r5)     // Catch:{ InterruptedException -> 0x0063 }
            if (r4 == 0) goto L_0x0029
            r8.wait()     // Catch:{ InterruptedException -> 0x0063 }
            goto L_0x0013
        L_0x0029:
            java.io.IOException r0 = new java.io.IOException     // Catch:{ InterruptedException -> 0x0063 }
            java.lang.String r1 = "stream closed"
            r0.<init>(r1)     // Catch:{ InterruptedException -> 0x0063 }
            throw r0     // Catch:{ InterruptedException -> 0x0063 }
        L_0x0032:
            long r4 = r8.bytesLeftInWriteWindow     // Catch:{ all -> 0x0060 }
            long r4 = java.lang.Math.min(r12, r4)     // Catch:{ all -> 0x0060 }
            int r2 = (int) r4
            com.squareup.okhttp.internal.framed.FrameWriter r4 = r8.frameWriter     // Catch:{ all -> 0x005d }
            int r4 = r4.maxDataLength()     // Catch:{ all -> 0x005d }
            int r4 = java.lang.Math.min(r2, r4)     // Catch:{ all -> 0x005d }
            r2 = r4
            long r4 = r8.bytesLeftInWriteWindow     // Catch:{ all -> 0x005d }
            long r6 = (long) r2     // Catch:{ all -> 0x005d }
            long r4 = r4 - r6
            r8.bytesLeftInWriteWindow = r4     // Catch:{ all -> 0x005d }
            monitor-exit(r8)     // Catch:{ all -> 0x005d }
            long r4 = (long) r2
            long r12 = r12 - r4
            com.squareup.okhttp.internal.framed.FrameWriter r4 = r8.frameWriter
            if (r10 == 0) goto L_0x0058
            int r5 = (r12 > r0 ? 1 : (r12 == r0 ? 0 : -1))
            if (r5 != 0) goto L_0x0058
            r5 = 1
            goto L_0x0059
        L_0x0058:
            r5 = r3
        L_0x0059:
            r4.data(r5, r9, r11, r2)
            goto L_0x000e
        L_0x005d:
            r0 = move-exception
            r1 = r2
            goto L_0x006a
        L_0x0060:
            r0 = move-exception
            r1 = r2
            goto L_0x006a
        L_0x0063:
            r0 = move-exception
            java.io.InterruptedIOException r1 = new java.io.InterruptedIOException     // Catch:{ all -> 0x0060 }
            r1.<init>()     // Catch:{ all -> 0x0060 }
            throw r1     // Catch:{ all -> 0x0060 }
        L_0x006a:
            monitor-exit(r8)     // Catch:{ all -> 0x006c }
            throw r0
        L_0x006c:
            r0 = move-exception
            goto L_0x006a
        L_0x006e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedConnection.writeData(int, boolean, okio.Buffer, long):void");
    }

    /* access modifiers changed from: package-private */
    public void addBytesToWriteWindow(long delta) {
        this.bytesLeftInWriteWindow += delta;
        if (delta > 0) {
            notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void writeSynResetLater(int streamId, ErrorCode errorCode) {
        final int i = streamId;
        final ErrorCode errorCode2 = errorCode;
        executor.submit(new NamedRunnable("OkHttp %s stream %d", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                try {
                    FramedConnection.this.writeSynReset(i, errorCode2);
                } catch (IOException e) {
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void writeSynReset(int streamId, ErrorCode statusCode) throws IOException {
        this.frameWriter.rstStream(streamId, statusCode);
    }

    /* access modifiers changed from: package-private */
    public void writeWindowUpdateLater(int streamId, long unacknowledgedBytesRead2) {
        final int i = streamId;
        final long j = unacknowledgedBytesRead2;
        executor.execute(new NamedRunnable("OkHttp Window Update %s stream %d", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                try {
                    FramedConnection.this.frameWriter.windowUpdate(i, j);
                } catch (IOException e) {
                }
            }
        });
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    public Ping ping() throws IOException {
        Ping ping = new Ping();
        synchronized (this) {
            try {
                if (!this.shutdown) {
                    int pingId = this.nextPingId;
                    try {
                        this.nextPingId += 2;
                        if (this.pings == null) {
                            this.pings = new HashMap();
                        }
                        this.pings.put(Integer.valueOf(pingId), ping);
                        writePing(false, pingId, 1330343787, ping);
                        return ping;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    throw new IOException("shutdown");
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void writePingLater(boolean reply, int payload1, int payload2, Ping ping) {
        final boolean z = reply;
        final int i = payload1;
        final int i2 = payload2;
        final Ping ping2 = ping;
        executor.execute(new NamedRunnable("OkHttp %s ping %08x%08x", new Object[]{this.hostName, Integer.valueOf(payload1), Integer.valueOf(payload2)}) {
            public void execute() {
                try {
                    FramedConnection.this.writePing(z, i, i2, ping2);
                } catch (IOException e) {
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void writePing(boolean reply, int payload1, int payload2, Ping ping) throws IOException {
        synchronized (this.frameWriter) {
            if (ping != null) {
                ping.send();
            }
            this.frameWriter.ping(reply, payload1, payload2);
        }
    }

    /* access modifiers changed from: private */
    public synchronized Ping removePing(int id) {
        return this.pings != null ? this.pings.remove(Integer.valueOf(id)) : null;
    }

    public void flush() throws IOException {
        this.frameWriter.flush();
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public void shutdown(ErrorCode statusCode) throws IOException {
        synchronized (this.frameWriter) {
            synchronized (this) {
                try {
                    if (!this.shutdown) {
                        this.shutdown = true;
                        int lastGoodStreamId2 = this.lastGoodStreamId;
                        try {
                            this.frameWriter.goAway(lastGoodStreamId2, statusCode, Util.EMPTY_BYTE_ARRAY);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }
    }

    public void close() throws IOException {
        close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
    }

    /* access modifiers changed from: private */
    public void close(ErrorCode connectionCode, ErrorCode streamCode) throws IOException {
        int i;
        IOException thrown = null;
        try {
            shutdown(connectionCode);
        } catch (IOException e) {
            thrown = e;
        }
        FramedStream[] streamsToClose = null;
        Ping[] pingsToCancel = null;
        synchronized (this) {
            if (!this.streams.isEmpty()) {
                streamsToClose = (FramedStream[]) this.streams.values().toArray(new FramedStream[this.streams.size()]);
                this.streams.clear();
                setIdle(false);
            }
            if (this.pings != null) {
                pingsToCancel = (Ping[]) this.pings.values().toArray(new Ping[this.pings.size()]);
                this.pings = null;
            }
        }
        if (streamsToClose != null) {
            for (FramedStream stream : streamsToClose) {
                try {
                    stream.close(streamCode);
                } catch (IOException e2) {
                    if (thrown != null) {
                        thrown = e2;
                    }
                }
            }
        }
        if (pingsToCancel != null) {
            for (Ping ping : pingsToCancel) {
                ping.cancel();
            }
        }
        try {
            this.frameWriter.close();
        } catch (IOException e3) {
            if (thrown == null) {
                thrown = e3;
            }
        }
        try {
            this.socket.close();
        } catch (IOException e4) {
            thrown = e4;
        }
        if (thrown != null) {
            throw thrown;
        }
    }

    public void sendConnectionPreface() throws IOException {
        this.frameWriter.connectionPreface();
        this.frameWriter.settings(this.okHttpSettings);
        int windowSize = this.okHttpSettings.getInitialWindowSize(65536);
        if (windowSize != 65536) {
            this.frameWriter.windowUpdate(0, (long) (windowSize - 65536));
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public void setSettings(Settings settings) throws IOException {
        synchronized (this.frameWriter) {
            synchronized (this) {
                if (!this.shutdown) {
                    this.okHttpSettings.merge(settings);
                    this.frameWriter.settings(settings);
                } else {
                    throw new IOException("shutdown");
                }
            }
        }
    }

    public static class Builder {
        /* access modifiers changed from: private */
        public boolean client;
        /* access modifiers changed from: private */
        public String hostName;
        /* access modifiers changed from: private */
        public Listener listener = Listener.REFUSE_INCOMING_STREAMS;
        /* access modifiers changed from: private */
        public Protocol protocol = Protocol.SPDY_3;
        /* access modifiers changed from: private */
        public PushObserver pushObserver = PushObserver.CANCEL;
        /* access modifiers changed from: private */
        public BufferedSink sink;
        /* access modifiers changed from: private */
        public Socket socket;
        /* access modifiers changed from: private */
        public BufferedSource source;

        public Builder(boolean client2) throws IOException {
            this.client = client2;
        }

        public Builder socket(Socket socket2) throws IOException {
            return socket(socket2, ((InetSocketAddress) socket2.getRemoteSocketAddress()).getHostName(), Okio.buffer(Okio.source(socket2)), Okio.buffer(Okio.sink(socket2)));
        }

        public Builder socket(Socket socket2, String hostName2, BufferedSource source2, BufferedSink sink2) {
            this.socket = socket2;
            this.hostName = hostName2;
            this.source = source2;
            this.sink = sink2;
            return this;
        }

        public Builder listener(Listener listener2) {
            this.listener = listener2;
            return this;
        }

        public Builder protocol(Protocol protocol2) {
            this.protocol = protocol2;
            return this;
        }

        public Builder pushObserver(PushObserver pushObserver2) {
            this.pushObserver = pushObserver2;
            return this;
        }

        public FramedConnection build() throws IOException {
            return new FramedConnection(this);
        }
    }

    class Reader extends NamedRunnable implements FrameReader.Handler {
        final FrameReader frameReader;

        private Reader(FrameReader frameReader2) {
            super("OkHttp %s", FramedConnection.this.hostName);
            this.frameReader = frameReader2;
        }

        /* access modifiers changed from: protected */
        public void execute() {
            ErrorCode connectionErrorCode = ErrorCode.INTERNAL_ERROR;
            ErrorCode streamErrorCode = ErrorCode.INTERNAL_ERROR;
            try {
                if (!FramedConnection.this.client) {
                    this.frameReader.readConnectionPreface();
                }
                while (this.frameReader.nextFrame(this)) {
                }
                try {
                    FramedConnection.this.close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
                } catch (IOException e) {
                }
            } catch (IOException e2) {
                connectionErrorCode = ErrorCode.PROTOCOL_ERROR;
                try {
                    FramedConnection.this.close(connectionErrorCode, ErrorCode.PROTOCOL_ERROR);
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    FramedConnection.this.close(connectionErrorCode, streamErrorCode);
                } catch (IOException e4) {
                }
                Util.closeQuietly((Closeable) this.frameReader);
                throw th;
            }
            Util.closeQuietly((Closeable) this.frameReader);
        }

        public void data(boolean inFinished, int streamId, BufferedSource source, int length) throws IOException {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushDataLater(streamId, source, length, inFinished);
                return;
            }
            FramedStream dataStream = FramedConnection.this.getStream(streamId);
            if (dataStream == null) {
                FramedConnection.this.writeSynResetLater(streamId, ErrorCode.INVALID_STREAM);
                source.skip((long) length);
                return;
            }
            dataStream.receiveData(source, length);
            if (inFinished) {
                dataStream.receiveFin();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:34:0x009b, code lost:
            if (r20.failIfStreamPresent() == false) goto L_0x00a8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x009d, code lost:
            r12.closeLater(com.squareup.okhttp.internal.framed.ErrorCode.PROTOCOL_ERROR);
            r1.this$0.removeStream(r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a7, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a8, code lost:
            r12.receiveHeaders(r10, r20);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ad, code lost:
            if (r8 == false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x00af, code lost:
            r12.receiveFin();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void headers(boolean r15, boolean r16, int r17, int r18, java.util.List<com.squareup.okhttp.internal.framed.Header> r19, com.squareup.okhttp.internal.framed.HeadersMode r20) {
            /*
                r14 = this;
                r1 = r14
                r8 = r16
                r9 = r17
                r10 = r19
                com.squareup.okhttp.internal.framed.FramedConnection r0 = com.squareup.okhttp.internal.framed.FramedConnection.this
                boolean r0 = r0.pushedStream(r9)
                if (r0 == 0) goto L_0x0015
                com.squareup.okhttp.internal.framed.FramedConnection r0 = com.squareup.okhttp.internal.framed.FramedConnection.this
                r0.pushHeadersLater(r9, r10, r8)
                return
            L_0x0015:
                com.squareup.okhttp.internal.framed.FramedConnection r11 = com.squareup.okhttp.internal.framed.FramedConnection.this
                monitor-enter(r11)
                r2 = 0
                com.squareup.okhttp.internal.framed.FramedConnection r0 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b8 }
                boolean r0 = r0.shutdown     // Catch:{ all -> 0x00b8 }
                if (r0 == 0) goto L_0x0023
                monitor-exit(r11)     // Catch:{ all -> 0x00b8 }
                return
            L_0x0023:
                com.squareup.okhttp.internal.framed.FramedConnection r0 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b8 }
                com.squareup.okhttp.internal.framed.FramedStream r0 = r0.getStream(r9)     // Catch:{ all -> 0x00b8 }
                r12 = r0
                if (r12 != 0) goto L_0x0096
                boolean r0 = r20.failIfStreamAbsent()     // Catch:{ all -> 0x00b3 }
                if (r0 == 0) goto L_0x003b
                com.squareup.okhttp.internal.framed.FramedConnection r0 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.ErrorCode r2 = com.squareup.okhttp.internal.framed.ErrorCode.INVALID_STREAM     // Catch:{ all -> 0x00b3 }
                r0.writeSynResetLater(r9, r2)     // Catch:{ all -> 0x00b3 }
                monitor-exit(r11)     // Catch:{ all -> 0x00b3 }
                return
            L_0x003b:
                com.squareup.okhttp.internal.framed.FramedConnection r0 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b3 }
                int r0 = r0.lastGoodStreamId     // Catch:{ all -> 0x00b3 }
                if (r9 > r0) goto L_0x0045
                monitor-exit(r11)     // Catch:{ all -> 0x00b3 }
                return
            L_0x0045:
                int r0 = r9 % 2
                com.squareup.okhttp.internal.framed.FramedConnection r2 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b3 }
                int r2 = r2.nextStreamId     // Catch:{ all -> 0x00b3 }
                r13 = 2
                int r2 = r2 % r13
                if (r0 != r2) goto L_0x0053
                monitor-exit(r11)     // Catch:{ all -> 0x00b3 }
                return
            L_0x0053:
                com.squareup.okhttp.internal.framed.FramedStream r0 = new com.squareup.okhttp.internal.framed.FramedStream     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r4 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b3 }
                r2 = r0
                r3 = r17
                r5 = r15
                r6 = r16
                r7 = r19
                r2.<init>(r3, r4, r5, r6, r7)     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r2 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b3 }
                int unused = r2.lastGoodStreamId = r9     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection r2 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b3 }
                java.util.Map r2 = r2.streams     // Catch:{ all -> 0x00b3 }
                java.lang.Integer r3 = java.lang.Integer.valueOf(r17)     // Catch:{ all -> 0x00b3 }
                r2.put(r3, r0)     // Catch:{ all -> 0x00b3 }
                java.util.concurrent.ExecutorService r2 = com.squareup.okhttp.internal.framed.FramedConnection.executor     // Catch:{ all -> 0x00b3 }
                com.squareup.okhttp.internal.framed.FramedConnection$Reader$1 r3 = new com.squareup.okhttp.internal.framed.FramedConnection$Reader$1     // Catch:{ all -> 0x00b3 }
                java.lang.String r4 = "OkHttp %s stream %d"
                java.lang.Object[] r5 = new java.lang.Object[r13]     // Catch:{ all -> 0x00b3 }
                r6 = 0
                com.squareup.okhttp.internal.framed.FramedConnection r7 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x00b3 }
                java.lang.String r7 = r7.hostName     // Catch:{ all -> 0x00b3 }
                r5[r6] = r7     // Catch:{ all -> 0x00b3 }
                r6 = 1
                java.lang.Integer r7 = java.lang.Integer.valueOf(r17)     // Catch:{ all -> 0x00b3 }
                r5[r6] = r7     // Catch:{ all -> 0x00b3 }
                r3.<init>(r4, r5, r0)     // Catch:{ all -> 0x00b3 }
                r2.execute(r3)     // Catch:{ all -> 0x00b3 }
                monitor-exit(r11)     // Catch:{ all -> 0x00b3 }
                return
            L_0x0096:
                monitor-exit(r11)     // Catch:{ all -> 0x00b3 }
                boolean r0 = r20.failIfStreamPresent()
                if (r0 == 0) goto L_0x00a8
                com.squareup.okhttp.internal.framed.ErrorCode r0 = com.squareup.okhttp.internal.framed.ErrorCode.PROTOCOL_ERROR
                r12.closeLater(r0)
                com.squareup.okhttp.internal.framed.FramedConnection r0 = com.squareup.okhttp.internal.framed.FramedConnection.this
                r0.removeStream(r9)
                return
            L_0x00a8:
                r3 = r20
                r12.receiveHeaders(r10, r3)
                if (r8 == 0) goto L_0x00b2
                r12.receiveFin()
            L_0x00b2:
                return
            L_0x00b3:
                r0 = move-exception
                r3 = r20
                r2 = r12
                goto L_0x00bb
            L_0x00b8:
                r0 = move-exception
                r3 = r20
            L_0x00bb:
                monitor-exit(r11)     // Catch:{ all -> 0x00bd }
                throw r0
            L_0x00bd:
                r0 = move-exception
                goto L_0x00bb
            */
            throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedConnection.Reader.headers(boolean, boolean, int, int, java.util.List, com.squareup.okhttp.internal.framed.HeadersMode):void");
        }

        public void rstStream(int streamId, ErrorCode errorCode) {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushResetLater(streamId, errorCode);
                return;
            }
            FramedStream rstStream = FramedConnection.this.removeStream(streamId);
            if (rstStream != null) {
                rstStream.receiveRstStream(errorCode);
            }
        }

        public void settings(boolean clearPrevious, Settings newSettings) {
            int i;
            long delta = 0;
            FramedStream[] streamsToNotify = null;
            synchronized (FramedConnection.this) {
                int priorWriteWindowSize = FramedConnection.this.peerSettings.getInitialWindowSize(65536);
                if (clearPrevious) {
                    FramedConnection.this.peerSettings.clear();
                }
                FramedConnection.this.peerSettings.merge(newSettings);
                if (FramedConnection.this.getProtocol() == Protocol.HTTP_2) {
                    ackSettingsLater(newSettings);
                }
                int peerInitialWindowSize = FramedConnection.this.peerSettings.getInitialWindowSize(65536);
                if (!(peerInitialWindowSize == -1 || peerInitialWindowSize == priorWriteWindowSize)) {
                    delta = (long) (peerInitialWindowSize - priorWriteWindowSize);
                    if (!FramedConnection.this.receivedInitialPeerSettings) {
                        FramedConnection.this.addBytesToWriteWindow(delta);
                        boolean unused = FramedConnection.this.receivedInitialPeerSettings = true;
                    }
                    if (!FramedConnection.this.streams.isEmpty()) {
                        streamsToNotify = (FramedStream[]) FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                    }
                }
                FramedConnection.executor.execute(new NamedRunnable("OkHttp %s settings", FramedConnection.this.hostName) {
                    public void execute() {
                        FramedConnection.this.listener.onSettings(FramedConnection.this);
                    }
                });
            }
            if (streamsToNotify != null && delta != 0) {
                for (FramedStream stream : streamsToNotify) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(delta);
                    }
                }
            }
        }

        private void ackSettingsLater(final Settings peerSettings) {
            FramedConnection.executor.execute(new NamedRunnable("OkHttp %s ACK Settings", new Object[]{FramedConnection.this.hostName}) {
                public void execute() {
                    try {
                        FramedConnection.this.frameWriter.ackSettings(peerSettings);
                    } catch (IOException e) {
                    }
                }
            });
        }

        public void ackSettings() {
        }

        public void ping(boolean reply, int payload1, int payload2) {
            if (reply) {
                Ping ping = FramedConnection.this.removePing(payload1);
                if (ping != null) {
                    ping.receive();
                    return;
                }
                return;
            }
            FramedConnection.this.writePingLater(true, payload1, payload2, (Ping) null);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x002d, code lost:
            if (r2 >= r0) goto L_0x004e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002f, code lost:
            r3 = r1[r2];
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
            if (r3.getId() <= r7) goto L_0x004b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
            if (r3.isLocallyInitiated() == false) goto L_0x004b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x003d, code lost:
            r3.receiveRstStream(com.squareup.okhttp.internal.framed.ErrorCode.REFUSED_STREAM);
            r6.this$0.removeStream(r3.getId());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x004b, code lost:
            r2 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x004e, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x002b, code lost:
            r0 = r1.length;
            r2 = 0;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void goAway(int r7, com.squareup.okhttp.internal.framed.ErrorCode r8, okio.ByteString r9) {
            /*
                r6 = this;
                r9.size()
                com.squareup.okhttp.internal.framed.FramedConnection r0 = com.squareup.okhttp.internal.framed.FramedConnection.this
                monitor-enter(r0)
                r1 = 0
                com.squareup.okhttp.internal.framed.FramedConnection r2 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x004f }
                java.util.Map r2 = r2.streams     // Catch:{ all -> 0x004f }
                java.util.Collection r2 = r2.values()     // Catch:{ all -> 0x004f }
                com.squareup.okhttp.internal.framed.FramedConnection r3 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x004f }
                java.util.Map r3 = r3.streams     // Catch:{ all -> 0x004f }
                int r3 = r3.size()     // Catch:{ all -> 0x004f }
                com.squareup.okhttp.internal.framed.FramedStream[] r3 = new com.squareup.okhttp.internal.framed.FramedStream[r3]     // Catch:{ all -> 0x004f }
                java.lang.Object[] r2 = r2.toArray(r3)     // Catch:{ all -> 0x004f }
                com.squareup.okhttp.internal.framed.FramedStream[] r2 = (com.squareup.okhttp.internal.framed.FramedStream[]) r2     // Catch:{ all -> 0x004f }
                r1 = r2
                com.squareup.okhttp.internal.framed.FramedConnection r2 = com.squareup.okhttp.internal.framed.FramedConnection.this     // Catch:{ all -> 0x0052 }
                r3 = 1
                boolean unused = r2.shutdown = r3     // Catch:{ all -> 0x0052 }
                monitor-exit(r0)     // Catch:{ all -> 0x0052 }
                int r0 = r1.length
                r2 = 0
            L_0x002d:
                if (r2 >= r0) goto L_0x004e
                r3 = r1[r2]
                int r4 = r3.getId()
                if (r4 <= r7) goto L_0x004b
                boolean r4 = r3.isLocallyInitiated()
                if (r4 == 0) goto L_0x004b
                com.squareup.okhttp.internal.framed.ErrorCode r4 = com.squareup.okhttp.internal.framed.ErrorCode.REFUSED_STREAM
                r3.receiveRstStream(r4)
                com.squareup.okhttp.internal.framed.FramedConnection r4 = com.squareup.okhttp.internal.framed.FramedConnection.this
                int r5 = r3.getId()
                r4.removeStream(r5)
            L_0x004b:
                int r2 = r2 + 1
                goto L_0x002d
            L_0x004e:
                return
            L_0x004f:
                r2 = move-exception
            L_0x0050:
                monitor-exit(r0)     // Catch:{ all -> 0x0052 }
                throw r2
            L_0x0052:
                r2 = move-exception
                goto L_0x0050
            */
            throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.framed.FramedConnection.Reader.goAway(int, com.squareup.okhttp.internal.framed.ErrorCode, okio.ByteString):void");
        }

        public void windowUpdate(int streamId, long windowSizeIncrement) {
            if (streamId == 0) {
                synchronized (FramedConnection.this) {
                    FramedConnection.this.bytesLeftInWriteWindow += windowSizeIncrement;
                    FramedConnection.this.notifyAll();
                }
                return;
            }
            FramedStream stream = FramedConnection.this.getStream(streamId);
            if (stream != null) {
                synchronized (stream) {
                    stream.addBytesToWriteWindow(windowSizeIncrement);
                }
            }
        }

        public void priority(int streamId, int streamDependency, int weight, boolean exclusive) {
        }

        public void pushPromise(int streamId, int promisedStreamId, List<Header> requestHeaders) {
            FramedConnection.this.pushRequestLater(promisedStreamId, requestHeaders);
        }

        public void alternateService(int streamId, String origin, ByteString protocol, String host, int port, long maxAge) {
        }
    }

    /* access modifiers changed from: private */
    public boolean pushedStream(int streamId) {
        return this.protocol == Protocol.HTTP_2 && streamId != 0 && (streamId & 1) == 0;
    }

    /* access modifiers changed from: private */
    public void pushRequestLater(int streamId, List<Header> requestHeaders) {
        synchronized (this) {
            if (this.currentPushRequests.contains(Integer.valueOf(streamId))) {
                writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
                return;
            }
            this.currentPushRequests.add(Integer.valueOf(streamId));
            final int i = streamId;
            final List<Header> list = requestHeaders;
            this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Request[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
                /* Debug info: failed to restart local var, previous not found, register: 4 */
                public void execute() {
                    if (FramedConnection.this.pushObserver.onRequest(i, list)) {
                        try {
                            FramedConnection.this.frameWriter.rstStream(i, ErrorCode.CANCEL);
                            synchronized (FramedConnection.this) {
                                FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void pushHeadersLater(int streamId, List<Header> requestHeaders, boolean inFinished) {
        final int i = streamId;
        final List<Header> list = requestHeaders;
        final boolean z = inFinished;
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Headers[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            /* Debug info: failed to restart local var, previous not found, register: 4 */
            public void execute() {
                boolean cancel = FramedConnection.this.pushObserver.onHeaders(i, list, z);
                if (cancel) {
                    try {
                        FramedConnection.this.frameWriter.rstStream(i, ErrorCode.CANCEL);
                    } catch (IOException e) {
                        return;
                    }
                }
                if (cancel || z) {
                    synchronized (FramedConnection.this) {
                        FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void pushDataLater(int streamId, BufferedSource source, int byteCount, boolean inFinished) throws IOException {
        Buffer buffer = new Buffer();
        source.require((long) byteCount);
        source.read(buffer, (long) byteCount);
        if (buffer.size() == ((long) byteCount)) {
            final int i = streamId;
            final Buffer buffer2 = buffer;
            final int i2 = byteCount;
            final boolean z = inFinished;
            this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Data[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
                /* Debug info: failed to restart local var, previous not found, register: 5 */
                public void execute() {
                    try {
                        boolean cancel = FramedConnection.this.pushObserver.onData(i, buffer2, i2, z);
                        if (cancel) {
                            FramedConnection.this.frameWriter.rstStream(i, ErrorCode.CANCEL);
                        }
                        if (cancel || z) {
                            synchronized (FramedConnection.this) {
                                FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                            }
                        }
                    } catch (IOException e) {
                    }
                }
            });
            return;
        }
        throw new IOException(buffer.size() + " != " + byteCount);
    }

    /* access modifiers changed from: private */
    public void pushResetLater(int streamId, ErrorCode errorCode) {
        final int i = streamId;
        final ErrorCode errorCode2 = errorCode;
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Reset[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                FramedConnection.this.pushObserver.onReset(i, errorCode2);
                synchronized (FramedConnection.this) {
                    FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                }
            }
        });
    }

    public static abstract class Listener {
        public static final Listener REFUSE_INCOMING_STREAMS = new Listener() {
            public void onStream(FramedStream stream) throws IOException {
                stream.close(ErrorCode.REFUSED_STREAM);
            }
        };

        public abstract void onStream(FramedStream framedStream) throws IOException;

        public void onSettings(FramedConnection connection) {
        }
    }
}
