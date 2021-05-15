package com.sec.internal.ims.rcs.util;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.imsservice.R;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RcsUtils {
    /* access modifiers changed from: private */
    public static String LOG_TAG = UiUtils.class.getSimpleName();
    private static String TAG = RcsUtils.class.getSimpleName();

    public static class DualRcs {
        private static boolean mIsDualRcsReg = false;
        private static boolean mIsDualRcsSettings = false;

        public static boolean isDualRcsReg() {
            String access$000 = RcsUtils.LOG_TAG;
            Log.i(access$000, "isDualRcsReg: mIsDualRcsReg " + mIsDualRcsReg);
            return mIsDualRcsReg;
        }

        public static boolean isDualRcsSettings() {
            return mIsDualRcsSettings;
        }

        public static void refreshDualRcsReg(Context ctx) {
            refreshDualRcsSettings(ctx);
            if (!SimUtil.isDualIMS()) {
                updateDualRcsRegi(ctx, false);
                return;
            }
            for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
                if (!isRegAllowed(ctx, i)) {
                    updateDualRcsRegi(ctx, false);
                    return;
                }
            }
            updateDualRcsRegi(ctx, true);
        }

        private static void updateDualRcsRegi(Context ctx, boolean isDualRcsReg) {
            if (mIsDualRcsReg != isDualRcsReg) {
                mIsDualRcsReg = isDualRcsReg;
                ctx.getContentResolver().notifyChange(ImsConstants.Uris.RCS_PREFERENCE_PROVIDER_SUPPORT_DUAL_RCS, (ContentObserver) null);
            }
        }

        public static void refreshDualRcsSettings(Context ctx) {
            if (!SimUtil.isDualIMS()) {
                mIsDualRcsSettings = false;
                return;
            }
            for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
                ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
                if (sm == null || !sm.isSimAvailable()) {
                    mIsDualRcsSettings = false;
                    return;
                }
            }
            for (int phoneId2 = 0; phoneId2 < SimUtil.getPhoneCount(); phoneId2++) {
                if (dualRcsPolicyCase(ctx, phoneId2)) {
                    mIsDualRcsSettings = true;
                    return;
                }
            }
            mIsDualRcsSettings = false;
        }

        public static boolean isRegAllowed(Context ctx, int phoneId) {
            if (phoneId == SimUtil.getDefaultPhoneId()) {
                return true;
            }
            if (!SimUtil.isDualIMS()) {
                return false;
            }
            return dualRcsPolicyCase(ctx, phoneId);
        }

        private static boolean dualRcsPolicyCase(Context ctx, int phoneId) {
            int dualRcsPolicy = ImsRegistry.getInt(phoneId, "dual_rcs_policy", 0);
            int counterPhoneId = phoneId == 0 ? 1 : 0;
            if (dualRcsPolicy == 0) {
                return false;
            }
            if (dualRcsPolicy == 1) {
                if (SimUtil.getSimMno(counterPhoneId).equals(SimUtil.getSimMno(phoneId))) {
                    return true;
                }
                return false;
            } else if (dualRcsPolicy == 2) {
                if (!UiUtils.isRcsEnabledinSettings(ctx, counterPhoneId) || ImsRegistry.getRcsProfileType(counterPhoneId).equals(ImsRegistry.getRcsProfileType(phoneId))) {
                    return true;
                }
                return false;
            } else if (dualRcsPolicy == 3) {
                return true;
            } else {
                String access$000 = RcsUtils.LOG_TAG;
                Log.i(access$000, "dualRcsPolicyCase: Invalid policy " + dualRcsPolicy);
                return false;
            }
        }
    }

    public static class UiUtils {
        public static final int RCS_PREF_ALWAYS_ASK = 2;
        public static final int RCS_PREF_ALWAYS_CONNECT = 1;
        public static final int RCS_PREF_NEVER = 0;
        /* access modifiers changed from: private */
        public static boolean mHasRcsUserConsent = false;
        private static AlertDialog mRcsPdnDialog = null;

        public static boolean isMainSwitchVisible(Context ctx, int phoneId) throws RemoteException {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            if (sm == null || !sm.isSimAvailable()) {
                return false;
            }
            boolean mIsVisible = ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.RCS.SHOW_MAIN_SWITCH, false);
            boolean rcsEnabled = isRcsEnabledinSettings(ctx, phoneId);
            String access$000 = RcsUtils.LOG_TAG;
            IMSLog.i(access$000, phoneId, "isMainSwitchVisible: mIsVisible= " + mIsVisible + ", rcsEnabled= " + rcsEnabled);
            if (!mIsVisible || !rcsEnabled) {
                return false;
            }
            return true;
        }

        public static boolean isRcsEnabledinSettings(Context ctx, int phoneId) {
            boolean rcsEnabled = isRcsEnabledInImsSwitch(ctx, phoneId);
            if (!rcsEnabled) {
                return false;
            }
            if (SimUtil.isSupportCarrierVersion(phoneId)) {
                return rcsEnabled;
            }
            String nwCode = OmcCode.getNWCode(phoneId);
            String salesCode = OmcCode.get();
            String access$000 = RcsUtils.LOG_TAG;
            IMSLog.i(access$000, phoneId, "isRcsEnabledinSettings: nwCode = " + nwCode + " , salesCode = " + salesCode);
            return nwCode.equals(salesCode);
        }

        private static boolean isRcsEnabledInImsSwitch(Context ctx, int phoneId) {
            ContentValues mnoInfo;
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            if (sm == null || (mnoInfo = sm.getMnoInfo()) == null) {
                return false;
            }
            boolean isEnableRcs = false;
            if (sm.isLabSimCard() || SimUtil.isSoftphoneEnabled() || CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false)) {
                isEnableRcs = true;
            }
            return isEnableRcs;
        }

        public static boolean isPctMode() {
            if ("1".equals(SemSystemProperties.get("persist.ims.pctmode", ""))) {
                return true;
            }
            return false;
        }

        public static boolean isSameRcsOperator(ImsProfile profile, ImsProfile otherSlotProfile) {
            String mnoName = profile.getMnoName();
            String rcsConfigMark = profile.getRcsConfigMark();
            String otherSlotMnoName = "";
            String otherSlotRcsConfigMark = "";
            if (otherSlotProfile != null) {
                otherSlotRcsConfigMark = otherSlotProfile.getRcsConfigMark();
                otherSlotMnoName = otherSlotProfile.getMnoName();
                if (otherSlotMnoName.length() > 3) {
                    otherSlotMnoName = otherSlotMnoName.substring(0, otherSlotMnoName.length() - 3);
                }
            }
            if (mnoName.length() > 3) {
                mnoName = mnoName.substring(0, mnoName.length() - 3);
            }
            String access$000 = RcsUtils.LOG_TAG;
            Log.i(access$000, "isSameOperatorByProfile: rcsConfigMark = " + rcsConfigMark + ", otherSlotRcsConfigMark = " + otherSlotRcsConfigMark + ", mnoName = " + mnoName + ", otherSlotMnoName = " + otherSlotMnoName);
            if ("".equals(rcsConfigMark) || "".equals(otherSlotRcsConfigMark)) {
                if (!mnoName.equals(otherSlotMnoName)) {
                    return false;
                }
                return true;
            } else if (!rcsConfigMark.equals(otherSlotRcsConfigMark)) {
                return false;
            } else {
                return true;
            }
        }

        public static boolean isRcsEnabledEnrichedCalling(int phoneId) {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            if (sm == null || !sm.isSimAvailable()) {
                return false;
            }
            try {
                return ImsRegistry.isServiceAvailable("ec", -1, phoneId);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean getRcsUserConsent(Context context, ITelephonyManager tm, int phoneId) {
            int roamPref = UserConfiguration.getUserConfig(context, phoneId, "rcs_roaming_pref", 1);
            int homePref = UserConfiguration.getUserConfig(context, phoneId, "rcs_home_pref", 1);
            boolean isRoaming = tm.isNetworkRoaming();
            int rcsConnectPref = isRoaming ? roamPref : homePref;
            String access$000 = RcsUtils.LOG_TAG;
            Log.i(access$000, "getRcsUserConsent: rcsConnectPref = " + rcsConnectPref + " , isRoaming = " + isRoaming);
            if (mHasRcsUserConsent) {
                mHasRcsUserConsent = false;
                return true;
            } else if (rcsConnectPref == 0) {
                if (!SimUtil.getSimMno(phoneId).isKor()) {
                    return false;
                }
                setRcsPrefValue(context, phoneId, isRoaming, 1);
                return true;
            } else if (rcsConnectPref != 2) {
                return true;
            } else {
                if (NetworkUtil.isMobileDataOn(context) && ImsConstants.SystemSettings.AIRPLANE_MODE.get(context, 0) != ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                    showPdnConfirmation(context, isRoaming);
                }
                return false;
            }
        }

        private static DialogInterface.OnClickListener createRcsPdnPrefClickListener(final Context context, final boolean isRoaming, final int pref) {
            return new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    String access$000 = RcsUtils.LOG_TAG;
                    Log.i(access$000, "User preference for RCS PDN: " + pref + " (roaming: " + isRoaming + ")");
                    int phoneId = SimUtil.getDefaultPhoneId();
                    UiUtils.setRcsPrefValue(context, phoneId, isRoaming, pref);
                    if (pref != 0) {
                        boolean unused = UiUtils.mHasRcsUserConsent = true;
                        ImsRegistry.getRegistrationManager().requestTryRegister(phoneId);
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        public static void setRcsPrefValue(Context context, int phoneId, boolean isRoaming, int pref) {
            if (isRoaming) {
                UserConfiguration.setUserConfig(context, phoneId, "rcs_roaming_pref", pref);
            } else {
                UserConfiguration.setUserConfig(context, phoneId, "rcs_home_pref", pref);
            }
        }

        private static void showPdnConfirmation(Context context, boolean isRoaming) {
            String str;
            if (!OmcCode.isKOROmcCode() && !OmcCode.isChinaOmcCode() && !OmcCode.isJPNOmcCode() && !NSDSNamespaces.NSDSSettings.CHANNEL_NAME_TMO.equals(OmcCode.get()) && !"VZW".equals(OmcCode.get()) && !"ATT".equals(OmcCode.get()) && !"APP".equals(OmcCode.get()) && !"BMC".equals(OmcCode.get())) {
                AlertDialog alertDialog = mRcsPdnDialog;
                if (alertDialog == null || !alertDialog.isShowing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, 16974546);
                    builder.setTitle(context.getResources().getString(R.string.dialog_title_rcs_service));
                    if (isRoaming) {
                        str = context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_roaming);
                    } else {
                        str = context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_home);
                    }
                    builder.setMessage(str);
                    builder.setPositiveButton(context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_allow_always), createRcsPdnPrefClickListener(context, isRoaming, 1));
                    builder.setNeutralButton(context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_allow_once), createRcsPdnPrefClickListener(context, isRoaming, 2));
                    builder.setNegativeButton(context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_deny), createRcsPdnPrefClickListener(context, isRoaming, 0));
                    AlertDialog create = builder.create();
                    mRcsPdnDialog = create;
                    create.getWindow().setType(2038);
                    mRcsPdnDialog.getWindow().addFlags(65792);
                    mRcsPdnDialog.setCanceledOnTouchOutside(false);
                    mRcsPdnDialog.setCancelable(false);
                    mRcsPdnDialog.show();
                }
            }
        }
    }

    public static boolean isAutoConfigNeeded(Set<String> serviceSet) {
        Set<String> tmp = new HashSet<>(serviceSet);
        tmp.retainAll(Arrays.asList(ImsProfile.getRcsServiceList()));
        return !tmp.isEmpty();
    }
}
