package com.sec.internal.ims.cmstore.strategy;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.cmstore.adapter.DeviceConfigAdapterConstants;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.cmstore.adapters.JanskyProviderAdapter;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.OmaErrorKey;
import com.sec.internal.ims.cmstore.helper.TMOVariables;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.strategy.DefaultCloudMessageStrategy;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.log.IMSLog;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TMOCmStrategy extends DefaultCloudMessageStrategy {
    private static final String TAG = TMOCmStrategy.class.getSimpleName();
    private String FAX_API_VERSION = "";
    private String FAX_ROOT_URL = "";
    private String FAX_SERVICE = "";
    private String MSTORE_API_VERSION = "";
    private String MSTORE_SERVERROOT = "";
    private String MSTORE_STORE_NAME = "";
    private String WSG_URI = "";
    private JanskyProviderAdapter mJanskyProviderAdapter;

    public static class TmoAttributeNames extends AttributeNames {
        public static String Content_Duration = "Content-Duration";
        public static String EmailAddress = "EmailAddress";
        public static String VVMOn = "VVMOn";
        public static String call_disposition = "CallDisposition";
        public static String call_duration = "Call-Duration";
        public static String call_starttimestamp = "call-timestamp";
        public static String call_type = "Call-Type";
        public static String client_correlator = "ClientCorrelator";
        public static String content_type = "Content-Type";
        public static String contribution_id = "Contribution-ID";
        public static String conversation_id = "Conversation-ID";
        public static String disposition_original_message_iD = "DispositionOriginalMessageID";
        public static String disposition_original_to = "DispositionOriginalTo";
        public static String disposition_status = "DispositionStatus";
        public static String disposition_type = "DispositionType";
        public static String inreplyto_contribution_Id = "InReplyTo-Contribution-ID";
        public static String is_cpm_group = "Is-CPM-Group";
        public static String message_id = "Message-Id";
        public static String mime_version = "MIME-Version";
        public static String multipartContentType = "MultipartContentType";
        public static String old_pwd = "OLD_PWD";
        public static String participating_device = "participating-device";
        public static String pwd = "PWD";
        public static String report_requested = "ReportRequested";
        public static String udh = "UDH";
        public static String x_cns_greeting_type = "X-CNS-Greeting-Type";
    }

    public static class TmoHttpHeaderValues {
        public static String DEVICE_ID_VALUE = "";
        public static final String USER_AGENT_ID = "T-Mobile P20";
        public static String USER_AGENT_ID_VALUE = "";
    }

    TMOCmStrategy(Context context) {
        Log.d(TAG, "TMOCmStrategy");
        this.mStrategyType = DefaultCloudMessageStrategy.CmStrategyType.TMOUS;
        this.mProtocol = OMAGlobalVariables.HTTPS;
        this.mMaxSearch = 100;
        onOmaFlowInitStart();
        initSuccessfullCallFlowTranslator();
        initFailedCallFlowTranslator();
        onOmaFlowInitComplete();
        initStandardRetrySchedule();
        initMessageAttributeRegistration();
        this.mJanskyProviderAdapter = new JanskyProviderAdapter(context);
        getDeviceId(context);
        this.mMaxSearch = 100;
    }

    private void initSuccessfullCallFlowTranslator() {
        initOmaSuccessCommonFlow();
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(201, CloudMessageCreateAllObjects.class.getSimpleName(), VvmHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.VVM_CHANGE_SUCCEED.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageBulkDeletion.class.getSimpleName(), VvmHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPLOAD_GREETING.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageBulkDeletion.class.getSimpleName(), VvmHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPLOAD_GREETING.getId()));
    }

    private void initFailedCallFlowTranslator() {
        initOmaFailureCommonFlow();
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(401, CloudMessageCreateAllObjects.class.getSimpleName(), VvmHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CREDENTIAL_EXPIRED.getId()));
    }

    private void initStandardRetrySchedule() {
        this.mStandardRetrySchedule = new HashMap();
        this.mStandardRetrySchedule.put(0, 0);
        this.mStandardRetrySchedule.put(1, 5000);
        this.mStandardRetrySchedule.put(2, 10001);
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
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IS_CPM_GROUP, TmoAttributeNames.is_cpm_group);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TEXT_CONTENT, AttributeNames.textcontent);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTRIBUTION_ID, TmoAttributeNames.contribution_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONVERSATION_ID, TmoAttributeNames.conversation_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IN_REPLY_TO_CONTRIBUTION_ID, TmoAttributeNames.inreplyto_contribution_Id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.UDH, TmoAttributeNames.udh);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_DISPOSITION, TmoAttributeNames.call_disposition);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_DURATION, TmoAttributeNames.call_duration);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_STARTTIMESTAMP, TmoAttributeNames.call_starttimestamp);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_TYPE, TmoAttributeNames.call_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.PARTICIPATING_DEVICE, TmoAttributeNames.participating_device);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_TYPE, TmoAttributeNames.disposition_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_STATUS, TmoAttributeNames.disposition_status);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID, TmoAttributeNames.disposition_original_message_iD);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO, TmoAttributeNames.disposition_original_to);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MULTIPARTCONTENTTYPE, TmoAttributeNames.multipartContentType);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MESSAGE_ID, TmoAttributeNames.message_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CLIENT_CORRELATOR, TmoAttributeNames.client_correlator);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.REPORT_REQUESTED, TmoAttributeNames.report_requested);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTENT_TYPE, TmoAttributeNames.content_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MIME_VERSION, TmoAttributeNames.mime_version);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.PWD, TmoAttributeNames.pwd);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.OLD_PWD, TmoAttributeNames.old_pwd);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.X_CNS_GREETING_TYPE, TmoAttributeNames.x_cns_greeting_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTENT_DURATION, TmoAttributeNames.Content_Duration);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.EMAILADDRESS, TmoAttributeNames.EmailAddress);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.VVMOn, TmoAttributeNames.VVMOn);
        this.mMessageAttributeRegistration = Collections.unmodifiableMap(this.mMessageAttributeRegistration);
    }

    public String getValidTokenByLine(String linenum) {
        String returnedToken = this.mJanskyProviderAdapter.getSIT(linenum);
        if (TextUtils.isEmpty(returnedToken)) {
            return "";
        }
        return "SIT " + returnedToken;
    }

    public String getNmsHost() {
        return this.MSTORE_SERVERROOT;
    }

    public String getFaxServerRoot() {
        return this.FAX_ROOT_URL;
    }

    public String getFaxApiVersion() {
        return this.FAX_API_VERSION;
    }

    public String getFaxServiceName() {
        return this.FAX_SERVICE;
    }

    public String getOMAApiVersion() {
        return this.MSTORE_API_VERSION;
    }

    public String getStoreName() {
        return this.MSTORE_STORE_NAME;
    }

    public String getNativeLine() {
        return this.mJanskyProviderAdapter.getNativeLine();
    }

    public DefaultCloudMessageStrategy.NmsNotificationType makeParamNotificationType(String pnsType, String pnsSubtype) {
        if ("SMS".equalsIgnoreCase(pnsType) && "MOMT".equalsIgnoreCase(pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType("SMS", 3);
        }
        if ("MMS".equalsIgnoreCase(pnsType) && "MOMT".equalsIgnoreCase(pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType("MMS", 4);
        }
        if (isNotificationTypeChat(pnsType, pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType(CloudMessageProviderContract.DataTypes.CHAT, 1);
        }
        if (isNotificationTypeFt(pnsType, pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType("FT", 1);
        }
        if (TMOConstants.TmoGcmPnsVariables.RCS_PAGE.equalsIgnoreCase(pnsType) && "IMDN".equalsIgnoreCase(pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType("IMDN", 13);
        }
        if (isNotificationTypeVvmData(pnsType, pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType("VVMDATA", 17);
        }
        if (TMOConstants.TmoGcmPnsVariables.VM.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.VVG.equalsIgnoreCase(pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType(CloudMessageProviderContract.DataTypes.VVMGREETING, 18);
        }
        if (TMOConstants.TmoGcmPnsVariables.VM.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.VVME.equalsIgnoreCase(pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType(CloudMessageProviderContract.DataTypes.VVMPROFILE, 20);
        }
        if (TMOConstants.TmoGcmPnsVariables.VM.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.VVMP.equalsIgnoreCase(pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType(CloudMessageProviderContract.DataTypes.VVMPIN, 19);
        }
        if (TMOConstants.TmoGcmPnsVariables.FAX.equalsIgnoreCase(pnsType) && (TMOConstants.TmoGcmPnsVariables.FAX.equalsIgnoreCase(pnsSubtype) || TMOConstants.TmoGcmPnsVariables.FAX_D.equalsIgnoreCase(pnsSubtype))) {
            return new DefaultCloudMessageStrategy.NmsNotificationType("FAX", 21);
        }
        if (TMOConstants.TmoGcmPnsVariables.CALL.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.HISTORY.equalsIgnoreCase(pnsSubtype)) {
            return new DefaultCloudMessageStrategy.NmsNotificationType("CALLLOGDATA", 16);
        }
        if (!TMOConstants.TmoGcmPnsVariables.RCS_SESSION.equalsIgnoreCase(pnsType) || !"GSO".equalsIgnoreCase(pnsSubtype)) {
            return null;
        }
        return new DefaultCloudMessageStrategy.NmsNotificationType("GSO", 34);
    }

    private boolean isNotificationTypeChat(String pnsType, String pnsSubtype) {
        return (TMOConstants.TmoGcmPnsVariables.RCS_SESSION.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.CHAT.equalsIgnoreCase(pnsSubtype)) || (TMOConstants.TmoGcmPnsVariables.RCS_PAGE.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.CHAT.equalsIgnoreCase(pnsSubtype));
    }

    private boolean isNotificationTypeFt(String pnsType, String pnsSubtype) {
        return (TMOConstants.TmoGcmPnsVariables.RCS_SESSION.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.FILE_TRANSFER.equalsIgnoreCase(pnsSubtype)) || (TMOConstants.TmoGcmPnsVariables.RCS_PAGE.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.LMM.equalsIgnoreCase(pnsSubtype));
    }

    private boolean isNotificationTypeVvmData(String pnsType, String pnsSubtype) {
        return (TMOConstants.TmoGcmPnsVariables.VM.equalsIgnoreCase(pnsType) && "VVM".equalsIgnoreCase(pnsSubtype)) || (TMOConstants.TmoGcmPnsVariables.VM.equalsIgnoreCase(pnsType) && TMOConstants.TmoGcmPnsVariables.VMTT.equalsIgnoreCase(pnsSubtype));
    }

    public int getTypeUsingMessageContext(String value) {
        String str = TAG;
        Log.d(str, "getTypeUsingMessageContext value: " + value);
        if (value.equals(MessageContextValues.pagerMessage)) {
            return 3;
        }
        if (value.equals(MessageContextValues.multiMediaMessage)) {
            return 4;
        }
        if (value.equals(TMOConstants.TmoMessageContextValues.chatMessage)) {
            return 11;
        }
        if (value.equals(TMOConstants.TmoMessageContextValues.fileMessage)) {
            return 12;
        }
        if (value.equals(TMOConstants.TmoMessageContextValues.standaloneMessagePager)) {
            return 11;
        }
        if (value.equals(TMOConstants.TmoMessageContextValues.standaloneMessageLLM)) {
            return 12;
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
        if (value.equals(TMOConstants.TmoMessageContextValues.callListMessage)) {
            return 16;
        }
        if (value.equals(TMOConstants.TmoMessageContextValues.greetingvoice)) {
            return 18;
        }
        if (value.equals(TMOConstants.TmoMessageContextValues.gsomessage) || value.equals(TMOConstants.TmoMessageContextValues.gsosession)) {
            return 34;
        }
        return 0;
    }

    public void setDeviceConfigUsed(Map<String, String> config) {
        Map<String, String> map = config;
        String SITUrl = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.SIT_URL);
        Log.i(TAG, "setDeviceConfigUsed " + IMSLog.checker(SITUrl));
        if (!TextUtils.isEmpty(SITUrl)) {
            try {
                String substring = SITUrl.substring(new URL(SITUrl).getProtocol().length() + 3);
                this.MSTORE_SERVERROOT = substring;
                Log.i(TAG, substring);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        String faxUrl = map.get(DeviceConfigAdapterConstants.TmoFax.ROOT_URL);
        if (!TextUtils.isEmpty(faxUrl)) {
            Log.i(TAG, "faxRootUrl: " + IMSLog.checker(faxUrl));
            try {
                this.FAX_ROOT_URL = faxUrl.substring(new URL(faxUrl).getProtocol().length() + 3);
                Log.i(TAG, "FAX_ROOT_URL: " + this.FAX_ROOT_URL);
            } catch (MalformedURLException e2) {
                e2.printStackTrace();
            }
        }
        String configDetails = "";
        if (map.containsKey(DeviceConfigAdapterConstants.FAX_API_VERSION)) {
            this.FAX_API_VERSION = map.get(DeviceConfigAdapterConstants.FAX_API_VERSION);
            configDetails = configDetails + "FAX_API_VERSION: " + this.FAX_API_VERSION;
        }
        if (map.containsKey(DeviceConfigAdapterConstants.TmoFax.SERVICE_NAME)) {
            this.FAX_SERVICE = map.get(DeviceConfigAdapterConstants.TmoFax.SERVICE_NAME);
            configDetails = configDetails + " FAX_SERVICE: " + this.FAX_SERVICE;
        }
        if (map.containsKey(DeviceConfigAdapterConstants.TmoMstoreServerValues.WSG_URI)) {
            this.WSG_URI = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.WSG_URI);
            configDetails = configDetails + " WSG_URI: " + this.WSG_URI;
        }
        if (map.containsKey(DeviceConfigAdapterConstants.TmoMstoreServerValues.API_VERSION)) {
            this.MSTORE_API_VERSION = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.API_VERSION);
            configDetails = configDetails + " MSTORE_API_VERSION: " + this.MSTORE_API_VERSION;
        }
        if (map.containsKey(DeviceConfigAdapterConstants.TmoMstoreServerValues.STORE_NAME)) {
            this.MSTORE_STORE_NAME = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.STORE_NAME);
            configDetails = configDetails + " MSTORE_STORE_NAME: " + this.MSTORE_STORE_NAME;
        }
        String maxBulkDelete = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.MAX_BULK_DELETE);
        if (!TextUtils.isEmpty(maxBulkDelete)) {
            Integer parsedValue = Integer.valueOf(maxBulkDelete);
            this.mMaxBulkDelete = parsedValue == null ? this.mMaxBulkDelete : parsedValue.intValue();
            configDetails = configDetails + " MAX_BULK_DELETE : " + this.mMaxBulkDelete;
        }
        String maxSearch = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.MAX_SEARCH);
        if (!TextUtils.isEmpty(maxSearch)) {
            Integer parsedValue2 = Integer.valueOf(maxSearch);
            this.mMaxSearch = parsedValue2 == null ? this.mMaxSearch : parsedValue2.intValue();
            configDetails = configDetails + " MAX_SEARCH : " + this.mMaxSearch;
        }
        Log.i(TAG, configDetails);
        String configDetails2 = "";
        String callId = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.CALL_HISTORY);
        if (!TextUtils.isEmpty(callId)) {
            TMOVariables.TmoMessageFolderId.mCallHistory = callId;
            configDetails2 = configDetails2 + " mCallHistory : " + callId;
        }
        String faxId = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.MEDIA_FAX);
        if (!TextUtils.isEmpty(faxId)) {
            TMOVariables.TmoMessageFolderId.mMediaFax = faxId;
            configDetails2 = configDetails2 + " mMediaFax : " + faxId;
        }
        String rcsId = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.RCS_MESSAGE_STORE);
        if (!TextUtils.isEmpty(rcsId)) {
            TMOVariables.TmoMessageFolderId.mRCSMessageStore = rcsId;
            configDetails2 = configDetails2 + " mRCSMessageStore : " + rcsId;
        }
        String greetingId = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_GREETINGS);
        if (!TextUtils.isEmpty(greetingId)) {
            TMOVariables.TmoMessageFolderId.mVVMailGreeting = greetingId;
            configDetails2 = configDetails2 + " mVVMailGreeting : " + greetingId;
        }
        String vmId = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_INBOX);
        if (!TextUtils.isEmpty(vmId)) {
            TMOVariables.TmoMessageFolderId.mVVMailInbox = vmId;
            configDetails2 = configDetails2 + " mVVMailInbox : " + vmId;
        }
        Log.i(TAG, "TmoMessageFolderId values: " + configDetails2);
        String calllog = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.CALL_LOG);
        if (!TextUtils.isEmpty(calllog)) {
            Log.i(TAG, "calllog " + calllog);
            TMOVariables.TmoMessageSyncPeriod.CALL_LOG = TimeUnit.DAYS.toMillis(Long.valueOf(calllog).longValue());
        } else {
            TMOVariables.TmoMessageSyncPeriod.CALL_LOG = TimeUnit.DAYS.toMillis(7);
        }
        String configDetails3 = "" + " CALL_LOG " + TMOVariables.TmoMessageSyncPeriod.CALL_LOG;
        String msg = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.MESSAGES);
        if (!TextUtils.isEmpty(msg)) {
            Log.i(TAG, "msg " + msg);
            TMOVariables.TmoMessageSyncPeriod.MESSAGE = TimeUnit.DAYS.toMillis(Long.valueOf(msg).longValue());
        } else {
            TMOVariables.TmoMessageSyncPeriod.MESSAGE = TimeUnit.DAYS.toMillis(7);
        }
        String configDetails4 = configDetails3 + " MESSAGE " + TMOVariables.TmoMessageSyncPeriod.MESSAGE;
        String vvm = map.get("VVM");
        if (!TextUtils.isEmpty(vvm)) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            String str2 = SITUrl;
            sb.append("vvm ");
            sb.append(vvm);
            Log.i(str, sb.toString());
            TMOVariables.TmoMessageSyncPeriod.VVM = TimeUnit.DAYS.toMillis(Long.valueOf(vvm).longValue());
        } else {
            TMOVariables.TmoMessageSyncPeriod.VVM = TimeUnit.DAYS.toMillis(7);
        }
        String configDetails5 = configDetails4 + " VVM " + TMOVariables.TmoMessageSyncPeriod.VVM;
        String fax = map.get("FAX");
        if (!TextUtils.isEmpty(fax)) {
            String str3 = TAG;
            StringBuilder sb2 = new StringBuilder();
            String str4 = vvm;
            sb2.append("fax ");
            sb2.append(fax);
            Log.i(str3, sb2.toString());
            TMOVariables.TmoMessageSyncPeriod.FAX = TimeUnit.DAYS.toMillis(Long.valueOf(fax).longValue());
        } else {
            Long l = 7L;
            TMOVariables.TmoMessageSyncPeriod.FAX = TimeUnit.DAYS.toMillis(l.longValue());
        }
        String configDetails6 = configDetails5 + " FAX " + TMOVariables.TmoMessageSyncPeriod.FAX;
        String greeting = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.VVM_GREETING);
        if (!TextUtils.isEmpty(greeting)) {
            String str5 = TAG;
            StringBuilder sb3 = new StringBuilder();
            String str6 = fax;
            sb3.append("greeting ");
            sb3.append(greeting);
            Log.i(str5, sb3.toString());
            TMOVariables.TmoMessageSyncPeriod.GREETING = TimeUnit.DAYS.toMillis(Long.valueOf(greeting).longValue());
        } else {
            Long l2 = 180L;
            TMOVariables.TmoMessageSyncPeriod.GREETING = TimeUnit.DAYS.toMillis(l2.longValue());
        }
        String configDetails7 = configDetails6 + " GREETING " + TMOVariables.TmoMessageSyncPeriod.GREETING;
        Log.i(TAG, "TmoMessageSyncPeriod values: " + configDetails7);
        String pushSyncDelay = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.PUSH_SYNC_DELAY);
        if (!TextUtils.isEmpty(pushSyncDelay)) {
            String str7 = TAG;
            StringBuilder sb4 = new StringBuilder();
            String str8 = configDetails7;
            sb4.append("pushSyncDelay ");
            sb4.append(pushSyncDelay);
            Log.i(str7, sb4.toString());
            TMOVariables.TmoMessageSyncPeriod.PUSH_SYNC_DELAY = Long.valueOf(pushSyncDelay).longValue();
            return;
        }
        TMOVariables.TmoMessageSyncPeriod.PUSH_SYNC_DELAY = 32000;
    }

    public boolean shouldEnableNetAPIWorking(boolean mIsNetworkValid, boolean mIsDefaultMsgAppNative, boolean mIsUserDeleteAccount, boolean mIsProvisionSuccess) {
        return mIsNetworkValid;
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierStrategyDiffFromCommonRuleByCode(IAPICallFlowListener callFlowListener, IHttpAPICommonInterface api, int statusCode) {
        if (statusCode != 401) {
            return false;
        }
        String str = TAG;
        Log.i(str, "API[" + api.getClass().getSimpleName() + "], 401, CREDENTIAL_EXPIRED");
        callFlowListener.onFailedEvent(OMASyncEventType.CREDENTIAL_EXPIRED.getId(), (Object) null);
        return true;
    }

    public boolean shouldEnableNetAPIPutFlag(String appType) {
        if (CloudMessageProviderContract.ApplicationTypes.RCSDATA.equalsIgnoreCase(appType)) {
            return false;
        }
        return true;
    }

    public boolean isValidOMARequestUrl() {
        if (!TextUtils.isEmpty(this.MSTORE_API_VERSION) && !TextUtils.isEmpty(this.MSTORE_STORE_NAME) && !TextUtils.isEmpty(this.MSTORE_SERVERROOT)) {
            return true;
        }
        Log.d(TAG, "isValidOMARequestUrl: false");
        return false;
    }

    private void getDeviceId(Context context) {
        TmoHttpHeaderValues.DEVICE_ID_VALUE = Util.getImei(context);
    }

    private void updateUserAgentIDHeader() {
        TmoHttpHeaderValues.USER_AGENT_ID_VALUE = TmoHttpHeaderValues.USER_AGENT_ID + ' ' + Build.VERSION.INCREMENTAL + ' ' + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ' ' + NSDSNamespaces.NSDSSettings.OS + ' ' + Build.VERSION.RELEASE + ' ' + Build.MODEL;
    }

    public void updateHTTPHeader() {
        updateUserAgentIDHeader();
    }
}
