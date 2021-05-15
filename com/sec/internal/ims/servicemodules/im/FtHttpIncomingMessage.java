package com.sec.internal.ims.servicemodules.im;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.helper.BlockedNumberUtil;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.State;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.config.util.AKAEapAuthHelper;
import com.sec.internal.ims.servicemodules.im.DownloadFileTask;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParserException;

public class FtHttpIncomingMessage extends FtMessage {
    private static final int EVENT_DOWNLOAD_CANCELED = 103;
    private static final int EVENT_DOWNLOAD_COMPLETED = 102;
    private static final int EVENT_DOWNLOAD_PROGRESS = 101;
    private static final int EVENT_DOWNLOAD_THUMBNAIL_CANCELED = 106;
    private static final int EVENT_DOWNLOAD_THUMBNAIL_COMPLETED = 104;
    private static final int EVENT_DOWNLOAD_THUMBNAIL_FAILED = 105;
    private static final int EVENT_RETRY_DOWNLOAD = 107;
    private static final int EVENT_RETRY_THUMBNAIL_DOWNLOAD = 108;
    private static final Pattern GSMA_FT_HTTP_URL_PATTERN = Pattern.compile("https://ftcontentserver\\.rcs\\.mnc\\d{3}\\.mcc\\d{3}\\.pub\\.3gppnetwork\\.org");
    /* access modifiers changed from: private */
    public static final String LOG_TAG = FtHttpIncomingMessage.class.getSimpleName();
    /* access modifiers changed from: private */
    public URL mDataUrl;

