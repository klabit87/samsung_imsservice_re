package com.sec.internal.ims.cmstore;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.interfaces.ims.cmstore.ICmsModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class CmsModule extends ServiceModuleBase implements ICmsModule {
    private static final int EVENT_GETPROFILE = 3;
    private static final int EVENT_SIM_READY = 1;
    private static final int EVENT_SIM_REFRESH = 2;
    private static final String LOG_TAG = CmsModule.class.getSimpleName();
    private static boolean isCmsServiceActive = false;
    private static final int mReadImsProfileValueDelay = 1200;
    private final Context mContext;
    private ISimManager mSimManager;

    public CmsModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
    }

    public String[] getServicesRequiring() {
        return new String[]{"cms", "im", "slm", "ft", "ft_http"};
    }

    public void handleIntent(Intent intent) {
        Log.v(LOG_TAG, "handleIntent");
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int i = msg.what;
        if (i != 1) {
            if (i == 2) {
                onSimRefresh();
            } else if (i == 3) {
                onProfileReady();
            }
        } else if (hasMessages(2)) {
            Log.d(LOG_TAG, "Sim refresh is ongoing. SIM readyretry after");
            sendEmptyMessageDelayed(1, 800);
        } else {
            onSimReady(!Util.isSimExist(this.mContext));
        }
    }

    private void onProfileReady() {
        String servicesFromGs = ImsRegistry.getString(0, GlobalSettingsConstants.Registration.EXTENDED_SERVICES, "");
        List<String> services = new ArrayList<>();
        int phoneId = SimUtil.getDefaultPhoneId();
        if (servicesFromGs != null) {
            for (String service : servicesFromGs.split(",")) {
                services.add(service);
            }
        }
        Log.d(LOG_TAG, "onProfileReady ,services: " + services);
        if ("AIO".equals(OmcCode.getNWCode(phoneId)) || !services.contains("cms")) {
            Log.v(LOG_TAG, "Central message store not enabled.");
            isCmsServiceActive = false;
            try {
                CloudMessageServiceWrapper mCldMsgService = CloudMessageServiceWrapper.getInstance(this.mContext);
                if (mCldMsgService != null) {
                    mCldMsgService.onDisableCms();
                }
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "onDisableRCS: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            enableCloudMessageService();
        }
    }

    private void onSimReady(boolean isAbsent) {
        String str = LOG_TAG;
        Log.v(str, "onSimReady: isAbsent=" + isAbsent);
        if (!isAbsent) {
            sendEmptyMessageDelayed(3, 1200);
        }
    }

    private void onSimRefresh() {
        if (isCmsServiceActive) {
            if (Util.isSimExist(this.mContext)) {
                Log.v(LOG_TAG, "onSimRefresh: SIM is still available");
                return;
            }
            Log.v(LOG_TAG, "onSimRefresh: SIM slot is removed");
            try {
                CloudMessageServiceWrapper mCldMsgService = CloudMessageServiceWrapper.getInstance(this.mContext);
                if (mCldMsgService != null) {
                    mCldMsgService.onSimRemoved();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        super.start();
        Log.v(LOG_TAG, "start");
        enableCloudMessageService();
    }

    public void init() {
        super.init();
        Log.v(LOG_TAG, "init");
        ISimManager simManager = SimManagerFactory.getSimManager();
        this.mSimManager = simManager;
        simManager.registerForSimReady(this, 1, (Object) null);
        this.mSimManager.registerForSimRefresh(this, 2, (Object) null);
        this.mSimManager.registerForSimRemoved(this, 2, (Object) null);
    }

    public void stop() {
        super.stop();
        Log.v(LOG_TAG, "stop");
    }

    public void onRegistered(ImsRegistration regiInfo) {
        if (!isCmsServiceActive || regiInfo == null || regiInfo.getPreferredImpu() == null || regiInfo.getPreferredImpu().getUri() == null) {
            Log.v(LOG_TAG, "onRegistered, null regiInfo");
            return;
        }
        super.onRegistered(regiInfo);
        try {
            String msisdn = regiInfo.getPreferredImpu().getUri().getMsisdn();
            String str = LOG_TAG;
            Log.d(str, "onRegistered, msisdn: " + IMSLog.checker(msisdn));
            CloudMessageServiceWrapper mCldMsgService = CloudMessageServiceWrapper.getInstance(this.mContext);
            if (msisdn != null && mCldMsgService != null) {
                mCldMsgService.onImsRegistered(msisdn);
            }
        } catch (RemoteException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "onRCSDbReady: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        super.onDeregistered(regiInfo, errorCode);
        Log.v(LOG_TAG, "onDeregistered");
    }

    public void onConfigured(int phoneId) {
        super.onConfigured(phoneId);
        Log.v(LOG_TAG, "onConfigured");
    }

    private void enableCloudMessageService() {
        isCmsServiceActive = true;
        CloudMessageServiceWrapper.setCmsProfileEnabled(this.mContext, true);
        try {
            CloudMessageServiceWrapper mCldMsgService = CloudMessageServiceWrapper.getInstance(this.mContext);
            if (mCldMsgService != null) {
                mCldMsgService.onRCSDbReady();
            }
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "onRCSDbReady: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
