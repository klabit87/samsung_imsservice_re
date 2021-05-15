package com.sec.internal.ims.cmstore.utils;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.StringGenerator;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.Link;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import com.sec.internal.omanetapi.nms.data.PayloadPartInfo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import org.xbill.DNS.KEYRecord;

public class Util {
    protected static final int FILE_RENAME_HASHVALUE_LEN = 5;
    private static final String HTTP = "http:";
    private static final String HTTPS = "https:";
    private static final String LOG_TAG = Util.class.getSimpleName();
    protected static final int MAX_FILE_NAME_LEN = 50;
    private static final long MAX_NOT_SYNC_TIME = 259200000;
    protected static final String mMMSPartsDir = (File.separatorChar + "MMS_files");
    protected static final String mRCSFilesDir = (File.separatorChar + "RCS_files");

    protected static String getIncomingFileDestinationDir(Context mContext, boolean isMMS) throws IOException {
        String folderName;
        String dir;
        if (isMMS) {
            folderName = mMMSPartsDir;
        } else {
            folderName = mRCSFilesDir;
        }
        String dir2 = null;
        if (mContext == null) {
            return null;
        }
        File filedir = mContext.getExternalFilesDir((String) null);
        if (filedir != null) {
            dir2 = filedir.getAbsolutePath();
        }
        if (dir2 == null) {
            return null;
        }
        File folder = new File(dir2 + folderName);
        if (folder.exists()) {
            dir = dir2 + folderName;
        } else if (folder.mkdir()) {
            dir = dir2 + folderName;
        } else {
            Log.e(LOG_TAG, "can not create dir");
            return null;
        }
        if (folder.exists()) {
            File nomedia = new File(dir + File.separatorChar + ".nomedia");
            if (!nomedia.exists()) {
                try {
                    boolean isCreate = nomedia.createNewFile();
                    String str = LOG_TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("create .nomedia file in ");
                    sb.append(dir);
                    sb.append(" : ");
                    sb.append(isCreate ? "successful" : "failed");
                    Log.d(str, sb.toString());
                } catch (IOException e) {
                    Log.e(LOG_TAG, "makeDirectoryToCopyImage, cannot create .nomedia file");
                    e.printStackTrace();
                }
            }
        }
        return dir;
    }

    public static String generateUniqueFilePath(Context mContext, String filefullname, boolean isMMS) throws IOException {
        if (!TextUtils.isEmpty(filefullname)) {
            return FilePathGenerator.generateUniqueFilePath(getIncomingFileDestinationDir(mContext, isMMS), filefullname, 50);
        }
        throw new IOException();
    }

    public static String getRandomFileName(String extension) {
        if (TextUtils.isEmpty(extension)) {
            return StringGenerator.generateString(5, 5);
        }
        return StringGenerator.generateString(5, 5) + "." + extension;
    }

