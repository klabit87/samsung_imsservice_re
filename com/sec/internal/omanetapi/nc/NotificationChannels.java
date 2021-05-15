package com.sec.internal.omanetapi.nc;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nc.data.ChannelData;
import com.sec.internal.omanetapi.nc.data.NotificationChannel;

public class NotificationChannels extends BaseNCRequest {
    private static final long serialVersionUID = 2784778118212806724L;
    private final String TAG = NotificationChannels.class.getSimpleName();

    public NotificationChannels(String serverRoot, String apiVersion, String userId) {
        super(serverRoot, apiVersion, userId);
        buildAPISpecificURLFromBase();
    }

    public void initPostRequest(NotificationChannel notificationChannel, boolean isJson) {
        OMAApiRequestParam.NotificationChannels request = new OMAApiRequestParam.NotificationChannels();
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        HttpPostBody body = null;
        if (isJson) {
            this.mNCRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNCRequestHeaderMap);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ChannelData.class, new GsonInterfaceAdapter(ChannelData.class));
            Gson gson = gsonBuilder.disableHtmlEscaping().create();
            request.notificationChannel = notificationChannel;
            body = new HttpPostBody(gson.toJson(request));
        }
        if (body != null) {
            setPostBody(body);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("channels");
        this.mBaseUrl = builder.build().toString();
        Log.i(this.TAG, IMSLog.checker(this.mBaseUrl));
    }
}
