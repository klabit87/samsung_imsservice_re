package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannel;
import java.io.IOException;

public class CloudMessageGetIndividualNotificationChannelInfo extends IndividualNotificationChannel {
    public static final String TAG = CloudMessageGetIndividualNotificationChannelInfo.class.getSimpleName();
    private static final long serialVersionUID = 8158555957984259234L;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    private final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageGetIndividualNotificationChannelInfo(final IAPICallFlowListener callFlowListener, IControllerCommonInterface callCommonInterface, String channelId, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNcHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getUserTelCtn(), channelId);
        this.mIAPICallFlowListener = callFlowListener;
        this.mIControllerCommonInterface = callCommonInterface;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
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
                        String str = CloudMessageGetIndividualNotificationChannelInfo.TAG;
                        Log.i(str, "result content: " + result.getDataString());
                        OMAApiResponseParam response = (OMAApiResponseParam) gson.fromJson(result.getDataString(), OMAApiResponseParam.class);
                        if (response == null || response.notificationChannel == null) {
                            Log.d(CloudMessageGetIndividualNotificationChannelInfo.TAG, "notification == null");
                            CloudMessageGetIndividualNotificationChannelInfo.this.mIAPICallFlowListener.onFailedCall(this);
                            return;
                        }
                        long channelLifeTime = (long) response.notificationChannel.channelLifetime;
                        String str2 = CloudMessageGetIndividualNotificationChannelInfo.TAG;
                        Log.d(str2, "channelLifeTime=" + channelLifeTime);
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            CloudMessageGetIndividualNotificationChannelInfo.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.UPDATE_NOTIFICATIONCHANNEL_LIFETIME.getId(), (channelLifeTime - 900) * 1000);
                        }
                    } catch (Exception e) {
                        String str3 = CloudMessageGetIndividualNotificationChannelInfo.TAG;
                        Log.e(str3, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageGetIndividualNotificationChannelInfo.this.mIAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                }
                if (CloudMessageGetIndividualNotificationChannelInfo.this.shouldCareAfterResponsePreProcess(callFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    callFlowListener.onFailedCall(CloudMessageGetIndividualNotificationChannelInfo.this);
                }
            }

            public void onFail(IOException arg1) {
                String str = CloudMessageGetIndividualNotificationChannelInfo.TAG;
                Log.e(str, "Http request onFail: " + arg1.getMessage());
                CloudMessageGetIndividualNotificationChannelInfo.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback) {
        String resUrl = this.mICloudMessageManagerHelper.getOMAChannelResURL();
        if (TextUtils.isEmpty(resUrl)) {
            return null;
        }
        return new CloudMessageGetIndividualNotificationChannelInfo(this.mIAPICallFlowListener, this.mIControllerCommonInterface, resUrl.substring(resUrl.lastIndexOf(47) + 1), this.mICloudMessageManagerHelper);
    }
}
