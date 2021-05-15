package com.sec.internal.ims.diagnosis;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SemHqmManager;
import android.os.SemSystemProperties;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import org.json.JSONObject;

public class ImsLogAgent extends ContentProvider {
    public static final String AUTHORITY = "com.sec.imsservice.log";
    private static final Object DMUI_LOCK = new Object();
    private static final String DRCS_KEY_RCS_USER_SETTING = "RUSS";
    private static final Object DRCS_LOCK = new Object();
    private static final Object DRPT_LOCK = new Object();
    private static final String INTENT_ACTION_BIG_DATA_INFO = "com.samsung.intent.action.BIG_DATA_INFO";
    private static final String INTENT_ACTION_DAILY_REPORT_EXPIRED = "com.sec.imsservice.ACTION_DAILY_REPORT_EXPIRED";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImsLogAgent.class.getSimpleName();
    private static final int PERIOD_OF_DAILY_REPORT = 86400000;
    private static final Object PSCI_LOCK = new Object();
    private static final Object REGI_LOCK = new Object();
    private static final Object SIMI_LOCK = new Object();
    private static final Object UNKNOWN_LOCK = new Object();
    private static final int URI_TYPE_SEND_LOG = 1;
    private static final UriMatcher sUriMatcher;
    private Context mContext = null;
    private PendingIntent mDailyReportExpiry;
    private SimpleEventLog mEventLog = null;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "send/*", 1);
    }

    public boolean onCreate() {
        this.mContext = getContext();
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 200);
        scheduleDailyReport();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_BIG_DATA_INFO);
        filter.addAction(INTENT_ACTION_DAILY_REPORT_EXPIRED);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* JADX WARNING: Removed duplicated region for block: B:12:0x0049  */
            /* JADX WARNING: Removed duplicated region for block: B:14:0x0052  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(android.content.Context r6, android.content.Intent r7) {
                /*
                    r5 = this;
                    java.lang.String r0 = com.sec.internal.ims.diagnosis.ImsLogAgent.LOG_TAG
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "onReceive: "
                    r1.append(r2)
                    java.lang.String r2 = r7.getAction()
                    r1.append(r2)
                    java.lang.String r1 = r1.toString()
                    android.util.Log.d(r0, r1)
                    java.lang.String r0 = r7.getAction()
                    int r1 = r0.hashCode()
                    r2 = 1086667083(0x40c5394b, float:6.163244)
                    r3 = 1
                    r4 = 0
                    if (r1 == r2) goto L_0x003c
                    r2 = 1869298685(0x6f6b3bfd, float:7.28015E28)
                    if (r1 == r2) goto L_0x0032
                L_0x0031:
                    goto L_0x0046
                L_0x0032:
                    java.lang.String r1 = "com.samsung.intent.action.BIG_DATA_INFO"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0031
                    r0 = r4
                    goto L_0x0047
                L_0x003c:
                    java.lang.String r1 = "com.sec.imsservice.ACTION_DAILY_REPORT_EXPIRED"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0031
                    r0 = r3
                    goto L_0x0047
                L_0x0046:
                    r0 = -1
                L_0x0047:
                    if (r0 == 0) goto L_0x0052
                    if (r0 == r3) goto L_0x004c
                    goto L_0x006b
                L_0x004c:
                    com.sec.internal.ims.diagnosis.ImsLogAgent r0 = com.sec.internal.ims.diagnosis.ImsLogAgent.this
                    r0.onDailyReport()
                    goto L_0x006b
                L_0x0052:
                    java.lang.String r0 = "simslot"
                    int r0 = r7.getIntExtra(r0, r4)
                    java.lang.String r1 = "feature"
                    int r1 = r7.getIntExtra(r1, r4)
                    java.lang.String r2 = "bigdata_info"
                    java.lang.String r2 = r7.getStringExtra(r2)
                    com.sec.internal.ims.diagnosis.ImsLogAgent r3 = com.sec.internal.ims.diagnosis.ImsLogAgent.this
                    r3.onCsCallInfoReceived(r0, r1, r2)
                L_0x006b:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.diagnosis.ImsLogAgent.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
            }
        }, filter);
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selecArgs, String sortOrder) {
        return null;
    }

    public int update(Uri uri, ContentValues cv, String selection, String[] selecArgs) {
        if (sUriMatcher.match(uri) != 1) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("update: Invalid uri [" + uri + "]");
            return 0;
        }
        String feature = uri.getLastPathSegment();
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        if (!"DRPT".equalsIgnoreCase(feature) && !DiagnosisConstants.FEATURE_DRCS.equalsIgnoreCase(feature)) {
            return sendStoredLog(phoneId, feature);
        }
        onDailyReport();
        return 1;
    }

    public Uri insert(Uri uri, ContentValues cv) {
        if (uri == null || cv == null) {
            Log.e(LOG_TAG, "insert: parameter Uri or ContentValues has unexpected null value");
            return null;
        }
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        String feature = cv.getAsString(DiagnosisConstants.KEY_FEATURE);
        Integer sendMode = cv.getAsInteger(DiagnosisConstants.KEY_SEND_MODE);
        Integer overWriteMode = cv.getAsInteger(DiagnosisConstants.KEY_OVERWRITE_MODE);
        if (overWriteMode == null) {
            overWriteMode = 0;
        }
        cv.remove(DiagnosisConstants.KEY_FEATURE);
        cv.remove(DiagnosisConstants.KEY_SEND_MODE);
        cv.remove(DiagnosisConstants.KEY_OVERWRITE_MODE);
        if (sendMode == null || sendMode.intValue() == 0) {
            sendLogs(phoneId, feature, cv);
        } else if (sendMode.intValue() == 1) {
            storeLogs(phoneId, feature, cv, overWriteMode.intValue());
        }
        return uri;
    }

    public int delete(Uri uri, String selection, String[] selecArgs) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (!DiagnosisConstants.CALL_METHOD_LOGANDADD.equals(method) || TextUtils.isEmpty(arg)) {
            return null;
        }
        this.mEventLog.logAndAdd(arg);
        return null;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mEventLog.dump(new IndentingPrintWriter(writer, "  "));
    }

    /* access modifiers changed from: private */
    public void onDailyReport() {
        Log.d(LOG_TAG, "onDailyReport");
        PendingIntent pendingIntent = this.mDailyReportExpiry;
        if (pendingIntent != null) {
            AlarmTimer.stop(this.mContext, pendingIntent);
            this.mDailyReportExpiry = null;
        }
        ImsSharedPrefHelper.remove(0, this.mContext, "DRPT", DiagnosisConstants.KEY_NEXT_DRPT_SCHEDULE);
        try {
            sendStoredLog(0, "DRPT");
            sendStoredLog(0, DiagnosisConstants.FEATURE_DRCS);
            if (ImsLogAgentUtil.getCommonHeader(this.mContext, 1).size() > 0) {
                sendStoredLog(1, "DRPT");
                sendStoredLog(1, DiagnosisConstants.FEATURE_DRCS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("sendLogToAgent: Exception - " + e.getMessage());
        } catch (Throwable th) {
            scheduleDailyReport();
            throw th;
        }
        scheduleDailyReport();
    }

    private int getPeriodForDailyReport() {
        int period = SemSystemProperties.getInt("persist.ims.debug.period.dr", PERIOD_OF_DAILY_REPORT);
        if (period <= 0) {
            return PERIOD_OF_DAILY_REPORT;
        }
        return period;
    }

    private synchronized String getFeatureName(String feature) {
        if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_REGI)) {
            return DiagnosisConstants.FEATURE_REGI;
        }
        if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_PSCI)) {
            return DiagnosisConstants.FEATURE_PSCI;
        }
        if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_SIMI)) {
            return DiagnosisConstants.FEATURE_SIMI;
        }
        if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_DMUI)) {
            return DiagnosisConstants.FEATURE_DMUI;
        }
        if (feature.equalsIgnoreCase("DRPT")) {
            return "DRPT";
        }
        if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_DRCS)) {
            return DiagnosisConstants.FEATURE_DRCS;
        }
        return "UNKNOWN";
    }

    private synchronized Object getFeatureLock(String feature) {
        if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_REGI)) {
            return REGI_LOCK;
        } else if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_PSCI)) {
            return PSCI_LOCK;
        } else if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_SIMI)) {
            return SIMI_LOCK;
        } else if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_DMUI)) {
            return DMUI_LOCK;
        } else if (feature.equalsIgnoreCase("DRPT")) {
            return DRPT_LOCK;
        } else if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_DRCS)) {
            return DRCS_LOCK;
        } else {
            return UNKNOWN_LOCK;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x010d, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean sendLogs(int r10, java.lang.String r11, android.content.ContentValues r12) {
        /*
            r9 = this;
            java.lang.Object r0 = r9.getFeatureLock(r11)
            monitor-enter(r0)
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x010f }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x010f }
            r2.<init>()     // Catch:{ all -> 0x010f }
            java.lang.String r3 = "sendLogs: feature ["
            r2.append(r3)     // Catch:{ all -> 0x010f }
            r2.append(r11)     // Catch:{ all -> 0x010f }
            java.lang.String r3 = "]"
            r2.append(r3)     // Catch:{ all -> 0x010f }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x010f }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x010f }
            boolean r1 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((android.content.ContentValues) r12)     // Catch:{ all -> 0x010f }
            if (r1 == 0) goto L_0x0046
            com.sec.internal.helper.SimpleEventLog r1 = r9.mEventLog     // Catch:{ all -> 0x010f }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x010f }
            r2.<init>()     // Catch:{ all -> 0x010f }
            java.lang.String r3 = "sendLogs: ["
            r2.append(r3)     // Catch:{ all -> 0x010f }
            r2.append(r11)     // Catch:{ all -> 0x010f }
            java.lang.String r3 = "] is null or empty!"
            r2.append(r3)     // Catch:{ all -> 0x010f }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x010f }
            r1.logAndAdd(r2)     // Catch:{ all -> 0x010f }
            r1 = 0
            monitor-exit(r0)     // Catch:{ all -> 0x010f }
            return r1
        L_0x0046:
            r1 = 0
            org.json.JSONObject r2 = new org.json.JSONObject     // Catch:{ JSONException -> 0x00b5 }
            r2.<init>()     // Catch:{ JSONException -> 0x00b5 }
            r1 = r2
            android.content.Context r2 = r9.mContext     // Catch:{ JSONException -> 0x00b5 }
            android.content.ContentValues r2 = com.sec.internal.ims.diagnosis.ImsLogAgentUtil.getCommonHeader(r2, r10)     // Catch:{ JSONException -> 0x00b5 }
            java.util.Set r3 = r2.keySet()     // Catch:{ JSONException -> 0x00b5 }
            java.util.Iterator r3 = r3.iterator()     // Catch:{ JSONException -> 0x00b5 }
        L_0x005b:
            boolean r4 = r3.hasNext()     // Catch:{ JSONException -> 0x00b5 }
            if (r4 == 0) goto L_0x0074
            java.lang.Object r4 = r3.next()     // Catch:{ JSONException -> 0x00b5 }
            java.lang.String r4 = (java.lang.String) r4     // Catch:{ JSONException -> 0x00b5 }
            java.lang.Object r5 = r2.get(r4)     // Catch:{ JSONException -> 0x00b5 }
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ JSONException -> 0x00b5 }
            r1.put(r4, r5)     // Catch:{ JSONException -> 0x00b5 }
            goto L_0x005b
        L_0x0074:
            java.util.Set r3 = r12.keySet()     // Catch:{ JSONException -> 0x00b5 }
            java.util.Iterator r3 = r3.iterator()     // Catch:{ JSONException -> 0x00b5 }
        L_0x007c:
            boolean r4 = r3.hasNext()     // Catch:{ JSONException -> 0x00b5 }
            if (r4 == 0) goto L_0x00b4
            java.lang.Object r4 = r3.next()     // Catch:{ JSONException -> 0x00b5 }
            java.lang.String r4 = (java.lang.String) r4     // Catch:{ JSONException -> 0x00b5 }
            java.lang.Object r5 = r12.get(r4)     // Catch:{ JSONException -> 0x00b5 }
            if (r5 != 0) goto L_0x00ab
            com.sec.internal.helper.SimpleEventLog r6 = r9.mEventLog     // Catch:{ JSONException -> 0x00b5 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ JSONException -> 0x00b5 }
            r7.<init>()     // Catch:{ JSONException -> 0x00b5 }
            java.lang.String r8 = "sendLogs: ["
            r7.append(r8)     // Catch:{ JSONException -> 0x00b5 }
            r7.append(r4)     // Catch:{ JSONException -> 0x00b5 }
            java.lang.String r8 = "] is null!"
            r7.append(r8)     // Catch:{ JSONException -> 0x00b5 }
            java.lang.String r7 = r7.toString()     // Catch:{ JSONException -> 0x00b5 }
            r6.logAndAdd(r7)     // Catch:{ JSONException -> 0x00b5 }
            goto L_0x007c
        L_0x00ab:
            java.lang.String r6 = java.lang.String.valueOf(r5)     // Catch:{ JSONException -> 0x00b5 }
            r1.put(r4, r6)     // Catch:{ JSONException -> 0x00b5 }
            goto L_0x007c
        L_0x00b4:
            goto L_0x00d1
        L_0x00b5:
            r2 = move-exception
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x010f }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x010f }
            r4.<init>()     // Catch:{ all -> 0x010f }
            java.lang.String r5 = "sendLogs: JSONException! "
            r4.append(r5)     // Catch:{ all -> 0x010f }
            java.lang.String r5 = r2.getMessage()     // Catch:{ all -> 0x010f }
            r4.append(r5)     // Catch:{ all -> 0x010f }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x010f }
            android.util.Log.e(r3, r4)     // Catch:{ all -> 0x010f }
        L_0x00d1:
            java.lang.String r2 = r9.normalizeLog(r1)     // Catch:{ all -> 0x010f }
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x010f }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x010f }
            r4.<init>()     // Catch:{ all -> 0x010f }
            java.lang.String r5 = "sendLogs: send ["
            r4.append(r5)     // Catch:{ all -> 0x010f }
            r4.append(r2)     // Catch:{ all -> 0x010f }
            java.lang.String r5 = "]"
            r4.append(r5)     // Catch:{ all -> 0x010f }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x010f }
            com.sec.internal.log.IMSLog.s(r3, r4)     // Catch:{ all -> 0x010f }
            r9.sendLogToHqmManager(r11, r2)     // Catch:{ all -> 0x010f }
            java.lang.String r3 = "DRPT"
            boolean r3 = r11.equalsIgnoreCase(r3)     // Catch:{ all -> 0x010f }
            if (r3 == 0) goto L_0x010c
            android.content.Context r3 = r9.mContext     // Catch:{ all -> 0x010f }
            android.content.ContentResolver r3 = r3.getContentResolver()     // Catch:{ all -> 0x010f }
            java.lang.String r4 = "content://com.sec.imsservice.log/log/drpt"
            android.net.Uri r4 = android.net.Uri.parse(r4)     // Catch:{ all -> 0x010f }
            r5 = 0
            r3.notifyChange(r4, r5)     // Catch:{ all -> 0x010f }
        L_0x010c:
            monitor-exit(r0)     // Catch:{ all -> 0x010f }
            r0 = 1
            return r0
        L_0x010f:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x010f }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.diagnosis.ImsLogAgent.sendLogs(int, java.lang.String, android.content.ContentValues):boolean");
    }

    /* access modifiers changed from: protected */
    public boolean sendLogToHqmManager(String feature, String custom_dataset) {
        SemHqmManager hqm = (SemHqmManager) this.mContext.getSystemService("HqmManagerService");
        if (hqm == null) {
            return false;
        }
        return hqm.sendHWParamToHQM(0, DiagnosisConstants.COMPONENT_ID, feature, "sm", "0.0", ImsConstants.RCS_AS.SEC, "", custom_dataset, "");
    }

    private int sendStoredLog(int phoneId, String uriLastSeg) {
        String feature = getFeatureName(uriLastSeg);
        int i = 0;
        if (feature.equals("UNKNOWN")) {
            this.mEventLog.logAndAdd("sendStoredLog: Invalid feature [" + feature + "]");
            return 0;
        }
        IMSLog.d(LOG_TAG, phoneId, "sendStoredLog: feature [" + feature + "]");
        synchronized (getFeatureLock(feature)) {
            SharedPreferences logStorage = ImsSharedPrefHelper.getSharedPref(phoneId, this.mContext, feature, 0, false);
            ContentValues cv = new ContentValues();
            for (Map.Entry<String, ?> entry : logStorage.getAll().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Integer) {
                    cv.put(key, Integer.valueOf(((Integer) value).intValue()));
                } else if (value instanceof String) {
                    cv.put(key, (String) value);
                } else if (value instanceof Long) {
                    cv.put(key, Long.valueOf(((Long) value).longValue()));
                } else {
                    this.mEventLog.logAndAdd(phoneId, "sendStoredLog: [" + key + "] has wrong data type!");
                }
            }
            if (CollectionUtils.isNullOrEmpty(cv)) {
                this.mEventLog.logAndAdd(phoneId, "sendStoredLog: [" + feature + "] is null or empty");
                return 0;
            }
            if (feature.equalsIgnoreCase("DRPT")) {
                cv.put(DiagnosisConstants.COMMON_KEY_VOLTE_SETTINGS, Integer.valueOf(ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, phoneId) == 0 ? 1 : 0));
                cv.put(DiagnosisConstants.COMMON_KEY_VIDEO_SETTINGS, Integer.valueOf(ImsConstants.SystemSettings.getVideoCallType(this.mContext, -1, phoneId) == 0 ? 1 : 0));
                if (VowifiConfig.isEnabled(this.mContext, phoneId)) {
                    i = 1;
                }
                cv.put(DiagnosisConstants.DRPT_KEY_VOWIFI_ENABLE_SETTINGS, Integer.valueOf(i));
                cv.put(DiagnosisConstants.DRPT_KEY_VOWIFI_PREF_SETTINGS, Integer.valueOf(VowifiConfig.getPrefMode(this.mContext, 1, phoneId)));
                cv.remove(DiagnosisConstants.KEY_NEXT_DRPT_SCHEDULE);
            } else if (feature.equalsIgnoreCase(DiagnosisConstants.FEATURE_DRCS)) {
                boolean isSecMessageInUse = isSmsAppDefault();
                if (!isSecMessageInUse) {
                    i = 1;
                }
                cv.put("CMAS", Integer.valueOf(i));
                if (!isSecMessageInUse) {
                    String smsPkg = Telephony.Sms.getDefaultSmsPackage(this.mContext);
                    if (!TextUtils.isEmpty(smsPkg)) {
                        cv.put("CMDA", smsPkg);
                    }
                }
                cv.put(DRCS_KEY_RCS_USER_SETTING, Integer.valueOf(ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, phoneId)));
            }
            IMSLog.d(LOG_TAG, phoneId, "sendStoredLog: send logs of [" + feature + "]");
            sendLogs(phoneId, feature, cv);
            logStorage.edit().clear().apply();
            return 1;
        }
    }

    private boolean storeLogs(int phoneId, String feature, ContentValues cv, int overWrite) {
        long numToSet;
        String str = feature;
        int i = overWrite;
        synchronized (getFeatureLock(str)) {
            try {
                int i2 = 0;
                if (CollectionUtils.isNullOrEmpty(cv)) {
                    SimpleEventLog simpleEventLog = this.mEventLog;
                    simpleEventLog.logAndAdd("storeLogs: [" + str + "] is null or empty");
                    return false;
                }
                SharedPreferences logStorage = ImsSharedPrefHelper.getSharedPref(phoneId, this.mContext, str, 0, false);
                SharedPreferences.Editor editor = logStorage.edit();
                for (String key : cv.keySet()) {
                    try {
                        Object obj = cv.get(key);
                        if (obj == null) {
                            SimpleEventLog simpleEventLog2 = this.mEventLog;
                            simpleEventLog2.logAndAdd("storeLogs: [" + key + "] is null!");
                        } else {
                            if (obj instanceof Integer) {
                                int numToSet2 = ((Integer) obj).intValue();
                                int storedVal = logStorage.getInt(key, i2);
                                if (i == 1) {
                                    numToSet2 += storedVal;
                                } else if (i == 2 && numToSet2 <= storedVal) {
                                    numToSet2 = storedVal;
                                }
                                editor.putInt(key, numToSet2);
                            } else if (obj instanceof Long) {
                                long numToSet3 = ((Long) obj).longValue();
                                long storedVal2 = logStorage.getLong(key, 0);
                                if (i == 1) {
                                    numToSet = numToSet3 + storedVal2;
                                } else if (i != 2 || numToSet3 > storedVal2) {
                                    numToSet = numToSet3;
                                } else {
                                    numToSet = storedVal2;
                                }
                                editor.putLong(key, numToSet);
                            } else if (obj instanceof String) {
                                editor.putString(key, (String) obj);
                            } else {
                                SimpleEventLog simpleEventLog3 = this.mEventLog;
                                simpleEventLog3.logAndAdd("storeLogs: [" + key + "] has wrong data type!");
                            }
                            int i3 = phoneId;
                            i2 = 0;
                        }
                    } catch (ClassCastException e) {
                        SimpleEventLog simpleEventLog4 = this.mEventLog;
                        simpleEventLog4.logAndAdd("storeLogs: ClassCastException! key: [" + key + "]!");
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
                ContentValues contentValues = cv;
                String str2 = LOG_TAG;
                Log.d(str2, "storeLogs: feature [" + str + "]");
                editor.apply();
                return true;
            } catch (Throwable th2) {
                th = th2;
                ContentValues contentValues2 = cv;
                throw th;
            }
        }
    }

    private void scheduleDailyReport() {
        if (this.mDailyReportExpiry == null) {
            SharedPreferences spDrpt = ImsSharedPrefHelper.getSharedPref(0, this.mContext, "DRPT", 0, false);
            long delay = 0;
            long scheduledTime = spDrpt.getLong(DiagnosisConstants.KEY_NEXT_DRPT_SCHEDULE, 0);
            long curTime = System.currentTimeMillis();
            if (scheduledTime <= 0 || scheduledTime > curTime) {
                if (scheduledTime == 0) {
                    scheduledTime = curTime + ((long) getPeriodForDailyReport());
                }
                delay = scheduledTime - curTime;
                spDrpt.edit().putLong(DiagnosisConstants.KEY_NEXT_DRPT_SCHEDULE, scheduledTime).apply();
            } else {
                Log.d(LOG_TAG, "scheduleDailyReport: DRPT timer is expired. Sending it now.");
            }
            String str = LOG_TAG;
            Log.d(str, "scheduleDailyReport: delay [" + delay + "] scheduled time [" + scheduledTime + "]");
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_ACTION_DAILY_REPORT_EXPIRED), 134217728);
            this.mDailyReportExpiry = broadcast;
            AlarmTimer.start(this.mContext, broadcast, delay);
        }
    }

    private String normalizeLog(JSONObject log) {
        return log.toString().replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\s+", "^");
    }

    /* access modifiers changed from: private */
    public void onCsCallInfoReceived(int phoneId, int feature, String data) {
        int i = phoneId;
        int i2 = feature;
        if (i2 != 0 && i2 != 1) {
            String str = LOG_TAG;
            Log.d(str, "onCsCallInfoReceived : ignore except CEND/DROP! received: " + i2);
        } else if (!TextUtils.isEmpty(data)) {
            String bigDataInfo = data.replace("\"", "");
            String str2 = LOG_TAG;
            Log.d(str2, "onCsCallInfoReceived: remove quotes [" + bigDataInfo + "]");
            String[] tparse = bigDataInfo.split(",");
            int callType = -1;
            int callState = -1;
            int callDropEvent = -1;
            if (tparse.length < 1) {
                Log.d(LOG_TAG, "onCsCallInfoReceived: No data");
                return;
            }
            int i3 = 0;
            while (i3 < tparse.length) {
                try {
                    if (tparse[i3].contains("Ctyp")) {
                        callType = Integer.parseInt(tparse[i3].split(":")[1].trim());
                    } else if (tparse[i3].contains("Csta")) {
                        callState = Integer.parseInt(tparse[i3].split(":")[1].trim());
                    } else if (tparse[i3].contains("Etyp")) {
                        callDropEvent = Integer.parseInt(tparse[i3].split(":")[1].trim());
                    }
                    i3++;
                } catch (NumberFormatException e) {
                    String str3 = LOG_TAG;
                    Log.e(str3, "onCsCallInfoReceived: NumberFormatException! " + e.getMessage());
                    return;
                }
            }
            if (callType >= 1 && callType <= 3) {
                ContentValues csDRTP = new ContentValues();
                ContentValues csPSCI = new ContentValues();
                if (i2 == 1) {
                    csPSCI.put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, 0);
                    if (callType == 3) {
                        csPSCI.put("TYPE", 7);
                    } else {
                        csPSCI.put("TYPE", Integer.valueOf(callType));
                    }
                    if (callState == 1) {
                        csPSCI.put(DiagnosisConstants.PSCI_KEY_CALL_STATE, 3);
                        csDRTP.put(DiagnosisConstants.DRPT_KEY_CSCALL_OUTGOING_FAIL, 1);
                    } else if (callState == 2) {
                        csPSCI.put(DiagnosisConstants.PSCI_KEY_CALL_STATE, 2);
                        csDRTP.put(DiagnosisConstants.DRPT_KEY_CSCALL_INCOMING_FAIL, 1);
                    } else {
                        csPSCI.put(DiagnosisConstants.PSCI_KEY_CALL_STATE, 5);
                    }
                    csPSCI.put(DiagnosisConstants.PSCI_KEY_FAIL_CODE, Integer.valueOf(callDropEvent));
                    storeLogs(i, DiagnosisConstants.FEATURE_PSCI, csPSCI, 0);
                    ImsLogAgentUtil.requestToSendStoredLog(i, this.mContext, DiagnosisConstants.FEATURE_PSCI);
                    String str4 = LOG_TAG;
                    Log.d(str4, "onCsCallInfoReceived: send PSCI: " + csPSCI);
                    csDRTP.put(DiagnosisConstants.DRPT_KEY_CSCALL_END_FAIL_COUNT, 1);
                }
                csDRTP.put(DiagnosisConstants.DRPT_KEY_CSCALL_END_TOTAL_COUNT, 1);
                String str5 = LOG_TAG;
                Log.d(str5, "onCsCallInfoReceived: storeLogs: " + csDRTP);
                storeLogs(i, "DRPT", csDRTP, 1);
            }
        }
    }

    private boolean isSmsAppDefault() {
        Log.d(LOG_TAG, "get default sms app.");
        String defaultSmsApp = null;
        try {
            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception e) {
            String str = LOG_TAG;
            Log.e(str, "Failed to getDefaultSmsPackage: " + e);
        }
        if (defaultSmsApp == null) {
            Log.e(LOG_TAG, "default sms app is null");
            return false;
        }
        String samsungPackage = PackageUtils.getMsgAppPkgName(this.mContext);
        boolean result = TextUtils.equals(defaultSmsApp, samsungPackage);
        String str2 = LOG_TAG;
        Log.d(str2, "default sms app:" + defaultSmsApp + " samsungPackage:" + samsungPackage);
        String str3 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("isDefaultMessageAppInUse : ");
        sb.append(result);
        Log.d(str3, sb.toString());
        return result;
    }
}
