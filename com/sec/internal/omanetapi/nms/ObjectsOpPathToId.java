package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;

public class ObjectsOpPathToId extends BaseNMSRequest {
    private static final String TAG = ObjectsOpPathToId.class.getSimpleName();

    public ObjectsOpPathToId(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath("operations");
        builder.appendPath("pathToId");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
