package com.sec.internal.ims.servicemodules.options;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class CapabilityForIncall extends Handler {
    private static final String LOG_TAG = "CapabilityForIncall";
    public static final String NAME = CapabilityForIncall.class.getSimpleName();
    protected Map<Integer, List<ICall>> mActiveCallLists = new HashMap();
    private CapabilityUtil mCapabilityUtil = null;
    protected boolean mIsNeedUpdateCallState = false;
    private String mRcsProfile = "";
    IRegistrationManager mRegMan = null;
    private CapabilityDiscoveryModule mServiceModule = null;

    public CapabilityForIncall(CapabilityDiscoveryModule capexServiceModule, CapabilityUtil capabilityUtil, IRegistrationManager rm) {
        this.mServiceModule = capexServiceModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mRegMan = rm;
    }

    public void processCallStateChanged(int phoneId, CopyOnWriteArrayList<ICall> calls, Map<Integer, ImsRegistration> imsRegInfoList) {
        post(new Runnable(calls, phoneId, imsRegInfoList) {
            public final /* synthetic */ CopyOnWriteArrayList f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ Map f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                CapabilityForIncall.this.lambda$processCallStateChanged$0$CapabilityForIncall(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$processCallStateChanged$0$CapabilityForIncall(CopyOnWriteArrayList calls, int phoneId, Map imsRegInfoList) {
        String extFeature;
        Set<String> services;
        int networkType;
        boolean z;
        int i = phoneId;
        Map map = imsRegInfoList;
        int nConnectedCalls = checkConnectedCalls(calls);
        List<ICall> activeCalls = setActiveCalls(i);
        int nPrevConnectedCalls = checkPrevConnectedCalls(activeCalls);
        IMSLog.i(LOG_TAG, i, "processCallStateChanged: nConnectedCalls=" + nConnectedCalls + " nPrevConnectedCalls=" + nPrevConnectedCalls);
        Capabilities ownCap = this.mServiceModule.getOwnCapabilitiesBase(i);
        CapabilityConfig mCapabilityConfig = this.mServiceModule.getCapabilityConfig(i);
        this.mRcsProfile = mCapabilityConfig != null ? mCapabilityConfig.getRcsProfile() : "";
        if (mCapabilityConfig != null && ((!mCapabilityConfig.usePresence() || ImsProfile.isRcsUpProfile(this.mRcsProfile)) && map.containsKey(Integer.valueOf(phoneId)) && ownCap.hasAnyFeature(Capabilities.FEATURE_CALL_SERVICE))) {
            long features = ownCap.getFeature();
            if (!CollectionUtils.isNullOrEmpty((Collection<?>) ownCap.getExtFeature())) {
                extFeature = String.join(",", ownCap.getExtFeature());
            } else {
                extFeature = "";
            }
            int networkType2 = this.mRegMan.getCurrentNetworkByPhoneId(i);
            boolean z2 = false;
            Set<String> services2 = this.mRegMan.getServiceForNetwork(((ImsRegistration) map.get(Integer.valueOf(phoneId))).getImsProfile(), ((ImsRegistration) map.get(Integer.valueOf(phoneId))).getRegiRat(), false, i);
            long features2 = this.mCapabilityUtil.filterFeaturesWithService(features, services2, networkType2);
            Iterator it = calls.iterator();
            while (it.hasNext()) {
                ICall call = (ICall) it.next();
                ICall prev = getCall(activeCalls, call.getNumber());
                IMSLog.s(LOG_TAG, "prev: " + prev + ", current: " + call);
                if (prev != null) {
                    ICall prev2 = prev;
                    services = services2;
                    ICall call2 = call;
                    networkType = networkType2;
                    z = false;
                    if ((!prev2.isConnected() || nPrevConnectedCalls > 1) && call2.isConnected() && nConnectedCalls == 1) {
                        setIncallFeature(phoneId, call2.getNumber(), features2, extFeature, true);
                    } else if ((prev2.isConnected() && nPrevConnectedCalls == 1 && (!call2.isConnected() || nConnectedCalls > 1)) || (!prev2.isConnected() && call2.isConnected() && nConnectedCalls > 1)) {
                        setIncallFeature(phoneId, call2.getNumber(), features2, extFeature, false);
                    }
                    activeCalls.remove(prev2);
                } else if (!call.isConnected() || nConnectedCalls != 1) {
                    services = services2;
                    ICall call3 = call;
                    networkType = networkType2;
                    z = false;
                    if (nConnectedCalls > 1) {
                        setIncallFeature(phoneId, call3.getNumber(), features2, extFeature, false);
                    }
                } else {
                    ICall iCall = prev;
                    services = services2;
                    ICall iCall2 = call;
                    z = false;
                    networkType = networkType2;
                    setIncallFeature(phoneId, call.getNumber(), features2, extFeature, true);
                }
                z2 = z;
                networkType2 = networkType;
                services2 = services;
                Map map2 = imsRegInfoList;
            }
            boolean z3 = z2;
            int i2 = networkType2;
            for (ICall call4 : activeCalls) {
                IMSLog.s(LOG_TAG, "Disconnected call: " + call4);
                if (call4.isConnected() && nPrevConnectedCalls == 1) {
                    this.mServiceModule.setCallNumber(i, (String) null);
                    this.mServiceModule.updateOwnCapabilities(i);
                    this.mServiceModule.setOwnCapabilities(i, z3);
                }
            }
            String str = extFeature;
        }
        this.mActiveCallLists.put(Integer.valueOf(phoneId), calls);
    }

    private int checkConnectedCalls(CopyOnWriteArrayList<ICall> calls) {
        int nConnectedCalls = 0;
        Iterator<ICall> it = calls.iterator();
        while (it.hasNext()) {
            if (it.next().isConnected()) {
                nConnectedCalls++;
            }
        }
        return nConnectedCalls;
    }

    private int checkPrevConnectedCalls(List<ICall> activeCalls) {
        int nPrevConnectedCalls = 0;
        for (ICall call : activeCalls) {
            if (call.isConnected()) {
                nPrevConnectedCalls++;
            }
        }
        return nPrevConnectedCalls;
    }

    private List<ICall> setActiveCalls(int phoneId) {
        List<ICall> activeCalls = new ArrayList<>();
        if (this.mActiveCallLists.containsKey(Integer.valueOf(phoneId))) {
            return this.mActiveCallLists.get(Integer.valueOf(phoneId));
        }
        return activeCalls;
    }

    public void processCallStateChangedOnDeregi(int phoneId, CopyOnWriteArrayList<ICall> calls) {
        post(new Runnable(calls, phoneId) {
            public final /* synthetic */ CopyOnWriteArrayList f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                CapabilityForIncall.this.lambda$processCallStateChangedOnDeregi$1$CapabilityForIncall(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$processCallStateChangedOnDeregi$1$CapabilityForIncall(CopyOnWriteArrayList calls, int phoneId) {
        Log.i(LOG_TAG, "mImsRegInfo: null");
        int nConnectedCalls = checkConnectedCalls(calls);
        List<ICall> activeCalls = setActiveCalls(phoneId);
        int nPrevConnectedCalls = checkPrevConnectedCalls(activeCalls);
        this.mIsNeedUpdateCallState = true;
        Iterator it = calls.iterator();
        while (it.hasNext()) {
            ICall call = (ICall) it.next();
            ICall prev = getCall(activeCalls, call.getNumber());
            if (prev != null) {
                if ((!prev.isConnected() || nPrevConnectedCalls > 1) && call.isConnected() && nConnectedCalls == 1) {
                    this.mServiceModule.setCallNumber(phoneId, call.getNumber());
                } else if ((prev.isConnected() && nPrevConnectedCalls == 1 && (!call.isConnected() || nConnectedCalls > 1)) || (!prev.isConnected() && call.isConnected() && nConnectedCalls > 1)) {
                    this.mServiceModule.setCallNumber(phoneId, (String) null);
                }
                activeCalls.remove(prev);
            } else if (call.isConnected() && nConnectedCalls == 1) {
                this.mServiceModule.setCallNumber(phoneId, call.getNumber());
            }
        }
        for (ICall call2 : activeCalls) {
            if (call2.isConnected() && nPrevConnectedCalls == 1) {
                this.mServiceModule.setCallNumber(phoneId, (String) null);
            }
        }
        this.mActiveCallLists.put(Integer.valueOf(phoneId), calls);
    }

    private ICall getCall(List<ICall> activeCalls, String number) {
        for (ICall call : activeCalls) {
            if (TextUtils.equals(call.getNumber(), number)) {
                return call;
            }
        }
        return null;
    }

    private void setIncallFeature(int phoneId, String callNumber, long features, String extFeature, boolean activeCsh) {
        IMSLog.i(LOG_TAG, phoneId, "SetIncallFeature");
        if (activeCsh) {
            Log.i(LOG_TAG, "Activate content share features.");
            this.mServiceModule.setCallNumber(phoneId, callNumber);
            this.mServiceModule.exchangeCapabilities(callNumber, features, phoneId, extFeature);
            this.mServiceModule.updateOwnCapabilities(phoneId);
            this.mServiceModule.setOwnCapabilities(phoneId, false);
            return;
        }
        Log.i(LOG_TAG, "Deactivate content share features.");
        this.mServiceModule.setCallNumber(phoneId, (String) null);
        this.mServiceModule.exchangeCapabilities(callNumber, ((long) (~Capabilities.FEATURE_VSH)) & features & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP) & (~Capabilities.FEATURE_ENRICHED_SHARED_SKETCH), phoneId, extFeature);
        this.mServiceModule.updateOwnCapabilities(phoneId);
        this.mServiceModule.setOwnCapabilities(phoneId, false);
    }

    public void exchangeCapabilitiesForVSH(int phoneId, boolean enable, Map<Integer, ImsRegistration> imsRegInfoList) {
        String extFeature;
        List<ICall> activeCalls;
        int i = phoneId;
        Map<Integer, ImsRegistration> map = imsRegInfoList;
        if (this.mRegMan == null || !map.containsKey(Integer.valueOf(phoneId))) {
            Log.i(LOG_TAG, "exchangeCapabilitiesForVSH: mRegMan or mImsRegInfo is null ");
            return;
        }
        int networkType = this.mRegMan.getCurrentNetworkByPhoneId(i);
        Set<String> services = this.mRegMan.getServiceForNetwork(map.get(Integer.valueOf(phoneId)).getImsProfile(), map.get(Integer.valueOf(phoneId)).getRegiRat(), false, i);
        Capabilities ownCap = this.mServiceModule.getOwnCapabilities(i);
        if (ownCap != null) {
            long features = this.mCapabilityUtil.filterFeaturesWithService(ownCap.getFeature(), services, networkType);
            if (!CollectionUtils.isNullOrEmpty((Collection<?>) ownCap.getExtFeature())) {
                extFeature = String.join(",", ownCap.getExtFeature());
            } else {
                extFeature = "";
            }
            if (this.mIsNeedUpdateCallState) {
                this.mIsNeedUpdateCallState = false;
            }
            List<ICall> activeCalls2 = new ArrayList<>();
            if (this.mActiveCallLists.containsKey(Integer.valueOf(phoneId))) {
                activeCalls = this.mActiveCallLists.get(Integer.valueOf(phoneId));
            } else {
                activeCalls = activeCalls2;
            }
            ICall activeCall = null;
            int nPrevConnectedCalls = 0;
            for (ICall call : activeCalls) {
                if (call.isConnected()) {
                    nPrevConnectedCalls++;
                    activeCall = call;
                }
            }
            if (nPrevConnectedCalls == 1) {
                if (enable) {
                    int i2 = nPrevConnectedCalls;
                    this.mServiceModule.exchangeCapabilities(activeCall.getNumber(), features, phoneId, extFeature);
                    return;
                }
                this.mServiceModule.exchangeCapabilities(activeCall.getNumber(), ((long) (~Capabilities.FEATURE_VSH)) & features, phoneId, extFeature);
            }
        }
    }

    public void triggerCapexForIncallRegiDeregi(int phoneId, ImsRegistration regiInfo) {
        if (regiInfo.hasService("options") && this.mActiveCallLists.containsKey(Integer.valueOf(phoneId)) && this.mActiveCallLists.get(Integer.valueOf(phoneId)).size() > 0) {
            List<ICall> activeCalls = this.mActiveCallLists.get(Integer.valueOf(phoneId));
            UriGenerator uriGenerator = this.mServiceModule.getUriGenerator();
            if (uriGenerator != null) {
                for (ICall call : activeCalls) {
                    if (call.isConnected()) {
                        this.mServiceModule.requestCapabilityExchange(uriGenerator.getNormalizedUri(call.getNumber(), true), CapabilityConstants.RequestType.REQUEST_TYPE_NONE, true, phoneId);
                    }
                }
            }
        }
    }
}
