package com.sec.internal.ims.entitlement.softphone;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import java.util.concurrent.atomic.AtomicInteger;

public class VSimClient extends StateMachine {
    private static AtomicInteger sNextSerial = new AtomicInteger();
    /* access modifiers changed from: private */
    public final String LOG_TAG = getClass().getSimpleName();
    protected final State mDefaultState = new DefaultState();

    public VSimClient(Looper looper) {
        super("VSimClient", looper);
        initState();
    }

    private void initState() {
        addState(this.mDefaultState);
    }

    protected class DefaultState extends State {
        protected DefaultState() {
        }

        public void enter() {
            String access$000 = VSimClient.this.LOG_TAG;
            Log.i(access$000, VSimClient.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            String access$000 = VSimClient.this.LOG_TAG;
            Log.e(access$000, "Unexpected event " + msg.what + ". current state is " + VSimClient.this.getCurrentState().getName());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public int getHttpTransactionId() {
        return sNextSerial.getAndIncrement();
    }
}
