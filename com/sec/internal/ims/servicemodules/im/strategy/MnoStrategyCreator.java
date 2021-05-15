package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;

public class MnoStrategyCreator {
    private static final String LOG_TAG = "MnoStrategyCreator";

    public static RcsPolicySettings.RcsPolicyType getPolicyType(Mno mno, int phoneId, Context context) {
        String rcsAs = ConfigUtil.getAcsServerType(context, phoneId);
        String rcsProfile = ConfigUtil.getRcsProfileLoaderInternalWithFeature(context, mno.getName(), phoneId);
        RcsPolicySettings.RcsPolicyType policyType = RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;
        if (!TextUtils.isEmpty(rcsAs)) {
            policyType = getPolicyTypeByRcsAs(rcsAs, mno);
        } else if (!TextUtils.isEmpty(rcsProfile)) {
            policyType = getPolicyTypeByRcsProfile(rcsProfile, mno);
        }
        if (policyType == RcsPolicySettings.RcsPolicyType.DEFAULT_RCS) {
            policyType = getPolicyTypeByMno(mno);
        }
        Log.i(LOG_TAG, "getPolicyType: phone" + phoneId + " " + mno + " => " + policyType);
        return policyType;
    }

    private static RcsPolicySettings.RcsPolicyType getPolicyTypeByRcsAs(String rcsAs, Mno mno) {
        if (ImsConstants.RCS_AS.JIBE.equals(rcsAs)) {
            if (mno == Mno.ORANGE_ROMANIA || mno == Mno.ORANGE_SLOVAKIA || mno == Mno.ORANGE_SPAIN || mno == Mno.ORANGE_BELGIUM) {
                return RcsPolicySettings.RcsPolicyType.ORANGE_UP;
            }
            if (mno == Mno.VODAFONE_INDIA || mno == Mno.IDEA_INDIA) {
                return RcsPolicySettings.RcsPolicyType.VODAFONE_IN_UP;
            }
            return RcsPolicySettings.RcsPolicyType.JIBE_UP;
        } else if (!ImsConstants.RCS_AS.SEC.equals(rcsAs)) {
            return RcsPolicySettings.RcsPolicyType.DEFAULT_UP;
        } else {
            if (mno == Mno.KT) {
                return RcsPolicySettings.RcsPolicyType.KT_UP;
            }
            return RcsPolicySettings.RcsPolicyType.SEC_UP;
        }
    }

    private static RcsPolicySettings.RcsPolicyType getPolicyTypeByRcsProfile(String rcsProfile, Mno mno) {
        if (!rcsProfile.startsWith("UP")) {
            return RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;
        }
        if (mno == Mno.BELL) {
            return RcsPolicySettings.RcsPolicyType.BMC_UP;
        }
        if (mno == Mno.SPRINT) {
            return RcsPolicySettings.RcsPolicyType.SPR_UP;
        }
        if (mno == Mno.VZW) {
            return RcsPolicySettings.RcsPolicyType.VZW_UP;
        }
        if (mno.isVodafone()) {
            return RcsPolicySettings.RcsPolicyType.VODA_UP;
        }
        if (mno.isTmobile() || mno == Mno.TELEKOM_ALBANIA) {
            return RcsPolicySettings.RcsPolicyType.TMOBILE_UP;
        }
        if (mno == Mno.SWISSCOM) {
            return RcsPolicySettings.RcsPolicyType.SWISSCOM_UP;
        }
        if (mno == Mno.CMCC) {
            return RcsPolicySettings.RcsPolicyType.CMCC;
        }
        if (mno.isRjil()) {
            return RcsPolicySettings.RcsPolicyType.RJIL_UP;
        }
        return RcsPolicySettings.RcsPolicyType.DEFAULT_UP;
    }

