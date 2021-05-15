package javax.mail.internet;

import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.BEncoderStream;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.QDecoderStream;
import com.sun.mail.util.QEncoderStream;
import com.sun.mail.util.QPDecoderStream;
import com.sun.mail.util.QPEncoderStream;
import com.sun.mail.util.UUDecoderStream;
import com.sun.mail.util.UUEncoderStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;

public class MimeUtility {
    public static final int ALL = -1;
    static final int ALL_ASCII = 1;
    static final int MOSTLY_ASCII = 2;
    static final int MOSTLY_NONASCII = 3;
    private static boolean decodeStrict;
    private static String defaultJavaCharset;
    private static String defaultMIMECharset;
    private static boolean encodeEolStrict;
    private static boolean foldEncodedWords;
    private static boolean foldText;
    private static Hashtable java2mime = new Hashtable(40);
    private static Hashtable mime2java = new Hashtable(10);

    private MimeUtility() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x002b A[SYNTHETIC, Splitter:B:13:0x002b] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0045 A[Catch:{ SecurityException -> 0x005d }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0047 A[Catch:{ SecurityException -> 0x005d }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x007b A[SYNTHETIC, Splitter:B:35:0x007b] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ab  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01c5  */
    /* JADX WARNING: Removed duplicated region for block: B:54:? A[RETURN, SYNTHETIC] */
    static {
        /*
            r0 = 1
            decodeStrict = r0
            r1 = 0
            encodeEolStrict = r1
            foldEncodedWords = r1
            foldText = r0
            java.lang.String r2 = "mail.mime.decodetext.strict"
            java.lang.String r2 = java.lang.System.getProperty(r2)     // Catch:{ SecurityException -> 0x005d }
            java.lang.String r3 = "false"
            if (r2 == 0) goto L_0x001c
            boolean r4 = r2.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x005d }
            if (r4 == 0) goto L_0x001c
            r4 = r1
            goto L_0x001d
        L_0x001c:
            r4 = r0
        L_0x001d:
            decodeStrict = r4     // Catch:{ SecurityException -> 0x005d }
            java.lang.String r4 = "mail.mime.encodeeol.strict"
            java.lang.String r4 = java.lang.System.getProperty(r4)     // Catch:{ SecurityException -> 0x005d }
            r2 = r4
            java.lang.String r4 = "true"
            if (r2 == 0) goto L_0x0033
            boolean r5 = r2.equalsIgnoreCase(r4)     // Catch:{ SecurityException -> 0x005d }
            if (r5 == 0) goto L_0x0033
            r5 = r0
            goto L_0x0034
        L_0x0033:
            r5 = r1
        L_0x0034:
            encodeEolStrict = r5     // Catch:{ SecurityException -> 0x005d }
            java.lang.String r5 = "mail.mime.foldencodedwords"
            java.lang.String r5 = java.lang.System.getProperty(r5)     // Catch:{ SecurityException -> 0x005d }
            r2 = r5
            if (r2 == 0) goto L_0x0047
            boolean r4 = r2.equalsIgnoreCase(r4)     // Catch:{ SecurityException -> 0x005d }
            if (r4 == 0) goto L_0x0047
            r4 = r0
            goto L_0x0048
        L_0x0047:
            r4 = r1
        L_0x0048:
            foldEncodedWords = r4     // Catch:{ SecurityException -> 0x005d }
            java.lang.String r4 = "mail.mime.foldtext"
            java.lang.String r4 = java.lang.System.getProperty(r4)     // Catch:{ SecurityException -> 0x005d }
            r2 = r4
            if (r2 == 0) goto L_0x005a
            boolean r3 = r2.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x005d }
            if (r3 == 0) goto L_0x005a
            r0 = r1
        L_0x005a:
            foldText = r0     // Catch:{ SecurityException -> 0x005d }
            goto L_0x005e
        L_0x005d:
            r0 = move-exception
        L_0x005e:
            java.util.Hashtable r0 = new java.util.Hashtable
            r1 = 40
            r0.<init>(r1)
            java2mime = r0
            java.util.Hashtable r0 = new java.util.Hashtable
            r1 = 10
            r0.<init>(r1)
            mime2java = r0
            java.lang.Class<javax.mail.internet.MimeUtility> r0 = javax.mail.internet.MimeUtility.class
            java.lang.String r1 = "/META-INF/javamail.charset.map"
            java.io.InputStream r0 = r0.getResourceAsStream(r1)     // Catch:{ Exception -> 0x009e }
            if (r0 == 0) goto L_0x009f
            com.sun.mail.util.LineInputStream r1 = new com.sun.mail.util.LineInputStream     // Catch:{ all -> 0x0097 }
            r1.<init>(r0)     // Catch:{ all -> 0x0097 }
            r0 = r1
            r1 = r0
            com.sun.mail.util.LineInputStream r1 = (com.sun.mail.util.LineInputStream) r1     // Catch:{ all -> 0x0097 }
            java.util.Hashtable r2 = java2mime     // Catch:{ all -> 0x0097 }
            loadMappings(r1, r2)     // Catch:{ all -> 0x0097 }
            r1 = r0
            com.sun.mail.util.LineInputStream r1 = (com.sun.mail.util.LineInputStream) r1     // Catch:{ all -> 0x0097 }
            java.util.Hashtable r2 = mime2java     // Catch:{ all -> 0x0097 }
            loadMappings(r1, r2)     // Catch:{ all -> 0x0097 }
            r0.close()     // Catch:{ Exception -> 0x0095 }
            goto L_0x009f
        L_0x0095:
            r1 = move-exception
            goto L_0x009f
        L_0x0097:
            r1 = move-exception
            r0.close()     // Catch:{ Exception -> 0x009c }
            goto L_0x009d
        L_0x009c:
            r2 = move-exception
        L_0x009d:
            throw r1     // Catch:{ Exception -> 0x009e }
        L_0x009e:
            r0 = move-exception
        L_0x009f:
            java.util.Hashtable r0 = java2mime
            boolean r0 = r0.isEmpty()
            java.lang.String r1 = "euc-kr"
            java.lang.String r2 = "ISO-8859-1"
            if (r0 == 0) goto L_0x01bd
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_1"
            r0.put(r3, r2)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_1"
            r0.put(r3, r2)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-1"
            r0.put(r3, r2)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_2"
            java.lang.String r4 = "ISO-8859-2"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_2"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-2"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_3"
            java.lang.String r4 = "ISO-8859-3"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_3"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-3"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_4"
            java.lang.String r4 = "ISO-8859-4"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_4"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-4"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_5"
            java.lang.String r4 = "ISO-8859-5"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_5"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-5"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_6"
            java.lang.String r4 = "ISO-8859-6"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_6"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-6"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_7"
            java.lang.String r4 = "ISO-8859-7"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_7"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-7"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_8"
            java.lang.String r4 = "ISO-8859-8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_9"
            java.lang.String r4 = "ISO-8859-9"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_9"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-9"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "sjis"
            java.lang.String r4 = "Shift_JIS"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "jis"
            java.lang.String r4 = "ISO-2022-JP"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso2022jp"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "euc_jp"
            java.lang.String r4 = "euc-jp"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "koi8_r"
            java.lang.String r4 = "koi8-r"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "euc_cn"
            java.lang.String r4 = "euc-cn"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "euc_tw"
            java.lang.String r4 = "euc-tw"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "euc_kr"
            r0.put(r3, r1)
        L_0x01bd:
            java.util.Hashtable r0 = mime2java
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x0219
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "iso-2022-cn"
            java.lang.String r4 = "ISO2022CN"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "iso-2022-kr"
            java.lang.String r4 = "ISO2022KR"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "utf-8"
            java.lang.String r4 = "UTF8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "utf8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "ja_jp.iso2022-7"
            java.lang.String r4 = "ISO2022JP"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "ja_jp.eucjp"
            java.lang.String r4 = "EUCJIS"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "KSC5601"
            r0.put(r1, r3)
            java.util.Hashtable r0 = mime2java
            java.lang.String r1 = "euckr"
            r0.put(r1, r3)
            java.util.Hashtable r0 = mime2java
            java.lang.String r1 = "us-ascii"
            r0.put(r1, r2)
            java.util.Hashtable r0 = mime2java
            java.lang.String r1 = "x-us-ascii"
            r0.put(r1, r2)
        L_0x0219:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeUtility.<clinit>():void");
    }

    public static String getEncoding(DataSource ds) {
        String encoding;
        try {
            ContentType cType = new ContentType(ds.getContentType());
            InputStream is = ds.getInputStream();
            int i = checkAscii(is, -1, !cType.match("text/*"));
            if (i == 1) {
                encoding = "7bit";
            } else if (i != 2) {
                encoding = HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64;
            } else {
                encoding = "quoted-printable";
            }
            try {
                is.close();
            } catch (IOException e) {
            }
            return encoding;
        } catch (Exception e2) {
            return HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64;
        }
    }

    public static String getEncoding(DataHandler dh) {
        if (dh.getName() != null) {
            return getEncoding(dh.getDataSource());
        }
        try {
            if (new ContentType(dh.getContentType()).match("text/*")) {
                AsciiOutputStream aos = new AsciiOutputStream(false, false);
                try {
                    dh.writeTo(aos);
                } catch (IOException e) {
                }
                int ascii = aos.getAscii();
                if (ascii == 1) {
                    return "7bit";
                }
                if (ascii != 2) {
                    return HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64;
                }
                return "quoted-printable";
            }
            AsciiOutputStream aos2 = new AsciiOutputStream(true, encodeEolStrict);
            try {
                dh.writeTo(aos2);
            } catch (IOException e2) {
            }
            if (aos2.getAscii() == 1) {
                return "7bit";
            }
            return HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64;
        } catch (Exception e3) {
            return HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64;
        }
    }

    public static InputStream decode(InputStream is, String encoding) throws MessagingException {
        if (encoding.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64)) {
            return new BASE64DecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPDecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUDecoderStream(is);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit")) {
            return is;
        }
        throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static OutputStream encode(OutputStream os, String encoding) throws MessagingException {
        if (encoding == null) {
            return os;
        }
        if (encoding.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64)) {
            return new BASE64EncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit")) {
            return os;
        }
        throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static OutputStream encode(OutputStream os, String encoding, String filename) throws MessagingException {
        if (encoding == null) {
            return os;
        }
        if (encoding.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64)) {
            return new BASE64EncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("quoted-printable")) {
            return new QPEncoderStream(os);
        }
        if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue")) {
            return new UUEncoderStream(os, filename);
        }
        if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit")) {
            return os;
        }
        throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static String encodeText(String text) throws UnsupportedEncodingException {
        return encodeText(text, (String) null, (String) null);
    }

    public static String encodeText(String text, String charset, String encoding) throws UnsupportedEncodingException {
        return encodeWord(text, charset, encoding, false);
    }

    public static String decodeText(String etext) throws UnsupportedEncodingException {
        String word;
        if (etext.indexOf("=?") == -1) {
            return etext;
        }
        StringTokenizer st = new StringTokenizer(etext, " \t\n\r", true);
        StringBuffer sb = new StringBuffer();
        StringBuffer wsb = new StringBuffer();
        boolean prevWasEncoded = false;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            char charAt = s.charAt(0);
            char c = charAt;
            if (charAt == ' ' || c == 9 || c == 13 || c == 10) {
                wsb.append(c);
            } else {
                try {
                    word = decodeWord(s);
                    if (!prevWasEncoded && wsb.length() > 0) {
                        sb.append(wsb);
                    }
                    prevWasEncoded = true;
                } catch (ParseException e) {
                    String word2 = s;
                    if (!decodeStrict) {
                        String dword = decodeInnerWords(word2);
                        if (dword != word2) {
                            if ((!prevWasEncoded || !word2.startsWith("=?")) && wsb.length() > 0) {
                                sb.append(wsb);
                            }
                            prevWasEncoded = word2.endsWith("?=");
                            word = dword;
                        } else {
                            if (wsb.length() > 0) {
                                sb.append(wsb);
                            }
                            prevWasEncoded = false;
                            word = word2;
                        }
                    } else {
                        if (wsb.length() > 0) {
                            sb.append(wsb);
                        }
                        prevWasEncoded = false;
                        word = word2;
                    }
                }
                sb.append(word);
                wsb.setLength(0);
            }
        }
        sb.append(wsb);
        return sb.toString();
    }

    public static String encodeWord(String word) throws UnsupportedEncodingException {
        return encodeWord(word, (String) null, (String) null);
    }

    public static String encodeWord(String word, String charset, String encoding) throws UnsupportedEncodingException {
        return encodeWord(word, charset, encoding, true);
    }

    private static String encodeWord(String string, String charset, String encoding, boolean encodingWord) throws UnsupportedEncodingException {
        String jcharset;
        boolean b64;
        int ascii = checkAscii(string);
        if (ascii == 1) {
            return string;
        }
        if (charset == null) {
            String jcharset2 = getDefaultJavaCharset();
            charset = getDefaultMIMECharset();
            jcharset = jcharset2;
        } else {
            jcharset = javaCharset(charset);
        }
        if (encoding == null) {
            if (ascii != 3) {
                encoding = "Q";
            } else {
                encoding = "B";
            }
        }
        if (encoding.equalsIgnoreCase("B")) {
            b64 = true;
        } else if (encoding.equalsIgnoreCase("Q")) {
            b64 = false;
        } else {
            throw new UnsupportedEncodingException("Unknown transfer encoding: " + encoding);
        }
        StringBuffer outb = new StringBuffer();
        doEncode(string, b64, jcharset, 68 - charset.length(), "=?" + charset + "?" + encoding + "?", true, encodingWord, outb);
        return outb.toString();
    }

    private static void doEncode(String string, boolean b64, String jcharset, int avail, String prefix, boolean first, boolean encodingWord, StringBuffer buf) throws UnsupportedEncodingException {
        int len;
        OutputStream eos;
        String str = string;
        boolean z = encodingWord;
        StringBuffer stringBuffer = buf;
        byte[] bytes = str.getBytes(jcharset);
        if (b64) {
            len = BEncoderStream.encodedLength(bytes);
        } else {
            len = QEncoderStream.encodedLength(bytes, z);
        }
        if (len > avail) {
            int length = string.length();
            int size = length;
            if (length > 1) {
                boolean z2 = b64;
                String str2 = jcharset;
                int i = avail;
                String str3 = prefix;
                boolean z3 = encodingWord;
                int size2 = size;
                doEncode(str.substring(0, size / 2), z2, str2, i, str3, first, z3, buf);
                doEncode(str.substring(size2 / 2, size2), z2, str2, i, str3, false, z3, buf);
                String str4 = prefix;
                return;
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (b64) {
            eos = new BEncoderStream(os);
        } else {
            eos = new QEncoderStream(os, z);
        }
        try {
            eos.write(bytes);
            eos.close();
        } catch (IOException e) {
        }
        byte[] encodedBytes = os.toByteArray();
        if (!first) {
            if (foldEncodedWords) {
                stringBuffer.append("\r\n ");
            } else {
                stringBuffer.append(" ");
            }
        }
        stringBuffer.append(prefix);
        for (byte b : encodedBytes) {
            stringBuffer.append((char) b);
        }
        stringBuffer.append("?=");
    }

    public static String decodeWord(String eword) throws ParseException, UnsupportedEncodingException {
        InputStream is;
        if (eword.startsWith("=?")) {
            int indexOf = eword.indexOf(63, 2);
            int pos = indexOf;
            if (indexOf != -1) {
                String charset = javaCharset(eword.substring(2, pos));
                int start = pos + 1;
                int indexOf2 = eword.indexOf(63, start);
                int pos2 = indexOf2;
                if (indexOf2 != -1) {
                    String encoding = eword.substring(start, pos2);
                    int start2 = pos2 + 1;
                    int indexOf3 = eword.indexOf("?=", start2);
                    int pos3 = indexOf3;
                    if (indexOf3 != -1) {
                        String word = eword.substring(start2, pos3);
                        try {
                            String decodedWord = "";
                            if (word.length() > 0) {
                                ByteArrayInputStream bis = new ByteArrayInputStream(ASCIIUtility.getBytes(word));
                                if (encoding.equalsIgnoreCase("B")) {
                                    is = new BASE64DecoderStream(bis);
                                } else if (encoding.equalsIgnoreCase("Q")) {
                                    is = new QDecoderStream(bis);
                                } else {
                                    throw new UnsupportedEncodingException("unknown encoding: " + encoding);
                                }
                                int count = bis.available();
                                byte[] bytes = new byte[count];
                                int count2 = is.read(bytes, 0, count);
                                if (count2 > 0) {
                                    decodedWord = new String(bytes, 0, count2, charset);
                                }
                            } else {
                                decodedWord = decodedWord;
                            }
                            if (pos3 + 2 >= eword.length()) {
                                return decodedWord;
                            }
                            String rest = eword.substring(pos3 + 2);
                            if (!decodeStrict) {
                                rest = decodeInnerWords(rest);
                            }
                            return String.valueOf(decodedWord) + rest;
                        } catch (UnsupportedEncodingException uex) {
                            throw uex;
                        } catch (IOException ioex) {
                            throw new ParseException(ioex.toString());
                        } catch (IllegalArgumentException e) {
                            throw new UnsupportedEncodingException(charset);
                        }
                    } else {
                        throw new ParseException("encoded word does not end with \"?=\": " + eword);
                    }
                } else {
                    throw new ParseException("encoded word does not include encoding: " + eword);
                }
            } else {
                throw new ParseException("encoded word does not include charset: " + eword);
            }
        } else {
            throw new ParseException("encoded word does not start with \"=?\": " + eword);
        }
    }

    private static String decodeInnerWords(String word) throws UnsupportedEncodingException {
        int end;
        int end2;
        int start = 0;
        StringBuffer buf = new StringBuffer();
        while (true) {
            int indexOf = word.indexOf("=?", start);
            int i = indexOf;
            if (indexOf >= 0) {
                buf.append(word.substring(start, i));
                int end3 = word.indexOf(63, i + 2);
                if (end3 < 0 || (end = word.indexOf(63, end3 + 1)) < 0 || (end2 = word.indexOf("?=", end + 1)) < 0) {
                    break;
                }
                String s = word.substring(i, end2 + 2);
                try {
                    s = decodeWord(s);
                } catch (ParseException e) {
                }
                buf.append(s);
                start = end2 + 2;
            } else {
                break;
            }
        }
        if (start == 0) {
            return word;
        }
        if (start < word.length()) {
            buf.append(word.substring(start));
        }
        return buf.toString();
    }

    public static String quote(String word, String specials) {
        int len = word.length();
        boolean needQuoting = false;
        for (int i = 0; i < len; i++) {
            char c = word.charAt(i);
            if (c == '\"' || c == '\\' || c == 13 || c == 10) {
                StringBuffer sb = new StringBuffer(len + 3);
                sb.append('\"');
                sb.append(word.substring(0, i));
                int lastc = 0;
                for (int j = i; j < len; j++) {
                    char cc = word.charAt(j);
                    if ((cc == '\"' || cc == '\\' || cc == 13 || cc == 10) && !(cc == 10 && lastc == 13)) {
                        sb.append('\\');
                    }
                    sb.append(cc);
                    lastc = cc;
                }
                sb.append('\"');
                return sb.toString();
            }
            if (c < ' ' || c >= 127 || specials.indexOf(c) >= 0) {
                needQuoting = true;
            }
        }
        if (!needQuoting) {
            return word;
        }
        StringBuffer sb2 = new StringBuffer(len + 2);
        sb2.append('\"');
        sb2.append(word);
        sb2.append('\"');
        return sb2.toString();
    }

    public static String fold(int used, String s) {
        char c;
        if (!foldText) {
            return s;
        }
        int end = s.length();
        while (true) {
            end--;
            if (end >= 0 && ((c = s.charAt(end)) == ' ' || c == 9 || c == 13 || c == 10)) {
            }
        }
        if (end != s.length() - 1) {
            s = s.substring(0, end + 1);
        }
        if (s.length() + used <= 76) {
            return s;
        }
        StringBuffer sb = new StringBuffer(s.length() + 4);
        char lastc = 0;
        int used2 = used;
        String s2 = s;
        while (true) {
            if (s2.length() + used2 <= 76) {
                break;
            }
            int lastspace = -1;
            int i = 0;
            while (i < s2.length() && (lastspace == -1 || used2 + i <= 76)) {
                char c2 = s2.charAt(i);
                if (!((c2 != ' ' && c2 != 9) || lastc == ' ' || lastc == 9)) {
                    lastspace = i;
                }
                lastc = c2;
                i++;
            }
            if (lastspace == -1) {
                sb.append(s2);
                s2 = "";
                break;
            }
            sb.append(s2.substring(0, lastspace));
            sb.append("\r\n");
            lastc = s2.charAt(lastspace);
            sb.append(lastc);
            s2 = s2.substring(lastspace + 1);
            used2 = 1;
        }
        sb.append(s2);
        return sb.toString();
    }

    public static String unfold(String s) {
        if (!foldText) {
            return s;
        }
        StringBuffer sb = null;
        while (true) {
            int indexOfAny = indexOfAny(s, "\r\n");
            int i = indexOfAny;
            if (indexOfAny < 0) {
                break;
            }
            int start = i;
            int l = s.length();
            int i2 = i + 1;
            if (i2 < l && s.charAt(i2 - 1) == 13 && s.charAt(i2) == 10) {
                i2++;
            }
            if (start == 0 || s.charAt(start - 1) != '\\') {
                if (i2 < l) {
                    char charAt = s.charAt(i2);
                    char c = charAt;
                    if (charAt == ' ' || c == 9) {
                        int i3 = i2 + 1;
                        while (i3 < l) {
                            char charAt2 = s.charAt(i3);
                            char c2 = charAt2;
                            if (charAt2 != ' ' && c2 != 9) {
                                break;
                            }
                            i3++;
                        }
                        if (sb == null) {
                            sb = new StringBuffer(s.length());
                        }
                        if (start != 0) {
                            sb.append(s.substring(0, start));
                            sb.append(' ');
                        }
                        s = s.substring(i3);
                    }
                }
                if (sb == null) {
                    sb = new StringBuffer(s.length());
                }
                sb.append(s.substring(0, i2));
                s = s.substring(i2);
            } else {
                if (sb == null) {
                    sb = new StringBuffer(s.length());
                }
                sb.append(s.substring(0, start - 1));
                sb.append(s.substring(start, i2));
                s = s.substring(i2);
            }
        }
        if (sb == null) {
            return s;
        }
        sb.append(s);
        return sb.toString();
    }

    private static int indexOfAny(String s, String any) {
        return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
        try {
            int len = s.length();
            for (int i = start; i < len; i++) {
                if (any.indexOf(s.charAt(i)) >= 0) {
                    return i;
                }
            }
            return -1;
        } catch (StringIndexOutOfBoundsException e) {
            return -1;
        }
    }

    public static String javaCharset(String charset) {
        Hashtable hashtable = mime2java;
        if (hashtable == null || charset == null) {
            return charset;
        }
        String alias = (String) hashtable.get(charset.toLowerCase(Locale.ENGLISH));
        return alias == null ? charset : alias;
    }

    public static String mimeCharset(String charset) {
        Hashtable hashtable = java2mime;
        if (hashtable == null || charset == null) {
            return charset;
        }
        String alias = (String) hashtable.get(charset.toLowerCase(Locale.ENGLISH));
        return alias == null ? charset : alias;
    }

    public static String getDefaultJavaCharset() {
        if (defaultJavaCharset == null) {
            String mimecs = null;
            try {
                mimecs = System.getProperty("mail.mime.charset");
            } catch (SecurityException e) {
            }
            if (mimecs == null || mimecs.length() <= 0) {
                try {
                    defaultJavaCharset = System.getProperty("file.encoding", "8859_1");
                } catch (SecurityException e2) {
                    String encoding = new InputStreamReader(new InputStream() {
                        public int read() {
                            return 0;
                        }
                    }).getEncoding();
                    defaultJavaCharset = encoding;
                    if (encoding == null) {
                        defaultJavaCharset = "8859_1";
                    }
                }
            } else {
                String javaCharset = javaCharset(mimecs);
                defaultJavaCharset = javaCharset;
                return javaCharset;
            }
        }
        return defaultJavaCharset;
    }

    static String getDefaultMIMECharset() {
        if (defaultMIMECharset == null) {
            try {
                defaultMIMECharset = System.getProperty("mail.mime.charset");
            } catch (SecurityException e) {
            }
        }
        if (defaultMIMECharset == null) {
            defaultMIMECharset = mimeCharset(getDefaultJavaCharset());
        }
        return defaultMIMECharset;
    }

    private static void loadMappings(LineInputStream is, Hashtable table) {
        while (true) {
            try {
                String currLine = is.readLine();
                if (currLine != null) {
                    if (currLine.startsWith("--") && currLine.endsWith("--")) {
                        return;
                    }
                    if (currLine.trim().length() != 0 && !currLine.startsWith("#")) {
                        StringTokenizer tk = new StringTokenizer(currLine, " \t");
                        try {
                            String key = tk.nextToken();
                            table.put(key.toLowerCase(Locale.ENGLISH), tk.nextToken());
                        } catch (NoSuchElementException e) {
                        }
                    }
                } else {
                    return;
                }
            } catch (IOException e2) {
                return;
            }
        }
    }

    static int checkAscii(String s) {
        int ascii = 0;
        int non_ascii = 0;
        int l = s.length();
        for (int i = 0; i < l; i++) {
            if (nonascii(s.charAt(i))) {
                non_ascii++;
            } else {
                ascii++;
            }
        }
        if (non_ascii == 0) {
            return 1;
        }
        if (ascii > non_ascii) {
            return 2;
        }
        return 3;
    }

    static int checkAscii(byte[] b) {
        int ascii = 0;
        int non_ascii = 0;
        for (byte b2 : b) {
            if (nonascii(b2 & 255)) {
                non_ascii++;
            } else {
                ascii++;
            }
        }
        if (non_ascii == 0) {
            return 1;
        }
        if (ascii > non_ascii) {
            return 2;
        }
        return 3;
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0081  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static int checkAscii(java.io.InputStream r17, int r18, boolean r19) {
        /*
            r0 = r18
            r1 = 0
            r2 = 0
            r3 = 4096(0x1000, float:5.74E-42)
            r4 = 0
            r5 = 0
            r6 = 0
            boolean r7 = encodeEolStrict
            r8 = 0
            if (r7 == 0) goto L_0x0012
            if (r19 == 0) goto L_0x0012
            r7 = 1
            goto L_0x0013
        L_0x0012:
            r7 = r8
        L_0x0013:
            r10 = 0
            byte[] r10 = (byte[]) r10
            r11 = -1
            if (r0 == 0) goto L_0x002e
            r12 = 4096(0x1000, float:5.74E-42)
            if (r0 != r11) goto L_0x001e
            goto L_0x0022
        L_0x001e:
            int r12 = java.lang.Math.min(r0, r12)
        L_0x0022:
            r3 = r12
            byte[] r10 = new byte[r3]
            r12 = r10
            r10 = r6
            r6 = r5
            r5 = r4
            r4 = r3
            r3 = r2
            r2 = r1
            r1 = r0
            goto L_0x0036
        L_0x002e:
            r12 = r10
            r10 = r6
            r6 = r5
            r5 = r4
            r4 = r3
            r3 = r2
            r2 = r1
            r1 = r0
        L_0x0036:
            if (r1 != 0) goto L_0x003b
            r14 = r17
            goto L_0x008a
        L_0x003b:
            r14 = r17
            int r0 = r14.read(r12, r8, r4)     // Catch:{ IOException -> 0x0088 }
            r15 = r0
            if (r0 != r11) goto L_0x0045
            goto L_0x008a
        L_0x0045:
            r0 = 0
            r16 = 0
            r8 = r16
        L_0x004a:
            if (r8 < r15) goto L_0x0053
            if (r1 == r11) goto L_0x0051
            int r1 = r1 - r15
            r8 = 0
            goto L_0x0036
        L_0x0051:
            r8 = 0
            goto L_0x0036
        L_0x0053:
            byte r11 = r12[r8]     // Catch:{ IOException -> 0x0088 }
            r11 = r11 & 255(0xff, float:3.57E-43)
            r9 = 10
            r13 = 13
            if (r7 == 0) goto L_0x0066
            if (r0 != r13) goto L_0x0061
            if (r11 != r9) goto L_0x0065
        L_0x0061:
            if (r0 == r13) goto L_0x0066
            if (r11 != r9) goto L_0x0066
        L_0x0065:
            r10 = 1
        L_0x0066:
            if (r11 == r13) goto L_0x0073
            if (r11 != r9) goto L_0x006b
            goto L_0x0073
        L_0x006b:
            int r5 = r5 + 1
            r9 = 998(0x3e6, float:1.398E-42)
            if (r5 <= r9) goto L_0x0074
            r6 = 1
            goto L_0x0074
        L_0x0073:
            r5 = 0
        L_0x0074:
            boolean r9 = nonascii(r11)     // Catch:{ IOException -> 0x0088 }
            if (r9 == 0) goto L_0x0081
            if (r19 == 0) goto L_0x007e
            r9 = 3
            return r9
        L_0x007e:
            int r3 = r3 + 1
            goto L_0x0083
        L_0x0081:
            int r2 = r2 + 1
        L_0x0083:
            r0 = r11
            int r8 = r8 + 1
            r11 = -1
            goto L_0x004a
        L_0x0088:
            r0 = move-exception
        L_0x008a:
            if (r1 != 0) goto L_0x0090
            if (r19 == 0) goto L_0x0090
            r8 = 3
            return r8
        L_0x0090:
            r8 = 3
            r0 = 2
            if (r3 != 0) goto L_0x009c
            if (r10 == 0) goto L_0x0097
            return r8
        L_0x0097:
            if (r6 == 0) goto L_0x009a
            return r0
        L_0x009a:
            r8 = 1
            return r8
        L_0x009c:
            if (r2 <= r3) goto L_0x009f
            return r0
        L_0x009f:
            r8 = 3
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeUtility.checkAscii(java.io.InputStream, int, boolean):int");
    }

    static final boolean nonascii(int b) {
        if (b < 127) {
            return (b >= 32 || b == 13 || b == 10 || b == 9) ? false : true;
        }
        return true;
    }
}
