package com.sec.internal.ims.cmstore;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessCallFlow;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.omanetapi.common.data.NotificationFormat;
import java.util.List;
import java.util.Map;

public class CloudMessageManagerHelper implements ICloudMessageManagerHelper {
    public String getDeviceId() {
        return CloudMessagePreferenceManager.getInstance().getDeviceId();
    }

    public Map<String, String> getMessageAttributeRegistration() {
        return CloudMessageStrategyManager.getStrategy().getMessageAttributeRegistration();
    }

    public int getTypeUsingMessageContext(String value) {
        return CloudMessageStrategyManager.getStrategy().getTypeUsingMessageContext(value);
    }

    public boolean shouldCorrectShortCode() {
        return CloudMessageStrategyManager.getStrategy().shouldCorrectShortCode();
    }

    public boolean isBulkUpdateEnabled() {
        return CloudMessageStrategyManager.getStrategy().isBulkUpdateEnabled();
    }

    public boolean isBulkDeleteEnabled() {
        return CloudMessageStrategyManager.getStrategy().isBulkDeleteEnabled();
    }

    public int getMaxBulkDeleteEntry() {
        return CloudMessageStrategyManager.getStrategy().getMaxBulkDeleteEntry();
    }

    public boolean isRetryEnabled() {
        return CloudMessageStrategyManager.getStrategy().isRetryEnabled();
    }

    public IControllerCommonInterface getControllerOfLastFailedApi() {
        return CloudMessageStrategyManager.getStrategy().getControllerOfLastFailedApi();
    }

    public boolean bulkOpTreatSuccessRequestResponse(int httpResponse) {
        return CloudMessageStrategyManager.getStrategy().bulkOpTreatSuccessRequestResponse(httpResponse);
    }

    public boolean bulkOpTreatSuccessIndividualResponse(int httpResponse) {
        return CloudMessageStrategyManager.getStrategy().bulkOpTreatSuccessIndividualResponse(httpResponse);
    }

    public boolean isEnableATTHeader() {
        return CloudMessageStrategyManager.getStrategy().isEnableATTHeader();
    }

    public String getNmsHost() {
        return CloudMessageStrategyManager.getStrategy().getNmsHost();
    }

    public boolean isEnableFolderIdInSearch() {
        return CloudMessageStrategyManager.getStrategy().isEnableFolderIdInSearch();
    }

    public boolean shouldClearCursorUponInitSyncDone() {
        return CloudMessageStrategyManager.getStrategy().shouldClearCursorUponInitSyncDone();
    }

    public boolean isBulkCreationEnabled() {
        return CloudMessageStrategyManager.getStrategy().isBulkCreationEnabled();
    }

    public Class<? extends IHttpAPICommonInterface> getLastFailedApi() {
        return CloudMessageStrategyManager.getStrategy().getLastFailedApi();
    }

    public void setDeviceConfigUsed(Map<String, String> config) {
        CloudMessageStrategyManager.getStrategy().setDeviceConfigUsed(config);
    }

    public Map<Class<? extends HttpRequestParams>, List<SuccessCallFlow>> getSuccessfullCallFlowTranslator() {
        return CloudMessageStrategyManager.getStrategy().getSuccessfullCallFlowTranslator();
    }

    public String getNativeLine() {
        return CloudMessageStrategyManager.getStrategy().getNativeLine();
    }

    public boolean needToHandleSimSwap() {
        return CloudMessageStrategyManager.getStrategy().needToHandleSimSwap();
    }

    public int getTotalRetryCounter() {
        return CloudMessagePreferenceManager.getInstance().getTotalRetryCounter();
    }

    public void saveTotalRetryCounter(int key) {
        CloudMessagePreferenceManager.getInstance().saveTotalRetryCounter(key);
    }

    public void removeKey(String key) {
        CloudMessagePreferenceManager.getInstance().removeKey(key);
    }

    public int getAdaptedRetrySchedule(int retryCounter) {
        return CloudMessageStrategyManager.getStrategy().getAdaptedRetrySchedule(retryCounter);
    }

    public Map<Class<? extends HttpRequestParams>, List<ErrorRule>> getFailedCallFlowTranslator() {
        return CloudMessageStrategyManager.getStrategy().getFailedCallFlowTranslator();
    }

    public boolean getIsInitSyncIndicatorRequired() {
        return CloudMessageStrategyManager.getStrategy().getIsInitSyncIndicatorRequired();
    }

    public void clearAll() {
        CloudMessagePreferenceManager.getInstance().clearAll();
    }

    public String getProtocol() {
        return CloudMessageStrategyManager.getStrategy().getProtocol();
    }

    public String getValidTokenByLine(String linenum) {
        return CloudMessageStrategyManager.getStrategy().getValidTokenByLine(linenum);
    }

    public String getContentType() {
        return CloudMessageStrategyManager.getStrategy().getContentType();
    }

    public String getNcHost() {
        return CloudMessageStrategyManager.getStrategy().getNcHost();
    }

    public String getOMAApiVersion() {
        return CloudMessageStrategyManager.getStrategy().getOMAApiVersion();
    }

    public String getUserTelCtn() {
        return CloudMessagePreferenceManager.getInstance().getUserTelCtn();
    }

    public long getOMASubscriptionIndex() {
        return CloudMessagePreferenceManager.getInstance().getOMASubscriptionIndex();
    }

