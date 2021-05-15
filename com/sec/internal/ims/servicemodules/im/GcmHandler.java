package com.sec.internal.ims.servicemodules.im;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GcmHandler {
    private static final String LOG_TAG = ImSessionProcessor.class.getSimpleName();
    private ImCache mCache;
    private ImModule mImModule;
    private ImSessionProcessor mImSessionProcessor;
    private ImTranslation mImTranslation;

    public GcmHandler(ImModule imModule, ImCache imCache, ImSessionProcessor imSessionProcessor, ImTranslation imTranslation) {
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mImSessionProcessor = imSessionProcessor;
        this.mImTranslation = imTranslation;
    }

    /* access modifiers changed from: protected */
    public void addParticipants(String chatId, List<ImsUri> participants) {
        String str = chatId;
        List<ImsUri> list = participants;
        String str2 = LOG_TAG;
        Log.i(str2, "AddParticipants: chatId=" + str + " participants=" + IMSLog.checker(participants));
        ImSession session = this.mCache.getImSession(str);
        if (session == null) {
            for (IChatEventListener listener : this.mImSessionProcessor.mChatEventListeners) {
                listener.onAddParticipantsFailed(str, list, ImErrorReason.ILLEGAL_SESSION_STATE);
            }
            return;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getOwnImsi());
        if (!this.mImModule.isRegistered(phoneId)) {
            for (IChatEventListener listener2 : this.mImSessionProcessor.mChatEventListeners) {
                listener2.onAddParticipantsFailed(str, list, ImErrorReason.ILLEGAL_SESSION_STATE);
            }
            return;
        }
        ImsUtil.listToDumpFormat(LogClass.IM_ADD_PARTICIPANT, phoneId, str);
        List<ImsUri> newParticipants = new ArrayList<>(list);
        newParticipants.removeAll(this.mImModule.getOwnUris(SimUtil.getSimSlotPriority()));
        if (newParticipants.isEmpty()) {
            Log.e(LOG_TAG, "addParticipants: requested for only own uri. Invalid.");
            for (IChatEventListener listener3 : this.mImSessionProcessor.mChatEventListeners) {
                listener3.onAddParticipantsFailed(str, list, ImErrorReason.INVALID);
            }
        } else if (session.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT || session.getParticipantsSize() == 0) {
            List<ImParticipant> insertedParticipants = new ArrayList<>();
            for (ImsUri uri : newParticipants) {
                ImsUri normalizedUri = this.mImModule.normalizeUri(uri);
                if (normalizedUri != null && session.getParticipant(normalizedUri) == null) {
                    insertedParticipants.add(new ImParticipant(chatId, ImParticipant.Status.INVITED, ImParticipant.Type.REGULAR, normalizedUri, ""));
                }
            }
            if (!insertedParticipants.isEmpty()) {
                this.mImSessionProcessor.onParticipantsInserted(session, insertedParticipants);
            }
            this.mImSessionProcessor.onAddParticipantsSucceeded(str, newParticipants);
        } else {
            session.addParticipants(newParticipants);
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupAlias(String chatId, String alias) {
        String str = LOG_TAG;
        Log.i(str, "changeGroupAlias: chatId=" + chatId + " alias=" + IMSLog.checker(alias));
        ImSession session = this.mCache.getImSession(chatId);
        if (session == null) {
            this.mImTranslation.onChangeGroupAliasFailed(chatId, alias, ImErrorReason.NO_SESSION);
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()))) {
            this.mImTranslation.onChangeGroupAliasFailed(chatId, alias, ImErrorReason.ILLEGAL_SESSION_STATE);
        } else {
            session.changeGroupAlias(alias);
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatIcon(String chatId, String icon_path) {
        String str = LOG_TAG;
        Log.i(str, "changeGroupChatIcon: chatId=" + chatId + " icon_path=" + icon_path);
        ImSession session = this.mCache.getImSession(chatId);
        if (session == null) {
            this.mImTranslation.onChangeGroupChatIconFailed(chatId, icon_path, ImErrorReason.NO_SESSION);
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()))) {
            this.mImTranslation.onChangeGroupChatIconFailed(chatId, icon_path, ImErrorReason.ILLEGAL_SESSION_STATE);
        } else if (TextUtils.isEmpty(icon_path)) {
            Log.e(LOG_TAG, "Delete icon");
            session.changeGroupChatIcon((String) null);
        } else if (!new File(icon_path).exists()) {
            Log.e(LOG_TAG, "icon file doesn't exist");
            this.mImTranslation.onChangeGroupChatIconFailed(chatId, icon_path, ImErrorReason.INVALID_ICON_PATH);
        } else {
            session.changeGroupChatIcon(icon_path);
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatLeader(String chatId, List<ImsUri> participants) {
        String str = LOG_TAG;
        Log.i(str, "changeGroupChatLeader: chatId=" + chatId + " participants=" + IMSLog.checker(participants));
        ImSession session = this.mCache.getImSession(chatId);
        if (session == null) {
            for (IChatEventListener listener : this.mImSessionProcessor.mChatEventListeners) {
                listener.onChangeGroupChatLeaderFailed(chatId, participants, ImErrorReason.NO_SESSION);
            }
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()))) {
            for (IChatEventListener listener2 : this.mImSessionProcessor.mChatEventListeners) {
                listener2.onChangeGroupChatLeaderFailed(chatId, participants, ImErrorReason.ILLEGAL_SESSION_STATE);
            }
            return;
        }
        session.changeGroupChatLeader(participants);
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatSubject(String chatId, String subject) {
        String str = LOG_TAG;
        Log.i(str, "changeGroupChatSubject: chatId=" + chatId + " subject=" + IMSLog.checker(subject));
        ImSession session = this.mCache.getImSession(chatId);
        if (session == null) {
            this.mImTranslation.onChangeGroupChatSubjectFailed(chatId, subject, ImErrorReason.NO_SESSION);
        } else if (!SimUtil.getSimMno(this.mImModule.getPhoneIdByIMSI(session.getChatData().getOwnIMSI())).isEur() || ImsProfile.isRcsUp2Profile(this.mImModule.getRcsProfile())) {
            session.changeGroupChatSubject(subject == null ? "" : subject);
        } else {
            this.mImTranslation.onChangeGroupChatSubjectFailed(chatId, subject, ImErrorReason.INVALID);
        }
    }

    /* access modifiers changed from: protected */
    public void removeParticipants(String chatId, List<ImsUri> participants) {
        ImParticipant p;
        String str = LOG_TAG;
        Log.i(str, "removeParticipants: chatId=" + chatId + " participants=" + IMSLog.checker(participants));
        ImSession session = this.mCache.getImSession(chatId);
        if (session == null) {
            for (IChatEventListener listener : this.mImSessionProcessor.mChatEventListeners) {
                listener.onRemoveParticipantsFailed(chatId, participants, ImErrorReason.NO_SESSION);
            }
            return;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getOwnImsi());
        ImsUtil.listToDumpFormat(LogClass.IM_REMOVE_PARTICIPANT, phoneId, chatId);
        if (!this.mImModule.isRegistered(phoneId)) {
            for (IChatEventListener listener2 : this.mImSessionProcessor.mChatEventListeners) {
                listener2.onRemoveParticipantsFailed(chatId, participants, ImErrorReason.ILLEGAL_SESSION_STATE);
            }
        } else if (session.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT) {
            List<ImParticipant> deletedParticipants = new ArrayList<>();
            for (ImsUri uri : participants) {
                ImsUri normalizedUri = this.mImModule.normalizeUri(uri);
                if (!(normalizedUri == null || (p = session.getParticipant(normalizedUri)) == null)) {
                    p.setStatus(ImParticipant.Status.DECLINED);
                    deletedParticipants.add(p);
                }
            }
            if (!deletedParticipants.isEmpty()) {
                this.mImSessionProcessor.onParticipantsDeleted(session, deletedParticipants);
            }
            this.mImSessionProcessor.onRemoveParticipantsSucceeded(chatId, participants);
        } else {
            for (ImsUri participant : participants) {
                ImParticipant p2 = session.getParticipant(participant);
                if (p2 == null || !(p2.getStatus() == ImParticipant.Status.ACCEPTED || p2.getStatus() == ImParticipant.Status.PENDING)) {
                    for (IChatEventListener listener3 : this.mImSessionProcessor.mChatEventListeners) {
                        listener3.onRemoveParticipantsFailed(chatId, participants, ImErrorReason.PARTICIPANT_ALREADY_LEFT);
                    }
                    return;
                }
            }
            session.removeParticipants(participants);
        }
    }

    /* access modifiers changed from: protected */
    public void updateParticipants(ImSession session, Set<ImsUri> normalizedParticipants) {
        if (session != null && session.getChatStateId() == ChatData.State.NONE.getId()) {
            Set<ImsUri> addedUris = new HashSet<>(normalizedParticipants);
            addedUris.removeAll(session.getParticipantsUri());
            List<ImParticipant> addedParticipants = new ArrayList<>();
            for (ImsUri uri : addedUris) {
                addedParticipants.add(new ImParticipant(session.getChatId(), uri));
            }
            if (!addedParticipants.isEmpty()) {
                this.mCache.addParticipant(addedParticipants);
                session.addParticipant(addedParticipants);
            }
            Set<ImsUri> deletedUris = new HashSet<>(session.getParticipantsUri());
            deletedUris.removeAll(normalizedParticipants);
            List<ImParticipant> deletedParticipants = new ArrayList<>();
            for (ImsUri deletedUri : deletedUris) {
                ImParticipant p = session.getParticipant(deletedUri);
                if (p != null) {
                    p.setStatus(ImParticipant.Status.DECLINED);
                    deletedParticipants.add(p);
                }
            }
            String str = LOG_TAG;
            Log.i(str, "added participants : " + IMSLog.checker(addedUris) + ", removed participants : " + IMSLog.checker(deletedUris));
            if (!deletedParticipants.isEmpty()) {
                this.mCache.deleteParticipant(deletedParticipants);
                session.deleteParticipant(deletedParticipants);
            }
        }
    }
}
