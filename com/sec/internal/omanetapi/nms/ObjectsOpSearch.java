package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;

public class ObjectsOpSearch extends BaseNMSRequest {
    private static final String TAG = ObjectsOpSearch.class.getSimpleName();
    private static final long serialVersionUID = -6272072039068987383L;

    public ObjectsOpSearch(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public void initPostRequest(SelectionCriteria selectionCriteria, boolean isJson) {
        OMAApiRequestParam.ObjectSearchRequest request = new OMAApiRequestParam.ObjectSearchRequest();
        request.selectionCriteria = selectionCriteria;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        HttpPostBody body = null;
        if (isJson) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            body = new HttpPostBody(new GsonBuilder().disableHtmlEscaping().create().toJson(request));
        }
        if (body != null) {
            setPostBody(body);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath("operations");
        builder.appendPath("search");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
