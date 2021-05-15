package com.sec.internal.ims.entitlement.nsds.ericssonnsds;

import android.content.Context;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.Request3gppAuthentication;
import com.sec.internal.constants.ims.entitilement.data.RequestGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.RequestManageConnectivity;
import com.sec.internal.constants.ims.entitilement.data.RequestManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.RequestManagePushToken;
import com.sec.internal.constants.ims.entitilement.data.RequestRegisteredMSISDN;
import com.sec.internal.constants.ims.entitilement.data.RequestServiceEntitlementStatus;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import com.sec.internal.ims.entitlement.util.DeviceNameHelper;
import com.sec.internal.ims.entitlement.util.HttpHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONException;

public class NSDSClient {
    private static final String DEVICE_ID = "device_id";
    private static final String HEADER_X_GENERIC_PROTOCOL_VERSION = "x-generic-protocol-version";
    private static final String LOG_TAG = NSDSClient.class.getSimpleName();
    private static final String X_GEN_PTC_VER = "1.0";
    private static Map<String, String> sNSDSHeaders = new ConcurrentHashMap();
    private static Looper sResponseLooper;
    private final Context mContext;
    private String mRequestUrl = null;
    private NSDSResponseHandler mResponseHandler;
    private ISimManager mSimManager;

    static {
        initNsdsCommonHeaders();
        initNsdsResponseLooper();
    }

    private static void initNsdsCommonHeaders() {
        sNSDSHeaders.put("Content-Type", "application/json");
        sNSDSHeaders.put("Content-Encoding", "gzip");
        sNSDSHeaders.put("Accept", "application/json");
        sNSDSHeaders.put("Accept-Encoding", "gzip");
        sNSDSHeaders.put(HEADER_X_GENERIC_PROTOCOL_VERSION, "1.0");
    }

    private static void initNsdsResponseLooper() {
        HandlerThread ht = new HandlerThread(LOG_TAG);
        ht.start();
        sResponseLooper = ht.getLooper();
    }

    public NSDSClient(Context context, ISimManager simManager) {
        this.mContext = context;
        this.mSimManager = simManager;
        this.mResponseHandler = new NSDSResponseHandler(sResponseLooper, this.mContext);
    }

    public String getDisplayName() {
        return DeviceNameHelper.getDeviceName(this.mContext);
    }

    public NSDSResponseHandler getResponseHandler() {
        return this.mResponseHandler;
    }

    public void executeRequestCollection(NSDSRequest[] arrRequest, Message callback, String version, String imsi, String deviceUid) {
        Message message = callback;
        JSONArray requestJsonArray = buildJSONArrayFromRequests(arrRequest);
        if (requestJsonArray != null) {
            Bundle messageIdMethodNameMap = buildMessageIdMethodBundle(arrRequest);
            HttpHelper httpHelper = new HttpHelper();
            Map<String, String> headers = buildNSDSRequestHeaders(version);
            NSDSNetworkInfoManager nsdsNetworkInfoManager = NSDSNetworkInfoManager.getInstance();
            httpHelper.executeNSDSRequest(getEntitlementServerUrl(imsi, deviceUid), headers, requestJsonArray, this.mResponseHandler.obtainParseResponseMessage(message, messageIdMethodNameMap), nsdsNetworkInfoManager.getSocketFactory(this.mSimManager.getSimSlotIndex()), nsdsNetworkInfoManager.getDns(this.mSimManager.getSimSlotIndex()));
            return;
        }
        String str = version;
        String str2 = imsi;
        String str3 = deviceUid;
        IMSLog.e(LOG_TAG, "executeRequestCollection: requestJsonArray is null");
        message.obj = null;
        callback.sendToTarget();
    }

