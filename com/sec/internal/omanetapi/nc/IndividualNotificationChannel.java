package com.sec.internal.omanetapi.nc;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nc.data.NotificationChannelLifetime;

public class IndividualNotificationChannel extends BaseNCRequest {
    private static final String TAG = IndividualNotificationChannel.class.getSimpleName();
    private static final long serialVersionUID = 7321524394607040641L;
    private final String mChannelId;

    public IndividualNotificationChannel(String serverRoot, String apiVersion, String userId, String channelId) {
        super(serverRoot, apiVersion, userId);
        this.mChannelId = channelId;
        buildAPISpecificURLFromBase();
    }

    public IndividualNotificationChannel(String serverRoot, String apiVersion, String userId) {
        super(serverRoot, apiVersion, userId);
        this.mChannelId = "";
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("channels");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initDeleteRequest() {
        super.initCommonDeleteRequest();
    }

    public void initPutRequest(NotificationChannelLifetime notiChannelLifetime, boolean isJson) {
        super.initCommonPutRequest();
        HttpPostBody body = null;
        if (isJson) {
            this.mNCRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNCRequestHeaderMap);
            OMAApiRequestParam.NotificationChannelLifetimeRequest request = new OMAApiRequestParam.NotificationChannelLifetimeRequest();
            request.notificationChannelLifetime = notiChannelLifetime;
            body = new HttpPostBody(new Gson().toJson(request));
        }
        if (body != null) {
            setPostBody(body);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("channels").appendPath(this.mChannelId);
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
