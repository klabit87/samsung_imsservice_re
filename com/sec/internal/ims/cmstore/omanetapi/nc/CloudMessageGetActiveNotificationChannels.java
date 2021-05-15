package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannel;
import com.sec.internal.omanetapi.nc.data.ChannelDeleteData;
import com.sec.internal.omanetapi.nc.data.NotificationChannelList;
import java.io.IOException;
import java.net.URL;

public class CloudMessageGetActiveNotificationChannels extends IndividualNotificationChannel {
    public static final String TAG = CloudMessageGetActiveNotificationChannels.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;

    public CloudMessageGetActiveNotificationChannels(final IAPICallFlowListener callFlowListener, IControllerCommonInterface callCommonInterface, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNcHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getUserTelCtn());
        this.mIAPICallFlowListener = callFlowListener;
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(iCloudMessageManagerHelper.getUserTelCtn()));
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 200) {
                    Gson gson = new Gson();
                    try {
                        String str = CloudMessageGetActiveNotificationChannels.TAG;
                        Log.i(str, "result content: " + result.getDataString());
                        OMAApiResponseParam response = (OMAApiResponseParam) gson.fromJson(result.getDataString(), OMAApiResponseParam.class);
                        if (response == null || response.notificationChannelList == null || response.notificationChannelList.length == 0) {
                            Log.d(CloudMessageGetActiveNotificationChannels.TAG, "no active channels, need create channel");
                            CloudMessageGetActiveNotificationChannels.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) null);
                            return;
                        }
                        NotificationChannelList[] notificationChannelList = response.notificationChannelList;
                        String str2 = CloudMessageGetActiveNotificationChannels.TAG;
                        Log.i(str2, "get active channels, notificationChannelList length = " + notificationChannelList.length);
                        int i = 0;
                        int length = notificationChannelList.length;
                        for (int i2 = 0; i2 < length; i2++) {
                            URL resUrl = notificationChannelList[i2].notificationChannel.resourceURL;
                            String str3 = CloudMessageGetActiveNotificationChannels.TAG;
                            Log.i(str3, "get active channels, resourceURL: " + resUrl);
                            i++;
                            if (resUrl != null) {
                                ChannelDeleteData channelDeleteData = new ChannelDeleteData();
                                channelDeleteData.channelUrl = resUrl.toString();
                                channelDeleteData.isNeedRecreateChannel = false;
                                if (i == notificationChannelList.length) {
                                    channelDeleteData.isNeedRecreateChannel = true;
                                }
                                CloudMessageGetActiveNotificationChannels.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.DELETE_NOTIFICATION_CHANNEL.getId(), channelDeleteData);
                            }
                        }
                    } catch (Exception e) {
                        String str4 = CloudMessageGetActiveNotificationChannels.TAG;
                        Log.e(str4, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageGetActiveNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
                    }
                } else if (CloudMessageGetActiveNotificationChannels.this.shouldCareAfterResponsePreProcess(callFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    callFlowListener.onFailedCall(CloudMessageGetActiveNotificationChannels.this);
                }
            }

            public void onFail(IOException arg1) {
                String str = CloudMessageGetActiveNotificationChannels.TAG;
                Log.e(str, "Http request onFail: " + arg1.getMessage());
                CloudMessageGetActiveNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
