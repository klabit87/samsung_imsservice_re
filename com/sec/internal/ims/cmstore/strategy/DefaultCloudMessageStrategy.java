package com.sec.internal.ims.cmstore.strategy;

import android.os.Handler;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqZCode;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorMsg;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorType;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.OmaErrorKey;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessCallFlow;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler;
import com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkUpdate;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteObjectFlag;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualPayLoad;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePutObjectFlag;
import com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.omanetapi.common.data.NotificationFormat;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCloudMessageStrategy implements ICloudMessageStrategy, IMessageAttributeInterface {
    private static final String LOG_TAG = DefaultCloudMessageStrategy.class.getSimpleName();
    public static final int MAX_RETRY_COUNTER = 4;
    protected String mContentType;
    protected Map<Class<? extends HttpRequestParams>, List<ErrorRule>> mFailedCallFlowTranslator;
    protected int mMaxBulkDelete = 100;
    protected int mMaxRetryCounter = 4;
    protected int mMaxSearch = 10;
    protected Map<String, String> mMessageAttributeRegistration;
    protected NotificationFormat mNotificationFormat;
    protected Map<OmaErrorKey, Integer> mOmaCallFlowTranslator;
    protected String mProtocol;
    protected Map<Integer, Integer> mStandardRetrySchedule;
    protected CmStrategyType mStrategyType = CmStrategyType.DEFAULT;
    protected Map<Class<? extends HttpRequestParams>, List<SuccessCallFlow>> mSuccessfullCallFlowTranslator;
    protected Map<ErrorType, ErrorMsg> sErrorMsgsTranslator;

    public static class NmsNotificationType {
        private int contractType;
        private String dataType;

        public NmsNotificationType(String data, int contract) {
            setDataType(data);
            setContractType(contract);
        }

        public String getDataType() {
            return this.dataType;
        }

        public void setDataType(String dataType2) {
            this.dataType = dataType2;
        }

        public int getContractType() {
            return this.contractType;
        }

        public void setContractType(int contractType2) {
            this.contractType = contractType2;
        }
    }

    DefaultCloudMessageStrategy() {
        Log.d(LOG_TAG, "DefaultCloudMessageStrategy");
    }

    public int getAdaptedRetrySchedule(int retryCounter) {
        return 0;
    }

    public String getNmsHost() {
        return "";
    }

    public String getNcHost() {
        return "";
    }

    public String getFaxServerRoot() {
        return "";
    }

    public String getFaxApiVersion() {
        return "";
    }

    public String getFaxServiceName() {
        return "";
    }

    public String getOMAApiVersion() {
        return "";
    }

    public String getStoreName() {
        return "";
    }

    public String getNativeLine() {
        return CloudMessagePreferenceManager.getInstance().getUserTelCtn();
    }

    public int getTypeUsingMessageContext(String value) {
        return 0;
    }

    public void setDeviceConfigUsed(Map<String, String> map) {
    }

    public boolean shouldCareAfterPreProcess(IAPICallFlowListener callFlowListener, IHttpAPICommonInterface api, HttpResponseParams response, Object paramOMAresponseforBufDBObj, BufferDBChangeParam dbParam, int overwriteEvent) {
        int code = response.getStatusCode();
        String apiType = api instanceof BaseNMSRequest ? "NMS" : "NC";
        String str = LOG_TAG;
        Log.i(str, apiType + "[" + api.getClass().getSimpleName() + "], res code[" + code + "]");
        if (isOmaErrorRuleMatch(code, api, callFlowListener, paramOMAresponseforBufDBObj, overwriteEvent)) {
            String str2 = LOG_TAG;
            Log.i(str2, apiType + "[" + api.getClass().getSimpleName() + "], isOmaErrorRuleMatch");
            return false;
        } else if (isCarrierStrategyBreakCommonRule(api, code)) {
            String str3 = LOG_TAG;
            Log.i(str3, apiType + "[" + api.getClass().getSimpleName() + "], [" + code + "] catch call");
            return true;
        } else if (!shouldCareAfterProcessOMACommonCase(callFlowListener, api, response, dbParam)) {
            String str4 = LOG_TAG;
            Log.i(str4, apiType + "[" + api.getClass().getSimpleName() + "], match common cases");
            return false;
        } else {
            String str5 = LOG_TAG;
            Log.i(str5, apiType + "[" + api.getClass().getSimpleName() + "], [" + code + "] catch call, return");
            return true;
        }
    }

    public boolean shouldEnableNetAPIWorking(boolean mIsNetworkValid, boolean mIsDefaultMsgAppNative, boolean mIsUserDeleteAccount, boolean mIsProvisionSuccess) {
        return false;
    }

    public void onOmaApiCredentialFailed(IControllerCommonInterface controller, INetAPIEventListener netAPIEventListener, IHttpAPICommonInterface api, int delaySecs) {
    }

    public void onOmaSuccess(IHttpAPICommonInterface api) {
    }

    /* access modifiers changed from: protected */
    public final void onOmaFlowInitStart() {
        this.mOmaCallFlowTranslator = new HashMap();
    }

    /* access modifiers changed from: protected */
    public final void onOmaFlowInitComplete() {
        this.mOmaCallFlowTranslator = Collections.unmodifiableMap(this.mOmaCallFlowTranslator);
    }

    /* access modifiers changed from: protected */
    public final void initOmaSuccessCommonFlow() {
        Log.i(LOG_TAG, "init OMA success common flow");
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(201, CloudMessageCreateAllObjects.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetIndividualObject.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetIndividualObject.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.DOWNLOAD_RETRIVED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetAllPayloads.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetAllPayloads.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.DOWNLOAD_RETRIVED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetIndividualPayLoad.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetIndividualPayLoad.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.DOWNLOAD_RETRIVED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(201, CloudMessagePutObjectFlag.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageDeleteObjectFlag.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageDeleteIndividualObject.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageObjectsOpSearch.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageObjectsOpSearch.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.BULK_UPDATE_OR_DELETE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.BULK_UPDATE_OR_DELETE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(403, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.FALLBACK_ONE_UPDATE_OR_DELETE.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageBulkUpdate.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.BULK_UPDATE_OR_DELETE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageBulkUpdate.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.BULK_UPDATE_OR_DELETE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(403, CloudMessageBulkUpdate.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.FALLBACK_ONE_UPDATE_OR_DELETE.getId()));
    }

    /* access modifiers changed from: protected */
    public void initOmaFailureCommonFlow() {
        Log.i(LOG_TAG, "init OMA failure common flow");
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetIndividualObject.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.DOWNLOAD_RETRIVED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetIndividualObject.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetAllPayloads.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetAllPayloads.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.DOWNLOAD_RETRIVED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetIndividualPayLoad.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetIndividualPayLoad.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.DOWNLOAD_RETRIVED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessagePutObjectFlag.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageDeleteObjectFlag.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageDeleteIndividualObject.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId()));
    }

    /* access modifiers changed from: protected */
    public boolean isFailedForOmaRetryAfter(IAPICallFlowListener callFlowListener, IHttpAPICommonInterface api, HttpResponseParams response, BufferDBChangeParam param) {
        List<String> retryAfterHeader = response.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER);
        if (retryAfterHeader == null || retryAfterHeader.size() <= 0) {
            return true;
        }
        String retryAfter = retryAfterHeader.get(0);
        String str = LOG_TAG;
        Log.i(str, "retryAfterHeader: " + retryAfterHeader.toString() + "API[" + api.getClass().getSimpleName() + "], retry after " + retryAfter + " seconds");
        try {
            int retryAfterValue = Integer.parseInt(retryAfter);
            if (retryAfterValue > 0) {
                callFlowListener.onOverRequest(api, CommonErrorName.RETRY_HEADER, retryAfterValue);
            } else if (param != null) {
                callFlowListener.onFailedCall(api, param);
            }
        } catch (NumberFormatException ex) {
            Log.e(LOG_TAG, ex.getMessage());
            callFlowListener.onFailedCall(api, param);
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: java.lang.String} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isFailedForLocationRedirect(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r7, com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r8, com.sec.internal.helper.httpclient.HttpResponseParams r9) {
        /*
            r6 = this;
            java.util.Map r0 = r9.getHeaders()
            java.lang.String r1 = "Location"
            java.lang.Object r0 = r0.get(r1)
            java.util.List r0 = (java.util.List) r0
            r1 = 0
            r2 = 0
            if (r0 == 0) goto L_0x001d
            int r3 = r0.size()
            if (r3 <= 0) goto L_0x001d
            java.lang.Object r3 = r0.get(r2)
            r1 = r3
            java.lang.String r1 = (java.lang.String) r1
        L_0x001d:
            boolean r3 = android.text.TextUtils.isEmpty(r1)
            if (r3 != 0) goto L_0x007d
            java.net.URL r3 = new java.net.URL     // Catch:{ MalformedURLException -> 0x006f }
            r3.<init>(r1)     // Catch:{ MalformedURLException -> 0x006f }
            boolean r4 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.isGcmReplacePolling()     // Catch:{ MalformedURLException -> 0x006f }
            if (r4 == 0) goto L_0x0041
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r4 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()     // Catch:{ MalformedURLException -> 0x006f }
            java.lang.String r5 = r3.getHost()     // Catch:{ MalformedURLException -> 0x006f }
            r4.saveNmsHost(r5)     // Catch:{ MalformedURLException -> 0x006f }
            java.lang.String r4 = r6.getNmsHost()     // Catch:{ MalformedURLException -> 0x006f }
            r8.updateServerRoot(r4)     // Catch:{ MalformedURLException -> 0x006f }
            goto L_0x006e
        L_0x0041:
            boolean r4 = r8 instanceof com.sec.internal.omanetapi.nms.BaseNMSRequest     // Catch:{ MalformedURLException -> 0x006f }
            if (r4 == 0) goto L_0x0058
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r4 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()     // Catch:{ MalformedURLException -> 0x006f }
            java.lang.String r5 = r3.getHost()     // Catch:{ MalformedURLException -> 0x006f }
            r4.saveNmsHost(r5)     // Catch:{ MalformedURLException -> 0x006f }
            java.lang.String r4 = r6.getNmsHost()     // Catch:{ MalformedURLException -> 0x006f }
            r8.updateServerRoot(r4)     // Catch:{ MalformedURLException -> 0x006f }
            goto L_0x006e
        L_0x0058:
            boolean r4 = r8 instanceof com.sec.internal.omanetapi.nc.BaseNCRequest     // Catch:{ MalformedURLException -> 0x006f }
            if (r4 == 0) goto L_0x006e
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r4 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()     // Catch:{ MalformedURLException -> 0x006f }
            java.lang.String r5 = r3.getHost()     // Catch:{ MalformedURLException -> 0x006f }
            r4.saveNcHost(r5)     // Catch:{ MalformedURLException -> 0x006f }
            java.lang.String r4 = r6.getNcHost()     // Catch:{ MalformedURLException -> 0x006f }
            r8.updateServerRoot(r4)     // Catch:{ MalformedURLException -> 0x006f }
        L_0x006e:
            goto L_0x0073
        L_0x006f:
            r3 = move-exception
            r3.printStackTrace()
        L_0x0073:
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MSTORE_REDIRECT
            int r3 = r3.getId()
            r7.onFailedEvent(r3, r8)
            return r2
        L_0x007d:
            r2 = 1
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.strategy.DefaultCloudMessageStrategy.isFailedForLocationRedirect(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener, com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.helper.httpclient.HttpResponseParams):boolean");
    }

    /* access modifiers changed from: protected */
    public boolean shouldCareAfterProcessOMACommonCase(IAPICallFlowListener callFlowListener, IHttpAPICommonInterface api, HttpResponseParams response, BufferDBChangeParam param) {
        int code = response.getStatusCode();
        if (code >= 500 && code != 503) {
            callFlowListener.onFailedCall(api, param);
            return false;
        } else if (isCarrierStrategyDiffFromCommonRuleByCode(callFlowListener, api, code)) {
            return false;
        } else {
            if (code == 302) {
                String str = LOG_TAG;
                Log.i(str, "API[" + api.getClass().getSimpleName() + "], 302");
                if (isFailedForLocationRedirect(callFlowListener, api, response)) {
                    callFlowListener.onFailedCall(api, param);
                }
                return false;
            } else if (code == 401) {
                callFlowListener.onFailedCall(api, param);
                return false;
            } else if (code == 408) {
                callFlowListener.onFailedCall(api, param);
                return false;
            } else if (code != 429 && code != 503) {
                return true;
            } else {
                if (isFailedForOmaRetryAfter(callFlowListener, api, response, param)) {
                    callFlowListener.onFailedCall(api, param);
                }
                return false;
            }
        }
    }

    private String getHandlerClassName(IAPICallFlowListener callFlowListener) {
        String handlerClassName = callFlowListener.getClass().getSimpleName();
        if (callFlowListener instanceof BaseDataChangeHandler) {
            return BaseDataChangeHandler.class.getSimpleName();
        }
        if (callFlowListener instanceof BaseDeviceDataUpdateHandler) {
            return BaseDeviceDataUpdateHandler.class.getSimpleName();
        }
        if (callFlowListener instanceof BaseSyncHandler) {
            return BaseSyncHandler.class.getSimpleName();
        }
        if ((callFlowListener instanceof OMAPollingScheduler) || (callFlowListener instanceof ChannelScheduler)) {
            return OMAPollingScheduler.class.getSimpleName();
        }
        return handlerClassName;
    }

    /* access modifiers changed from: protected */
    public boolean isOmaErrorRuleMatch(int code, IHttpAPICommonInterface api, IAPICallFlowListener callFlowListener, Object param, int overwriteEvent) {
        int event = Integer.MIN_VALUE;
        OmaErrorKey mainKey = new OmaErrorKey(code, api.getClass().getSimpleName(), getHandlerClassName(callFlowListener));
        OmaErrorKey subKey = new OmaErrorKey(code, api.getClass().getSimpleName(), Handler.class.getSimpleName());
        if (this.mOmaCallFlowTranslator.containsKey(mainKey)) {
            event = this.mOmaCallFlowTranslator.get(mainKey).intValue();
        } else if (this.mOmaCallFlowTranslator.containsKey(subKey)) {
            event = this.mOmaCallFlowTranslator.get(subKey).intValue();
        }
        if (overwriteEvent != Integer.MIN_VALUE) {
            event = overwriteEvent;
        }
        if (event == Integer.MIN_VALUE) {
            return false;
        }
        OMASyncEventType matchedRule = OMASyncEventType.valueOf(event);
        String matchedRuleName = matchedRule == null ? null : matchedRule.name();
        String str = LOG_TAG;
        Log.i(str, "API[" + api.getClass().getSimpleName() + "], match rule[" + matchedRuleName + "]");
        if (code < 200 || code >= 300) {
            callFlowListener.onFailedEvent(event, param);
            return true;
        }
        callFlowListener.onSuccessfulEvent(api, event, param);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierStrategyBreakCommonRule(IHttpAPICommonInterface api, int statusCode) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierStrategyDiffFromCommonRuleByCode(IAPICallFlowListener callFlowListener, IHttpAPICommonInterface api, int statusCode) {
        return false;
    }

    public IControllerCommonInterface getControllerOfLastFailedApi() {
        return null;
    }

    public Class<? extends IHttpAPICommonInterface> getLastFailedApi() {
        return null;
    }

    public boolean shouldEnableNetAPIPutFlag(String appType) {
        return true;
    }

    public void clearOmaRetryData() {
    }

    public boolean isValidOMARequestUrl() {
        return true;
    }

    public NmsNotificationType makeParamNotificationType(String pnsType, String pnsSubtype) {
        return null;
    }

    public void updateHTTPHeader() {
    }

    protected enum CmStrategyType {
        DEFAULT,
        ATT,
        TMOUS;

        /* access modifiers changed from: protected */
        public boolean isOneOf(CmStrategyType... types) {
            for (CmStrategyType type : types) {
                if (this == type) {
                    return true;
                }
            }
            return false;
        }
    }

    public final Map<Class<? extends HttpRequestParams>, List<SuccessCallFlow>> getSuccessfullCallFlowTranslator() {
        return this.mSuccessfullCallFlowTranslator;
    }

    public final Map<Class<? extends HttpRequestParams>, List<ErrorRule>> getFailedCallFlowTranslator() {
        return this.mFailedCallFlowTranslator;
    }

    public final int getMaxRetryCounter() {
        return this.mMaxRetryCounter;
    }

    public final Map<String, String> getMessageAttributeRegistration() {
        return this.mMessageAttributeRegistration;
    }

    public final String getProtocol() {
        return this.mProtocol;
    }

    public final void setProtocol(String httpPrex) {
    }

    public String getValidTokenByLine(String linenum) {
        return null;
    }

    public final NotificationFormat getNotificaitonFormat() {
        return this.mNotificationFormat;
    }

    public final String getContentType() {
        return this.mContentType;
    }

    public final boolean isMidPrimaryIdForMmsCorrelationId() {
        return !this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isProvisionRequired() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isMultiLineSupported() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isPollingAllowed() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isThumbNailEnabledForRcsFT() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isNmsEventHasMessageDetail() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean alwaysInsertMsgWhenNonExist() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isCaptivePortalCheckSupported() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isGoForwardSyncSupported() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final String getSmsHashTagOrCorrelationTag(String phoneNum, int type, String body) {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT, CmStrategyType.TMOUS)) {
            return AmbsUtils.generateSmsHashCode(phoneNum, type, body);
        }
        return null;
    }

    public final boolean isEnableFolderIdInSearch() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean getIsInitSyncIndicatorRequired() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isTokenRequestedFromProvision() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isRetryEnabled() {
        return !this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean requiresInterworkingCrossSearch() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT, CmStrategyType.TMOUS);
    }

    public final boolean isBulkUpdateEnabled() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }

    public final boolean isBulkDeleteEnabled() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return true;
    }

    public final boolean isBulkCreationEnabled() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }

    public final boolean isPostMethodForBulkDelete() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final int getMaxBulkDeleteEntry() {
        return this.mMaxBulkDelete;
    }

    public final int getMaxSearchEntry() {
        return this.mMaxSearch;
    }

    public final boolean isDeviceConfigUsed() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isUIButtonUsed() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean requiresMsgUploadInInitSync() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isEnableATTHeader() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isEnableTMOHeader() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isNotifyAppOnUpdateCloudFail() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isAirplaneModeChangeHandled() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isSmsInitialSearchUsingResUrl() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean shouldStopSendingAPIwhenNetworklost() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean shouldSkipMessage(ParamOMAObject omaParam) {
        if (!this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return false;
        }
        String content = omaParam.TEXT_CONTENT;
        String from = omaParam.FROM;
        if (content == null || from == null) {
            return false;
        }
        return ReqZCode.isSmsZCode(content, from.replaceAll("[^0-9]+", ""));
    }

    public final boolean shouldCorrectShortCode() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isAppTriggerMessageSearch() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean needToHandleSimSwap() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean bulkOpTreatSuccessIndividualResponse(int httpResponse) {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS) && (httpResponse == 403 || httpResponse == 404);
    }

    public final boolean bulkOpTreatSuccessRequestResponse(int httpResponse) {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS) && httpResponse == 404;
    }

    public final boolean shouldSkipCmasSMS(String address) {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return address == null || address.contains("#CMAS#");
        }
        return false;
    }

    public final boolean shouldPersistImsRegNum() {
        return !this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isStoreImdnEnabled() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }

    public final boolean isGcmReplacePolling() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean shouldClearCursorUponInitSyncDone() {
        return !this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isNeedCheckBlockedNumberBeforeCopyRcsDb() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean querySessionByConversation() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean shouldStopInitSyncUponLowMemory() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }

    public final boolean shouldCareGroupChatAttribute() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }

    public final boolean isSupportAtt72HoursRule() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }
}
