package com.sec.internal.ims.servicemodules.im;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.FingerPrintGenerator;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.State;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.data.response.FileResizeResponse;
import com.sec.internal.ims.servicemodules.im.listener.FtMessageListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.FileDurationUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FtMsrpMessage extends FtMessage {
    /* access modifiers changed from: private */
    public final String LOG_TAG;
    /* access modifiers changed from: private */
    public ImsUri mConferenceUri;
    /* access modifiers changed from: private */
    public ImsUri mNewContactValueUri;
    /* access modifiers changed from: private */
    public int mRetryTimer = -1;
    /* access modifiers changed from: private */
    public boolean mSwapUriType;

    protected FtMsrpMessage(Builder<?> builder) {
        super(builder);
        String name;
        if (TextUtils.isEmpty(this.mImdnId) || this.mImdnId.length() < 4) {
            name = "";
        } else {
            name = TextUtils.substring(this.mImdnId, 0, 4);
        }
        this.LOG_TAG = FtMsrpMessage.class.getSimpleName() + "#" + name;
        this.mRawHandle = builder.mRawHandle;
    }

    public static Builder<?> builder() {
        return new Builder2((AnonymousClass1) null);
    }

    public void receiveTransfer(Message callback, FtIncomingSessionEvent event, boolean resume) {
        this.mIsResuming = resume;
        this.mFtCompleteCallback = callback;
        this.mStateMachine.sendMessage(this.mStateMachine.obtainMessage(10, (Object) event));
    }

    public void startFileTransferTimer() {
        String str = this.LOG_TAG;
        Log.i(str, "startFileTransferTimer() : " + this.mId);
        this.mStateMachine.getHandler().removeMessages(23);
        this.mStateMachine.sendMessageDelayed(this.mStateMachine.obtainMessage(23), 300000);
    }

    /* access modifiers changed from: protected */
    public void sendDeliveredNotification(Object rawHandle, String conversationId, String contributionId, Message onComplete, String ownImsi, boolean isGroupchat, boolean isBotSessionAnonymized) {
        SendImdnParams sendImdnParams = new SendImdnParams(rawHandle, this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, this.mRemoteUri.getMsisdn(), (String) null), this.mChatId, this.mConversationId == null ? conversationId : this.mConversationId, StringIdGenerator.generateContributionId(), ownImsi, onComplete, this.mDeviceId, getNewImdnData(NotificationStatus.DELIVERED), isGroupchat, new Date(), isBotSessionAnonymized);
        if (this.mIsSlmSvcMsg) {
            this.mSlmService.sendSlmDeliveredNotification(sendImdnParams);
        } else {
            this.mImsService.sendFtDeliveredNotification(sendImdnParams);
        }
    }

    /* access modifiers changed from: protected */
    public void sendDisplayedNotification(Object rawHandle, String conversationId, String contributionId, Message onComplete, String ownImsi, boolean isGroupchat, boolean isBotSessionAnonymized) {
        SendImdnParams sendImdnParams = new SendImdnParams(rawHandle, this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, this.mRemoteUri.getMsisdn(), (String) null), this.mChatId, this.mConversationId == null ? conversationId : this.mConversationId, StringIdGenerator.generateContributionId(), ownImsi, onComplete, this.mDeviceId, getNewImdnData(NotificationStatus.DISPLAYED), isGroupchat, new Date(), isBotSessionAnonymized);
        if (this.mIsSlmSvcMsg) {
            this.mSlmService.sendSlmDisplayedNotification(sendImdnParams);
        } else {
            this.mImsService.sendFtDisplayedNotification(sendImdnParams);
        }
    }

    /* access modifiers changed from: protected */
    public void sendRejectFtSession(FtRejectReason reason) {
        FtIncomingSessionEvent event = new FtIncomingSessionEvent();
        event.mRawHandle = this.mRawHandle;
        event.mIsSlmSvcMsg = this.mIsSlmSvcMsg;
        sendRejectFtSession(reason, event);
    }

    /* access modifiers changed from: protected */
    public void sendRejectFtSession(FtRejectReason reason, FtIncomingSessionEvent event) {
        this.mRejectReason = reason;
        RejectFtSessionParams rejectParams = new RejectFtSessionParams(event.mRawHandle, this.mStateMachine.obtainMessage(7), reason, this.mFileTransferId, this.mImdnId);
        if (event.mIsSlmSvcMsg) {
            this.mSlmService.rejectFtSlmMessage(rejectParams);
        } else {
            this.mImsService.rejectFtSession(rejectParams);
        }
    }

    /* access modifiers changed from: protected */
    public boolean renameFile() {
        File oldFile = new File(this.mFilePath);
        String str = this.LOG_TAG;
        Log.i(str, "temporary file path: " + this.mFilePath);
        String dir = oldFile.getParent();
        File folder = new File(dir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.mFilePath = FilePathGenerator.generateUniqueFilePath(dir, this.mFileName, 128);
        String str2 = this.LOG_TAG;
        Log.i(str2, "new file path: " + this.mFilePath);
        if (oldFile.renameTo(new File(this.mFilePath))) {
            Log.i(this.LOG_TAG, "file rename success");
            return true;
        }
        Log.e(this.LOG_TAG, "file rename fail");
        return false;
    }

    public void setConferenceUri(ImsUri uri) {
        this.mConferenceUri = uri;
    }

    public Object getRawHandle() {
        return this.mRawHandle;
    }

    /* access modifiers changed from: protected */
    public FtMessage.FtStateMachine createFtStateMachine(String name, Looper looper) {
        return new FtMsrpStateMachine("FtMsrpMessage#" + name, looper);
    }

    public int getTransferMech() {
        return 0;
    }

    /* access modifiers changed from: private */
    public void setCancelReasonBasedOnLineType() {
        if (isChatbotMessage() || this.mCancelReason == CancelReason.REJECTED_BY_REMOTE) {
            this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
        } else {
            this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
        }
    }

    /* access modifiers changed from: private */
    public void updateFtMessageInfo(FtIncomingSessionEvent event) {
        this.mRawHandle = event.mRawHandle;
        this.mFilePath = event.mFilePath;
        this.mFileName = event.mFileName;
        this.mFileSize = event.mFileSize;
        this.mContributionId = event.mContributionId;
        this.mConversationId = event.mConversationId;
        this.mContentType = event.mContentType;
        setSlmSvcMsg(event.mIsSlmSvcMsg);
    }

    public void setSlmSvcMsg(boolean isSlmSvcMsg) {
        this.mIsSlmSvcMsg = isSlmSvcMsg;
        if (isSlmSvcMsg) {
            setMessagingTech(this.mFileSize > ((long) this.mConfig.getPagerModeLimit()) ? ImConstants.MessagingTech.SLM_LARGE_MODE : ImConstants.MessagingTech.SLM_PAGER_MODE);
        } else {
            setMessagingTech(ImConstants.MessagingTech.NORMAL);
        }
    }

    /* access modifiers changed from: private */
    public boolean isChatbotMessage() {
        return !this.mIsGroupChat && ChatbotUriUtil.hasChatbotUri(this.mListener.onRequestParticipantUris(this.mChatId));
    }

    public static abstract class Builder<T extends Builder<T>> extends FtMessage.Builder<T> {
        Object mRawHandle;

        public T rawHandle(Object rawHandle) {
            this.mRawHandle = rawHandle;
            return (Builder) self();
        }

        public FtMsrpMessage build() {
            return new FtMsrpMessage(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        private Builder2() {
        }

        /* synthetic */ Builder2(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: protected */
        public Builder2 self() {
            return this;
        }
    }

    private class FtMsrpStateMachine extends FtMessage.FtStateMachine {
        /* access modifiers changed from: private */
        public final State mAcceptingState = new AcceptingState();
        private final State mAttachedState = new AttachedState();
        /* access modifiers changed from: private */
        public final State mCanceledState = new CanceledState();
        /* access modifiers changed from: private */
        public final State mCancelingState = new CancelingState();
        /* access modifiers changed from: private */
        public final State mCompletedState = new CompletedState();
        protected final MappingTranslator<Integer, State> mDbStateTranslator = new MappingTranslator.Builder().map(0, this.mInitialState).map(6, this.mCanceledState).map(2, this.mCanceledState).map(1, this.mCanceledState).map(3, this.mCompletedState).map(7, this.mCanceledState).map(4, this.mCanceledState).map(5, this.mCanceledState).map(9, this.mCanceledState).buildTranslator();
        private final State mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public final State mInProgressState = new InProgressState();
        private final State mInitialState = new InitialState();
        private final State mSendingState = new SendingState();
        protected final MappingTranslator<IState, Integer> mStateTranslator = new MappingTranslator.Builder().map(this.mInitialState, 0).map(this.mAttachedState, 6).map(this.mSendingState, 9).map(this.mAcceptingState, 1).map(this.mInProgressState, 2).map(this.mCompletedState, 3).map(this.mCancelingState, 7).map(this.mCanceledState, 4).buildTranslator();

        protected FtMsrpStateMachine(String name, Looper looper) {
            super(name, looper);
        }

        /* access modifiers changed from: protected */
        public void initState(State currentState) {
            addState(this.mDefaultState);
            addState(this.mInitialState, this.mDefaultState);
            addState(this.mAttachedState, this.mDefaultState);
            addState(this.mSendingState, this.mDefaultState);
            addState(this.mAcceptingState, this.mDefaultState);
            addState(this.mInProgressState, this.mDefaultState);
            addState(this.mCompletedState, this.mDefaultState);
            addState(this.mCancelingState, this.mDefaultState);
            addState(this.mCanceledState, this.mDefaultState);
            logi("setting current state as " + currentState.getName() + " for messageId : " + FtMsrpMessage.this.mId);
            setInitialState(currentState);
            start();
        }

        /* access modifiers changed from: private */
        public void onAttachSlmFile() {
            logi("onAttachSlmFile()");
            if (FtMsrpMessage.this.isChatbotMessage()) {
                loge("onAttachSlmFile: Chatbot, Display Error");
                FtMsrpMessage.this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
                transitionTo(this.mCanceledState);
            } else if (FtMsrpMessage.this.mFileSize <= FtMsrpMessage.this.mConfig.getSlmMaxMsgSize()) {
                FtMsrpMessage.this.setSlmSvcMsg(true);
                transitionTo(this.mAttachedState);
            } else if (!FtMsrpMessage.this.mIsResizable || !FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_LARGE_MSG_RESIZING)) {
                String access$200 = FtMsrpMessage.this.LOG_TAG;
                Log.i(access$200, "File size is greater than allowed MaxSlmSize mFileSize:" + FtMsrpMessage.this.mFileSize + ", SLMMaxMsgSize:" + FtMsrpMessage.this.mConfig.getSlmMaxMsgSize());
                FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                transitionTo(this.mCanceledState);
            } else {
                logi("request resizing for LMM");
                FtMessageListener ftMessageListener = FtMsrpMessage.this.mListener;
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                ftMessageListener.onFileResizingNeeded(ftMsrpMessage, ftMsrpMessage.mConfig.getSlmMaxMsgSize());
                FtMsrpMessage.this.setSlmSvcMsg(true);
            }
        }

        /* access modifiers changed from: private */
        public void onAttachFile(boolean checkCapability) {
            IMnoStrategy.StatusCode code;
            if (!checkCapability || !((code = FtMsrpMessage.this.getRcsStrategy().checkCapability(FtMsrpMessage.this.mListener.onRequestParticipantUris(FtMsrpMessage.this.mChatId), (long) Capabilities.FEATURE_FT_SERVICE).getStatusCode()) == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || code == IMnoStrategy.StatusCode.FALLBACK_TO_SLM)) {
                long MaxSizeFileTr = Math.max(FtMsrpMessage.this.mConfig.getMaxSizeExtraFileTr(), FtMsrpMessage.this.mConfig.getMaxSizeFileTr());
                if (!FtMsrpMessage.this.isOutgoing() || MaxSizeFileTr == 0 || FtMsrpMessage.this.mFileSize <= MaxSizeFileTr) {
                    if (FtMsrpMessage.this.isOutgoing() && (FtMsrpMessage.this.mContentType.startsWith(TMOConstants.CallLogTypes.VIDEO) || FtMsrpMessage.this.mContentType.startsWith(TMOConstants.CallLogTypes.AUDIO))) {
                        FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                        ftMsrpMessage.mTimeDuration = FileDurationUtil.getFileDurationTime(ftMsrpMessage.mFilePath);
                    }
                    if (FtMsrpMessage.this.mIsResuming) {
                        FtMsrpMessage.this.mContributionId = StringIdGenerator.generateContributionId();
                    }
                    if (FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_QUICKFT)) {
                        FtMsrpMessage.this.mFileFingerPrint = FingerPrintGenerator.getFileMD5(new File(FtMsrpMessage.this.mFilePath), "SHA1");
                        log("getFileMD5: mFilePath: " + FtMsrpMessage.this.mFilePath + " mFileFingerPrint: " + FtMsrpMessage.this.mFileFingerPrint);
                        if (FtMsrpMessage.this.mFileFingerPrint == null) {
                            FtMsrpMessage.this.mFileFingerPrint = "";
                        }
                    }
                    if (!FtMsrpMessage.this.isOutgoing() || !FtMsrpMessage.this.mConfig.isFtThumb() || FtMsrpMessage.this.mThumbnailPath != null || !FtMsrpMessage.this.mThumbnailTool.isSupported(FtMsrpMessage.this.mContentType)) {
                        transitionTo(this.mAttachedState);
                        return;
                    }
                    FtMsrpMessage.this.mThumbnailTool.createThumb(FtMsrpMessage.this.mFilePath, FtMsrpMessage.this.mThumbnailTool.getThumbSavedDirectory(), FtMsrpMessage.this.MAX_SIZE_THUMBNAIL, obtainMessage(19));
                    return;
                }
                loge("Attached file (" + FtMsrpMessage.this.mFileSize + ") exceeds MaxSizeFileTr (" + MaxSizeFileTr + ")");
                FtMsrpMessage.this.mCancelReason = CancelReason.TOO_LARGE;
                transitionTo(this.mCanceledState);
                return;
            }
            logi("onAttachFile: Capability checking failed.");
            if (FtMsrpMessage.this.isChatbotMessage()) {
                log("onAttachFile: Chatbot messgage no fallback");
                FtMsrpMessage.this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
            } else if (code != IMnoStrategy.StatusCode.FALLBACK_TO_SLM || FtMsrpMessage.this.mIsResuming) {
                FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                logi("onAttachFile: mCancelReason = " + FtMsrpMessage.this.mCancelReason);
            } else {
                logi("onAttachFile: fallback to SLM");
                onAttachSlmFile();
                return;
            }
            transitionTo(this.mCanceledState);
        }

        /* access modifiers changed from: private */
        public void onCreateThumbnail() {
            transitionTo(this.mAttachedState);
        }

        /* access modifiers changed from: private */
        public void onFileTransferInviteReceived(boolean isResumed) {
            if (FtMsrpMessage.this.mStatus == ImConstants.Status.BLOCKED) {
                logi("Auto reject file transfer, session blocked");
                FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                transitionTo(this.mCancelingState);
                return;
            }
            long maxSizeFileTrIncoming = FtMsrpMessage.this.mConfig.getMaxSizeFileTrIncoming() == -1 ? Math.max(FtMsrpMessage.this.mConfig.getMaxSizeExtraFileTr(), FtMsrpMessage.this.mConfig.getMaxSizeFileTr()) : FtMsrpMessage.this.mConfig.getMaxSizeFileTrIncoming();
            logi("onFileTransferInviteReceived(): mFileSize = " + FtMsrpMessage.this.mFileSize + " maxSizeFileTr = " + maxSizeFileTrIncoming);
            if (maxSizeFileTrIncoming != 0 && FtMsrpMessage.this.mFileSize > maxSizeFileTrIncoming) {
                loge("Auto reject file transfer, larger than max size mFileSize:" + FtMsrpMessage.this.mFileSize + ",MaxSizeFileTr:" + maxSizeFileTrIncoming);
                FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.FORBIDDEN_MAX_SIZE_EXCEEDED);
                FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                transitionTo(this.mCancelingState);
            } else if (!FtMsrpMessage.this.isExternalStorageAvailable()) {
                loge("Auto reject file transfer, ExternalStorage is not Available");
                FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                transitionTo(this.mCancelingState);
            } else {
                try {
                    String dir = FilePathGenerator.getIncomingFileDestinationDir(FtMsrpMessage.this.mListener.onRequestIncomingFtTransferPath());
                    if (!isResumed) {
                        if (FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.USE_TEMPFILE_WHEN_DOWNLOAD)) {
                            FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                            ftMsrpMessage.mFilePath = FilePathGenerator.generateUniqueFilePath(dir, FtMsrpMessage.this.mFileName + ".tmp", 128);
                        } else {
                            FtMsrpMessage.this.mFilePath = FilePathGenerator.generateUniqueFilePath(dir, FtMsrpMessage.this.mFileName, 128);
                        }
                        if (new File(FtMsrpMessage.this.mFilePath).createNewFile()) {
                            logi("Created a file for received FT: " + FtMsrpMessage.this.mFilePath);
                        } else {
                            loge("Auto reject file transfer, Failed to create a file for received FT");
                            FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                            FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                            transitionTo(this.mCancelingState);
                            return;
                        }
                    }
                    if (!FtMessage.checkAvailableStorage(dir, FtMsrpMessage.this.mFileSize - FtMsrpMessage.this.mTransferredBytes)) {
                        loge("Auto reject file transfer, disk space not available");
                        FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                        FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                        transitionTo(this.mCancelingState);
                        return;
                    }
                    FtMsrpMessage.this.getRcsStrategy().forceRefreshCapability(FtMsrpMessage.this.mListener.onRequestParticipantUris(FtMsrpMessage.this.mChatId), true, (ImError) null);
                    FtMsrpMessage.this.mListener.onTransferReceived(FtMsrpMessage.this);
                    transitionTo(this.mAcceptingState);
                } catch (IOException e) {
                    loge("Auto reject file transfer, internal error");
                    FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.NOT_ACCEPTABLE_HERE);
                    FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                    transitionTo(this.mCancelingState);
                }
            }
        }

        /* access modifiers changed from: private */
        public void onSendFile() {
            Log.i(FtMsrpMessage.this.LOG_TAG, "onSendFile");
            boolean isResuming = FtMsrpMessage.this.mIsResuming;
            Set<ImsUri> participants = FtMsrpMessage.this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, FtMsrpMessage.this.mListener.onRequestParticipantUris(FtMsrpMessage.this.mChatId));
            if (FtMsrpMessage.this.mNewContactValueUri != null) {
                participants.clear();
                participants.add(FtMsrpMessage.this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, FtMsrpMessage.this.mNewContactValueUri.getMsisdn(), (String) null));
                ImsUri unused = FtMsrpMessage.this.mNewContactValueUri = null;
            }
            if (FtMsrpMessage.this.mSwapUriType) {
                Set<ImsUri> networkParticipants = FtMsrpMessage.this.mUriGenerator.swapUriType(new ArrayList<>(participants));
                participants.clear();
                participants.addAll(networkParticipants);
                boolean unused2 = FtMsrpMessage.this.mSwapUriType = false;
            }
            if (FtMsrpMessage.this.getRcsStrategy().isResendFTResume(FtMsrpMessage.this.mIsGroupChat)) {
                isResuming = false;
            }
            if (FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.RESUME_WITH_COMPLETE_FILE)) {
                logi("resume resend complete file ");
                FtMsrpMessage.this.mTransferredBytes = 0;
            }
            if (FtMsrpMessage.this.getType() == ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT) {
                Set<ImsUri> tempParticipants = new HashSet<>();
                for (ImsUri uri : participants) {
                    tempParticipants.add(PublicAccountUri.convertToPublicAccountUri(uri.toString()));
                }
                participants = tempParticipants;
            }
            int i = FtMsrpMessage.this.mId;
            String str = FtMsrpMessage.this.mContributionId;
            String str2 = FtMsrpMessage.this.mConversationId;
            String str3 = FtMsrpMessage.this.mInReplyToContributionId;
            Message obtainMessage = obtainMessage(2);
            Message obtainMessage2 = obtainMessage(22);
            ArrayList arrayList = new ArrayList(participants);
            ImsUri access$600 = FtMsrpMessage.this.mConferenceUri;
            String str4 = FtMsrpMessage.this.mUserAlias;
            String str5 = FtMsrpMessage.this.mFileName;
            String str6 = FtMsrpMessage.this.mFilePath;
            boolean isResuming2 = isResuming;
            Set<ImsUri> set = participants;
            long j = FtMsrpMessage.this.mFileSize;
            String str7 = FtMsrpMessage.this.mContentType;
            String str8 = str6;
            ImDirection imDirection = FtMsrpMessage.this.mDirection;
            long j2 = j;
            long j3 = FtMsrpMessage.this.mTransferredBytes;
            Set set2 = FtMsrpMessage.this.mDispNotification;
            String str9 = FtMsrpMessage.this.mImdnId;
            Date date = new Date();
            String str10 = str9;
            String str11 = FtMsrpMessage.this.mFileTransferId;
            String str12 = FtMsrpMessage.this.mThumbnailPath;
            int i2 = FtMsrpMessage.this.mTimeDuration;
            String str13 = FtMsrpMessage.this.mDeviceName;
            String str14 = FtMsrpMessage.this.mReliableMessage;
            boolean z = FtMsrpMessage.this.mExtraFt;
            long j4 = j3;
            String str15 = str14;
            boolean z2 = z;
            String str16 = str12;
            int i3 = i2;
            Set set3 = set2;
            String str17 = str10;
            String str18 = str8;
            long j5 = j2;
            String str19 = str7;
            ImDirection imDirection2 = imDirection;
            boolean z3 = isResuming2;
            long j6 = j4;
            SendFtSessionParams params = new SendFtSessionParams(i, str, str2, str3, obtainMessage, obtainMessage2, arrayList, access$600, str4, str5, str18, j5, str19, imDirection2, z3, j6, set3, str17, date, str11, str16, i3, str13, str15, z2, FtMsrpMessage.this.getType() == ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT, FtMsrpMessage.this.mFileFingerPrint, FtMsrpMessage.this.mSimIMSI);
            if (FtMsrpMessage.this.mReportMsgParams != null) {
                params.mReportMsgParams = FtMsrpMessage.this.mReportMsgParams;
            }
            FtMsrpMessage.this.mImsService.sendFtSession(params);
            if (!(FtMsrpMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER) <= 0 || FtMsrpMessage.this.mListener.onRequestRegistrationType() == null || FtMsrpMessage.this.mListener.onRequestRegistrationType().intValue() == 18)) {
                logi(getName() + " Stack response timer starts");
                removeMessages(17);
                sendMessageDelayed(obtainMessage(17), ((long) FtMsrpMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER)) * 1000);
            }
            transitionTo(this.mSendingState);
        }

        /* access modifiers changed from: private */
        public void onSendSlmFile() {
            if (FtMsrpMessage.this.sendSlmFile(obtainMessage(12))) {
                transitionTo(this.mSendingState);
            } else {
                transitionTo(this.mCanceledState);
            }
        }

        /* access modifiers changed from: private */
        public void handleFTFailure(IMnoStrategy.StatusCode statusCode, ImError ftError) {
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[statusCode.ordinal()]) {
                case 1:
                    ImsUri unused = FtMsrpMessage.this.mNewContactValueUri = null;
                    if (ftError == ImError.UNSUPPORTED_URI_SCHEME) {
                        logi("onSendFileDone retry with other URI format");
                        boolean unused2 = FtMsrpMessage.this.mSwapUriType = true;
                    }
                    sendMessage(obtainMessage(18));
                    return;
                case 2:
                    ImsUri unused3 = FtMsrpMessage.this.mNewContactValueUri = null;
                    sendMessageDelayed(obtainMessage(18), ((long) FtMsrpMessage.this.mRetryTimer) * 1000);
                    return;
                case 3:
                    ImsUri unused4 = FtMsrpMessage.this.mNewContactValueUri = null;
                    sendMessageDelayed(obtainMessage(18), 1000);
                    return;
                case 4:
                    sendMessage(obtainMessage(18));
                    return;
                case 5:
                    FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(ftError);
                    FtMsrpMessage.this.getRcsStrategy().forceRefreshCapability(FtMsrpMessage.this.mListener.onRequestParticipantUris(FtMsrpMessage.this.mChatId), false, ftError);
                    if (FtMsrpMessage.this.mDirection == ImDirection.INCOMING) {
                        transitionTo(this.mCanceledState);
                        return;
                    }
                    logi("SendingState: fallback to FtSLM: " + FtMsrpMessage.this.mCancelReason);
                    FtMsrpMessage.this.mCancelReason = CancelReason.UNKNOWN;
                    handleFallbackToSlm();
                    return;
                case 6:
                    setCancelReason(ftError, true);
                    return;
                default:
                    setCancelReason(ftError, false);
                    return;
            }
        }

        private void setCancelReason(ImError ftError, boolean fallbackToLegacy) {
            FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(ftError);
            FtMsrpMessage.this.getRcsStrategy().forceRefreshCapability(FtMsrpMessage.this.mListener.onRequestParticipantUris(FtMsrpMessage.this.mChatId), false, ftError);
            if (fallbackToLegacy && FtMsrpMessage.this.mDirection == ImDirection.OUTGOING) {
                FtMsrpMessage.this.setCancelReasonBasedOnLineType();
            }
            transitionTo(this.mCanceledState);
        }

        /* access modifiers changed from: private */
        public void handleFallbackToSlm() {
            if (FtMsrpMessage.this.isChatbotMessage()) {
                logi("handleFallbackToSlm: Chatbot, Display Error");
                FtMsrpMessage.this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
                transitionTo(this.mCanceledState);
            } else if (FtMsrpMessage.this.mFileSize <= FtMsrpMessage.this.mConfig.getSlmMaxMsgSize()) {
                FtMsrpMessage.this.setSlmSvcMsg(true);
                onSendSlmFile();
            } else if (!FtMsrpMessage.this.mIsResizable || !FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_LARGE_MSG_RESIZING)) {
                String access$200 = FtMsrpMessage.this.LOG_TAG;
                Log.i(access$200, "File size is greater than allowed MaxSlmSize mFileSize:" + FtMsrpMessage.this.mFileSize + ", SLMMaxMsgSize:" + FtMsrpMessage.this.mConfig.getSlmMaxMsgSize());
                FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                transitionTo(this.mCanceledState);
            } else {
                FtMsrpMessage.this.setSlmSvcMsg(true);
                FtMsrpMessage.this.mRawHandle = null;
                logi("request resizing for LMM");
                FtMessageListener ftMessageListener = FtMsrpMessage.this.mListener;
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                ftMessageListener.onFileResizingNeeded(ftMsrpMessage, ftMsrpMessage.mConfig.getSlmMaxMsgSize());
                transitionTo(this.mSendingState);
            }
        }

        /* access modifiers changed from: private */
        public void handleRaceCondition(FtIncomingSessionEvent event) {
            logi("handleRaceCondition msgId=" + FtMsrpMessage.this.mId);
            if (FtMsrpMessage.this.isOutgoing() && FtMsrpMessage.this.mTransferredBytes != 0) {
                FtMsrpMessage.this.mRawHandle = event.mRawHandle;
                transitionTo(this.mAcceptingState);
                sendMessage(4);
            } else if (FtMsrpMessage.this.isOutgoing() || FtMsrpMessage.this.getIsSlmSvcMsg() || !event.mIsSlmSvcMsg) {
                logi("Cancel Incoming FT");
                FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.BUSY_HERE, event);
            } else {
                Log.i(FtMsrpMessage.this.LOG_TAG, "updateFtMsrpMessageInfo: service has been changed to SLM by sender.");
                FtMsrpMessage.this.updateFtMessageInfo(event);
                transitionTo(this.mAcceptingState);
                sendMessage(4);
            }
        }

        /* access modifiers changed from: private */
        public void onHandleFileResizeResponse(FileResizeResponse resizeResponse) {
            if (FtMsrpMessage.this.validateFileResizeResponse(resizeResponse)) {
                File file = new File(resizeResponse.resizedFilePath);
                FtMsrpMessage.this.mFileSize = file.length();
                FtMsrpMessage.this.mFileName = file.getName();
                FtMsrpMessage.this.mFilePath = resizeResponse.resizedFilePath;
                FtMsrpMessage.this.triggerObservers(ImCacheAction.UPDATED);
                if (getCurrentState() == this.mInitialState) {
                    transitionTo(this.mAttachedState);
                } else {
                    onSendSlmFile();
                }
            } else {
                FtMsrpMessage.this.setSlmSvcMsg(false);
                FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                transitionTo(this.mCanceledState);
            }
        }

        /* access modifiers changed from: protected */
        public int getStateId() {
            Integer ret = this.mStateTranslator.translate(getCurrentState());
            if (ret == null) {
                return 0;
            }
            return ret.intValue();
        }

        /* access modifiers changed from: protected */
        public State getState(Integer stateId) {
            return this.mDbStateTranslator.translate(stateId);
        }

        final class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 13) {
                    FtMsrpMessage.this.onSendDeliveredNotificationDone();
                    return true;
                } else if (i == 15) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.logi(getName() + " EVENT_AUTOACCEPT_RESUMING : " + FtMsrpMessage.this.mId);
                    FtMsrpMessage.this.mImsService.acceptFtSession(new AcceptFtSessionParams(FtMsrpMessage.this.mId, FtMsrpMessage.this.mRawHandle, FtMsrpMessage.this.mFilePath, FtMsrpMessage.this.mUserAlias, FtMsrpStateMachine.this.obtainMessage(5), 1 + FtMsrpMessage.this.mTransferredBytes, FtMsrpMessage.this.mFileSize));
                    FtMsrpMessage.this.acquireWakeLock();
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine2.transitionTo(ftMsrpStateMachine2.mAcceptingState);
                    return true;
                } else if (i != 23) {
                    if (FtMsrpStateMachine.this.getCurrentState() != null) {
                        FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine3.loge("Unexpected event, current state is " + FtMsrpStateMachine.this.getCurrentState().getName() + " event: " + msg.what);
                    }
                    return false;
                } else {
                    FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine4.logi("EVENT_TRANSFER_TIMER_TIMEOUT : " + FtMsrpMessage.this.mId);
                    FtMsrpMessage.this.cancelTransfer(CancelReason.CANCELED_BY_SYSTEM);
                    return true;
                }
            }
        }

        final class InitialState extends State {
            InitialState() {
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                boolean z = false;
                if (i != 1) {
                    if (i == 8) {
                        FtMsrpMessage.this.mCancelReason = (CancelReason) msg.obj;
                        FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                        ftMsrpStateMachine.transitionTo(ftMsrpStateMachine.mCanceledState);
                        return true;
                    } else if (i == 14) {
                        FtMsrpMessage.this.updateStatus(ImConstants.Status.QUEUED);
                        return true;
                    } else if (i == 16) {
                        FtMsrpStateMachine.this.onAttachSlmFile();
                        return true;
                    } else if (i == 10) {
                        FtMsrpStateMachine.this.onFileTransferInviteReceived(false);
                        return true;
                    } else if (i == 11) {
                        FtMsrpStateMachine.this.deferMessage(msg);
                        return true;
                    } else if (i == 19) {
                        FtMsrpMessage.this.mThumbnailPath = (String) ((AsyncResult) msg.obj).result;
                        FtMsrpStateMachine.this.onCreateThumbnail();
                        return true;
                    } else if (i != 20) {
                        return false;
                    } else {
                        FtMsrpStateMachine.this.onHandleFileResizeResponse((FileResizeResponse) msg.obj);
                        return true;
                    }
                } else if (FtMsrpMessage.this.isBroadcastMsg()) {
                    FtMsrpStateMachine.this.onAttachSlmFile();
                    return true;
                } else {
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    if (msg.arg1 == 1) {
                        z = true;
                    }
                    ftMsrpStateMachine2.onAttachFile(z);
                    return true;
                }
            }
        }

        final class AttachedState extends State {
            AttachedState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                if (FtMsrpMessage.this.mIsResuming) {
                    FtMsrpStateMachine.this.sendMessage(11);
                } else {
                    FtMsrpMessage.this.mListener.onTransferCreated(FtMsrpMessage.this);
                }
                FtMsrpMessage.this.updateState();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 8) {
                    FtMsrpMessage.this.mCancelReason = (CancelReason) msg.obj;
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.transitionTo(ftMsrpStateMachine.mCanceledState);
                    return true;
                } else if (i == 10) {
                    FtMsrpMessage.this.mRawHandle = ((FtIncomingSessionEvent) msg.obj).mRawHandle;
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine2.transitionTo(ftMsrpStateMachine2.mAcceptingState);
                    FtMsrpStateMachine.this.sendMessage(4);
                    return true;
                } else if (i != 11) {
                    return false;
                } else {
                    if (FtMsrpMessage.this.mIsSlmSvcMsg) {
                        FtMsrpStateMachine.this.onSendSlmFile();
                        return true;
                    }
                    FtMsrpStateMachine.this.onSendFile();
                    return true;
                }
            }
        }

        final class SendingState extends State {
            SendingState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpMessage.this.updateStatus(ImConstants.Status.SENDING);
                FtMsrpMessage.this.updateState();
                FtMsrpMessage.this.acquireWakeLock();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 2) {
                    onSendFileDone((AsyncResult) msg.obj);
                    return true;
                } else if (i == 3) {
                    FtTransferProgressEvent event = (FtTransferProgressEvent) msg.obj;
                    if (!Objects.equals(FtMsrpMessage.this.mRawHandle, event.mRawHandle)) {
                        FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                        ftMsrpStateMachine.logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtMsrpMessage.this.mRawHandle + ", event.mRawHandle=" + event.mRawHandle);
                        return true;
                    }
                    FtMsrpStateMachine.this.removeMessages(17);
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine2.logi("SendingState: EVENT_TRANSFER_PROGRESS event.mState = " + event.mState);
                    if (event.mState == FtTransferProgressEvent.State.COMPLETED) {
                        FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mCompletedState);
                        return true;
                    } else if (event.mState == FtTransferProgressEvent.State.TRANSFERRING) {
                        FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine4.transitionTo(ftMsrpStateMachine4.mInProgressState);
                        return true;
                    } else {
                        FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(event.mReason.getImError());
                        if (FtMsrpMessage.this.mIsSlmSvcMsg) {
                            FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                        }
                        FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine5.transitionTo(ftMsrpStateMachine5.mCanceledState);
                        return true;
                    }
                } else if (i == 8) {
                    FtMsrpMessage.this.mCancelReason = (CancelReason) msg.obj;
                    if (FtMsrpMessage.this.mRawHandle == null) {
                        Log.i(FtMsrpMessage.this.LOG_TAG, "mRawHandle is null");
                        FtMsrpStateMachine ftMsrpStateMachine6 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine6.transitionTo(ftMsrpStateMachine6.mCanceledState);
                        return true;
                    }
                    FtMsrpMessage.this.sendCancelFtSession(FtMsrpMessage.this.mCancelReason);
                    if (FtMsrpMessage.this.mCancelReason == CancelReason.CANCELED_BY_SYSTEM) {
                        FtMsrpStateMachine ftMsrpStateMachine7 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine7.transitionTo(ftMsrpStateMachine7.mCanceledState);
                        return true;
                    }
                    FtMsrpStateMachine ftMsrpStateMachine8 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine8.transitionTo(ftMsrpStateMachine8.mCancelingState);
                    return true;
                } else if (i == 10) {
                    FtMsrpStateMachine.this.handleRaceCondition((FtIncomingSessionEvent) msg.obj);
                    return true;
                } else if (i == 12) {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        return true;
                    }
                    FtResult result = (FtResult) ar.result;
                    FtMsrpStateMachine ftMsrpStateMachine9 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine9.logi("SLM send file done : " + result.mRawHandle);
                    if (result.getImError() == ImError.SUCCESS) {
                        FtMsrpMessage.this.mRawHandle = result.mRawHandle;
                        if (FtMsrpMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER) <= 0 || FtMsrpMessage.this.mListener.onRequestRegistrationType() == null || FtMsrpMessage.this.mListener.onRequestRegistrationType().intValue() == 18) {
                            FtMsrpStateMachine ftMsrpStateMachine10 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine10.transitionTo(ftMsrpStateMachine10.mInProgressState);
                            return true;
                        }
                        FtMsrpStateMachine ftMsrpStateMachine11 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine11.logi(getName() + " Stack response timer starts");
                        FtMsrpStateMachine.this.removeMessages(17);
                        FtMsrpStateMachine ftMsrpStateMachine12 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine12.sendMessageDelayed(ftMsrpStateMachine12.obtainMessage(17), ((long) FtMsrpMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER)) * 1000);
                        return true;
                    }
                    FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                    FtMsrpStateMachine ftMsrpStateMachine13 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine13.transitionTo(ftMsrpStateMachine13.mCanceledState);
                    return true;
                } else if (i == 20) {
                    FtMsrpStateMachine.this.onHandleFileResizeResponse((FileResizeResponse) msg.obj);
                    return true;
                } else if (i == 22) {
                    FtMsrpMessage.this.mRawHandle = ((FtResult) ((AsyncResult) msg.obj).result).mRawHandle;
                    FtMsrpStateMachine ftMsrpStateMachine14 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine14.logi("update session handle mRawHandle=" + FtMsrpMessage.this.mRawHandle + " id = " + FtMsrpMessage.this.getId());
                    return true;
                } else if (i == 17) {
                    FtMsrpStateMachine.this.logi("Stack response timer expires, cancel file and fallback");
                    FtMsrpMessage.this.sendCancelFtSession(CancelReason.DEDICATED_BEARER_UNAVAILABLE_TIMEOUT);
                    FtMsrpStateMachine ftMsrpStateMachine15 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine15.transitionTo(ftMsrpStateMachine15.mCancelingState);
                    return true;
                } else if (i != 18) {
                    return false;
                } else {
                    FtMsrpMessage.this.mContributionId = StringIdGenerator.generateContributionId();
                    FtMsrpStateMachine.this.onSendFile();
                    FtMsrpMessage.this.incrementRetryCount();
                    return true;
                }
            }

            private void onSendFileDone(AsyncResult ar) {
                if (ar.exception != null) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.transitionTo(ftMsrpStateMachine.mCanceledState);
                    return;
                }
                FtResult result = (FtResult) ar.result;
                ImError ftError = result.getImError();
                int unused = FtMsrpMessage.this.mRetryTimer = result.mRetryTimer;
                FtMsrpStateMachine.this.removeMessages(17);
                FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                ftMsrpStateMachine2.logi("onSendFileDone : " + ftError + " retryTimer: " + FtMsrpMessage.this.mRetryTimer + " newContactValue: " + FtMsrpMessage.this.mNewContactValueUri);
                if (ftError == ImError.SUCCESS) {
                    FtMsrpMessage.this.mRawHandle = result.mRawHandle;
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mInProgressState);
                    return;
                }
                IMnoStrategy.StrategyResponse strategyResponseFtFailure = FtMsrpMessage.this.getRcsStrategy().handleSendingFtMsrpMessageFailure(ftError, FtMsrpMessage.this.getCurrentRetryCount(), FtMsrpMessage.this.mRetryTimer, FtMsrpMessage.this.mNewContactValueUri, FtMsrpMessage.this.mListener.onRequestChatType(FtMsrpMessage.this.mChatId), false);
                FtMsrpMessage.this.mErrorNotificationId = strategyResponseFtFailure.getErrorNotificationId();
                IMnoStrategy.StatusCode statusCode = strategyResponseFtFailure.getStatusCode();
                FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                ftMsrpStateMachine4.logi("SendingState: onSendFileDone. statusCode : " + statusCode);
                if (FtMsrpMessage.this.getRcsStrategy().isNeedToReportToRegiGvn(ftError) && FtMsrpMessage.this.mDirection == ImDirection.OUTGOING) {
                    FtMsrpMessage.this.mListener.onFtErrorReport(ftError);
                }
                if (!FtMsrpMessage.this.isChatbotMessage() || !(statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM || ftError == ImError.GONE || ftError == ImError.REQUEST_PENDING)) {
                    FtMsrpStateMachine.this.handleFTFailure(statusCode, ftError);
                } else {
                    FtMsrpStateMachine.this.handleFTFailure(IMnoStrategy.StatusCode.DISPLAY_ERROR, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED);
                }
            }
        }

        final class AcceptingState extends State {
            AcceptingState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpMessage.this.mImsService.setFtMessageId(FtMsrpMessage.this.mRawHandle, FtMsrpMessage.this.mId);
                FtMsrpMessage.this.updateState();
            }

            public boolean processMessage(Message msg) {
                Message message = msg;
                int i = message.what;
                if (i == 3) {
                    FtTransferProgressEvent event = (FtTransferProgressEvent) message.obj;
                    if (!Objects.equals(FtMsrpMessage.this.mRawHandle, event.mRawHandle)) {
                        FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                        ftMsrpStateMachine.logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtMsrpMessage.this.mRawHandle + ", event.mRawHandle=" + event.mRawHandle);
                        return true;
                    }
                    FtMsrpStateMachine.this.removeMessages(23);
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine2.sendMessageDelayed(ftMsrpStateMachine2.obtainMessage(23), 300000);
                    if (event.mState == FtTransferProgressEvent.State.CANCELED) {
                        FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(event.mReason.getImError());
                        FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mCanceledState);
                        return true;
                    }
                    FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine4.loge(getName() + ": Unexpected progress state " + event.mState);
                    return true;
                } else if (i == 4) {
                    long start = 0;
                    long end = 0;
                    if (FtMsrpMessage.this.mTransferredBytes > 0) {
                        start = FtMsrpMessage.this.mTransferredBytes + 1;
                        end = FtMsrpMessage.this.mFileSize;
                        if (FtMsrpMessage.this.getRcsStrategy().isResendFTResume(FtMsrpMessage.this.mIsGroupChat)) {
                            FtMsrpStateMachine.this.logi("Force FT to resume from the beginning");
                            start = 0;
                            end = 0;
                        }
                    }
                    if (FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.RESUME_WITH_COMPLETE_FILE)) {
                        FtMsrpStateMachine.this.logi("Request complete file");
                        start = 1;
                        end = FtMsrpMessage.this.mFileSize;
                    }
                    AcceptFtSessionParams acceptParams = new AcceptFtSessionParams(FtMsrpMessage.this.mId, FtMsrpMessage.this.mRawHandle, FtMsrpMessage.this.mFilePath, FtMsrpMessage.this.mUserAlias, FtMsrpStateMachine.this.obtainMessage(5), start, end);
                    if (FtMsrpMessage.this.mIsSlmSvcMsg) {
                        FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine5.logi("Accepting SLM message, msgId : " + FtMsrpMessage.this.mId);
                        FtMsrpMessage.this.mSlmService.acceptFtSlmMessage(acceptParams);
                    } else {
                        FtMsrpMessage.this.mImsService.acceptFtSession(acceptParams);
                    }
                    FtMsrpMessage.this.acquireWakeLock();
                    return true;
                } else if (i == 5) {
                    FtResult result = (FtResult) ((AsyncResult) message.obj).result;
                    if (result.getImError() == ImError.SUCCESS) {
                        FtMsrpStateMachine ftMsrpStateMachine6 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine6.transitionTo(ftMsrpStateMachine6.mInProgressState);
                        return true;
                    }
                    FtMsrpStateMachine.this.loge("AcceptingState: Failed to accept transfer.");
                    FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(result.getImError());
                    FtMsrpStateMachine ftMsrpStateMachine7 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine7.transitionTo(ftMsrpStateMachine7.mCanceledState);
                    return true;
                } else if (i == 6) {
                    FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                    FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                    FtMsrpStateMachine ftMsrpStateMachine8 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine8.transitionTo(ftMsrpStateMachine8.mCancelingState);
                    return true;
                } else if (i != 8) {
                    return false;
                } else {
                    FtMsrpMessage.this.mCancelReason = (CancelReason) message.obj;
                    FtMsrpMessage.this.sendCancelFtSession(FtMsrpMessage.this.mCancelReason);
                    if (FtMsrpMessage.this.mCancelReason == CancelReason.CANCELED_BY_SYSTEM) {
                        FtMsrpStateMachine ftMsrpStateMachine9 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine9.transitionTo(ftMsrpStateMachine9.mCanceledState);
                        return true;
                    }
                    FtMsrpStateMachine ftMsrpStateMachine10 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine10.transitionTo(ftMsrpStateMachine10.mCancelingState);
                    return true;
                }
            }
        }

        final class InProgressState extends State {
            InProgressState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId + " isSlm : " + FtMsrpMessage.this.mIsSlmSvcMsg);
                FtMsrpMessage.this.updateState();
                FtMsrpMessage.this.mListener.onTransferInProgress(FtMsrpMessage.this);
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 3) {
                    FtTransferProgressEvent event = (FtTransferProgressEvent) msg.obj;
                    if (!Objects.equals(FtMsrpMessage.this.mRawHandle, event.mRawHandle)) {
                        FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                        ftMsrpStateMachine.logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtMsrpMessage.this.mRawHandle + ", event.mRawHandle=" + event.mRawHandle);
                        return true;
                    }
                    FtMsrpStateMachine.this.removeMessages(23);
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine2.sendMessageDelayed(ftMsrpStateMachine2.obtainMessage(23), 300000);
                    int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[event.mState.ordinal()];
                    if (i2 == 1) {
                        FtMsrpMessage.this.updateTransferredBytes((FtMsrpMessage.this.mFileSize - event.mTotal) + event.mTransferred);
                        FtMsrpMessage.this.mListener.onTransferProgressReceived(FtMsrpMessage.this);
                        return true;
                    } else if (i2 == 2) {
                        FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(event.mReason.getImError());
                        if (FtMsrpMessage.this.mIsSlmSvcMsg) {
                            FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                        }
                        FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mCanceledState);
                        return true;
                    } else if (i2 != 3) {
                        if (i2 != 4) {
                            return true;
                        }
                        FtMsrpMessage.this.mTransferredBytes = (FtMsrpMessage.this.mFileSize - event.mTotal) + event.mTransferred;
                        FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine4.logi("INTERRUPTED mFileSize: " + FtMsrpMessage.this.mFileSize + " mTotal: " + event.mTotal + " mTransferred: " + event.mTransferred);
                        FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(event.mReason.getImError());
                        if (FtMsrpMessage.this.mIsSlmSvcMsg) {
                            if (FtMsrpMessage.this.mCancelReason != CancelReason.REJECTED_BY_REMOTE) {
                                FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                            }
                            FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine5.transitionTo(ftMsrpStateMachine5.mCanceledState);
                            return true;
                        } else if (FtMsrpMessage.this.mDirection != ImDirection.INCOMING || !FtMsrpMessage.this.mFilePath.endsWith(".tmp") || event.mTotal != event.mTransferred || !FtMsrpMessage.this.renameFile()) {
                            onTransferInterrupted(event);
                            return true;
                        } else {
                            FtMsrpStateMachine.this.logi("Transferred size is same with total size");
                            FtMsrpStateMachine ftMsrpStateMachine6 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine6.transitionTo(ftMsrpStateMachine6.mCompletedState);
                            return true;
                        }
                    } else if ((FtMsrpMessage.this.mDirection == ImDirection.INCOMING || FtMsrpMessage.this.mFilePath.endsWith(".tmp")) && !FtMsrpMessage.this.renameFile()) {
                        FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                        FtMsrpStateMachine ftMsrpStateMachine7 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine7.transitionTo(ftMsrpStateMachine7.mCanceledState);
                        return true;
                    } else {
                        FtMsrpStateMachine ftMsrpStateMachine8 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine8.transitionTo(ftMsrpStateMachine8.mCompletedState);
                        return true;
                    }
                } else if (i == 8) {
                    FtMsrpMessage.this.mCancelReason = (CancelReason) msg.obj;
                    FtMsrpMessage.this.sendCancelFtSession(FtMsrpMessage.this.mCancelReason);
                    if (FtMsrpMessage.this.mCancelReason == CancelReason.CANCELED_BY_SYSTEM) {
                        FtMsrpStateMachine ftMsrpStateMachine9 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine9.transitionTo(ftMsrpStateMachine9.mCanceledState);
                        return true;
                    }
                    FtMsrpStateMachine ftMsrpStateMachine10 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine10.transitionTo(ftMsrpStateMachine10.mCancelingState);
                    return true;
                } else if (i != 10) {
                    return false;
                } else {
                    FtMsrpStateMachine.this.handleRaceCondition((FtIncomingSessionEvent) msg.obj);
                    return true;
                }
            }

            private void onTransferInterrupted(FtTransferProgressEvent event) {
                Result reason = event.mReason;
                IMnoStrategy.StatusCode statusCode = FtMsrpMessage.this.getRcsStrategy().handleFtMsrpInterruption(reason.getImError()).getStatusCode();
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi("onTransferInterrupted() : errorReason : " + reason + ", statusCode : " + statusCode);
                List<String> dumps = new ArrayList<>();
                dumps.add(String.valueOf(reason.getReasonHdr() != null ? reason.getReasonHdr().getCode() : 0));
                dumps.add(String.valueOf(FtMsrpMessage.this.getRetryCount()));
                FtMsrpMessage.this.listToDumpFormat(LogClass.FT_MSRP_CANCEL, 0, dumps);
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[statusCode.ordinal()];
                if (i != 5) {
                    if (i != 6) {
                        FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine2.transitionTo(ftMsrpStateMachine2.mCanceledState);
                        return;
                    }
                    FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mCanceledState);
                } else if (FtMsrpMessage.this.mDirection == ImDirection.INCOMING) {
                    FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine4.transitionTo(ftMsrpStateMachine4.mCanceledState);
                } else {
                    FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine5.logi("onTransferInterrupted : fallback to FtSLM: " + FtMsrpMessage.this.mCancelReason);
                    FtMsrpMessage.this.mCancelReason = CancelReason.UNKNOWN;
                    FtMsrpStateMachine.this.handleFallbackToSlm();
                }
            }
        }

        final class CompletedState extends State {
            CompletedState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.log(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                if (FtMsrpMessage.this.mIsBootup) {
                    FtMsrpStateMachine.this.logi("Message is loaded from bootup, no need for notifications");
                    FtMsrpMessage.this.mIsBootup = false;
                    return;
                }
                if (FtMsrpMessage.this.getDirection() == ImDirection.OUTGOING) {
                    FtMsrpMessage.this.setSentTimestamp(System.currentTimeMillis());
                    FtMsrpMessage.this.updateStatus(ImConstants.Status.SENT);
                } else {
                    FtMsrpMessage.this.updateStatus(ImConstants.Status.UNREAD);
                }
                if (FtMsrpMessage.this.isDeliveredNotificationRequired()) {
                    FtMsrpMessage.this.setDesiredNotificationStatus(NotificationStatus.DELIVERED);
                    FtMsrpMessage.this.updateDeliveredTimestamp(System.currentTimeMillis());
                    FtMsrpMessage.this.mListener.onSendDeliveredNotification(FtMsrpMessage.this);
                }
                FtMsrpMessage.this.mIsConferenceUriChanged = false;
                FtMsrpMessage.this.invokeFtQueueCallBack();
                FtMsrpMessage.this.removeThumbnail();
                FtMsrpStateMachine.this.removeMessages(21);
                FtMsrpStateMachine.this.removeMessages(23);
                PreciseAlarmManager.getInstance(FtMsrpMessage.this.getContext()).removeMessage(FtMsrpStateMachine.this.obtainMessage(21));
                FtMsrpMessage.this.updateState();
                FtMsrpMessage.this.listToDumpFormat(LogClass.FT_MSRP_COMPLETE, 0, new ArrayList<>());
                FtMsrpMessage.this.releaseWakeLock();
                FtMsrpMessage.this.mListener.onTransferCompleted(FtMsrpMessage.this);
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 8) {
                    FtMsrpMessage.this.mListener.onCancelRequestFailed(FtMsrpMessage.this);
                    return true;
                } else if (i != 10) {
                    return false;
                } else {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.logi(getName() + " msgId : " + FtMsrpMessage.this.mId + " resuming request after ft is completed");
                    if (FtMsrpMessage.this.mDirection != ImDirection.OUTGOING || FtMsrpMessage.this.mIsSlmSvcMsg) {
                        FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE, (FtIncomingSessionEvent) msg.obj);
                        FtMsrpMessage.this.invokeFtQueueCallBack();
                        return true;
                    }
                    FtMsrpMessage.this.mRawHandle = ((FtIncomingSessionEvent) msg.obj).mRawHandle;
                    FtMsrpStateMachine.this.onFileTransferInviteReceived(true);
                    return true;
                }
            }
        }

        final class CancelingState extends State {
            CancelingState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpMessage.this.updateState();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 2) {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar == null) {
                        return true;
                    }
                    ImError reason = ((FtResult) ar.result).getImError();
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.logi("onSendFileDone in CancelingState: " + reason);
                    return true;
                } else if (i == 3) {
                    FtTransferProgressEvent event = (FtTransferProgressEvent) msg.obj;
                    if (!Objects.equals(FtMsrpMessage.this.mRawHandle, event.mRawHandle)) {
                        FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine2.logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtMsrpMessage.this.mRawHandle + ", event.mRawHandle=" + event.mRawHandle);
                        return true;
                    }
                    FtMsrpStateMachine.this.removeMessages(23);
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine3.sendMessageDelayed(ftMsrpStateMachine3.obtainMessage(23), 300000);
                    int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[event.mState.ordinal()];
                    if (i2 != 1) {
                        if (i2 != 2) {
                            if (i2 != 3) {
                                if (i2 != 4) {
                                    return true;
                                }
                            } else if ((FtMsrpMessage.this.mDirection == ImDirection.INCOMING || FtMsrpMessage.this.mFilePath.endsWith(".tmp")) && !FtMsrpMessage.this.renameFile()) {
                                FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                                FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                                ftMsrpStateMachine4.transitionTo(ftMsrpStateMachine4.mCanceledState);
                                return true;
                            } else {
                                FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                                ftMsrpStateMachine5.transitionTo(ftMsrpStateMachine5.mCompletedState);
                                return true;
                            }
                        }
                        if (FtMsrpMessage.this.mCancelReason == CancelReason.DEDICATED_BEARER_UNAVAILABLE_TIMEOUT) {
                            handleFTTimeout(ImError.DEDICATED_BEARER_FALLBACK);
                            return true;
                        }
                        FtMsrpStateMachine ftMsrpStateMachine6 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine6.transitionTo(ftMsrpStateMachine6.mCanceledState);
                        return true;
                    }
                    FtMsrpMessage.this.updateTransferredBytes((FtMsrpMessage.this.mFileSize - event.mTotal) + event.mTransferred);
                    return true;
                } else if (i == 7) {
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2 == null) {
                        return true;
                    }
                    if (((FtResult) ar2.result).getImError() != ImError.SUCCESS) {
                        FtMsrpStateMachine.this.loge("CancelingState: Failed to cancel transfer.");
                    }
                    FtMsrpStateMachine ftMsrpStateMachine7 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine7.transitionTo(ftMsrpStateMachine7.mCanceledState);
                    return true;
                } else if (i == 8) {
                    CancelReason reason2 = (CancelReason) msg.obj;
                    FtMsrpStateMachine ftMsrpStateMachine8 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine8.logi("cancel transfer in cancelingState reason = " + reason2);
                    if (reason2 != CancelReason.CANCELED_BY_SYSTEM) {
                        return true;
                    }
                    FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                    FtMsrpStateMachine ftMsrpStateMachine9 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine9.transitionTo(ftMsrpStateMachine9.mCanceledState);
                    return true;
                } else if (i != 9) {
                    return false;
                } else {
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3 == null) {
                        return true;
                    }
                    String access$200 = FtMsrpMessage.this.LOG_TAG;
                    Log.i(access$200, "CancelingState: cancel transfer result = " + ((FtResult) ar3.result));
                    if (FtMsrpMessage.this.mCancelReason == CancelReason.DEDICATED_BEARER_UNAVAILABLE_TIMEOUT) {
                        handleFTTimeout(ImError.DEDICATED_BEARER_FALLBACK);
                        return true;
                    }
                    FtMsrpStateMachine ftMsrpStateMachine10 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine10.transitionTo(ftMsrpStateMachine10.mCanceledState);
                    return true;
                }
            }

            private void handleFTTimeout(ImError ftError) {
                IMnoStrategy.StrategyResponse strategyResponse = FtMsrpMessage.this.getRcsStrategy().handleFtFailure(ftError, FtMsrpMessage.this.mListener.onRequestChatType(FtMsrpMessage.this.mChatId));
                if (strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM || !FtMsrpMessage.this.mIsSlmSvcMsg) {
                    FtMsrpStateMachine.this.handleFTFailure(strategyResponse.getStatusCode(), ftError);
                    return;
                }
                FtMsrpStateMachine.this.logi("handleFTTimeout: FALLBACK_TO_LEGACY for slm FT");
                FtMsrpStateMachine.this.handleFTFailure(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY, ftError);
            }
        }

        final class CanceledState extends State {
            CanceledState() {
            }

            public void enter() {
                FtMsrpStateMachine.this.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                if (FtMsrpMessage.this.mIsBootup) {
                    FtMsrpStateMachine.this.logi("Message is loaded from bootup, no need for notifications");
                    FtMsrpMessage.this.mIsBootup = false;
                    return;
                }
                IMnoStrategy mnoStrategy = FtMsrpMessage.this.getRcsStrategy();
                if (mnoStrategy == null) {
                    FtMsrpStateMachine.this.loge("mnoStrategy is null");
                    return;
                }
                if (FtMsrpMessage.this.mIsSlmSvcMsg) {
                    FtMsrpMessage.this.mResumableOptionCode = FtResumableOption.NOTRESUMABLE.getId();
                } else {
                    FtMsrpMessage.this.mResumableOptionCode = mnoStrategy.getftResumableOption(FtMsrpMessage.this.mCancelReason, FtMsrpMessage.this.mIsGroupChat, FtMsrpMessage.this.mDirection, FtMsrpMessage.this.getTransferMech()).getId();
                }
                FtMsrpStateMachine.this.logi(getName() + " mResumableOptionCode: " + FtMsrpMessage.this.mResumableOptionCode);
                FtMsrpMessage.this.updateStatus(ImConstants.Status.FAILED);
                int ftResumeTimer = mnoStrategy.getNextFileTransferAutoResumeTimer(FtMsrpMessage.this.mDirection, FtMsrpMessage.this.mRetryCount);
                if (FtMsrpMessage.this.mResumableOptionCode == FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE.getId() && ftResumeTimer >= 0) {
                    FtMsrpStateMachine.this.logi(getName() + " start ft auto resume timer: " + ftResumeTimer);
                    if (ftResumeTimer < 10) {
                        FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                        ftMsrpStateMachine.sendMessageDelayed(ftMsrpStateMachine.obtainMessage(21), ((long) ftResumeTimer) * 1000);
                    } else {
                        PreciseAlarmManager.getInstance(FtMsrpMessage.this.getContext()).sendMessageDelayed(getClass().getSimpleName(), FtMsrpStateMachine.this.obtainMessage(21), ((long) ftResumeTimer) * 1000);
                    }
                    FtMsrpMessage.this.mRetryCount++;
                }
                ImsUri unused = FtMsrpMessage.this.mNewContactValueUri = null;
                boolean unused2 = FtMsrpMessage.this.mSwapUriType = false;
                FtMsrpMessage.this.mListener.onTransferCanceled(FtMsrpMessage.this);
                FtMsrpMessage.this.mIsConferenceUriChanged = false;
                FtMsrpStateMachine.this.removeMessages(23);
                FtMsrpMessage.this.invokeFtQueueCallBack();
                FtMsrpMessage.this.updateState();
                FtMsrpMessage.this.releaseWakeLock();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                boolean z = false;
                if (i == 1) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    if (msg.arg1 == 1) {
                        z = true;
                    }
                    ftMsrpStateMachine.onAttachFile(z);
                    return true;
                } else if (i == 8) {
                    FtMsrpMessage.this.mCancelReason = (CancelReason) msg.obj;
                    FtMsrpMessage.this.mListener.onTransferCanceled(FtMsrpMessage.this);
                    FtMsrpMessage.this.invokeFtQueueCallBack();
                    return true;
                } else if (i == 10) {
                    FtMsrpStateMachine.this.removeMessages(21);
                    FtIncomingSessionEvent event = (FtIncomingSessionEvent) msg.obj;
                    if (!FtMsrpMessage.this.isOutgoing() && !FtMsrpMessage.this.getIsSlmSvcMsg() && event.mIsSlmSvcMsg) {
                        Log.i(FtMsrpMessage.this.LOG_TAG, "updateFtMsrpMessageInfo: service has been changed to SLM by sender.");
                        FtMsrpMessage.this.updateFtMessageInfo(event);
                        FtMsrpStateMachine.this.onFileTransferInviteReceived(false);
                        return true;
                    } else if (!FtMsrpMessage.this.isOutgoing() || event.mIsSlmSvcMsg || !FtMsrpMessage.this.getIsSlmSvcMsg()) {
                        FtMsrpMessage.this.mRawHandle = event.mRawHandle;
                        FtMsrpStateMachine.this.onFileTransferInviteReceived(true);
                        return true;
                    } else {
                        FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE, (FtIncomingSessionEvent) msg.obj);
                        FtMsrpMessage.this.invokeFtQueueCallBack();
                        return true;
                    }
                } else if (i != 16) {
                    switch (i) {
                        case 19:
                            FtMsrpMessage.this.mThumbnailPath = (String) ((AsyncResult) msg.obj).result;
                            FtMsrpStateMachine.this.onCreateThumbnail();
                            return true;
                        case 20:
                            FtMsrpStateMachine.this.onHandleFileResizeResponse((FileResizeResponse) msg.obj);
                            return true;
                        case 21:
                            if (FtMsrpMessage.this.mListener.onRequestRegistrationType() != null) {
                                FtMsrpStateMachine.this.removeMessages(21);
                                PreciseAlarmManager.getInstance(FtMsrpMessage.this.getContext()).removeMessage(FtMsrpStateMachine.this.obtainMessage(21));
                                FtMsrpMessage.this.mListener.onAutoResumeTransfer(FtMsrpMessage.this);
                                return true;
                            }
                            FtMsrpStateMachine.this.logi("unregistered, schedule auto resume");
                            int ftResumeTimer = FtMsrpMessage.this.getRcsStrategy().getNextFileTransferAutoResumeTimer(FtMsrpMessage.this.mDirection, FtMsrpMessage.this.mRetryCount);
                            if (FtMsrpMessage.this.mResumableOptionCode != FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE.getId() || ftResumeTimer < 0) {
                                return true;
                            }
                            FtMsrpStateMachine.this.logi(getName() + " start ft auto resume timer: " + ftResumeTimer);
                            if (ftResumeTimer < 10) {
                                FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                                ftMsrpStateMachine2.sendMessageDelayed(ftMsrpStateMachine2.obtainMessage(21), ((long) ftResumeTimer) * 1000);
                            } else {
                                PreciseAlarmManager.getInstance(FtMsrpMessage.this.getContext()).sendMessageDelayed(getClass().getSimpleName(), FtMsrpStateMachine.this.obtainMessage(21), ((long) ftResumeTimer) * 1000);
                            }
                            FtMsrpMessage.this.mRetryCount++;
                            return true;
                        default:
                            return false;
                    }
                } else {
                    FtMsrpStateMachine.this.onAttachSlmFile();
                    return true;
                }
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.FtMsrpMessage$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode;

        static {
            int[] iArr = new int[FtTransferProgressEvent.State.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State = iArr;
            try {
                iArr[FtTransferProgressEvent.State.TRANSFERRING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[FtTransferProgressEvent.State.CANCELED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[FtTransferProgressEvent.State.COMPLETED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[FtTransferProgressEvent.State.INTERRUPTED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            int[] iArr2 = new int[IMnoStrategy.StatusCode.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode = iArr2;
            try {
                iArr2[IMnoStrategy.StatusCode.RETRY_IMMEDIATE.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.RETRY_AFTER.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.RETRY_AFTER_SESSION.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.RETRY_WITH_NEW_CONTACT_HEADER.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.FALLBACK_TO_SLM.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY.ordinal()] = 6;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.DISPLAY_ERROR.ordinal()] = 7;
            } catch (NoSuchFieldError e11) {
            }
        }
    }
}
