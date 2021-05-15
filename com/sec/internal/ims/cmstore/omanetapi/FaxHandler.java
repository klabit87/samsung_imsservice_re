package com.sec.internal.ims.cmstore.omanetapi;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.google.gson.GsonBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRuleHandling;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.FaxSerializer;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageSendFax;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.AttributeContent;
import com.sec.internal.omanetapi.nms.data.AttributeList;
import com.sec.internal.omanetapi.nms.data.Object;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FaxHandler extends Handler implements IAPICallFlowListener, IControllerCommonInterface {
    public static final int CREDENTIAL_EXPIRED = 256;
    private static final String FAX_MSG = "fax-message";
    private static final String MIME_VERSION = "1.0";
    private static String REPORT_REQUESTED = CloudMessageProviderContract.JsonData.TRUE;
    public static final int RETRIEVE_FAX = 777;
    private static final String SCHEME_URI_TEL = "tel";
    public static final int SEND_ERROR_NO_RETRY = 2;
    public static final int SEND_FAX = 0;
    private static final String TAG = FaxHandler.class.getSimpleName();
    private final BufferDBTranslation mBufferDbTranslation;
    public String mFaxLine;
    Object mFaxObject;
    public String mFilePath;
    private final ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final INetAPIEventListener mINetAPIEventListener;
    private final IRetryStackAdapterHelper mIRetryStackAdapterHelper;
    private String mIsimDomain;
    public String mToLine;
    private final RegistrantList mUpdateFromCloudRegistrants;

    private enum Direction {
        Out
    }

    public FaxHandler(Looper looper, Context context, OMANetAPIHandler netAPIHandler, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mUpdateFromCloudRegistrants = new RegistrantList();
        this.mFilePath = "";
        this.mFaxLine = "";
        this.mToLine = "";
        this.mIsimDomain = "";
        this.mFaxObject = null;
        this.mFaxObject = new Object();
        this.mBufferDbTranslation = new BufferDBTranslation(context, iCloudMessageManagerHelper);
        this.mINetAPIEventListener = netAPIHandler;
        this.mIRetryStackAdapterHelper = iRetryStackAdapterHelper;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
    }

    public void registerForUpdateFromCloud(Handler h, int what, Object obj) {
        this.mUpdateFromCloudRegistrants.add(new Registrant(h, what, obj));
    }

    private void notifyBufferDB(ParamOMAresponseforBufDB param) {
        if (param == null) {
            Log.e(TAG, "notifyBufferDB ParamOMAresponseforBufDB is null");
        }
        this.mUpdateFromCloudRegistrants.notifyRegistrants(new AsyncResult((Object) null, param, (Throwable) null));
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        removeMessages(msg.what);
        String str = TAG;
        Log.i(str, "message: " + msg.what);
        int i = msg.what;
        if (i == 0) {
            try {
                postFaxJsonRequest(this.mToLine, this.mFaxLine, HashManager.generateHash(new Timestamp(new Date().getTime()).toString()), this.mFilePath);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else if (i == 2) {
            notifyBufferDB((ParamOMAresponseforBufDB) obtainMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId(), new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.VVM_FAX_ERROR_WITH_NO_RETRY).setBufferDBChangeParam((BufferDBChangeParam) msg.obj).build()).obj);
        } else if (i == 256) {
            this.mINetAPIEventListener.onOmaAuthenticationFailed(new ParamOMAresponseforBufDB.Builder().setLine(this.mFaxLine).build(), 0);
        } else if (i == 777) {
            notifyBufferDB((ParamOMAresponseforBufDB) msg.obj);
        }
    }

    public void sendFaxUsingBufferDBTranslation(BufferDBChangeParam param) {
        String str = TAG;
        Log.i(str, "sendFaxUsingBufferDBTranslation param: " + param);
        ParamObjectUpload uploadParam = new ParamObjectUpload(this.mBufferDbTranslation.getFaxObjectPairFromCursor(param), param);
        try {
            ImsUri imsuri = ImsUri.parse(param.mLine);
            this.mFaxLine = param.mLine;
            param.mLine = imsuri.getMsisdn().replace("+", "");
        } catch (NullPointerException e) {
            String str2 = TAG;
            Log.e(str2, "null pointer: " + e.toString());
        }
        if (!TextUtils.isEmpty(param.mLine)) {
            HttpController.getInstance().execute(new CloudMessageSendFax(this, uploadParam, this.mICloudMessageManagerHelper));
        }
    }

    public void postFaxJsonRequest(String toMsisdn, String fromMsisdn, String messageId, String filePath) {
        String jsonBody = buildNMSObjectJson(toMsisdn, fromMsisdn, messageId);
        String str = TAG;
        Log.d(str, "postFaxJsonRequest(): toMsisdn: " + IMSLog.checker(toMsisdn) + " fromMsisdn: " + IMSLog.checker(fromMsisdn) + " jsonBody:" + jsonBody);
        List<HttpPostBody> multiParts = new ArrayList<>();
        HttpPostBody jsonPostBody = new HttpPostBody(HttpPostBody.CONTENT_DISPOSITION_FORM_DATA, "application/json", jsonBody);
        HttpPostBody httpPdfBody = new HttpPostBody("attachment;filename = \"fax.pdf\"", "application/pdf", getFileContentInBytes(filePath));
        multiParts.add(jsonPostBody);
        multiParts.add(httpPdfBody);
        HttpController.getInstance().execute(new CloudMessageSendFax(this, new ParamObjectUpload(new Pair<>(this.mFaxObject, new HttpPostBody("form-data;name=attachments", "multipart/mixed", multiParts)), new BufferDBChangeParam(21, -1, false, fromMsisdn)), this.mICloudMessageManagerHelper));
    }

    private String buildNMSObjectJson(String toMsisdn, String fromMsisdn, String messageId) {
        SimpleDateFormat sFormatOfName = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        sFormatOfName.setTimeZone(TimeZone.getTimeZone("UTC"));
        AttributeContent attributeContent = new AttributeContent(sFormatOfName.format(new Date(System.currentTimeMillis())), "fax-message", messageId, "1.0", Direction.Out.toString(), "12345", buildAddressUri(SCHEME_URI_TEL, fromMsisdn), new String[]{buildAddressUri(SCHEME_URI_TEL, toMsisdn)}, "Multipart/fax-message", REPORT_REQUESTED);
        AttributeList attributes = new AttributeList();
        AttributeTranslator trans = new AttributeTranslator(this.mICloudMessageManagerHelper);
        this.mFaxObject.parentFolderPath = "/Media Folder/FAX Media";
        this.mFaxObject.attributes = attributes;
        trans.setDate(createStringArr(attributeContent.date));
        trans.setMessageContext(createStringArr(attributeContent.messageContext));
        trans.setMessageId(createStringArr(attributeContent.messageId));
        trans.setMimeVersion(createStringArr(attributeContent.miMeVersion));
        trans.setDirection(createStringArr(attributeContent.direction));
        trans.setClientCorrelator(createStringArr(attributeContent.clientCorrelator));
        trans.setFrom(createStringArr(attributeContent.from));
        trans.setTo(attributeContent.to);
        trans.setContentType(createStringArr(attributeContent.contentType));
        trans.setReportRequested(createStringArr(attributeContent.reportRequested));
        this.mFaxObject.attributes = trans.getAttributeList();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Object.class, new FaxSerializer());
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create().toJson(this.mFaxObject, Object.class);
    }

    private String[] createStringArr(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return new String[]{str};
    }

    private String buildAddressUri(String scheme, String msisdn) {
        StringBuilder builder = new StringBuilder();
        builder.append(scheme + ":+");
        builder.append(msisdn);
        if ("sip".equals(scheme)) {
            builder.append("@");
            builder.append(this.mIsimDomain);
        }
        return builder.toString();
    }

    private byte[] getFileContentInBytes(String filePath) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream(filePath);
            byte[] buffer = new byte[256];
            int read = is2.read(buffer);
            String str = TAG;
            Log.i(str, "getRcsFilePayloadFromPath, bytes " + read);
            while (read >= 0) {
                baos.write(buffer, 0, read);
                read = is2.read(buffer);
            }
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is2.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            try {
                baos.close();
            } catch (IOException e4) {
                e4.printStackTrace();
            }
            if (is != null) {
                is.close();
            }
        } catch (Throwable th) {
            try {
                baos.close();
            } catch (IOException e5) {
                e5.printStackTrace();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            throw th;
        }
        String str2 = TAG;
        Log.i(str2, "getRcsFilePayloadFromPath, all bytes: " + baos.size());
        return Base64.encode(baos.toByteArray(), 2);
    }

    public void onGoToEvent(int event, Object param) {
    }

    public void onMoveOnToNext(IHttpAPICommonInterface request, Object param) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request, String callFlow) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request) {
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface request, int event, Object param) {
    }

    public void onFailedCall(IHttpAPICommonInterface request, String errorCode) {
        String str = TAG;
        Log.i(str, "onFailedCall: errorCode: " + errorCode);
        if (Integer.valueOf(errorCode).intValue() == 401) {
            ErrorRuleHandling.handleErrorCode(this, request, errorCode, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
        } else {
            update(2);
        }
    }

    public void onFailedCall(IHttpAPICommonInterface request, BufferDBChangeParam bufferDBInfo) {
        sendMessage(obtainMessage(2, bufferDBInfo));
    }

    public void onFailedCall(IHttpAPICommonInterface request) {
    }

    public void onFailedEvent(int event, Object param) {
    }

    public void onOverRequest(IHttpAPICommonInterface request, String errorCode, int retryAfter) {
        sendMessage(obtainMessage(OMASyncEventType.SELF_RETRY.getId(), Integer.valueOf(retryAfter)));
    }

    public void onFixedFlow(int event) {
        sendMessage(obtainMessage(event));
    }

    public void onFixedFlowWithMessage(Message msg) {
        if (!(msg == null || msg.obj == null)) {
            String str = TAG;
            Log.i(str, "onFixedFlowWithMessage message is " + ((ParamOMAresponseforBufDB) msg.obj).getActionType());
        }
        sendMessage(msg);
    }

    public void start() {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void stop() {
    }

    public boolean update(int eventType) {
        sendMessage(obtainMessage(eventType));
        return false;
    }

    public boolean updateMessage(Message msg) {
        return sendMessage(msg);
    }

    public boolean updateDelay(int eventType, long delay) {
        String str = TAG;
        Log.d(str, "updateDelay: eventType: " + eventType + " delay: " + delay);
        return sendMessageDelayed(obtainMessage(eventType), delay);
    }

    public boolean updateDelayRetry(int eventType, long delay) {
        return false;
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener listener) {
    }
}
