package com.sec.internal.ims.servicemodules.im;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.RcsHqmAgent;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.UploadFileTask;
import com.sec.internal.ims.servicemodules.im.data.response.FileResizeResponse;
import com.sec.internal.ims.servicemodules.im.interfaces.IFtHttpXmlComposer;
import com.sec.internal.ims.servicemodules.im.listener.FtMessageListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.FileDurationUtil;
import com.sec.internal.ims.servicemodules.im.util.FtFallbackHttpUrlUtil;
import com.sec.internal.ims.servicemodules.im.util.FtHttpXmlComposer;
import com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import org.xmlpull.v1.XmlPullParserException;

public class FtHttpOutgoingMessage extends FtMessage {
    private static final int EVENT_RETRY_UPLOAD = 305;
    private static final int EVENT_SEND_MESSAGE_DONE = 304;
    private static final int EVENT_UPLOAD_CANCELED = 303;
    private static final int EVENT_UPLOAD_COMPLETED = 302;
    private static final int EVENT_UPLOAD_PROGRESS = 201;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = FtHttpOutgoingMessage.class.getSimpleName();
    /* access modifiers changed from: private */
    public boolean isUseDeaccentedFilePath;

    protected FtHttpOutgoingMessage(Builder<?> builder) {
        super(builder);
    }

    public static Builder<?> builder() {
        return new Builder2((AnonymousClass1) null);
    }

