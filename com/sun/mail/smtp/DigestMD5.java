package com.sun.mail.smtp;

import com.sec.internal.helper.header.AuthenticationInfoHeader;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class DigestMD5 {
    private static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private String clientResponse;
    private PrintStream debugout;
    private MessageDigest md5;
    private String uri;

    public DigestMD5(PrintStream debugout2) {
        this.debugout = debugout2;
        if (debugout2 != null) {
            debugout2.println("DEBUG DIGEST-MD5: Loaded");
        }
    }

    public byte[] authClient(String host, String user, String passwd, String realm, String serverChallenge) throws IOException {
        String realm2;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
        try {
            SecureRandom random = new SecureRandom();
            this.md5 = MessageDigest.getInstance("MD5");
            StringBuffer result = new StringBuffer();
            StringBuilder sb = new StringBuilder("smtp/");
            String str = host;
            sb.append(str);
            this.uri = sb.toString();
            byte[] bytes = new byte[32];
            PrintStream printStream = this.debugout;
            if (printStream != null) {
                printStream.println("DEBUG DIGEST-MD5: Begin authentication ...");
            }
            Hashtable map = tokenize(serverChallenge);
            if (realm == null) {
                String text = (String) map.get("realm");
                if (text != null) {
                    realm2 = new StringTokenizer(text, ",").nextToken();
                } else {
                    realm2 = str;
                }
            } else {
                realm2 = realm;
            }
            String nonce = (String) map.get(WwwAuthenticateHeader.HEADER_PARAM_NONCE);
            random.nextBytes(bytes);
            b64os.write(bytes);
            b64os.flush();
            String cnonce = bos.toString();
            bos.reset();
            MessageDigest messageDigest = this.md5;
            SecureRandom secureRandom = random;
            messageDigest.update(messageDigest.digest(ASCIIUtility.getBytes(String.valueOf(user) + ":" + realm2 + ":" + passwd)));
            MessageDigest messageDigest2 = this.md5;
            messageDigest2.update(ASCIIUtility.getBytes(":" + nonce + ":" + cnonce));
            this.clientResponse = String.valueOf(toHex(this.md5.digest())) + ":" + nonce + ":" + "00000001" + ":" + cnonce + ":" + "auth" + ":";
            MessageDigest messageDigest3 = this.md5;
            StringBuilder sb2 = new StringBuilder("AUTHENTICATE:");
            sb2.append(this.uri);
            messageDigest3.update(ASCIIUtility.getBytes(sb2.toString()));
            MessageDigest messageDigest4 = this.md5;
            StringBuilder sb3 = new StringBuilder(String.valueOf(this.clientResponse));
            sb3.append(toHex(this.md5.digest()));
            messageDigest4.update(ASCIIUtility.getBytes(sb3.toString()));
            result.append("username=\"" + user + "\"");
            result.append(",realm=\"" + realm2 + "\"");
            StringBuilder sb4 = new StringBuilder(",qop=");
            sb4.append("auth");
            result.append(sb4.toString());
            result.append(",nc=" + "00000001");
            result.append(",nonce=\"" + nonce + "\"");
            result.append(",cnonce=\"" + cnonce + "\"");
            result.append(",digest-uri=\"" + this.uri + "\"");
            StringBuilder sb5 = new StringBuilder(",response=");
            sb5.append(toHex(this.md5.digest()));
            result.append(sb5.toString());
            PrintStream printStream2 = this.debugout;
            if (printStream2 != null) {
                printStream2.println("DEBUG DIGEST-MD5: Response => " + result.toString());
            }
            b64os.write(ASCIIUtility.getBytes(result.toString()));
            b64os.flush();
            return bos.toByteArray();
        } catch (NoSuchAlgorithmException ex) {
            String str2 = user;
            String str3 = serverChallenge;
            PrintStream printStream3 = this.debugout;
            if (printStream3 != null) {
                printStream3.println("DEBUG DIGEST-MD5: " + ex);
            }
            throw new IOException(ex.toString());
        }
    }

    public boolean authServer(String serverResponse) throws IOException {
        Hashtable map = tokenize(serverResponse);
        MessageDigest messageDigest = this.md5;
        messageDigest.update(ASCIIUtility.getBytes(":" + this.uri));
        MessageDigest messageDigest2 = this.md5;
        messageDigest2.update(ASCIIUtility.getBytes(String.valueOf(this.clientResponse) + toHex(this.md5.digest())));
        String text = toHex(this.md5.digest());
        if (text.equals((String) map.get(AuthenticationInfoHeader.HEADER_PARAM_RSP_AUTH))) {
            return true;
        }
        PrintStream printStream = this.debugout;
        if (printStream == null) {
            return false;
        }
        printStream.println("DEBUG DIGEST-MD5: Expected => rspauth=" + text);
        return false;
    }

    private Hashtable tokenize(String serverResponse) throws IOException {
        Hashtable map = new Hashtable();
        byte[] bytes = serverResponse.getBytes();
        String key = null;
        StreamTokenizer tokens = new StreamTokenizer(new InputStreamReader(new BASE64DecoderStream(new ByteArrayInputStream(bytes, 4, bytes.length - 4))));
        tokens.ordinaryChars(48, 57);
        tokens.wordChars(48, 57);
        while (true) {
            int nextToken = tokens.nextToken();
            int ttype = nextToken;
            if (nextToken == -1) {
                return map;
            }
            if (ttype != -3) {
                if (ttype != 34) {
                }
            } else if (key == null) {
                key = tokens.sval;
            }
            PrintStream printStream = this.debugout;
            if (printStream != null) {
                printStream.println("DEBUG DIGEST-MD5: Received => " + key + "='" + tokens.sval + "'");
            }
            if (map.containsKey(key)) {
                map.put(key, map.get(key) + "," + tokens.sval);
            } else {
                map.put(key, tokens.sval);
            }
            key = null;
        }
    }

    private static String toHex(byte[] bytes) {
        char[] result = new char[(bytes.length * 2)];
        int i = 0;
        for (byte b : bytes) {
            int temp = b & 255;
            int i2 = i + 1;
            char[] cArr = digits;
            result[i] = cArr[temp >> 4];
            i = i2 + 1;
            result[i2] = cArr[temp & 15];
        }
        return new String(result);
    }
}
