package com.squareup.okhttp.internal.http;

import com.squareup.okhttp.Address;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.RouteDatabase;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.io.RealConnection;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import okio.Sink;

public final class StreamAllocation {
    public final Address address;
    private boolean canceled;
    private RealConnection connection;
    private final ConnectionPool connectionPool;
    private boolean released;
    private RouteSelector routeSelector;
    private HttpStream stream;

    public StreamAllocation(ConnectionPool connectionPool2, Address address2) {
        this.connectionPool = connectionPool2;
        this.address = address2;
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    public HttpStream newStream(int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws RouteException, IOException {
        HttpStream resultStream;
        try {
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout, writeTimeout, connectionRetryEnabled, doExtensiveHealthChecks);
            if (resultConnection.framedConnection != null) {
                resultStream = new Http2xStream(this, resultConnection.framedConnection);
            } else {
                resultConnection.getSocket().setSoTimeout(readTimeout);
                resultConnection.source.timeout().timeout((long) readTimeout, TimeUnit.MILLISECONDS);
                resultConnection.sink.timeout().timeout((long) writeTimeout, TimeUnit.MILLISECONDS);
                resultStream = new Http1xStream(this, resultConnection.source, resultConnection.sink);
            }
            synchronized (this.connectionPool) {
                resultConnection.streamCount++;
                this.stream = resultStream;
            }
            return resultStream;
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        if (r0.isHealthy(r8) == false) goto L_0x0015;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.squareup.okhttp.internal.io.RealConnection findHealthyConnection(int r4, int r5, int r6, boolean r7, boolean r8) throws java.io.IOException, com.squareup.okhttp.internal.http.RouteException {
        /*
            r3 = this;
        L_0x0000:
            com.squareup.okhttp.internal.io.RealConnection r0 = r3.findConnection(r4, r5, r6, r7)
            com.squareup.okhttp.ConnectionPool r1 = r3.connectionPool
            monitor-enter(r1)
            int r2 = r0.streamCount     // Catch:{ all -> 0x0019 }
            if (r2 != 0) goto L_0x000d
            monitor-exit(r1)     // Catch:{ all -> 0x0019 }
            return r0
        L_0x000d:
            monitor-exit(r1)     // Catch:{ all -> 0x0019 }
            boolean r1 = r0.isHealthy(r8)
            if (r1 == 0) goto L_0x0015
            return r0
        L_0x0015:
            r3.connectionFailed()
            goto L_0x0000
        L_0x0019:
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0019 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.http.StreamAllocation.findHealthyConnection(int, int, int, boolean, boolean):com.squareup.okhttp.internal.io.RealConnection");
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003b, code lost:
        r8 = new com.squareup.okhttp.internal.io.RealConnection(r9.routeSelector.next());
        acquire(r8);
        r2 = r9.connectionPool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004c, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        com.squareup.okhttp.internal.Internal.instance.put(r9.connectionPool, r8);
        r9.connection = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0058, code lost:
        if (r9.canceled != false) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005a, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005b, code lost:
        r8.connect(r10, r11, r12, r9.address.getConnectionSpecs(), r13);
        routeDatabase().connected(r8.getRoute());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0074, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007c, code lost:
        throw new java.io.IOException("Canceled");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.squareup.okhttp.internal.io.RealConnection findConnection(int r10, int r11, int r12, boolean r13) throws java.io.IOException, com.squareup.okhttp.internal.http.RouteException {
        /*
            r9 = this;
            com.squareup.okhttp.ConnectionPool r0 = r9.connectionPool
            monitor-enter(r0)
            boolean r1 = r9.released     // Catch:{ all -> 0x009a }
            if (r1 != 0) goto L_0x0091
            com.squareup.okhttp.internal.http.HttpStream r1 = r9.stream     // Catch:{ all -> 0x009a }
            if (r1 != 0) goto L_0x0088
            boolean r1 = r9.canceled     // Catch:{ all -> 0x009a }
            if (r1 != 0) goto L_0x0080
            com.squareup.okhttp.internal.io.RealConnection r1 = r9.connection     // Catch:{ all -> 0x009a }
            if (r1 == 0) goto L_0x0019
            boolean r2 = r1.noNewStreams     // Catch:{ all -> 0x009a }
            if (r2 != 0) goto L_0x0019
            monitor-exit(r0)     // Catch:{ all -> 0x009a }
            return r1
        L_0x0019:
            com.squareup.okhttp.internal.Internal r2 = com.squareup.okhttp.internal.Internal.instance     // Catch:{ all -> 0x009a }
            com.squareup.okhttp.ConnectionPool r3 = r9.connectionPool     // Catch:{ all -> 0x009a }
            com.squareup.okhttp.Address r4 = r9.address     // Catch:{ all -> 0x009a }
            com.squareup.okhttp.internal.io.RealConnection r2 = r2.get(r3, r4, r9)     // Catch:{ all -> 0x009a }
            if (r2 == 0) goto L_0x0029
            r9.connection = r2     // Catch:{ all -> 0x009a }
            monitor-exit(r0)     // Catch:{ all -> 0x009a }
            return r2
        L_0x0029:
            com.squareup.okhttp.internal.http.RouteSelector r3 = r9.routeSelector     // Catch:{ all -> 0x009a }
            if (r3 != 0) goto L_0x003a
            com.squareup.okhttp.internal.http.RouteSelector r3 = new com.squareup.okhttp.internal.http.RouteSelector     // Catch:{ all -> 0x009a }
            com.squareup.okhttp.Address r4 = r9.address     // Catch:{ all -> 0x009a }
            com.squareup.okhttp.internal.RouteDatabase r5 = r9.routeDatabase()     // Catch:{ all -> 0x009a }
            r3.<init>(r4, r5)     // Catch:{ all -> 0x009a }
            r9.routeSelector = r3     // Catch:{ all -> 0x009a }
        L_0x003a:
            monitor-exit(r0)     // Catch:{ all -> 0x009a }
            com.squareup.okhttp.internal.http.RouteSelector r0 = r9.routeSelector
            com.squareup.okhttp.Route r1 = r0.next()
            com.squareup.okhttp.internal.io.RealConnection r0 = new com.squareup.okhttp.internal.io.RealConnection
            r0.<init>(r1)
            r8 = r0
            r9.acquire(r8)
            com.squareup.okhttp.ConnectionPool r2 = r9.connectionPool
            monitor-enter(r2)
            com.squareup.okhttp.internal.Internal r0 = com.squareup.okhttp.internal.Internal.instance     // Catch:{ all -> 0x007d }
            com.squareup.okhttp.ConnectionPool r3 = r9.connectionPool     // Catch:{ all -> 0x007d }
            r0.put(r3, r8)     // Catch:{ all -> 0x007d }
            r9.connection = r8     // Catch:{ all -> 0x007d }
            boolean r0 = r9.canceled     // Catch:{ all -> 0x007d }
            if (r0 != 0) goto L_0x0075
            monitor-exit(r2)     // Catch:{ all -> 0x007d }
            com.squareup.okhttp.Address r0 = r9.address
            java.util.List r6 = r0.getConnectionSpecs()
            r2 = r8
            r3 = r10
            r4 = r11
            r5 = r12
            r7 = r13
            r2.connect(r3, r4, r5, r6, r7)
            com.squareup.okhttp.internal.RouteDatabase r0 = r9.routeDatabase()
            com.squareup.okhttp.Route r2 = r8.getRoute()
            r0.connected(r2)
            return r8
        L_0x0075:
            java.io.IOException r0 = new java.io.IOException     // Catch:{ all -> 0x007d }
            java.lang.String r3 = "Canceled"
            r0.<init>(r3)     // Catch:{ all -> 0x007d }
            throw r0     // Catch:{ all -> 0x007d }
        L_0x007d:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x007d }
            throw r0
        L_0x0080:
            java.io.IOException r1 = new java.io.IOException     // Catch:{ all -> 0x009a }
            java.lang.String r2 = "Canceled"
            r1.<init>(r2)     // Catch:{ all -> 0x009a }
            throw r1     // Catch:{ all -> 0x009a }
        L_0x0088:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException     // Catch:{ all -> 0x009a }
            java.lang.String r2 = "stream != null"
            r1.<init>(r2)     // Catch:{ all -> 0x009a }
            throw r1     // Catch:{ all -> 0x009a }
        L_0x0091:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException     // Catch:{ all -> 0x009a }
            java.lang.String r2 = "released"
            r1.<init>(r2)     // Catch:{ all -> 0x009a }
            throw r1     // Catch:{ all -> 0x009a }
        L_0x009a:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x009a }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.http.StreamAllocation.findConnection(int, int, int, boolean):com.squareup.okhttp.internal.io.RealConnection");
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public void streamFinished(HttpStream stream2) {
        synchronized (this.connectionPool) {
            if (stream2 != null) {
                if (stream2 == this.stream) {
                }
            }
            throw new IllegalStateException("expected " + this.stream + " but was " + stream2);
        }
        deallocate(false, false, true);
    }

    public HttpStream stream() {
        HttpStream httpStream;
        synchronized (this.connectionPool) {
            httpStream = this.stream;
        }
        return httpStream;
    }

    private RouteDatabase routeDatabase() {
        return Internal.instance.routeDatabase(this.connectionPool);
    }

    public synchronized RealConnection connection() {
        return this.connection;
    }

    public void release() {
        deallocate(false, true, false);
    }

    public void noNewStreams() {
        deallocate(true, false, false);
    }

    private void deallocate(boolean noNewStreams, boolean released2, boolean streamFinished) {
        RealConnection connectionToClose = null;
        synchronized (this.connectionPool) {
            if (streamFinished) {
                try {
                    this.stream = null;
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            if (released2) {
                this.released = true;
            }
            if (this.connection != null) {
                if (noNewStreams) {
                    this.connection.noNewStreams = true;
                }
                if (this.stream == null && (this.released || this.connection.noNewStreams)) {
                    release(this.connection);
                    if (this.connection.streamCount > 0) {
                        this.routeSelector = null;
                    }
                    if (this.connection.allocations.isEmpty()) {
                        this.connection.idleAtNanos = System.nanoTime();
                        if (Internal.instance.connectionBecameIdle(this.connectionPool, this.connection)) {
                            connectionToClose = this.connection;
                        }
                    }
                    this.connection = null;
                }
            }
        }
        if (connectionToClose != null) {
            Util.closeQuietly(connectionToClose.getSocket());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        r1.cancel();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0014, code lost:
        r2.cancel();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
        if (r1 == null) goto L_0x0012;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancel() {
        /*
            r5 = this;
            com.squareup.okhttp.ConnectionPool r0 = r5.connectionPool
            monitor-enter(r0)
            r1 = 1
            r2 = 0
            r5.canceled = r1     // Catch:{ all -> 0x001a }
            com.squareup.okhttp.internal.http.HttpStream r1 = r5.stream     // Catch:{ all -> 0x001a }
            com.squareup.okhttp.internal.io.RealConnection r2 = r5.connection     // Catch:{ all -> 0x0018 }
            monitor-exit(r0)     // Catch:{ all -> 0x0020 }
            if (r1 == 0) goto L_0x0012
            r1.cancel()
            goto L_0x0017
        L_0x0012:
            if (r2 == 0) goto L_0x0017
            r2.cancel()
        L_0x0017:
            return
        L_0x0018:
            r3 = move-exception
            goto L_0x001e
        L_0x001a:
            r3 = move-exception
            r1 = r2
            r4 = r2
            r1 = r4
        L_0x001e:
            monitor-exit(r0)     // Catch:{ all -> 0x0020 }
            throw r3
        L_0x0020:
            r3 = move-exception
            goto L_0x001e
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.internal.http.StreamAllocation.cancel():void");
    }

    private void connectionFailed(IOException e) {
        synchronized (this.connectionPool) {
            if (this.routeSelector != null) {
                if (this.connection.streamCount == 0) {
                    this.routeSelector.connectFailed(this.connection.getRoute(), e);
                } else {
                    this.routeSelector = null;
                }
            }
        }
        connectionFailed();
    }

    public void connectionFailed() {
        deallocate(true, false, true);
    }

    public void acquire(RealConnection connection2) {
        connection2.allocations.add(new WeakReference(this));
    }

    private void release(RealConnection connection2) {
        int size = connection2.allocations.size();
        for (int i = 0; i < size; i++) {
            if (connection2.allocations.get(i).get() == this) {
                connection2.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }

    public boolean recover(RouteException e) {
        if (this.connection != null) {
            connectionFailed(e.getLastConnectException());
        }
        RouteSelector routeSelector2 = this.routeSelector;
        if ((routeSelector2 == null || routeSelector2.hasNext()) && isRecoverable(e)) {
            return true;
        }
        return false;
    }

    public boolean recover(IOException e, Sink requestBodyOut) {
        RealConnection realConnection = this.connection;
        if (realConnection != null) {
            int streamCount = realConnection.streamCount;
            connectionFailed(e);
            if (streamCount == 1) {
                return false;
            }
        }
        boolean canRetryRequestBody = requestBodyOut == null || (requestBodyOut instanceof RetryableSink);
        RouteSelector routeSelector2 = this.routeSelector;
        return (routeSelector2 == null || routeSelector2.hasNext()) && isRecoverable(e) && canRetryRequestBody;
    }

    private boolean isRecoverable(IOException e) {
        if (!(e instanceof ProtocolException) && !(e instanceof InterruptedIOException)) {
            return true;
        }
        return false;
    }

    private boolean isRecoverable(RouteException e) {
        IOException ioe = e.getLastConnectException();
        if (ioe instanceof ProtocolException) {
            return false;
        }
        if (ioe instanceof InterruptedIOException) {
            return ioe instanceof SocketTimeoutException;
        }
        if ((!(ioe instanceof SSLHandshakeException) || !(ioe.getCause() instanceof CertificateException)) && !(ioe instanceof SSLPeerUnverifiedException)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return this.address.toString();
    }
}
