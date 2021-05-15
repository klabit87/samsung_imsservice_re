package com.sec.internal.ims.cmstore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.utils.CloudMessagePreferenceConstants;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.DebugFlag;
import com.sec.internal.log.IMSLog;

public class CloudMessagePreferenceManager {
    private static final String ACS_HAS_NMS = "acs_has_nms";
    private static final String ACS_NMS_HOST = "acs_nms_host";
    private static final String APP_VER = "app_ver";
    private static final String ATS_TOKEN = "ats_token";
    private static final String AUTH_ZCODE = "auth_zcode";
    private static final String BUFFER_DB_LOADED = "buffer_db_loaded";
    private static final String COUNT_USER_INPUT_PHONE_NUMBER = "count_user_input_phone_number";
    private static final String DEVICE_ID = "device_id";
    private static final String HAS_SHOWN_POPUP_OPT_IN = "has_shown_popup_opt_in";
    private static final String HUI_6014_ERR = "6014_err";
    private static final String INITIAL_SYNC_STATUS = "initial sync status";
    private static final String IS_IMSI_FIXED_FOR_ATT_DATABASE = "is_imsi_fixed_for_att_database";
    private static final String IS_NATIVE_MSGAPP_DEFAULT = "is_native_message_app_default";
    private static final String LAST_API_CREATE_SERVICE = "last_api_create_service";
    private static final String LAST_SCREEN = "last_screen";
    private static final String LAST_SCREEN_USER_STOP_BACKUP = "last_screen_where_user_stop_backup";
    private static final String MSG_STORE_TOKEN = "msg_store_token";
    private static final String NC_HOST = "nc_host";
    private static final String NETWORK_OK_TIME = "network_is_available_time";
    private static final String NEW_USER_OPT_IN_CASE = "new_user_opt_in_case";
    private static final String NMS_HOST = "nms_host";
    private static final String NSDS_AUTHORY = "com.samsung.ims.nsds.provider";
    private static final String OBJECT_SEARCH_CURSOR = "object_search_cursor";
    private static final String OMA_CALLBACK_URL = "oma_callback_url";
    private static final String OMA_CHANNELS_CHANNEL_URL = "oma_channels_channel_url";
    private static final String OMA_CHANNELS_RESOURCE_URL = "oma_channels_resources_url";
    private static final String OMA_CHANNEL_CREATE_TIME = "oma_channel_create_lifetime";
    private static final String OMA_CHANNEL_LIFETIME = "oma_channel_lifetime";
    private static final String OMA_RETRY_COUNT = "oma_retry_count";
    private static final String OMA_SUBSCIRPTION_CHANNEL_DURATION = "oma_subscription_channel_duration";
    private static final String OMA_SUBSCIRPTION_RESTART_TOKEN = "oma_subscription_restart_token";
    private static final String OMA_SUBSCIRPTION_RESURL = "oma_subscription_resurl";
    private static final String OMA_SUBSCIRPTION_TIME = "oma_subscription_time";
    private static final String OMA_SUBSCRIPTION_INDEX = "oma_subscription_index";
    private static final String PAT = "cps_pat";
    private static final String PAT_GENERATE_TIME = "pat_generate_time";
    private static final String PREFERENCE_FILE_NAME = "cloudmessage";
    private static final String PREFERENCE_MIGRATE_SUCCESS = "cmsmigratesuccess";
    private static final String PREFERENCE_USER_DEBUG = "cmsuserdebug";
    private static final String PREF_KEY_RETRY_STACK = "Retry_Stack";
    private static final String REDIRECT_DOMAIN = "redirect_domain";
    private static final String SIM_IMSI = "sim_imsi";
    private static final String STEADY_STATE_FLAG = "steady_state_flag";
    private static final String TAG = CloudMessagePreferenceManager.class.getSimpleName();
    private static final String TBS_REQUIRED = "tbs_required";
    private static final String TERM_CONDITION_ID = "T&C";
    private static final String USER_CTN = "user_ctn_id";
    private static final String USER_CTN_IS_INPUT = "is_user_input_ctn";
    private static final String USER_DELETE_ACCOUNT = "user_requested_delete_account";
    private static final String USER_OPT_IN_FLAG = "user_opt_in_flag";
    private static final String ZCODE_COUNTER = "zcode_counter";
    private static final String ZCODE_LAST_REQUEST_ID = "zcode_last_request_id";
    private static boolean isInit = false;
    private static Context mContext;
    private static SharedPreferences mMigrateSuccessPreference;
    private static SharedPreferences mPreferences;
    private static SharedPreferences mUserDebugPreference;
    private static CloudMessagePreferenceManager sInstance = new CloudMessagePreferenceManager();

