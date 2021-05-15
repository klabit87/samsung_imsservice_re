package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Network;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.MediaHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ModifyVideoData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.NotifyVideoEventData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CallResponse;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.sve.IImsMediaEventListener;
import com.sec.sve.SecVideoEngineManager;

public class ResipMediaHandler extends MediaHandler {
    private static final int DTMF_VOLUME = 85;
    private static final int EVENT_CONNECT_TO_SVE = 300;
    private static final int EVENT_HOLD_VIDEO_RESPONSE = 201;
    private static final int EVENT_MODIFY_VIDEO = 107;
    private static final int EVENT_RESUME_VIDEO_RESPONSE = 202;
    private static final int EVENT_SVE_CONNECTED = 301;
    private static final int EVENT_VIDEO_EVENT = 108;
    public static final int MAX_VIDEO_CODEC_LIST_CHAR_SIZE = 256;
    private static final int NOTIFY_CAM_CAPTURE_FAILURE = 13;
    private static final int NOTIFY_CAM_CAPTURE_SUCCESS = 12;
    private static final int NOTIFY_CAM_DISABLED_ERROR = 16;
    private static final int NOTIFY_CAM_FIRST_FRAME_READY = 8;
    private static final int NOTIFY_CAM_START_FAILURE = 7;
    private static final int NOTIFY_CAM_START_SUCCESS = 6;
    private static final int NOTIFY_CAM_STOP_SUCCESS = 9;
    private static final int NOTIFY_CAM_SWITCH_FAILURE = 11;
    private static final int NOTIFY_CAM_SWITCH_SUCCESS = 10;
    private static final int NOTIFY_CHANGE_PEER_DIMENSION = 15;
    private static final int NOTIFY_EMOJI_INFO_CHANGE = 74;
    private static final int NOTIFY_EMOJI_START_FAILURE = 71;
    private static final int NOTIFY_EMOJI_START_SUCCESS = 70;
    private static final int NOTIFY_EMOJI_STOP_FAILURE = 73;
    private static final int NOTIFY_EMOJI_STOP_SUCCESS = 72;
    private static final int NOTIFY_FAR_FRAME_READY = 5;
    private static final int NOTIFY_LCL_CAPTURE_FAILURE = 2;
    private static final int NOTIFY_LCL_CAPTURE_SUCCESS = 1;
    private static final int NOTIFY_NO_FAR_FRAME = 14;
    private static final int NOTIFY_RECORD_START_FAILURE = 51;
    private static final int NOTIFY_RECORD_START_FAILURE_NO_MEMORY = 52;
    private static final int NOTIFY_RECORD_START_SUCCESS = 50;
    private static final int NOTIFY_RECORD_STOP_FAILURE = 61;
    private static final int NOTIFY_RECORD_STOP_SUCCESS = 60;
    private static final int NOTIFY_RMT_CAPTURE_FAILURE = 4;
    private static final int NOTIFY_RMT_CAPTURE_SUCCESS = 3;
    public static final int NOTIFY_VIDEO_ATTEMPTED = 40;
    public static final int NOTIFY_VIDEO_FAIR_QUALITY = 31;
    public static final int NOTIFY_VIDEO_GOOD_QUALITY = 32;
    public static final int NOTIFY_VIDEO_MAX_QUALITY = 34;
    public static final int NOTIFY_VIDEO_POOR_QUALITY = 30;
    public static final int NOTIFY_VIDEO_RTCP_CLEAR = 23;
    public static final int NOTIFY_VIDEO_RTCP_TIMEOUT = 21;
    public static final int NOTIFY_VIDEO_RTP_CLEAR = 22;
    public static final int NOTIFY_VIDEO_RTP_TIMEOUT = 20;
    public static final int NOTIFY_VIDEO_VERYPOOR_QUALITY = 33;
    private Context mContext;
    public String mHwSupportedVideoCodecList = "";
    private IImsFramework mImsFramework;
    private IImsMediaEventListener mMediaEventlistener = new IImsMediaEventListener.Stub() {
        public void onAudioRtpRtcpTimeout(int channel, int event) {
            String access$000 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$000, "onAudioRtpRtcpTimeout " + event);
            IMSMediaEvent me = new IMSMediaEvent();
            me.setChannelId(channel);
            me.setPhoneId(channel / 8);
            me.setAudioEvent(event);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(me);
        }

        public void onRtpLossRate(int channel, int interval, float lossRate, float jitter) {
            IMSMediaEvent me = new IMSMediaEvent();
            me.setChannelId(channel);
            me.setPhoneId(channel / 8);
            me.setAudioEvent(78);
            me.setRtpLossRate(new RtpLossRateNoti(interval, lossRate, jitter, 0));
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(me);
        }

        public void onRtpStats(int channel, int lossData, int delay, int jitter, int measuredPeriod, int direction) {
            IMSMediaEvent me = new IMSMediaEvent();
            me.setChannelId(channel);
            me.setPhoneId(channel / 8);
            me.setAudioEvent(32);
            me.setAudioRtpStats(new IMSMediaEvent.AudioRtpStats(channel, lossData, delay, jitter, measuredPeriod, direction));
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(me);
        }

