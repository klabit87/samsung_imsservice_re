package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;

public class AllPayloads extends BaseNMSRequest {
    public static final String TAG = AllPayloads.class.getSimpleName();
    private static final long serialVersionUID = 6070003579804341648L;
    private String mObjectId;

    public AllPayloads(String serverRoot, String apiVersion, String storeName, String boxId, String objectId) {
        super(serverRoot, apiVersion, storeName, boxId);
        this.mObjectId = objectId;
        buildAPISpecificURLFromBase();
    }

    public AllPayloads(String baseUrl) {
        super(baseUrl);
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initGetRequest(String[] acceptContentType) {
        super.initCommonGetRequest();
        setMultipleContentType("Accept", acceptContentType);
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath(this.mObjectId);
        builder.appendPath("payload");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