    private CloudMessagePreferenceManager() {
    }

    public static void init(Context context) {
        if (!isInit) {
            mContext = context;
            mPreferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
            mUserDebugPreference = mContext.getSharedPreferences(PREFERENCE_USER_DEBUG, 0);
            mMigrateSuccessPreference = mContext.getSharedPreferences(PREFERENCE_MIGRATE_SUCCESS, 0);
            initUserDebug();
            isInit = true;
        }
    }

    public static CloudMessagePreferenceManager getInstance() {
        return sInstance;
    }

    public void clearAll() {
        Log.d(TAG, "clear all preferences data");
        SharedPreferences.Editor editor = getPrefEditor();
        editor.clear();
        editor.apply();
    }

    private SharedPreferences.Editor getPrefEditor() {
        return mPreferences.edit();
    }

    private void saveKeyStringValue(String key, String value) {
        String str = TAG;
        IMSLog.s(str, "save key: " + key + ",value: " + value);
        SharedPreferences.Editor editor = getPrefEditor();
        editor.putString(key, value);
        editor.apply();
    }

    public void removeKey(String key) {
        String str = TAG;
        IMSLog.s(str, "remove key: " + key);
        SharedPreferences.Editor editor = getPrefEditor();
        editor.remove(key);
        editor.apply();
    }

