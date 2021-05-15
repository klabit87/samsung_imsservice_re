package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.NotificationListContainer;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.CallbackReference;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.AllSubscriptions;
import com.sec.internal.omanetapi.nms.data.NmsSubscription;
import com.sec.internal.omanetapi.nms.data.SearchCriteria;
import com.sec.internal.omanetapi.nms.data.SearchCriterion;
import java.io.IOException;

public class CloudMessageCreateSubscriptionChannel extends AllSubscriptions {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageCreateSubscriptionChannel.class.getSimpleName();
    private static final long serialVersionUID = 3483856569808284340L;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CloudMessageCreateSubscriptionChannel(IAPICallFlowListener callFlowListener, String notifyURL, String restartToken, IControllerCommonInterface callCommonInterface, boolean needPresetSearchRemove, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNcHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), iCloudMessageManagerHelper.getUserTelCtn());
        String str = notifyURL;
        String str2 = restartToken;
        boolean z = needPresetSearchRemove;
        final ICloudMessageManagerHelper iCloudMessageManagerHelper2 = iCloudMessageManagerHelper;
        this.mIControllerCommonInterface = callCommonInterface;
        NmsSubscription subscription = new NmsSubscription();
        CallbackReference callbackReference = new CallbackReference();
        callbackReference.notifyURL = str;
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            callbackReference.callbackData = "custom_data";
        } else {
            callbackReference.notificationFormat = iCloudMessageManagerHelper.getNotificaitonFormat();
        }
        subscription.callbackReference = callbackReference;
        subscription.duration = 86400;
        subscription.clientCorrelator = "";
        subscription.restartToken = str2;
        String str3 = TAG;
        Log.i(str3, "notifyURL " + str + " request restartToken " + str2 + " isGcmReplacePolling: " + ATTGlobalVariables.isGcmReplacePolling() + " needPresetSearchRemove" + z);
        if (ATTGlobalVariables.isGcmReplacePolling() && !z) {
            SearchCriteria searchCriteria = new SearchCriteria();
            SearchCriterion[] searchCriterion = {new SearchCriterion()};
            searchCriterion[0].type = "PresetSearch";
            searchCriterion[0].name = "UPOneDotO";
            searchCriterion[0].value = "";
            searchCriteria.criterion = searchCriterion;
            subscription.filter = searchCriteria;
        }
        String pat = iCloudMessageManagerHelper2.getValidTokenByLine(iCloudMessageManagerHelper.getUserTelCtn());
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            initSubscribeRequestHeaders(iCloudMessageManagerHelper.getContentType(), pat);
        } else {
            initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), pat);
        }
        initPostRequest(subscription, true);
        final IAPICallFlowListener iAPICallFlowListener = callFlowListener;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String access$000 = CloudMessageCreateSubscriptionChannel.TAG;
                Log.i(access$000, "The content of the response = " + result.getDataString());
                iAPICallFlowListener.onGoToEvent(OMASyncEventType.CREATE_SUBSCRIPTION_FINISHED.getId(), (Object) null);
                if (result.getStatusCode() == 201) {
                    OMAApiResponseParam response = CloudMessageCreateSubscriptionChannel.this.getResponse(result);
                    if (response == null) {
                        iAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                    NmsSubscription nmsSubscription = response.nmsSubscription;
                    if (nmsSubscription != null) {
                        String restartToken = nmsSubscription.restartToken;
                        int duration = nmsSubscription.duration.intValue();
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            CloudMessageCreateSubscriptionChannel.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId(), (((long) duration) * 1000) - ScheduleConstant.POLLING_TIME_OUT);
                        }
                        iCloudMessageManagerHelper2.saveOMASubscriptionIndex(nmsSubscription.index.longValue() - 1);
                        iCloudMessageManagerHelper2.saveOMASubscriptionRestartToken(restartToken);
                        iCloudMessageManagerHelper2.saveOMASubscriptionTime(System.currentTimeMillis());
                        iCloudMessageManagerHelper2.saveOMASubscriptionChannelDuration(duration);
                        if (nmsSubscription.resourceURL != null) {
                            iCloudMessageManagerHelper2.saveOMASubscriptionResUrl(nmsSubscription.resourceURL.toString());
                            NotificationListContainer.getInstance().clear();
                        }
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            iAPICallFlowListener.onSuccessfulCall(this, (String) null);
                        } else {
                            iAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LONG_POLLING_REQUEST.getId(), (Object) null);
                        }
                    } else {
                        iAPICallFlowListener.onFailedCall(this);
                    }
                } else {
                    if (result.getStatusCode() == 400) {
                        String messageId = null;
                        try {
                            OMAApiResponseParam response2 = (OMAApiResponseParam) new Gson().fromJson(result.getDataString(), OMAApiResponseParam.class);
                            if (response2 != null) {
                                messageId = response2.requestError.serviceException.messageId;
                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            messageId = CloudMessageCreateSubscriptionChannel.this.getResponseMessageId(result.getDataString());
                        }
                        if (messageId != null && messageId.equals("SVC0003")) {
                            String access$0002 = CloudMessageCreateSubscriptionChannel.TAG;
                            Log.d(access$0002, "messageId is " + messageId + ", remove PresetSearch Filter and resend subscription HTTP request");
                            iAPICallFlowListener.onFailedEvent(OMASyncEventType.REQUEST_SUBSCRIPTION_AFTER_PSF_REMOVED.getId(), (Object) null);
                            return;
                        }
                    }
                    if (CloudMessageCreateSubscriptionChannel.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                        iAPICallFlowListener.onFailedCall(CloudMessageCreateSubscriptionChannel.this);
                    }
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageCreateSubscriptionChannel.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