    public void executeRequestCollection(NSDSRequest[] arrRequest, Message callback, boolean includeAuthorizationHeader, String version, String userAgent, String imei, String deviceId, String imsi) {
        Message message = callback;
        JSONArray requestJsonArray = buildJSONArrayFromRequests(arrRequest);
        if (requestJsonArray != null) {
            Bundle messageIdMethodNameMap = buildMessageIdMethodBundle(arrRequest);
            HttpHelper httpHelper = new HttpHelper();
            Map<String, String> headers = buildNSDSRequestHeaders(includeAuthorizationHeader, version, userAgent, imei, deviceId);
            NSDSNetworkInfoManager nsdsNetworkInfoManager = NSDSNetworkInfoManager.getInstance();
            Map<String, String> map = headers;
            JSONArray jSONArray = requestJsonArray;
            Bundle bundle = messageIdMethodNameMap;
            httpHelper.executeNSDSRequest(getEntitlementServerUrl(imsi, deviceId), map, jSONArray, this.mResponseHandler.obtainParseResponseMessage(message, messageIdMethodNameMap), nsdsNetworkInfoManager.getSocketFactory(this.mSimManager.getSimSlotIndex()), nsdsNetworkInfoManager.getDns(this.mSimManager.getSimSlotIndex()));
            return;
        }
        String str = deviceId;
        String str2 = imsi;
        IMSLog.e(LOG_TAG, "executeRequestCollection: requestJsonArray is null");
        message.obj = null;
        callback.sendToTarget();
    }

    private Bundle buildMessageIdMethodBundle(NSDSRequest[] arrRequest) {
        Bundle messageIdMethodNameMap = new Bundle();
        for (NSDSRequest request : arrRequest) {
            messageIdMethodNameMap.putString(String.valueOf(request.messageId), request.method);
        }
        return messageIdMethodNameMap;
    }

    private String getEntitlementServerUrl(String imsi, String deviceId) {
        IMnoNsdsStrategy mnoStrategy = MnoNsdsStrategyCreator.getInstance(this.mContext, this.mSimManager.getSimSlotIndex()).getMnoStrategy();
        if (mnoStrategy == null) {
            IMSLog.e(LOG_TAG, "initHttpRequest: mnoStrategy is null");
            return null;
        }
        String url = mnoStrategy.getEntitlementServerUrl(imsi, deviceId);
        if (TextUtils.isEmpty(this.mRequestUrl)) {
            return url;
        }
        String url2 = this.mRequestUrl;
        String str = LOG_TAG;
        IMSLog.s(str, "mURL:" + url2);
        return url2;
    }

