package com.squareup.okhttp;

import com.squareup.okhttp.internal.Util;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import okio.Buffer;
import okio.BufferedSource;
import org.xbill.DNS.TTL;

public abstract class ResponseBody implements Closeable {
    private Reader reader;

    public abstract long contentLength() throws IOException;

    public abstract MediaType contentType();

    public abstract BufferedSource source() throws IOException;

    public final InputStream byteStream() throws IOException {
        return source().inputStream();
    }

    public final byte[] bytes() throws IOException {
        long contentLength = contentLength();
        if (contentLength <= TTL.MAX_VALUE) {
            BufferedSource source = source();
            try {
                byte[] bytes = source.readByteArray();
                Util.closeQuietly((Closeable) source);
                if (contentLength == -1 || contentLength == ((long) bytes.length)) {
                    return bytes;
                }
                throw new IOException("Content-Length and stream length disagree");
            } catch (Throwable th) {
                Util.closeQuietly((Closeable) source);
                throw th;
            }
        } else {
            throw new IOException("Cannot buffer entire body for content length: " + contentLength);
        }
    }

    public final Reader charStream() throws IOException {
        Reader r = this.reader;
        if (r != null) {
            return r;
        }
        InputStreamReader inputStreamReader = new InputStreamReader(byteStream(), charset());
        this.reader = inputStreamReader;
        return inputStreamReader;
    }

    public final String string() throws IOException {
        return new String(bytes(), charset().name());
    }

    private Charset charset() {
        MediaType contentType = contentType();
        Charset charset = Util.UTF_8;
        return contentType != null ? contentType.charset(charset) : charset;
    }

    public void close() throws IOException {
        source().close();
    }

    public static ResponseBody create(MediaType contentType, String content) {
        Charset charset = Util.UTF_8;
        if (contentType != null && (charset = contentType.charset()) == null) {
            charset = Util.UTF_8;
            contentType = MediaType.parse(contentType + "; charset=utf-8");
        }
        Buffer buffer = new Buffer().writeString(content, charset);
        return create(contentType, buffer.size(), buffer);
    }

    public static ResponseBody create(MediaType contentType, byte[] content) {
        return create(contentType, (long) content.length, new Buffer().write(content));
    }

    public static ResponseBody create(final MediaType contentType, final long contentLength, final BufferedSource content) {
        if (content != null) {
            return new ResponseBody() {
                public MediaType contentType() {
                    return contentType;
                }

                public long contentLength() {
                    return contentLength;
                }

                public BufferedSource source() {
                    return content;
                }
            };
        }
        throw new NullPointerException("source == null");
    }
}
