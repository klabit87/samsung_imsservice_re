package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;
import java.util.Calendar;

public class TelephonyAdapterPrimaryDeviceUp extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceUp.class.getSimpleName();

    public TelephonyAdapterPrimaryDeviceUp(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerSmsReceiver();
        registerPortSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void createSmsReceiver() {
        this.mSmsReceiver = new SmsReceiver();
    }

    /* access modifiers changed from: protected */
    public void createPortSmsReceiver() {
        this.mPortSmsReceiver = new PortSmsReceiver();
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        int i2 = msg.what;
        if (i2 == 3) {
            this.mOtp = null;
            try {
                String str2 = LOG_TAG;
                int i3 = this.mPhoneId;
                IMSLog.i(str2, i3, "semaphore release with mCurrentPermits: " + this.mCurrentPermits);
                this.mSemaphore.release(this.mCurrentPermits);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else if (i2 == 4) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "receive port sms");
            if (msg.obj == null) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "no SMS data!");
            } else if (((String) msg.obj).contains(SMS_CONFIGURATION_REQUEST)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "request force configuration");
                this.mModuleHandler.sendEmptyMessage(4);
            } else {
                removeMessages(5);
                this.mPortOtp = (String) msg.obj;
                String str3 = LOG_TAG;
                int i4 = this.mPhoneId;
                IMSLog.i(str3, i4, "mPortOtp: " + IMSLog.checker(this.mPortOtp));
                this.mPortOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                try {
                    String str4 = LOG_TAG;
                    int i5 = this.mPhoneId;
                    IMSLog.i(str4, i5, "otp received: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                    this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                    this.mCurrentPortPermits = 0;
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                }
            }
        } else if (i2 == 5) {
            this.mPortOtp = null;
            try {
                String str5 = LOG_TAG;
                int i6 = this.mPhoneId;
                IMSLog.i(str5, i6, "otp timeout: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                this.mCurrentPortPermits = 0;
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            }
        } else if (i2 != 8) {
            super.handleMessage(msg);
        } else if (this.mIsWaitingForOtp) {
            notifyAutoConfigurationListener(50, true);
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

    protected class ReadyState extends TelephonyAdapterPrimaryDeviceBase.ReadyState {
        protected ReadyState() {
            super();
        }

        public String getSmsDestPort() {
            return SMS_DEST_PORT;
        }

        public String getSmsOrigPort() {
            return SMS_ORIG_PORT;
        }

        public String getExistingOtp() {
            TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp = false;
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceUp.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceUp.this.mOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getExistingOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mOtp;
        }

        public String getExistingPortOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceUp.this.mPortOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceUp.this.mPortOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getExistingPortOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mPortOtp;
        }

        public String getOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getOtp");
            TelephonyAdapterPrimaryDeviceUp.this.mCurrentPermits = 0;
            TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp = true;
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            telephonyAdapterPrimaryDeviceUp.sendMessageDelayed(telephonyAdapterPrimaryDeviceUp.obtainMessage(8), 300);
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp2 = TelephonyAdapterPrimaryDeviceUp.this;
            telephonyAdapterPrimaryDeviceUp2.sendMessageDelayed(telephonyAdapterPrimaryDeviceUp2.obtainMessage(3), 310000);
            try {
                TelephonyAdapterPrimaryDeviceUp.this.mCurrentPermits = TelephonyAdapterPrimaryDeviceUp.this.mSemaphore.availablePermits() + 1;
                String access$100 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                IMSLog.i(access$100, i, "semaphore acquire with mCurrentPermits: " + TelephonyAdapterPrimaryDeviceUp.this.mCurrentPermits);
                TelephonyAdapterPrimaryDeviceUp.this.mSemaphore.acquire(TelephonyAdapterPrimaryDeviceUp.this.mCurrentPermits);
            } catch (InterruptedException e) {
                TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp = false;
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp = false;
                e2.printStackTrace();
            }
            TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp = false;
            return TelephonyAdapterPrimaryDeviceUp.this.mOtp;
        }

        public String getPortOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getPortOtp");
            TelephonyAdapterPrimaryDeviceUp.this.mCurrentPortPermits = 0;
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            telephonyAdapterPrimaryDeviceUp.sendMessageDelayed(telephonyAdapterPrimaryDeviceUp.obtainMessage(5), 300000);
            try {
                TelephonyAdapterPrimaryDeviceUp.this.mCurrentPortPermits = TelephonyAdapterPrimaryDeviceUp.this.mPortOtpSemaphore.availablePermits() + 1;
                String access$100 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                IMSLog.i(access$100, i, "getPortOtp: semaphore acquire with mCurrentPortPermits: " + TelephonyAdapterPrimaryDeviceUp.this.mCurrentPortPermits);
                TelephonyAdapterPrimaryDeviceUp.this.mPortOtpSemaphore.acquire(TelephonyAdapterPrimaryDeviceUp.this.mCurrentPortPermits);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
            return TelephonyAdapterPrimaryDeviceUp.this.mPortOtp;
        }

        public void registerAutoConfigurationListener(IAutoConfigurationListener listener) {
            if (listener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceUp.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener != null) {
                    String access$100 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                    IMSLog.i(access$100, i, "register listener: " + listener);
                    IMSLog.c(LogClass.TAPDU_LISTNER, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId + "," + listener);
                    TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.register(listener);
                    if (!TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.isEmpty()) {
                        for (Integer intValue : TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.keySet()) {
                            int pn = intValue.intValue();
                            notifyAutoConfigurationListener(pn, ((Boolean) TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.get(Integer.valueOf(pn))).booleanValue());
                        }
                    }
                }
            }
        }

        public void unregisterAutoConfigurationListener(IAutoConfigurationListener listener) {
            if (listener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceUp.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener != null) {
                    String access$100 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                    IMSLog.i(access$100, i, "unregister listener: " + listener);
                    TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.unregister(listener);
                    TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.clear();
                }
            }
        }

        public void notifyAutoConfigurationListener(int type, boolean result) {
            String access$100 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
            IMSLog.i(access$100, i, "notifyAutoConfigurationListener: type: " + type + ", result: " + result);
            if (type != 50 && type != 52) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "unknown notification type");
                return;
            } else if (type != 50 || TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp) {
                synchronized (TelephonyAdapterPrimaryDeviceUp.this.mLock) {
                    if (TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener == null) {
                        IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "mAutoConfigurationListener: empty");
                        return;
                    }
                    try {
                        int length = TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.beginBroadcast();
                        String access$1002 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                        int i2 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                        IMSLog.i(access$1002, i2, "listener length: " + length);
                        if (type == 50) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "onVerificationCodeNeeded");
                            IMSLog.c(LogClass.TAPDU_OTP_NEEDED, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId + ",VCN,LEN:" + length);
                        } else {
                            String access$1003 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                            int i3 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                            IMSLog.i(access$1003, i3, "onAutoConfigurationCompleted, result: " + result);
                            IMSLog.c(LogClass.TAPDU_ACS_RESULT, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId + ",ACS:" + result + ",LEN:" + length);
                        }
                        if (length == 0) {
                            String access$1004 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                            int i4 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                            IMSLog.i(access$1004, i4, "Listener not registered yet. Postpone notify later: " + type);
                            if (type == 52) {
                                TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.clear();
                            }
                            TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.put(Integer.valueOf(type), Boolean.valueOf(result));
                        }
                        for (int index = 0; index < length; index++) {
                            IAutoConfigurationListener listener = TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.getBroadcastItem(index);
                            if (type == 50) {
                                listener.onVerificationCodeNeeded();
                            } else {
                                listener.onAutoConfigurationCompleted(result);
                                TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.clear();
                            }
                        }
                    } catch (RemoteException | IllegalStateException | NullPointerException e) {
                        String access$1005 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                        int i5 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                        IMSLog.i(access$1005, i5, "beginBroadcast Exception: " + e.getMessage());
                    }
                    try {
                        TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.finishBroadcast();
                    } catch (IllegalStateException e2) {
                        String access$1006 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                        int i6 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                        IMSLog.i(access$1006, i6, "finishBroadcast Exception: " + e2.getMessage());
                    }
                }
            } else {
                String access$1007 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                int i7 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                IMSLog.i(access$1007, i7, "ignore notification type, mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp);
                return;
            }
            return;
        }

        public void sendVerificationCode(String value) {
            String access$100 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
            IMSLog.i(access$100, i, "sendVerificationCode value: " + value);
            IMSLog.c(LogClass.TAPDU_SEND_OTP, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId + ",VC:" + value);
            TelephonyAdapterPrimaryDeviceUp.this.removeMessages(3);
            String access$1002 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
            IMSLog.i(access$1002, i2, "mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp);
            if (TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp) {
                TelephonyAdapterPrimaryDeviceUp.this.mOtp = value;
                if (TelephonyAdapterPrimaryDeviceUp.this.mOtp != null) {
                    TelephonyAdapterPrimaryDeviceUp.this.mOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                }
                try {
                    String access$1003 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                    int i3 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                    IMSLog.i(access$1003, i3, "semaphore release with mCurrentPermits: " + TelephonyAdapterPrimaryDeviceUp.this.mCurrentPermits);
                    TelephonyAdapterPrimaryDeviceUp.this.mSemaphore.release(TelephonyAdapterPrimaryDeviceUp.this.mCurrentPermits);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        protected AbsentState() {
            super();
        }

        public String getSmsDestPort() {
            return SMS_DEST_PORT;
        }

        public String getSmsOrigPort() {
            return SMS_ORIG_PORT;
        }

        public String getExistingOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceUp.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceUp.this.mOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getExistingOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mOtp;
        }

        public String getExistingPortOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceUp.this.mPortOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceUp.this.mPortOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getExistingPortOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mPortOtp;
        }

        public String getOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceUp.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceUp.this.mOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mOtp;
        }

        public String getPortOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceUp.this.mPortOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceUp.this.mPortOtpReceivedTime + 3000) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getPortOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mPortOtp;
        }

        public String getIdentityByPhoneId(int phoneId) {
            return null;
        }

        public String getDeviceId(int slotId) {
            return null;
        }
    }

    private class SmsReceiver extends TelephonyAdapterPrimaryDeviceBase.SmsReceiverBase {
        private static final String SMS_OTP_FORMAT_IOT_SERVER = "your messenger verification code is";
        private static final String SMS_OTP_FORMAT_PROD_SERVER = "messenger's enhanced features have been enabled";
        private static final String SMS_OTP_NEW_FORMAT_GOOGLE_SERVER = "confirmation id";
        private static final String SMS_OTP_NEW_FORMAT_GOOGLE_SERVER_AMX = "activation code is";
        private static final String SMS_OTP_NEW_FORMAT_NEWPACE_SERVER = "the verification code for new messaging features";
        private static final String SMS_OTP_OLD_FORMAT_NEWPACE_SERVER = "here is your krypton code. please be aware that this code expires after 15 minutes then re-authentication might be needed";
        private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

        public SmsReceiver() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "SmsReceiver");
            this.mIntentFilter = new IntentFilter();
            this.mIntentFilter.addAction(SMS_RECEIVED_ACTION);
        }

        public void onReceive(Context context, Intent intent) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "onReceive");
            if (!intent.getAction().equals(SMS_RECEIVED_ACTION)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "invalid intent");
                return;
            }
            StringBuilder body = new StringBuilder();
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            if (pdus == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "invalid pdus");
                return;
            }
            String format = intent.getStringExtra("format");
            if (TextUtils.isEmpty(format)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "invalid format");
                return;
            }
            SmsMessage[] message = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                message[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
            }
            for (SmsMessage m : message) {
                body.append(m.getDisplayMessageBody());
            }
            String smsBody = body.toString();
            if (TextUtils.isEmpty(smsBody)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "invalid smsBody");
                return;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "smsBody: " + IMSLog.checker(smsBody));
            String otp = parseOtp(smsBody);
            if (otp == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "failed to parse smsBody, wait for next one");
            } else if (TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp) {
                TelephonyAdapterPrimaryDeviceUp.this.mOtp = otp;
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "mOtp: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceUp.this.mOtp));
                TelephonyAdapterPrimaryDeviceUp.this.mOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
            }
        }

        private String parseOtp(String body) {
            String otp = null;
            int length = body.length();
            if (body.toLowerCase().contains(SMS_OTP_FORMAT_PROD_SERVER) || body.toLowerCase().contains(SMS_OTP_FORMAT_IOT_SERVER)) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < length; i++) {
                    char ch = body.charAt(i);
                    if (ch >= '0' && ch <= '9') {
                        sb.append(ch);
                    }
                }
                otp = sb.toString();
            } else if (body.toLowerCase().contains(SMS_OTP_NEW_FORMAT_GOOGLE_SERVER)) {
                StringBuffer sb2 = new StringBuffer();
                int i2 = body.indexOf(58);
                while (i2 > 0 && i2 < length && i2 < body.indexOf(41)) {
                    sb2.append(body.charAt(i2));
                    i2++;
                }
                otp = sb2.toString();
            } else if (body.toLowerCase().contains(SMS_OTP_NEW_FORMAT_GOOGLE_SERVER_AMX)) {
                StringBuffer sb3 = new StringBuffer();
                int i3 = body.indexOf(40) + 1;
                while (i3 > 0 && i3 < length && i3 < body.indexOf(41)) {
                    sb3.append(body.charAt(i3));
                    i3++;
                }
                otp = sb3.toString();
            } else if (body.toLowerCase().contains(SMS_OTP_OLD_FORMAT_NEWPACE_SERVER) || body.toLowerCase().contains(SMS_OTP_NEW_FORMAT_NEWPACE_SERVER)) {
                StringBuffer sb4 = new StringBuffer();
                int i4 = body.indexOf(58);
                while (i4 > 0 && i4 < length) {
                    char ch2 = body.charAt(i4);
                    if (ch2 >= '0' && ch2 <= '9') {
                        sb4.append(ch2);
                    }
                    i4++;
                }
                otp = sb4.toString();
            }
            String access$100 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
            int i5 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
            IMSLog.i(access$100, i5, "parseOtp: " + IMSLog.checker(otp));
            return otp;
        }
    }

    private class PortSmsReceiver extends TelephonyAdapterPrimaryDeviceBase.PortSmsReceiverBase {
        private PortSmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage[] smss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (smss != null && smss[0] != null) {
                SmsMessage sms = smss[0];
                int phoneId = SimManagerFactory.getSlotId(intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1));
                String message = sms.getDisplayMessageBody();
                if (message == null) {
                    message = new String(sms.getUserData(), Charset.forName("UTF-16"));
                }
                Message msg = TelephonyAdapterPrimaryDeviceUp.this.obtainMessage();
                msg.what = 4;
                msg.arg1 = phoneId;
                msg.obj = message;
                if (TelephonyAdapterPrimaryDeviceUp.this.mPhoneId == phoneId) {
                    TelephonyAdapterPrimaryDeviceUp.this.sendMessage(msg);
                }
            }
        }
    }

    public String getOtp() {
        return this.mState.getOtp();
    }
}
