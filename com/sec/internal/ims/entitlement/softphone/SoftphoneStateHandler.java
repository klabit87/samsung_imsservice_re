package com.sec.internal.ims.entitlement.softphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.State;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.entitlement.softphone.responses.AccessTokenResponse;
import com.sec.internal.ims.entitlement.softphone.responses.AddAddressResponse;
import com.sec.internal.ims.entitlement.softphone.responses.AddressValidationResponse;
import com.sec.internal.ims.entitlement.softphone.responses.AkaAuthenticationResponse;
import com.sec.internal.ims.entitlement.softphone.responses.CallForwardingResponse;
import com.sec.internal.ims.entitlement.softphone.responses.CallWaitingResponse;
import com.sec.internal.ims.entitlement.softphone.responses.ImsNetworkIdentifiersResponse;
import com.sec.internal.ims.entitlement.softphone.responses.SoftphoneResponse;
import com.sec.internal.ims.entitlement.softphone.responses.TermsAndConditionsResponse;
import com.sec.internal.log.IMSLog;
import com.sec.vsim.attsoftphone.data.GeneralNotify;

public class SoftphoneStateHandler extends VSimClient {
    private final IntentFilter INTENT_FILTER_AKA_CHALLENGE;
    private final IntentFilter INTENT_FILTER_LOCATION_SERVICE;
    private final IntentFilter INTENT_FILTER_SHUTDOWN_SERVICE;
    private final IntentFilter INTENT_FILTER_SOFTPHONE_ALARM;
    private final IntentFilter INTENT_FILTER_SOFTPHONE_REGISTRATION;
    public final String LOG_TAG;
    /* access modifiers changed from: private */
    public final String mAccountId;
    protected final State mActivatedState = new ActivatedState();
    protected final State mActivatingState = new ActivatingState();
    /* access modifiers changed from: private */
    public final State mAirplaneState = new AirplaneState();
    private final BroadcastReceiver mAkaEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String str = SoftphoneStateHandler.this.LOG_TAG;
            IMSLog.i(str, "Intent received : " + intent);
            if ("com.sec.imsservice.REQUEST_AKA_CHALLENGE".equals(intent.getAction())) {
                String nonce = intent.getStringExtra(WwwAuthenticateHeader.HEADER_PARAM_NONCE);
                String impi = intent.getStringExtra("impi");
                int id = intent.getIntExtra("id", 0);
                String str2 = SoftphoneStateHandler.this.LOG_TAG;
                IMSLog.s(str2, "AKA challenge for id: " + id + ", mProfileId: " + SoftphoneStateHandler.this.mClient.getProfileId() + ", impi: " + impi);
                if (SoftphoneStateHandler.this.mClient.getProfileId() == id) {
                    SoftphoneStateHandler.this.mClient.onRequestAkaChallenge(nonce, 0);
                }
            }
        }
    };
    protected SoftphoneClient mClient;
    private final Context mContext;
    protected final State mDeactivatingState = new DeactivatingState();
    public SimpleEventLog mEventLog;
    protected final State mInitialState = new InitialState();
    protected final State mReadyState = new ReadyState();
    /* access modifiers changed from: private */
    public final State mRefreshState = new RefreshState();
    protected final State mRegisteredState = new RegisteredState();
    protected final State mReleasingState = new ReleasingState();
    /* access modifiers changed from: private */
    public final State mReloginState = new ReloginState();
    /* access modifiers changed from: private */
    public final State mServiceOutState = new ServiceOut();
    private final BroadcastReceiver mSoftphoneAlarmReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("Receive Alarm Intent, action: " + action);
            if ("refresh_token".equalsIgnoreCase(action)) {
                Message msg = SoftphoneStateHandler.this.obtainMessage(15, 0, (int) SoftphoneNamespaces.mTimeoutType4[0], (Object) null);
                int attempt = intent.getIntExtra(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 3);
                Bundle data = new Bundle();
                data.putInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, attempt);
                msg.setData(data);
                SoftphoneStateHandler.this.sendMessage(msg);
            } else if (SoftphoneNamespaces.SoftphoneAlarm.ACTION_RESEND_SMS.equalsIgnoreCase(action)) {
                SoftphoneStateHandler.this.removeMessages(1020);
                SoftphoneStateHandler.this.sendMessage(16, 0);
                SoftphoneStateHandler.this.mClient.scheduleSmsAlarm();
            } else if (SoftphoneNamespaces.SoftphoneAlarm.ACTION_REFRESH_IDENTITY.equalsIgnoreCase(action)) {
                SoftphoneStateHandler.this.sendMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_RELOGIN, 0, 0);
            }
        }
    };
    private final BroadcastReceiver mSoftphoneRegistrationReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SoftphoneStateHandler.this.mEventLog.logAndAdd("Receive SoftphoneRegistrationFailure Intent");
            if (SoftphoneStateHandler.this.mClient.isTarget(intent.getStringExtra(SoftphoneContract.SoftphoneRegistrationFailure.EXTRA_IMPI))) {
            }
        }
    };
    private final BroadcastReceiver mSoftphoneShutdownReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SoftphoneStateHandler.this.mEventLog.logAndAdd("Receive Shutdown Intent");
            SoftphoneStateHandler.this.sendMessage(1024);
        }
    };
    protected final State mUserSwitchState = new UserSwitchState();

    public SoftphoneStateHandler(Looper looper, Context context, String accountId, SoftphoneClient client) {
        super(looper);
        this.mContext = context;
        this.mClient = client;
        this.mAccountId = accountId;
        this.LOG_TAG = SoftphoneStateHandler.class.getSimpleName() + '-' + this.mAccountId;
        this.mEventLog = new SimpleEventLog(context, this.LOG_TAG, 200);
        IntentFilter intentFilter = new IntentFilter();
        this.INTENT_FILTER_SOFTPHONE_ALARM = intentFilter;
        intentFilter.addAction("refresh_token");
        this.INTENT_FILTER_SOFTPHONE_ALARM.addAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_RESEND_SMS);
        this.INTENT_FILTER_SOFTPHONE_ALARM.addAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_REFRESH_IDENTITY);
        this.mContext.registerReceiver(this.mSoftphoneAlarmReceiver, this.INTENT_FILTER_SOFTPHONE_ALARM);
        IntentFilter intentFilter2 = new IntentFilter();
        this.INTENT_FILTER_SOFTPHONE_REGISTRATION = intentFilter2;
        intentFilter2.addAction(SoftphoneContract.SoftphoneRegistrationFailure.ACTION_TRY_REGISTER);
        this.mContext.registerReceiver(this.mSoftphoneRegistrationReceiver, this.INTENT_FILTER_SOFTPHONE_REGISTRATION);
        IntentFilter intentFilter3 = new IntentFilter();
        this.INTENT_FILTER_LOCATION_SERVICE = intentFilter3;
        intentFilter3.addAction("android.location.PROVIDERS_CHANGED");
        IntentFilter intentFilter4 = new IntentFilter();
        this.INTENT_FILTER_SHUTDOWN_SERVICE = intentFilter4;
        intentFilter4.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(this.mSoftphoneShutdownReceiver, this.INTENT_FILTER_SHUTDOWN_SERVICE);
        IntentFilter intentFilter5 = new IntentFilter();
        this.INTENT_FILTER_AKA_CHALLENGE = intentFilter5;
        intentFilter5.addAction("com.sec.imsservice.REQUEST_AKA_CHALLENGE");
        this.mContext.registerReceiver(this.mAkaEventReceiver, this.INTENT_FILTER_AKA_CHALLENGE);
        initState();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        this.mEventLog.logAndAdd("finalize()");
        super.finalize();
    }

    public int getAccountStatus() {
        int status = -1;
        Cursor cursor = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildAccountIdUri(this.mAccountId, (long) this.mClient.getUserId()), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor != null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("getAccountStatus found " + cursor.getCount() + " records");
            if (cursor.moveToFirst()) {
                status = cursor.getInt(cursor.getColumnIndex("status"));
            }
            cursor.close();
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("getAccountStatus status: " + status);
        return status;
    }

    private void initState() {
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mActivatingState, this.mDefaultState);
        addState(this.mReadyState, this.mDefaultState);
        addState(this.mActivatedState, this.mReadyState);
        addState(this.mRegisteredState, this.mActivatedState);
        addState(this.mRefreshState, this.mReadyState);
        addState(this.mAirplaneState, this.mReadyState);
        addState(this.mServiceOutState, this.mReadyState);
        addState(this.mReleasingState, this.mDefaultState);
        addState(this.mUserSwitchState, this.mReleasingState);
        addState(this.mDeactivatingState, this.mReleasingState);
        addState(this.mReloginState, this.mReleasingState);
        setInitialState(this.mInitialState);
        start();
    }

    private class InitialState extends State {
        private InitialState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
            SoftphoneStateHandler.this.mClient.startInitstate();
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 0) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(100, msg.arg1, msg.arg2));
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mActivatingState);
                return true;
            } else if (i != 15) {
                String str = SoftphoneStateHandler.this.LOG_TAG;
                IMSLog.e(str, "Unexpected event : current status is " + SoftphoneStateHandler.this.getAccountStatus());
                return false;
            } else {
                SoftphoneStateHandler.this.mClient.refreshToken(msg.arg1, (long) msg.arg2, msg.getData().getInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 3));
                SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                softphoneStateHandler2.transitionTo(softphoneStateHandler2.mReadyState);
                return true;
            }
        }
    }

    private class ActivatingState extends State {
        private ActivatingState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 100) {
                AccessTokenResponse response = (AccessTokenResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, AccessTokenResponse.class, 200);
                SoftphoneClient softphoneClient = SoftphoneStateHandler.this.mClient;
                int i2 = msg.arg1;
                boolean z = true;
                if (msg.arg2 != 1) {
                    z = false;
                }
                softphoneClient.processExchangeForAccessTokenResponse(response, i2, z);
                return true;
            } else if (i == 1018) {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mDeactivatingState);
                return true;
            } else if (i == 1027) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(100, msg.arg1, msg.arg2));
                return true;
            } else if (i == 1035) {
                SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                softphoneStateHandler2.transitionTo(softphoneStateHandler2.mReadyState);
                return true;
            } else if (i != 1036) {
                String str = SoftphoneStateHandler.this.LOG_TAG;
                IMSLog.e(str, "Unexpected event : current status is " + SoftphoneStateHandler.this.getAccountStatus());
                return false;
            } else {
                SoftphoneStateHandler softphoneStateHandler3 = SoftphoneStateHandler.this;
                softphoneStateHandler3.transitionTo(softphoneStateHandler3.mInitialState);
                return true;
            }
        }
    }

    private class ReadyState extends State {
        private ReadyState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 1) {
                Message onComplete = SoftphoneStateHandler.this.obtainMessage(101, msg.arg1, msg.arg2);
                onComplete.setData(msg.getData());
                ((SoftphoneHttpTransaction) msg.obj).commit(onComplete);
                return true;
            } else if (i == 2) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(102, msg.arg1, msg.arg2));
                return true;
            } else if (i == 3) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(103, msg.arg1, msg.arg2));
                return true;
            } else if (i == 4) {
                SoftphoneStateHandler.this.mClient.getImsNetworkIdentifiers(true, false, 0, SoftphoneNamespaces.mTimeoutType1[0], msg.arg1);
                return true;
            } else if (i != 6) {
                if (i != 7) {
                    if (i == 18) {
                        SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_IDENTITY_RELEASED, (String) null);
                        return true;
                    } else if (i == 19) {
                        Message onComplete2 = SoftphoneStateHandler.this.obtainMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_REQUEST_AKA_CHALLENGE_DONE, msg.arg1, msg.arg2);
                        onComplete2.setData(msg.getData());
                        ((SoftphoneHttpTransaction) msg.obj).commit(onComplete2);
                        return true;
                    } else if (i == 1010) {
                        SoftphoneStateHandler.this.mClient.processSetCallWaitingInfoResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 200), msg.arg1, msg.getData().getParcelable("communication-waiting"));
                        return true;
                    } else if (i == 1011) {
                        SoftphoneStateHandler.this.mClient.processSetCallForwardingInfoResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 200), msg.arg1, msg.getData().getParcelable("communication-diversion"));
                        return true;
                    } else if (i == 1017) {
                        SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                        SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_DEREGISTERED, (String) msg.obj);
                        return true;
                    } else if (i == 1018) {
                        SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                        softphoneStateHandler.transitionTo(softphoneStateHandler.mDeactivatingState);
                        SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                        return true;
                    } else if (i == 1024) {
                        SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                        softphoneStateHandler2.transitionTo(softphoneStateHandler2.mReleasingState);
                        SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                        SoftphoneStateHandler.this.mClient.resetCurrentAddresses();
                        return true;
                    } else if (i == 1025) {
                        SoftphoneStateHandler softphoneStateHandler3 = SoftphoneStateHandler.this;
                        softphoneStateHandler3.transitionTo(softphoneStateHandler3.mUserSwitchState);
                        SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                        SoftphoneStateHandler.this.mClient.resetCurrentAddresses();
                        return true;
                    } else if (i == 1030) {
                        SoftphoneStateHandler softphoneStateHandler4 = SoftphoneStateHandler.this;
                        softphoneStateHandler4.transitionTo(softphoneStateHandler4.mReloginState);
                        SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                        return true;
                    } else if (i == 1031) {
                        SoftphoneStateHandler softphoneStateHandler5 = SoftphoneStateHandler.this;
                        softphoneStateHandler5.transitionTo(softphoneStateHandler5.mAirplaneState);
                        SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                        SoftphoneStateHandler.this.mClient.resetCurrentAddresses();
                        return true;
                    } else if (i == 1033) {
                        SoftphoneStateHandler softphoneStateHandler6 = SoftphoneStateHandler.this;
                        softphoneStateHandler6.transitionTo(softphoneStateHandler6.mServiceOutState);
                        return true;
                    } else if (i != 1034) {
                        switch (i) {
                            case 7:
                                break;
                            case 8:
                                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(108, msg.arg1, msg.arg2));
                                return true;
                            case 9:
                                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(109, msg.arg1, msg.arg2));
                                return true;
                            case 10:
                                Message onComplete3 = SoftphoneStateHandler.this.obtainMessage(1010, msg.arg1, msg.arg2);
                                onComplete3.setData(msg.getData());
                                ((SoftphoneHttpTransaction) msg.obj).commit(onComplete3);
                                return true;
                            case 11:
                                Message onComplete4 = SoftphoneStateHandler.this.obtainMessage(1011, msg.arg1, msg.arg2);
                                onComplete4.setData(msg.getData());
                                ((SoftphoneHttpTransaction) msg.obj).commit(onComplete4);
                                return true;
                            case 15:
                                SoftphoneStateHandler.this.mClient.refreshToken(msg.arg1, (long) msg.arg2, msg.getData().getInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 3));
                                return true;
                            case 1015:
                                HttpResponseParams httpResponse = (HttpResponseParams) msg.obj;
                                int statusCode = -1;
                                if (httpResponse != null) {
                                    statusCode = httpResponse.getStatusCode();
                                }
                                SoftphoneStateHandler.this.mClient.processRefreshTokenResponse((AccessTokenResponse) SoftphoneResponseUtils.parseJsonResponse(httpResponse, AccessTokenResponse.class, 200), statusCode, msg.arg1, msg.arg2);
                                return true;
                            case 1020:
                                SoftphoneStateHandler.this.deferMessage(msg);
                                return true;
                            case 1022:
                                SoftphoneStateHandler.this.deferMessage(msg);
                                return true;
                            case SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_REFRESHSTATE:
                                SoftphoneStateHandler softphoneStateHandler7 = SoftphoneStateHandler.this;
                                softphoneStateHandler7.transitionTo(softphoneStateHandler7.mRefreshState);
                                return true;
                            case SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_ACTIVATEDSTATE:
                                SoftphoneStateHandler softphoneStateHandler8 = SoftphoneStateHandler.this;
                                softphoneStateHandler8.transitionTo(softphoneStateHandler8.mActivatedState);
                                return true;
                            default:
                                switch (i) {
                                    case 101:
                                        SoftphoneStateHandler.this.mClient.processImsNetworkIdentifiersResponse((ImsNetworkIdentifiersResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, ImsNetworkIdentifiersResponse.class, 200), false, msg.arg1, msg.arg2 == 1, msg.getData().getInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 6));
                                        return true;
                                    case 102:
                                        SoftphoneStateHandler.this.mClient.processTermsAndConditionsResponse((TermsAndConditionsResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, TermsAndConditionsResponse.class, 200), msg.arg1);
                                        return true;
                                    case 103:
                                        SoftphoneStateHandler.this.mClient.processProvisionAccountResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 204), msg.arg1);
                                        return true;
                                    case 104:
                                        SoftphoneStateHandler.this.mClient.processImsNetworkIdentifiersResponse((ImsNetworkIdentifiersResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, ImsNetworkIdentifiersResponse.class, 200), true, msg.arg1, msg.arg2 == 1, msg.getData().getInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 6));
                                        return true;
                                    default:
                                        switch (i) {
                                            case 106:
                                                AddressValidationResponse response = (AddressValidationResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, AddressValidationResponse.class, 201);
                                                response.mTransactionId = msg.arg1;
                                                response.mAddressId = msg.arg2;
                                                Bundle data = msg.getData();
                                                response.mConfirmed = data.getBoolean(SoftphoneNamespaces.SoftphoneSettings.CONFIRMED, false);
                                                SoftphoneStateHandler.this.mClient.processValidateE911AddressResponse(response, data.getInt("retry_count", 3));
                                                return true;
                                            case 107:
                                                SoftphoneStateHandler.this.mClient.processAddE911AddressResponse((AddAddressResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, AddAddressResponse.class, 200), msg.arg1, msg.arg2);
                                                return true;
                                            case 108:
                                                SoftphoneStateHandler.this.mClient.processGetCallWaitingInfoResponse((CallWaitingResponse) SoftphoneResponseUtils.parseXmlResponse((HttpResponseParams) msg.obj, CallWaitingResponse.class, 200, false), msg.arg1);
                                                return true;
                                            case 109:
                                                HttpResponseParams httpResponse2 = (HttpResponseParams) msg.obj;
                                                if (httpResponse2 != null) {
                                                    String xml = httpResponse2.getDataString();
                                                    if (xml != null) {
                                                        xml = xml.replace("<cp:conditions></cp:conditions>", "<cp:conditions><ss:unconditional/></cp:conditions>");
                                                    }
                                                    httpResponse2.setDataString(xml);
                                                }
                                                SoftphoneStateHandler.this.mClient.processGetCallForwardingInfoResponse((CallForwardingResponse) SoftphoneResponseUtils.parseXmlResponse(httpResponse2, CallForwardingResponse.class, 200, false), msg.arg1);
                                                return true;
                                            default:
                                                String str = SoftphoneStateHandler.this.LOG_TAG;
                                                IMSLog.e(str, "Unexpected event : current status is " + SoftphoneStateHandler.this.getAccountStatus());
                                                return false;
                                        }
                                }
                        }
                    } else {
                        SoftphoneStateHandler.this.mClient.processAkaChallengeResponse((AkaAuthenticationResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, AkaAuthenticationResponse.class, 200), msg.arg1, msg.getData().getString(WwwAuthenticateHeader.HEADER_PARAM_NONCE));
                        return true;
                    }
                }
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(107, msg.arg1, msg.arg2));
                return true;
            } else {
                Message onComplete5 = SoftphoneStateHandler.this.obtainMessage(106, msg.arg1, msg.arg2);
                onComplete5.setData(msg.getData());
                ((SoftphoneHttpTransaction) msg.obj).commit(onComplete5);
                return true;
            }
        }
    }

    private class ActivatedState extends State {
        private ActivatedState() {
        }

        public boolean processMessage(Message msg) {
            SoftphoneStateHandler.this.mEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i != 14) {
                boolean z = true;
                if (i == 1014) {
                    SoftphoneStateHandler.this.mClient.notifyRegisterStatus(false, "AKA failed.");
                    SoftphoneStateHandler.this.mClient.getAutoRetryComSet(false, true);
                    SoftphoneStateHandler.this.mClient.handleDeRegisterRequest();
                    SoftphoneStateHandler.this.sendMessage(1018);
                    return true;
                } else if (i == 16) {
                    SoftphoneStateHandler.this.mClient.obtainPdCookies(msg.arg1);
                    return true;
                } else if (i == 17) {
                    SoftphoneStateHandler.this.mClient.handleDeRegisterRequest();
                    return true;
                } else if (i == 1016) {
                    SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_REGISTERED, (String) msg.obj);
                    SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                    softphoneStateHandler.transitionTo(softphoneStateHandler.mRegisteredState);
                    return true;
                } else if (i != 1017) {
                    if (i == 1028) {
                        SoftphoneClient softphoneClient = SoftphoneStateHandler.this.mClient;
                        int i2 = msg.arg1;
                        if (msg.arg2 != 1) {
                            z = false;
                        }
                        softphoneClient.reLogin(i2, z);
                        return true;
                    } else if (i != 1029) {
                        return false;
                    } else {
                        SoftphoneStateHandler.this.deferMessage(msg);
                        return true;
                    }
                } else if (msg.arg1 == 1408) {
                    return true;
                } else {
                    SoftphoneStateHandler.this.mClient.notifyRegisterStatus(false, (String) null);
                    return true;
                }
            } else {
                SoftphoneStateHandler.this.mClient.handleTryRegisterRequest();
                return true;
            }
        }
    }

    private class RegisteredState extends State {
        private RegisteredState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
            SoftphoneStateHandler.this.mClient.notifyRegisterStatus(true, (String) null);
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 14) {
                SoftphoneStateHandler.this.mClient.notifyRegisterStatus(true, (String) null);
                return true;
            } else if (i == 1025) {
                SoftphoneStateHandler.this.mClient.updateAccountStatus(SoftphoneStateHandler.this.mAccountId, 4);
                return false;
            } else if (i == 1029) {
                SoftphoneStateHandler.this.mClient.handleDeRegisterRequest();
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mRefreshState);
                return true;
            } else if (i != 1030) {
                switch (i) {
                    case 1017:
                        SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_DEREGISTERED, (String) msg.obj);
                        SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                        softphoneStateHandler2.transitionTo(softphoneStateHandler2.mActivatedState);
                        return true;
                    case 1018:
                        SoftphoneStateHandler softphoneStateHandler3 = SoftphoneStateHandler.this;
                        softphoneStateHandler3.transitionTo(softphoneStateHandler3.mDeactivatingState);
                        return true;
                    case 1019:
                        SoftphoneStateHandler.this.mClient.handleLabelUpdated();
                        return true;
                    case 1020:
                        ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(1021, msg.arg1, msg.arg2));
                        return true;
                    case 1021:
                        SoftphoneStateHandler.this.mClient.processObtainPdCookiesResponse((HttpResponseParams) msg.obj, msg.arg1);
                        return true;
                    case 1022:
                        ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_SEND_MESSAGE_DONE, msg.arg1, msg.arg2));
                        return true;
                    case SoftphoneNamespaces.SoftphoneEvents.EVENT_SEND_MESSAGE_DONE:
                        SoftphoneStateHandler.this.mClient.processSendSMSResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 200), msg.arg1);
                        return true;
                    default:
                        return false;
                }
            } else {
                SoftphoneStateHandler softphoneStateHandler4 = SoftphoneStateHandler.this;
                softphoneStateHandler4.transitionTo(softphoneStateHandler4.mReloginState);
                return true;
            }
        }
    }

    private class RefreshState extends State {
        private RefreshState() {
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 5) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(105, msg.arg1, msg.arg2));
                return true;
            } else if (i == 18) {
                SoftphoneStateHandler.this.mClient.getImsNetworkIdentifiers(false, true, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mReadyState);
                return true;
            } else if (i != 105) {
                return false;
            } else {
                SoftphoneStateHandler.this.mClient.processReleaseImsNetworkIdentitiesResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 204), msg.arg1);
                return true;
            }
        }
    }

    private class AirplaneState extends State {
        private AirplaneState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
            SoftphoneStateHandler.this.mClient.updateAccountStatus(SoftphoneStateHandler.this.mAccountId, 4);
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 5) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(105, msg.arg1, msg.arg2));
                return true;
            } else if (i == 101) {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mReadyState);
                return false;
            } else if (i == 105) {
                SoftphoneStateHandler.this.mClient.processReleaseImsNetworkIdentitiesResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 204), msg.arg1);
                return true;
            } else if (i == 1017) {
                return true;
            } else {
                if (i != 1032) {
                    return false;
                }
                SoftphoneStateHandler.this.mClient.getImsNetworkIdentifiers(false, true, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
                return true;
            }
        }
    }

    private class ServiceOut extends State {
        private ServiceOut() {
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            if (msg.what != 1032) {
                return false;
            }
            SoftphoneStateHandler.this.mClient.handleDeRegisterRequest();
            SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
            softphoneStateHandler.transitionTo(softphoneStateHandler.mRefreshState);
            return true;
        }
    }

    private class ReleasingState extends State {
        private ReleasingState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 0) {
                SoftphoneStateHandler.this.mClient.notifyProgress(new GeneralNotify(0, false, "Logout is in progress. Please try again later."));
                return true;
            } else if (i == 5) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(105, msg.arg1, msg.arg2));
                return true;
            } else if (i == 105) {
                SoftphoneStateHandler.this.mClient.processReleaseImsNetworkIdentitiesResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 204), msg.arg1);
                return true;
            } else if (i == 1017) {
                SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_DEREGISTERED, (String) msg.obj);
                return true;
            } else if (i == 1034) {
                String nonce = msg.getData().getString(WwwAuthenticateHeader.HEADER_PARAM_NONCE);
                SoftphoneStateHandler.this.mClient.processAkaChallengeResponse((AkaAuthenticationResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, AkaAuthenticationResponse.class, 200), msg.arg1, nonce);
                return true;
            } else if (i == 18) {
                SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_IDENTITY_RELEASED, (String) null);
                return true;
            } else if (i != 19) {
                return false;
            } else {
                Message onComplete = SoftphoneStateHandler.this.obtainMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_REQUEST_AKA_CHALLENGE_DONE, msg.arg1, msg.arg2);
                onComplete.setData(msg.getData());
                ((SoftphoneHttpTransaction) msg.obj).commit(onComplete);
                return true;
            }
        }
    }

    private class UserSwitchState extends State {
        private UserSwitchState() {
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 1017) {
                return true;
            }
            if (i == 1026) {
                SoftphoneStateHandler.this.mClient.updateAccountStatus(SoftphoneStateHandler.this.mAccountId, 2);
                return true;
            } else if (i != 1032) {
                return false;
            } else {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mReadyState);
                SoftphoneStateHandler.this.mClient.getImsNetworkIdentifiers(false, true, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
                return true;
            }
        }
    }

    private class DeactivatingState extends State {
        private DeactivatingState() {
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 12) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(1012, msg.arg1, msg.arg2));
                return true;
            } else if (i == 13) {
                ((SoftphoneHttpTransaction) msg.obj).commit(SoftphoneStateHandler.this.obtainMessage(1013, msg.arg1, msg.arg2));
                return true;
            } else if (i == 18) {
                SoftphoneStateHandler.this.mClient.revokeAccessToken();
                return true;
            } else if (i == 1017) {
                SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                return true;
            } else if (i == 1036) {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mInitialState);
                return true;
            } else if (i == 1012) {
                SoftphoneStateHandler.this.mClient.processRevokeAccessTokenResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 200));
                return true;
            } else if (i != 1013) {
                return false;
            } else {
                SoftphoneStateHandler.this.mClient.processRevokeRefreshTokenResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) msg.obj, SoftphoneResponse.class, 200));
                return true;
            }
        }
    }

    private class ReloginState extends State {
        private ReloginState() {
        }

        public boolean processMessage(Message msg) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + msg.what);
            int i = msg.what;
            if (i == 18) {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mInitialState);
                return true;
            } else if (i != 1018) {
                return false;
            } else {
                SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                softphoneStateHandler2.transitionTo(softphoneStateHandler2.mDeactivatingState);
                return true;
            }
        }
    }
}
