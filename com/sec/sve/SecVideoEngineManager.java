package com.sec.sve;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Network;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.Surface;
import com.sec.sve.ISecVideoEngineService;

public class SecVideoEngineManager {
    /* access modifiers changed from: private */
    public final String LOG_TAG = SecVideoEngineManager.class.getSimpleName();
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(SecVideoEngineManager.this.LOG_TAG, "Connected");
            ISecVideoEngineService unused = SecVideoEngineManager.this.mService = ISecVideoEngineService.Stub.asInterface(service);
            SecVideoEngineManager.this.mListener.onConnected();
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(SecVideoEngineManager.this.LOG_TAG, "Disconnected");
            ISecVideoEngineService unused = SecVideoEngineManager.this.mService = null;
            SecVideoEngineManager.this.mListener.onDisconnected();
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public final ConnectionListener mListener;
    /* access modifiers changed from: private */
    public ISecVideoEngineService mService;

    public interface ConnectionListener {
        void onConnected();

        void onDisconnected();
    }

    public SecVideoEngineManager(Context context, ConnectionListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public void connectService() {
        try {
            if (this.mService == null) {
                Intent serviceIntent = new Intent();
                serviceIntent.setClassName("com.sec.sve", "com.sec.sve.service.SecVideoEngineService");
                this.mContext.bindServiceAsUser(serviceIntent, this.mConnection, 1, UserHandle.CURRENT);
            }
        } catch (SecurityException e) {
        }
    }

    public void disconnectService() {
        Context context = this.mContext;
        if (context != null) {
            context.unbindService(this.mConnection);
            this.mService = null;
        }
    }

    public void bindToNetwork(Network network) {
        if (this.mService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return;
        }
        String str = this.LOG_TAG;
        Log.d(str, "bindToNetwork " + network);
        try {
            this.mService.bindToNetwork(network);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setPreviewSurface(Surface surface, int color) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setPreviewSurface(surface, color);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDisplaySurface(Surface surface, int color) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setDisplaySurface(surface, color);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOrientation(int orientation) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setOrientation(orientation);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setZoom(float value) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setZoom(value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchCamera() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.switchCamera();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreviewResolution(int width, int height) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setPreviewResolution(width, height);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendStillImage(int sessionId, boolean enable, String filePath, String frameSize) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sendStillImage(sessionId, enable, filePath, frameSize);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCameraEffect(int value) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setCameraEffect(value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void saeInitialize(int convertedMno, int dtmfMode, int sas) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.saeInitialize(convertedMno, dtmfMode, sas);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void saeTerminate() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.saeTerminate();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int saeSetCodecInfo(int channel, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetCodecInfo(channel, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, chAwareRecv, cmr, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeCreateChannel(int channel, int mno, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeCreateChannel(channel, mno, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeStartChannel(int channel, int direction, boolean enableIpv6) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeStartChannel(channel, direction, enableIpv6);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeUpdateChannel(int channel, int dir, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeUpdateChannel(channel, dir, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeStopChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeStopChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeModifyChannel(int channel, int direction) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeModifyChannel(channel, direction);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeDeleteChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeDeleteChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeHandleDtmf(int channel, int code, int mode, int operation) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeHandleDtmf(channel, code, mode, operation);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetDtmfCodecInfo(int channel, int type, int rxtype, int bitrate, int inband) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetDtmfCodecInfo(channel, type, rxtype, bitrate, inband);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeEnableSRTP(int channel, int direction, int profile, byte[] key, int keylen) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeEnableSRTP(channel, direction, profile, key, keylen);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetRtcpOnCall(int channel, int rr, int rs) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetRtcpOnCall(channel, rr, rs);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetRtpTimeout(int channel, long sec) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetRtpTimeout(channel, sec);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetRtcpTimeout(int channel, long sec) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetRtcpTimeout(channel, sec);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetRtcpXr(int channel, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetRtcpXr(channel, flag, blocks, statflags, rttmode, maxsizesInt);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public TimeInfo saeGetLastPlayedVoiceTime(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return null;
        }
        try {
            return iSecVideoEngineService.saeGetLastPlayedVoiceTime(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int saeSetVoicePlayDelay(int channel, int delayTime) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetVoicePlayDelay(channel, delayTime);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetTOS(int channel, int tos) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetTOS(channel, tos);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeGetVersion(byte[] version, int bufflen) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeGetVersion(version, bufflen);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeGetAudioRxTrackId(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeGetAudioRxTrackId(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetAudioPath(int dir_in, int dir_out) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetAudioPath(dir_in, dir_out);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveCreateChannel() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveCreateChannel();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStartChannel(int channel, int oldDirection, int newDirection) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStartChannel(channel, oldDirection, newDirection);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStopChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStopChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetConnection(int channel, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, int crbtType) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetConnection(channel, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, crbtType);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetCodecInfo(int channel, int as, int rs, int rr, int recvCodecPT, int sendCodecPT, String name, int dir, int width, int height, int frameRate, int maxBitrate, boolean enableAVPF, int supportAVPFType, boolean enableOrientation, int CVOGranularity, int H264Profile, int H264Level, int H264ConstraintInfo, int H264PackMode, byte[] sps, byte[] pps, byte[] vps, int spsLen, int ppsLen, int vpsLen) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetCodecInfo(channel, as, rs, rr, recvCodecPT, sendCodecPT, name, dir, width, height, frameRate, maxBitrate, enableAVPF, supportAVPFType, enableOrientation, CVOGranularity, H264Profile, H264Level, H264ConstraintInfo, H264PackMode, sps, pps, vps, spsLen, ppsLen, vpsLen);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetSRTPParams(int sessionId, String offerSuite, byte[] aucTagKeyLocal, int sendKeySize, int ucTagKeyLenLocal, int uiTimetoLiveLocal, int uiMKILocal, String answerSuite, byte[] aucTagKeyRemote, int recvKeySize, int ucTagKeyLenRemote, int uiTimetoLiveRemote, int uiMKIRemote) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetSRTPParams(sessionId, offerSuite, aucTagKeyLocal, sendKeySize, ucTagKeyLenLocal, uiTimetoLiveLocal, uiMKILocal, answerSuite, aucTagKeyRemote, recvKeySize, ucTagKeyLenRemote, uiTimetoLiveRemote, uiMKIRemote);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetSRTPParams(int sessionId, int srtpProfile, int keyId, int keytype, char csId, int csbIdValue, byte[] inkey, int inkeyLength, byte[] rand, int randLengthValue) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetGcmSrtpParams(sessionId, srtpProfile, keyId, keytype, csId, csbIdValue, inkey, inkeyLength, rand, randLengthValue);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetMediaConfig(int sessionId, boolean timeOutOnBoth, int rtpTimeout, boolean rtpKeepAlive, int rtcpTimeout, int mtuSize, int mno) {
        return sveSetMediaConfig(sessionId, timeOutOnBoth, rtpTimeout, rtpKeepAlive, rtcpTimeout, mtuSize, mno, 2000);
    }

    public int sveSetMediaConfig(int sessionId, boolean timeOutOnBoth, int rtpTimeout, boolean rtpKeepAlive, int rtcpTimeout, int mtuSize, int mno, int keepAliveInterval) {
        if (this.mService == null) {
            return -1;
        }
        String str = this.LOG_TAG;
        Log.d(str, "sveSetMediaConfig keepAliveInterval " + keepAliveInterval);
        try {
            return this.mService.sveSetMediaConfig(sessionId, timeOutOnBoth, rtpTimeout, rtpKeepAlive, rtcpTimeout, mtuSize, mno, keepAliveInterval);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStartCamera(int sessionId, int cameraId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStartCamera(sessionId, cameraId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStopCamera() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStopCamera();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetHeldInfo(int sessionId, boolean isLocal, boolean isHeld) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetHeldInfo(sessionId, isLocal, isHeld);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public TimeInfo sveGetLastPlayedVideoTime(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return null;
        }
        try {
            return iSecVideoEngineService.sveGetLastPlayedVideoTime(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String sveGetCodecCapacity(int codecMaxLen) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return null;
        }
        try {
            return iSecVideoEngineService.sveGetCodecCapacity(codecMaxLen);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int sveSetVideoPlayDelay(int sessionId, int delayTime) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetVideoPlayDelay(sessionId, delayTime);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetNetworkQoS(int sessionId, int ul_bler, int dl_bler, int grant) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetNetworkQoS(sessionId, ul_bler, dl_bler, grant);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSendGeneralEvent(int event, int arg1, int arg2, String arg3) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSendGeneralEvent(event, arg1, arg2, arg3);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public TimeInfo sveGetRtcpTimeInfo(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return null;
        }
        try {
            return iSecVideoEngineService.sveGetRtcpTimeInfo(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void registerForMediaEventListener(IImsMediaEventListener listener) {
        Log.d(this.LOG_TAG, "registerForMediaEventListener");
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.registerForMediaEventListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterForMediaEventListener(IImsMediaEventListener listener) {
        Log.d(this.LOG_TAG, "unregisterForMediaEventListener");
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.unregisterForMediaEventListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerForCmcEventListener(ICmcMediaEventListener listener) {
        Log.d(this.LOG_TAG, "registerForCmcEventListener");
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.registerForCmcEventListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterForCmcEventListener(ICmcMediaEventListener listener) {
        Log.d(this.LOG_TAG, "unregisterForCmcEventListener");
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.unregisterForCmcEventListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void steInitialize() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.steInitialize();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int steSetCodecInfo(int channel, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetCodecInfo(channel, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, chAwareRecv, cmr, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steCreateChannel(int mno, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steCreateChannel(mno, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steStartChannel(int channel, int direction, boolean enableIpv6) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steStartChannel(channel, direction, enableIpv6);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steUpdateChannel(int channel, int dir, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steUpdateChannel(channel, dir, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steStopChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steStopChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steModifyChannel(int channel, int direction) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steModifyChannel(channel, direction);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetRtcpXr(int channel, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetRtcpXr(channel, flag, blocks, statflags, rttmode, maxsizesInt);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetRtpTimeout(int channel, long sec) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetRtpTimeout(channel, sec);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetCallOptions(int channel, boolean isRtcpOnCall) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetCallOptions(channel, isRtcpOnCall);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetNetId(int channel, int netId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetNetId(channel, netId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steDeleteChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steDeleteChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSendText(int channel, String text, int len) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSendText(channel, text, len);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steEnableSRTP(int channel, int direction, int profile, byte[] key, int keylen) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steEnableSRTP(channel, direction, profile, key, keylen);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetRtcpOnCall(int channel, int rr, int rs) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetRtcpOnCall(channel, rr, rs);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetRtcpTimeout(int channel, long sec) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetRtcpTimeout(channel, sec);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetSessionId(int channel, int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetSessionId(channel, sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void sreInitialize() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sreInitialize();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public String sreGetVersion() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return "";
        }
        try {
            return iSecVideoEngineService.sreGetVersion();
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public int sreSetMdmn(int sessionId, boolean isMdmn) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetMdmn(sessionId, isMdmn);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean sreGetMdmn(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return false;
        }
        try {
            return iSecVideoEngineService.sreGetMdmn(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int sreSetNetId(int sessionId, long netId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetNetId(sessionId, netId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreCreateStream(int phoneId, int sessionId, int mno, String localIp, int localPort, String remoteIp, int remotePort, boolean isIpv6, boolean isMdmn, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreCreateStream(phoneId, sessionId, mno, localIp, localPort, remoteIp, remotePort, isIpv6, isMdmn, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStartStream(int sessionId, int oldDirection, int newDirection) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStartStream(sessionId, oldDirection, newDirection);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreDeleteStream(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreDeleteStream(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreUpdateStream(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreUpdateStream(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreCreateRelayChannel(int lhs_stream, int rhs_stream) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreCreateRelayChannel(lhs_stream, rhs_stream);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreDeleteRelayChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreDeleteRelayChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStartRelayChannel(int channel, int direction) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStartRelayChannel(channel, direction);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStopRelayChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStopRelayChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreHoldRelayChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreHoldRelayChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreResumeRelayChannel(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreResumeRelayChannel(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreUpdateRelayChannel(int channel, int stream) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreUpdateRelayChannel(channel, stream);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetConnection(int sessionId, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, int crbtType) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetConnection(sessionId, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, crbtType);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreEnableSRTP(int sessionId, int direction, int profile, byte[] key, int keylen) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreEnableSRTP(sessionId, direction, profile, key, keylen);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetRtcpOnCall(int sessionId, int rr, int rs, int rtpTimer, int rtcpTimer) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetRtcpOnCall(sessionId, rr, rs, rtpTimer, rtcpTimer);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetRtpTimeout(int sessionId, int sec) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetRtpTimeout(sessionId, sec);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetRtcpTimeout(int sessionId, int sec) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetRtcpTimeout(sessionId, sec);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetRtcpXr(int sessionId, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetRtcpXr(sessionId, flag, blocks, statflags, rttmode, maxsizesInt);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetCodecInfo(int sessionId, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw, int protocol) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetCodecInfo(sessionId, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, chAwareRecv, cmr, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw, protocol);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetDtmfCodecInfo(int phoneId, int sessionId, int type, int rxtype, int bitrate, int inband) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetDtmfCodecInfo(phoneId, sessionId, type, rxtype, bitrate, inband);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStartRecording(int sessionId, int streamId, int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStartRecording(sessionId, streamId, channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStopRecording(int sessionId, int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStopRecording(sessionId, channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isSupportingCameraMotor() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return false;
        }
        try {
            return iSecVideoEngineService.isSupportingCameraMotor();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int sveRecorderCreate(int sessionId, String filename, int audioId, int audioSampleRate, String audioCodec, int videoId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveRecorderCreate(sessionId, filename, audioId, audioSampleRate, audioCodec, videoId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveCmcRecorderCreate(int sessionId, int audioId, int audioSampleRate, String audioCodec, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveCmcRecorderCreate(sessionId, audioId, audioSampleRate, audioCodec, audioSource, outputFormat, maxFileSize, maxDuration, outputPath, audioEncodingBR, audioChannels, audioSamplingR, audioEncoder, durationInterval, fileSizeInterval, author);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveRecorderDelete(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveRecorderDelete(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveRecorderStart(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveRecorderStart(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveRecorderStop(int sessionId, boolean saveFile) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveRecorderStop(sessionId, saveFile);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeStartRecording(int channel, int direction, int samplingRate, boolean bIsApVoice) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeStartRecording(channel, direction, samplingRate, bIsApVoice);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeStopRecording(int channel, boolean bIsApVoice) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeStopRecording(channel, bIsApVoice);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStartRecording(int channel, int direction) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStartRecording(channel, direction);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStopRecording(int channel) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStopRecording(channel);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void startEmoji(int sessionId, String emojiInfo) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sveStartEmoji(sessionId, emojiInfo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopEmoji(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sveStopEmoji(sessionId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void restartEmoji(int sessionId) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sveRestartEmoji(sessionId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int cpveStartInjection(String filename, int samplingRate) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return -1;
        }
        try {
            return iSecVideoEngineService.cpveStartInjection(filename, samplingRate);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int cpveStopInjection() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return -1;
        }
        try {
            return iSecVideoEngineService.cpveStopInjection();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