    private String getKeyStringValue(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    private void saveKeyIntegerValue(String key, int value) {
        String str = TAG;
        IMSLog.s(str, "save key: " + key + ",value: " + value);
        SharedPreferences.Editor editor = getPrefEditor();
        editor.putInt(key, value);
        editor.apply();
    }

    private int getKeyIntegerValue(String key, int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }

    private void saveKeyBooleanValue(String key, boolean value) {
        String str = TAG;
        IMSLog.s(str, "save key: " + key + ",value: " + value);
        SharedPreferences.Editor editor = getPrefEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private boolean getKeyBooleanValue(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key, defaultValue);
    }

    private void saveKeyLongValue(String key, long value) {
        String str = TAG;
        IMSLog.s(str, "save key: " + key + ",value: " + value);
        SharedPreferences.Editor editor = getPrefEditor();
        editor.putLong(key, value);
        editor.apply();
    }

    private long getKeyLongValue(String key, long defaultValue) {
        return mPreferences.getLong(key, defaultValue);
    }

    public boolean isEmptyPref() {
        SharedPreferences sharedPreferences = mPreferences;
        return sharedPreferences == null || sharedPreferences.getAll().size() == 0;
    }

    public String getZCodeLastRequestId(String defaultValue) {
        return getKeyStringValue(ZCODE_LAST_REQUEST_ID, defaultValue);
    }

    public void saveZCodeLastRequestId(String lastRequestId) {
        saveKeyStringValue(ZCODE_LAST_REQUEST_ID, lastRequestId);
    }

    public void saveAppVer(String version) {
        saveKeyStringValue(APP_VER, version);
    }

    public String getTermConditionId() {
        return getKeyStringValue(TERM_CONDITION_ID, "");
    }

    public void saveTermConditionId(String tcId) {
        saveKeyStringValue(TERM_CONDITION_ID, tcId);
    }

    public void saveAuthZCode(String authZCode) {
        saveKeyStringValue(AUTH_ZCODE, authZCode);
    }

    public String getAuthZCode() {
        return getKeyStringValue(AUTH_ZCODE, "");
    }

    public void saveSimImsi(String simImsi) {
        if (!TextUtils.isEmpty(simImsi)) {
            saveKeyStringValue("sim_imsi", simImsi);
        }
    }

    public String getSimImsi() {
        return getKeyStringValue("sim_imsi", "");
    }

    public void saveUserTbsRquired(boolean isTbsRequired) {
        saveKeyBooleanValue(TBS_REQUIRED, isTbsRequired);
    }

    public boolean getUserTbs() {
        return getKeyBooleanValue(TBS_REQUIRED, false);
    }

    public void saveUserCtn(String userCtn, boolean isUserInputCtn) {
        if (!TextUtils.isEmpty(userCtn)) {
            saveKeyStringValue(USER_CTN, userCtn);
            saveKeyBooleanValue(USER_CTN_IS_INPUT, isUserInputCtn);
        }
    }

    public void clearInvalidUserCtn() {
        removeKey(USER_CTN);
        removeKey(USER_CTN_IS_INPUT);
    }

    public String getUserCtn() {
        return getKeyStringValue(USER_CTN, "");
    }

    public String getUserTelCtn() {
        ImsUri normalizedUri;
        String userCtn = getUserCtn();
        if (TextUtils.isEmpty(userCtn) || (normalizedUri = Util.getNormalizedTelUri(userCtn)) == null) {
            return "";
        }
        return normalizedUri.toString();
    }

    public boolean getIsUserInputCtn() {
        return getKeyBooleanValue(USER_CTN_IS_INPUT, false);
    }

    public void increaseUserInputNumberCount() {
        saveKeyIntegerValue(COUNT_USER_INPUT_PHONE_NUMBER, getKeyIntegerValue(COUNT_USER_INPUT_PHONE_NUMBER, 0) + 1);
    }

    public void removeUserInputNumberCount() {
        SharedPreferences.Editor editor = getPrefEditor();
        editor.remove(COUNT_USER_INPUT_PHONE_NUMBER);
        editor.apply();
    }

    public boolean isNoMoreChanceUserInputNumber() {
        if (getKeyIntegerValue(COUNT_USER_INPUT_PHONE_NUMBER, 0) >= 2) {
            return true;
        }
        return false;
    }

    public void increazeZCodeCounter() {
        saveKeyIntegerValue(ZCODE_COUNTER, getKeyIntegerValue(ZCODE_COUNTER, 0) + 1);
    }

    public boolean isZCodeMax2Tries() {
        return getKeyIntegerValue(ZCODE_COUNTER, 0) >= 1;
    }

    public void removeZCodeCounter() {
        SharedPreferences.Editor editor = getPrefEditor();
        editor.remove(ZCODE_COUNTER);
        editor.apply();
    }

    public void saveUserDeleteAccount(boolean state) {
        saveKeyBooleanValue(USER_DELETE_ACCOUNT, state);
    }

    public boolean hasUserDeleteAccount() {
        boolean result = getKeyBooleanValue(USER_DELETE_ACCOUNT, false);
        String str = TAG;
        Log.i(str, "hasUserDeleteAccount: " + result);
        return result;
    }

    public int getLastScreen() {
        return getKeyIntegerValue(LAST_SCREEN, -1);
    }

    public void saveLastScreen(int screenId) {
        saveKeyIntegerValue(LAST_SCREEN, screenId);
    }

    public int getLastScreenUserStopBackup() {
        return getKeyIntegerValue(LAST_SCREEN_USER_STOP_BACKUP, -1);
    }

    public void saveLastScreenUserStopBackup(int screenId) {
        saveKeyIntegerValue(LAST_SCREEN_USER_STOP_BACKUP, screenId);
    }

    public void saveAtsToken(String atsToken) {
        saveKeyStringValue(ATS_TOKEN, atsToken);
    }

    public String getAtsToken() {
        String atsToken = getKeyStringValue(ATS_TOKEN, "");
        String str = TAG;
        Log.i(str, "atsToken: " + atsToken);
        return atsToken;
    }

    public void saveMsgStoreSessionId(String atsToken) {
        saveKeyStringValue(MSG_STORE_TOKEN, atsToken);
    }

    public String getMsgStoreSessionId() {
        return getKeyStringValue(MSG_STORE_TOKEN, "");
    }

    public void saveNmsHost(String atsToken) {
        saveKeyStringValue(NMS_HOST, atsToken);
    }

    public String getNmsHost() {
        return getKeyStringValue(NMS_HOST, "");
    }

    public void saveAcsNmsHost(String atsToken) {
        saveKeyStringValue(ACS_NMS_HOST, atsToken);
    }

    public String getAcsNmsHost() {
        return getKeyStringValue(ACS_NMS_HOST, "");
    }

    public void saveRedirectDomain(String domain) {
        saveKeyStringValue(REDIRECT_DOMAIN, domain);
    }

    public String getRedirectDomain() {
        return getKeyStringValue(REDIRECT_DOMAIN, "");
    }

    public void savePATAndTime(String pat) {
        saveKeyStringValue(PAT, pat);
        saveKeyLongValue(PAT_GENERATE_TIME, System.currentTimeMillis());
    }

    public String getValidPAT() {
        String res = getKeyStringValue(PAT, (String) null);
        if (!TextUtils.isEmpty(res)) {
            if (System.currentTimeMillis() - getKeyLongValue(PAT_GENERATE_TIME, System.currentTimeMillis()) < ReqConstant.PAT_LIFE_CYCLE) {
                return res;
            }
        }
        return null;
    }

    public boolean hasUserOptedIn() {
        return getKeyBooleanValue(USER_OPT_IN_FLAG, false);
    }

    public void saveUserOptedIn(boolean state) {
        saveKeyBooleanValue(USER_OPT_IN_FLAG, state);
    }

    public boolean hasShownPopupOptIn() {
        return getKeyBooleanValue(HAS_SHOWN_POPUP_OPT_IN, false);
    }

    public void saveIfHasShownPopupOptIn(boolean state) {
        saveKeyBooleanValue(HAS_SHOWN_POPUP_OPT_IN, state);
    }

    public int getNewUserOptInCase() {
        return getKeyIntegerValue(NEW_USER_OPT_IN_CASE, EnumProvision.NewUserOptInCase.DEFAULT.getId());
    }

    public void saveNewUserOptInCase(int state) {
        saveKeyIntegerValue(NEW_USER_OPT_IN_CASE, state);
    }

    public boolean isLastAPIRequestCreateAccount() {
        return getKeyBooleanValue(LAST_API_CREATE_SERVICE, false);
    }

    public void saveLastApiRequestCreateAccount(boolean state) {
        saveKeyBooleanValue(LAST_API_CREATE_SERVICE, state);
    }

    public boolean isHUI6014Err() {
        return getKeyBooleanValue(HUI_6014_ERR, false);
    }

    public void saveIfHUI6014Err(boolean state) {
        saveKeyBooleanValue(HUI_6014_ERR, state);
    }

    public void saveIfSteadyState(boolean state) {
        saveKeyBooleanValue(STEADY_STATE_FLAG, state);
    }

    public boolean ifSteadyState() {
        return getKeyBooleanValue(STEADY_STATE_FLAG, false);
    }

    public void saveNativeMsgAppIsDefault(boolean value) {
        saveKeyBooleanValue(IS_NATIVE_MSGAPP_DEFAULT, value);
    }

    public int getTotalRetryCounter() {
        return getInstance().getKeyIntegerValue(CloudMessagePreferenceConstants.RETRY_TOTAL_COUNTER, 0);
    }

    public void saveTotalRetryCounter(int retryTimes) {
        saveKeyIntegerValue(CloudMessagePreferenceConstants.RETRY_TOTAL_COUNTER, retryTimes);
    }

    public void saveLastRetryTime(long retryTimes) {
        saveKeyLongValue(CloudMessagePreferenceConstants.LAST_RETRY_TIME, retryTimes);
    }

    public String getRetryStackData() {
        return getKeyStringValue(PREF_KEY_RETRY_STACK, "");
    }

    public void saveRetryStackData(String retryStackData) {
        saveKeyStringValue(PREF_KEY_RETRY_STACK, retryStackData);
    }

    public void saveInitialSyncStatus(int status) {
        saveKeyIntegerValue(INITIAL_SYNC_STATUS, status);
    }

    public void saveObjectSearchCursor(String cursor) {
        saveKeyStringValue(OBJECT_SEARCH_CURSOR, cursor);
    }

    public void saveNcHost(String ncHost) {
        saveKeyStringValue(NC_HOST, ncHost);
    }

    public String getNcHost() {
        return getKeyStringValue(NC_HOST, "");
    }

    public void saveOMAChannelResURL(String Url) {
        saveKeyStringValue(OMA_CHANNELS_RESOURCE_URL, Url);
    }

    public String getOMAChannelResURL() {
        return getKeyStringValue(OMA_CHANNELS_RESOURCE_URL, "");
    }

    public void saveOMAChannelURL(String Url) {
        saveKeyStringValue(OMA_CHANNELS_CHANNEL_URL, Url);
    }

    public String getOMAChannelURL() {
        return getKeyStringValue(OMA_CHANNELS_CHANNEL_URL, "");
    }

    public void saveOMACallBackURL(String Url) {
        saveKeyStringValue(OMA_CALLBACK_URL, Url);
    }

    public String getOMACallBackURL() {
        return getKeyStringValue(OMA_CALLBACK_URL, "");
    }

    public void saveOMASubscriptionIndex(long index) {
        saveKeyLongValue(OMA_SUBSCRIPTION_INDEX, index);
    }

    public long getOMASubscriptionIndex() {
        return getKeyLongValue(OMA_SUBSCRIPTION_INDEX, 0);
    }

    public void saveOMASubscriptionRestartToken(String token) {
        saveKeyStringValue(OMA_SUBSCIRPTION_RESTART_TOKEN, token);
    }

    public String getOMASSubscriptionRestartToken() {
        return getKeyStringValue(OMA_SUBSCIRPTION_RESTART_TOKEN, (String) null);
    }

    public void saveOMASubscriptionTime(long time) {
        saveKeyLongValue(OMA_SUBSCIRPTION_TIME, time);
    }

    public long getOMASubscriptionTime() {
        return getKeyLongValue(OMA_SUBSCIRPTION_TIME, 0);
    }

    public void clearOMASubscriptionTime() {
        removeKey(OMA_SUBSCIRPTION_TIME);
    }

    public void saveOMASubscriptionChannelDuration(int time) {
        saveKeyIntegerValue(OMA_SUBSCIRPTION_CHANNEL_DURATION, time);
    }

    public int getOMASubscriptionChannelDuration() {
        return getKeyIntegerValue(OMA_SUBSCIRPTION_CHANNEL_DURATION, 0);
    }

    public void clearOMASubscriptionChannelDuration() {
        removeKey(OMA_SUBSCIRPTION_CHANNEL_DURATION);
    }

    public void saveOMAChannelLifeTime(long time) {
        saveKeyLongValue(OMA_CHANNEL_LIFETIME, time);
    }

    public long getOMAChannelLifeTime() {
        return getKeyLongValue(OMA_CHANNEL_LIFETIME, 0);
    }

    public void saveOMAChannelCreateTime(long time) {
        saveKeyLongValue(OMA_CHANNEL_CREATE_TIME, time);
    }

    public long getOMAChannelCreateTime() {
        return getKeyLongValue(OMA_CHANNEL_CREATE_TIME, 0);
    }

    public void saveOMASubscriptionResUrl(String url) {
        saveKeyStringValue(OMA_SUBSCIRPTION_RESURL, url);
    }

    public String getOMASubscriptionResUrl() {
        return getKeyStringValue(OMA_SUBSCIRPTION_RESURL, "");
    }

    public int getOmaRetryCounter() {
        return getKeyIntegerValue(OMA_RETRY_COUNT, 0);
    }

    public void saveOmaRetryCounter(int retryCount) {
        saveKeyIntegerValue(OMA_RETRY_COUNT, retryCount);
    }

    public boolean getBufferDbLoaded() {
        return getKeyBooleanValue(BUFFER_DB_LOADED, false);
    }

    public void saveBufferDbLoaded(boolean isDBLoaded) {
        saveKeyBooleanValue(BUFFER_DB_LOADED, isDBLoaded);
    }

    public void saveDeviceId(String deviceId) {
        saveKeyStringValue("device_id", deviceId);
    }

    public String getDeviceId() {
        String deviceId = getKeyStringValue("device_id", "");
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = Util.getImei(mContext);
            if (TextUtils.isEmpty(deviceId)) {
                Log.d(TAG, "can't get imei from sp and telephonymgr");
                return "";
            }
            saveDeviceId(deviceId);
        }
        return deviceId;
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    public String getGcmTokenFromVsim() {
        Cursor cursor;
        String token = null;
        try {
            cursor = mContext.getContentResolver().query(Uri.parse("content://com.samsung.ims.nsds.provider/devices/push_token"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            while (cursor.moveToNext()) {
                token = cursor.getString(cursor.getColumnIndex("device_push_token"));
            }
            if (cursor != null) {
                cursor.close();
            }
            return token;
        } catch (SQLException | IllegalArgumentException e) {
            String str = TAG;
            Log.e(str, "!!!Could not get data from db " + e.toString());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void saveNetworkAvailableTime(long time) {
        new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        saveKeyLongValue(NETWORK_OK_TIME, time);
    }

    public long getNetworkAvailableTime() {
        return getKeyLongValue(NETWORK_OK_TIME, -1);
    }

    public void saveMigrateSuccessFlag(boolean isSuccess) {
        String str = TAG;
        Log.d(str, "saveMigrateSuccess is " + isSuccess);
        SharedPreferences sharedPreferences = mMigrateSuccessPreference;
        if (sharedPreferences != null) {
            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putBoolean(IS_IMSI_FIXED_FOR_ATT_DATABASE, isSuccess);
            ed.apply();
        }
    }

    public boolean getMigrateSuccessFlag() {
        Log.d(TAG, "getMigrateSuccessFlag ");
        SharedPreferences sharedPreferences = mMigrateSuccessPreference;
        if (sharedPreferences == null) {
            return false;
        }
        return sharedPreferences.getBoolean(IS_IMSI_FIXED_FOR_ATT_DATABASE, false);
    }

    public static void initUserDebug() {
        SharedPreferences sharedPreferences = mUserDebugPreference;
        if (sharedPreferences == null) {
            IMSLog.s(TAG, "mUserDebugPreference is null failed to init");
            return;
        }
        if (sharedPreferences.getBoolean(DebugFlag.DEBUG_FLAG, false)) {
            String appId = mUserDebugPreference.getString("app_id", "");
            String cpsHostName = mUserDebugPreference.getString(DebugFlag.CPS_HOST_NAME, "");
            String authHostName = mUserDebugPreference.getString(DebugFlag.AUTH_HOST_NAME, "");
            String timeLine = mUserDebugPreference.getString(DebugFlag.RETRY_TIME, "");
            ATTGlobalVariables.setValue(appId, authHostName, cpsHostName, mUserDebugPreference.getString(DebugFlag.NC_HOST_NAME, ""));
            DebugFlag.DEBUG_RETRY_TIMELINE_FLAG = true;
            DebugFlag.setRetryTimeLine(timeLine);
        } else {
            ATTGlobalVariables.initDefault();
            DebugFlag.DEBUG_RETRY_TIMELINE_FLAG = false;
            DebugFlag.initRetryTimeLine();
        }
        String str = TAG;
        IMSLog.s(str, "appId=" + ATTGlobalVariables.APP_ID + ", cpsHostName=" + ATTGlobalVariables.CPS_HOST_NAME + ", authHostName=" + ATTGlobalVariables.ACMS_HOST_NAME + ", ncHostName=" + ATTGlobalVariables.DEFAULT_PRODUCT_NC_HOST + "timeLine=" + DebugFlag.debugRetryTimeLine);
    }

    public static void saveUserDebug() {
        SharedPreferences sharedPreferences = mUserDebugPreference;
        if (sharedPreferences == null) {
            String str = TAG;
            IMSLog.s(str, "mUserDebugPreference is null failed to save, debug:" + DebugFlag.DEBUG_RETRY_TIMELINE_FLAG);
            return;
        }
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(DebugFlag.DEBUG_FLAG, DebugFlag.DEBUG_RETRY_TIMELINE_FLAG);
        ed.putString("app_id", ATTGlobalVariables.APP_ID);
        ed.putString(DebugFlag.CPS_HOST_NAME, ATTGlobalVariables.CPS_HOST_NAME);
        ed.putString(DebugFlag.AUTH_HOST_NAME, ATTGlobalVariables.ACMS_HOST_NAME);
        ed.putString(DebugFlag.RETRY_TIME, DebugFlag.debugRetryTimeLine);
        ed.putString(DebugFlag.NC_HOST_NAME, ATTGlobalVariables.DEFAULT_PRODUCT_NC_HOST);
        ed.apply();
    }
}
