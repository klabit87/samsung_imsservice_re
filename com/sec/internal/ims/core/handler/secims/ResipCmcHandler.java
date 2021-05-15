package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.core.handler.CmcHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.NotifyCmcRecordEventData;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.sve.ICmcMediaEventListener;
import com.sec.sve.SecVideoEngineManager;

public class ResipCmcHandler extends CmcHandler {
    private static final int EVENT_CMC_RECORD_EVENT = 303;
    private static final int EVENT_CONNECT_TO_SVE = 300;
    private static final int EVENT_DISCONNECT_TO_SVE = 302;
    private static final int EVENT_SVE_CONNECTED = 301;
    private static final int NOTIFY_RECORD_START_FAILURE = 51;
    private static final int NOTIFY_RECORD_START_FAILURE_NO_MEMORY = 52;
    private static final int NOTIFY_RECORD_START_SUCCESS = 50;
    private static final int NOTIFY_RECORD_STOP_FAILURE = 61;
    private static final int NOTIFY_RECORD_STOP_NO_MEMORY = 62;
    private static final int NOTIFY_RECORD_STOP_SUCCESS = 60;
    public static final int NOTIFY_RELAY_MEDIA_PAUSE = 4;
    public static final int NOTIFY_RELAY_MEDIA_RESUME = 3;
    public static final int NOTIFY_RELAY_MEDIA_START = 1;
    public static final int NOTIFY_RELAY_MEDIA_STOP = 2;
    private ICmcMediaEventListener mCmcMediaEventlistener = new ICmcMediaEventListener.Stub() {
        public void onRelayEvent(int streamId, int event) {
            String access$000 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$000, "onRelayEvent streamId : " + streamId + " event : " + event);
            if (event <= 0) {
                Log.e(ResipCmcHandler.this.LOG_TAG, "Invalid Relay Event");
            } else if (event == 1 || event == 2 || event == 3 || event == 4) {
                ResipCmcHandler.this.mStackIf.sendRelayEvent(streamId, event);
            }
        }