    private Map<String, String> buildNSDSRequestHeaders(String version) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildNSDSRequestHeaders: version " + version);
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.putAll(sNSDSHeaders);
        headers.put(HEADER_X_GENERIC_PROTOCOL_VERSION, version);
        return headers;
    }

    private Map<String, String> buildNSDSRequestHeaders(boolean includeAuthorizationHeader, String version, String userAgent, String imei, String deviceUid) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildNSDSRequestHeaders: version " + version + " imei " + imei + "userAgent " + userAgent);
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.putAll(sNSDSHeaders);
        headers.put(HEADER_X_GENERIC_PROTOCOL_VERSION, version);
        if (imei != null) {
            headers.put("device_id", imei);
        }
        if (userAgent != null) {
            headers.put("User-Agent", userAgent);
        }
        if (includeAuthorizationHeader) {
            String tokenType = NSDSHelper.getAccessTokenType(this.mContext, deviceUid);
            String token = NSDSHelper.getAccessToken(this.mContext, deviceUid);
            String str2 = LOG_TAG;
            IMSLog.s(str2, "tokenType = " + tokenType + "\ntoken = " + token);
            if (!(tokenType == null || token == null)) {
                headers.put("Authorization", tokenType + " " + token);
            }
        }
        return headers;
    }

    private JSONArray buildJSONArrayFromRequests(NSDSRequest[] arrRequest) {
        JSONArray requestJsonArray = null;
        try {
            requestJsonArray = new JSONArray(new Gson().toJson(arrRequest));
            String str = LOG_TAG;
            IMSLog.s(str, "buildJSONArrayFromRequests:" + requestJsonArray.toString());
            return requestJsonArray;
        } catch (JSONException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "could not buld JSONArrayRequests:" + e.getMessage());
            return requestJsonArray;
        }
    }

    private int getDeviceType() {
        return 0;
    }

    public Request3gppAuthentication buildAuthenticationRequest(int messageId, boolean isAkaAuth, String akaChallengeResp, String akaToken, String deviceName, String imsiEap, String deviceId) {
        Request3gppAuthentication request = new Request3gppAuthentication();
        request.messageId = messageId;
        request.method = NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH;
        request.deviceId = deviceId;
        request.deviceType = getDeviceType();
        request.osType = 0;
        if (!TextUtils.isEmpty(deviceName)) {
            request.deviceName = deviceName;
        } else {
            request.deviceName = getDisplayName();
        }
        if (isAkaAuth) {
            request.imsiEap = imsiEap;
            String str = LOG_TAG;
            IMSLog.s(str, "buildAuthenticationRequest getimsi: " + request.imsiEap);
        }
        String str2 = LOG_TAG;
        IMSLog.s(str2, "buildAuthenticationRequest imsi: " + request.imsiEap);
        request.akaToken = akaToken;
        request.akaChallengeRsp = akaChallengeResp;
        return request;
    }

    public RequestManageConnectivity buildManageConnectivityRequest(int messageId, int operation, String vimsi, String remoteDeviceId, String deviceGroup, String csr, String deviceId) {
        RequestManageConnectivity request = new RequestManageConnectivity();
        request.messageId = messageId;
        request.method = NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY;
        request.deviceId = deviceId;
        request.operation = operation;
        request.vimsi = vimsi;
        request.remoteDeviceId = remoteDeviceId;
        request.deviceGroup = deviceGroup;
        request.csr = csr;
        return request;
    }

    public RequestManagePushToken buildManagePushTokenRequest(int messageId, String msisdn, String serviceName, String clientId, int operation, String pushToken, String deviceId) {
        RequestManagePushToken request = new RequestManagePushToken();
        request.messageId = messageId;
        request.method = "managePushToken";
        request.deviceId = deviceId;
        request.msisdn = msisdn;
        request.serviceName = serviceName;
        request.clientId = clientId;
        request.operation = operation;
        request.pushToken = pushToken;
        return request;
    }

    public RequestGetMSISDN buildGetMSISDNRequest(int messageId, String deviceId) {
        RequestGetMSISDN request = new RequestGetMSISDN();
        request.messageId = messageId;
        request.method = NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN;
        request.deviceId = deviceId;
        return request;
    }

    public RequestRegisteredMSISDN buildRegisteredMSISDNRequest(int messageId, String serviceName, int operation, Boolean isAvailable, String deviceId) {
        RequestRegisteredMSISDN request = new RequestRegisteredMSISDN();
        request.messageId = messageId;
        request.method = NSDSNamespaces.NSDSMethodNamespace.REGISTERED_MSISDN;
        request.deviceId = deviceId;
        request.operation = operation;
        request.serviceName = serviceName;
        request.isAvailable = isAvailable;
        return request;
    }

    public RequestManageLocationAndTC buildManageLocationAndTCRequest(int messageId, String serviceFingerPrint, String deviceId) {
        RequestManageLocationAndTC request = new RequestManageLocationAndTC();
        request.messageId = messageId;
        request.method = NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC;
        request.deviceId = deviceId;
        request.serviceFingerprint = serviceFingerPrint;
        return request;
    }

    public RequestServiceEntitlementStatus buildServiceEntitlementStatusRequest(int messageId, ArrayList<String> serviceEntitlementList, String deviceId) {
        RequestServiceEntitlementStatus request = new RequestServiceEntitlementStatus();
        request.messageId = messageId;
        request.method = NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS;
        request.deviceId = deviceId;
        request.serviceList = serviceEntitlementList;
        return request;
    }

    public void setRequestUrl(String requestUrl) {
        this.mRequestUrl = requestUrl;
    }
}
