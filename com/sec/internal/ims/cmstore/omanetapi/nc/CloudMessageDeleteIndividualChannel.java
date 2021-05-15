package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannel;
import java.io.IOException;

public class CloudMessageDeleteIndividualChannel extends IndividualNotificationChannel {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageDeleteIndividualChannel.class.getSimpleName();
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageDeleteIndividualChannel(IAPICallFlowListener callFlowListener, IControllerCommonInterface callCommonInterface, String channelId, boolean isNeedRecreateChannel, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNcHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getUserTelCtn(), channelId);
        this.mIAPICallFlowListener = callFlowListener;
        this.mIControllerCommonInterface = callCommonInterface;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        final String mChannelId = channelId;
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(iCloudMessageManagerHelper.getUserTelCtn()));
        initDeleteRequest();
        final boolean z = isNeedRecreateChannel;
        final IAPICallFlowListener iAPICallFlowListener = callFlowListener;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String access$000 = CloudMessageDeleteIndividualChannel.TAG;
                Log.i(access$000, "isNeedRecreateChannel: " + z);
                if (result.getStatusCode() == 200 || result.getStatusCode() == 204) {
                    clearChannelData();
                    if (z) {
                        CloudMessageDeleteIndividualChannel.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) null);
                    } else {
                        iAPICallFlowListener.onSuccessfulCall(this);
                    }
                } else if (CloudMessageDeleteIndividualChannel.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    if (z) {
                        clearChannelData();
                        CloudMessageDeleteIndividualChannel.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) null);
                        return;
                    }
                    iAPICallFlowListener.onMoveOnToNext(CloudMessageDeleteIndividualChannel.this, (Object) null);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageDeleteIndividualChannel.TAG;
                Log.i(access$000, "onFail isNeedRecreateChannel: " + z);
                if (z) {
                    clearChannelData();
                    CloudMessageDeleteIndividualChannel.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) null);
                }
            }

            private void clearChannelData() {
                String resUrl = CloudMessageDeleteIndividualChannel.this.mICloudMessageManagerHelper.getOMAChannelResURL();
                if (!TextUtils.isEmpty(resUrl)) {
                    String nativeChannelId = resUrl.substring(resUrl.lastIndexOf("/") + 1);
                    String access$000 = CloudMessageDeleteIndividualChannel.TAG;
                    Log.i(access$000, "clearChannelData resUrl: " + resUrl + " mChannelId: " + mChannelId + " nativeChannelId: " + nativeChannelId);
                    if (mChannelId.equalsIgnoreCase(nativeChannelId)) {
                        CloudMessageDeleteIndividualChannel.this.mICloudMessageManagerHelper.saveOMAChannelResURL("");
                        CloudMessageDeleteIndividualChannel.this.mICloudMessageManagerHelper.saveOMAChannelURL("");
                        CloudMessageDeleteIndividualChannel.this.mICloudMessageManagerHelper.saveOMACallBackURL("");
                        CloudMessageDeleteIndividualChannel.this.mICloudMessageManagerHelper.saveOMAChannelCreateTime(0);
                        CloudMessageDeleteIndividualChannel.this.mICloudMessageManagerHelper.saveOMAChannelLifeTime(0);
                        CloudMessageDeleteIndividualChannel.this.mICloudMessageManagerHelper.clearOMASubscriptionChannelDuration();
                        CloudMessageDeleteIndividualChannel.this.mICloudMessageManagerHelper.clearOMASubscriptionTime();
                    }
                }
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