    public static void saveFiletoPath(byte[] data, String path) throws IOException {
        if (data != null && path != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                fos.write(data);
                fos.close();
            } catch (Throwable th) {
                if (fos != null) {
                    fos.close();
                }
                throw th;
            }
        }
    }

    public static String convertStreamToString(InputStream data) throws IOException {
        if (data == null) {
            return "";
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[2000];
            while (true) {
                int read = data.read(buf);
                int len = read;
                if (read > 0) {
                    byteArrayOutputStream.write(buf, 0, len);
                } else {
                    return new String(byteArrayOutputStream.toByteArray());
                }
            }
        } finally {
            byteArrayOutputStream.close();
        }
    }

    public static long saveInputStreamtoPath(InputStream data, String path) throws IOException {
        long totalsaved = 0;
        if (!(data == null || path == null)) {
            FileOutputStream out = null;
            try {
                FileOutputStream out2 = new FileOutputStream(path);
                byte[] buf = new byte[KEYRecord.Flags.FLAG2];
                while (true) {
                    int read = data.read(buf);
                    int len = read;
                    if (read <= 0) {
                        break;
                    }
                    out2.write(buf, 0, len);
                    totalsaved += (long) len;
                }
                out2.close();
            } catch (Throwable th) {
                if (out != null) {
                    out.close();
                }
                throw th;
            }
        }
        return totalsaved;
    }

    public static long saveMimeBodyToPath(MimeMultipart mimepart, String filepath) throws IOException, MessagingException {
        FileOutputStream out = new FileOutputStream(new File(filepath));
        try {
            mimepart.writeTo(out);
        } catch (Throwable th) {
        }
        out.close();
        return (long) mimepart.getCount();
    }

    public static String decodeUrlFromServer(String input) {
        String value;
        if (TextUtils.isEmpty(input)) {
            return null;
        }
        try {
            value = URLDecoder.decode(input, "UTF-8").replace(' ', '+');
        } catch (UnsupportedEncodingException e) {
            String str = LOG_TAG;
            Log.e(str, "decodeUrlFromServer: " + e.getMessage());
            e.printStackTrace();
            value = input;
        }
        String str2 = LOG_TAG;
        Log.d(str2, "decodeUrlFromServer to: " + value);
        return value;
    }

    public static String getFileNamefromContentType(String contentType) {
        String[] stringarray;
        String[] nameSplit;
        String value = "download";
        if (TextUtils.isEmpty(contentType) || (stringarray = contentType.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)) == null || stringarray.length < 2) {
            return value;
        }
        String name = stringarray[1];
        if (name.contains(";") && (nameSplit = name.split(";")) != null && nameSplit.length > 1) {
            name = nameSplit[0];
        }
        if (name.contains("\"")) {
            String[] namesplit = name.split("\"");
            if (namesplit != null && namesplit.length > 1) {
                value = namesplit[1];
            }
        } else {
            value = name;
        }
        String str = LOG_TAG;
        Log.d(str, "getFileNamefromContentType: " + contentType + " to: " + value);
        return value;
    }

    public static String getFileNamefromContentDisposition(String contentDisposition) {
        String[] stringarray;
        String value = "";
        if (TextUtils.isEmpty(contentDisposition) || (stringarray = contentDisposition.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)) == null || stringarray.length < 2) {
            return value;
        }
        String filename = stringarray[1];
        if (filename.contains("\"")) {
            String[] namesplit = filename.split("\"");
            if (namesplit != null && namesplit.length > 1) {
                value = namesplit[1];
            }
        } else {
            value = filename;
        }
        String str = LOG_TAG;
        Log.d(str, "getFileNamefromContentDisposition: " + contentDisposition + " to: " + value);
        return value;
    }

    public static String findParaStr(String originalStr, String target) {
        String extension;
        Log.d(LOG_TAG, "findParaStr: " + originalStr);
        if (TextUtils.isEmpty(originalStr) || TextUtils.isEmpty(target)) {
            return "";
        }
        String[] paraList = originalStr.split(";");
        int length = paraList.length;
        int i = 0;
        while (i < length) {
            String[] list = paraList[i].split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            if (list.length <= 1 || !list[0].trim().equalsIgnoreCase(target)) {
                i++;
            } else {
                String value = list[1].replaceAll("\"", "");
                if (originalStr.lastIndexOf(".") < 0) {
                    Log.d(LOG_TAG, "no extension, to: " + value);
                    return value;
                }
                String fullExtension = originalStr.substring(originalStr.lastIndexOf(".") + 1);
                if (fullExtension.indexOf("\"") > 0) {
                    extension = fullExtension.substring(0, fullExtension.indexOf("\""));
                } else {
                    extension = fullExtension.substring(0);
                }
                if (!value.endsWith(extension)) {
                    value = "file." + extension;
                }
                Log.d(LOG_TAG, "findParaStr, value to: " + value);
                return value;
            }
        }
        Log.d(LOG_TAG, "findParaStr, to: " + "");
        return "";
    }

    public static String generateLocation(PayloadPartInfo payloadPartInfo) {
        String location;
        String str = LOG_TAG;
        Log.d(str, "contentType=" + payloadPartInfo.contentType + ", contentDisposition=" + payloadPartInfo.contentDisposition + ", contentLocation=" + payloadPartInfo.contentLocation + ", contentId=" + payloadPartInfo.contentId);
        if (!TextUtils.isEmpty(payloadPartInfo.contentType) && (location = getFileNamefromContentType(payloadPartInfo.contentType)) != null && !location.equals("download") && !location.equalsIgnoreCase("UTF-8")) {
            return location;
        }
        if (!TextUtils.isEmpty(payloadPartInfo.contentDisposition)) {
            String location2 = findParaStr(payloadPartInfo.contentDisposition, "filename");
            if (!TextUtils.isEmpty(location2)) {
                return location2;
            }
        }
        if (payloadPartInfo.contentLocation != null && !TextUtils.isEmpty(payloadPartInfo.contentLocation.getPath())) {
            return payloadPartInfo.contentLocation.getPath();
        }
        if (!TextUtils.isEmpty(payloadPartInfo.contentId)) {
            return payloadPartInfo.contentId;
        }
        return getRandomFileName((String) null);
    }

    public static String generateUniqueFileName(PayloadPartInfo payloadPartInfo) {
        String originalFileName = generateLocation(payloadPartInfo);
        try {
            originalFileName = URLDecoder.decode(originalFileName, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            Log.e(LOG_TAG, "generateUniqueFileName - " + e.getMessage());
        }
        String[] stringarray = originalFileName.split("\\.");
        String extension = "";
        if (stringarray != null && stringarray.length >= 2) {
            extension = stringarray[stringarray.length - 1];
            originalFileName = originalFileName.substring(0, (originalFileName.length() - extension.length()) - 1);
        }
        String originalFileName2 = originalFileName + "_" + StringGenerator.generateString(5, 5);
        if (!TextUtils.isEmpty(extension)) {
            originalFileName2 = originalFileName2 + "." + extension;
        }
        Log.d(LOG_TAG, "generateUniqueFileName() final originalFileName: " + originalFileName2 + "extension: " + extension);
        return originalFileName2;
    }

    public static ImsUri getNormalizedTelUri(String number) {
        String str = LOG_TAG;
        Log.i(str, "getNormalizedTelUri: " + IMSLog.checker(number));
        if (number != null && !number.contains("#") && !number.contains("*") && !number.contains(",") && !number.contains("N")) {
            return UriUtil.parseNumber(number, "US");
        }
        return null;
    }

    public static String getPhoneNum(String str) {
        if (str == null) {
            return null;
        }
        int i = str.indexOf(58);
        if (i > 0) {
            str = str.substring(i + 1, str.length());
        }
        int j = str.indexOf(64);
        if (j > 0) {
            return str.substring(0, j);
        }
        return str;
    }

    public static String getTelUri(String number) {
        String str = LOG_TAG;
        Log.i(str, "getTelUri: " + IMSLog.checker(number));
        if (number == null || number.contains("#") || number.contains("*") || number.contains(",") || number.contains("N")) {
            return null;
        }
        if (number.startsWith("tel:")) {
            return number;
        }
        ImsUri imsuri = ImsUri.parse(number);
        if (imsuri == null) {
            ImsUri uri = getNormalizedTelUri(number);
            if (uri != null) {
                return uri.toString();
            }
            Log.d(LOG_TAG, "getTelUri: parsing fail, return original number");
            return number;
        }
        String msisdn = UriUtil.getMsisdnNumber(imsuri);
        if (TextUtils.isEmpty(msisdn)) {
            return number;
        }
        ImsUri teluri = UriUtil.parseNumber(msisdn, "us");
        return teluri == null ? number : teluri.toString();
    }

    public static String getMsisdn(String number) {
        String str = LOG_TAG;
        Log.d(str, "getMsisdn: " + IMSLog.checker(number));
        if (number == null || number.contains("#") || number.contains("*") || number.contains(",") || number.contains("N")) {
            return null;
        }
        ImsUri imsuri = ImsUri.parse(number);
        if (imsuri != null) {
            return imsuri.getMsisdn();
        }
        ImsUri uri = getNormalizedTelUri(number);
        if (uri == null) {
            return number;
        }
        return uri.getMsisdn();
    }

    public static void deleteFilesinMmsBufferFolder() {
        String[] children;
        File dir = new File(mMMSPartsDir);
        if (dir.exists() && dir.isDirectory() && (children = dir.list()) != null) {
            for (int i = 0; i < children.length; i++) {
                try {
                    new File(dir, children[i]).delete();
                } catch (SecurityException e) {
                    String str = LOG_TAG;
                    Log.e(str, "deleteFilesinMmsBufferFolder: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getLineTelUriFromObjUrl(String objUrl) {
        String decodedurl = decodeUrlFromServer(objUrl);
        String str = LOG_TAG;
        Log.d(str, "getLineTelUriFromObjUrl: " + IMSLog.checker(decodedurl));
        if (decodedurl == null) {
            return objUrl;
        }
        String[] values = decodedurl.split("/");
        if (values == null) {
            return null;
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i].contains("tel:+")) {
                return values[i];
            }
        }
        return null;
    }

    public static String extractObjIdFromResUrl(String resUrl) {
        String decodedurl = decodeUrlFromServer(resUrl);
        if (decodedurl == null) {
            return resUrl;
        }
        String objId = decodedurl.substring(decodedurl.lastIndexOf(47) + 1);
        String str = LOG_TAG;
        Log.d(str, "extractObjIdFromResUrl: " + IMSLog.checker(objId));
        return objId;
    }

    public static String generateHash() {
        try {
            return HashManager.generateHash(new Timestamp(new Date().getTime()).toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String replaceUrlPrefix(String url, String newHeader) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(newHeader) || url.startsWith(newHeader)) {
            return url;
        }
        if (url.startsWith(HTTPS)) {
            return url.replaceFirst(HTTPS, newHeader);
        }
        if (url.startsWith(HTTP)) {
            return url.replaceFirst(HTTP, newHeader);
        }
        return url;
    }

    public static String replaceUrlPrefix(String url, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        String newHeader = iCloudMessageManagerHelper.getProtocol() + ":";
        Log.d(LOG_TAG, "replaceUrlPrefix with" + newHeader);
        return replaceUrlPrefix(url, newHeader);
    }

    public static String replaceHostOfURL(String host, String urlstr) {
        try {
            URL url = new URL(urlstr);
            return new URL(url.getProtocol(), host, url.getFile()).toString();
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String[] parseEmailOverSlm(ImsUri originalSender, String message) {
        if (originalSender != null && originalSender.getUser() != null && !couldBeEmailGateway(originalSender.getUser())) {
            return null;
        }
        String[] parts = message.split("( /)|( )", 2);
        if (parts.length < 2) {
            return null;
        }
        int len = message.length();
        int firstAt = message.indexOf(64);
        int lastAt = message.lastIndexOf(64);
        int firstDot = message.indexOf(46, lastAt + 1);
        int lastDot = message.lastIndexOf(46);
        if (firstAt <= 0 || firstAt != lastAt || lastAt + 1 >= firstDot || firstDot > lastDot || lastDot >= len - 1) {
            return null;
        }
        return parts;
    }

    private static boolean couldBeEmailGateway(String address) {
        return address.length() <= 4;
    }

    public static boolean isSimExist(Context context) {
        TelephonyManager tm;
        if (context == null || (tm = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)) == null || tm.getSimState() == 1) {
            return false;
        }
        return true;
    }

    public static String getImei(Context context) {
        TelephonyManager tm;
        if (context == null || (tm = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)) == null) {
            return "";
        }
        try {
            return (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getImei", new Class[0]), tm, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isWifiCallingEnabled(Context context) {
        if (Settings.Global.getInt(context.getContentResolver(), VowifiConfig.WIFI_CALL_ENABLE, 0) == 1 || Settings.System.getInt(context.getContentResolver(), "wifi_call_enable1", 0) == 1) {
            Log.d(LOG_TAG, "Wi-Fi Calling is Enabled");
            return true;
        }
        Log.d(LOG_TAG, "Wi-Fi Calling is Disabled");
        return false;
    }

    public static long parseTimeStamp(String datevalue) {
        int i = 0;
        SimpleDateFormat[] sFormatOfName = {new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault()), new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault())};
        int length = sFormatOfName.length;
        while (i < length) {
            try {
                Date date = sFormatOfName[i].parse(datevalue);
                if (date != null) {
                    return date.getTime();
                }
                return System.currentTimeMillis();
            } catch (ParseException e) {
                String str = LOG_TAG;
                Log.e(str, "ParseException: " + e.getMessage());
                i++;
            }
        }
        return -1;
    }

    public static boolean isOver72Hours(String msgDate) {
        if (TextUtils.isEmpty(msgDate)) {
            return false;
        }
        long msgRecTime = parseTimeStamp(msgDate);
        long onLineTime = CloudMessagePreferenceManager.getInstance().getNetworkAvailableTime();
        long interval = onLineTime - msgRecTime;
        if (onLineTime == -1 || msgRecTime == -1 || interval <= MAX_NOT_SYNC_TIME) {
            return false;
        }
        Log.d(LOG_TAG, "over 72 hours");
        return true;
    }

    public static boolean isMatchedSubscriptionID(NotificationList notiList) {
        boolean matched = false;
        String subscriptionUrl = CloudMessagePreferenceManager.getInstance().getOMASubscriptionResUrl();
        if (!TextUtils.isEmpty(subscriptionUrl)) {
            String subscriptionID = getLastPathFromUrl(subscriptionUrl);
            String str = LOG_TAG;
            Log.d(str, "isMatchedSubscriptionID subscriptionID = " + subscriptionID);
            Link[] linkArr = notiList.nmsEventList.link;
            int length = linkArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                Link l = linkArr[i];
                if ((PhoneConstants.SUBSCRIPTION_KEY.equalsIgnoreCase(l.rel) || "NmsSubscription".equalsIgnoreCase(l.rel)) && l.href != null && subscriptionID.equalsIgnoreCase(getLastPathFromUrl(l.href.toString()))) {
                    matched = true;
                    break;
                }
                i++;
            }
        }
        String subscriptionID2 = LOG_TAG;
        Log.d(subscriptionID2, "isMatchedSubscriptionID " + matched);
        return matched;
    }

    public static String getLastPathFromUrl(String url) {
        String[] subPaths = url.split("/");
        return subPaths[subPaths.length - 1];
    }
}
