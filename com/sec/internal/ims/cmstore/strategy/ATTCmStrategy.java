package com.sec.internal.ims.cmstore.strategy;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqToken;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqZCode;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccountEligibility;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestCreateAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestDeleteAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestHUIToken;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestPat;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestTC;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorMsg;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorType;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.OmaErrorKey;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessCallFlow;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessfulCallHandling;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.DebugFlag;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLargeDataPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLongPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetActiveNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageUpdateNotificationChannelLifeTime;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualSubscription;
import com.sec.internal.ims.cmstore.strategy.DefaultCloudMessageStrategy;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.servicemodules.volte2.CallStateMachine;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.omanetapi.common.data.NotificationFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ATTCmStrategy extends DefaultCloudMessageStrategy {
    private static final String LOG_TAG = ATTCmStrategy.class.getSimpleName();
    private final String ATT_API_VERSION = "v1";
    private final String ATT_STORE_NAME = "base";
    private int mApiFailCount = 0;
    private IControllerCommonInterface mControllerOfLastFailedAPI = null;
    private Class<? extends IHttpAPICommonInterface> mLastFailedAPI = null;

    public static class ATTAttributeNames extends AttributeNames {
        public static String call_disposition = "CallDisposition";
        public static String call_duration = "CallDuration";
        public static String contribution_id = "Contribution-ID";
        public static String conversation_id = "Conversation-ID";
        public static String disposition_original_message_iD = "DispositionOriginalMessageID";
        public static String disposition_original_to = "DispositionOriginalTo";
        public static String disposition_status = "DispositionStatus";
        public static String disposition_type = "DispositionType";
        public static String inreplyto_contribution_Id = "InReplyTo-Contribution-ID";
        public static String is_cpm_group = "Is-CPM-Group";
        public static String is_open_group = "Is-OPEN-Group";
        public static String multipartContentType = "MultipartContentType";
        public static String udh = "UDH";
    }

    ATTCmStrategy() {
        Log.d(LOG_TAG, "ATTCmStrategy");
        this.mStrategyType = DefaultCloudMessageStrategy.CmStrategyType.ATT;
        this.mProtocol = OMAGlobalVariables.HTTPS;
        this.mContentType = "application/json";
        this.mNotificationFormat = NotificationFormat.JSON;
        onOmaFlowInitStart();
        initSuccessfulCallFlowTranslator();
        initFailedCallFlowTranslator();
        onOmaFlowInitComplete();
        initStandardRetrySchedule();
        initMessageAttributeRegistration();
        initOmaRetryVariables();
    }

    public String getValidTokenByLine(String linenum) {
        return "Bearer PAT_" + CloudMessagePreferenceManager.getInstance().getValidPAT();
    }

    public int getAdaptedRetrySchedule(int retryCounter) {
        if (this.mStandardRetrySchedule == null) {
            return 0;
        }
        if (DebugFlag.DEBUG_RETRY_TIMELINE_FLAG) {
            return DebugFlag.getRetryTimeLine(retryCounter);
        }
        int standardTimer = ((Integer) this.mStandardRetrySchedule.get(Integer.valueOf(retryCounter))).intValue();
        Random rand = new Random(System.currentTimeMillis());
        if (retryCounter == 0) {
            return ((rand.nextInt(61) + 0) * 1000) + standardTimer;
        }
        if (retryCounter == 1 || retryCounter == 2 || retryCounter == 3 || retryCounter == 4) {
            return (((int) Math.floor((double) (((float) standardTimer) * (rand.nextFloat() + 1.0f)))) / 1000) * 1000;
        }
        return standardTimer;
    }

    private void initOmaRetryVariables() {
        this.mApiFailCount = CloudMessagePreferenceManager.getInstance().getOmaRetryCounter();
        String str = LOG_TAG;
        Log.i(str, "OMA fail count is: " + this.mApiFailCount);
    }

    private void initSuccessfulCallFlowTranslator() {
        initProvisionSuccessfullCallFlowTranslator();
        initOMASuccessfulCallFlowTranslator();
    }

    private void initProvisionSuccessfullCallFlowTranslator() {
        this.mSuccessfullCallFlowTranslator = new HashMap();
        List<SuccessCallFlow> reqTokenFlow = new ArrayList<>();
        reqTokenFlow.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_SESSION_GEN));
        this.mSuccessfullCallFlowTranslator.put(ReqToken.class, reqTokenFlow);
        List<SuccessCallFlow> reqSessionFlow = new ArrayList<>();
        reqSessionFlow.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_ACCOUNT_ELIGIBILITY));
        reqSessionFlow.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_STEADY_STATE_REQ_HUIMSTOKEN, EnumProvision.ProvisionEventType.REQ_HUI_TOKEN));
        reqSessionFlow.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_DELETE_ACCOUNT, EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT));
        reqSessionFlow.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_CREATE_ACCOUNT, EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT));
        reqSessionFlow.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_GET_SVC_ACCOUNT, EnumProvision.ProvisionEventType.REQ_SERVICE_ACCOUNT));
        reqSessionFlow.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_GET_TC, EnumProvision.ProvisionEventType.REQ_GET_TC));
        this.mSuccessfullCallFlowTranslator.put(ReqSession.class, reqSessionFlow);
        List<SuccessCallFlow> requestAccountEligibilityFlow = new ArrayList<>();
        requestAccountEligibilityFlow.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_SERVICE_ACCOUNT));
        this.mSuccessfullCallFlowTranslator.put(RequestAccountEligibility.class, requestAccountEligibilityFlow);
        List<SuccessCallFlow> requestTCFlow = new ArrayList<>();
        requestTCFlow.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT));
        this.mSuccessfullCallFlowTranslator.put(RequestTC.class, requestTCFlow);
        List<SuccessCallFlow> requestCreateAccountFlow = new ArrayList<>();
        requestCreateAccountFlow.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_HUI_TOKEN));
        this.mSuccessfullCallFlowTranslator.put(RequestCreateAccount.class, requestCreateAccountFlow);
        List<SuccessCallFlow> requestMsSessionFlow = new ArrayList<>();
        requestMsSessionFlow.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_PAT));
        this.mSuccessfullCallFlowTranslator.put(RequestHUIToken.class, requestMsSessionFlow);
        List<SuccessCallFlow> requestPatFlow = new ArrayList<>();
        requestPatFlow.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.READY_PAT));
        this.mSuccessfullCallFlowTranslator.put(RequestPat.class, requestPatFlow);
        List<SuccessCallFlow> deleteAccountFlow = new ArrayList<>();
        deleteAccountFlow.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.DELETE_ACCOUNT_SUCCESS));
        this.mSuccessfullCallFlowTranslator.put(RequestDeleteAccount.class, deleteAccountFlow);
        this.mSuccessfullCallFlowTranslator = Collections.unmodifiableMap(this.mSuccessfullCallFlowTranslator);
    }

    private void initOMASuccessfulCallFlowTranslator() {
        initOmaSuccessCommonFlow();
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageCreateLongPolling.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(201, CloudMessageCreateNotificationChannels.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetIndividualNotificationChannelInfo.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageUpdateNotificationChannelLifeTime.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId()));
    }

    private void initFailedCallFlowTranslator() {
        initProvisionFailedCallFlowTranslator();
        initOMAFailedCallFlowTranslator();
    }

    private void initProvisionFailedCallFlowTranslator() {
        this.sErrorMsgsTranslator = new HashMap();
        this.sErrorMsgsTranslator = Collections.unmodifiableMap(this.sErrorMsgsTranslator);
        this.mFailedCallFlowTranslator = new HashMap();
        List<ErrorRule> errZcode = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(ReqZCode.class, errZcode);
        errZcode.add(new ErrorRule(ATTConstants.ATTErrorNames.ERROR_CODE_201, ErrorRule.RetryAttribute.RETRY_FORBIDDEN, EnumProvision.ProvisionEventType.ZCODE_ERROR_201.getId(), EnumProvision.ProvisionEventType.ZCODE_ERROR_201.getId(), new ErrorMsg(ErrorType.PROVISIONING, 0)));
        errZcode.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId(), EnumProvision.ProvisionEventType.AUTH_ERR.getId(), (ErrorMsg) null));
        errZcode.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.getId(), EnumProvision.ProvisionEventType.AUTH_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errToken = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(ReqToken.class, errToken);
        errToken.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.getId(), EnumProvision.ProvisionEventType.AUTH_ERR.getId(), (ErrorMsg) null));
        errToken.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_ATS_TOKEN.getId(), EnumProvision.ProvisionEventType.AUTH_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errReqCookie = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(ReqSession.class, errReqCookie);
        errReqCookie.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.AUTH_ERR.getId(), (ErrorMsg) null));
        errReqCookie.add(new ErrorRule(ATTConstants.ATTErrorNames.ERROR_CODE_201, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.getId(), EnumProvision.ProvisionEventType.AUTH_ERR.getId(), (ErrorMsg) null));
        errReqCookie.add(new ErrorRule(ATTConstants.ATTErrorNames.ERROR_CODE_202, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.getId(), EnumProvision.ProvisionEventType.AUTH_ERR.getId(), (ErrorMsg) null));
        errReqCookie.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.AUTH_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errEligibility = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(RequestAccountEligibility.class, errEligibility);
        ErrorMsg temp = new ErrorMsg(ErrorType.PROVISIONING_BLOCKED, 0);
        errEligibility.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errEligibility.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_PROVISION_SHUTDOWN, ErrorRule.RetryAttribute.RETRY_FORBIDDEN, -1, EnumProvision.ProvisionEventType.CPS_PROVISION_SHUTDOWN.getId(), temp));
        errEligibility.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_ACCOUNT_NOT_ELIGIBLE, ErrorRule.RetryAttribute.RETRY_FORBIDDEN, -1, EnumProvision.ProvisionEventType.ACCOUNT_NOT_ELIGIBLE.getId(), temp));
        errEligibility.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_ACCOUNT_ELIGIBILITY.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errCheckAccount = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(RequestAccount.class, errCheckAccount);
        ErrorMsg temp2 = new ErrorMsg(ErrorType.PROVISIONING_BLOCKED, 0);
        errCheckAccount.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errCheckAccount.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_PROVISION_SHUTDOWN, ErrorRule.RetryAttribute.RETRY_FORBIDDEN, -1, EnumProvision.ProvisionEventType.CPS_PROVISION_SHUTDOWN.getId(), temp2));
        errCheckAccount.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errCheckAccount.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_SERVICE_ACCOUNT.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errMsSession = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(RequestHUIToken.class, errMsSession);
        errMsSession.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_ENCORE_METASWITCH_ACCOUNT_NOT_PROVISIONED, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_ACCOUNT_ELIGIBILITY.getId(), EnumProvision.ProvisionEventType.ACCESS_ERR.getId(), (ErrorMsg) null));
        errMsSession.add(new ErrorRule(ATTConstants.ATTErrorNames.LAST_RETRY_CREATE_ACCOUNT, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.LAST_RETRY_CREATE_ACCOUNT.getId(), EnumProvision.ProvisionEventType.ACCESS_ERR.getId(), (ErrorMsg) null));
        errMsSession.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_HUI_JSON, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.ACCESS_ERR.getId(), (ErrorMsg) null));
        errMsSession.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.ACCESS_ERR.getId(), (ErrorMsg) null));
        errMsSession.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId(), EnumProvision.ProvisionEventType.ACCESS_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errTC = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(RequestTC.class, errTC);
        errTC.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errTC.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_PROVISION_SHUTDOWN, ErrorRule.RetryAttribute.RETRY_FORBIDDEN, EnumProvision.ProvisionEventType.CPS_PROVISION_SHUTDOWN.getId(), -1, (ErrorMsg) null));
        errTC.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_GET_TC.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errCreateAccount = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(RequestCreateAccount.class, errCreateAccount);
        errCreateAccount.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_TC_ERROR_1007, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_GET_TC.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errCreateAccount.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_TC_ERROR_1008, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_GET_TC.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errCreateAccount.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errCreateAccount.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errRequestDeleteAccount = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(RequestDeleteAccount.class, errRequestDeleteAccount);
        errRequestDeleteAccount.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.ordinal(), EnumProvision.ProvisionEventType.STOP_BACKUP_ERR.getId(), (ErrorMsg) null));
        errRequestDeleteAccount.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT.getId(), EnumProvision.ProvisionEventType.STOP_BACKUP_ERR.getId(), (ErrorMsg) null));
        List<ErrorRule> errRequestPat = new ArrayList<>();
        this.mFailedCallFlowTranslator.put(RequestPat.class, errRequestPat);
        errRequestPat.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.ordinal(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errRequestPat.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_SESSION_ID, ErrorRule.RetryAttribute.RETRY_ALLOW, EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        errRequestPat.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE, EnumProvision.ProvisionEventType.REQ_PAT.getId(), EnumProvision.ProvisionEventType.PROVISION_ERR.getId(), (ErrorMsg) null));
        this.mFailedCallFlowTranslator = Collections.unmodifiableMap(this.mFailedCallFlowTranslator);
    }

    /* access modifiers changed from: protected */
    public void initOmaFailureCommonFlow() {
    }

    private void initOMAFailedCallFlowTranslator() {
        initOmaFailureCommonFlow();
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(409, CloudMessageCreateLongPolling.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(CallStateMachine.ON_REINVITE_TIMER_EXPIRED, CloudMessageCreateLongPolling.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetIndividualNotificationChannelInfo.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetActiveNotificationChannels.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageCreateLargeDataPolling.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.DEFAULT.getId()));
    }

    private void initStandardRetrySchedule() {
        this.mStandardRetrySchedule = new HashMap();
        if (DebugFlag.DEBUG_RETRY_TIMELINE_FLAG) {
            this.mStandardRetrySchedule.put(0, 0);
            this.mStandardRetrySchedule.put(1, 5000);
            this.mStandardRetrySchedule.put(2, 10001);
            this.mStandardRetrySchedule.put(3, 10002);
            this.mStandardRetrySchedule.put(4, 10003);
            this.mStandardRetrySchedule.put(5, 10004);
        } else {
            this.mStandardRetrySchedule.put(0, 0);
            this.mStandardRetrySchedule.put(1, 300000);
            this.mStandardRetrySchedule.put(2, 1800000);
            this.mStandardRetrySchedule.put(3, 14400000);
            this.mStandardRetrySchedule.put(4, 43200000);
            this.mStandardRetrySchedule.put(5, 86400000);
        }
        this.mStandardRetrySchedule = Collections.unmodifiableMap(this.mStandardRetrySchedule);
    }

    private void initMessageAttributeRegistration() {
        this.mMessageAttributeRegistration = new HashMap();
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DATE, "Date");
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MESSAGE_CONTEXT, AttributeNames.message_context);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DIRECTION, "Direction");
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.FROM, AttributeNames.from);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TO, AttributeNames.to);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.BCC, AttributeNames.bcc);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CC, AttributeNames.cc);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.SUBJECT, AttributeNames.subject);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IS_CPM_GROUP, ATTAttributeNames.is_cpm_group);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IS_OPEN_GROUP, ATTAttributeNames.is_open_group);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TEXT_CONTENT, AttributeNames.textcontent);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTRIBUTION_ID, ATTAttributeNames.contribution_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONVERSATION_ID, ATTAttributeNames.conversation_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IN_REPLY_TO_CONTRIBUTION_ID, ATTAttributeNames.inreplyto_contribution_Id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.UDH, ATTAttributeNames.udh);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_DISPOSITION, ATTAttributeNames.call_disposition);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_DURATION, ATTAttributeNames.call_duration);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_TYPE, ATTAttributeNames.disposition_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_STATUS, ATTAttributeNames.disposition_status);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID, ATTAttributeNames.disposition_original_message_iD);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO, ATTAttributeNames.disposition_original_to);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MULTIPARTCONTENTTYPE, ATTAttributeNames.multipartContentType);
        this.mMessageAttributeRegistration = Collections.unmodifiableMap(this.mMessageAttributeRegistration);
    }

    public String getNmsHost() {
        if (!ATTGlobalVariables.isGcmReplacePolling()) {
            return CloudMessagePreferenceManager.getInstance().getNmsHost();
        }
        String nmsHost = CloudMessagePreferenceManager.getInstance().getNmsHost();
        String str = LOG_TAG;
        Log.d(str, "use host for gcm, NMS Host value=" + nmsHost);
        if (!TextUtils.isEmpty(nmsHost)) {
            return nmsHost;
        }
        String nmsHost2 = CloudMessagePreferenceManager.getInstance().getAcsNmsHost();
        if (TextUtils.isEmpty(nmsHost2)) {
            return ATTGlobalVariables.DEFAULT_NMS_HOST;
        }
        return nmsHost2;
    }

    public String getNcHost() {
        if (!ATTGlobalVariables.isGcmReplacePolling()) {
            return CloudMessagePreferenceManager.getInstance().getNcHost();
        }
        String ncHost = CloudMessagePreferenceManager.getInstance().getNcHost();
        if (TextUtils.isEmpty(ncHost)) {
            ncHost = ATTGlobalVariables.DEFAULT_PRODUCT_NC_HOST;
        }
        String str = LOG_TAG;
        Log.d(str, "NC Host value=" + ncHost);
        return ncHost;
    }

    public String getOMAApiVersion() {
        return "v1";
    }

    public String getStoreName() {
        return "base";
    }

    public String getNativeLine() {
        return CloudMessagePreferenceManager.getInstance().getUserTelCtn();
    }

    public int getTypeUsingMessageContext(String value) {
        if (value.equals(MessageContextValues.pagerMessage)) {
            return 3;
        }
        if (value.equals(MessageContextValues.multiMediaMessage)) {
            return 4;
        }
        if (value.equals(ATTConstants.ATTMessageContextValues.chatMessage)) {
            return 11;
        }
        if (value.equals(ATTConstants.ATTMessageContextValues.fileMessage)) {
            return 12;
        }
        if (value.equals(ATTConstants.ATTMessageContextValues.standaloneMessage)) {
            return 14;
        }
        if (value.equals("imdn-message")) {
            return 13;
        }
        if (value.equals(MessageContextValues.voiceMessage)) {
            return 17;
        }
        if (value.equals(MessageContextValues.faxMessage)) {
            return 21;
        }
        return 0;
    }

    public boolean shouldEnableNetAPIWorking(boolean mIsNetworkValid, boolean mIsDefaultMsgAppNative, boolean mIsUserDeleteAccount, boolean mIsProvisionSuccess) {
        return mIsNetworkValid && mIsDefaultMsgAppNative && !mIsUserDeleteAccount && mIsProvisionSuccess;
    }

    private void increaseFailedCount(IHttpAPICommonInterface api, IControllerCommonInterface controller) {
        if (api.getClass().equals(this.mLastFailedAPI)) {
            this.mApiFailCount++;
            Log.i(LOG_TAG, "failed count increment 1, failed count: " + this.mApiFailCount);
            CloudMessagePreferenceManager.getInstance().saveOmaRetryCounter(this.mApiFailCount);
            return;
        }
        this.mLastFailedAPI = api.getClass();
        this.mControllerOfLastFailedAPI = controller;
        Log.i(LOG_TAG, "fail count keep same[" + this.mApiFailCount + "], lastFailedAPI: " + this.mLastFailedAPI.getSimpleName() + ", currentFailedAPI: " + api.getClass().getSimpleName());
    }

    public void onOmaApiCredentialFailed(IControllerCommonInterface controller, INetAPIEventListener netAPIEventListener, IHttpAPICommonInterface api, int delaySecs) {
        controller.setOnApiSucceedOnceListener((OMANetAPIHandler.OnApiSucceedOnceListener) null);
        if (this.mApiFailCount >= getMaxRetryCounter()) {
            String str = LOG_TAG;
            Log.i(str, "OMA API failed " + this.mApiFailCount + " times before, OMA API retired more than " + getMaxRetryCounter() + " times, pop up error screen");
            clearOmaRetryVariables();
            netAPIEventListener.onOmaFailExceedMaxCount();
            return;
        }
        long delay = (long) getAdaptedRetrySchedule(this.mApiFailCount);
        if (delaySecs > 0) {
            delay = Math.max(delay, ((long) delaySecs) * 1000);
        }
        String str2 = LOG_TAG;
        Log.i(str2, "OMA API failed " + this.mApiFailCount + " times beforeGo ahead fallback to SessionGen after " + (delay / 1000) + " seconds");
        Message msg = new Message();
        msg.what = OMASyncEventType.CREDENTIAL_EXPIRED.getId();
        msg.obj = Long.valueOf(delay);
        controller.updateMessage(msg);
        increaseFailedCount(api, controller);
    }

    public void onOmaSuccess(IHttpAPICommonInterface api) {
        if (api.getClass().equals(this.mLastFailedAPI)) {
            clearOmaRetryVariables();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierStrategyBreakCommonRule(IHttpAPICommonInterface api, int statusCode) {
        Log.d(LOG_TAG, "isCarrierStrategyBreakCommonRule()");
        if ((api instanceof CloudMessageDeleteIndividualSubscription) && statusCode != 302) {
            return true;
        }
        if ((api instanceof CloudMessageDeleteIndividualChannel) && statusCode != 302) {
            return true;
        }
        if (!(api instanceof CloudMessageCreateLargeDataPolling)) {
            return false;
        }
        Log.d(LOG_TAG, "CloudMessageCreateLargeDataPolling, other status code");
        return true;
    }

    public IControllerCommonInterface getControllerOfLastFailedApi() {
        return this.mControllerOfLastFailedAPI;
    }

    public Class<? extends IHttpAPICommonInterface> getLastFailedApi() {
        return this.mLastFailedAPI;
    }

    private void clearOmaRetryVariables() {
        Log.i(LOG_TAG, "clear oma retry variables");
        this.mLastFailedAPI = null;
        this.mControllerOfLastFailedAPI = null;
        this.mApiFailCount = 0;
        CloudMessagePreferenceManager.getInstance().saveOmaRetryCounter(this.mApiFailCount);
    }

    public void clearOmaRetryData() {
        clearOmaRetryVariables();
    }
}
