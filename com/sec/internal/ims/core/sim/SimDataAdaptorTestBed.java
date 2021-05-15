package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import java.util.List;

/* compiled from: SimDataAdaptor */
class SimDataAdaptorTestBed extends SimDataAdaptor {
    private static final String LOG_TAG = SimDataAdaptorTestBed.class.getSimpleName();

    SimDataAdaptorTestBed(SimManager simManager) {
        super(simManager);
    }

    public String getImpuFromList(List<String> impus) {
        Log.i(LOG_TAG, "getImpuFromList:");
        if (impus == null || impus.size() == 0) {
            return null;
        }
        if (impus.size() > 1 && !TextUtils.isEmpty(impus.get(1)) && impus.get(1).toLowerCase().startsWith("sip:+82")) {
            return impus.get(1);
        }
        if (!TextUtils.isEmpty(impus.get(0))) {
            return impus.get(0);
        }
        return null;
    }
}
