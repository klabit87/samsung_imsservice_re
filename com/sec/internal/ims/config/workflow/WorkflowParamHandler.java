package com.sec.internal.ims.config.workflow;

import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorkflowParamHandler {
    protected static final String CHARSET = "utf-8";
    private static final String GC_ACS_URL = "http://rcs-acs-mccXXX.jibe.google.com";
    private static final String LOG_TAG = WorkflowParamHandler.class.getSimpleName();
    protected int mPhoneId;
    protected ITelephonyAdapter mTelephony;
    protected WorkflowBase mWorkflowBase;

    public WorkflowParamHandler(WorkflowBase base, int phoneId, ITelephonyAdapter telephonyAdapter) {
        this.mWorkflowBase = base;
        this.mPhoneId = phoneId;
        this.mTelephony = telephonyAdapter;
    }

    /* access modifiers changed from: protected */
    public String initUrl() throws NoInitialDataException {
        Map<String, String> info = new HashMap<>();
        getMccMncInfo(info);
        return buildUrl(info);
    }

    /* access modifiers changed from: protected */
    public String initUrl(String fqdn) throws NoInitialDataException {
        if (CollectionUtils.isNullOrEmpty(fqdn)) {
            return initUrl();
        }
        return "http://" + fqdn;
    }

    /* access modifiers changed from: protected */
    public void getMccMncInfo(Map<String, String> info) throws NoInitialDataException {
        String imsi;
        info.put(ConfigConstants.URL.MCC_PNAME, this.mTelephony.getMcc());
        info.put(ConfigConstants.URL.MNC_PNAME, this.mTelephony.getMnc());
        if (TextUtils.isEmpty(info.get(ConfigConstants.URL.MCC_PNAME)) || TextUtils.isEmpty(info.get(ConfigConstants.URL.MNC_PNAME))) {
            throw new NoInitialDataException("MCC or MNC is not prepared");
        } else if (this.mWorkflowBase.mMno == Mno.SPRINT && (imsi = this.mTelephony.getImsi()) != null && imsi.length() >= 6) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "mcc, mnc from imsi");
            info.put(ConfigConstants.URL.MCC_PNAME, imsi.substring(0, 3));
            info.put(ConfigConstants.URL.MNC_PNAME, imsi.substring(3, 6));
        }
    }

    /* access modifiers changed from: protected */
    public String buildUrl(Map<String, String> info) throws NoInitialDataException {
        String url;
        String mcc = this.mTelephony.getMcc();
        String mnc = this.mTelephony.getMnc();
        if (mcc == null || mnc == null) {
            throw new NoInitialDataException("MCC or MNC is not prepared");
        }
        String url2 = ConfigUtil.getAcsCustomServerUrl(this.mWorkflowBase.mContext, this.mPhoneId);
        if (isConfigProxy()) {
            return ConfigConstants.URL.INTERNAL_CONFIG_PROXY_TEMPLATE.replace(ConfigConstants.URL.MCC_PVALUE, info.get(ConfigConstants.URL.MCC_PNAME)).replace(ConfigConstants.URL.MNC_PVALUE, info.get(ConfigConstants.URL.MNC_PNAME));
        }
        if (TextUtils.isEmpty(url2)) {
            if (this.mWorkflowBase.mMno == Mno.CMCC) {
                url = "http://config.rcs.chinamobile.com";
            } else {
                url = ConfigConstants.URL.CONFIG_TEMPLATE.replace(ConfigConstants.URL.MCC_PVALUE, info.get(ConfigConstants.URL.MCC_PNAME)).replace(ConfigConstants.URL.MNC_PVALUE, info.get(ConfigConstants.URL.MNC_PNAME));
            }
            checkUrlConnection(url);
            return url;
        } else if (url2.equals(GC_ACS_URL)) {
            return GC_ACS_URL.replace("XXX", mcc);
        } else {
            return url2;
        }
    }

    /* access modifiers changed from: protected */
    public void checkUrlConnection(String url) throws NoInitialDataException {
        InetAddress netAddr;
        if (this.mWorkflowBase.mMno == Mno.ATT) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "skip to checkUrlConnection");
            return;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "checkUrlConnection: url: " + url.replaceFirst("https?://", ""));
        try {
            if (this.mWorkflowBase.mMno != Mno.VZW || this.mWorkflowBase.mNetwork == null) {
                netAddr = InetAddress.getByName(url.replaceFirst("https?://", ""));
            } else {
                netAddr = this.mWorkflowBase.mNetwork.getByName(url.replaceFirst("https?://", ""));
            }
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "addr: " + netAddr.toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new NoInitialDataException("connection is not prepared");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isConfigProxy() {
        if (ConfigUtil.getAutoconfigSourceWithFeature(this.mWorkflowBase.mContext, this.mPhoneId, 0) != 1) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "config proxy is disabled.");
            return false;
        }
        int retry = 0;
        while (retry < 15) {
            try {
                IHttpAdapter http = new HttpAdapter(this.mPhoneId);
                http.open(ConfigConstants.URL.INTERNAL_CONFIG_PROXY_AUTHORITY);
                IHttpAdapter.Response response = http.request();
                http.close();
                if (response != null && response.getStatusCode() == 200 && response.getBody() != null && new String(response.getBody(), CHARSET).compareToIgnoreCase(ConfigConstants.KEY.INTERNAL_CONFIG_PROXY_AUTHORITY) == 0) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mWorkflowBase.sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            retry++;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "config proxy is enabled and got exception (retry: " + retry + ")");
        return true;
    }

    /* access modifiers changed from: protected */
    public String getModelInfoFromBuildVersion(String modelName, String swVersion, int maxLength, boolean fromEnd) {
        if (!TextUtils.isEmpty(modelName)) {
            String[] elements = modelName.split("-");
            if (elements.length == 2 && elements[1] != null && !elements[1].isEmpty()) {
                String model = elements[1];
                if (swVersion.startsWith(model) && swVersion.length() > model.length()) {
                    swVersion = swVersion.substring(model.length());
                }
            }
        }
        if (swVersion.length() <= maxLength) {
            return swVersion;
        }
        if (!fromEnd) {
            return swVersion.substring(0, maxLength - 1);
        }
        int len = swVersion.length();
        return swVersion.substring(len - maxLength, len);
    }

    /* access modifiers changed from: protected */
    public boolean isSupportCarrierVersion() {
        return SimUtil.isSupportCarrierVersion(this.mPhoneId);
    }

    /* access modifiers changed from: protected */
    public String getModelInfoFromCarrierVersion(String modelName, String swVersion, int maxLength, boolean fromEnd) {
        String version = getModelInfoFromBuildVersion(modelName, swVersion, maxLength, fromEnd);
        String rcsConfigMark = ((IConfigModule) this.mWorkflowBase.mModuleHandler).getRcsConfigMark(this.mPhoneId);
        if (!TextUtils.isEmpty(rcsConfigMark)) {
            if (isSupportCarrierVersion()) {
                version = version + rcsConfigMark;
            } else {
                version = version + "om";
            }
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "terminal version [" + version + "] : adds [" + rcsConfigMark + "] to terminal version");
        return version;
    }

    /* access modifiers changed from: protected */
    public String encodeRFC3986(String value) {
        try {
            return URLEncoder.encode(value, CHARSET).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, e.toString());
            e.printStackTrace();
            return value;
        }
    }

    /* access modifiers changed from: protected */
    public String encodeRFC7254(String value) {
        String result = value;
        String last = "0";
        if (TextUtils.isEmpty(value)) {
            return result;
        }
        if (value.length() > 14) {
            last = value.substring(14);
        }
        return "urn%3agsma%3aimei%3a" + String.format("%s-%s-%s", new Object[]{value.substring(0, 8), value.substring(8, 14), last});
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getParsedXmlFromBody() {
        byte[] body = this.mWorkflowBase.mSharedInfo.getHttpResponse().getBody();
        if (body == null) {
            body = new String("").getBytes();
        }
        try {
            return this.mWorkflowBase.mXmlParser.parse(new String(body, CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRequiredAuthentication(Map<String, String> parsedXml) throws Exception {
        if (parsedXml == null) {
            throw new InvalidXmlException("no parsedXml data");
        } else if (parsedXml.get("root/vers/version") != null && parsedXml.get("root/vers/validity") != null) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isRequiredAuthentication: parsedXml need to contain version or validity item");
            if (this.mWorkflowBase.mCookieHandler.isCookie(this.mWorkflowBase.mSharedInfo.getHttpResponse())) {
                return true;
            }
            throw new UnknownStatusException("no body and no cookie, something wrong");
        }
    }

    /* access modifiers changed from: protected */
    public void parseParam(Map<String, String> parsedXml) {
        String userPwdPath = ConfigConstants.PATH.USERPWD;
        String userPwd = parsedXml.get(userPwdPath);
        if (TextUtils.isEmpty(userPwd)) {
            userPwdPath = ConfigConstants.PATH.USERPWD_UP20;
            userPwd = parsedXml.get(userPwdPath);
        }
        if (userPwd != null && !userPwd.isEmpty()) {
            String encryptParam = ConfigUtil.encryptParam(userPwd);
            String data = encryptParam;
            if (encryptParam != null) {
                parsedXml.put(userPwdPath, data);
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.s(str, i, "encrypt data: " + data);
            }
        }
        if (parsedXml.get(ConfigConstants.PATH.IM_MAX_SIZE) == null) {
            String maxSize = parsedXml.get(ConfigConstants.PATH.IM_MAX_SIZE_1_TO_1);
            if (maxSize != null) {
                String str2 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(str2, i2, "maxsize is empty, use it as maxsize1to1 value: " + maxSize);
                parsedXml.put(ConfigConstants.PATH.IM_MAX_SIZE, maxSize);
            } else {
                parsedXml.put(ConfigConstants.PATH.IM_MAX_SIZE, "");
                parsedXml.put(ConfigConstants.PATH.IM_MAX_SIZE_1_TO_1, "");
            }
        }
        checkSetToGS(parsedXml);
    }

    /* access modifiers changed from: protected */
    public void parseParamForAtt(Map<String, String> parsedXml) {
        String value = parsedXml.get("root/application/1/im/ftHTTPCSURI".toLowerCase(Locale.US));
        if (value != null && !value.toLowerCase(Locale.US).startsWith(OMAGlobalVariables.HTTP)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleFtHttpCsUriValue: FT_HTTP_CS_URI has invalid URL");
            parsedXml.put("root/application/1/im/ftHTTPCSURI".toLowerCase(Locale.US), "");
        }
        ConfigUtil.encryptParams(parsedXml, "root/application/1/im/ftHTTPCSUser", "root/application/1/im/ftHTTPCSPwd", "root/application/1/im/ftHTTPCSURI", "root/application/1/serviceproviderext/nms_url", "root/application/1/serviceproviderext/nc_url", "root/token/token");
        try {
            parsedXml.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, new String(this.mWorkflowBase.mSharedInfo.getHttpResponse().getBody(), CHARSET));
        } catch (UnsupportedEncodingException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Failed to put xml!");
            e.printStackTrace();
        }
        String size = parsedXml.get("root/application/1/im/ext/att/slmMaxRecipients".toLowerCase(Locale.US));
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "slmMaxRecipients: " + size);
        if (TextUtils.isEmpty(size)) {
            size = parsedXml.get("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US));
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "max_adhoc_group_size: " + size);
        }
        if (!TextUtils.isEmpty(size)) {
            parsedXml.put("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(Locale.US), size);
        }
    }

    /* access modifiers changed from: protected */
    public void parseParamForLocalFile(Map<String, String> parsedXml) {
        parsedXml.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, this.mWorkflowBase.mSharedInfo.getXml());
        if (!TextUtils.isEmpty(parsedXml.get("root/application/1/im/ext/att/slmMaxRecipients".toLowerCase(Locale.US)))) {
            parsedXml.put("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/ext/att/slmMaxRecipients".toLowerCase(Locale.US)));
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "Using slmMaxRecipients: " + parsedXml.get("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(Locale.US)));
        } else if (!TextUtils.isEmpty(parsedXml.get("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US)))) {
            parsedXml.put("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US)));
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "slmMaxRecipients is null. Using max_adhoc_group_size instead: " + parsedXml.get("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(Locale.US)));
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "slmMaxRecipients and max_adhoc_group_size is null");
        }
    }

    /* access modifiers changed from: protected */
    public void moveHttpParam(Map<String, String> parsedXml) {
        if (this.mWorkflowBase.mMno == Mno.TMOUS && !TextUtils.isEmpty(parsedXml.get("root/application/1/im/ext/max_adhoc_open_group_size"))) {
            parsedXml.put("root/application/1/im/ext/ftMSRPftWarnSize".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/ftWarnSize".toLowerCase(Locale.US)));
            parsedXml.put("root/application/1/im/ext/ftMSRPMaxSizeFileTr".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/MaxSizeFileTr".toLowerCase(Locale.US)));
            parsedXml.put("root/application/1/im/ext/ftMSRPMaxSizeFileTrIncoming".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/MaxSizeFileTrIncoming".toLowerCase(Locale.US)));
            parsedXml.put("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US)));
            parsedXml.put("root/application/1/im/ftWarnSize".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/ext/fthttpftwarnsize"));
            parsedXml.put("root/application/1/im/MaxSizeFileTr".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/ext/fthttpmaxsizefiletr"));
            parsedXml.put("root/application/1/im/MaxSizeFileTrIncoming".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/ext/fthttpmaxsizefiletrincoming"));
            parsedXml.put("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US), parsedXml.get("root/application/1/im/ext/max_adhoc_open_group_size"));
            parsedXml.remove("root/application/1/im/ext/fthttpftwarnsize");
            parsedXml.remove("root/application/1/im/ext/fthttpmaxsizefiletr");
            parsedXml.remove("root/application/1/im/ext/fthttpmaxsizefiletrincoming");
            parsedXml.remove("root/application/1/im/ext/max_adhoc_open_group_size");
        }
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getUserMessage(Map<String, String> data) {
        Map<String, String> message = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getKey().startsWith(ConfigConstants.PATH.MSG)) {
                message.put(entry.getKey(), entry.getValue());
            }
        }
        return message;
    }

    /* access modifiers changed from: protected */
    public boolean getUserAccept(Map<String, String> data) {
        boolean userAccept = true;
        boolean versionChange = this.mWorkflowBase.getVersion() != this.mWorkflowBase.getVersion(data);
        Map<String, String> msg = getUserMessage(data);
        if (msg.size() == 4 && versionChange) {
            userAccept = getUserAcceptWithDialog(msg);
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getUserAccept: userAccept: " + userAccept + " versionChange: " + versionChange);
        return userAccept;
    }

    /* access modifiers changed from: protected */
    public boolean getUserAcceptWithDialog(Map<String, String> msg) {
        this.mWorkflowBase.mPowerController.release();
        boolean userAccept = this.mWorkflowBase.mDialog.getAcceptReject(msg.get(ConfigConstants.PATH.MSG_TITLE), msg.get(ConfigConstants.PATH.MSG_MESSAGE), msg.get(ConfigConstants.PATH.MSG_ACCEPT_BUTTON), msg.get(ConfigConstants.PATH.MSG_REJECT_BUTTON), this.mPhoneId);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getUserAcceptWithDialog: userAccept: " + userAccept);
        this.mWorkflowBase.mPowerController.lock();
        return userAccept;
    }

    /* access modifiers changed from: protected */
    public void setOpModeWithUserAccept(boolean userAccept, Map<String, String> data, WorkflowBase.OpMode mode) {
        if (userAccept) {
            WorkflowBase workflowBase = this.mWorkflowBase;
            workflowBase.setOpMode(workflowBase.getOpMode(data), data);
            return;
        }
        this.mWorkflowBase.setOpMode(mode, (Map<String, String>) null);
    }

    /* access modifiers changed from: protected */
    public void checkSetToGS(Map<String, String> parsedXml) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "checkSetToGS:");
        setChatSettings(parsedXml);
        setGroupChatSettings(parsedXml);
        String slmAuth = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.STANDALONE_MSG_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(slmAuth)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "SlmAuth set to " + slmAuth);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/services/standaloneMsgAuth".toLowerCase(Locale.US), slmAuth);
            } else {
                parsedXml.put("root/application/1/services/standaloneMsgAuth".toLowerCase(Locale.US), slmAuth);
            }
        }
        String geoPushAuth = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.GEOPUSH_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(geoPushAuth)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "GeoPushAuth set to " + geoPushAuth);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/services/geolocPushAuth".toLowerCase(Locale.US), geoPushAuth);
            } else {
                parsedXml.put("root/application/1/services/geolocPushAuth".toLowerCase(Locale.US), geoPushAuth);
            }
        }
        setFtSettings(parsedXml);
        setUxSettings(parsedXml);
        setClientControlSettings(parsedXml);
        setCapabilitySettings(parsedXml);
    }

    private void setChatSettings(Map<String, String> parsedXml) {
        String chatAuth = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.CHAT_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(chatAuth)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "ChatAuth set to " + chatAuth);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/services/ChatAuth".toLowerCase(Locale.US), chatAuth);
            } else {
                parsedXml.put("root/application/1/services/ChatAuth".toLowerCase(Locale.US), chatAuth);
            }
        }
        String imSessionTimer = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.IM_SESSION_TIMER, this.mPhoneId);
        if (!TextUtils.isEmpty(imSessionTimer)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "ImSessionTimer set to " + imSessionTimer);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/im/TimerIdle".toLowerCase(Locale.US), imSessionTimer);
            } else {
                parsedXml.put("root/application/1/im/TimerIdle".toLowerCase(Locale.US), imSessionTimer);
            }
        }
    }

    private void setGroupChatSettings(Map<String, String> parsedXml) {
        String groupChatAuth = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.GROUP_CHAT_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(groupChatAuth)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "GroupChatAuth set to " + groupChatAuth);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/services/GroupChatAuth".toLowerCase(Locale.US), groupChatAuth);
            } else {
                parsedXml.put("root/application/1/services/GroupChatAuth".toLowerCase(Locale.US), groupChatAuth);
            }
        }
        String maxAdhocGroupSize = ConfigUtil.getSetting("max_adhoc_group_size", this.mPhoneId);
        if (!TextUtils.isEmpty(maxAdhocGroupSize)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "MaxAdhocGroupSize set to " + maxAdhocGroupSize);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US), maxAdhocGroupSize);
            } else {
                parsedXml.put("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US), maxAdhocGroupSize);
            }
        }
        String autoAccpetGroupChat = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.AUTO_ACCEPT_GROUP_CHAT, this.mPhoneId);
        if (!TextUtils.isEmpty(autoAccpetGroupChat)) {
            String str3 = LOG_TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str3, i3, "AutoAcceptGroupChat set to " + autoAccpetGroupChat);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/im/autacceptgroupchat".toLowerCase(Locale.US), autoAccpetGroupChat);
            } else {
                parsedXml.put("root/application/1/im/autacceptgroupchat".toLowerCase(Locale.US), autoAccpetGroupChat);
            }
        }
    }

    private void setFtSettings(Map<String, String> parsedXml) {
        String ftDefaultMech = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.FT_DEFAULT_MECH, this.mPhoneId);
        if (!TextUtils.isEmpty(ftDefaultMech)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "FtDefaultMech set to " + ftDefaultMech);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/im/ftDefaultMech".toLowerCase(Locale.US), ftDefaultMech);
            } else {
                parsedXml.put("root/application/1/im/ftDefaultMech".toLowerCase(Locale.US), ftDefaultMech);
            }
        }
    }

    private void setUxSettings(Map<String, String> parsedXml) {
        String messagingUx = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.MESSAGING_UX, this.mPhoneId);
        if (!TextUtils.isEmpty(messagingUx)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "MessagingUx set to " + messagingUx);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/ux/messagingUX".toLowerCase(Locale.US), messagingUx);
            } else {
                parsedXml.put("root/application/1/ux/messagingUX".toLowerCase(Locale.US), messagingUx);
            }
        }
        String userAliasAuth = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.USER_ALIAS_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(userAliasAuth)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "UserAliasAuth set to " + userAliasAuth);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/ux/userAliasAuth".toLowerCase(Locale.US), userAliasAuth);
            } else {
                parsedXml.put("root/application/1/ux/userAliasAuth".toLowerCase(Locale.US), userAliasAuth);
            }
        }
    }

    private void setClientControlSettings(Map<String, String> parsedXml) {
        String reconnectGuardTimer = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.RECONNECT_GUARD_TIMER, this.mPhoneId);
        if (!TextUtils.isEmpty(reconnectGuardTimer)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "ReconGuardTimer set to " + reconnectGuardTimer);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/clientControl/reconnectGuardTimer".toLowerCase(Locale.US), reconnectGuardTimer);
            } else {
                parsedXml.put("root/application/1/clientControl/reconnectGuardTimer".toLowerCase(Locale.US), reconnectGuardTimer);
            }
        }
        String maxOneToOneRecipients = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.MAX_1TO_MANY_RECIPIENTS, this.mPhoneId);
        if (!TextUtils.isEmpty(maxOneToOneRecipients)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "Max1ToManyRecipients set to " + maxOneToOneRecipients);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/clientControl/max1toManyRecipients".toLowerCase(Locale.US), maxOneToOneRecipients);
            } else {
                parsedXml.put("root/application/1/clientControl/max1toManyRecipients".toLowerCase(Locale.US), maxOneToOneRecipients);
            }
        }
    }

    private void setCapabilitySettings(Map<String, String> parsedXml) {
        String capabilityDefaultDisc = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.CAPABILITY_DISCOVERY_MECH, this.mPhoneId);
        if (!TextUtils.isEmpty(capabilityDefaultDisc)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "CapDiscMech set to " + capabilityDefaultDisc);
            if (parsedXml == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/capdiscovery/defaultdisc".toLowerCase(Locale.US), capabilityDefaultDisc);
            } else {
                parsedXml.put("root/application/1/capdiscovery/defaultdisc".toLowerCase(Locale.US), capabilityDefaultDisc);
            }
        }
    }
}
