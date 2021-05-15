package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.BulkUpdate;

public class BulkUpdating extends BaseNMSRequest {
    private static final String TAG = BulkUpdating.class.getSimpleName();
    private static final long serialVersionUID = 1;
    private transient BulkUpdate mBulkupdate;

    public BulkUpdating(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public void initBulkUpdateRequest(BulkUpdate bulkupdate, boolean isJson) {
        OMAApiRequestParam.BulkUpdateRequest request = new OMAApiRequestParam.BulkUpdateRequest();
        request.bulkUpdate = bulkupdate;
        this.mBulkupdate = bulkupdate;
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

    public BulkUpdate getBulkUpdateParam() {
        return this.mBulkupdate;
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath("operations");
        builder.appendPath("bulkUpdate");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, this.mBaseUrl);
    }
}
