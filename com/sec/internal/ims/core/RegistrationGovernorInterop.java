package com.sec.internal.ims.core;

import android.content.Context;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;

public class RegistrationGovernorInterop extends RegistrationGovernorBase {
    public RegistrationGovernorInterop(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        return true;
    }
}
