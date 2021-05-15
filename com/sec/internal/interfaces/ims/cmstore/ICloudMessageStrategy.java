package com.sec.internal.interfaces.ims.cmstore;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessCallFlow;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.strategy.DefaultCloudMessageStrategy;
import com.sec.internal.omanetapi.common.data.NotificationFormat;
import java.util.List;
import java.util.Map;

public interface ICloudMessageStrategy {
    boolean alwaysInsertMsgWhenNonExist();

    boolean bulkOpTreatSuccessIndividualResponse(int i);

    boolean bulkOpTreatSuccessRequestResponse(int i);

    void clearOmaRetryData();

    int getAdaptedRetrySchedule(int i);

    String getContentType();

    IControllerCommonInterface getControllerOfLastFailedApi();

    Map<Class<? extends HttpRequestParams>, List<ErrorRule>> getFailedCallFlowTranslator();

    String getFaxApiVersion();

    String getFaxServerRoot();

    String getFaxServiceName();

    boolean getIsInitSyncIndicatorRequired();

    Class<? extends IHttpAPICommonInterface> getLastFailedApi();

    int getMaxBulkDeleteEntry();

    int getMaxRetryCounter();

    int getMaxSearchEntry();

    Map<String, String> getMessageAttributeRegistration();

    String getNativeLine();

    String getNcHost();

    String getNmsHost();

    NotificationFormat getNotificaitonFormat();

    String getOMAApiVersion();

    String getProtocol();

    String getSmsHashTagOrCorrelationTag(String str, int i, String str2);

    String getStoreName();

    Map<Class<? extends HttpRequestParams>, List<SuccessCallFlow>> getSuccessfullCallFlowTranslator();

    int getTypeUsingMessageContext(String str);

    String getValidTokenByLine(String str);

    boolean isAirplaneModeChangeHandled();

    boolean isAppTriggerMessageSearch();

    boolean isBulkCreationEnabled();

    boolean isBulkDeleteEnabled();

    boolean isBulkUpdateEnabled();

    boolean isCaptivePortalCheckSupported();

    boolean isDeviceConfigUsed();

    boolean isEnableATTHeader();

    boolean isEnableFolderIdInSearch();

    boolean isEnableTMOHeader();

    boolean isGcmReplacePolling();

    boolean isGoForwardSyncSupported();

    boolean isMidPrimaryIdForMmsCorrelationId();

    boolean isMultiLineSupported();

    boolean isNeedCheckBlockedNumberBeforeCopyRcsDb();

    boolean isNmsEventHasMessageDetail();

    boolean isNotifyAppOnUpdateCloudFail();

    boolean isPollingAllowed();

    boolean isPostMethodForBulkDelete();

    boolean isProvisionRequired();

    boolean isRetryEnabled();

    boolean isSmsInitialSearchUsingResUrl();

    boolean isStoreImdnEnabled();

    boolean isSupportAtt72HoursRule();

    boolean isThumbNailEnabledForRcsFT();

    boolean isTokenRequestedFromProvision();

    boolean isUIButtonUsed();

    boolean isValidOMARequestUrl();

    DefaultCloudMessageStrategy.NmsNotificationType makeParamNotificationType(String str, String str2);

    boolean needToHandleSimSwap();

    void onOmaApiCredentialFailed(IControllerCommonInterface iControllerCommonInterface, INetAPIEventListener iNetAPIEventListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i);

    void onOmaSuccess(IHttpAPICommonInterface iHttpAPICommonInterface);

    boolean querySessionByConversation();

    boolean requiresInterworkingCrossSearch();

    boolean requiresMsgUploadInInitSync();

    void setDeviceConfigUsed(Map<String, String> map);

    void setProtocol(String str);

    boolean shouldCareAfterPreProcess(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, HttpResponseParams httpResponseParams, Object obj, BufferDBChangeParam bufferDBChangeParam, int i);

    boolean shouldCareGroupChatAttribute();

    boolean shouldClearCursorUponInitSyncDone();

    boolean shouldCorrectShortCode();

    boolean shouldEnableNetAPIPutFlag(String str);

    boolean shouldEnableNetAPIWorking(boolean z, boolean z2, boolean z3, boolean z4);

    boolean shouldPersistImsRegNum();

    boolean shouldSkipCmasSMS(String str);

    boolean shouldSkipMessage(ParamOMAObject paramOMAObject);

    boolean shouldStopInitSyncUponLowMemory();

    boolean shouldStopSendingAPIwhenNetworklost();

    void updateHTTPHeader();
}
