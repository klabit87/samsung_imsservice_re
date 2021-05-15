package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;

public class IndividualFolder extends BaseNMSRequest {
    private static final String TAG = IndividualFolder.class.getSimpleName();
    private final String mAttrFilter;
    private final String mFolderId;
    private final String mFromCursor;
    private final String mListFilter;
    private final int mMaxEntries;
    private final String mPath;

    public IndividualFolder(String serverRoot, String apiVersion, String storeName, String boxId, String folderId, String fromCursor, int maxEntries, String listFilter, String path, String attrFilter) {
        super(serverRoot, apiVersion, storeName, boxId);
        this.mFolderId = folderId;
        this.mFromCursor = fromCursor;
        this.mMaxEntries = maxEntries;
        this.mListFilter = listFilter;
        this.mPath = path;
        this.mAttrFilter = attrFilter;
        buildAPISpecificURLFromBase();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("folders");
        builder.appendPath(this.mFolderId);
        String str = this.mFromCursor;
        if (str != null) {
            builder.appendQueryParameter("fromCursor", str);
        }
        int i = this.mMaxEntries;
        if (i > 0) {
            builder.appendQueryParameter("maxEntries", String.valueOf(i));
        }
        String str2 = this.mListFilter;
        if (str2 != null) {
            builder.appendQueryParameter("listFilter", str2);
        }
        String str3 = this.mPath;
        if (str3 != null) {
            builder.appendQueryParameter("path", str3);
        }
        String str4 = this.mAttrFilter;
        if (str4 != null) {
            builder.appendQueryParameter("attrFilter", str4);
        }
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
