package com.sec.internal.ims.servicemodules.csh;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.gsma.services.rcs.sharing.image.ImageSharing;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.ims.servicemodules.csh.event.CshErrorReason;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.IshFileTransfer;
import com.sec.internal.ims.servicemodules.csh.event.IshIncomingSessionEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshSessionEstablishedEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferCompleteEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferFailedEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferProgressEvent;
import com.sec.internal.ims.servicemodules.csh.event.OpenApiTranslationFilter;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.StorageEnvironment;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ImageShareModule extends CshModuleBase implements IImageShareModule {
    private static final int EVENT_INCOMING_SESSION = 2;
    private static final int EVENT_SESSION_ESTABLISHED = 3;
    private static final int EVENT_TRANSFER_COMPLETE = 4;
    private static final int EVENT_TRANSFER_FAILED = 6;
    private static final int EVENT_TRANSFER_PROGRESS = 5;
    /* access modifiers changed from: private */
    public static String LOG_TAG;
    public static final String NAME;
    private boolean[] mHasImageShareSupport = {false, false};
    private IIshServiceInterface mImsService;
    /* access modifiers changed from: private */
    public IshTranslation mIshTranslation;
    private final List<IImageShareEventListener> mListeners = new ArrayList();
    private long[] mMaxSize = {0, 0};
    private int mRegistrationId = -1;

    static {
        Class<ImageShareModule> cls = ImageShareModule.class;
        NAME = cls.getSimpleName();
        LOG_TAG = cls.getSimpleName();
    }

    public ImageShareModule(Looper looper, Context context, IIshServiceInterface ishServiceInterface) {
        super(looper, context);
        this.mImsService = ishServiceInterface;
        this.mCache = CshCache.getInstance(ishServiceInterface);
        this.mIshTranslation = new IshTranslation(this.mContext, this);
    }

    public String getName() {
        return NAME;
    }

    public String[] getServicesRequiring() {
        return new String[]{"is"};
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        updateFeatures(phoneId);
    }

    public void start() {
        if (!isRunning()) {
            super.start();
            this.mImsService.registerForIshIncomingSession(this, 2, (Object) null);
            this.mImsService.registerForIshSessionEstablished(this, 3, (Object) null);
            this.mImsService.registerForIshTransferComplete(this, 4, (Object) null);
            this.mImsService.registerForIshTransferProgress(this, 5, (Object) null);
            this.mImsService.registerForIshTransferFailed(this, 6, (Object) null);
        }
    }

    public void stop() {
        for (int i = 0; i < this.mCache.getSize(); i++) {
            if (this.mCache.getSessionAt(i).getContent().shareType == 1) {
                ((ImageShare) this.mCache.getSessionAt(i)).sessionFailed();
            }
        }
        super.stop();
        disableIshFeature();
        this.mImsService.unregisterForIshIncomingSession(this);
        this.mImsService.unregisterForIshSessionEstablished(this);
        this.mImsService.unregisterForIshTransferComplete(this);
        this.mImsService.unregisterForIshTransferProgress(this);
        this.mImsService.unregisterForIshTransferFailed(this);
    }

    public void onConfigured(int phoneId) {
        String str = LOG_TAG;
        Log.d(str, "onConfigured: phoneId = " + phoneId);
        updateFeatures(phoneId);
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
        Log.i(LOG_TAG, "onRegistered");
        if (regiInfo.getImsProfile() != null) {
            this.mRegistrationId = getRegistrationInfoId(regiInfo);
        }
        updateServiceStatus(regiInfo.getPhoneId());
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        Log.i(LOG_TAG, "onDeregistered");
        super.onDeregistered(regiInfo, errorCode);
        if (getImsRegistration() != null) {
            this.mRegistrationId = -1;
            updateServiceStatus(regiInfo.getPhoneId());
        }
    }

    public void onNetworkChanged(NetworkEvent event, int phoneId) {
    }

    public long getMaxSize() {
        return this.mMaxSize[this.mActiveCallPhoneId];
    }

    public long getWarnSize() {
        return 0;
    }

    public Future<ImageShare> createShare(final ImsUri contactUri, final String filePath) {
        FutureTask<ImageShare> future = new FutureTask<>(new Callable<ImageShare>() {
            public ImageShare call() throws Exception {
                Log.i(ImageShareModule.LOG_TAG, "createShare");
                ImageShare session = ImageShareModule.this.mCache.newOutgoingImageShare(ImageShareModule.this, contactUri, filePath);
                if (session != null) {
                    session.startQutgoingSession();
                    ImageShareModule.this.mIshTranslation.broadcastOutgoingSucceeded(session.mContent.shareId, contactUri, filePath);
                }
                return session;
            }
        });
        post(future);
        return future;
    }

    public void acceptShare(long sharedId) {
        String str = LOG_TAG;
        Log.i(str, "acceptShare sharedId " + sharedId);
        ImageShare session = getSession(sharedId);
        if (session == null) {
            String str2 = LOG_TAG;
            Log.w(str2, "Detected illegal share id passed from intent. Was " + sharedId);
            this.mIshTranslation.broadcastCommunicationError();
        } else if (StorageEnvironment.isSdCardStateFine(session.getFileSize())) {
            session.acceptIncomingSession();
        } else {
            session.incomingSessionPreReject();
        }
    }

    public void cancelShare(long sharedId) {
        String str = LOG_TAG;
        Log.i(str, "cancelShare sharedId " + sharedId);
        ImageShare session = getSession(sharedId);
        if (session != null) {
            session.cancelByLocalSession();
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + sharedId);
        this.mIshTranslation.broadcastCommunicationError();
    }

    public ImageShare getSession(long sharedId) {
        try {
            return (ImageShare) super.getSession(sharedId);
        } catch (ClassCastException e) {
            String str = LOG_TAG;
            Log.w(str, sharedId + " is not Image Share");
            return null;
        }
    }

    public void ishSessionEstablishedEvent(IshSessionEstablishedEvent event) {
        ImageShare session = (ImageShare) this.mCache.getSession(event.mSessionId);
        String str = LOG_TAG;
        Log.i(str, "ishSessionEstablishedEvent sessionId : " + event.mSessionId);
        if (session != null) {
            session.mContent.reasonCode = Integer.valueOf(ImageSharing.ReasonCode.FAILED_SHARING.toString()).intValue();
            session.sessioinEstablished();
            this.mIshTranslation.broadcastConnected(session.getContent().shareId, session.getContent().shareContactUri);
            return;
        }
        Log.e(LOG_TAG, "Session is Not found");
    }

    public void ishTransferFailedEvent(IshTransferFailedEvent event) {
        int sessionId = event.mSessionId;
        CshErrorReason reason = event.mReason;
        String str = LOG_TAG;
        Log.i(str, "ishTransferFailedEvent sessionId : " + sessionId + " Reason : " + reason);
        ImageShare session = (ImageShare) this.mCache.getSession(sessionId);
        ImsRegistration imsRegistration = getImsRegistration();
        if (reason == CshErrorReason.FORBIDDEN) {
            IRegistrationGovernor governor = null;
            IRegistrationManager rm = ImsRegistry.getRegistrationManager();
            if (imsRegistration != null) {
                governor = rm.getRegistrationGovernor(imsRegistration.getHandle());
            }
            if (governor != null) {
                governor.onSipError("ish_tapi", new SipError(403, "Forbidden"));
            }
        }
        if (session != null) {
            session.sessionFailed();
            if (imsRegistration != null) {
                getServiceModuleManager().getCapabilityDiscoveryModule().exchangeCapabilitiesForVSH(imsRegistration.getPhoneId(), true);
            }
            this.mIshTranslation.broadcastCanceled(session.mContent.shareId, session.mContent.shareContactUri, session.mContent.shareDirection, ishReasonTranslator(reason));
            if (event.mReason == CshErrorReason.MSRP_TIMEOUT) {
                this.mIshTranslation.broadcastCshServiceNotReady();
                return;
            }
            return;
        }
        Log.d(LOG_TAG, "Already removed session");
    }

    public void ishCancelFailed(int sessionId) {
        ImageShare session = (ImageShare) this.mCache.getSession(sessionId);
        if (session != null) {
            session.sessionFailed();
            this.mIshTranslation.broadcastCanceled(session.mContent.shareId, session.mContent.shareContactUri, session.mContent.shareDirection, 12);
            String str = LOG_TAG;
            Log.i(str, "ishCancelFailed sessionId : " + sessionId + " broadcast finished");
            return;
        }
        Log.d(LOG_TAG, "Already removed session");
    }

    public void ishTransferCompleteEvent(IshTransferCompleteEvent event) {
        ImageShare session = (ImageShare) this.mCache.getSession(event.mSessionId);
        if (session != null) {
            session.transferCompleted();
            this.mIshTranslation.broadcastCompleted(session.mContent.shareId, session.getContent().shareContactUri);
            if (session.getContent().shareDirection == 0) {
                this.mIshTranslation.broadcastSystemRefresh(session.getContent().dataPath);
                return;
            }
            return;
        }
        Log.d(LOG_TAG, "Already removed session");
    }

    public void ishTransferProgressEvent(IshTransferProgressEvent event) {
        ImageShare session = (ImageShare) this.mCache.getSession(event.mSessionId);
        if (session != null) {
            long progress = event.mProgress;
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = LOG_TAG;
            Log.i(str, "progressing for in_progress state: " + ((100 * progress) / session.getContent().dataSize) + "%");
            session.getContent().dataProgress = progress;
            contentResolver.notifyChange(ICshConstants.ShareDatabase.ACTIVE_SESSIONS_URI, (ContentObserver) null);
            this.mIshTranslation.broadcastProgress(session.mContent.shareId, session.getContent().shareContactUri, progress, session.getContent().dataSize);
            for (IImageShareEventListener listener : this.mListeners) {
                listener.onIshTransferProgressEvent(String.valueOf(session.mContent.shareId), progress);
            }
            return;
        }
        Log.e(LOG_TAG, "Session is Not found");
    }

    public void ishIncomingSessionEvent(IshIncomingSessionEvent event) {
        final int sessionId = event.mSessionId;
        final ImsUri remoteUri = event.mRemoteUri;
        String userAlias = event.mUserAlias;
        final IshFileTransfer ft = event.mFt;
        String str = LOG_TAG;
        Log.i(str, "onIshIncomingSessionEvent( #" + sessionId + ", " + IMSLog.checker(remoteUri) + "," + IMSLog.checker(userAlias) + "): Enter");
        if (ft.getMimeType().startsWith(OpenApiTranslationFilter.SOS_CONTENT_TYPE_PREFIX)) {
            Log.v(LOG_TAG, "Skipping OpenAPI incoming session message");
        } else {
            post(new Runnable() {
                public void run() {
                    ImageShare session = ImageShareModule.this.mCache.newIncommingImageShare(ImageShareModule.this, sessionId, remoteUri, ft);
                    if (session != null) {
                        Log.d(ImageShareModule.LOG_TAG, "created incoming session");
                        session.incomingSessionDone();
                    }
                }
            });
        }
    }

    public void notityIncommingSession(long sharedId, ImsUri contactUri, String filePath, long fileSize) {
        this.mIshTranslation.broadcastIncomming(sharedId, contactUri, filePath, fileSize);
    }

    public void notifyLimitExceeded(long sharedId, ImsUri remoteUri) {
        this.mIshTranslation.broadcastLimitExceeded(sharedId, remoteUri);
    }

    public void notifyInvalidDataPath(String path) {
        this.mIshTranslation.broadcastInvalidDataPath(path);
    }

    private void disableIshFeature() {
        Log.d(LOG_TAG, "disableIshFeature");
        ModuleChannel.createChannel(ModuleChannel.CAPDISCOVERY, this).disableFeature((long) Capabilities.FEATURE_ISH);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.csh.ImageShareModule$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason;

        static {
            int[] iArr = new int[CshErrorReason.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason = iArr;
            try {
                iArr[CshErrorReason.CANCELED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.REMOTE_CONNECTION_CLOSED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.REJECTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.USER_BUSY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.TEMPORAIRLY_NOT_AVAILABLE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.NOT_REACHABLE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.ENGINE_ERROR.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.FILE_IO.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.FORMAT_NOT_SUPPORTED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.USER_NOT_FOUND.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.BEARER_LOST.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.NONE.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.REQUEST_TIMED_OUT.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.ACK_TIMEOUT.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.FORBIDDEN.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
        }
    }

    private int ishReasonTranslator(CshErrorReason value) {
        switch (AnonymousClass3.$SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[value.ordinal()]) {
            case 1:
            case 2:
                return 10;
            case 3:
            case 4:
            case 5:
            case 6:
                return 4;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return 3;
            case 13:
            case 14:
                return 6;
            case 15:
                return 12;
            default:
                return 9;
        }
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int i = msg.what;
        if (i == 2) {
            ishIncomingSessionEvent((IshIncomingSessionEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 3) {
            ishSessionEstablishedEvent((IshSessionEstablishedEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 4) {
            ishTransferCompleteEvent((IshTransferCompleteEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 5) {
            ishTransferProgressEvent((IshTransferProgressEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 6) {
            ishTransferFailedEvent((IshTransferFailedEvent) ((AsyncResult) msg.obj).result);
        }
    }

    public void handleIntent(Intent intent) {
        this.mIshTranslation.handleIntent(intent);
    }

    private void readConfig(int phoneId) {
        this.mHasImageShareSupport[phoneId] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_IS_AUTH, phoneId), false).booleanValue();
        this.mMaxSize[phoneId] = (long) RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_MAX_SIZE_IMAGE_SHARE, phoneId), 0).intValue();
        String str = LOG_TAG;
        Log.i(str, "readConfig phonId : " + phoneId + " ImageShare enable " + this.mHasImageShareSupport[phoneId] + ", ImageShare Max size " + this.mMaxSize[phoneId]);
        if (SimUtil.getSimMno(phoneId) == Mno.SPRINT && this.mHasImageShareSupport[phoneId]) {
            Log.d(LOG_TAG, "readconfig: isauth true but forced disable for SPRINT");
            disableIshFeature();
            this.mHasImageShareSupport[phoneId] = false;
        }
    }

    /* access modifiers changed from: protected */
    public void updateServiceStatus(int phoneId) {
        super.updateServiceStatus(phoneId);
        boolean status = !this.mIsDuringMultipartyCall && getImsRegistration() != null && CapabilityUtil.hasFeature(this.mEnabledFeatures[phoneId], (long) Capabilities.FEATURE_ISH) && this.mRemoteCapabilities.hasFeature(Capabilities.FEATURE_ISH);
        if (this.mIsServiceReady != status) {
            this.mIsServiceReady = status;
            if (status) {
                this.mIshTranslation.broadcastServiceReady();
            } else {
                this.mIshTranslation.broadcastServiceNotReady();
            }
        }
    }

    public ImsRegistration getImsRegistration() {
        if (this.mRegistrationId != -1) {
            return ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationId);
        }
        return null;
    }

    private void updateFeatures(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "updateFeatures: phoneId: " + phoneId);
        readConfig(phoneId);
        boolean isRcsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, phoneId) == 1;
        if (!this.mHasImageShareSupport[phoneId] || !isRcsEnabled || DmConfigHelper.getImsSwitchValue(this.mContext, "is", phoneId) != 1) {
            Log.d(LOG_TAG, "updateFeatures: RCS is disabled.");
            this.mEnabledFeatures[phoneId] = 0;
            return;
        }
        this.mEnabledFeatures[phoneId] = (long) Capabilities.FEATURE_ISH;
    }

    public void registerImageShareEventListener(IImageShareEventListener listener) {
        this.mListeners.add(listener);
    }
}
