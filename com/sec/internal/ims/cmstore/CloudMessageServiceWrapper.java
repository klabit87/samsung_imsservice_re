package com.sec.internal.ims.cmstore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.ims.ICentralMsgStoreService;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.log.IMSLog;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class CloudMessageServiceWrapper {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CloudMessageServiceWrapper.class.getSimpleName();
    /* access modifiers changed from: private */
    public static boolean mCmsProfileEnabled = false;
    private static CloudMessageServiceWrapper sInstance;
    private ServiceConnection mCloudMessageConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String access$000 = CloudMessageServiceWrapper.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("service connect :");
            sb.append(className != null ? className.toString() : "");
            Log.d(access$000, sb.toString());
            ICentralMsgStoreService unused = CloudMessageServiceWrapper.this.mCloudMessageService = ICentralMsgStoreService.Stub.asInterface(service);
            if (CloudMessageServiceWrapper.mCmsProfileEnabled) {
                try {
                    CloudMessageServiceWrapper.this.notifyStatusChanged(CloudMessageProviderContract.SimStatusValue.SIM_READY, CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_ENABLE);
                } catch (RemoteException e) {
                    CloudMessageServiceWrapper.this.serviceNotBindYet();
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            String access$000 = CloudMessageServiceWrapper.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("service disconnect :");
            sb.append(className != null ? className.toString() : "");
            Log.d(access$000, sb.toString());
            ICentralMsgStoreService unused = CloudMessageServiceWrapper.this.mCloudMessageService = null;
        }
    };
    /* access modifiers changed from: private */
    public ICentralMsgStoreService mCloudMessageService = null;
    private final Context mContext;

    public CloudMessageServiceWrapper(Context context) {
        this.mContext = context;
        init();
    }

    public static synchronized CloudMessageServiceWrapper setCmsProfileEnabled(Context context, boolean isEnabled) {
        CloudMessageServiceWrapper cloudMessageServiceWrapper;
        synchronized (CloudMessageServiceWrapper.class) {
            String str = LOG_TAG;
            Log.d(str, "setCmsProfileEnabled: " + isEnabled);
            mCmsProfileEnabled = isEnabled;
            if (sInstance == null && isEnabled) {
                sInstance = new CloudMessageServiceWrapper(context);
            }
            cloudMessageServiceWrapper = sInstance;
        }
        return cloudMessageServiceWrapper;
    }

    /* access modifiers changed from: private */
    public void serviceNotBindYet() {
        Log.e(LOG_TAG, "Service is not binded yet");
    }

    private String getJsonString(String dataType, int message_id, String imdnId, String line) {
        JsonArray jsonArrayRowIds = new JsonArray();
        JsonObject jsobjct = new JsonObject();
        jsobjct.addProperty("type", dataType);
        jsobjct.addProperty("id", String.valueOf(message_id));
        jsobjct.addProperty(CloudMessageProviderContract.JsonData.CORRELATION_ID, String.valueOf(imdnId));
        jsobjct.addProperty("preferred_line", line);
        jsonArrayRowIds.add(jsobjct);
        return jsonArrayRowIds.toString();
    }

    private String getJsonStringMsgList(List<String> list) {
        JsonArray jsonArrayRowIds = new JsonArray();
        for (String id : list) {
            if (id != null) {
                JsonObject jsobjct = new JsonObject();
                jsobjct.addProperty("type", CloudMessageProviderContract.DataTypes.CHAT);
                jsobjct.addProperty("id", id);
                jsobjct.addProperty(CloudMessageProviderContract.JsonData.CORRELATION_ID, "");
                jsobjct.addProperty("preferred_line", "");
                jsonArrayRowIds.add(jsobjct);
            }
        }
        return jsonArrayRowIds.toString();
    }

    private String getJsonStringMsgImdnList(List<String> list) {
        JsonArray jsonArrayRowIds = new JsonArray();
        for (String id : list) {
            if (id != null) {
                JsonObject jsobjct = new JsonObject();
                jsobjct.addProperty("type", CloudMessageProviderContract.DataTypes.CHAT);
                jsobjct.addProperty(CloudMessageProviderContract.JsonData.CORRELATION_ID, id);
                jsobjct.addProperty("preferred_line", "");
                jsonArrayRowIds.add(jsobjct);
            }
        }
        return jsonArrayRowIds.toString();
    }

    private String getJsonStringChatIdList(List<String> list) {
        JsonArray jsonArrayRowIds = new JsonArray();
        for (String id : list) {
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty("type", CloudMessageProviderContract.DataTypes.CHAT);
            jsobjct.addProperty(CloudMessageProviderContract.JsonData.CHAT_ID, id);
            jsobjct.addProperty(CloudMessageProviderContract.JsonData.CORRELATION_ID, "");
            jsobjct.addProperty("preferred_line", "");
            jsonArrayRowIds.add(jsobjct);
        }
        return jsonArrayRowIds.toString();
    }

    public static synchronized CloudMessageServiceWrapper getInstance(Context context) {
        CloudMessageServiceWrapper cloudMessageServiceWrapper;
        synchronized (CloudMessageServiceWrapper.class) {
            String str = LOG_TAG;
            Log.d(str, "getInstance, mCmsProfileEnabled: " + mCmsProfileEnabled);
            if (sInstance == null && mCmsProfileEnabled) {
                sInstance = new CloudMessageServiceWrapper(context);
            }
            cloudMessageServiceWrapper = sInstance;
        }
        return cloudMessageServiceWrapper;
    }

    public void receiveRCSMessage(int message_id, String imdnId, String line) throws RemoteException {
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.receivedMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, getJsonString(CloudMessageProviderContract.DataTypes.CHAT, message_id, imdnId, line));
        } else {
            serviceNotBindYet();
        }
    }

    public void sentRCSMessage(int message_id, String imdnId, String line) throws RemoteException {
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.sentMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, getJsonString(CloudMessageProviderContract.DataTypes.CHAT, message_id, imdnId, line));
        } else {
            serviceNotBindYet();
        }
    }

    public void deleteRCSMessageListUsingMsgId(List<String> list) throws RemoteException {
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.deleteMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, getJsonStringMsgList(list));
        } else {
            serviceNotBindYet();
        }
    }

    public void deleteRCSMessageListUsingImdnId(List<String> list) throws RemoteException {
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.deleteMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, getJsonStringMsgImdnList(list));
        } else {
            serviceNotBindYet();
        }
    }

    public void deleteRCSMessageListUsingChatId(List<String> list) throws RemoteException {
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.deleteMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, getJsonStringChatIdList(list));
        } else {
            serviceNotBindYet();
        }
    }

    public void readRCSMessageList(List<String> list) throws RemoteException {
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.readMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, getJsonStringMsgList(list));
        } else {
            serviceNotBindYet();
        }
    }

    public void createParticipant(String chatId) throws RemoteException {
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.createParticipant(CloudMessageProviderContract.ApplicationTypes.RCSDATA, chatId);
        } else {
            serviceNotBindYet();
        }
    }

    public void createSession(String chatId) throws RemoteException {
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.createSession(CloudMessageProviderContract.ApplicationTypes.RCSDATA, chatId);
        } else {
            serviceNotBindYet();
        }
    }

    public void onRCSDbReady() throws RemoteException {
        Log.d(LOG_TAG, "onRCSDbReady: ");
        notifyStatusChanged(CloudMessageProviderContract.SimStatusValue.SIM_READY, CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_ENABLE);
    }

    public void onDisableCms() throws RemoteException {
        Log.d(LOG_TAG, "onDisableCms: ");
        notifyStatusChanged(CloudMessageProviderContract.SimStatusValue.SIM_READY, CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_DISABLE);
    }

    public void onSimRemoved() throws RemoteException {
        Log.d(LOG_TAG, "onSimRemoved: ");
        notifyStatusChanged(CloudMessageProviderContract.SimStatusValue.SIM_REMOVED, CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_DISABLE);
        mCmsProfileEnabled = false;
    }

    public void onImsRegistered(String lineNum) throws RemoteException {
        String str = LOG_TAG;
        Log.d(str, "onImsRegistered: " + IMSLog.checker(lineNum));
        ICentralMsgStoreService iCentralMsgStoreService = this.mCloudMessageService;
        if (iCentralMsgStoreService != null) {
            iCentralMsgStoreService.manualSync(CloudMessageProviderContract.ApplicationTypes.RCSDATA, lineNum);
        } else {
            serviceNotBindYet();
        }
    }

    private void init() {
        if (this.mCloudMessageService == null) {
            Log.d(LOG_TAG, "bind to cloud message service");
            Intent intent = new Intent(this.mContext, CloudMessageService.class);
            intent.putExtra("appName", this.mContext.getPackageName());
            this.mContext.bindService(intent, this.mCloudMessageConnection, 1);
        }
    }

    /* access modifiers changed from: private */
    public void notifyStatusChanged(String simSatus, String event) throws RemoteException {
        try {
            if (this.mCloudMessageService != null) {
                JSONObject jsonObjMsg = new JSONObject();
                jsonObjMsg.put(CloudMessageProviderContract.JsonParamTags.SIM_STATUS, simSatus);
                jsonObjMsg.put(CloudMessageProviderContract.JsonParamTags.CMS_PROFILE_EVENT, event);
                this.mCloudMessageService.onRCSDBReady(jsonObjMsg.toString());
                return;
            }
            serviceNotBindYet();
        } catch (SecurityException | JSONException e) {
            Log.e(LOG_TAG, "notifyStatusChanged Failed due to Exception");
        }
    }
}
