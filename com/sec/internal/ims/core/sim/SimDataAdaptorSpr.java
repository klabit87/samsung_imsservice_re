package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;

/* compiled from: SimDataAdaptor */
class SimDataAdaptorSpr extends SimDataAdaptor {
    private static final String LOG_TAG = SimDataAdaptorSpr.class.getSimpleName();
    private String mLastMsisdn = null;

    public SimDataAdaptorSpr(SimManager simManager) {
        super(simManager);
    }

    public boolean hasValidMsisdn() {
        String msisdn = this.mSimManager.getLine1Number();
        String str = LOG_TAG;
        IMSLog.s(str, "hasValidMsisdn : " + msisdn);
        this.mLastMsisdn = msisdn;
        if (isValidMsisdn(msisdn)) {
            return true;
        }
        this.mSimpleEventLog.logAndAdd("hasValidMsisdn: HFA isn't completed for SPR");
        IMSLog.c(LogClass.SIM_SPR_NEED_HFA, this.mSimManager.getSimSlotIndex() + ",NEED HFA");
        return false;
    }

    public boolean needHandleLoadedAgain(String operator) {
        if (super.needHandleLoadedAgain(operator)) {
            return true;
        }
        String msisdn = this.mSimManager.getLine1Number();
        if (TextUtils.equals(this.mLastMsisdn, msisdn) || !isValidMsisdn(msisdn)) {
            return false;
        }
        return true;
    }

    private boolean isValidMsisdn(String msisdn) {
        return !TextUtils.isEmpty(msisdn) && !msisdn.startsWith("000000");
    }
}
