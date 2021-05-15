package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.helper.MailBoxHelper;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.NotificationListContainer;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.NotificationList;
import com.sec.internal.omanetapi.nc.data.LongPollingRequestParameters;
import java.io.IOException;
import java.util.Arrays;

public class CloudMessageCreateLargeDataPolling extends NotificationList {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageCreateLargeDataPolling.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageCreateLargeDataPolling(final IAPICallFlowListener callFlowListener, IControllerCommonInterface iControllerCommonInterface, String channelURL, final ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(channelURL);
        this.mIAPICallFlowListener = callFlowListener;
        this.mIControllerCommonInterface = iControllerCommonInterface;
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, iCloudMessageManagerHelper);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(iCloudMessageManagerHelper.getUserTelCtn()));
        initPostRequest((LongPollingRequestParameters) null, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                } else {
                    HttpResponseParams httpResponseParams = result;
                }
                if (result.getStatusCode() == 200) {
                    try {
                        OMAApiResponseParam response = (OMAApiResponseParam) new Gson().fromJson(result.getDataString(), OMAApiResponseParam.class);
                        String access$000 = CloudMessageCreateLargeDataPolling.TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("OMAApiResponseParam response ");
                        sb.append(result.getDataString());
                        sb.append(" response.notificationList: ");
                        sb.append(response != null ? Arrays.toString(response.notificationList) : null);
                        Log.i(access$000, sb.toString());
                        if (response == null || response.notificationList == null) {
                            CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onFailedCall(this);
                            CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                            return;
                        }
                        com.sec.internal.omanetapi.nc.data.NotificationList[] notificationList = response.notificationList;
                        boolean setDelayedUpdateSubscription = false;
                        if (notificationList.length > 0) {
                            if (MailBoxHelper.isMailBoxReset(result.getDataString())) {
                                Log.i(CloudMessageCreateLargeDataPolling.TAG, "MailBoxReset true");
                                CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.MAILBOX_RESET.getId(), (Object) null);
                                CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                                return;
                            } else if (notificationList[0].nmsEventList != null && Util.isMatchedSubscriptionID(notificationList[0])) {
                                long savedindex = iCloudMessageManagerHelper.getOMASubscriptionIndex();
                                long curindex = notificationList[0].nmsEventList.index.longValue();
                                String access$0002 = CloudMessageCreateLargeDataPolling.TAG;
                                Log.i(access$0002, "curindex: " + curindex + ",savedindex: " + savedindex);
                                if (curindex > savedindex + 1) {
                                    if (NotificationListContainer.getInstance().isEmpty()) {
                                        setDelayedUpdateSubscription = true;
                                    }
                                    NotificationListContainer.getInstance().insertContainer(Long.valueOf(curindex), notificationList);
                                } else if (curindex == savedindex + 1) {
                                    iCloudMessageManagerHelper.saveOMASubscriptionRestartToken(notificationList[0].nmsEventList.restartToken);
                                    iCloudMessageManagerHelper.saveOMASubscriptionIndex(curindex);
                                    CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CLOUD_UPDATE.getId(), new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setNotificationList(notificationList).build());
                                    long savedindex2 = iCloudMessageManagerHelper.getOMASubscriptionIndex();
                                    while (true) {
                                        if (NotificationListContainer.getInstance().isEmpty() || NotificationListContainer.getInstance().peekFirstIndex() != savedindex2 + 1) {
                                            break;
                                        }
                                        com.sec.internal.omanetapi.nc.data.NotificationList[] notificationList2 = NotificationListContainer.getInstance().popFirstEntry().getValue();
                                        String restartToken = notificationList2[0].nmsEventList.restartToken;
                                        long curindex2 = notificationList2[0].nmsEventList.index.longValue();
                                        iCloudMessageManagerHelper.saveOMASubscriptionRestartToken(restartToken);
                                        iCloudMessageManagerHelper.saveOMASubscriptionIndex(curindex2);
                                        CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CLOUD_UPDATE.getId(), new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setNotificationList(notificationList2).build());
                                        savedindex2 = iCloudMessageManagerHelper.getOMASubscriptionIndex();
                                        if (NotificationListContainer.getInstance().isEmpty()) {
                                            CloudMessageCreateLargeDataPolling.this.mIControllerCommonInterface.update(OMASyncEventType.REMOVE_UPDATE_SUBSCRIPTION_CHANNEL.getId());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (setDelayedUpdateSubscription) {
                            CloudMessageCreateLargeDataPolling.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId(), 60000);
                        }
                        CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                    } catch (Exception e) {
                        String access$0003 = CloudMessageCreateLargeDataPolling.TAG;
                        Log.e(access$0003, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onFailedCall(this);
                        CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                    }
                } else if (!CloudMessageCreateLargeDataPolling.this.shouldCareAfterResponsePreProcess(callFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                } else {
                    callFlowListener.onFailedCall(CloudMessageCreateLargeDataPolling.this);
                    CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageCreateLargeDataPolling.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onFailedCall(this);
                CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
            }
        });
    }
}
