package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.NotificationChannels;
import com.sec.internal.omanetapi.nc.data.ChannelData;
import com.sec.internal.omanetapi.nc.data.ChannelType;
import com.sec.internal.omanetapi.nc.data.GcmChannelData;
import com.sec.internal.omanetapi.nc.data.LongPollingData;
import com.sec.internal.omanetapi.nc.data.NotificationChannel;
import java.io.IOException;

public class CloudMessageCreateNotificationChannels extends NotificationChannels {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageCreateNotificationChannels.class.getSimpleName();
    private static final long serialVersionUID = 3299934859221120896L;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageCreateNotificationChannels(final IAPICallFlowListener callFlowListener, IControllerCommonInterface callCommonInterface, final boolean isNeedDeleteSubscription, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNcHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getUserTelCtn());
        this.mIAPICallFlowListener = callFlowListener;
        this.mIControllerCommonInterface = callCommonInterface;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        NotificationChannel notificationChannel = new NotificationChannel();
        notificationChannel.clientCorrelator = "";
        notificationChannel.applicationTag = "";
        notificationChannel.channelLifetime = 86400;
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            notificationChannel.channelType = ChannelType.NativeChannel;
            GcmChannelData channelData = new GcmChannelData();
            channelData.channelSubType = OMAGlobalVariables.CHANNEL_TYPE_GCM;
            channelData.channelSubTypeVersion = "1.0";
            channelData.registrationToken = this.mICloudMessageManagerHelper.getGcmTokenFromVsim();
            channelData.maxNotifications = 1;
            notificationChannel.channelData = channelData;
        } else {
            notificationChannel.channelType = ChannelType.LongPolling;
            LongPollingData channelData2 = new LongPollingData();
            channelData2.maxNotifications = 1;
            notificationChannel.channelData = channelData2;
        }
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(this.mICloudMessageManagerHelper.getUserTelCtn()));
        initPostRequest(notificationChannel, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                if (result.getStatusCode() == 201) {
                    GsonBuilder gsonBilder = new GsonBuilder();
                    gsonBilder.registerTypeAdapter(ChannelData.class, new GsonInterfaceAdapter(LongPollingData.class));
                    Gson gson = gsonBilder.create();
                    try {
                        String access$000 = CloudMessageCreateNotificationChannels.TAG;
                        Log.i(access$000, "GetDataString=" + result.getDataString());
                        OMAApiResponseParam response = (OMAApiResponseParam) gson.fromJson(result.getDataString(), OMAApiResponseParam.class);
                        if (response == null || response.notificationChannel == null) {
                            CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
                            return;
                        }
                        NotificationChannel channel = response.notificationChannel;
                        String resUrl = channel.resourceURL == null ? "" : channel.resourceURL.toString();
                        if (!ATTGlobalVariables.isGcmReplacePolling()) {
                            LongPollingData data = (LongPollingData) channel.channelData;
                            if (data.channelURL == null) {
                                CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
                                return;
                            }
                            CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.saveOMAChannelURL(data.channelURL.toString());
                        }
                        long channelLifeTime = (long) channel.channelLifetime;
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            CloudMessageCreateNotificationChannels.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.UPDATE_NOTIFICATIONCHANNEL_LIFETIME.getId(), (channelLifeTime - 900) * 1000);
                        }
                        String access$0002 = CloudMessageCreateNotificationChannels.TAG;
                        Log.i(access$0002, "channelLifeTime=" + channelLifeTime + " callbackURL: " + channel.callbackURL + " isNeedDeleteSubscription: " + isNeedDeleteSubscription);
                        if (channel.callbackURL != null && !channel.callbackURL.equals(CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.getOMACallBackURL())) {
                            if (isNeedDeleteSubscription) {
                                CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.DELETE_SUBCRIPTION_CHANNEL.getId(), CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.getOMASubscriptionResUrl());
                            }
                            CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.saveOMASubscriptionTime(0);
                            CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.saveOMASubscriptionChannelDuration(0);
                        }
                        CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.saveOMAChannelResURL(resUrl);
                        if (channel.callbackURL != null) {
                            CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.saveOMACallBackURL(channel.callbackURL.toString());
                        }
                        CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.saveOMAChannelCreateTime(System.currentTimeMillis());
                        CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.saveOMAChannelLifeTime(channelLifeTime);
                        CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.clearOMASubscriptionChannelDuration();
                        CloudMessageCreateNotificationChannels.this.mICloudMessageManagerHelper.clearOMASubscriptionTime();
                        CloudMessageCreateNotificationChannels.this.mIControllerCommonInterface.update(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL_FINISHED.getId());
                    } catch (Exception e) {
                        String access$0003 = CloudMessageCreateNotificationChannels.TAG;
                        Log.e(access$0003, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                }
                if (CloudMessageCreateNotificationChannels.this.shouldCareAfterResponsePreProcess(callFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    callFlowListener.onFailedCall(CloudMessageCreateNotificationChannels.this);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageCreateNotificationChannels.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback) {
        return new CloudMessageCreateNotificationChannels(this.mIAPICallFlowListener, this.mIControllerCommonInterface, true, this.mICloudMessageManagerHelper);
    }
}
