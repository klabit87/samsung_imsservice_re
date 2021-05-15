package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.FlagList;

public class AllFlags extends BaseNMSRequest {
    private static final String TAG = AllFlags.class.getSimpleName();
    private final String mObjectId;

    public AllFlags(String serverRoot, String apiVersion, String storeName, String boxId, String objectId) {
        super(serverRoot, apiVersion, storeName, boxId);
        this.mObjectId = objectId;
        buildAPISpecificURLFromBase();
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initPutRequest(FlagList flagList, boolean isJson) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.PUT);
        setFollowRedirects(false);
        HttpPostBody body = null;
        if (isJson) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            body = new HttpPostBody(new Gson().toJson(flagList));
        }
        if (body != null) {
            setPostBody(body);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath(this.mObjectId);
        builder.appendPath("flags");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
