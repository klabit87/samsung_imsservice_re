package com.sec.internal.helper;

import android.net.Network;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpController;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.xbill.DNS.KEYRecord;

public class HttpRequest {
    public static final String BOUNDARY = "00content0boundary00";
    public static final String CHARSET_UTF8 = "UTF-8";
    private static ConnectionFactory CONNECTION_FACTORY = ConnectionFactory.DEFAULT;
    private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary=00content0boundary00";
    private static final String CRLF = "\r\n";
    public static final String ENCODING_GZIP = "gzip";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_AUTHENTICATION_INFO = "Authentication-Info";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_LOCATION = "Location";
    public static final String HEADER_RANGE = "Range";
    public static final String HEADER_RETRY_AFTER = "Retry-After";
    public static final String HEADER_SUPPORTED_VERSIONS = "Supported-Versions";
    public static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String PARAM_CHARSET = "charset";
    private static SSLSocketFactory TRUSTED_FACTORY = null;
    private static HostnameVerifier TRUSTED_VERIFIER = null;
    /* access modifiers changed from: private */
    public int bufferSize = KEYRecord.Flags.FLAG2;
    private HttpURLConnection connection = null;
    private String httpProxyHost;
    private int httpProxyPort;
    private boolean ignoreCloseExceptions = true;
    private boolean multipart;
    private Network network;
    private RequestOutputStream output;
    /* access modifiers changed from: private */
    public UploadProgress progress = UploadProgress.DEFAULT;
    private final String requestMethod;
    /* access modifiers changed from: private */
    public long totalSize = -1;
    /* access modifiers changed from: private */
    public long totalWritten = 0;
    private boolean uncompress = false;
    private final URL url;

    public interface ConnectionFactory {
        public static final ConnectionFactory DEFAULT = new ConnectionFactory() {
            public HttpURLConnection create(URL url, Network network) throws IOException {
                if (network != null) {
                    return (HttpURLConnection) network.openConnection(url);
                }
                return (HttpURLConnection) url.openConnection();
            }

            public HttpURLConnection create(URL url, Proxy proxy, Network network) throws IOException {
                if (network != null) {
                    return (HttpURLConnection) network.openConnection(url, proxy);
                }
                return (HttpURLConnection) url.openConnection(proxy);
            }
        };

        HttpURLConnection create(URL url, Network network) throws IOException;

        HttpURLConnection create(URL url, Proxy proxy, Network network) throws IOException;
    }

    public interface UploadProgress {
        public static final UploadProgress DEFAULT = new UploadProgress() {
            public void onUpload(long uploaded, long total) {
            }

            public boolean isCancelled() {
                return false;
            }
        };

        boolean isCancelled();

        void onUpload(long j, long j2);
    }

    static /* synthetic */ long access$214(HttpRequest x0, long x1) {
        long j = x0.totalWritten + x1;
        x0.totalWritten = j;
        return j;
    }

    /* access modifiers changed from: private */
    public static String getValidCharset(String charset) {
        if (charset == null || charset.length() <= 0) {
            return "UTF-8";
        }
        return charset;
    }

