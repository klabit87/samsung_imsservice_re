package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;

public class IndividualFolderData extends BaseNMSRequest {
    private static final String TAG = IndividualFolderData.class.getSimpleName();
    private final String mFolderId;
    private final String mResourceRelPath;

    public IndividualFolderData(String serverRoot, String apiVersion, String storeName, String boxId, String folderId, String ResourceRelPath) {
        super(serverRoot, apiVersion, storeName, boxId);
        this.mFolderId = folderId;
        this.mResourceRelPath = ResourceRelPath;
        buildAPISpecificURLFromBase();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("folders");
        builder.appendPath(this.mFolderId);
        builder.appendPath(this.mResourceRelPath);
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
