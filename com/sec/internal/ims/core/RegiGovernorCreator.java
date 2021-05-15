package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;

public abstract class RegiGovernorCreator {
    private static final String LOG_TAG = "RegiGvnCreator";

    public static IRegistrationGovernor getInstance(Mno mno, RegistrationManagerInternal regMan, ITelephonyManager tm, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        if (!DeviceUtil.getGcfMode()) {
            String rcsAs = ConfigUtil.getAcsServerType(context, task.getPhoneId());
            if (task.isRcsOnly() && ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(rcsAs)) {
                return new RegistrationGovernorRcsJibe(regMan, tm, task, pdnController, vsm, cm, context);
            }
            if (mno != Mno.MDMN || task.getProfile().getCmcType() == 0) {
                return getInstanceInternal(mno, regMan, tm, task, pdnController, vsm, cm, context);
            }
            return new RegistrationGovernorCmc(regMan, tm, task, pdnController, vsm, cm, context);
        }
        Mno mno2 = mno;
        Context context2 = context;
        return new RegistrationGovernorImpl(regMan, tm, task, pdnController, vsm, cm, context);
    }

    private static IRegistrationGovernor getInstanceInternal(Mno mno, RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("getInstance: creating RegistrationGovernor for ");
        Mno mno2 = mno;
        sb.append(mno);
        Log.i(LOG_TAG, sb.toString());
        if (mno.isKor()) {
            return new RegistrationGovernorKor(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isUSA()) {
            return getInstanceForUsa(mno, regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isChn()) {
            return getInstanceForChina(mno, regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isHkMo() || mno.isTw()) {
            return new RegistrationGovernorHkTw(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isCanada()) {
            return new RegistrationGovernorCan(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isJpn()) {
            return getInstanceForJapan(mno, regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isMea()) {
            return new RegistrationGovernorMeAfrica(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isEur()) {
            return new RegistrationGovernorEur(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isOce()) {
            return new RegistrationGovernorOce(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isSea()) {
            return new RegistrationGovernorSea(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isLatin()) {
            return new RegistrationGovernorLatin(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno.isSwa()) {
            return getInstanceForSwa(mno, regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        return new RegistrationGovernorBase(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    private static IRegistrationGovernor getInstanceForUsa(Mno mno, RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        Mno mno2 = mno;
        if (mno2 == Mno.VZW) {
            return new RegistrationGovernorVzw(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno2 == Mno.ATT) {
            if (SimUtil.isSoftphoneEnabled()) {
                return new RegistrationGovernorSoftphone(regMan, telephonyManager, task, pdnController, vsm, cm, context);
            }
            return new RegistrationGovernorAtt(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        } else if (mno2 == Mno.TMOUS) {
            return new RegistrationGovernorTmo(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        } else {
            if (mno2 == Mno.SPRINT) {
                return new RegistrationGovernorSpr(regMan, telephonyManager, task, pdnController, vsm, cm, context);
            }
            if (mno2 == Mno.USCC) {
                return new RegistrationGovernorUsc(regMan, telephonyManager, task, pdnController, vsm, cm, context);
            }
            if (mno2 == Mno.GENERIC_IR92 || mno2 == Mno.ALTICE || mno2 == Mno.GCI) {
                return new RegistrationGovernorIR92(regMan, telephonyManager, task, pdnController, vsm, cm, context);
            }
            return new RegistrationGovernorBase(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
    }

    private static IRegistrationGovernor getInstanceForSwa(Mno mno, RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        if (mno == Mno.RJIL) {
            return new RegistrationGovernorRjil(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        return new RegistrationGovernorSwa(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    private static IRegistrationGovernor getInstanceForChina(Mno mno, RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        Mno mno2 = mno;
        if (mno2 == Mno.CMCC) {
            return new RegistrationGovernorCmcc(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno2 == Mno.CU) {
            return new RegistrationGovernorCu(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno2 == Mno.CTC || mno2 == Mno.CTCMO) {
            return new RegistrationGovernorCtc(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        return new RegistrationGovernorBase(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    private static IRegistrationGovernor getInstanceForJapan(Mno mno, RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        Mno mno2 = mno;
        if (mno2 == Mno.DOCOMO) {
            return new RegistrationGovernorDcm(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno2 == Mno.KDDI) {
            return new RegistrationGovernorKddi(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        if (mno2 == Mno.SOFTBANK) {
            return new RegistrationGovernorSoftBank(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        }
        return new RegistrationGovernorBase(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }
}