    private static SSLSocketFactory getTrustedFactory() throws HttpRequestException {
        SSLSocketFactory sSLSocketFactory;
        synchronized (HttpRequest.class) {
            if (TRUSTED_FACTORY == null) {
                TrustManager[] trustAllCerts = {new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }
                }};
                try {
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init((KeyManager[]) null, trustAllCerts, new SecureRandom());
                    TRUSTED_FACTORY = context.getSocketFactory();
                } catch (GeneralSecurityException e) {
                    IOException ioException = new IOException("Security exception configuring SSL context");
                    ioException.initCause(e);
                    throw new HttpRequestException(ioException);
                }
            }
            sSLSocketFactory = TRUSTED_FACTORY;
        }
        return sSLSocketFactory;
    }

    private static HostnameVerifier getTrustedVerifier() {
        HostnameVerifier hostnameVerifier;
        synchronized (HttpRequest.class) {
            if (TRUSTED_VERIFIER == null) {
                TRUSTED_VERIFIER = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        Log.d("HttpRequest", "This is verify() in HttpRequest.");
                        return true;
                    }
                };
            }
            hostnameVerifier = TRUSTED_VERIFIER;
        }
        return hostnameVerifier;
    }

    public static class HttpRequestException extends RuntimeException {
        private static final long serialVersionUID = -1170466989781746231L;

        public HttpRequestException(IOException cause) {
            super(cause);
        }

        public IOException getCause() {
            return (IOException) super.getCause();
        }
    }

    protected static abstract class Operation<V> implements Callable<V> {
        /* access modifiers changed from: protected */
        public abstract void done() throws IOException;

        /* access modifiers changed from: protected */
        public abstract V run() throws HttpRequestException, IOException;

        protected Operation() {
        }

        public V call() throws HttpRequestException {
            boolean thrown = false;
            try {
                V run = run();
                try {
                    done();
                } catch (IOException e) {
                    if (0 == 0) {
                        throw new HttpRequestException(e);
                    }
                }
                return run;
            } catch (HttpRequestException e2) {
                throw e2;
            } catch (IOException e3) {
                thrown = true;
                throw new HttpRequestException(e3);
            } catch (Throwable th) {
                try {
                    done();
                } catch (IOException e4) {
                    if (!thrown) {
                        throw new HttpRequestException(e4);
                    }
                }
                throw th;
            }
        }
    }

    protected static abstract class CloseOperation<V> extends Operation<V> {
        private final Closeable closeable;
        FileOutputStream fileOutputStream;
        private final boolean ignoreCloseExceptions;

        protected CloseOperation(Closeable closeable2, boolean ignoreCloseExceptions2, FileOutputStream fileOutputStream2) {
            this.closeable = closeable2;
            this.ignoreCloseExceptions = ignoreCloseExceptions2;
            this.fileOutputStream = fileOutputStream2;
        }

        /* access modifiers changed from: protected */
        public void done() throws IOException {
            Closeable closeable2 = this.closeable;
            if (closeable2 instanceof Flushable) {
                ((Flushable) closeable2).flush();
            }
            if (this.ignoreCloseExceptions) {
                try {
                    this.closeable.close();
                    if (this.fileOutputStream != null) {
                        this.fileOutputStream.close();
                    }
                } catch (IOException e) {
                }
            } else {
                this.closeable.close();
                FileOutputStream fileOutputStream2 = this.fileOutputStream;
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
            }
        }
    }

    public static class RequestOutputStream extends BufferedOutputStream {
        private final CharsetEncoder encoder;

        public RequestOutputStream(OutputStream stream, String charset, int bufferSize) {
            super(stream, bufferSize);
            this.encoder = Charset.forName(HttpRequest.getValidCharset(charset)).newEncoder();
        }

        public RequestOutputStream write(String value) throws IOException {
            ByteBuffer bytes = this.encoder.encode(CharBuffer.wrap(value));
            super.write(bytes.array(), 0, bytes.limit());
            return this;
        }
    }

    public static HttpRequest get(CharSequence url2) throws HttpRequestException {
        return new HttpRequest(url2, "GET");
    }

    public static HttpRequest post(CharSequence url2) throws HttpRequestException {
        return new HttpRequest(url2, "POST");
    }

    public static HttpRequest put(CharSequence url2) throws HttpRequestException {
        return new HttpRequest(url2, "PUT");
    }

    public static HttpRequest put(URL url2) throws HttpRequestException {
        return new HttpRequest(url2, "PUT");
    }

    public HttpRequest(CharSequence url2, String method) throws HttpRequestException {
        try {
            this.url = new URL(url2.toString());
            this.requestMethod = method;
        } catch (MalformedURLException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest(URL url2, String method) throws HttpRequestException {
        this.url = url2;
        this.requestMethod = method;
    }

    private Proxy createProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.httpProxyHost, this.httpProxyPort));
    }

    private HttpURLConnection createConnection() {
        HttpURLConnection connection2;
        try {
            if (this.httpProxyHost != null) {
                connection2 = CONNECTION_FACTORY.create(this.url, createProxy(), this.network);
            } else {
                connection2 = CONNECTION_FACTORY.create(this.url, this.network);
            }
            connection2.setRequestMethod(this.requestMethod);
            return connection2;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public String toString() {
        return method() + ' ' + url();
    }

    public HttpURLConnection getConnection() {
        if (this.connection == null) {
            this.connection = createConnection();
        }
        return this.connection;
    }

    public int code() throws HttpRequestException {
        try {
            closeOutput();
            return getConnection().getResponseCode();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public String getCipherSuite() {
        HttpURLConnection httpURLConnection = this.connection;
        if (httpURLConnection == null || !(httpURLConnection instanceof HttpsURLConnection)) {
            return null;
        }
        try {
            return ((HttpsURLConnection) httpURLConnection).getCipherSuite();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean ok() throws HttpRequestException {
        return 200 == code();
    }

    public String message() throws HttpRequestException {
        try {
            closeOutput();
            return getConnection().getResponseMessage();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest disconnect() {
        getConnection().disconnect();
        return this;
    }

    public HttpRequest chunk(int size) {
        getConnection().setChunkedStreamingMode(size);
        return this;
    }

    public HttpRequest bufferSize(int size) {
        if (size >= 1) {
            this.bufferSize = size;
            return this;
        }
        throw new IllegalArgumentException("Size must be greater than zero");
    }

    /* access modifiers changed from: protected */
    public ByteArrayOutputStream byteStream() {
        int size = contentLength();
        if (size > 0) {
            return new ByteArrayOutputStream(size);
        }
        return new ByteArrayOutputStream();
    }

    public String body(String charset) throws HttpRequestException {
        ByteArrayOutputStream output2 = byteStream();
        try {
            copy(buffer(), output2);
            return output2.toString(getValidCharset(charset));
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public String body() throws HttpRequestException {
        return body(charset());
    }

    public BufferedInputStream buffer() throws HttpRequestException {
        return new BufferedInputStream(stream(), this.bufferSize);
    }

    public InputStream stream() throws HttpRequestException {
        InputStream stream;
        if (code() < 400) {
            try {
                stream = getConnection().getInputStream();
            } catch (IOException e) {
                throw new HttpRequestException(e);
            }
        } else {
            stream = getConnection().getErrorStream();
            if (stream == null) {
                try {
                    stream = getConnection().getInputStream();
                } catch (IOException e2) {
                    if (contentLength() <= 0) {
                        stream = new ByteArrayInputStream(new byte[0]);
                    } else {
                        throw new HttpRequestException(e2);
                    }
                }
            }
        }
        if (!this.uncompress || !"gzip".equals(contentEncoding())) {
            return stream;
        }
        try {
            return new GZIPInputStream(stream);
        } catch (IOException e3) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    throw new HttpRequestException(ex);
                }
            }
            throw new HttpRequestException(e3);
        }
    }

    public HttpRequest receive(OutputStream output2) throws HttpRequestException {
        try {
            return copy(buffer(), output2);
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest readTimeout(int timeout) {
        getConnection().setReadTimeout(timeout);
        return this;
    }

    public HttpRequest connectTimeout(int timeout) {
        getConnection().setConnectTimeout(timeout);
        return this;
    }

    public HttpRequest header(String name, String value) {
        getConnection().setRequestProperty(name, value);
        return this;
    }

    public String header(String name) throws HttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderField(name);
    }

    public int intHeader(String name) throws HttpRequestException {
        return intHeader(name, -1);
    }

    public int intHeader(String name, int defaultValue) throws HttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(name, defaultValue);
    }

    public String parameter(String headerName, String paramName) {
        return getParam(header(headerName), paramName);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003e, code lost:
        r8 = r11.substring(r7 + 1, r5).trim();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getParam(java.lang.String r11, java.lang.String r12) {
        /*
            r10 = this;
            r0 = 0
            if (r11 == 0) goto L_0x0076
            int r1 = r11.length()
            if (r1 != 0) goto L_0x000b
            goto L_0x0076
        L_0x000b:
            int r1 = r11.length()
            r2 = 59
            int r3 = r11.indexOf(r2)
            r4 = 1
            int r3 = r3 + r4
            if (r3 == 0) goto L_0x0075
            if (r3 != r1) goto L_0x001c
            goto L_0x0075
        L_0x001c:
            int r5 = r11.indexOf(r2, r3)
            r6 = -1
            if (r5 != r6) goto L_0x0024
            r5 = r1
        L_0x0024:
            if (r3 >= r5) goto L_0x0074
            r7 = 61
            int r7 = r11.indexOf(r7, r3)
            if (r7 == r6) goto L_0x006a
            if (r7 >= r5) goto L_0x006a
            java.lang.String r8 = r11.substring(r3, r7)
            java.lang.String r8 = r8.trim()
            boolean r8 = r12.equals(r8)
            if (r8 == 0) goto L_0x006a
            int r8 = r7 + 1
            java.lang.String r8 = r11.substring(r8, r5)
            java.lang.String r8 = r8.trim()
            int r9 = r8.length()
            if (r9 == 0) goto L_0x006a
            r0 = 2
            if (r9 <= r0) goto L_0x0069
            r0 = 0
            char r0 = r8.charAt(r0)
            r2 = 34
            if (r2 != r0) goto L_0x0069
            int r0 = r9 + -1
            char r0 = r8.charAt(r0)
            if (r2 != r0) goto L_0x0069
            int r0 = r9 + -1
            java.lang.String r0 = r8.substring(r4, r0)
            return r0
        L_0x0069:
            return r8
        L_0x006a:
            int r3 = r5 + 1
            int r5 = r11.indexOf(r2, r3)
            if (r5 != r6) goto L_0x0073
            r5 = r1
        L_0x0073:
            goto L_0x0024
        L_0x0074:
            return r0
        L_0x0075:
            return r0
        L_0x0076:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.HttpRequest.getParam(java.lang.String, java.lang.String):java.lang.String");
    }

    public String charset() {
        return parameter("Content-Type", "charset");
    }

    public HttpRequest userAgent(String userAgent) {
        return header("User-Agent", userAgent);
    }

    public HttpRequest useCaches(boolean useCaches) {
        getConnection().setUseCaches(useCaches);
        return this;
    }

    public HttpRequest acceptEncoding(String acceptEncoding) {
        return header("Accept-Encoding", acceptEncoding);
    }

    public String contentEncoding() {
        return header("Content-Encoding");
    }

    public HttpRequest authorization(String authorization) {
        return header("Authorization", authorization);
    }

    public HttpRequest contentType(String contentType) {
        return contentType(contentType, (String) null);
    }

    public HttpRequest contentType(String contentType, String charset) {
        if (charset == null || charset.length() <= 0) {
            return header("Content-Type", contentType);
        }
        return header("Content-Type", contentType + "; charset=" + charset);
    }

    public int contentLength() {
        return intHeader("Content-Length");
    }

    public HttpRequest contentLength(String contentLength) {
        return contentLength(Integer.parseInt(contentLength));
    }

    public HttpRequest contentLength(int contentLength) {
        getConnection().setFixedLengthStreamingMode(contentLength);
        return this;
    }

    public String wwwAuthenticate() {
        return header("WWW-Authenticate");
    }

    public HttpRequest range(long start, long end) {
        if (start < 0) {
            throw new IllegalArgumentException("Cannot have negative start: " + start);
        } else if (end < 0) {
            return header("Range", String.format("bytes=%s-", new Object[]{Long.valueOf(start)}));
        } else {
            return header("Range", String.format("bytes=%s-%s", new Object[]{Long.valueOf(start), Long.valueOf(end)}));
        }
    }

    public HttpRequest contentRange(long first, long last, long size) {
        if (first < 0 || last < 0 || first > last) {
            throw new IllegalArgumentException("Invalid argument: " + first + "," + last);
        }
        return header("Content-Range", String.format("bytes %s-%s/%s", new Object[]{Long.valueOf(first), Long.valueOf(last), Long.valueOf(size)}));
    }

    /* access modifiers changed from: protected */
    public HttpRequest copy(InputStream input, OutputStream output2) throws IOException {
        final InputStream inputStream = input;
        final OutputStream outputStream = output2;
        return (HttpRequest) new CloseOperation<HttpRequest>(input, this.ignoreCloseExceptions, (FileOutputStream) null) {
            public HttpRequest run() throws IOException {
                byte[] buffer = new byte[HttpRequest.this.bufferSize];
                do {
                    int read = inputStream.read(buffer);
                    int read2 = read;
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, read2);
                    outputStream.flush();
                    HttpRequest.access$214(HttpRequest.this, (long) read2);
                    HttpRequest.this.progress.onUpload(HttpRequest.this.totalWritten, HttpRequest.this.totalSize);
                } while (!HttpRequest.this.progress.isCancelled());
                return HttpRequest.this;
            }
        }.call();
    }

    public HttpRequest progress(UploadProgress callback) {
        if (callback == null) {
            this.progress = UploadProgress.DEFAULT;
        } else {
            this.progress = callback;
        }
        this.totalWritten = 0;
        return this;
    }

    private HttpRequest incrementTotalSize(long size) {
        if (this.totalSize == -1) {
            this.totalSize = 0;
        }
        this.totalSize += size;
        return this;
    }

    /* access modifiers changed from: protected */
    public HttpRequest closeOutput() throws IOException {
        RequestOutputStream requestOutputStream = this.output;
        if (requestOutputStream == null) {
            return this;
        }
        if (this.multipart) {
            requestOutputStream.write("\r\n--00content0boundary00--\r\n");
        }
        if (this.ignoreCloseExceptions) {
            try {
                this.output.close();
            } catch (IOException e) {
            }
        } else {
            this.output.close();
        }
        this.output = null;
        return this;
    }

    /* access modifiers changed from: protected */
    public HttpRequest closeOutputQuietly() throws HttpRequestException {
        try {
            return closeOutput();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    /* access modifiers changed from: protected */
    public HttpRequest openOutput() throws IOException {
        if (this.output != null) {
            return this;
        }
        getConnection().setDoOutput(true);
        this.output = new RequestOutputStream(getConnection().getOutputStream(), getParam(getConnection().getRequestProperty("Content-Type"), "charset"), this.bufferSize);
        return this;
    }

    /* access modifiers changed from: protected */
    public HttpRequest startPart() throws IOException {
        if (!this.multipart) {
            this.multipart = true;
            contentType(CONTENT_TYPE_MULTIPART).openOutput();
            this.output.write("--00content0boundary00\r\n");
        } else {
            this.output.write("\r\n--00content0boundary00\r\n");
        }
        return this;
    }

    /* access modifiers changed from: protected */
    public HttpRequest writePartHeader(String name, String filename, String contentType) throws IOException {
        StringBuilder partBuffer = new StringBuilder();
        partBuffer.append("form-data; name=\"");
        partBuffer.append(name);
        if (filename != null) {
            partBuffer.append("\"; filename=\"");
            partBuffer.append(filename);
        }
        partBuffer.append('\"');
        partHeader(HttpController.HEADER_CONTENT_DISPOSITION, partBuffer.toString());
        if (contentType != null) {
            partHeader("Content-Type", contentType);
        }
        return send((CharSequence) CRLF);
    }

    public HttpRequest part(String name, String filename, String contentType, String part) throws HttpRequestException {
        try {
            startPart();
            writePartHeader(name, filename, contentType);
            this.output.write(part);
            return this;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public HttpRequest part(String name, String filename, String contentType, File part) throws HttpRequestException {
        InputStream stream;
        try {
            InputStream fileStream = new FileInputStream(part);
            try {
                stream = new BufferedInputStream(fileStream);
                incrementTotalSize(part.length());
                part(name, filename, contentType, stream);
                stream.close();
                fileStream.close();
                return this;
            } catch (Throwable th) {
                fileStream.close();
                throw th;
            }
            throw th;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    public HttpRequest part(String name, String filename, String contentType, InputStream part) throws HttpRequestException {
        try {
            startPart();
            writePartHeader(name, filename, contentType);
            copy(part, this.output);
            return this;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest partHeader(String name, String value) throws HttpRequestException {
        return send((CharSequence) name).send((CharSequence) ": ").send((CharSequence) value).send((CharSequence) CRLF);
    }

    public HttpRequest send(InputStream input) throws HttpRequestException {
        try {
            openOutput();
            copy(input, this.output);
            return this;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest send(CharSequence value) throws HttpRequestException {
        try {
            openOutput();
            this.output.write(value.toString());
            return this;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest trustAllCerts() throws HttpRequestException {
        HttpURLConnection connection2 = getConnection();
        if (connection2 instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection2).setSSLSocketFactory(getTrustedFactory());
        }
        return this;
    }

    public HttpRequest trustAllHosts() {
        HttpURLConnection connection2 = getConnection();
        if (connection2 instanceof HttpsURLConnection) {
            Log.d("HttpRequest", "trustAllHosts() - this connections is instance of HttpsURLConnection ");
            ((HttpsURLConnection) connection2).setHostnameVerifier(getTrustedVerifier());
        }
        return this;
    }

    public URL url() {
        return getConnection().getURL();
    }

    public String method() {
        return getConnection().getRequestMethod();
    }

    public HttpRequest useNetwork(Network network2) {
        this.network = network2;
        return this;
    }

    public long getPartHeaderLength(String name, String filename, String contentType, boolean firstPart) {
        StringBuilder header = new StringBuilder();
        if (firstPart) {
            header.append("--00content0boundary00\r\n");
        } else {
            header.append("\r\n--00content0boundary00\r\n");
        }
        StringBuilder partBuffer = new StringBuilder();
        partBuffer.append("form-data; name=\"");
        partBuffer.append(name);
        if (filename != null) {
            partBuffer.append("\"; filename=\"");
            partBuffer.append(filename);
        }
        partBuffer.append('\"');
        header.append("Content-Disposition: " + partBuffer.toString() + CRLF);
        if (contentType != null) {
            header.append("Content-Type: " + contentType + CRLF);
        }
        header.append(CRLF);
        Log.d("HttpRequest", "The length of header: " + header.length());
        return (long) header.length();
    }

    public HttpRequest setFollowRedirect(boolean isFollowRedirect) {
        getConnection().setInstanceFollowRedirects(isFollowRedirect);
        return this;
    }

    public HttpRequest setParams(Network network2, boolean useCache, int connectTimeout, int readTimeout, String userAgent) {
        return useNetwork(network2).useCaches(useCache).connectTimeout(connectTimeout).readTimeout(readTimeout).userAgent(userAgent).setFollowRedirect(false);
    }
}
