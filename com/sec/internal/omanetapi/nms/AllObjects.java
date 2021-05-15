package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.Object;
import java.util.ArrayList;
import java.util.List;

public class AllObjects extends BaseNMSRequest {
    public static final String TAG = AllObjects.class.getSimpleName();
    private static final long serialVersionUID = -3559371338445770425L;

    public AllObjects(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public AllObjects(String baseUrl) {
        super(baseUrl);
    }

    public void initPostRequest(Object object, boolean isJson, HttpPostBody payloads) {
        if (payloads != null) {
            String str = TAG;
            Log.d(str, "initPostRequest: postBody: " + payloads.toString());
            setUrl(this.mBaseUrl);
            this.mNMSRequestHeaderMap.put("Content-Type", HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA);
            setHeaders(this.mNMSRequestHeaderMap);
            setMethod(HttpRequestParams.Method.POST);
            setFollowRedirects(false);
            List<HttpPostBody> requestParts = new ArrayList<>();
            if (isJson) {
                OMAApiRequestParam.AllObjectRequest objectParam = new OMAApiRequestParam.AllObjectRequest();
                objectParam.object = object;
                requestParts.add(new HttpPostBody("form-data; name=\"root-fields\"", "application/json", new Gson().toJson(objectParam)));
            }
            requestParts.add(payloads);
            setPostBody(new HttpPostBody(requestParts));
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("objects");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
