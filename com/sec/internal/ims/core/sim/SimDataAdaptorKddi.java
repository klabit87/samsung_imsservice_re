package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import java.util.List;

/* compiled from: SimDataAdaptor */
class SimDataAdaptorKddi extends SimDataAdaptor {
    private static final String LOG_TAG = SimDataAdaptorKddi.class.getSimpleName();

    SimDataAdaptorKddi(SimManager simManager) {
        super(simManager);
        this.mPreferredImpuIndex = 0;
    }

    public String getImpuFromList(List<String> impus) {
        Log.i(LOG_TAG, "getImpuFromList");
        if (impus == null || impus.size() == 0) {
            return null;
        }
        if (TextUtils.isEmpty(impus.get(this.mPreferredImpuIndex)) || !SimManager.isValidImpu(impus.get(this.mPreferredImpuIndex))) {
            return getValidImpu(impus);
        }
        return impus.get(this.mPreferredImpuIndex);
    }
}
