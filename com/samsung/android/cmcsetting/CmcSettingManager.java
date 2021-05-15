package com.samsung.android.cmcsetting;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.cmcnsd.CmcNsdManager;
import com.samsung.android.cmcsetting.CmcSettingManagerConstants;
import com.samsung.android.cmcsetting.listeners.CmcActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcCallActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcDeviceInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcLineInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcMessageActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcNetworkModeInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcSameWifiNetworkStatusListener;
import com.samsung.android.cmcsetting.listeners.CmcSamsungAccountInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcWatchActivationInfoChangedListener;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.core.cmc.CmcAccountManager;
import java.util.ArrayList;
import java.util.Iterator;

public class CmcSettingManager {
    private static final String AUTHORITY = "com.samsung.android.mdec.provider.setting";
    private static final String DEVICE_CATEGORY_BT_WATCH = "BT-Watch";
    private static final String DEVICE_CATEGORY_LAPTOP = "Laptop";
    private static final String DEVICE_CATEGORY_PC = "PC";
    private static final String DEVICE_CATEGORY_PHONE = "Phone";
    private static final String DEVICE_CATEGORY_SPEAKER = "Speaker";
    private static final String DEVICE_CATEGORY_TABLET = "Tablet";
    private static final String DEVICE_CATEGORY_TV = "TV";
    private static final String DEVICE_CATEGORY_UNDEFINED = "Undefined";
    private static final String DEVICE_TYPE_PD = "pd";
    private static final String DEVICE_TYPE_SD = "sd";
    private static final String LOG_TAG = "CmcSettingManager";
    private static final String METHOD_GET_ACTIVATIONS = "get_activations";
    private static final String METHOD_GET_CALL_ALLOWED_SD_BY_PD = "get_call_allowed_sd_by_pd";
    private static final String METHOD_GET_CMC_ACTIVATION = "get_cmc_activation";
    private static final String METHOD_GET_CMC_SUPPORTED = "get_cmc_supported";
    private static final String METHOD_GET_DEVICE_CATEGORY = "get_device_category";
    private static final String METHOD_GET_DEVICE_ID_LIST = "get_device_id_list";
    private static final String METHOD_GET_DEVICE_INFO = "get_device_info";
    private static final String METHOD_GET_DEVICE_NAME = "get_device_name";
    private static final String METHOD_GET_DEVICE_TYPE = "get_device_type";
    private static final String METHOD_GET_FRE = "get_fre";
    private static final String METHOD_GET_LINE_ACTIVE_SIM_SLOT = "get_line_active_sim_slot";
    private static final String METHOD_GET_LINE_ID = "get_line_id";
    private static final String METHOD_GET_LINE_IMPU = "get_line_impu";
    private static final String METHOD_GET_LINE_INFO = "get_line_info";
    private static final String METHOD_GET_LINE_MSISDN = "get_line_msisdn";
    private static final String METHOD_GET_LINE_NMS_ADDR_LIST = "get_line_nms_addr_list";
    private static final String METHOD_GET_LINE_PCSCF_ADDR_LIST = "get_line_pcscf_addr_list";
    private static final String METHOD_GET_MESSAGE_ALLOWED_SD_BY_PD = "get_message_allowed_sd_by_pd";
    private static final String METHOD_GET_OWN_ACTIVATION_TIME = "get_own_activation_time";
    private static final String METHOD_GET_OWN_CALL_ACTIVATION_TIME = "get_own_call_activation_time";
    private static final String METHOD_GET_OWN_DEVICE_ID = "get_own_device_id";
    private static final String METHOD_GET_OWN_DEVICE_NAME = "get_own_device_name";
    private static final String METHOD_GET_OWN_DEVICE_TYPE = "get_own_device_type";
    private static final String METHOD_GET_OWN_MESSAGE_ACTIVATION_TIME = "get_own_message_activation_time";
    private static final String METHOD_GET_OWN_NETWORK_MODE = "get_own_network_mode";
    private static final String METHOD_GET_OWN_SERVICE_VERSION = "get_own_service_version";
    private static final String METHOD_GET_PD_DEVICE_NAME = "get_pd_device_name";
    private static final String METHOD_GET_SA_INFO = "get_sa_info";
    private static final String METHOD_GET_WATCH_ACTIVATION = "get_watch_activation";
    private static final String METHOD_GET_WATCH_REGISTERED = "get_watch_registered";
    private static final String METHOD_GET_WATCH_SUPPORTED = "get_watch_supported";
    private static final String METHOD_OPEN_CMC_SETTING_MENU = "open_cmc_setting_menu";
    private static final String METHOD_VERSION = "v1";
    private static final int NETWORK_MODE_USE_MOBILE_NETWORK = 0;
    private static final int NETWORK_MODE_WIFI_ONLY = 1;
    private static final String PARAM_AUTO_ACTIVATION = "auto_activation";
    private static final String PARAM_BT_MAC_ID = "bt_mac_address";
    private static final String PARAM_DEVICE_ID = "device_id";
    private static final int RESULT_OK = 1;
    private static final String RET_ACCESS_TOKEN = "access_token";
    private static final String RET_ACTIVE_SIM_SLOT = "active_sim_slot";
    private static final String RET_CMC_ACTIVATION = "cmc_activation";
    private static final String RET_CMC_SUPPORTED = "cmc_supported";
    private static final String RET_DEVICE_ACTIVATION = "activation";
    private static final String RET_DEVICE_CALL_ACTIVATION = "call_activation";
    private static final String RET_DEVICE_CALL_ALLOWED_SD_BY_PD = "call_allowed_sd_by_pd";
    private static final String RET_DEVICE_CATEGORY = "device_category";
    private static final String RET_DEVICE_ID_LIST = "device_id_list";
    private static final String RET_DEVICE_MESSAGE_ACTIVATION = "message_activation";
    private static final String RET_DEVICE_MESSAGE_ALLOWED_SD_BY_PD = "message_allowed_sd_by_pd";
    private static final String RET_DEVICE_NAME = "device_name";
    private static final String RET_DEVICE_TYPE = "device_type";
    private static final String RET_ERROR_REASON = "error_reason";
    private static final String RET_FRE = "Fre";
    private static final String RET_IMPU = "impu";
    private static final String RET_LINE_ID = "line_id";
    private static final String RET_MSISDN = "msisdn";
    private static final String RET_NETWORK_MODE = "network_mode";
    private static final String RET_NMS_ADDR_LIST = "nms_addr_list";
    private static final String RET_OWN_ACTIVATION_TIME = "own_activation_time";
    private static final String RET_OWN_CALL_ACTIVATION_TIME = "own_call_activation_time";
    private static final String RET_OWN_DEVICE_ID = "own_device_id";
    private static final String RET_OWN_DEVICE_NAME = "own_device_name";
    private static final String RET_OWN_DEVICE_TYPE = "own_device_type";
    private static final String RET_OWN_MESSAGE_ACTIVATION_TIME = "own_message_activation_time";
    private static final String RET_OWN_SERVICE_VERSION = "own_service_version";
    private static final String RET_PCSCF_ADDR_LIST = "pcscf_addr_list";
    private static final String RET_PD_DEVICE_NAME = "pd_device_name";
    private static final String RET_RESULT = "result";
    private static final String RET_SAMSUNG_USER_ID = "samsung_user_id";
    private static final String RET_WATCH_ACTIVATION = "watch_activation";
    private static final String RET_WATCH_REGISTER = "watch_register";
    private static final String RET_WATCH_SUPPORTED = "watch_supported";
    private static boolean SHIP_BUILD = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
    private Uri authorityUri = Uri.parse("content://com.samsung.android.mdec.provider.setting");
    private Uri authorityUriForCmcActivation = Uri.parse("content://com.samsung.android.mdec.provider.setting/cmc_activation");
    private Uri authorityUriForCmcCallActivation = Uri.parse("content://com.samsung.android.mdec.provider.setting/cmc_call_activation");
    private Uri authorityUriForCmcMessageActivation = Uri.parse("content://com.samsung.android.mdec.provider.setting/cmc_message_activation");
    private Uri authorityUriForDevices = Uri.parse("content://com.samsung.android.mdec.provider.setting/devices");
    private Uri authorityUriForLines = Uri.parse("content://com.samsung.android.mdec.provider.setting/lines");
    private Uri authorityUriForNetworkMode = Uri.parse("content://com.samsung.android.mdec.provider.setting/network_mode");
    private Uri authorityUriForSaInfo = Uri.parse("content://com.samsung.android.mdec.provider.setting/sainfo");
    private Uri authorityUriForSameWifiNetworkStatus = Uri.parse("content://com.samsung.android.mdec.provider.setting/same_wifi_network_status");
    private Uri authorityUriForWatchActivation = Uri.parse("content://com.samsung.android.mdec.provider.setting/watch_activation");
    /* access modifiers changed from: private */
    public ArrayList<CmcActivationInfoChangedListener> mCmcActivationChangedListenerList = null;
    private ContentObserver mCmcActivationDbChangeObserver = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcCallActivationInfoChangedListener> mCmcCallActivationChangedListenerList = null;
    private ContentObserver mCmcCallActivationDbChangeObserver = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcDeviceInfoChangedListener> mCmcDeviceInfoChangedListenerList = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcLineInfoChangedListener> mCmcLineInfoChangedListenerList = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcMessageActivationInfoChangedListener> mCmcMessageActivationChangedListenerList = null;
    private ContentObserver mCmcMessageActivationDbChangeObserver = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcNetworkModeInfoChangedListener> mCmcNetworkModeChangedListenerList = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcSamsungAccountInfoChangedListener> mCmcSamsungAccountInfoChangedListenerList = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcWatchActivationInfoChangedListener> mCmcWatchActivationChangedListenerList = null;
    private Context mContext = null;
    private ContentObserver mDevicesDbChangeObserver = null;
    private ContentObserver mLinesDbChangeObserver = null;
    private ContentObserver mNetworkModeDbChangeObserver = null;
    private ContentObserver mSaInfoDbChangeObserver = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcSameWifiNetworkStatusListener> mSameWifiNetworkStatusListenerList = null;
    private ContentObserver mSameWifiNetworkStatusObserver = null;
    private ContentObserver mWatchActivationDbChangeObserver = null;

