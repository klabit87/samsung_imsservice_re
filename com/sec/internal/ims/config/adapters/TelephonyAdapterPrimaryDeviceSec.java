package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Calendar;

public class TelephonyAdapterPrimaryDeviceSec extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceSec.class.getSimpleName();
    private static final String NIRSMS_KEYWORD = "NIRSMS0001";

    public TelephonyAdapterPrimaryDeviceSec(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerPortSmsReceiver();
        initState();
    }

    public void handleMessage(Message msg) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "message:" + msg.what);
        int i = msg.what;
        if (i != 3) {
            boolean z = false;
            if (i == 4) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "receive port sms");
                if (msg.obj == null) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "no SMS data!");
                } else if (((String) msg.obj).contains(SMS_CONFIGURATION_REQUEST)) {
                    if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) != -1) {
                        z = true;
                    }
                    boolean isRcsUserSettingAgreed = z;
                    IMSLog.c(LogClass.TAPDS_RECE_NRCR, this.mPhoneId + ",NRCR:" + SMS_CONFIGURATION_REQUEST + ", RcsUserSetting:" + isRcsUserSettingAgreed);
                    if (isRcsUserSettingAgreed) {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "force configuration request");
                        this.mModuleHandler.sendMessage(obtainMessage(4, Integer.valueOf(this.mPhoneId)));
                        return;
                    }
                    IMSLog.i(LOG_TAG, this.mPhoneId, "User didn't try RCS service yet");
                } else {
                    removeMessages(5);
                    this.mPortOtp = (String) msg.obj;
                    this.mPortOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                    try {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "otp received: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                        this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                        this.mCurrentPortPermits = 0;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            } else if (i == 5) {
                this.mPortOtp = null;
                try {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "otp timeout: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                    this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                    this.mCurrentPortPermits = 0;
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                }
            } else if (i != 8) {
                super.handleMessage(msg);
            } else if (this.mIsWaitingForOtp) {
                notifyAutoConfigurationListener(50, true);
            }
        } else {
            this.mOtp = null;
            try {
                IMSLog.i(LOG_TAG, this.mPhoneId, "semaphore release with mCurrentPermits: " + this.mCurrentPermits);
                this.mSemaphore.release(this.mCurrentPermits);
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            }
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
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "ready state");
        }

        public String getOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "getOtp");
            TelephonyAdapterPrimaryDeviceSec.this.mCurrentPermits = 0;
            TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp = true;
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec = TelephonyAdapterPrimaryDeviceSec.this;
            telephonyAdapterPrimaryDeviceSec.sendMessageDelayed(telephonyAdapterPrimaryDeviceSec.obtainMessage(8), 300);
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec2 = TelephonyAdapterPrimaryDeviceSec.this;
            telephonyAdapterPrimaryDeviceSec2.sendMessageDelayed(telephonyAdapterPrimaryDeviceSec2.obtainMessage(3), 310000);
            try {
                TelephonyAdapterPrimaryDeviceSec.this.mCurrentPermits = TelephonyAdapterPrimaryDeviceSec.this.mSemaphore.availablePermits() + 1;
                String access$100 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                IMSLog.i(access$100, i, "semaphore acquire with mCurrentPermits: " + TelephonyAdapterPrimaryDeviceSec.this.mCurrentPermits);
                TelephonyAdapterPrimaryDeviceSec.this.mSemaphore.acquire(TelephonyAdapterPrimaryDeviceSec.this.mCurrentPermits);
            } catch (InterruptedException e) {
                TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp = false;
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp = false;
                e2.printStackTrace();
            }
            TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp = false;
            String access$1002 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
            IMSLog.i(access$1002, i2, "otp: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceSec.this.mOtp));
            return TelephonyAdapterPrimaryDeviceSec.this.mOtp;
        }

        public String getPortOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "getPortOtp");
            TelephonyAdapterPrimaryDeviceSec.this.mCurrentPortPermits = 0;
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec = TelephonyAdapterPrimaryDeviceSec.this;
            telephonyAdapterPrimaryDeviceSec.sendMessageDelayed(telephonyAdapterPrimaryDeviceSec.obtainMessage(5), 900000);
            try {
                TelephonyAdapterPrimaryDeviceSec.this.mCurrentPortPermits = TelephonyAdapterPrimaryDeviceSec.this.mPortOtpSemaphore.availablePermits() + 1;
                String access$100 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                IMSLog.i(access$100, i, "getPortOtp: semaphore acquire with mCurrentPortPermits: " + TelephonyAdapterPrimaryDeviceSec.this.mCurrentPortPermits);
                TelephonyAdapterPrimaryDeviceSec.this.mPortOtpSemaphore.acquire(TelephonyAdapterPrimaryDeviceSec.this.mCurrentPortPermits);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
            String access$1002 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
            IMSLog.i(access$1002, i2, "receive Port OTP:" + IMSLog.checker(TelephonyAdapterPrimaryDeviceSec.this.mPortOtp));
            return TelephonyAdapterPrimaryDeviceSec.this.mPortOtp;
        }

        public void registerAutoConfigurationListener(IAutoConfigurationListener listener) {
            if (listener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceSec.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener != null) {
                    String access$100 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                    IMSLog.i(access$100, i, "register listener: " + listener);
                    IMSLog.c(LogClass.TAPDS_LISTNER, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + "," + listener);
                    TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.register(listener);
                    if (!TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.isEmpty()) {
                        for (Integer intValue : TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.keySet()) {
                            int pn = intValue.intValue();
                            notifyAutoConfigurationListener(pn, ((Boolean) TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.get(Integer.valueOf(pn))).booleanValue());
                        }
                    }
                }
            }
        }

        public void unregisterAutoConfigurationListener(IAutoConfigurationListener listener) {
            if (listener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceSec.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener != null) {
                    String access$100 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                    IMSLog.i(access$100, i, "unregister listener: " + listener);
                    TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.unregister(listener);
                    TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.clear();
                }
            }
        }

        public void notifyAutoConfigurationListener(int type, boolean result) {
            String access$100 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
            IMSLog.i(access$100, i, "notifyAutoConfigurationListener: type: " + type + ", result: " + result);
            if (type != 50 && type != 52) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "notifyAutoConfigurationListener: unknown notification type");
                return;
            } else if (type != 50 || TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp) {
                synchronized (TelephonyAdapterPrimaryDeviceSec.this.mLock) {
                    if (TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener == null) {
                        IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "notifyAutoConfigurationListener: mAutoConfigurationListener: empty");
                        return;
                    }
                    try {
                        int length = TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.beginBroadcast();
                        String access$1002 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                        int i2 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                        IMSLog.i(access$1002, i2, "notifyAutoConfigurationListener: listener length: " + length);
                        if (type == 50) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "notifyAutoConfigurationListener: onVerificationCodeNeeded");
                            IMSLog.c(LogClass.TAPDS_OTP_NEEDED, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + ",VCN,LEN:" + length);
                        } else {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "notifyAutoConfigurationListener: onAutoConfigurationCompleted");
                            IMSLog.c(LogClass.TAPDS_ACS_RESULT, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + ",ACS:" + result + ",LEN:" + length);
                        }
                        if (length == 0) {
                            String access$1003 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                            int i3 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                            IMSLog.i(access$1003, i3, "Listener not registered yet. Postpone notify later: " + type);
                            if (type == 52) {
                                TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.clear();
                            }
                            TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.put(Integer.valueOf(type), Boolean.valueOf(result));
                        }
                        for (int index = 0; index < length; index++) {
                            IAutoConfigurationListener listener = TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.getBroadcastItem(index);
                            if (type == 50) {
                                listener.onVerificationCodeNeeded();
                            } else {
                                listener.onAutoConfigurationCompleted(result);
                                TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.clear();
                            }
                        }
                    } catch (RemoteException | IllegalStateException | NullPointerException e) {
                        String access$1004 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                        int i4 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                        IMSLog.i(access$1004, i4, "notifyAutoConfigurationListener: Exception: " + e.getMessage());
                    }
                    try {
                        TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.finishBroadcast();
                    } catch (IllegalStateException e2) {
                        String access$1005 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                        int i5 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                        IMSLog.i(access$1005, i5, "notifyAutoConfigurationListener: finishBroadcast Exception: " + e2.getMessage());
                    }
                }
            } else {
                String access$1006 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                int i6 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                IMSLog.i(access$1006, i6, "notifyAutoConfigurationListener: ignore notification type, mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp);
                return;
            }
            return;
        }

        public void sendVerificationCode(String value) {
            String access$100 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
            IMSLog.i(access$100, i, "sendVerificationCode value: " + value);
            IMSLog.c(LogClass.TAPDS_SEND_OTP, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + ",VC:" + value);
            if (TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp) {
                TelephonyAdapterPrimaryDeviceSec.this.removeMessages(3);
                TelephonyAdapterPrimaryDeviceSec.this.mOtp = value;
                if (TelephonyAdapterPrimaryDeviceSec.this.mOtp != null) {
                    TelephonyAdapterPrimaryDeviceSec.this.mOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                }
                try {
                    String access$1002 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                    int i2 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                    IMSLog.i(access$1002, i2, "semaphore release with mCurrentPermits: " + TelephonyAdapterPrimaryDeviceSec.this.mCurrentPermits);
                    TelephonyAdapterPrimaryDeviceSec.this.mSemaphore.release(TelephonyAdapterPrimaryDeviceSec.this.mCurrentPermits);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else if (TelephonyAdapterPrimaryDeviceSec.NIRSMS_KEYWORD.equals(value)) {
                if (ImsConstants.SystemSettings.getRcsUserSetting(TelephonyAdapterPrimaryDeviceSec.this.mContext, -1, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId) != -1) {
                    IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "sendVerificationCode: NIRSMS0001 received, force configuration request");
                    IMSLog.c(LogClass.TAPDS_RECE_NIRSMS, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + ",NRCR:" + value);
                    Handler handler = TelephonyAdapterPrimaryDeviceSec.this.mModuleHandler;
                    TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec = TelephonyAdapterPrimaryDeviceSec.this;
                    handler.sendMessage(telephonyAdapterPrimaryDeviceSec.obtainMessage(4, Integer.valueOf(telephonyAdapterPrimaryDeviceSec.mPhoneId)));
                    return;
                }
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "sendVerificationCode: NIRSMS0001 received, but User didn't try RCS service yet");
            }
        }
    }

    private class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        private AbsentState() {
            super();
        }

        public String getOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceSec.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceSec.this.mOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "getOtp exist");
            return TelephonyAdapterPrimaryDeviceSec.this.mOtp;
        }

        public String getPortOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceSec.this.mPortOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceSec.this.mPortOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "getPortOtp exist");
            return TelephonyAdapterPrimaryDeviceSec.this.mPortOtp;
        }

        public String getIdentityByPhoneId(int phoneId) {
            String identity = "";
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, phoneId, "getIdentityByPhoneId: ABSENT");
            String imei = TelephonyAdapterPrimaryDeviceSec.this.mTelephony.getDeviceId(phoneId);
            if (!TextUtils.isEmpty(imei)) {
                identity = "IMEI_" + imei;
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, phoneId, "identity error");
            }
            return identity.replaceAll("[\\W]", "");
        }

        public String getSubscriberId(int subscriptionId) {
            return null;
        }

        public String getMsisdn(int subscriptionId) {
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
