package com.sec.internal.log;

import android.os.Environment;
import android.os.SemSystemProperties;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IMSLog {
    private static final boolean DEBUG_MODE;
    private static final boolean ENG_MODE = SemSystemProperties.get("ro.build.type", "user").equals("eng");
    private static final String EOL = System.getProperty("line.separator", "\n");
    private static final String INDENT = "  ";
    private static final int MAX_DUMP_LEN = 1024;
    private static String SALES_CODE = null;
    private static final boolean SHIP_BUILD = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
    private static final String TAG = IMSLog.class.getSimpleName();
    private static EncryptedLogger encryptedLogger = EncryptedLogger.getInstance();
    private static boolean mIsOtpAuthorized = false;
    private static boolean mIsUt = false;
    private static final int mLengthOfPlaneText = 4;
    private static HashSet<String> mShowSLogInShipBuildSet;
    private static ByteBuffer sByteBuffer = null;
    private static long sDumpStartTime = 0;
    private static FileChannel sFileChannel = null;
    private static Map<String, String> sIndent = new ConcurrentHashMap();
    private static PrintWriter sPw = null;

    public enum LAZER_TYPE {
        CALL,
        REGI
    }

    static {
        boolean z = true;
        if (SemSystemProperties.getInt("ro.debuggable", 0) != 1) {
            z = false;
        }
        DEBUG_MODE = z;
        String str = SemSystemProperties.get(OmcCode.PERSIST_OMC_CODE_PROPERTY, "");
        SALES_CODE = str;
        if (TextUtils.isEmpty(str)) {
            SALES_CODE = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, "");
        }
        HashSet<String> hashSet = new HashSet<>();
        mShowSLogInShipBuildSet = hashSet;
        hashSet.add("ATX");
        mShowSLogInShipBuildSet.add("OMX");
        mShowSLogInShipBuildSet.add("VDR");
        mShowSLogInShipBuildSet.add("VDP");
        mShowSLogInShipBuildSet.add("VOP");
    }

    private static String getImsDumpPath() {
        File dir;
        String rtn = null;
        String path = Environment.getExternalStorageDirectory().toString();
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        try {
            File dir2 = new File(path.concat("/log/ims_logs/"));
            if (dir2.exists()) {
                File[] subDirs = dir2.listFiles(new FileFilter() {
                    public boolean accept(File path) {
                        return path.isDirectory();
                    }
                });
                long lastModified = -1;
                int length = subDirs.length;
                int i = 0;
                while (i < length) {
                    File subDir = subDirs[i];
                    boolean isValid = false;
                    File[] listFiles = subDir.listFiles();
                    int length2 = listFiles.length;
                    int i2 = 0;
                    while (i2 < length2) {
                        File sub = listFiles[i2];
                        String name = sub.getName();
                        int pos = name.lastIndexOf(".");
                        if (!sub.isFile() || pos <= 0) {
                            dir = dir2;
                        } else {
                            dir = dir2;
                            if (subDir.getName().endsWith(name.substring(0, pos))) {
                                isValid = true;
                            }
                        }
                        i2++;
                        dir2 = dir;
                    }
                    File dir3 = dir2;
                    if (isValid && lastModified < subDir.lastModified()) {
                        rtn = subDir.getAbsolutePath();
                        lastModified = subDir.lastModified();
                    }
                    i++;
                    dir2 = dir3;
                }
                return rtn;
            }
            return null;
        } catch (SecurityException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void dumpToFile(String msg) {
        if (sFileChannel != null) {
            synchronized (IMSLog.class) {
                if (sFileChannel != null) {
                    try {
                        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
                        int offset = 0;
                        while (offset < msgBytes.length) {
                            int length = Math.min(1024, msgBytes.length - offset);
                            sByteBuffer.put(msgBytes, offset, length);
                            offset += length;
                            sByteBuffer.flip();
                            sFileChannel.write(sByteBuffer);
                            sByteBuffer.clear();
                        }
                        sByteBuffer.put(EOL.getBytes(StandardCharsets.UTF_8));
                        sByteBuffer.flip();
                        sFileChannel.write(sByteBuffer);
                        sByteBuffer.clear();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public static void increaseIndent(String tag) {
        if (!sIndent.containsKey(tag)) {
            sIndent.put(tag, "");
        }
        Map<String, String> map = sIndent;
        map.put(tag, map.get(tag).concat(INDENT));
    }

    public static void decreaseIndent(String tag) {
        if (sIndent.containsKey(tag)) {
            Map<String, String> map = sIndent;
            map.put(tag, map.get(tag).replaceFirst(INDENT, ""));
        }
    }

    public static void prepareDump(PrintWriter pw) {
        FileInputStream fis;
        if (sFileChannel == null) {
            synchronized (IMSLog.class) {
                if (sFileChannel == null) {
                    sPw = pw;
                    String imsDumpPath = getImsDumpPath();
                    if (!TextUtils.isEmpty(imsDumpPath)) {
                        try {
                            sFileChannel = new FileOutputStream(new File(imsDumpPath.concat("/ims_dump.log")), true).getChannel();
                            sByteBuffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder());
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US);
                            sDumpStartTime = SystemClock.elapsedRealtime();
                            String str = TAG;
                            dump(str, "dump started at " + sdf.format(new Date()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            postDump(pw);
                        }
                    } else {
                        postDump(pw);
                    }
                }
            }
        }
        try {
            fis = new FileInputStream("/efs/sec_efs/.otp_auth");
            byte[] buffer = new byte[1024];
            if (fis.read(buffer) > 0) {
                mIsOtpAuthorized = CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(new String(buffer).trim());
            }
            fis.close();
            return;
        } catch (IOException e2) {
            String str2 = TAG;
            d(str2, "IOException:" + e2);
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* JADX INFO: finally extract failed */
    public static void postDump(PrintWriter pw) {
        if (sFileChannel != null) {
            synchronized (IMSLog.class) {
                if (sFileChannel != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US);
                    String str = TAG;
                    dump(str, "dump finished at " + sdf.format(new Date()));
                    if (sDumpStartTime > 0) {
                        long elapsedTime = SystemClock.elapsedRealtime() - sDumpStartTime;
                        String str2 = TAG;
                        dump(str2, "elapsed time: " + elapsedTime + "msecs");
                    }
                    sDumpStartTime = 0;
                    try {
                        sFileChannel.close();
                        sFileChannel = null;
                    } catch (IOException e) {
                        try {
                            e.printStackTrace();
                            sFileChannel = null;
                        } catch (Throwable th) {
                            sFileChannel = null;
                            throw th;
                        }
                    }
                    if (pw != null) {
                        pw.close();
                    }
                    sPw = null;
                }
                if (sByteBuffer != null) {
                    sByteBuffer.clear();
                    sByteBuffer = null;
                }
            }
        }
        mIsOtpAuthorized = false;
    }

    public static void dump(String tag, String msg) {
        dump(tag, 0, msg, true);
    }

    public static void dump(String tag, int phoneId, String msg) {
        dump(tag, phoneId, msg, true);
    }

    public static void dump(String tag, String msg, boolean needShipCheck) {
        dump(tag, 0, msg, needShipCheck);
    }

    public static void dump(String tag, int phoneId, String msg, boolean needShipCheck) {
        String indent = sIndent.get(tag);
        if (indent != null) {
            msg = indent + msg;
        }
        String filteredLog = filterLog(msg, needShipCheck);
        dumpToFile(filteredLog);
        PrintWriter printWriter = sPw;
        if (printWriter != null) {
            printWriter.println(filteredLog);
        }
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void d(String tag, int phoneId, String msg) {
        Log.d(tag + "<" + phoneId + ">", msg);
    }

    public static void d(String tag, int phoneId, IRegisterTask task, String msg) {
        Log.d(tag + "<" + phoneId + ">", "[" + task.getProfile().getName() + "|" + task.getState() + "] " + msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void i(String tag, int phoneId, String msg) {
        Log.i(tag + "<" + phoneId + ">", msg);
    }

    public static void i(String tag, int phoneId, IRegisterTask task, String msg) {
        Log.i(tag + "<" + phoneId + ">", "[" + task.getProfile().getName() + "|" + task.getState() + "] " + msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, int phoneId, String msg) {
        Log.e(tag + "<" + phoneId + ">", msg);
    }

    public static void e(String tag, int phoneId, IRegisterTask task, String msg) {
        Log.e(tag + "<" + phoneId + ">", "[" + task.getProfile().getName() + "|" + task.getState() + "] " + msg);
    }

    public static void s(String tag, String msg) {
        if (!isShipBuild()) {
            Log.d(tag, msg);
        }
    }

    public static void s(String tag, int phoneId, String msg) {
        if (!isShipBuild()) {
            Log.d(tag + "<" + phoneId + ">", msg);
        }
    }

    public static void s(String tag, int phoneId, IRegisterTask task, String msg) {
        if (!isShipBuild()) {
            Log.d(tag + "<" + phoneId + ">", "[" + task.getProfile().getName() + "|" + task.getState() + "] " + msg);
        }
    }

    public static void g(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static String checker(Object obj) {
        return checker(obj, false);
    }

    public static String checker(Object obj, boolean allowAtUt) {
        if (obj == null) {
            return null;
        }
        if (isShipBuild() && (!allowAtUt || !mIsUt)) {
            return "xxxxx";
        }
        return "" + obj;
    }

    public static String numberChecker(String log) {
        return numberChecker(log, 4, false);
    }

    public static String numberChecker(String log, int lengthOfDigit) {
        return numberChecker(log, lengthOfDigit, false);
    }

    public static String numberChecker(String log, int lengthOfDigit, boolean allowAtUt) {
        if (log == null) {
            return null;
        }
        if (!isShipBuild() || (allowAtUt && mIsUt)) {
            return log;
        }
        return log.replaceAll("\\d(?=\\d{2})", "*");
    }

    public static boolean isShipBuild() {
        return SHIP_BUILD && !mShowSLogInShipBuildSet.contains(SALES_CODE);
    }

    public static boolean isEngMode() {
        return ENG_MODE;
    }

    public static void lazer(LAZER_TYPE type, String log) {
        if (type == LAZER_TYPE.CALL) {
            e("#IMSCALL", log);
        } else if (type == LAZER_TYPE.REGI) {
            e("#IMSREGI", log);
        }
    }

    public static void lazer(IRegisterTask task, String log) {
        e("#IMSREGI", "(" + task.getPhoneId() + ", " + task.getProfile().getName() + ") " + log);
    }

    private static String filterLog(String log, boolean needShipCheck) {
        if (!mIsOtpAuthorized && needShipCheck && isShipBuild()) {
            return log.replaceAll("\\d(?=\\d{2})", "*");
        }
        return log;
    }

    public static void c(int logClass) {
        c(logClass, (String) null);
    }

    public static void c(int logClass, String description) {
        c(logClass, description, false);
    }

    public static void c(int logClass, String description, boolean flush) {
        CriticalLogger.getInstance().write(logClass, description);
        if (flush) {
            CriticalLogger.getInstance().flush();
        }
    }

    public static String vx(String tag, String message) {
        return x(tag, message, (Throwable) null, 2);
    }

    public static String dx(String tag, String message) {
        return x(tag, message, (Throwable) null, 3);
    }

    public static String ex(String tag, String message, Throwable t) {
        return x(tag, message, t, 6);
    }

    private static String x(String tag, String message, Throwable t, int logLevel) {
        String finalMessage = message;
        if (t != null) {
            StackTraceElement[] stElements = t.getStackTrace();
            StringBuffer sb = new StringBuffer();
            sb.append(message);
            if (t.getMessage() != null) {
                sb.append("\n");
                sb.append(t.getMessage());
            }
            if (stElements.length > 0) {
                sb.append("\n");
                for (StackTraceElement ste : stElements) {
                    sb.append(ste.toString());
                    sb.append("\n");
                }
                sb.append("\n");
            }
            finalMessage = sb.toString();
        }
        return encryptedLogger.doLog(tag, finalMessage, logLevel);
    }

    public static void dumpSecretKey(String tag) {
        String secretKey = encryptedLogger.getBase64EncodedSecretKey();
        if (DEBUG_MODE) {
            secretKey = secretKey + "\n" + encryptedLogger._debug_GetSecretKeyInfo();
        }
        if (secretKey != null) {
            dump(tag, secretKey, false);
        } else {
            dump(tag, "Secret key is not ready");
        }
    }

    public static void setIsUt(boolean isUt) {
        mIsUt = isUt;
    }
}
