package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;

public class FoldersOpPathToId extends BaseNMSRequest {
    private static final String TAG = FoldersOpPathToId.class.getSimpleName();

    public FoldersOpPathToId(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public void initGetRequest(String path) {
        if (path != null) {
            Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
            builder.appendQueryParameter("path", path);
            this.mBaseUrl = builder.build().toString();
        }
        super.initCommonGetRequest();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("folders");
        builder.appendPath("operations");
        builder.appendPath("pathToId");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
