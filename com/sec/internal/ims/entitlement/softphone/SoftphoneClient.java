package com.sec.internal.ims.entitlement.softphone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.SparseArray;
import com.google.gson.Gson;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.softphone.requests.AddAddressRequest;
import com.sec.internal.ims.entitlement.softphone.requests.AddressValidationRequest;
import com.sec.internal.ims.entitlement.softphone.requests.ExchangeForAccessTokenRequest;
import com.sec.internal.ims.entitlement.softphone.requests.ProvisionAccountRequest;
import com.sec.internal.ims.entitlement.softphone.requests.ReleaseImsNetworkIdentifiersRequest;
import com.sec.internal.ims.entitlement.softphone.requests.RevokeTokenRequest;
import com.sec.internal.ims.entitlement.softphone.requests.SendSMSRequest;
import com.sec.internal.ims.entitlement.softphone.responses.AccessTokenResponse;
import com.sec.internal.ims.entitlement.softphone.responses.AddAddressResponse;
import com.sec.internal.ims.entitlement.softphone.responses.AddressValidationResponse;
import com.sec.internal.ims.entitlement.softphone.responses.AkaAuthenticationResponse;
import com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse;
import com.sec.internal.ims.entitlement.softphone.responses.CallWaitingResponse;
import com.sec.internal.ims.entitlement.softphone.responses.ImsNetworkIdentifiersResponse;
import com.sec.internal.ims.entitlement.softphone.responses.SoftphoneResponse;
import com.sec.internal.ims.entitlement.softphone.responses.TermsAndConditionsResponse;
import com.sec.internal.ims.entitlement.util.EncryptionHelper;
import com.sec.internal.ims.entitlement.util.GeolocationUpdateFlow;
import com.sec.internal.ims.entitlement.util.SharedPrefHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IndentingPrintWriter;
import com.sec.vsim.attsoftphone.IEmergencyServiceListener;
import com.sec.vsim.attsoftphone.IProgressListener;
import com.sec.vsim.attsoftphone.ISupplementaryServiceListener;
import com.sec.vsim.attsoftphone.data.AddressValidationNotify;
import com.sec.vsim.attsoftphone.data.CallForwardingInfo;
import com.sec.vsim.attsoftphone.data.CallWaitingInfo;
import com.sec.vsim.attsoftphone.data.DeviceInfo;
import com.sec.vsim.attsoftphone.data.GeneralNotify;
import com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.SecretKey;
import org.json.JSONException;
import org.json.JSONObject;

