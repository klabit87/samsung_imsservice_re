package com.sec.internal.helper.os;

import android.os.Build;
import android.telephony.PreciseDataConnectionState;
import com.sec.ims.extensions.ReflectionUtils;

public class PreciseDataConnectionStateWrapper {
    private PreciseDataConnectionState mState;

    public PreciseDataConnectionStateWrapper(PreciseDataConnectionState state) {
        this.mState = state;
    }

    public boolean equals(Object obj) {
        return this.mState.equals(obj);
    }

    public int hashCode() {
        return this.mState.hashCode();
    }

    public String toString() {
        return this.mState.toString();
    }

    public int getDataConnectionFailCause() {
        if (Build.VERSION.SDK_INT >= 30) {
            return this.mState.getLastCauseCode();
        }
        try {
            return ((Integer) ReflectionUtils.invoke2(PreciseDataConnectionState.class.getMethod("getDataConnectionFailCause", new Class[0]), this.mState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getDataConnectionApnTypeBitMask() {
        try {
            return ((Integer) ReflectionUtils.invoke2(PreciseDataConnectionState.class.getMethod("getDataConnectionApnTypeBitMask", new Class[0]), this.mState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
