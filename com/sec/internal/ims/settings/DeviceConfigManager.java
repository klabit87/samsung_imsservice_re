package com.sec.internal.ims.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.telephony.ITelephony;
import com.sec.ims.configuration.DATA;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsSettings;
import com.sec.ims.settings.NvConfiguration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.constants.ims.util.CscParserConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.XmlUtils;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.config.adapters.StorageAdapter;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.settings.SettingsProviderUtility;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DeviceConfigManager {
    private static final String CONFIG_URI = "content://com.samsung.rcs.dmconfigurationprovider/";
    private static final String DATABASE_NAME_PREFIX = "OMADM_";
    public static final String DEFAULTMSGAPPINUSE = "defaultmsgappinuse";
    private static final String DEFAULT_DATABASE_NAME = "DEFAULT";
    public static final String IMS = "ims";
    private static final String IMS_TEST_MODE = "IMS_TEST_MODE";
    private static final Object LOCK = new Object();
    public static final String LOG_TAG = "DeviceConfigManager";
    public static final String NV_INIT_DONE = "nv_init_done";
    public static final String NV_VERSION_DEFAULT = "1";
    public static final String NV_VERSION_USC_NR_OOB = "2";
    public static final String OMADM_PREFIX = "omadm/./3GPP_IMS/";
    private static final String OMCNW_CODE_PROPERTY = "ro.csc.omcnw_code";
    private static final String OMC_CODE_PROPERTY = "ro.csc.sales_code";
    public static final String RCS = "rcs";
    public static final String RCS_SWITCH = "rcsswitch";
    public static final String VOLTE = "volte";
    private static PendingIntent mAlarmIntent;
    private static AlarmManager mAlarmManager;
    private Context mContext;
    private DebugConfigStorage mDebugConfigStorage;
    protected IStorageAdapter mDmStorage = null;
    private SimpleEventLog mEventLog;
    private ImsServiceSwitch mImsServiceSwitch;
    private SimConstants.SIM_STATE mLastSimState = SimConstants.SIM_STATE.UNKNOWN;
    private Mno mMno = Mno.DEFAULT;
    private String mMvnoName = "";
    private ArrayList<String> mNvList = new ArrayList<>();
    private NvStorage mNvStorage = null;
    private int mPhoneId = 0;
    private ImsProfileCache mProfileCache;
    private SmsSetting mSmsSetting;
    private UserConfigStorage mUserConfigStorage;

    public DeviceConfigManager(Context context, int phoneId) {
        boolean z = false;
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 500);
        this.mMno = Mno.DEFAULT;
        String prevMnoname = GlobalSettingsManager.getInstance(this.mContext, phoneId).getGlobalSettings().getPreviousMno();
        if ("".equals(prevMnoname)) {
            this.mMno = Mno.fromSalesCode(SemSystemProperties.get("ro.csc.omcnw_code", SemSystemProperties.get("ro.csc.sales_code", "")));
        } else {
            this.mMno = Mno.fromName(prevMnoname);
        }
        ImsProfileCache imsProfileCache = new ImsProfileCache(this.mContext, this.mMno.getName(), phoneId);
        this.mProfileCache = imsProfileCache;
        imsProfileCache.load(this.mMno == Mno.GCF ? true : z);
        this.mSmsSetting = new SmsSetting(this.mContext, this.mPhoneId);
        updateNvList();
        if (!this.mNvList.isEmpty()) {
            this.mNvStorage = new NvStorage(this.mContext, this.mMno.getMatchedNetworkCode(OmcCode.getNWCode(this.mPhoneId)), this.mPhoneId);
        }
        this.mDmStorage = new StorageAdapter();
        this.mUserConfigStorage = new UserConfigStorage(this.mContext, prevMnoname, phoneId);
        this.mDebugConfigStorage = new DebugConfigStorage(this.mContext);
        this.mImsServiceSwitch = new ImsServiceSwitch(this.mContext, this.mPhoneId);
        if (SettingsProviderUtility.getDbCreatState(this.mContext) == SettingsProviderUtility.DB_CREAT_STATE.DB_CREATING_FAIL && restoreDefaultImsProfile()) {
            SettingsProviderUtility.setDbCreated(this.mContext, true);
        }
    }

    private static void getConfig(XmlPullParser parser, SparseArray<String> values, ContentValues writtenNvItems, ArrayList<String> carrierNvList) {
        int key = -1;
        boolean initDone = writtenNvItems.containsKey("omadm/./3GPP_IMS/nv_init_done");
        Object obj = "";
        while (true) {
            try {
                int next = parser.next();
                int event = next;
                if (next == 1) {
                    return;
                }
                if (event == 2 && ImsConstants.Intents.EXTRA_UPDATED_ITEM.equalsIgnoreCase(parser.getName())) {
                    key = Integer.parseInt(parser.getAttributeValue(0));
                } else if (event == 3) {
                    if ("configuration".equalsIgnoreCase(parser.getName())) {
                        return;
                    }
                } else if (event == 4 && parser.getText().trim().length() > 0) {
                    String nvName = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(key)).getName().replace("./3GPP_IMS/", "");
                    if (carrierNvList.contains(nvName) && initDone) {
                        if (writtenNvItems.containsKey("omadm/./3GPP_IMS/" + nvName)) {
                        }
                    }
                    values.put(key, parser.getText());
                }
            } catch (IOException | NumberFormatException | XmlPullParserException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public GlobalSettingsRepo getGlobalSettingsRepo() {
        return GlobalSettingsManager.getInstance(this.mContext, this.mPhoneId).getGlobalSettings();
    }

    /* access modifiers changed from: package-private */
    public ImsProfileCache getProfileCache() {
        return this.mProfileCache;
    }

    /* access modifiers changed from: package-private */
    public SmsSetting getSmsSetting() {
        return this.mSmsSetting;
    }

    /* access modifiers changed from: package-private */
    public UserConfigStorage getUserConfigStorage() {
        return this.mUserConfigStorage;
    }

    /* access modifiers changed from: package-private */
    public DebugConfigStorage getDebugConfigStorage() {
        return this.mDebugConfigStorage;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<String> getNvList() {
        return this.mNvList;
    }

    private void updateNvList() {
        this.mNvList.clear();
        ArrayList<String> nvList = getNvList(this.mMno.getMatchedNetworkCode(OmcCode.getNWCode(this.mPhoneId)));
        this.mNvList = nvList;
        if (!nvList.isEmpty()) {
            this.mNvList.add(NV_INIT_DONE);
        }
        Log.d("DeviceConfigManager[" + this.mPhoneId + "]", "updateNvList(" + this.mMno.getMatchedNetworkCode(OmcCode.getNWCode(this.mPhoneId)) + ") : nv list : " + Arrays.toString(this.mNvList.toArray()));
    }

    /* Debug info: failed to restart local var, previous not found, register: 14 */
    private boolean initStorage() {
        Cursor cursor;
        synchronized (LOCK) {
            if (this.mDmStorage.getState() != 1) {
                String name = getDababaseName(this.mMno.getMatchedSalesCode(OmcCode.get()));
                SimpleEventLog simpleEventLog = this.mEventLog;
                simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] DM CONFIG DB : " + name + ", Mno : " + this.mMno.getMatchedSalesCode(OmcCode.get()));
                this.mDmStorage.open(this.mContext, name, this.mPhoneId);
                updateNvList();
                ContentValues writtenNv = new ContentValues();
                if (!this.mNvList.isEmpty()) {
                    if (this.mNvStorage == null) {
                        this.mNvStorage = new NvStorage(this.mContext, this.mMno.getMatchedNetworkCode(OmcCode.getNWCode(this.mPhoneId)), this.mPhoneId);
                    }
                    Cursor nvItems = this.mNvStorage.query(NvStorage.ID_OMADM, (String[]) null);
                    if (nvItems != null) {
                        try {
                            if (nvItems.moveToFirst()) {
                                do {
                                    writtenNv.put(nvItems.getString(0), nvItems.getString(1));
                                } while (nvItems.moveToNext());
                            }
                        } catch (Throwable th) {
                            th.addSuppressed(th);
                        }
                    }
                    if (nvItems != null) {
                        nvItems.close();
                    }
                }
                SparseArray<String> values = getDefaultDmConfig(this.mMno.getMatchedSalesCode(OmcCode.get()), writtenNv, this.mNvList);
                if (values != null && values.size() > 0) {
                    initDmConfig(values, this.mNvList);
                    if (!this.mNvList.isEmpty() && !writtenNv.containsKey("omadm/./3GPP_IMS/nv_init_done")) {
                        NvConfiguration.set(this.mContext, NV_INIT_DONE, "1");
                    }
                }
                int volteEnableVal = -1;
                int eabEnableVal = -1;
                int pollist = -1;
                cursor = readMultipleDm(new String[]{ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, ConfigConstants.ConfigPath.OMADM_POLL_LIST_SUB_EXP}, (String) null, (String[]) null);
                if (cursor != null) {
                    if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                        do {
                            String key = cursor.getString(0);
                            String value = cursor.getString(1);
                            if (ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED.equalsIgnoreCase(key)) {
                                volteEnableVal = value.equalsIgnoreCase("1");
                            } else if (ConfigConstants.ConfigPath.OMADM_EAB_SETTING.equalsIgnoreCase(key)) {
                                eabEnableVal = value.equalsIgnoreCase("1");
                            } else if (ConfigConstants.ConfigPath.OMADM_POLL_LIST_SUB_EXP.equalsIgnoreCase(key)) {
                                pollist = Integer.parseInt(value);
                            }
                        } while (cursor.moveToNext());
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (!ConfigUtil.isRcsEur(this.mMno) && !this.mMno.isOce() && this.mMno != Mno.ROGERS) {
                    if (!this.mMno.isLatin() || this.mMno == Mno.TCE) {
                        if (!this.mMno.isKor()) {
                            if (this.mMno != Mno.BELL) {
                                if ((this.mMno == Mno.GENERIC_IR92 || this.mMno == Mno.GCI || this.mMno == Mno.ALTICE) && volteEnableVal != 1) {
                                    initVoLTEFeature();
                                }
                            }
                        }
                        if (this.mMno.isKor() && pollist != 30) {
                            changePollListSubExp(30);
                        }
                        if (eabEnableVal != 1) {
                            initEabFeature();
                        }
                    }
                }
                if (volteEnableVal != 1) {
                    initVoLTEFeature();
                }
                if (eabEnableVal != 0) {
                    disableEabFeature();
                }
            }
        }
        return true;
        throw th;
        throw th;
    }

    private Cursor readAllOfDm(Uri uri) {
        String[] columnNames = {"PATH", "VALUE"};
        String[] columnValues = new String[columnNames.length];
        Map<String, String> readData = this.mDmStorage.readAll(uri.toString().replaceFirst(CONFIG_URI, ""));
        MatrixCursor cursor = new MatrixCursor(columnNames);
        if (readData != null) {
            for (Map.Entry<String, String> entry : readData.entrySet()) {
                if (!this.mNvList.contains(entry.getKey().replace("omadm/./3GPP_IMS/", ""))) {
                    columnValues[0] = entry.getKey();
                    columnValues[1] = entry.getValue();
                    cursor.addRow(columnValues);
                }
            }
        } else {
            IMSLog.e(LOG_TAG, this.mPhoneId, "readData is null");
        }
        if (!this.mNvList.isEmpty()) {
            Cursor nvItems = this.mNvStorage.query(NvStorage.ID_OMADM, (String[]) null);
            if (nvItems != null) {
                try {
                    if (nvItems.moveToFirst()) {
                        do {
                            if (this.mNvList.contains(nvItems.getString(0).replace("omadm/./3GPP_IMS/", ""))) {
                                columnValues[0] = nvItems.getString(0);
                                columnValues[1] = nvItems.getString(1);
                                cursor.addRow(columnValues);
                            }
                        } while (nvItems.moveToNext());
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (nvItems != null) {
                nvItems.close();
            }
        }
        return cursor;
        throw th;
    }

    private Cursor readMultipleDm(String[] projection, String selection, String[] selectionArgs) {
        List<String> dm = new ArrayList<>();
        List<String> nv = new ArrayList<>();
        for (String path : projection) {
            if (this.mNvList.contains(path)) {
                nv.add(path);
            } else {
                if (!path.contains("omadm/./3GPP_IMS/")) {
                    path = "omadm/./3GPP_IMS/" + path;
                }
                dm.add(path);
            }
        }
        if (dm.size() > 0 && nv.size() > 0) {
            return new MergeCursor(new Cursor[]{this.mDmStorage.query((String[]) dm.toArray(new String[0])), this.mNvStorage.query(NvStorage.ID_OMADM, projection)});
        } else if (dm.size() > 0) {
            return this.mDmStorage.query((String[]) dm.toArray(new String[0]));
        } else {
            if (nv.size() > 0) {
                return this.mNvStorage.query(NvStorage.ID_OMADM, projection);
            }
            return null;
        }
    }

    private Cursor readSingleDm(Uri uri, String selection, String[] selectionArgs) {
        String key = uri.getLastPathSegment();
        if (this.mNvList.contains(key)) {
            IMSLog.d(LOG_TAG, this.mPhoneId, "read from NV");
            return this.mNvStorage.query(NvStorage.ID_OMADM, new String[]{key});
        }
        String path = uri.toString();
        int i = this.mPhoneId;
        IMSLog.d(LOG_TAG, i, "read from DB : " + path);
        String value = this.mDmStorage.read(path.replaceFirst(CONFIG_URI, ""));
        MatrixCursor cursor = new MatrixCursor(new String[]{"PATH", "VALUE"});
        cursor.addRow(new String[]{path.replace(CONFIG_URI, "").toLowerCase(Locale.US), value});
        return cursor;
    }

    public int deleteDm(Uri uri) {
        if (!initStorage()) {
            return 0;
        }
        String path = uri.toString();
        int i = this.mPhoneId;
        IMSLog.d(LOG_TAG, i, "delete uri:" + IMSLog.checker(path));
        String key = path.replace(CONFIG_URI, "").replace(NvStorage.ID_OMADM, "");
        if (this.mNvList.contains(key)) {
            this.mNvStorage.delete(key);
        }
        if (path.matches("^content://com.samsung.rcs.dmconfigurationprovider/[\\.\\w-_/]*")) {
            int count = this.mDmStorage.delete(path.replaceFirst(CONFIG_URI, ""));
            this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(CONFIG_URI, this.mPhoneId), (ContentObserver) null);
            return count;
        }
        throw new IllegalArgumentException(path + " is not a correct DmConfigurationProvider Uri");
    }

    public Uri insertDm(Uri uri, ContentValues values) {
        if (!initStorage()) {
            return null;
        }
        ContentValues nv = new ContentValues();
        Map<String, String> data = new HashMap<>();
        for (Map.Entry<String, Object> value : values.valueSet()) {
            if (value.getValue() instanceof String) {
                String key = value.getKey();
                if (key.lastIndexOf("/") == key.length()) {
                    key = key.substring(0, key.length() - 1);
                }
                String dmItem = key;
                if (key.lastIndexOf("/") >= 0) {
                    dmItem = key.substring(key.lastIndexOf("/") + 1);
                }
                IMSLog.d(LOG_TAG, this.mPhoneId, "dmItem : " + dmItem);
                if (this.mNvList.contains(dmItem)) {
                    nv.put(dmItem, (String) value.getValue());
                } else {
                    if (!key.startsWith("omadm/./3GPP_IMS/")) {
                        if (key.startsWith("./3GPP_IMS/")) {
                            key = DmConfigModule.DM_PATH + key;
                        } else {
                            key = "omadm/./3GPP_IMS/" + dmItem;
                        }
                    }
                    data.put(key, (String) value.getValue());
                    if (value.getKey().contains(IMS_TEST_MODE)) {
                        SemSystemProperties.set(ImsConstants.SystemProperties.IMS_TEST_MODE_PROP, (String) value.getValue());
                        if (this.mMno == Mno.VZW || this.mMno == Mno.GCF) {
                            sendRawRequest(Integer.valueOf((String) value.getValue()).intValue());
                        }
                    }
                }
            }
        }
        if (nv.size() > 0) {
            this.mNvStorage.insert(NvStorage.ID_OMADM, nv);
        }
        if (data.size() > 0) {
            this.mDmStorage.writeAll(data);
        }
        return uri;
    }

    public int updateDm(Uri uri, ContentValues values) {
        insertDm(uri, values);
        return values.size();
    }

    public Cursor queryDm(Uri uri, String[] projection, String selection, String[] selectionArgs, boolean isReadAllOmadm) {
        if (!initStorage()) {
            MatrixCursor cursor = new MatrixCursor(new String[]{"NODATA"});
            cursor.addRow(new String[]{"NODATA"});
            return cursor;
        } else if (isReadAllOmadm) {
            return readAllOfDm(uri);
        } else {
            if (projection != null) {
                return readMultipleDm(projection, selection, selectionArgs);
            }
            return readSingleDm(uri, selection, selectionArgs);
        }
    }

    private void sendRawRequest(int testMode) {
        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(PhoneConstants.PHONE_KEY));
        if (telephony != null) {
            byte[] cmd = new byte[5];
            byte[] resp = new byte[4];
            int i = 0;
            cmd[0] = 9;
            cmd[1] = 15;
            cmd[2] = 0;
            cmd[3] = 5;
            if (testMode == 1) {
                i = 1;
            }
            cmd[4] = (byte) i;
            try {
                telephony.invokeOemRilRequestRaw(cmd, resp);
                Log.d(LOG_TAG, "set testmode as " + testMode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initVoLTEFeature() {
        String operator = SemSystemProperties.get(Mno.MOCK_MNO_PROPERTY, "");
        if (TextUtils.isEmpty(operator)) {
            operator = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getSimOperator();
        }
        ContentValues cscImsSettings = CscParser.getCscImsSetting(operator, this.mPhoneId);
        if (cscImsSettings != null && cscImsSettings.size() > 0) {
            boolean isEnableVoLTE = CollectionUtils.getBooleanValue(cscImsSettings, CscParserConstants.CustomerSettingTable.VoLTE.ENABLE_VOLTE, false);
            boolean isEnableVoWIFI = CollectionUtils.getBooleanValue(cscImsSettings, CscParserConstants.CustomerSettingTable.DeviceManagement.SUPPORT_VOWIFI, false);
            if (isEnableVoLTE || isEnableVoWIFI) {
                ContentValues cv = new ContentValues();
                cv.put("VOLTE_ENABLED", "1");
                insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/VOLTE_ENABLED"), cv);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initSmsOverImsFeature() {
        boolean isSmsOverIpNetworkIndication = GlobalSettingsManager.getInstance(this.mContext, this.mPhoneId).getBoolean(GlobalSettingsConstants.Registration.SMS_OVER_IP_INDICATION, false);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("[" + this.mPhoneId + "] initSmsOverImsFeature: isSmsOverIpNetworkIndication: " + isSmsOverIpNetworkIndication);
        NvConfiguration.setSmsIpNetworkIndi(this.mContext, isSmsOverIpNetworkIndication, this.mPhoneId);
    }

    /* access modifiers changed from: protected */
    public void initIPsecFeature() {
        ImsProfile profile;
        boolean isIPsecEnabled = false;
        List<ImsProfile> profileList = ImsProfileLoaderInternal.getProfileList(this.mContext, this.mPhoneId);
        if (profileList == null || profileList.size() == 0) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "initIPsecFeature: profileList null ");
            return;
        }
        if (profileList != null && profileList.size() > 0) {
            Iterator<ImsProfile> it = profileList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                profile = it.next();
                if (profile == null || (!profile.hasService("mmtel") && !profile.hasService("mmtel-video"))) {
                }
            }
            isIPsecEnabled = profile.isIpSecEnabled();
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("[" + this.mPhoneId + "] initIPsecFeature: isIPsecEnabled: " + isIPsecEnabled);
        ContentValues cv = new ContentValues();
        cv.put("IPSEC_ENABLED", isIPsecEnabled ? "1" : "0");
        insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/IPSEC_ENABLED"), cv);
    }

    /* access modifiers changed from: protected */
    public void initH265Hd720Payload() {
        ImsProfile profile;
        int h265_hd720_payload = 112;
        List<ImsProfile> profileList = ImsProfileLoaderInternal.getProfileList(this.mContext, this.mPhoneId);
        if (profileList == null || profileList.size() == 0) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "initH265Hd720Payload: profileList null ");
            return;
        }
        if (profileList != null && profileList.size() > 0) {
            Iterator<ImsProfile> it = profileList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                profile = it.next();
                if (profile == null || (!profile.hasService("mmtel") && !profile.hasService("mmtel-video"))) {
                }
            }
            h265_hd720_payload = profile.getH265Hd720pPayload();
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("[" + this.mPhoneId + "] initH265Hd720Payload: h265_hd720_payload: " + h265_hd720_payload);
        ContentValues cv = new ContentValues();
        cv.put("H265_720P", Integer.toString(h265_hd720_payload));
        insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/H265_720P"), cv);
    }

    /* access modifiers changed from: protected */
    public void initEabFeature() {
        ContentValues cscImsSettings;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (sm != null && (cscImsSettings = CscParser.getCscImsSetting(sm.getNetworkNames(), this.mPhoneId)) != null && cscImsSettings.size() > 0) {
            boolean isEnableRcs = CollectionUtils.getBooleanValue(cscImsSettings, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS, false);
            boolean isEnableRcsChat = CollectionUtils.getBooleanValue(cscImsSettings, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS_CHAT_SERVICE, false);
            if (isEnableRcs || isEnableRcsChat) {
                ContentValues cv = new ContentValues();
                cv.put("EAB_SETTING", "1");
                insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/EAB_SETTING"), cv);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void disableEabFeature() {
        ContentValues cv = new ContentValues();
        cv.put("EAB_SETTING", "0");
        insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/EAB_SETTING"), cv);
    }

    /* access modifiers changed from: protected */
    public void changePollListSubExp(int expire) {
        ContentValues cscImsSettings;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (sm != null && (cscImsSettings = CscParser.getCscImsSetting(sm.getNetworkNames(), this.mPhoneId)) != null && cscImsSettings.size() > 0) {
            boolean isEnableRcs = CollectionUtils.getBooleanValue(cscImsSettings, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS, false);
            boolean isEnableRcsChat = CollectionUtils.getBooleanValue(cscImsSettings, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS_CHAT_SERVICE, false);
            if (isEnableRcs || isEnableRcsChat) {
                ContentValues cv = new ContentValues();
                cv.put("POLL_LIST_SUB_EXP", String.valueOf(expire));
                insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/POLL_LIST_SUB_EXP"), cv);
            }
        }
    }

    private String getDababaseName(String salesCode) {
        if (TextUtils.isEmpty(salesCode)) {
            return "OMADM_DEFAULT";
        }
        String dbName = salesCode;
        if (this.mMno == Mno.SPRINT) {
            dbName = this.mMno.getAllSalesCodes()[0];
        }
        return DATABASE_NAME_PREFIX + dbName;
    }

    /* access modifiers changed from: protected */
    public void initDmConfig(SparseArray<String> initValues, ArrayList<String> nvList) {
        Map<String, String> writtenDb = new HashMap<>();
        writtenDb.putAll(this.mDmStorage.readAll("omadm/*"));
        Map<String, String> dbType = new HashMap<>();
        ContentValues nvType = new ContentValues();
        for (int i = 0; i < initValues.size(); i++) {
            String item = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(initValues.keyAt(i))).getName().replace("./3GPP_IMS/", "");
            String value = initValues.valueAt(i);
            if (nvList.contains(item)) {
                Log.d("DeviceConfigManager[" + this.mPhoneId + "]", "initDmConfig : put into NV : " + item + ", " + value);
                nvType.put(item, value);
            } else {
                if (!item.contains("omadm/./3GPP_IMS/")) {
                    item = "omadm/./3GPP_IMS/" + item;
                }
                if (!writtenDb.containsKey(item)) {
                    Log.d("DeviceConfigManager[" + this.mPhoneId + "]", "initDmConfig : put into DB : " + item + ", " + value);
                    dbType.put(item, value);
                }
            }
        }
        if (dbType.size() > 0) {
            this.mDmStorage.writeAll(dbType);
        }
        if (nvType.size() > 0) {
            this.mNvStorage.insert(NvStorage.ID_OMADM, nvType);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0243, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0247, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void updateMno(android.content.ContentValues r14) {
        /*
            r13 = this;
            monitor-enter(r13)
            com.sec.internal.helper.SimpleEventLog r0 = r13.mEventLog     // Catch:{ all -> 0x0249 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0249 }
            r1.<init>()     // Catch:{ all -> 0x0249 }
            java.lang.String r2 = "simSlot["
            r1.append(r2)     // Catch:{ all -> 0x0249 }
            int r2 = r13.mPhoneId     // Catch:{ all -> 0x0249 }
            r1.append(r2)     // Catch:{ all -> 0x0249 }
            java.lang.String r2 = "] updateMno"
            r1.append(r2)     // Catch:{ all -> 0x0249 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0249 }
            r0.logAndAdd(r1)     // Catch:{ all -> 0x0249 }
            java.lang.Object r0 = LOCK     // Catch:{ all -> 0x0249 }
            monitor-enter(r0)     // Catch:{ all -> 0x0249 }
            java.lang.String r1 = "mnoname"
            java.lang.String r1 = r14.getAsString(r1)     // Catch:{ all -> 0x0244 }
            java.lang.String r2 = "hassim"
            java.lang.Boolean r2 = r14.getAsBoolean(r2)     // Catch:{ all -> 0x0244 }
            r3 = 0
            if (r2 != 0) goto L_0x0036
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r3)     // Catch:{ all -> 0x0244 }
            r2 = r4
        L_0x0036:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.fromName(r1)     // Catch:{ all -> 0x0244 }
            r13.mMno = r4     // Catch:{ all -> 0x0244 }
            java.lang.String r4 = "mvnoname"
            java.lang.String r4 = r14.getAsString(r4)     // Catch:{ all -> 0x0244 }
            r13.mMvnoName = r4     // Catch:{ all -> 0x0244 }
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r4 = r13.mLastSimState     // Catch:{ all -> 0x0244 }
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r5 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.UNKNOWN     // Catch:{ all -> 0x0244 }
            if (r4 == r5) goto L_0x006e
            boolean r4 = r2.booleanValue()     // Catch:{ all -> 0x0244 }
            if (r4 != 0) goto L_0x006e
            com.sec.internal.constants.Mno r4 = r13.mMno     // Catch:{ all -> 0x0244 }
            boolean r4 = r4.isHkMo()     // Catch:{ all -> 0x0244 }
            if (r4 != 0) goto L_0x006e
            com.sec.internal.constants.Mno r4 = r13.mMno     // Catch:{ all -> 0x0244 }
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.CTCMO     // Catch:{ all -> 0x0244 }
            if (r4 == r5) goto L_0x006e
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r3 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.ABSENT     // Catch:{ all -> 0x0244 }
            r13.mLastSimState = r3     // Catch:{ all -> 0x0244 }
            java.lang.String r3 = "DeviceConfigManager"
            int r4 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            java.lang.String r5 = "Skip updating config modules when SIM ejected"
            com.sec.internal.log.IMSLog.i(r3, r4, r5)     // Catch:{ all -> 0x0244 }
            monitor-exit(r0)     // Catch:{ all -> 0x0244 }
            monitor-exit(r13)
            return
        L_0x006e:
            boolean r4 = r2.booleanValue()     // Catch:{ all -> 0x0244 }
            if (r4 == 0) goto L_0x0077
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r4 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.LOADED     // Catch:{ all -> 0x0244 }
            goto L_0x0079
        L_0x0077:
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r4 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.ABSENT     // Catch:{ all -> 0x0244 }
        L_0x0079:
            r13.mLastSimState = r4     // Catch:{ all -> 0x0244 }
            com.sec.internal.ims.settings.ImsProfileCache r4 = r13.mProfileCache     // Catch:{ all -> 0x0244 }
            r4.updateMno(r14)     // Catch:{ all -> 0x0244 }
            com.sec.internal.ims.settings.UserConfigStorage r4 = r13.mUserConfigStorage     // Catch:{ all -> 0x0244 }
            r4.reset(r1)     // Catch:{ all -> 0x0244 }
            android.content.Context r4 = r13.mContext     // Catch:{ all -> 0x0244 }
            int r5 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            com.sec.internal.ims.settings.GlobalSettingsManager r4 = com.sec.internal.ims.settings.GlobalSettingsManager.getInstance(r4, r5)     // Catch:{ all -> 0x0244 }
            com.sec.internal.ims.settings.GlobalSettingsRepo r4 = r4.getGlobalSettings()     // Catch:{ all -> 0x0244 }
            boolean r5 = r4.updateMno(r14)     // Catch:{ all -> 0x0244 }
            r6 = 268500992(0x10010000, float:2.5440764E-29)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x0244 }
            r7.<init>()     // Catch:{ all -> 0x0244 }
            int r8 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            r7.append(r8)     // Catch:{ all -> 0x0244 }
            java.lang.String r8 = ",UPD MNO:"
            r7.append(r8)     // Catch:{ all -> 0x0244 }
            r7.append(r5)     // Catch:{ all -> 0x0244 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x0244 }
            com.sec.internal.log.IMSLog.c(r6, r7)     // Catch:{ all -> 0x0244 }
            r6 = 0
            if (r5 == 0) goto L_0x015e
            com.sec.internal.interfaces.ims.config.IStorageAdapter r7 = r13.mDmStorage     // Catch:{ all -> 0x0244 }
            if (r7 == 0) goto L_0x00bd
            com.sec.internal.interfaces.ims.config.IStorageAdapter r7 = r13.mDmStorage     // Catch:{ all -> 0x0244 }
            r7.close()     // Catch:{ all -> 0x0244 }
            goto L_0x00c4
        L_0x00bd:
            com.sec.internal.ims.config.adapters.StorageAdapter r7 = new com.sec.internal.ims.config.adapters.StorageAdapter     // Catch:{ all -> 0x0244 }
            r7.<init>()     // Catch:{ all -> 0x0244 }
            r13.mDmStorage = r7     // Catch:{ all -> 0x0244 }
        L_0x00c4:
            com.sec.internal.ims.settings.NvStorage r7 = r13.mNvStorage     // Catch:{ all -> 0x0244 }
            if (r7 == 0) goto L_0x00cf
            com.sec.internal.ims.settings.NvStorage r7 = r13.mNvStorage     // Catch:{ all -> 0x0244 }
            r7.close()     // Catch:{ all -> 0x0244 }
            r13.mNvStorage = r6     // Catch:{ all -> 0x0244 }
        L_0x00cf:
            r13.initStorage()     // Catch:{ all -> 0x0244 }
            com.sec.internal.ims.settings.ImsServiceSwitch r7 = r13.mImsServiceSwitch     // Catch:{ all -> 0x0244 }
            r7.unregisterObserver()     // Catch:{ all -> 0x0244 }
            com.sec.internal.ims.settings.ImsServiceSwitch r7 = r13.mImsServiceSwitch     // Catch:{ all -> 0x0244 }
            r7.updateServiceSwitch(r14)     // Catch:{ all -> 0x0244 }
            com.sec.internal.ims.settings.ImsServiceSwitch r7 = r13.mImsServiceSwitch     // Catch:{ all -> 0x0244 }
            java.lang.String r8 = "enableServiceVolte"
            boolean r7 = r7.isImsSwitchEnabled(r8)     // Catch:{ all -> 0x0244 }
            java.lang.String r8 = "imsSwitchType"
            java.lang.Integer r8 = r14.getAsInteger(r8)     // Catch:{ all -> 0x0244 }
            int r8 = r8.intValue()     // Catch:{ all -> 0x0244 }
            r9 = 4
            if (r8 != r9) goto L_0x0147
            java.lang.String r8 = "hassim"
            java.lang.Boolean r8 = r14.getAsBoolean(r8)     // Catch:{ all -> 0x0244 }
            boolean r8 = r8.booleanValue()     // Catch:{ all -> 0x0244 }
            r9 = 1
            r10 = -1
            if (r8 == 0) goto L_0x010d
            android.content.Context r8 = r13.mContext     // Catch:{ all -> 0x0244 }
            int r11 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            int r8 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVoiceCallType(r8, r10, r11)     // Catch:{ all -> 0x0244 }
            if (r8 == r10) goto L_0x010d
            if (r7 != 0) goto L_0x010d
            r8 = r9
            goto L_0x010e
        L_0x010d:
            r8 = r3
        L_0x010e:
            java.lang.String r11 = "hassim"
            java.lang.Boolean r11 = r14.getAsBoolean(r11)     // Catch:{ all -> 0x0244 }
            boolean r11 = r11.booleanValue()     // Catch:{ all -> 0x0244 }
            if (r11 == 0) goto L_0x012f
            android.content.Context r11 = r13.mContext     // Catch:{ all -> 0x0244 }
            int r12 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            int r11 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVideoCallType(r11, r10, r12)     // Catch:{ all -> 0x0244 }
            if (r11 == r10) goto L_0x012f
            com.sec.internal.ims.settings.ImsServiceSwitch r10 = r13.mImsServiceSwitch     // Catch:{ all -> 0x0244 }
            java.lang.String r11 = "enableServiceVilte"
            boolean r10 = r10.isImsSwitchEnabled(r11)     // Catch:{ all -> 0x0244 }
            if (r10 != 0) goto L_0x012f
            goto L_0x0130
        L_0x012f:
            r9 = r3
        L_0x0130:
            r4.resetUserSettingAsDefault(r8, r9, r3)     // Catch:{ all -> 0x0244 }
            if (r7 != 0) goto L_0x0147
            android.content.ContentValues r10 = new android.content.ContentValues     // Catch:{ all -> 0x0244 }
            r10.<init>()     // Catch:{ all -> 0x0244 }
            java.lang.String r11 = "show_regi_info_in_sec_settings"
            java.lang.Boolean r12 = java.lang.Boolean.valueOf(r3)     // Catch:{ all -> 0x0244 }
            r10.put(r11, r12)     // Catch:{ all -> 0x0244 }
            r4.update(r10)     // Catch:{ all -> 0x0244 }
        L_0x0147:
            com.sec.internal.constants.Mno r8 = r13.mMno     // Catch:{ all -> 0x0244 }
            boolean r8 = r8.isKor()     // Catch:{ all -> 0x0244 }
            if (r8 == 0) goto L_0x015e
            r13.initSmsOverImsFeature()     // Catch:{ all -> 0x0244 }
            r13.initIPsecFeature()     // Catch:{ all -> 0x0244 }
            com.sec.internal.constants.Mno r8 = r13.mMno     // Catch:{ all -> 0x0244 }
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.KT     // Catch:{ all -> 0x0244 }
            if (r8 != r9) goto L_0x015e
            r13.initH265Hd720Payload()     // Catch:{ all -> 0x0244 }
        L_0x015e:
            int r7 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            boolean r7 = com.sec.internal.ims.rcs.RcsPolicyManager.loadRcsSettings(r7, r3)     // Catch:{ all -> 0x0244 }
            if (r5 != 0) goto L_0x0168
            if (r7 == 0) goto L_0x01ab
        L_0x0168:
            com.sec.internal.helper.SimpleEventLog r8 = r13.mEventLog     // Catch:{ all -> 0x0244 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x0244 }
            r9.<init>()     // Catch:{ all -> 0x0244 }
            java.lang.String r10 = "simSlot["
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            int r10 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            java.lang.String r10 = "] updateMno: notifyUpdated: GlobalSettings("
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            r9.append(r5)     // Catch:{ all -> 0x0244 }
            java.lang.String r10 = "), RcsPolicy("
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            r9.append(r7)     // Catch:{ all -> 0x0244 }
            java.lang.String r10 = ")"
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0244 }
            r8.logAndAdd(r9)     // Catch:{ all -> 0x0244 }
            android.content.Context r8 = r13.mContext     // Catch:{ all -> 0x0244 }
            android.content.ContentResolver r8 = r8.getContentResolver()     // Catch:{ all -> 0x0244 }
            android.net.Uri r9 = com.sec.internal.constants.ims.settings.GlobalSettingsConstants.CONTENT_URI     // Catch:{ all -> 0x0244 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0244 }
            int r10 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            android.net.Uri r9 = com.sec.internal.helper.UriUtil.buildUri(r9, r10)     // Catch:{ all -> 0x0244 }
            r8.notifyChange(r9, r6)     // Catch:{ all -> 0x0244 }
        L_0x01ab:
            com.sec.internal.ims.settings.SmsSetting r8 = r13.mSmsSetting     // Catch:{ all -> 0x0244 }
            boolean r8 = r8.updateMno(r14, r5)     // Catch:{ all -> 0x0244 }
            if (r8 == 0) goto L_0x01c8
            android.content.Context r9 = r13.mContext     // Catch:{ all -> 0x0244 }
            android.content.ContentResolver r9 = r9.getContentResolver()     // Catch:{ all -> 0x0244 }
            android.net.Uri r10 = com.sec.internal.constants.ims.ImsConstants.Uris.SMS_SETTING     // Catch:{ all -> 0x0244 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x0244 }
            int r11 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            android.net.Uri r10 = com.sec.internal.helper.UriUtil.buildUri(r10, r11)     // Catch:{ all -> 0x0244 }
            r9.notifyChange(r10, r6)     // Catch:{ all -> 0x0244 }
        L_0x01c8:
            com.sec.internal.constants.Mno r6 = r13.mMno     // Catch:{ all -> 0x0244 }
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.GCF     // Catch:{ all -> 0x0244 }
            if (r6 != r9) goto L_0x0205
            java.lang.String r6 = r13.getGcfInitRat()     // Catch:{ all -> 0x0244 }
            boolean r6 = android.text.TextUtils.isEmpty(r6)     // Catch:{ all -> 0x0244 }
            if (r6 == 0) goto L_0x0205
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0244 }
            r6.<init>()     // Catch:{ all -> 0x0244 }
            java.lang.String r9 = "DeviceConfigManager["
            r6.append(r9)     // Catch:{ all -> 0x0244 }
            int r9 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            r6.append(r9)     // Catch:{ all -> 0x0244 }
            java.lang.String r9 = "]"
            r6.append(r9)     // Catch:{ all -> 0x0244 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0244 }
            java.lang.String r9 = "init rat : lte,wifi"
            android.util.Log.d(r6, r9)     // Catch:{ all -> 0x0244 }
            android.content.ContentValues r6 = new android.content.ContentValues     // Catch:{ all -> 0x0244 }
            r6.<init>()     // Catch:{ all -> 0x0244 }
            java.lang.String r9 = "rat"
            java.lang.String r10 = "lte,wifi"
            r6.put(r9, r10)     // Catch:{ all -> 0x0244 }
            r13.updateGcfInitRat(r6)     // Catch:{ all -> 0x0244 }
        L_0x0205:
            com.sec.internal.constants.Mno r6 = r13.mMno     // Catch:{ all -> 0x0244 }
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.VZW     // Catch:{ all -> 0x0244 }
            if (r6 == r9) goto L_0x0211
            com.sec.internal.constants.Mno r6 = r13.mMno     // Catch:{ all -> 0x0244 }
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.GCF     // Catch:{ all -> 0x0244 }
            if (r6 != r9) goto L_0x0241
        L_0x0211:
            java.lang.String r6 = "persist.sys.ims_test_mode"
            int r3 = android.os.SemSystemProperties.getInt(r6, r3)     // Catch:{ all -> 0x0244 }
            r13.sendRawRequest(r3)     // Catch:{ all -> 0x0244 }
            com.sec.internal.helper.SimpleEventLog r6 = r13.mEventLog     // Catch:{ all -> 0x0244 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x0244 }
            r9.<init>()     // Catch:{ all -> 0x0244 }
            java.lang.String r10 = "simSlot["
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            int r10 = r13.mPhoneId     // Catch:{ all -> 0x0244 }
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            java.lang.String r10 = "] updateMno: send IMS_TESTMODE("
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            r9.append(r3)     // Catch:{ all -> 0x0244 }
            java.lang.String r10 = ")"
            r9.append(r10)     // Catch:{ all -> 0x0244 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0244 }
            r6.logAndAdd(r9)     // Catch:{ all -> 0x0244 }
        L_0x0241:
            monitor-exit(r0)     // Catch:{ all -> 0x0244 }
            monitor-exit(r13)
            return
        L_0x0244:
            r1 = move-exception
        L_0x0245:
            monitor-exit(r0)     // Catch:{ all -> 0x0247 }
            throw r1     // Catch:{ all -> 0x0249 }
        L_0x0247:
            r1 = move-exception
            goto L_0x0245
        L_0x0249:
            r14 = move-exception
            monitor-exit(r13)
            throw r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.updateMno(android.content.ContentValues):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0019, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.lang.String getMnoName() {
        /*
            r4 = this;
            monitor-enter(r4)
            java.lang.Object r0 = LOCK     // Catch:{ all -> 0x001b }
            monitor-enter(r0)     // Catch:{ all -> 0x001b }
            java.lang.String r1 = ""
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r2 = r4.mLastSimState     // Catch:{ all -> 0x0016 }
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r3 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.UNKNOWN     // Catch:{ all -> 0x0016 }
            if (r2 == r3) goto L_0x0013
            com.sec.internal.constants.Mno r2 = r4.mMno     // Catch:{ all -> 0x0016 }
            java.lang.String r2 = r2.getName()     // Catch:{ all -> 0x0016 }
            r1 = r2
        L_0x0013:
            monitor-exit(r0)     // Catch:{ all -> 0x0016 }
            monitor-exit(r4)
            return r1
        L_0x0016:
            r1 = move-exception
        L_0x0017:
            monitor-exit(r0)     // Catch:{ all -> 0x0019 }
            throw r1     // Catch:{ all -> 0x001b }
        L_0x0019:
            r1 = move-exception
            goto L_0x0017
        L_0x001b:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.getMnoName():java.lang.String");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0015, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.lang.String getMvnoName() {
        /*
            r4 = this;
            monitor-enter(r4)
            java.lang.Object r0 = LOCK     // Catch:{ all -> 0x0017 }
            monitor-enter(r0)     // Catch:{ all -> 0x0017 }
            java.lang.String r1 = ""
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r2 = r4.mLastSimState     // Catch:{ all -> 0x0012 }
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r3 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.UNKNOWN     // Catch:{ all -> 0x0012 }
            if (r2 == r3) goto L_0x000f
            java.lang.String r2 = r4.mMvnoName     // Catch:{ all -> 0x0012 }
            r1 = r2
        L_0x000f:
            monitor-exit(r0)     // Catch:{ all -> 0x0012 }
            monitor-exit(r4)
            return r1
        L_0x0012:
            r1 = move-exception
        L_0x0013:
            monitor-exit(r0)     // Catch:{ all -> 0x0015 }
            throw r1     // Catch:{ all -> 0x0017 }
        L_0x0015:
            r1 = move-exception
            goto L_0x0013
        L_0x0017:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.getMvnoName():java.lang.String");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0013, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean getHasSim() {
        /*
            r3 = this;
            monitor-enter(r3)
            java.lang.Object r0 = LOCK     // Catch:{ all -> 0x0015 }
            monitor-enter(r0)     // Catch:{ all -> 0x0015 }
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r1 = r3.mLastSimState     // Catch:{ all -> 0x0010 }
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r2 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.LOADED     // Catch:{ all -> 0x0010 }
            if (r1 != r2) goto L_0x000c
            r1 = 1
            goto L_0x000d
        L_0x000c:
            r1 = 0
        L_0x000d:
            monitor-exit(r0)     // Catch:{ all -> 0x0010 }
            monitor-exit(r3)
            return r1
        L_0x0010:
            r1 = move-exception
        L_0x0011:
            monitor-exit(r0)     // Catch:{ all -> 0x0013 }
            throw r1     // Catch:{ all -> 0x0015 }
        L_0x0013:
            r1 = move-exception
            goto L_0x0011
        L_0x0015:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.getHasSim():boolean");
    }

    public boolean restoreDefaultImsProfile() {
        this.mProfileCache.resetToDefault();
        return true;
    }

    public void updateGcfConfig(ContentValues values) {
        if (values != null && values.size() != 0) {
            Boolean isGcfEnabled = values.getAsBoolean("GCF_CONFIG_ENABLE");
            if (isGcfEnabled == null) {
                IMSLog.d(LOG_TAG, this.mPhoneId, "GCF_CONFIG_ENABLE is null");
            } else {
                DeviceUtil.setGcfMode(isGcfEnabled.booleanValue());
            }
        }
    }

    public void updateDnsBlock(ContentValues values) {
        if (values != null) {
            Boolean isDnsBlockEnabled = values.getAsBoolean("DNS_BLOCK_ENABLE");
            if (isDnsBlockEnabled != null) {
                SemSystemProperties.set("net.tether.always", isDnsBlockEnabled.booleanValue() ? "1" : "");
            } else {
                IMSLog.d(LOG_TAG, this.mPhoneId, "DNS_BLOCK_ENABLE is null");
            }
        }
    }

    public Cursor queryGcfConfig() {
        Boolean isGcfEnabled = Boolean.valueOf(DeviceUtil.getGcfMode());
        MatrixCursor c = new MatrixCursor(ImsSettings.ImsServiceSwitchTable.PROJECTION);
        c.addRow(new Object[]{"GCF_CONFIG_ENABLE", String.valueOf(isGcfEnabled)});
        return c;
    }

    public void setImsUserSetting(String name, int value) {
        if (!TextUtils.isEmpty(name)) {
            Mno mno = SimUtil.getSimMno(this.mPhoneId);
            if (name.startsWith(ImsConstants.SystemSettings.VOLTE_SLOT1.getName())) {
                this.mImsServiceSwitch.setVoiceCallType(mno.getName(), value);
            } else if (name.startsWith(ImsConstants.SystemSettings.VILTE_SLOT1.getName())) {
                this.mImsServiceSwitch.setVideoCallType(mno.getName(), value);
            } else if (name.startsWith(ImsConstants.SystemSettings.RCS_USER_SETTING1.getName())) {
                this.mImsServiceSwitch.setRcsUserSetting(value);
            }
        }
    }

    public void enableImsSwitch(String service, boolean enable) {
        if (!TextUtils.isEmpty(service)) {
            if ("volte".equalsIgnoreCase(service)) {
                this.mImsServiceSwitch.enableVoLte(enable);
            } else if (RCS.equalsIgnoreCase(service)) {
                this.mImsServiceSwitch.enableRcs(enable);
            } else {
                this.mImsServiceSwitch.enable(service, enable);
            }
        }
    }

    public void resetImsSwitch() {
        this.mImsServiceSwitch.doInit();
    }

    public Cursor queryImsUserSetting(String[] names) {
        MatrixCursor c = new MatrixCursor(ImsSettings.ImsUserSettingTable.PROJECTION);
        String simMno = SimUtil.getSimMno(this.mPhoneId).getName();
        if (names != null) {
            for (String name : names) {
                IMSLog.d(LOG_TAG, this.mPhoneId, "queryImsUserSetting: name " + name);
                if (ImsConstants.SystemSettings.VOLTE_SLOT1.getName().equalsIgnoreCase(name)) {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.getVoiceCallType(simMno))});
                } else if (ImsConstants.SystemSettings.VILTE_SLOT1.getName().equalsIgnoreCase(name)) {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.getVideoCallType(simMno))});
                } else if (ImsConstants.SystemSettings.RCS_USER_SETTING1.getName().equalsIgnoreCase(name) && SimUtil.getSimMno(this.mPhoneId) != Mno.DEFAULT) {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.getRcsUserSetting())});
                }
            }
        }
        return c;
    }

    public Cursor queryImsSwitch(String[] names) {
        MatrixCursor c = new MatrixCursor(ImsSettings.ImsServiceSwitchTable.PROJECTION);
        if (names != null) {
            for (String name : names) {
                IMSLog.d(LOG_TAG, this.mPhoneId, "queryImsSwitch: name " + name);
                if ("volte".equalsIgnoreCase(name)) {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.isVoLteEnabled() ? 1 : 0)});
                } else if (RCS_SWITCH.equalsIgnoreCase(name)) {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.isRcsSwitchEnabled() ? 1 : 0)});
                } else if (RCS.equalsIgnoreCase(name)) {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.isRcsEnabled() ? 1 : 0)});
                } else if (IMS.equalsIgnoreCase(name)) {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.isImsEnabled() ? 1 : 0)});
                } else if (DEFAULTMSGAPPINUSE.equalsIgnoreCase(name)) {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.isDefaultMessageAppInUse() ? 1 : 0)});
                } else {
                    c.addRow(new Object[]{name, Integer.valueOf(this.mImsServiceSwitch.isEnabled(name) ? 1 : 0)});
                }
            }
        }
        return c;
    }

    public void updateProvisioningProperty(ContentValues values) {
        Boolean status = values.getAsBoolean("status");
        Log.d("DeviceConfigManager[" + this.mPhoneId + "]", "updateProvisioningProperty : " + status);
        if (status == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "status is null.");
        } else if (status.booleanValue()) {
            ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
        } else {
            ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 0);
            VowifiConfig.setEnabled(this.mContext, 0, this.mPhoneId);
        }
    }

    public void updateWificallingProperty(ContentValues values) {
        Boolean status = values.getAsBoolean("status");
        if (status == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "status is null.");
        } else if (status.booleanValue()) {
            ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
            VowifiConfig.setEnabled(this.mContext, 1, this.mPhoneId);
        } else {
            VowifiConfig.setEnabled(this.mContext, 0, this.mPhoneId);
        }
    }

    public void updateGcfInitRat(ContentValues values) {
        if (values != null && values.size() != 0) {
            String gcfInitRat = values.getAsString("rat");
            if (TextUtils.isEmpty(gcfInitRat)) {
                Log.d(LOG_TAG, "updateGcfInitRat is empty");
                gcfInitRat = "";
            }
            SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "gcf_init_rat", 0, false).edit();
            editor.putString("rat", gcfInitRat);
            editor.apply();
        }
    }

    public void updateDtLocUserConsent(ContentValues values) {
        if (values != null && values.size() != 0) {
            int dtLocUserConsent = -1;
            if (values.getAsInteger("dtlocation") != null) {
                dtLocUserConsent = values.getAsInteger("dtlocation").intValue();
            }
            SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "dtlocuserconsent", 0, false).edit();
            editor.putInt("dtlocation", dtLocUserConsent);
            editor.apply();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public String getGcfInitRat() {
        Cursor cr;
        String gcfRat = "";
        try {
            cr = this.mContext.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/gcfinitrat"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (cr != null) {
                if (cr.moveToFirst()) {
                    gcfRat = cr.getString(cr.getColumnIndex("rat"));
                }
            }
            if (cr != null) {
                cr.close();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to get getGcfInitialRegistrationRat");
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return gcfRat;
        throw th;
    }

    private XmlPullParser getParser() {
        int id;
        if (CscParser.isPilotSetting(0)) {
            Log.d(LOG_TAG, "[pilot] getResources : dmconfigpilot.xml");
            id = this.mContext.getResources().getIdentifier("dmconfigpilot", "xml", this.mContext.getPackageName());
        } else {
            Log.d(LOG_TAG, "[commercial] getResources : dmconfig.xml");
            id = this.mContext.getResources().getIdentifier("dmconfig", "xml", this.mContext.getPackageName());
        }
        return this.mContext.getResources().getXml(id);
    }

    public ArrayList<String> getNvList(String mnoName) {
        XmlPullParser xpp = getParser();
        if (xpp == null) {
            Log.e(LOG_TAG, "can not find matched dmConfig.xml");
            return null;
        }
        try {
            XmlUtils.beginDocument(xpp, "configurations");
            for (int event = xpp.getEventType(); event != 1; event = xpp.next()) {
                if (event == 2 && "configuration".equalsIgnoreCase(xpp.getName())) {
                    if (matchConfigName(mnoName, xpp.getAttributeValue(0))) {
                        return parseNvList(xpp);
                    }
                    XmlUtils.skipCurrentTag(xpp);
                }
            }
            return new ArrayList<>();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<String> parseNvList(XmlPullParser parser) {
        ArrayList<String> carrierNvList = new ArrayList<>();
        while (true) {
            try {
                int next = parser.next();
                int event = next;
                if (next == 1) {
                    break;
                } else if (event != 2 || !ImsConstants.Intents.EXTRA_UPDATED_ITEM.equalsIgnoreCase(parser.getName())) {
                    if (event == 3 && "configuration".equalsIgnoreCase(parser.getName())) {
                        break;
                    }
                } else {
                    String item = parser.getAttributeValue(0);
                    String storageType = parser.getAttributeValue((String) null, "type");
                    if (!TextUtils.isEmpty(storageType) && TextUtils.equals(storageType.toUpperCase(), "NV")) {
                        carrierNvList.add(((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(Integer.parseInt(item))).getName().replace("./3GPP_IMS/", ""));
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }
        return carrierNvList;
    }

    private SparseArray<String> getDefaultDmConfig(String salesCode, ContentValues writtenNv, ArrayList<String> carrierNvList) {
        XmlPullParser xpp = getParser();
        if (xpp == null) {
            Log.e(LOG_TAG, "can not find matched dmConfig.xml");
            return null;
        }
        String name = TextUtils.isEmpty(salesCode) ? "default" : salesCode;
        SparseArray<String> initValues = new SparseArray<>();
        try {
            XmlUtils.beginDocument(xpp, "configurations");
            for (int event = xpp.getEventType(); event != 1; event = xpp.next()) {
                if (event == 2 && "configuration".equalsIgnoreCase(xpp.getName())) {
                    String dmConfigName = xpp.getAttributeValue(0);
                    if (matchConfigName("default", dmConfigName)) {
                        getConfig(xpp, initValues, writtenNv, carrierNvList);
                    } else if (matchConfigName(name, dmConfigName)) {
                        getConfig(xpp, initValues, writtenNv, carrierNvList);
                        return initValues;
                    } else {
                        XmlUtils.skipCurrentTag(xpp);
                    }
                }
            }
            return initValues;
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean matchConfigName(String name, String configName) {
        Log.d(LOG_TAG, "Configname : " + configName + " name : " + name);
        String[] configNames = configName.split(",");
        int length = configNames.length;
        for (int i = 0; i < length; i++) {
            if (configNames[i].equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of DeviceConfigManager:");
        this.mEventLog.dump();
        this.mProfileCache.dump();
        this.mSmsSetting.dump();
        NvStorage nvStorage = this.mNvStorage;
        if (nvStorage != null) {
            nvStorage.dump();
        }
        this.mImsServiceSwitch.dump();
        getGlobalSettingsRepo().dump();
    }
}
