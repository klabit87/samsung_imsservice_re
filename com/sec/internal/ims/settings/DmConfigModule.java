package com.sec.internal.ims.settings;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.configuration.DATA;
import com.sec.ims.settings.NvConfiguration;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DmConfigModule extends Handler implements ISequentialInitializable {
    public static final String CONFIG_DM_PROVIDER = "content://com.samsung.rcs.dmconfigurationprovider/omadm/";
    public static final String DM_PATH = "omadm/";
    private static final int EVT_FINISH_DM_CONFIG = 1;
    private static final int EVT_FINISH_OMADM_PROV_UPDATE = 2;
    private static final String INTENT_ACTION_DM_VALUE_UPDATE = "com.samsung.ims.action.DM_UPDATE";
    public static final String INTERNAL_KEY_PROCESS_NAME = "INTERNAL_KEY_PROCESS_NAME";
    private static final String KOR_DM_PACKAGE_NAME = "com.ims.dm";
    private static final String LOG_TAG = "DmConfigModule";
    private static final int VZW_OMADM_PENDING_DELAY = 5000;
    /* access modifiers changed from: private */
    public Context mContext;
    private DmContentValues mDmContentValues;
    private SimpleEventLog mEventLog;
    protected IImsFramework mImsFramework = null;
    ContentObserver mMnoUpdateObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange) {
            Cursor cursor = DmConfigModule.this.mContext.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/nvlist"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        String nvList = cursor.getString(0);
                        if (nvList != null) {
                            String nvList2 = nvList.replace("[", "").replace("]", "").replace(" ", "");
                            DmConfigModule.this.mNvList.clear();
                            DmConfigModule.this.mNvList.addAll(Arrays.asList(nvList2.split(",")));
                        } else {
                            Log.e(DmConfigModule.LOG_TAG, "nvList is null");
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            Log.d(DmConfigModule.LOG_TAG, "nv list reloaded:" + DmConfigModule.this.mNvList);
            if (cursor != null) {
                cursor.close();
                return;
            }
            return;
            throw th;
        }
    };
    /* access modifiers changed from: private */
    public ArrayList<String> mNvList = new ArrayList<>();
    private int mOmadmProvisioningTransactionId = -1;
    protected IRegistrationManager mRegMgr;
    private BroadcastReceiver mVzwTestModeReceiver = null;

    public DmConfigModule(Context context, Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mContext = context;
        this.mDmContentValues = new DmContentValues();
        this.mImsFramework = imsFramework;
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 200);
    }

    public void initSequentially() {
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.sec.ims.settings/mno"), true, this.mMnoUpdateObserver);
        if (IMSLog.isEngMode()) {
            registerVzwTestReceiver();
        }
    }

    public void setRegistrationManager(IRegistrationManager regMgr) {
        this.mRegMgr = regMgr;
    }

    private void registerVzwTestReceiver() {
        Log.d(LOG_TAG, "registerVzwTestReceiver");
        IntentFilter vzwTestReceiver = new IntentFilter();
        vzwTestReceiver.addAction(INTENT_ACTION_DM_VALUE_UPDATE);
        AnonymousClass1 r1 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String dmItem = intent.getStringExtra("ITEM");
                int dmValue = intent.getIntExtra("VALUE", -1);
                int phoneId = intent.getIntExtra(ImsConstants.Intents.EXTRA_REGI_PHONE_ID, 0);
                if (TextUtils.equals(dmItem, "157") || TextUtils.equals(dmItem, "106")) {
                    Log.d(DmConfigModule.LOG_TAG, "dmItem : " + dmItem + ", value : " + dmValue);
                    ContentValues cv = new ContentValues();
                    cv.put(dmItem, String.valueOf(dmValue));
                    DmConfigModule.this.updateConfigValues(cv, -1, phoneId);
                    return;
                }
                Log.d(DmConfigModule.LOG_TAG, "This item is not allowed : " + dmItem);
            }
        };
        this.mVzwTestModeReceiver = r1;
        this.mContext.registerReceiver(r1, vzwTestReceiver);
    }

    public int startDmConfig() {
        if (this.mDmContentValues == null) {
            this.mDmContentValues = new DmContentValues();
        }
        int newId = this.mDmContentValues.getNewTransactionId();
        Log.d(LOG_TAG, "Start getting ims-config by OTA-DM with TransactionId " + newId);
        return newId;
    }

    public void finishDmConfig(int transactionId) {
        Log.d(LOG_TAG, "finish getting ims-config by OTA-DM with transactionId " + transactionId);
        sendMessage(obtainMessage(1, transactionId, 0, (Object) null));
    }

    public ContentValues getConfigValues(String[] fields, int phoneId) {
        int i;
        int type;
        int fieldIndex;
        String[] strArr = fields;
        int i2 = phoneId;
        ContentValues result = new ContentValues();
        if (strArr == null || strArr.length <= 0) {
            Log.e(LOG_TAG, "Error on fields");
            return result;
        }
        String caller = PackageUtils.getProcessNameById(this.mContext, Binder.getCallingPid());
        Map<String, String> dmDatas = DmConfigHelper.read(this.mContext, "omadm/*", i2);
        Set<String> keySet = dmDatas.keySet();
        int length = strArr.length;
        int i3 = 0;
        while (i3 < length) {
            String field = strArr[i3];
            String name = "";
            String pathName = "";
            String value = "";
            try {
                fieldIndex = Integer.parseInt(field);
                if (fieldIndex < 0) {
                    i = length;
                    i3++;
                    strArr = fields;
                    length = i;
                } else {
                    if (fieldIndex >= 900) {
                        type = 3;
                    } else {
                        type = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(fieldIndex)).getType();
                        name = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(fieldIndex)).getName();
                        pathName = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(fieldIndex)).getPathName();
                    }
                    String str = "";
                    if (type == 0) {
                        i = length;
                        String value2 = value;
                        int i4 = type;
                        if (!this.mNvList.contains(name)) {
                            Iterator<String> it = keySet.iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    value = value2;
                                    break;
                                }
                                String path = it.next();
                                if (pathName.equals(path)) {
                                    String value3 = dmDatas.get(path);
                                    if (!TextUtils.equals("VOICE_DOMAIN_PREF_EUTRAN", field) || !TextUtils.equals("com.ims.dm", caller)) {
                                        value = value3;
                                    } else {
                                        value = "-1";
                                    }
                                }
                            }
                        } else {
                            value = NvConfiguration.get(this.mContext, name, str, i2);
                        }
                    } else {
                        i = length;
                        if (type == 1) {
                            int i5 = type;
                        } else if (type == 3) {
                            String str2 = value;
                            if (Integer.parseInt("74") == fieldIndex) {
                                int i6 = type;
                                value = this.mImsFramework.getString(i2, "dm_app_id", "ap2001");
                            } else {
                                if (Integer.parseInt("75") == fieldIndex) {
                                    value = this.mImsFramework.getString(i2, "dm_user_disp_name", "ap2001");
                                } else {
                                    throw new IllegalArgumentException("Unsupported Segment : Global Type " + fieldIndex);
                                }
                            }
                        } else if (type == 4) {
                            value = DmConfigHelper.getImsSwitchValue(this.mContext, name, i2) == 1 ? "1" : "0";
                            int i7 = type;
                        } else if (type != 5) {
                            int i8 = type;
                        } else {
                            value = RcsConfigurationHelper.readStringParamWithPath(this.mContext, name);
                            int i9 = type;
                        }
                        if (Integer.parseInt("91") == fieldIndex) {
                            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
                            if (sm != null) {
                                str = sm.getSimSerialNumber();
                            }
                            value = str;
                        }
                    }
                    result.put(field, value);
                    Log.d(LOG_TAG, "result (" + fieldIndex + ") " + name + " [ " + value + " ]");
                    i3++;
                    strArr = fields;
                    length = i;
                }
            } catch (NumberFormatException e) {
                Log.d(LOG_TAG, "get xNode " + field);
                type = 0;
                name = field;
                NumberFormatException numberFormatException = e;
                pathName = DM_PATH + name;
                fieldIndex = -1;
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x010a  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x011a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean updateConfigValues(android.content.ContentValues r28, int r29, int r30) {
        /*
            r27 = this;
            r1 = r27
            r2 = r28
            r3 = r29
            r4 = r30
            java.lang.String r5 = " "
            java.lang.String r6 = "./3GPP_IMS/"
            java.lang.String r7 = ""
            android.content.Context r0 = r1.mContext
            int r8 = android.os.Binder.getCallingPid()
            java.lang.String r8 = com.sec.internal.helper.os.PackageUtils.getProcessNameById(r0, r8)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r9 = "updateConfigValues: caller ["
            r0.append(r9)
            r0.append(r8)
            java.lang.String r9 = "] updateMap["
            r0.append(r9)
            r0.append(r2)
            java.lang.String r9 = "] transactionId ["
            r0.append(r9)
            r0.append(r3)
            java.lang.String r9 = "]"
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            java.lang.String r10 = "DmConfigModule"
            android.util.Log.d(r10, r0)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r11 = r0
            r0 = 0
            android.content.ContentValues r12 = new android.content.ContentValues
            r12.<init>()
            android.content.ContentValues r13 = new android.content.ContentValues
            r13.<init>()
            android.content.ContentValues r14 = new android.content.ContentValues
            r14.<init>()
            java.lang.String[] r15 = com.sec.ims.configuration.DATA.DM_FIELD_INDEX.values()
            android.content.ContentValues r15 = r1.getConfigValues(r15, r4)
            r16 = r0
            android.content.Context r0 = r1.mContext
            java.lang.String r3 = "omadm/*"
            java.util.Map r3 = com.sec.internal.helper.DmConfigHelper.read(r0, r3, r4)
            java.util.Set r0 = r28.keySet()
            java.util.Iterator r17 = r0.iterator()
        L_0x0074:
            boolean r0 = r17.hasNext()
            java.lang.String r18 = "31"
            java.lang.String r19 = "94"
            java.lang.String r20 = "93"
            r21 = r8
            if (r0 == 0) goto L_0x021c
            java.lang.Object r0 = r17.next()
            r8 = r0
            java.lang.String r8 = (java.lang.String) r8
            java.lang.Object r0 = r15.get(r8)
            r22 = r0
            java.lang.String r22 = (java.lang.String) r22
            java.lang.Object r0 = r2.get(r8)
            r2 = r0
            java.lang.String r2 = (java.lang.String) r2
            java.util.List r0 = com.sec.ims.configuration.DATA.DM_FIELD_LIST     // Catch:{ NumberFormatException -> 0x00af }
            r23 = r15
            int r15 = java.lang.Integer.parseInt(r8)     // Catch:{ NumberFormatException -> 0x00ad }
            java.lang.Object r0 = r0.get(r15)     // Catch:{ NumberFormatException -> 0x00ad }
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r0 = (com.sec.ims.configuration.DATA.DM_FIELD_INFO) r0     // Catch:{ NumberFormatException -> 0x00ad }
            r25 = r3
            r26 = r12
            r3 = r22
            goto L_0x010c
        L_0x00ad:
            r0 = move-exception
            goto L_0x00b2
        L_0x00af:
            r0 = move-exception
            r23 = r15
        L_0x00b2:
            java.lang.Object r15 = r3.get(r8)
            r22 = r15
            java.lang.String r22 = (java.lang.String) r22
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r24 = r0
            java.lang.String r0 = "update xNode "
            r15.append(r0)
            r15.append(r8)
            java.lang.String r0 = " ["
            r15.append(r0)
            r15.append(r2)
            r15.append(r9)
            java.lang.String r0 = r15.toString()
            android.util.Log.d(r10, r0)
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r0 = new com.sec.ims.configuration.DATA$DM_FIELD_INFO
            java.lang.String r15 = r8.replace(r6, r7)
            r25 = r3
            r26 = r12
            r3 = 0
            r12 = -1
            r0.<init>(r12, r3, r15)
            java.lang.String r3 = "LBO_P-CSCF_Address"
            boolean r3 = r8.contains(r3)
            if (r3 == 0) goto L_0x010a
            java.util.Optional r3 = java.util.Optional.ofNullable(r2)
            com.sec.internal.ims.settings.-$$Lambda$DmConfigModule$6G_PDl7NrM4OVB9xMsMT9OT4Mxs r12 = com.sec.internal.ims.settings.$$Lambda$DmConfigModule$6G_PDl7NrM4OVB9xMsMT9OT4Mxs.INSTANCE
            r3.ifPresent(r12)
            if (r2 == 0) goto L_0x0105
            java.lang.String r2 = r2.replace(r5, r7)
            r3 = r22
            goto L_0x010c
        L_0x0105:
            java.lang.String r2 = ""
            r3 = r22
            goto L_0x010c
        L_0x010a:
            r3 = r22
        L_0x010c:
            if (r0 != 0) goto L_0x011a
            r2 = r28
            r8 = r21
            r15 = r23
            r3 = r25
            r12 = r26
            goto L_0x0074
        L_0x011a:
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r15 = "Idx ["
            r12.append(r15)
            r12.append(r8)
            java.lang.String r15 = "], Type ["
            r12.append(r15)
            int r15 = r0.getType()
            r12.append(r15)
            java.lang.String r15 = "], Val ["
            r12.append(r15)
            r12.append(r3)
            java.lang.String r15 = "] => ["
            r12.append(r15)
            r12.append(r2)
            r12.append(r9)
            java.lang.String r12 = r12.toString()
            android.util.Log.d(r10, r12)
            java.lang.String r12 = "10"
            boolean r12 = android.text.TextUtils.equals(r8, r12)
            if (r12 != 0) goto L_0x0169
            java.lang.String r12 = "72"
            boolean r12 = android.text.TextUtils.equals(r8, r12)
            if (r12 != 0) goto L_0x0169
            java.lang.String r12 = "116"
            boolean r12 = android.text.TextUtils.equals(r8, r12)
            if (r12 == 0) goto L_0x0166
            goto L_0x0169
        L_0x0166:
            r22 = r5
            goto L_0x01a1
        L_0x0169:
            r11.append(r5)
            r11.append(r8)
            java.lang.String r12 = ":"
            r11.append(r12)
            r11.append(r3)
            java.lang.String r12 = ","
            r11.append(r12)
            r11.append(r2)
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r22 = r5
            java.lang.String r5 = "OMADM update : "
            r15.append(r5)
            r15.append(r8)
            r15.append(r12)
            r15.append(r3)
            r15.append(r12)
            r15.append(r2)
            java.lang.String r5 = r15.toString()
            android.util.Log.d(r10, r5)
        L_0x01a1:
            int r5 = r0.getType()
            if (r5 == 0) goto L_0x01cc
            r12 = 3
            if (r5 == r12) goto L_0x01c2
            r12 = 4
            if (r5 == r12) goto L_0x01b0
            r12 = r26
            goto L_0x0210
        L_0x01b0:
            java.lang.String r5 = "1"
            boolean r5 = r5.equals(r2)
            android.content.Context r12 = r1.mContext
            java.lang.String r15 = r0.getName()
            com.sec.internal.helper.DmConfigHelper.setImsSwitch(r12, r15, r5, r4)
            r12 = r26
            goto L_0x0210
        L_0x01c2:
            java.lang.String r5 = r0.getName()
            r14.put(r5, r2)
            r12 = r26
            goto L_0x0210
        L_0x01cc:
            java.util.ArrayList<java.lang.String> r5 = r1.mNvList
            java.lang.String r12 = r0.getName()
            java.lang.String r12 = r12.replace(r6, r7)
            boolean r5 = r5.contains(r12)
            if (r5 == 0) goto L_0x01fe
            java.lang.String r5 = r0.getName()
            r13.put(r5, r2)
            int r5 = java.lang.Integer.parseInt(r8)
            int r12 = java.lang.Integer.parseInt(r20)
            if (r12 == r5) goto L_0x01f9
            int r12 = java.lang.Integer.parseInt(r19)
            if (r12 == r5) goto L_0x01f9
            int r12 = java.lang.Integer.parseInt(r18)
            if (r12 != r5) goto L_0x01fb
        L_0x01f9:
            r16 = 1
        L_0x01fb:
            r12 = r26
            goto L_0x0210
        L_0x01fe:
            boolean r5 = android.text.TextUtils.equals(r3, r2)
            if (r5 != 0) goto L_0x020e
            java.lang.String r5 = r0.getName()
            r12 = r26
            r12.put(r5, r2)
            goto L_0x0210
        L_0x020e:
            r12 = r26
        L_0x0210:
            r2 = r28
            r8 = r21
            r5 = r22
            r15 = r23
            r3 = r25
            goto L_0x0074
        L_0x021c:
            r25 = r3
            r23 = r15
            com.sec.internal.helper.SimpleEventLog r0 = r1.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "OMADM update :"
            r2.append(r3)
            r2.append(r11)
            java.lang.String r2 = r2.toString()
            r0.logAndAdd(r2)
            int r0 = r12.size()
            java.lang.String r2 = "INTERNAL_KEY_PROCESS_NAME"
            if (r0 <= 0) goto L_0x0244
            r3 = r21
            r12.put(r2, r3)
            goto L_0x0246
        L_0x0244:
            r3 = r21
        L_0x0246:
            int r0 = r13.size()
            if (r0 <= 0) goto L_0x024f
            r13.put(r2, r3)
        L_0x024f:
            r0 = 1
            if (r16 == 0) goto L_0x0326
            java.util.Set r2 = r13.keySet()
            int r5 = r1.mOmadmProvisioningTransactionId
            r6 = 2
            r7 = -1
            if (r5 == r7) goto L_0x02f7
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r7 = r1.mDmContentValues
            r7.addConfigData(r5, r6, r13)
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r5 = r1.mDmContentValues
            int r7 = r1.mOmadmProvisioningTransactionId
            android.content.ContentValues r13 = r5.getConfigData(r7, r6)
            if (r13 != 0) goto L_0x026c
            return r0
        L_0x026c:
            java.util.Iterator r5 = r2.iterator()
        L_0x0270:
            boolean r7 = r5.hasNext()
            if (r7 == 0) goto L_0x02a2
            java.lang.Object r7 = r5.next()
            java.lang.String r7 = (java.lang.String) r7
            com.sec.internal.helper.SimpleEventLog r8 = r1.mEventLog
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r15 = "OMADM update "
            r10.append(r15)
            r10.append(r7)
            java.lang.String r15 = " = ["
            r10.append(r15)
            java.lang.Object r15 = r13.get(r7)
            r10.append(r15)
            r10.append(r9)
            java.lang.String r10 = r10.toString()
            r8.logAndAdd(r10)
            goto L_0x0270
        L_0x02a2:
            java.util.List r5 = com.sec.ims.configuration.DATA.DM_FIELD_LIST
            int r7 = java.lang.Integer.parseInt(r20)
            java.lang.Object r5 = r5.get(r7)
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r5 = (com.sec.ims.configuration.DATA.DM_FIELD_INFO) r5
            java.lang.String r5 = r5.getName()
            boolean r5 = r2.contains(r5)
            if (r5 == 0) goto L_0x0325
            java.util.List r5 = com.sec.ims.configuration.DATA.DM_FIELD_LIST
            int r7 = java.lang.Integer.parseInt(r19)
            java.lang.Object r5 = r5.get(r7)
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r5 = (com.sec.ims.configuration.DATA.DM_FIELD_INFO) r5
            java.lang.String r5 = r5.getName()
            boolean r5 = r2.contains(r5)
            if (r5 == 0) goto L_0x0325
            java.util.List r5 = com.sec.ims.configuration.DATA.DM_FIELD_LIST
            int r7 = java.lang.Integer.parseInt(r18)
            java.lang.Object r5 = r5.get(r7)
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r5 = (com.sec.ims.configuration.DATA.DM_FIELD_INFO) r5
            java.lang.String r5 = r5.getName()
            boolean r5 = r2.contains(r5)
            if (r5 == 0) goto L_0x0325
            r1.removeMessages(r6)
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r5 = r1.mDmContentValues
            int r7 = r1.mOmadmProvisioningTransactionId
            r5.removeConfigData(r7, r6)
            r5 = -1
            r1.mOmadmProvisioningTransactionId = r5
            android.net.Uri r5 = com.sec.ims.settings.NvConfiguration.URI
            r1.insertData(r5, r13)
            goto L_0x0325
        L_0x02f7:
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r5 = r1.mDmContentValues
            int r5 = r5.getNewTransactionId()
            r1.mOmadmProvisioningTransactionId = r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r7 = "OMADM update, created transaction : "
            r5.append(r7)
            int r7 = r1.mOmadmProvisioningTransactionId
            r5.append(r7)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r10, r5)
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r5 = r1.mDmContentValues
            int r7 = r1.mOmadmProvisioningTransactionId
            r5.addConfigData(r7, r6, r13)
            android.os.Message r5 = r1.obtainMessage(r6)
            r6 = 5000(0x1388, double:2.4703E-320)
            r1.sendMessageDelayed(r5, r6)
        L_0x0325:
            return r0
        L_0x0326:
            r2 = r29
            if (r2 >= 0) goto L_0x034a
            java.lang.String r5 = "immediately write DM config"
            android.util.Log.d(r10, r5)
            int r5 = r12.size()
            if (r5 == 0) goto L_0x033e
            java.lang.String r5 = "content://com.samsung.rcs.dmconfigurationprovider/omadm/"
            android.net.Uri r5 = android.net.Uri.parse(r5)
            r1.insertData(r5, r12)
        L_0x033e:
            int r5 = r13.size()
            if (r5 == 0) goto L_0x0361
            android.net.Uri r5 = com.sec.ims.settings.NvConfiguration.URI
            r1.insertData(r5, r13)
            goto L_0x0361
        L_0x034a:
            int r5 = r12.size()
            if (r5 == 0) goto L_0x0356
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r5 = r1.mDmContentValues
            r6 = 0
            r5.addConfigData(r2, r6, r12)
        L_0x0356:
            int r5 = r13.size()
            if (r5 == 0) goto L_0x0361
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r5 = r1.mDmContentValues
            r5.addConfigData(r2, r0, r13)
        L_0x0361:
            int r5 = r14.size()
            if (r5 <= 0) goto L_0x0373
            android.content.Context r5 = r1.mContext
            android.content.ContentResolver r5 = r5.getContentResolver()
            android.net.Uri r6 = com.sec.internal.constants.ims.settings.GlobalSettingsConstants.CONTENT_URI
            r7 = 0
            r5.update(r6, r14, r7, r7)
        L_0x0373:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DmConfigModule.updateConfigValues(android.content.ContentValues, int, int):boolean");
    }

    private void insertData(Uri uri, ContentValues cv) {
        if (!cv.containsKey(INTERNAL_KEY_PROCESS_NAME)) {
            cv.put(INTERNAL_KEY_PROCESS_NAME, PackageUtils.getProcessNameById(this.mContext, Binder.getCallingPid()));
        }
        this.mContext.getContentResolver().insert(uri, cv);
    }

    public void handleMessage(Message msg) {
        Log.d(LOG_TAG, "handleMessage: evt=" + msg.what);
        int i = msg.what;
        if (i == 1) {
            DmContentValues dmContentValues = this.mDmContentValues;
            if (dmContentValues != null) {
                ContentValues configData = dmContentValues.getConfigData(msg.arg1, 0);
                if (configData == null) {
                    Log.e(LOG_TAG, "no opt transactionId " + msg.arg1);
                } else {
                    this.mDmContentValues.removeConfigData(msg.arg1, 0);
                    insertData(Uri.parse(CONFIG_DM_PROVIDER), configData);
                }
                ContentValues configData2 = this.mDmContentValues.getConfigData(msg.arg1, 1);
                if (configData2 == null) {
                    Log.e(LOG_TAG, "no nv transactionId " + msg.arg1);
                } else {
                    this.mDmContentValues.removeConfigData(msg.arg1, 1);
                    insertData(NvConfiguration.URI, configData2);
                }
                this.mRegMgr.onDmConfigurationComplete();
                if (this.mDmContentValues.allTransactionDone()) {
                    Log.d(LOG_TAG, "all config transaction done");
                    this.mDmContentValues = null;
                }
            }
        } else if (i != 2) {
            Log.e(LOG_TAG, "unknown event");
        } else {
            ContentValues configData3 = this.mDmContentValues.getConfigData(this.mOmadmProvisioningTransactionId, 2);
            if (configData3 == null) {
                Log.e(LOG_TAG, "no pending transaction for : " + this.mOmadmProvisioningTransactionId);
                return;
            }
            Log.d(LOG_TAG, "EVT_FINISH_OMADM_PROV_UPDATE, completing transaction : " + this.mOmadmProvisioningTransactionId);
            this.mDmContentValues.removeConfigData(this.mOmadmProvisioningTransactionId, 2);
            this.mOmadmProvisioningTransactionId = -1;
            insertData(NvConfiguration.URI, configData3);
        }
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        this.mEventLog.dump();
    }

    static class DmContentValues {
        private static final String LOG_TAG = "DmContentValues";
        protected static final int NUM_OF_MAP = 3;
        protected static final int TYPE_CONFIG_DB = 0;
        protected static final int TYPE_NV = 1;
        protected static final int TYPE_OTA = 2;
        private static int mMaxTransactionId = 0;
        private List<Map<Integer, ContentValues>> mTransactionMaps = new ArrayList();

        DmContentValues() {
            for (int i = 0; i < 3; i++) {
                this.mTransactionMaps.add(new HashMap());
            }
        }

        /* access modifiers changed from: protected */
        public int getNewTransactionId() {
            int i = mMaxTransactionId + 1;
            mMaxTransactionId = i;
            return i;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.content.ContentValues} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v20, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: android.content.ContentValues} */
        /* access modifiers changed from: protected */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void addConfigData(int r5, int r6, android.content.ContentValues r7) {
            /*
                r4 = this;
                r0 = 0
                java.util.List<java.util.Map<java.lang.Integer, android.content.ContentValues>> r1 = r4.mTransactionMaps
                java.lang.Object r1 = r1.get(r6)
                java.util.Map r1 = (java.util.Map) r1
                java.lang.Integer r2 = java.lang.Integer.valueOf(r5)
                boolean r1 = r1.containsKey(r2)
                if (r1 == 0) goto L_0x0027
                java.util.List<java.util.Map<java.lang.Integer, android.content.ContentValues>> r1 = r4.mTransactionMaps
                java.lang.Object r1 = r1.get(r6)
                java.util.Map r1 = (java.util.Map) r1
                java.lang.Integer r2 = java.lang.Integer.valueOf(r5)
                java.lang.Object r1 = r1.get(r2)
                r0 = r1
                android.content.ContentValues r0 = (android.content.ContentValues) r0
                goto L_0x006f
            L_0x0027:
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "no transaction with transactionId "
                r1.append(r2)
                r1.append(r5)
                java.lang.String r2 = " create new transaction"
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                java.lang.String r2 = "DmContentValues"
                android.util.Log.d(r2, r1)
                java.util.List<java.util.Map<java.lang.Integer, android.content.ContentValues>> r1 = r4.mTransactionMaps
                java.lang.Object r1 = r1.get(r6)
                java.util.Map r1 = (java.util.Map) r1
                java.lang.Integer r2 = java.lang.Integer.valueOf(r5)
                android.content.ContentValues r3 = new android.content.ContentValues
                r3.<init>()
                r1.put(r2, r3)
                java.util.List<java.util.Map<java.lang.Integer, android.content.ContentValues>> r1 = r4.mTransactionMaps
                java.lang.Object r1 = r1.get(r6)
                java.util.Map r1 = (java.util.Map) r1
                java.lang.Integer r2 = java.lang.Integer.valueOf(r5)
                java.lang.Object r1 = r1.get(r2)
                r0 = r1
                android.content.ContentValues r0 = (android.content.ContentValues) r0
                int r1 = mMaxTransactionId
                if (r5 <= r1) goto L_0x006f
                mMaxTransactionId = r5
            L_0x006f:
                r0.putAll(r7)
                java.util.List<java.util.Map<java.lang.Integer, android.content.ContentValues>> r1 = r4.mTransactionMaps
                java.lang.Object r1 = r1.get(r6)
                java.util.Map r1 = (java.util.Map) r1
                java.lang.Integer r2 = java.lang.Integer.valueOf(r5)
                r1.put(r2, r0)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DmConfigModule.DmContentValues.addConfigData(int, int, android.content.ContentValues):void");
        }

        /* access modifiers changed from: protected */
        public ContentValues getConfigData(int transactionId, int type) {
            if (this.mTransactionMaps.size() == 0 || this.mTransactionMaps.get(type) == null || !this.mTransactionMaps.get(type).containsKey(Integer.valueOf(transactionId))) {
                return null;
            }
            return (ContentValues) this.mTransactionMaps.get(type).get(Integer.valueOf(transactionId));
        }

        /* access modifiers changed from: protected */
        public void removeConfigData(int transactionId, int type) {
            if (this.mTransactionMaps.get(type).containsKey(Integer.valueOf(transactionId))) {
                this.mTransactionMaps.get(type).remove(Integer.valueOf(transactionId));
            }
        }

        /* access modifiers changed from: protected */
        public boolean allTransactionDone() {
            for (int i = 0; i < 3; i++) {
                if (!this.mTransactionMaps.get(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }
}
