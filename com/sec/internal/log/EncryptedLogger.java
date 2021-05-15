package com.sec.internal.log;

import android.os.FileObserver;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.imscr.LogClass;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.xbill.DNS.KEYRecord;

public class EncryptedLogger {
    private static final String B64PublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4HnSCdRy3WviYMvfRDtEcLAQU3Mi3et4f9W0ivmrMc1B+5LUEoWbrb6Rb5IKf7BI7qRflHKOfn1a9R1pYEBaBnrNrQHuIOhG4b3zYkAU+i093wKtE/dLvpa+NOEAfn/HMO0qVdRjdVs9FaJWYbjRNeiZC3PIX8bLFwqgOLwe70HOi9V7vcrrUyhJTMfXz77Zm1bbCMtU2R7UJUnI0b2fQyKdIhYgZiKChmfHH395939x2yQd8ZFYPGbmB+Zq4mCivEZSSaNZ6h9r6YYdoFSmgLVM1upBvt3kEpOE91TWbtIS4nLBWvLIfZTW4MA77BltW7mtkO61ZepLqkdj0eFoXQIDAQAB";
    private static int ENCRYPTED_LOGGER_ENTRY_MAX_PAYLOAD = ((NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_RENEW_GEN_FAILURE + 1) * 2);
    private static int ENCRYPTED_LOGGER_LINE_MAX_PAYLOAD = NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_RENEW_GEN_FAILURE;
    private static final String KEY_POSTFIX = "⁝❜";
    private static final String KEY_PREFIX = "❛⁝";
    private static final String LOG_MIDFIX = "══";
    private static final String LOG_POSTFIX = "═╝";
    private static final String LOG_PREFIX = "╔═";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = EncryptedLogger.class.getSimpleName();
    private static final String PublicKeyId = "R001";
    private static EncryptedLogger singleInstance = null;
    private Cipher cipher = null;
    private byte[] iv = null;
    private SecretKey secretKey = null;
    /* access modifiers changed from: private */
    public SilentLogWatcher silentLogWatcher;

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
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public static synchronized com.sec.internal.log.EncryptedLogger getInstance() {
        /*
            java.lang.Class<com.sec.internal.log.EncryptedLogger> r0 = com.sec.internal.log.EncryptedLogger.class
            monitor-enter(r0)
            monitor-enter(r0)     // Catch:{ all -> 0x0017 }
            com.sec.internal.log.EncryptedLogger r1 = singleInstance     // Catch:{ all -> 0x0014 }
            if (r1 != 0) goto L_0x000f
            com.sec.internal.log.EncryptedLogger r1 = new com.sec.internal.log.EncryptedLogger     // Catch:{ all -> 0x0014 }
            r1.<init>()     // Catch:{ all -> 0x0014 }
            singleInstance = r1     // Catch:{ all -> 0x0014 }
        L_0x000f:
            monitor-exit(r0)     // Catch:{ all -> 0x0014 }
            com.sec.internal.log.EncryptedLogger r1 = singleInstance     // Catch:{ all -> 0x0017 }
            monitor-exit(r0)
            return r1
        L_0x0014:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0014 }
            throw r1     // Catch:{ all -> 0x0017 }
        L_0x0017:
            r1 = move-exception
            monitor-exit(r0)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.log.EncryptedLogger.getInstance():com.sec.internal.log.EncryptedLogger");
    }

    private EncryptedLogger() {
        initCipher();
    }

    private void initCipher() {
        try {
            Log.d(LOG_TAG, "initCipher");
            KeyGenerator generator = KeyGenerator.getInstance(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
            generator.init(256, SecureRandom.getInstance("SHA1PRNG"));
            this.secretKey = generator.generateKey();
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            this.cipher = instance;
            instance.init(1, this.secretKey);
            this.iv = this.cipher.getIV();
            writeSecretKeyToLogcat();
            writeSecretKeyToCriticalLog();
            startSilentLogWatcher();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public String getBase64EncodedSecretKey() {
        SecretKey secretKey2 = this.secretKey;
        if (secretKey2 == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(concatBytes(encryptRSA(concatBytes(secretKey2.getEncoded(), this.iv)), PublicKeyId.getBytes()));
    }

    public String getBase64EncodedSecretKeyWithDelimiter() {
        if (this.secretKey == null) {
            return null;
        }
        return KEY_PREFIX + getBase64EncodedSecretKey() + KEY_POSTFIX;
    }

    public String doLog(String tag, String msg, int logLevel) {
        long startNano = System.nanoTime();
        String base64EncryptedLog = Base64.getMimeEncoder(ENCRYPTED_LOGGER_LINE_MAX_PAYLOAD, "\n".getBytes()).encodeToString(encryptAES(msg));
        long durationNano = System.nanoTime() - startNano;
        int length = base64EncryptedLog.length();
        if (length > ENCRYPTED_LOGGER_ENTRY_MAX_PAYLOAD) {
            int from = 0;
            while (from < length) {
                int min = from + Math.min(ENCRYPTED_LOGGER_ENTRY_MAX_PAYLOAD, base64EncryptedLog.length() - from);
                int from2 = min;
                writeLog(tag, makeEncryptMessagePackage(base64EncryptedLog.substring(from, min), from2 < length, durationNano), logLevel);
                from = from2;
            }
            return makeEncryptMessagePackage(base64EncryptedLog, false, durationNano);
        }
        String returnMessage = makeEncryptMessagePackage(base64EncryptedLog, false, durationNano);
        writeLog(tag, returnMessage, logLevel);
        return returnMessage;
    }

    private void writeLog(String tag, String messagePackage, int logLevel) {
        if (logLevel == 2) {
            Log.v(tag, messagePackage);
        } else if (logLevel == 3) {
            Log.d(tag, messagePackage);
        } else if (logLevel == 4) {
            Log.i(tag, messagePackage);
        } else if (logLevel == 5) {
            Log.w(tag, messagePackage);
        } else if (logLevel == 6) {
            Log.e(tag, messagePackage);
        }
    }

    private String makeEncryptMessagePackage(String msg, boolean useMid, long encryptionNanoTime) {
        StringBuffer sb = new StringBuffer();
        sb.append("╔═ " + encryptionNanoTime);
        String str = "\n";
        sb.append(str);
        sb.append(msg);
        if (useMid) {
            str = "";
        }
        sb.append(str);
        sb.append(useMid ? LOG_MIDFIX : LOG_POSTFIX);
        return sb.toString();
    }

    private byte[] encryptAES(String msg) {
        try {
            return this.cipher.doFinal(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return new byte[]{0};
        }
    }

    private byte[] encryptRSA(byte[] source) {
        try {
            PublicKey publicKey = transformPublicKey(B64PublicKey);
            Cipher cipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher2.init(1, publicKey);
            return cipher2.doFinal(source);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return new byte[]{0};
        }
    }

    private PublicKey transformPublicKey(String stringPublicKey) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(stringPublicKey.getBytes())));
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return null;
        }
    }

    private byte[] concatBytes(byte[] a, byte[] b) {
        byte[] c = new byte[(a.length + b.length)];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /* access modifiers changed from: private */
    public void writeSecretKeyToLogcat() {
        Log.d(LOG_TAG, getBase64EncodedSecretKeyWithDelimiter());
    }

    private void writeSecretKeyToCriticalLog() {
        String secretKey2 = getBase64EncodedSecretKey();
        int length = secretKey2.length();
        for (int i = 0; i < length; i += 50) {
            IMSLog.c(LogClass.GEN_ENCRYPTED_LOGGER_KEY, secretKey2.substring(i, i + 50 < length ? i + 50 : length));
        }
    }

    public String _debug_GetSecretKeyInfo() {
        return "  " + Base64.getEncoder().encodeToString(this.secretKey.getEncoded()) + "\n  " + Base64.getEncoder().encodeToString(this.iv) + "\n";
    }

    public void startSilentLogWatcher() {
        new Thread() {
            public void run() {
                int countDown = 10;
                while (true) {
                    int countDown2 = countDown - 1;
                    if (countDown > 0) {
                        try {
                            if (Files.exists(Paths.get("/sdcard", new String[0]), new LinkOption[0])) {
                                SilentLogWatcher unused = EncryptedLogger.this.silentLogWatcher = new SilentLogWatcher(EncryptedLogger.this);
                                EncryptedLogger.this.silentLogWatcher.startWatch();
                                countDown = 0;
                            } else {
                                Log.d(EncryptedLogger.LOG_TAG, "/sdcard is not mounted yet");
                                Thread.sleep(3000);
                                countDown = countDown2;
                            }
                        } catch (Exception e) {
                            Log.e(EncryptedLogger.LOG_TAG, e.getMessage(), e);
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }.start();
    }

    private class SilentLogWatcher {
        int[] EVENT = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, KEYRecord.Flags.FLAG4, 4095};
        String[] NAME = {"ACCESS", "MODIFY", "ATTRIB", "CLOSE_WRITE", "CLOSE_NOWRITE", "OPEN", "MOVED_FROM", "MOVED_TO", "CREATE", HttpController.METHOD_DELETE, "DELETE_SELF", "MOVE_SELF", "ALL_EVENTS"};
        /* access modifiers changed from: private */
        public final Path SILENT_LOG_HOME;
        FileObserver[] fileObservers;
        final /* synthetic */ EncryptedLogger this$0;

        public SilentLogWatcher(EncryptedLogger encryptedLogger) {
            final EncryptedLogger encryptedLogger2 = encryptedLogger;
            this.this$0 = encryptedLogger2;
            Path path = Paths.get("/sdcard/log/ap_silentlog", new String[0]);
            this.SILENT_LOG_HOME = path;
            this.fileObservers = new FileObserver[path.getNameCount()];
            int i = 0;
            while (i < this.SILENT_LOG_HOME.getNameCount() - 1) {
                final int index = i;
                this.fileObservers[index] = new FileObserver(this.SILENT_LOG_HOME.getRoot().resolve(this.SILENT_LOG_HOME.subpath(0, index + 1)).toFile()) {
                    public void onEvent(int event, String eventPath) {
                        String child = SilentLogWatcher.this.SILENT_LOG_HOME.getName(index + 1).toString();
                        if (eventPath != null && child.equals(eventPath)) {
                            if ((event & 256) == 256) {
                                SilentLogWatcher.this.fileObservers[index + 1].startWatching();
                            } else if ((event & 512) == 512) {
                                SilentLogWatcher.this.fileObservers[index + 1].stopWatching();
                            }
                        }
                    }

                    public void startWatching() {
                        super.startWatching();
                        if (Files.exists(SilentLogWatcher.this.SILENT_LOG_HOME.getRoot().resolve(SilentLogWatcher.this.SILENT_LOG_HOME.subpath(0, index + 2)), new LinkOption[0])) {
                            SilentLogWatcher.this.fileObservers[index + 1].startWatching();
                        }
                    }

                    public void stopWatching() {
                        super.stopWatching();
                        int x = index;
                        while (true) {
                            x++;
                            if (x < SilentLogWatcher.this.fileObservers.length) {
                                SilentLogWatcher.this.fileObservers[x].stopWatching();
                            } else {
                                return;
                            }
                        }
                    }
                };
                i++;
            }
            this.fileObservers[i] = new SilentLogObserver(this.SILENT_LOG_HOME);
        }

        public void startWatch() {
            this.fileObservers[0].startWatching();
        }

        private String translateEvent(int event) {
            StringBuffer sb = new StringBuffer();
            int i = 0;
            while (true) {
                int[] iArr = this.EVENT;
                if (i >= iArr.length) {
                    return sb.toString();
                }
                if ((iArr[i] & event) == iArr[i]) {
                    sb.append(this.NAME[i] + ",");
                }
                i++;
            }
        }

        private class SilentLogObserver extends FileObserver {
            public static final String CHILD_PATH_REGEX = "20\\d{6}_\\d{6}";
            /* access modifiers changed from: private */
            public long lastWriteTime;
            /* access modifiers changed from: private */
            public Path mPath;
            /* access modifiers changed from: private */
            public Timer timer = new Timer();

            public SilentLogObserver(Path path) {
                super(path.toFile());
                this.mPath = path;
            }

            public void startWatching() {
                super.startWatching();
                String access$100 = EncryptedLogger.LOG_TAG;
                Log.d(access$100, "startWatching : " + SilentLogWatcher.this.SILENT_LOG_HOME.getRoot() + SilentLogWatcher.this.SILENT_LOG_HOME.subpath(0, SilentLogWatcher.this.SILENT_LOG_HOME.getNameCount()));
                try {
                    this.timer.schedule(new TimerTask() {
                        public void run() {
                            try {
                                Optional<Path> pathOption = Files.list(SilentLogObserver.this.mPath).reduce($$Lambda$EncryptedLogger$SilentLogWatcher$SilentLogObserver$1$vcm2YZzfx1Co4i0Ej92wiKdgjjg.INSTANCE);
                                if (pathOption.isPresent()) {
                                    Path lastModifiedPath = pathOption.get();
                                    if (Files.getLastModifiedTime(lastModifiedPath, new LinkOption[0]).toMillis() + 10000 >= System.currentTimeMillis() && lastModifiedPath.getFileName().toString().matches(SilentLogObserver.CHILD_PATH_REGEX)) {
                                        SilentLogObserver.this.timer.schedule(new KeyTimerTask(lastModifiedPath), 0);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(EncryptedLogger.LOG_TAG, e.getMessage(), e);
                            }
                        }

                        static /* synthetic */ Path lambda$run$0(Path p1, Path p2) {
                            try {
                                return Files.getLastModifiedTime(p1, new LinkOption[0]).compareTo(Files.getLastModifiedTime(p2, new LinkOption[0])) > 0 ? p1 : p2;
                            } catch (Exception e) {
                                throw new RuntimeException(e.getMessage());
                            }
                        }
                    }, 1000);
                } catch (Exception e) {
                    Log.e(EncryptedLogger.LOG_TAG, e.getMessage(), e);
                }
            }

            public void stopWatching() {
                super.stopWatching();
                String access$100 = EncryptedLogger.LOG_TAG;
                Log.d(access$100, "stopWatching : " + SilentLogWatcher.this.SILENT_LOG_HOME.getRoot() + SilentLogWatcher.this.SILENT_LOG_HOME.subpath(0, SilentLogWatcher.this.SILENT_LOG_HOME.getNameCount()));
            }

            public void onEvent(int event, String eventPath) {
                if ((event & 256) == 256 && eventPath != null) {
                    try {
                        if (eventPath.matches(CHILD_PATH_REGEX)) {
                            this.timer.schedule(new KeyTimerTask(this.mPath.resolve(eventPath)), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                        }
                    } catch (Exception e) {
                        Log.e(EncryptedLogger.LOG_TAG, e.getMessage());
                    }
                }
            }

            private class KeyTimerTask extends TimerTask {
                private Path mPath;

                public KeyTimerTask(Path path) {
                    this.mPath = path;
                }

                public void run() {
                    try {
                        if (Files.list(this.mPath).anyMatch($$Lambda$EncryptedLogger$SilentLogWatcher$SilentLogObserver$KeyTimerTask$gaQJIM5t1NJpsOLIWU7wD9LSf1I.INSTANCE) && SilentLogObserver.this.lastWriteTime + 10000 < System.currentTimeMillis()) {
                            long unused = SilentLogObserver.this.lastWriteTime = System.currentTimeMillis();
                            SilentLogWatcher.this.this$0.writeSecretKeyToLogcat();
                        }
                    } catch (Exception e) {
                        Log.e(EncryptedLogger.LOG_TAG, e.getMessage());
                    }
                }
            }
        }
    }
}
