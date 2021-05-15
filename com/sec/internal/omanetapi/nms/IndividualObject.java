package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;

public class IndividualObject extends BaseNMSRequest {
    public static final String TAG = IndividualObject.class.getSimpleName();
    private static final long serialVersionUID = -4932381371123746768L;
    private final String mObjectId;

    public IndividualObject(String serverRoot, String apiVersion, String storeName, String boxId, String objectId) {
        super(serverRoot, apiVersion, storeName, boxId);
        this.mObjectId = objectId;
        buildAPISpecificURLFromBase();
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initDeleteRequest() {
        super.initCommonDeleteRequest();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath(this.mObjectId);
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