    private enum OBSERVER_TYPE {
        mainCctivation,
        messageActivation,
        callActivation,
        watchActivation,
        networkMode,
        lineInfo,
        deviceInfo,
        saInfo,
        sameWifiNetworkStatus,
        all
    }

    public boolean init(Context context) {
        Log.d(LOG_TAG, "init : CmcSettingManager version : 1.3.3");
        Log.d(LOG_TAG, "context : " + context);
        if (context != null) {
            String string = Settings.Global.getString(context.getContentResolver(), "cmc_package_name");
            if (TextUtils.isEmpty(string)) {
                string = CmcNsdManager.SERVICE_PACKAGE;
            }
            if (isSupportVersion(context, string)) {
                this.mContext = context;
                return true;
            }
            Log.e(LOG_TAG, "valid package is not exist");
        } else {
            Log.e(LOG_TAG, "context is null");
        }
        return false;
    }

    public void deInit() {
        Log.d(LOG_TAG, "deInit");
        unregisterListener();
        this.mContext = null;
    }

    public boolean registerListener(CmcActivationInfoChangedListener cmcActivationInfoChangedListener) {
        Log.d(LOG_TAG, "registerListener : CmcActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcActivationInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mCmcActivationChangedListenerList == null) {
                this.mCmcActivationChangedListenerList = new ArrayList<>();
            }
            this.mCmcActivationChangedListenerList.add(cmcActivationInfoChangedListener);
            registerObserver(OBSERVER_TYPE.mainCctivation);
            return true;
        }
    }

    public boolean unregisterListener(CmcActivationInfoChangedListener cmcActivationInfoChangedListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcActivationInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcActivationInfoChangedListener> arrayList = this.mCmcActivationChangedListenerList;
        if (arrayList == null || !arrayList.remove(cmcActivationInfoChangedListener) || this.mCmcActivationChangedListenerList.size() != 0) {
            return true;
        }
        this.mCmcActivationChangedListenerList = null;
        unregisterObserver(OBSERVER_TYPE.mainCctivation);
        return true;
    }

    public boolean registerListener(CmcMessageActivationInfoChangedListener cmcMessageActivationInfoChangedListener) {
        Log.d(LOG_TAG, "registerListener : CmcMessageActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcMessageActivationInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mCmcMessageActivationChangedListenerList == null) {
                this.mCmcMessageActivationChangedListenerList = new ArrayList<>();
            }
            this.mCmcMessageActivationChangedListenerList.add(cmcMessageActivationInfoChangedListener);
            registerObserver(OBSERVER_TYPE.messageActivation);
            return true;
        }
    }

    public boolean unregisterListener(CmcMessageActivationInfoChangedListener cmcMessageActivationInfoChangedListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcMessageActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcMessageActivationInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcMessageActivationInfoChangedListener> arrayList = this.mCmcMessageActivationChangedListenerList;
        if (arrayList == null || !arrayList.remove(cmcMessageActivationInfoChangedListener) || this.mCmcMessageActivationChangedListenerList.size() != 0) {
            return true;
        }
        this.mCmcMessageActivationChangedListenerList = null;
        unregisterObserver(OBSERVER_TYPE.messageActivation);
        return true;
    }

    public boolean registerListener(CmcCallActivationInfoChangedListener cmcCallActivationInfoChangedListener) {
        Log.d(LOG_TAG, "registerListener : CmcCallActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcCallActivationInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mCmcCallActivationChangedListenerList == null) {
                this.mCmcCallActivationChangedListenerList = new ArrayList<>();
            }
            this.mCmcCallActivationChangedListenerList.add(cmcCallActivationInfoChangedListener);
            registerObserver(OBSERVER_TYPE.callActivation);
            return true;
        }
    }

    public boolean unregisterListener(CmcCallActivationInfoChangedListener cmcCallActivationInfoChangedListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcCallActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcCallActivationInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcCallActivationInfoChangedListener> arrayList = this.mCmcCallActivationChangedListenerList;
        if (arrayList == null || !arrayList.remove(cmcCallActivationInfoChangedListener) || this.mCmcCallActivationChangedListenerList.size() != 0) {
            return true;
        }
        this.mCmcCallActivationChangedListenerList = null;
        unregisterObserver(OBSERVER_TYPE.callActivation);
        return true;
    }

    public boolean registerListener(CmcWatchActivationInfoChangedListener cmcWatchActivationInfoChangedListener) {
        Log.d(LOG_TAG, "registerListener : CmcWatchActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcWatchActivationInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mCmcWatchActivationChangedListenerList == null) {
                this.mCmcWatchActivationChangedListenerList = new ArrayList<>();
            }
            this.mCmcWatchActivationChangedListenerList.add(cmcWatchActivationInfoChangedListener);
            registerObserver(OBSERVER_TYPE.watchActivation);
            return true;
        }
    }

    public boolean unregisterListener(CmcWatchActivationInfoChangedListener cmcWatchActivationInfoChangedListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcWatchActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcWatchActivationInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcWatchActivationInfoChangedListener> arrayList = this.mCmcWatchActivationChangedListenerList;
        if (arrayList == null || !arrayList.remove(cmcWatchActivationInfoChangedListener) || this.mCmcWatchActivationChangedListenerList.size() != 0) {
            return true;
        }
        this.mCmcWatchActivationChangedListenerList = null;
        unregisterObserver(OBSERVER_TYPE.watchActivation);
        return true;
    }

    public boolean registerListener(CmcNetworkModeInfoChangedListener cmcNetworkModeInfoChangedListener) {
        Log.d(LOG_TAG, "registerListener : CmcNetworkModeInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcNetworkModeInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mCmcNetworkModeChangedListenerList == null) {
                this.mCmcNetworkModeChangedListenerList = new ArrayList<>();
            }
            this.mCmcNetworkModeChangedListenerList.add(cmcNetworkModeInfoChangedListener);
            registerObserver(OBSERVER_TYPE.networkMode);
            return true;
        }
    }

    public boolean unregisterListener(CmcNetworkModeInfoChangedListener cmcNetworkModeInfoChangedListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcNetworkModeInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcNetworkModeInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcNetworkModeInfoChangedListener> arrayList = this.mCmcNetworkModeChangedListenerList;
        if (arrayList == null || !arrayList.remove(cmcNetworkModeInfoChangedListener) || this.mCmcNetworkModeChangedListenerList.size() != 0) {
            return true;
        }
        this.mCmcNetworkModeChangedListenerList = null;
        unregisterObserver(OBSERVER_TYPE.networkMode);
        return true;
    }

    public boolean registerListener(CmcLineInfoChangedListener cmcLineInfoChangedListener) {
        Log.d(LOG_TAG, "registerListener : CmcLineInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcLineInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mCmcLineInfoChangedListenerList == null) {
                this.mCmcLineInfoChangedListenerList = new ArrayList<>();
            }
            this.mCmcLineInfoChangedListenerList.add(cmcLineInfoChangedListener);
            registerObserver(OBSERVER_TYPE.lineInfo);
            return true;
        }
    }

    public boolean unregisterListener(CmcLineInfoChangedListener cmcLineInfoChangedListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcLineInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcLineInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcLineInfoChangedListener> arrayList = this.mCmcLineInfoChangedListenerList;
        if (arrayList == null || !arrayList.remove(cmcLineInfoChangedListener) || this.mCmcLineInfoChangedListenerList.size() != 0) {
            return true;
        }
        this.mCmcLineInfoChangedListenerList = null;
        unregisterObserver(OBSERVER_TYPE.lineInfo);
        return true;
    }

    public boolean registerListener(CmcDeviceInfoChangedListener cmcDeviceInfoChangedListener) {
        Log.d(LOG_TAG, "registerListener : CmcDeviceInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcDeviceInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mCmcDeviceInfoChangedListenerList == null) {
                this.mCmcDeviceInfoChangedListenerList = new ArrayList<>();
            }
            this.mCmcDeviceInfoChangedListenerList.add(cmcDeviceInfoChangedListener);
            registerObserver(OBSERVER_TYPE.deviceInfo);
            return true;
        }
    }

    public boolean unregisterListener(CmcDeviceInfoChangedListener cmcDeviceInfoChangedListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcDeviceInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcDeviceInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcDeviceInfoChangedListener> arrayList = this.mCmcDeviceInfoChangedListenerList;
        if (arrayList == null || !arrayList.remove(cmcDeviceInfoChangedListener) || this.mCmcDeviceInfoChangedListenerList.size() != 0) {
            return true;
        }
        this.mCmcDeviceInfoChangedListenerList = null;
        unregisterObserver(OBSERVER_TYPE.deviceInfo);
        return true;
    }

    public boolean registerListener(CmcSamsungAccountInfoChangedListener cmcSamsungAccountInfoChangedListener) {
        Log.d(LOG_TAG, "registerListener : CmcSamsungAccountInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcSamsungAccountInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mCmcSamsungAccountInfoChangedListenerList == null) {
                this.mCmcSamsungAccountInfoChangedListenerList = new ArrayList<>();
            }
            this.mCmcSamsungAccountInfoChangedListenerList.add(cmcSamsungAccountInfoChangedListener);
            registerObserver(OBSERVER_TYPE.saInfo);
            return true;
        }
    }

    public boolean unregisterListener(CmcSamsungAccountInfoChangedListener cmcSamsungAccountInfoChangedListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcSamsungAccountInfoChangedListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcSamsungAccountInfoChangedListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcSamsungAccountInfoChangedListener> arrayList = this.mCmcSamsungAccountInfoChangedListenerList;
        if (arrayList == null || !arrayList.remove(cmcSamsungAccountInfoChangedListener) || this.mCmcSamsungAccountInfoChangedListenerList.size() != 0) {
            return true;
        }
        this.mCmcSamsungAccountInfoChangedListenerList = null;
        unregisterObserver(OBSERVER_TYPE.saInfo);
        return true;
    }

    public boolean registerListener(CmcSameWifiNetworkStatusListener cmcSameWifiNetworkStatusListener) {
        Log.d(LOG_TAG, "registerListener : CmcSameWifiNetworkStatusListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        } else if (cmcSameWifiNetworkStatusListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        } else {
            if (this.mSameWifiNetworkStatusListenerList == null) {
                this.mSameWifiNetworkStatusListenerList = new ArrayList<>();
            }
            this.mSameWifiNetworkStatusListenerList.add(cmcSameWifiNetworkStatusListener);
            registerObserver(OBSERVER_TYPE.sameWifiNetworkStatus);
            return true;
        }
    }

    public boolean unregisterListener(CmcSameWifiNetworkStatusListener cmcSameWifiNetworkStatusListener) {
        Log.d(LOG_TAG, "unregisterListener : CmcSameWifiNetworkStatusListener");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        if (cmcSameWifiNetworkStatusListener == null) {
            Log.e(LOG_TAG, "listener is null");
            return false;
        }
        ArrayList<CmcSameWifiNetworkStatusListener> arrayList = this.mSameWifiNetworkStatusListenerList;
        if (arrayList == null || !arrayList.remove(cmcSameWifiNetworkStatusListener) || this.mSameWifiNetworkStatusListenerList.size() != 0) {
            return true;
        }
        this.mSameWifiNetworkStatusListenerList = null;
        unregisterObserver(OBSERVER_TYPE.sameWifiNetworkStatus);
        return true;
    }

    public boolean unregisterListener() {
        Log.d(LOG_TAG, "unregisterListener : all");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Log.d(LOG_TAG, "context : " + this.mContext);
        this.mCmcActivationChangedListenerList = null;
        this.mCmcMessageActivationChangedListenerList = null;
        this.mCmcCallActivationChangedListenerList = null;
        this.mCmcWatchActivationChangedListenerList = null;
        this.mCmcNetworkModeChangedListenerList = null;
        this.mCmcLineInfoChangedListenerList = null;
        this.mCmcDeviceInfoChangedListenerList = null;
        this.mCmcSamsungAccountInfoChangedListenerList = null;
        this.mSameWifiNetworkStatusListenerList = null;
        unregisterObserver(OBSERVER_TYPE.all);
        return true;
    }

    public boolean openCmcSetting(boolean z) {
        Log.d(LOG_TAG, "openCmcSetting : " + z);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(PARAM_AUTO_ACTIVATION, z);
        try {
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/open_cmc_setting_menu", (String) null, bundle);
            if (call == null) {
                return false;
            }
            int i = call.getInt(RET_RESULT, -1);
            if (i == 1) {
                Log.d(LOG_TAG, "call inf : openCmcSetting success");
            } else {
                Log.e(LOG_TAG, "call inf : openCmcSetting fail : " + call.getString("error_reason"));
            }
            if (i == 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public boolean getCmcSupported() {
        Log.d(LOG_TAG, "getCmcSupported");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_cmc_supported", (String) null, (Bundle) null);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                boolean z = call.getBoolean(RET_CMC_SUPPORTED, false);
                Log.d(LOG_TAG, "call inf : getCmcSupported success : " + z);
                return z;
            }
            Log.e(LOG_TAG, "call inf : getCmcSupported fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public boolean getWatchSupported() {
        Log.d(LOG_TAG, "getWatchSupported");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_watch_supported", (String) null, (Bundle) null);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                boolean z = call.getBoolean(RET_WATCH_SUPPORTED, false);
                Log.d(LOG_TAG, "call inf : getWatchSupported success : " + z);
                return z;
            }
            Log.e(LOG_TAG, "call inf : getWatchSupported fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public boolean getFre() {
        Log.d(LOG_TAG, "getFre");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_fre", (String) null, (Bundle) null);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                boolean z = call.getBoolean(RET_FRE, false);
                Log.d(LOG_TAG, "call inf : getFre success : " + z);
                return z;
            }
            Log.e(LOG_TAG, "call inf : getFre fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public boolean getOwnCmcActivation() {
        Log.d(LOG_TAG, "getOwnCmcActivation");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        int i = Settings.Global.getInt(context.getContentResolver(), RET_CMC_ACTIVATION, 0);
        Log.d(LOG_TAG, "cmc activation : " + i);
        if (i == 1) {
            return true;
        }
        return false;
    }

    public boolean getOwnCmcMessageActivation() {
        Log.d(LOG_TAG, "getOwnCmcMessageActivation");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        if (DEVICE_TYPE_PD.equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "cmc_device_type"))) {
            int i = Settings.Global.getInt(this.mContext.getContentResolver(), RET_CMC_ACTIVATION, 0);
            Log.d(LOG_TAG, "pd : cmc activation : " + i);
            if (i != 1) {
                return false;
            }
        } else {
            int i2 = Settings.Global.getInt(this.mContext.getContentResolver(), "cmc_message_activation", 0);
            Log.d(LOG_TAG, "sd : cmc message activation : " + i2);
            if (i2 != 1) {
                return false;
            }
        }
        return true;
    }

    public boolean getOwnCmcCallActivation() {
        Log.d(LOG_TAG, "getOwnCmcCallActivation");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        if (DEVICE_TYPE_PD.equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "cmc_device_type"))) {
            int i = Settings.Global.getInt(this.mContext.getContentResolver(), RET_CMC_ACTIVATION, 0);
            Log.d(LOG_TAG, "pd : cmc activation : " + i);
            if (i != 1) {
                return false;
            }
        } else {
            int i2 = Settings.Global.getInt(this.mContext.getContentResolver(), "cmc_call_activation", 0);
            Log.d(LOG_TAG, "sd : cmc call activation : " + i2);
            if (i2 != 1) {
                return false;
            }
        }
        return true;
    }

    public boolean getCmcActivation(String str) {
        Log.d(LOG_TAG, "getCmcActivation : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_activations", (String) null, bundle);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                int i = call.getInt("activation", -1);
                Log.d(LOG_TAG, "call inf : getCmcActivation success : " + i);
                if (i == 1) {
                    return true;
                }
                return false;
            }
            Log.e(LOG_TAG, "call inf : getCmcActivation fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            if (TextUtils.isEmpty(str) || !str.equalsIgnoreCase(Settings.Global.getString(this.mContext.getContentResolver(), "cmc_device_id"))) {
                return false;
            }
            int i2 = Settings.Global.getInt(this.mContext.getContentResolver(), RET_CMC_ACTIVATION, 0);
            Log.d(LOG_TAG, "cmc activation : " + i2);
            if (i2 != 1) {
                return false;
            }
        }
    }

    public boolean getCmcMessageActivation(String str) {
        Log.d(LOG_TAG, "getCmcMessageActivation : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_activations", (String) null, bundle);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                int i = call.getInt(RET_DEVICE_MESSAGE_ACTIVATION, -1);
                Log.d(LOG_TAG, "call inf : getCmcMessageActivation success : " + i);
                if (i == 1) {
                    return true;
                }
                return false;
            }
            Log.e(LOG_TAG, "call inf : getCmcMessageActivation fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public boolean getCmcCallActivation(String str) {
        Log.d(LOG_TAG, "getCmcCallActivation : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_activations", (String) null, bundle);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                int i = call.getInt(RET_DEVICE_CALL_ACTIVATION, -1);
                Log.d(LOG_TAG, "call inf : getCmcCallActivation success : " + i);
                if (i == 1) {
                    return true;
                }
                return false;
            }
            Log.e(LOG_TAG, "call inf : getCmcCallActivation fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public boolean getWatchActivation() {
        Log.d(LOG_TAG, "getWatchActivation");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        int i = Settings.Global.getInt(context.getContentResolver(), "cmc_watch_activation", 0);
        Log.d(LOG_TAG, "cmc watch activation : " + i);
        if (i == 1) {
            return true;
        }
        return false;
    }

    public CmcSettingManagerConstants.DeviceType getOwnDeviceType() {
        Log.d(LOG_TAG, "getOwnDeviceType");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        String string = Settings.Global.getString(context.getContentResolver(), "cmc_device_type");
        Log.d(LOG_TAG, "own device type - db : " + string);
        if (TextUtils.isEmpty(string)) {
            string = SemSystemProperties.get(CmcAccountManager.CMC_DEVICE_TYPE_PROP, "");
            Log.d(LOG_TAG, "own device type - ro : " + string);
            if (TextUtils.isEmpty(string)) {
                String str = SemSystemProperties.get("ro.build.characteristics");
                Log.d(LOG_TAG, "own device type - characteristics : " + str);
                return (str == null || !str.contains("tablet")) ? CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD : CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD;
            }
        }
        return getDeviceTypeInternal(string);
    }

    public String getOwnDeviceId() {
        String str = "";
        Log.d(LOG_TAG, "getOwnDeviceId");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_own_device_id", (String) null, (Bundle) null);
            if (call == null) {
                str = null;
            } else if (call.getInt(RET_RESULT, -1) == 1) {
                str = call.getString(RET_OWN_DEVICE_ID, str);
                Log.d(LOG_TAG, "call inf : getOwnDeviceId success : " + str);
            } else {
                Log.e(LOG_TAG, "call inf : getOwnDeviceId fail : " + call.getString("error_reason"));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occurred : " + e.toString());
            str = Settings.Global.getString(this.mContext.getContentResolver(), "cmc_device_id");
        }
        Log.d(LOG_TAG, "own device id: " + str);
        return str;
    }

    public String getOwnDeviceName() {
        Log.d(LOG_TAG, "getOwnDeviceName");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_own_device_name", (String) null, (Bundle) null);
            if (call != null) {
                if (call.getInt(RET_RESULT, -1) == 1) {
                    String string = call.getString(RET_OWN_DEVICE_NAME, "");
                    Log.d(LOG_TAG, "call inf : getOwnDeviceName success : " + string);
                    return string;
                }
                Log.e(LOG_TAG, "call inf : getOwnDeviceName fail : " + call.getString("error_reason"));
                return "";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
        }
        return null;
    }

    public String getOwnServiceVersion() {
        Log.d(LOG_TAG, "getOwnServiceVersion");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        String string = Settings.Global.getString(context.getContentResolver(), "cmc_service_version");
        Log.d(LOG_TAG, "own service version in global : " + string);
        if (!TextUtils.isEmpty(string)) {
            return string;
        }
        String str = SemSystemProperties.get(CmcAccountManager.CMC_VERSION_PROP, "");
        Log.d(LOG_TAG, "own service version in prop : " + str);
        return str;
    }

    public CmcSettingManagerConstants.NetworkMode getOwnNetworkMode() {
        Log.d(LOG_TAG, "getOwnNetworkMode");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        int i = Settings.Global.getInt(context.getContentResolver(), "cmc_network_type", -1);
        Log.d(LOG_TAG, "own network mode : " + i);
        if (i == 0) {
            return CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_USE_MOBILE_NETWORK;
        }
        if (1 == i) {
            return CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_WIFI_ONLY;
        }
        return CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_UNDEFINED;
    }

    public long getOwnCmcActivationTime() {
        Log.d(LOG_TAG, "getOwnCmcActivationTime");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return -1;
        }
        long j = Settings.Global.getLong(context.getContentResolver(), "cmc_activation_time", 0);
        Log.d(LOG_TAG, "own activation time : " + j);
        return j;
    }

    public long getOwnMessageActivationTime() {
        Log.d(LOG_TAG, "getOwnMessageActivationTime");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return -1;
        }
        long j = Settings.Global.getLong(context.getContentResolver(), "cmc_message_activation_time", 0);
        Log.d(LOG_TAG, "own message activation time : " + j);
        return j;
    }

    public long getOwnCallActivationTime() {
        Log.d(LOG_TAG, "getOwnCallActivationTime");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return -1;
        }
        long j = Settings.Global.getLong(context.getContentResolver(), "cmc_call_activation_time", 0);
        Log.d(LOG_TAG, "own call activation time : " + j);
        return j;
    }

    public String getLineId() {
        Log.d(LOG_TAG, "getLineId");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_id", (String) null, (Bundle) null);
            if (call != null) {
                if (call.getInt(RET_RESULT, -1) == 1) {
                    String string = call.getString("line_id", "");
                    Log.d(LOG_TAG, "getLineId success : " + inspector(string));
                    return string;
                }
                Log.e(LOG_TAG, "getLineId fail : " + call.getString("error_reason"));
                return "";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
        }
        return null;
    }

    public String getLineMsisdn() {
        Log.d(LOG_TAG, "getLineMsisdn");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_msisdn", (String) null, (Bundle) null);
            if (call != null) {
                if (call.getInt(RET_RESULT, -1) == 1) {
                    String string = call.getString("msisdn", "");
                    Log.d(LOG_TAG, "call inf : getMsisdn success : " + inspector(string));
                    return string;
                }
                Log.e(LOG_TAG, "call inf : getMsisdn fail : " + call.getString("error_reason"));
                return "";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
        }
        return null;
    }

    public ArrayList<String> getLineNmsAddrList() {
        Log.d(LOG_TAG, "getLineNmsAddrList");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_nms_addr_list", (String) null, (Bundle) null);
            if (call == null) {
                return null;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                ArrayList<String> stringArrayList = call.getStringArrayList(RET_NMS_ADDR_LIST);
                Log.d(LOG_TAG, "call inf : getNmsAddrList success : " + inspector(stringArrayList));
                return stringArrayList;
            }
            Log.e(LOG_TAG, "call inf : getNmsAddrList fail : " + call.getString("error_reason"));
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return null;
        }
    }

    public ArrayList<String> getLinePcscfAddrList() {
        Log.d(LOG_TAG, "getLinePcscfAddrList");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_pcscf_addr_list", (String) null, (Bundle) null);
            if (call == null) {
                return null;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                ArrayList<String> stringArrayList = call.getStringArrayList("pcscf_addr_list");
                Log.d(LOG_TAG, "call inf : getPcscfAddrList success : " + inspector(stringArrayList));
                return stringArrayList;
            }
            Log.e(LOG_TAG, "call inf : getPcscfAddrList fail : " + call.getString("error_reason"));
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return null;
        }
    }

    public int getLineActiveSimSlot() {
        Log.d(LOG_TAG, "getLineActiveSimSlot");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return -1;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_active_sim_slot", (String) null, (Bundle) null);
            if (call == null) {
                return -1;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                int i = call.getInt(RET_ACTIVE_SIM_SLOT, -1);
                Log.d(LOG_TAG, "call inf : getActiveSimSlot success : " + i);
                return i;
            }
            Log.e(LOG_TAG, "call inf : getActiveSimSlot fail : " + call.getString("error_reason"));
            return -1;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return -1;
        }
    }

    public String getLineImpu() {
        Log.d(LOG_TAG, "getLineImpu");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_impu", (String) null, (Bundle) null);
            if (call != null) {
                if (call.getInt(RET_RESULT, -1) == 1) {
                    String string = call.getString("impu", "");
                    Log.d(LOG_TAG, "call inf : getLineImpu success : " + inspector(string));
                    return string;
                }
                Log.e(LOG_TAG, "call inf : getLineImpu fail : " + call.getString("error_reason"));
                return "";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
        }
        return null;
    }

    public ArrayList<String> getDeviceIdList() {
        Log.d(LOG_TAG, "getDeviceIdList");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_device_id_list", (String) null, (Bundle) null);
            if (call == null) {
                return null;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                ArrayList<String> stringArrayList = call.getStringArrayList(RET_DEVICE_ID_LIST);
                Log.d(LOG_TAG, "call inf : getDeviceIdList success : " + stringArrayList);
                return stringArrayList;
            }
            Log.e(LOG_TAG, "call inf : getDeviceIdList fail : " + call.getString("error_reason"));
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return null;
        }
    }

    public String getDeviceName(String str) {
        Log.d(LOG_TAG, "getDeviceName : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_device_name", (String) null, bundle);
            if (call != null) {
                if (call.getInt(RET_RESULT, -1) == 1) {
                    String string = call.getString("device_name", "");
                    Log.d(LOG_TAG, "call inf : getDeviceName success : " + string);
                    return string;
                }
                Log.e(LOG_TAG, "call inf : getDeviceName fail : " + call.getString("error_reason"));
                return "";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
        }
        return null;
    }

    public CmcSettingManagerConstants.DeviceCategory getDeviceCategory(String str) {
        Log.d(LOG_TAG, "getDeviceCategory : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        CmcSettingManagerConstants.DeviceCategory deviceCategory = CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_UNDEFINED;
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_device_category", (String) null, bundle);
            if (call == null) {
                return deviceCategory;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                String string = call.getString(RET_DEVICE_CATEGORY, "");
                Log.d(LOG_TAG, "call inf : getDeviceCategory success : " + string);
                return getDeviceCategoryInternal(string);
            }
            Log.e(LOG_TAG, "call inf : getDeviceCategory fail : " + call.getString("error_reason"));
            return deviceCategory;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return deviceCategory;
        }
    }

    public CmcSettingManagerConstants.DeviceType getDeviceType(String str) {
        Log.d(LOG_TAG, "getDeviceType : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        CmcSettingManagerConstants.DeviceType deviceType = CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_UNDEFINED;
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_device_type", (String) null, bundle);
            if (call == null) {
                return deviceType;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                String string = call.getString("device_type", "");
                Log.d(LOG_TAG, "call inf : getDeviceType success : " + string);
                return getDeviceTypeInternal(string);
            }
            Log.e(LOG_TAG, "call inf : getDeviceType fail : " + call.getString("error_reason"));
            return deviceType;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return deviceType;
        }
    }

    public String getPdDeviceName() {
        Log.d(LOG_TAG, "getPdDeviceName");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_pd_device_name", (String) null, (Bundle) null);
            if (call != null) {
                if (call.getInt(RET_RESULT, -1) == 1) {
                    String string = call.getString(RET_PD_DEVICE_NAME, "");
                    Log.d(LOG_TAG, "call inf : getPdDeviceName success : " + string);
                    return string;
                }
                Log.e(LOG_TAG, "call inf : getPdDeviceName fail : " + call.getString("error_reason"));
                return "";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
        }
        return null;
    }

    public boolean isMessageAllowedSdByPd(String str) {
        Log.d(LOG_TAG, "isMessageAllowedSdByPd : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_message_allowed_sd_by_pd", (String) null, bundle);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                int i = call.getInt(RET_DEVICE_MESSAGE_ALLOWED_SD_BY_PD, -1);
                Log.d(LOG_TAG, "call inf : isMessageAllowedSdByPd success : " + i);
                if (i == 1) {
                    return true;
                }
                return false;
            }
            Log.e(LOG_TAG, "call inf : isMessageAllowedSdByPd fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public boolean isCallAllowedSdByPd(String str) {
        Log.d(LOG_TAG, "isCallAllowedSdByPd : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_call_allowed_sd_by_pd", (String) null, bundle);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                int i = call.getInt(RET_DEVICE_CALL_ALLOWED_SD_BY_PD, -1);
                Log.d(LOG_TAG, "call inf : isCallAllowedSdByPd success : " + i);
                if (i == 1) {
                    return true;
                }
                return false;
            }
            Log.e(LOG_TAG, "call inf : isCallAllowedSdByPd fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public CmcLineInfo getLineInfo() {
        Log.d(LOG_TAG, "getLineInfo");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_info", (String) null, (Bundle) null);
            if (call == null) {
                return null;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                CmcLineInfo cmcLineInfo = new CmcLineInfo();
                for (String str : call.keySet()) {
                    if ("line_id".equalsIgnoreCase(str)) {
                        cmcLineInfo.setLineId(call.getString(str));
                    } else if ("msisdn".equalsIgnoreCase(str)) {
                        cmcLineInfo.setMsisdn(call.getString(str));
                    } else if ("impu".equalsIgnoreCase(str)) {
                        cmcLineInfo.setImpu(call.getString(str));
                    } else if (RET_ACTIVE_SIM_SLOT.equalsIgnoreCase(str)) {
                        cmcLineInfo.setLineSlotIndex(call.getInt(str));
                    } else if (RET_NMS_ADDR_LIST.equalsIgnoreCase(str)) {
                        cmcLineInfo.setNmsAddrList(call.getStringArrayList(str));
                    } else if ("pcscf_addr_list".equalsIgnoreCase(str)) {
                        cmcLineInfo.setPcscfAddrList(call.getStringArrayList(str));
                    }
                }
                return cmcLineInfo;
            }
            Log.e(LOG_TAG, "call inf : getLineInfo fail : " + call.getString("error_reason"));
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return null;
        }
    }

    public CmcDeviceInfo getDeviceInfo(String str) {
        Log.d(LOG_TAG, "getDeviceInfo : " + str);
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_device_info", (String) null, bundle);
            if (call == null) {
                return null;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                CmcDeviceInfo cmcDeviceInfo = new CmcDeviceInfo();
                cmcDeviceInfo.setDeviceId(str);
                for (String str2 : call.keySet()) {
                    if ("device_name".equalsIgnoreCase(str2)) {
                        cmcDeviceInfo.setDeviceName(call.getString(str2));
                    } else if (RET_DEVICE_CATEGORY.equalsIgnoreCase(str2)) {
                        cmcDeviceInfo.setDeviceCategory(getDeviceCategoryInternal(call.getString(str2)));
                    } else if ("device_type".equalsIgnoreCase(str2)) {
                        cmcDeviceInfo.setDeviceType(getDeviceTypeInternal(call.getString(str2)));
                    } else {
                        boolean z = false;
                        if ("activation".equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setActivation(z);
                        } else if (RET_DEVICE_MESSAGE_ACTIVATION.equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setMessageActivation(z);
                        } else if (RET_DEVICE_CALL_ACTIVATION.equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setCallActivation(z);
                        } else if (RET_DEVICE_MESSAGE_ALLOWED_SD_BY_PD.equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setMessageAllowedSdByPd(z);
                        } else if (RET_DEVICE_CALL_ALLOWED_SD_BY_PD.equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setCallAllowedSdByPd(z);
                        }
                    }
                }
                return cmcDeviceInfo;
            }
            Log.e(LOG_TAG, "call inf : getDeviceInfo fail : " + call.getString("error_reason"));
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return null;
        }
    }

    public boolean hasMessageAllowedSdByPd() {
        Log.d(LOG_TAG, "hasMessageAllowedSdByPd");
        if (this.mContext != null) {
            return isExistActivationSd(false);
        }
        Log.e(LOG_TAG, "context is null");
        return false;
    }

    public boolean hasCallAllowedSdByPd() {
        Log.d(LOG_TAG, "hasCallAllowedSdByPd");
        if (this.mContext != null) {
            return isExistActivationSd(true);
        }
        Log.e(LOG_TAG, "context is null");
        return false;
    }

    public boolean getWatchRegistered(String str) {
        Log.d(LOG_TAG, "getWatchRegistered : " + inspector(str));
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString(PARAM_BT_MAC_ID, str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_watch_registered", (String) null, bundle);
            if (call == null) {
                return false;
            }
            if (call.getInt(RET_RESULT, -1) == 1) {
                int i = call.getInt(RET_WATCH_REGISTER, -1);
                Log.d(LOG_TAG, "call inf : getWatchRegistered success : " + i);
                if (i == 1) {
                    return true;
                }
                return false;
            }
            Log.e(LOG_TAG, "call inf : getWatchRegistered fail : " + call.getString("error_reason"));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
            return false;
        }
    }

    public boolean getCmcActivation() {
        Log.d(LOG_TAG, "getCmcActivation");
        if (this.mContext != null) {
            return getOwnCmcActivation();
        }
        Log.e(LOG_TAG, "context is null");
        return false;
    }

    public boolean getMessageActivation(String str) {
        Log.d(LOG_TAG, "getMessageActivation : " + str);
        if (this.mContext != null) {
            return isMessageAllowedSdByPd(str);
        }
        Log.e(LOG_TAG, "context is null");
        return false;
    }

    public boolean getCallActivation(String str) {
        Log.d(LOG_TAG, "getCallActivation : " + str);
        if (this.mContext != null) {
            return isCallAllowedSdByPd(str);
        }
        Log.e(LOG_TAG, "context is null");
        return false;
    }

    public boolean isExistMessageActivationSD() {
        Log.d(LOG_TAG, "isExistMessageActivationSD");
        if (this.mContext != null) {
            return hasMessageAllowedSdByPd();
        }
        Log.e(LOG_TAG, "context is null");
        return false;
    }

    public boolean isExistCallActivationSD() {
        Log.d(LOG_TAG, "isExistCallActivationSD");
        if (this.mContext != null) {
            return hasCallAllowedSdByPd();
        }
        Log.e(LOG_TAG, "context is null");
        return false;
    }

    public boolean isReadyBothPdAndSd() {
        boolean z;
        boolean z2;
        Log.d(LOG_TAG, "isReadyBothPdAndSd");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        ArrayList<String> deviceIdList = getDeviceIdList();
        if (deviceIdList != null) {
            Iterator<String> it = deviceIdList.iterator();
            z2 = false;
            z = false;
            while (it.hasNext()) {
                String next = it.next();
                CmcSettingManagerConstants.DeviceType deviceType = getDeviceType(next);
                if (deviceType != CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD || z2) {
                    if (deviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD && !z && getCmcActivation(next)) {
                        z = true;
                    }
                } else if (getCmcActivation(next)) {
                    z2 = true;
                }
                if (z2 && z) {
                    break;
                }
            }
        } else {
            z2 = false;
            z = false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("isReadyBothPdAndSd procedure finish  : isExistPd(");
        sb.append(z2);
        sb.append("), isExistSd(");
        sb.append(z);
        sb.append("), ret(");
        sb.append(z2 && z);
        sb.append(")");
        Log.d(LOG_TAG, sb.toString());
        if (!z2 || !z) {
            return false;
        }
        return true;
    }

    public CmcSaInfo getSamsungAccountInfo() {
        Log.d(LOG_TAG, "getSamsungAccountInfo");
        if (this.mContext == null) {
            Log.e(LOG_TAG, "context is null");
            return null;
        }
        CmcSaInfo cmcSaInfo = new CmcSaInfo();
        try {
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_sa_info", (String) null, (Bundle) null);
            if (call != null) {
                if (call.getInt(RET_RESULT, -1) == 1) {
                    cmcSaInfo.setSaUserId(call.getString(RET_SAMSUNG_USER_ID));
                    cmcSaInfo.setSaAccessToken(call.getString("access_token"));
                    Log.d(LOG_TAG, "call inf : getSamsungAccountInfo success");
                } else {
                    Log.e(LOG_TAG, "call inf : getSamsungAccountInfo fail : " + call.getString("error_reason"));
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception is occured : " + e.toString());
        }
        return cmcSaInfo;
    }

    public boolean isSameWifiNetworkOnly() {
        Log.d(LOG_TAG, "isSameWifiNetworkOnly");
        Context context = this.mContext;
        if (context == null) {
            Log.e(LOG_TAG, "context is null");
            return false;
        }
        int i = Settings.Global.getInt(context.getContentResolver(), "cmc_same_wifi_network_status", 0);
        Log.d(LOG_TAG, "isSameWifiNetworkStatus : " + i);
        if (i == 1) {
            return true;
        }
        return false;
    }

    private void registerObserver(OBSERVER_TYPE observer_type) {
        if (Looper.myLooper() == null) {
            Log.d(LOG_TAG, "looper is null create");
            Looper.prepare();
        }
        switch (AnonymousClass19.$SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[observer_type.ordinal()]) {
            case 1:
                registerCmcActivationObserver();
                return;
            case 2:
                registerCmcMessageActivationObserver();
                return;
            case 3:
                registerCmcCallActivationObserver();
                return;
            case 4:
                registerCmcWatchActivationObserver();
                return;
            case 5:
                registerCmcNetworkModeObserver();
                return;
            case 6:
                registerCmcLineInfoObserver();
                return;
            case 7:
                registerCmcDeviceInfoObserver();
                return;
            case 8:
                registerSamsungAccountInfoObserver();
                return;
            case 9:
                registerSameWifiNetworkStatusObserver();
                return;
            default:
                return;
        }
    }

    /* renamed from: com.samsung.android.cmcsetting.CmcSettingManager$19  reason: invalid class name */
    static /* synthetic */ class AnonymousClass19 {
        static final /* synthetic */ int[] $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE;

        static {
            int[] iArr = new int[OBSERVER_TYPE.values().length];
            $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE = iArr;
            try {
                iArr[OBSERVER_TYPE.mainCctivation.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.messageActivation.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.callActivation.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.watchActivation.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.networkMode.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.lineInfo.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.deviceInfo.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.saInfo.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.sameWifiNetworkStatus.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[OBSERVER_TYPE.all.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    private void registerCmcActivationObserver() {
        if (this.mCmcActivationDbChangeObserver == null) {
            this.mCmcActivationDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mCmcActivationDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcActivation();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForCmcActivation, true, this.mCmcActivationDbChangeObserver);
        }
    }

    private void registerCmcMessageActivationObserver() {
        if (this.mCmcMessageActivationDbChangeObserver == null) {
            this.mCmcMessageActivationDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mCmcMessageActivationDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcMessageActivation();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForCmcMessageActivation, true, this.mCmcMessageActivationDbChangeObserver);
        }
    }

    private void registerCmcCallActivationObserver() {
        if (this.mCmcCallActivationDbChangeObserver == null) {
            this.mCmcCallActivationDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mCmcCallActivationDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcCallActivation();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForCmcCallActivation, true, this.mCmcCallActivationDbChangeObserver);
        }
    }

    private void registerCmcWatchActivationObserver() {
        if (this.mWatchActivationDbChangeObserver == null) {
            this.mWatchActivationDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mWatchActivationDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcWatchActivation();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForWatchActivation, true, this.mWatchActivationDbChangeObserver);
        }
    }

    private void registerCmcNetworkModeObserver() {
        if (this.mNetworkModeDbChangeObserver == null) {
            this.mNetworkModeDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mNetworkModeDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcNetworkMode();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForNetworkMode, true, this.mNetworkModeDbChangeObserver);
        }
    }

    private void registerCmcLineInfoObserver() {
        if (this.mLinesDbChangeObserver == null) {
            this.mLinesDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mLinesDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcLines();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForLines, true, this.mLinesDbChangeObserver);
        }
    }

    private void registerCmcDeviceInfoObserver() {
        if (this.mDevicesDbChangeObserver == null) {
            this.mDevicesDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mDevicesDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcDevices();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForDevices, true, this.mDevicesDbChangeObserver);
        }
    }

    private void registerSamsungAccountInfoObserver() {
        if (this.mSaInfoDbChangeObserver == null) {
            this.mSaInfoDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mSaInfoDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcSaInfo();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForSaInfo, true, this.mSaInfoDbChangeObserver);
        }
    }

    private void registerSameWifiNetworkStatusObserver() {
        if (this.mSameWifiNetworkStatusObserver == null) {
            this.mSameWifiNetworkStatusObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d(CmcSettingManager.LOG_TAG, "mSameWifiNetworkStatusObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventSameWifiNetworkStatus();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForSameWifiNetworkStatus, true, this.mSameWifiNetworkStatusObserver);
        }
    }

    private void unregisterObserver(OBSERVER_TYPE observer_type) {
        switch (AnonymousClass19.$SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[observer_type.ordinal()]) {
            case 1:
                unregisterCmcActivationObserver();
                return;
            case 2:
                unregisterCmcMessageActivationObserver();
                return;
            case 3:
                unregisterCmcCallActivationObserver();
                return;
            case 4:
                unregisterCmcWatchActivationObserver();
                return;
            case 5:
                unregisterCmcNetworkModeObserver();
                return;
            case 6:
                unregisterCmcLineInfoObserver();
                return;
            case 7:
                unregisterCmcDeviceInfoObserver();
                return;
            case 8:
                unregisterSamsungAccountInfoObserver();
                return;
            case 9:
                unregisterSameWifiNetworkStatusObserver();
                return;
            case 10:
                unregisterCmcActivationObserver();
                unregisterCmcMessageActivationObserver();
                unregisterCmcCallActivationObserver();
                unregisterCmcWatchActivationObserver();
                unregisterCmcNetworkModeObserver();
                unregisterCmcLineInfoObserver();
                unregisterCmcDeviceInfoObserver();
                unregisterSamsungAccountInfoObserver();
                unregisterSameWifiNetworkStatusObserver();
                return;
            default:
                return;
        }
    }

    private void unregisterCmcActivationObserver() {
        if (this.mCmcActivationDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mCmcActivationDbChangeObserver);
            this.mCmcActivationDbChangeObserver = null;
        }
    }

    private void unregisterCmcMessageActivationObserver() {
        if (this.mCmcMessageActivationDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mCmcMessageActivationDbChangeObserver);
            this.mCmcMessageActivationDbChangeObserver = null;
        }
    }

    private void unregisterCmcCallActivationObserver() {
        if (this.mCmcCallActivationDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mCmcCallActivationDbChangeObserver);
            this.mCmcCallActivationDbChangeObserver = null;
        }
    }

    private void unregisterCmcWatchActivationObserver() {
        if (this.mWatchActivationDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mWatchActivationDbChangeObserver);
            this.mWatchActivationDbChangeObserver = null;
        }
    }

    private void unregisterCmcNetworkModeObserver() {
        if (this.mNetworkModeDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mNetworkModeDbChangeObserver);
            this.mNetworkModeDbChangeObserver = null;
        }
    }

    private void unregisterCmcLineInfoObserver() {
        if (this.mLinesDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mLinesDbChangeObserver);
            this.mLinesDbChangeObserver = null;
        }
    }

    private void unregisterCmcDeviceInfoObserver() {
        if (this.mDevicesDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDevicesDbChangeObserver);
            this.mDevicesDbChangeObserver = null;
        }
    }

    private void unregisterSamsungAccountInfoObserver() {
        if (this.mSaInfoDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSaInfoDbChangeObserver);
            this.mSaInfoDbChangeObserver = null;
        }
    }

    private void unregisterSameWifiNetworkStatusObserver() {
        if (this.mSameWifiNetworkStatusObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSameWifiNetworkStatusObserver);
            this.mSameWifiNetworkStatusObserver = null;
        }
    }

    private CmcSettingManagerConstants.DeviceType getDeviceTypeInternal(String str) {
        if (DEVICE_TYPE_PD.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD;
        }
        if (DEVICE_TYPE_SD.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD;
        }
        return CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_UNDEFINED;
    }

    private CmcSettingManagerConstants.DeviceCategory getDeviceCategoryInternal(String str) {
        if (DEVICE_CATEGORY_PHONE.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_PHONE;
        }
        if (DEVICE_CATEGORY_TABLET.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_TABLET;
        }
        if (DEVICE_CATEGORY_BT_WATCH.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_BT_WATCH;
        }
        if (DEVICE_CATEGORY_SPEAKER.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_SPEAKER;
        }
        if (DEVICE_CATEGORY_PC.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_PC;
        }
        if (DEVICE_CATEGORY_TV.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_TV;
        }
        if (DEVICE_CATEGORY_LAPTOP.equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_LAPTOP;
        }
        return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_UNDEFINED;
    }

    private boolean isExistActivationSd(boolean z) {
        ArrayList<String> deviceIdList = getDeviceIdList();
        if (deviceIdList == null || deviceIdList.size() <= 0) {
            return false;
        }
        Iterator<String> it = deviceIdList.iterator();
        while (it.hasNext()) {
            CmcDeviceInfo deviceInfo = getDeviceInfo(it.next());
            if (deviceInfo != null && deviceInfo.getDeviceType() == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD) {
                if (z) {
                    if (deviceInfo.isCallAllowedSdByPd()) {
                        Log.d(LOG_TAG, "call activation sd is exist");
                        return true;
                    }
                } else if (deviceInfo.isMessageAllowedSdByPd()) {
                    Log.d(LOG_TAG, "message activation sd is exist");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSupportVersion(Context context, String str) {
        try {
            String valueOf = String.valueOf(context.getPackageManager().getPackageInfo(str, 0).versionName);
            Log.d(LOG_TAG, "cur version : " + valueOf);
            if ("2.2.00.00".compareTo(valueOf) < 0) {
                return true;
            }
            Log.e(LOG_TAG, "Not supported version or not exist");
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String inspector(Object obj) {
        if (obj == null) {
            return null;
        }
        if (SHIP_BUILD) {
            return "xxxxx";
        }
        return "" + obj;
    }

    /* access modifiers changed from: private */
    public void sendEventCmcActivation() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcActivationChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcActivationChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcActivationInfoChangedListener cmcActivationInfoChangedListener = (CmcActivationInfoChangedListener) it.next();
                        if (cmcActivationInfoChangedListener != null) {
                            cmcActivationInfoChangedListener.onChangedCmcActivation();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcMessageActivation() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcMessageActivationChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcMessageActivationChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcMessageActivationInfoChangedListener cmcMessageActivationInfoChangedListener = (CmcMessageActivationInfoChangedListener) it.next();
                        if (cmcMessageActivationInfoChangedListener != null) {
                            cmcMessageActivationInfoChangedListener.onChangedCmcMessageActivation();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcCallActivation() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcCallActivationChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcCallActivationChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcCallActivationInfoChangedListener cmcCallActivationInfoChangedListener = (CmcCallActivationInfoChangedListener) it.next();
                        if (cmcCallActivationInfoChangedListener != null) {
                            cmcCallActivationInfoChangedListener.onChangedCmcCallActivation();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcWatchActivation() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcWatchActivationChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcWatchActivationChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcWatchActivationInfoChangedListener cmcWatchActivationInfoChangedListener = (CmcWatchActivationInfoChangedListener) it.next();
                        if (cmcWatchActivationInfoChangedListener != null) {
                            cmcWatchActivationInfoChangedListener.onChangedWatchActivation();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcNetworkMode() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcNetworkModeChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcNetworkModeChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcNetworkModeInfoChangedListener cmcNetworkModeInfoChangedListener = (CmcNetworkModeInfoChangedListener) it.next();
                        if (cmcNetworkModeInfoChangedListener != null) {
                            cmcNetworkModeInfoChangedListener.onChangedNetworkMode();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcLines() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcLineInfoChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcLineInfoChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcLineInfoChangedListener cmcLineInfoChangedListener = (CmcLineInfoChangedListener) it.next();
                        if (cmcLineInfoChangedListener != null) {
                            cmcLineInfoChangedListener.onChangedLineInfo();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcDevices() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcDeviceInfoChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcDeviceInfoChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcDeviceInfoChangedListener cmcDeviceInfoChangedListener = (CmcDeviceInfoChangedListener) it.next();
                        if (cmcDeviceInfoChangedListener != null) {
                            cmcDeviceInfoChangedListener.onChangedDeviceInfo();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcSaInfo() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcSamsungAccountInfoChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcSamsungAccountInfoChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcSamsungAccountInfoChangedListener cmcSamsungAccountInfoChangedListener = (CmcSamsungAccountInfoChangedListener) it.next();
                        if (cmcSamsungAccountInfoChangedListener != null) {
                            cmcSamsungAccountInfoChangedListener.onChangedSamsungAccountInfo();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventSameWifiNetworkStatus() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mSameWifiNetworkStatusListenerList != null) {
                    Iterator it = CmcSettingManager.this.mSameWifiNetworkStatusListenerList.iterator();
                    while (it.hasNext()) {
                        CmcSameWifiNetworkStatusListener cmcSameWifiNetworkStatusListener = (CmcSameWifiNetworkStatusListener) it.next();
                        if (cmcSameWifiNetworkStatusListener != null) {
                            cmcSameWifiNetworkStatusListener.onChangedSameWifiNetworkStatus();
                        }
                    }
                }
            }
        }).start();
    }
}
