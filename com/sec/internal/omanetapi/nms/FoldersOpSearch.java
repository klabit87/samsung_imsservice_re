package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;

public class FoldersOpSearch extends BaseNMSRequest {
    private static final String TAG = FoldersOpSearch.class.getSimpleName();

    public FoldersOpSearch(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public void initPostRequest(SelectionCriteria selectionCriteria, boolean isJson) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        HttpPostBody body = null;
        if (isJson) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            body = new HttpPostBody(new Gson().toJson(selectionCriteria));
        }
        if (body != null) {
            setPostBody(body);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("folders");
        builder.appendPath("operations");
        builder.appendPath("search");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
