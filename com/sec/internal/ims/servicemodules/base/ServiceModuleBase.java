package com.sec.internal.ims.servicemodules.base;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.Registration;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ServiceModuleBase extends Handler implements IServiceModule {
    /* access modifiers changed from: protected */
    public int mDefaultPhoneId;
    protected long[] mEnabledFeatures;
    protected final CopyOnWriteArrayList<Registration> mRegistrationList;
    State mState;

    protected static class InitialState extends State {
    }

    protected static class ReadyState extends State {
    }

    protected static class RunningState extends State {
    }

    protected static class StoppedState extends State {
    }

    public abstract void handleIntent(Intent intent);

    protected ServiceModuleBase(Looper looper) {
        super(looper);
        this.mState = new InitialState();
        this.mDefaultPhoneId = 0;
        this.mRegistrationList = new CopyOnWriteArrayList<>();
        this.mDefaultPhoneId = SimUtil.getSimSlotPriority();
        long[] jArr = new long[SimUtil.getPhoneCount()];
        this.mEnabledFeatures = jArr;
        Arrays.fill(jArr, 0);
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public long getSupportFeature(int phoneId) {
        return this.mEnabledFeatures[phoneId];
    }

    public void init() {
        updateState(new ReadyState());
    }

    public void start() {
        updateState(new RunningState());
    }

    public void stop() {
        Arrays.fill(this.mEnabledFeatures, 0);
        this.mRegistrationList.clear();
        updateState(new StoppedState());
    }

    public void clearRegistrationList() {
        this.mRegistrationList.clear();
    }

    public boolean isReady() {
        return this.mState instanceof ReadyState;
    }

    public boolean isRunning() {
        return this.mState instanceof RunningState;
    }

    @Deprecated
    public boolean isStopped() {
        return this.mState instanceof StoppedState;
    }

    /* access modifiers changed from: protected */
    public ImsRegistration getRegistrationInfo() {
        if (this.mRegistrationList.size() > 0) {
            return this.mRegistrationList.get(0).getImsRegi();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int getRegistrationInfoId(ImsRegistration regiInfo) {
        return IRegistrationManager.getRegistrationInfoId(regiInfo.getImsProfile().getId(), regiInfo.getPhoneId());
    }

    public ImsRegistration getImsRegistration() {
        return getImsRegistration(this.mDefaultPhoneId);
    }

    public ImsRegistration getImsRegistration(int phoneId) {
        Registration reg = getRegistration(phoneId);
        if (reg != null) {
            return reg.getImsRegi();
        }
        return null;
    }

    public ImsRegistration getImsRegistration(int phoneId, boolean isEmergency) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = it.next();
            if (reg != null && reg.getImsRegi().getPhoneId() == phoneId && reg.getImsRegi().getImsProfile().hasEmergencySupport() == isEmergency) {
                return reg.getImsRegi();
            }
        }
        return null;
    }

    public Registration getRegistration(int phoneId) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = it.next();
            if (reg != null && reg.getImsRegi().getPhoneId() == phoneId && !reg.getImsRegi().getImsProfile().hasEmergencySupport() && reg.getImsRegi().getImsProfile().getCmcType() == 0) {
                return reg;
            }
        }
        return null;
    }

    public void onRegistered(ImsRegistration regiInfo) {
        Registration reg;
        int oldRegiIndex = -1;
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            reg = it.next();
            if (reg.getImsRegi().getHandle() == regiInfo.getHandle() || (reg.getImsRegi().getPhoneId() == regiInfo.getPhoneId() && TextUtils.equals(reg.getImsRegi().getImsProfile().getName(), regiInfo.getImsProfile().getName()))) {
                oldRegiIndex = this.mRegistrationList.indexOf(reg);
            }
        }
        oldRegiIndex = this.mRegistrationList.indexOf(reg);
        if (oldRegiIndex == -1) {
            this.mRegistrationList.add(new Registration(regiInfo, false));
        } else {
            this.mRegistrationList.set(oldRegiIndex, new Registration(regiInfo, true));
        }
    }

    public void onDeregistering(ImsRegistration reg) {
    }

    public void onReRegistering(int phoneId, Set<String> set) {
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = it.next();
            if (reg.getImsRegi().getHandle() == regiInfo.getHandle()) {
                this.mRegistrationList.remove(reg);
                return;
            }
        }
    }

    public void onNetworkChanged(NetworkEvent event, int phoneId) {
    }

    public void onConfigured(int phoneId) {
    }

    public void onImsConifgChanged(int phoneId, String dmUri) {
    }

    public void onSimChanged(int phoneId) {
    }

    public void onSimReady(int subId) {
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
    }

    public void onCallStateChanged(int phoneId, List<ICall> list) {
    }

    public void updateCapabilities(int phoneId) {
    }

    public ImsFeature.Capabilities queryCapabilityStatus(int phoneId) {
        return new ImsFeature.Capabilities();
    }

    public void handleMessage(Message msg) {
        if (msg.what > 8000 && msg.what != 8999) {
            handleModuleChannelRequest(msg);
        }
    }

    public void handleModuleChannelRequest(Message msg) {
    }

    /* access modifiers changed from: protected */
    public void sendModuleResponse(Message msg, int result, Object obj) {
        Message resp = (Message) msg.getData().getParcelable("callback_msg");
        if (resp != null) {
            resp.arg1 = result;
            resp.obj = new Object[]{(ModuleChannel.Listener) resp.obj, obj};
            resp.sendToTarget();
        }
    }

    private void updateState(State state) {
        State state2 = this.mState;
        if (state2 != state) {
            state2.exit();
            this.mState = state;
            state.enter();
        }
    }

    public void cleanUp() {
    }

    public void dump() {
    }

    /* access modifiers changed from: protected */
    public IServiceModuleManager getServiceModuleManager() {
        return ImsRegistry.getServiceModuleManager();
    }
}
