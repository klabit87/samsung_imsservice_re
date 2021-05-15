package com.sec.internal.ims.entitlement.config;

import android.os.Handler;
import android.os.Looper;
import com.sec.internal.helper.State;

public abstract class EntitlementConfigModuleBase extends Handler {
    State mState = new InitialState();

    protected static class InitialState extends State {
    }

    protected static class ReadyState extends State {
    }

    protected static class RunningState extends State {
    }

    protected EntitlementConfigModuleBase(Looper looper) {
        super(looper);
    }

    public void init() {
        updateState(new ReadyState());
    }

    public void start() {
        updateState(new RunningState());
    }

    public void onSimReady(boolean isSwapped) {
    }

    public void onDeviceReady() {
    }

    public void forceConfigUpdate() {
    }

    public void retriveAkaToken() {
    }

    private void updateState(State state) {
        State state2 = this.mState;
        if (state2 != state) {
            state2.exit();
            this.mState = state;
            state.enter();
        }
    }
}
