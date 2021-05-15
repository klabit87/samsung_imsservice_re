package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;

public class IndividualVvmProfile extends BaseNMSRequest {
    public static final String TAG = IndividualVvmProfile.class.getSimpleName();
    private static final long serialVersionUID = -6892711250370417577L;

    public IndividualVvmProfile(String serverRoot, String apiVersion, String storeName, String boxId) {
        super(serverRoot, apiVersion, storeName, boxId);
        buildAPISpecificURLFromBase();
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initPostRequest(VvmServiceProfile profile, boolean isJson) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        OMAApiRequestParam.VvmServiceProfileRequest request = new OMAApiRequestParam.VvmServiceProfileRequest();
        request.vvmserviceProfile = profile;
        HttpPostBody body = null;
        if (isJson) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            body = new HttpPostBody(new Gson().toJson(request));
        } else {
            Log.e(TAG, "XML");
        }
        setPostBody(body);
    }

    public void initPutRequest(VvmServiceProfile profile, boolean isJson) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.PUT);
        setFollowRedirects(false);
        this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
        setHeaders(this.mNMSRequestHeaderMap);
        if (isJson) {
            OMAApiRequestParam.VvmServiceProfileRequest serviceProfile = new OMAApiRequestParam.VvmServiceProfileRequest();
            serviceProfile.vvmserviceProfile = profile;
            setPostBody(new HttpPostBody(new Gson().toJson(serviceProfile)));
            return;
        }
        Log.e(TAG, "XML");
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("vvmserviceProfile");
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, this.mBaseUrl);
    }
}
