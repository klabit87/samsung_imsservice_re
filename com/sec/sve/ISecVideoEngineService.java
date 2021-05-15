package com.sec.sve;

import android.net.Network;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;

public interface ISecVideoEngineService extends IInterface {
    void bindToNetwork(Network network) throws RemoteException;

    int cpveStartInjection(String str, int i) throws RemoteException;

    int cpveStopInjection() throws RemoteException;

    boolean isSupportingCameraMotor() throws RemoteException;

    void onDestroy() throws RemoteException;

    void registerForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) throws RemoteException;

    void registerForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) throws RemoteException;

    int saeCreateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6, String str3, boolean z, boolean z2) throws RemoteException;

    int saeDeleteChannel(int i) throws RemoteException;

    int saeEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException;

    int saeGetAudioRxTrackId(int i) throws RemoteException;

    TimeInfo saeGetLastPlayedVoiceTime(int i) throws RemoteException;

    int saeGetVersion(byte[] bArr, int i) throws RemoteException;

    int saeHandleDtmf(int i, int i2, int i3, int i4) throws RemoteException;

    void saeInitialize(int i, int i2, int i3) throws RemoteException;

    int saeModifyChannel(int i, int i2) throws RemoteException;

    int saeSetAudioPath(int i, int i2) throws RemoteException;

    int saeSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException;

    int saeSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int saeSetRtcpOnCall(int i, int i2, int i3) throws RemoteException;

    int saeSetRtcpTimeout(int i, long j) throws RemoteException;

    int saeSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException;

    int saeSetRtpTimeout(int i, long j) throws RemoteException;

    int saeSetTOS(int i, int i2) throws RemoteException;

    int saeSetVoicePlayDelay(int i, int i2) throws RemoteException;

    int saeStartChannel(int i, int i2, boolean z) throws RemoteException;

    int saeStartRecording(int i, int i2, int i3, boolean z) throws RemoteException;

    int saeStopChannel(int i) throws RemoteException;

    int saeStopRecording(int i, boolean z) throws RemoteException;

    void saeTerminate() throws RemoteException;

    int saeUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) throws RemoteException;

    void sendStillImage(int i, boolean z, String str, String str2) throws RemoteException;

    void setCameraEffect(int i) throws RemoteException;

    void setDisplaySurface(Surface surface, int i) throws RemoteException;

    void setOrientation(int i) throws RemoteException;

    void setPreviewResolution(int i, int i2) throws RemoteException;

    void setPreviewSurface(Surface surface, int i) throws RemoteException;

    void setZoom(float f) throws RemoteException;

    int sreCreateRelayChannel(int i, int i2) throws RemoteException;

    int sreCreateStream(int i, int i2, int i3, String str, int i4, String str2, int i5, boolean z, boolean z2, int i6, int i7, String str3, boolean z3, boolean z4) throws RemoteException;

    int sreDeleteRelayChannel(int i) throws RemoteException;

    int sreDeleteStream(int i) throws RemoteException;

    int sreEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException;

    boolean sreGetMdmn(int i) throws RemoteException;

    String sreGetVersion() throws RemoteException;

    int sreHoldRelayChannel(int i) throws RemoteException;

    void sreInitialize() throws RemoteException;

    int sreResumeRelayChannel(int i) throws RemoteException;

    int sreSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, int i15) throws RemoteException;

    int sreSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6) throws RemoteException;

    int sreSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException;

    int sreSetMdmn(int i, boolean z) throws RemoteException;

    int sreSetNetId(int i, long j) throws RemoteException;

    int sreSetRtcpOnCall(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int sreSetRtcpTimeout(int i, int i2) throws RemoteException;

    int sreSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException;

    int sreSetRtpTimeout(int i, int i2) throws RemoteException;

    int sreStartRecording(int i, int i2, int i3) throws RemoteException;

    int sreStartRelayChannel(int i, int i2) throws RemoteException;

    int sreStartStream(int i, int i2, int i3) throws RemoteException;

    int sreStopRecording(int i, int i2) throws RemoteException;

    int sreStopRelayChannel(int i) throws RemoteException;

    int sreUpdateRelayChannel(int i, int i2) throws RemoteException;

    int sreUpdateStream(int i) throws RemoteException;

    int steCreateChannel(int i, String str, int i2, String str2, int i3, int i4, int i5, String str3, boolean z, boolean z2) throws RemoteException;

    int steDeleteChannel(int i) throws RemoteException;

    int steEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException;

    void steInitialize() throws RemoteException;

    int steModifyChannel(int i, int i2) throws RemoteException;

    int steSendText(int i, String str, int i2) throws RemoteException;

    int steSetCallOptions(int i, boolean z) throws RemoteException;

    int steSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException;

    int steSetNetId(int i, int i2) throws RemoteException;

    int steSetRtcpOnCall(int i, int i2, int i3) throws RemoteException;

    int steSetRtcpTimeout(int i, long j) throws RemoteException;

    int steSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException;

    int steSetRtpTimeout(int i, long j) throws RemoteException;

    int steSetSessionId(int i, int i2) throws RemoteException;

    int steStartChannel(int i, int i2, boolean z) throws RemoteException;

    int steStopChannel(int i) throws RemoteException;

    int steUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) throws RemoteException;

    int sveCmcRecorderCreate(int i, int i2, int i3, String str, int i4, int i5, long j, int i6, String str2, int i7, int i8, int i9, int i10, int i11, long j2, String str3) throws RemoteException;

    int sveCreateChannel() throws RemoteException;

    String sveGetCodecCapacity(int i) throws RemoteException;

    TimeInfo sveGetLastPlayedVideoTime(int i) throws RemoteException;

    TimeInfo sveGetRtcpTimeInfo(int i) throws RemoteException;

    int sveRecorderCreate(int i, String str, int i2, int i3, String str2, int i4) throws RemoteException;

    int sveRecorderDelete(int i) throws RemoteException;

    int sveRecorderStart(int i) throws RemoteException;

    int sveRecorderStop(int i, boolean z) throws RemoteException;

    void sveRestartEmoji(int i) throws RemoteException;

    int sveSendGeneralEvent(int i, int i2, int i3, String str) throws RemoteException;

    int sveSetCodecInfo(int i, int i2, int i3, int i4, int i5, int i6, String str, int i7, int i8, int i9, int i10, int i11, boolean z, int i12, boolean z2, int i13, int i14, int i15, int i16, int i17, byte[] bArr, byte[] bArr2, byte[] bArr3, int i18, int i19, int i20) throws RemoteException;

    int sveSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6) throws RemoteException;

    int sveSetGcmSrtpParams(int i, int i2, int i3, int i4, char c, int i5, byte[] bArr, int i6, byte[] bArr2, int i7) throws RemoteException;

    int sveSetHeldInfo(int i, boolean z, boolean z2) throws RemoteException;

    int sveSetMediaConfig(int i, boolean z, int i2, boolean z2, int i3, int i4, int i5, int i6) throws RemoteException;

    int sveSetNetworkQoS(int i, int i2, int i3, int i4) throws RemoteException;

    int sveSetSRTPParams(int i, String str, byte[] bArr, int i2, int i3, int i4, int i5, String str2, byte[] bArr2, int i6, int i7, int i8, int i9) throws RemoteException;

    int sveSetVideoPlayDelay(int i, int i2) throws RemoteException;

    int sveStartCamera(int i, int i2) throws RemoteException;

    int sveStartChannel(int i, int i2, int i3) throws RemoteException;

    void sveStartEmoji(int i, String str) throws RemoteException;

    int sveStartRecording(int i, int i2) throws RemoteException;

    int sveStopCamera() throws RemoteException;

    int sveStopChannel(int i) throws RemoteException;

    void sveStopEmoji(int i) throws RemoteException;

    int sveStopRecording(int i) throws RemoteException;

    void switchCamera() throws RemoteException;

    void unregisterForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) throws RemoteException;

    void unregisterForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) throws RemoteException;

    public static class Default implements ISecVideoEngineService {
        public void onDestroy() throws RemoteException {
        }

        public void setPreviewSurface(Surface surface, int color) throws RemoteException {
        }

        public void setDisplaySurface(Surface surface, int color) throws RemoteException {
        }

        public void setOrientation(int orientation) throws RemoteException {
        }

        public void setZoom(float value) throws RemoteException {
        }

        public void switchCamera() throws RemoteException {
        }

        public void sendStillImage(int channel, boolean enable, String filePath, String frameSize) throws RemoteException {
        }

        public void setCameraEffect(int value) throws RemoteException {
        }

        public void setPreviewResolution(int width, int height) throws RemoteException {
        }

        public void bindToNetwork(Network network) throws RemoteException {
        }

        public void saeInitialize(int convertedMno, int dtmfMode, int sas) throws RemoteException {
        }

        public void saeTerminate() throws RemoteException {
        }

        public int saeSetCodecInfo(int channel, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw) throws RemoteException {
            return 0;
        }

        public int saeCreateChannel(int channel, int mno, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) throws RemoteException {
            return 0;
        }

        public int saeStartChannel(int channel, int direction, boolean enableIpv6) throws RemoteException {
            return 0;
        }

        public int saeUpdateChannel(int channel, int dir, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort) throws RemoteException {
            return 0;
        }

        public int saeStopChannel(int channel) throws RemoteException {
            return 0;
        }

        public int saeModifyChannel(int channel, int direction) throws RemoteException {
            return 0;
        }

        public int saeDeleteChannel(int channel) throws RemoteException {
            return 0;
        }

        public int saeHandleDtmf(int channel, int code, int mode, int operation) throws RemoteException {
            return 0;
        }

        public int saeSetDtmfCodecInfo(int channel, int type, int rxtype, int bitrate, int inband) throws RemoteException {
            return 0;
        }

        public int saeEnableSRTP(int channel, int direction, int profile, byte[] key, int keylen) throws RemoteException {
            return 0;
        }

        public int saeSetRtcpOnCall(int channel, int rr, int rs) throws RemoteException {
            return 0;
        }

        public int saeSetRtpTimeout(int channel, long sec) throws RemoteException {
            return 0;
        }

        public int saeSetRtcpTimeout(int channel, long sec) throws RemoteException {
            return 0;
        }

        public int saeSetRtcpXr(int channel, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) throws RemoteException {
            return 0;
        }

        public TimeInfo saeGetLastPlayedVoiceTime(int channel) throws RemoteException {
            return null;
        }

        public int saeSetVoicePlayDelay(int channel, int delayTime) throws RemoteException {
            return 0;
        }

        public int saeSetTOS(int channel, int tos) throws RemoteException {
            return 0;
        }

        public int saeGetVersion(byte[] version, int bufflen) throws RemoteException {
            return 0;
        }

        public int saeGetAudioRxTrackId(int channel) throws RemoteException {
            return 0;
        }

        public int saeSetAudioPath(int dir_in, int dir_out) throws RemoteException {
            return 0;
        }

        public int sveCreateChannel() throws RemoteException {
            return 0;
        }

        public int sveStartChannel(int channel, int oldDirection, int newDirection) throws RemoteException {
            return 0;
        }

        public int sveStopChannel(int channel) throws RemoteException {
            return 0;
        }

        public int sveSetConnection(int channel, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, int crbtType) throws RemoteException {
            return 0;
        }

        public int sveSetCodecInfo(int channel, int as, int rs, int rr, int recvCodecPT, int sendCodecPT, String name, int dir, int width, int height, int frameRate, int maxBitrate, boolean enableAVPF, int supportAVPFType, boolean enableOrientation, int CVOGranularity, int H264Profile, int H264Level, int H264ConstraintInfo, int H264PackMode, byte[] sps, byte[] pps, byte[] vps, int spsLen, int ppsLen, int vpsLen) throws RemoteException {
            return 0;
        }

        public int sveSetSRTPParams(int sessionId, String offerSuite, byte[] aucTagKeyLocal, int sendKeySize, int ucTagKeyLenLocal, int uiTimetoLiveLocal, int uiMKILocal, String answerSuite, byte[] aucTagKeyRemote, int recvKeySize, int ucTagKeyLenRemote, int uiTimetoLiveRemote, int uiMKIRemote) throws RemoteException {
            return 0;
        }

        public int sveSetGcmSrtpParams(int sessionId, int srtpProfile, int keyId, int keytype, char csId, int csbIdValue, byte[] inkey, int inkeyLength, byte[] rand, int randLengthValue) throws RemoteException {
            return 0;
        }

        public int sveSetMediaConfig(int sessionId, boolean timeOutOnBoth, int rtpTimeout, boolean rtpKeepAlive, int rtcpTimeout, int mtuSize, int mno, int keepAliveInterval) throws RemoteException {
            return 0;
        }

        public int sveStartCamera(int sessionId, int cameraId) throws RemoteException {
            return 0;
        }

        public int sveStopCamera() throws RemoteException {
            return 0;
        }

        public void sveStartEmoji(int sessionId, String effect) throws RemoteException {
        }

        public void sveStopEmoji(int sessionId) throws RemoteException {
        }

        public void sveRestartEmoji(int sessionId) throws RemoteException {
        }

        public int sveSetHeldInfo(int sessionId, boolean isLocal, boolean isHeld) throws RemoteException {
            return 0;
        }

        public TimeInfo sveGetLastPlayedVideoTime(int sessionId) throws RemoteException {
            return null;
        }

        public int sveSetVideoPlayDelay(int sessionId, int delayTime) throws RemoteException {
            return 0;
        }

        public int sveSetNetworkQoS(int sessionId, int ul_bler, int dl_bler, int grant) throws RemoteException {
            return 0;
        }

        public int sveSendGeneralEvent(int event, int arg1, int arg2, String arg3) throws RemoteException {
            return 0;
        }

        public TimeInfo sveGetRtcpTimeInfo(int sessionId) throws RemoteException {
            return null;
        }

        public String sveGetCodecCapacity(int codecMaxLen) throws RemoteException {
            return null;
        }

        public void steInitialize() throws RemoteException {
        }

        public int steSetCodecInfo(int channel, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw) throws RemoteException {
            return 0;
        }

        public int steCreateChannel(int mno, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) throws RemoteException {
            return 0;
        }

        public int steStartChannel(int channel, int direction, boolean enableIpv6) throws RemoteException {
            return 0;
        }

        public int steUpdateChannel(int channel, int dir, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort) throws RemoteException {
            return 0;
        }

        public int steStopChannel(int channel) throws RemoteException {
            return 0;
        }

        public int steModifyChannel(int channel, int direction) throws RemoteException {
            return 0;
        }

        public int steDeleteChannel(int channel) throws RemoteException {
            return 0;
        }

        public int steSendText(int channel, String text, int len) throws RemoteException {
            return 0;
        }

        public int steEnableSRTP(int channel, int direction, int profile, byte[] key, int keylen) throws RemoteException {
            return 0;
        }

        public int steSetRtcpOnCall(int channel, int rr, int rs) throws RemoteException {
            return 0;
        }

        public int steSetRtpTimeout(int channel, long sec) throws RemoteException {
            return 0;
        }

        public int steSetRtcpTimeout(int channel, long sec) throws RemoteException {
            return 0;
        }

        public int steSetRtcpXr(int channel, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) throws RemoteException {
            return 0;
        }

        public int steSetCallOptions(int channel, boolean isRtcpOnCall) throws RemoteException {
            return 0;
        }

        public int steSetNetId(int channel, int netId) throws RemoteException {
            return 0;
        }

        public int steSetSessionId(int channelId, int sessionId) throws RemoteException {
            return 0;
        }

        public void sreInitialize() throws RemoteException {
        }

        public String sreGetVersion() throws RemoteException {
            return null;
        }

        public int sreSetMdmn(int sessionId, boolean isMdmn) throws RemoteException {
            return 0;
        }

        public boolean sreGetMdmn(int sessionId) throws RemoteException {
            return false;
        }

        public int sreSetNetId(int sessionId, long netId) throws RemoteException {
            return 0;
        }

        public int sreCreateStream(int phoneId, int sessionId, int mno, String localIp, int localPort, String remoteIp, int remotePort, boolean isIpv6, boolean isMdmn, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) throws RemoteException {
            return 0;
        }

        public int sreStartStream(int sessionId, int oldDirection, int newDirection) throws RemoteException {
            return 0;
        }

        public int sreDeleteStream(int sessionId) throws RemoteException {
            return 0;
        }

        public int sreUpdateStream(int sessionId) throws RemoteException {
            return 0;
        }

        public int sreCreateRelayChannel(int lhs_stream, int rhs_stream) throws RemoteException {
            return 0;
        }

        public int sreDeleteRelayChannel(int channel) throws RemoteException {
            return 0;
        }

        public int sreStartRelayChannel(int channel, int direction) throws RemoteException {
            return 0;
        }

        public int sreStopRelayChannel(int channel) throws RemoteException {
            return 0;
        }

        public int sreHoldRelayChannel(int channel) throws RemoteException {
            return 0;
        }

        public int sreResumeRelayChannel(int channel) throws RemoteException {
            return 0;
        }

        public int sreUpdateRelayChannel(int channel, int stream) throws RemoteException {
            return 0;
        }

        public int sreSetConnection(int sessionId, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, int crbtType) throws RemoteException {
            return 0;
        }

        public int sreEnableSRTP(int sessionId, int direction, int profile, byte[] key, int keylen) throws RemoteException {
            return 0;
        }

        public int sreSetRtcpOnCall(int sessionId, int rr, int rs, int rtpTimer, int rtcpTimer) throws RemoteException {
            return 0;
        }

        public int sreSetRtpTimeout(int sessionId, int sec) throws RemoteException {
            return 0;
        }

        public int sreSetRtcpTimeout(int sessionId, int sec) throws RemoteException {
            return 0;
        }

        public int sreSetRtcpXr(int sessionId, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) throws RemoteException {
            return 0;
        }

        public int sreSetCodecInfo(int sessionId, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw, int protocol) throws RemoteException {
            return 0;
        }

        public int sreSetDtmfCodecInfo(int phoneId, int sessionId, int type, int rxtype, int bitrate, int inband) throws RemoteException {
            return 0;
        }

        public int sreStartRecording(int sessionId, int streamId, int channel) throws RemoteException {
            return 0;
        }

        public int sreStopRecording(int sessionId, int channel) throws RemoteException {
            return 0;
        }

        public boolean isSupportingCameraMotor() throws RemoteException {
            return false;
        }

        public int sveRecorderCreate(int sessionId, String filename, int audioId, int audioSampleRate, String audioCodec, int videoId) throws RemoteException {
            return 0;
        }

        public int sveCmcRecorderCreate(int sessionId, int audioId, int audioSampleRate, String audioCodec, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) throws RemoteException {
            return 0;
        }

        public int sveRecorderDelete(int sessionId) throws RemoteException {
            return 0;
        }

        public int sveRecorderStart(int sessionId) throws RemoteException {
            return 0;
        }

        public int sveRecorderStop(int sessionId, boolean saveFile) throws RemoteException {
            return 0;
        }

        public int saeStartRecording(int channel, int direction, int samplingRate, boolean bIsApVoice) throws RemoteException {
            return 0;
        }

        public int saeStopRecording(int channel, boolean bIsApVoice) throws RemoteException {
            return 0;
        }

        public int sveStartRecording(int channel, int direction) throws RemoteException {
            return 0;
        }

        public int sveStopRecording(int channel) throws RemoteException {
            return 0;
        }

        public int cpveStartInjection(String filename, int samplingRate) throws RemoteException {
            return 0;
        }

        public int cpveStopInjection() throws RemoteException {
            return 0;
        }

        public void registerForMediaEventListener(IImsMediaEventListener listener) throws RemoteException {
        }

        public void unregisterForMediaEventListener(IImsMediaEventListener listener) throws RemoteException {
        }

        public void registerForCmcEventListener(ICmcMediaEventListener listener) throws RemoteException {
        }

        public void unregisterForCmcEventListener(ICmcMediaEventListener listener) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISecVideoEngineService {
        private static final String DESCRIPTOR = "com.sec.sve.ISecVideoEngineService";
        static final int TRANSACTION_bindToNetwork = 10;
        static final int TRANSACTION_cpveStartInjection = 106;
        static final int TRANSACTION_cpveStopInjection = 107;
        static final int TRANSACTION_isSupportingCameraMotor = 96;
        static final int TRANSACTION_onDestroy = 1;
        static final int TRANSACTION_registerForCmcEventListener = 110;
        static final int TRANSACTION_registerForMediaEventListener = 108;
        static final int TRANSACTION_saeCreateChannel = 14;
        static final int TRANSACTION_saeDeleteChannel = 19;
        static final int TRANSACTION_saeEnableSRTP = 22;
        static final int TRANSACTION_saeGetAudioRxTrackId = 31;
        static final int TRANSACTION_saeGetLastPlayedVoiceTime = 27;
        static final int TRANSACTION_saeGetVersion = 30;
        static final int TRANSACTION_saeHandleDtmf = 20;
        static final int TRANSACTION_saeInitialize = 11;
        static final int TRANSACTION_saeModifyChannel = 18;
        static final int TRANSACTION_saeSetAudioPath = 32;
        static final int TRANSACTION_saeSetCodecInfo = 13;
        static final int TRANSACTION_saeSetDtmfCodecInfo = 21;
        static final int TRANSACTION_saeSetRtcpOnCall = 23;
        static final int TRANSACTION_saeSetRtcpTimeout = 25;
        static final int TRANSACTION_saeSetRtcpXr = 26;
        static final int TRANSACTION_saeSetRtpTimeout = 24;
        static final int TRANSACTION_saeSetTOS = 29;
        static final int TRANSACTION_saeSetVoicePlayDelay = 28;
        static final int TRANSACTION_saeStartChannel = 15;
        static final int TRANSACTION_saeStartRecording = 102;
        static final int TRANSACTION_saeStopChannel = 17;
        static final int TRANSACTION_saeStopRecording = 103;
        static final int TRANSACTION_saeTerminate = 12;
        static final int TRANSACTION_saeUpdateChannel = 16;
        static final int TRANSACTION_sendStillImage = 7;
        static final int TRANSACTION_setCameraEffect = 8;
        static final int TRANSACTION_setDisplaySurface = 3;
        static final int TRANSACTION_setOrientation = 4;
        static final int TRANSACTION_setPreviewResolution = 9;
        static final int TRANSACTION_setPreviewSurface = 2;
        static final int TRANSACTION_setZoom = 5;
        static final int TRANSACTION_sreCreateRelayChannel = 79;
        static final int TRANSACTION_sreCreateStream = 75;
        static final int TRANSACTION_sreDeleteRelayChannel = 80;
        static final int TRANSACTION_sreDeleteStream = 77;
        static final int TRANSACTION_sreEnableSRTP = 87;
        static final int TRANSACTION_sreGetMdmn = 73;
        static final int TRANSACTION_sreGetVersion = 71;
        static final int TRANSACTION_sreHoldRelayChannel = 83;
        static final int TRANSACTION_sreInitialize = 70;
        static final int TRANSACTION_sreResumeRelayChannel = 84;
        static final int TRANSACTION_sreSetCodecInfo = 92;
        static final int TRANSACTION_sreSetConnection = 86;
        static final int TRANSACTION_sreSetDtmfCodecInfo = 93;
        static final int TRANSACTION_sreSetMdmn = 72;
        static final int TRANSACTION_sreSetNetId = 74;
        static final int TRANSACTION_sreSetRtcpOnCall = 88;
        static final int TRANSACTION_sreSetRtcpTimeout = 90;
        static final int TRANSACTION_sreSetRtcpXr = 91;
        static final int TRANSACTION_sreSetRtpTimeout = 89;
        static final int TRANSACTION_sreStartRecording = 94;
        static final int TRANSACTION_sreStartRelayChannel = 81;
        static final int TRANSACTION_sreStartStream = 76;
        static final int TRANSACTION_sreStopRecording = 95;
        static final int TRANSACTION_sreStopRelayChannel = 82;
        static final int TRANSACTION_sreUpdateRelayChannel = 85;
        static final int TRANSACTION_sreUpdateStream = 78;
        static final int TRANSACTION_steCreateChannel = 55;
        static final int TRANSACTION_steDeleteChannel = 60;
        static final int TRANSACTION_steEnableSRTP = 62;
        static final int TRANSACTION_steInitialize = 53;
        static final int TRANSACTION_steModifyChannel = 59;
        static final int TRANSACTION_steSendText = 61;
        static final int TRANSACTION_steSetCallOptions = 67;
        static final int TRANSACTION_steSetCodecInfo = 54;
        static final int TRANSACTION_steSetNetId = 68;
        static final int TRANSACTION_steSetRtcpOnCall = 63;
        static final int TRANSACTION_steSetRtcpTimeout = 65;
        static final int TRANSACTION_steSetRtcpXr = 66;
        static final int TRANSACTION_steSetRtpTimeout = 64;
        static final int TRANSACTION_steSetSessionId = 69;
        static final int TRANSACTION_steStartChannel = 56;
        static final int TRANSACTION_steStopChannel = 58;
        static final int TRANSACTION_steUpdateChannel = 57;
        static final int TRANSACTION_sveCmcRecorderCreate = 98;
        static final int TRANSACTION_sveCreateChannel = 33;
        static final int TRANSACTION_sveGetCodecCapacity = 52;
        static final int TRANSACTION_sveGetLastPlayedVideoTime = 47;
        static final int TRANSACTION_sveGetRtcpTimeInfo = 51;
        static final int TRANSACTION_sveRecorderCreate = 97;
        static final int TRANSACTION_sveRecorderDelete = 99;
        static final int TRANSACTION_sveRecorderStart = 100;
        static final int TRANSACTION_sveRecorderStop = 101;
        static final int TRANSACTION_sveRestartEmoji = 45;
        static final int TRANSACTION_sveSendGeneralEvent = 50;
        static final int TRANSACTION_sveSetCodecInfo = 37;
        static final int TRANSACTION_sveSetConnection = 36;
        static final int TRANSACTION_sveSetGcmSrtpParams = 39;
        static final int TRANSACTION_sveSetHeldInfo = 46;
        static final int TRANSACTION_sveSetMediaConfig = 40;
        static final int TRANSACTION_sveSetNetworkQoS = 49;
        static final int TRANSACTION_sveSetSRTPParams = 38;
        static final int TRANSACTION_sveSetVideoPlayDelay = 48;
        static final int TRANSACTION_sveStartCamera = 41;
        static final int TRANSACTION_sveStartChannel = 34;
        static final int TRANSACTION_sveStartEmoji = 43;
        static final int TRANSACTION_sveStartRecording = 104;
        static final int TRANSACTION_sveStopCamera = 42;
        static final int TRANSACTION_sveStopChannel = 35;
        static final int TRANSACTION_sveStopEmoji = 44;
        static final int TRANSACTION_sveStopRecording = 105;
        static final int TRANSACTION_switchCamera = 6;
        static final int TRANSACTION_unregisterForCmcEventListener = 111;
        static final int TRANSACTION_unregisterForMediaEventListener = 109;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecVideoEngineService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecVideoEngineService)) {
                return new Proxy(obj);
            }
            return (ISecVideoEngineService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            */
        public boolean onTransact(int r70, android.os.Parcel r71, android.os.Parcel r72, int r73) throws android.os.RemoteException {
            /*
                r69 = this;
                r0 = r69
                r15 = r70
                r14 = r71
                r13 = r72
                java.lang.String r12 = "com.sec.sve.ISecVideoEngineService"
                r1 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r11 = 1
                if (r15 == r1) goto L_0x11ce
                r1 = 0
                switch(r15) {
                    case 1: goto L_0x11be;
                    case 2: goto L_0x119a;
                    case 3: goto L_0x1176;
                    case 4: goto L_0x1162;
                    case 5: goto L_0x114e;
                    case 6: goto L_0x113e;
                    case 7: goto L_0x1117;
                    case 8: goto L_0x1103;
                    case 9: goto L_0x10eb;
                    case 10: goto L_0x10cb;
                    case 11: goto L_0x10ae;
                    case 12: goto L_0x109b;
                    case 13: goto L_0x0fb6;
                    case 14: goto L_0x0f59;
                    case 15: goto L_0x0f34;
                    case 16: goto L_0x0ef1;
                    case 17: goto L_0x0eda;
                    case 18: goto L_0x0ebf;
                    case 19: goto L_0x0ea8;
                    case 20: goto L_0x0e85;
                    case 21: goto L_0x0e57;
                    case 22: goto L_0x0e29;
                    case 23: goto L_0x0e0a;
                    case 24: goto L_0x0def;
                    case 25: goto L_0x0dd4;
                    case 26: goto L_0x0d9f;
                    case 27: goto L_0x0d7f;
                    case 28: goto L_0x0d64;
                    case 29: goto L_0x0d49;
                    case 30: goto L_0x0d2e;
                    case 31: goto L_0x0d17;
                    case 32: goto L_0x0cfc;
                    case 33: goto L_0x0ce9;
                    case 34: goto L_0x0cca;
                    case 35: goto L_0x0cb2;
                    case 36: goto L_0x0c6d;
                    case 37: goto L_0x0bae;
                    case 38: goto L_0x0b47;
                    case 39: goto L_0x0af2;
                    case 40: goto L_0x0aa7;
                    case 41: goto L_0x0a8a;
                    case 42: goto L_0x0a75;
                    case 43: goto L_0x0a5c;
                    case 44: goto L_0x0a47;
                    case 45: goto L_0x0a32;
                    case 46: goto L_0x0a09;
                    case 47: goto L_0x09e7;
                    case 48: goto L_0x09ca;
                    case 49: goto L_0x09a5;
                    case 50: goto L_0x0980;
                    case 51: goto L_0x095e;
                    case 52: goto L_0x0944;
                    case 53: goto L_0x0936;
                    case 54: goto L_0x0850;
                    case 55: goto L_0x07fa;
                    case 56: goto L_0x07d9;
                    case 57: goto L_0x0797;
                    case 58: goto L_0x0781;
                    case 59: goto L_0x0767;
                    case 60: goto L_0x0751;
                    case 61: goto L_0x0733;
                    case 62: goto L_0x0706;
                    case 63: goto L_0x06e8;
                    case 64: goto L_0x06ce;
                    case 65: goto L_0x06b4;
                    case 66: goto L_0x0680;
                    case 67: goto L_0x0663;
                    case 68: goto L_0x0649;
                    case 69: goto L_0x062f;
                    case 70: goto L_0x0621;
                    case 71: goto L_0x060f;
                    case 72: goto L_0x05f2;
                    case 73: goto L_0x05dc;
                    case 74: goto L_0x05bc;
                    case 75: goto L_0x0544;
                    case 76: goto L_0x0525;
                    case 77: goto L_0x050e;
                    case 78: goto L_0x04f7;
                    case 79: goto L_0x04dc;
                    case 80: goto L_0x04c5;
                    case 81: goto L_0x04aa;
                    case 82: goto L_0x0493;
                    case 83: goto L_0x047c;
                    case 84: goto L_0x0465;
                    case 85: goto L_0x044a;
                    case 86: goto L_0x0407;
                    case 87: goto L_0x03d9;
                    case 88: goto L_0x03ab;
                    case 89: goto L_0x0390;
                    case 90: goto L_0x0374;
                    case 91: goto L_0x033d;
                    case 92: goto L_0x0252;
                    case 93: goto L_0x021d;
                    case 94: goto L_0x01fe;
                    case 95: goto L_0x01e2;
                    case 96: goto L_0x01d0;
                    case 97: goto L_0x0199;
                    case 98: goto L_0x0124;
                    case 99: goto L_0x0112;
                    case 100: goto L_0x0100;
                    case 101: goto L_0x00e7;
                    case 102: goto L_0x00c6;
                    case 103: goto L_0x00ad;
                    case 104: goto L_0x0097;
                    case 105: goto L_0x0085;
                    case 106: goto L_0x006f;
                    case 107: goto L_0x0061;
                    case 108: goto L_0x004f;
                    case 109: goto L_0x003d;
                    case 110: goto L_0x002b;
                    case 111: goto L_0x0019;
                    default: goto L_0x0014;
                }
            L_0x0014:
                boolean r1 = super.onTransact(r70, r71, r72, r73)
                return r1
            L_0x0019:
                r14.enforceInterface(r12)
                android.os.IBinder r1 = r71.readStrongBinder()
                com.sec.sve.ICmcMediaEventListener r1 = com.sec.sve.ICmcMediaEventListener.Stub.asInterface(r1)
                r0.unregisterForCmcEventListener(r1)
                r72.writeNoException()
                return r11
            L_0x002b:
                r14.enforceInterface(r12)
                android.os.IBinder r1 = r71.readStrongBinder()
                com.sec.sve.ICmcMediaEventListener r1 = com.sec.sve.ICmcMediaEventListener.Stub.asInterface(r1)
                r0.registerForCmcEventListener(r1)
                r72.writeNoException()
                return r11
            L_0x003d:
                r14.enforceInterface(r12)
                android.os.IBinder r1 = r71.readStrongBinder()
                com.sec.sve.IImsMediaEventListener r1 = com.sec.sve.IImsMediaEventListener.Stub.asInterface(r1)
                r0.unregisterForMediaEventListener(r1)
                r72.writeNoException()
                return r11
            L_0x004f:
                r14.enforceInterface(r12)
                android.os.IBinder r1 = r71.readStrongBinder()
                com.sec.sve.IImsMediaEventListener r1 = com.sec.sve.IImsMediaEventListener.Stub.asInterface(r1)
                r0.registerForMediaEventListener(r1)
                r72.writeNoException()
                return r11
            L_0x0061:
                r14.enforceInterface(r12)
                int r1 = r69.cpveStopInjection()
                r72.writeNoException()
                r13.writeInt(r1)
                return r11
            L_0x006f:
                r14.enforceInterface(r12)
                java.lang.String r1 = r71.readString()
                int r2 = r71.readInt()
                int r3 = r0.cpveStartInjection(r1, r2)
                r72.writeNoException()
                r13.writeInt(r3)
                return r11
            L_0x0085:
                r14.enforceInterface(r12)
                int r1 = r71.readInt()
                int r2 = r0.sveStopRecording(r1)
                r72.writeNoException()
                r13.writeInt(r2)
                return r11
            L_0x0097:
                r14.enforceInterface(r12)
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r0.sveStartRecording(r1, r2)
                r72.writeNoException()
                r13.writeInt(r3)
                return r11
            L_0x00ad:
                r14.enforceInterface(r12)
                int r2 = r71.readInt()
                int r3 = r71.readInt()
                if (r3 == 0) goto L_0x00bb
                r1 = r11
            L_0x00bb:
                int r3 = r0.saeStopRecording(r2, r1)
                r72.writeNoException()
                r13.writeInt(r3)
                return r11
            L_0x00c6:
                r14.enforceInterface(r12)
                int r2 = r71.readInt()
                int r3 = r71.readInt()
                int r4 = r71.readInt()
                int r5 = r71.readInt()
                if (r5 == 0) goto L_0x00dc
                r1 = r11
            L_0x00dc:
                int r5 = r0.saeStartRecording(r2, r3, r4, r1)
                r72.writeNoException()
                r13.writeInt(r5)
                return r11
            L_0x00e7:
                r14.enforceInterface(r12)
                int r2 = r71.readInt()
                int r3 = r71.readInt()
                if (r3 == 0) goto L_0x00f5
                r1 = r11
            L_0x00f5:
                int r3 = r0.sveRecorderStop(r2, r1)
                r72.writeNoException()
                r13.writeInt(r3)
                return r11
            L_0x0100:
                r14.enforceInterface(r12)
                int r1 = r71.readInt()
                int r2 = r0.sveRecorderStart(r1)
                r72.writeNoException()
                r13.writeInt(r2)
                return r11
            L_0x0112:
                r14.enforceInterface(r12)
                int r1 = r71.readInt()
                int r2 = r0.sveRecorderDelete(r1)
                r72.writeNoException()
                r13.writeInt(r2)
                return r11
            L_0x0124:
                r14.enforceInterface(r12)
                int r19 = r71.readInt()
                r1 = r19
                int r20 = r71.readInt()
                r2 = r20
                int r21 = r71.readInt()
                r3 = r21
                java.lang.String r22 = r71.readString()
                r4 = r22
                int r23 = r71.readInt()
                r5 = r23
                int r24 = r71.readInt()
                r6 = r24
                long r25 = r71.readLong()
                r7 = r25
                int r27 = r71.readInt()
                r9 = r27
                java.lang.String r28 = r71.readString()
                r10 = r28
                int r29 = r71.readInt()
                r11 = r29
                int r30 = r71.readInt()
                r33 = r12
                r12 = r30
                int r31 = r71.readInt()
                r13 = r31
                int r34 = r71.readInt()
                r14 = r34
                int r35 = r71.readInt()
                r15 = r35
                long r36 = r71.readLong()
                r16 = r36
                java.lang.String r38 = r71.readString()
                r18 = r38
                r0 = r69
                int r0 = r0.sveCmcRecorderCreate(r1, r2, r3, r4, r5, r6, r7, r9, r10, r11, r12, r13, r14, r15, r16, r18)
                r72.writeNoException()
                r8 = r72
                r8.writeInt(r0)
                r7 = 1
                return r7
            L_0x0199:
                r7 = r11
                r33 = r12
                r8 = r13
                r15 = r71
                r14 = r33
                r15.enforceInterface(r14)
                int r9 = r71.readInt()
                java.lang.String r10 = r71.readString()
                int r11 = r71.readInt()
                int r12 = r71.readInt()
                java.lang.String r13 = r71.readString()
                int r16 = r71.readInt()
                r0 = r69
                r1 = r9
                r2 = r10
                r3 = r11
                r4 = r12
                r5 = r13
                r6 = r16
                int r0 = r0.sveRecorderCreate(r1, r2, r3, r4, r5, r6)
                r72.writeNoException()
                r8.writeInt(r0)
                return r7
            L_0x01d0:
                r7 = r11
                r8 = r13
                r15 = r14
                r14 = r12
                r15.enforceInterface(r14)
                boolean r0 = r69.isSupportingCameraMotor()
                r72.writeNoException()
                r8.writeInt(r0)
                return r7
            L_0x01e2:
                r7 = r11
                r8 = r13
                r15 = r14
                r14 = r12
                r15.enforceInterface(r14)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                r13 = r69
                int r2 = r13.sreStopRecording(r0, r1)
                r72.writeNoException()
                r8.writeInt(r2)
                return r7
            L_0x01fe:
                r7 = r11
                r8 = r13
                r15 = r14
                r13 = r0
                r14 = r12
                r15.enforceInterface(r14)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r13.sreStartRecording(r0, r1, r2)
                r72.writeNoException()
                r8.writeInt(r3)
                return r7
            L_0x021d:
                r7 = r11
                r8 = r13
                r15 = r14
                r13 = r0
                r14 = r12
                r15.enforceInterface(r14)
                int r9 = r71.readInt()
                int r10 = r71.readInt()
                int r11 = r71.readInt()
                int r12 = r71.readInt()
                int r16 = r71.readInt()
                int r17 = r71.readInt()
                r0 = r69
                r1 = r9
                r2 = r10
                r3 = r11
                r4 = r12
                r5 = r16
                r6 = r17
                int r0 = r0.sreSetDtmfCodecInfo(r1, r2, r3, r4, r5, r6)
                r72.writeNoException()
                r8.writeInt(r0)
                return r7
            L_0x0252:
                r7 = r11
                r8 = r13
                r15 = r14
                r13 = r0
                r14 = r12
                r15.enforceInterface(r14)
                int r32 = r71.readInt()
                java.lang.String r33 = r71.readString()
                int r34 = r71.readInt()
                int r35 = r71.readInt()
                int r36 = r71.readInt()
                int r37 = r71.readInt()
                int r38 = r71.readInt()
                int r39 = r71.readInt()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0282
                r9 = r7
                goto L_0x0283
            L_0x0282:
                r9 = r1
            L_0x0283:
                int r40 = r71.readInt()
                r10 = r40
                int r41 = r71.readInt()
                r11 = r41
                int r42 = r71.readInt()
                r12 = r42
                int r43 = r71.readInt()
                r6 = r13
                r13 = r43
                int r44 = r71.readInt()
                r5 = r14
                r14 = r44
                int r0 = r71.readInt()
                char r4 = (char) r0
                r3 = r15
                r15 = r4
                int r0 = r71.readInt()
                char r2 = (char) r0
                r16 = r2
                int r0 = r71.readInt()
                char r1 = (char) r0
                r17 = r1
                int r0 = r71.readInt()
                char r0 = (char) r0
                r18 = r0
                int r7 = r71.readInt()
                char r7 = (char) r7
                r19 = r7
                r46 = r0
                int r0 = r71.readInt()
                char r0 = (char) r0
                r20 = r0
                int r47 = r71.readInt()
                r21 = r47
                int r48 = r71.readInt()
                r22 = r48
                java.lang.String r49 = r71.readString()
                r23 = r49
                java.lang.String r50 = r71.readString()
                r24 = r50
                java.lang.String r51 = r71.readString()
                r25 = r51
                java.lang.String r52 = r71.readString()
                r26 = r52
                java.lang.String r53 = r71.readString()
                r27 = r53
                java.lang.String r54 = r71.readString()
                r28 = r54
                java.lang.String r55 = r71.readString()
                r29 = r55
                java.lang.String r56 = r71.readString()
                r30 = r56
                int r57 = r71.readInt()
                r31 = r57
                r58 = r0
                r0 = r69
                r59 = r1
                r1 = r32
                r60 = r2
                r2 = r33
                r3 = r34
                r61 = r4
                r4 = r35
                r62 = r5
                r5 = r36
                r6 = r37
                r45 = r7
                r7 = r38
                r8 = r39
                int r0 = r0.sreSetCodecInfo(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30, r31)
                r72.writeNoException()
                r15 = r72
                r15.writeInt(r0)
                r12 = 1
                return r12
            L_0x033d:
                r62 = r12
                r15 = r13
                r12 = r11
                r11 = r71
                r10 = r62
                r11.enforceInterface(r10)
                int r7 = r71.readInt()
                int r8 = r71.readInt()
                int r9 = r71.readInt()
                int r13 = r71.readInt()
                int r14 = r71.readInt()
                int[] r16 = r71.createIntArray()
                r0 = r69
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r13
                r5 = r14
                r6 = r16
                int r0 = r0.sreSetRtcpXr(r1, r2, r3, r4, r5, r6)
                r72.writeNoException()
                r15.writeInt(r0)
                return r12
            L_0x0374:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                r14 = r69
                int r2 = r14.sreSetRtcpTimeout(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x0390:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r14.sreSetRtpTimeout(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x03ab:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r6 = r71.readInt()
                int r7 = r71.readInt()
                int r8 = r71.readInt()
                int r9 = r71.readInt()
                int r13 = r71.readInt()
                r0 = r69
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r13
                int r0 = r0.sreSetRtcpOnCall(r1, r2, r3, r4, r5)
                r72.writeNoException()
                r15.writeInt(r0)
                return r12
            L_0x03d9:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r6 = r71.readInt()
                int r7 = r71.readInt()
                int r8 = r71.readInt()
                byte[] r9 = r71.createByteArray()
                int r13 = r71.readInt()
                r0 = r69
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r13
                int r0 = r0.sreEnableSRTP(r1, r2, r3, r4, r5)
                r72.writeNoException()
                r15.writeInt(r0)
                return r12
            L_0x0407:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r9 = r71.readInt()
                java.lang.String r13 = r71.readString()
                int r16 = r71.readInt()
                java.lang.String r17 = r71.readString()
                int r18 = r71.readInt()
                int r19 = r71.readInt()
                int r20 = r71.readInt()
                int r21 = r71.readInt()
                r0 = r69
                r1 = r9
                r2 = r13
                r3 = r16
                r4 = r17
                r5 = r18
                r6 = r19
                r7 = r20
                r8 = r21
                int r0 = r0.sreSetConnection(r1, r2, r3, r4, r5, r6, r7, r8)
                r72.writeNoException()
                r15.writeInt(r0)
                return r12
            L_0x044a:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r14.sreUpdateRelayChannel(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x0465:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r14.sreResumeRelayChannel(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x047c:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r14.sreHoldRelayChannel(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x0493:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r14.sreStopRelayChannel(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x04aa:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r14.sreStartRelayChannel(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x04c5:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r14.sreDeleteRelayChannel(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x04dc:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r14.sreCreateRelayChannel(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x04f7:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r14.sreUpdateStream(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x050e:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r14.sreDeleteStream(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x0525:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r14.sreStartStream(r0, r1, r2)
                r72.writeNoException()
                r15.writeInt(r3)
                return r12
            L_0x0544:
                r10 = r12
                r15 = r13
                r12 = r11
                r11 = r14
                r14 = r0
                r11.enforceInterface(r10)
                int r16 = r71.readInt()
                int r17 = r71.readInt()
                int r18 = r71.readInt()
                java.lang.String r19 = r71.readString()
                int r20 = r71.readInt()
                java.lang.String r21 = r71.readString()
                int r22 = r71.readInt()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0570
                r8 = r12
                goto L_0x0571
            L_0x0570:
                r8 = r1
            L_0x0571:
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0579
                r9 = r12
                goto L_0x057a
            L_0x0579:
                r9 = r1
            L_0x057a:
                int r23 = r71.readInt()
                int r24 = r71.readInt()
                java.lang.String r25 = r71.readString()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x058e
                r13 = r12
                goto L_0x058f
            L_0x058e:
                r13 = r1
            L_0x058f:
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0596
                r1 = r12
            L_0x0596:
                r7 = r14
                r14 = r1
                r0 = r69
                r1 = r16
                r2 = r17
                r3 = r18
                r4 = r19
                r5 = r20
                r6 = r21
                r7 = r22
                r63 = r10
                r10 = r23
                r11 = r24
                r12 = r25
                int r0 = r0.sreCreateStream(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14)
                r72.writeNoException()
                r15.writeInt(r0)
                r12 = 1
                return r12
            L_0x05bc:
                r63 = r12
                r15 = r13
                r12 = r11
                r14 = r71
                r13 = r63
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                long r1 = r71.readLong()
                r11 = r69
                int r3 = r11.sreSetNetId(r0, r1)
                r72.writeNoException()
                r15.writeInt(r3)
                return r12
            L_0x05dc:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                boolean r1 = r11.sreGetMdmn(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x05f2:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r2 = r71.readInt()
                if (r2 == 0) goto L_0x0604
                r1 = r12
            L_0x0604:
                int r2 = r11.sreSetMdmn(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x060f:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                java.lang.String r0 = r69.sreGetVersion()
                r72.writeNoException()
                r15.writeString(r0)
                return r12
            L_0x0621:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                r69.sreInitialize()
                r72.writeNoException()
                return r12
            L_0x062f:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r11.steSetSessionId(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x0649:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r11.steSetNetId(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x0663:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r2 = r71.readInt()
                if (r2 == 0) goto L_0x0675
                r1 = r12
            L_0x0675:
                int r2 = r11.steSetCallOptions(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x0680:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r7 = r71.readInt()
                int r8 = r71.readInt()
                int r9 = r71.readInt()
                int r10 = r71.readInt()
                int r16 = r71.readInt()
                int[] r17 = r71.createIntArray()
                r0 = r69
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r10
                r5 = r16
                r6 = r17
                int r0 = r0.steSetRtcpXr(r1, r2, r3, r4, r5, r6)
                r72.writeNoException()
                r15.writeInt(r0)
                return r12
            L_0x06b4:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                long r1 = r71.readLong()
                int r3 = r11.steSetRtcpTimeout(r0, r1)
                r72.writeNoException()
                r15.writeInt(r3)
                return r12
            L_0x06ce:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                long r1 = r71.readLong()
                int r3 = r11.steSetRtpTimeout(r0, r1)
                r72.writeNoException()
                r15.writeInt(r3)
                return r12
            L_0x06e8:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r11.steSetRtcpOnCall(r0, r1, r2)
                r72.writeNoException()
                r15.writeInt(r3)
                return r12
            L_0x0706:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r6 = r71.readInt()
                int r7 = r71.readInt()
                int r8 = r71.readInt()
                byte[] r9 = r71.createByteArray()
                int r10 = r71.readInt()
                r0 = r69
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r10
                int r0 = r0.steEnableSRTP(r1, r2, r3, r4, r5)
                r72.writeNoException()
                r15.writeInt(r0)
                return r12
            L_0x0733:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                java.lang.String r1 = r71.readString()
                int r2 = r71.readInt()
                int r3 = r11.steSendText(r0, r1, r2)
                r72.writeNoException()
                r15.writeInt(r3)
                return r12
            L_0x0751:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r11.steDeleteChannel(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x0767:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r11.steModifyChannel(r0, r1)
                r72.writeNoException()
                r15.writeInt(r2)
                return r12
            L_0x0781:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r11.steStopChannel(r0)
                r72.writeNoException()
                r15.writeInt(r1)
                return r12
            L_0x0797:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r9 = r71.readInt()
                int r10 = r71.readInt()
                java.lang.String r16 = r71.readString()
                int r17 = r71.readInt()
                java.lang.String r18 = r71.readString()
                int r19 = r71.readInt()
                int r20 = r71.readInt()
                int r21 = r71.readInt()
                r0 = r69
                r1 = r9
                r2 = r10
                r3 = r16
                r4 = r17
                r5 = r18
                r6 = r19
                r7 = r20
                r8 = r21
                int r0 = r0.steUpdateChannel(r1, r2, r3, r4, r5, r6, r7, r8)
                r72.writeNoException()
                r15.writeInt(r0)
                return r12
            L_0x07d9:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r0 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r71.readInt()
                if (r3 == 0) goto L_0x07ef
                r1 = r12
            L_0x07ef:
                int r3 = r11.steStartChannel(r0, r2, r1)
                r72.writeNoException()
                r15.writeInt(r3)
                return r12
            L_0x07fa:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r16 = r71.readInt()
                java.lang.String r17 = r71.readString()
                int r18 = r71.readInt()
                java.lang.String r19 = r71.readString()
                int r20 = r71.readInt()
                int r21 = r71.readInt()
                int r22 = r71.readInt()
                java.lang.String r23 = r71.readString()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0829
                r9 = r12
                goto L_0x082a
            L_0x0829:
                r9 = r1
            L_0x082a:
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0832
                r10 = r12
                goto L_0x0833
            L_0x0832:
                r10 = r1
            L_0x0833:
                r0 = r69
                r1 = r16
                r2 = r17
                r3 = r18
                r4 = r19
                r5 = r20
                r6 = r21
                r7 = r22
                r8 = r23
                int r0 = r0.steCreateChannel(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
                r72.writeNoException()
                r15.writeInt(r0)
                return r12
            L_0x0850:
                r15 = r13
                r13 = r12
                r12 = r11
                r11 = r0
                r14.enforceInterface(r13)
                int r31 = r71.readInt()
                java.lang.String r32 = r71.readString()
                int r33 = r71.readInt()
                int r34 = r71.readInt()
                int r35 = r71.readInt()
                int r36 = r71.readInt()
                int r37 = r71.readInt()
                int r38 = r71.readInt()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x087f
                r9 = r12
                goto L_0x0880
            L_0x087f:
                r9 = r1
            L_0x0880:
                int r39 = r71.readInt()
                r10 = r39
                int r40 = r71.readInt()
                r8 = r11
                r11 = r40
                int r41 = r71.readInt()
                r7 = r12
                r12 = r41
                int r42 = r71.readInt()
                r6 = r13
                r13 = r42
                int r43 = r71.readInt()
                r5 = r14
                r14 = r43
                int r0 = r71.readInt()
                char r4 = (char) r0
                r3 = r15
                r15 = r4
                int r0 = r71.readInt()
                char r2 = (char) r0
                r16 = r2
                int r0 = r71.readInt()
                char r1 = (char) r0
                r17 = r1
                int r0 = r71.readInt()
                char r0 = (char) r0
                r18 = r0
                int r7 = r71.readInt()
                char r7 = (char) r7
                r19 = r7
                r44 = r0
                int r0 = r71.readInt()
                char r0 = (char) r0
                r20 = r0
                int r45 = r71.readInt()
                r21 = r45
                int r46 = r71.readInt()
                r22 = r46
                java.lang.String r47 = r71.readString()
                r23 = r47
                java.lang.String r48 = r71.readString()
                r24 = r48
                java.lang.String r49 = r71.readString()
                r25 = r49
                java.lang.String r50 = r71.readString()
                r26 = r50
                java.lang.String r51 = r71.readString()
                r27 = r51
                java.lang.String r52 = r71.readString()
                r28 = r52
                java.lang.String r53 = r71.readString()
                r29 = r53
                java.lang.String r54 = r71.readString()
                r30 = r54
                r55 = r0
                r0 = r69
                r56 = r1
                r1 = r31
                r57 = r2
                r2 = r32
                r3 = r33
                r58 = r4
                r4 = r34
                r5 = r35
                r64 = r6
                r6 = r36
                r59 = r7
                r7 = r37
                r8 = r38
                int r0 = r0.steSetCodecInfo(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30)
                r72.writeNoException()
                r14 = r72
                r14.writeInt(r0)
                r15 = 1
                return r15
            L_0x0936:
                r15 = r11
                r14 = r13
                r13 = r71
                r13.enforceInterface(r12)
                r69.steInitialize()
                r72.writeNoException()
                return r15
            L_0x0944:
                r15 = r11
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                r11 = r69
                java.lang.String r1 = r11.sveGetCodecCapacity(r0)
                r72.writeNoException()
                r14.writeString(r1)
                return r15
            L_0x095e:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                com.sec.sve.TimeInfo r2 = r11.sveGetRtcpTimeInfo(r0)
                r72.writeNoException()
                if (r2 == 0) goto L_0x097c
                r14.writeInt(r15)
                r2.writeToParcel(r14, r15)
                goto L_0x097f
            L_0x097c:
                r14.writeInt(r1)
            L_0x097f:
                return r15
            L_0x0980:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                java.lang.String r3 = r71.readString()
                int r4 = r11.sveSendGeneralEvent(r0, r1, r2, r3)
                r72.writeNoException()
                r14.writeInt(r4)
                return r15
            L_0x09a5:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r71.readInt()
                int r4 = r11.sveSetNetworkQoS(r0, r1, r2, r3)
                r72.writeNoException()
                r14.writeInt(r4)
                return r15
            L_0x09ca:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r11.sveSetVideoPlayDelay(r0, r1)
                r72.writeNoException()
                r14.writeInt(r2)
                return r15
            L_0x09e7:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                com.sec.sve.TimeInfo r2 = r11.sveGetLastPlayedVideoTime(r0)
                r72.writeNoException()
                if (r2 == 0) goto L_0x0a05
                r14.writeInt(r15)
                r2.writeToParcel(r14, r15)
                goto L_0x0a08
            L_0x0a05:
                r14.writeInt(r1)
            L_0x0a08:
                return r15
            L_0x0a09:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                int r2 = r71.readInt()
                if (r2 == 0) goto L_0x0a1f
                r2 = r15
                goto L_0x0a20
            L_0x0a1f:
                r2 = r1
            L_0x0a20:
                int r3 = r71.readInt()
                if (r3 == 0) goto L_0x0a27
                r1 = r15
            L_0x0a27:
                int r3 = r11.sveSetHeldInfo(r0, r2, r1)
                r72.writeNoException()
                r14.writeInt(r3)
                return r15
            L_0x0a32:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                r11.sveRestartEmoji(r0)
                r72.writeNoException()
                return r15
            L_0x0a47:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                r11.sveStopEmoji(r0)
                r72.writeNoException()
                return r15
            L_0x0a5c:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                java.lang.String r1 = r71.readString()
                r11.sveStartEmoji(r0, r1)
                r72.writeNoException()
                return r15
            L_0x0a75:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r69.sveStopCamera()
                r72.writeNoException()
                r14.writeInt(r0)
                return r15
            L_0x0a8a:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r11.sveStartCamera(r0, r1)
                r72.writeNoException()
                r14.writeInt(r2)
                return r15
            L_0x0aa7:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r9 = r71.readInt()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0abd
                r2 = r15
                goto L_0x0abe
            L_0x0abd:
                r2 = r1
            L_0x0abe:
                int r10 = r71.readInt()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0aca
                r4 = r15
                goto L_0x0acb
            L_0x0aca:
                r4 = r1
            L_0x0acb:
                int r16 = r71.readInt()
                int r17 = r71.readInt()
                int r18 = r71.readInt()
                int r19 = r71.readInt()
                r0 = r69
                r1 = r9
                r3 = r10
                r5 = r16
                r6 = r17
                r7 = r18
                r8 = r19
                int r0 = r0.sveSetMediaConfig(r1, r2, r3, r4, r5, r6, r7, r8)
                r72.writeNoException()
                r14.writeInt(r0)
                return r15
            L_0x0af2:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r16 = r71.readInt()
                int r17 = r71.readInt()
                int r18 = r71.readInt()
                int r19 = r71.readInt()
                int r0 = r71.readInt()
                char r10 = (char) r0
                int r20 = r71.readInt()
                byte[] r21 = r71.createByteArray()
                int r22 = r71.readInt()
                byte[] r23 = r71.createByteArray()
                int r24 = r71.readInt()
                r0 = r69
                r1 = r16
                r2 = r17
                r3 = r18
                r4 = r19
                r5 = r10
                r6 = r20
                r7 = r21
                r8 = r22
                r9 = r23
                r25 = r10
                r10 = r24
                int r0 = r0.sveSetGcmSrtpParams(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
                r72.writeNoException()
                r14.writeInt(r0)
                return r15
            L_0x0b47:
                r15 = r11
                r11 = r0
                r68 = r14
                r14 = r13
                r13 = r68
                r13.enforceInterface(r12)
                int r16 = r71.readInt()
                java.lang.String r17 = r71.readString()
                byte[] r18 = r71.createByteArray()
                int r19 = r71.readInt()
                int r20 = r71.readInt()
                int r21 = r71.readInt()
                int r22 = r71.readInt()
                java.lang.String r23 = r71.readString()
                byte[] r24 = r71.createByteArray()
                int r25 = r71.readInt()
                int r26 = r71.readInt()
                int r27 = r71.readInt()
                int r28 = r71.readInt()
                r0 = r69
                r1 = r16
                r2 = r17
                r3 = r18
                r4 = r19
                r5 = r20
                r6 = r21
                r7 = r22
                r8 = r23
                r9 = r24
                r10 = r25
                r11 = r26
                r65 = r12
                r12 = r27
                r13 = r28
                int r0 = r0.sveSetSRTPParams(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
                r72.writeNoException()
                r14.writeInt(r0)
                return r15
            L_0x0bae:
                r15 = r11
                r65 = r12
                r14 = r13
                r12 = r71
                r11 = r65
                r12.enforceInterface(r11)
                int r27 = r71.readInt()
                int r28 = r71.readInt()
                int r29 = r71.readInt()
                int r30 = r71.readInt()
                int r31 = r71.readInt()
                int r32 = r71.readInt()
                java.lang.String r33 = r71.readString()
                int r34 = r71.readInt()
                int r35 = r71.readInt()
                int r36 = r71.readInt()
                int r37 = r71.readInt()
                int r38 = r71.readInt()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0bf1
                r13 = r15
                goto L_0x0bf2
            L_0x0bf1:
                r13 = r1
            L_0x0bf2:
                int r39 = r71.readInt()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0bfd
                r1 = r15
            L_0x0bfd:
                r10 = r15
                r15 = r1
                int r40 = r71.readInt()
                r16 = r40
                int r41 = r71.readInt()
                r17 = r41
                int r42 = r71.readInt()
                r18 = r42
                int r43 = r71.readInt()
                r19 = r43
                int r44 = r71.readInt()
                r20 = r44
                byte[] r45 = r71.createByteArray()
                r21 = r45
                byte[] r46 = r71.createByteArray()
                r22 = r46
                byte[] r47 = r71.createByteArray()
                r23 = r47
                int r48 = r71.readInt()
                r24 = r48
                int r49 = r71.readInt()
                r25 = r49
                int r50 = r71.readInt()
                r26 = r50
                r0 = r69
                r1 = r27
                r2 = r28
                r3 = r29
                r4 = r30
                r5 = r31
                r6 = r32
                r7 = r33
                r8 = r34
                r9 = r35
                r10 = r36
                r66 = r11
                r11 = r37
                r12 = r38
                r14 = r39
                int r0 = r0.sveSetCodecInfo(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26)
                r72.writeNoException()
                r14 = r72
                r14.writeInt(r0)
                r12 = 1
                return r12
            L_0x0c6d:
                r66 = r12
                r14 = r13
                r12 = r11
                r15 = r71
                r13 = r66
                r15.enforceInterface(r13)
                int r9 = r71.readInt()
                java.lang.String r10 = r71.readString()
                int r11 = r71.readInt()
                java.lang.String r16 = r71.readString()
                int r17 = r71.readInt()
                int r18 = r71.readInt()
                int r19 = r71.readInt()
                int r20 = r71.readInt()
                r0 = r69
                r1 = r9
                r2 = r10
                r3 = r11
                r4 = r16
                r5 = r17
                r6 = r18
                r7 = r19
                r8 = r20
                int r0 = r0.sveSetConnection(r1, r2, r3, r4, r5, r6, r7, r8)
                r72.writeNoException()
                r14.writeInt(r0)
                return r12
            L_0x0cb2:
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                r9 = r69
                int r1 = r9.sveStopChannel(r0)
                r72.writeNoException()
                r14.writeInt(r1)
                return r12
            L_0x0cca:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r9.sveStartChannel(r0, r1, r2)
                r72.writeNoException()
                r14.writeInt(r3)
                return r12
            L_0x0ce9:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r69.sveCreateChannel()
                r72.writeNoException()
                r14.writeInt(r0)
                return r12
            L_0x0cfc:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r9.saeSetAudioPath(r0, r1)
                r72.writeNoException()
                r14.writeInt(r2)
                return r12
            L_0x0d17:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r9.saeGetAudioRxTrackId(r0)
                r72.writeNoException()
                r14.writeInt(r1)
                return r12
            L_0x0d2e:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                byte[] r0 = r71.createByteArray()
                int r1 = r71.readInt()
                int r2 = r9.saeGetVersion(r0, r1)
                r72.writeNoException()
                r14.writeInt(r2)
                return r12
            L_0x0d49:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r9.saeSetTOS(r0, r1)
                r72.writeNoException()
                r14.writeInt(r2)
                return r12
            L_0x0d64:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r9.saeSetVoicePlayDelay(r0, r1)
                r72.writeNoException()
                r14.writeInt(r2)
                return r12
            L_0x0d7f:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                com.sec.sve.TimeInfo r2 = r9.saeGetLastPlayedVoiceTime(r0)
                r72.writeNoException()
                if (r2 == 0) goto L_0x0d9b
                r14.writeInt(r12)
                r2.writeToParcel(r14, r12)
                goto L_0x0d9e
            L_0x0d9b:
                r14.writeInt(r1)
            L_0x0d9e:
                return r12
            L_0x0d9f:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r7 = r71.readInt()
                int r8 = r71.readInt()
                int r10 = r71.readInt()
                int r11 = r71.readInt()
                int r16 = r71.readInt()
                int[] r17 = r71.createIntArray()
                r0 = r69
                r1 = r7
                r2 = r8
                r3 = r10
                r4 = r11
                r5 = r16
                r6 = r17
                int r0 = r0.saeSetRtcpXr(r1, r2, r3, r4, r5, r6)
                r72.writeNoException()
                r14.writeInt(r0)
                return r12
            L_0x0dd4:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                long r1 = r71.readLong()
                int r3 = r9.saeSetRtcpTimeout(r0, r1)
                r72.writeNoException()
                r14.writeInt(r3)
                return r12
            L_0x0def:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                long r1 = r71.readLong()
                int r3 = r9.saeSetRtpTimeout(r0, r1)
                r72.writeNoException()
                r14.writeInt(r3)
                return r12
            L_0x0e0a:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r9.saeSetRtcpOnCall(r0, r1, r2)
                r72.writeNoException()
                r14.writeInt(r3)
                return r12
            L_0x0e29:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r6 = r71.readInt()
                int r7 = r71.readInt()
                int r8 = r71.readInt()
                byte[] r10 = r71.createByteArray()
                int r11 = r71.readInt()
                r0 = r69
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r10
                r5 = r11
                int r0 = r0.saeEnableSRTP(r1, r2, r3, r4, r5)
                r72.writeNoException()
                r14.writeInt(r0)
                return r12
            L_0x0e57:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r6 = r71.readInt()
                int r7 = r71.readInt()
                int r8 = r71.readInt()
                int r10 = r71.readInt()
                int r11 = r71.readInt()
                r0 = r69
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r10
                r5 = r11
                int r0 = r0.saeSetDtmfCodecInfo(r1, r2, r3, r4, r5)
                r72.writeNoException()
                r14.writeInt(r0)
                return r12
            L_0x0e85:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r71.readInt()
                int r4 = r9.saeHandleDtmf(r0, r1, r2, r3)
                r72.writeNoException()
                r14.writeInt(r4)
                return r12
            L_0x0ea8:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r9.saeDeleteChannel(r0)
                r72.writeNoException()
                r14.writeInt(r1)
                return r12
            L_0x0ebf:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r71.readInt()
                int r2 = r9.saeModifyChannel(r0, r1)
                r72.writeNoException()
                r14.writeInt(r2)
                return r12
            L_0x0eda:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r1 = r9.saeStopChannel(r0)
                r72.writeNoException()
                r14.writeInt(r1)
                return r12
            L_0x0ef1:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r10 = r71.readInt()
                int r11 = r71.readInt()
                java.lang.String r16 = r71.readString()
                int r17 = r71.readInt()
                java.lang.String r18 = r71.readString()
                int r19 = r71.readInt()
                int r20 = r71.readInt()
                int r21 = r71.readInt()
                r0 = r69
                r1 = r10
                r2 = r11
                r3 = r16
                r4 = r17
                r5 = r18
                r6 = r19
                r7 = r20
                r8 = r21
                int r0 = r0.saeUpdateChannel(r1, r2, r3, r4, r5, r6, r7, r8)
                r72.writeNoException()
                r14.writeInt(r0)
                return r12
            L_0x0f34:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r0 = r71.readInt()
                int r2 = r71.readInt()
                int r3 = r71.readInt()
                if (r3 == 0) goto L_0x0f4c
                r11 = r12
                goto L_0x0f4d
            L_0x0f4c:
                r11 = r1
            L_0x0f4d:
                r1 = r11
                int r3 = r9.saeStartChannel(r0, r2, r1)
                r72.writeNoException()
                r14.writeInt(r3)
                return r12
            L_0x0f59:
                r9 = r0
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r16 = r71.readInt()
                int r17 = r71.readInt()
                java.lang.String r18 = r71.readString()
                int r19 = r71.readInt()
                java.lang.String r20 = r71.readString()
                int r21 = r71.readInt()
                int r22 = r71.readInt()
                int r23 = r71.readInt()
                java.lang.String r24 = r71.readString()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0f8d
                r10 = r12
                goto L_0x0f8e
            L_0x0f8d:
                r10 = r1
            L_0x0f8e:
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0f96
                r11 = r12
                goto L_0x0f97
            L_0x0f96:
                r11 = r1
            L_0x0f97:
                r0 = r69
                r1 = r16
                r2 = r17
                r3 = r18
                r4 = r19
                r5 = r20
                r6 = r21
                r7 = r22
                r8 = r23
                r9 = r24
                int r0 = r0.saeCreateChannel(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r72.writeNoException()
                r14.writeInt(r0)
                return r12
            L_0x0fb6:
                r15 = r14
                r14 = r13
                r13 = r12
                r12 = r11
                r15.enforceInterface(r13)
                int r31 = r71.readInt()
                java.lang.String r32 = r71.readString()
                int r33 = r71.readInt()
                int r34 = r71.readInt()
                int r35 = r71.readInt()
                int r36 = r71.readInt()
                int r37 = r71.readInt()
                int r38 = r71.readInt()
                int r0 = r71.readInt()
                if (r0 == 0) goto L_0x0fe5
                r9 = r12
                goto L_0x0fe6
            L_0x0fe5:
                r9 = r1
            L_0x0fe6:
                int r39 = r71.readInt()
                r10 = r39
                int r40 = r71.readInt()
                r11 = r40
                int r41 = r71.readInt()
                r42 = r12
                r12 = r41
                int r43 = r71.readInt()
                r8 = r13
                r13 = r43
                int r44 = r71.readInt()
                r7 = r14
                r14 = r44
                int r0 = r71.readInt()
                char r6 = (char) r0
                r5 = r15
                r15 = r6
                int r0 = r71.readInt()
                char r4 = (char) r0
                r16 = r4
                int r0 = r71.readInt()
                char r3 = (char) r0
                r17 = r3
                int r0 = r71.readInt()
                char r2 = (char) r0
                r18 = r2
                int r0 = r71.readInt()
                char r1 = (char) r0
                r19 = r1
                int r0 = r71.readInt()
                char r0 = (char) r0
                r20 = r0
                int r45 = r71.readInt()
                r21 = r45
                int r46 = r71.readInt()
                r22 = r46
                java.lang.String r47 = r71.readString()
                r23 = r47
                java.lang.String r48 = r71.readString()
                r24 = r48
                java.lang.String r49 = r71.readString()
                r25 = r49
                java.lang.String r50 = r71.readString()
                r26 = r50
                java.lang.String r51 = r71.readString()
                r27 = r51
                java.lang.String r52 = r71.readString()
                r28 = r52
                java.lang.String r53 = r71.readString()
                r29 = r53
                java.lang.String r54 = r71.readString()
                r30 = r54
                r55 = r0
                r0 = r69
                r56 = r1
                r1 = r31
                r57 = r2
                r2 = r32
                r58 = r3
                r3 = r33
                r59 = r4
                r4 = r34
                r5 = r35
                r60 = r6
                r6 = r36
                r7 = r37
                r67 = r8
                r8 = r38
                int r0 = r0.saeSetCodecInfo(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30)
                r72.writeNoException()
                r2 = r72
                r2.writeInt(r0)
                return r42
            L_0x109b:
                r42 = r11
                r67 = r12
                r2 = r13
                r0 = r71
                r3 = r67
                r0.enforceInterface(r3)
                r69.saeTerminate()
                r72.writeNoException()
                return r42
            L_0x10ae:
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                int r1 = r71.readInt()
                int r4 = r71.readInt()
                int r5 = r71.readInt()
                r6 = r69
                r6.saeInitialize(r1, r4, r5)
                r72.writeNoException()
                return r42
            L_0x10cb:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                int r1 = r71.readInt()
                if (r1 == 0) goto L_0x10e3
                android.os.Parcelable$Creator r1 = android.net.Network.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r0)
                android.net.Network r1 = (android.net.Network) r1
                goto L_0x10e4
            L_0x10e3:
                r1 = 0
            L_0x10e4:
                r6.bindToNetwork(r1)
                r72.writeNoException()
                return r42
            L_0x10eb:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                int r1 = r71.readInt()
                int r4 = r71.readInt()
                r6.setPreviewResolution(r1, r4)
                r72.writeNoException()
                return r42
            L_0x1103:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                int r1 = r71.readInt()
                r6.setCameraEffect(r1)
                r72.writeNoException()
                return r42
            L_0x1117:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                int r4 = r71.readInt()
                int r5 = r71.readInt()
                if (r5 == 0) goto L_0x112d
                r11 = r42
                goto L_0x112e
            L_0x112d:
                r11 = r1
            L_0x112e:
                r1 = r11
                java.lang.String r5 = r71.readString()
                java.lang.String r7 = r71.readString()
                r6.sendStillImage(r4, r1, r5, r7)
                r72.writeNoException()
                return r42
            L_0x113e:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                r69.switchCamera()
                r72.writeNoException()
                return r42
            L_0x114e:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                float r1 = r71.readFloat()
                r6.setZoom(r1)
                r72.writeNoException()
                return r42
            L_0x1162:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                int r1 = r71.readInt()
                r6.setOrientation(r1)
                r72.writeNoException()
                return r42
            L_0x1176:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                int r1 = r71.readInt()
                if (r1 == 0) goto L_0x118e
                android.os.Parcelable$Creator r1 = android.view.Surface.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r0)
                android.view.Surface r1 = (android.view.Surface) r1
                goto L_0x118f
            L_0x118e:
                r1 = 0
            L_0x118f:
                int r4 = r71.readInt()
                r6.setDisplaySurface(r1, r4)
                r72.writeNoException()
                return r42
            L_0x119a:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                int r1 = r71.readInt()
                if (r1 == 0) goto L_0x11b2
                android.os.Parcelable$Creator r1 = android.view.Surface.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r0)
                android.view.Surface r1 = (android.view.Surface) r1
                goto L_0x11b3
            L_0x11b2:
                r1 = 0
            L_0x11b3:
                int r4 = r71.readInt()
                r6.setPreviewSurface(r1, r4)
                r72.writeNoException()
                return r42
            L_0x11be:
                r6 = r0
                r42 = r11
                r3 = r12
                r2 = r13
                r0 = r14
                r0.enforceInterface(r3)
                r69.onDestroy()
                r72.writeNoException()
                return r42
            L_0x11ce:
                r42 = r11
                r3 = r12
                r2 = r13
                r2.writeString(r3)
                return r42
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.sve.ISecVideoEngineService.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }

        private static class Proxy implements ISecVideoEngineService {
            public static ISecVideoEngineService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onDestroy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDestroy();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPreviewSurface(Surface surface, int color) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(color);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPreviewSurface(surface, color);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDisplaySurface(Surface surface, int color) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(color);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDisplaySurface(surface, color);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOrientation(int orientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(orientation);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOrientation(orientation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setZoom(float value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(value);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setZoom(value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void switchCamera() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().switchCamera();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendStillImage(int channel, boolean enable, String filePath, String frameSize) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeString(filePath);
                    _data.writeString(frameSize);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendStillImage(channel, enable, filePath, frameSize);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCameraEffect(int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCameraEffect(value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPreviewResolution(int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPreviewResolution(width, height);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void bindToNetwork(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bindToNetwork(network);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void saeInitialize(int convertedMno, int dtmfMode, int sas) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(convertedMno);
                    _data.writeInt(dtmfMode);
                    _data.writeInt(sas);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().saeInitialize(convertedMno, dtmfMode, sas);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void saeTerminate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().saeTerminate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetCodecInfo(int channel, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeString(name);
                    _data.writeInt(type);
                    _data.writeInt(rx_type);
                    _data.writeInt(freq);
                    _data.writeInt(bitrate);
                    _data.writeInt(ptime);
                    _data.writeInt(maxptime);
                    _data.writeInt(octectAligned ? 1 : 0);
                    _data.writeInt(mode_set);
                    _data.writeInt(nchannel);
                    _data.writeInt(dtxEnable);
                    _data.writeInt(red_level);
                    _data.writeInt(red_pt);
                    _data.writeInt(dtx);
                    _data.writeInt(dtxRecv);
                    _data.writeInt(hfOnly);
                    _data.writeInt(evsModeSwitch);
                    _data.writeInt(chSend);
                    _data.writeInt(chRecv);
                    _data.writeInt(chAwareRecv);
                    _data.writeInt(cmr);
                    _data.writeString(brSendMin);
                    _data.writeString(brSendMax);
                    _data.writeString(brRecvMin);
                    _data.writeString(brRecvMax);
                    _data.writeString(sendBwRange);
                    _data.writeString(recvBwRange);
                    _data.writeString(defaultBr);
                    _data.writeString(defaultBw);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeSetCodecInfo(channel, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, chAwareRecv, cmr, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeCreateChannel(int channel, int mno, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(channel);
                        _data.writeInt(mno);
                        _data.writeString(localIp);
                        _data.writeInt(localPort);
                        _data.writeString(remoteIp);
                        _data.writeInt(remotePort);
                        _data.writeInt(localRTCPPort);
                        _data.writeInt(remoteRTCPPort);
                        _data.writeString(pdn);
                        int i = 1;
                        _data.writeInt(xqEnabled ? 1 : 0);
                        if (!ttyChannel) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int saeCreateChannel = Stub.getDefaultImpl().saeCreateChannel(channel, mno, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
                        _reply.recycle();
                        _data.recycle();
                        return saeCreateChannel;
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    int i2 = channel;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int saeStartChannel(int channel, int direction, boolean enableIpv6) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    _data.writeInt(enableIpv6 ? 1 : 0);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeStartChannel(channel, direction, enableIpv6);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeUpdateChannel(int channel, int dir, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(channel);
                    } catch (Throwable th) {
                        th = th;
                        int i = dir;
                        String str = localIp;
                        int i2 = localPort;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(dir);
                    } catch (Throwable th2) {
                        th = th2;
                        String str2 = localIp;
                        int i22 = localPort;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(localIp);
                        try {
                            _data.writeInt(localPort);
                            _data.writeString(remoteIp);
                            _data.writeInt(remotePort);
                            _data.writeInt(localRTCPPort);
                            _data.writeInt(remoteRTCPPort);
                            if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int saeUpdateChannel = Stub.getDefaultImpl().saeUpdateChannel(channel, dir, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort);
                            _reply.recycle();
                            _data.recycle();
                            return saeUpdateChannel;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i222 = localPort;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    int i3 = channel;
                    int i4 = dir;
                    String str22 = localIp;
                    int i2222 = localPort;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int saeStopChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeStopChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeModifyChannel(int channel, int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeModifyChannel(channel, direction);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeDeleteChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeDeleteChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeHandleDtmf(int channel, int code, int mode, int operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(code);
                    _data.writeInt(mode);
                    _data.writeInt(operation);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeHandleDtmf(channel, code, mode, operation);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetDtmfCodecInfo(int channel, int type, int rxtype, int bitrate, int inband) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(type);
                    _data.writeInt(rxtype);
                    _data.writeInt(bitrate);
                    _data.writeInt(inband);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeSetDtmfCodecInfo(channel, type, rxtype, bitrate, inband);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeEnableSRTP(int channel, int direction, int profile, byte[] key, int keylen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    _data.writeInt(profile);
                    _data.writeByteArray(key);
                    _data.writeInt(keylen);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeEnableSRTP(channel, direction, profile, key, keylen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetRtcpOnCall(int channel, int rr, int rs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(rr);
                    _data.writeInt(rs);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeSetRtcpOnCall(channel, rr, rs);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetRtpTimeout(int channel, long sec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeLong(sec);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeSetRtpTimeout(channel, sec);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetRtcpTimeout(int channel, long sec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeLong(sec);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeSetRtcpTimeout(channel, sec);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetRtcpXr(int channel, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(channel);
                    } catch (Throwable th) {
                        th = th;
                        int i = flag;
                        int i2 = blocks;
                        int i3 = statflags;
                        int i4 = rttmode;
                        int[] iArr = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flag);
                        try {
                            _data.writeInt(blocks);
                            try {
                                _data.writeInt(statflags);
                            } catch (Throwable th2) {
                                th = th2;
                                int i42 = rttmode;
                                int[] iArr2 = maxsizesInt;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i32 = statflags;
                            int i422 = rttmode;
                            int[] iArr22 = maxsizesInt;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i22 = blocks;
                        int i322 = statflags;
                        int i4222 = rttmode;
                        int[] iArr222 = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(rttmode);
                        try {
                            _data.writeIntArray(maxsizesInt);
                            if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int saeSetRtcpXr = Stub.getDefaultImpl().saeSetRtcpXr(channel, flag, blocks, statflags, rttmode, maxsizesInt);
                            _reply.recycle();
                            _data.recycle();
                            return saeSetRtcpXr;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int[] iArr2222 = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    int i5 = channel;
                    int i6 = flag;
                    int i222 = blocks;
                    int i3222 = statflags;
                    int i42222 = rttmode;
                    int[] iArr22222 = maxsizesInt;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public TimeInfo saeGetLastPlayedVoiceTime(int channel) throws RemoteException {
                TimeInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeGetLastPlayedVoiceTime(channel);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = TimeInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetVoicePlayDelay(int channel, int delayTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(delayTime);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeSetVoicePlayDelay(channel, delayTime);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetTOS(int channel, int tos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(tos);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeSetTOS(channel, tos);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeGetVersion(byte[] version, int bufflen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(version);
                    _data.writeInt(bufflen);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeGetVersion(version, bufflen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeGetAudioRxTrackId(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeGetAudioRxTrackId(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeSetAudioPath(int dir_in, int dir_out) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dir_in);
                    _data.writeInt(dir_out);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeSetAudioPath(dir_in, dir_out);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveCreateChannel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveCreateChannel();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveStartChannel(int channel, int oldDirection, int newDirection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(oldDirection);
                    _data.writeInt(newDirection);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveStartChannel(channel, oldDirection, newDirection);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveStopChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveStopChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveSetConnection(int channel, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, int crbtType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(channel);
                    } catch (Throwable th) {
                        th = th;
                        String str = localIp;
                        int i = localPort;
                        String str2 = remoteIp;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(localIp);
                    } catch (Throwable th2) {
                        th = th2;
                        int i2 = localPort;
                        String str22 = remoteIp;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(localPort);
                        try {
                            _data.writeString(remoteIp);
                            _data.writeInt(remotePort);
                            _data.writeInt(localRTCPPort);
                            _data.writeInt(remoteRTCPPort);
                            _data.writeInt(crbtType);
                            if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sveSetConnection = Stub.getDefaultImpl().sveSetConnection(channel, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, crbtType);
                            _reply.recycle();
                            _data.recycle();
                            return sveSetConnection;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        String str222 = remoteIp;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    int i3 = channel;
                    String str3 = localIp;
                    int i22 = localPort;
                    String str2222 = remoteIp;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int sveSetCodecInfo(int channel, int as, int rs, int rr, int recvCodecPT, int sendCodecPT, String name, int dir, int width, int height, int frameRate, int maxBitrate, boolean enableAVPF, int supportAVPFType, boolean enableOrientation, int CVOGranularity, int H264Profile, int H264Level, int H264ConstraintInfo, int H264PackMode, byte[] sps, byte[] pps, byte[] vps, int spsLen, int ppsLen, int vpsLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(as);
                    _data.writeInt(rs);
                    _data.writeInt(rr);
                    _data.writeInt(recvCodecPT);
                    _data.writeInt(sendCodecPT);
                    _data.writeString(name);
                    _data.writeInt(dir);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(frameRate);
                    _data.writeInt(maxBitrate);
                    _data.writeInt(enableAVPF ? 1 : 0);
                    _data.writeInt(supportAVPFType);
                    _data.writeInt(enableOrientation ? 1 : 0);
                    _data.writeInt(CVOGranularity);
                    _data.writeInt(H264Profile);
                    _data.writeInt(H264Level);
                    _data.writeInt(H264ConstraintInfo);
                    _data.writeInt(H264PackMode);
                    _data.writeByteArray(sps);
                    _data.writeByteArray(pps);
                    _data.writeByteArray(vps);
                    _data.writeInt(spsLen);
                    _data.writeInt(ppsLen);
                    _data.writeInt(vpsLen);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveSetCodecInfo(channel, as, rs, rr, recvCodecPT, sendCodecPT, name, dir, width, height, frameRate, maxBitrate, enableAVPF, supportAVPFType, enableOrientation, CVOGranularity, H264Profile, H264Level, H264ConstraintInfo, H264PackMode, sps, pps, vps, spsLen, ppsLen, vpsLen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveSetSRTPParams(int sessionId, String offerSuite, byte[] aucTagKeyLocal, int sendKeySize, int ucTagKeyLenLocal, int uiTimetoLiveLocal, int uiMKILocal, String answerSuite, byte[] aucTagKeyRemote, int recvKeySize, int ucTagKeyLenRemote, int uiTimetoLiveRemote, int uiMKIRemote) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(offerSuite);
                    _data.writeByteArray(aucTagKeyLocal);
                    _data.writeInt(sendKeySize);
                    _data.writeInt(ucTagKeyLenLocal);
                    _data.writeInt(uiTimetoLiveLocal);
                    _data.writeInt(uiMKILocal);
                    _data.writeString(answerSuite);
                    _data.writeByteArray(aucTagKeyRemote);
                    _data.writeInt(recvKeySize);
                    _data.writeInt(ucTagKeyLenRemote);
                    _data.writeInt(uiTimetoLiveRemote);
                    _data.writeInt(uiMKIRemote);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveSetSRTPParams(sessionId, offerSuite, aucTagKeyLocal, sendKeySize, ucTagKeyLenLocal, uiTimetoLiveLocal, uiMKILocal, answerSuite, aucTagKeyRemote, recvKeySize, ucTagKeyLenRemote, uiTimetoLiveRemote, uiMKIRemote);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveSetGcmSrtpParams(int sessionId, int srtpProfile, int keyId, int keytype, char csId, int csbIdValue, byte[] inkey, int inkeyLength, byte[] rand, int randLengthValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(sessionId);
                    } catch (Throwable th) {
                        th = th;
                        int i = srtpProfile;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(srtpProfile);
                        _data.writeInt(keyId);
                        _data.writeInt(keytype);
                        _data.writeInt(csId);
                        _data.writeInt(csbIdValue);
                        _data.writeByteArray(inkey);
                        _data.writeInt(inkeyLength);
                        _data.writeByteArray(rand);
                        _data.writeInt(randLengthValue);
                        if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int sveSetGcmSrtpParams = Stub.getDefaultImpl().sveSetGcmSrtpParams(sessionId, srtpProfile, keyId, keytype, csId, csbIdValue, inkey, inkeyLength, rand, randLengthValue);
                        _reply.recycle();
                        _data.recycle();
                        return sveSetGcmSrtpParams;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    int i2 = sessionId;
                    int i3 = srtpProfile;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int sveSetMediaConfig(int sessionId, boolean timeOutOnBoth, int rtpTimeout, boolean rtpKeepAlive, int rtcpTimeout, int mtuSize, int mno, int keepAliveInterval) throws RemoteException {
                int i;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(sessionId);
                        i = 1;
                        _data.writeInt(timeOutOnBoth ? 1 : 0);
                    } catch (Throwable th) {
                        th = th;
                        int i2 = rtpTimeout;
                        int i3 = rtcpTimeout;
                        int i4 = mtuSize;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(rtpTimeout);
                        if (!rtpKeepAlive) {
                            i = 0;
                        }
                        _data.writeInt(i);
                    } catch (Throwable th2) {
                        th = th2;
                        int i32 = rtcpTimeout;
                        int i42 = mtuSize;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(rtcpTimeout);
                        try {
                            _data.writeInt(mtuSize);
                            _data.writeInt(mno);
                            _data.writeInt(keepAliveInterval);
                            if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sveSetMediaConfig = Stub.getDefaultImpl().sveSetMediaConfig(sessionId, timeOutOnBoth, rtpTimeout, rtpKeepAlive, rtcpTimeout, mtuSize, mno, keepAliveInterval);
                            _reply.recycle();
                            _data.recycle();
                            return sveSetMediaConfig;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i422 = mtuSize;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    int i5 = sessionId;
                    int i22 = rtpTimeout;
                    int i322 = rtcpTimeout;
                    int i4222 = mtuSize;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int sveStartCamera(int sessionId, int cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(cameraId);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveStartCamera(sessionId, cameraId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveStopCamera() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveStopCamera();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sveStartEmoji(int sessionId, String effect) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(effect);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sveStartEmoji(sessionId, effect);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sveStopEmoji(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sveStopEmoji(sessionId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sveRestartEmoji(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sveRestartEmoji(sessionId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveSetHeldInfo(int sessionId, boolean isLocal, boolean isHeld) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    int i = 1;
                    _data.writeInt(isLocal ? 1 : 0);
                    if (!isHeld) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveSetHeldInfo(sessionId, isLocal, isHeld);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TimeInfo sveGetLastPlayedVideoTime(int sessionId) throws RemoteException {
                TimeInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveGetLastPlayedVideoTime(sessionId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = TimeInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveSetVideoPlayDelay(int sessionId, int delayTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(delayTime);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveSetVideoPlayDelay(sessionId, delayTime);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveSetNetworkQoS(int sessionId, int ul_bler, int dl_bler, int grant) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(ul_bler);
                    _data.writeInt(dl_bler);
                    _data.writeInt(grant);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveSetNetworkQoS(sessionId, ul_bler, dl_bler, grant);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveSendGeneralEvent(int event, int arg1, int arg2, String arg3) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeInt(arg1);
                    _data.writeInt(arg2);
                    _data.writeString(arg3);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveSendGeneralEvent(event, arg1, arg2, arg3);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TimeInfo sveGetRtcpTimeInfo(int sessionId) throws RemoteException {
                TimeInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveGetRtcpTimeInfo(sessionId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = TimeInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String sveGetCodecCapacity(int codecMaxLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(codecMaxLen);
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveGetCodecCapacity(codecMaxLen);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void steInitialize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().steInitialize();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steSetCodecInfo(int channel, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeString(name);
                    _data.writeInt(type);
                    _data.writeInt(rx_type);
                    _data.writeInt(freq);
                    _data.writeInt(bitrate);
                    _data.writeInt(ptime);
                    _data.writeInt(maxptime);
                    _data.writeInt(octectAligned ? 1 : 0);
                    _data.writeInt(mode_set);
                    _data.writeInt(nchannel);
                    _data.writeInt(dtxEnable);
                    _data.writeInt(red_level);
                    _data.writeInt(red_pt);
                    _data.writeInt(dtx);
                    _data.writeInt(dtxRecv);
                    _data.writeInt(hfOnly);
                    _data.writeInt(evsModeSwitch);
                    _data.writeInt(chSend);
                    _data.writeInt(chRecv);
                    _data.writeInt(chAwareRecv);
                    _data.writeInt(cmr);
                    _data.writeString(brSendMin);
                    _data.writeString(brSendMax);
                    _data.writeString(brRecvMin);
                    _data.writeString(brRecvMax);
                    _data.writeString(sendBwRange);
                    _data.writeString(recvBwRange);
                    _data.writeString(defaultBr);
                    _data.writeString(defaultBw);
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steSetCodecInfo(channel, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, chAwareRecv, cmr, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steCreateChannel(int mno, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(mno);
                    } catch (Throwable th) {
                        th = th;
                        String str = localIp;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(localIp);
                        _data.writeInt(localPort);
                        _data.writeString(remoteIp);
                        _data.writeInt(remotePort);
                        _data.writeInt(localRTCPPort);
                        _data.writeInt(remoteRTCPPort);
                        _data.writeString(pdn);
                        int i = 1;
                        _data.writeInt(xqEnabled ? 1 : 0);
                        if (!ttyChannel) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int steCreateChannel = Stub.getDefaultImpl().steCreateChannel(mno, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
                        _reply.recycle();
                        _data.recycle();
                        return steCreateChannel;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    int i2 = mno;
                    String str2 = localIp;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int steStartChannel(int channel, int direction, boolean enableIpv6) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    _data.writeInt(enableIpv6 ? 1 : 0);
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steStartChannel(channel, direction, enableIpv6);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steUpdateChannel(int channel, int dir, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(channel);
                    } catch (Throwable th) {
                        th = th;
                        int i = dir;
                        String str = localIp;
                        int i2 = localPort;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(dir);
                    } catch (Throwable th2) {
                        th = th2;
                        String str2 = localIp;
                        int i22 = localPort;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(localIp);
                        try {
                            _data.writeInt(localPort);
                            _data.writeString(remoteIp);
                            _data.writeInt(remotePort);
                            _data.writeInt(localRTCPPort);
                            _data.writeInt(remoteRTCPPort);
                            if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int steUpdateChannel = Stub.getDefaultImpl().steUpdateChannel(channel, dir, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort);
                            _reply.recycle();
                            _data.recycle();
                            return steUpdateChannel;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i222 = localPort;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    int i3 = channel;
                    int i4 = dir;
                    String str22 = localIp;
                    int i2222 = localPort;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int steStopChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steStopChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steModifyChannel(int channel, int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steModifyChannel(channel, direction);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steDeleteChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steDeleteChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steSendText(int channel, String text, int len) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeString(text);
                    _data.writeInt(len);
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steSendText(channel, text, len);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steEnableSRTP(int channel, int direction, int profile, byte[] key, int keylen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    _data.writeInt(profile);
                    _data.writeByteArray(key);
                    _data.writeInt(keylen);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steEnableSRTP(channel, direction, profile, key, keylen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steSetRtcpOnCall(int channel, int rr, int rs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(rr);
                    _data.writeInt(rs);
                    if (!this.mRemote.transact(63, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steSetRtcpOnCall(channel, rr, rs);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steSetRtpTimeout(int channel, long sec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeLong(sec);
                    if (!this.mRemote.transact(64, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steSetRtpTimeout(channel, sec);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steSetRtcpTimeout(int channel, long sec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeLong(sec);
                    if (!this.mRemote.transact(65, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steSetRtcpTimeout(channel, sec);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steSetRtcpXr(int channel, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(channel);
                    } catch (Throwable th) {
                        th = th;
                        int i = flag;
                        int i2 = blocks;
                        int i3 = statflags;
                        int i4 = rttmode;
                        int[] iArr = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flag);
                        try {
                            _data.writeInt(blocks);
                            try {
                                _data.writeInt(statflags);
                            } catch (Throwable th2) {
                                th = th2;
                                int i42 = rttmode;
                                int[] iArr2 = maxsizesInt;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i32 = statflags;
                            int i422 = rttmode;
                            int[] iArr22 = maxsizesInt;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i22 = blocks;
                        int i322 = statflags;
                        int i4222 = rttmode;
                        int[] iArr222 = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(rttmode);
                        try {
                            _data.writeIntArray(maxsizesInt);
                            if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int steSetRtcpXr = Stub.getDefaultImpl().steSetRtcpXr(channel, flag, blocks, statflags, rttmode, maxsizesInt);
                            _reply.recycle();
                            _data.recycle();
                            return steSetRtcpXr;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int[] iArr2222 = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    int i5 = channel;
                    int i6 = flag;
                    int i222 = blocks;
                    int i3222 = statflags;
                    int i42222 = rttmode;
                    int[] iArr22222 = maxsizesInt;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int steSetCallOptions(int channel, boolean isRtcpOnCall) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(isRtcpOnCall ? 1 : 0);
                    if (!this.mRemote.transact(67, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steSetCallOptions(channel, isRtcpOnCall);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steSetNetId(int channel, int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(netId);
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steSetNetId(channel, netId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int steSetSessionId(int channelId, int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channelId);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(69, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().steSetSessionId(channelId, sessionId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sreInitialize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(70, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sreInitialize();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String sreGetVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(71, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreGetVersion();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreSetMdmn(int sessionId, boolean isMdmn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(isMdmn ? 1 : 0);
                    if (!this.mRemote.transact(72, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreSetMdmn(sessionId, isMdmn);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sreGetMdmn(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    boolean z = false;
                    if (!this.mRemote.transact(73, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreGetMdmn(sessionId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreSetNetId(int sessionId, long netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeLong(netId);
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreSetNetId(sessionId, netId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreCreateStream(int phoneId, int sessionId, int mno, String localIp, int localPort, String remoteIp, int remotePort, boolean isIpv6, boolean isMdmn, int localRTCPPort, int remoteRTCPPort, String pdn, boolean xqEnabled, boolean ttyChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(sessionId);
                    _data.writeInt(mno);
                    _data.writeString(localIp);
                    _data.writeInt(localPort);
                    _data.writeString(remoteIp);
                    _data.writeInt(remotePort);
                    int i = 1;
                    _data.writeInt(isIpv6 ? 1 : 0);
                    _data.writeInt(isMdmn ? 1 : 0);
                    _data.writeInt(localRTCPPort);
                    _data.writeInt(remoteRTCPPort);
                    _data.writeString(pdn);
                    _data.writeInt(xqEnabled ? 1 : 0);
                    if (!ttyChannel) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreCreateStream(phoneId, sessionId, mno, localIp, localPort, remoteIp, remotePort, isIpv6, isMdmn, localRTCPPort, remoteRTCPPort, pdn, xqEnabled, ttyChannel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreStartStream(int sessionId, int oldDirection, int newDirection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(oldDirection);
                    _data.writeInt(newDirection);
                    if (!this.mRemote.transact(76, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreStartStream(sessionId, oldDirection, newDirection);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreDeleteStream(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(77, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreDeleteStream(sessionId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreUpdateStream(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(78, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreUpdateStream(sessionId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreCreateRelayChannel(int lhs_stream, int rhs_stream) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(lhs_stream);
                    _data.writeInt(rhs_stream);
                    if (!this.mRemote.transact(79, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreCreateRelayChannel(lhs_stream, rhs_stream);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreDeleteRelayChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(80, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreDeleteRelayChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreStartRelayChannel(int channel, int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    if (!this.mRemote.transact(81, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreStartRelayChannel(channel, direction);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreStopRelayChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(82, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreStopRelayChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreHoldRelayChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreHoldRelayChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreResumeRelayChannel(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreResumeRelayChannel(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreUpdateRelayChannel(int channel, int stream) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(stream);
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreUpdateRelayChannel(channel, stream);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreSetConnection(int sessionId, String localIp, int localPort, String remoteIp, int remotePort, int localRTCPPort, int remoteRTCPPort, int crbtType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(sessionId);
                    } catch (Throwable th) {
                        th = th;
                        String str = localIp;
                        int i = localPort;
                        String str2 = remoteIp;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(localIp);
                    } catch (Throwable th2) {
                        th = th2;
                        int i2 = localPort;
                        String str22 = remoteIp;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(localPort);
                        try {
                            _data.writeString(remoteIp);
                            _data.writeInt(remotePort);
                            _data.writeInt(localRTCPPort);
                            _data.writeInt(remoteRTCPPort);
                            _data.writeInt(crbtType);
                            if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sreSetConnection = Stub.getDefaultImpl().sreSetConnection(sessionId, localIp, localPort, remoteIp, remotePort, localRTCPPort, remoteRTCPPort, crbtType);
                            _reply.recycle();
                            _data.recycle();
                            return sreSetConnection;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        String str222 = remoteIp;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    int i3 = sessionId;
                    String str3 = localIp;
                    int i22 = localPort;
                    String str2222 = remoteIp;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int sreEnableSRTP(int sessionId, int direction, int profile, byte[] key, int keylen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(direction);
                    _data.writeInt(profile);
                    _data.writeByteArray(key);
                    _data.writeInt(keylen);
                    if (!this.mRemote.transact(87, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreEnableSRTP(sessionId, direction, profile, key, keylen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreSetRtcpOnCall(int sessionId, int rr, int rs, int rtpTimer, int rtcpTimer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(rr);
                    _data.writeInt(rs);
                    _data.writeInt(rtpTimer);
                    _data.writeInt(rtcpTimer);
                    if (!this.mRemote.transact(88, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreSetRtcpOnCall(sessionId, rr, rs, rtpTimer, rtcpTimer);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreSetRtpTimeout(int sessionId, int sec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(sec);
                    if (!this.mRemote.transact(89, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreSetRtpTimeout(sessionId, sec);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreSetRtcpTimeout(int sessionId, int sec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(sec);
                    if (!this.mRemote.transact(90, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreSetRtcpTimeout(sessionId, sec);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreSetRtcpXr(int sessionId, int flag, int blocks, int statflags, int rttmode, int[] maxsizesInt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(sessionId);
                    } catch (Throwable th) {
                        th = th;
                        int i = flag;
                        int i2 = blocks;
                        int i3 = statflags;
                        int i4 = rttmode;
                        int[] iArr = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flag);
                        try {
                            _data.writeInt(blocks);
                            try {
                                _data.writeInt(statflags);
                            } catch (Throwable th2) {
                                th = th2;
                                int i42 = rttmode;
                                int[] iArr2 = maxsizesInt;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i32 = statflags;
                            int i422 = rttmode;
                            int[] iArr22 = maxsizesInt;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i22 = blocks;
                        int i322 = statflags;
                        int i4222 = rttmode;
                        int[] iArr222 = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(rttmode);
                        try {
                            _data.writeIntArray(maxsizesInt);
                            if (this.mRemote.transact(91, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sreSetRtcpXr = Stub.getDefaultImpl().sreSetRtcpXr(sessionId, flag, blocks, statflags, rttmode, maxsizesInt);
                            _reply.recycle();
                            _data.recycle();
                            return sreSetRtcpXr;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int[] iArr2222 = maxsizesInt;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    int i5 = sessionId;
                    int i6 = flag;
                    int i222 = blocks;
                    int i3222 = statflags;
                    int i42222 = rttmode;
                    int[] iArr22222 = maxsizesInt;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int sreSetCodecInfo(int sessionId, String name, int type, int rx_type, int freq, int bitrate, int ptime, int maxptime, boolean octectAligned, int mode_set, int nchannel, int dtxEnable, int red_level, int red_pt, char dtx, char dtxRecv, char hfOnly, char evsModeSwitch, char chSend, char chRecv, int chAwareRecv, int cmr, String brSendMin, String brSendMax, String brRecvMin, String brRecvMax, String sendBwRange, String recvBwRange, String defaultBr, String defaultBw, int protocol) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(name);
                    _data.writeInt(type);
                    _data.writeInt(rx_type);
                    _data.writeInt(freq);
                    _data.writeInt(bitrate);
                    _data.writeInt(ptime);
                    _data.writeInt(maxptime);
                    _data.writeInt(octectAligned ? 1 : 0);
                    _data.writeInt(mode_set);
                    _data.writeInt(nchannel);
                    _data.writeInt(dtxEnable);
                    _data.writeInt(red_level);
                    _data.writeInt(red_pt);
                    _data.writeInt(dtx);
                    _data.writeInt(dtxRecv);
                    _data.writeInt(hfOnly);
                    _data.writeInt(evsModeSwitch);
                    _data.writeInt(chSend);
                    _data.writeInt(chRecv);
                    _data.writeInt(chAwareRecv);
                    _data.writeInt(cmr);
                    _data.writeString(brSendMin);
                    _data.writeString(brSendMax);
                    _data.writeString(brRecvMin);
                    _data.writeString(brRecvMax);
                    _data.writeString(sendBwRange);
                    _data.writeString(recvBwRange);
                    _data.writeString(defaultBr);
                    _data.writeString(defaultBw);
                    _data.writeInt(protocol);
                    if (!this.mRemote.transact(92, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreSetCodecInfo(sessionId, name, type, rx_type, freq, bitrate, ptime, maxptime, octectAligned, mode_set, nchannel, dtxEnable, red_level, red_pt, dtx, dtxRecv, hfOnly, evsModeSwitch, chSend, chRecv, chAwareRecv, cmr, brSendMin, brSendMax, brRecvMin, brRecvMax, sendBwRange, recvBwRange, defaultBr, defaultBw, protocol);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreSetDtmfCodecInfo(int phoneId, int sessionId, int type, int rxtype, int bitrate, int inband) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(phoneId);
                    } catch (Throwable th) {
                        th = th;
                        int i = sessionId;
                        int i2 = type;
                        int i3 = rxtype;
                        int i4 = bitrate;
                        int i5 = inband;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(sessionId);
                        try {
                            _data.writeInt(type);
                            try {
                                _data.writeInt(rxtype);
                            } catch (Throwable th2) {
                                th = th2;
                                int i42 = bitrate;
                                int i52 = inband;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i32 = rxtype;
                            int i422 = bitrate;
                            int i522 = inband;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i22 = type;
                        int i322 = rxtype;
                        int i4222 = bitrate;
                        int i5222 = inband;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(bitrate);
                        try {
                            _data.writeInt(inband);
                            if (this.mRemote.transact(93, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sreSetDtmfCodecInfo = Stub.getDefaultImpl().sreSetDtmfCodecInfo(phoneId, sessionId, type, rxtype, bitrate, inband);
                            _reply.recycle();
                            _data.recycle();
                            return sreSetDtmfCodecInfo;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int i52222 = inband;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    int i6 = phoneId;
                    int i7 = sessionId;
                    int i222 = type;
                    int i3222 = rxtype;
                    int i42222 = bitrate;
                    int i522222 = inband;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int sreStartRecording(int sessionId, int streamId, int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(streamId);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(94, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreStartRecording(sessionId, streamId, channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sreStopRecording(int sessionId, int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(95, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sreStopRecording(sessionId, channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSupportingCameraMotor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(96, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportingCameraMotor();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveRecorderCreate(int sessionId, String filename, int audioId, int audioSampleRate, String audioCodec, int videoId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(sessionId);
                    } catch (Throwable th) {
                        th = th;
                        String str = filename;
                        int i = audioId;
                        int i2 = audioSampleRate;
                        String str2 = audioCodec;
                        int i3 = videoId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(filename);
                        try {
                            _data.writeInt(audioId);
                            try {
                                _data.writeInt(audioSampleRate);
                            } catch (Throwable th2) {
                                th = th2;
                                String str22 = audioCodec;
                                int i32 = videoId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i22 = audioSampleRate;
                            String str222 = audioCodec;
                            int i322 = videoId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i4 = audioId;
                        int i222 = audioSampleRate;
                        String str2222 = audioCodec;
                        int i3222 = videoId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(audioCodec);
                        try {
                            _data.writeInt(videoId);
                            if (this.mRemote.transact(97, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sveRecorderCreate = Stub.getDefaultImpl().sveRecorderCreate(sessionId, filename, audioId, audioSampleRate, audioCodec, videoId);
                            _reply.recycle();
                            _data.recycle();
                            return sveRecorderCreate;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int i32222 = videoId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    int i5 = sessionId;
                    String str3 = filename;
                    int i42 = audioId;
                    int i2222 = audioSampleRate;
                    String str22222 = audioCodec;
                    int i322222 = videoId;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int sveCmcRecorderCreate(int sessionId, int audioId, int audioSampleRate, String audioCodec, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(audioId);
                    _data.writeInt(audioSampleRate);
                    _data.writeString(audioCodec);
                    _data.writeInt(audioSource);
                    _data.writeInt(outputFormat);
                    _data.writeLong(maxFileSize);
                    _data.writeInt(maxDuration);
                    _data.writeString(outputPath);
                    _data.writeInt(audioEncodingBR);
                    _data.writeInt(audioChannels);
                    _data.writeInt(audioSamplingR);
                    _data.writeInt(audioEncoder);
                    _data.writeInt(durationInterval);
                    _data.writeLong(fileSizeInterval);
                    _data.writeString(author);
                    if (!this.mRemote.transact(98, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveCmcRecorderCreate(sessionId, audioId, audioSampleRate, audioCodec, audioSource, outputFormat, maxFileSize, maxDuration, outputPath, audioEncodingBR, audioChannels, audioSamplingR, audioEncoder, durationInterval, fileSizeInterval, author);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveRecorderDelete(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(99, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveRecorderDelete(sessionId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveRecorderStart(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(100, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveRecorderStart(sessionId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveRecorderStop(int sessionId, boolean saveFile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(saveFile ? 1 : 0);
                    if (!this.mRemote.transact(101, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveRecorderStop(sessionId, saveFile);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeStartRecording(int channel, int direction, int samplingRate, boolean bIsApVoice) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    _data.writeInt(samplingRate);
                    _data.writeInt(bIsApVoice ? 1 : 0);
                    if (!this.mRemote.transact(102, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeStartRecording(channel, direction, samplingRate, bIsApVoice);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saeStopRecording(int channel, boolean bIsApVoice) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(bIsApVoice ? 1 : 0);
                    if (!this.mRemote.transact(103, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saeStopRecording(channel, bIsApVoice);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveStartRecording(int channel, int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(direction);
                    if (!this.mRemote.transact(104, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveStartRecording(channel, direction);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sveStopRecording(int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    if (!this.mRemote.transact(105, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sveStopRecording(channel);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cpveStartInjection(String filename, int samplingRate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filename);
                    _data.writeInt(samplingRate);
                    if (!this.mRemote.transact(106, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cpveStartInjection(filename, samplingRate);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cpveStopInjection() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(107, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cpveStopInjection();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerForMediaEventListener(IImsMediaEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(108, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerForMediaEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterForMediaEventListener(IImsMediaEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(109, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterForMediaEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerForCmcEventListener(ICmcMediaEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(110, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerForCmcEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterForCmcEventListener(ICmcMediaEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(111, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterForCmcEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISecVideoEngineService impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static ISecVideoEngineService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
