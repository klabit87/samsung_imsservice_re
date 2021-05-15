package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.helper.MailBoxHelper;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.NotificationList;
import com.sec.internal.omanetapi.nc.data.LongPollingRequestParameters;
import java.io.IOException;

public class CloudMessageCreateLongPolling extends NotificationList {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageCreateLongPolling.class.getSimpleName();
    private static final long serialVersionUID = -1240603457039213893L;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public CloudMessageCreateLongPolling(final IAPICallFlowListener callFlowListener, String channelURL, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(channelURL);
        this.mIAPICallFlowListener = callFlowListener;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, iCloudMessageManagerHelper);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(this.mICloudMessageManagerHelper.getUserTelCtn()));
        initPostRequest((LongPollingRequestParameters) null, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.ONE_POLLING_FINISHED.getId(), (Object) null);
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 200) {
                    OMAApiResponseParam response = (OMAApiResponseParam) new Gson().fromJson(result.getDataString(), OMAApiResponseParam.class);
                    if (response == null || response.notificationList == null) {
                        Log.i(CloudMessageCreateLongPolling.TAG, "response or notificationList is null, polling failed");
                        CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                    com.sec.internal.omanetapi.nc.data.NotificationList[] notificationList = response.notificationList;
                    boolean isMissingNotification = false;
                    if (notificationList.length > 0) {
                        if (MailBoxHelper.isMailBoxReset(result.getDataString())) {
                            Log.i(CloudMessageCreateLongPolling.TAG, "MailBoxReset true");
                            CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.MAILBOX_RESET.getId(), (Object) null);
                            return;
                        } else if (notificationList[0].nmsEventList != null) {
                            long savedindex = CloudMessageCreateLongPolling.this.mICloudMessageManagerHelper.getOMASubscriptionIndex();
                            long curindex = notificationList[0].nmsEventList.index.longValue();
                            String access$100 = CloudMessageCreateLongPolling.TAG;
                            Log.i(access$100, "savedindex: " + savedindex + " curindex: " + curindex);
                            if (savedindex != 0 && curindex > 1 + savedindex) {
                                isMissingNotification = true;
                            }
                            String restartToken = notificationList[0].nmsEventList.restartToken;
                            CloudMessageCreateLongPolling.this.mICloudMessageManagerHelper.saveOMASubscriptionIndex(curindex);
                            CloudMessageCreateLongPolling.this.mICloudMessageManagerHelper.saveOMASubscriptionRestartToken(restartToken);
                        }
                    }
                    ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setNotificationList(notificationList);
                    if (isMissingNotification) {
                        CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId(), (Object) null);
                    }
                    CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CLOUD_UPDATE.getId(), builder.build());
                }
                if (CloudMessageCreateLongPolling.this.shouldCareAfterResponsePreProcess(callFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    callFlowListener.onFailedCall(CloudMessageCreateLongPolling.this);
                }
            }

            public void onFail(IOException arg1) {
                String access$100 = CloudMessageCreateLongPolling.TAG;
                Log.e(access$100, "Http request onFail: " + arg1.getMessage());
                CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback) {
        return new CloudMessageCreateLongPolling(this.mIAPICallFlowListener, this.mICloudMessageManagerHelper.getOMAChannelURL(), this.mICloudMessageManagerHelper);
    }
}
