package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimpleEventLog;
import java.util.List;

class SimDataAdaptor {
    protected static final String LOG_TAG = SimDataAdaptor.class.getSimpleName();
    protected String mLastOperator = "";
    protected int mPreferredImpuIndex = 1;
    protected SimManager mSimManager = null;
    protected SimpleEventLog mSimpleEventLog = null;

    SimDataAdaptor(SimManager simManager) {
        this.mSimManager = simManager;
        this.mSimpleEventLog = simManager.getSimpleEventLog();
        this.mLastOperator = this.mSimManager.getSimOperator();
    }

    public static SimDataAdaptor getSimDataAdaptor(SimManager sm) {
        Mno mno = sm.getSimMno();
        if (mno == Mno.TMOUS) {
            return new SimDataAdaptorTmoUs(sm);
        }
        if (mno == Mno.ATT) {
            return new SimDataAdaptorAtt(sm);
        }
        if (mno == Mno.SAMSUNG) {
            return new SimDataAdaptorTestBed(sm);
        }
        if (mno == Mno.VZW) {
            return new SimDataAdaptorVzw(sm);
        }
        if (mno == Mno.GCF) {
            return new SimDataAdaptorGcf(sm);
        }
        if (mno == Mno.KDDI) {
            return new SimDataAdaptorKddi(sm);
        }
        if (mno == Mno.CMCC) {
            return new SimDataAdaptorCmcc(sm);
        }
        if (mno == Mno.SPRINT) {
            return new SimDataAdaptorSpr(sm);
        }
        if (mno == Mno.USCC) {
            return new SimDataAdaptorUsc(sm);
        }
        return new SimDataAdaptor(sm);
    }

    public String getEmergencyImpu(List<String> impus) {
        Log.i(LOG_TAG, "getEmergencyImpu:");
        if (impus == null || impus.size() == 0 || TextUtils.isEmpty(impus.get(0))) {
            return null;
        }
        return impus.get(0);
    }

    public String getImpuFromList(List<String> impus) {
        Log.i(LOG_TAG, "getImpuFromList:");
        if (impus == null || impus.size() == 0) {
            return null;
        }
        if (impus.size() <= 1 || TextUtils.isEmpty(impus.get(this.mPreferredImpuIndex)) || !SimManager.isValidImpu(impus.get(this.mPreferredImpuIndex))) {
            return getValidImpu(impus);
        }
        return impus.get(this.mPreferredImpuIndex);
    }

    /* access modifiers changed from: protected */
    public String getValidImpu(List<String> impus) {
        for (String impu : impus) {
            if (SimManager.isValidImpu(impu)) {
                return impu;
            }
        }
        return null;
    }

    public boolean hasValidMsisdn() {
        return true;
    }

    public boolean needHandleLoadedAgain(String operator) {
        if (TextUtils.equals(operator, this.mLastOperator)) {
            return false;
        }
        String str = LOG_TAG;
        Log.i(str, "Different operator. Last:" + this.mLastOperator + ", new:" + operator);
        this.mLastOperator = operator;
        return true;
    }
}