    public boolean isAutoResumable() {
        return !getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY);
    }

    public int getTransferMech() {
        return 1;
    }

    /* access modifiers changed from: protected */
    public FtMessage.FtStateMachine createFtStateMachine(String name, Looper looper) {
        return new FtHttpStateMachine("FtHttpOutgoingMessage#" + name, looper);
    }

    /* access modifiers changed from: protected */
    public void sendDeliveredNotification(Object rawHandle, String conversationId, String contributionId, Message onComplete, String ownImsi, boolean isGroupchat, boolean isBotSessionAnonymized) {
    }

    /* access modifiers changed from: protected */
    public void sendDisplayedNotification(Object rawHandle, String conversationId, String contributionId, Message onComplete, String ownImsi, boolean isGroupchat, boolean isBotSessionAnonymized) {
    }

    public void onSendMessageDone(Result result, IMnoStrategy.StrategyResponse strategyResponse) {
        String str = LOG_TAG;
        Log.i(str, "onSendMessageDone: mid = " + this.mId + ", mStatus = " + this.mStatus + ", mBody = " + IMSLog.checker(this.mBody));
        if (result.getImError() != ImError.SUCCESS) {
            updateStatus(ImConstants.Status.FAILED);
            this.mListener.onMessageSendingFailed(this, strategyResponse, result);
        } else if (this.mStatus != ImConstants.Status.SENT) {
            this.mListener.onTransferCompleted(this);
            setSentTimestamp(System.currentTimeMillis());
            updateStatus(ImConstants.Status.SENT);
            this.mListener.onMessageSendingSucceeded(this);
        }
    }

    /* access modifiers changed from: private */
    public void deleteTemporaryFile() {
        if (this.isUseDeaccentedFilePath) {
            if (new File(this.mDeaccentedFilePath).delete()) {
                Log.i(LOG_TAG, "Success to delete temporary file");
            } else {
                Log.e(LOG_TAG, "Fail to delete temporary file");
            }
        }
    }

    private int getImsRegistrationCurrentRat(int phoneId) {
        try {
            ImsRegistration[] regis = ImsRegistry.getRegistrationManager().getRegistrationInfoByPhoneId(phoneId);
            if (regis == null) {
                return -1;
            }
            for (ImsRegistration regi : regis) {
                if (regi.hasService("ft_http")) {
                    return regi.getCurrentRat();
                }
            }
            return -1;
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "getImsRegistrationCurrentRat: NullPointerException e = " + e);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public boolean sendRCSMInfoToHQM(String orst, String cause) {
        String str;
        String str2;
        int i;
        int phoneId = SimUtil.getDefaultPhoneId();
        Set<ImsUri> participantUris = this.mListener.onRequestParticipantUris(this.mChatId);
        boolean hasChatbotParticipant = !this.mIsGroupChat && ChatbotUriUtil.hasChatbotUri(participantUris);
        Map<String, String> rcsmKeys = new LinkedHashMap<>();
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_ORST, orst);
        String str3 = "0";
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MDIR, str3);
        if (this.mIsGroupChat) {
            str3 = "1";
        }
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MGRP, str3);
        if (hasChatbotParticipant) {
            str = "FT_CHATBOT";
        } else {
            str = "FT";
        }
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MTYP, str);
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MCID, getChatId());
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MIID, getImdnId());
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MSIZ, String.valueOf(getFileSize()));
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_PTCN, String.valueOf(participantUris.size()));
        String rat = String.valueOf(getImsRegistrationCurrentRat(phoneId));
        if (isWifiConnected()) {
            str2 = rat + DiagnosisConstants.RCSM_MRAT_WIFI_POSTFIX;
        } else {
            str2 = rat;
        }
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MRAT, str2);
        String fileName = getFileName();
        String fileExt = "";
        if (fileName != null && (i = fileName.lastIndexOf(46)) > -1) {
            fileExt = fileName.substring(i + 1);
        }
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_FTYP, fileExt);
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_FTRC, String.valueOf(getRetryCount()));
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_HTTP, cause);
        return RcsHqmAgent.sendRCSInfoToHQM(getContext(), DiagnosisConstants.FEATURE_RCSM, phoneId, rcsmKeys);
    }

    public static abstract class Builder<T extends Builder<T>> extends FtMessage.Builder<T> {
        public FtHttpOutgoingMessage build() {
            return new FtHttpOutgoingMessage(this);
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

    private class FtHttpStateMachine extends FtMessage.FtStateMachine {
        /* access modifiers changed from: private */
        public final AttachedState mAttachedState = new AttachedState(this, (AnonymousClass1) null);
        private final CanceledNeedToNotifyState mCanceledNeedToNotifyState = new CanceledNeedToNotifyState(this, (AnonymousClass1) null);
        /* access modifiers changed from: private */
        public final CanceledState mCanceledState = new CanceledState(this, (AnonymousClass1) null);
        /* access modifiers changed from: private */
        public final CancelingState mCancelingState = new CancelingState(this, (AnonymousClass1) null);
        /* access modifiers changed from: private */
        public final CompletedState mCompletedState = new CompletedState(this, (AnonymousClass1) null);
        protected final MappingTranslator<Integer, State> mDbStateTranslator = new MappingTranslator.Builder().map(0, this.mInitialState).map(6, this.mAttachedState).map(9, this.mCanceledState).map(2, this.mInProgressState).map(7, this.mCanceledState).map(4, this.mCanceledState).map(3, this.mCompletedState).map(10, this.mCanceledNeedToNotifyState).buildTranslator();
        private final DefaultState mDefaultState = new DefaultState(this, (AnonymousClass1) null);
        /* access modifiers changed from: private */
        public final InProgressState mInProgressState = new InProgressState(this, (AnonymousClass1) null);
        private final InitialState mInitialState = new InitialState(this, (AnonymousClass1) null);
        private final SendingState mSendingState = new SendingState();
        protected final MappingTranslator<IState, Integer> mStateTranslator = new MappingTranslator.Builder().map(this.mInitialState, 0).map(this.mAttachedState, 6).map(this.mSendingState, 9).map(this.mInProgressState, 2).map(this.mCancelingState, 7).map(this.mCanceledState, 4).map(this.mCompletedState, 3).map(this.mCanceledNeedToNotifyState, 10).buildTranslator();

        protected FtHttpStateMachine(String name, Looper looper) {
            super(name, looper);
        }

        /* access modifiers changed from: protected */
        public void initState(State currentState) {
            addState(this.mDefaultState);
            addState(this.mInitialState, this.mDefaultState);
            addState(this.mAttachedState, this.mDefaultState);
            addState(this.mSendingState, this.mDefaultState);
            addState(this.mInProgressState, this.mDefaultState);
            addState(this.mCancelingState, this.mDefaultState);
            addState(this.mCanceledState, this.mInitialState);
            addState(this.mCompletedState, this.mInitialState);
            addState(this.mCanceledNeedToNotifyState, this.mDefaultState);
            String access$900 = FtHttpOutgoingMessage.LOG_TAG;
            Log.i(access$900, "setting current state as " + currentState.getName() + " for messageId : " + FtHttpOutgoingMessage.this.mId);
            setInitialState(currentState);
            start();
        }

        /* access modifiers changed from: private */
        public void handleFTHttpFailure() {
            logi("handleFTHttpFailure");
            IMnoStrategy.StrategyResponse response = FtHttpOutgoingMessage.this.getRcsStrategy().handleFtHttpRequestFailure(FtHttpOutgoingMessage.this.mCancelReason, FtHttpOutgoingMessage.this.mDirection, FtHttpOutgoingMessage.this.mIsGroupChat);
            boolean hasChatbotParticipant = ChatbotUriUtil.hasChatbotUri(FtHttpOutgoingMessage.this.mListener.onRequestParticipantUris(FtHttpOutgoingMessage.this.mChatId));
            if (response.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM || hasChatbotParticipant) {
                if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail() && !FtHttpOutgoingMessage.this.mIsGroupChat && !hasChatbotParticipant) {
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                }
                transitionTo(this.mCanceledState);
            } else if (FtHttpOutgoingMessage.this.mFileSize <= FtHttpOutgoingMessage.this.mConfig.getSlmMaxMsgSize()) {
                FtHttpOutgoingMessage.this.mIsSlmSvcMsg = true;
                if (FtHttpOutgoingMessage.this.sendSlmFile(obtainMessage(12))) {
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.UNKNOWN;
                    transitionTo(this.mSendingState);
                    return;
                }
                if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail() && !FtHttpOutgoingMessage.this.mIsGroupChat) {
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                }
                transitionTo(this.mCanceledState);
            } else if (!FtHttpOutgoingMessage.this.mIsResizable || !FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_LARGE_MSG_RESIZING)) {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "File size is greater than allowed MaxSlmSize mFileSize:" + FtHttpOutgoingMessage.this.mFileSize + ", SLMMaxMsgSize:" + FtHttpOutgoingMessage.this.mConfig.getSlmMaxMsgSize());
                if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail() && !FtHttpOutgoingMessage.this.mIsGroupChat) {
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                }
                transitionTo(this.mCanceledState);
            } else {
                FtHttpOutgoingMessage.this.mIsSlmSvcMsg = true;
                logi("request resizing for LMM");
                FtMessageListener ftMessageListener = FtHttpOutgoingMessage.this.mListener;
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                ftMessageListener.onFileResizingNeeded(ftHttpOutgoingMessage, ftHttpOutgoingMessage.mConfig.getSlmMaxMsgSize());
                transitionTo(this.mSendingState);
            }
        }

        /* access modifiers changed from: private */
        public void handleTransferProgress(FtTransferProgressEvent event) {
            if (!Objects.equals(FtHttpOutgoingMessage.this.mRawHandle, event.mRawHandle)) {
                logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtHttpOutgoingMessage.this.mRawHandle + ", event.mRawHandle=" + event.mRawHandle);
                return;
            }
            removeMessages(23);
            sendMessageDelayed(obtainMessage(23), 300000);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[event.mState.ordinal()];
            if (i == 1) {
                FtHttpOutgoingMessage.this.updateTransferredBytes(event.mTransferred);
                FtHttpOutgoingMessage.this.mListener.onTransferProgressReceived(FtHttpOutgoingMessage.this);
            } else if (i == 2) {
                if (getCurrentState() == this.mInProgressState) {
                    FtHttpOutgoingMessage.this.mCancelReason = FtMessage.translateToCancelReason(event.mReason.getImError());
                }
                if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail() && !FtHttpOutgoingMessage.this.mIsGroupChat) {
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                }
                transitionTo(this.mCanceledState);
            } else if (i == 3) {
                transitionTo(this.mCompletedState);
            } else if (i == 4) {
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage.mTransferredBytes = (ftHttpOutgoingMessage.mFileSize - event.mTotal) + event.mTransferred;
                logi("INTERRUPTED mFileSize: " + FtHttpOutgoingMessage.this.mFileSize + " mTotal: " + event.mTotal + " mTransferred: " + event.mTransferred);
                if (getCurrentState() == this.mInProgressState) {
                    FtHttpOutgoingMessage.this.mCancelReason = FtMessage.translateToCancelReason(event.mReason.getImError());
                }
                if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail() && !FtHttpOutgoingMessage.this.mIsGroupChat) {
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                }
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

        private final class DefaultState extends State {
            private DefaultState() {
            }

            /* synthetic */ DefaultState(FtHttpStateMachine x0, AnonymousClass1 x1) {
                this();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (FtHttpStateMachine.this.getCurrentState() == null) {
                    return false;
                }
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.e(access$900, "Unexpected event, current state is " + FtHttpStateMachine.this.getCurrentState().getName() + " event: " + msg.what);
                return false;
            }
        }

        private final class InitialState extends State {
            private InitialState() {
            }

            /* synthetic */ InitialState(FtHttpStateMachine x0, AnonymousClass1 x1) {
                this();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    long maxSizeFileTr = Math.max(FtHttpOutgoingMessage.this.mConfig.getMaxSizeExtraFileTr(), FtHttpOutgoingMessage.this.mConfig.getMaxSizeFileTr());
                    if (maxSizeFileTr == 0 || FtHttpOutgoingMessage.this.mFileSize <= maxSizeFileTr) {
                        FtHttpOutgoingMessage.this.mListener.onTransferCreated(FtHttpOutgoingMessage.this);
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.transitionTo(ftHttpStateMachine.mAttachedState);
                        return true;
                    }
                    String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                    Log.e(access$900, "Attached file (" + FtHttpOutgoingMessage.this.mFileSize + ") exceeds MaxSizeFileTr (" + maxSizeFileTr + ")");
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.TOO_LARGE;
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                    return true;
                } else if (i == 8) {
                    FtHttpOutgoingMessage.this.mCancelReason = (CancelReason) msg.obj;
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                    return true;
                } else if (i != 16) {
                    return false;
                } else {
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.ERROR;
                    FtHttpStateMachine.this.handleFTHttpFailure();
                    return true;
                }
            }
        }

        private final class AttachedState extends State {
            private AttachedState() {
            }

            /* synthetic */ AttachedState(FtHttpStateMachine x0, AnonymousClass1 x1) {
                this();
            }

            public void enter() {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "AttachedState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
            }

            public boolean processMessage(Message msg) {
                if (msg.what != 11) {
                    return false;
                }
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                return true;
            }
        }

        final class SendingState extends State {
            SendingState() {
            }

            public void enter() {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "SendingState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateStatus(ImConstants.Status.SENDING);
                FtHttpOutgoingMessage.this.updateState();
                FtHttpOutgoingMessage.this.acquireWakeLock();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 8) {
                    FtHttpOutgoingMessage.this.mCancelReason = (CancelReason) msg.obj;
                    if (FtHttpOutgoingMessage.this.mRawHandle == null) {
                        Log.i(FtHttpOutgoingMessage.LOG_TAG, "mRawHandle is null");
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                        return true;
                    }
                    FtHttpOutgoingMessage.this.sendCancelFtSession(FtHttpOutgoingMessage.this.mCancelReason);
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCancelingState);
                    return true;
                } else if (i == 12) {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        return true;
                    }
                    FtResult result = (FtResult) ar.result;
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    ftHttpStateMachine3.logi("SLM send file done : " + result.mRawHandle);
                    if (result.getImError() == ImError.SUCCESS) {
                        FtHttpOutgoingMessage.this.mRawHandle = result.mRawHandle;
                        FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                        ftHttpStateMachine4.transitionTo(ftHttpStateMachine4.mInProgressState);
                        return true;
                    }
                    if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail() && !FtHttpOutgoingMessage.this.mIsGroupChat) {
                        FtHttpOutgoingMessage.this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                    }
                    FtHttpStateMachine ftHttpStateMachine5 = FtHttpStateMachine.this;
                    ftHttpStateMachine5.transitionTo(ftHttpStateMachine5.mCanceledState);
                    return true;
                } else if (i != 20) {
                    return false;
                } else {
                    FileResizeResponse resizeResponse = (FileResizeResponse) msg.obj;
                    if (FtHttpOutgoingMessage.this.validateFileResizeResponse(resizeResponse)) {
                        File file = new File(resizeResponse.resizedFilePath);
                        FtHttpOutgoingMessage.this.mFileSize = file.length();
                        FtHttpOutgoingMessage.this.mFileName = file.getName();
                        FtHttpOutgoingMessage.this.mFilePath = resizeResponse.resizedFilePath;
                        FtHttpOutgoingMessage.this.triggerObservers(ImCacheAction.UPDATED);
                        if (FtHttpOutgoingMessage.this.sendSlmFile(FtHttpStateMachine.this.obtainMessage(12))) {
                            return true;
                        }
                        FtHttpStateMachine ftHttpStateMachine6 = FtHttpStateMachine.this;
                        ftHttpStateMachine6.transitionTo(ftHttpStateMachine6.mCanceledState);
                        return true;
                    }
                    if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail() && !FtHttpOutgoingMessage.this.mIsGroupChat) {
                        FtHttpOutgoingMessage.this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                    }
                    FtHttpStateMachine ftHttpStateMachine7 = FtHttpStateMachine.this;
                    ftHttpStateMachine7.transitionTo(ftHttpStateMachine7.mCanceledState);
                    return true;
                }
            }
        }

        private final class InProgressState extends State {
            UploadFileTask uploadTask;

            private InProgressState() {
            }

            /* synthetic */ InProgressState(FtHttpStateMachine x0, AnonymousClass1 x1) {
                this();
            }

            public void enter() {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "InProgressState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpStateMachine.this.removeMessages(305);
                FtHttpOutgoingMessage.this.setRetryCount(0);
                FtHttpOutgoingMessage.this.updateState();
                if (FtHttpOutgoingMessage.this.mIsBootup && (FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY) || FtHttpOutgoingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_CANCEL_FOR_DEREGI) > 0)) {
                    Log.i(FtHttpOutgoingMessage.LOG_TAG, "Do not auto resume message loaded from bootup");
                    FtHttpOutgoingMessage.this.mIsBootup = false;
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.DEVICE_UNREGISTERED;
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                } else if (!FtHttpOutgoingMessage.this.mIsSlmSvcMsg) {
                    tryUpload();
                }
                FtHttpOutgoingMessage.this.acquireWakeLock();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i != 1) {
                    if (i == 3) {
                        FtHttpStateMachine.this.handleTransferProgress((FtTransferProgressEvent) msg.obj);
                        return true;
                    } else if (i == 8) {
                        handleCancelTransfer((CancelReason) msg.obj);
                        return true;
                    } else if (i != 11) {
                        if (i == 23) {
                            FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                            ftHttpStateMachine.logi("EVENT_TRANSFER_TIMER_TIMEOUT : " + FtHttpOutgoingMessage.this.mId);
                            FtHttpOutgoingMessage.this.cancelTransfer(CancelReason.CANCELED_BY_SYSTEM);
                            return true;
                        } else if (i != 201) {
                            switch (i) {
                                case 50:
                                    FtHttpStateMachine.this.removeMessages(51);
                                    tryUpload();
                                    return true;
                                case 51:
                                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                                    ftHttpStateMachine2.sendMessage(ftHttpStateMachine2.obtainMessage(303, -1, -1, CancelReason.ERROR));
                                    return true;
                                case 52:
                                    String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                                    Log.i(access$900, "EVENT_DELAY_CANCEL_TRANSFER mId=" + FtHttpOutgoingMessage.this.mId);
                                    UploadFileTask uploadFileTask = this.uploadTask;
                                    if (uploadFileTask != null) {
                                        uploadFileTask.cancel(true);
                                        this.uploadTask = null;
                                    }
                                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                                    return true;
                                default:
                                    switch (i) {
                                        case 302:
                                            handleUploadCompleted((String) msg.obj);
                                            return true;
                                        case 303:
                                            handleUploadCanceled(msg);
                                            return true;
                                        case 304:
                                            return true;
                                        case 305:
                                            handleRetryUpload(msg.arg2);
                                            return true;
                                        default:
                                            return false;
                                    }
                            }
                        } else {
                            FtHttpOutgoingMessage.this.updateTransferredBytes(((Long) msg.obj).longValue());
                            String access$9002 = FtHttpOutgoingMessage.LOG_TAG;
                            Log.i(access$9002, "EVENT_UPLOAD_PROGRESS " + FtHttpOutgoingMessage.this.mTransferredBytes + "/" + FtHttpOutgoingMessage.this.mFileSize);
                            FtHttpOutgoingMessage.this.mListener.onTransferProgressReceived(FtHttpOutgoingMessage.this);
                            return true;
                        }
                    }
                }
                FtHttpStateMachine.this.removeMessages(305);
                PreciseAlarmManager.getInstance(FtHttpOutgoingMessage.this.getContext()).removeMessage(FtHttpStateMachine.this.obtainMessage(52));
                tryUpload();
                return true;
            }

            private void handleCancelTransfer(CancelReason cancelReason) {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "EVENT_CANCEL_TRANSFER " + FtHttpOutgoingMessage.this.mId + " CancelReason " + cancelReason);
                FtHttpStateMachine.this.removeMessages(305);
                PreciseAlarmManager.getInstance(FtHttpOutgoingMessage.this.getContext()).removeMessage(FtHttpStateMachine.this.obtainMessage(52));
                if (FtHttpOutgoingMessage.this.mIsSlmSvcMsg) {
                    FtHttpOutgoingMessage.this.mCancelReason = cancelReason;
                    FtHttpOutgoingMessage.this.sendCancelFtSession(FtHttpOutgoingMessage.this.mCancelReason);
                    if (FtHttpOutgoingMessage.this.mCancelReason == CancelReason.CANCELED_BY_SYSTEM) {
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                        return;
                    }
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCancelingState);
                    return;
                }
                UploadFileTask uploadFileTask = this.uploadTask;
                if (uploadFileTask != null) {
                    uploadFileTask.cancel(true);
                    if (this.uploadTask.mHttpRequest != null) {
                        this.uploadTask.mHttpRequest.disconnect();
                    }
                    this.uploadTask = null;
                }
                if (cancelReason != CancelReason.DEVICE_UNREGISTERED || FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
                    FtHttpOutgoingMessage.this.mCancelReason = cancelReason;
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                    return;
                }
                int cancelDelay = FtHttpOutgoingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_CANCEL_FOR_DEREGI);
                if (cancelDelay > 0) {
                    PreciseAlarmManager.getInstance(FtHttpOutgoingMessage.this.getContext()).sendMessageDelayed(getClass().getSimpleName(), FtHttpStateMachine.this.obtainMessage(52), ((long) cancelDelay) * 1000);
                }
                FtHttpOutgoingMessage.this.updateResumeableOptionCode(FtHttpOutgoingMessage.this.getRcsStrategy().getftResumableOption(cancelReason, FtHttpOutgoingMessage.this.mIsGroupChat, FtHttpOutgoingMessage.this.mDirection, FtHttpOutgoingMessage.this.getTransferMech()).getId());
            }

            private void handleRetryUpload(int errorCode) {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "EVENT_RETRY_UPLOAD mId=" + FtHttpOutgoingMessage.this.mId + ", error: " + errorCode + "Retry count=" + FtHttpOutgoingMessage.this.getRetryCount() + "/" + 3);
                FtHttpStateMachine.this.removeMessages(305);
                if (!FtHttpOutgoingMessage.this.mMnoStrategy.isFTHTTPAutoResumeAndCancelPerConnectionChange() || FtHttpOutgoingMessage.this.checkAvailableRetry()) {
                    if (Mno.TMOUS != SimManagerFactory.getSimManager().getSimMno()) {
                        boolean unused = FtHttpOutgoingMessage.this.isUseDeaccentedFilePath = !FtHttpOutgoingMessage.this.mFilePath.equals(FtHttpOutgoingMessage.this.mDeaccentedFilePath) && (errorCode == 500 || FtHttpOutgoingMessage.this.isUseDeaccentedFilePath);
                        String access$9002 = FtHttpOutgoingMessage.LOG_TAG;
                        Log.i(access$9002, "Using deaccented file: " + FtHttpOutgoingMessage.this.isUseDeaccentedFilePath);
                    }
                    if (FtHttpOutgoingMessage.this.getRetryCount() < 3) {
                        FtHttpOutgoingMessage.this.setRetryCount(FtHttpOutgoingMessage.this.getRetryCount() + 1);
                        tryUpload();
                        return;
                    }
                    if (errorCode > 0) {
                        FtHttpOutgoingMessage.this.mTransferredBytes = 0;
                    }
                    FtHttpStateMachine.this.handleFTHttpFailure();
                } else if (FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                }
            }

            private void handleUploadCompleted(String result) {
                if (TextUtils.isEmpty(result)) {
                    FtHttpStateMachine.this.handleFTHttpFailure();
                    return;
                }
                FtHttpOutgoingMessage.this.mBody = result;
                FtHttpOutgoingMessage.this.mContentType = MIMEContentType.FT_HTTP;
                FtHttpOutgoingMessage.this.mFileExpire = null;
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.d(access$900, "EVENT_UPLOAD_COMPLETED Result = " + result);
                try {
                    FtHttpFileInfo fileInfo = FtHttpXmlParser.parse(FtHttpOutgoingMessage.this.mBody);
                    if (fileInfo != null) {
                        FtHttpOutgoingMessage.this.mFileBrandedUrl = fileInfo.getBrandedUrl();
                        FtHttpOutgoingMessage.this.mFileExpire = fileInfo.getDataUntil();
                        FtHttpOutgoingMessage.this.mFileDataUrl = fileInfo.getDataUrl().toString();
                        boolean areFallbackParamsPresent = FtFallbackHttpUrlUtil.areFallbackParamsPresent(FtHttpOutgoingMessage.this.mFileDataUrl);
                        boolean isAudioMessage = false;
                        if (FileDisposition.RENDER == FtHttpOutgoingMessage.this.mFileDisposition) {
                            FtHttpOutgoingMessage.this.mPlayingLength = FileDurationUtil.getFileDurationTime(FtHttpOutgoingMessage.this.mFilePath) / 1000;
                            if (FtHttpOutgoingMessage.this.mPlayingLength >= 0) {
                                Log.w(FtHttpOutgoingMessage.LOG_TAG, "Assumed that Audio Message is being sent!");
                                isAudioMessage = true;
                                IFtHttpXmlComposer xmlComposer = new FtHttpXmlComposer();
                                FtHttpOutgoingMessage.this.mBody = xmlComposer.composeXmlForAudioMessage(fileInfo, FtHttpOutgoingMessage.this.mPlayingLength);
                            }
                        }
                        if (ImsProfile.isRcsUpProfile(FtHttpOutgoingMessage.this.mConfig.getRcsProfile())) {
                            if (!areFallbackParamsPresent) {
                                Log.i(FtHttpOutgoingMessage.LOG_TAG, "Fallback params are not present in the content URL returned from fthttp content server!");
                                FtHttpOutgoingMessage.this.mFileDataUrl = FtFallbackHttpUrlUtil.addFtFallbackParams(FtHttpOutgoingMessage.this.mFileDataUrl, fileInfo.getFileSize(), fileInfo.getContentType(), fileInfo.getDataUntil());
                                fileInfo.setData(new FtHttpFileInfo.Data(new URL(FtHttpOutgoingMessage.this.mFileDataUrl), fileInfo.getDataUntil()));
                            }
                            if (isAudioMessage) {
                                FtHttpOutgoingMessage.this.mFileDataUrl = FtFallbackHttpUrlUtil.addDurationFtFallbackParam(FtHttpOutgoingMessage.this.mFileDataUrl, FtHttpOutgoingMessage.this.mPlayingLength);
                                fileInfo.setData(new FtHttpFileInfo.Data(new URL(FtHttpOutgoingMessage.this.mFileDataUrl), fileInfo.getDataUntil()));
                            }
                        }
                    }
                    String access$9002 = FtHttpOutgoingMessage.LOG_TAG;
                    Log.i(access$9002, "EVENT_UPLOAD_COMPLETED file expiration: " + FtHttpOutgoingMessage.this.mFileExpire);
                } catch (IOException | NullPointerException | XmlPullParserException e) {
                    e.printStackTrace();
                }
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCompletedState);
            }

            private void handleUploadCanceled(Message msg) {
                if (msg.obj != null) {
                    FtHttpOutgoingMessage.this.mCancelReason = (CancelReason) msg.obj;
                }
                int retryTime = FtHttpOutgoingMessage.this.getRcsStrategy().getFtHttpRetryInterval(msg.arg1, FtHttpOutgoingMessage.this.getRetryCount());
                if (retryTime >= 0) {
                    String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                    Log.i(access$900, "EVENT_UPLOAD_CANCELED: " + FtHttpOutgoingMessage.this.mId + " retry upload after " + retryTime + " secs");
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.sendMessageDelayed(ftHttpStateMachine.obtainMessage(305, 0, msg.arg2), ((long) retryTime) * 1000);
                    return;
                }
                FtHttpStateMachine.this.handleFTHttpFailure();
            }

            /* Debug info: failed to restart local var, previous not found, register: 8 */
            private void copyToTemporaryFile(File src, File dst) {
                FileOutputStream outStream;
                if (FtHttpOutgoingMessage.this.isUseDeaccentedFilePath) {
                    Log.i(FtHttpOutgoingMessage.LOG_TAG, "Copying file to temporal directory with new file name");
                    try {
                        FileInputStream inStream = new FileInputStream(src);
                        try {
                            outStream = new FileOutputStream(dst);
                            FileChannel inChannel = inStream.getChannel();
                            inChannel.transferTo(0, inChannel.size(), outStream.getChannel());
                            outStream.close();
                            inStream.close();
                            return;
                        } catch (Throwable th) {
                            inStream.close();
                            throw th;
                        }
                    } catch (IOException e) {
                        Log.e(FtHttpOutgoingMessage.LOG_TAG, e.getMessage());
                        return;
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                } else {
                    return;
                }
                throw th;
            }

            private void tryUpload() {
                FtHttpOutgoingMessage.this.mIsWifiUsed = FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FT_WIFI_DISCONNECTED) && FtHttpOutgoingMessage.this.isWifiConnected();
                if (FtHttpOutgoingMessage.this.isUseDeaccentedFilePath) {
                    FtHttpOutgoingMessage.this.mTransferredBytes = 0;
                    copyToTemporaryFile(new File(FtHttpOutgoingMessage.this.mFilePath), new File(FtHttpOutgoingMessage.this.mDeaccentedFilePath));
                }
                Uri FtHttpCsUri = FtHttpOutgoingMessage.this.getRcsStrategy().getFtHttpCsUri(FtHttpOutgoingMessage.this.mConfig, FtHttpOutgoingMessage.this.mListener.onRequestParticipantUris(FtHttpOutgoingMessage.this.mChatId), FtHttpOutgoingMessage.this.getExtraFt(), FtHttpOutgoingMessage.this.mIsGroupChat);
                if (FtHttpCsUri != null) {
                    UploadFileTask uploadFileTask = this.uploadTask;
                    if (uploadFileTask != null && uploadFileTask.getStatus() != AsyncTask.Status.FINISHED) {
                        Log.i(FtHttpOutgoingMessage.LOG_TAG, "Task is already running or pending.");
                    } else if (FtHttpOutgoingMessage.this.needToAcquireNetworkForFT()) {
                        FtHttpOutgoingMessage.this.acquireNetworkForFT(FtHttpOutgoingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY));
                    } else {
                        if (FtHttpOutgoingMessage.this.getFtCallback() == null) {
                            FtHttpOutgoingMessage.this.setFtCompleteCallback(FtHttpOutgoingMessage.this.mListener.onRequestCompleteCallback(FtHttpOutgoingMessage.this.mChatId));
                        }
                        if (FtHttpOutgoingMessage.this.mTransferredBytes > 0) {
                            this.uploadTask = new UploadResumeFileTask(FtHttpOutgoingMessage.this.mConfig.getPhoneId());
                            creatUploadFileTask(FtHttpCsUri.toString(), this.uploadTask);
                            return;
                        }
                        this.uploadTask = new UploadFileTask(FtHttpOutgoingMessage.this.mConfig.getPhoneId());
                        FtHttpOutgoingMessage.this.mFileTransferId = FtMessage.sTidGenerator.generate().toString();
                        creatUploadFileTask(FtHttpCsUri.toString(), this.uploadTask);
                    }
                } else {
                    Log.e(FtHttpOutgoingMessage.LOG_TAG, "getHttpCsUri is null, can't transfer file");
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(303, -1, -1, CancelReason.ERROR));
                }
            }

            private void creatUploadFileTask(String url, UploadFileTask uploadTask2) {
                Executor uploadThreadPool;
                final UploadFileTask uploadFileTask = uploadTask2;
                String path = FtHttpOutgoingMessage.this.isUseDeaccentedFilePath ? FtHttpOutgoingMessage.this.mDeaccentedFilePath : FtHttpOutgoingMessage.this.mFilePath;
                boolean isInternetPdn = FtHttpOutgoingMessage.this.getExtraFt() || FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FT_INTERNET_PDN);
                Executor uploadThreadPool2 = FtMessage.sFtHttpThreadPool;
                if (ImsProfile.isRcsUpProfile(FtHttpOutgoingMessage.this.mConfig.getRcsProfile())) {
                    uploadThreadPool = AsyncTask.SERIAL_EXECUTOR;
                } else {
                    uploadThreadPool = uploadThreadPool2;
                }
                UploadFileTask.UploadRequest[] uploadRequestArr = new UploadFileTask.UploadRequest[1];
                uploadRequestArr[0] = new UploadFileTask.UploadRequest(url, FtHttpOutgoingMessage.this.mFileSize, path, true, FtHttpOutgoingMessage.this.mFileTransferId, FtHttpOutgoingMessage.this.mConfig.getFtHttpCsUser(), FtHttpOutgoingMessage.this.mConfig.getFtHttpCsPwd(), FtHttpOutgoingMessage.this.getFtHttpUserAgent(), isInternetPdn ? null : FtHttpOutgoingMessage.this.mNetwork, FtHttpOutgoingMessage.this.mConfig.isFtHttpTrustAllCerts(), new UploadFileTask.UploadTaskCallback() {
                    public void onStarted() {
                        if (uploadFileTask instanceof UploadFileTask) {
                            Log.i(FtHttpOutgoingMessage.LOG_TAG, "Posting Started event");
                            FtHttpOutgoingMessage.this.mListener.onTransferInProgress(FtHttpOutgoingMessage.this);
                        }
                    }

                    public void onProgressUpdate(long transferred) {
                        FtHttpStateMachine.this.sendMessage(201, (Object) Long.valueOf(transferred));
                    }

                    public void onCompleted(String result) {
                        FtHttpStateMachine.this.sendMessage(302, (Object) result);
                        FtHttpOutgoingMessage.this.listToDumpFormat(LogClass.FT_HTTP_UPLOAD_COMPLETE, 0, new ArrayList<>());
                    }

                    public void onCanceled(CancelReason reason, int retryTime, int errorCode, boolean fullUploadNeeded) {
                        if (fullUploadNeeded) {
                            FtHttpOutgoingMessage.this.mTransferredBytes = 0;
                        }
                        FtHttpStateMachine.this.sendMessage(FtHttpStateMachine.this.obtainMessage(303, retryTime, errorCode, reason));
                        if (errorCode != -1) {
                            boolean unused = FtHttpOutgoingMessage.this.sendRCSMInfoToHQM(DiagnosisConstants.RCSM_ORST_HTTP, String.valueOf(errorCode));
                            List<String> dumps = new ArrayList<>();
                            dumps.add(String.valueOf(errorCode));
                            dumps.add(String.valueOf(FtHttpOutgoingMessage.this.getRetryCount()));
                            FtHttpOutgoingMessage.this.listToDumpFormat(LogClass.FT_HTTP_UPLOAD_CANCEL, 0, dumps);
                        }
                    }

                    public void onFinished() {
                    }
                }, FtHttpOutgoingMessage.this.mContentType);
                uploadFileTask.executeOnExecutor(uploadThreadPool, uploadRequestArr);
            }
        }

        private final class CancelingState extends State {
            private CancelingState() {
            }

            /* synthetic */ CancelingState(FtHttpStateMachine x0, AnonymousClass1 x1) {
                this();
            }

            public void enter() {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "CancelingState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 3) {
                    FtHttpStateMachine.this.handleTransferProgress((FtTransferProgressEvent) msg.obj);
                    return true;
                } else if (i == 23) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.logi("EVENT_TRANSFER_TIMER_TIMEOUT : " + FtHttpOutgoingMessage.this.mId);
                    FtHttpOutgoingMessage.this.cancelTransfer(CancelReason.CANCELED_BY_SYSTEM);
                    return true;
                } else if (i == 8) {
                    CancelReason reason = (CancelReason) msg.obj;
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.logi("cancel transfer in cancelingState reason = " + reason);
                    if (reason != CancelReason.CANCELED_BY_SYSTEM) {
                        return true;
                    }
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                    return true;
                } else if (i != 9) {
                    return false;
                } else {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar == null) {
                        return true;
                    }
                    String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                    Log.i(access$900, "CancelingState: cancel transfer result = " + ((FtResult) ar.result));
                    FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                    ftHttpStateMachine4.transitionTo(ftHttpStateMachine4.mCanceledState);
                    return true;
                }
            }
        }

        private final class CanceledState extends State {
            private CanceledState() {
            }

            /* synthetic */ CanceledState(FtHttpStateMachine x0, AnonymousClass1 x1) {
                this();
            }

            public void enter() {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "CanceledState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
                if (FtHttpOutgoingMessage.this.mIsSlmSvcMsg) {
                    FtHttpOutgoingMessage.this.mIsSlmSvcMsg = false;
                    FtHttpStateMachine.this.removeMessages(23);
                }
                if (FtHttpOutgoingMessage.this.mIsNetworkRequested) {
                    FtHttpOutgoingMessage.this.releaseNetworkAcquiredForFT();
                }
                if (FtHttpOutgoingMessage.this.mIsBootup) {
                    Log.i(FtHttpOutgoingMessage.LOG_TAG, "Message is loaded from bootup, no need for notifications");
                    FtHttpOutgoingMessage.this.mIsBootup = false;
                    return;
                }
                FtHttpOutgoingMessage.this.releaseWakeLock();
                FtHttpOutgoingMessage.this.mResumableOptionCode = FtHttpOutgoingMessage.this.getRcsStrategy().getftResumableOption(FtHttpOutgoingMessage.this.mCancelReason, FtHttpOutgoingMessage.this.mIsGroupChat, FtHttpOutgoingMessage.this.mDirection, FtHttpOutgoingMessage.this.getTransferMech()).getId();
                FtHttpOutgoingMessage.this.updateStatus(ImConstants.Status.FAILED);
                FtHttpOutgoingMessage.this.mListener.onTransferCanceled(FtHttpOutgoingMessage.this);
                FtHttpOutgoingMessage.this.setFtCompleteCallback((Message) null);
                FtHttpOutgoingMessage.this.deleteTemporaryFile();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i != 1) {
                    if (i == 8) {
                        FtHttpOutgoingMessage.this.mListener.onCancelRequestFailed(FtHttpOutgoingMessage.this);
                        return true;
                    } else if (i != 11) {
                        return false;
                    }
                }
                if (!FtHttpOutgoingMessage.this.mIsResuming) {
                    return true;
                }
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                return true;
            }
        }

        private final class CompletedState extends State {
            private CompletedState() {
            }

            /* synthetic */ CompletedState(FtHttpStateMachine x0, AnonymousClass1 x1) {
                this();
            }

            public void enter() {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "CompletedState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
                boolean isSlmMsg = FtHttpOutgoingMessage.this.mIsSlmSvcMsg;
                if (FtHttpOutgoingMessage.this.isFtSms() || isSlmMsg) {
                    FtHttpStateMachine.this.removeMessages(23);
                    FtHttpOutgoingMessage.this.updateStatus(ImConstants.Status.SENT);
                    FtHttpOutgoingMessage.this.setSentTimestamp(System.currentTimeMillis());
                    FtHttpOutgoingMessage.this.mIsSlmSvcMsg = false;
                }
                if (FtHttpOutgoingMessage.this.mIsNetworkRequested) {
                    FtHttpOutgoingMessage.this.releaseNetworkAcquiredForFT();
                }
                if (FtHttpOutgoingMessage.this.mIsBootup) {
                    Log.i(FtHttpOutgoingMessage.LOG_TAG, "Message is loaded from bootup, no need for notifications");
                    FtHttpOutgoingMessage.this.mIsBootup = false;
                    return;
                }
                FtHttpOutgoingMessage.this.releaseWakeLock();
                if (FtHttpOutgoingMessage.this.isFtSms() || isSlmMsg) {
                    FtHttpOutgoingMessage.this.mListener.onTransferCompleted(FtHttpOutgoingMessage.this);
                } else {
                    FtHttpOutgoingMessage.this.invokeFtQueueCallBack();
                }
                FtHttpOutgoingMessage.this.deleteTemporaryFile();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i != 1) {
                    if (i != 8) {
                        return false;
                    }
                    FtHttpOutgoingMessage.this.mListener.onCancelRequestFailed(FtHttpOutgoingMessage.this);
                    return true;
                } else if (!FtHttpOutgoingMessage.this.mIsResuming) {
                    return true;
                } else {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                    return true;
                }
            }
        }

        private final class CanceledNeedToNotifyState extends State {
            private CanceledNeedToNotifyState() {
            }

            /* synthetic */ CanceledNeedToNotifyState(FtHttpStateMachine x0, AnonymousClass1 x1) {
                this();
            }

            public void enter() {
                String access$900 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(access$900, "CanceledState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
                FtHttpOutgoingMessage.this.mResumableOptionCode = 0;
                FtHttpOutgoingMessage.this.updateStatus(ImConstants.Status.FAILED);
                FtHttpOutgoingMessage.this.mListener.onTransferCanceled(FtHttpOutgoingMessage.this);
                if (!FtHttpOutgoingMessage.this.isFtSms()) {
                    FtHttpOutgoingMessage.this.invokeFtQueueCallBack();
                }
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                return false;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State;

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
        }
    }
}
