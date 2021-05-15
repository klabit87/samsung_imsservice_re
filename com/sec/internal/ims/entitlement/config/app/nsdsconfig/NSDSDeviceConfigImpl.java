package com.sec.internal.ims.entitlement.config.app.nsdsconfig;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.flow.DeviceConfigurationUpdate;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.AkaTokenRetrievalFlow;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.storagehelper.EntitlementConfigDBHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.entitlement.config.IEntitlementConfig;
import com.sec.internal.log.IMSLog;

public class NSDSDeviceConfigImpl extends Handler implements IEntitlementConfig {
    private static final int FORCE_CONFIG_UPDATE = 2;
    private static final String LOG_TAG = NSDSDeviceConfigImpl.class.getSimpleName();
    private static final int RETRIEVE_AKA_TOKEN = 3;
    private static final int RETRIEVE_DEVICE_CONFIG = 0;
    private static final int UPDATE_DEVICE_CONFIG = 1;
    private BaseFlowImpl mBaseFlowImpl;
    private Context mContext;
    private EntitlementConfigDBHelper mEntitlementConfigDBHelper;

    public NSDSDeviceConfigImpl(Looper looper, Context context, ISimManager simManager) {
        super(looper);
        this.mContext = context;
        this.mBaseFlowImpl = new BaseFlowImpl(looper, this.mContext, simManager);
        this.mEntitlementConfigDBHelper = new EntitlementConfigDBHelper(context.createCredentialProtectedStorageContext());
    }

    public void getDeviceConfig(String imsi, int deviceEventType) {
        String str = LOG_TAG;
        IMSLog.s(str, "getDeviceConfig: " + imsi);
        int msgWhat = 0;
        if (deviceEventType == 18) {
            msgWhat = 2;
        } else if (deviceEventType == 19) {
            msgWhat = 3;
        } else if (this.mEntitlementConfigDBHelper.isDeviceConfigAvailable(imsi)) {
            msgWhat = 1;
        }
        sendEmptyMessage(msgWhat);
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        IMSLog.i(str, "handleMessage:" + msg.what);
        int i = msg.what;
        if (i == 0) {
            retrieveDeviceConfiguration(14);
        } else if (i == 1) {
            retrieveDeviceConfiguration(15);
        } else if (i == 2) {
            retrieveDeviceConfiguration(18);
        } else if (i == 3) {
            retrieveAkaToken(19);
        }
    }

    private void retrieveDeviceConfiguration(int deviceEventType) {
        DeviceConfigurationUpdate deviceConfigurationUpdate = new DeviceConfigurationUpdate(getLooper(), this.mContext, this.mBaseFlowImpl, this.mEntitlementConfigDBHelper);
        String imsi = this.mBaseFlowImpl.getSimManager().getImsi();
        String str = LOG_TAG;
        IMSLog.s(str, "retrieveDeviceConfiguration: imsi:" + imsi);
        deviceConfigurationUpdate.performDeviceConfigRetrieval(deviceEventType, 0);
    }

    private void retrieveAkaToken(int deviceEventType) {
        AkaTokenRetrievalFlow akaTokenRetrieval = new AkaTokenRetrievalFlow(getLooper(), this.mContext, this.mBaseFlowImpl, this.mEntitlementConfigDBHelper);
        String imsi = this.mBaseFlowImpl.getSimManager().getImsi();
        String str = LOG_TAG;
        IMSLog.s(str, "akaTokenRetrieval: imsi:" + imsi);
        akaTokenRetrieval.performAkaTokenRetrieval(deviceEventType, 0);
    }
}