    protected FtHttpIncomingMessage(Builder<?> builder) {
        super(builder);
        String str = LOG_TAG;
        Log.i(str, "data url=" + IMSLog.checker(builder.mDataUrl));
        try {
            if (builder.mDataUrl != null) {
                this.mDataUrl = new URL(builder.mDataUrl);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Malformed data url");
        }
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    public void receiveTransfer() {
        this.mStateMachine.sendMessage(this.mStateMachine.obtainMessage(10));
    }

    public int getTransferMech() {
        return 1;
    }

    public boolean isAutoResumable() {
        return !getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY);
    }

    public String getDataUrl() {
        URL url = this.mDataUrl;
        if (url != null) {
            return url.toString();
        }
        return null;
    }

    /* access modifiers changed from: private */
    public boolean isFtHttpUrlTrusted(String url) {
        if (GSMA_FT_HTTP_URL_PATTERN.matcher(url).find()) {
            return true;
        }
        for (String template : getRcsStrategy().stringArraySetting(RcsPolicySettings.RcsPolicy.FTHTTP_NON_STANDARD_URLS)) {
            if (url.startsWith(template)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public FtMessage.FtStateMachine createFtStateMachine(String name, Looper looper) {
        return new FtHttpStateMachine("FtHttpIncomingMessage#" + name, looper);
    }

    public Map<String, String> getParamsforDl(String url) {
        Map<String, String> params = new HashMap<>();
        if (!TextUtils.isEmpty(this.mConfig.getFtHttpDLUrl())) {
            params.put(ImsConstants.FtDlParams.FT_DL_URL, url);
            if (!TextUtils.isEmpty(this.mImdnId)) {
                params.put("id", this.mImdnId);
            }
            if (!TextUtils.isEmpty(this.mConversationId)) {
                params.put(ImsConstants.FtDlParams.FT_DL_CONV_ID, this.mConversationId);
            }
            if (this.mRemoteUri != null) {
                params.put(ImsConstants.FtDlParams.FT_DL_OTHER_PARTY, this.mRemoteUri.toString());
            }
        }
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.IS_EAP_SUPPORTED)) {
            params.put("EAP_ID", AKAEapAuthHelper.composeRootNai(this.mConfig.getPhoneId()));
        }
        return params;
    }

    public static abstract class Builder<T extends Builder<T>> extends FtMessage.Builder<T> {
        /* access modifiers changed from: private */
        public String mDataUrl;

        public T dataUrl(String dataUrl) {
            this.mDataUrl = dataUrl;
            return (Builder) self();
        }

        public FtHttpIncomingMessage build() {
            return new FtHttpIncomingMessage(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        private Builder2() {
        }

        /* access modifiers changed from: protected */
        public Builder2 self() {
            return this;
        }
    }

    private class FtHttpStateMachine extends FtMessage.FtStateMachine {
        /* access modifiers changed from: private */
        public final AcceptingState mAcceptingState = new AcceptingState();
        /* access modifiers changed from: private */
        public final CanceledState mCanceledState = new CanceledState();
        /* access modifiers changed from: private */
        public final CompletedState mCompletedState = new CompletedState();
        protected final MappingTranslator<Integer, State> mDbStateTranslator = new MappingTranslator.Builder().map(0, this.mInitialState).map(1, this.mAcceptingState).map(2, this.mInProgressState).map(4, this.mCanceledState).map(3, this.mCompletedState).buildTranslator();
        private final DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public final InProgressState mInProgressState = new InProgressState();
        private final InitialState mInitialState = new InitialState();
        protected final MappingTranslator<IState, Integer> mStateTranslator = new MappingTranslator.Builder().map(this.mInitialState, 0).map(this.mAcceptingState, 1).map(this.mInProgressState, 2).map(this.mCanceledState, 4).map(this.mCompletedState, 3).buildTranslator();

        protected FtHttpStateMachine(String name, Looper looper) {
            super(name, looper);
        }

        /* access modifiers changed from: protected */
        public void initState(State currentState) {
            addState(this.mDefaultState);
            addState(this.mInitialState, this.mDefaultState);
            addState(this.mAcceptingState, this.mDefaultState);
            addState(this.mInProgressState, this.mDefaultState);
            addState(this.mCanceledState, this.mDefaultState);
            addState(this.mCompletedState, this.mDefaultState);
            String access$800 = FtHttpIncomingMessage.LOG_TAG;
            Log.i(access$800, "setting current state as " + currentState.getName() + " for messageId : " + FtHttpIncomingMessage.this.mId);
            setInitialState(currentState);
            start();
        }

        /* access modifiers changed from: protected */
        public State getState(Integer stateId) {
            return this.mDbStateTranslator.translate(stateId);
        }

        /* access modifiers changed from: protected */
        public int getStateId() {
            Integer ret = this.mStateTranslator.translate(getCurrentState());
            if (ret == null) {
                return 0;
            }
            return ret.intValue();
        }

        private final class DefaultState extends State {
            private DefaultState() {
            }

            public boolean processMessage(Message msg) {
                if (msg.what != 13) {
                    if (FtHttpStateMachine.this.getCurrentState() != null) {
                        String access$800 = FtHttpIncomingMessage.LOG_TAG;
                        Log.e(access$800, "Unexpected event, current state is " + FtHttpStateMachine.this.getCurrentState().getName() + " event: " + msg.what);
                    }
                    return false;
                }
                FtHttpIncomingMessage.this.onSendDeliveredNotificationDone();
                return true;
            }
        }

        private final class InitialState extends State {
            DownloadFileTask thumbnailDownloadTask;

            private InitialState() {
            }

            public void enter() {
                String access$800 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(access$800, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                FtHttpIncomingMessage.this.updateStatus(ImConstants.Status.UNREAD);
            }

            public boolean processMessage(Message msg) {
                CanceledState state;
                int i = msg.what;
                if (i == 4) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                    return true;
                } else if (i == 8) {
                    FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                    return true;
                } else if (i != 10) {
                    boolean z = true;
                    if (i == 108) {
                        Log.i(FtHttpIncomingMessage.LOG_TAG, "EVENT_RETRY_THUMBNAIL_DOWNLOAD mId=" + FtHttpIncomingMessage.this.mId + ", Retry count=" + FtHttpIncomingMessage.this.getRetryCount());
                        FtHttpStateMachine.this.removeMessages(108);
                        if (!FtHttpIncomingMessage.this.checkAvailableRetry() || FtHttpIncomingMessage.this.getRetryCount() >= 3) {
                            FtHttpIncomingMessage.this.setRetryCount(0);
                            FtHttpIncomingMessage.this.mThumbnailPath = null;
                            FtHttpStateMachine.this.sendMessage(104);
                            return true;
                        }
                        FtHttpIncomingMessage.this.setRetryCount(FtHttpIncomingMessage.this.getRetryCount() + 1);
                        tryThumbnailDownload();
                        return true;
                    } else if (i == 50) {
                        FtHttpStateMachine.this.removeMessages(51);
                        handleReceiverTransferEvent();
                        return true;
                    } else if (i != 51) {
                        switch (i) {
                            case 104:
                                long warnSizeFileTr = FtHttpIncomingMessage.this.mConfig.getFtWarnSize();
                                long maxSizeFileTrIncoming = FtHttpIncomingMessage.this.mConfig.getMaxSizeFileTrIncoming();
                                Log.i(FtHttpIncomingMessage.LOG_TAG, "EVENT_DOWNLOAD_THUMBNAIL_COMPLETED: maxSizeFileTrIncoming(" + maxSizeFileTrIncoming + "), warnSizeFileTr(" + warnSizeFileTr + ")");
                                if (FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.BLOCK_MSG) && BlockedNumberUtil.isBlockedNumber(FtHttpIncomingMessage.this.getContext(), FtHttpIncomingMessage.this.mRemoteUri.getMsisdn())) {
                                    Log.i(FtHttpIncomingMessage.LOG_TAG, "from blocked number.. go to CanceledState.");
                                    FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                                    state = FtHttpStateMachine.this.mCanceledState;
                                } else if (!FtMessage.checkAvailableStorage(FilePathGenerator.getIncomingFileDestinationDir(FtHttpIncomingMessage.this.mListener.onRequestIncomingFtTransferPath()), FtHttpIncomingMessage.this.mFileSize - FtHttpIncomingMessage.this.mTransferredBytes)) {
                                    Log.e(FtHttpIncomingMessage.LOG_TAG, "Auto cancel file transfer, disk space not available");
                                    if (FtHttpIncomingMessage.this.mConfig.getFtCancelMemoryFull()) {
                                        FtHttpIncomingMessage.this.mRejectReason = FtRejectReason.DECLINE;
                                        FtHttpIncomingMessage.this.mCancelReason = CancelReason.LOW_MEMORY;
                                        state = FtHttpStateMachine.this.mCanceledState;
                                    } else {
                                        state = acceptFileTransfer(maxSizeFileTrIncoming, warnSizeFileTr);
                                    }
                                } else if ((FtHttpIncomingMessage.this.mConfig.isFtAutAccept() || (FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FTHTTP_FORCE_AUTO_ACCEPT_ON_WIFI) && FtHttpIncomingMessage.this.isWifiConnected())) && !FtHttpIncomingMessage.this.getExtraFt() && !FtHttpIncomingMessage.this.getRcsStrategy().isBMode(false)) {
                                    state = acceptFileTransfer(maxSizeFileTrIncoming, warnSizeFileTr);
                                } else {
                                    state = FtHttpStateMachine.this.mAcceptingState;
                                }
                                FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                                if (state != FtHttpStateMachine.this.mInProgressState) {
                                    z = false;
                                }
                                ftHttpIncomingMessage.mIsAutoDownload = z;
                                FtHttpIncomingMessage.this.mListener.onTransferReceived(FtHttpIncomingMessage.this);
                                FtHttpStateMachine.this.transitionTo(state);
                                return true;
                            case 105:
                                int retryTime = FtHttpIncomingMessage.this.getRcsStrategy().getFtHttpRetryInterval(msg.arg1, FtHttpIncomingMessage.this.getRetryCount());
                                if (retryTime >= 0) {
                                    Log.e(FtHttpIncomingMessage.LOG_TAG, "EVENT_DOWNLOAD_THUMBNAIL_FAILED: " + FtHttpIncomingMessage.this.mId + " retry download after " + retryTime + " secs");
                                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                                    ftHttpStateMachine3.sendMessageDelayed(ftHttpStateMachine3.obtainMessage(108, 0, msg.arg2), ((long) retryTime) * 1000);
                                    return true;
                                }
                                FtHttpIncomingMessage.this.mThumbnailPath = null;
                                FtHttpStateMachine.this.sendMessage(104);
                                return true;
                            case 106:
                                DownloadFileTask downloadFileTask = this.thumbnailDownloadTask;
                                if (downloadFileTask != null) {
                                    downloadFileTask.cancel(true);
                                    this.thumbnailDownloadTask = null;
                                }
                                FtHttpIncomingMessage.this.mThumbnailPath = null;
                                FtHttpIncomingMessage.this.mIsAutoDownload = false;
                                FtHttpIncomingMessage.this.mListener.onTransferReceived(FtHttpIncomingMessage.this);
                                FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                                ftHttpStateMachine4.transitionTo(ftHttpStateMachine4.mAcceptingState);
                                return true;
                            default:
                                return false;
                        }
                    } else {
                        FtHttpIncomingMessage.this.mCancelReason = CancelReason.ERROR;
                        FtHttpStateMachine ftHttpStateMachine5 = FtHttpStateMachine.this;
                        ftHttpStateMachine5.transitionTo(ftHttpStateMachine5.mCanceledState);
                        return true;
                    }
                } else if (FtHttpIncomingMessage.this.needToAcquireNetworkForFT()) {
                    FtHttpIncomingMessage.this.acquireNetworkForFT(FtHttpIncomingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY));
                    return true;
                } else {
                    handleReceiverTransferEvent();
                    return true;
                }
            }

            private IState acceptFileTransfer(long maxSizeFileTrIncoming, long warnSizeFileTr) {
                if (maxSizeFileTrIncoming == 0 || FtHttpIncomingMessage.this.mFileSize <= maxSizeFileTrIncoming) {
                    if (FtHttpIncomingMessage.this.getRcsStrategy().isWarnSizeFile(FtHttpIncomingMessage.this.mNetwork, FtHttpIncomingMessage.this.mFileSize, warnSizeFileTr, FtHttpIncomingMessage.this.isWifiConnected()) || isAutodownloadBlocked()) {
                        Log.i(FtHttpIncomingMessage.LOG_TAG, "Going to AcceptingState(due to warning size)");
                        return FtHttpStateMachine.this.mAcceptingState;
                    }
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "Going to InProgressState");
                    return FtHttpStateMachine.this.mInProgressState;
                }
                Log.e(FtHttpIncomingMessage.LOG_TAG, "Auto cancel file transfer, max size exceeded");
                FtHttpIncomingMessage.this.mRejectReason = FtRejectReason.FORBIDDEN_MAX_SIZE_EXCEEDED;
                FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                return FtHttpStateMachine.this.mCanceledState;
            }

            /* Debug info: failed to restart local var, previous not found, register: 8 */
            private boolean isAutodownloadBlocked() {
                Cursor cursor;
                try {
                    cursor = FtHttpIncomingMessage.this.getContext().getContentResolver().query(ImsConstants.Uris.MMS_PREFERENCE_PROVIDER_DATASAVER_URI, (String[]) null, (String) null, (String[]) null, (String) null);
                    String enable = ConfigConstants.VALUE.INFO_COMPLETED;
                    if (cursor != null) {
                        if (cursor.moveToNext()) {
                            enable = cursor.getString(cursor.getColumnIndexOrThrow("pref_value"));
                        }
                    }
                    String access$800 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(access$800, " enable datasaver : " + enable);
                    if (CloudMessageProviderContract.JsonData.TRUE.equals(enable)) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return true;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if ((FtHttpIncomingMessage.this.mIsGroupChat && !FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.BLOCK_FT_AUTO_DOWNLOAD_FOR_GC)) || !BlockedNumberUtil.isBlockedNumber(FtHttpIncomingMessage.this.getContext(), FtHttpIncomingMessage.this.mRemoteUri.getMsisdn())) {
                        return false;
                    }
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "It is blocked number.");
                    return true;
                } catch (IllegalStateException e) {
                    Log.e(FtHttpIncomingMessage.LOG_TAG, "isAutodownloadBlocked: IllegalStateException");
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
                throw th;
            }

            /* JADX WARNING: Code restructure failed: missing block: B:11:0x0114, code lost:
                r1 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
                r1.printStackTrace();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:18:0x0163, code lost:
                r0 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:19:0x0164, code lost:
                r0.printStackTrace();
                r1 = r5.this$1;
                r1.transitionTo(com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.access$900(r1));
             */
            /* JADX WARNING: Failed to process nested try/catch */
            /* JADX WARNING: Removed duplicated region for block: B:18:0x0163 A[ExcHandler: IOException | NullPointerException | XmlPullParserException (r0v5 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:1:0x0029] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private void handleReceiverTransferEvent() {
                /*
                    r5 = this;
                    java.lang.String r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.LOG_TAG
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "handleReceiverTransferEvent: "
                    r1.append(r2)
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this
                    java.lang.String r2 = r2.mBody
                    r1.append(r2)
                    java.lang.String r1 = r1.toString()
                    com.sec.internal.log.IMSLog.s(r0, r1)
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this
                    long r1 = java.lang.System.currentTimeMillis()
                    r0.updateDeliveredTimestamp(r1)
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r0 = r0.mBody     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo r0 = com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser.parse(r0)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.net.URL r2 = r0.getDataUrl()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.net.URL unused = r1.mDataUrl = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r1 = r1.getRcsStrategy()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = "fthttp_ignore_when_untrusted_url"
                    boolean r1 = r1.boolSetting(r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    if (r1 == 0) goto L_0x00b7
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.net.URL r2 = r2.mDataUrl     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = r2.toString()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    boolean r1 = r1.isFtHttpUrlTrusted(r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    if (r1 != 0) goto L_0x00b7
                    java.lang.String r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.LOG_TAG     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r2.<init>()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r3 = "FT["
                    r2.append(r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    int r3 = r3.mId     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r2.append(r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r3 = "] was silently cancelled due to untrusted URL: "
                    r2.append(r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.net.URL r3 = r3.mDataUrl     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r3 = r3.toString()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r2.append(r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = r2.toString()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    android.util.Log.i(r1, r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.INVALID_URL_TEMPLATE     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mCancelReason = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine$CanceledState r2 = r2.mCanceledState     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.transitionTo(r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.constants.ims.servicemodules.im.ImCacheAction r2 = com.sec.internal.constants.ims.servicemodules.im.ImCacheAction.UPDATED     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.triggerObservers(r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    return
                L_0x00b7:
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = r0.getFileName()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mFileName = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    long r2 = r0.getFileSize()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mFileSize = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = r0.getContentType()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mContentType = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = r2.mContentType     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r2 = com.sec.internal.ims.servicemodules.im.FtMessage.getType(r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mType = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = r0.getDataUntil()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mFileExpire = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.constants.ims.servicemodules.im.FileDisposition r2 = r0.getFileDisposition()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mFileDisposition = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    int r2 = r0.getPlayingLength()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mPlayingLength = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0114, IOException | NullPointerException | XmlPullParserException -> 0x0163, IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0114, IOException | NullPointerException | XmlPullParserException -> 0x0163, IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = r0.getFileName()     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0114, IOException | NullPointerException | XmlPullParserException -> 0x0163, IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r3 = "UTF-8"
                    java.lang.String r2 = java.net.URLDecoder.decode(r2, r3)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0114, IOException | NullPointerException | XmlPullParserException -> 0x0163, IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r1.mFileName = r2     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0114, IOException | NullPointerException | XmlPullParserException -> 0x0163, IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    goto L_0x0118
                L_0x0114:
                    r1 = move-exception
                    r1.printStackTrace()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                L_0x0118:
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.listener.FtMessageListener r1 = r1.mListener     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r1 = r1.onRequestIncomingFtTransferPath()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r1 = com.sec.internal.helper.FilePathGenerator.getIncomingFileDestinationDir(r1)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r3.<init>()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r4 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r4 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r4 = r4.mFileName     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r3.append(r4)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r4 = ".tmp"
                    r3.append(r4)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r4 = 3
                    java.lang.String r4 = com.sec.internal.helper.StringGenerator.generateString(r4, r4)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r3.append(r4)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r3 = r3.toString()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r4 = 128(0x80, float:1.794E-43)
                    java.lang.String r3 = com.sec.internal.helper.FilePathGenerator.generateUniqueFilePath(r1, r3, r4)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    r2.mFilePath = r3     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    java.lang.String r2 = r2.getReliableMessage()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    if (r2 == 0) goto L_0x015f
                    r5.tryReliableDownload()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                    goto L_0x0162
                L_0x015f:
                    r5.tryThumbnailDownload()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x0163 }
                L_0x0162:
                    goto L_0x0170
                L_0x0163:
                    r0 = move-exception
                    r0.printStackTrace()
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine$CanceledState r2 = r1.mCanceledState
                    r1.transitionTo(r2)
                L_0x0170:
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this
                    com.sec.internal.constants.ims.servicemodules.im.ImCacheAction r1 = com.sec.internal.constants.ims.servicemodules.im.ImCacheAction.UPDATED
                    r0.triggerObservers(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.InitialState.handleReceiverTransferEvent():void");
            }

            private void tryReliableDownload() {
                String dir = FilePathGenerator.getIncomingFileDestinationDir(FtHttpIncomingMessage.this.mListener.onRequestIncomingFtTransferPath());
                String url = FtHttpIncomingMessage.this.getReliableMessage();
                String path = dir + File.separatorChar + url.substring(url.lastIndexOf(47) + 1);
                if (new File(path).exists()) {
                    Log.e(FtHttpIncomingMessage.LOG_TAG, "tryReliableDownload, file already exist.");
                    FtHttpIncomingMessage.this.setReliableMessage(path);
                    tryThumbnailDownload();
                    return;
                }
                Log.i(FtHttpIncomingMessage.LOG_TAG, "tryReliableDownload, url: " + IMSLog.checker(url) + " path: " + path);
                downloadReliable(url, path);
            }

            private void downloadReliable(String url, String path) {
                final String str = path;
                new DownloadFileTask(FtHttpIncomingMessage.this.mConfig.getPhoneId()).executeOnExecutor(FtMessage.sFtHttpThreadPool, new DownloadFileTask.DownloadRequest[]{new DownloadFileTask.DownloadRequest(url, 1, 0, path, FtHttpIncomingMessage.this.mConfig.getFtHttpCsUser(), FtHttpIncomingMessage.this.mConfig.getFtHttpCsPwd(), FtHttpIncomingMessage.this.getFtHttpUserAgent(), FtHttpIncomingMessage.this.mNetwork, FtHttpIncomingMessage.this.mConfig.isFtHttpTrustAllCerts(), FtHttpIncomingMessage.this.mConfig.getFtHttpDLUrl(), FtHttpIncomingMessage.this.getParamsforDl(url), new DownloadFileTask.DownloadTaskCallback() {
                    public void onProgressUpdate(long transferred) {
                    }

                    public void onCompleted(long transferred) {
                        FtHttpIncomingMessage.this.setReliableMessage(str);
                        InitialState.this.tryThumbnailDownload();
                    }

                    public void onCanceled(CancelReason reason, int retryTime, int errorCode) {
                        InitialState.this.tryThumbnailDownload();
                    }
                })});
            }

            /* access modifiers changed from: private */
            public void tryThumbnailDownload() {
                File file = new File(FtHttpIncomingMessage.this.mFilePath);
                try {
                    FtHttpFileInfo fileInfo = FtHttpXmlParser.parse(FtHttpIncomingMessage.this.mBody);
                    long thumbnailSize = getThumbnailSize(fileInfo);
                    if (thumbnailSize == 0) {
                        FtHttpIncomingMessage.this.mThumbnailPath = null;
                        FtHttpStateMachine.this.sendMessage(104);
                        return;
                    }
                    if (fileInfo != null && FtHttpIncomingMessage.this.mThumbnailContentType == null) {
                        FtHttpIncomingMessage.this.mThumbnailContentType = fileInfo.getThumbnailContentType();
                    }
                    if (FtHttpIncomingMessage.this.mThumbnailPath == null) {
                        FtHttpIncomingMessage.this.mThumbnailPath = FilePathGenerator.generateUniqueThumbnailPath(FtHttpIncomingMessage.this.mFileName, FtHttpIncomingMessage.this.mThumbnailContentType, FtHttpIncomingMessage.this.mListener.onRequestIncomingFtTransferPath(), 128);
                    }
                    String access$800 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(access$800, "tryThumbnailDownload: thumbnailContentType=" + FtHttpIncomingMessage.this.mThumbnailContentType + ", thumbnailPath=" + FtHttpIncomingMessage.this.mThumbnailPath);
                    downloadThumbnail(FtHttpIncomingMessage.this.mThumbnailPath, fileInfo.getThumbnailDataUrl().toString(), thumbnailSize);
                } catch (IllegalArgumentException e) {
                    String access$8002 = FtHttpIncomingMessage.LOG_TAG;
                    Log.e(access$8002, "Invalid path: " + file.getParent());
                    FtHttpIncomingMessage.this.mThumbnailPath = null;
                    FtHttpStateMachine.this.sendMessage(104);
                } catch (IOException | XmlPullParserException e2) {
                    e2.printStackTrace();
                    FtHttpIncomingMessage.this.mThumbnailPath = null;
                    FtHttpStateMachine.this.sendMessage(104);
                }
            }

            private long getThumbnailSize(FtHttpFileInfo fileInfo) {
                long maxSizeThumbnail = FtHttpIncomingMessage.this.mConfig.getFtWarnSize() != 0 ? FtHttpIncomingMessage.this.mConfig.getFtWarnSize() : FtHttpIncomingMessage.this.MAX_SIZE_DOWNLOAD_THUMBNAIL;
                if (fileInfo == null || !fileInfo.isThumbnailExist() || fileInfo.getThumbnailFileSize() > maxSizeThumbnail) {
                    return 0;
                }
                return fileInfo.getThumbnailFileSize();
            }

            private void downloadThumbnail(String path, String url, long size) {
                boolean isInternetPdn = FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FT_INTERNET_PDN);
                DownloadFileTask downloadFileTask = new DownloadFileTask(FtHttpIncomingMessage.this.mConfig.getPhoneId());
                this.thumbnailDownloadTask = downloadFileTask;
                Executor executor = FtMessage.sFtHttpThreadPool;
                DownloadFileTask.DownloadRequest[] downloadRequestArr = new DownloadFileTask.DownloadRequest[1];
                final long j = size;
                downloadRequestArr[0] = new DownloadFileTask.DownloadRequest(url, j, 0, path, FtHttpIncomingMessage.this.mConfig.getFtHttpCsUser(), FtHttpIncomingMessage.this.mConfig.getFtHttpCsPwd(), FtHttpIncomingMessage.this.getFtHttpUserAgent(), isInternetPdn ? null : FtHttpIncomingMessage.this.mNetwork, FtHttpIncomingMessage.this.mConfig.isFtHttpTrustAllCerts(), FtHttpIncomingMessage.this.mConfig.getFtHttpDLUrl(), FtHttpIncomingMessage.this.getParamsforDl(url), new DownloadFileTask.DownloadTaskCallback() {
                    public void onProgressUpdate(long transferred) {
                        if (transferred > j + FtHttpIncomingMessage.this.FT_SIZE_MARGIN) {
                            FtHttpStateMachine.this.sendMessage(FtHttpStateMachine.this.obtainMessage(106));
                        }
                    }

                    public void onCompleted(long transferred) {
                        if (transferred < j - FtHttpIncomingMessage.this.FT_SIZE_MARGIN || transferred > j + FtHttpIncomingMessage.this.FT_SIZE_MARGIN) {
                            FtHttpStateMachine.this.sendMessage(FtHttpStateMachine.this.obtainMessage(106));
                            return;
                        }
                        String newThumbnailPath = FilePathGenerator.renameThumbnail(FtHttpIncomingMessage.this.mThumbnailPath, FtHttpIncomingMessage.this.mThumbnailContentType, FtHttpIncomingMessage.this.mFileName, 128);
                        if (newThumbnailPath != null) {
                            FtHttpIncomingMessage.this.mThumbnailPath = newThumbnailPath;
                            FtHttpIncomingMessage.this.triggerObservers(ImCacheAction.UPDATED);
                        }
                        FtHttpStateMachine.this.sendMessage(104, (Object) Long.valueOf(transferred));
                    }

                    public void onCanceled(CancelReason reason, int retryTime, int errorCode) {
                        FtHttpStateMachine.this.sendMessage(FtHttpStateMachine.this.obtainMessage(105, retryTime, errorCode, reason));
                    }
                });
                downloadFileTask.executeOnExecutor(executor, downloadRequestArr);
            }
        }

        private final class AcceptingState extends State {
            private AcceptingState() {
            }

            public void enter() {
                String access$800 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(access$800, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                FtHttpIncomingMessage.this.updateState();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 4) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                    return true;
                } else if (i != 6) {
                    return false;
                } else {
                    FtHttpIncomingMessage.this.mRejectReason = FtRejectReason.DECLINE;
                    FtHttpIncomingMessage.this.mCancelReason = CancelReason.REJECTED_BY_USER;
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                    return true;
                }
            }
        }

        private final class InProgressState extends State {
            DownloadFileTask downloadTask;

            private InProgressState() {
            }

            public void enter() {
                String access$800 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(access$800, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                if (FtHttpIncomingMessage.this.needToAcquireNetworkForFT()) {
                    FtHttpIncomingMessage.this.acquireNetworkForFT(FtHttpIncomingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY));
                    FtHttpIncomingMessage.this.acquireWakeLock();
                    return;
                }
                FtHttpStateMachine.this.removeMessages(107);
                FtHttpIncomingMessage.this.setRetryCount(0);
                FtHttpIncomingMessage.this.updateState();
                FtHttpIncomingMessage.this.mListener.onTransferInProgress(FtHttpIncomingMessage.this);
                if (FtHttpIncomingMessage.this.mTransferredBytes >= FtHttpIncomingMessage.this.mFileSize) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.sendMessage(102, (Object) Long.valueOf(FtHttpIncomingMessage.this.mTransferredBytes));
                } else if (!FtHttpIncomingMessage.this.mIsBootup || (!FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY) && FtHttpIncomingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_CANCEL_FOR_DEREGI) <= 0)) {
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.sendMessage(101, (Object) Long.valueOf(FtHttpIncomingMessage.this.mTransferredBytes));
                    tryDownload();
                } else {
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "Do not auto resume message loaded from bootup");
                    FtHttpIncomingMessage.this.mIsBootup = false;
                    FtHttpIncomingMessage.this.mCancelReason = CancelReason.DEVICE_UNREGISTERED;
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                }
                FtHttpIncomingMessage.this.acquireWakeLock();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i == 6) {
                    String access$800 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(access$800, getName() + " EVENT_REJECT_TRANSFER");
                    DownloadFileTask downloadFileTask = this.downloadTask;
                    if (downloadFileTask != null) {
                        downloadFileTask.cancel(true);
                        this.downloadTask = null;
                    }
                    FtHttpStateMachine.this.removeMessages(107);
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                    return true;
                } else if (i == 8) {
                    CancelReason cancelReason = (CancelReason) msg.obj;
                    String access$8002 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(access$8002, getName() + " EVENT_CANCEL_TRANSFER CancelReason " + cancelReason);
                    FtHttpStateMachine.this.removeMessages(107);
                    PreciseAlarmManager.getInstance(FtHttpIncomingMessage.this.getContext()).removeMessage(FtHttpStateMachine.this.obtainMessage(52));
                    DownloadFileTask downloadFileTask2 = this.downloadTask;
                    if (downloadFileTask2 != null) {
                        downloadFileTask2.cancel(true);
                        this.downloadTask = null;
                    }
                    if (cancelReason != CancelReason.DEVICE_UNREGISTERED || FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
                        FtHttpIncomingMessage.this.mCancelReason = cancelReason;
                        FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                        ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                        return true;
                    }
                    int cancelDelay = FtHttpIncomingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_CANCEL_FOR_DEREGI);
                    if (cancelDelay <= 0) {
                        return true;
                    }
                    PreciseAlarmManager.getInstance(FtHttpIncomingMessage.this.getContext()).sendMessageDelayed(getClass().getSimpleName(), FtHttpStateMachine.this.obtainMessage(52), ((long) cancelDelay) * 1000);
                    return true;
                } else if (i == 10) {
                    FtHttpStateMachine.this.removeMessages(107);
                    PreciseAlarmManager.getInstance(FtHttpIncomingMessage.this.getContext()).removeMessage(FtHttpStateMachine.this.obtainMessage(52));
                    if (FtHttpIncomingMessage.this.mTransferredBytes < FtHttpIncomingMessage.this.mFileSize) {
                        tryDownload();
                        return true;
                    }
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    ftHttpStateMachine3.sendMessage(102, (Object) Long.valueOf(FtHttpIncomingMessage.this.mTransferredBytes));
                    return true;
                } else if (i != 107) {
                    switch (i) {
                        case 50:
                            FtHttpStateMachine.this.removeMessages(51);
                            FtHttpStateMachine.this.removeMessages(107);
                            FtHttpIncomingMessage.this.setRetryCount(0);
                            FtHttpIncomingMessage.this.updateState();
                            FtHttpIncomingMessage.this.mListener.onTransferInProgress(FtHttpIncomingMessage.this);
                            if (FtHttpIncomingMessage.this.mTransferredBytes < FtHttpIncomingMessage.this.mFileSize) {
                                FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                                ftHttpStateMachine4.sendMessage(101, (Object) Long.valueOf(FtHttpIncomingMessage.this.mTransferredBytes));
                                tryDownload();
                                return true;
                            }
                            FtHttpStateMachine ftHttpStateMachine5 = FtHttpStateMachine.this;
                            ftHttpStateMachine5.sendMessage(102, (Object) Long.valueOf(FtHttpIncomingMessage.this.mTransferredBytes));
                            return true;
                        case 51:
                            FtHttpIncomingMessage.this.mCancelReason = CancelReason.ERROR;
                            FtHttpStateMachine ftHttpStateMachine6 = FtHttpStateMachine.this;
                            ftHttpStateMachine6.transitionTo(ftHttpStateMachine6.mCanceledState);
                            return true;
                        case 52:
                            String access$8003 = FtHttpIncomingMessage.LOG_TAG;
                            Log.i(access$8003, "EVENT_DELAY_CANCEL_TRANSFER mId=" + FtHttpIncomingMessage.this.mId);
                            DownloadFileTask downloadFileTask3 = this.downloadTask;
                            if (downloadFileTask3 != null) {
                                downloadFileTask3.cancel(true);
                                this.downloadTask = null;
                            }
                            FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                            FtHttpStateMachine ftHttpStateMachine7 = FtHttpStateMachine.this;
                            ftHttpStateMachine7.transitionTo(ftHttpStateMachine7.mCanceledState);
                            return true;
                        default:
                            switch (i) {
                                case 101:
                                    FtHttpIncomingMessage.this.updateTransferredBytes(((Long) msg.obj).longValue());
                                    String access$8004 = FtHttpIncomingMessage.LOG_TAG;
                                    Log.i(access$8004, "EVENT_DOWNLOAD_PROGRESS " + FtHttpIncomingMessage.this.mTransferredBytes + "/" + FtHttpIncomingMessage.this.mFileSize);
                                    FtHttpIncomingMessage.this.mListener.onTransferProgressReceived(FtHttpIncomingMessage.this);
                                    return true;
                                case 102:
                                    FtHttpIncomingMessage.this.mTransferredBytes = ((Long) msg.obj).longValue();
                                    FtHttpStateMachine ftHttpStateMachine8 = FtHttpStateMachine.this;
                                    ftHttpStateMachine8.transitionTo(ftHttpStateMachine8.mCompletedState);
                                    return true;
                                case 103:
                                    FtHttpIncomingMessage.this.mCancelReason = (CancelReason) msg.obj;
                                    this.downloadTask = null;
                                    int retryTime = FtHttpIncomingMessage.this.getRcsStrategy().getFtHttpRetryInterval(msg.arg1, FtHttpIncomingMessage.this.getRetryCount());
                                    if (retryTime >= 0) {
                                        String access$8005 = FtHttpIncomingMessage.LOG_TAG;
                                        Log.i(access$8005, "EVENT_RETRY_DOWNLOAD: " + FtHttpIncomingMessage.this.mId + " retry download after " + retryTime + " secs");
                                        FtHttpStateMachine ftHttpStateMachine9 = FtHttpStateMachine.this;
                                        ftHttpStateMachine9.sendMessageDelayed(ftHttpStateMachine9.obtainMessage(107, 0, msg.arg2), ((long) retryTime) * 1000);
                                        return true;
                                    }
                                    FtHttpStateMachine ftHttpStateMachine10 = FtHttpStateMachine.this;
                                    ftHttpStateMachine10.transitionTo(ftHttpStateMachine10.mCanceledState);
                                    return true;
                                default:
                                    return false;
                            }
                    }
                } else {
                    String access$8006 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(access$8006, "EVENT_RETRY_DOWNLOAD mId=" + FtHttpIncomingMessage.this.mId + "Retry count = " + FtHttpIncomingMessage.this.getRetryCount());
                    int errorCode = msg.arg2;
                    FtHttpStateMachine.this.removeMessages(107);
                    if (!FtHttpIncomingMessage.this.checkAvailableRetry()) {
                        if (!FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
                            return true;
                        }
                        FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                        FtHttpStateMachine ftHttpStateMachine11 = FtHttpStateMachine.this;
                        ftHttpStateMachine11.transitionTo(ftHttpStateMachine11.mCanceledState);
                        return true;
                    } else if (errorCode == 503) {
                        tryDownload();
                        return true;
                    } else if (FtHttpIncomingMessage.this.getRetryCount() < 3) {
                        FtHttpIncomingMessage.this.setRetryCount(FtHttpIncomingMessage.this.getRetryCount() + 1);
                        tryDownload();
                        return true;
                    } else {
                        FtHttpStateMachine ftHttpStateMachine12 = FtHttpStateMachine.this;
                        ftHttpStateMachine12.transitionTo(ftHttpStateMachine12.mCanceledState);
                        return true;
                    }
                }
            }

            private void tryDownload() {
                FtHttpIncomingMessage.this.mIsWifiUsed = FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FT_WIFI_DISCONNECTED) && FtHttpIncomingMessage.this.isWifiConnected();
                File file = new File(FtHttpIncomingMessage.this.mFilePath);
                File folder = new File(file.getParent());
                if (!folder.exists() && !folder.mkdirs()) {
                    Log.e(FtHttpIncomingMessage.LOG_TAG, "Fail to create folder");
                }
                try {
                    if (!FtMessage.checkAvailableStorage(file.getParent(), FtHttpIncomingMessage.this.mFileSize - FtHttpIncomingMessage.this.mTransferredBytes)) {
                        Log.e(FtHttpIncomingMessage.LOG_TAG, "Auto cancel file transfer, disk space not available");
                        FtHttpIncomingMessage.this.mRejectReason = FtRejectReason.DECLINE;
                        FtHttpIncomingMessage.this.mCancelReason = CancelReason.LOW_MEMORY;
                        FtHttpStateMachine.this.transitionTo(FtHttpStateMachine.this.mCanceledState);
                    } else if (!FtHttpIncomingMessage.this.checkValidPeriod()) {
                        Log.e(FtHttpIncomingMessage.LOG_TAG, "Auto cancel file transfer, file has expired");
                        FtHttpIncomingMessage.this.mRejectReason = FtRejectReason.DECLINE;
                        FtHttpIncomingMessage.this.mCancelReason = CancelReason.VALIDITY_EXPIRED;
                        FtHttpStateMachine.this.transitionTo(FtHttpStateMachine.this.mCanceledState);
                    } else if (FtHttpIncomingMessage.this.mDataUrl == null) {
                        String access$800 = FtHttpIncomingMessage.LOG_TAG;
                        Log.e(access$800, getName() + ": Data url is null, go to Canceled");
                        FtHttpStateMachine.this.transitionTo(FtHttpStateMachine.this.mCanceledState);
                    } else {
                        if (this.downloadTask != null) {
                            if (this.downloadTask.getStatus() != AsyncTask.Status.FINISHED) {
                                Log.i(FtHttpIncomingMessage.LOG_TAG, "Task is already running or pending.");
                                return;
                            }
                        }
                        if (FtHttpIncomingMessage.this.needToAcquireNetworkForFT()) {
                            FtHttpIncomingMessage.this.acquireNetworkForFT(FtHttpIncomingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY));
                            FtHttpIncomingMessage.this.acquireWakeLock();
                            return;
                        }
                        createDownloadTask(FtHttpIncomingMessage.this.mDataUrl.toString());
                    }
                } catch (IllegalArgumentException e) {
                    String access$8002 = FtHttpIncomingMessage.LOG_TAG;
                    Log.e(access$8002, "Invalid path: " + file.getParent());
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                }
            }

            private void createDownloadTask(String url) {
                boolean isInternetPdn = FtHttpIncomingMessage.this.getExtraFt() || FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FT_INTERNET_PDN);
                DownloadFileTask downloadFileTask = new DownloadFileTask(FtHttpIncomingMessage.this.mConfig.getPhoneId());
                this.downloadTask = downloadFileTask;
                Executor executor = FtMessage.sFtHttpThreadPool;
                DownloadFileTask.DownloadRequest[] downloadRequestArr = new DownloadFileTask.DownloadRequest[1];
                String str = url;
                downloadRequestArr[0] = new DownloadFileTask.DownloadRequest(str, FtHttpIncomingMessage.this.mFileSize, FtHttpIncomingMessage.this.mTransferredBytes, FtHttpIncomingMessage.this.mFilePath, FtHttpIncomingMessage.this.mConfig.getFtHttpCsUser(), FtHttpIncomingMessage.this.mConfig.getFtHttpCsPwd(), FtHttpIncomingMessage.this.getFtHttpUserAgent(), isInternetPdn ? null : FtHttpIncomingMessage.this.mNetwork, FtHttpIncomingMessage.this.mConfig.isFtHttpTrustAllCerts(), FtHttpIncomingMessage.this.mConfig.getFtHttpDLUrl(), FtHttpIncomingMessage.this.getParamsforDl(str), new DownloadFileTask.DownloadTaskCallback() {
                    public void onProgressUpdate(long transferred) {
                        if (transferred > FtHttpIncomingMessage.this.mFileSize + FtHttpIncomingMessage.this.FT_SIZE_MARGIN) {
                            FtHttpStateMachine.this.sendMessage(FtHttpStateMachine.this.obtainMessage(8, (Object) CancelReason.INVALID_FT_FILE_SIZE));
                        } else {
                            FtHttpStateMachine.this.sendMessage(101, (Object) Long.valueOf(transferred));
                        }
                    }

                    public void onCompleted(long transferred) {
                        if (transferred < FtHttpIncomingMessage.this.mFileSize - FtHttpIncomingMessage.this.FT_SIZE_MARGIN || transferred > FtHttpIncomingMessage.this.mFileSize + FtHttpIncomingMessage.this.FT_SIZE_MARGIN) {
                            FtHttpStateMachine.this.sendMessage(FtHttpStateMachine.this.obtainMessage(8, (Object) CancelReason.INVALID_FT_FILE_SIZE));
                            return;
                        }
                        FtHttpIncomingMessage.this.renameFile();
                        FtHttpStateMachine.this.sendMessage(102, (Object) Long.valueOf(transferred));
                        FtHttpIncomingMessage.this.listToDumpFormat(LogClass.FT_HTTP_DOWNLOAD_COMPLETE, 0, new ArrayList<>());
                    }

                    public void onCanceled(CancelReason reason, int retryTime, int errorCode) {
                        FtHttpStateMachine.this.sendMessage(FtHttpStateMachine.this.obtainMessage(103, retryTime, errorCode, reason));
                        if (errorCode != -1) {
                            List<String> dumps = new ArrayList<>();
                            dumps.add(String.valueOf(errorCode));
                            dumps.add(String.valueOf(FtHttpIncomingMessage.this.getRetryCount()));
                            FtHttpIncomingMessage.this.listToDumpFormat(LogClass.FT_HTTP_DOWNLOAD_CANCEL, 0, dumps);
                        }
                    }
                });
                downloadFileTask.executeOnExecutor(executor, downloadRequestArr);
            }
        }

        private final class CanceledState extends State {
            private CanceledState() {
            }

            public void enter() {
                String access$800 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(access$800, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                FtHttpIncomingMessage.this.updateState();
                if (FtHttpIncomingMessage.this.mIsNetworkRequested) {
                    FtHttpIncomingMessage.this.releaseNetworkAcquiredForFT();
                }
                if (FtHttpIncomingMessage.this.mIsBootup) {
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "Message is loaded from bootup, no need for notifications");
                    FtHttpIncomingMessage.this.mIsBootup = false;
                    return;
                }
                FtHttpIncomingMessage.this.mResumableOptionCode = FtHttpIncomingMessage.this.getRcsStrategy().getftResumableOption(FtHttpIncomingMessage.this.mCancelReason, FtHttpIncomingMessage.this.mIsGroupChat, FtHttpIncomingMessage.this.mDirection, FtHttpIncomingMessage.this.getTransferMech()).getId();
                String access$8002 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(access$8002, "mResumableOptionCode : " + FtHttpIncomingMessage.this.mResumableOptionCode);
                FtHttpIncomingMessage.this.updateStatus(ImConstants.Status.FAILED);
                FtHttpIncomingMessage.this.mListener.onTransferCanceled(FtHttpIncomingMessage.this);
                FtHttpIncomingMessage.this.releaseWakeLock();
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i != 8) {
                    if (i != 10) {
                        return false;
                    }
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                    return true;
                } else if (msg.obj == CancelReason.INVALID_FT_FILE_SIZE) {
                    return true;
                } else {
                    FtHttpIncomingMessage.this.mListener.onCancelRequestFailed(FtHttpIncomingMessage.this);
                    return true;
                }
            }
        }

        private final class CompletedState extends State {
            private CompletedState() {
            }

            public void enter() {
                String access$800 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(access$800, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                FtHttpIncomingMessage.this.updateState();
                if (FtHttpIncomingMessage.this.mIsNetworkRequested) {
                    FtHttpIncomingMessage.this.releaseNetworkAcquiredForFT();
                }
                if (FtHttpIncomingMessage.this.mIsBootup) {
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "Message is loaded from bootup, no need for notifications");
                    FtHttpIncomingMessage.this.mIsBootup = false;
                    return;
                }
                FtHttpIncomingMessage.this.removeThumbnail();
                FtHttpIncomingMessage.this.mListener.onTransferCompleted(FtHttpIncomingMessage.this);
                FtHttpIncomingMessage.this.releaseWakeLock();
            }

            public boolean processMessage(Message msg) {
                if (msg.what != 8) {
                    return false;
                }
                if (msg.obj == CancelReason.INVALID_FT_FILE_SIZE) {
                    return true;
                }
                FtHttpIncomingMessage.this.mListener.onCancelRequestFailed(FtHttpIncomingMessage.this);
                return true;
            }
        }
    }
}
