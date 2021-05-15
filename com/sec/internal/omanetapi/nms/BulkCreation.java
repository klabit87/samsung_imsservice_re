package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import java.util.ArrayList;
import java.util.List;

public class BulkCreation extends BaseNMSRequest {
    public static final String TAG = BulkCreation.class.getSimpleName();
    private static final long serialVersionUID = 6070003579804341648L;

    public BulkCreation(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public BulkCreation(String baseUrl) {
        super(baseUrl);
    }

    public void initPostRequest(ObjectList objectList, boolean isJson, List<HttpPostBody> payloads) {
        if (payloads != null) {
            try {
                String str = TAG;
                Log.d(str, "initPostRequest: postBody: " + payloads.toString());
            } catch (OutOfMemoryError e) {
                String str2 = TAG;
                Log.e(str2, "initPostRequest: postBody: " + payloads.size());
            }
            setUrl(this.mBaseUrl);
            this.mNMSRequestHeaderMap.put("Content-Type", HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA);
            setHeaders(this.mNMSRequestHeaderMap);
            setMethod(HttpRequestParams.Method.POST);
            setFollowRedirects(false);
            List<HttpPostBody> requestParts = new ArrayList<>();
            if (isJson) {
                OMAApiRequestParam.BulkCreationRequest objectParam = new OMAApiRequestParam.BulkCreationRequest();
                objectParam.objectList = objectList;
                requestParts.add(new HttpPostBody("form-data; name=\"root-fields\"", "application/json", new Gson().toJson(objectParam)));
            }
            requestParts.addAll(payloads);
            setPostBody(new HttpPostBody(requestParts));
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        builder.appendPath("operations");
        builder.appendPath("bulkCreation");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, this.mBaseUrl);
    }
}
