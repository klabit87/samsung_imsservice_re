package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import java.util.ArrayList;
import java.util.List;

public class ObjectsOpBulkCreation extends BaseNMSRequest {
    private static final String TAG = ObjectsOpBulkCreation.class.getSimpleName();

    public ObjectsOpBulkCreation(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public void initPostRequest(ObjectList objectList, boolean isJson, List<HttpPostBody> payloadsList) {
        setUrl(this.mBaseUrl);
        this.mNMSRequestHeaderMap.put("Content-Type", HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA);
        setHeaders(this.mNMSRequestHeaderMap);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        List<HttpPostBody> requestParts = new ArrayList<>();
        if (isJson) {
            requestParts.add(new HttpPostBody("form-data; name=root-fields", "application/json", new Gson().toJson(objectList)));
        }
        for (HttpPostBody payloads : payloadsList) {
            requestParts.add(payloads);
        }
        setPostBody(new HttpPostBody(requestParts));
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath("operations");
        builder.appendPath("bulkCreation");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