    private static RcsPolicySettings.RcsPolicyType getPolicyTypeByMno(Mno mno) {
        if (mno.isTmobile() || mno == Mno.TELEKOM_ALBANIA) {
            return RcsPolicySettings.RcsPolicyType.TMOBILE;
        }
        if (mno.isOrange()) {
            return RcsPolicySettings.RcsPolicyType.ORANGE;
        }
        if (mno.isVodafone()) {
            return RcsPolicySettings.RcsPolicyType.VODA;
        }
        if (mno == Mno.ATT) {
            return RcsPolicySettings.RcsPolicyType.ATT;
        }
        if (mno == Mno.TMOUS) {
            return RcsPolicySettings.RcsPolicyType.TMOUS;
        }
        if (mno == Mno.SPRINT) {
            return RcsPolicySettings.RcsPolicyType.SPR;
        }
        if (mno == Mno.USCC) {
            return RcsPolicySettings.RcsPolicyType.USCC;
        }
        if (mno == Mno.VZW) {
            return RcsPolicySettings.RcsPolicyType.VZW;
        }
        if (mno == Mno.BELL) {
            return RcsPolicySettings.RcsPolicyType.BMC;
        }
        if (mno == Mno.CMCC) {
            return RcsPolicySettings.RcsPolicyType.CMCC;
        }
        if (mno == Mno.SINGTEL) {
            return RcsPolicySettings.RcsPolicyType.SINGTEL;
        }
        if (mno == Mno.TCE) {
            return RcsPolicySettings.RcsPolicyType.TCE;
        }
        if (mno == Mno.TELSTRA) {
            return RcsPolicySettings.RcsPolicyType.TELSTRA;
        }
        if (mno.isOneOf(Mno.TELENOR_NORWAY, Mno.TELENOR_SWE)) {
            return RcsPolicySettings.RcsPolicyType.TELENOR;
        }
        if (mno.isOneOf(Mno.TELIA_NORWAY, Mno.TELIA_SWE)) {
            return RcsPolicySettings.RcsPolicyType.TELIA;
        }
        if (mno == Mno.RJIL) {
            return RcsPolicySettings.RcsPolicyType.RJIL;
        }
        return RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;
    }

    public static IMnoStrategy makeInstance(Mno mno, int phoneId, Context context) {
        IMnoStrategy strategy;
        RcsPolicySettings.RcsPolicyType policyType = getPolicyType(mno, phoneId, context);
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[policyType.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
                strategy = new DefaultRCSMnoStrategy(context, phoneId);
                break;
            case 5:
                strategy = new UsccStrategy(context, phoneId);
                break;
            case 6:
                strategy = new CmccStrategy(context, phoneId);
                break;
            case 7:
                strategy = new RjilStrategy(context, phoneId);
                break;
            case 8:
                strategy = new RjilUPStrategy(context, phoneId);
                break;
            case 9:
                strategy = new DTStrategy(context, phoneId);
                break;
            case 10:
            case 11:
            case 12:
                strategy = new EmeiaStrategy(context, phoneId);
                break;
            case 13:
                strategy = new AttStrategy(context, phoneId);
                break;
            case 14:
                strategy = new TmoStrategy(context, phoneId);
                break;
            case 15:
                strategy = new VzwStrategy(context, phoneId);
                break;
            case 16:
                strategy = new SprStrategy(context, phoneId);
                break;
            case 17:
                strategy = new BmcStrategy(context, phoneId);
                break;
            case 18:
                strategy = new BmcUPStrategy(context, phoneId);
                break;
            case 19:
                strategy = new TceStrategy(context, phoneId);
                break;
            case 20:
            case 21:
                strategy = new DefaultUPMnoStrategy(context, phoneId);
                break;
            case 22:
                strategy = new VzwUPStrategy(context, phoneId);
                break;
            case 23:
                strategy = new VodaUPStrategy(context, phoneId);
                break;
            case 24:
            case 25:
            case 26:
            case 27:
                strategy = new JibeUPStrategy(context, phoneId);
                break;
            case 28:
                strategy = new SecUPStrategy(context, phoneId);
                break;
            case 29:
                strategy = new KtUPStrategy(context, phoneId);
                break;
            case 30:
                strategy = new SwisscomUPStrategy(context, phoneId);
                break;
            default:
                policyType = RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;
                strategy = new DefaultRCSMnoStrategy(context, phoneId);
                break;
        }
        strategy.setPolicyType(policyType);
        return strategy;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.MnoStrategyCreator$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType;

        static {
            int[] iArr = new int[RcsPolicySettings.RcsPolicyType.values().length];
            $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType = iArr;
            try {
                iArr[RcsPolicySettings.RcsPolicyType.DEFAULT_RCS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.TELSTRA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.ORANGE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.SINGTEL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.USCC.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.CMCC.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.RJIL.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.RJIL_UP.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.TMOBILE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.VODA.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.TELENOR.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.TELIA.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.ATT.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.TMOUS.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.VZW.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.SPR.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.BMC.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.BMC_UP.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.TCE.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.DEFAULT_UP.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.TMOBILE_UP.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.VZW_UP.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.VODA_UP.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.JIBE_UP.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.ORANGE_UP.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.VODAFONE_IN_UP.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.SPR_UP.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.SEC_UP.ordinal()] = 28;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.KT_UP.ordinal()] = 29;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[RcsPolicySettings.RcsPolicyType.SWISSCOM_UP.ordinal()] = 30;
            } catch (NoSuchFieldError e30) {
            }
        }
    }
}
