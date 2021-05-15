package com.sec.internal.ims.servicemodules.im;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.DownloadFileTask;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.translator.TranslatorCollection;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ConferenceInfoUpdater {
    protected static final String LOG_TAG = ConferenceInfoUpdater.class.getSimpleName();
    Map<ImParticipant, Date> mAddedParticipants;
    Set<ImParticipant> mDeletedParticipants;
    ImSession mImSession;
    Set<ImParticipant> mInsertedParticipants;
    boolean mIsLeaderChange;
    Map<ImParticipant, Date> mJoinedParticipants;
    Map<ImParticipant, Date> mKickedOutParticipants;
    Map<ImParticipant, Date> mLeftParticipants;
    private ImSessionListener mListener;
    IMnoStrategy mMnoStrategy;
    String mNewLeader;
    ImsUri mOwnUri;
    int mPhoneId;
    Set<ImParticipant> mUpdatedParticipants;
    UriGenerator mUriGenerator;

    protected ConferenceInfoUpdater(ImSession imSession, int phoneId, ImsUri ownUri, IMnoStrategy mnoStrategy, UriGenerator uriGenerator, ImSessionListener listener) {
        this.mPhoneId = phoneId;
        this.mOwnUri = ownUri;
        this.mMnoStrategy = mnoStrategy;
        this.mUriGenerator = uriGenerator;
        this.mListener = listener;
        this.mImSession = imSession;
    }

    /* access modifiers changed from: protected */
    public void onConferenceInfoUpdated(ImSessionConferenceInfoUpdateEvent event, String leader) {
        if (this.mMnoStrategy == null) {
            IMSLog.e(LOG_TAG, "onConferenceInfoUpdated : Fail!! Strategy is null");
            return;
        }
        this.mAddedParticipants = new HashMap();
        this.mJoinedParticipants = new HashMap();
        this.mLeftParticipants = new HashMap();
        this.mKickedOutParticipants = new HashMap();
        this.mInsertedParticipants = new HashSet();
        this.mUpdatedParticipants = new HashSet();
        this.mDeletedParticipants = new HashSet();
        this.mIsLeaderChange = false;
        this.mNewLeader = null;
        for (ImConferenceParticipantInfo info : event.mParticipantsInfo) {
            if (!(info == null || info.mUri == null)) {
                String str = LOG_TAG;
                IMSLog.s(str, "onConferenceInfoUpdated : " + info.mUri);
                info.mUri = this.mImSession.mGetter.normalizeUri(info.mUri);
                if (info.mIsOwn || info.mUri.equals(this.mOwnUri)) {
                    ownInfoUpdated(info, leader);
                } else {
                    ImParticipant participant = this.mImSession.getParticipant(info.mUri);
                    ImParticipant.Status status = TranslatorCollection.translateEngineParticipantInfo(info, participant);
                    if (participant == null) {
                        newParticipantAdded(info, status);
                    } else {
                        ImParticipant.Status prevStatus = participant.getStatus();
                        String str2 = LOG_TAG;
                        IMSLog.s(str2, info.mUri + " prevStatus=" + prevStatus + " status=" + status);
                        if (!(status == null || prevStatus == status)) {
                            participantStatusUpdated(info, participant, status, prevStatus);
                        }
                        ImParticipant.Type type = info.mIsChairman ? ImParticipant.Type.CHAIRMAN : ImParticipant.Type.REGULAR;
                        if (type != participant.getType()) {
                            this.mIsLeaderChange = true;
                            this.mImSession.logi("onConferenceInfoUpdated, mIsLeaderChange=true.");
                            participant.setType(type);
                            this.mUpdatedParticipants.add(participant);
                            if (type == ImParticipant.Type.CHAIRMAN) {
                                this.mNewLeader = participant.getUri().toString();
                            }
                        }
                        if (!TextUtils.isEmpty(info.mDispName) && !info.mDispName.equals(participant.getUserAlias())) {
                            participant.setUserAlias(info.mDispName);
                            this.mUpdatedParticipants.add(participant);
                            this.mListener.onParticipantAliasUpdated(this.mImSession.getChatId(), participant);
                        }
                    }
                }
            }
        }
        if (event.mConferenceInfoType == ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.FULL) {
            findAbsentParticipant(event);
        }
        notifyParticipantsInfo();
        if (isSubjectChanged(this.mImSession.getSubjectData(), event.mSubjectData)) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "onConferenceInfoUpdated, event.mSubjectData= " + event.mSubjectData + ", mChatData.getSubjectData()= " + this.mImSession.getSubjectData());
            this.mImSession.updateSubjectData(event.mSubjectData);
            this.mListener.onChatSubjectUpdated(this.mImSession.getChatId(), event.mSubjectData);
        }
        if (event.mIconData != null) {
            onGroupChatIconUpdated(event.mIconData);
        }
    }

    private boolean needToNotifyParticipantUpdates(ImConferenceParticipantInfo.ImConferenceUserElemState state) {
        IMnoStrategy iMnoStrategy = this.mMnoStrategy;
        if (iMnoStrategy == null) {
            return false;
        }
        if (!iMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.CHECK_PARTICIPANT_OF_PARTIAL_STATE) || state != ImConferenceParticipantInfo.ImConferenceUserElemState.PARTIAL) {
            return true;
        }
        return false;
    }

    private void downloadGroupIcon(ImIconData iconData) {
        ImConfig config = this.mImSession.mConfig;
        if (!config.getFtHttpEnabled()) {
            this.mImSession.logi("downloadGroupIcon: FT HTTP is not enabled.");
            return;
        }
        DownloadFileTask downloadFileTask = new DownloadFileTask(this.mPhoneId);
        String dir = this.mImSession.mGetter.onRequestIncomingFtTransferPath() + File.separatorChar + "received_files";
        File folder = new File(dir);
        if (!folder.exists() && !folder.mkdir()) {
            Log.e(LOG_TAG, "downloadGroupIcon: cannot create dir. Use default download directory.");
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        }
        final String path = dir + File.separatorChar + StringIdGenerator.generateFileTransferId();
        Map<String, String> params = new HashMap<>();
        String url = iconData.getIconUri();
        if (!TextUtils.isEmpty(config.getFtHttpDLUrl())) {
            params.put(ImsConstants.FtDlParams.FT_DL_URL, url);
            if (!TextUtils.isEmpty(this.mImSession.getConversationId())) {
                params.put(ImsConstants.FtDlParams.FT_DL_CONV_ID, this.mImSession.getConversationId());
            }
        }
        final ImIconData imIconData = iconData;
        String str = url;
        downloadFileTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new DownloadFileTask.DownloadRequest[]{new DownloadFileTask.DownloadRequest(url, 1, 0, path, config.getFtHttpCsUser(), config.getFtHttpCsPwd(), this.mMnoStrategy.getFtHttpUserAgent(config), this.mImSession.mGetter.getNetwork(0), config.isFtHttpTrustAllCerts(), config.getFtHttpDLUrl(), params, new DownloadFileTask.DownloadTaskCallback() {
            public void onProgressUpdate(long transferred) {
                String str = ConferenceInfoUpdater.LOG_TAG;
                Log.i(str, "Downloading Group Icon : " + transferred);
            }

            public void onCompleted(long transferred) {
                String str = ConferenceInfoUpdater.LOG_TAG;
                Log.i(str, "Downloading Group Icon has been completed : " + transferred);
                imIconData.setIconLocation(path);
                ConferenceInfoUpdater.this.mImSession.sendMessage(ConferenceInfoUpdater.this.mImSession.obtainMessage((int) ImSessionEvent.DOWNLOAD_GROUP_ICON_DONE, (Object) imIconData));
            }

            public void onCanceled(CancelReason reason, int retryTime, int errorCode) {
                String str = ConferenceInfoUpdater.LOG_TAG;
                Log.i(str, "Downloading Group Icon was failed : " + reason + " " + retryTime + " " + errorCode);
            }
        })});
    }

    private void triggerCapability(ImParticipant p, CapabilityRefreshType type) {
        ICapabilityDiscoveryModule discoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        if (discoveryModule != null) {
            discoveryModule.getCapabilities(p.getUri(), type, SimManagerFactory.getPhoneId(this.mImSession.getChatData().getOwnIMSI()));
        }
    }

    private void ownInfoUpdated(ImConferenceParticipantInfo info, String leader) {
        IMSLog.s(LOG_TAG, "ownInfoUpdated");
        if (info.mIsChairman) {
            String imsUri = info.mUri.toString();
            this.mNewLeader = imsUri;
            if (leader == null || !imsUri.equals(leader)) {
                this.mListener.onGroupChatLeaderInformed(this.mImSession, this.mNewLeader);
                this.mImSession.mLeaderParticipant = this.mNewLeader;
            }
        }
        if (!TextUtils.isEmpty(info.mDispName) && !info.mDispName.equals(this.mImSession.getChatData().getOwnGroupAlias())) {
            String str = LOG_TAG;
            IMSLog.s(str, "onConferenceInfoUpdated, old ownGroupAlias= " + this.mImSession.getChatData().getOwnGroupAlias() + ", new DispName=" + info.mDispName);
            this.mImSession.getChatData().updateOwnGroupAlias(info.mDispName);
        }
    }

    private void newParticipantAdded(ImConferenceParticipantInfo info, ImParticipant.Status status) {
        if (status == ImParticipant.Status.ACCEPTED || status == ImParticipant.Status.TO_INVITE) {
            ImParticipant.Type type = ImParticipant.Type.REGULAR;
            if (info.mIsChairman) {
                this.mNewLeader = info.mUri.toString();
                type = ImParticipant.Type.CHAIRMAN;
                this.mListener.onGroupChatLeaderChanged(this.mImSession, this.mNewLeader);
                this.mImSession.mLeaderParticipant = this.mNewLeader;
            }
            ImParticipant imParticipant = new ImParticipant(this.mImSession.getChatId(), status, type, info.mUri, info.mDispName);
            this.mInsertedParticipants.add(imParticipant);
            if (status == ImParticipant.Status.ACCEPTED) {
                this.mAddedParticipants.put(imParticipant, info.mJoiningTime);
                return;
            }
            return;
        }
        this.mImSession.logi("Participant doesn't exist, nor status is connected/pending...ignore");
    }

    private void participantStatusUpdated(ImConferenceParticipantInfo info, ImParticipant participant, ImParticipant.Status status, ImParticipant.Status prevStatus) {
        if (isJoinedParticipant(status, prevStatus)) {
            this.mJoinedParticipants.put(participant, info.mJoiningTime);
        }
        if (isKickedOutParticipant(prevStatus, info.mUserElemState, info.mDisconnectionReason)) {
            this.mKickedOutParticipants.put(participant, info.mDisconnectionTime);
        } else if (isLeftParticipant(status, prevStatus, info.mUserElemState)) {
            this.mLeftParticipants.put(participant, info.mDisconnectionTime);
        }
        if (status == ImParticipant.Status.FAILED && info.mDisconnectionCause == ImError.REMOTE_USER_INVALID) {
            this.mImSession.logi("invitation has failed with 404. update capability");
            triggerCapability(participant, CapabilityRefreshType.ALWAYS_FORCE_REFRESH);
        }
        if (status == ImParticipant.Status.DECLINED || (status == ImParticipant.Status.FAILED && this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.REMOVE_FAILED_PARTICIPANT_GROUPCHAT))) {
            participant.setStatus(status);
            this.mDeletedParticipants.add(participant);
        } else if (!isNonUpdateState(status, prevStatus)) {
            participant.setStatus(status);
            this.mUpdatedParticipants.add(participant);
        }
    }

    public void findAbsentParticipant(ImSessionConferenceInfoUpdateEvent event) {
        for (ImParticipant participant : this.mImSession.getParticipants()) {
            boolean isAbsent = true;
            Iterator<ImConferenceParticipantInfo> it = event.mParticipantsInfo.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ImConferenceParticipantInfo info = it.next();
                if (participant.getUri() != null && participant.getUri().equals(info.mUri)) {
                    isAbsent = false;
                    break;
                }
            }
            if (isAbsent && participant.getStatus() != ImParticipant.Status.INVITED) {
                ImSession imSession = this.mImSession;
                imSession.logi("onConferenceInfoUpdated, " + IMSLog.checker(participant.getUri()) + " is absent from updated full list.");
                this.mLeftParticipants.put(participant, (Object) null);
                participant.setStatus(ImParticipant.Status.DECLINED);
                this.mDeletedParticipants.add(participant);
            }
        }
    }

    public void notifyParticipantsInfo() {
        if (!this.mInsertedParticipants.isEmpty()) {
            this.mListener.onParticipantsInserted(this.mImSession, this.mInsertedParticipants);
        }
        if (!this.mUpdatedParticipants.isEmpty()) {
            this.mListener.onParticipantsUpdated(this.mImSession, this.mUpdatedParticipants);
        }
        if (!this.mDeletedParticipants.isEmpty()) {
            this.mListener.onParticipantsDeleted(this.mImSession, this.mDeletedParticipants);
            if (this.mImSession.getParticipants().isEmpty()) {
                this.mImSession.setSessionUri((ImsUri) null);
            }
        }
        if (!this.mAddedParticipants.isEmpty()) {
            this.mListener.onNotifyParticipantsAdded(this.mImSession, this.mAddedParticipants);
        }
        if (!this.mJoinedParticipants.isEmpty()) {
            this.mListener.onNotifyParticipantsJoined(this.mImSession, this.mJoinedParticipants);
        }
        if (!this.mLeftParticipants.isEmpty()) {
            this.mListener.onNotifyParticipantsLeft(this.mImSession, this.mLeftParticipants);
        }
        if (!this.mKickedOutParticipants.isEmpty()) {
            this.mListener.onNotifyParticipantsKickedOut(this.mImSession, this.mKickedOutParticipants);
        }
        if (this.mIsLeaderChange && !TextUtils.isEmpty(this.mNewLeader)) {
            this.mListener.onGroupChatLeaderChanged(this.mImSession, this.mNewLeader);
            this.mImSession.mLeaderParticipant = this.mNewLeader;
        }
    }

    public void onGroupChatIconUpdated(ImIconData iconData) {
        String str = LOG_TAG;
        IMSLog.s(str, "onConferenceInfoUpdated, event.mIconData= " + iconData + ", mChatData.getIconData()= " + this.mImSession.getIconData());
        if (iconData.getIconType() != ImIconData.IconType.ICON_TYPE_URI) {
            this.mImSession.updateIconData(iconData);
            this.mListener.onGroupChatIconUpdated(this.mImSession.getChatId(), iconData);
        } else if (TextUtils.isEmpty(iconData.getIconUri())) {
            this.mImSession.updateIconData(iconData);
            this.mListener.onGroupChatIconDeleted(this.mImSession.getChatId());
        } else if (this.mImSession.getIconData() == null || !iconData.getIconUri().equals(this.mImSession.getIconData().getIconUri()) || TextUtils.isEmpty(this.mImSession.getIconData().getIconLocation())) {
            downloadGroupIcon(iconData);
        } else if (!new File(this.mImSession.getIconData().getIconLocation()).exists()) {
            downloadGroupIcon(iconData);
        } else {
            iconData.setIconLocation(this.mImSession.getIconData().getIconLocation());
            this.mImSession.updateIconData(iconData);
        }
    }

    private boolean isJoinedParticipant(ImParticipant.Status status, ImParticipant.Status prevStatus) {
        return status == ImParticipant.Status.ACCEPTED && (prevStatus == ImParticipant.Status.INITIAL || prevStatus == ImParticipant.Status.TO_INVITE || (prevStatus == ImParticipant.Status.INVITED && !this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.DISPLAY_INVITED_SYSTEMMESSAGE)));
    }

    private boolean isKickedOutParticipant(ImParticipant.Status prevStatus, ImConferenceParticipantInfo.ImConferenceUserElemState userElemState, ImConferenceParticipantInfo.ImConferenceDisconnectionReason disconnectionReason) {
        return userElemState == ImConferenceParticipantInfo.ImConferenceUserElemState.DELETED && disconnectionReason == ImConferenceParticipantInfo.ImConferenceDisconnectionReason.BOOTED && (prevStatus == ImParticipant.Status.ACCEPTED || prevStatus == ImParticipant.Status.PENDING || (prevStatus == ImParticipant.Status.INVITED && this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.DISPLAY_INVITED_SYSTEMMESSAGE)));
    }

    private boolean isLeftParticipant(ImParticipant.Status status, ImParticipant.Status prevStatus, ImConferenceParticipantInfo.ImConferenceUserElemState userElemState) {
        return needToNotifyParticipantUpdates(userElemState) && status == ImParticipant.Status.DECLINED && (prevStatus == ImParticipant.Status.ACCEPTED || prevStatus == ImParticipant.Status.GONE || prevStatus == ImParticipant.Status.PENDING || (prevStatus == ImParticipant.Status.INVITED && this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.DISPLAY_INVITED_SYSTEMMESSAGE)));
    }

    private boolean isNonUpdateState(ImParticipant.Status status, ImParticipant.Status prevStatus) {
        return status == ImParticipant.Status.TO_INVITE || (status == ImParticipant.Status.INVITED && (prevStatus == ImParticipant.Status.ACCEPTED || prevStatus == ImParticipant.Status.PENDING)) || !(status != ImParticipant.Status.GONE || prevStatus == ImParticipant.Status.ACCEPTED || prevStatus == ImParticipant.Status.PENDING);
    }

    private boolean isSubjectChanged(ImSubjectData currData, ImSubjectData receivedData) {
        if (receivedData == null) {
            return false;
        }
        String oldSubject = "";
        String newSubject = "";
        if (currData != null && !TextUtils.isEmpty(currData.getSubject())) {
            oldSubject = currData.getSubject();
        }
        if (!TextUtils.isEmpty(receivedData.getSubject())) {
            newSubject = receivedData.getSubject();
        }
        return !oldSubject.equals(newSubject);
    }
}
