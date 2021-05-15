package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.ims.config.util.AKAEapAuthHelper;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSClient;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseFlowImpl extends Handler {
    public static final int ACTIVATE_SIM_DEVICE = 32;
    public static final int AKA_TOKEN_PRESENT = 1;
    private static final int AKA_TOKEN_RECEIVED = 5;
    protected static final int CHALLENGE_CALCULATED = 2;
    public static final int CHECK_LOC_AND_TC = 33;
    public static final int DEACTIVATE_DEVICE = 40;
    public static final int ENTITLMENT_CHECK = 30;
    protected static final int EVENT_SIM_AUTH_RESPONSE = 4;
    protected static final int INITIAL_3GPP_AUTH_RESPONSE = 1;
    protected static final String KEY_AKA_TOKEN = "AKA_TOKEN";
    protected static final String KEY_IMSI_EAP = "IMSI_EAP";
    public static final String KEY_REQUEST_MESSAGE = "REQUEST_MESSAGE";
    private static final String LOG_TAG = BaseFlowImpl.class.getSimpleName();
    public static final int REGISTER_PUSH_TOKEN = 41;
    public static final int REMOVE_PUSH_TOKEN = 42;
    protected static final int RESPONSE_RECEIVED = 3;
    public static final int RETRIEVE_AKA_TOKEN = 47;
    public static final int RETRIEVE_AVAILABLE_MSISDN = 45;
    public static final int RETRIEVE_DEVICE_CONFIG = 31;
    public static final int UPDATE_DEVICE_CONFIG = 38;
    protected String mAkaToken = null;
    private Context mContext;
    private ArrayList<Message> mDeferredMessages = new ArrayList<>();
    protected NSDSClient mNSDSClient;
    private String mSimAuthType = null;
    private ISimManager mSimManager;

    public BaseFlowImpl(Looper looper, Context context, ISimManager simManager) {
        super(looper);
        this.mContext = context;
        this.mSimManager = simManager;
        this.mNSDSClient = new NSDSClient(context, simManager);
        getAkaTokenFromSharedPreference();
        IMSLog.i(LOG_TAG, "created.");
    }

    public NSDSClient getNSDSClient() {
        return this.mNSDSClient;
    }

    public ISimManager getSimManager() {
        return this.mSimManager;
    }

    public void setSimAuthAppType(String simAuthAppType) {
        this.mSimAuthType = simAuthAppType;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String str = LOG_TAG;
        IMSLog.i(str, "handleMessage: evt " + msg.what);
        boolean isAkaTokenPresent = msg.arg1 == 1;
        int i = msg.what;
        if (i == 1) {
            Bundle bundle = msg.getData();
            Response3gppAuthentication response3gppAuthentication = (Response3gppAuthentication) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH);
            if (isResponseAkaChallenge(response3gppAuthentication)) {
                calculateChallenge(response3gppAuthentication);
                return;
            }
            IMSLog.e(LOG_TAG, "responseCollection is null");
            reportResultForDeferredMessages(bundle);
        } else if (i != 2) {
            if (i == 3) {
                onResponseReceived(msg);
            } else if (i == 4) {
                processSimAuthResponse(msg.getData().getString("AKA_CHALLENGE"), (String) msg.obj);
            } else if (i == 5) {
                moveDeferredMessageAtFrontOfQueue();
            } else if (!isAkaTokenPresent) {
                processBaseRequest(msg);
            } else {
                executeRequestWithAkaToken(msg, (String) msg.getData().get("IMSI_EAP"), (String) msg.getData().get(KEY_AKA_TOKEN));
            }
        } else if (!this.mDeferredMessages.isEmpty()) {
            executeRequest(this.mDeferredMessages.remove(0), (String) msg.obj);
        } else {
            IMSLog.e(LOG_TAG, "!!!!Deferred messages should not be empty here.!!!. It will recover with initial3gppAuth");
        }
    }

    private void processBaseRequest(Message msg) {
        if (getAkaTokenFromSharedPreference() != null) {
            executeRequest(msg, (String) null);
        } else if (this.mDeferredMessages.isEmpty()) {
            deferWithZeroIndexMessage(msg);
            requestInitial3gppAuthentication(Message.obtain(msg));
        } else {
            deferMessage(msg);
        }
    }

    public void performOperationWithAkaToken(int operation, String imsiEap, String akaToken, NSDSBaseProcedure responseValidator, Messenger messenger) {
        Message message = obtainMessage(operation, responseValidator);
        message.arg1 = 1;
        Bundle bundle = new Bundle();
        bundle.putString("IMSI_EAP", imsiEap);
        bundle.putString(KEY_AKA_TOKEN, akaToken);
        message.setData(bundle);
        message.replyTo = messenger;
        sendMessage(message);
    }

    public void performOperation(int operation, NSDSBaseProcedure responseValidator, Messenger messenger) {
        Message message = obtainMessage(operation, responseValidator);
        message.replyTo = messenger;
        sendMessage(message);
    }

    public void resubmitWithChallenge(Message challengedMessage, Response3gppAuthentication response3gppAuthentication) {
        String str = LOG_TAG;
        IMSLog.i(str, "resubmitWithChallenge:" + challengedMessage);
        if (this.mDeferredMessages.isEmpty()) {
            deferWithZeroIndexMessage(challengedMessage);
            calculateChallenge(response3gppAuthentication);
            return;
        }
        deferMessage(challengedMessage);
    }

    private String getAkaTokenFromSharedPreference() {
        SharedPreferences pref = NSDSSharedPrefHelper.getSharedPref(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (pref != null) {
            this.mAkaToken = pref.getString(this.mSimManager.getImsi(), (String) null);
        }
        return this.mAkaToken;
    }

    private final void deferWithZeroIndexMessage(Message msg) {
        if (msg != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "deferWithZeroIndexMessage msg:" + msg.toString());
            this.mDeferredMessages.add(0, Message.obtain(msg));
        }
    }

    /* access modifiers changed from: protected */
    public final void deferMessage(Message msg) {
        String str = LOG_TAG;
        IMSLog.i(str, "deferMessage msg:" + msg.toString());
        this.mDeferredMessages.add(Message.obtain(msg));
    }

    private void moveDeferredMessageAtFrontOfQueue() {
        for (int i = 0; i < this.mDeferredMessages.size(); i++) {
            Message curMsg = this.mDeferredMessages.get(i);
            String str = LOG_TAG;
            IMSLog.i(str, "moveDeferredMessageAtFrontOfQueue; what=" + curMsg.what);
            sendMessageAtFrontOfQueue(curMsg);
        }
        this.mDeferredMessages.clear();
    }

    private void reportResultForDeferredMessages(Bundle bundleNSDSResponses) {
        IMSLog.i(LOG_TAG, "3gpp auth failed. reportResultForDeferredMessages: ");
        for (int i = 0; i < this.mDeferredMessages.size(); i++) {
            reportResult(this.mDeferredMessages.get(i), bundleNSDSResponses);
        }
        this.mDeferredMessages.clear();
    }

    private void executeRequest(Message msg, String calculatedChallenge) {
        Message message = msg;
        String akaToken = getAkaTokenFromSharedPreference();
        NSDSBaseProcedure baseProcedure = (NSDSBaseProcedure) message.obj;
        NSDSRequest[] arrRequest = baseProcedure.buildRequests(new NSDSCommonParameters(calculatedChallenge, akaToken, NSDSHelper.getImsiEap(this.mContext, this.mSimManager.getSimSlotIndex(), this.mSimManager.getImsi(), this.mSimManager.getSimOperator()), getEncodedDeviceId()));
        if (arrRequest == null) {
            IMSLog.e(LOG_TAG, "executeRequest: NSDS Requests is null. reporting failure");
            reportResult(message, (Bundle) null);
            return;
        }
        this.mNSDSClient.executeRequestCollection(arrRequest, obtainReponseReceivedMessage(msg), baseProcedure.shouldIncludeAuthHeader(), baseProcedure.getVersionInfo(), baseProcedure.getUserAgent(), baseProcedure.getImeiForUA(), getDeviceId(), this.mSimManager.getImsi());
    }

    private void executeRequestWithAkaToken(Message msg, String imsiEap, String akaToken) {
        NSDSBaseProcedure baseProcedure = (NSDSBaseProcedure) msg.obj;
        NSDSRequest[] arrRequest = baseProcedure.buildRequests(new NSDSCommonParameters((String) null, akaToken, imsiEap, getEncodedDeviceId()));
        if (arrRequest == null) {
            IMSLog.e(LOG_TAG, "executeRequest: NSDS Requests is null. reporting failure");
            reportResult(msg, (Bundle) null);
            return;
        }
        this.mNSDSClient.executeRequestCollection(arrRequest, obtainReponseReceivedMessage(msg), baseProcedure.getVersionInfo(), this.mSimManager.getImsi(), getDeviceId());
    }

    private Message obtainReponseReceivedMessage(Message msg) {
        Bundle requestBundle = new Bundle();
        requestBundle.putParcelable(KEY_REQUEST_MESSAGE, Message.obtain(msg));
        Message parsedResponseMessage = obtainMessage(3);
        parsedResponseMessage.setData(requestBundle);
        return parsedResponseMessage;
    }

    /* JADX WARNING: type inference failed for: r3v12, types: [android.os.Parcelable] */
    /* JADX WARNING: type inference failed for: r3v14, types: [android.os.Parcelable] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onResponseReceived(android.os.Message r8) {
        /*
            r7 = this;
            android.os.Bundle r0 = r8.getData()
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Response received : "
            r2.append(r3)
            if (r0 == 0) goto L_0x0017
            java.lang.String r3 = r0.toString()
            goto L_0x0019
        L_0x0017:
            java.lang.String r3 = "null"
        L_0x0019:
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r1, r2)
            r1 = 0
            r2 = 0
            if (r0 == 0) goto L_0x0039
            java.lang.String r3 = "3gppAuthentication"
            android.os.Parcelable r3 = r0.getParcelable(r3)
            r1 = r3
            com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication r1 = (com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication) r1
            java.lang.String r3 = "REQUEST_MESSAGE"
            android.os.Parcelable r3 = r0.getParcelable(r3)
            r2 = r3
            android.os.Message r2 = (android.os.Message) r2
        L_0x0039:
            boolean r3 = r7.isAuthenticationSuccessful(r1)
            if (r3 == 0) goto L_0x0068
            java.lang.String r3 = r1.akaToken
            if (r3 == 0) goto L_0x0068
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "onResponseReceived: akaToken:"
            r4.append(r5)
            java.lang.String r5 = r1.akaToken
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.s(r3, r4)
            java.lang.String r3 = r1.akaToken
            r7.mAkaToken = r3
            r7.updateAkaTokenInSharedPref(r3)
            r3 = 5
            r7.sendEmptyMessage(r3)
            goto L_0x0095
        L_0x0068:
            boolean r3 = r7.isResponseAkaChallenge(r1)
            if (r3 == 0) goto L_0x0095
            r3 = 0
            if (r2 == 0) goto L_0x0079
            int r4 = r2.arg1
            r5 = 1
            if (r4 != r5) goto L_0x0077
            goto L_0x0078
        L_0x0077:
            r5 = 0
        L_0x0078:
            r3 = r5
        L_0x0079:
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "response is akaChallenge. shouldIgnoreChallenge:"
            r5.append(r6)
            r5.append(r3)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r4, r5)
            if (r3 != 0) goto L_0x0095
            r7.clearAkaToken()
        L_0x0095:
            r7.reportResult(r2, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl.onResponseReceived(android.os.Message):void");
    }

    private void clearAkaToken() {
        IMSLog.i(LOG_TAG, "clearAkaToken()");
        this.mAkaToken = null;
        NSDSSharedPrefHelper.removeAkaToken(this.mContext, this.mSimManager.getImsi());
    }

    /* access modifiers changed from: protected */
    public void reportResult(Message requestMessage, Bundle bundleNSDSResponses) {
        if (requestMessage != null) {
            try {
                if (requestMessage.replyTo != null) {
                    Message responseMessage = Message.obtain((Handler) null, 1);
                    responseMessage.setData(bundleNSDSResponses);
                    requestMessage.replyTo.send(responseMessage);
                    return;
                }
            } catch (RemoteException re) {
                String str = LOG_TAG;
                IMSLog.s(str, "Could not send the response" + re.getMessage());
                return;
            }
        }
        IMSLog.e(LOG_TAG, "!!!requestMsg is null or requestMsg.replyTo is null!!!!");
    }

    /* access modifiers changed from: protected */
    public void requestInitial3gppAuthentication(Message msg) {
        IMSLog.i(LOG_TAG, "requestInitial3gppAuthentication()");
        this.mNSDSClient.executeRequestCollection(new NSDSRequest[]{this.mNSDSClient.buildAuthenticationRequest(new AtomicInteger().incrementAndGet(), true, (String) null, (String) null, (String) null, NSDSHelper.getImsiEap(this.mContext, this.mSimManager.getSimSlotIndex(), this.mSimManager.getImsi(), this.mSimManager.getSimOperator()), getEncodedDeviceId())}, obtainMessage(1), ((NSDSBaseProcedure) msg.obj).getVersionInfo(), this.mSimManager.getImsi(), getDeviceId());
    }

    public String getDeviceId() {
        return DeviceIdHelper.getDeviceId(this.mContext, this.mSimManager.getSimSlotIndex());
    }

    public String getEncodedDeviceId() {
        return DeviceIdHelper.getEncodedDeviceId(getDeviceId());
    }

    private void requestIsimAuthentication(String nonce, String akaChallenge) {
        IMSLog.i(LOG_TAG, "requestIsimAuthentication");
        Message msg = obtainMessage(4);
        Bundle dataMap = new Bundle();
        dataMap.putString("AKA_CHALLENGE", akaChallenge);
        msg.setData(dataMap);
        String str = this.mSimAuthType;
        if (str == null || !str.equals(NSDSNamespaces.NSDSSimAuthType.USIM)) {
            String str2 = this.mSimAuthType;
            if (str2 == null || !str2.equals(NSDSNamespaces.NSDSSimAuthType.ISIM)) {
                this.mSimManager.requestIsimAuthentication(nonce, msg);
            } else {
                this.mSimManager.requestIsimAuthentication(nonce, 5, msg);
            }
        } else {
            this.mSimManager.requestIsimAuthentication(nonce, 2, msg);
        }
    }

    private void calculateChallenge(Response3gppAuthentication response3gppAuthentication) {
        this.mAkaToken = response3gppAuthentication.akaToken;
        String challenge = AKAEapAuthHelper.decodeChallenge(response3gppAuthentication.akaChallenge);
        requestIsimAuthentication(AKAEapAuthHelper.getNonce(challenge), challenge);
    }

    private void processSimAuthResponse(String akaChallenge, String isimResponse) {
        String imsiEap = NSDSHelper.getImsiEap(this.mContext, this.mSimManager.getSimSlotIndex(), this.mSimManager.getImsi(), this.mSimManager.getSimOperator());
        if (imsiEap == null) {
            IMSLog.e(LOG_TAG, "process3gppAuthResponse: failed to get SIM info");
            report3gppAuthError((Response3gppAuthentication) null);
            return;
        }
        String challengeResponse = AKAEapAuthHelper.generateChallengeResponse(akaChallenge, isimResponse, imsiEap);
        if (challengeResponse == null) {
            IMSLog.e(LOG_TAG, "process3gppAuthResponse: failed to generate challenge response");
            report3gppAuthError((Response3gppAuthentication) null);
            return;
        }
        String str = LOG_TAG;
        IMSLog.s(str, "process3gppAuthResponse: challenge response " + challengeResponse);
        Message messageProcessCalculatedChallenge = obtainMessage(2);
        messageProcessCalculatedChallenge.obj = challengeResponse;
        messageProcessCalculatedChallenge.sendToTarget();
    }

    private void report3gppAuthError(Response3gppAuthentication reponse3gppAuthentication) {
        Bundle bundleNSDSResponses = new Bundle();
        if (reponse3gppAuthentication != null) {
            bundleNSDSResponses.putParcelable(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH, reponse3gppAuthentication);
        }
        reportResultForDeferredMessages(bundleNSDSResponses);
    }

    /* access modifiers changed from: protected */
    public void updateAkaTokenInSharedPref(String akaToken) {
        SharedPreferences pref = NSDSSharedPrefHelper.getSharedPref(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (pref == null) {
            IMSLog.e(LOG_TAG, "updateAkaTokenInSharedPref: failed");
            return;
        }
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(this.mSimManager.getImsi(), akaToken);
        boolean isUpdated = prefEditor.commit();
        String str = LOG_TAG;
        IMSLog.i(str, "updateAkaTokenInSharedPref: isSuccess: " + isUpdated + " akaToken:" + akaToken);
    }

    /* access modifiers changed from: protected */
    public boolean isAuthenticationSuccessful(Response3gppAuthentication response3GppAuthentication) {
        return response3GppAuthentication != null && response3GppAuthentication.responseCode == 1000;
    }

    private boolean isResponseAkaChallenge(Response3gppAuthentication response3GppAuthentication) {
        return response3GppAuthentication != null && response3GppAuthentication.responseCode == 1003;
    }
}
