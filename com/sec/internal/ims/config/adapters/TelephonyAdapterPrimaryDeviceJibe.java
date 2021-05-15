package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

public class TelephonyAdapterPrimaryDeviceJibe extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceJibe.class.getSimpleName();
    /* access modifiers changed from: private */
    public Semaphore mOtpSemaphore = new Semaphore(0);

    public TelephonyAdapterPrimaryDeviceJibe(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerPortSmsReceiver();
        initState();
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        switch (msg.what) {
            case 2:
                if (this.mIsWaitingForOtp) {
                    notifyAutoConfigurationListener(50, true);
                    return;
                }
                return;
            case 3:
                this.mOtp = null;
                try {
                    String str2 = LOG_TAG;
                    int i2 = this.mPhoneId;
                    IMSLog.i(str2, i2, "semaphore release with mCurrentOtpPermits: " + this.mCurrentOtpPermits);
                    IMSLog.c(LogClass.TAPDJ_OTP_TIMEOUT, this.mPhoneId + ",OT");
                    this.mOtpSemaphore.release(this.mCurrentOtpPermits);
                    return;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return;
                }
            case 4:
                IMSLog.i(LOG_TAG, this.mPhoneId, "receive port sms");
                if (msg.obj == null) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "no SMS data!");
                    return;
                } else if (((String) msg.obj).contains(SMS_CONFIGURATION_REQUEST)) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "request force configuration");
                    this.mModuleHandler.sendMessage(obtainMessage(21, Integer.valueOf(this.mPhoneId)));
                    return;
                } else {
                    removeMessages(5);
                    this.mPortOtp = (String) msg.obj;
                    String str3 = LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.i(str3, i3, "mPortOtp: " + IMSLog.checker(this.mPortOtp));
                    this.mPortOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                    try {
                        String str4 = LOG_TAG;
                        int i4 = this.mPhoneId;
                        IMSLog.i(str4, i4, "otp received: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                        this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                        this.mCurrentPortPermits = 0;
                        return;
                    } catch (IllegalArgumentException e2) {
                        e2.printStackTrace();
                        return;
                    }
                }
            case 5:
                this.mPortOtp = null;
                try {
                    String str5 = LOG_TAG;
                    int i5 = this.mPhoneId;
                    IMSLog.i(str5, i5, "otp timeout: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                    IMSLog.c(LogClass.TAPDJ_PORT_OTP_TIMEOUT, this.mPhoneId + ",POT");
                    this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                    this.mCurrentPortPermits = 0;
                    return;
                } catch (IllegalArgumentException e3) {
                    e3.printStackTrace();
                    return;
                }
            case 6:
                if (this.mIsWaitingForMsisdn) {
                    notifyAutoConfigurationListener(51, true);
                    return;
                }
                return;
            case 7:
                this.mMsisdn = null;
                try {
                    String str6 = LOG_TAG;
                    int i6 = this.mPhoneId;
                    IMSLog.i(str6, i6, "semaphore release with mCurrentMsisdnPermits: " + this.mCurrentMsisdnPermits);
                    IMSLog.c(LogClass.TAPDJ_MSISDN_TIMEOUT, this.mPhoneId + ",MT");
                    this.mMsisdnSemaphore.release(this.mCurrentMsisdnPermits);
                    return;
                } catch (IllegalArgumentException e4) {
                    e4.printStackTrace();
                    return;
                }
            default:
                super.handleMessage(msg);
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void getState(String state) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getState: change to " + state);
        if (TelephonyAdapterState.READY_STATE.equals(state)) {
            this.mState = new ReadyState();
        } else if (TelephonyAdapterState.ABSENT_STATE.equals(state)) {
            this.mState = new AbsentState();
        } else {
            super.getState(state);
        }
    }

    private class ReadyState extends TelephonyAdapterPrimaryDeviceBase.ReadyState {
        public ReadyState() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "ready state");
        }

        public String getOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getOtp");
            TelephonyAdapterPrimaryDeviceJibe.this.mCurrentOtpPermits = 0;
            TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp = true;
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe.obtainMessage(2), 300);
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe2.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe2.obtainMessage(3), 310000);
            try {
                TelephonyAdapterPrimaryDeviceJibe.this.mCurrentOtpPermits = TelephonyAdapterPrimaryDeviceJibe.this.mOtpSemaphore.availablePermits() + 1;
                String access$100 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                IMSLog.i(access$100, i, "semaphore acquire with mCurrentOtpPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentOtpPermits);
                TelephonyAdapterPrimaryDeviceJibe.this.mOtpSemaphore.acquire(TelephonyAdapterPrimaryDeviceJibe.this.mCurrentOtpPermits);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
            TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp = false;
            return TelephonyAdapterPrimaryDeviceJibe.this.mOtp;
        }

        public String getPortOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getPortOtp");
            TelephonyAdapterPrimaryDeviceJibe.this.mCurrentPortPermits = 0;
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe.obtainMessage(5), 300000);
            try {
                TelephonyAdapterPrimaryDeviceJibe.this.mCurrentPortPermits = TelephonyAdapterPrimaryDeviceJibe.this.mPortOtpSemaphore.availablePermits() + 1;
                String access$100 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                IMSLog.i(access$100, i, "getPortOtp: semaphore acquire with mCurrentPortPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentPortPermits);
                TelephonyAdapterPrimaryDeviceJibe.this.mPortOtpSemaphore.acquire(TelephonyAdapterPrimaryDeviceJibe.this.mCurrentPortPermits);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
            return TelephonyAdapterPrimaryDeviceJibe.this.mPortOtp;
        }

        public String getMsisdnNumber() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getMsisdnNumber");
            String msisdn = getMsisdn();
            if (!TextUtils.isEmpty(msisdn)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "msisdn exists from telephony");
                IMSLog.c(LogClass.TAPDJ_EXIST_MSISDN_TELEPHONY, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",EMT");
                TelephonyAdapterPrimaryDeviceJibe.this.mMsisdn = msisdn;
                return TelephonyAdapterPrimaryDeviceJibe.this.mMsisdn;
            }
            Mno mno = SimUtil.getSimMno(TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId);
            if (ConfigUtil.isRcsPreConsent(TelephonyAdapterPrimaryDeviceJibe.this.mContext, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId) || (!mno.isEur() && !mno.isSea() && !mno.isOce() && !mno.isMea() && !mno.isSwa())) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "need to get msisdn from application");
                TelephonyAdapterPrimaryDeviceJibe.this.mCurrentMsisdnPermits = 0;
                TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn = true;
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe.obtainMessage(6), 300);
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe2.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe2.obtainMessage(7), 310000);
                try {
                    TelephonyAdapterPrimaryDeviceJibe.this.mCurrentMsisdnPermits = TelephonyAdapterPrimaryDeviceJibe.this.mMsisdnSemaphore.availablePermits() + 1;
                    String access$100 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(access$100, i, "semaphore acquire with mCurrentMsisdnPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentMsisdnPermits);
                    TelephonyAdapterPrimaryDeviceJibe.this.mMsisdnSemaphore.acquire(TelephonyAdapterPrimaryDeviceJibe.this.mCurrentMsisdnPermits);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                }
                TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn = false;
                return TelephonyAdapterPrimaryDeviceJibe.this.mMsisdn;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "operator using jibe, but not GC, use FW's dialog to ask for MSISDN");
            return null;
        }

        public void registerAutoConfigurationListener(IAutoConfigurationListener listener) {
            if (listener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceJibe.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener != null) {
                    String access$100 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(access$100, i, "register listener: " + listener);
                    IMSLog.c(LogClass.TAPDJ_REG_LISTNER, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",RL:" + listener);
                    TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.register(listener);
                    if (!TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.isEmpty()) {
                        for (Integer intValue : TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.keySet()) {
                            int pn = intValue.intValue();
                            notifyAutoConfigurationListener(pn, ((Boolean) TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.get(Integer.valueOf(pn))).booleanValue());
                        }
                    }
                }
            }
        }

        public void unregisterAutoConfigurationListener(IAutoConfigurationListener listener) {
            if (listener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceJibe.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener != null) {
                    String access$100 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(access$100, i, "unregister listener: " + listener);
                    IMSLog.c(LogClass.TAPDJ_UNREG_LISTNER, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",UL:" + listener);
                    TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.unregister(listener);
                    TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.clear();
                }
            }
        }

        public void notifyAutoConfigurationListener(int type, boolean result) {
            String access$100 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(access$100, i, "notifyAutoConfigurationListener: type: " + type + ", result: " + result);
            if (type != 50 && type != 51 && type != 52) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: unknown notification type");
                return;
            } else if ((type != 50 || TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp) && (type != 51 || TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn)) {
                synchronized (TelephonyAdapterPrimaryDeviceJibe.this.mLock) {
                    if (TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener == null) {
                        IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: mAutoConfigurationListener: empty");
                        return;
                    }
                    try {
                        int length = TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.beginBroadcast();
                        String access$1002 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                        int i2 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                        IMSLog.i(access$1002, i2, "notifyAutoConfigurationListener: listener length: " + length);
                        if (type == 50) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: onVerificationCodeNeeded");
                            IMSLog.c(LogClass.TAPDJ_OTP_NEEDED, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",VCN,LEN:" + length);
                        } else if (type == 51) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: onMsisdnNumberNeeded");
                            IMSLog.c(LogClass.TAPDJ_MSISDN_NEEDED, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",MNN,LEN:" + length);
                        } else {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: onAutoConfigurationCompleted");
                            IMSLog.c(LogClass.TAPDJ_ACS_COMPLETED, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",ACC:" + result + ",LEN:" + length);
                        }
                        if (length == 0) {
                            String access$1003 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                            int i3 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                            IMSLog.i(access$1003, i3, "Listener not registered yet. Postpone notify later: " + type);
                            if (type == 52) {
                                TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.clear();
                            }
                            TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.put(Integer.valueOf(type), Boolean.valueOf(result));
                        }
                        for (int index = 0; index < length; index++) {
                            IAutoConfigurationListener listener = TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.getBroadcastItem(index);
                            if (type == 50) {
                                listener.onVerificationCodeNeeded();
                            } else if (type == 51) {
                                listener.onMsisdnNumberNeeded();
                            } else {
                                listener.onAutoConfigurationCompleted(result);
                                TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.clear();
                            }
                        }
                    } catch (RemoteException | IllegalStateException | NullPointerException e) {
                        String access$1004 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                        int i4 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                        IMSLog.i(access$1004, i4, "notifyAutoConfigurationListener: Exception: " + e.getMessage());
                    }
                    try {
                        TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.finishBroadcast();
                    } catch (IllegalStateException e2) {
                        String access$1005 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                        int i5 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                        IMSLog.i(access$1005, i5, "notifyAutoConfigurationListener: Exception: " + e2.getMessage());
                    }
                }
            } else {
                String access$1006 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                int i6 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                IMSLog.i(access$1006, i6, "notifyAutoConfigurationListener: ignore notification type, mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp + " mIsWaitingForMsisdn: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn);
                return;
            }
            return;
        }

        public void sendVerificationCode(String value) {
            String access$100 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(access$100, i, "sendVerificationCode value: " + value);
            IMSLog.c(LogClass.TAPDJ_SEND_OTP, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",VC:" + value);
            TelephonyAdapterPrimaryDeviceJibe.this.removeMessages(3);
            String access$1002 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(access$1002, i2, "mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp);
            if (TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp) {
                TelephonyAdapterPrimaryDeviceJibe.this.mOtp = value;
                try {
                    String access$1003 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i3 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(access$1003, i3, "semaphore release with mCurrentOtpPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentOtpPermits);
                    TelephonyAdapterPrimaryDeviceJibe.this.mOtpSemaphore.release(TelephonyAdapterPrimaryDeviceJibe.this.mCurrentOtpPermits);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMsisdnNumber(String value) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "sendMsisdnNumber");
            IMSLog.c(LogClass.TAPDJ_SEND_MSISDN, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",MN");
            TelephonyAdapterPrimaryDeviceJibe.this.removeMessages(7);
            if (value == null || "".equals(value)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "value is null or empty");
            }
            String access$100 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(access$100, i, "mIsWaitingForMsisdn: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn);
            if (TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn) {
                TelephonyAdapterPrimaryDeviceJibe.this.mMsisdn = value;
                try {
                    String access$1002 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i2 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(access$1002, i2, "semaphore release with mCurrentMsisdnPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentMsisdnPermits);
                    TelephonyAdapterPrimaryDeviceJibe.this.mMsisdnSemaphore.release(TelephonyAdapterPrimaryDeviceJibe.this.mCurrentMsisdnPermits);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        private AbsentState() {
            super();
        }

        public String getOtp() {
            return null;
        }

        public String getPortOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceJibe.this.mPortOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceJibe.this.mPortOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getPortOtp exist");
            return TelephonyAdapterPrimaryDeviceJibe.this.mPortOtp;
        }

        public String getIdentityByPhoneId(int phoneId) {
            return null;
        }

        public String getDeviceId(int slotId) {
            return null;
        }
    }

    public String getOtp() {
        return this.mState.getOtp();
    }
}
