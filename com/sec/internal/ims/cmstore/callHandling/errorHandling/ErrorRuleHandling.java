package com.sec.internal.ims.cmstore.callHandling.errorHandling;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.CloudMessagePreferenceConstants;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.util.List;

public class ErrorRuleHandling {
    private static final String TAG = ErrorRuleHandling.class.getSimpleName();

    public static void handleErrorCode(IControllerCommonInterface controller, IHttpAPICommonInterface api, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        handleErrorCode(controller, api, CommonErrorName.DEFAULT_ERROR_TYPE, 0, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    public static void handleErrorHeader(IControllerCommonInterface controller, IHttpAPICommonInterface api, String errorCode, int retryAfter, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        handleErrorCode(controller, api, errorCode, retryAfter, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    public static void handleErrorCode(IControllerCommonInterface controller, IHttpAPICommonInterface request, String errorCode, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        handleErrorCode(controller, request, errorCode, 0, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    private static void handleErrorCode(IControllerCommonInterface controller, IHttpAPICommonInterface request, String errorCode, int retryAfter, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        String str = TAG;
        Log.e(str, "retryAfter: " + retryAfter + " errorCode: " + errorCode);
        if (!TextUtils.isEmpty(errorCode)) {
            ErrorRule error = findErrorConfig(request, errorCode, iCloudMessageManagerHelper);
            if (error == null) {
                error = findErrorConfig(request, CommonErrorName.DEFAULT_ERROR_TYPE, iCloudMessageManagerHelper);
            }
            String str2 = TAG;
            Log.i(str2, "Failed API name: " + request.getClass().getSimpleName() + ", error code: " + error);
            if (error != null) {
                handleError(controller, request, error, retryAfter, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
            }
        }
    }

    public static void handleError(IControllerCommonInterface controller, IHttpAPICommonInterface request, ErrorRule error, int retryAfter, IRetryStackAdapterHelper retryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        if (request != null) {
            boolean retrybefore = retryStackAdapterHelper.checkRequestRetried(request);
            int totalCounter = iCloudMessageManagerHelper.getTotalRetryCounter();
            if (error.mRetryAttr == ErrorRule.RetryAttribute.RETRY_FORBIDDEN) {
                Log.e(TAG, "retry forbidden");
                controller.update(error.mFailEvent);
            } else if (retryStackAdapterHelper.isRetryTimesFinished()) {
                Log.e(TAG, "retry time finished");
                if (error.mFailEvent == OMASyncEventType.SYNC_ERR.getId() || error.mFailEvent == EnumProvision.ProvisionEventType.ACCESS_ERR.getId()) {
                    retryStackAdapterHelper.searchAndPush(request);
                    iCloudMessageManagerHelper.removeKey(CloudMessagePreferenceConstants.RETRY_TOTAL_COUNTER);
                    iCloudMessageManagerHelper.removeKey(CloudMessagePreferenceConstants.LAST_RETRY_TIME);
                    String str = TAG;
                    Log.i(str, "steady state error screen should be displayed. saving retry instance, total counter: " + iCloudMessageManagerHelper.getTotalRetryCounter());
                } else {
                    retryStackAdapterHelper.clearRetryHistory();
                    String str2 = TAG;
                    Log.i(str2, "clear retry stack and counter, total counter: " + iCloudMessageManagerHelper.getTotalRetryCounter());
                }
                controller.update(error.mFailEvent);
            } else {
                long delay = 0;
                if (retrybefore) {
                    increaseTotalRetryCounter(iCloudMessageManagerHelper);
                    totalCounter = iCloudMessageManagerHelper.getTotalRetryCounter();
                    delay = (long) iCloudMessageManagerHelper.getAdaptedRetrySchedule(totalCounter);
                    retryStackAdapterHelper.saveRetryLastFailedTime(System.currentTimeMillis());
                }
                String str3 = TAG;
                Log.i(str3, "RETRY LOGIC::delay from the schedule: " + delay + "RETRY LOGIC::next retry Counter=" + totalCounter + "RETRY LOGIC::retry event is " + error.mRetryAttr);
                retryStackAdapterHelper.searchAndPush(request);
                if (error.mRetryAttr == ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE && retryAfter > 0) {
                    delay = Math.max(delay, ((long) retryAfter) * 1000);
                }
                controller.updateDelayRetry(error.mRetryEvent, delay);
            }
        }
    }

    private static synchronized void increaseTotalRetryCounter(ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        synchronized (ErrorRuleHandling.class) {
            iCloudMessageManagerHelper.saveTotalRetryCounter(iCloudMessageManagerHelper.getTotalRetryCounter() + 1);
        }
    }

    private static ErrorRule findErrorConfig(IHttpAPICommonInterface api, String errorCode, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        List<ErrorRule> errors;
        if (!(iCloudMessageManagerHelper.getFailedCallFlowTranslator() == null || (errors = iCloudMessageManagerHelper.getFailedCallFlowTranslator().get(api.getClass())) == null)) {
            for (ErrorRule error : errors) {
                if (errorCode.equals(error.mErrorCode)) {
                    return error;
                }
            }
        }
        return null;
    }
}
