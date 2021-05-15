package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.BulkDelete;

public class BulkDeletion extends BaseNMSRequest {
    private static final String TAG = BulkDeletion.class.getSimpleName();
    private static final long serialVersionUID = 1;

    public BulkDeletion(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public void initDeleteRequest(BulkDelete bulkdelete, boolean isJson) {
        super.initCommonDeleteRequest();
        OMAApiRequestParam.BulkDeletionRequest request = new OMAApiRequestParam.BulkDeletionRequest();
        request.bulkDelete = bulkdelete;
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
        builder.appendPath("bulkDelete");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
