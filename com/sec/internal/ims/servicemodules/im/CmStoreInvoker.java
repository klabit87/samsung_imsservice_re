package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.util.Log;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.internal.ims.settings.RcsPolicySettings;
import java.lang.reflect.Method;
import java.util.List;

public class CmStoreInvoker {
    private static final String LOG_TAG = CmStoreInvoker.class.getSimpleName();
    Method mCldGetInstance = null;
    Class<?> mCldMsgServiceClass = null;
    Object mCldMsgServiceObj = null;
    ImModule mImModule;

    CmStoreInvoker(ImModule imModule) {
        this.mImModule = imModule;
    }

    private boolean isReady() {
        try {
            if (this.mCldMsgServiceClass == null) {
                this.mCldMsgServiceClass = Class.forName("com.sec.internal.ims.cmstore.CloudMessageServiceWrapper");
            }
            if (this.mCldGetInstance == null) {
                this.mCldGetInstance = this.mCldMsgServiceClass.getMethod("getInstance", new Class[]{Context.class});
            }
            if (this.mCldMsgServiceObj == null) {
                this.mCldMsgServiceObj = this.mCldGetInstance.invoke((Object) null, new Object[]{this.mImModule.getContext()});
            }
            if (this.mCldMsgServiceClass == null || this.mCldMsgServiceObj == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void onCreateSession(int phoneId, ImSession session) {
        if (isCmStoreEnabled(phoneId) && isReady()) {
            try {
                Method createSessionMethod = this.mCldMsgServiceClass.getMethod("createSession", new Class[]{String.class});
                Method createParticipantMethod = this.mCldMsgServiceClass.getMethod("createParticipant", new Class[]{String.class});
                ReflectionUtils.invoke(createSessionMethod, this.mCldMsgServiceObj, new Object[]{session.getChatId()});
                ReflectionUtils.invoke(createParticipantMethod, this.mCldMsgServiceObj, new Object[]{session.getChatId()});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    /* access modifiers changed from: protected */
    public synchronized void onReceiveRcsMessage(int phoneId, ImMessage msg) {
        if (isCmStoreEnabled(phoneId) && isReady()) {
            try {
                ReflectionUtils.invoke(this.mCldMsgServiceClass.getMethod("receiveRCSMessage", new Class[]{Integer.TYPE, String.class, String.class}), this.mCldMsgServiceObj, new Object[]{Integer.valueOf(msg.getId()), msg.getImdnId(), null});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    /* access modifiers changed from: protected */
    public synchronized void onReadRcsMessageList(int phoneId, List<String> list) {
        if (isCmStoreEnabled(phoneId) && isReady() && list != null) {
            try {
                String str = LOG_TAG;
                Log.i(str, "readMessagesforCloudSync: list " + list);
                ReflectionUtils.invoke(this.mCldMsgServiceClass.getMethod("readRCSMessageList", new Class[]{List.class}), this.mCldMsgServiceObj, new Object[]{list});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    /* access modifiers changed from: protected */
    public synchronized void onDeleteRcsMessagesUsingMsgId(List<String> list, boolean isLocalWipeout) {
        if (isCmStoreEnabled() && isReady() && !isLocalWipeout && list != null) {
            try {
                String str = LOG_TAG;
                Log.i(str, "deleteMessagesforCloudSyncUsingMsgId: list " + list);
                ReflectionUtils.invoke(this.mCldMsgServiceClass.getMethod("deleteRCSMessageListUsingMsgId", new Class[]{List.class}), this.mCldMsgServiceObj, new Object[]{list});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    /* access modifiers changed from: protected */
    public synchronized void onDeleteRcsMessagesUsingImdnId(List<String> list, boolean isLocalWipeout) {
        if (isCmStoreEnabled() && isReady() && !isLocalWipeout && list != null) {
            try {
                String str = LOG_TAG;
                Log.i(str, "deleteMessagesforCloudSyncUsingMsgId: list " + list);
                ReflectionUtils.invoke(this.mCldMsgServiceClass.getMethod("deleteRCSMessageListUsingImdnId", new Class[]{List.class}), this.mCldMsgServiceObj, new Object[]{list});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    /* access modifiers changed from: protected */
    public synchronized void onDeleteRcsMessagesUsingChatId(List<String> list, boolean isLocalWipeout) {
        if (isCmStoreEnabled() && isReady() && !isLocalWipeout && list != null) {
            try {
                String str = LOG_TAG;
                Log.i(str, "deleteMessagesforCloudSyncUsingChatId: list " + list);
                ReflectionUtils.invoke(this.mCldMsgServiceClass.getMethod("deleteRCSMessageListUsingChatId", new Class[]{List.class}), this.mCldMsgServiceObj, new Object[]{list});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private boolean isCmStoreEnabled() {
        return this.mImModule.getRcsStrategy() != null && this.mImModule.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CENTRAL_MSG_STORE);
    }

    private boolean isCmStoreEnabled(int phoneId) {
        return this.mImModule.getRcsStrategy(phoneId) != null && this.mImModule.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.CENTRAL_MSG_STORE);
    }
}
