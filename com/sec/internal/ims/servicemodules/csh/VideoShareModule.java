package com.sec.internal.ims.servicemodules.csh;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.ims.servicemodules.csh.event.CshErrorReason;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.ICshSuccessCallback;
import com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.OpenApiTranslationFilter;
import com.sec.internal.ims.servicemodules.csh.event.VshIncomingSessionEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshIntents;
import com.sec.internal.ims.servicemodules.csh.event.VshOrientation;
import com.sec.internal.ims.servicemodules.csh.event.VshSessionEstablishedEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshSessionTerminatedEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshSwitchCameraParams;
import com.sec.internal.ims.servicemodules.csh.event.VshVideoDisplayParams;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.DeviceOrientationStatus;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class VideoShareModule extends CshModuleBase implements IVideoShareModule {
    private static final int DISABLED_ALL_COVERAGES = 0;
    private static final int ENABLED_3G_COVERAGES = 4;
    private static final int ENABLED_ALL_COVERAGES = 1;
    private static final int ENABLED_HSPA_COVERAGES = 8;
    private static final int ENABLED_LTE_COVERAGES = 16;
    private static final int ENABLED_WLAN_COVERAGES = 2;
    private static final int EVENT_CANCEL_SHARE = 5;
    private static final int EVENT_INCOMING_SESSION = 2;
    private static final int EVENT_SESSION_ESTABLISHED = 3;
    private static final int EVENT_SESSION_TEMINATED = 4;
    /* access modifiers changed from: private */
    public static final String EXTRA_SESSIONID;
    /* access modifiers changed from: private */
    public static final String INTENT_MAX_DURATION_TIME;
    /* access modifiers changed from: private */
    public static String LOG_TAG;
    public static final String NAME;
    private boolean[] mHasVideoShareSupport = {false, false};
    private final IvshServiceInterface mImsService;
    /* access modifiers changed from: private */
    public int mInComingTerminateId = -1;
    private long[] mInitialFeatures = {0, 0};
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(VideoShareModule.INTENT_MAX_DURATION_TIME)) {
                int sessionId = intent.getIntExtra(VideoShareModule.EXTRA_SESSIONID, -1);
                VideoShare session = VideoShareModule.this.getSession(sessionId);
                if (session != null) {
                    String access$200 = VideoShareModule.LOG_TAG;
                    Log.d(access$200, "Session #" + sessionId + " duration is approaching/longer than the VS MAX DURATION :" + VideoShareModule.this.maxDurationTime[VideoShareModule.this.mActiveCallPhoneId] + "s");
                    session.maxDurationTime();
                    return;
                }
                String access$2002 = VideoShareModule.LOG_TAG;
                Log.w(access$2002, "Session #" + sessionId + " is not found");
            }
        }
    };
    private int[] mNetworkType = {0, 0};
    private int mRegistrationId = -1;
    private UriGenerator mUriGenerator;
    private int[] mVsAuth = {0, 0};
    /* access modifiers changed from: private */
    public boolean mVshInComingEntered = false;
    /* access modifiers changed from: private */
    public final VshTranslation mVshTranslation;
    /* access modifiers changed from: private */
    public int[] maxDurationTime = {0, 0};

    static {
        Class<VideoShareModule> cls = VideoShareModule.class;
        NAME = cls.getSimpleName();
        LOG_TAG = cls.getSimpleName();
        INTENT_MAX_DURATION_TIME = cls.getName() + ".max_duration_time";
        EXTRA_SESSIONID = cls.getName() + "SessionID";
    }

    public VideoShareModule(Looper looper, Context context, IvshServiceInterface vshServiceInterface) {
        super(looper, context);
        this.mImsService = vshServiceInterface;
        this.mCache = CshCache.getInstance(vshServiceInterface);
        this.mVshTranslation = new VshTranslation(this.mContext, this);
        this.mUriGenerator = UriGeneratorFactory.getInstance().get();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_MAX_DURATION_TIME);
        this.mContext.registerReceiver(this.mIntentReceiver, filter, (String) null, this);
    }

    public String getName() {
        return NAME;
    }

    public String[] getServicesRequiring() {
        return new String[]{"vs"};
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        updateFeatures(phoneId);
    }

    public void start() {
        if (!isRunning()) {
            super.start();
            this.mImsService.registerForVshIncomingSession(this, 2, (Object) null);
            this.mImsService.registerForVshSessionEstablished(this, 3, (Object) null);
            this.mImsService.registerForVshSessionTerminated(this, 4, (Object) null);
        }
    }

    public void stop() {
        for (int i = 0; i < this.mCache.getSize(); i++) {
            if (this.mCache.getSessionAt(i).getContent().shareType == 2) {
                ((VideoShare) this.mCache.getSessionAt(i)).sessionFailed();
            }
        }
        super.stop();
        disableVshFeature();
        this.mImsService.unregisterForVshIncomingSession(this);
        this.mImsService.unregisterForVshSessionEstablished(this);
        this.mImsService.unregisterForVshSessionTerminated(this);
    }

    public void onConfigured(int phoneId) {
        String str = LOG_TAG;
        Log.d(str, "onConfigured: phoneId = " + phoneId);
        updateFeatures(phoneId);
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
        String str = LOG_TAG;
        Log.i(str, "onRegistered() phoneId = " + regiInfo.getPhoneId() + ", services : " + regiInfo.getServices());
        if (regiInfo.getImsProfile() != null) {
            this.mRegistrationId = getRegistrationInfoId(regiInfo);
        }
        updateServiceStatus(regiInfo.getPhoneId());
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (tm != null) {
            int networkType = tm.getNetworkType();
            if (this.mNetworkType[regiInfo.getPhoneId()] != 18) {
                this.mNetworkType[regiInfo.getPhoneId()] = blurNetworkType(networkType);
            }
        }
        this.mHasVideoShareSupport[regiInfo.getPhoneId()] = isVsEnabled(this.mNetworkType[regiInfo.getPhoneId()], regiInfo.getPhoneId());
        if (this.mHasVideoShareSupport[regiInfo.getPhoneId()]) {
            Log.i(LOG_TAG, "enable VSH");
            this.mEnabledFeatures[regiInfo.getPhoneId()] = (long) Capabilities.FEATURE_VSH;
        } else {
            Log.i(LOG_TAG, "disable VSH");
            this.mEnabledFeatures[regiInfo.getPhoneId()] = 0;
        }
        ICapabilityDiscoveryModule discoveryModule = getServiceModuleManager().getCapabilityDiscoveryModule();
        discoveryModule.updateOwnCapabilities(regiInfo.getPhoneId());
        discoveryModule.exchangeCapabilitiesForVSHOnRegi(this.mHasVideoShareSupport[regiInfo.getPhoneId()], regiInfo.getPhoneId());
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        Log.i(LOG_TAG, "onDeregistered");
        super.onDeregistered(regiInfo, errorCode);
        if (getImsRegistration() == null) {
            this.mEnabledFeatures[regiInfo.getPhoneId()] = this.mInitialFeatures[regiInfo.getPhoneId()];
            return;
        }
        this.mRegistrationId = -1;
        updateServiceStatus(regiInfo.getPhoneId());
        this.mEnabledFeatures[regiInfo.getPhoneId()] = this.mInitialFeatures[regiInfo.getPhoneId()];
    }

    public void onNetworkChanged(NetworkEvent event, int phoneId) {
        int network;
        if (event.isWifiConnected) {
            network = 18;
        } else {
            network = event.network;
        }
        if (getImsRegistration() != null) {
            String str = LOG_TAG;
            Log.i(str, "onNetworkChanged: " + event + " network: " + network);
            if (network != this.mNetworkType[phoneId] && network == 3) {
                for (int i = 0; i < this.mCache.getSize(); i++) {
                    IContentShare session = this.mCache.getSessionAt(i);
                    if (session != null && session.getContent().shareType == 2 && session.getContent().shareDirection == 1) {
                        sendMessage(obtainMessage(5, Long.valueOf(session.getContent().shareId)));
                    }
                }
            }
            if (network != this.mNetworkType[phoneId]) {
                boolean hasVideoShareSupport = isVsEnabled(network, phoneId);
                if (this.mHasVideoShareSupport[phoneId] != hasVideoShareSupport) {
                    if (hasVideoShareSupport) {
                        Log.i(LOG_TAG, "enable VSH");
                        this.mEnabledFeatures[phoneId] = (long) Capabilities.FEATURE_VSH;
                    } else {
                        Log.i(LOG_TAG, "disable VSH");
                        this.mEnabledFeatures[phoneId] = 0;
                    }
                    ICapabilityDiscoveryModule discoveryModule = getServiceModuleManager().getCapabilityDiscoveryModule();
                    discoveryModule.updateOwnCapabilities(phoneId);
                    if (!(network == 18 || this.mNetworkType[phoneId] == 18)) {
                        discoveryModule.exchangeCapabilitiesForVSHOnRegi(hasVideoShareSupport, phoneId);
                    }
                }
                this.mHasVideoShareSupport[phoneId] = hasVideoShareSupport;
            }
        }
        this.mNetworkType[phoneId] = network;
    }

    public void onCallStateChanged(int phoneId, List<ICall> calls) {
        processCallStateChanged(phoneId, new CopyOnWriteArrayList(calls));
    }

    private void processCallStateChanged(int phoneId, CopyOnWriteArrayList<ICall> calls) {
        super.onCallStateChanged(phoneId, calls);
        if (this.mNPrevConnectedCalls == 0 || this.mIsDuringMultipartyCall) {
            for (int i = 0; i < this.mCache.getSize(); i++) {
                if (this.mCache.getSessionAt(i).getContent().shareType == 2) {
                    Log.i(LOG_TAG, "processCallStateChanged: call cancelByUserSession");
                    ((VideoShare) this.mCache.getSessionAt(i)).cancelByUserSession();
                }
            }
        }
    }

    public int getMaxDurationTime() {
        return this.maxDurationTime[this.mActiveCallPhoneId];
    }

    public Context getContext() {
        return this.mContext;
    }

    public Future<VideoShare> createShare(final ImsUri contactUri, final String videoPath) {
        FutureTask<VideoShare> future = new FutureTask<>(new Callable<VideoShare>() {
            public VideoShare call() throws Exception {
                Log.i(VideoShareModule.LOG_TAG, "createShare");
                if (VideoShareModule.this.getImsRegistration() == null) {
                    VideoShareModule.this.mVshTranslation.broadcastCommunicationError();
                    return null;
                }
                VideoShare session = VideoShareModule.this.mCache.newOutgoingVideoShare(VideoShareModule.this, contactUri, videoPath);
                if (session != null) {
                    session.startQutgoingSession();
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
        VideoShare session = getSession(sharedId);
        if (session != null) {
            session.acceptIncomingSession();
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + sharedId);
        this.mVshTranslation.broadcastCommunicationError();
    }

    public void cancelShare(long sharedId) {
        String str = LOG_TAG;
        Log.i(str, "cancelShare sharedId " + sharedId);
        VideoShare session = getSession(sharedId);
        if (session != null) {
            session.cancelByUserSession();
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + sharedId);
        this.mVshTranslation.broadcastCommunicationError();
    }

    public void toggleCamera(long sharedId) {
        String str = LOG_TAG;
        Log.i(str, "toggleCamera sharedId " + sharedId);
        if (getSession(sharedId) != null) {
            this.mImsService.switchCamera(new VshSwitchCameraParams(new ICshSuccessCallback() {
                public void onSuccess() {
                }

                public void onFailure() {
                    Log.d(VideoShareModule.LOG_TAG, "IToggleCamera onFailure");
                }
            }));
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + sharedId);
        this.mVshTranslation.broadcastCommunicationError();
    }

    public void changeSurfaceOrientation(long sharedId, int orientation) {
        VideoShare session = getSession(sharedId);
        if (session != null) {
            session.setPhoneOrientation(DeviceOrientationStatus.translate(orientation));
            String str = LOG_TAG;
            Log.i(str, "changeSurfaceOrientation sharedId : " + sharedId + " onSuccess");
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + sharedId);
        this.mVshTranslation.broadcastCommunicationError();
    }

    public VideoShare getSession(long sharedId) {
        try {
            return (VideoShare) super.getSession(sharedId);
        } catch (ClassCastException e) {
            String str = LOG_TAG;
            Log.w(str, sharedId + " is not Video Share");
            return null;
        }
    }

    public VideoShare getSession(int sessionId) {
        try {
            return (VideoShare) this.mCache.getSession(sessionId);
        } catch (ClassCastException e) {
            String str = LOG_TAG;
            Log.w(str, sessionId + " is not Video Share");
            return null;
        }
    }

    private void disableVshFeature() {
        Log.d(LOG_TAG, "disableVshFeature");
        ModuleChannel.createChannel(ModuleChannel.CAPDISCOVERY, this).disableFeature((long) Capabilities.FEATURE_VSH);
    }

    private void vshIncomingSessionEvent(VshIncomingSessionEvent event) {
        int sessionId = event.mSessionId;
        ImsUri remoteUri = event.mRemoteUri;
        String contentType = event.mContentType;
        int source = event.mSource;
        String filePath = event.mFilePath;
        String str = LOG_TAG;
        Log.i(str, "vshIncomingSessionEvent #" + sessionId + ", " + IMSLog.checker(remoteUri));
        this.mVshInComingEntered = true;
        this.mInComingTerminateId = -1;
        if (contentType == null || !contentType.startsWith(OpenApiTranslationFilter.SOS_CONTENT_TYPE_PREFIX)) {
            final int i = sessionId;
            final int i2 = source;
            final String str2 = filePath;
            final ImsUri imsUri = remoteUri;
            post(new Runnable() {
                public void run() {
                    String videoPath;
                    if (i == VideoShareModule.this.mInComingTerminateId) {
                        Log.d(VideoShareModule.LOG_TAG, "InComing Video Share is already cancelled by stack");
                        boolean unused = VideoShareModule.this.mVshInComingEntered = false;
                        int unused2 = VideoShareModule.this.mInComingTerminateId = -1;
                        return;
                    }
                    if (i2 == 1) {
                        videoPath = VshIntents.LIVE_VIDEO_CONTENTPATH;
                    } else {
                        videoPath = str2;
                    }
                    VideoShare session = VideoShareModule.this.mCache.newIncommingVideoShare(VideoShareModule.this, i, imsUri, videoPath);
                    if (session != null) {
                        Log.i(VideoShareModule.LOG_TAG, "created incoming session");
                        session.incomingSessionDone();
                    }
                    boolean unused3 = VideoShareModule.this.mVshInComingEntered = false;
                    int unused4 = VideoShareModule.this.mInComingTerminateId = -1;
                }
            });
            return;
        }
        Log.v(LOG_TAG, "Skipping OpenAPI incoming session message");
    }

    public void notityIncommingSession(long sharedId, ImsUri contactUri, String filePath) {
        ImsUri uri;
        boolean hasActiveCall = false;
        new ArrayList();
        for (Integer phoneId : this.mActiveCallLists.keySet()) {
            Iterator<ICall> it = ((List) this.mActiveCallLists.get(phoneId)).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ICall call = it.next();
                if (call.isConnected() && (uri = this.mUriGenerator.getNormalizedUri(call.getNumber(), true)) != null && uri.equals(contactUri)) {
                    hasActiveCall = true;
                    break;
                }
            }
        }
        if (!hasActiveCall) {
            sendMessage(obtainMessage(5, Long.valueOf(sharedId)));
        } else {
            this.mVshTranslation.broadcastIncomming(sharedId, contactUri, filePath);
        }
    }

    public void notifyApprochingVsMaxDuration(long sharedId, int remainingTime) {
        this.mVshTranslation.broadcastApproachingVsMaxDuration(sharedId, remainingTime);
    }

    private void vshSessionEstablishedEvent(VshSessionEstablishedEvent event) {
        String str = LOG_TAG;
        Log.i(str, "vshSessionEstablishedEvent session #" + event.mSessionId);
        VideoShare session = getSession(event.mSessionId);
        if (session != null) {
            PendingIntent pendingIntent = null;
            if (this.maxDurationTime[this.mActiveCallPhoneId] != 0) {
                Intent intent = new Intent(INTENT_MAX_DURATION_TIME);
                intent.putExtra(EXTRA_SESSIONID, event.mSessionId);
                pendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
            }
            session.sessioinEstablished(event.mResolution, pendingIntent);
            this.mVshTranslation.broadcastConnected(session.getContent().shareId, session.getContent().shareContactUri);
            session.setPhoneOrientation(DeviceOrientationStatus.getDeviceOrientation(this.mContext));
            return;
        }
        Log.e(LOG_TAG, "Session is Not found");
    }

    private void vshSessionTerminatedEvent(VshSessionTerminatedEvent event) {
        String str = LOG_TAG;
        Log.i(str, "vshSessionTerminatedEvent session #" + event.mSessionId + " Reason : " + event.mReason);
        VideoShare session = getSession(event.mSessionId);
        ImsRegistration imsRegistration = getImsRegistration();
        if (session != null) {
            session.sessionTerminatedByStack();
            CshInfo content = session.getContent();
            if (imsRegistration != null) {
                getServiceModuleManager().getCapabilityDiscoveryModule().exchangeCapabilitiesForVSH(imsRegistration.getPhoneId(), true);
            }
            int error = vshReasonTranslator(event.mReason);
            content.reasonCode = error;
            this.mVshTranslation.broadcastCanceled(content.shareId, content.shareContactUri, content.shareDirection, error);
            if (event.mReason == CshErrorReason.RTP_RTCP_TIMEOUT) {
                this.mVshTranslation.broadcastCshServiceNotReady();
            }
            if (event.mReason == CshErrorReason.CSH_CAM_ERROR) {
                this.mVshTranslation.broadcastCshCamError();
                return;
            }
            return;
        }
        Log.d(LOG_TAG, "Already removed session");
        if (this.mVshInComingEntered) {
            this.mInComingTerminateId = event.mSessionId;
        }
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int i = msg.what;
        if (i == 2) {
            vshIncomingSessionEvent((VshIncomingSessionEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 3) {
            vshSessionEstablishedEvent((VshSessionEstablishedEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 4) {
            vshSessionTerminatedEvent((VshSessionTerminatedEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 5) {
            cancelShare(((Long) msg.obj).longValue());
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.csh.VideoShareModule$5  reason: invalid class name */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason;

        static {
            int[] iArr = new int[CshErrorReason.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason = iArr;
            try {
                iArr[CshErrorReason.CANCELED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.USER_BUSY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.REJECTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.TEMPORAIRLY_NOT_AVAILABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.ENGINE_ERROR.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.FILE_IO.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.FORMAT_NOT_SUPPORTED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.REQUEST_TIMED_OUT.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.USER_NOT_FOUND.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.ACK_TIMED_OUT.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.BEARER_LOST.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.NORMAL.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.UNKNOWN.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.FORBIDDEN.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[CshErrorReason.RTP_RTCP_TIMEOUT.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
        }
    }

    private int vshReasonTranslator(CshErrorReason value) {
        switch (AnonymousClass5.$SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[value.ordinal()]) {
            case 1:
                return 2;
            case 2:
            case 3:
            case 4:
                return 4;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return 3;
            case 12:
            case 13:
                return 10;
            case 14:
                return 13;
            case 15:
                return 6;
            default:
                return 9;
        }
    }

    private int blurNetworkType(int network) {
        if (!(network == 1 || network == 2)) {
            if (network == 15) {
                return 10;
            }
            if (network != 16) {
                switch (network) {
                    case 8:
                    case 9:
                    case 10:
                        return 10;
                    default:
                        return network;
                }
            }
        }
        return 16;
    }

    public void handleIntent(Intent intent) {
        this.mVshTranslation.handleIntent(intent);
    }

    public void setVshPhoneOrientation(VshOrientation orientation) {
        this.mImsService.setVshPhoneOrientation(orientation);
    }

    public void setVshVideoDisplay(VshVideoDisplayParams params) {
        this.mImsService.setVshVideoDisplay(params);
    }

    public void resetVshVideoDisplay(VshVideoDisplayParams params) {
        this.mImsService.resetVshVideoDisplay(params);
    }

    private void readConfig(int phoneId) {
        this.mVsAuth[phoneId] = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_VS_AUTH, phoneId), 0).intValue();
        String str = LOG_TAG;
        Log.i(str, "readConfig: VsAuth " + this.mVsAuth[phoneId]);
        this.maxDurationTime[phoneId] = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId("maxtimevideoshare", phoneId), 0).intValue();
        if (SimUtil.getSimMno(phoneId) == Mno.SPRINT && this.mVsAuth[phoneId] == 1) {
            Log.d(LOG_TAG, "readconfig: vsauth true but forced disable for SPRINT");
            disableVshFeature();
            this.mVsAuth[phoneId] = 0;
        }
    }

    public boolean isVsEnabled(int networkType, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "networkType is " + networkType + ", VsAuth is " + this.mVsAuth[phoneId]);
        int[] iArr = this.mVsAuth;
        if (iArr[phoneId] == 0) {
            return false;
        }
        if ((iArr[phoneId] & 1) > 0) {
            return true;
        }
        switch (networkType) {
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 12:
                if ((iArr[phoneId] & 4) > 0) {
                    return true;
                }
                return false;
            case 10:
            case 15:
                if ((iArr[phoneId] & 8) > 0) {
                    return true;
                }
                return false;
            case 13:
                if ((iArr[phoneId] & 16) > 0) {
                    return true;
                }
                return false;
            case 18:
                if ((iArr[phoneId] & 2) > 0) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public void updateServiceStatus(int phoneId) {
        super.updateServiceStatus(phoneId);
        boolean status = !this.mIsDuringMultipartyCall && getImsRegistration() != null && CapabilityUtil.hasFeature(this.mEnabledFeatures[phoneId], (long) Capabilities.FEATURE_VSH) && this.mRemoteCapabilities.hasFeature(Capabilities.FEATURE_VSH);
        if (this.mIsServiceReady != status) {
            this.mIsServiceReady = status;
            if (status) {
                this.mVshTranslation.broadcastServiceReady();
            } else {
                this.mVshTranslation.broadcastServiceNotReady();
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
        readConfig(phoneId);
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (tm != null) {
            int networkType = tm.getNetworkType();
            int[] iArr = this.mNetworkType;
            if (iArr[phoneId] != 18) {
                iArr[phoneId] = blurNetworkType(networkType);
            }
        }
        this.mHasVideoShareSupport[phoneId] = isVsEnabled(this.mNetworkType[phoneId], phoneId);
        Log.i(LOG_TAG, "updateFeatures: phoneId " + phoneId + ", HasVideoShareSupport = " + this.mHasVideoShareSupport[phoneId]);
        boolean isVsEnabled = false;
        boolean isRcsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, phoneId) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "vs", phoneId) == 1) {
            isVsEnabled = true;
        }
        if (this.mVsAuth[phoneId] == 0 || !isRcsEnabled || !isVsEnabled) {
            Log.d(LOG_TAG, "updateFeatures: RCS is disabled.");
            this.mEnabledFeatures[phoneId] = 0;
        } else {
            this.mEnabledFeatures[phoneId] = (long) Capabilities.FEATURE_VSH;
        }
        this.mInitialFeatures[phoneId] = this.mEnabledFeatures[phoneId];
    }
}
