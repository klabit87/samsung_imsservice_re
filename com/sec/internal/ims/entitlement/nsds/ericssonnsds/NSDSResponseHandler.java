package com.sec.internal.ims.entitlement.nsds.ericssonnsds;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageService;
import com.sec.internal.constants.ims.entitilement.data.ResponseRegisteredDevices;
import com.sec.internal.constants.ims.entitilement.data.ResponseRegisteredMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NSDSResponseHandler extends Handler {
    private static final String KEY_CALLBACK = "callback";
    private static final String KEY_MESSAGE_ID_METHOD_BUNDLE = "messageIdMethodBundle";
    private static final String LOG_TAG = NSDSResponseHandler.class.getSimpleName();
    private static final int METHOD_ID_GET_MSISDN = 3;
    private static final int METHOD_ID_MANAGE_CONNECTIVITY = 2;
    private static final int METHOD_ID_MANAGE_LOC_AND_TC = 5;
    private static final int METHOD_ID_MANAGE_PUSH_TOKEN = 4;
    private static final int METHOD_ID_MANAGE_SERVICE = 6;
    private static final int METHOD_ID_REGISTERED_DEVICES = 8;
    private static final int METHOD_ID_REGISTERED_MSISDN = 7;
    private static final int METHOD_ID_REQ_3GPP_AUTH = 1;
    private static final int METHOD_ID_SERVICE_ENTITLEMENT_STATUS = 9;
    private static final int PARSE_REPSONSE = 0;
    private static final Map<String, Integer> sMapNSDSMethods;
    private Context mContext;
    private SimpleEventLog mEventLog = null;

    static {
        HashMap hashMap = new HashMap();
        sMapNSDSMethods = hashMap;
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH, 1);
        sMapNSDSMethods.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, 2);
        sMapNSDSMethods.put(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN, 3);
        sMapNSDSMethods.put("managePushToken", 4);
        sMapNSDSMethods.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC, 5);
        sMapNSDSMethods.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_SERVICE, 6);
        sMapNSDSMethods.put(NSDSNamespaces.NSDSMethodNamespace.REGISTERED_MSISDN, 7);
        sMapNSDSMethods.put(NSDSNamespaces.NSDSMethodNamespace.REGISTERED_DEVICES, 8);
        sMapNSDSMethods.put(NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, 9);
    }

    public NSDSResponseHandler(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 20);
    }

    public Message obtainParseResponseMessage(Message callback, Bundle messageIdMethodNameBundle) {
        Message message = obtainMessage(0);
        Bundle data = new Bundle();
        data.putParcelable(KEY_CALLBACK, callback);
        data.putBundle(KEY_MESSAGE_ID_METHOD_BUNDLE, messageIdMethodNameBundle);
        message.setData(data);
        return message;
    }

    /* JADX WARNING: type inference failed for: r3v7, types: [android.os.Parcelable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r7) {
        /*
            r6 = this;
            android.os.Bundle r0 = r7.getData()
            r1 = 0
            r2 = 0
            if (r0 == 0) goto L_0x0017
            java.lang.String r3 = "callback"
            android.os.Parcelable r3 = r0.getParcelable(r3)
            r1 = r3
            android.os.Message r1 = (android.os.Message) r1
            java.lang.String r3 = "messageIdMethodBundle"
            android.os.Bundle r2 = r0.getBundle(r3)
        L_0x0017:
            if (r1 != 0) goto L_0x0021
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "handleMessage(): callback is null. return..."
            com.sec.internal.log.IMSLog.e(r3, r4)
            return
        L_0x0021:
            int r3 = r7.what
            if (r3 == 0) goto L_0x003e
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Response for Unknown EricssonNSDSRequest: "
            r4.append(r5)
            int r5 = r7.what
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r3, r4)
            goto L_0x0059
        L_0x003e:
            java.lang.Object r3 = r7.obj
            com.sec.internal.helper.httpclient.HttpResponseParams r3 = (com.sec.internal.helper.httpclient.HttpResponseParams) r3
            android.os.Bundle r3 = r6.parseResponse(r2, r3)
            if (r3 == 0) goto L_0x0055
            android.os.Bundle r4 = r1.getData()
            if (r4 == 0) goto L_0x0055
            android.os.Bundle r4 = r1.getData()
            r4.putAll(r3)
        L_0x0055:
            r1.sendToTarget()
        L_0x0059:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSResponseHandler.handleMessage(android.os.Message):void");
    }

    private Bundle parseHttpErrorResponse(HttpResponseParams result) {
        String str = LOG_TAG;
        IMSLog.i(str, "parseHttpErrorResponse: status code " + result.getStatusCode());
        Bundle httpErrorResponse = new Bundle();
        httpErrorResponse.putInt(NSDSNamespaces.NSDSDataMapKey.HTTP_RESP_CODE, result.getStatusCode());
        httpErrorResponse.putString(NSDSNamespaces.NSDSDataMapKey.HTTP_RESP_REASON, result.getStatusReason());
        return httpErrorResponse;
    }

    private Bundle parseResponse(Bundle messageIdMethodMap, HttpResponseParams result) {
        Bundle bundle;
        NSDSResponseHandler nSDSResponseHandler = this;
        HttpResponseParams httpResponseParams = result;
        Bundle bundle2 = null;
        if (httpResponseParams == null) {
            IMSLog.i(LOG_TAG, "parseJsonData: Check for http failure. most likely connection reset by peer");
            return null;
        } else if (result.getStatusCode() != 200) {
            return nSDSResponseHandler.parseHttpErrorResponse(httpResponseParams);
        } else {
            String jsonData = result.getDataString();
            if (jsonData == null) {
                IMSLog.i(LOG_TAG, "parseResponse: null json data");
                return null;
            }
            JsonParser parser = new JsonParser();
            Gson gson = new Gson();
            try {
                JsonArray array = parser.parse(jsonData).getAsJsonArray();
                if (array == null) {
                    Bundle bundle3 = messageIdMethodMap;
                    bundle = null;
                } else if (array.size() == 0) {
                    Bundle bundle4 = messageIdMethodMap;
                    bundle = null;
                } else {
                    Bundle responseBundle = new Bundle();
                    Iterator it = array.iterator();
                    while (it.hasNext()) {
                        JsonElement element = (JsonElement) it.next();
                        try {
                            NSDSResponse rsp = (NSDSResponse) gson.fromJson(element, NSDSResponse.class);
                            int messageId = rsp.messageId;
                            String methodName = messageIdMethodMap.getString(String.valueOf(messageId));
                            if (methodName == null) {
                                IMSLog.e(LOG_TAG, "Cannot find method for message id: " + messageId);
                                return bundle2;
                            }
                            NSDSResponse nsdsResponseForMethod = null;
                            SimpleEventLog simpleEventLog = nSDSResponseHandler.mEventLog;
                            StringBuilder sb = new StringBuilder();
                            Iterator it2 = it;
                            sb.append("parseResponse: method: ");
                            sb.append(methodName);
                            sb.append(" (");
                            sb.append(nSDSResponseHandler.toString(rsp.responseCode));
                            sb.append(")");
                            simpleEventLog.logAndAdd(sb.toString());
                            IMSLog.c(LogClass.ES_HTTP_RESPONSE, methodName + "," + rsp.responseCode);
                            switch (sMapNSDSMethods.get(methodName).intValue()) {
                                case 1:
                                    nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, Response3gppAuthentication.class);
                                    break;
                                case 2:
                                    nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, ResponseManageConnectivity.class);
                                    break;
                                case 3:
                                    nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, ResponseGetMSISDN.class);
                                    break;
                                case 4:
                                    nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, ResponseManagePushToken.class);
                                    break;
                                case 5:
                                    nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, ResponseManageLocationAndTC.class);
                                    break;
                                case 6:
                                    nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, ResponseManageService.class);
                                    break;
                                case 7:
                                    nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, ResponseRegisteredMSISDN.class);
                                    break;
                                case 8:
                                    nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, ResponseRegisteredDevices.class);
                                    break;
                                case 9:
                                    try {
                                        nsdsResponseForMethod = (NSDSResponse) gson.fromJson(element, ResponseServiceEntitlementStatus.class);
                                        break;
                                    } catch (JsonSyntaxException e) {
                                        IMSLog.e(LOG_TAG, "Syntax error while parsing individual response: " + methodName + e.getMessage());
                                        return null;
                                    }
                            }
                            if (nsdsResponseForMethod != null) {
                                nsdsResponseForMethod.method = methodName;
                                responseBundle.putParcelable(methodName, nsdsResponseForMethod);
                            }
                            bundle2 = null;
                            nSDSResponseHandler = this;
                            HttpResponseParams httpResponseParams2 = result;
                            it = it2;
                        } catch (JsonSyntaxException e2) {
                            Bundle bundle5 = messageIdMethodMap;
                            IMSLog.e(LOG_TAG, "Syntax error while parsing generic response" + e2.getMessage());
                            return null;
                        }
                    }
                    Bundle bundle6 = messageIdMethodMap;
                    return responseBundle;
                }
                IMSLog.e(LOG_TAG, "empty result");
                return bundle;
            } catch (JsonSyntaxException e3) {
                Bundle bundle7 = messageIdMethodMap;
                IMSLog.s(LOG_TAG, "cannot parse result" + e3.getMessage());
                return null;
            }
        }
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName());
        IMSLog.increaseIndent(LOG_TAG);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(LOG_TAG);
    }

    private String toString(int responseCode) {
        if (responseCode == 1000) {
            return "REQUEST_SUCCESSFUL";
        }
        if (responseCode == 1010) {
            return "ERROR_MAX_DEVICE_REACHED";
        }
        if (responseCode == 1080) {
            return "ERROR_INVALID_PUSH_TOKEN";
        }
        if (responseCode == 1500) {
            return "ERROR_REQUEST_ONGOING";
        }
        if (responseCode == 9999) {
            return "ERROR_UNSUPPORTED_OPERATION";
        }
        if (responseCode == 1028) {
            return "ERROR_DEVICE_LOCKED";
        }
        if (responseCode == 1029) {
            return "ERROR_INVALID_DEVICE_STATUS";
        }
        if (responseCode == 1048) {
            return "ERROR_SERVICE_NOT_ENTITLED";
        }
        if (responseCode == 1049) {
            return "ERROR_SERVICE_NOT_PERMITTED";
        }
        if (responseCode == 1053) {
            return "ERROR_INVALID_SERVICE_INSTANCEID";
        }
        if (responseCode == 1054) {
            return "ERROR_INVALID_DEVICE_GROUP";
        }
        if (responseCode == 1060) {
            return "ERROR_NO_MSISDN_FOUND";
        }
        if (responseCode == 1061) {
            return "ERROR_CREATION_FAILURE";
        }
        if (responseCode == 1111) {
            return "ERROR_SERVER_ERROR";
        }
        if (responseCode == 1112) {
            return "ERROR_3GPP_AUTH_ONGOING";
        }
        switch (responseCode) {
            case 1003:
                return "REQUEST_AKA_CHALLENGE";
            case 1004:
                return "ERROR_INVALID_REQUEST";
            case 1005:
                return "ERROR_INVALID_IP_AUTHENTICATION";
            case 1006:
                return "ERROR_AKA_AUTHENTICATION_FAILED";
            case 1007:
                return "FORBIDDEN_REQUEST";
            case 1008:
                return "INVALID_CLIENT_ID";
            default:
                switch (responseCode) {
                    case 1020:
                        return "ERROR_INVALID_DEVICE_ID";
                    case 1021:
                        return "ERROR_NO_EPDG";
                    case 1022:
                        return "ERROR_CERTIFICATE_GENERATION_FAILURE";
                    case SoftphoneNamespaces.SoftphoneEvents.EVENT_SEND_MESSAGE_DONE:
                        return "ERROR_REMOVAL_SERVICE_FAILURE";
                    case 1024:
                        return "ERROR_INVALID_OWNERID";
                    case 1025:
                        return "ERROR_INVALID_CSR";
                    default:
                        switch (responseCode) {
                            case NSDSNamespaces.NSDSResponseCode.ERROR_MAX_SERVICE_REACHED:
                                return "ERROR_MAX_SERVICE_REACHED";
                            case NSDSNamespaces.NSDSResponseCode.ERROR_INVALID_FINGERPRINT:
                                return "ERROR_INVALID_FINGERPRINT";
                            case 1042:
                                return "ERROR_INVALID_TARGET_DEVICEID";
                            case 1043:
                                return "ERROR_INVALID_TARGET_USER";
                            case NSDSNamespaces.NSDSResponseCode.ERROR_MAX_SERVICE_INSTANCE_REACHED:
                                return "ERROR_MAX_SERVICE_INSTANCE_REACHED";
                            case 1045:
                                return "ERROR_COPY_FORBIDDEN";
                            case NSDSNamespaces.NSDSResponseCode.ERROR_INVALID_SERVICE_NAME:
                                return "ERROR_INVALID_SERVICE_NAME";
                            default:
                                return "ERROR_UNKNOWN";
                        }
                }
        }
    }
}
