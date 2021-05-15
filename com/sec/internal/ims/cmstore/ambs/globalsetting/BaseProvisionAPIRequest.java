package com.sec.internal.ims.cmstore.ambs.globalsetting;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.PersistentHttpCookieStore;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseProvisionAPIRequest extends HttpRequestParams implements IHttpAPICommonInterface {
    public static final String TAG = BaseProvisionAPIRequest.class.getSimpleName();
    private static final long serialVersionUID = -3500664057158035738L;
    protected final transient PersistentHttpCookieStore mCookieStore = ((PersistentHttpCookieStore) ((CookieManager) CookieHandler.getDefault()).getCookieStore());
    protected transient IAPICallFlowListener mFlowListener;

    public BaseProvisionAPIRequest(Map<String, String> headers, IAPICallFlowListener callFlowListener) {
        super(headers);
        this.mFlowListener = callFlowListener;
    }

    public BaseProvisionAPIRequest(String contentType, IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper clouldMessageManager) {
        super(prepareDefaultHeader(contentType, clouldMessageManager));
        this.mFlowListener = callFlowListener;
        setFollowRedirects(false);
    }

    public BaseProvisionAPIRequest(IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper clouldMessageManager) {
        super(prepareDefaultHeader(clouldMessageManager));
        this.mFlowListener = callFlowListener;
        setFollowRedirects(false);
    }

    private static Map<String, String> prepareDefaultHeader(ICloudMessageManagerHelper cloudMessageManager) {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/x-www-form-urlencoded");
        return processDefaultHeader(header, cloudMessageManager);
    }

    private static Map<String, String> prepareDefaultHeader(String contentType, ICloudMessageManagerHelper cloudMessageManager) {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", contentType);
        return processDefaultHeader(header, cloudMessageManager);
    }

    private static Map<String, String> processDefaultHeader(Map<String, String> header, ICloudMessageManagerHelper cloudMessageManager) {
        header.put("Connection", "close");
        header.put("x-att-clientVersion", ATTGlobalVariables.VERSION_NAME);
        header.put("x-att-clientId", ATTGlobalVariables.getHttpClientID());
        header.put("x-att-contextInfo", ATTGlobalVariables.BUILD_INFO);
        header.put("x-att-deviceId", cloudMessageManager.getDeviceId());
        return header;
    }

    /* access modifiers changed from: protected */
    public void goSuccessfulCall(String param) {
        this.mFlowListener.onSuccessfulCall(this, param);
    }

    /* access modifiers changed from: protected */
    public void goSuccessfulCall() {
        this.mFlowListener.onSuccessfulCall(this);
    }

    /* access modifiers changed from: protected */
    public void goFailedCall() {
        this.mFlowListener.onFailedCall(this);
    }

    /* access modifiers changed from: protected */
    public void goFailedCall(String param) {
        this.mFlowListener.onFailedCall((IHttpAPICommonInterface) this, param);
    }

    /* access modifiers changed from: protected */
    public boolean checkAndHandleCPSError(String resBody) {
        if (TextUtils.isEmpty(resBody)) {
            return false;
        }
        try {
            JSONObject requestError = new JSONObject(resBody).optJSONObject("requestError");
            if (requestError == null) {
                return false;
            }
            String messageId = requestError.getJSONObject("serviceException").getString("messageId");
            if (!ATTConstants.ATTErrorNames.CPS_TC_ERROR_1007.equals(messageId) && !ATTConstants.ATTErrorNames.CPS_TC_ERROR_1008.equals(messageId)) {
                if (!ATTConstants.ATTErrorNames.CPS_PROVISION_SHUTDOWN.equals(messageId)) {
                    return false;
                }
            }
            String str = TAG;
            Log.d(str, "CPS errors: " + messageId);
            goFailedCall(messageId);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public int checkRetryAfter(HttpResponseParams result) {
        List<String> retryAfterHeader = result.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER);
        if (retryAfterHeader == null || retryAfterHeader.size() <= 0) {
            return -1;
        }
        String retryAfter = retryAfterHeader.get(0);
        String str = TAG;
        Log.d(str, "retryAfter is " + retryAfter + "seconds retryAfterHeader: " + retryAfterHeader.toString());
        try {
            return Integer.parseInt(retryAfter);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback) {
        return this;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return this;
    }

    public void updateServerRoot(String serverRoot) {
        String str = TAG;
        Log.d(str, "updateServerRoot" + serverRoot);
    }
}
