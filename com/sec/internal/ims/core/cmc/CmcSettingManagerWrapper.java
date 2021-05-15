package com.sec.internal.ims.core.cmc;

import android.content.Context;
import android.util.Log;
import com.samsung.android.cmcsetting.CmcSaInfo;
import com.samsung.android.cmcsetting.CmcSettingManager;
import com.samsung.android.cmcsetting.CmcSettingManagerConstants;
import com.samsung.android.cmcsetting.listeners.CmcActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcCallActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcDeviceInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcLineInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcNetworkModeInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcSameWifiNetworkStatusListener;
import com.samsung.android.cmcsetting.listeners.CmcSamsungAccountInfoChangedListener;
import java.util.List;

public class CmcSettingManagerWrapper {
    private static final String LOG_TAG = "CmcSettingManagerWrapper";
    private CmcAccountManager mCmcAccountMgr;
    CmcSettingManager mCmcSettingManager;
    protected Context mContext;

    public CmcSettingManagerWrapper(Context context, CmcAccountManager cmcAccountManager) {
        this.mContext = context;
        this.mCmcAccountMgr = cmcAccountManager;
    }

    public void init() {
        Log.i(LOG_TAG, "init");
        CmcSettingManager cmcSettingManager = new CmcSettingManager();
        this.mCmcSettingManager = cmcSettingManager;
        if (cmcSettingManager.init(this.mContext)) {
            Log.i(LOG_TAG, "init listeners");
            this.mCmcSettingManager.registerListener(new CmcActivationInfoChangedListener() {
                public final void onChangedCmcActivation() {
                    CmcSettingManagerWrapper.this.lambda$init$0$CmcSettingManagerWrapper();
                }
            });
            this.mCmcSettingManager.registerListener(new CmcNetworkModeInfoChangedListener() {
                public final void onChangedNetworkMode() {
                    CmcSettingManagerWrapper.this.lambda$init$1$CmcSettingManagerWrapper();
                }
            });
            this.mCmcSettingManager.registerListener(new CmcLineInfoChangedListener() {
                public final void onChangedLineInfo() {
                    CmcSettingManagerWrapper.this.lambda$init$2$CmcSettingManagerWrapper();
                }
            });
            this.mCmcSettingManager.registerListener(new CmcDeviceInfoChangedListener() {
                public final void onChangedDeviceInfo() {
                    CmcSettingManagerWrapper.this.lambda$init$3$CmcSettingManagerWrapper();
                }
            });
            this.mCmcSettingManager.registerListener(new CmcCallActivationInfoChangedListener() {
                public final void onChangedCmcCallActivation() {
                    CmcSettingManagerWrapper.this.lambda$init$4$CmcSettingManagerWrapper();
                }
            });
            this.mCmcSettingManager.registerListener(new CmcSamsungAccountInfoChangedListener() {
                public final void onChangedSamsungAccountInfo() {
                    CmcSettingManagerWrapper.this.lambda$init$5$CmcSettingManagerWrapper();
                }
            });
            this.mCmcSettingManager.registerListener(new CmcSameWifiNetworkStatusListener() {
                public final void onChangedSameWifiNetworkStatus() {
                    CmcSettingManagerWrapper.this.lambda$init$6$CmcSettingManagerWrapper();
                }
            });
        }
    }

    public /* synthetic */ void lambda$init$0$CmcSettingManagerWrapper() {
        Log.i(LOG_TAG, "onChangedCmcActivation");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    public /* synthetic */ void lambda$init$1$CmcSettingManagerWrapper() {
        Log.i(LOG_TAG, "onChangedNetworkMode");
        this.mCmcAccountMgr.notifyCmcNwPrefChanged();
    }

    public /* synthetic */ void lambda$init$2$CmcSettingManagerWrapper() {
        Log.i(LOG_TAG, "onChangedLineInfo");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    public /* synthetic */ void lambda$init$3$CmcSettingManagerWrapper() {
        Log.i(LOG_TAG, "onChangedDeviceInfo");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    public /* synthetic */ void lambda$init$4$CmcSettingManagerWrapper() {
        Log.i(LOG_TAG, "onChangedCmcCallActivation");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    public /* synthetic */ void lambda$init$5$CmcSettingManagerWrapper() {
        Log.i(LOG_TAG, "onChangedSamsungAccountInfo:");
        this.mCmcAccountMgr.onChangedSamsungAccountInfo(getCmcSaAccessToken());
    }

    public /* synthetic */ void lambda$init$6$CmcSettingManagerWrapper() {
        Log.i(LOG_TAG, "onChangedSameWifiNetwork");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    public boolean getCmcSupported() {
        return this.mCmcSettingManager.getCmcSupported();
    }

    public String getDeviceType() {
        CmcSettingManagerConstants.DeviceType type = this.mCmcSettingManager.getOwnDeviceType();
        if (type == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            return "pd";
        }
        if (type == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD) {
            return "sd";
        }
        return "";
    }

    public String getDeviceId() {
        return this.mCmcSettingManager.getOwnDeviceId();
    }

    public int getPreferedNetwork() {
        CmcSettingManagerConstants.NetworkMode mode = this.mCmcSettingManager.getOwnNetworkMode();
        if (mode == CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_USE_MOBILE_NETWORK) {
            return 0;
        }
        if (mode == CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_WIFI_ONLY) {
            return 1;
        }
        return 1;
    }

    public String getServiceVersion() {
        return this.mCmcSettingManager.getOwnServiceVersion();
    }

    public String getLineId() {
        return this.mCmcSettingManager.getLineId();
    }

    public List<String> getDeviceIdList() {
        return this.mCmcSettingManager.getDeviceIdList();
    }

    public String getLineImpu() {
        return this.mCmcSettingManager.getLineImpu();
    }

    public String getDeviceTypeWithDeviceId(String deviceId) {
        CmcSettingManagerConstants.DeviceType type = this.mCmcSettingManager.getDeviceType(deviceId);
        if (type == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            return "pd";
        }
        if (type == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD) {
            return "sd";
        }
        return "";
    }

    public List<String> getPcscfAddressList() {
        return this.mCmcSettingManager.getLinePcscfAddrList();
    }

    public int getActiveSimSlot() {
        return this.mCmcSettingManager.getLineActiveSimSlot();
    }

    public boolean isCallAllowedSdByPd(String deviceId) {
        if (this.mCmcSettingManager.getOwnDeviceType() == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            return true;
        }
        return this.mCmcSettingManager.isCallAllowedSdByPd(deviceId);
    }

    public boolean getOwnCmcActivation() {
        return this.mCmcSettingManager.getOwnCmcActivation();
    }

    public boolean getCmcCallActivation(String deviceId) {
        return this.mCmcSettingManager.getCmcCallActivation(deviceId);
    }

    public String getCmcSaAccessToken() {
        CmcSaInfo cmcSa = this.mCmcSettingManager.getSamsungAccountInfo();
        if (cmcSa == null) {
            return "";
        }
        return cmcSa.getSaAccessToken();
    }

    public boolean isSameWifiNetworkOnly() {
        return this.mCmcSettingManager.isSameWifiNetworkOnly();
    }
}