        public void onVideoEvent(int result, int event, int sessionId, int arg1, int arg2) {
            String access$400 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$400, "Result : " + result + " event : " + event + " session id : " + sessionId);
            if (result != 3) {
                Log.e(ResipMediaHandler.this.LOG_TAG, "Invalid Video Event");
            }
            if (sessionId >= 1007) {
                Log.e(ResipMediaHandler.this.LOG_TAG, "Ignore PTT Video Event in legacy VoLTE");
                return;
            }
            IMSMediaEvent me = new IMSMediaEvent();
            me.setSessionID(sessionId);
            if (event != 117) {
                switch (event) {
                    case 1:
                    case 3:
                    case 12:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAPTURE_SUCCEEDED);
                        break;
                    case 2:
                    case 4:
                    case 13:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAPTURE_FAILED);
                        break;
                    case 5:
                        me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE);
                        break;
                    case 6:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS);
                        break;
                    case 7:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL);
                        break;
                    case 8:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_FIRST_FRAME_READY);
                        break;
                    case 9:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_STOP_SUCCESS);
                        break;
                    case 10:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_SUCCESS);
                        break;
                    case 11:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_FAIL);
                        break;
                    case 14:
                        me.setState(IMSMediaEvent.MEDIA_STATE.NO_FAR_FRAME);
                        break;
                    case 15:
                        me.setWidth(arg1);
                        me.setHeight(arg2);
                        me.setState(IMSMediaEvent.MEDIA_STATE.CHANGE_PEER_DIMENSION);
                        break;
                    case 16:
                        me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_DISABLED_ERROR);
                        me.setVideoEvent(event);
                        break;
                    default:
                        switch (event) {
                            case 20:
                                me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RTP_TIMEOUT);
                                me.setVideoEvent(event);
                                break;
                            case 21:
                                me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RTCP_TIMEOUT);
                                me.setVideoEvent(event);
                                break;
                            case 22:
                            case 23:
                                break;
                            default:
                                switch (event) {
                                    case 30:
                                        me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY);
                                        break;
                                    case 31:
                                        me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY);
                                        break;
                                    case 32:
                                        me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY);
                                        break;
                                    case 33:
                                        me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY);
                                        break;
                                    case 34:
                                        me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_MAX_QUALITY);
                                        break;
                                    default:
                                        switch (event) {
                                            case 70:
                                                me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_SUCCESS);
                                                break;
                                            case 71:
                                                me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE);
                                                break;
                                            case 72:
                                                me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_SUCCESS);
                                                break;
                                            case 73:
                                                me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_FAILURE);
                                                break;
                                            case 74:
                                                me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_INFO_CHANGE);
                                                break;
                                        }
                                }
                        }
                }
            }
            me.setVideoEvent(event);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(me);
        }

        public void onTextReceive(int channel, int sessionId, String text, int length, int event) {
            String access$800 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$800, "onTextReceive " + event);
            IMSMediaEvent me = new IMSMediaEvent();
            me.setTextEvent(event);
            me.setChannelId(channel);
            me.setSessionID(sessionId);
            me.setPhoneId(channel / 8);
            me.setRttText(text);
            me.setRttTextLen(length);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(me);
        }

        public void onTextRtpRtcpTimeout(int channel, int event) {
            String access$1000 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$1000, "onTextRtpRtcpTimeout " + event);
            IMSMediaEvent me = new IMSMediaEvent();
            me.setTextEvent(event);
            me.setChannelId(channel);
            me.setPhoneId(channel / 8);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(me);
        }

        public void onDtmfEvent(int channel, int dtmfKey) {
            String access$1200 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$1200, "onDtmfEvent dtmfKey : " + dtmfKey);
            IMSMediaEvent me = new IMSMediaEvent();
            me.setDtmfEvent(0);
            me.setDtmfKey(dtmfKey);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(me);
        }

        public void onRecordEvent(int sessionId, int errCode) {
            String access$1400 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$1400, "onRecordEvent errCode : " + errCode);
            IMSMediaEvent me = new IMSMediaEvent();
            me.setSessionID(sessionId);
            if (errCode == 1) {
                me.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_NO_SPACE);
            } else if (errCode == 2) {
                me.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS);
            }
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(me);
        }

        public void onAudioInjectionEnded(long startTime, long stopTime) {
        }

        public void onRecordingStopped(long startTime, long stopTime, String recordingFilePath) {
        }
    };
    private RingBackToneHandler mRingBackToneHandler = null;
    private HandlerThread mRingBackToneThread = null;
    private StackIF mStackIf;
    /* access modifiers changed from: private */
    public boolean mSveConnected = false;
    /* access modifiers changed from: private */
    public boolean mSveConnecting = false;
    private SecVideoEngineManager mSveManager;
    private ToneGenerator mToneGenerator = null;

    public ResipMediaHandler(Looper looper, Context context, IImsFramework imsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = imsFramework;
        this.mSveManager = new SecVideoEngineManager(context, new SecVideoEngineManager.ConnectionListener() {
            public void onDisconnected() {
                Log.i(ResipMediaHandler.this.LOG_TAG, "sve disconnected");
                boolean unused = ResipMediaHandler.this.mSveConnected = false;
                boolean unused2 = ResipMediaHandler.this.mSveConnecting = false;
                ResipMediaHandler.this.sendEmptyMessageDelayed(300, 1000);
            }

            public void onConnected() {
                Log.i(ResipMediaHandler.this.LOG_TAG, "sve connected.");
                boolean unused = ResipMediaHandler.this.mSveConnected = true;
                boolean unused2 = ResipMediaHandler.this.mSveConnecting = false;
                ResipMediaHandler.this.sendEmptyMessage(301);
            }
        });
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerModifyVideoEvent(this, 107, (Object) null);
        this.mStackIf.registerVideoEvent(this, 108, (Object) null);
        this.mSveConnected = false;
        this.mSveConnecting = false;
        sendEmptyMessage(300);
        HandlerThread handlerThread = new HandlerThread("RingBackToneThread");
        this.mRingBackToneThread = handlerThread;
        handlerThread.start();
        this.mRingBackToneHandler = new RingBackToneHandler(this.mRingBackToneThread.getLooper());
    }

    public void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats rtpStats) {
        this.mStackIf.sendRtpStatsToStack(rtpStats);
    }

    public void holdVideo(int phoneId, int sessionId) {
        String str = this.LOG_TAG;
        Log.i(str, "holdVideo: sessionId " + sessionId);
        UserAgent ua = getUa(phoneId);
        if (ua != null) {
            ua.holdVideo(sessionId, obtainMessage(201, Integer.valueOf(sessionId)));
        }
    }

    public void resumeVideo(int phoneId, int sessionId) {
        String str = this.LOG_TAG;
        Log.i(str, "resumeVideo: sessionId " + sessionId);
        UserAgent ua = getUa(phoneId);
        if (ua != null) {
            ua.resumeVideo(sessionId, obtainMessage(202, Integer.valueOf(sessionId)));
        }
    }

    public void startCamera(int phoneId, int sessionId, int cameraId) {
        UserAgent ua = getUa(phoneId);
        if (ua == null) {
            Log.e(this.LOG_TAG, "startCamera: can't find UserAgent for mmtel-video.");
        } else {
            ua.startCamera(sessionId, cameraId);
        }
    }

    public void setPreviewResolution(int width, int height) {
        this.mSveManager.setPreviewResolution(width, height);
    }

    public void setPreviewSurface(Object windowHandle, int color) {
        String str = this.LOG_TAG;
        Log.i(str, "setPreviewSurface() color : " + color);
        this.mSveManager.setPreviewSurface((Surface) windowHandle, color);
    }

    public void setDisplaySurface(Object windowHandle, int color) {
        String str = this.LOG_TAG;
        Log.i(str, "setDisplaySurface() color : " + color);
        this.mSveManager.setDisplaySurface((Surface) windowHandle, color);
    }

    public void stopCamera(int phoneId) {
        UserAgent ua = getUa(phoneId);
        if (ua == null) {
            Log.e(this.LOG_TAG, "stopCamera: can't find UserAgent for mmtel-video.");
        } else {
            ua.stopCamera();
        }
    }

    public void setOrientation(int orientation) {
        this.mSveManager.setOrientation(orientation);
    }

    public void setZoom(float value) {
        this.mSveManager.setZoom(value);
    }

    public void setCamera(int cameraId) {
    }

    public void switchCamera() {
        this.mSveManager.switchCamera();
    }

    public void sendStillImage(int sessionId, boolean enable, String filePath, String frameSize) {
        this.mSveManager.sendStillImage(sessionId, enable, filePath, frameSize);
    }

    public void setCameraEffect(int value) {
        this.mSveManager.setCameraEffect(value);
    }

    public void startRecord(int phoneId, int sessionId, String filePath) {
        UserAgent ua = getUa(phoneId);
        if (ua == null) {
            Log.e(this.LOG_TAG, "startRecord: can't find UserAgent for mmtel-video.");
        } else {
            ua.startRecord(sessionId, filePath);
        }
    }

    public void stopRecord(int phoneId, int sessionId) {
        UserAgent ua = getUa(phoneId);
        if (ua == null) {
            Log.e(this.LOG_TAG, "stopRecord: can't find UserAgent for mmtel-video.");
        } else {
            ua.stopRecord(sessionId);
        }
    }

    public void startEmoji(int phoneId, int sessionId, String emojiInfo) {
        this.mSveManager.startEmoji(sessionId, emojiInfo);
    }

    public void stopEmoji(int phoneId, int sessionId) {
        this.mSveManager.stopEmoji(sessionId);
    }

    public void restartEmoji(int phoneId, int sessionId) {
        this.mSveManager.restartEmoji(sessionId);
    }

    public void bindToNetwork(Network network) {
        String str = this.LOG_TAG;
        Log.i(str, "bindToNetwork : " + network);
        this.mSveManager.bindToNetwork(network);
    }

    private UserAgent getUa(int phoneId) {
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        UserAgent ua = (UserAgent) rm.getUserAgent("mmtel-video", phoneId);
        if (ua == null) {
            return (UserAgent) rm.getUserAgent("vs", phoneId);
        }
        return ua;
    }

    private UserAgent getUaWithService(int phoneId, String service) {
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        if (phoneId != -1) {
            return (UserAgent) rm.getUserAgent(service, phoneId);
        }
        return (UserAgent) rm.getUserAgent(service);
    }

    private UserAgent getUaForMediaEvent(int phoneId, int target, int eventType) {
        if (eventType != 1) {
            return getUaWithService(phoneId, "mmtel");
        }
        if (target == 0 || target == 1) {
            return getUaWithService(phoneId, "vs");
        }
        return getUaWithService(phoneId, "mmtel-video");
    }

    private void onModifyVideo(AsyncResult result) {
        ModifyVideoData modifyData = (ModifyVideoData) result.result;
        int sessionId = (int) modifyData.session();
        int direction = (int) modifyData.direction();
        boolean isHeldCall = modifyData.isHeldCall();
        String str = this.LOG_TAG;
        Log.i(str, "onModifyVideo() session: " + sessionId + ", direction: " + direction + ", isHoldCall: " + isHeldCall);
        IMSMediaEvent event = new IMSMediaEvent();
        event.setSessionID(sessionId);
        if (direction == 0) {
            event.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_HELD);
        } else {
            event.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RESUMED);
        }
        event.setIsHeldCall(isHeldCall);
        this.mMediaEventRegistrants.notifyResult(event);
    }

    private void onVideoEvent(AsyncResult result) {
        NotifyVideoEventData videoEvent = (NotifyVideoEventData) result.result;
        int sessionId = (int) videoEvent.session();
        int event = (int) videoEvent.event();
        int arg1 = (int) videoEvent.arg1();
        int arg2 = (int) videoEvent.arg2();
        String str = this.LOG_TAG;
        Log.i(str, "onVideoEvent() session: " + sessionId + ", event: " + event);
        IMSMediaEvent me = new IMSMediaEvent();
        me.setPhoneId((int) videoEvent.phoneId());
        me.setSessionID(sessionId);
        if (event == 20) {
            me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RTP_TIMEOUT);
        } else if (event == 21) {
            me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RTCP_TIMEOUT);
        } else if (event == 40) {
            me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_ATTEMPTED);
        } else if (event == 60) {
            me.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS);
        } else if (event != 61) {
            switch (event) {
                case 1:
                case 3:
                case 12:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAPTURE_SUCCEEDED);
                    break;
                case 2:
                case 4:
                case 13:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAPTURE_FAILED);
                    break;
                case 5:
                    me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE);
                    break;
                case 6:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS);
                    break;
                case 7:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL);
                    break;
                case 8:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_FIRST_FRAME_READY);
                    break;
                case 9:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_STOP_SUCCESS);
                    break;
                case 10:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_SUCCESS);
                    break;
                case 11:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_FAIL);
                    break;
                case 14:
                    me.setState(IMSMediaEvent.MEDIA_STATE.NO_FAR_FRAME);
                    break;
                case 15:
                    me.setWidth(arg1);
                    me.setHeight(arg2);
                    me.setState(IMSMediaEvent.MEDIA_STATE.CHANGE_PEER_DIMENSION);
                    break;
                case 16:
                    me.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_DISABLED_ERROR);
                    break;
                default:
                    switch (event) {
                        case 30:
                            me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY);
                            break;
                        case 31:
                            me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY);
                            break;
                        case 32:
                            me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY);
                            break;
                        case 33:
                            me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY);
                            break;
                        case 34:
                            me.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_MAX_QUALITY);
                            break;
                        default:
                            switch (event) {
                                case 50:
                                    me.setState(IMSMediaEvent.MEDIA_STATE.RECORD_START_SUCCESS);
                                    break;
                                case 51:
                                    me.setState(IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE);
                                    break;
                                case 52:
                                    me.setState(IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE_NO_SPACE);
                                    break;
                                default:
                                    switch (event) {
                                        case 70:
                                            me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_SUCCESS);
                                            break;
                                        case 71:
                                            me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE);
                                            break;
                                        case 72:
                                            me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_SUCCESS);
                                            break;
                                        case 73:
                                            me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_FAILURE);
                                            break;
                                        case 74:
                                            me.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_INFO_CHANGE);
                                            break;
                                    }
                            }
                    }
            }
        } else {
            me.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE);
        }
        this.mMediaEventRegistrants.notifyResult(me);
    }

    private void onHoldVideoResponse(AsyncResult result) {
        if (((CallResponse) result.result).result() != 0) {
            IMSMediaEvent event = new IMSMediaEvent();
            event.setSessionID(((Integer) result.userObj).intValue());
            event.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_HOLD_FAILED);
            this.mMediaEventRegistrants.notifyResult(event);
        }
    }

    private void onResumeVideoResponse(AsyncResult result) {
        if (((CallResponse) result.result).result() != 0) {
            IMSMediaEvent event = new IMSMediaEvent();
            event.setSessionID(((Integer) result.userObj).intValue());
            event.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RESUME_FAILED);
            this.mMediaEventRegistrants.notifyResult(event);
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 107) {
            onModifyVideo((AsyncResult) msg.obj);
        } else if (i == 108) {
            onVideoEvent((AsyncResult) msg.obj);
        } else if (i == 201) {
            onHoldVideoResponse((AsyncResult) msg.obj);
        } else if (i == 202) {
            onResumeVideoResponse((AsyncResult) msg.obj);
        } else if (i == 300) {
            connectToSve();
        } else if (i == 301) {
            onSveConnected();
        }
    }

    private void connectToSve() {
        String str = this.LOG_TAG;
        Log.e(str, "SVE is not connected ? " + this.mSveConnected);
        if (!this.mSveConnected && !this.mSveConnecting) {
            Log.i(this.LOG_TAG, "connectToSve");
            this.mSveManager.connectService();
            this.mSveConnecting = true;
        }
    }

    private void onSveConnected() {
        if (this.mSveConnected) {
            registerMediaEventListener();
            this.mSveManager.sveSendGeneralEvent(0, 0, 0, "");
            this.mSveManager.saeTerminate();
            this.mHwSupportedVideoCodecList = this.mSveManager.sveGetCodecCapacity(256);
            String str = this.LOG_TAG;
            Log.i(str, "onSveConnected hwSupportedVideoCodecList : " + this.mHwSupportedVideoCodecList);
            return;
        }
        Log.e(this.LOG_TAG, "SVE was not connected!!!");
    }

    public void saeInitialize(int convertedMno, int dtmfMode, int sas) {
        String str = this.LOG_TAG;
        Log.i(str, "saeInitialize convertedMno = " + convertedMno + " " + dtmfMode + " " + sas);
        this.mSveManager.saeInitialize(convertedMno, dtmfMode, sas);
    }

    public int saeSetCodecInfo(int channel, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw) {
        short s = (short) chAwareRecv;
        short s2 = (short) cmr;
        int i = channel;
        return this.mSveManager.saeSetCodecInfo(i, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, s, s2, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw);
    }

    public int saeCreateChannel(int channel, int mno, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) {
        return this.mSveManager.saeCreateChannel(channel, mno, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
    }

    public int saeStartChannel(int channel, int direction, boolean enableIpv6) {
        return this.mSveManager.saeStartChannel(channel, direction, enableIpv6);
    }

    public int saeUpdateChannel(int channel, int dir, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort) {
        return this.mSveManager.saeUpdateChannel(channel, dir, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort);
    }

    public int saeStopChannel(int channel) {
        return this.mSveManager.saeStopChannel(channel);
    }

    public int saeModifyChannel(int channel, int direction) {
        return this.mSveManager.saeModifyChannel(channel, direction);
    }

    public int saeDeleteChannel(int channel) {
        return this.mSveManager.saeDeleteChannel(channel);
    }

    public int saeHandleDtmf(int channel, int code, int mode, int operation) {
        return this.mSveManager.saeHandleDtmf(channel, code, mode, operation);
    }

    public int saeSetDtmfCodecInfo(int channel, int type, int rxtype, int bitrate, int inband) {
        return this.mSveManager.saeSetDtmfCodecInfo(channel, type, rxtype, bitrate, inband);
    }

    public int saeEnableSRTP(int channel, int direction, int profile, byte[] key, int keylen) {
        return this.mSveManager.saeEnableSRTP(channel, direction, profile, key, keylen);
    }

    public int saeSetRtcpOnCall(int channel, int rr, int rs) {
        return this.mSveManager.saeSetRtcpOnCall(channel, rr, rs);
    }

    public int saeSetRtpTimeout(int channel, long sec) {
        return this.mSveManager.saeSetRtpTimeout(channel, sec);
    }

    public int saeSetRtcpTimeout(int channel, long sec) {
        return this.mSveManager.saeSetRtcpTimeout(channel, sec);
    }

    public int saeSetRtcpXr(int channel, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) {
        return this.mSveManager.saeSetRtcpXr(channel, flag, blocks, statflags, rttmode, maxsizesInt);
    }

    public Object saeGetLastPlayedVoiceTime(int channel) {
        return this.mSveManager.saeGetLastPlayedVoiceTime(channel);
    }

    public int saeSetVoicePlayDelay(int channel, int delayTime) {
        return this.mSveManager.saeSetVoicePlayDelay(channel, delayTime);
    }

    public int saeSetTOS(int channel, int tos) {
        return this.mSveManager.saeSetTOS(channel, tos);
    }

    public int saeGetVersion(byte[] version, int bufflen) {
        return this.mSveManager.saeGetVersion(version, bufflen);
    }

    public int saeGetAudioRxTrackId(int channel) {
        return this.mSveManager.saeGetAudioRxTrackId(channel);
    }

    public int saeSetAudioPath(int dir_in, int dir_out) {
        return this.mSveManager.saeSetAudioPath(dir_in, dir_out);
    }

    public int sveCreateChannel() {
        return this.mSveManager.sveCreateChannel();
    }

    public int sveStartChannel(int channel, int oldDirection, int newDirection) {
        return this.mSveManager.sveStartChannel(channel, oldDirection, newDirection);
    }

    public int sveStopChannel(int channel) {
        return this.mSveManager.sveStopChannel(channel);
    }

    public int sveSetConnection(int channel, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, int crbtType) {
        return this.mSveManager.sveSetConnection(channel, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, crbtType);
    }

    public int sveSetCodecInfo(int channel, int as, int rs, int rr, int recvCodecPT, int sendCodecPT, String name, int dir, int width, int height, int frameRate, int maxBitrate, boolean enableAVPF, int supportAVPFType, boolean enableOrientation, int CVOGranularity, int H264Profile, int H264Level, int H264ConstraintInfo, int H264PackMode, byte[] sps, byte[] pps, byte[] vps, int spsLen, int ppsLen, int vpsLen) {
        return this.mSveManager.sveSetCodecInfo(channel, as, rs, rr, recvCodecPT, sendCodecPT, name, dir, width, height, frameRate, maxBitrate, enableAVPF, supportAVPFType, enableOrientation, CVOGranularity, H264Profile, H264Level, H264ConstraintInfo, H264PackMode, sps, pps, vps, spsLen, ppsLen, vpsLen);
    }

    public int sveSetSRTPParams(int sessionId, String offerSuite, byte[] aucTagKeyLocal, int sendKeySize, int ucTagKeyLenLocal, int uiTimetoLiveLocal, int uiMKILocal, String answerSuite, byte[] aucTagKeyRemote, int recvKeySize, int ucTagKeyLenRemote, int uiTimetoLiveRemote, int uiMKIRemote) {
        return this.mSveManager.sveSetSRTPParams(sessionId, offerSuite, aucTagKeyLocal, sendKeySize, ucTagKeyLenLocal, uiTimetoLiveLocal, uiMKILocal, answerSuite, aucTagKeyRemote, recvKeySize, ucTagKeyLenRemote, uiTimetoLiveRemote, uiMKIRemote);
    }

    public int sveSetMediaConfig(int sessionId, boolean timeOutOnBoth, int rtpTimeout, boolean rtpKeepAlive, int rtcpTimeout, int mtuSize, int mno) {
        return this.mSveManager.sveSetMediaConfig(sessionId, timeOutOnBoth, rtpTimeout, rtpKeepAlive, rtcpTimeout, mtuSize, mno);
    }

    public int sveStartCamera(int sessionId, int cameraId) {
        return this.mSveManager.sveStartCamera(sessionId, cameraId);
    }

    public int sveStopCamera() {
        return this.mSveManager.sveStopCamera();
    }

    public int sveSetHeldInfo(int sessionId, boolean isLocal, boolean isHeld) {
        return this.mSveManager.sveSetHeldInfo(sessionId, isLocal, isHeld);
    }

    public Object sveGetLastPlayedVideoTime(int sessionId) {
        return this.mSveManager.sveGetLastPlayedVideoTime(sessionId);
    }

    public int sveSetVideoPlayDelay(int sessionId, int delayTime) {
        return this.mSveManager.sveSetVideoPlayDelay(sessionId, delayTime);
    }

    public int sveSetNetworkQoS(int sessionId, int ul_bler, int dl_bler, int grant) {
        return this.mSveManager.sveSetNetworkQoS(sessionId, ul_bler, dl_bler, grant);
    }

    public int sveSendGeneralEvent(int event, int arg1, int arg2, String arg3) {
        return this.mSveManager.sveSendGeneralEvent(event, arg1, arg2, arg3);
    }

    public Object sveGetRtcpTimeInfo(int sessionId) {
        return this.mSveManager.sveGetRtcpTimeInfo(sessionId);
    }

    public int sveStartRecording(int sessionId, int direction) {
        return this.mSveManager.sveStartRecording(sessionId, direction);
    }

    public int sveStopRecording(int sessionId) {
        return this.mSveManager.sveStopRecording(sessionId);
    }

    public int saeStartRecording(int channel, int direction, int samplingRate, boolean isApVoice) {
        return this.mSveManager.saeStartRecording(channel, direction, samplingRate, isApVoice);
    }

    public int saeStopRecording(int channel, boolean isApVoice) {
        return this.mSveManager.saeStopRecording(channel, isApVoice);
    }

    public int sveRecorderCreate(int sessionId, String filename, int audioId, int audioSampleRate, String audioCodec, int videoId) {
        return this.mSveManager.sveRecorderCreate(sessionId, filename, audioId, audioSampleRate, audioCodec, videoId);
    }

    public int sveCmcRecorderCreate(int sessionId, int audioId, int audioSampleRate, String audioCodec, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) {
        return this.mSveManager.sveCmcRecorderCreate(sessionId, audioId, audioSampleRate, audioCodec, audioSource, outputFormat, maxFileSize, maxDuration, outputPath, audioEncodingBR, audioChannels, audioSamplingR, audioEncoder, durationInterval, fileSizeInterval, author);
    }

    public int sveRecorderDelete(int sessionId) {
        return this.mSveManager.sveRecorderDelete(sessionId);
    }

    public int sveRecorderStart(int sessionId) {
        return this.mSveManager.sveRecorderStart(sessionId);
    }

    public int sveRecorderStop(int sessionId, boolean saveFile) {
        return this.mSveManager.sveRecorderStop(sessionId, saveFile);
    }

    public void initToneGenerator() {
        if (this.mToneGenerator == null) {
            Log.i(this.LOG_TAG, "init ToneGenerator");
            this.mToneGenerator = new ToneGenerator(8, 85);
        }
    }

    public void deinitToneGenerator() {
        if (this.mToneGenerator != null) {
            Log.i(this.LOG_TAG, "deinit ToneGenerator");
            this.mToneGenerator.release();
            this.mToneGenerator = null;
        }
    }

    public void triggerTone(boolean isStart, int tone, int duration) {
        if (this.mToneGenerator == null) {
            Log.e(this.LOG_TAG, "ToneGenerator was not initialized");
            return;
        }
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Tone #");
        sb.append(tone);
        sb.append(isStart ? " start" : "stop");
        Log.i(str, sb.toString());
        if (tone < 0 || tone > 15) {
            tone = 0;
        }
        if (isStart) {
            this.mToneGenerator.startTone(tone, duration);
        } else {
            this.mToneGenerator.stopTone();
        }
    }

    public void setAudioParameters(int phoneId, String audioParameters) {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService(TMOConstants.CallLogTypes.AUDIO);
        if (audioManager != null) {
            IVolteServiceModule vsm = this.mImsFramework.getServiceModuleManager().getVolteServiceModule();
            int i = 1;
            if (SimUtil.getPhoneCount() != 1) {
                if (phoneId != 0) {
                    i = 0;
                }
                if (vsm.hasCsCall(i)) {
                    Log.i(this.LOG_TAG, "skip to set to Audio F/W");
                    return;
                }
            }
            String str = this.LOG_TAG;
            Log.i(str, " set to Audio F/W" + audioParameters);
            audioManager.setParameters(audioParameters);
        }
    }

    private static class RingBackToneHandler extends Handler {
        public static final int MUTE_RINGBACK_TONE = 3;
        public static final int START_RINGBACK_TONE = 1;
        public static final int STOP_RINGBACK_TONE = 2;
        private static final String TAG = "RBTHandler";
        private int mStreamType;
        private ToneGenerator mToneGenerator = null;
        private int mToneType;
        private int mVolume;

        public RingBackToneHandler(Looper looper) {
            super(looper);
        }

        private void startRingBackTone() {
            Log.i(TAG, "Start RBT!");
            if (!(this.mToneGenerator != null || this.mStreamType == -1 || this.mVolume == -1)) {
                this.mToneGenerator = new ToneGenerator(this.mStreamType, this.mVolume);
            }
            ToneGenerator toneGenerator = this.mToneGenerator;
            if (toneGenerator != null) {
                toneGenerator.startTone(this.mToneType);
            }
        }

        private void stopRingBackTone() {
            Log.i(TAG, "Stop RBT!");
            ToneGenerator toneGenerator = this.mToneGenerator;
            if (toneGenerator != null) {
                toneGenerator.stopTone();
                this.mToneGenerator.release();
            }
            this.mToneGenerator = null;
            this.mStreamType = -1;
            this.mVolume = -1;
        }

        private void muteRingBackTone() {
            ToneGenerator toneGenerator = this.mToneGenerator;
            if (toneGenerator != null) {
                toneGenerator.semSetVolume(0.0f);
                this.mVolume = 0;
            }
        }

        public void setRingBackToneData(int streamType, int volume, int toneType) {
            this.mStreamType = streamType;
            this.mVolume = volume;
            this.mToneType = toneType;
            this.mToneGenerator = new ToneGenerator(streamType, volume);
        }

        public boolean isPlayingRingBackTone() {
            return this.mToneGenerator != null && this.mVolume > 0;
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                Log.e(TAG, "Invalid Message");
                return;
            }
            Log.i(TAG, "Event " + msg.what);
            int i = msg.what;
            if (i == 1) {
                startRingBackTone();
            } else if (i == 2) {
                stopRingBackTone();
            } else if (i != 3) {
                Log.e(TAG, "Invalid event");
            } else {
                muteRingBackTone();
            }
        }
    }

    public int startLocalRingBackTone(int streamType, int volume, int toneType) {
        String str = this.LOG_TAG;
        Log.i(str, "start RBT with st" + streamType + " v-" + volume + " tt-" + toneType);
        this.mRingBackToneHandler.setRingBackToneData(streamType, volume, toneType);
        this.mRingBackToneHandler.sendEmptyMessage(1);
        return 0;
    }

    public int stopLocalRingBackTone() {
        this.mRingBackToneHandler.sendEmptyMessage(2);
        return 0;
    }

    public boolean muteLocalRingBackTone() {
        if (!this.mRingBackToneHandler.isPlayingRingBackTone() && !this.mRingBackToneHandler.hasMessages(1)) {
            return false;
        }
        this.mRingBackToneHandler.sendEmptyMessage(3);
        return true;
    }

    public void registerMediaEventListener() {
        this.mSveManager.registerForMediaEventListener(this.mMediaEventlistener);
    }

    public void unregisterMediaEventListener() {
        this.mSveManager.unregisterForMediaEventListener(this.mMediaEventlistener);
    }

    public void sendMediaEvent(int phoneId, int target, int event, int eventType) {
        UserAgent ua = getUaForMediaEvent(phoneId, target, eventType);
        if (ua == null) {
            Log.e(this.LOG_TAG, "User Agent was empty!");
        } else {
            ua.sendMediaEvent(target, event, eventType);
        }
    }

    public void steInitialize() {
        this.mSveManager.steInitialize();
    }

    public int steSetCodecInfo(int channel, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw) {
        short s = (short) chAwareRecv;
        short s2 = (short) cmr;
        int i = channel;
        return this.mSveManager.steSetCodecInfo(i, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, s, s2, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw);
    }

    public int steCreateChannel(int mno, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) {
        return this.mSveManager.steCreateChannel(mno, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
    }

    public int steStartChannel(int channel, int direction, boolean enableIpv6) {
        return this.mSveManager.steStartChannel(channel, direction, enableIpv6);
    }

    public int steStopChannel(int channel) {
        return this.mSveManager.steStopChannel(channel);
    }

    public int steDeleteChannel(int channel) {
        return this.mSveManager.steDeleteChannel(channel);
    }

    public int steUpdateChannel(int channel, int dir, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort) {
        return this.mSveManager.steUpdateChannel(channel, dir, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort);
    }

    public int steModifyChannel(int channel, int direction) {
        return this.mSveManager.steModifyChannel(channel, direction);
    }

    public int steSetNetId(int channel, int netId) {
        return this.mSveManager.steSetNetId(channel, netId);
    }

    public int steSetSessionId(int channel, int sessionId) {
        return this.mSveManager.steSetSessionId(channel, sessionId);
    }

    public int steSendText(int channel, String text, int len) {
        return this.mSveManager.steSendText(channel, text, len);
    }

    public int steSetCallOptions(int channel, boolean isRtcpOnCall) {
        return this.mSveManager.steSetCallOptions(channel, isRtcpOnCall);
    }

    public int steSetRtcpTimeout(int channel, long sec) {
        return this.mSveManager.steSetRtcpTimeout(channel, sec);
    }

    public int steEnableSRTP(int channel, int direction, int profile, byte[] key, int keylen) {
        return this.mSveManager.steEnableSRTP(channel, direction, profile, key, keylen);
    }

    public int steSetRtcpOnCall(int channel, int rr, int rs) {
        return this.mSveManager.steSetRtcpOnCall(channel, rr, rs);
    }

    public String getHwSupportedVideoCodecs(String profileCodecs) {
        String str = this.mHwSupportedVideoCodecList;
        if (str == null || str.isEmpty()) {
            Log.i(this.LOG_TAG, "getHwSupportedVideoCodecs - fails to get HW supported codec");
            return profileCodecs;
        }
        String[] profileList = profileCodecs.split(",");
        String[] supportedCodecList = this.mHwSupportedVideoCodecList.split(",");
        String filteredCodecs = "";
        for (String profileCodec : profileList) {
            for (String supportedCodec : supportedCodecList) {
                if (supportedCodec.equals(profileCodec)) {
                    if (TextUtils.isEmpty(filteredCodecs)) {
                        filteredCodecs = profileCodec;
                    } else {
                        filteredCodecs = filteredCodecs + "," + profileCodec;
                    }
                }
            }
        }
        Log.i(this.LOG_TAG, "getHwSupportedVideoCodecs filteredCodecs : " + filteredCodecs);
        return filteredCodecs;
    }

    public boolean isSupportingCameraMotor() {
        boolean cameraMotor = this.mSveManager.isSupportingCameraMotor();
        String str = this.LOG_TAG;
        Log.i(str, "isSupportingCameraMotor : " + cameraMotor);
        return cameraMotor;
    }
}
