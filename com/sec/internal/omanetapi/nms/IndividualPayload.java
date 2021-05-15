package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;

public class IndividualPayload extends BaseNMSRequest {
    public static final String TAG = IndividualPayload.class.getSimpleName();
    private static final long serialVersionUID = 7015982621979245863L;
    private String mObjectId;
    private String mPayloadPartId;

    public IndividualPayload(String serverRoot, String apiVersion, String storeName, String boxId, String objectId, String payloadPartId) {
        super(serverRoot, apiVersion, storeName, boxId);
        this.mObjectId = objectId;
        this.mPayloadPartId = payloadPartId;
        buildAPISpecificURLFromBase();
    }

    public IndividualPayload(String resUrl) {
        super(resUrl);
    }

    public void initGetRequest(String[] acceptContentType) {
        super.initCommonGetRequest();
        setMultipleContentType("Accept", acceptContentType);
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath(this.mObjectId);
        builder.appendPath("payloadParts");
        builder.appendPath(this.mPayloadPartId);
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