public class SoftphoneClient {
    public final String LOG_TAG;
    private String mAccessToken = null;
    private String mAccessTokenType = null;
    /* access modifiers changed from: private */
    public final String mAccountId;
    private final AlarmManager mAlarmManager;
    private String mAppKey;
    private String mAppSecret;
    private AtomicBoolean mAutoRetry = new AtomicBoolean(false);
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final EncryptionHelper mEncryptionHelper;
    private int mEnvironment;
    public SimpleEventLog mEventLog;
    public String mHost;
    protected ImsNetworkIdentity mIdentity = new ImsNetworkIdentity();
    protected ImsManager mImsManager;
    protected final IImsRegistrationListener mImsRegistrationListener = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration reg) {
            SimpleEventLog simpleEventLog = SoftphoneClient.this.mEventLog;
            simpleEventLog.logAndAdd("onRegistered(): imsprofile id: " + reg.getImsProfile().getId() + ", mProfileId: " + SoftphoneClient.this.mProfileId);
            if (reg.getImsProfile().getId() == SoftphoneClient.this.mProfileId) {
                SoftphoneClient softphoneClient = SoftphoneClient.this;
                softphoneClient.updateAccountStatus(softphoneClient.mAccountId, 5);
                ImsUri uri = reg.getPreferredImpu().getUri();
                String msisdn = "";
                if (uri != null) {
                    msisdn = uri.getMsisdn();
                }
                SoftphoneClient.this.mStateHandler.sendMessage(1016, (Object) msisdn);
            }
        }

        public void onDeregistered(ImsRegistration reg, ImsRegistrationError errorCode) {
            SimpleEventLog simpleEventLog = SoftphoneClient.this.mEventLog;
            simpleEventLog.logAndAdd("onDeregistered(): imsprofile id: " + reg.getImsProfile().getId() + ", mProfileId: " + SoftphoneClient.this.mProfileId);
            if (reg.getImsProfile().getId() == SoftphoneClient.this.mProfileId) {
                SoftphoneClient softphoneClient = SoftphoneClient.this;
                softphoneClient.updateAccountStatus(softphoneClient.mAccountId, 4);
                SoftphoneClient.this.mStateHandler.sendMessage(1017, errorCode.getSipErrorCode(), -1, reg.getOwnNumber() != null ? reg.getOwnNumber() : "");
                if (SoftphoneClient.this.mLoggedOut) {
                    SoftphoneClient.this.mProfileId = -1;
                }
            }
        }
    };
    protected AtomicBoolean mIsRecovery = new AtomicBoolean(false);
    protected boolean mIsRegisterPending = false;
    private SparseArray<Object> mListeners = new SparseArray<>();
    protected boolean mLoggedOut = true;
    protected int mProfileId = -1;
    private ConcurrentHashMap<Integer, IProgressListener> mProgressListeners = new ConcurrentHashMap<>();
    protected PendingIntent mRefreshIdentityIntent = null;
    private String mRefreshToken = null;
    protected PendingIntent mRefreshTokenIntent = null;
    private final SoftphoneRequestBuilder mRequestBuilder;
    private PendingIntent mResendSmsIntent = null;
    private SecretKey mSecretKey;
    protected SharedPrefHelper mSharedPrefHelper;
    private final SoftphoneEmergencyService mSoftphoneEmergencyServcie;
    protected ContentObserver mSoftphoneLabelObserver;
    protected SoftphoneStateHandler mStateHandler;
    private ConcurrentHashMap<Integer, ISupplementaryServiceListener> mSupplementaryServiceListeners = new ConcurrentHashMap<>();
    private String mTGaurdAppId = null;
    private String mTGaurdToken = null;
    private final TelephonyManager mTelephonyManager;
    private long mTokenExpiresTime = -1;
    private UserHandle mUserHandle = null;
    /* access modifiers changed from: private */
    public int mUserId = 0;
    private final UserManager mUserManager;

    public SoftphoneClient(String accountId, Context context, Looper looper) {
        this.mContext = context;
        this.mAccountId = accountId;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY);
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        int currentUser = Extensions.ActivityManager.getCurrentUser();
        this.mUserId = currentUser;
        UserHandle userForSerialNumber = this.mUserManager.getUserForSerialNumber((long) currentUser);
        this.mUserHandle = userForSerialNumber;
        if (userForSerialNumber == null) {
            this.mUserHandle = ContextExt.CURRENT_OR_SELF;
        }
        this.LOG_TAG = SoftphoneClient.class.getSimpleName() + '-' + this.mAccountId + '-' + this.mUserId;
        this.mEventLog = new SimpleEventLog(context, this.LOG_TAG, 200);
        this.mSharedPrefHelper = new SharedPrefHelper(SoftphoneNamespaces.SoftphoneSharedPref.SHARED_PREF_NAME);
        this.mEncryptionHelper = EncryptionHelper.getInstance(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
        try {
            this.mSecretKey = EncryptionHelper.generateKey(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "exception" + e.getMessage());
        }
        ImsManager imsManager = new ImsManager(this.mContext, new ImsManager.ConnectionListener() {
            public void onConnected() {
                SoftphoneClient.this.mEventLog.logAndAdd("Connected to ImsService.");
                SoftphoneClient.this.mImsManager.registerImsRegistrationListener(SoftphoneClient.this.mImsRegistrationListener);
                if (SoftphoneClient.this.mIsRegisterPending) {
                    SoftphoneClient.this.handleTryRegisterRequest();
                }
            }

            public void onDisconnected() {
                SoftphoneClient.this.mEventLog.logAndAdd("Disconnected from ImsService.");
            }
        });
        this.mImsManager = imsManager;
        imsManager.connectService();
        this.mStateHandler = new SoftphoneStateHandler(looper, context, accountId, this);
        this.mSoftphoneEmergencyServcie = new SoftphoneEmergencyService(context);
        this.mRequestBuilder = new SoftphoneRequestBuilder(context);
        this.mSoftphoneLabelObserver = new ContentObserver(new Handler(looper)) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
            }

            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange);
                Uri accountLabelUri = SoftphoneContract.SoftphoneAccount.buildAccountLabelUri(SoftphoneClient.this.mAccountId, (long) SoftphoneClient.this.mUserId);
                SimpleEventLog simpleEventLog = SoftphoneClient.this.mEventLog;
                simpleEventLog.logAndAdd("mSoftphoneLabelObserver onChange: " + uri);
                if (accountLabelUri.equals(uri)) {
                    SoftphoneClient.this.mStateHandler.sendMessage(1019);
                }
            }
        };
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        this.mEventLog.logAndAdd("finalize()");
        super.finalize();
    }

    public String getAccessToken() {
        return this.mAccessToken;
    }

    public String getAccessTokenType() {
        return this.mAccessTokenType;
    }

    public int getProfileId() {
        return this.mProfileId;
    }

    public boolean getAutoRetryComSet(boolean expect, boolean update) {
        return this.mAutoRetry.compareAndSet(expect, update);
    }

    public void onUserSwitchedAway() {
        this.mEventLog.logAndAdd("onUserSwitchedAway()");
        this.mImsManager.unregisterImsRegistrationListener(this.mImsRegistrationListener);
    }

    public void onUserSwitchedBack() {
        this.mEventLog.logAndAdd("onUserSwitchedBack()");
        this.mImsManager.registerImsRegistrationListener(this.mImsRegistrationListener);
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_USER_SWITCH_BACK);
    }

    public void updateAccountStatus(String accountId, int status) {
        Uri uri;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("updateAccountStatus(): " + status);
        ContentValues values = new ContentValues();
        if (status == 0) {
            uri = SoftphoneContract.SoftphoneAccount.buildDeActivateAccountUri(accountId);
        } else if (status == 2) {
            uri = SoftphoneContract.SoftphoneAccount.buildActivateAccountUri(accountId);
        } else if (status != 5) {
            Uri uri2 = SoftphoneContract.SoftphoneAccount.buildAccountIdUri(accountId);
            values.put("status", Integer.valueOf(status));
            this.mContext.getContentResolver().update(uri2, values, "status > ?", new String[]{String.valueOf(0)});
            return;
        } else {
            uri = SoftphoneContract.SoftphoneAccount.buildRegisteredAccountUri(accountId);
        }
        if (uri != null) {
            if (this.mContext.getContentResolver().update(uri, values, "userid = ?", new String[]{String.valueOf(this.mUserId)}) == 0) {
                values.put(SoftphoneContract.AccountColumns.USERID, Integer.valueOf(this.mUserId));
                this.mContext.getContentResolver().insert(uri, values);
            }
        }
    }

    private void updateAccountInfo(String impi, String impu) {
        Uri uri = SoftphoneContract.SoftphoneAccount.buildAccountIdUri(this.mAccountId, (long) this.mUserId);
        ContentValues values = new ContentValues();
        values.put("impi", impi);
        values.put("msisdn", impu.substring(impu.indexOf(":") + 1, impu.indexOf("@")));
        values.put(SoftphoneContract.AccountColumns.SECRET_KEY, Base64.encodeToString(this.mSecretKey.getEncoded(), 2));
        values.put("environment", Integer.valueOf(this.mEnvironment));
        this.mContext.getContentResolver().update(uri, values, (String) null, (String[]) null);
    }

    private void saveAccountIdentities(String impi, String impu, String fqdn) {
        Map<String, String> mp = new HashMap<>();
        mp.put(this.mAccountId + ":" + this.mUserId + ":" + "impi", this.mEncryptionHelper.encrypt(impi, this.mSecretKey));
        mp.put(this.mAccountId + ":" + this.mUserId + ":" + "impu", this.mEncryptionHelper.encrypt(impu, this.mSecretKey));
        mp.put(this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_FQDN, this.mEncryptionHelper.encrypt(fqdn, this.mSecretKey));
        this.mSharedPrefHelper.save(this.mContext, mp);
    }

    private int storeTokens(String accessToken, String tokenType) {
        Uri uri = SoftphoneContract.SoftphoneAccount.buildAccountIdUri(this.mAccountId, (long) this.mUserId);
        ContentValues values = new ContentValues();
        values.put("access_token", accessToken);
        values.put(SoftphoneContract.AccountColumns.TOKEN_TYPE, tokenType);
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        sharedPrefHelper.save(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token", this.mEncryptionHelper.encrypt(this.mRefreshToken, this.mSecretKey));
        return this.mContext.getContentResolver().update(uri, values, (String) null, (String[]) null);
    }

    private void saveTokens(String accessToken, String accessTokenType, long expireSeconds, String refreshToken) {
        this.mAccessToken = accessToken;
        this.mAccessTokenType = accessTokenType;
        this.mTokenExpiresTime = expireSeconds;
        this.mRefreshToken = refreshToken;
        storeTokens(this.mEncryptionHelper.encrypt(accessToken, this.mSecretKey), this.mEncryptionHelper.encrypt(this.mAccessTokenType, this.mSecretKey));
    }

    private void scheduleRefreshTokenAlarm(long afterMillis, int attempt) {
        Intent intent = new Intent();
        intent.setAction("refresh_token");
        intent.putExtra(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, attempt);
        this.mRefreshTokenIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
        this.mAlarmManager.setAndAllowWhileIdle(0, System.currentTimeMillis() + afterMillis, this.mRefreshTokenIntent);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("refresh token after " + afterMillis + ", attempt: " + attempt);
    }

    public synchronized void scheduleSmsAlarm() {
        Calendar calendar = Calendar.getInstance();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("current time: " + calendar.get(2) + "-" + calendar.get(5) + "-" + calendar.get(1) + " " + calendar.get(10) + ":" + calendar.get(12) + ":" + calendar.get(13));
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        StringBuilder sb = new StringBuilder();
        sb.append(this.mAccountId);
        sb.append(":");
        sb.append(this.mUserId);
        sb.append(":");
        sb.append(SoftphoneNamespaces.SoftphoneSharedPref.LAST_SMS_TIME);
        sharedPrefHelper.save(context, sb.toString(), calendar.getTimeInMillis());
        calendar.add(5, 30);
        Intent intent = new Intent();
        intent.setAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_RESEND_SMS);
        this.mResendSmsIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
        this.mAlarmManager.setAndAllowWhileIdle(0, calendar.getTimeInMillis(), this.mResendSmsIntent);
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("schedule to send SMS at: " + calendar.get(2) + "-" + calendar.get(5) + "-" + calendar.get(1) + " " + calendar.get(10) + ":" + calendar.get(12) + ":" + calendar.get(13));
    }

    private void resumeSmsAlarm(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("last sms time: " + calendar.get(2) + "-" + calendar.get(5) + "-" + calendar.get(1) + " " + calendar.get(10) + ":" + calendar.get(12) + ":" + calendar.get(13));
        calendar.add(5, 30);
        Intent intent = new Intent();
        intent.setAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_RESEND_SMS);
        this.mResendSmsIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
        this.mAlarmManager.setAndAllowWhileIdle(0, calendar.getTimeInMillis(), this.mResendSmsIntent);
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("schedule to send SMS at: " + calendar.get(2) + "-" + calendar.get(5) + "-" + calendar.get(1) + " " + calendar.get(10) + ":" + calendar.get(12) + ":" + calendar.get(13));
    }

    private void scheduleRefreshIdentityAlarm(long afterMillis) {
        Intent intent = new Intent();
        intent.setAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_REFRESH_IDENTITY);
        this.mRefreshIdentityIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
        this.mAlarmManager.setAndAllowWhileIdle(0, System.currentTimeMillis() + afterMillis, this.mRefreshIdentityIntent);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("refresh identity after " + afterMillis);
    }

    private void saveListener(int transactionId, Object obj) {
        this.mListeners.append(transactionId, obj);
    }

    private Object findAndRemoveListener(int transactionId) {
        Object obj = this.mListeners.get(transactionId);
        this.mListeners.delete(transactionId);
        return obj;
    }

    private void setupEnvironment(int environment) {
        this.mEnvironment = environment;
        this.mAppKey = SoftphoneAuthUtils.setupAppKey(environment, Build.MODEL);
        this.mAppSecret = SoftphoneAuthUtils.setupAppSecret(environment, Build.MODEL);
        this.mHost = SoftphoneNamespaces.SoftphoneSettings.PROD_HOST;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setupEnvironment(): appKey: " + IMSLog.checker(this.mAppKey) + ", appSecret: " + IMSLog.checker(this.mAppSecret));
    }

    private void removeSharedPreferences() {
        this.mEventLog.logAndAdd("removeSharedPreferences()");
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        sharedPrefHelper.remove(context, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_TOKEN, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_APPID, this.mAccountId + ":" + this.mUserId + ":" + "environment", this.mAccountId + ":" + this.mUserId + ":" + "impi", this.mAccountId + ":" + this.mUserId + ":" + "impu", this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_FQDN, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token", this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_PD_COOKIES, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.LAST_SMS_TIME);
    }

    public boolean isTarget(String impi) {
        boolean isTarget = false;
        if (!this.mIdentity.impiEmpty() && this.mIdentity.getImpi().equalsIgnoreCase(impi)) {
            isTarget = true;
        }
        String str = this.LOG_TAG;
        IMSLog.s(str, "isTarget(): impi: " + impi + ", " + isTarget);
        return isTarget;
    }

    private void resetAccountStatus() {
        updateAccountStatus(this.mAccountId, 0);
        broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_REQUEST_LOGOUT, (String) null);
        PendingIntent pendingIntent = this.mRefreshTokenIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mRefreshTokenIntent = null;
        }
        PendingIntent pendingIntent2 = this.mResendSmsIntent;
        if (pendingIntent2 != null) {
            this.mAlarmManager.cancel(pendingIntent2);
            this.mResendSmsIntent = null;
        }
    }

    public void registerProgressListener(int callerUid, IProgressListener listener) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("registerProgressListener current size: " + this.mProgressListeners.size() + " UID: " + callerUid + " listener: " + listener);
        this.mProgressListeners.put(Integer.valueOf(callerUid), listener);
    }

    public void deregisterProgressListener(int callerUid) {
        this.mProgressListeners.remove(Integer.valueOf(callerUid));
    }

    public void registerSupplementaryServiceListener(int callerUid, ISupplementaryServiceListener listener) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("SupplementaryServiceListener current size: " + this.mSupplementaryServiceListeners.size() + " UID: " + callerUid + " listener: " + listener);
        this.mSupplementaryServiceListeners.put(Integer.valueOf(callerUid), listener);
    }

    public void deregisterSupplementaryServiceListener(int callerUid) {
        this.mSupplementaryServiceListeners.remove(Integer.valueOf(callerUid));
    }

    /* access modifiers changed from: package-private */
    public void restoreAccessToken(String authorizationCode, String accountId, boolean autoRegister, String tGuardAppId, int environment) {
        if (!tokenExist() || !this.mIsRecovery.compareAndSet(false, true)) {
            int i = environment;
            if (!this.mIsRecovery.get()) {
                exchangeForAccessToken(authorizationCode, accountId, autoRegister, tGuardAppId, environment, 0, SoftphoneNamespaces.mTimeoutType4[0]);
                return;
            }
            return;
        }
        this.mEventLog.logAndAdd("restoreAccessToken(): Softphone Service is recovering");
        setupEnvironment(environment);
        updateAccountStatus(this.mAccountId, 2);
        getAccountSecretKey();
        refreshTokenAfterRecovery();
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_REDISTATE);
    }

    public void exchangeForAccessToken(String authorizationCode, String accountId, boolean autoRegister, String tGuardAppId, int environment) {
        exchangeForAccessToken(authorizationCode, accountId, autoRegister, tGuardAppId, environment, 0, SoftphoneNamespaces.mTimeoutType4[0]);
    }

    public void startInitstate() {
        if (this.mAutoRetry.getAndSet(false)) {
            exchangeForAccessToken(this.mTGaurdToken, this.mAccountId, true, this.mTGaurdAppId, this.mEnvironment);
        }
    }

    private void exchangeForAccessToken(String authorizationCode, String accountId, boolean autoRegister, String tGuardAppId, int environment, int retryCount, long timeout) {
        String str = authorizationCode;
        String str2 = accountId;
        boolean z = autoRegister;
        String str3 = tGuardAppId;
        int i = environment;
        int i2 = retryCount;
        long j = timeout;
        int currentUser = Extensions.ActivityManager.getCurrentUser();
        this.mUserId = currentUser;
        UserHandle userForSerialNumber = this.mUserManager.getUserForSerialNumber((long) currentUser);
        this.mUserHandle = userForSerialNumber;
        if (userForSerialNumber == null) {
            this.mUserHandle = ContextExt.CURRENT_OR_SELF;
        }
        String str4 = this.LOG_TAG;
        IMSLog.s(str4, "exchangeForAccessToken request: authCode: " + str + ", tGuardAppId: " + str3);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("exchangeForAccessToken request: accountId: " + str2 + ", autoRegister: " + z + ", environment: " + i + ", retryCount: " + i2 + ", timeout: " + j + ", mUserId: " + this.mUserId);
        if (str == null) {
            this.mEventLog.logAndAdd("authorizationCode is null");
            return;
        }
        this.mTGaurdToken = str;
        this.mTGaurdAppId = str3;
        Map<String, String> mp = new HashMap<>();
        mp.put(this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_TOKEN, this.mEncryptionHelper.encrypt(str, this.mSecretKey));
        mp.put(this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_APPID, this.mEncryptionHelper.encrypt(str3, this.mSecretKey));
        mp.put(this.mAccountId + ":" + this.mUserId + ":" + "environment", Integer.toString(environment));
        this.mSharedPrefHelper.save(this.mContext, mp);
        setupEnvironment(i);
        ExchangeForAccessTokenRequest exchangeForAccessTokenRequest = SoftphoneRequestBuilder.buildExchangeForAccessTokenRequest(this.mAppKey, this.mAppSecret, str2, str);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.TOKEN_PATH);
        txn.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            txn.setJsonBody(new JSONObject(new Gson().toJson(exchangeForAccessTokenRequest)));
        } catch (JSONException e) {
            String str5 = this.LOG_TAG;
            IMSLog.s(str5, "could not build JSONObject:" + e.getMessage());
        }
        txn.setRequestMethod(HttpRequestParams.Method.POST);
        txn.setTimeout(j);
        if (i2 > 0) {
            this.mStateHandler.sendMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_RETRY_OBTAIN_ACCESS_TOKEN, i2, z, txn);
        } else {
            this.mStateHandler.sendMessage(0, i2, z ? 1 : 0, txn);
        }
    }

    public void provisionAccount() {
        provisionAccount(0, SoftphoneNamespaces.mTimeoutType3[0]);
    }

    private void provisionAccount(int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("provisionAccount(): retryCount: " + retryCount + ", timeout: " + timeout);
        ProvisionAccountRequest provisionAccountRequest = SoftphoneRequestBuilder.buildProvisionAccountRequest();
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.PROVISION_ACCOUNT_PATH);
        txn.addRequestHeader("Content-Type", "application/json");
        try {
            txn.setJsonBody(new JSONObject(new Gson().toJson(provisionAccountRequest)));
        } catch (JSONException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "could not build JSONObject:" + e.getMessage());
        }
        txn.setRequestMethod(HttpRequestParams.Method.POST);
        txn.setTimeout(timeout);
        this.mStateHandler.sendMessage(3, retryCount, -1, txn);
    }

    public void validateE911Address(int addressId, boolean confirmed, IEmergencyServiceListener listener) {
        validateE911Address(addressId, confirmed, listener, 0, SoftphoneNamespaces.mTimeoutType2[0]);
    }

    private void validateE911Address(int addressId, boolean confirmed, IEmergencyServiceListener listener, int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("validateE911Address(): addressId: " + addressId + ", confirmed: " + confirmed + ", retryCount: " + retryCount + ", timeout: " + timeout);
        AddressValidationRequest addressValidationRequest = this.mRequestBuilder.buildAddressValidationRequest(addressId, confirmed);
        int transactionId = this.mStateHandler.getHttpTransactionId();
        if (listener != null) {
            saveListener(transactionId, listener);
        }
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.E911ADDRESS_VALIDATION_PATH);
        txn.addRequestHeader("Content-Type", "application/json");
        try {
            txn.setJsonBody(new JSONObject(new Gson().toJson(addressValidationRequest)));
        } catch (JSONException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "could not build JSONObject:" + e.getMessage());
        }
        txn.setRequestMethod(HttpRequestParams.Method.POST);
        txn.setTimeout(timeout);
        Message msg = this.mStateHandler.obtainMessage(6, transactionId, addressId, txn);
        Bundle data = new Bundle();
        data.putInt("retry_count", retryCount);
        data.putBoolean(SoftphoneNamespaces.SoftphoneSettings.CONFIRMED, confirmed);
        msg.setData(data);
        this.mStateHandler.sendMessage(msg);
    }

    public void tryRegister() {
        this.mStateHandler.sendMessage(14);
    }

    public void tryDeregister() {
        updateAccountStatus(this.mAccountId, 3);
        this.mStateHandler.sendMessage(17);
    }

    public void logOut() {
        this.mEventLog.logAndAdd("logOut()");
        this.mStateHandler.sendMessage(17);
        this.mStateHandler.removeMessages(14);
        this.mStateHandler.removeMessages(1);
        this.mStateHandler.removeMessages(4);
        this.mStateHandler.removeMessages(SoftphoneNamespaces.SoftphoneEvents.EVENT_RELOGIN);
        this.mAutoRetry.set(false);
        this.mIsRecovery.set(false);
        resetAccountStatus();
        resetCurrentAddresses();
        removeSharedPreferences();
        deregisterSoftphoneLabelObserver();
        this.mStateHandler.sendMessage(1018);
    }

    public void reLogin(int retryCount, boolean needNewToken) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("reLogin(): retryCount: " + retryCount + ", needNewToken: " + needNewToken + ", callState: " + this.mTelephonyManager.getCallState());
        if (this.mTelephonyManager.getCallState() != 0) {
            long expBackoff = (1 << retryCount) * 60000;
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("reLogin(): backoff: " + expBackoff);
            int retryCount2 = retryCount + 1;
            if (retryCount2 > 6) {
                retryCount2 = 6;
            }
            this.mStateHandler.sendMessageDelayed(SoftphoneNamespaces.SoftphoneEvents.EVENT_RELOGIN, retryCount2, needNewToken ? 1 : 0, expBackoff);
            return;
        }
        NetworkInfo info = this.mConnectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            this.mEventLog.logAndAdd("reLogin(): network info is null or not connected");
            this.mStateHandler.sendMessage(17);
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_OUT_OF_SERVICE);
            return;
        }
        this.mStateHandler.removeMessages(14);
        this.mStateHandler.removeMessages(4);
        deregisterSoftphoneLabelObserver();
        if (needNewToken) {
            this.mStateHandler.sendMessage(17);
            SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
            Context context = this.mContext;
            sharedPrefHelper.remove(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token");
            this.mAutoRetry.set(true);
            this.mIsRecovery.set(false);
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_START_RELOGIN);
            return;
        }
        this.mStateHandler.sendMessage(1029);
    }

    public void onUserSwitch() {
        this.mEventLog.logAndAdd("onUserSwitch()");
        this.mStateHandler.sendMessage(17);
        this.mStateHandler.sendMessage(1025);
    }

    public void getCallWaitingInfo() {
        getCallWaitingInfo(0, SoftphoneNamespaces.mTimeoutType1[0]);
    }

    private void getCallWaitingInfo(int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getCallWaitingInfo(): retryCount: " + retryCount + ", timeout: " + timeout);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.CALL_WAITING_PATH);
        txn.addRequestHeader("Accept", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        txn.setRequestMethod(HttpRequestParams.Method.GET);
        txn.setTimeout(timeout);
        this.mStateHandler.sendMessage(8, retryCount, -1, txn);
    }

    public void getCallForwardingInfo() {
        getCallForwardingInfo(0, SoftphoneNamespaces.mTimeoutType1[0]);
    }

    private void getCallForwardingInfo(int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getCallForwardingInfo():retryCount: " + retryCount + ", timeout: " + timeout);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.CALL_FORWARDING_PATH);
        txn.addRequestHeader("Accept", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        txn.setRequestMethod(HttpRequestParams.Method.GET);
        txn.setTimeout(timeout);
        this.mStateHandler.sendMessage(9, retryCount, -1, txn);
    }

    public void setCallWaitingInfo(CallWaitingInfo info) {
        setCallWaitingInfo(info, 0, SoftphoneNamespaces.mTimeoutType2[0]);
    }

    private void setCallWaitingInfo(CallWaitingInfo info, int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setCallWaitingInfo(): retryCount: " + retryCount + ", timeout: " + timeout);
        if (info == null) {
            notifySsProgress(new SupplementaryServiceNotify(10, false, "null info"));
            return;
        }
        SoftphoneHttpTransaction txn = getCallHandlingTxn(SoftphoneNamespaces.SoftphoneSettings.CALL_WAITING_PATH, SoftphoneRequestBuilder.buildSetCallWaitingInfoRequest(info));
        txn.setTimeout(timeout);
        Message msg = this.mStateHandler.obtainMessage(10, retryCount, -1, txn);
        Bundle data = new Bundle();
        data.putParcelable("communication-waiting", info);
        msg.setData(data);
        this.mStateHandler.sendMessage(msg);
    }

    public void setCallForwardingInfo(CallForwardingInfo info) {
        setCallForwardingInfo(info, 0, SoftphoneNamespaces.mTimeoutType2[0]);
    }

    private void setCallForwardingInfo(CallForwardingInfo info, int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setCallForwardingInfo() retryCount: " + retryCount + ", timeout: " + timeout);
        if (info == null) {
            notifySsProgress(new SupplementaryServiceNotify(11, false, "null info"));
            return;
        }
        SoftphoneHttpTransaction txn = getCallHandlingTxn(SoftphoneNamespaces.SoftphoneSettings.CALL_FORWARDING_PATH, SoftphoneRequestBuilder.buildSetCallForwardingInfoRequest(info));
        txn.setTimeout(timeout);
        Message msg = this.mStateHandler.obtainMessage(11, retryCount, -1, txn);
        Bundle data = new Bundle();
        data.putParcelable("communication-diversion", info);
        msg.setData(data);
        this.mStateHandler.sendMessage(msg);
    }

    private SoftphoneHttpTransaction getCallHandlingTxn(String path, String xml) {
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(path);
        txn.addRequestHeader("Content-Type", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        txn.addRequestHeader("Accept", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        txn.setStringBody(xml);
        txn.setRequestMethod(HttpRequestParams.Method.PUT);
        return txn;
    }

    public List<DeviceInfo> getDeviceList() {
        List<DeviceInfo> devices = new ArrayList<>();
        ImsRegistration[] reg = this.mImsManager.getRegistrationInfo();
        if (reg != null && reg.length > 0) {
            List<NameAddr> deviceList = reg[0].getDeviceList();
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("reg.deviceList: " + deviceList);
            for (NameAddr na : deviceList) {
                String displayName = na.getDisplayName();
                String deviceId = "";
                ImsUri uri = na.getUri();
                if (uri != null && (deviceId = uri.getParam("gr")) == null) {
                    deviceId = "";
                }
                if (!displayName.isEmpty() || !deviceId.isEmpty()) {
                    devices.add(new DeviceInfo(displayName, deviceId));
                } else {
                    devices.add(new DeviceInfo("D;" + this.mAppKey + ";Smartphone", ""));
                }
            }
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("getDeviceList(): " + devices);
        return devices;
    }

    public int getUserId() {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getUserId(): " + this.mUserId);
        return this.mUserId;
    }

    public void onAirplaneModeOn() {
        this.mEventLog.logAndAdd("onAirplaneModeOn()");
        this.mStateHandler.sendMessage(17);
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_AIRPLANE_MODE_ON);
    }

    public void onNetworkConnected() {
        this.mEventLog.logAndAdd("onNetworkConnected()");
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_NETWORK_CONNECTED);
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("Dump of " + this.LOG_TAG);
        pw.increaseIndent();
        this.mEventLog.dump(pw);
        pw.decreaseIndent();
    }

    public void notifyProgress(GeneralNotify noti) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("listener size: " + this.mProgressListeners.size());
        Enumeration<IProgressListener> listener = this.mProgressListeners.elements();
        while (listener.hasMoreElements()) {
            try {
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd("Notify: " + noti.mRequestId);
                listener.nextElement().onNotify(noti);
            } catch (RemoteException e) {
                String str = this.LOG_TAG;
                IMSLog.s(str, "exception" + e.getMessage());
            }
        }
    }

    private void notifySsProgress(SupplementaryServiceNotify noti) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("listener size: " + this.mSupplementaryServiceListeners.size());
        Enumeration<ISupplementaryServiceListener> listener = this.mSupplementaryServiceListeners.elements();
        while (listener.hasMoreElements()) {
            try {
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd("Notify: " + noti.mRequestId);
                listener.nextElement().onNotify(noti);
            } catch (RemoteException e) {
                String str = this.LOG_TAG;
                IMSLog.s(str, "exception" + e.getMessage());
            }
        }
    }

    public void notifyRegisterStatus(boolean registered, String reason) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("notifyRegisterStatus(): registered: " + registered + ", reason: " + reason);
        notifyProgress(new GeneralNotify(14, registered, reason));
    }

    public void getImsNetworkIdentifiers(boolean justProvisioned, boolean autoRegister, int retryCount, long timeout, int attempt) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getImsNetworkIdentifiers(): justProvisioned: " + justProvisioned + ", autoRegister: " + autoRegister + ", retryCount: " + retryCount + ", timeout: " + timeout + ", attempt: " + attempt);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.OBTAIN_IDENTIFIERS_PATH);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mAccessTokenType);
        sb.append(" ");
        sb.append(this.mAccessToken);
        txn.addRequestHeader("Authorization", sb.toString());
        txn.setRequestMethod(HttpRequestParams.Method.GET);
        txn.setTimeout(timeout);
        if (justProvisioned) {
            Message msg = this.mStateHandler.obtainMessage(104, retryCount, autoRegister, (Object) null);
            Bundle data = new Bundle();
            data.putInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, attempt);
            msg.setData(data);
            txn.commit(msg);
            return;
        }
        Message msg2 = this.mStateHandler.obtainMessage(1, retryCount, autoRegister, txn);
        Bundle data2 = new Bundle();
        data2.putInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, attempt);
        msg2.setData(data2);
        if (attempt > 0) {
            long expBackoff = (1 << attempt) * 60000;
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("SoftphoneEvents(): backoff: " + expBackoff);
            this.mStateHandler.sendMessageDelayed(msg2, expBackoff);
            return;
        }
        this.mStateHandler.sendMessage(msg2);
    }

    public void broadcastIntent(String intentAction, String msisdn) {
        Intent intent = new Intent(intentAction);
        intent.putExtra("account_id", this.mAccountId);
        if (msisdn != null) {
            intent.putExtra("msisdn", msisdn);
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("broadcastIntent: " + intent.toString());
        String str = this.LOG_TAG;
        IMSLog.s(str, "broadcastIntent: extras: " + intent.getExtras());
        IntentUtil.sendBroadcast(this.mContext, intent, this.mUserHandle);
    }

    public void processImsNetworkIdentifiersResponse(ImsNetworkIdentifiersResponse response, boolean justProvisioned, int retryCount, boolean autoRegister, int attempt) {
        ImsNetworkIdentifiersResponse imsNetworkIdentifiersResponse = response;
        boolean z = justProvisioned;
        int i = retryCount;
        boolean z2 = autoRegister;
        int i2 = attempt;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processImsNetworkIdentifiersResponse(): success: " + imsNetworkIdentifiersResponse.mSuccess + ", justProvisioned: " + z + ", autoRegister: " + z2 + ", retryCount: " + i + ", attempt: " + i2);
        if (!imsNetworkIdentifiersResponse.mSuccess || !isImsNetworkIdentifiersResponseValid(response)) {
            if (imsNetworkIdentifiersResponse.mStatusCode == -1) {
                int retryCount2 = i + 1;
                if (retryCount2 < 3) {
                    getImsNetworkIdentifiers(justProvisioned, autoRegister, retryCount2, SoftphoneNamespaces.mTimeoutType1[retryCount2], attempt);
                    return;
                }
            } else {
                int retryCount3 = i;
            }
            if (z) {
                int attempt2 = i2 + 1;
                if (attempt2 < 3) {
                    this.mStateHandler.sendMessageDelayed(4, attempt2, 45000);
                    return;
                }
                imsNetworkIdentifiersResponse.mReason = "Please try again later or call AT&T Customer Care.";
                this.mEventLog.logAndAdd("processImsNetworkIdentifiersResponse(): notify getImsNetworkIdentity failure after 3 attempts");
                int i3 = attempt2;
            } else if (imsNetworkIdentifiersResponse.mReason != null && imsNetworkIdentifiersResponse.mReason.contains("LDAP Record not found")) {
                getTermsAndConditions();
                return;
            } else if (z2) {
                if (imsNetworkIdentifiersResponse.mStatusCode == 401) {
                    SimpleEventLog simpleEventLog2 = this.mEventLog;
                    simpleEventLog2.logAndAdd("processImsNetworkIdentifiersResponse(): statusCode: " + imsNetworkIdentifiersResponse.mStatusCode + ", invalid access token, reLogin");
                    reLogin(0, true);
                    return;
                }
                int attempt3 = i2 + 1;
                if (attempt3 < 6) {
                    getImsNetworkIdentifiers(false, autoRegister, 0, SoftphoneNamespaces.mTimeoutType1[0], attempt3);
                    return;
                }
                logOut();
            }
        } else {
            this.mSoftphoneEmergencyServcie.compareAndSaveE911Address(imsNetworkIdentifiersResponse.mIdentitiesResponse.mLocations, this.mAccountId);
            registerSoftphoneLabelObserver(this.mAccountId);
            this.mIdentity = new ImsNetworkIdentity(imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPrivateUserId, imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId, new ArrayList(Arrays.asList(new String[]{imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mFQDN})), this.mAppKey);
            scheduleRefreshIdentityAlarm(10800000);
            updateAccountInfo(imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPrivateUserId, imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId);
            saveAccountIdentities(imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPrivateUserId, imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId, imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mFQDN);
            broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_LOGIN_COMPLETED, imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId.substring(imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId.indexOf(":") + 1, imsNetworkIdentifiersResponse.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId.indexOf("@")));
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_ACTIVATEDSTATE);
            if (z2) {
                checkAutoRegistrationCondition();
            }
            this.mIsRecovery.set(false);
            int i4 = i;
        }
        notifyProgress(new GeneralNotify(4, imsNetworkIdentifiersResponse.mSuccess, imsNetworkIdentifiersResponse.mReason));
    }

    private boolean isImsNetworkIdentifiersResponseValid(ImsNetworkIdentifiersResponse response) {
        if (response.mIdentitiesResponse != null && response.mIdentitiesResponse.mSubscriberIdentities != null && response.mIdentitiesResponse.mSubscriberIdentities.mPrivateUserId != null && response.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId != null && response.mIdentitiesResponse.mSubscriberIdentities.mFQDN != null && response.mIdentitiesResponse.mLocations != null) {
            return true;
        }
        response.mSuccess = false;
        response.mReason = "Cannot retrieve account info. Please call AT&T Customer Care.";
        notifyProgress(new GeneralNotify(4, false, response.mReason));
        return false;
    }

    private void checkAutoRegistrationCondition() {
        if (!tryRegisterWithDefaultAddress()) {
            new GeolocationUpdateFlow(this.mContext).requestGeolocationUpdate(0, 0, 3, new GeolocationUpdateFlow.LocationUpdateListener() {
                public final void onAddressObtained(Address address) {
                    SoftphoneClient.this.lambda$checkAutoRegistrationCondition$0$SoftphoneClient(address);
                }
            });
        }
    }

    public /* synthetic */ void lambda$checkAutoRegistrationCondition$0$SoftphoneClient(Address address) {
        if (address != null) {
            String countryCode = address.getCountryCode();
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("onAddressObtained(): " + countryCode);
            if ("US".equalsIgnoreCase(countryCode) || "VI".equalsIgnoreCase(countryCode) || "PR".equalsIgnoreCase(countryCode)) {
                broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_MISSING_E911, (String) null);
                return;
            }
            tryRegister();
            broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_IN_INTERNATIONAL, (String) null);
            return;
        }
        this.mEventLog.logAndAdd("onAddressObtained(): cannot determine location");
        broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_LOCATION_UNKNOWN, (String) null);
    }

    private boolean tryRegisterWithDefaultAddress() {
        if (getCurrentAddress() != -1) {
            tryRegister();
            return true;
        }
        long addressId = getDefaultAddress();
        if (addressId == -1) {
            return false;
        }
        setAddressCurrent(addressId);
        tryRegister();
        return true;
    }

    private long getDefaultAddress() {
        Cursor cursor = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAddress.buildGetDefaultAddressUri(this.mAccountId), (String[]) null, (String) null, (String[]) null, (String) null);
        long addressId = -1;
        if (cursor != null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("found " + cursor.getCount() + " default addresses");
            if (cursor.moveToFirst()) {
                addressId = cursor.getLong(cursor.getColumnIndex("_id"));
            }
            cursor.close();
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("getDefaultAddress(): id = " + addressId);
        return addressId;
    }

    private long getCurrentAddress() {
        Cursor cursor = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAddress.buildGetCurrentAddressUri(this.mAccountId), (String[]) null, (String) null, (String[]) null, (String) null);
        long addressId = -1;
        if (cursor != null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("found " + cursor.getCount() + " current addresses");
            if (cursor.moveToFirst()) {
                addressId = cursor.getLong(cursor.getColumnIndex("_id"));
            }
            cursor.close();
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("getCurrentAddress(): id = " + addressId);
        return addressId;
    }

    private void setAddressCurrent(long addressId) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setAddressCurrent(): id = " + addressId);
        this.mContext.getContentResolver().update(SoftphoneContract.SoftphoneAddress.buildSetCurrentAddressUri(this.mAccountId, addressId), new ContentValues(), (String) null, (String[]) null);
    }

    public void getTermsAndConditions() {
        getTermsAndConditions(0, SoftphoneNamespaces.mTimeoutType1[0]);
    }

    private void getTermsAndConditions(int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getTermsAndConditions(): retryCount:" + retryCount + ", timeout: " + timeout);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.TERMS_AND_CONDITIONS_PATH);
        txn.setRequestMethod(HttpRequestParams.Method.GET);
        txn.setTimeout(timeout);
        this.mStateHandler.sendMessage(2, retryCount, -1, txn);
    }

    public void processTermsAndConditionsResponse(TermsAndConditionsResponse response, int retryCount) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processTermsAndConditionsResponse(): success: " + response.mSuccess + ", retryCount: " + retryCount);
        if (response.mSuccess) {
            response.mReason = response.mTCResponse.mUrl;
        } else if (response.mStatusCode == -1 && (retryCount2 = retryCount + 1) < 3) {
            getTermsAndConditions(retryCount2, SoftphoneNamespaces.mTimeoutType1[retryCount2]);
            return;
        }
        notifyProgress(new GeneralNotify(2, response.mSuccess, response.mReason));
    }

    public void processProvisionAccountResponse(SoftphoneResponse response, int retryCount) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processProvisionAccountResponse(): success: " + response.mSuccess + ", retryCount: " + retryCount);
        if (response.mSuccess) {
            this.mStateHandler.sendMessageDelayed(4, 0, 45000);
        } else if (response.mStatusCode == -1 && (retryCount2 = retryCount + 1) < 3) {
            provisionAccount(retryCount2, SoftphoneNamespaces.mTimeoutType3[retryCount2]);
            return;
        }
        notifyProgress(new GeneralNotify(3, response.mSuccess, response.mReason));
    }

    public void processValidateE911AddressResponse(AddressValidationResponse response, int retryCount) {
        boolean confirmationRequired;
        int retryCount2;
        this.mEventLog.logAndAdd("processAddressValidationResponse(): success: " + response.mSuccess + ", retryCount: " + retryCount);
        if (response.mSuccess) {
            updateE911AddressLocally(response.mAddressId, response.mE911Locations.mAddressIdentifier, response.mE911Locations.mExpirationDate);
            addE911Address(response.mAddressId, 0, SoftphoneNamespaces.mTimeoutType1[0]);
            confirmationRequired = false;
        } else if (response.mStatusCode == -1 && (retryCount2 = retryCount + 1) < 3) {
            validateE911Address(response.mAddressId, response.mConfirmed, (IEmergencyServiceListener) findAndRemoveListener(response.mTransactionId), retryCount2, SoftphoneNamespaces.mTimeoutType2[retryCount2]);
            return;
        } else if (response.mReason.contains("Address Confirmation Required")) {
            confirmationRequired = true;
        } else {
            confirmationRequired = false;
        }
        AddressValidationNotify noti = new AddressValidationNotify(6, response.mSuccess, response.mReason, response.mAddressId, confirmationRequired);
        IEmergencyServiceListener listener = (IEmergencyServiceListener) findAndRemoveListener(response.mTransactionId);
        if (listener != null) {
            try {
                listener.onNotify(noti);
            } catch (RemoteException e) {
                IMSLog.s(this.LOG_TAG, "exception" + e.getMessage());
            }
        }
    }

    private void updateE911AddressLocally(int addressId, String addressIdentifier, String expireDate) {
        this.mEventLog.logAndAdd("updateE911AddressLocally()");
        Uri uri = SoftphoneContract.SoftphoneAddress.buildAddressUri((long) addressId);
        ContentValues values = new ContentValues();
        values.put("account_id", this.mAccountId);
        values.put(SoftphoneContract.AddressColumns.E911AID, addressIdentifier);
        values.put(SoftphoneContract.AddressColumns.EXPIRE_DATE, expireDate);
        this.mContext.getContentResolver().update(uri, values, (String) null, (String[]) null);
    }

    private void addE911Address(int addressId, int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("addE911Address(): addressId: " + addressId + ", retryCount: " + retryCount + ", timeout: " + timeout);
        AddAddressRequest addAddressRequest = this.mRequestBuilder.buildAddAddressRequest(addressId);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.E911ADDRESS_PATH);
        txn.addRequestHeader("Content-Type", "application/json");
        try {
            txn.setJsonBody(new JSONObject(new Gson().toJson(addAddressRequest)));
        } catch (JSONException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "could not build JSONObject:" + e.getMessage());
        }
        txn.setRequestMethod(HttpRequestParams.Method.POST);
        txn.setTimeout(timeout);
        this.mStateHandler.sendMessage(7, retryCount, addressId, txn);
    }

    public void processAddE911AddressResponse(AddAddressResponse response, int retryCount, int addressId) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processAddAddressResponse(): success: " + response.mSuccess + ", retryCount: " + retryCount + ", addressId: " + addressId);
        if (response.mSuccess) {
            this.mSoftphoneEmergencyServcie.compareAndSaveE911Address(response.mLocationResponse.mLocations, this.mAccountId);
        } else if (response.mStatusCode == -1 && (retryCount2 = retryCount + 1) < 3) {
            addE911Address(addressId, retryCount2, SoftphoneNamespaces.mTimeoutType1[retryCount2]);
            return;
        }
        notifyProgress(new GeneralNotify(7, response.mSuccess, response.mReason));
    }

    private void registerSoftphoneLabelObserver(String accountId) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("registerSoftphoneLabelObserver() for " + accountId);
        this.mContext.getContentResolver().registerContentObserver(SoftphoneContract.SoftphoneAccount.buildAccountLabelUri(accountId, (long) this.mUserId), false, this.mSoftphoneLabelObserver);
    }

    private void deregisterSoftphoneLabelObserver() {
        this.mEventLog.logAndAdd("deregisterSoftphoneLabelObserver()");
        this.mContext.getContentResolver().unregisterContentObserver(this.mSoftphoneLabelObserver);
    }

    public void handleTryRegisterRequest() {
        if (!this.mLoggedOut) {
            this.mEventLog.logAndAdd("There is an ongoing profile registration.");
            return;
        }
        ImsProfile profile = SoftphoneAuthUtils.createProfileFromTemplate(this.mContext, this.mIdentity, this.mAccountId, this.mUserId);
        if (profile != null) {
            int registerAdhocProfile = this.mImsManager.registerAdhocProfile(profile);
            this.mProfileId = registerAdhocProfile;
            if (registerAdhocProfile == -1) {
                this.mIsRegisterPending = true;
                this.mEventLog.logAndAdd("Register is pending because ImsService is not connected");
            } else {
                this.mLoggedOut = false;
                this.mIsRegisterPending = false;
            }
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("injected profile ID: " + this.mProfileId);
            return;
        }
        this.mEventLog.logAndAdd("fail to build profile");
        notifyRegisterStatus(false, "Fail to build profile.");
    }

    public void handleDeRegisterRequest() {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("handleDeRegisterRequest(): mProfileId = " + this.mProfileId);
        int i = this.mProfileId;
        if (i != -1) {
            this.mImsManager.deregisterAdhocProfile(i);
            this.mLoggedOut = true;
        }
    }

    public void handleLabelUpdated() {
        this.mEventLog.logAndAdd("handleLabelUpdated()");
        handleDeRegisterRequest();
        SoftphoneStateHandler softphoneStateHandler = this.mStateHandler;
        softphoneStateHandler.deferMessage(softphoneStateHandler.obtainMessage(14));
    }

    public void resetCurrentAddresses() {
        this.mEventLog.logAndAdd("resetCurrentAddresses()");
        this.mContext.getContentResolver().update(SoftphoneContract.SoftphoneAddress.buildResetCurrentAddressUri(this.mAccountId), new ContentValues(), (String) null, (String[]) null);
    }

    public void releaseImsNetworkIdentities(int retryCount, long timeout) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("releaseImsNetworkIdentities(): retryCount: " + retryCount + ", timeout: " + timeout);
        if (this.mIdentity.impiEmpty()) {
            this.mEventLog.logAndAdd("No IMS network identifiers to release.");
            this.mStateHandler.sendMessage(18);
            return;
        }
        ReleaseImsNetworkIdentifiersRequest releaseImsNetworkIdentifiersRequest = SoftphoneRequestBuilder.buildReleaseImsNetworkIdentifiersRequest(this.mIdentity.getImpi(), this.mIdentity.getImpu());
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.RELEASE_IDENTIFIERS_PATH);
        txn.addRequestHeader("Content-Type", "application/json");
        try {
            txn.setJsonBody(new JSONObject(new Gson().toJson(releaseImsNetworkIdentifiersRequest)));
        } catch (JSONException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "could not build JSONObject:" + e.getMessage());
        }
        txn.setRequestMethod(HttpRequestParams.Method.PUT);
        txn.setTimeout(timeout);
        this.mStateHandler.sendMessage(5, retryCount, -1, txn);
    }

    public void processReleaseImsNetworkIdentitiesResponse(SoftphoneResponse response, int retryCount) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processReleaseImsNetworkIdentitiesResponse(): success: " + response.mSuccess + ", retryCount: " + retryCount);
        if (response.mSuccess) {
            this.mIdentity.clear();
            SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
            Context context = this.mContext;
            sharedPrefHelper.remove(context, this.mAccountId + ":" + this.mUserId + ":" + "impi", this.mAccountId + ":" + this.mUserId + ":" + "impu", this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_FQDN);
        } else if (response.mStatusCode != -1 || (retryCount2 = retryCount + 1) >= 3) {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("Fail to ReleaseImsNetworkIdentities: " + response.mReason);
        } else {
            releaseImsNetworkIdentities(retryCount2, SoftphoneNamespaces.mTimeoutType1[retryCount2]);
            return;
        }
        PendingIntent pendingIntent = this.mRefreshIdentityIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mRefreshIdentityIntent = null;
        }
        this.mStateHandler.sendMessage(18);
    }

    public void revokeAccessToken() {
        this.mEventLog.logAndAdd("revokeAccessToken()");
        this.mStateHandler.removeMessages(15);
        revokeToken("access_token", this.mAccessToken);
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        sharedPrefHelper.remove(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token");
    }

    public void processRevokeAccessTokenResponse(SoftphoneResponse response) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processRevokeAccessTokenResponse(): success: " + response.mSuccess);
        if (response.mSuccess) {
            this.mAccessToken = null;
            this.mAccessTokenType = null;
            this.mTokenExpiresTime = -1;
        } else {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("Fail to RevokeAccessToken: " + response.mReason);
        }
        revokeToken("refresh_token", this.mRefreshToken);
    }

    public void processRevokeRefreshTokenResponse(SoftphoneResponse response) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processRevokeRefreshTokenResponse(): success: " + response.mSuccess);
        if (response.mSuccess) {
            this.mRefreshToken = null;
        } else {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("Fail to RevokeRefreshToken: " + response.mReason);
        }
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_INITSTATE);
    }

    private void revokeToken(String tokenType, String token) {
        this.mEventLog.logAndAdd("revokeToken()");
        String str = this.LOG_TAG;
        IMSLog.s(str, "revokeToken(): tokenType: " + tokenType + " , token: " + token);
        RevokeTokenRequest revokeTokenRequest = SoftphoneRequestBuilder.buildRevokeTokenRequest(this.mAppKey, this.mAppSecret, token, tokenType);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.REVOKE_TOKEN_PATH);
        txn.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            txn.setJsonBody(new JSONObject(new Gson().toJson(revokeTokenRequest)));
        } catch (JSONException e) {
            String str2 = this.LOG_TAG;
            IMSLog.s(str2, "could not build JSONObject:" + e.getMessage());
        }
        txn.setRequestMethod(HttpRequestParams.Method.POST);
        if (tokenType.equalsIgnoreCase("access_token")) {
            this.mStateHandler.sendMessage(12, (Object) txn);
        } else if (tokenType.equalsIgnoreCase("refresh_token")) {
            this.mStateHandler.sendMessage(13, (Object) txn);
        }
    }

    public void refreshToken(int retryCount, long timeout, int attempt) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("refreshToken(): retryCount: " + retryCount + ", timeout: " + timeout + ", attempt: " + attempt);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.TOKEN_PATH);
        txn.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        txn.setStringBody("client_id=" + this.mAppKey + "&client_secret=" + this.mAppSecret + "&grant_type=refresh_token&refresh_token=" + this.mRefreshToken);
        txn.setRequestMethod(HttpRequestParams.Method.POST);
        txn.setTimeout(timeout);
        txn.commit(this.mStateHandler.obtainMessage(1015, retryCount, attempt));
    }

    public void processRefreshTokenResponse(AccessTokenResponse response, int statusCode, int retryCount, int attempt) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processRefreshTokenResponse(): success: " + response.mSuccess + ", statusCode: " + statusCode + ", retryCount: " + retryCount + ", attempt: " + attempt);
        if (response.mSuccess) {
            saveTokens(response.mAccessToken, response.mTokenType, Long.parseLong(response.mExpiresIn), response.mRefreshToken);
            scheduleRefreshTokenAlarm(this.mTokenExpiresTime * 900, 0);
            if (this.mIsRecovery.get()) {
                handleImsNetworkIdentityAfterRecovery();
            }
        } else if (response.mStatusCode == -1 && (retryCount2 = retryCount + 1) < 3) {
            refreshToken(retryCount2, SoftphoneNamespaces.mTimeoutType4[retryCount2], attempt);
        } else if (statusCode == 401) {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("processRefreshTokenResponse(): statusCode: " + statusCode + ", invalid access token, reLogin");
            reLogin(0, true);
        } else {
            int attempt2 = attempt + 1;
            if (attempt2 <= 3) {
                scheduleRefreshTokenAlarm((this.mTokenExpiresTime * 100) / 3, attempt2);
                return;
            }
            SimpleEventLog simpleEventLog3 = this.mEventLog;
            simpleEventLog3.logAndAdd("processRefreshTokenResponse(): statusCode: " + statusCode + ", unable to refresh token, try reLogin");
            reLogin(0, true);
        }
    }

    public void processGetCallWaitingInfoResponse(CallWaitingResponse response, int retryCount) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processGetCallWaitingInfoResponse(): success: " + response.mSuccess + ", retryCount: " + retryCount);
        if (response.mSuccess) {
            this.mEventLog.logAndAdd(response.mActive);
            notifySsProgress(new SupplementaryServiceNotify(8, response.mSuccess, response.mReason, new CallWaitingInfo(CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(response.mActive))));
        } else if (response.mStatusCode != -1 || (retryCount2 = retryCount + 1) >= 3) {
            notifySsProgress(new SupplementaryServiceNotify(8, false, response.mReason));
        } else {
            getCallWaitingInfo(retryCount2, SoftphoneNamespaces.mTimeoutType1[retryCount2]);
        }
    }

    private void checkWithCondition(List<CallForwardingInfo> infos, CallForwardingResponse.Ruleset.Rule rule, boolean active, int noRetryTimer, String number) {
        if (rule.mConditions.mBusy != null) {
            this.mEventLog.logAndAdd("condition: busy");
            infos.add(new CallForwardingInfo(active && !number.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, noRetryTimer, 1, number));
        } else if (rule.mConditions.mNoAnswer != null) {
            this.mEventLog.logAndAdd("condition: no-answer");
            infos.add(new CallForwardingInfo(active && !number.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, noRetryTimer, 2, number));
        } else if (rule.mConditions.mNotReachable != null) {
            this.mEventLog.logAndAdd("condition: not-reachable");
            infos.add(new CallForwardingInfo(active && !number.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, noRetryTimer, 3, number));
        } else if (rule.mConditions.mNotRegistered != null) {
            this.mEventLog.logAndAdd("condition: not-registered");
            infos.add(new CallForwardingInfo(active && !number.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, noRetryTimer, 8, number));
        } else if (rule.mConditions.mUnconditional != null) {
            this.mEventLog.logAndAdd("condition: unconditional");
            infos.add(new CallForwardingInfo(active && !number.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, noRetryTimer, 0, number));
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void processGetCallForwardingInfoResponse(com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse r25, int r26) {
        /*
            r24 = this;
            r6 = r24
            r7 = r25
            r8 = r26
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "processGetCallForwardingInfoResponse(): success: "
            r1.append(r2)
            boolean r2 = r7.mSuccess
            r1.append(r2)
            java.lang.String r2 = ", retryCount: "
            r1.append(r2)
            r1.append(r8)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            boolean r0 = r7.mSuccess
            r9 = 9
            r10 = -1
            r11 = 3
            if (r0 == 0) goto L_0x0207
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.String r1 = r7.mActive
            r0.logAndAdd(r1)
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.String r1 = r7.mNoReplyTimer
            r0.logAndAdd(r1)
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "num of rules: "
            r1.append(r2)
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset r2 = r7.mRuleset
            java.util.List<com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule> r2 = r2.mRules
            int r2 = r2.size()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r13 = r0
            java.lang.String r0 = r7.mActive
            java.lang.String r1 = "true"
            boolean r14 = r1.equalsIgnoreCase(r0)
            java.lang.String r0 = r7.mNoReplyTimer
            int r21 = java.lang.Integer.parseInt(r0)
            java.lang.String r0 = ""
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset r1 = r7.mRuleset
            java.util.List<com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule> r1 = r1.mRules
            java.util.Iterator r22 = r1.iterator()
        L_0x007a:
            boolean r1 = r22.hasNext()
            if (r1 == 0) goto L_0x01f9
            java.lang.Object r1 = r22.next()
            r15 = r1
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule r15 = (com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse.Ruleset.Rule) r15
            com.sec.internal.helper.SimpleEventLog r1 = r6.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "ruleId: "
            r2.append(r3)
            java.lang.String r3 = r15.mId
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Action r1 = r15.mActions
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Action$ForwardTo r1 = r1.mForwardTo
            java.lang.String r1 = r1.mTarget
            java.lang.String r2 = ":"
            java.lang.String[] r5 = r1.split(r2)
            int r1 = r5.length
            r2 = 1
            int r1 = r1 - r2
            r4 = r5[r1]
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            r0.logAndAdd(r4)
            java.lang.String r0 = r15.mId
            int r1 = r0.hashCode()
            r3 = 4
            r12 = 2
            switch(r1) {
                case -2094921849: goto L_0x00ea;
                case -1169678268: goto L_0x00e0;
                case -225471283: goto L_0x00d6;
                case 107705890: goto L_0x00cc;
                case 424630474: goto L_0x00c2;
                default: goto L_0x00c1;
            }
        L_0x00c1:
            goto L_0x00f4
        L_0x00c2:
            java.lang.String r1 = "call-diversion-not-reachable"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x00c1
            r0 = r11
            goto L_0x00f5
        L_0x00cc:
            java.lang.String r1 = "call-diversion-busy"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x00c1
            r0 = r2
            goto L_0x00f5
        L_0x00d6:
            java.lang.String r1 = "call-diversion-not-logged-in"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x00c1
            r0 = r3
            goto L_0x00f5
        L_0x00e0:
            java.lang.String r1 = "call-diversion-unconditional"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x00c1
            r0 = 0
            goto L_0x00f5
        L_0x00ea:
            java.lang.String r1 = "call-diversion-no-reply"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x00c1
            r0 = r12
            goto L_0x00f5
        L_0x00f4:
            r0 = r10
        L_0x00f5:
            if (r0 == 0) goto L_0x01cd
            if (r0 == r2) goto L_0x01a4
            if (r0 == r12) goto L_0x017b
            if (r0 == r11) goto L_0x0151
            if (r0 == r3) goto L_0x0127
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Unknown ruleId: "
            r1.append(r2)
            java.lang.String r2 = r15.mId
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r0 = r24
            r1 = r13
            r2 = r15
            r3 = r14
            r12 = r4
            r4 = r21
            r23 = r5
            r5 = r12
            r0.checkWithCondition(r1, r2, r3, r4, r5)
            goto L_0x01f6
        L_0x0127:
            r12 = r4
            r23 = r5
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x013d
            boolean r1 = r12.isEmpty()
            if (r1 != 0) goto L_0x013d
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r1 = r15.mConditions
            java.lang.String r1 = r1.mRuleDeactivated
            if (r1 != 0) goto L_0x013d
            r16 = r2
            goto L_0x013f
        L_0x013d:
            r16 = 0
        L_0x013f:
            r17 = 0
            r19 = 8
            r1 = r15
            r15 = r0
            r18 = r21
            r20 = r12
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x01f6
        L_0x0151:
            r12 = r4
            r23 = r5
            r1 = r15
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x0168
            boolean r3 = r12.isEmpty()
            if (r3 != 0) goto L_0x0168
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r3 = r1.mConditions
            java.lang.String r3 = r3.mRuleDeactivated
            if (r3 != 0) goto L_0x0168
            r16 = r2
            goto L_0x016a
        L_0x0168:
            r16 = 0
        L_0x016a:
            r17 = 0
            r19 = 3
            r15 = r0
            r18 = r21
            r20 = r12
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x01f6
        L_0x017b:
            r12 = r4
            r23 = r5
            r1 = r15
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x0192
            boolean r3 = r12.isEmpty()
            if (r3 != 0) goto L_0x0192
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r3 = r1.mConditions
            java.lang.String r3 = r3.mRuleDeactivated
            if (r3 != 0) goto L_0x0192
            r16 = r2
            goto L_0x0194
        L_0x0192:
            r16 = 0
        L_0x0194:
            r17 = 0
            r19 = 2
            r15 = r0
            r18 = r21
            r20 = r12
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x01f6
        L_0x01a4:
            r12 = r4
            r23 = r5
            r1 = r15
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x01bb
            boolean r3 = r12.isEmpty()
            if (r3 != 0) goto L_0x01bb
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r3 = r1.mConditions
            java.lang.String r3 = r3.mRuleDeactivated
            if (r3 != 0) goto L_0x01bb
            r16 = r2
            goto L_0x01bd
        L_0x01bb:
            r16 = 0
        L_0x01bd:
            r17 = 0
            r19 = 1
            r15 = r0
            r18 = r21
            r20 = r12
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x01f6
        L_0x01cd:
            r12 = r4
            r23 = r5
            r1 = r15
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x01e4
            boolean r3 = r12.isEmpty()
            if (r3 != 0) goto L_0x01e4
            com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r3 = r1.mConditions
            java.lang.String r3 = r3.mRuleDeactivated
            if (r3 != 0) goto L_0x01e4
            r16 = r2
            goto L_0x01e6
        L_0x01e4:
            r16 = 0
        L_0x01e6:
            r17 = 0
            r19 = 0
            r15 = r0
            r18 = r21
            r20 = r12
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
        L_0x01f6:
            r0 = r12
            goto L_0x007a
        L_0x01f9:
            com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify r1 = new com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify
            boolean r2 = r7.mSuccess
            java.lang.String r3 = r7.mReason
            r1.<init>(r9, r2, r3, r13)
            r6.notifySsProgress(r1)
            r0 = r8
            goto L_0x0223
        L_0x0207:
            int r0 = r7.mStatusCode
            if (r0 != r10) goto L_0x0217
            int r0 = r8 + 1
            if (r0 >= r11) goto L_0x0218
            long[] r1 = com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces.mTimeoutType1
            r1 = r1[r0]
            r6.getCallForwardingInfo(r0, r1)
            return
        L_0x0217:
            r0 = r8
        L_0x0218:
            com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify r1 = new com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify
            java.lang.String r2 = r7.mReason
            r3 = 0
            r1.<init>(r9, r3, r2)
            r6.notifySsProgress(r1)
        L_0x0223:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.softphone.SoftphoneClient.processGetCallForwardingInfoResponse(com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse, int):void");
    }

    public void processSetCallWaitingInfoResponse(SoftphoneResponse response, int retryCount, CallWaitingInfo info) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processSetCallWaitingInfoResponse(): success: " + response.mSuccess + ", retryCount:" + retryCount);
        if (response.mStatusCode != -1 || (retryCount2 = retryCount + 1) >= 3) {
            notifySsProgress(new SupplementaryServiceNotify(10, response.mSuccess, response.mReason));
        } else {
            setCallWaitingInfo(info, retryCount2, SoftphoneNamespaces.mTimeoutType2[retryCount2]);
        }
    }

    public void processSetCallForwardingInfoResponse(SoftphoneResponse response, int retryCount, CallForwardingInfo info) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processSetCallForwardingInfoResponse(): success: " + response.mSuccess + ", retryCount:" + retryCount);
        if (response.mStatusCode != -1 || (retryCount2 = retryCount + 1) >= 3) {
            notifySsProgress(new SupplementaryServiceNotify(11, response.mSuccess, response.mReason));
        } else {
            setCallForwardingInfo(info, retryCount2, SoftphoneNamespaces.mTimeoutType2[retryCount2]);
        }
    }

    public void processAkaChallengeResponse(AkaAuthenticationResponse response, int retryCount, String nonce) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "processAkaChallengeResponse(): retryCount: " + retryCount + "response: " + response);
        String result = SoftphoneAuthUtils.processAkaAuthenticationResponse(response);
        if (result.isEmpty()) {
            String str2 = this.LOG_TAG;
            IMSLog.e(str2, "aka failed " + (retryCount + 1) + " time(s)");
            int retryCount2 = retryCount + 1;
            if (retryCount2 < 3) {
                onRequestAkaChallenge(nonce, retryCount2);
                return;
            }
            IMSLog.e(this.LOG_TAG, "aka failed over 3 times, deregister...");
            ContextExt.sendBroadcastAsUser(this.mContext, new Intent("com.sec.imsservice.AKA_CHALLENGE_FAILED"), ContextExt.ALL);
            return;
        }
        IMSLog.i(this.LOG_TAG, "Sending AKA response Intent to SimManager");
        Intent intent = new Intent("com.sec.imsservice.AKA_CHALLENGE_COMPLETE");
        intent.putExtra("result", result);
        intent.putExtra("id", this.mProfileId);
        ContextExt.sendBroadcastAsUser(this.mContext, intent, ContextExt.ALL);
    }

    private String getContextInfo() {
        return "mdl=" + SoftphoneAuthUtils.getDeviceType(this.mContext) + ",os=" + Build.VERSION.RELEASE;
    }

    private SoftphoneHttpTransaction addMsipHeaders(SoftphoneHttpTransaction txn) {
        String deviceModel = SemFloatingFeature.getInstance().getString(ImsConstants.SecFloatingFeatures.CONFIG_BRAND_NAME);
        txn.addRequestHeader("x-att-clientId", SoftphoneNamespaces.SoftphoneSettings.MSIP_CLIENTID_PREFIX + deviceModel);
        txn.addRequestHeader("x-att-clientVersion", "1.0");
        txn.addRequestHeader("x-att-deviceId", this.mTelephonyManager.getDeviceId());
        txn.addRequestHeader("x-att-contextInfo", getContextInfo());
        return txn;
    }

    public void obtainPdCookies(int retryCount) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("obtainPdCookies(): retryCount: " + retryCount);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        if (1 == this.mEnvironment) {
            txn.setRequestURL("https://tprodsmsx.att.net/commonLogin/nxsEDAM/controller.do");
            txn.setQueryParameters(this.mRequestBuilder.buildObtainPdCookiesQueryParams(this.mAccountId, this.mUserId, this.mSecretKey, "messagessd.att.net"), true);
            txn.addRequestHeader(HttpController.HEADER_HOST, SoftphoneNamespaces.SoftphoneSettings.MSIP_PROD_TOKEN_HOST);
        } else {
            txn.setRequestURL("https://tstagesms.stage.att.net/commonLogin/nxsEDAM/controller.do");
            txn.setQueryParameters(this.mRequestBuilder.buildObtainPdCookiesQueryParams(this.mAccountId, this.mUserId, this.mSecretKey, "messagessd.stage.att.net"), true);
            txn.addRequestHeader(HttpController.HEADER_HOST, SoftphoneNamespaces.SoftphoneSettings.MSIP_STAGE_TOKEN_HOST);
        }
        txn.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        SoftphoneHttpTransaction txn2 = addMsipHeaders(txn);
        txn2.setRequestMethod(HttpRequestParams.Method.POST);
        this.mStateHandler.sendMessageDelayed(1020, retryCount, -1, txn2, ((long) retryCount) * 60000);
    }

    public void onRequestAkaChallenge(String nonce, int retryCount) {
        long timeout = SoftphoneNamespaces.mTimeoutType1[retryCount];
        String str = this.LOG_TAG;
        IMSLog.i(str, "onRequestAkaChallenge : nonce = " + nonce + ", retryCount = " + retryCount + ", timeout = " + timeout);
        String[] randAutn = SoftphoneAuthUtils.splitRandAutn(nonce);
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        txn.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.AKA_AUTH_PATH);
        txn.addRequestHeader("randomChallenge", randAutn[0]);
        txn.addRequestHeader("networkAuthenticatorToken", randAutn[1]);
        txn.setRequestMethod(HttpRequestParams.Method.GET);
        txn.setTimeout(timeout);
        Message message = Message.obtain((Handler) null, 19, retryCount, -1, txn);
        Bundle data = new Bundle();
        data.putString(WwwAuthenticateHeader.HEADER_PARAM_NONCE, nonce);
        message.setData(data);
        this.mStateHandler.sendMessage(message);
    }

    public void processObtainPdCookiesResponse(HttpResponseParams response, int retryCount) {
        if (response != null) {
            Map<String, List<String>> responseHeaders = response.getHeaders();
            if (responseHeaders != null) {
                List<String> cookies = responseHeaders.get("Set-Cookie");
                StringBuilder builder = new StringBuilder();
                if (cookies != null) {
                    for (String cookie : cookies) {
                        for (String part : cookie.split("[;,]")) {
                            if (part.contains("PD-ID=") || part.contains("PD-H-SESSION-ID")) {
                                builder.append(part);
                                builder.append(";");
                            }
                        }
                    }
                }
                String pdCookies = builder.toString();
                this.mEventLog.logAndAdd("processObtainPdCookiesResponse()");
                IMSLog.s(this.LOG_TAG, "processObtainPdCookiesResponse(): " + pdCookies);
                if (!pdCookies.isEmpty()) {
                    this.mSharedPrefHelper.save(this.mContext, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_PD_COOKIES, this.mEncryptionHelper.encrypt(pdCookies.substring(0, pdCookies.length() - 1), this.mSecretKey));
                    sendSMS(pdCookies, 0);
                    return;
                }
                retryObtainPdCookies(retryCount);
                return;
            }
            retryObtainPdCookies(retryCount);
            return;
        }
        retryObtainPdCookies(retryCount);
    }

    private void retryObtainPdCookies(int retryCount) {
        int retryCount2 = retryCount + 1;
        if (retryCount2 < 3) {
            obtainPdCookies(retryCount2);
        }
    }

    private void sendSMS(String cookies, int retryCount) {
        this.mEventLog.logAndAdd("sendSMS()");
        String impu = this.mIdentity.getImpu();
        SendSMSRequest sendSMSRequest = SoftphoneRequestBuilder.buildSendSMSRequest(impu.substring(impu.indexOf(":") + 1, impu.indexOf("@")));
        int transactionId = this.mStateHandler.getHttpTransactionId();
        SoftphoneHttpTransaction txn = new SoftphoneHttpTransaction(this);
        if (1 == this.mEnvironment) {
            txn.setRequestURL("https://messagessd.att.net/messaging/v0/outbound");
            txn.addRequestHeader(HttpController.HEADER_HOST, "messagessd.att.net");
        } else {
            txn.setRequestURL("https://messagessd.stage.att.net/messaging/v0/outbound");
            txn.addRequestHeader(HttpController.HEADER_HOST, "messagessd.stage.att.net");
        }
        txn.addRequestHeader(HttpController.HEADER_COOKIE, cookies);
        txn.addRequestHeader("Content-Type", "application/json");
        txn.addRequestHeader("Accept", "application/json");
        txn.addRequestHeader("transactionId", Integer.toString(transactionId));
        SoftphoneHttpTransaction txn2 = addMsipHeaders(txn);
        try {
            String nonEscapedStr = new JSONObject(new Gson().toJson(sendSMSRequest)).toString().replace("\\/", "/");
            String str = this.LOG_TAG;
            IMSLog.s(str, "sendSMS(): " + nonEscapedStr);
            txn2.setByteData(nonEscapedStr.getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            String str2 = this.LOG_TAG;
            IMSLog.s(str2, "could not build JSONObject:" + e.getMessage());
        }
        txn2.setRequestMethod(HttpRequestParams.Method.POST);
        this.mStateHandler.sendMessageDelayed(1022, retryCount, -1, txn2, 60000 * ((long) retryCount));
    }

    public void processSendSMSResponse(SoftphoneResponse response, int retryCount) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processSendSMSResponse(): success: " + response.mSuccess + ", retryCount: " + retryCount);
        if (!response.mSuccess && (retryCount2 = retryCount + 1) < 3) {
            SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
            Context context = this.mContext;
            sendSMS(this.mEncryptionHelper.decrypt(sharedPrefHelper.get(context, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_PD_COOKIES), this.mSecretKey), retryCount2);
        }
    }

    private boolean tokenExist() {
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        if (sharedPrefHelper != null) {
            Context context = this.mContext;
            this.mRefreshToken = sharedPrefHelper.get(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token");
            SimpleEventLog simpleEventLog = this.mEventLog;
            StringBuilder sb = new StringBuilder();
            sb.append("tokenExist: ");
            sb.append(this.mRefreshToken);
            simpleEventLog.logAndAdd(sb.toString());
            if (this.mRefreshToken != null) {
                return true;
            }
            return false;
        }
        this.mEventLog.logAndAdd("sharedPrefHelper is null");
        return false;
    }

    private void getAccountSecretKey() {
        Cursor cursor = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildAccountIdUri(this.mAccountId, (long) this.mUserId), (String[]) null, "userid = ?", new String[]{String.valueOf(this.mUserId)}, (String) null);
        if (cursor != null && cursor.moveToFirst()) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("found " + cursor.getCount() + " secretKey");
            this.mSecretKey = EncryptionHelper.getSecretKey(cursor);
            cursor.close();
        }
    }

    private void refreshTokenAfterRecovery() {
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        String str = sharedPrefHelper.get(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token");
        this.mRefreshToken = str;
        this.mRefreshToken = this.mEncryptionHelper.decrypt(str, this.mSecretKey);
        Message msg = this.mStateHandler.obtainMessage(15, 0, (int) SoftphoneNamespaces.mTimeoutType4[0], (Object) null);
        Bundle data = new Bundle();
        data.putInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 0);
        msg.setData(data);
        this.mStateHandler.sendMessage(msg);
    }

    private void handleImsNetworkIdentityAfterRecovery() {
        registerSoftphoneLabelObserver(this.mAccountId);
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        String impi = sharedPrefHelper.get(context, this.mAccountId + ":" + this.mUserId + ":" + "impi");
        SharedPrefHelper sharedPrefHelper2 = this.mSharedPrefHelper;
        Context context2 = this.mContext;
        String impu = sharedPrefHelper2.get(context2, this.mAccountId + ":" + this.mUserId + ":" + "impu");
        SharedPrefHelper sharedPrefHelper3 = this.mSharedPrefHelper;
        Context context3 = this.mContext;
        String fqdn = sharedPrefHelper3.get(context3, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_FQDN);
        if (impi == null || impu == null || fqdn == null) {
            this.mEventLog.logAndAdd("Recovery: no previous identity");
            getImsNetworkIdentifiers(false, true, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
            if (this.mResendSmsIntent == null) {
                SharedPrefHelper sharedPrefHelper4 = this.mSharedPrefHelper;
                Context context4 = this.mContext;
                resumeSmsAlarm(sharedPrefHelper4.getLong(context4, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.LAST_SMS_TIME, Calendar.getInstance().getTimeInMillis()));
                return;
            }
            return;
        }
        this.mEventLog.logAndAdd("Recovery: identity found. Try to relogin");
        this.mIdentity = new ImsNetworkIdentity(this.mEncryptionHelper.decrypt(impi, this.mSecretKey), this.mEncryptionHelper.decrypt(impu, this.mSecretKey), new ArrayList(Arrays.asList(new String[]{this.mEncryptionHelper.decrypt(fqdn, this.mSecretKey)})), this.mAppKey);
        releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_REFRESHSTATE);
    }

    public void processExchangeForAccessTokenResponse(AccessTokenResponse response, int retryCount, boolean autoRegister) {
        int retryCount2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processExchangeForAccessTokenResponse(): success: " + response.mSuccess + ", mReason: " + response.mReason + ", retryCount: " + retryCount + ", autoRegister: " + autoRegister);
        if (response.mSuccess) {
            updateAccountStatus(this.mAccountId, 2);
            saveTokens(response.mAccessToken, response.mTokenType, Long.parseLong(response.mExpiresIn), response.mRefreshToken);
            scheduleRefreshTokenAlarm(this.mTokenExpiresTime * 900, 0);
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_REDISTATE);
            getImsNetworkIdentifiers(false, autoRegister, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
            if (!autoRegister) {
                scheduleSmsAlarm();
                obtainPdCookies(0);
            } else if (this.mResendSmsIntent == null) {
                SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
                Context context = this.mContext;
                resumeSmsAlarm(sharedPrefHelper.getLong(context, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.LAST_SMS_TIME, Calendar.getInstance().getTimeInMillis()));
            }
        } else if (response.mStatusCode != -1 || (retryCount2 = retryCount + 1) >= 3) {
            resetAccountStatus();
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_INITSTATE);
        } else {
            boolean z = autoRegister;
            exchangeForAccessToken(this.mTGaurdToken, this.mAccountId, z, this.mTGaurdAppId, this.mEnvironment, retryCount2, SoftphoneNamespaces.mTimeoutType4[retryCount2]);
            return;
        }
        notifyProgress(new GeneralNotify(0, response.mSuccess, response.mReason));
    }
}
