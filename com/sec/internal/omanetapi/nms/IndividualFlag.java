package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.log.IMSLog;

public class IndividualFlag extends BaseNMSRequest {
    public static final String TAG = IndividualFlag.class.getSimpleName();
    private static final long serialVersionUID = -1015575143165860338L;
    private final String mFlagName;
    private final String mObjectId;

    public IndividualFlag(String serverRoot, String apiVersion, String storeName, String boxId, String objectId, String flagName) {
        super(serverRoot, apiVersion, storeName, boxId);
        this.mObjectId = objectId;
        this.mFlagName = flagName;
        buildAPISpecificURLFromBase();
    }

    public void initPutRequest(boolean isJson) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.PUT);
        setFollowRedirects(false);
        HttpPostBody body = new HttpPostBody("");
        if (isJson) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
        }
        setPostBody(body);
    }

    public void initDeleteRequest() {
        super.initCommonDeleteRequest();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath(this.mObjectId);
        builder.appendPath("flags");
        builder.appendPath(this.mFlagName);
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
