package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorIR92 extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnIR92";

    public RegistrationGovernorIR92(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        if (task.getMno() == Mno.ALTICE) {
            IMSLog.i(LOG_TAG, task.getPhoneId(), "Force to enable vocecall_type for ATL.");
            ImsConstants.SystemSettings.setVoiceCallType(context, 0, task.getPhoneId());
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> services) {
        Log.i(LOG_TAG, "applyVoPsPolicy:");
        if (services == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            removeService(services, "mmtel", "applyVoPsPolicy");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
        }
        return services;
    }

    public Set<String> filterService(Set<String> services, int network) {
        return super.filterService(applyMmtelUserSettings(services, network), network);
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        if ("SERVICE_OPTION_NOT_SUBSCRIBED".equalsIgnoreCase(reason) && this.mTask.getPdnType() == 11 && this.mTask.getRegistrationRat() == 13) {
            Log.i(LOG_TAG, "send ImsNotAvailable");
            this.mRegMan.notifyImsNotAvailable(this.mTask, true);
        }
    }
}
