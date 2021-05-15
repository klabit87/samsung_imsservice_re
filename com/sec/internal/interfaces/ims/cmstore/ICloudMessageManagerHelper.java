package com.sec.internal.interfaces.ims.cmstore;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessCallFlow;
import com.sec.internal.omanetapi.common.data.NotificationFormat;
import java.util.List;
import java.util.Map;

public interface ICloudMessageManagerHelper {
    boolean bulkOpTreatSuccessIndividualResponse(int i);

    boolean bulkOpTreatSuccessRequestResponse(int i);

    void clearAll();

    void clearOMASubscriptionChannelDuration();

    void clearOMASubscriptionTime();

    int getAdaptedRetrySchedule(int i);

    String getContentType();

    IControllerCommonInterface getControllerOfLastFailedApi();

    String getDeviceId();

    Map<Class<? extends HttpRequestParams>, List<ErrorRule>> getFailedCallFlowTranslator();

    String getFaxApiVersion();

    String getFaxServerRoot();

    String getFaxServiceName();

    String getGcmTokenFromVsim();

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

    String getOMACallBackURL();

    String getOMAChannelResURL();

    String getOMAChannelURL();

    long getOMASubscriptionIndex();

    String getOMASubscriptionResUrl();

    String getProtocol();

    String getStoreName();

    Map<Class<? extends HttpRequestParams>, List<SuccessCallFlow>> getSuccessfullCallFlowTranslator();

    int getTotalRetryCounter();

    int getTypeUsingMessageContext(String str);

    String getUserCtn();

    boolean getUserTbs();

    String getUserTelCtn();

    String getValidTokenByLine(String str);

    boolean isBulkCreationEnabled();

    boolean isBulkDeleteEnabled();

    boolean isBulkUpdateEnabled();

    boolean isEnableATTHeader();

    boolean isEnableFolderIdInSearch();

    boolean isMidPrimaryIdForMmsCorrelationId();

    boolean isMultiLineSupported();

    boolean isPollingAllowed();

    boolean isPostMethodForBulkDelete();

    boolean isRetryEnabled();

    boolean isTokenRequestedFromProvision();

    boolean needToHandleSimSwap();

    void onOmaApiCredentialFailed(IControllerCommonInterface iControllerCommonInterface, INetAPIEventListener iNetAPIEventListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i);

    void onOmaSuccess(IHttpAPICommonInterface iHttpAPICommonInterface);

    void removeKey(String str);

    void saveNcHost(String str);

    void saveOMACallBackURL(String str);

    void saveOMAChannelCreateTime(long j);

    void saveOMAChannelLifeTime(long j);

    void saveOMAChannelResURL(String str);

    void saveOMAChannelURL(String str);

    void saveOMASubscriptionChannelDuration(int i);

    void saveOMASubscriptionIndex(long j);

    void saveOMASubscriptionResUrl(String str);

    void saveOMASubscriptionRestartToken(String str);

    void saveOMASubscriptionTime(long j);

    void saveTotalRetryCounter(int i);

    void setDeviceConfigUsed(Map<String, String> map);

    boolean shouldClearCursorUponInitSyncDone();

    boolean shouldCorrectShortCode();

    boolean shouldStopInitSyncUponLowMemory();

    boolean shouldStopSendingAPIwhenNetworklost();
}