    public void saveOMASubscriptionRestartToken(String token) {
        CloudMessagePreferenceManager.getInstance().saveOMASubscriptionRestartToken(token);
    }

    public void saveOMASubscriptionIndex(long index) {
        CloudMessagePreferenceManager.getInstance().saveOMASubscriptionIndex(index);
    }

    public String getOMAChannelURL() {
        return CloudMessagePreferenceManager.getInstance().getOMAChannelURL();
    }

    public String getGcmTokenFromVsim() {
        return CloudMessagePreferenceManager.getInstance().getGcmTokenFromVsim();
    }

    public void saveOMAChannelURL(String Url) {
        CloudMessagePreferenceManager.getInstance().saveOMAChannelURL(Url);
    }

    public String getOMACallBackURL() {
        return CloudMessagePreferenceManager.getInstance().getOMACallBackURL();
    }

    public String getOMASubscriptionResUrl() {
        return CloudMessagePreferenceManager.getInstance().getOMASubscriptionResUrl();
    }

    public void saveOMAChannelResURL(String Url) {
        CloudMessagePreferenceManager.getInstance().saveOMAChannelResURL(Url);
    }

    public void saveOMACallBackURL(String Url) {
        CloudMessagePreferenceManager.getInstance().saveOMACallBackURL(Url);
    }

    public void saveOMAChannelCreateTime(long time) {
        CloudMessagePreferenceManager.getInstance().saveOMAChannelCreateTime(time);
    }

    public void saveOMAChannelLifeTime(long time) {
        CloudMessagePreferenceManager.getInstance().saveOMAChannelLifeTime(time);
    }

    public void clearOMASubscriptionChannelDuration() {
        CloudMessagePreferenceManager.getInstance().clearOMASubscriptionChannelDuration();
    }

    public void clearOMASubscriptionTime() {
        CloudMessagePreferenceManager.getInstance().clearOMASubscriptionTime();
    }

    public void saveOMASubscriptionTime(long time) {
        CloudMessagePreferenceManager.getInstance().saveOMASubscriptionTime(time);
    }

    public void saveOMASubscriptionChannelDuration(int time) {
        CloudMessagePreferenceManager.getInstance().saveOMASubscriptionChannelDuration(time);
    }

    public String getOMAChannelResURL() {
        return CloudMessagePreferenceManager.getInstance().getOMAChannelResURL();
    }

    public String getStoreName() {
        return CloudMessageStrategyManager.getStrategy().getStoreName();
    }

    public int getMaxSearchEntry() {
        return CloudMessageStrategyManager.getStrategy().getMaxSearchEntry();
    }

    public String getFaxServerRoot() {
        return CloudMessageStrategyManager.getStrategy().getFaxServerRoot();
    }

    public String getFaxApiVersion() {
        return CloudMessageStrategyManager.getStrategy().getFaxApiVersion();
    }

    public String getFaxServiceName() {
        return CloudMessageStrategyManager.getStrategy().getFaxServiceName();
    }

    public void saveNcHost(String ncHost) {
        CloudMessagePreferenceManager.getInstance().saveNcHost(ncHost);
    }

    public boolean isMidPrimaryIdForMmsCorrelationId() {
        return CloudMessageStrategyManager.getStrategy().isMidPrimaryIdForMmsCorrelationId();
    }

    public boolean isPollingAllowed() {
        return CloudMessageStrategyManager.getStrategy().isPollingAllowed();
    }

    public boolean isMultiLineSupported() {
        return CloudMessageStrategyManager.getStrategy().isMultiLineSupported();
    }

    public boolean isTokenRequestedFromProvision() {
        return CloudMessageStrategyManager.getStrategy().isTokenRequestedFromProvision();
    }

    public boolean shouldStopSendingAPIwhenNetworklost() {
        return CloudMessageStrategyManager.getStrategy().shouldStopSendingAPIwhenNetworklost();
    }

    public boolean shouldStopInitSyncUponLowMemory() {
        return CloudMessageStrategyManager.getStrategy().shouldStopInitSyncUponLowMemory();
    }

    public void onOmaApiCredentialFailed(IControllerCommonInterface controller, INetAPIEventListener netAPIEventListener, IHttpAPICommonInterface api, int delaySecs) {
        CloudMessageStrategyManager.getStrategy().onOmaApiCredentialFailed(controller, netAPIEventListener, api, delaySecs);
    }

    public void onOmaSuccess(IHttpAPICommonInterface api) {
        CloudMessageStrategyManager.getStrategy().onOmaSuccess(api);
    }

    public boolean isPostMethodForBulkDelete() {
        return CloudMessageStrategyManager.getStrategy().isPostMethodForBulkDelete();
    }

    public NotificationFormat getNotificaitonFormat() {
        return CloudMessageStrategyManager.getStrategy().getNotificaitonFormat();
    }

    public String getUserCtn() {
        return CloudMessagePreferenceManager.getInstance().getUserCtn();
    }

    public boolean getUserTbs() {
        return CloudMessagePreferenceManager.getInstance().getUserTbs();
    }

    public void saveOMASubscriptionResUrl(String url) {
        CloudMessagePreferenceManager.getInstance().saveOMASubscriptionResUrl(url);
    }

    public int getMaxRetryCounter() {
        return CloudMessageStrategyManager.getStrategy().getMaxRetryCounter();
    }
}
