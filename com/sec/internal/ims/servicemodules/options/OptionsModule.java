package com.sec.internal.ims.servicemodules.options;

import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.options.OptionsRequestController;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.interfaces.ims.servicemodules.options.IOptionsModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class OptionsModule extends ServiceModuleBase implements IOptionsModule, ICapabilityExchangeControl {
    private static final int EVT_CAPABILITIES_UPDATE = 1;
    private static final String LOG_TAG = "OptionsModule";
    OptionsRequestController mController;
    private PhoneIdKeyMap<Boolean> mIsOptionsEnabled = new PhoneIdKeyMap<>(SimUtil.getPhoneCount(), false);
    ICapabilityEventListener mListener;

    public OptionsModule(Looper looper) {
        super(looper);
    }

    public void registerCapabilityEventListener(ICapabilityEventListener listener) {
        this.mListener = listener;
    }

    public String getName() {
        return OptionsModule.class.getSimpleName();
    }

    public String[] getServicesRequiring() {
        return new String[]{"options"};
    }

    public void init() {
        super.init();
        Log.i(LOG_TAG, "init");
        OptionsRequestController optionsRequestController = new OptionsRequestController(getLooper());
        this.mController = optionsRequestController;
        optionsRequestController.registerOptionsEvent(new OptionsRequestController.IOptionsEventListener() {
            public final void onCapabilityUpdate(OptionsEvent optionsEvent) {
                OptionsModule.this.lambda$init$0$OptionsModule(optionsEvent);
            }
        });
        this.mController.init();
    }

    public /* synthetic */ void lambda$init$0$OptionsModule(OptionsEvent event) {
        sendMessage(obtainMessage(1, event));
    }

    public void onRegistered(ImsRegistration reg) {
        super.onRegistered(reg);
        this.mController.setImsRegistration(reg);
        this.mIsOptionsEnabled.put(reg.getPhoneId(), true);
        Log.i(LOG_TAG, "onRegistered: Options service is enabled.");
    }

    public void onDeregistered(ImsRegistration reg, int error) {
        super.onDeregistered(reg, error);
        this.mController.setImsDeRegistration(reg);
        this.mIsOptionsEnabled.put(reg.getPhoneId(), false);
        Log.i(LOG_TAG, "onDeregistered: Options service is disabled.");
        ICapabilityEventListener iCapabilityEventListener = this.mListener;
        if (iCapabilityEventListener != null) {
            iCapabilityEventListener.onMediaReady(false, false, reg.getPhoneId());
        }
    }

    public boolean isReadyToRequest(int phoneId) {
        return this.mIsOptionsEnabled.get(phoneId).booleanValue();
    }

    public void setOwnCapabilities(long features, int phoneId) {
        if (isRunning()) {
            this.mController.setOwnCapabilities(features, phoneId);
            if (this.mListener != null && this.mIsOptionsEnabled.get(phoneId).booleanValue()) {
                this.mListener.onMediaReady(true, false, phoneId);
            }
        }
    }

    public int requestCapabilityExchange(List<ImsUri> list, CapabilityConstants.RequestType type, int phoneId) {
        Log.e(LOG_TAG, "requestCapabilityExchange: OPTIONS doesn't support list.");
        return 0;
    }

    public boolean requestCapabilityExchange(ImsUri uri, ICapabilityExchangeControl.ICapabilityExchangeCallback callback, CapabilityConstants.RequestType type, boolean isAlwaysForce, long myFeatures, int phoneId, String extFeature) {
        ImsUri uri2;
        ICapabilityExchangeControl.ICapabilityExchangeCallback iCapabilityExchangeCallback = callback;
        int i = phoneId;
        if (!isRunning()) {
            return false;
        }
        IMSLog.s(LOG_TAG, i, "requestCapabilityExchange: uri " + uri.toString() + " iari " + extFeature);
        if (!SimUtil.getSimMno(phoneId).isRjil() || ChatbotUriUtil.hasUriBotPlatform(uri)) {
            uri2 = uri;
        } else {
            uri2 = UriGeneratorFactory.getInstance().get(i).getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, uri.getMsisdn(), (String) null);
        }
        if (iCapabilityExchangeCallback != null) {
            callback.onComplete((Capabilities) null);
        }
        if (this.mIsOptionsEnabled.get(i).booleanValue()) {
            return this.mController.requestCapabilityExchange(uri2, myFeatures, phoneId, extFeature);
        }
        return false;
    }

    public boolean sendCapexResponse(ImsUri uri, long myFeatures, String txId, int lastSeen, int phoneId, String extFeature) {
        if (!isRunning()) {
            return false;
        }
        IMSLog.s(LOG_TAG, "sendCapexResponse: uri " + uri.toString());
        return this.mController.sendCapexResponse(uri, myFeatures, txId, lastSeen, phoneId, extFeature);
    }

    private void onCapabilitiesUpdate(OptionsEvent event) {
        IMSLog.s(LOG_TAG, "onCapabilitiesUpdate: success " + event.isSuccess() + " uri " + event.getUri());
        CapabilityConstants.CapExResult result = CapabilityConstants.CapExResult.SUCCESS;
        Mno mno = SimManagerFactory.getSimManager().getSimMno();
        if (!event.isSuccess()) {
            result = convertOptionsError(event.getReason());
            if (result != CapabilityConstants.CapExResult.USER_AVAILABLE_OFFLINE) {
                event.setFeatures((long) Capabilities.FEATURE_NON_RCS_USER);
            } else {
                event.setFeatures((long) Capabilities.FEATURE_OFFLINE_RCS_USER);
            }
        } else if ((ConfigUtil.isRcsEur(mno) || mno == Mno.TELSTRA || "TEL".equals(OmcCode.get())) && event.getFeatures() == 0) {
            event.setFeatures((long) Capabilities.FEATURE_NON_RCS_USER);
        }
        if (this.mListener != null) {
            List<ImsUri> list = new ArrayList<>();
            list.add(event.getUri());
            Log.i(LOG_TAG, "onCapabilitiesUpdate: success " + event.isSuccess() + " txID " + event.getTxId());
            this.mListener.onCapabilityUpdate(list, result, (String) null, event);
        }
    }

    private CapabilityConstants.CapExResult convertOptionsError(OptionsEvent.OptionsFailureReason reason) {
        if (reason == OptionsEvent.OptionsFailureReason.USER_NOT_AVAILABLE) {
            return CapabilityConstants.CapExResult.USER_NOT_FOUND;
        }
        if (reason == OptionsEvent.OptionsFailureReason.DOES_NOT_EXIST_ANYWHERE) {
            return CapabilityConstants.CapExResult.DOES_NOT_EXIST_ANYWHERE;
        }
        if (reason == OptionsEvent.OptionsFailureReason.USER_NOT_REACHABLE || reason == OptionsEvent.OptionsFailureReason.USER_NOT_REGISTERED) {
            return CapabilityConstants.CapExResult.USER_UNAVAILABLE;
        }
        if (reason == OptionsEvent.OptionsFailureReason.FORBIDDEN_403) {
            return CapabilityConstants.CapExResult.FORBIDDEN_403;
        }
        if (reason == OptionsEvent.OptionsFailureReason.REQUEST_TIMED_OUT) {
            return CapabilityConstants.CapExResult.REQUEST_TIMED_OUT;
        }
        if (reason == OptionsEvent.OptionsFailureReason.INVALID_DATA) {
            return CapabilityConstants.CapExResult.INVALID_DATA;
        }
        if (reason == OptionsEvent.OptionsFailureReason.USER_AVAILABLE_OFFLINE || reason == OptionsEvent.OptionsFailureReason.AUTOMATA_PRESENT) {
            return CapabilityConstants.CapExResult.USER_AVAILABLE_OFFLINE;
        }
        return CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == 1) {
            onCapabilitiesUpdate((OptionsEvent) msg.obj);
        }
    }

    public void handleIntent(Intent intent) {
    }

    public void readConfig(int phoneId) {
    }

    public void registerService(String serviceId, String version, int phoneId) {
    }

    public void deRegisterService(List<String> list, int phoneId) {
    }

    public void reset(int phoneId) {
    }
}
