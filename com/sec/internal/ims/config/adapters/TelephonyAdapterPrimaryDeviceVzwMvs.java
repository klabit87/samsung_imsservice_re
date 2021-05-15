package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import com.verizon.loginclient.TokenLoginClient;
import java.nio.charset.Charset;
import java.util.concurrent.Semaphore;

public class TelephonyAdapterPrimaryDeviceVzwMvs extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceVzwMvs.class.getSimpleName();
    protected String mAppToken = null;
    protected TokenLoginClient mAppTokenClient = null;
    protected TokenLoginClient.ILoginClientReceiver mAppTokenClientReceiver = new TokenLoginClient.ILoginClientReceiver() {
        public void onTokenResult(TokenLoginClient.TokenQueryData result) {
            String access$100 = TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId;
            IMSLog.i(access$100, i, "onTokenResult: AppToken is received subId: " + result.subscriptionId);
            String access$1002 = TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId;
            IMSLog.s(access$1002, i2, "onTokenResult: AppToken: " + result.token);
            TelephonyAdapterPrimaryDeviceVzwMvs telephonyAdapterPrimaryDeviceVzwMvs = TelephonyAdapterPrimaryDeviceVzwMvs.this;
            telephonyAdapterPrimaryDeviceVzwMvs.sendMessage(telephonyAdapterPrimaryDeviceVzwMvs.obtainMessage(12, result.token));
        }

        public void onErrorResult(TokenLoginClient.ResultCode status, Throwable ex) {
            String access$100 = TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId;
            IMSLog.i(access$100, i, "onErrorResult: status: " + status);
            TelephonyAdapterPrimaryDeviceVzwMvs telephonyAdapterPrimaryDeviceVzwMvs = TelephonyAdapterPrimaryDeviceVzwMvs.this;
            telephonyAdapterPrimaryDeviceVzwMvs.sendMessage(telephonyAdapterPrimaryDeviceVzwMvs.obtainMessage(12, (Object) null));
        }
    };
    protected Semaphore mAppTokenSemaphore = new Semaphore(0);
    protected int mCurrentAppTokenPermits = 0;

    public TelephonyAdapterPrimaryDeviceVzwMvs(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerPortSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void createPortSmsReceiver() {
        this.mPortSmsReceiver = new PortSmsReceiver();
    }

    /* access modifiers changed from: protected */
    public synchronized void registerAppTokenClient() throws IllegalStateException, IllegalArgumentException, InterruptedException {
        unregisterAppTokenClient();
        this.mAppTokenClient = new TokenLoginClient(this.mContext, this.mAppTokenClientReceiver, this.mLooper, Integer.valueOf(this.mSubId));
        IMSLog.i(LOG_TAG, this.mPhoneId, "registerAppTokenClient: registered");
        this.mAppTokenClient.setTimeout(60000);
        IMSLog.i(LOG_TAG, this.mPhoneId, "registerAppTokenClient: set the timeout to 60 seconds");
    }

    /* access modifiers changed from: protected */
    public synchronized void unregisterAppTokenClient() {
        if (this.mAppTokenClient == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterAppTokenClient: already unregistered");
            return;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterAppTokenClient: unregistered");
        this.mAppTokenClient.cancelQuery();
        this.mAppTokenClient = null;
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        int i2 = msg.what;
        if (i2 == 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "receive port sms");
            if (msg.obj == null) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "invalid sms configuration request");
            } else if (((String) msg.obj).contains(SMS_CONFIGURATION_REQUEST)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "force configuration request");
                IMSLog.c(LogClass.TAPDVM_RECEIVED_PORTSMS, this.mPhoneId + ",REVPO");
                this.mModuleHandler.sendMessage(obtainMessage(21, Integer.valueOf(this.mPhoneId)));
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "invalid port sms");
            }
        } else if (i2 == 12 || i2 == 13) {
            removeMessages(13);
            this.mAppToken = (msg.what != 12 || msg.obj == null) ? null : (String) msg.obj;
            String str2 = LOG_TAG;
            int i3 = this.mPhoneId;
            IMSLog.s(str2, i3, "mAppToken: " + this.mAppToken);
            try {
                this.mCurrentAppTokenPermits = this.mAppTokenSemaphore.availablePermits() + 1;
                String str3 = LOG_TAG;
                int i4 = this.mPhoneId;
                IMSLog.i(str3, i4, "release with mCurrentAppTokenPermits: " + this.mCurrentAppTokenPermits);
                this.mAppTokenSemaphore.release(this.mCurrentAppTokenPermits);
            } catch (IllegalArgumentException e) {
                String str4 = LOG_TAG;
                int i5 = this.mPhoneId;
                IMSLog.i(str4, i5, "can't release with mCurrentAppTokenPermits: " + e.getMessage());
            }
        } else {
            super.handleMessage(msg);
        }
    }

    public String getOtp() {
        return this.mState.getOtp();
    }

    public void cleanup() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup: send apptoken timeout message");
        removeMessages(13);
        sendEmptyMessage(13);
        super.cleanup();
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
        private ReadyState() {
            super();
        }

        public String getOtp() {
            return null;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x00b5, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            com.sec.internal.log.IMSLog.i(com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.access$100(), r4.this$0.mPhoneId, "getAppToken: cannot get apptoken");
            r0.printStackTrace();
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:11:0x00b5 A[ExcHandler: IllegalArgumentException | IllegalStateException | InterruptedException (r0v6 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:1:0x0026] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.String getAppToken(boolean r5) {
            /*
                r4 = this;
                java.lang.String r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this
                int r1 = r1.mPhoneId
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "getAppToken: isRetry: "
                r2.append(r3)
                r2.append(r5)
                java.lang.String r2 = r2.toString()
                com.sec.internal.log.IMSLog.i(r0, r1, r2)
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this
                r1 = 0
                r0.mAppToken = r1
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this
                r1 = 0
                r0.mCurrentAppTokenPermits = r1
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r0.registerAppTokenClient()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                if (r5 == 0) goto L_0x0054
                java.lang.String r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG     // Catch:{ SecurityException -> 0x0042, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ SecurityException -> 0x0042, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                int r1 = r1.mPhoneId     // Catch:{ SecurityException -> 0x0042, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.lang.String r2 = "getAppToken: invalidate apptoken"
                com.sec.internal.log.IMSLog.i(r0, r1, r2)     // Catch:{ SecurityException -> 0x0042, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ SecurityException -> 0x0042, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.verizon.loginclient.TokenLoginClient r0 = r0.mAppTokenClient     // Catch:{ SecurityException -> 0x0042, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r0.invalidateToken()     // Catch:{ SecurityException -> 0x0042, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                goto L_0x0054
            L_0x0042:
                r0 = move-exception
                java.lang.String r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r2 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                int r2 = r2.mPhoneId     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.lang.String r3 = "getAppToken: cannot invalidate apptoken"
                com.sec.internal.log.IMSLog.i(r1, r2, r3)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r0.printStackTrace()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                goto L_0x0055
            L_0x0054:
            L_0x0055:
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r2 = 13
                android.os.Message r1 = r1.obtainMessage(r2)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r2 = 65000(0xfde8, double:3.21143E-319)
                r0.sendMessageDelayed(r1, r2)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.lang.String r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                int r1 = r1.mPhoneId     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.lang.String r2 = "getAppToken: query apptoken"
                com.sec.internal.log.IMSLog.i(r0, r1, r2)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.verizon.loginclient.TokenLoginClient r0 = r0.mAppTokenClient     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r0.queryTokenAsync()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.util.concurrent.Semaphore r1 = r1.mAppTokenSemaphore     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                int r1 = r1.availablePermits()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                int r1 = r1 + 1
                r0.mCurrentAppTokenPermits = r1     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.lang.String r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                int r1 = r1.mPhoneId     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r2.<init>()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.lang.String r3 = "getAppToken: acquire with mCurrentAppTokenPermits: "
                r2.append(r3)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r3 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                int r3 = r3.mCurrentAppTokenPermits     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r2.append(r3)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.lang.String r2 = r2.toString()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.log.IMSLog.i(r0, r1, r2)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                java.util.concurrent.Semaphore r0 = r0.mAppTokenSemaphore     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                int r1 = r1.mCurrentAppTokenPermits     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                r0.acquire(r1)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b5 }
                goto L_0x00c6
            L_0x00b3:
                r0 = move-exception
                goto L_0x00d1
            L_0x00b5:
                r0 = move-exception
                java.lang.String r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG     // Catch:{ all -> 0x00b3 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r2 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this     // Catch:{ all -> 0x00b3 }
                int r2 = r2.mPhoneId     // Catch:{ all -> 0x00b3 }
                java.lang.String r3 = "getAppToken: cannot get apptoken"
                com.sec.internal.log.IMSLog.i(r1, r2, r3)     // Catch:{ all -> 0x00b3 }
                r0.printStackTrace()     // Catch:{ all -> 0x00b3 }
            L_0x00c6:
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this
                r0.unregisterAppTokenClient()
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this
                java.lang.String r0 = r0.mAppToken
                return r0
            L_0x00d1:
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.this
                r1.unregisterAppTokenClient()
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs.ReadyState.getAppToken(boolean):java.lang.String");
        }
    }

    private class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        private AbsentState() {
            super();
        }

        public String getOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG, TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId, "getOtp method can't run in absentState");
            return null;
        }

        public String getIdentityByPhoneId(int phoneId) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG, TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId, "getIdentityByPhoneId method can't run in absentState");
            return null;
        }

        public String getDeviceId(int slotId) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG, TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId, "getDeviceId method can't run in absentState");
            return null;
        }
    }

    private class PortSmsReceiver extends TelephonyAdapterPrimaryDeviceBase.PortSmsReceiverBase {
        private PortSmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            Intent intent2 = intent;
            String format = intent2.getStringExtra("format");
            String access$100 = TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId;
            IMSLog.i(access$100, i, "readMessageFromSMSIntent: format: " + format);
            if (SmsMessage.FORMAT_3GPP2.equals(format)) {
                try {
                    Object[] messages = (Object[]) intent2.getSerializableExtra("pdus");
                    if (messages != null && messages[0] != null) {
                        String message = new String((byte[]) messages[0], Charset.forName("UTF-8"));
                        String access$1002 = TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG;
                        int i2 = TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId;
                        IMSLog.i(access$1002, i2, "readMessageFromSMSIntent: message: " + message);
                        IMSLog.c(LogClass.TAPDVM_MSG, TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId + ",MSG:" + message);
                        TelephonyAdapterPrimaryDeviceVzwMvs telephonyAdapterPrimaryDeviceVzwMvs = TelephonyAdapterPrimaryDeviceVzwMvs.this;
                        telephonyAdapterPrimaryDeviceVzwMvs.sendMessage(telephonyAdapterPrimaryDeviceVzwMvs.obtainMessage(0, message));
                    }
                } catch (ClassCastException e) {
                    String access$1003 = TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG;
                    int i3 = TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId;
                    IMSLog.i(access$1003, i3, "readMessageFromSMSIntent: ClassCastException: cannot get message" + e.getMessage());
                }
            } else {
                android.telephony.SmsMessage[] smss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                if (smss != null && smss[0] != null) {
                    android.telephony.SmsMessage sms = smss[0];
                    String message2 = sms.getDisplayMessageBody();
                    if (message2 == null) {
                        message2 = new String(sms.getUserData(), Charset.forName("UTF-8"));
                    }
                    String access$1004 = TelephonyAdapterPrimaryDeviceVzwMvs.LOG_TAG;
                    int i4 = TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId;
                    IMSLog.i(access$1004, i4, "readMessageFromSMSIntent: message: " + message2);
                    IMSLog.c(LogClass.TAPDVM_MSG, TelephonyAdapterPrimaryDeviceVzwMvs.this.mPhoneId + ",MSG:" + message2);
                    TelephonyAdapterPrimaryDeviceVzwMvs telephonyAdapterPrimaryDeviceVzwMvs2 = TelephonyAdapterPrimaryDeviceVzwMvs.this;
                    telephonyAdapterPrimaryDeviceVzwMvs2.sendMessage(telephonyAdapterPrimaryDeviceVzwMvs2.obtainMessage(0, message2));
                }
            }
        }
    }
}
