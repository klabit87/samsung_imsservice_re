package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.IndividualSubscription;
import com.sec.internal.omanetapi.nms.data.NmsSubscription;
import com.sec.internal.omanetapi.nms.data.NmsSubscriptionUpdate;
import java.io.IOException;

public class CloudMessageUpdateSubscriptionChannel extends IndividualSubscription {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageUpdateSubscriptionChannel.class.getSimpleName();
    private static final long serialVersionUID = -4589569264005795758L;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageUpdateSubscriptionChannel(final IAPICallFlowListener callFlowListener, String restartToken, String url, IControllerCommonInterface callCommonInterface, final ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(url);
        this.mIControllerCommonInterface = callCommonInterface;
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, iCloudMessageManagerHelper);
        NmsSubscriptionUpdate subscriptionUpdate = new NmsSubscriptionUpdate();
        subscriptionUpdate.duration = 86400;
        subscriptionUpdate.restartToken = restartToken;
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(iCloudMessageManagerHelper.getUserTelCtn()));
        initPostRequest(subscriptionUpdate, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String access$000 = CloudMessageUpdateSubscriptionChannel.TAG;
                Log.i(access$000, "response = " + result.getDataString());
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 200) {
                    OMAApiResponseParam response = CloudMessageUpdateSubscriptionChannel.this.getResponse(result);
                    if (response == null) {
                        callFlowListener.onFailedCall(this);
                        return;
                    }
                    NmsSubscription nmsSubscription = response.nmsSubscription;
                    if (nmsSubscription != null) {
                        String restartToken = nmsSubscription.restartToken;
                        int duration = nmsSubscription.duration.intValue();
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            CloudMessageUpdateSubscriptionChannel.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId(), (((long) duration) * 1000) - ScheduleConstant.POLLING_TIME_OUT);
                        }
                        iCloudMessageManagerHelper.saveOMASubscriptionIndex(nmsSubscription.index.longValue() - 1);
                        iCloudMessageManagerHelper.saveOMASubscriptionRestartToken(restartToken);
                        iCloudMessageManagerHelper.saveOMASubscriptionTime(System.currentTimeMillis());
                        iCloudMessageManagerHelper.saveOMASubscriptionChannelDuration(duration);
                        if (!ATTGlobalVariables.isGcmReplacePolling()) {
                            callFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LONG_POLLING_REQUEST.getId(), (Object) null);
                            return;
                        }
                        return;
                    }
                    callFlowListener.onFailedCall(this);
                } else if (CloudMessageUpdateSubscriptionChannel.this.shouldCareAfterResponsePreProcess(callFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    callFlowListener.onFailedCall(CloudMessageUpdateSubscriptionChannel.this);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageUpdateSubscriptionChannel.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                callFlowListener.onFailedCall(this);
            }
        });
    }
}
