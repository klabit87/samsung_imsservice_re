package com.sec.internal.ims.settings;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.SemSystemProperties;
import android.provider.BaseColumns;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonNull;
import com.samsung.android.cmcsetting.CmcSettingManager;
import com.samsung.android.cmcsetting.CmcSettingManagerConstants;
import com.samsung.android.feature.SemCarrierFeature;
import com.sec.ims.configuration.DATA;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.config.adapters.StorageAdapter;
import com.sec.internal.ims.config.workflow.WorkflowSec;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.WfcEpdgManager;
import com.sec.internal.ims.core.cmc.CmcAccountManager;
import com.sec.internal.ims.core.sim.MnoMap;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsProvider extends ContentProvider {
    private static final int ACS_INFO = 37;
    private static final String ACTION_CARRIER_CHANGED = "com.samsung.carrier.action.CARRIER_CHANGED";
    private static final int CONFIG_DB_RESET = 28;
    private static final int CSC_PROFILE = 7;
    private static final int CSC_PROFILE_ID = 8;
    private static final int CSC_SETTING = 9;
    private static final int CSC_SETTING_ID = 10;
    private static final int DEBUG_CONFIG = 21;
    private static final int DM_ACCESS = 24;
    private static final String DM_CONFIG_URI = "com.samsung.rcs.dmconfigurationprovider";
    private static final int DNS_BLOCK = 20;
    private static final int DOWNLOAD_CONFIG = 29;
    private static final int DT_LOC_USER_CONSENT = 40;
    private static final int EPDG_SYSTEM_SETTINGS = 39;
    private static final String EXTRA_CARRIER_PHONEID = "com.samsung.carrier.extra.CARRIER_PHONE_ID";
    private static final String EXTRA_CARRIER_STATE = "com.samsung.carrier.extra.CARRIER_STATE";
    private static final int GCF_CONFIG_NAME = 19;
    private static final int GCF_INIT_RAT = 35;
    private static final int IMPU = 17;
    private static final int IMS_GLOBAL = 4;
    private static final int IMS_GLOBAL_ID = 5;
    private static final int IMS_GLOBAL_RESET = 6;
    private static final int IMS_PROFILE = 1;
    private static final int IMS_PROFILE_ID = 2;
    private static final int IMS_PROFILE_RESET = 3;
    private static final int IMS_SMK_SECRET_KEY = 33;
    private static final int IMS_SWITCH = 11;
    private static final int IMS_SWITCH_NAME = 13;
    private static final int IMS_SWITCH_RESET = 12;
    private static final int IMS_USER_SETTING = 36;
    private static final String LOG_DELETE = "Delete";
    private static final String LOG_INSERT = "Insert";
    private static final String LOG_QUERY = "Query";
    public static final String LOG_TAG = "ImsSettingsProvider";
    private static final String LOG_UPDATE = "Update";
    private static final int MNO = 23;
    private static final int NV_LIST = 26;
    private static final int NV_STORAGE = 15;
    private static final int PROFILE_MATCHER = 0;
    private static final int RCS_VER = 31;
    private static final String RCS_VERSION = "6.0.3";
    private static final int READ_ALL_OMADM = 25;
    private static final int RESET_DOWNLOAD_CONFIG = 30;
    private static final int SELF_PROVISIONING = 18;
    private static final int SELF_WIFICALLINGACTIVATION = 22;
    private static final int SIM_DATA = 14;
    private static final int SIM_MOBILITY = 32;
    private static final IntentFilter SIM_STATE_CHANGED_INTENT_FILTER;
    private static final int SMK_UPDATED_INFO = 34;
    private static final int SMS_SETTING = 38;
    private static final int USER_CONFIG = 16;
    private static final String mSecretKey = "3C061A6726A7E3CAF9634D43D93CAC61";
    private static final UriMatcher sUriMatcher;
    private final BroadcastReceiver mCarrierFeatureReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SemSystemProperties.getBoolean("mdc.sys.enable_smff", false)) {
                String action = intent.getAction();
                String carrierState = intent.getStringExtra(SettingsProvider.EXTRA_CARRIER_STATE);
                int phoneId = intent.getIntExtra(SettingsProvider.EXTRA_CARRIER_PHONEID, 0);
                int currentCarrierId = SemCarrierFeature.getInstance().getCarrierId(phoneId, false);
                Log.d(SettingsProvider.LOG_TAG, "intent : action : " + action + " phoneId : " + phoneId + " , extra : " + carrierState);
                WfcEpdgManager wfcEpdgMgr = ImsServiceStub.getInstance().getWfcEpdgManager();
                if (SettingsProvider.ACTION_CARRIER_CHANGED.equals(action)) {
                    ImsAutoUpdate autoUpdate = ImsAutoUpdate.getInstance(SettingsProvider.this.mContext, phoneId);
                    if ("UPDATED".equals(carrierState)) {
                        SettingsProvider.this.doCarrierFeatureUpdate(autoUpdate, phoneId, currentCarrierId);
                        if (wfcEpdgMgr != null) {
                            wfcEpdgMgr.onCarrierUpdate(intent);
                        }
                    } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(carrierState) && currentCarrierId != -1) {
                        int savedCarrierId = SettingsProvider.this.getSavedCarrierId(phoneId);
                        String savedSwVer = SettingsProvider.this.getSavedSwVersion(phoneId);
                        String curSwVer = Build.VERSION.INCREMENTAL;
                        Log.d(SettingsProvider.LOG_TAG, "saved CarrierId : " + savedCarrierId + " Current Carrier Id : " + currentCarrierId + " / saved Sw Ver : " + savedSwVer + " current Sw Ver : " + curSwVer);
                        if (savedCarrierId != currentCarrierId || !curSwVer.equals(savedSwVer)) {
                            SettingsProvider.this.doCarrierFeatureUpdate(autoUpdate, phoneId, currentCarrierId);
                            if (wfcEpdgMgr != null) {
                                wfcEpdgMgr.onCarrierUpdate(intent);
                            }
                        }
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext = null;
    private Map<Integer, DeviceConfigManager> mDeviceConfigManager = new ConcurrentHashMap();
    private SimpleEventLog mEventLog;
    private TelephonyManager mTelephonyManager;

    public static final class ImpuRecordTable implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.sec.ims.settings/impu");
        public static final String IMPU = "impu";
        public static final String IMSI = "imsi";
        public static final String TABLE_NAME = "impu";
        public static final String TIMESTAMP = "timestamp";
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "match_profile_id", 0);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "profile", 1);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "profile/#", 2);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "profile/reset", 3);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "global", 4);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "global/#", 5);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "global/reset", 6);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "config/reset", 28);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "cscprofile", 7);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "cscprofile/#", 8);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "cscsetting", 9);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "cscsetting/#", 10);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "imsswitch", 11);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "imsswitch/*", 13);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "imsswitchreset", 12);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "simdata", 14);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "nvstorage/*", 15);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "userconfig", 16);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "impu", 17);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "selfprovisioning", 18);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "gcfconfig", 19);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "dnsblock", 20);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "debugconfig/#", 21);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "selfwificallingactivation", 22);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "mno", 23);
        sUriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", "omadm/./3GPP_IMS/*", 24);
        sUriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", NvStorage.ID_OMADM, 24);
        sUriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", "presence", 24);
        sUriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", "*", 24);
        sUriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", (String) null, 24);
        sUriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", "omadm/*", 25);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "nvlist", 26);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "ims_info/rcs_ver", 31);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "secretkey", 33);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "downloadconfig", 29);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "resetconfig", 30);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "smkupdatedinfo", 34);
        IntentFilter intentFilter = new IntentFilter();
        SIM_STATE_CHANGED_INTENT_FILTER = intentFilter;
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        SIM_STATE_CHANGED_INTENT_FILTER.addAction(ImsConstants.Intents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "simmobility", 32);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "gcfinitrat", 35);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "imsusersetting", 36);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "acsinfo", 37);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "sms_setting", 38);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "epdgsettings", 39);
        sUriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "dtlocuserconsent", 40);
    }

    public boolean onCreate() {
        this.mContext = getContext();
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 500);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        for (int i = 0; i < this.mTelephonyManager.getPhoneCount(); i++) {
            this.mDeviceConfigManager.put(Integer.valueOf(i), new DeviceConfigManager(this.mContext, i));
        }
        if (this.mTelephonyManager.getPhoneCount() == 0 && isCmcSecondaryDevice()) {
            this.mDeviceConfigManager.put(Integer.valueOf(ImsConstants.Phone.SLOT_1), new DeviceConfigManager(this.mContext, ImsConstants.Phone.SLOT_1));
            Log.d(LOG_TAG, "CMC supported no NOSIM model : DeviceConfigManager");
        }
        this.mContext.registerReceiver(this.mCarrierFeatureReceiver, new IntentFilter(ACTION_CARRIER_CHANGED));
        return true;
    }

    /* access modifiers changed from: private */
    public void doCarrierFeatureUpdate(ImsAutoUpdate autoUpdate, int phoneId, int currentCarrierId) {
        boolean isMnoMapUpdated = false;
        if (autoUpdate.loadCarrierFeature(phoneId)) {
            isMnoMapUpdated = (autoUpdate.getMnomap(4, ImsAutoUpdate.TAG_MNOMAP_REMOVE) == JsonNull.INSTANCE && autoUpdate.getMnomap(4, ImsAutoUpdate.TAG_MNOMAP_ADD) == JsonNull.INSTANCE) ? false : true;
            resetStoredConfig(isMnoMapUpdated);
            if (isMnoMapUpdated) {
                saveUpdatedCarrierId(phoneId, currentCarrierId, isMnoMapUpdated);
                this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/mnomap_updated", phoneId), (ContentObserver) null);
                return;
            }
            this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/carrier_feature_updated", phoneId), (ContentObserver) null);
        }
        saveUpdatedCarrierId(phoneId, currentCarrierId, isMnoMapUpdated);
    }

    /* access modifiers changed from: private */
    public int getSavedCarrierId(int phoneId) {
        return ImsSharedPrefHelper.getInt(phoneId, this.mContext, ImsSharedPrefHelper.CARRIER_ID, ImsSharedPrefHelper.CARRIER_ID, -1);
    }

    /* access modifiers changed from: private */
    public String getSavedSwVersion(int phoneId) {
        return ImsSharedPrefHelper.getString(phoneId, this.mContext, ImsSharedPrefHelper.CARRIER_ID, "swversion", "");
    }

    private void saveUpdatedCarrierId(int phoneId, int carrierId, boolean isMnoUpdated) {
        ImsSharedPrefHelper.save(phoneId, this.mContext, ImsSharedPrefHelper.CARRIER_ID, ImsSharedPrefHelper.CARRIER_ID, carrierId);
        ImsSharedPrefHelper.save(phoneId, this.mContext, ImsSharedPrefHelper.CARRIER_ID, "swversion", Build.VERSION.INCREMENTAL);
        ImsSharedPrefHelper.save(phoneId, this.mContext, ImsSharedPrefHelper.CARRIER_ID, "needMnoUpdate", isMnoUpdated);
    }

    /* access modifiers changed from: package-private */
    public DeviceConfigManager getDeviceConfigManager(int phoneId) {
        DeviceConfigManager res = this.mDeviceConfigManager.get(Integer.valueOf(phoneId));
        if (res == null) {
            IMSLog.d(LOG_TAG, phoneId, "getDeviceConfigManager: Not exist.");
        }
        return res;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arg0) throws OperationApplicationException {
        return super.applyBatch(arg0);
    }

    private ArrayList<String> getAllServiceSwitches() {
        ArrayList<String> ret = new ArrayList<>();
        ret.add("volte");
        ret.add(DeviceConfigManager.RCS);
        ret.add(DeviceConfigManager.IMS);
        ret.add(ImsConstants.SystemSettings.VILTE_SLOT1.getName());
        ret.add(ImsConstants.SystemSettings.VOLTE_SLOT1.getName());
        ret.add(DeviceConfigManager.DEFAULTMSGAPPINUSE);
        ret.add("mmtel");
        ret.add("mmtel-video");
        ret.add("mmtel-call-composer");
        ret.add("smsip");
        ret.add("ss");
        ret.add("cdpn");
        ret.add("options");
        ret.add("presence");
        ret.add("im");
        ret.add("ft");
        ret.add("ft_http");
        ret.add("slm");
        ret.add("lastseen");
        ret.add("is");
        ret.add("vs");
        ret.add("euc");
        ret.add("gls");
        ret.add("profile");
        ret.add("ec");
        ret.add("cab");
        ret.add("cms");
        ret.add(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
        return ret;
    }

    private Cursor getSavedImpu(String imsi) {
        String impuString = ImsSharedPrefHelper.getString(-1, this.mContext, ImsSharedPrefHelper.SAVED_IMPU, imsi, "");
        MatrixCursor c = new MatrixCursor(new String[]{"impu"});
        c.addRow(new Object[]{impuString});
        return c;
    }

    private int setSavedImpu(ContentValues values) {
        ImsSharedPrefHelper.save(-1, this.mContext, ImsSharedPrefHelper.SAVED_IMPU, values.getAsString("imsi"), values.getAsString("impu"));
        return 1;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri uriWoFragment;
        String uriInfo;
        List<ImsProfile> profiles;
        String[] projection2;
        Uri uri2 = uri;
        String[] strArr = projection;
        String str = selection;
        String[] strArr2 = selectionArgs;
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        DeviceConfigManager dcm = getDeviceConfigManager(phoneId);
        if (dcm == null) {
            return null;
        }
        String uriInfo2 = "Uri[" + uri2 + "]";
        Uri uriWoFragment2 = uri;
        String frag = "#" + uri.getFragment();
        if (!CollectionUtils.isNullOrEmpty(frag)) {
            uriWoFragment = Uri.parse(uri.toString().replace(frag, ""));
        } else {
            uriWoFragment = uriWoFragment2;
        }
        if (str != null) {
            uriInfo2 = uriInfo2 + ", sel : " + str;
        }
        if (strArr != null) {
            uriInfo = uriInfo2 + ", pro : " + Arrays.toString(projection);
        } else {
            uriInfo = uriInfo2;
        }
        if (!IMSLog.isShipBuild()) {
            dumpBinderInfo(LOG_QUERY, false, uriInfo, false);
        }
        int match = sUriMatcher.match(uriWoFragment);
        if (match == 1) {
            MatrixCursor cursor = new MatrixCursor(new String[]{"profile"});
            if (!TextUtils.isEmpty(selection)) {
                String token = str.substring(str.indexOf(AuthenticationHeaders.HEADER_PRARAM_SPERATOR) + 1);
                IMSLog.d(LOG_TAG, phoneId, "ImsProfile query with  " + token);
                if (str.startsWith("mdmn_type")) {
                    profiles = dcm.getProfileCache().getProfileListByMdmnType(token);
                } else if (str.startsWith("salescode")) {
                    profiles = new ArrayList<>();
                } else if (str.startsWith(MnoMap.Param.MCCMNC)) {
                    profiles = new ArrayList<>();
                } else if (str.startsWith("mnoname")) {
                    profiles = dcm.getProfileCache().getProfileListByMnoName(token, dcm.getGlobalSettingsRepo().getGlobalGcEnabled());
                } else {
                    profiles = new ArrayList<>();
                }
            } else {
                profiles = dcm.getProfileCache().getAllProfileList();
            }
            for (ImsProfile p : profiles) {
                cursor.newRow().add("profile", p.toJson());
            }
            return cursor;
        } else if (match == 2) {
            String str2 = uriInfo;
            MatrixCursor cursor2 = new MatrixCursor(new String[]{"profile"});
            ImsProfile profile = dcm.getProfileCache().getProfile(Integer.parseInt(uriWoFragment.getLastPathSegment()));
            if (profile != null) {
                cursor2.newRow().add("profile", profile.toJson());
            }
            return cursor2;
        } else if (match == 4) {
            return dcm.getGlobalSettingsRepo().query(strArr, str, strArr2);
        } else {
            if (match == 11 || match == 13) {
                String str3 = uriInfo;
                if (strArr == null) {
                    projection2 = (String[]) getAllServiceSwitches().toArray(new String[0]);
                } else {
                    projection2 = strArr;
                }
                return dcm.queryImsSwitch(projection2);
            } else if (match == 19) {
                return dcm.queryGcfConfig();
            } else {
                if (match == 21) {
                    return dcm.getDebugConfigStorage().query(phoneId, strArr);
                }
                if (match != 40) {
                    switch (match) {
                        case 15:
                            break;
                        case 16:
                            return dcm.getUserConfigStorage().query(strArr);
                        case 17:
                            return getSavedImpu(strArr2[0]);
                        default:
                            switch (match) {
                                case 23:
                                    MatrixCursor cursor3 = new MatrixCursor(new String[]{"mnoname", ISimManager.KEY_MVNO_NAME, ISimManager.KEY_HAS_SIM});
                                    cursor3.newRow().add("mnoname", dcm.getMnoName()).add(ISimManager.KEY_MVNO_NAME, dcm.getMvnoName()).add(ISimManager.KEY_HAS_SIM, Boolean.valueOf(dcm.getHasSim()));
                                    return cursor3;
                                case 24:
                                    break;
                                case 25:
                                    if (!uriWoFragment.getLastPathSegment().equals("*")) {
                                        break;
                                    } else {
                                        String str4 = uriInfo;
                                        return dcm.queryDm(uriWoFragment, projection, selection, selectionArgs, true);
                                    }
                                case 26:
                                    MatrixCursor cursor4 = new MatrixCursor(new String[]{"NVLIST"});
                                    if (dcm.getNvList() != null && !dcm.getNvList().isEmpty()) {
                                        cursor4.addRow(new String[]{Arrays.toString(dcm.getNvList().toArray())});
                                    }
                                    return cursor4;
                                default:
                                    switch (match) {
                                        case 31:
                                            MatrixCursor cur = new MatrixCursor(new String[]{"rcs_ver"});
                                            cur.newRow().add(RCS_VERSION);
                                            return cur;
                                        case 32:
                                            MatrixCursor cur2 = new MatrixCursor(new String[]{"simmobility"});
                                            cur2.newRow().add(Integer.valueOf(SlotBasedConfig.getInstance(phoneId).isSimMobilityActivated() ? 1 : 0));
                                            return cur2;
                                        case 33:
                                            MatrixCursor cur3 = new MatrixCursor(new String[]{"secretkey"});
                                            cur3.newRow().add(mSecretKey);
                                            return cur3;
                                        case 34:
                                            MatrixCursor cur4 = new MatrixCursor(new String[]{"smkupdatedinfo"});
                                            String updatedInfo = null;
                                            try {
                                                updatedInfo = ImsAutoUpdate.getInstance(this.mContext, phoneId).getSmkConfig().toString();
                                            } catch (Exception e) {
                                                Log.d(LOG_TAG, "failed to get SMK Updated Information : " + e);
                                            }
                                            if (updatedInfo != null && updatedInfo.length() > 0) {
                                                Log.d(LOG_TAG, "updated info return, query to ImsSettings");
                                                cur4.newRow().add(updatedInfo);
                                                return cur4;
                                            }
                                        case 35:
                                            break;
                                        case 36:
                                            return dcm.queryImsUserSetting(strArr);
                                        case 37:
                                            int result = 0;
                                            MatrixCursor acsCur = null;
                                            if ("acsversion".equals(str)) {
                                                acsCur = new MatrixCursor(new String[]{"acsversion"});
                                                result = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId("version", phoneId), -1).intValue();
                                            } else if ("acscount".equals(str)) {
                                                acsCur = new MatrixCursor(new String[]{"acscount"});
                                                result = WorkflowSec.getAcsTriggerCount();
                                            }
                                            if (acsCur != null) {
                                                acsCur.newRow().add(Integer.valueOf(result));
                                            }
                                            return acsCur;
                                        case 38:
                                            return dcm.getSmsSetting().getAsCursor();
                                        default:
                                            throw new IllegalArgumentException("Unknown URI " + uri2);
                                    }
                                    String gcfInitRat = ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "gcf_init_rat", 0, false).getString("rat", "");
                                    MatrixCursor cur5 = new MatrixCursor(new String[]{"rat"});
                                    cur5.newRow().add(gcfInitRat);
                                    return cur5;
                            }
                            break;
                    }
                    return dcm.queryDm(uriWoFragment, projection, selection, selectionArgs, false);
                }
                int dtLocUserConsent = ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "dtlocuserconsent", 0, false).getInt("dtlocation", -1);
                MatrixCursor cur6 = new MatrixCursor(new String[]{"dtlocation"});
                cur6.newRow().add(Integer.valueOf(dtLocUserConsent));
                return cur6;
            }
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        DeviceConfigManager dcm = getDeviceConfigManager(phoneId);
        if (dcm == null || values == null) {
            return null;
        }
        dumpBinderInfo(LOG_INSERT, true, "Uri[" + uri + "], value : " + values.toString(), true);
        Uri uriWoFragment = uri;
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        sb.append(uri.getFragment());
        String frag = sb.toString();
        if (!CollectionUtils.isNullOrEmpty(frag)) {
            uriWoFragment = Uri.parse(uri.toString().replace(frag, ""));
        }
        long id = 0;
        int match = sUriMatcher.match(uriWoFragment);
        if (match == 1) {
            id = (long) dcm.getProfileCache().insert(new ImsProfile(values));
        } else if (match != 21) {
            if (match != 24) {
                if (match != 39) {
                    switch (match) {
                        case 15:
                            break;
                        case 16:
                            dcm.getUserConfigStorage().insert(values);
                            break;
                        case 17:
                            setSavedImpu(values);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown URI " + uri);
                    }
                } else {
                    updateEpdgSystemSettings(values);
                }
            }
            sendData(phoneId, values);
            dcm.insertDm(uriWoFragment, values);
        } else {
            dcm.getDebugConfigStorage().insert(phoneId, values);
        }
        getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
        return Uri.withAppendedPath(uri, Long.toString(id));
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        DeviceConfigManager dcm = getDeviceConfigManager(phoneId);
        if (dcm == null) {
            return 0;
        }
        dumpBinderInfo(LOG_DELETE, true, uri.toString(), true);
        Uri uriWoFragment = uri;
        String frag = "#" + uri.getFragment();
        if (!CollectionUtils.isNullOrEmpty(frag)) {
            uriWoFragment = Uri.parse(uri.toString().replace(frag, ""));
        }
        int match = sUriMatcher.match(uriWoFragment);
        if (match == 2) {
            dcm.getProfileCache().remove(Integer.valueOf(uriWoFragment.getLastPathSegment()).intValue());
            return 0;
        } else if (match == 15 || match == 24) {
            return dcm.deleteDm(uriWoFragment);
        } else {
            if (match == 30) {
                ImsAutoUpdate.getInstance(this.mContext, phoneId).clearSmkConfig();
                resetStoredConfig(false);
                getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
                return 0;
            }
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Uri uri2 = uri;
        ContentValues contentValues = values;
        boolean notifyChange = true;
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        DeviceConfigManager dcm = getDeviceConfigManager(phoneId);
        boolean isMnoMapUpdated = false;
        if (dcm == null) {
            return 0;
        }
        dumpBinderInfo(LOG_UPDATE, true, "Uri[" + uri2 + "], value : " + values.toString(), true);
        Uri uriWoFragment = uri;
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        sb.append(uri.getFragment());
        String frag = sb.toString();
        if (!CollectionUtils.isNullOrEmpty(frag)) {
            uriWoFragment = Uri.parse(uri.toString().replace(frag, ""));
        }
        int count = 0;
        int match = sUriMatcher.match(uriWoFragment);
        if (match == 15) {
            Log.d(LOG_TAG, "update: not supported in NV_STORAGE. use insert");
        } else if (match == 40) {
            dcm.updateDtLocUserConsent(contentValues);
        } else if (match == 28) {
            new StorageAdapter().forceDeleteALL(this.mContext);
        } else if (match == 29) {
            IMSLog.c(LogClass.SMK_UPDATE, phoneId + ",SMK UPDATE");
            ImsAutoUpdate autoUpdate = ImsAutoUpdate.getInstance(this.mContext, phoneId);
            autoUpdate.updateSmkConfig(contentValues.getAsString(ImsConstants.DOWNLOAD_CONFIG));
            if (!(autoUpdate.getMnomap(0, ImsAutoUpdate.TAG_MNOMAP_REMOVE) == JsonNull.INSTANCE && autoUpdate.getMnomap(0, ImsAutoUpdate.TAG_MNOMAP_ADD) == JsonNull.INSTANCE)) {
                isMnoMapUpdated = true;
            }
            resetStoredConfig(isMnoMapUpdated);
            if (isMnoMapUpdated) {
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.sec.ims.settings/mnomap_updated"), (ContentObserver) null);
                notifyChange = false;
            }
        } else if (match == 35) {
            dcm.updateGcfInitRat(contentValues);
        } else if (match != 36) {
            switch (match) {
                case 1:
                    IMSLog.e(LOG_TAG, phoneId, "update: Bulk edit not supported.");
                    return 0;
                case 2:
                    int id = Integer.valueOf(uriWoFragment.getLastPathSegment()).intValue();
                    Mno mno = Mno.fromName(dcm.getGlobalSettingsRepo().getPreviousMno());
                    int badEventExpiry = dcm.getProfileCache().getProfile(id).getBadEventExpiry();
                    int extendedPublishTimer = dcm.getProfileCache().getProfile(id).getExtendedPublishTimer();
                    count = dcm.getProfileCache().update(id, contentValues);
                    if (mno == Mno.ATT && !(badEventExpiry == dcm.getProfileCache().getProfile(id).getBadEventExpiry() && extendedPublishTimer == dcm.getProfileCache().getProfile(id).getExtendedPublishTimer())) {
                        IMSLog.d(LOG_TAG, phoneId, "update : badEventExpiry or badEventExpiry for ATT");
                        return count;
                    }
                case 3:
                    dcm.restoreDefaultImsProfile();
                    break;
                case 4:
                case 5:
                    dcm.getGlobalSettingsRepo().update(contentValues);
                    break;
                case 6:
                    IMSLog.d(LOG_TAG, phoneId, "update: reset.");
                    dcm.getGlobalSettingsRepo().reset();
                    dcm.getGlobalSettingsRepo().load();
                    break;
                default:
                    switch (match) {
                        case 11:
                        case 13:
                            String service = contentValues.getAsString("service");
                            boolean enabled = contentValues.getAsBoolean("enabled").booleanValue();
                            if ("ipme".equalsIgnoreCase(service)) {
                                notifyChange = false;
                            }
                            dcm.enableImsSwitch(service, enabled);
                            break;
                        case 12:
                            dcm.resetImsSwitch();
                            break;
                        default:
                            switch (match) {
                                case 18:
                                    dcm.updateProvisioningProperty(contentValues);
                                    break;
                                case 19:
                                    dcm.updateGcfConfig(contentValues);
                                    break;
                                case 20:
                                    dcm.updateDnsBlock(contentValues);
                                    break;
                                default:
                                    switch (match) {
                                        case 22:
                                            dcm.updateWificallingProperty(contentValues);
                                            break;
                                        case 23:
                                            dcm.updateMno(contentValues);
                                            break;
                                        case 24:
                                            sendData(phoneId, contentValues);
                                            dcm.updateDm(uriWoFragment, contentValues);
                                            break;
                                        default:
                                            throw new IllegalArgumentException("Unknown URI " + uri2);
                                    }
                            }
                    }
            }
        } else {
            dcm.setImsUserSetting(contentValues.getAsString("name"), contentValues.getAsInteger(ImsConstants.Intents.EXTRA_UPDATED_VALUE).intValue());
        }
        if (notifyChange) {
            Log.d(LOG_TAG, "notifyChange uri [" + uri2 + "]");
            getContext().getContentResolver().notifyChange(uri2, (ContentObserver) null);
        }
        return count;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if ("dump".equals(method)) {
            dump((FileDescriptor) null, (PrintWriter) null, (String[]) null);
        }
        return null;
    }

    private void resetStoredConfig(boolean isMnoMapUpdate) {
        if (isMnoMapUpdate) {
            for (int i = 0; i < this.mDeviceConfigManager.size(); i++) {
                SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(i, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false);
                if (sp != null) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("mnoname", "default");
                    editor.apply();
                }
            }
        }
        for (int phoneId = 0; phoneId < this.mDeviceConfigManager.size(); phoneId++) {
            Optional.ofNullable(getDeviceConfigManager(phoneId)).ifPresent($$Lambda$SettingsProvider$dto_6VMgBRlj66nRZ_9Q1tnQHlI.INSTANCE);
            RcsPolicyManager.loadRcsSettings(phoneId, true);
            this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), phoneId), (ContentObserver) null);
            this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(ImsConstants.Uris.SMS_SETTING.toString(), phoneId), (ContentObserver) null);
        }
    }

    static /* synthetic */ void lambda$resetStoredConfig$0(DeviceConfigManager dcm) {
        dcm.restoreDefaultImsProfile();
        dcm.getGlobalSettingsRepo().reset();
        dcm.getGlobalSettingsRepo().loadByDynamicConfig();
        dcm.getSmsSetting().init();
    }

    private void dumpBinderInfo(String action, boolean useEventLog, String additionalInfo, boolean showCaller) {
        String logs;
        int pid = Binder.getCallingPid();
        String logs2 = action + "(" + pid;
        if (showCaller) {
            String processName = PackageUtils.getProcessNameById(getContext(), pid);
            logs = logs2 + ", " + processName.substring(processName.lastIndexOf(".") + 1) + ") : ";
        } else {
            logs = logs2 + ") : ";
        }
        if (IMSLog.isShipBuild()) {
            if (additionalInfo.contains("impu") || additionalInfo.contains("imsi")) {
                additionalInfo = additionalInfo.replaceAll("\\d", "x");
            } else {
                additionalInfo = additionalInfo.replaceAll("\\d++@", "xxx@");
            }
        }
        String logs3 = logs + additionalInfo;
        if (useEventLog) {
            this.mEventLog.logAndAdd(logs3);
        } else {
            Log.d(LOG_TAG, logs3);
        }
    }

    private void sendData(int phoneId, ContentValues cv) {
        String processName;
        int i = phoneId;
        ContentValues contentValues = cv;
        if (!CollectionUtils.isNullOrEmpty(cv)) {
            ContentValues data = new ContentValues();
            StringBuilder itemInfo = new StringBuilder();
            if (TextUtils.isEmpty(contentValues.getAsString(DmConfigModule.INTERNAL_KEY_PROCESS_NAME))) {
                processName = PackageUtils.getProcessNameById(getContext(), Binder.getCallingPid());
            } else {
                processName = contentValues.getAsString(DmConfigModule.INTERNAL_KEY_PROCESS_NAME);
            }
            contentValues.remove(DmConfigModule.INTERNAL_KEY_PROCESS_NAME);
            IMSLog.c(LogClass.OMADM_UPDATER_AND_SIZE, i + ",UPD:" + processName + "," + cv.size());
            for (String key : cv.keySet()) {
                if (!key.contains(DeviceConfigManager.NV_INIT_DONE)) {
                    int idxOfKey = -1;
                    Iterator it = DATA.DM_FIELD_LIST.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        DATA.DM_FIELD_INFO field = (DATA.DM_FIELD_INFO) it.next();
                        if (field.getPathName().contains(key)) {
                            idxOfKey = field.getIndex();
                            break;
                        }
                    }
                    String value = contentValues.getAsString(key);
                    if (idxOfKey != -1 || !TextUtils.isEmpty(value)) {
                        if (idxOfKey >= 0) {
                            itemInfo.append(idxOfKey + ":");
                        } else {
                            Log.e(LOG_TAG, "xNode item: " + key);
                            itemInfo.append("X:");
                        }
                        if (!TextUtils.isEmpty(value)) {
                            if (value.contains(":") || value.contains(".")) {
                                IMSLog.s(LOG_TAG, "Replace sensitive data: " + value);
                                value = "HIDE";
                            }
                            itemInfo.append(value);
                        }
                        itemInfo.append("^");
                        IMSLog.c(LogClass.OMADM_UPDATED_ITEM, i + "," + value + "," + getShortenKeyForXNode(key));
                        contentValues = cv;
                    } else {
                        Log.e(LOG_TAG, "Ignore: " + key + ": [" + value + "]");
                    }
                }
            }
            if (!TextUtils.isEmpty(itemInfo)) {
                itemInfo.deleteCharAt(itemInfo.length() - 1);
                itemInfo.insert(0, cv.size() + "^");
                data.put(DiagnosisConstants.DMUI_KEY_SETTING_TYPE, itemInfo.toString());
                data.put(DiagnosisConstants.DMUI_KEY_CALLER_INFO, processName);
                IMSLog.s(LOG_TAG, "sendData : " + data);
                ImsLogAgentUtil.sendLogToAgent(i, this.mContext, DiagnosisConstants.FEATURE_DMUI, data);
                ContentValues dailyReport = new ContentValues();
                dailyReport.put(DiagnosisConstants.KEY_SEND_MODE, 1);
                dailyReport.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
                dailyReport.put(DiagnosisConstants.DRPT_KEY_OMADM_UPDATE_COUNT, 1);
                ImsLogAgentUtil.storeLogToAgent(i, this.mContext, "DRPT", dailyReport);
            }
        }
    }

    private String getShortenKeyForXNode(String xNodeKey) {
        String[] shortenKey = xNodeKey.replaceFirst("\\./3GPP_IMS/", "").split("/");
        StringBuilder rtn = new StringBuilder();
        int len = shortenKey.length;
        if (len >= 2) {
            rtn.append(shortenKey[len - 2]);
            rtn.append("/");
            rtn.append(shortenKey[len - 1]);
        } else if (len > 0) {
            rtn.append(shortenKey[0]);
        }
        return rtn.toString();
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mEventLog.dump();
        this.mDeviceConfigManager.values().forEach($$Lambda$LsiG4Tuwsh8ilkgtfIEqgb3hFVY.INSTANCE);
    }

    private boolean isCmcSecondaryDevice() {
        CmcSettingManager cmcSettingMgr = new CmcSettingManager();
        cmcSettingMgr.init(this.mContext);
        CmcSettingManagerConstants.DeviceType type = cmcSettingMgr.getOwnDeviceType();
        cmcSettingMgr.deInit();
        IMSLog.d(LOG_TAG, "onCreate: isCmcSecondaryDevice: api: " + type);
        if (type == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD) {
            return true;
        }
        if (type == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            return false;
        }
        String deviceType_prop = SemSystemProperties.get(CmcAccountManager.CMC_DEVICE_TYPE_PROP, "");
        if (TextUtils.isEmpty(deviceType_prop)) {
            return false;
        }
        IMSLog.d(LOG_TAG, "onCreate: isCmcSecondaryDevice: prop: " + deviceType_prop);
        if ("sd".equalsIgnoreCase(deviceType_prop)) {
            return true;
        }
        return false;
    }

    private void updateEpdgSystemSettings(ContentValues values) {
        for (Map.Entry<String, ?> e : values.valueSet()) {
            String key = e.getKey();
            int lastChar = key.matches(".+[1-9]$") ? Integer.parseInt(key.substring(key.length() - 1)) : -1;
            int val = -1;
            Object value = e.getValue();
            if (value instanceof Integer) {
                val = ((Integer) value).intValue();
            } else if (value instanceof String) {
                try {
                    val = Integer.parseInt((String) value);
                } catch (NumberFormatException e2) {
                }
            }
            if (lastChar < 0 || val < 0) {
                Log.e(LOG_TAG, "updateEpdgSystemSettings: Skip wrong input [" + key + ": " + ((String) Optional.ofNullable(value).map($$Lambda$JsVbJ5mpbRjwJuW_A3bDJMqYpF0.INSTANCE).orElse("null!")) + "] => lastChar [" + lastChar + "], val [" + val + "]");
            } else if (key.replace(VowifiConfig.WIFI_CALL_ENABLE, "").length() == 1) {
                ImsConstants.SystemSettings.setWiFiCallEnabled(this.mContext, lastChar - 1, val);
            } else if (key.replace(VowifiConfig.WIFI_CALL_PREFERRED, "").length() == 1) {
                ImsConstants.SystemSettings.setWiFiCallPreferred(this.mContext, lastChar - 1, val);
            } else if (key.replace(VowifiConfig.WIFI_CALL_WHEN_ROAMING, "").length() == 1) {
                ImsConstants.SystemSettings.setWiFiCallWhenRoaming(this.mContext, lastChar - 1, val);
            }
        }
    }
}
