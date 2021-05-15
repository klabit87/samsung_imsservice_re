package com.sec.internal.helper.httpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompressionUtil {
    public static final int BUFFER_SIZE = 32;
    private static final String TAG = GzipCompressionUtil.class.getSimpleName();

    public static byte[] compress(String string) throws IOException {
        return compress(string.getBytes());
    }

    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(data);
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return compressed;
    }

    /* JADX INFO: finally extract failed */
    public static String decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, 32);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[32];
        while (true) {
            try {
                int read = gis.read(data);
                int bytesRead = read;
                if (read != -1) {
                    string.append(new String(data, 0, bytesRead));
                } else {
                    gis.close();
                    is.close();
                    return string.toString();
                }
            } catch (Throwable th) {
                gis.close();
                throw th;
            }
        }
    }
}
