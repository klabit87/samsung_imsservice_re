package com.sec.internal.ims.cmstore.callHandling.successfullCall;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestCreateAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestPat;
import com.sec.internal.ims.cmstore.ambs.provision.ProvisionController;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.util.List;

public class SuccessfulCallHandling {
    public static final String HAPPY_PATH_DEFAULT = "HAP.DEF";
    public static final String TAG = SuccessfulCallHandling.class.getSimpleName();
    private static final long ZERO_DELAY = 0;

    public static void callHandling(ProvisionController controller, IHttpAPICommonInterface request, IRetryStackAdapterHelper retryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        internalCallHandling(controller, request, HAPPY_PATH_DEFAULT, 0, retryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    public static void callHandling(ProvisionController controller, IHttpAPICommonInterface request, String callFlow, IRetryStackAdapterHelper retryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        internalCallHandling(controller, request, callFlow, 0, retryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    private static void internalCallHandling(ProvisionController controller, IHttpAPICommonInterface request, String callFlow, long delay, IRetryStackAdapterHelper retryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        IHttpAPICommonInterface top = retryStackAdapterHelper.getLastFailedRequest();
        if (top != null && top.getClass().getSimpleName().equals(request.getClass().getSimpleName())) {
            IHttpAPICommonInterface popApi = retryStackAdapterHelper.pop();
            String popApiName = popApi == null ? null : popApi.getClass().getSimpleName();
            Log.d(TAG, "API " + popApiName + " Pop from Retry Stack");
        }
        int retryCounter = CloudMessagePreferenceManager.getInstance().getTotalRetryCounter();
        if (retryStackAdapterHelper.isEmpty() && retryCounter > 0 && isEndOfCallFlow(request, callFlow)) {
            Log.d(TAG, "end of call flow. stack is empty. reset the counter to 0");
            CloudMessagePreferenceManager.getInstance().saveTotalRetryCounter(0);
        }
        if (request.getClass().getSimpleName().equalsIgnoreCase(RequestAccount.class.getSimpleName())) {
            Log.d(TAG, "RequestAccount request has no happy path in strategy");
            return;
        }
        Log.d(TAG, "Proceeding Flow:: " + callFlow);
        EnumProvision.ProvisionEventType event = findEvent(request, callFlow, iCloudMessageManagerHelper);
        if (event != null) {
            controller.updateDelay(event.getId(), delay);
            return;
        }
        Log.e(TAG, "event is null. end of call flow");
        if (!retryStackAdapterHelper.isEmpty()) {
            String retryAPIName = "Retry API:: is null";
            IHttpAPICommonInterface retry = retryStackAdapterHelper.getLastFailedRequest();
            if (retry != null) {
                retryAPIName = "Retry API:: " + retry.getClass().getSimpleName();
                retryStackAdapterHelper.retryApi(retry, controller, iCloudMessageManagerHelper, retryStackAdapterHelper);
            }
            Log.d(TAG, "stack is NOT empty, " + retryAPIName);
            return;
        }
        Log.d(TAG, "stack is empty. reset the counter to 0");
        CloudMessagePreferenceManager.getInstance().saveTotalRetryCounter(0);
    }

    private static boolean isEndOfCallFlow(IHttpAPICommonInterface request, String callFlow) {
        String requestName = request.getClass().getSimpleName();
        if (requestName.equalsIgnoreCase(ReqSession.class.getSimpleName())) {
            return true;
        }
        if ((!requestName.equalsIgnoreCase(RequestAccount.class.getSimpleName()) || !ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER.equalsIgnoreCase(callFlow)) && !requestName.equalsIgnoreCase(RequestCreateAccount.class.getSimpleName()) && !requestName.equalsIgnoreCase(RequestPat.class.getSimpleName())) {
            return false;
        }
        return true;
    }

    private static EnumProvision.ProvisionEventType findEvent(IHttpAPICommonInterface api, String callFlow, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        List<SuccessCallFlow> savedCallFlow;
        if (!(TextUtils.isEmpty(callFlow) || iCloudMessageManagerHelper.getSuccessfullCallFlowTranslator() == null || (savedCallFlow = iCloudMessageManagerHelper.getSuccessfullCallFlowTranslator().get(api.getClass())) == null)) {
            for (SuccessCallFlow flow : savedCallFlow) {
                if (callFlow.equals(flow.mFlow)) {
                    return flow.mProvisionEventType;
                }
            }
        }
        return null;
    }
}
