package com.sec.internal.ims.servicemodules.volte2;

import android.net.Network;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import com.sec.ims.volte2.IImsMediaCallProvider;
import com.sec.ims.volte2.IVideoServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.servicemodules.volte2.data.DtmfInfo;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImsMediaController extends IImsMediaCallProvider.Stub implements IImsMediaController {
    public static final int CAMERA_ID_DEFAULT = 2;
    public static final int CAMERA_ID_FRONT = 1;
    public static final int CAMERA_ID_REAR = 0;
    private static final int EVENT_IMS_MEDIA_EVENT = 1;
    private static final String LOG_TAG = ImsMediaController.class.getSimpleName();
    private List<ImsCallSession> mCallSessions = null;
    private final RemoteCallbackList<IVideoServiceEventListener> mCallbacks = new RemoteCallbackList<>();
    private int mDefaultCameraId = -1;
    private SimpleEventLog mEventLog;
    private boolean mIsUsingCamera = false;
    private Handler mMediaEventHandler = null;
    private IMediaServiceInterface mMediaSvcIntf = null;
    private int mPendingCameraId = -1;
    private int mPendingCameraRequestor = -1;
    private IVolteServiceModuleInternal mVolteServiceModule = null;

    public ImsMediaController(IVolteServiceModuleInternal vsm, Looper looper, SimpleEventLog eventLog) {
        this.mEventLog = eventLog;
        this.mCallSessions = new ArrayList();
        this.mVolteServiceModule = vsm;
        this.mMediaSvcIntf = vsm.getMediaSvcIntf();
        this.mMediaEventHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                AsyncResult ar = (AsyncResult) msg.obj;
                if (msg.what == 1) {
                    ImsMediaController.this.onImsMediaEvent((IMSMediaEvent) ar.result);
                }
            }
        };
        init();
    }

    public void init() {
        this.mMediaSvcIntf.registerForMediaEvent(this.mMediaEventHandler, 1, (Object) null);
    }

    public void registerForMediaEvent(ImsCallSession session) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "registerForMediaEvent: session " + session.getSessionId());
            synchronized (this.mCallSessions) {
                this.mCallSessions.add(session);
            }
            return;
        }
        Log.e(LOG_TAG, "registerForMediaEvent: session null!!!");
    }

    public void unregisterForMediaEvent(ImsCallSession session) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "unregisterForMediaEvent: session " + session.getSessionId());
            synchronized (this.mCallSessions) {
                this.mCallSessions.remove(session);
            }
            return;
        }
        Log.e(LOG_TAG, "unregisterForMediaEvent: session null!!!");
    }

    public synchronized void registerForVideoServiceEvent(IVideoServiceEventListener listener) {
        Log.i(LOG_TAG, "registerForVideoServiceEvent");
        this.mCallbacks.register(listener);
    }

    public synchronized void unregisterForVideoServiceEvent(IVideoServiceEventListener listener) {
        Log.i(LOG_TAG, "unregisterForVideoServiceEvent");
        this.mCallbacks.unregister(listener);
    }

    public void setCamera(String cameraId) {
        try {
            int parseInt = Integer.parseInt(cameraId);
            this.mDefaultCameraId = parseInt;
            this.mMediaSvcIntf.setCamera(parseInt);
        } catch (NumberFormatException e) {
            String str = LOG_TAG;
            Log.i(str, "Invalid for ImsVideoCall : setCamera- " + cameraId);
        }
    }

    public void setPreviewSurface(Surface surface) {
        this.mMediaSvcIntf.setPreviewSurface(surface, 0);
    }

    public void setPreviewSurfaceForPhoneId(int phoneId, Surface surface) {
        this.mMediaSvcIntf.setPreviewSurface(surface, 0);
    }

    public void setDisplaySurface(Surface surface) {
        this.mMediaSvcIntf.setDisplaySurface(surface, 0);
    }

    public void setDisplaySurfaceForPhoneId(int phoneId, Surface surface) {
        this.mMediaSvcIntf.setDisplaySurface(surface, 0);
    }

    public void setDeviceOrientation(int rotation) {
        this.mMediaSvcIntf.setOrientation(rotation);
    }

    public void setZoom(float value) {
        this.mMediaSvcIntf.setZoom(value);
    }

    public void requestCallDataUsage() {
        this.mMediaSvcIntf.requestCallDataUsage();
    }

    public void holdVideo(int sessionId) {
        String str = LOG_TAG;
        Log.i(str, "holdVideo: sessionId=" + sessionId);
        int phoneId = 0;
        ImsCallSession session = getSession(sessionId);
        if (!(session == null || session.getCallState() == null)) {
            phoneId = session.getPhoneId();
        }
        IMSLog.c(LogClass.VOLTE_HOLD_VIDEO, phoneId + "," + sessionId);
        this.mMediaSvcIntf.holdVideo(phoneId, sessionId);
        setVideoPause(sessionId, true);
    }

    public void resumeVideo(int sessionId) {
        String str = LOG_TAG;
        Log.i(str, "resumeVideo: sessionId=" + sessionId);
        int phoneId = 0;
        ImsCallSession session = getSession(sessionId);
        if (!(session == null || session.getCallState() == null)) {
            phoneId = session.getPhoneId();
        }
        IMSLog.c(LogClass.VOLTE_RESUME_VIDEO, phoneId + "," + sessionId);
        this.mMediaSvcIntf.resumeVideo(phoneId, sessionId);
    }

    public void setPreviewResolution(int width, int height) {
        String str = LOG_TAG;
        Log.i(str, "setPreviewResolution width : " + width + " height : " + height);
        this.mMediaSvcIntf.setPreviewResolution(width, height);
    }

    private synchronized void logCamera(boolean isStart, int sessionId, int camera, boolean isNotification) {
        ImsCallSession session;
        if (this.mIsUsingCamera != isStart) {
            String state = "null";
            if (!(sessionId < 0 || (session = getSession(sessionId)) == null || session.getCallState() == null)) {
                state = session.getCallState().name();
            }
            SimpleEventLog simpleEventLog = this.mEventLog;
            StringBuilder sb = new StringBuilder();
            sb.append(isStart ? "start" : "stop");
            sb.append("Camera: sessionId=");
            sb.append(sessionId);
            sb.append(" (");
            sb.append(state);
            sb.append("), camera=");
            sb.append(camera);
            sb.append(" noti=");
            sb.append(isNotification);
            simpleEventLog.add(sb.toString());
            this.mIsUsingCamera = isStart;
        }
    }

    public void startCamera(Surface surface) {
        Log.i(LOG_TAG, "startCamera:");
        logCamera(true, -1, -1, false);
        this.mMediaSvcIntf.startCamera(surface);
    }

    public void startCamera(int sessionId, int camera) {
        ImsCallSession activeSession;
        String str = LOG_TAG;
        Log.i(str, "startCamera: sessionId=" + sessionId + " camera=" + camera);
        ImsCallSession cameraSession = getSession(sessionId);
        if (sessionId < 0 && (activeSession = getActiveCall()) != null) {
            sessionId = activeSession.getSessionId();
            String str2 = LOG_TAG;
            Log.i(str2, "startCamera: using active sessionId=" + sessionId + " camera=" + camera);
        }
        ImsCallSession s = getCameraUsingSession();
        if (s != null) {
            if (s.getSessionId() == sessionId) {
                String str3 = LOG_TAG;
                Log.i(str3, "startCamera: camera already active for session " + sessionId);
                if (this.mDefaultCameraId == -1) {
                    this.mDefaultCameraId = camera;
                    return;
                }
                return;
            } else if (s.getCallState() == CallConstants.STATE.VideoHeld) {
                s.stopCamera();
            } else {
                String str4 = LOG_TAG;
                Log.i(str4, "startCamera: camera in use. pending sesssion " + sessionId);
                this.mPendingCameraRequestor = sessionId;
                this.mPendingCameraId = camera;
                if (cameraSession != null) {
                    cameraSession.setUsingCamera(false);
                    return;
                }
                return;
            }
        }
        this.mPendingCameraRequestor = -1;
        this.mPendingCameraId = -1;
        if (camera != 2 && camera >= 0) {
            this.mDefaultCameraId = camera;
        }
        if (cameraSession != null) {
            cameraSession.setUsingCamera(true);
        }
        int phoneId = 0;
        ImsCallSession session = getSession(sessionId);
        if (session != null) {
            phoneId = session.getPhoneId();
        }
        IMSLog.c(LogClass.VOLTE_START_CAMERA, phoneId + "," + sessionId + "," + camera);
        logCamera(true, sessionId, camera, false);
        this.mMediaSvcIntf.startCamera(phoneId, sessionId, this.mDefaultCameraId);
    }

    public void startCameraForActiveExcept(int session) {
        String str = LOG_TAG;
        Log.i(str, "startCameraForActiveExcept: " + session);
        ImsCallSession s = getActiveExcept(session);
        if (s != null) {
            Log.i(LOG_TAG, "active VT session found");
            s.startLastUsedCamera();
        }
    }

    public void stopCamera() {
        int phoneId = 0;
        for (ImsCallSession s : this.mCallSessions) {
            s.setUsingCamera(false);
            phoneId = s.getPhoneId();
        }
        Log.i(LOG_TAG, "stopCamera:");
        logCamera(false, -1, -1, false);
        IMSLog.c(LogClass.VOLTE_STOP_CAMERA, "" + phoneId);
        this.mMediaSvcIntf.stopCamera(phoneId);
    }

    public void stopActiveCamera() {
        Log.i(LOG_TAG, "stopActiveCamera:");
        ImsCallSession s = getCameraUsingSession();
        if (s != null) {
            Log.i(LOG_TAG, "active VT session found");
            s.stopCamera();
        }
    }

    public void stopCamera(int sessionId) {
        String str = LOG_TAG;
        Log.i(str, "stopCamera: sessionId=" + sessionId);
        ImsCallSession session = getSession(sessionId);
        if (session == null || session.getUsingCamera()) {
            if (this.mPendingCameraRequestor == sessionId) {
                this.mPendingCameraRequestor = -1;
                ImsCallSession s = getCameraUsingSession();
                if (!(s == null || s.getSessionId() == sessionId)) {
                    Log.i(LOG_TAG, "stopCamera: cancel pending camera.");
                    return;
                }
            }
            int phoneId = 0;
            for (ImsCallSession s2 : this.mCallSessions) {
                s2.setUsingCamera(false);
                if (s2.getSessionId() == sessionId) {
                    phoneId = s2.getPhoneId();
                }
            }
            logCamera(false, sessionId, -1, false);
            this.mMediaSvcIntf.stopCamera(phoneId);
            if (this.mPendingCameraRequestor > 0) {
                String str2 = LOG_TAG;
                Log.i(str2, "stopCamera: start camera for pending session " + this.mPendingCameraRequestor);
                ImsCallSession pendingSession = getSession(this.mPendingCameraRequestor);
                if (!(pendingSession == null || pendingSession.getCallState() == CallConstants.STATE.ReadyToCall)) {
                    logCamera(true, this.mPendingCameraRequestor, this.mPendingCameraId, false);
                    this.mMediaSvcIntf.startCamera(pendingSession.getPhoneId(), this.mPendingCameraRequestor, this.mPendingCameraId);
                    pendingSession.setUsingCamera(true);
                }
                this.mPendingCameraRequestor = -1;
                this.mPendingCameraId = -1;
                return;
            }
            return;
        }
        Log.i(LOG_TAG, "Do not call stopCamera multiple times");
    }

    public void switchCamera() {
        String str = LOG_TAG;
        Log.i(str, "switchCamera: current camera " + this.mDefaultCameraId);
        ImsCallSession session = getCameraUsingSession();
        if (session == null || session.getCallState() == CallConstants.STATE.IncomingCall) {
            Log.i(LOG_TAG, "switchCamera: skip because incoming vtcall state");
            return;
        }
        if (this.mDefaultCameraId == 1) {
            this.mDefaultCameraId = 0;
        } else {
            this.mDefaultCameraId = 1;
        }
        this.mMediaSvcIntf.switchCamera();
    }

    public void resetCameraId() {
        Log.i(LOG_TAG, "resetCameraId:");
        this.mDefaultCameraId = -1;
        this.mMediaSvcIntf.resetCameraId();
    }

    public void getCameraInfo(int id) {
        this.mMediaSvcIntf.getCameraInfo(id);
    }

    public void startRender(boolean isNearEnd) {
        this.mMediaSvcIntf.startRender(isNearEnd);
    }

    public void startVideoRenderer(Surface surface) {
        this.mMediaSvcIntf.startVideoRenderer(surface);
    }

    public void stopVideoRenderer() {
        this.mMediaSvcIntf.stopVideoRenderer();
    }

    public void swipeVideoSurface() {
        this.mMediaSvcIntf.swipeVideoSurface();
    }

    public void deinitSurface(boolean isNearEnd) {
        this.mMediaSvcIntf.deinitSurface(isNearEnd);
    }

    public void getMaxZoom() {
        this.mMediaSvcIntf.getMaxZoom();
    }

    public void getZoom() {
        this.mMediaSvcIntf.getZoom();
    }

    public int getDefaultCameraId() {
        return this.mDefaultCameraId;
    }

    public synchronized void startRecord(String filePath) {
        ImsCallSession session = getActiveCall();
        if (session == null) {
            IMSMediaEvent event = new IMSMediaEvent();
            event.setState(IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE);
            onRecordEvent(event);
            return;
        }
        if (filePath.isEmpty() || !filePath.contains("VideoCall")) {
            filePath = Environment.getExternalStorageDirectory().toString() + "/VideoCall";
        }
        if (!filePath.contains(".mp4")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
            filePath = filePath.concat("/" + dateFormat.format(new Date()) + ".mp4");
        }
        File file = new File(filePath);
        new File(file.isDirectory() ? file.getPath() : file.getParent()).mkdirs();
        this.mMediaSvcIntf.startRecord(session.getPhoneId(), session.getSessionId(), filePath);
    }

    public synchronized void stopRecord() {
        ImsCallSession session = getActiveCall();
        if (session != null) {
            this.mMediaSvcIntf.stopRecord(session.getPhoneId(), session.getSessionId());
        } else {
            IMSMediaEvent event = new IMSMediaEvent();
            event.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE);
            onRecordEvent(event);
        }
    }

    public synchronized void startEmoji(String emojiInfo) {
        ImsCallSession session = getActiveCall();
        if (session != null) {
            this.mMediaSvcIntf.startEmoji(session.getPhoneId(), session.getSessionId(), emojiInfo);
        } else {
            IMSMediaEvent event = new IMSMediaEvent();
            event.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE);
            onEmojiEvent(event);
        }
    }

    public synchronized void stopEmoji(int sessionId) {
        String str = LOG_TAG;
        Log.i(str, "stopEmoji : " + sessionId);
        this.mMediaSvcIntf.stopEmoji(0, sessionId);
    }

    public void sendLiveVideo(int sessionId) {
        Log.i(LOG_TAG, "sendStillImage() disable");
        this.mMediaSvcIntf.sendStillImage(sessionId, false, (String) null, (String) null);
    }

    public void sendStillImage(int sessionId, String filePath, int imageFormat, String frameSize, int toFlip) {
        String str = LOG_TAG;
        Log.i(str, "sendStillImage() enable filePath: " + filePath + " frameSize: " + frameSize);
        this.mMediaSvcIntf.sendStillImage(sessionId, true, filePath, frameSize);
    }

    public void setCameraEffect(int value) {
        String str = LOG_TAG;
        Log.i(str, "setCameraEffect() value: " + value);
        this.mMediaSvcIntf.setCameraEffect(value);
    }

    public void bindToNetwork(Network network) {
        String str = LOG_TAG;
        Log.i(str, "bindToNetwork() " + network);
        this.mMediaSvcIntf.bindToNetwork(network);
    }

    private String getFrameSize() {
        ImsCallSession session = getActiveCall();
        if (session != null) {
            return session.getCallProfile().getMediaProfile().getVideoSize();
        }
        return "VGA";
    }

    private ImsCallSession getCameraUsingSession() {
        for (ImsCallSession s : this.mCallSessions) {
            if (s.getUsingCamera()) {
                return s;
            }
        }
        return null;
    }

    private ImsCallSession getActiveExcept(int session) {
        for (ImsCallSession s : this.mCallSessions) {
            if (s != null && s.getSessionId() != session && s.getCallState() == CallConstants.STATE.IncomingCall && ImsCallUtil.isVideoCall(s.getCallProfile().getCallType())) {
                return s;
            }
        }
        for (ImsCallSession s2 : this.mCallSessions) {
            if (s2 != null && s2.getSessionId() != session && s2.getCallState() == CallConstants.STATE.InCall && ImsCallUtil.isCameraUsingCall(s2.getCallProfile().getCallType())) {
                return s2;
            }
        }
        return null;
    }

    private ImsCallSession getActiveCall() {
        for (ImsCallSession s : this.mCallSessions) {
            if (s != null && s.getCallState() == CallConstants.STATE.InCall) {
                return s;
            }
        }
        return null;
    }

    private void onCaptureEvent(IMSMediaEvent event, boolean success) {
        String str = LOG_TAG;
        Log.i(str, "onCaptureEvent: success=" + success);
    }

    private synchronized void onCameraEvent(IMSMediaEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onCameraEvent " + event.getState());
        int state = -1;
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS) {
            state = 1;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_STOP_SUCCESS) {
            state = 3;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL) {
            state = 2;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_SUCCESS) {
            state = 5;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_FAIL) {
            state = 6;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_DISABLED_ERROR) {
            state = 7;
        }
        if (state == -1) {
            Log.i(LOG_TAG, "camera state not supported");
            return;
        }
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onCameraState(event.getSessionID(), state);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onVideoQuality(IMSMediaEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onVideoQuality " + event.getState());
        int quality = -1;
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY) {
            quality = 0;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY) {
            quality = 0;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY) {
            quality = 1;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY) {
            quality = 2;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_MAX_QUALITY) {
            quality = 2;
        }
        if (quality == -1) {
            Log.i(LOG_TAG, "video quality not supported");
            return;
        }
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoQualityChanged(event.getSessionID(), quality);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onRecordEvent(IMSMediaEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onRecordEvent " + event.getState());
        int state = -1;
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_START_SUCCESS) {
            state = 0;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE) {
            state = 1;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE_NO_SPACE) {
            state = 2;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS) {
            state = 3;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE) {
            state = 4;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_STOP_NO_SPACE) {
            state = 5;
        }
        if (state == -1) {
            Log.i(LOG_TAG, "unknwon record event");
            return;
        }
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onRecordState(event.getSessionID(), state);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onEmojiEvent(IMSMediaEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onEmojiEvent " + event.getState());
        int state = -1;
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_START_SUCCESS) {
            state = 0;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE) {
            state = 1;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_SUCCESS) {
            state = 2;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_FAILURE) {
            state = 3;
        }
        if (event.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_INFO_CHANGE) {
            this.mMediaSvcIntf.restartEmoji(0, event.getSessionID());
        } else if (state == -1) {
            Log.i(LOG_TAG, "unknown emoji event");
        } else {
            int length = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    this.mCallbacks.getBroadcastItem(i).onEmojiState(event.getSessionID(), state);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallbacks.finishBroadcast();
        }
    }

    private synchronized void onVideoHold(IMSMediaEvent event) {
        Log.i(LOG_TAG, "onVideoHold or no far frame");
        if (!event.isHeldCall()) {
            ImsCallSession session = getSession(event.getSessionID());
            if (session == null || session.getCallState() != CallConstants.STATE.HoldingVideo) {
                int length = this.mCallbacks.beginBroadcast();
                for (int i = 0; i < length; i++) {
                    try {
                        this.mCallbacks.getBroadcastItem(i).onVideoState(event.getSessionID(), 1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                this.mCallbacks.finishBroadcast();
            }
        }
    }

    private synchronized void onVideoResumed(IMSMediaEvent event) {
        Log.i(LOG_TAG, "onVideoResumed or far frame ready");
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoState(event.getSessionID(), 2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onVideoAvailable(IMSMediaEvent event) {
        Log.i(LOG_TAG, "onVideoAvailable");
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoState(event.getSessionID(), 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void onCallDowngraded(IMSMediaEvent event) {
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoState(event.getSessionID(), 3);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onVideoOrientationChanged(IMSMediaEvent event) {
        Log.i(LOG_TAG, "onVideoOrientationChanged");
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoOrientChanged(event.getSessionID());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onCameraFirstFrameReady(IMSMediaEvent event) {
        Log.i(LOG_TAG, "onCameraFirstFrameReady");
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onCameraState(event.getSessionID(), 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private void onClearUsingCamera() {
        synchronized (this.mCallSessions) {
            for (ImsCallSession s : this.mCallSessions) {
                if (s != null) {
                    s.setUsingCamera(false);
                }
            }
        }
    }

    private void onCameraStopSuccess() {
        if (this.mPendingCameraRequestor > 0) {
            String str = LOG_TAG;
            Log.i(str, "CAMERA_STOP_SUCCESS: start camera for pending session " + this.mPendingCameraRequestor);
            ImsCallSession pendingSession = getSession(this.mPendingCameraRequestor);
            if (!(pendingSession == null || pendingSession.getCallState() == CallConstants.STATE.ReadyToCall)) {
                logCamera(true, this.mPendingCameraRequestor, this.mPendingCameraId, false);
                this.mMediaSvcIntf.startCamera(pendingSession.getPhoneId(), this.mPendingCameraRequestor, this.mPendingCameraId);
                pendingSession.setUsingCamera(true);
            }
            this.mPendingCameraRequestor = -1;
            this.mPendingCameraId = -1;
        }
    }

    private synchronized void onChangePeerDimension(IMSMediaEvent event) {
        Log.i(LOG_TAG, "onChangePeerDimension");
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onChangePeerDimension(event.getSessionID(), event.getWidth(), event.getHeight());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void changeCameraCapabilities(int sessionId, int width, int height) {
        Log.i(LOG_TAG, "changeCameraCapabilities");
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).changeCameraCapabilities(sessionId, width, height);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void receiveSessionModifyRequest(int sessionId, CallProfile profile) {
        Log.i(LOG_TAG, "receiveSessionModifyRequest");
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).receiveSessionModifyRequest(sessionId, profile);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void receiveSessionModifyResponse(int sessionId, int status, CallProfile requestedProfile, CallProfile responseProfile) {
        Log.i(LOG_TAG, "receiveSessionModifyResponse");
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).receiveSessionModifyResponse(sessionId, status, requestedProfile, responseProfile);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void setVideoPause(int sessionId, boolean isVideoPause) {
        String str = LOG_TAG;
        Log.i(str, "setVideoPause : " + isVideoPause);
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).setVideoPause(sessionId, isVideoPause);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void onChangeCallDataUsage(int sessionId, long dataUsage) {
        String str = LOG_TAG;
        Log.i(str, "onChangeCallDataUsage : " + dataUsage);
        int length = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onChangeCallDataUsage(sessionId, dataUsage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private ImsCallSession getSession(int sessionId) {
        synchronized (this.mCallSessions) {
            for (ImsCallSession s : this.mCallSessions) {
                if (s != null && s.getSessionId() == sessionId) {
                    return s;
                }
            }
            return null;
        }
    }

    private void onHandleAudioEvent(IMSMediaEvent event) {
        Log.i(LOG_TAG, "handling Audio Event");
        int audioEvent = event.getAudioEvent();
        if (!(audioEvent == 18 || audioEvent == 61)) {
            if (audioEvent == 78) {
                IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mVolteServiceModule;
                if (iVolteServiceModuleInternal != null) {
                    iVolteServiceModuleInternal.sendRtpLossRate(event.getPhoneId(), event.getRtpLossRate());
                    return;
                }
                return;
            } else if (!(audioEvent == 28 || audioEvent == 29 || audioEvent == 31)) {
                if (audioEvent == 32) {
                    this.mMediaSvcIntf.sendRtpStatsToStack(event.getAudioRtpStats());
                    return;
                }
                return;
            }
        }
        this.mMediaSvcIntf.sendMediaEvent(event.getPhoneId(), event.getChannelId(), event.getAudioEvent(), 0);
    }

    private void onHandleTextEvent(IMSMediaEvent event) {
        Log.i(LOG_TAG, "handling Text Event");
        int textEvent = event.getTextEvent();
        if (textEvent != 1) {
            if (textEvent == 2) {
                this.mMediaSvcIntf.sendMediaEvent(event.getPhoneId(), event.getChannelId(), event.getTextEvent(), 2);
            }
        } else if (this.mVolteServiceModule != null) {
            this.mVolteServiceModule.onTextReceived(new TextInfo(event.getSessionID(), event.getRttText(), event.getRttTextLen()));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0022, code lost:
        if (r6.getSessionID() != 1) goto L_0x0048;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean onHandleVideoEvent(com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent r6) {
        /*
            r5 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "handling Video Event"
            android.util.Log.i(r0, r1)
            int r0 = r6.getVideoEvent()
            r1 = 16
            r2 = 1
            if (r0 == r1) goto L_0x0018
            r1 = 117(0x75, float:1.64E-43)
            if (r0 == r1) goto L_0x0025
            switch(r0) {
                case 20: goto L_0x0025;
                case 21: goto L_0x0025;
                case 22: goto L_0x0025;
                case 23: goto L_0x0025;
                default: goto L_0x0017;
            }
        L_0x0017:
            goto L_0x0048
        L_0x0018:
            int r0 = r6.getSessionID()
            if (r0 == 0) goto L_0x0025
            int r0 = r6.getSessionID()
            if (r0 == r2) goto L_0x0025
            goto L_0x0048
        L_0x0025:
            com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface r0 = r5.mMediaSvcIntf
            int r1 = r6.getPhoneId()
            int r3 = r6.getSessionID()
            int r4 = r6.getVideoEvent()
            r0.sendMediaEvent(r1, r3, r4, r2)
            int r0 = r6.getVideoEvent()
            r1 = 20
            if (r0 == r1) goto L_0x0048
            int r0 = r6.getVideoEvent()
            r1 = 21
            if (r0 == r1) goto L_0x0048
            r0 = 0
            return r0
        L_0x0048:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsMediaController.onHandleVideoEvent(com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent):boolean");
    }

    private void onHandleDtmfEvent(IMSMediaEvent event) {
        Log.i(LOG_TAG, "handling DTMF Event");
        if (event.getDtmfEvent() == 0 && this.mVolteServiceModule != null) {
            this.mVolteServiceModule.getCmcServiceHelper().onCmcDtmfInfo(new DtmfInfo(event.getDtmfKey(), -1, -1, -1));
        }
    }

    private ImsCallSession getSessionByIMSMediaEvent(IMSMediaEvent event) {
        for (ImsCallSession s : this.mCallSessions) {
            if (s != null) {
                if (SimUtil.getSimMno(s.getPhoneId()) == Mno.SKT) {
                    String str = LOG_TAG;
                    Log.i(str, "Find conference call session : " + s.getSessionId());
                    return s;
                } else if (s.getSessionId() == event.getSessionID()) {
                    return s;
                }
            }
        }
        return null;
    }

    private void onNotifyIMSMediaEvent(ImsCallSession session, IMSMediaEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onImsMediaEvent: state=" + event.getState() + " phoneId=" + session.getPhoneId());
        switch (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[event.getState().ordinal()]) {
            case 1:
                event.setIsNearEnd(false);
                event.setFileName((String) null);
                onCaptureEvent(event, true);
                break;
            case 2:
                onCaptureEvent(event, false);
                break;
            case 3:
                onVideoHold(event);
                break;
            case 4:
                onVideoResumed(event);
                break;
            case 5:
                onVideoAvailable(event);
                break;
            case 6:
                onVideoOrientationChanged(event);
                break;
            case 7:
                onVideoHold(event);
                break;
            case 8:
                onCameraFirstFrameReady(event);
                break;
            case 9:
                onClearUsingCamera();
                session.setUsingCamera(true);
                session.setStartCameraState(true);
                logCamera(true, -1, -1, true);
                onCameraEvent(event);
                break;
            case 10:
                onClearUsingCamera();
                onCameraStopSuccess();
                logCamera(false, -1, -1, true);
                final int sessionId = session.getSessionId();
                IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mVolteServiceModule;
                if (iVolteServiceModuleInternal != null) {
                    iVolteServiceModuleInternal.post(new Runnable() {
                        public void run() {
                            ImsMediaController.this.startCameraForActiveExcept(sessionId);
                        }
                    });
                }
                onCameraEvent(event);
                break;
            case 11:
                onClearUsingCamera();
                session.setStartCameraState(false);
                onCameraEvent(event);
                break;
            case 12:
                session.onSwitchCamera();
                onCameraEvent(event);
                break;
            case 13:
            case 14:
            case 15:
                onCameraEvent(event);
                break;
            case 16:
                onChangePeerDimension(event);
                break;
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
                onVideoQuality(event);
                break;
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
                onRecordEvent(event);
                break;
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
                onEmojiEvent(event);
                break;
        }
        session.notifyImsMediaEvent(event);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.ImsMediaController$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE;

        static {
            int[] iArr = new int[IMSMediaEvent.MEDIA_STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE = iArr;
            try {
                iArr[IMSMediaEvent.MEDIA_STATE.CAPTURE_SUCCEEDED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAPTURE_FAILED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_HELD.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_RESUMED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_ORIENTATION.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.NO_FAR_FRAME.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_FIRST_FRAME_READY.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_STOP_SUCCESS.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_SUCCESS.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_EVENT.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_FAIL.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_DISABLED_ERROR.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CHANGE_PEER_DIMENSION.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_MAX_QUALITY.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_START_SUCCESS.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE_NO_SPACE.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_STOP_NO_SPACE.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.EMOJI_START_SUCCESS.ordinal()] = 28;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE.ordinal()] = 29;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_SUCCESS.ordinal()] = 30;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_FAILURE.ordinal()] = 31;
            } catch (NoSuchFieldError e31) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.EMOJI_INFO_CHANGE.ordinal()] = 32;
            } catch (NoSuchFieldError e32) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void onImsMediaEvent(IMSMediaEvent event) {
        ImsCallSession session;
        if (event.isAudioEvent()) {
            onHandleAudioEvent(event);
        } else if (event.isTextEvent()) {
            onHandleTextEvent(event);
        } else {
            if (event.isVideoEvent()) {
                if (!onHandleVideoEvent(event)) {
                    return;
                }
            } else if (event.isDtmfEvent()) {
                onHandleDtmfEvent(event);
                return;
            }
            synchronized (this.mCallSessions) {
                session = getSessionByIMSMediaEvent(event);
            }
            if (session == null) {
                String str = LOG_TAG;
                Log.i(str, "onImsMediaEvent: session " + event.getSessionID() + " not found.");
                if (event.getSessionID() != 0 && event.getSessionID() != 1 && event.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS) {
                    stopCamera(event.getSessionID());
                    return;
                }
                return;
            }
            event.setSessionID(session.getSessionId());
            event.setPhoneId(session.getPhoneId());
            onNotifyIMSMediaEvent(session, event);
        }
    }

    public boolean isSupportingCameraMotor() {
        return this.mMediaSvcIntf.isSupportingCameraMotor();
    }
}
