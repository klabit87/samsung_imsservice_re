package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.net.Uri;
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
import com.sec.internal.omanetapi.nc.data.NotificationChannelLifetime;
import java.io.IOException;

public class CloudMessageUpdateNotificationChannelLifeTime extends IndividualNotificationChannel {
    public static final String TAG = CloudMessageUpdateNotificationChannelLifeTime.class.getSimpleName();
    private static final long serialVersionUID = 8158555957984259234L;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageUpdateNotificationChannelLifeTime(final IAPICallFlowListener callFlowListener, IControllerCommonInterface callCommonInterface, String channelId, final ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNcHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getUserTelCtn(), channelId);
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("channelLifetime");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, this.mBaseUrl);
        this.mIAPICallFlowListener = callFlowListener;
        this.mIControllerCommonInterface = callCommonInterface;
        NotificationChannelLifetime notificationChannelLifetime = new NotificationChannelLifetime();
        notificationChannelLifetime.channelLifetime = 86400;
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(iCloudMessageManagerHelper.getUserTelCtn()));
        this.mNCRequestHeaderMap.remove("Authorization");
        initPutRequest(notificationChannelLifetime, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 200) {
                    Gson gson = new Gson();
                    try {
                        String str = CloudMessageUpdateNotificationChannelLifeTime.TAG;
                        Log.i(str, "result content: " + result.getDataString());
                        OMAApiResponseParam response = (OMAApiResponseParam) gson.fromJson(result.getDataString(), OMAApiResponseParam.class);
                        if (response == null) {
                            CloudMessageUpdateNotificationChannelLifeTime.this.mIAPICallFlowListener.onFailedCall(this);
                            return;
                        }
                        long channelLifeTime = response.notificationChannelLifetime.channelLifetime;
                        String str2 = CloudMessageUpdateNotificationChannelLifeTime.TAG;
                        Log.i(str2, "channelLifeTime: " + channelLifeTime);
                        iCloudMessageManagerHelper.saveOMAChannelCreateTime(System.currentTimeMillis());
                        iCloudMessageManagerHelper.saveOMAChannelLifeTime(channelLifeTime);
                        CloudMessageUpdateNotificationChannelLifeTime.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.UPDATE_NOTIFICATIONCHANNEL_LIFETIME.getId(), (channelLifeTime - 900) * 1000);
                    } catch (Exception e) {
                        String str3 = CloudMessageUpdateNotificationChannelLifeTime.TAG;
                        Log.e(str3, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageUpdateNotificationChannelLifeTime.this.mIAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                }
                if (CloudMessageUpdateNotificationChannelLifeTime.this.shouldCareAfterResponsePreProcess(callFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                }
            }

            public void onFail(IOException arg1) {
                String str = CloudMessageUpdateNotificationChannelLifeTime.TAG;
                Log.e(str, "Http request onFail: " + arg1.getMessage());
                CloudMessageUpdateNotificationChannelLifeTime.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
