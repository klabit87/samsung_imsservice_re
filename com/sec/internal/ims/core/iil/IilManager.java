package com.sec.internal.ims.core.iil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.iil.IIilManager;
import com.sec.internal.log.IMSLog;
import java.io.IOException;

public class IilManager extends Handler implements IIilManager {
    static final String DMCONFIG_URI = "content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/";
    protected static final int EVENT_IIL_CONNECTED = 101;
    private static final int EVENT_IMS_READY = 12;
    private static final int EVENT_IMS_SETTING_CHANGED = 5;
    private static final int EVENT_IMS_SETTING_DELAYED = 11;
    private static final int EVENT_IMS_SETTING_REFRESH = 6;
    private static final int EVENT_MODE_CHANGE_DONE = 10;
    protected static final int EVENT_NEW_IPC = 100;
    private static final int EVENT_REGISTRATION_DONE = 1;
    private static final int EVENT_REGISTRATION_E911_DONE = 3;
    private static final int EVENT_REGISTRATION_E911_FAILED = 4;
    private static final int EVENT_REGISTRATION_FAILED = 2;
    private static final int EVENT_REGISTRATION_RETRY_OVER = 7;
    private static final int EVENT_SIM_STATE_CHANGED = 9;
    private static final int EVENT_UPDATE_SSAC_INFO = 14;
    static final int FEATURE_TAG_CS = 1;
    static final int FEATURE_TAG_MMTEL = 16;
    static final int FEATURE_TAG_SMSIP = 2;
    static final int FEATURE_TAG_VIDEO = 8;
    static final int FEATURE_TAG_VOLTE = 4;
    private static final int ISIM_LOADED_BOOTING = 0;
    private static final String LOG_TAG = "IilManager";
    static final int NET_TYPE_BLUETOOTH = 3;
    static final int NET_TYPE_ETHERNET = 4;
    static final int NET_TYPE_MAX = 5;
    static final int NET_TYPE_MOBILE = 0;
    static final int NET_TYPE_WIFI = 1;
    static final int NET_TYPE_WIMAX = 2;
    static final int PREF_REGISTRATION_DONE = 3;
    static final int PREF_SETTING_CHANGED = 2;
    static final int PREF_SETTING_REFRESH = 1;
    private static final int REQUEST_NETWORK_MODE_CHANGE = 5;
    /* access modifiers changed from: private */
    public Context mContext;
    int mEcmp;
    int[] mEcmpByNetType = new int[5];
    int mEpdgMode;
    int[] mEpdgModeByNetType = new int[5];
    int mFeatureMask;
    int[] mFeatureMaskByNetType = new int[5];
    int mFeatureTag;
    private final IImsFramework mImsFramework;
    IilImsPreference mImsPreference;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                String iccState = intent.getStringExtra("ss");
                int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY, 0);
                int i = IilManager.this.mSlotId;
                IMSLog.s(IilManager.LOG_TAG, i, "SimStateChanaged: phoneId: " + phoneId);
                if (phoneId == IilManager.this.mSlotId) {
                    IilManager iilManager = IilManager.this;
                    iilManager.sendMessage(iilManager.obtainMessage(9, iccState));
                }
            }
        }
    };
    private IpcDispatcher mIpcDispatcher;
    int mLimitedMode;
    int[] mLimitedModeByNetType = new int[5];
    private boolean mNeedTwwan911TimerUpdate = true;
    /* access modifiers changed from: private */
    public int mNetworkClass = 0;
    /* access modifiers changed from: private */
    public int mNetworkType = 0;
    int mPdnType;
    private IilPhoneStateListener mPhoneStateListener = new IilPhoneStateListener();
    /* access modifiers changed from: private */
    public ImsRegistration mReg = null;
    int mSlotId = -1;
    int mSrvccVersion;
    private int mSubId = -1;

    public IilManager(Context context, int slotId, IImsFramework imsFramework) {
        IMSLog.s(LOG_TAG, slotId, LOG_TAG);
        this.mContext = context;
        this.mImsFramework = imsFramework;
        this.mFeatureMask = 0;
        this.mPdnType = 0;
        this.mFeatureTag = 0;
        this.mEcmp = 0;
        this.mLimitedMode = 0;
        this.mEpdgMode = 0;
        this.mSlotId = slotId;
        this.mSrvccVersion = 0;
        for (int i = 0; i < 5; i++) {
            this.mFeatureMaskByNetType[i] = 0;
            this.mEcmpByNetType[i] = 0;
            this.mLimitedModeByNetType[i] = 0;
            this.mEpdgModeByNetType[i] = 0;
        }
        IpcDispatcher ipcDispatcher = new IpcDispatcher(this.mSlotId);
        this.mIpcDispatcher = ipcDispatcher;
        ipcDispatcher.setRegistrant(100, this);
        this.mIpcDispatcher.setRegistrantForIilConnected(101, this);
        this.mIpcDispatcher.initDipatcher();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        ContentObserver imsSettingsObserver = new ImsSettingsObserver(this);
        this.mContext.getContentResolver().registerContentObserver(GlobalSettingsConstants.CONTENT_URI, false, imsSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Uri.parse(DMCONFIG_URI), false, imsSettingsObserver);
    }

    private class ImsSettingsObserver extends ContentObserver {
        public ImsSettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            IMSLog.i(IilManager.LOG_TAG, "ImsSettings updated");
            IilManager.this.removeMessages(5);
            IilManager.this.sendEmptyMessage(5);
        }
    }

    private void handleNewIpc(IpcMessage ipcMsg) {
        if (ipcMsg.getMainCmd() != 112) {
            return;
        }
        if (ipcMsg.getSubCmd() == 11) {
            handleSetDeregistration((IilIpcMessage) ipcMsg);
        } else if (ipcMsg.getSubCmd() == 14) {
            handleSSACInfo((IilIpcMessage) ipcMsg);
        } else if (ipcMsg.getSubCmd() == 16) {
            handleImsSupportStateChanged((IilIpcMessage) ipcMsg);
        } else if (ipcMsg.getSubCmd() == 17) {
            handleIsimLoaded((IilIpcMessage) ipcMsg);
        } else if (ipcMsg.getSubCmd() == 6) {
            if (ipcMsg.getCmdType() == 2) {
                handleGetImsPreference();
            }
        } else if (ipcMsg.getSubCmd() == 21) {
            handleSetPreferredNetworkType((IilIpcMessage) ipcMsg);
        } else if (ipcMsg.getSubCmd() == 22) {
            handleSipSuspend((IilIpcMessage) ipcMsg);
        }
    }

    public void onIilConnected() {
        int i;
        IMSLog.i(LOG_TAG, this.mSlotId, "onIilConnected");
        for (int netType = 0; netType < 5; netType++) {
            IMSLog.s(LOG_TAG, this.mSlotId, "IMS registraton at onIilConnected() : mFeatureMaskByNetType[" + netType + "]=" + this.mFeatureMaskByNetType[netType]);
            if (this.mFeatureMaskByNetType[netType] > 0) {
                IpcDispatcher ipcDispatcher = this.mIpcDispatcher;
                if (this.mLimitedModeByNetType[netType] == 0) {
                    i = 1;
                } else {
                    i = 2;
                }
                if (!ipcDispatcher.sendMessage(IilIpcMessage.encodeImsRegisgtrationInfo(i, (this.mFeatureMaskByNetType[netType] & 1) > 0, (2 & this.mFeatureMaskByNetType[netType]) > 0, (this.mFeatureMaskByNetType[netType] & 4) > 0, (this.mFeatureMaskByNetType[netType] & 8) > 0, (this.mFeatureMaskByNetType[netType] & 32) > 0, netType, this.mFeatureTag, this.mEcmpByNetType[netType], this.mEpdgModeByNetType[netType], 0, 0, (String) null, 0))) {
                    IMSLog.s(LOG_TAG, this.mSlotId, "send IMS registraton info failed at onIilConnected() :" + netType);
                }
            }
        }
    }

    public void handleSetDeregistration(IilIpcMessage msg) {
        this.mIpcDispatcher.sendGeneralResponse(true, (IpcMessage) msg);
        byte[] ipcBody = msg.getBody();
        int i = this.mSlotId;
        IMSLog.s(LOG_TAG, i, "de-reg reason : " + ipcBody[0]);
        this.mImsFramework.sendDeregister(ipcBody[0], this.mSlotId);
    }

    public void handleGetImsPreference() {
        IMSLog.s(LOG_TAG, this.mSlotId, "handleGetImsPreference");
        UpdateImsPreference();
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsPreferenceResp(this.mImsPreference));
    }

    public void handleSSACInfo(IilIpcMessage msg) {
        IMSLog.s(LOG_TAG, this.mSlotId, "handleSSACInfo()");
        byte[] ipcBody = msg.getBody();
        byte voiceFactor = ipcBody[0];
        int voiceTime = (((ipcBody[2] & 255) << 8) + ((ipcBody[1] & 255) << 0)) * 1000;
        byte videoFactor = ipcBody[3];
        int videoTime = (((ipcBody[5] & 255) << 8) + ((ipcBody[4] & 255) << 0)) * 1000;
        removeMessages(14);
        try {
            this.mImsFramework.getServiceModuleManager().getVolteServiceModule().updateSSACInfo(voiceFactor, voiceTime, videoFactor, videoTime);
        } catch (NullPointerException e) {
            IMSLog.e(LOG_TAG, this.mSlotId, "handleSSACInfo: NPE - resend SSAC to VSM");
            sendMessageDelayed(obtainMessage(14, msg), 500);
        }
    }

    public void handleImsSupportStateChanged(IilIpcMessage msg) {
        this.mIpcDispatcher.sendGeneralResponse(true, (IpcMessage) msg);
        byte[] ipcBody = msg.getBody();
        int i = this.mSlotId;
        IMSLog.s(LOG_TAG, i, "handleImsSupportStateChanged() reason: " + ipcBody[0] + "state: " + ipcBody[1]);
    }

    public void handleIsimLoaded(IilIpcMessage msg) {
        byte isRefreshed = msg.getBody()[0];
        int i = this.mSlotId;
        IMSLog.s(LOG_TAG, i, "handleIsimLoaded() isRefreshed: " + isRefreshed);
        if (isRefreshed == 0) {
            this.mImsFramework.setIsimLoaded();
        }
    }

    public void handleSetPreferredNetworkType(IilIpcMessage msg) {
        this.mIpcDispatcher.sendGeneralResponse(true, (IpcMessage) msg);
        byte[] ipcBody = msg.getBody();
        byte reason = ipcBody[0];
        byte netType = ipcBody[1];
        int i = this.mSlotId;
        IMSLog.i(LOG_TAG, i, "handleSetPreferredNetworkType reason: " + reason + " new NW type: " + netType);
        if (needSkipDeregister(netType)) {
            ImsRegistration imsRegistration = this.mReg;
            if (imsRegistration != null) {
                sendMessage(obtainMessage(10, Integer.valueOf(imsRegistration.getNetworkType())));
            } else {
                IMSLog.s(LOG_TAG, this.mSlotId, "mReg = null, do nothing!");
            }
        } else {
            this.mImsFramework.sendDeregister(5, this.mSlotId);
        }
    }

    private boolean needSkipDeregister(int netType) {
        Mno mno = SimUtil.getSimMno(this.mSlotId);
        ImsRegistration imsRegistration = this.mReg;
        if (imsRegistration != null && imsRegistration.getEpdgStatus()) {
            return true;
        }
        if (mno.isUSA()) {
            return false;
        }
        switch (netType) {
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 15:
            case 17:
            case 19:
            case 20:
            case 22:
                return true;
            default:
                if (netType >= 23) {
                    return true;
                }
                return false;
        }
    }

    public void handleSipSuspend(IilIpcMessage msg) {
        byte[] ipcBody = msg.getBody();
        boolean z = false;
        byte cause = ipcBody[0];
        byte action = ipcBody[1];
        byte srcRat = ipcBody[2];
        byte tgtRat = ipcBody[3];
        if (cause != 1) {
            return;
        }
        if (srcRat != 3 || tgtRat != 6) {
            if (srcRat != 6 || tgtRat != 3) {
                IImsFramework iImsFramework = this.mImsFramework;
                if (action == 1) {
                    z = true;
                }
                iImsFramework.suspendRegister(z, this.mSlotId);
            }
        }
    }

    public void onReceiveRegistrationRetryOver() {
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsRetryOverNoti(5, false, false, false, false, false, 0, 0));
    }

    public void onReceiveImsSettingChange() {
        IMSLog.s(LOG_TAG, this.mSlotId, "onReceiveImsSettingChange");
        this.mNeedTwwan911TimerUpdate = true;
        UpdateImsPreference();
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsPreferenceNoti(this.mImsPreference, 2));
    }

    public void onReceiveImsSettingRefresh() {
        IMSLog.s(LOG_TAG, this.mSlotId, "onReceiveImsSettingRefresh");
        UpdateImsPreference();
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsPreferenceNoti(this.mImsPreference, 1));
    }

    public void onReceiveSimStateChange(String simState) {
        int i = this.mSlotId;
        IMSLog.s(LOG_TAG, i, "onReceiveSimStateChange() : simState=" + simState);
        if (SimUtil.getSimMno(this.mSlotId) == Mno.ATT && IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(simState)) {
            onReceiveImsSettingRefresh();
        }
        if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(simState)) {
            this.mNeedTwwan911TimerUpdate = true;
        }
    }

    public void onReceiveModeChangeDone() {
        IMSLog.s(LOG_TAG, this.mSlotId, "onReceiveModeChangeDone()");
        this.mIpcDispatcher.sendMessage(IilIpcMessage.ImsChangePreferredNetwork());
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveRegistrationInfo() {
        /*
            r4 = this;
            r0 = 0
            int r1 = r4.mPdnType
            if (r1 == 0) goto L_0x002a
            r2 = 1
            if (r1 == r2) goto L_0x0028
            r2 = 6
            if (r1 == r2) goto L_0x0026
            r2 = 7
            if (r1 == r2) goto L_0x0024
            r2 = 9
            if (r1 == r2) goto L_0x0022
            r2 = 11
            if (r1 == r2) goto L_0x002a
            int r1 = r4.mSlotId
            java.lang.String r2 = "IilManager"
            java.lang.String r3 = "saveRegistrationInfo : invalid network type"
            com.sec.internal.log.IMSLog.s(r2, r1, r3)
            r0 = -1
            goto L_0x002c
        L_0x0022:
            r0 = 4
            goto L_0x002c
        L_0x0024:
            r0 = 3
            goto L_0x002c
        L_0x0026:
            r0 = 2
            goto L_0x002c
        L_0x0028:
            r0 = 1
            goto L_0x002c
        L_0x002a:
            r0 = 0
        L_0x002c:
            r1 = -1
            if (r0 == r1) goto L_0x0047
            int[] r1 = r4.mFeatureMaskByNetType
            int r2 = r4.mFeatureMask
            r1[r0] = r2
            int[] r1 = r4.mEcmpByNetType
            int r2 = r4.mEcmp
            r1[r0] = r2
            int[] r1 = r4.mLimitedModeByNetType
            int r2 = r4.mLimitedMode
            r1[r0] = r2
            int[] r1 = r4.mEpdgModeByNetType
            int r2 = r4.mEpdgMode
            r1[r0] = r2
        L_0x0047:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.iil.IilManager.saveRegistrationInfo():void");
    }

    public static int featureTagToInt(String featureTag) {
        int tag = 0;
        if (TextUtils.isEmpty(featureTag)) {
            return 0;
        }
        if (featureTag.indexOf("cs") >= 0) {
            tag = 0 | 1;
        }
        if (featureTag.indexOf("smsip") >= 0) {
            tag |= 2;
        }
        if (featureTag.indexOf("volte") >= 0) {
            tag |= 4;
        }
        if (featureTag.indexOf(TMOConstants.CallLogTypes.VIDEO) >= 0) {
            tag |= 8;
        }
        if (featureTag.indexOf("mmtel") >= 0) {
            return tag | 16;
        }
        return tag;
    }

    public static String featureMaskToString(int featureMask) {
        String rtString = "";
        if ((featureMask & 1) == 1) {
            rtString = rtString + "VOLTE";
        }
        if ((featureMask & 2) == 2) {
            if (!rtString.isEmpty()) {
                rtString = rtString + ", ";
            }
            rtString = rtString + "SMSIP";
        }
        if ((featureMask & 4) == 4) {
            if (!rtString.isEmpty()) {
                rtString = rtString + ", ";
            }
            rtString = rtString + "RCS";
        }
        if ((featureMask & 8) == 8) {
            if (!rtString.isEmpty()) {
                rtString = rtString + ", ";
            }
            rtString = rtString + "PSVT";
        }
        if ((featureMask & 32) != 32) {
            return rtString;
        }
        if (!rtString.isEmpty()) {
            rtString = rtString + ", ";
        }
        return rtString + "CDPN";
    }

    private void UpdateImsServiceState() {
        UpdateImsPreference();
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsPreferenceNoti(this.mImsPreference, 2));
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00f2  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0141  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01ba  */
    /* JADX WARNING: Removed duplicated region for block: B:55:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void UpdateImsPreference() {
        /*
            r19 = this;
            r1 = r19
            java.lang.String r2 = "mmtel"
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference
            if (r0 != 0) goto L_0x000f
            com.sec.internal.ims.core.iil.IilImsPreference r0 = new com.sec.internal.ims.core.iil.IilImsPreference
            r0.<init>()
            r1.mImsPreference = r0
        L_0x000f:
            java.lang.String r3 = r19.getSmsFormat()
            int r0 = r1.mSlotId
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "UpdateImsPreference: SmsFormat="
            r4.append(r5)
            r4.append(r3)
            java.lang.String r4 = r4.toString()
            java.lang.String r5 = "IilManager"
            com.sec.internal.log.IMSLog.i(r5, r0, r4)
            boolean r0 = android.text.TextUtils.isEmpty(r3)
            r4 = 0
            r6 = 1
            if (r0 != 0) goto L_0x005a
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference     // Catch:{ NumberFormatException -> 0x003e }
            int r7 = java.lang.Integer.parseInt(r3)     // Catch:{ NumberFormatException -> 0x003e }
            byte r7 = (byte) r7     // Catch:{ NumberFormatException -> 0x003e }
            r0.setSmsFormat(r7)     // Catch:{ NumberFormatException -> 0x003e }
            goto L_0x005a
        L_0x003e:
            r0 = move-exception
            java.lang.String r7 = "3GPP"
            boolean r7 = r7.equals(r3)
            if (r7 == 0) goto L_0x004d
            com.sec.internal.ims.core.iil.IilImsPreference r7 = r1.mImsPreference
            r7.setSmsFormat(r4)
            goto L_0x005a
        L_0x004d:
            java.lang.String r7 = "3GPP2"
            boolean r7 = r7.equals(r3)
            if (r7 == 0) goto L_0x005a
            com.sec.internal.ims.core.iil.IilImsPreference r7 = r1.mImsPreference
            r7.setSmsFormat(r6)
        L_0x005a:
            int r0 = r1.mSlotId
            com.sec.internal.constants.Mno r7 = com.sec.internal.helper.SimUtil.getSimMno(r0)
            boolean r0 = r19.getSmsOverIp()
            r8 = 3
            if (r0 != 0) goto L_0x007f
            com.sec.internal.constants.Mno[] r0 = new com.sec.internal.constants.Mno[r8]
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.CTC
            r0[r4] = r9
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.CTCMO
            r0[r6] = r9
            r9 = 2
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.RJIL
            r0[r9] = r10
            boolean r0 = r7.isOneOf(r0)
            if (r0 == 0) goto L_0x007d
            goto L_0x007f
        L_0x007d:
            r0 = r4
            goto L_0x0080
        L_0x007f:
            r0 = r6
        L_0x0080:
            r9 = r0
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference
            if (r9 == 0) goto L_0x0087
            r10 = r6
            goto L_0x0088
        L_0x0087:
            r10 = r4
        L_0x0088:
            r0.setSmsOverIms(r10)
            com.sec.internal.interfaces.ims.IImsFramework r0 = r1.mImsFramework
            int r10 = r1.mSlotId
            java.lang.String r11 = "sms_write_uicc"
            java.lang.String r12 = "0"
            java.lang.String r0 = r0.getString(r10, r11, r12)
            java.lang.String r10 = "1"
            boolean r10 = r10.equals(r0)
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference
            r0.setSmsWriteUicc(r10)
            com.sec.internal.interfaces.ims.IImsFramework r0 = r1.mImsFramework
            int r11 = r1.mSlotId
            java.lang.String r12 = "voice_domain_pref_eutran"
            int r8 = r0.getInt(r11, r12, r8)
            com.sec.internal.interfaces.ims.IImsFramework r0 = r1.mImsFramework
            int r11 = r1.mSlotId
            java.lang.String r12 = "voice_domain_pref_utran"
            int r11 = r0.getInt(r11, r12, r6)
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference
            byte r12 = (byte) r8
            r0.setEutranDomain(r12)
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference
            byte r12 = (byte) r11
            r0.setUtranDomain(r12)
            com.sec.internal.interfaces.ims.IImsFramework r0 = r1.mImsFramework
            int r12 = r1.mSlotId
            java.lang.String r13 = "ss_domain_setting"
            java.lang.String r14 = "PS"
            java.lang.String r12 = r0.getString(r12, r13, r14)
            byte r13 = r1.convertSsDomainToByte(r12)
            r0 = -1
            if (r13 == r0) goto L_0x00de
            com.sec.internal.ims.core.iil.IilImsPreference r15 = r1.mImsPreference
            r15.setSsDomain(r13)
        L_0x00de:
            com.sec.internal.interfaces.ims.IImsFramework r15 = r1.mImsFramework
            int r4 = r1.mSlotId
            java.lang.String r6 = "ussd_domain_setting"
            java.lang.String r0 = "CS"
            java.lang.String r4 = r15.getString(r4, r6, r0)
            byte r6 = r1.convertUssdDomainToByte(r4)
            r15 = -1
            if (r6 == r15) goto L_0x00f7
            com.sec.internal.ims.core.iil.IilImsPreference r15 = r1.mImsPreference
            r15.setUssdDomain(r6)
        L_0x00f7:
            com.sec.internal.interfaces.ims.IImsFramework r15 = r1.mImsFramework
            r16 = r3
            int r3 = r1.mSlotId
            r17 = r4
            java.lang.String r4 = "emergency_domain_setting"
            java.lang.String r3 = r15.getString(r3, r4, r14)
            java.lang.String r4 = "IMS"
            boolean r4 = r4.equalsIgnoreCase(r3)
            if (r4 != 0) goto L_0x0141
            boolean r4 = r14.equalsIgnoreCase(r3)
            if (r4 == 0) goto L_0x0114
            goto L_0x0141
        L_0x0114:
            java.lang.String r4 = "CSFB"
            boolean r4 = r4.equalsIgnoreCase(r3)
            if (r4 != 0) goto L_0x013a
            boolean r0 = r0.equalsIgnoreCase(r3)
            if (r0 == 0) goto L_0x0123
            goto L_0x013a
        L_0x0123:
            int r0 = r1.mSlotId
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r14 = "Invalid emergencyDomainPref="
            r4.append(r14)
            r4.append(r3)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.e(r5, r0, r4)
            goto L_0x0147
        L_0x013a:
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference
            r4 = 1
            r0.setEccPreference(r4)
            goto L_0x0147
        L_0x0141:
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference
            r4 = 0
            r0.setEccPreference(r4)
        L_0x0147:
            com.sec.internal.interfaces.ims.IImsFramework r0 = r1.mImsFramework
            int r4 = r1.mSlotId
            java.lang.String r14 = "ss_csfb_with_imserror"
            r15 = 1
            boolean r4 = r0.getBoolean(r4, r14, r15)
            com.sec.internal.ims.core.iil.IilImsPreference r0 = r1.mImsPreference
            r0.setSsCsfb(r4)
            com.sec.internal.interfaces.ims.IImsFramework r0 = r1.mImsFramework     // Catch:{ RemoteException -> 0x017a }
            r14 = 13
            int r15 = r1.mSlotId     // Catch:{ RemoteException -> 0x017a }
            boolean r0 = r0.isServiceAvailable(r2, r14, r15)     // Catch:{ RemoteException -> 0x017a }
            com.sec.internal.interfaces.ims.IImsFramework r14 = r1.mImsFramework     // Catch:{ RemoteException -> 0x017a }
            int r15 = r1.mSlotId     // Catch:{ RemoteException -> 0x017a }
            r18 = r3
            r3 = 18
            boolean r2 = r14.isServiceAvailable(r2, r3, r15)     // Catch:{ RemoteException -> 0x0178 }
            com.sec.internal.ims.core.iil.IilImsPreference r3 = r1.mImsPreference     // Catch:{ RemoteException -> 0x0178 }
            byte r14 = r1.convertSupportTypeToByte(r0, r2)     // Catch:{ RemoteException -> 0x0178 }
            r3.setImsSupportType(r14)     // Catch:{ RemoteException -> 0x0178 }
            goto L_0x0197
        L_0x0178:
            r0 = move-exception
            goto L_0x017d
        L_0x017a:
            r0 = move-exception
            r18 = r3
        L_0x017d:
            int r2 = r1.mSlotId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r14 = "UpdateImsPreference: "
            r3.append(r14)
            java.lang.String r14 = r0.getMessage()
            r3.append(r14)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.e(r5, r2, r3)
        L_0x0197:
            com.sec.internal.interfaces.ims.IImsFramework r0 = r1.mImsFramework
            int r2 = r1.mSlotId
            r3 = 10
            java.lang.String r5 = "srvcc_version"
            int r0 = r0.getInt(r2, r5, r3)
            r1.mSrvccVersion = r0
            com.sec.internal.ims.core.iil.IilImsPreference r2 = r1.mImsPreference
            byte r0 = (byte) r0
            r2.setSrvccVersion(r0)
            r2 = 1
            boolean r0 = r1.getRoamingSupportValueforVolte(r2)
            com.sec.internal.ims.core.iil.IilImsPreference r2 = r1.mImsPreference
            r2.setSupportVolteRoaming(r0)
            boolean r2 = r1.mNeedTwwan911TimerUpdate
            if (r2 == 0) goto L_0x01bd
            r19.updateTwwan911Timer()
        L_0x01bd:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.iil.IilManager.UpdateImsPreference():void");
    }

    public boolean getRoamingSupportValueforVolte(boolean defval) {
        ImsProfile profile = this.mImsFramework.getRegistrationManager().getImsProfile(this.mSlotId, ImsProfile.PROFILE_TYPE.VOLTE);
        if (profile == null || !profile.hasService("mmtel")) {
            return defval;
        }
        return profile.isAllowedOnRoaming();
    }

    private byte convertSsDomainToByte(String ssDomain) {
        if ("PS".equalsIgnoreCase(ssDomain) || "PS_ALWAYS".equalsIgnoreCase(ssDomain)) {
            return 0;
        }
        if ("CS".equalsIgnoreCase(ssDomain) || "CS_ALWAYS".equalsIgnoreCase(ssDomain)) {
            return 1;
        }
        if (DiagnosisConstants.PSCI_KEY_CALL_BEARER.equalsIgnoreCase(ssDomain) || "PS_ONLY_VOLTEREGIED".equalsIgnoreCase(ssDomain)) {
            return 2;
        }
        if ("PS_ONLY_PSREGIED".equalsIgnoreCase(ssDomain)) {
            return 3;
        }
        int i = this.mSlotId;
        IMSLog.e(LOG_TAG, i, "Invalid value: " + ssDomain + " from GENERAL.FIELD for SS_DOMAIN_SETTING");
        return -1;
    }

    private byte convertUssdDomainToByte(String ussdDomain) {
        if ("PS".equalsIgnoreCase(ussdDomain)) {
            return 0;
        }
        if ("CS".equalsIgnoreCase(ussdDomain)) {
            return 1;
        }
        if (DiagnosisConstants.PSCI_KEY_CALL_BEARER.equalsIgnoreCase(ussdDomain)) {
            return 2;
        }
        int i = this.mSlotId;
        IMSLog.e(LOG_TAG, i, "Invalid UssdDomain=" + ussdDomain);
        return -1;
    }

    private byte convertSupportTypeToByte(boolean supportVolte, boolean supportVowifi) {
        if (supportVolte && supportVowifi) {
            return 3;
        }
        if (supportVolte) {
            return 1;
        }
        if (supportVowifi) {
            return 2;
        }
        return 0;
    }

    public int getSrvccVersion() {
        return this.mSrvccVersion;
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public boolean getSmsOverIp() {
        Cursor cursor;
        try {
            cursor = this.mContext.getContentResolver().query(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/sms_over_ip_network_indication", this.mSlotId), (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    boolean equals = "1".equals(cursor.getString(1));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return equals;
                }
            }
            if (cursor == null) {
                return false;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            int i = this.mSlotId;
            IMSLog.e(LOG_TAG, i, "getSmsOverIp: Exception : " + e.getMessage());
            return false;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public String getSmsFormat() {
        Cursor cursor;
        String smsFormat = "3GPP";
        try {
            cursor = this.mContext.getContentResolver().query(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/SMS_FORMAT", this.mSlotId), (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    smsFormat = cursor.getString(1);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return smsFormat;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            int i = this.mSlotId;
            IMSLog.e(LOG_TAG, i, "getSmsFormat: Exception : " + e.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return smsFormat;
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    private void updateTwwan911Timer() {
        Cursor cursor;
        int twwan911Timer = 40;
        try {
            cursor = this.mContext.getContentResolver().query(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/TWWAN_911_FAIL_TIMER", this.mSlotId), (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex("VALUE");
                    if (index >= 0) {
                        twwan911Timer = cursor.getInt(index);
                        IMSLog.i(LOG_TAG, "Read Twwan911 timer from index(" + index + "): " + twwan911Timer);
                    } else {
                        twwan911Timer = cursor.getInt(1);
                        IMSLog.i(LOG_TAG, "Read Twwan911 timer from default index(1): " + twwan911Timer);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            twwan911Timer = 40;
            IMSLog.e(LOG_TAG, "Twwan911 timer read fail: " + e);
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        if (twwan911Timer < 0) {
            IMSLog.e(LOG_TAG, "Use default Twwan911 timer because database has wrong value: " + twwan911Timer);
            twwan911Timer = 40;
        }
        SystemProperties.set("ril.twwan911Timer", String.valueOf(twwan911Timer));
        IMSLog.i(LOG_TAG, "Twwan911 timer update complete: " + SystemProperties.get("ril.twwan911Timer"));
        this.mNeedTwwan911TimerUpdate = false;
        return;
        throw th;
    }

    public void handleMessage(Message msg) {
        int i = this.mSlotId;
        IMSLog.s(LOG_TAG, i, "handleMessage: event " + msg.what);
        int i2 = msg.what;
        if (i2 == 14) {
            handleSSACInfo((IilIpcMessage) msg.obj);
        } else if (i2 == 100) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                handleNewIpc((IpcMessage) ar.result);
            } else if (ar.exception instanceof IOException) {
                IMSLog.e(LOG_TAG, this.mSlotId, "RILD crashed. restarting IMS.");
            } else {
                int i3 = this.mSlotId;
                IMSLog.e(LOG_TAG, i3, "Exception processing IPC data. Exception:" + ar.exception);
            }
        } else if (i2 != 101) {
            switch (i2) {
                case 1:
                    onRegistrationDone((Registration) msg.obj);
                    return;
                case 2:
                    onRegistrationFailed((Registration) msg.obj);
                    return;
                case 3:
                    onEmergencyRegistrationDone(msg.arg1);
                    return;
                case 4:
                    onEmergencyRegistrationFailed();
                    return;
                case 5:
                    onReceiveImsSettingChange();
                    return;
                case 6:
                    onReceiveImsSettingRefresh();
                    return;
                case 7:
                    onReceiveRegistrationRetryOver();
                    return;
                default:
                    switch (i2) {
                        case 9:
                            onReceiveSimStateChange((String) msg.obj);
                            return;
                        case 10:
                            onReceiveModeChangeDone();
                            return;
                        case 11:
                            UpdateImsPreference();
                            return;
                        case 12:
                            UpdateImsServiceState();
                            return;
                        default:
                            return;
                    }
            }
        } else {
            onIilConnected();
        }
    }

    /* access modifiers changed from: private */
    public void updateFeature(ImsRegistration reg) {
        if (reg != null) {
            int feature = 0;
            ImsProfile profile = reg.getImsProfile();
            Mno mno = SimUtil.getSimMno(this.mSlotId);
            if ((mno.isOneOf(Mno.CMCC) || mno.isKor()) && !reg.hasVolteService()) {
                int i = this.mSlotId;
                IMSLog.s(LOG_TAG, i, "updateFeature: this is not Volte registration " + reg.getServices());
                return;
            }
            int i2 = this.mSlotId;
            IMSLog.i(LOG_TAG, i2, "updateFeature: service=" + reg.getServices() + "mNetworkType=" + this.mNetworkType);
            if (reg.hasService("mmtel") && isServiceAvailable()) {
                feature = 0 | 1;
            }
            if (reg.hasService("mmtel-video") && (mno.isKor() || isServiceAvailable())) {
                feature |= 8;
            }
            if (reg.hasService("smsip")) {
                if (reg.getImsProfile().getMnoName().toUpperCase().contains("ORANGE_FR")) {
                    feature |= 2;
                } else if (!disallowReregistration(mno)) {
                    feature |= 2;
                } else if (isServiceAvailable()) {
                    feature |= 2;
                }
            }
            if (reg.hasService("cdpn")) {
                feature |= 32;
            }
            int networkType = reg.getNetworkType();
            int ecmpStatus = reg.getEcmpStatus();
            boolean epdgStatus = reg.getEpdgStatus();
            Registration regEvent = new Registration(feature, networkType, ecmpStatus, 0, epdgStatus ? 1 : 0, reg.getRegiRat());
            if (feature != 0) {
                updateFeatureWithMmtel(reg, regEvent, feature);
            } else {
                updateFeatureWithoutMmtel(regEvent, profile);
            }
        }
    }

    private void updateFeatureWithMmtel(ImsRegistration reg, Registration regEvent, int feature) {
        if (reg.getImsProfile().hasEmergencySupport()) {
            sendMessage(obtainMessage(3, Integer.valueOf(reg.getNetworkType())));
            return;
        }
        String imsi = SimManagerFactory.getImsiFromPhoneId(this.mSlotId);
        int limitedMode = 0;
        if (SimUtil.getSimMno(this.mSlotId) == Mno.VZW && "0".equals(SystemProperties.get(ImsConstants.SystemProperties.GCF_MODE_PROPERTY, "0"))) {
            limitedMode = reg.isImsiBased(imsi);
        }
        regEvent.setLimitedMode((int) limitedMode);
        if ((feature & 1) == 0 && (feature & 8) == 8) {
            regEvent.setFeatureTags("cs");
        }
        ImsUri imsUri = reg.getRegisteredImpu();
        if (imsUri != null) {
            regEvent.setImpu(imsUri.toString());
        }
        sendMessage(obtainMessage(1, regEvent));
    }

    private void updateFeatureWithoutMmtel(Registration regEvent, ImsProfile profile) {
        IMSLog.s(LOG_TAG, this.mSlotId, "onRegistered: Registration without MMTEL service");
        if (profile.hasEmergencySupport()) {
            onEmergencyRegistrationFailed();
            return;
        }
        regEvent.setSipError(200);
        regEvent.setDeregiReasonCode(0);
        sendMessage(obtainMessage(2, regEvent));
    }

    /* access modifiers changed from: private */
    public boolean disallowReregistration(Mno mno) {
        return mno.isOneOf(Mno.TMOBILE_CZ, Mno.TMOBILE_PL, Mno.TMOBILE_HUNGARY, Mno.TMOBILE_GREECE, Mno.TMOBILE_NED, Mno.TMOBILE_CROATIA, Mno.TMOBILE_SLOVAKIA, Mno.TMOBILE, Mno.DIGI, Mno.ORANGE, Mno.ORANGE_POLAND, Mno.BOG, Mno.TELECOM_ITALY, Mno.TELEFONICA_UK, Mno.VODAFONE_NEWZEALAND, Mno.WINDTRE);
    }

    private boolean isServiceAvailable() {
        int i;
        Mno mno = SimUtil.getSimMno(this.mSlotId);
        if (mno.isOneOf(Mno.ATT) || disallowReregistration(mno)) {
            if (SimUtil.isSoftphoneEnabled() || (i = this.mNetworkType) == 13 || i == 18) {
                return true;
            }
            return false;
        } else if (!mno.isKor() || this.mNetworkType == 13) {
            return true;
        } else {
            return false;
        }
    }

    private void onRegistrationDone(Registration registrationinfo) {
        int errorCode;
        int i;
        removeMessages(1);
        this.mFeatureMask = registrationinfo.getFeatureMask();
        this.mPdnType = registrationinfo.getNetworkType();
        this.mEcmp = registrationinfo.getEcmpMode();
        this.mLimitedMode = registrationinfo.getLimitedMode();
        this.mEpdgMode = registrationinfo.getEpdgMode();
        String featureTag = registrationinfo.getFeatureTags();
        String errorMessage = registrationinfo.getErrorMessage();
        String impu = registrationinfo.getImpu();
        int rat = registrationinfo.getRegiRat();
        this.mFeatureTag = featureTagToInt(featureTag);
        if (!TextUtils.isEmpty(errorMessage)) {
            errorCode = 1606;
        } else {
            errorCode = 0;
        }
        IMSLog.s(LOG_TAG, this.mSlotId, "onRegistrationDone - FeatureMask: " + featureMaskToString(this.mFeatureMask) + "(" + this.mFeatureMask + "), PDN type: " + this.mPdnType + ", FeatureTag: " + featureTag + "(" + this.mFeatureTag + "), Ecmp: " + this.mEcmp + ", LimitedMode: " + this.mLimitedMode + ", EpdgMode: " + this.mEpdgMode + ", errorMessage: " + errorMessage + "(" + errorCode + ")");
        saveRegistrationInfo();
        IpcDispatcher ipcDispatcher = this.mIpcDispatcher;
        if (this.mLimitedMode == 0) {
            i = 1;
        } else {
            i = 2;
        }
        int i2 = errorCode;
        if (!ipcDispatcher.sendMessage(IilIpcMessage.encodeImsRegisgtrationInfo(i, (this.mFeatureMask & 1) > 0, (2 & this.mFeatureMask) > 0, (this.mFeatureMask & 4) > 0, (this.mFeatureMask & 8) > 0, (this.mFeatureMask & 32) > 0, this.mPdnType, this.mFeatureTag, this.mEcmp, this.mEpdgMode, errorCode, 0, impu, rat))) {
            sendMessageDelayed(obtainMessage(1, registrationinfo), 1000);
            return;
        }
        Registration registration = registrationinfo;
        UpdateImsPreference();
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsPreferenceNoti(this.mImsPreference, 3));
    }

    private void onRegistrationFailed(Registration deregInfo) {
        this.mFeatureMask = 0;
        this.mPdnType = deregInfo.getNetworkType();
        this.mFeatureTag = 0;
        this.mEcmp = 0;
        this.mLimitedMode = 0;
        IMSLog.s(LOG_TAG, this.mSlotId, "onRegistrationFailed");
        saveRegistrationInfo();
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsRegisgtrationInfo(0, false, false, false, false, false, this.mPdnType, 0, 0, 0, deregInfo.getDeregiReasonCode(), deregInfo.getSipError(), (String) null, deregInfo.getRegiRat()));
    }

    private void onEmergencyRegistrationDone(int pdnType) {
        IMSLog.s(LOG_TAG, this.mSlotId, "onRegistrationDone (Emergency)");
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsRegisgtrationInfo(4, false, false, false, false, false, pdnType, 0, 0, 0, 0, 0, (String) null, 0));
    }

    public void onEmergencyRegistrationFailed() {
        IMSLog.s(LOG_TAG, this.mSlotId, "onRegistrationFailed (Emergency)");
        this.mIpcDispatcher.sendMessage(IilIpcMessage.encodeImsRegisgtrationInfo(3, false, false, false, false, false, 0, 0, 0, 0, 0, 0, (String) null, 0));
    }

    private TelephonyManager getTelephonyManager(int subId) {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (subId >= 0) {
            return tm.createForSubscriptionId(subId);
        }
        return tm;
    }

    private final class IilPhoneStateListener extends PhoneStateListener {
        public IilPhoneStateListener() {
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            int i = IilManager.this.mSlotId;
            IMSLog.s(IilManager.LOG_TAG, i, "onDataConnectionStateChanged(): state " + state + ", networkType " + networkType + " old " + IilManager.this.mNetworkType);
            if (!SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) || IilManager.this.mSlotId == SubscriptionManager.from(IilManager.this.mContext).getDefaultDataPhoneId()) {
                doUpdateFeature(networkType);
            } else {
                IMSLog.s(IilManager.LOG_TAG, IilManager.this.mSlotId, "onDataConnectionStateChanged(): Not DDS SIM");
            }
        }

        public void onServiceStateChanged(ServiceState state) {
            ServiceStateWrapper serviceState = new ServiceStateWrapper(state);
            int i = IilManager.this.mSlotId;
            IMSLog.s(IilManager.LOG_TAG, i, "onServiceStateChanged(): data regstate " + serviceState.getDataRegState() + ", network type " + serviceState.getDataNetworkType());
            SubscriptionManager subMan = SubscriptionManager.from(IilManager.this.mContext);
            if (SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) && IilManager.this.mSlotId != subMan.getDefaultDataPhoneId()) {
                IMSLog.s(IilManager.LOG_TAG, IilManager.this.mSlotId, "onServiceStateChanged(): Not DDS SIM");
            } else if (serviceState.getDataRegState() != 0) {
                IMSLog.s(IilManager.LOG_TAG, IilManager.this.mSlotId, "onServiceStateChanged(): not in Service");
            } else {
                doUpdateFeature(serviceState.getDataNetworkType());
            }
        }

        private void doUpdateFeature(int networkType) {
            int networkclass = TelephonyManagerExt.getNetworkClass(networkType);
            boolean doUpdate = false;
            if (networkType != 0 && IilManager.this.mNetworkType != networkType) {
                Mno mno = SimUtil.getSimMno(IilManager.this.mSlotId);
                if (mno == Mno.ATT) {
                    if (IilManager.this.mNetworkClass != networkclass) {
                        doUpdate = true;
                    }
                } else if (mno.isKor() || IilManager.this.disallowReregistration(mno)) {
                    doUpdate = true;
                }
                int unused = IilManager.this.mNetworkType = networkType;
                int unused2 = IilManager.this.mNetworkClass = networkclass;
                if (IilManager.this.mReg != null && doUpdate) {
                    IilManager iilManager = IilManager.this;
                    iilManager.updateFeature(iilManager.mReg);
                }
            }
        }
    }

    private void registerPhoneStateListener() {
        int subId = SimUtil.getSubId(this.mSlotId);
        this.mSubId = subId;
        getTelephonyManager(subId).listen(this.mPhoneStateListener, 65);
    }

    private void unRegisterPhoneStateListener() {
        if (this.mPhoneStateListener != null) {
            getTelephonyManager(this.mSubId).listen(this.mPhoneStateListener, 0);
        }
    }

    public void notifyImsReady(boolean readiness) {
        unRegisterPhoneStateListener();
        if (readiness) {
            registerPhoneStateListener();
        }
        sendMessage(obtainMessage(12));
    }

    public void notifyImsRegistration(ImsRegistration reg, boolean registered, ImsRegistrationError error) {
        int i = this.mSlotId;
        IMSLog.s(LOG_TAG, i, "notifyImsRegistration: registered=" + registered + " registration=" + reg + " error=" + error);
        ImsProfile profile = reg.getImsProfile();
        if (this.mSlotId != reg.getPhoneId()) {
            IMSLog.s(LOG_TAG, this.mSlotId, "Not matched slotId. Ignore notification.");
        } else if (profile.getCmcType() == 1) {
            IMSLog.s(LOG_TAG, this.mSlotId, "CMC PD registered. Ignore notification.");
        } else if (!registered) {
            if (reg.hasVolteService()) {
                this.mReg = null;
            }
            if (!reg.hasService("mmtel") && !reg.hasService("mmtel-video")) {
                return;
            }
            if (profile.hasEmergencySupport()) {
                sendMessage(obtainMessage(4));
                return;
            }
            if (error.getDetailedDeregiReason() == 31) {
                sendMessage(obtainMessage(10, Integer.valueOf(reg.getNetworkType())));
            }
            int networkType = reg.getNetworkType();
            int ecmpStatus = reg.getEcmpStatus();
            boolean epdgStatus = reg.getEpdgStatus();
            Registration registration = new Registration(0, networkType, ecmpStatus, 0, epdgStatus ? 1 : 0, reg.getRegiRat());
            registration.setSipError(error.getSipErrorCode());
            registration.setDeregiReasonCode(error.getDeregistrationReason());
            sendMessage(obtainMessage(2, registration));
        } else if (reg.hasVolteService()) {
            this.mReg = reg;
            updateFeature(reg);
        }
    }
}
