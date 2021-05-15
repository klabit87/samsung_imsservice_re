package com.sec.internal.ims.config.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class TelephonyAdapterPrimaryDeviceBase extends Handler implements ITelephonyAdapter {
    protected static final int EVENT_DDS_CHANGED = 100;
    protected static final int EVENT_SIM_REMOVE_OR_REFRESH = 101;
    protected static final int HANDLE_EVENT_SIM_READY = 9;
    protected static final int HANDLE_EVENT_SIM_REMOVED = 10;
    protected static final int HANDLE_EVENT_SIM_STATE_CHANGED = 11;
    protected static final int HANDLE_GET_APP_TOKEN = 12;
    protected static final int HANDLE_GET_APP_TOKEN_TIMEOUT = 13;
    protected static final int HANDLE_GET_MSISDN = 6;
    protected static final int HANDLE_GET_MSISDN_TIMEOUT = 7;
    protected static final int HANDLE_GET_OTP = 2;
    protected static final int HANDLE_GET_OTP_TIMEOUT = 3;
    protected static final int HANDLE_GET_PORT_OTP = 4;
    protected static final int HANDLE_GET_PORT_OTP_TIMEOUT = 5;
    protected static final int HANDLE_INTENT_DATA_SMS_RECEIVED_ACTION = 1;
    protected static final int HANDLE_NOTIFY_OTP_NEEDED = 8;
    protected static final int HANDLE_SMS_CONFIGURATION_REQUEST = 0;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceBase.class.getSimpleName();
    protected static final int NOTIFY_AUTO_CONFIGURATION_COMPLETED = 52;
    protected static final int NOTIFY_MSISDN_NUMBER_NEEDED = 51;
    protected static final int NOTIFY_VERIFICATION_CODE_NEEDED = 50;
    protected static String SMS_CONFIGURATION_REQUEST = "-rcscfg";
    protected final RemoteCallbackList<IAutoConfigurationListener> mAutoConfigurationListener = new RemoteCallbackList<>();
    protected Context mContext;
    protected int mCurrentMsisdnPermits = 0;
    protected int mCurrentOtpPermits = 0;
    protected int mCurrentPermits = 0;
    protected int mCurrentPortPermits = 0;
    protected boolean mIsWaitingForMsisdn = false;
    protected boolean mIsWaitingForOtp = false;
    protected final Object mLock = new Object();
    protected Looper mLooper;
    protected Handler mModuleHandler;
    protected String mMsisdn = null;
    protected Semaphore mMsisdnSemaphore = new Semaphore(0);
    protected String mOtp = null;
    protected long mOtpReceivedTime = 0;
    protected int mPhoneId;
    protected String mPortOtp = null;
    protected long mPortOtpReceivedTime = 0;
    protected Semaphore mPortOtpSemaphore = new Semaphore(0);
    protected PortSmsReceiverBase mPortSmsReceiver;
    protected Map<Integer, Boolean> mPostponedNotification;
    protected Semaphore mSemaphore = new Semaphore(0);
    protected ISimManager mSimManager;
    protected SmsReceiverBase mSmsReceiver;
    protected TelephonyAdapterState mState = null;
    protected int mSubId = 0;
    protected ITelephonyManager mTelephony;

    public TelephonyAdapterPrimaryDeviceBase(Context context, Handler handler, int phoneId) {
        super(handler.getLooper());
        this.mContext = context;
        this.mModuleHandler = handler;
        this.mLooper = handler.getLooper();
        this.mPhoneId = phoneId;
        this.mTelephony = TelephonyManagerWrapper.getInstance(this.mContext);
        this.mSimManager = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        this.mPostponedNotification = new HashMap();
        getState(TelephonyAdapterState.IDLE_STATE);
        registerSimEventListener();
    }

    /* access modifiers changed from: protected */
    public void registerSimEventListener() {
        ISimManager iSimManager = this.mSimManager;
        if (iSimManager != null) {
            iSimManager.registerForSimReady(this, 9, (Object) null);
            this.mSimManager.registerForSimRemoved(this, 10, (Object) null);
            this.mSimManager.registerForSimStateChanged(this, 11, (Object) null);
            this.mSubId = this.mSimManager.getSubscriptionId();
        }
    }

    /* access modifiers changed from: protected */
    public void createSmsReceiver() {
        this.mSmsReceiver = new SmsReceiverBase();
    }

    /* access modifiers changed from: protected */
    public void registerSmsReceiver() {
        if (this.mModuleHandler != null) {
            createSmsReceiver();
            Context context = this.mContext;
            SmsReceiverBase smsReceiverBase = this.mSmsReceiver;
            context.registerReceiver(smsReceiverBase, smsReceiverBase.getIntentFilter());
        }
    }

    /* access modifiers changed from: protected */
    public void createPortSmsReceiver() {
        this.mPortSmsReceiver = new PortSmsReceiverBase();
    }

    /* access modifiers changed from: protected */
    public void registerPortSmsReceiver() {
        if (this.mModuleHandler != null) {
            createPortSmsReceiver();
            Context context = this.mContext;
            PortSmsReceiverBase portSmsReceiverBase = this.mPortSmsReceiver;
            context.registerReceiver(portSmsReceiverBase, portSmsReceiverBase.getIntentFilter());
        }
    }

    /* access modifiers changed from: protected */
    public void sendSmsPushForConfigRequest(boolean isForceConfigRequest) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "sendSmsPushForConfigRequest: isForceConfigRequest: " + isForceConfigRequest);
        IMSLog.c(LogClass.TAPDB_RECE_PUSHSMS, this.mPhoneId + ",RPUSH");
        if (isForceConfigRequest) {
            this.mModuleHandler.sendEmptyMessage(4);
        } else {
            this.mModuleHandler.sendEmptyMessage(21);
        }
    }

    /* access modifiers changed from: protected */
    public void updateOtpInfo(Message msg, boolean useWaitingForOtp) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateOtpInfo: mIsWaitingForOtp: " + this.mIsWaitingForOtp + " useWaitingForOtp: " + useWaitingForOtp);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPhoneId);
        sb.append(",ROTP");
        IMSLog.c(LogClass.TAPDB_RECE_OTP, sb.toString());
        this.mOtp = (String) msg.obj;
        this.mOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
        if (this.mIsWaitingForOtp || !useWaitingForOtp) {
            this.mSemaphore.release();
        }
    }

    /* access modifiers changed from: protected */
    public void handleReceivedDataSms(Message msg, boolean isForceConfigRequest, boolean useWaitingForOtp) {
        if (msg.obj == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleReceivedDataSms: no received data sms");
        } else if (((String) msg.obj).contains(SMS_CONFIGURATION_REQUEST)) {
            sendSmsPushForConfigRequest(isForceConfigRequest);
        } else {
            updateOtpInfo(msg, useWaitingForOtp);
        }
    }

    /* access modifiers changed from: protected */
    public void handleOtpTimeout(boolean useWaitingForOtp) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleOtpTimeout: useWaitingForOtpFlag: " + useWaitingForOtp);
        IMSLog.c(LogClass.TAPDB_OTP_TIMEOUT, this.mPhoneId + ",TOTP");
        removeMessages(3);
        this.mOtp = null;
        this.mOtpReceivedTime = 0;
        if (this.mIsWaitingForOtp || !useWaitingForOtp) {
            this.mSemaphore.release();
        }
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        int i2 = msg.what;
        if (i2 == 1) {
            handleReceivedDataSms(msg, true, true);
        } else if (i2 != 3) {
            switch (i2) {
                case 9:
                    String str2 = LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.i(str2, i3, "SIM_READY, Current state: " + this.mState.getClass().getSimpleName());
                    if (!(this.mState instanceof ReadyState)) {
                        getState(TelephonyAdapterState.READY_STATE);
                        return;
                    }
                    return;
                case 10:
                    String str3 = LOG_TAG;
                    int i4 = this.mPhoneId;
                    IMSLog.i(str3, i4, "SIM_REMOVED, Current state: " + this.mState.getClass().getSimpleName());
                    if (!(this.mState instanceof AbsentState)) {
                        getState(TelephonyAdapterState.ABSENT_STATE);
                        return;
                    }
                    return;
                case 11:
                    String str4 = LOG_TAG;
                    int i5 = this.mPhoneId;
                    IMSLog.i(str4, i5, "SIM_STATE_CHANGED, Current state: " + this.mState.getClass().getSimpleName());
                    int simState = this.mTelephony.getSimState();
                    String iccState = this.mTelephony.getTelephonyProperty(this.mPhoneId, ImsConstants.SystemProperties.SIM_STATE, "UNKNOWN");
                    int default_phoneId = SimUtil.getSimSlotPriority();
                    String str5 = LOG_TAG;
                    int i6 = this.mPhoneId;
                    IMSLog.i(str5, i6, "sim state:" + simState + ", icc state:" + iccState);
                    int i7 = this.mPhoneId;
                    if (i7 != default_phoneId) {
                        String str6 = LOG_TAG;
                        IMSLog.i(str6, i7, "Omit no default sim event. phoneId = " + this.mPhoneId + " default_phoneId = " + default_phoneId);
                        return;
                    } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(iccState)) {
                        if (this.mState instanceof IdleState) {
                            getState(TelephonyAdapterState.READY_STATE);
                            return;
                        }
                        return;
                    } else if (1 == simState) {
                        if (this.mState instanceof IdleState) {
                            getState(TelephonyAdapterState.ABSENT_STATE);
                            return;
                        }
                        return;
                    } else if (!"IMSI".equals(iccState)) {
                        TelephonyAdapterState telephonyAdapterState = this.mState;
                        if ((telephonyAdapterState instanceof ReadyState) || (telephonyAdapterState instanceof AbsentState)) {
                            getState(TelephonyAdapterState.IDLE_STATE);
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        } else {
            handleOtpTimeout(true);
        }
    }

    protected abstract class SmsReceiver extends BroadcastReceiver {
        protected IntentFilter mIntentFilter = null;

        /* access modifiers changed from: protected */
        public abstract void readMessageFromSMSIntent(Intent intent);

        public SmsReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            this.mIntentFilter = intentFilter;
            intentFilter.addAction(AECNamespace.Action.RECEIVED_SMS_NOTIFICATION);
            this.mIntentFilter.addDataScheme("sms");
            this.mIntentFilter.addDataAuthority("localhost", TelephonyAdapterState.SMS_DEST_PORT);
        }

        public void onReceive(Context context, Intent intent) {
            if (AECNamespace.Action.RECEIVED_SMS_NOTIFICATION.equals(intent.getAction())) {
                try {
                    readMessageFromSMSIntent(intent);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }

    protected class PortSmsReceiverBase extends SmsReceiver {
        public PortSmsReceiverBase() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "PortSmsReceiverBase");
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage[] smss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (smss != null && smss[0] != null) {
                SmsMessage sms = smss[0];
                String message = sms.getDisplayMessageBody();
                if (message == null) {
                    message = new String(sms.getUserData(), Charset.forName("UTF-16"));
                }
                TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase = TelephonyAdapterPrimaryDeviceBase.this;
                telephonyAdapterPrimaryDeviceBase.sendMessage(telephonyAdapterPrimaryDeviceBase.obtainMessage(4, message));
            }
        }
    }

    protected class SmsReceiverBase extends SmsReceiver {
        public SmsReceiverBase() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "SmsReceiverBase");
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage[] smss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (smss != null && smss[0] != null) {
                SmsMessage sms = smss[0];
                int phoneId = SimManagerFactory.getSlotId(intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1));
                String message = sms.getDisplayMessageBody();
                if (message == null) {
                    message = new String(sms.getUserData(), Charset.forName("UTF-16"));
                }
                Message msg = TelephonyAdapterPrimaryDeviceBase.this.obtainMessage();
                msg.what = 1;
                msg.arg1 = phoneId;
                msg.obj = message;
                if (TelephonyAdapterPrimaryDeviceBase.this.mPhoneId == phoneId) {
                    TelephonyAdapterPrimaryDeviceBase.this.sendMessage(msg);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initState() {
        int simState = this.mTelephony.getSimState(this.mPhoneId);
        if (5 == simState) {
            if (TextUtils.isEmpty(this.mTelephony.getSubscriberId(this.mSubId))) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "simState is ready but imsi is empty");
                getState(TelephonyAdapterState.IDLE_STATE);
                return;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "simState is ready and imsi is existed");
            getState(TelephonyAdapterState.READY_STATE);
        } else if (1 == simState) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "simState is absent");
            getState(TelephonyAdapterState.ABSENT_STATE);
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "simState is not ready");
            getState(TelephonyAdapterState.IDLE_STATE);
        }
    }

    /* access modifiers changed from: protected */
    public void getState(String state) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getState: change to " + state);
        if (TelephonyAdapterState.IDLE_STATE.equals(state)) {
            this.mState = new IdleState();
        } else if (TelephonyAdapterState.READY_STATE.equals(state)) {
            this.mState = new ReadyState();
        } else if (TelephonyAdapterState.ABSENT_STATE.equals(state)) {
            this.mState = new AbsentState();
        }
    }

    protected class IdleState extends TelephonyAdapterState {
        public IdleState() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "idle state");
        }
    }

    protected class ReadyState extends TelephonyAdapterState {
        public ReadyState() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "ready state");
        }

        public boolean isReady() {
            return true;
        }

        public String getPrimaryIdentity() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "getPrimaryIdentity()");
            String identity = "";
            if (!TextUtils.isEmpty(getImsi())) {
                identity = "IMSI_" + getImsi();
            } else if (!TextUtils.isEmpty(getMsisdn())) {
                identity = "MSISDN_" + getMsisdn();
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "identity error");
            }
            return identity.replaceAll("[\\W]", "");
        }

        public String getMcc() {
            String simOperator = TelephonyAdapterPrimaryDeviceBase.this.mSimManager != null ? TelephonyAdapterPrimaryDeviceBase.this.mSimManager.getSimOperator() : "";
            if (TextUtils.isEmpty(simOperator)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "MCC sim operator: empty");
                return simOperator;
            }
            try {
                return simOperator.substring(0, 3);
            } catch (IndexOutOfBoundsException e) {
                String access$000 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(access$000, i, "sim operator:" + simOperator);
                return simOperator;
            }
        }

        public String getMnc() {
            String simOperator = TelephonyAdapterPrimaryDeviceBase.this.mSimManager != null ? TelephonyAdapterPrimaryDeviceBase.this.mSimManager.getSimOperator() : "";
            if (TextUtils.isEmpty(simOperator)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "MNC sim operator: empty");
                return simOperator;
            }
            try {
                if (simOperator.length() > 5) {
                    return simOperator.substring(3, 6);
                }
                return "0" + simOperator.substring(3, 5);
            } catch (IndexOutOfBoundsException e) {
                String access$000 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(access$000, i, "sim operator:" + simOperator);
                return simOperator;
            }
        }

        public String getImsi() {
            if (TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getSubscriberId(SimUtil.getSubId(TelephonyAdapterPrimaryDeviceBase.this.mPhoneId)) != null) {
                return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getSubscriberId(SimUtil.getSubId(TelephonyAdapterPrimaryDeviceBase.this.mPhoneId));
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "imsi error");
            return "";
        }

        public String getImei() {
            if (TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getDeviceId(TelephonyAdapterPrimaryDeviceBase.this.mPhoneId) != null) {
                return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getDeviceId(TelephonyAdapterPrimaryDeviceBase.this.mPhoneId);
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "imei error");
            return "";
        }

        public String getMsisdn() {
            String msisdn = TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getMsisdn(SimUtil.getSubId(TelephonyAdapterPrimaryDeviceBase.this.mPhoneId));
            if (TextUtils.isEmpty(msisdn)) {
                msisdn = TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getLine1Number();
                if (TextUtils.isEmpty(msisdn)) {
                    IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "MSISDN doesn't exist");
                    msisdn = "";
                }
            }
            return ImsCallUtil.validatePhoneNumber(msisdn, getSimCountryCode());
        }

        public String getSimCountryCode() {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getSimCountryIso().toUpperCase(Locale.ENGLISH);
        }

        public String getSipUri() {
            return "";
        }

        public String getNetType() {
            if (TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getNetworkType() == 13) {
                return "LTE";
            }
            return "3G";
        }

        public String getOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceBase.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceBase.this.mOtpReceivedTime + 3000) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "OTP don't exist. wait OTP");
                IMSLog.c(LogClass.TAPDB_WAIT_OTP, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId + ",WOTP");
                TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = true;
                try {
                    TelephonyAdapterPrimaryDeviceBase.this.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Throwable th) {
                    TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = false;
                    throw th;
                }
                TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = false;
                TelephonyAdapterPrimaryDeviceBase.this.removeMessages(3);
                String access$000 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(access$000, i, "receive OTP: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceBase.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceBase.this.mOtp;
        }

        public String getIdentityByPhoneId(int phoneId) {
            return ConfigUtil.buildIdentity(TelephonyAdapterPrimaryDeviceBase.this.mContext, phoneId);
        }

        public String getSubscriberId(int subscriptionId) {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getSubscriberId(subscriptionId);
        }

        public String getMsisdn(int subscriptionId) {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getMsisdn(subscriptionId);
        }

        public String getDeviceId(int slotId) {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getDeviceId(slotId);
        }
    }

    protected class AbsentState extends TelephonyAdapterState {
        ImsProfile mImsProfile = null;

        public AbsentState() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "absent state");
            List<ImsProfile> profile = ImsProfileLoaderInternal.getProfileList(TelephonyAdapterPrimaryDeviceBase.this.mContext, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId);
            if (profile == null || profile.size() <= 0 || profile.get(0) == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "AbsentState : no ImsProfile loaded");
            } else {
                this.mImsProfile = profile.get(0);
            }
        }

        public boolean isReady() {
            return true;
        }

        public String getPrimaryIdentity() {
            String identity = "";
            if (!TextUtils.isEmpty(getImsi())) {
                identity = "IMSI_" + getImsi();
            } else if (!TextUtils.isEmpty(getMsisdn())) {
                identity = "MSISDN_" + getMsisdn();
            } else if (!TextUtils.isEmpty(getImei())) {
                identity = "IMEI_" + getImei();
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "identity error");
            }
            return identity.replaceAll("[\\W]", "");
        }

        public String getMcc() {
            ImsProfile imsProfile = this.mImsProfile;
            if (imsProfile != null) {
                return imsProfile.getDefaultMcc();
            }
            return "45001".substring(0, 3);
        }

        public String getMnc() {
            ImsProfile imsProfile = this.mImsProfile;
            if (imsProfile != null) {
                return imsProfile.getDefaultMnc();
            }
            try {
                if ("45001".length() > 5) {
                    return "45001".substring(3, 6);
                }
                return "0" + "45001".substring(3, 5);
            } catch (IndexOutOfBoundsException e) {
                String access$000 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(access$000, i, "sim operator:" + "45001");
                return "45001";
            }
        }

        public String getImsi() {
            return "";
        }

        public String getImei() {
            if (TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getDeviceId() != null) {
                return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getDeviceId();
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "imei error");
            return "";
        }

        public String getMsisdn() {
            return "";
        }

        public String getSimCountryCode() {
            return "";
        }

        public String getSipUri() {
            return "";
        }

        public String getNetType() {
            if (TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getNetworkType() == 13) {
                return "LTE";
            }
            return "3G";
        }

        public String getOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceBase.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceBase.this.mOtpReceivedTime + 3000) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "OTP don't exist. wait OTP");
                TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = true;
                try {
                    TelephonyAdapterPrimaryDeviceBase.this.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Throwable th) {
                    TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = false;
                    throw th;
                }
                TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = false;
                String access$000 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(access$000, i, "receive OTP:" + IMSLog.checker(TelephonyAdapterPrimaryDeviceBase.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceBase.this.mOtp;
        }

        public String getIdentityByPhoneId(int phoneId) {
            return ConfigUtil.buildIdentity(TelephonyAdapterPrimaryDeviceBase.this.mContext, phoneId);
        }

        public String getDeviceId(int slotId) {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getDeviceId(slotId);
        }
    }

    public boolean isReady() {
        return this.mState.isReady();
    }

    public String getPrimaryIdentity() {
        return this.mState.getPrimaryIdentity();
    }

    public String getMcc() {
        return this.mState.getMcc();
    }

    public String getMnc() {
        return this.mState.getMnc();
    }

    public String getImsi() {
        return this.mState.getImsi();
    }

    public String getImei() {
        return this.mState.getImei();
    }

    public String getSimCountryCode() {
        return this.mState.getSimCountryCode();
    }

    public String getMsisdn() {
        return this.mState.getMsisdn();
    }

    public String getSipUri() {
        return this.mState.getSipUri();
    }

    public String getNetType() {
        return this.mState.getNetType();
    }

    public String getSmsDestPort() {
        return this.mState.getSmsDestPort();
    }

    public String getSmsOrigPort() {
        return this.mState.getSmsOrigPort();
    }

    public String getExistingOtp() {
        return this.mState.getExistingOtp();
    }

    public String getExistingPortOtp() {
        return this.mState.getExistingPortOtp();
    }

    public String getOtp() {
        sendMessageDelayed(obtainMessage(3), 300000);
        return this.mState.getOtp();
    }

    public String getPortOtp() {
        return this.mState.getPortOtp();
    }

    public String getMsisdnNumber() {
        return this.mState.getMsisdnNumber();
    }

    public String getAppToken(boolean isRetry) {
        return this.mState.getAppToken(isRetry);
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener listener) {
        this.mState.registerAutoConfigurationListener(listener);
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener listener) {
        this.mState.unregisterAutoConfigurationListener(listener);
    }

    public void notifyAutoConfigurationListener(int type, boolean result) {
        this.mState.notifyAutoConfigurationListener(type, result);
    }

    public void sendVerificationCode(String value) {
        this.mState.sendVerificationCode(value);
    }

    public void sendMsisdnNumber(String value) {
        this.mState.sendMsisdnNumber(value);
    }

    public void registerUneregisterForOTP(boolean val) {
    }

    public String getIdentityByPhoneId(int phoneId) {
        return this.mState.getIdentityByPhoneId(phoneId);
    }

    public String getSubscriberId(int subscriptionId) {
        return this.mState.getSubscriberId(subscriptionId);
    }

    public String getMsisdn(int subscriptionId) {
        return this.mState.getMsisdn(subscriptionId);
    }

    public String getDeviceId(int slotId) {
        return this.mState.getDeviceId(slotId);
    }

    public void cleanup() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup");
        if (!(this.mModuleHandler == null || this.mSmsReceiver == null)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregister mSmsReceiver");
            this.mContext.unregisterReceiver(this.mSmsReceiver);
            this.mSmsReceiver = null;
        }
        if (!(this.mModuleHandler == null || this.mPortSmsReceiver == null)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregister mPortSmsReceiver");
            this.mContext.unregisterReceiver(this.mPortSmsReceiver);
            this.mPortSmsReceiver = null;
        }
        if (this.mSimManager != null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "deregister SimReady/SimRemoved/SimStateChanged");
            this.mSimManager.deregisterForSimReady(this);
            this.mSimManager.deregisterForSimRemoved(this);
            this.mSimManager.deregisterForSimStateChanged(this);
        }
        this.mState.cleanup();
    }
}