        public void onRelayStreamEvent(int streamId, int event, int sessionId) {
            String access$300 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$300, "onRelayStreamEvent streamId : " + streamId + " event : " + event + " Session Id : " + sessionId);
            IMSMediaEvent me = new IMSMediaEvent();
            me.setRelayStreamEvent(event);
            me.setStreamId(streamId);
            me.setSessionID(sessionId);
            ResipCmcHandler.this.mCmcMediaEventRegistrants.notifyResult(me);
        }

        public void onRelayRtpStats(int streamId, int sessionId, int lossData, int delay, int jitter, int measuredPeriod, int direction) {
            IMSMediaEvent me = new IMSMediaEvent();
            me.setRelayStreamEvent(5);
            int i = streamId;
            me.setStreamId(streamId);
            int i2 = sessionId;
            me.setSessionID(sessionId);
            me.setRelayRtpStats(new IMSMediaEvent.AudioRtpStats(streamId, lossData, delay, jitter, measuredPeriod, direction));
            ResipCmcHandler.this.mCmcMediaEventRegistrants.notifyResult(me);
        }

        public void onRelayChannelEvent(int channelId, int event) {
            String access$600 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$600, "onRelayChannelEvent channelId : " + channelId + " event : " + event);
            IMSMediaEvent me = new IMSMediaEvent();
            me.setRelayChannelEvent(event);
            me.setRelayChannelId(channelId);
            ResipCmcHandler.this.mCmcMediaEventRegistrants.notifyResult(me);
        }

        public void onCmcRecordEvent(int sessionId, int event, int arg) {
            String access$800 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$800, "onCmcRecordEvent sessionId : " + sessionId + " event : " + event + " arg : " + arg);
            IMSMediaEvent me = new IMSMediaEvent();
            me.setSessionID(sessionId);
            me.setCmcRecordingEvent(event);
            me.setCmcRecordingArg(arg);
            ResipCmcHandler.this.mCmcMediaEventRegistrants.notifyResult(me);
        }

        public void onCmcRecorderStoppedEvent(int startTime, int stopTime, String filePath) {
            String access$1000 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$1000, "onCmcRecorderStoppedEvent startTime : " + startTime + " , stopTime : " + stopTime);
        }
    };
    private int mCmcRecordingCh = -1;
    private int mCmcRegiPhoneId = -1;
    private Context mContext;
    private IImsFramework mImsFramework;
    /* access modifiers changed from: private */
    public StackIF mStackIf;
    /* access modifiers changed from: private */
    public boolean mSveConnected = false;
    /* access modifiers changed from: private */
    public boolean mSveConnecting = false;
    private SecVideoEngineManager mSveManager;

    public ResipCmcHandler(Looper looper, Context context, IImsFramework imsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = imsFramework;
        this.mSveManager = new SecVideoEngineManager(context, new SecVideoEngineManager.ConnectionListener() {
            public void onDisconnected() {
                Log.i(ResipCmcHandler.this.LOG_TAG, "sve disconnected");
                boolean unused = ResipCmcHandler.this.mSveConnected = false;
                boolean unused2 = ResipCmcHandler.this.mSveConnecting = false;
                if (ResipCmcHandler.this.needToReconnect()) {
                    ResipCmcHandler.this.sendEmptyMessageDelayed(300, 1000);
                }
            }

            public void onConnected() {
                Log.i(ResipCmcHandler.this.LOG_TAG, "sve connected.");
                boolean unused = ResipCmcHandler.this.mSveConnected = true;
                boolean unused2 = ResipCmcHandler.this.mSveConnecting = false;
                ResipCmcHandler.this.sendEmptyMessage(301);
            }
        });
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerCmcRecordEvent(this, 303, (Object) null);
        this.mSveConnected = false;
        this.mSveConnecting = false;
        this.mCmcRegiPhoneId = -1;
    }

    public void sendConnectToSve(int phoneId) {
        this.mCmcRegiPhoneId = phoneId;
        sendEmptyMessage(300);
    }

    public void sendDisonnectToSve() {
        if (!needToReconnect()) {
            sendEmptyMessage(302);
        }
    }

    /* access modifiers changed from: private */
    public boolean needToReconnect() {
        int cmcType = 5;
        if (this.mImsFramework.getP2pCC().isEnabledWifiDirectFeature()) {
            cmcType = 7;
        }
        for (int type = 1; type <= cmcType; type += 2) {
            UserAgent ua = getUaByCmcType(this.mCmcRegiPhoneId, type);
            if (ua != null && ua.getImsRegistration() != null) {
                return true;
            }
        }
        return false;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 300:
                connectToSve();
                return;
            case 301:
                onSveConnected();
                return;
            case 302:
                disconnectToSve();
                return;
            case 303:
                onCmcRecordEvent((AsyncResult) msg.obj);
                return;
            default:
                return;
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

    private void disconnectToSve() {
        String str = this.LOG_TAG;
        Log.i(str, "SVE is connected ? " + this.mSveConnected);
        if (this.mSveConnected || this.mSveConnecting) {
            Log.i(this.LOG_TAG, "disconnectToSve");
            this.mSveManager.disconnectService();
            this.mSveConnecting = false;
            this.mSveConnected = false;
        }
    }

    private void onSveConnected() {
        if (this.mSveConnected) {
            registerCmcMediaEventListener();
            Log.i(this.LOG_TAG, "onSveConnected");
            return;
        }
        Log.e(this.LOG_TAG, "SVE was not connected!!!");
    }

    private void onCmcRecordEvent(AsyncResult result) {
        NotifyCmcRecordEventData recordEvent = (NotifyCmcRecordEventData) result.result;
        int sessionId = (int) recordEvent.session();
        int event = (int) recordEvent.event();
        String str = this.LOG_TAG;
        Log.i(str, "onCmcRecordEvent() session: " + sessionId + ", event: " + event);
        IMSMediaEvent me = new IMSMediaEvent();
        me.setPhoneId((int) recordEvent.phoneId());
        me.setSessionID(sessionId);
        if (event == 50 || event == 60) {
            me.setCmcRecordingEvent(0);
        } else {
            me.setCmcRecordingEvent(event);
        }
        this.mCmcMediaEventRegistrants.notifyResult(me);
    }

    public void registerCmcMediaEventListener() {
        this.mSveManager.registerForCmcEventListener(this.mCmcMediaEventlistener);
    }

    public void unregisterCmcMediaEventListener() {
        this.mSveManager.unregisterForCmcEventListener(this.mCmcMediaEventlistener);
    }

    public boolean startCmcRecord(int phoneId, int sessionId, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) {
        UserAgent ua = getUaByCmcType(phoneId, 1);
        if (ua == null) {
            Log.e(this.LOG_TAG, "startCmcRecord: can't find UserAgent for cmc.");
            return false;
        }
        ua.startCmcRecord(sessionId, audioSource, outputFormat, maxFileSize, maxDuration, outputPath, audioEncodingBR, audioChannels, audioSamplingR, audioEncoder, durationInterval, fileSizeInterval, author);
        return true;
    }

    public boolean stopCmcRecord(int phoneId, int sessionId) {
        UserAgent ua = getUaByCmcType(phoneId, 1);
        if (ua == null) {
            Log.e(this.LOG_TAG, "stopCmcRecord: can't find UserAgent for cmc.");
            return false;
        }
        ua.stopRecord(sessionId);
        return true;
    }

    private UserAgent getUaByCmcType(int phoneId, int cmcType) {
        IUserAgent[] uaList = this.mImsFramework.getRegistrationManager().getUserAgentByPhoneId(phoneId, "mmtel");
        if (uaList.length == 0) {
            return null;
        }
        for (IUserAgent ua : uaList) {
            if (ua != null && ua.getImsProfile().getCmcType() == cmcType) {
                return (UserAgent) ua;
            }
        }
        return null;
    }

    public void sendMediaEvent(int phoneId, int sessionId, int event, int handle) {
        UserAgent ua = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(handle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "User Agent was empty!");
        } else {
            ua.sendMediaEvent(sessionId, event, 3);
        }
    }

    public void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats rtpStats) {
        this.mStackIf.sendRtpStatsToStack(rtpStats);
    }

    public void sreInitialize() {
        this.mSveManager.sreInitialize();
    }

    public String sreGetVersion() {
        return this.mSveManager.sreGetVersion();
    }

    public int sreSetMdmn(int sessionId, boolean isMdmn) {
        return this.mSveManager.sreSetMdmn(sessionId, isMdmn);
    }

    public boolean sreGetMdmn(int sessionId) {
        return this.mSveManager.sreGetMdmn(sessionId);
    }

    public int sreSetNetId(int sessionId, long netId) {
        return this.mSveManager.sreSetNetId(sessionId, netId);
    }

    public int sreCreateStream(int phoneId, int sessionId, int mno, String localIp, int localPort, String remoteIp, int remotePort, boolean isIpv6, boolean isMdmn, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) {
        return this.mSveManager.sreCreateStream(phoneId, sessionId, mno, localIp, localPort, remoteIp, remotePort, isIpv6, isMdmn, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
    }

    public int sreStartStream(int sessionId, int oldDirection, int newDirection) {
        return this.mSveManager.sreStartStream(sessionId, oldDirection, newDirection);
    }

    public int sreDeleteStream(int sessionId) {
        return this.mSveManager.sreDeleteStream(sessionId);
    }

    public int sreUpdateStream(int sessionId) {
        return this.mSveManager.sreUpdateStream(sessionId);
    }

    public int sreCreateRelayChannel(int lhs_stream, int rhs_stream) {
        return this.mSveManager.sreCreateRelayChannel(lhs_stream, rhs_stream);
    }

    public int sreDeleteRelayChannel(int channel) {
        return this.mSveManager.sreDeleteRelayChannel(channel);
    }

    public int sreStartRelayChannel(int channel, int direction) {
        return this.mSveManager.sreStartRelayChannel(channel, direction);
    }

    public int sreStopRelayChannel(int channel) {
        return this.mSveManager.sreStopRelayChannel(channel);
    }

    public int sreHoldRelaySession(int sessionId) {
        IMSMediaEvent me = new IMSMediaEvent();
        me.setRelayStreamEvent(10);
        me.setSessionID(sessionId);
        this.mCmcMediaEventRegistrants.notifyResult(me);
        return 0;
    }

    public int sreResumeRelaySession(int sessionId) {
        IMSMediaEvent me = new IMSMediaEvent();
        me.setRelayStreamEvent(11);
        me.setSessionID(sessionId);
        this.mCmcMediaEventRegistrants.notifyResult(me);
        return 0;
    }

    public int sreHoldRelayChannel(int channel) {
        return this.mSveManager.sreHoldRelayChannel(channel);
    }

    public int sreResumeRelayChannel(int channel) {
        return this.mSveManager.sreResumeRelayChannel(channel);
    }

    public int sreUpdateRelayChannel(int channel, int stream) {
        return this.mSveManager.sreUpdateRelayChannel(channel, stream);
    }

    public int sreSetConnection(int sessionId, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, int crbtType) {
        return this.mSveManager.sreSetConnection(sessionId, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, crbtType);
    }

    public int sreEnableSRTP(int sessionId, int direction, int profile, byte[] key, int keylen) {
        return this.mSveManager.sreEnableSRTP(sessionId, direction, profile, key, keylen);
    }

    public int sreSetRtcpOnCall(int sessionId, int rr, int rs, int rtpTimer, int rtcpTimer) {
        return this.mSveManager.sreSetRtcpOnCall(sessionId, rr, rs, rtpTimer, rtcpTimer);
    }

    public int sreSetRtpTimeout(int sessionId, int sec) {
        return this.mSveManager.sreSetRtpTimeout(sessionId, sec);
    }

    public int sreSetRtcpTimeout(int sessionId, int sec) {
        return this.mSveManager.sreSetRtcpTimeout(sessionId, sec);
    }

    public int sreSetRtcpXr(int sessionId, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) {
        return this.mSveManager.sreSetRtcpXr(sessionId, flag, blocks, statflags, rttmode, maxsizesInt);
    }

    public int sreSetCodecInfo(int sessionId, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw, int protocol) {
        short s = (short) chAwareRecv;
        short s2 = (short) cmr;
        int i = sessionId;
        return this.mSveManager.sreSetCodecInfo(i, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, s, s2, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw, protocol);
    }

    public int sreSetDtmfCodecInfo(int phoneId, int sessionId, int type, int rxtype, int bitrate, int inband) {
        return this.mSveManager.sreSetDtmfCodecInfo(phoneId, sessionId, type, rxtype, bitrate, inband);
    }

    public int sreStartRecording(int sessionId) {
        IMSMediaEvent me = new IMSMediaEvent();
        me.setRelayStreamEvent(12);
        me.setSessionID(sessionId);
        this.mCmcMediaEventRegistrants.notifyResult(me);
        return 0;
    }

    public int sreStartRecordingChannel(int sessionId, int streamId, int channel) {
        this.mCmcRecordingCh = channel;
        return this.mSveManager.sreStartRecording(sessionId, streamId, channel);
    }

    public int sreStopRecording(int sessionId) {
        return this.mSveManager.sreStopRecording(sessionId, this.mCmcRecordingCh);
    }
}
