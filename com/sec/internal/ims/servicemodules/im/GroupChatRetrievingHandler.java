package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingGroupChatListEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GroupChatRetrievingHandler extends Handler {
    private static final int EVENT_GROUP_INFO_NOTIFICATION = 2;
    private static final int EVENT_GROUP_LIST_NOTIFICATION = 1;
    private static final int EVENT_SUBSCRIBE_NEXT_GROUP_CHAT_INFO = 3;
    private static final String LOG_TAG = ImModule.class.getSimpleName();
    private final Context mContext;
    private final HashMap<Uri, ImIncomingGroupChatListEvent.Entry> mGroupChatMap = new HashMap<>();
    private final ImCache mImCache;
    private final IImServiceInterface mImService;
    private final ImTranslation mImTranslation;
    private final String mOwnImsi;
    private final String mOwnPhoneNumber;
    private final ArrayList<Uri> mPendingGroupChatUri = new ArrayList<>();

    public GroupChatRetrievingHandler(Looper looper, Context context, ImCache imCache, ImTranslation imTranslation, IImServiceInterface imService, String ownPhoneNumber, String owmImsi) {
        super(looper);
        this.mContext = context;
        this.mImCache = imCache;
        this.mImTranslation = imTranslation;
        this.mImService = imService;
        this.mOwnPhoneNumber = ownPhoneNumber;
        this.mOwnImsi = owmImsi;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int i = msg.what;
        if (i == 1) {
            handleGroupListNotification((ImIncomingGroupChatListEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 2) {
            handleGroupInfoNotification((ImSessionConferenceInfoUpdateEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 3) {
            subscribeNextGroupChatInfo();
        }
    }

    public void startToRetrieveGroupChatList() {
        if (this.mOwnPhoneNumber == null) {
            Log.e(LOG_TAG, " can not retrieve the group chat list because own number is null");
            return;
        }
        Context context = this.mContext;
        SharedPreferences sp = context.getSharedPreferences("grouplist_setting_" + this.mOwnPhoneNumber, 0);
        int version = sp.getInt("version", 0);
        boolean increaseMode = sp.getBoolean("increaseMode", false);
        if (version != 0) {
            String str = LOG_TAG;
            Log.e(str, " startToRetrieveGroupChatList() version:" + version);
            return;
        }
        this.mGroupChatMap.clear();
        this.mPendingGroupChatUri.clear();
        this.mImService.registerForGroupChatListUpdate(this, 1, (Object) null);
        this.mImService.registerForGroupChatInfoUpdate(this, 2, (Object) null);
        this.mImService.subscribeGroupChatList(version, increaseMode, this.mOwnImsi);
    }

    private void handleGroupListNotification(ImIncomingGroupChatListEvent event) {
        if (this.mOwnImsi.equals(event.mOwnImsi)) {
            int version = event.version;
            List<ImIncomingGroupChatListEvent.Entry> list = event.entryList;
            if (list != null) {
                for (ImIncomingGroupChatListEvent.Entry entry : list) {
                    if (entry.sessionUri != null) {
                        this.mPendingGroupChatUri.add(entry.sessionUri);
                        this.mGroupChatMap.put(entry.sessionUri, entry);
                    }
                }
                Context context = this.mContext;
                SharedPreferences.Editor editor = context.getSharedPreferences("grouplist_setting_" + this.mOwnPhoneNumber, 0).edit();
                editor.putInt("version", version);
                editor.apply();
                sendEmptyMessage(3);
            }
        }
    }

    private void handleGroupInfoNotification(ImSessionConferenceInfoUpdateEvent event) {
        if (this.mOwnImsi.equals(event.mOwnImsi)) {
            String str = LOG_TAG;
            Log.i(str, "handleGroupInfoNotification() start, uri:" + event.mChatId);
            Uri uri = Uri.parse(event.mChatId);
            if (!this.mPendingGroupChatUri.contains(uri)) {
                Log.e(LOG_TAG, "handleGroupInfoNotification() fail, can not find that group chat in pending list");
                return;
            }
            ImIncomingGroupChatListEvent.Entry entry = this.mGroupChatMap.get(uri);
            if (entry == null) {
                Log.e(LOG_TAG, "handleGroupInfoNotification() fail, can not find that group chat in map");
                return;
            }
            this.mImCache.removeImCacheActionListener(this.mImTranslation);
            ImSession imSession = this.mImCache.getImSessionByConversationId(this.mOwnImsi, entry.pConvID, true);
            if (imSession == null) {
                HashSet hashSet = new HashSet();
                for (ImConferenceParticipantInfo info : event.mParticipantsInfo) {
                    hashSet.add(info.mUri);
                }
                imSession = this.mImCache.makeNewEmptySession(event.mOwnImsi, hashSet, ChatData.ChatType.REGULAR_GROUP_CHAT, ImDirection.OUTGOING, (ChatMode) null);
                ChatData chatData = imSession.getChatData();
                chatData.setConversationId(entry.pConvID);
                chatData.setSessionUri(ImsUri.parse(entry.sessionUri.toString()));
                chatData.updateSubject(entry.subject);
                chatData.updateSubjectData(event.mSubjectData);
                chatData.updateState(ChatData.State.CLOSED_BY_USER);
            }
            imSession.onConferenceInfoUpdated(new ImSessionConferenceInfoUpdateEvent(imSession.getChatId(), event.mConferenceInfoType, event.mParticipantsInfo, event.mMaxUserCount, event.mSubjectData, event.mRawHandle, event.mOwnImsi, event.mIconData));
            this.mImCache.addImCacheActionListener(this.mImTranslation);
            this.mPendingGroupChatUri.remove(uri);
            sendEmptyMessage(3);
        }
    }

    private void subscribeNextGroupChatInfo() {
        if (this.mPendingGroupChatUri.isEmpty()) {
            Log.i(LOG_TAG, "subscribeNextGroupChatInfo() finish, list is empty");
            this.mPendingGroupChatUri.clear();
            this.mGroupChatMap.clear();
            this.mImService.unRegisterForGroupChatListUpdate(this);
            this.mImService.unRegisterForGroupChatInfoUpdate(this);
            return;
        }
        this.mImService.subscribeGroupChatInfo(this.mPendingGroupChatUri.get(0), this.mOwnImsi);
    }
}
