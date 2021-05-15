package com.sec.internal.google;

import android.net.Uri;
import android.os.RemoteException;
import android.telecom.VideoProfile;
import android.view.Surface;
import com.android.ims.internal.IImsVideoCallCallback;
import com.android.ims.internal.IImsVideoCallProvider;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IImsMediaCallProvider;
import com.sec.ims.volte2.IVideoServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class ImsVideoCallProviderImpl extends IImsVideoCallProvider.Stub {
    private static final int EMOJI_START_FAILURE = 1201;
    private static final int EMOJI_START_SUCCESS = 1200;
    private static final int EMOJI_STOP_FAILURE = 1203;
    private static final int EMOJI_STOP_SUCCESS = 1202;
    private static final String LOG_TAG = "ImsVTProviderImpl";
    private static final int NOTIFY_DOWNGRADED = 1001;
    private static final int NOTIFY_VIDEO_RESUMED = 1000;
    private static final int RECORD_START_FAILURE = 1101;
    private static final int RECORD_START_FAILURE_NO_SPACE = 1110;
    private static final int RECORD_START_SUCCESS = 1100;
    private static final int RECORD_STOP_FAILURE = 1103;
    private static final int RECORD_STOP_NO_SPACE = 1111;
    private static final int RECORD_STOP_SUCCESS = 1102;
    private boolean mIsDummyCamera = false;
    /* access modifiers changed from: private */
    public boolean mIsVideoPause = false;
    /* access modifiers changed from: private */
    public IImsMediaCallProvider mMediaController = null;
    private List<IVideoServiceEventListener> mRelay = null;
    /* access modifiers changed from: private */
    public IImsCallSession mSession = null;

    public ImsVideoCallProviderImpl(IImsCallSession session) {
        this.mSession = session;
        this.mRelay = new ArrayList();
        try {
            this.mMediaController = this.mSession.getMediaCallProvider();
        } catch (RemoteException e) {
        }
    }

    public void setCallback(IImsVideoCallCallback callback) throws RemoteException {
        if (this.mSession != null && this.mMediaController != null) {
            if (callback == null) {
                for (IVideoServiceEventListener listner : this.mRelay) {
                    this.mMediaController.unregisterForVideoServiceEvent(listner);
                }
                this.mRelay.clear();
                synchronized (this) {
                    this.mSession = null;
                }
                return;
            }
            IVideoServiceEventListener listner2 = new ImsVideoCallEventListener(callback);
            this.mRelay.add(listner2);
            this.mMediaController.registerForVideoServiceEvent(listner2);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0177, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCamera(java.lang.String r10, int r11) throws android.os.RemoteException {
        /*
            r9 = this;
            monitor-enter(r9)
            com.sec.ims.volte2.IImsCallSession r0 = r9.mSession     // Catch:{ all -> 0x0178 }
            if (r0 == 0) goto L_0x0176
            com.sec.ims.volte2.IImsMediaCallProvider r0 = r9.mMediaController     // Catch:{ all -> 0x0178 }
            if (r0 != 0) goto L_0x000b
            goto L_0x0176
        L_0x000b:
            r0 = 805306405(0x30000025, float:4.6566334E-10)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0178 }
            r1.<init>()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r2 = r9.mSession     // Catch:{ all -> 0x0178 }
            int r2 = r2.getPhoneId()     // Catch:{ all -> 0x0178 }
            r1.append(r2)     // Catch:{ all -> 0x0178 }
            java.lang.String r2 = ","
            r1.append(r2)     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r2 = r9.mSession     // Catch:{ all -> 0x0178 }
            int r2 = r2.getSessionId()     // Catch:{ all -> 0x0178 }
            r1.append(r2)     // Catch:{ all -> 0x0178 }
            java.lang.String r2 = ","
            r1.append(r2)     // Catch:{ all -> 0x0178 }
            r1.append(r10)     // Catch:{ all -> 0x0178 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0178 }
            com.sec.internal.log.IMSLog.c(r0, r1)     // Catch:{ all -> 0x0178 }
            if (r10 != 0) goto L_0x004b
            com.sec.ims.volte2.IImsCallSession r0 = r9.mSession     // Catch:{ all -> 0x0178 }
            boolean r1 = r9.mIsDummyCamera     // Catch:{ all -> 0x0178 }
            r0.stopCameraForProvider(r1)     // Catch:{ all -> 0x0178 }
            boolean r0 = r9.mIsDummyCamera     // Catch:{ all -> 0x0178 }
            if (r0 == 0) goto L_0x0049
            r0 = 0
            r9.mIsDummyCamera = r0     // Catch:{ all -> 0x0178 }
        L_0x0049:
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            return
        L_0x004b:
            java.lang.String r0 = "effect,"
            boolean r0 = r10.contains(r0)     // Catch:{ all -> 0x0178 }
            if (r0 == 0) goto L_0x0063
            r0 = 7
            java.lang.String r0 = r10.substring(r0)     // Catch:{ all -> 0x0178 }
            int r0 = java.lang.Integer.parseInt(r0)     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsMediaCallProvider r1 = r9.mMediaController     // Catch:{ all -> 0x0178 }
            r1.setCameraEffect(r0)     // Catch:{ all -> 0x0178 }
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            return
        L_0x0063:
            java.lang.String r0 = "startRecord,"
            boolean r0 = r10.contains(r0)     // Catch:{ all -> 0x0178 }
            if (r0 == 0) goto L_0x0079
            r0 = 12
            java.lang.String r0 = r10.substring(r0)     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsMediaCallProvider r1 = r9.mMediaController     // Catch:{ all -> 0x0178 }
            r1.startRecord(r0)     // Catch:{ all -> 0x0178 }
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            return
        L_0x0079:
            java.lang.String r0 = "stopRecord"
            boolean r0 = r10.contains(r0)     // Catch:{ all -> 0x0178 }
            if (r0 == 0) goto L_0x0089
            com.sec.ims.volte2.IImsMediaCallProvider r0 = r9.mMediaController     // Catch:{ all -> 0x0178 }
            r0.stopRecord()     // Catch:{ all -> 0x0178 }
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            return
        L_0x0089:
            java.lang.String r0 = "filter,0"
            boolean r0 = r10.contains(r0)     // Catch:{ all -> 0x0178 }
            if (r0 == 0) goto L_0x009e
            com.sec.ims.volte2.IImsMediaCallProvider r0 = r9.mMediaController     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r1 = r9.mSession     // Catch:{ all -> 0x0178 }
            int r1 = r1.getSessionId()     // Catch:{ all -> 0x0178 }
            r0.stopEmoji(r1)     // Catch:{ all -> 0x0178 }
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            return
        L_0x009e:
            java.lang.String r0 = "filter"
            boolean r0 = r10.contains(r0)     // Catch:{ all -> 0x0178 }
            if (r0 == 0) goto L_0x00ad
            com.sec.ims.volte2.IImsMediaCallProvider r0 = r9.mMediaController     // Catch:{ all -> 0x0178 }
            r0.startEmoji(r10)     // Catch:{ all -> 0x0178 }
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            return
        L_0x00ad:
            com.sec.ims.volte2.IImsMediaCallProvider r0 = r9.mMediaController     // Catch:{ all -> 0x0178 }
            int r0 = r0.getDefaultCameraId()     // Catch:{ all -> 0x0178 }
            int r1 = java.lang.Integer.parseInt(r10)     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r2 = r9.mSession     // Catch:{ all -> 0x0178 }
            int r2 = r2.getSessionId()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r3 = r9.mSession     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.data.CallProfile r3 = r3.getCallProfile()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.data.MediaProfile r3 = r3.getMediaProfile()     // Catch:{ all -> 0x0178 }
            int r3 = r3.getWidth()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r4 = r9.mSession     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.data.CallProfile r4 = r4.getCallProfile()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.data.MediaProfile r4 = r4.getMediaProfile()     // Catch:{ all -> 0x0178 }
            int r4 = r4.getHeight()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r5 = r9.mSession     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.data.MediaProfile r5 = r5.getMediaProfile()     // Catch:{ all -> 0x0178 }
            java.lang.String r5 = r5.getVideoSize()     // Catch:{ all -> 0x0178 }
            r6 = 1
            r7 = -1
            if (r1 != r7) goto L_0x00f3
            r9.mIsDummyCamera = r6     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r6 = r9.mSession     // Catch:{ all -> 0x0178 }
            r6.startCameraForProvider(r1)     // Catch:{ all -> 0x0178 }
            goto L_0x0128
        L_0x00f3:
            if (r0 == r1) goto L_0x0105
            if (r0 == r7) goto L_0x0105
            com.sec.ims.volte2.IImsCallSession r7 = r9.mSession     // Catch:{ all -> 0x0178 }
            boolean r7 = r7.getUsingCamera()     // Catch:{ all -> 0x0178 }
            if (r7 == 0) goto L_0x0105
            com.sec.ims.volte2.IImsMediaCallProvider r6 = r9.mMediaController     // Catch:{ all -> 0x0178 }
            r6.switchCamera()     // Catch:{ all -> 0x0178 }
            goto L_0x0128
        L_0x0105:
            com.sec.ims.volte2.IImsCallSession r7 = r9.mSession     // Catch:{ all -> 0x0178 }
            boolean r7 = r7.getUsingCamera()     // Catch:{ all -> 0x0178 }
            if (r7 == 0) goto L_0x0123
            java.util.List<com.sec.ims.volte2.IVideoServiceEventListener> r7 = r9.mRelay     // Catch:{ all -> 0x0178 }
            java.util.Iterator r7 = r7.iterator()     // Catch:{ all -> 0x0178 }
        L_0x0113:
            boolean r8 = r7.hasNext()     // Catch:{ all -> 0x0178 }
            if (r8 == 0) goto L_0x0123
            java.lang.Object r8 = r7.next()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IVideoServiceEventListener r8 = (com.sec.ims.volte2.IVideoServiceEventListener) r8     // Catch:{ all -> 0x0178 }
            r8.onCameraState(r2, r6)     // Catch:{ all -> 0x0178 }
            goto L_0x0113
        L_0x0123:
            com.sec.ims.volte2.IImsCallSession r6 = r9.mSession     // Catch:{ all -> 0x0178 }
            r6.startCameraForProvider(r1)     // Catch:{ all -> 0x0178 }
        L_0x0128:
            com.sec.ims.volte2.IImsCallSession r6 = r9.mSession     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.data.CallProfile r6 = r6.getCallProfile()     // Catch:{ all -> 0x0178 }
            int r6 = r6.getCallType()     // Catch:{ all -> 0x0178 }
            r7 = 8
            if (r6 != r7) goto L_0x013e
            if (r3 != 0) goto L_0x013e
            if (r4 != 0) goto L_0x013e
            r3 = 480(0x1e0, float:6.73E-43)
            r4 = 640(0x280, float:8.97E-43)
        L_0x013e:
            java.util.List<com.sec.ims.volte2.IVideoServiceEventListener> r6 = r9.mRelay     // Catch:{ all -> 0x0178 }
            java.util.Iterator r6 = r6.iterator()     // Catch:{ all -> 0x0178 }
        L_0x0144:
            boolean r7 = r6.hasNext()     // Catch:{ all -> 0x0178 }
            if (r7 == 0) goto L_0x0174
            java.lang.Object r7 = r6.next()     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IVideoServiceEventListener r7 = (com.sec.ims.volte2.IVideoServiceEventListener) r7     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.IImsCallSession r8 = r9.mSession     // Catch:{ all -> 0x0178 }
            com.sec.ims.volte2.data.CallProfile r8 = r8.getCallProfile()     // Catch:{ all -> 0x0178 }
            boolean r8 = r8.isVideoCRBT()     // Catch:{ all -> 0x0178 }
            if (r8 != 0) goto L_0x0173
            java.lang.String r8 = "LAND"
            boolean r8 = r5.contains(r8)     // Catch:{ all -> 0x0178 }
            if (r8 == 0) goto L_0x0170
            java.lang.String r8 = "QCIF"
            boolean r8 = r5.contains(r8)     // Catch:{ all -> 0x0178 }
            if (r8 != 0) goto L_0x0170
            r7.changeCameraCapabilities(r2, r4, r3)     // Catch:{ all -> 0x0178 }
            goto L_0x0173
        L_0x0170:
            r7.changeCameraCapabilities(r2, r3, r4)     // Catch:{ all -> 0x0178 }
        L_0x0173:
            goto L_0x0144
        L_0x0174:
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            return
        L_0x0176:
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            return
        L_0x0178:
            r0 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0178 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.google.ImsVideoCallProviderImpl.setCamera(java.lang.String, int):void");
    }

    public void setPreviewSurface(Surface surface) throws RemoteException {
        if (this.mSession != null && this.mMediaController != null) {
            synchronized (this) {
                if (!(this.mSession == null || this.mMediaController == null)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.mSession.getPhoneId());
                    sb.append(",");
                    sb.append(this.mSession.getSessionId());
                    sb.append(",");
                    sb.append(surface == null ? "0" : "1");
                    IMSLog.c(LogClass.VOLTE_SET_PREVIEW_SURFACE, sb.toString());
                    this.mMediaController.setPreviewSurfaceForPhoneId(this.mSession.getPhoneId(), surface);
                }
            }
        }
    }

    public void setDisplaySurface(Surface surface) throws RemoteException {
        synchronized (this) {
            if (!(this.mSession == null || this.mMediaController == null)) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.mSession.getPhoneId());
                sb.append(",");
                sb.append(this.mSession.getSessionId());
                sb.append(",");
                sb.append(surface == null ? "0" : "1");
                IMSLog.c(LogClass.VOLTE_SET_DISPLAY_SURFACE, sb.toString());
                this.mMediaController.setDisplaySurfaceForPhoneId(this.mSession.getPhoneId(), surface);
            }
        }
    }

    public void setDeviceOrientation(int rotation) throws RemoteException {
        synchronized (this) {
            if (!(this.mSession == null || this.mMediaController == null)) {
                IMSLog.c(LogClass.VOLTE_SET_ORIENTATION, this.mSession.getPhoneId() + "," + this.mSession.getSessionId() + "," + rotation);
                this.mMediaController.setDeviceOrientation(rotation);
            }
        }
    }

    public void setZoom(float value) throws RemoteException {
        synchronized (this) {
            if (!(this.mSession == null || this.mMediaController == null)) {
                this.mMediaController.setZoom(value);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c4, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendSessionModifyRequest(android.telecom.VideoProfile r6, android.telecom.VideoProfile r7) {
        /*
            r5 = this;
            monitor-enter(r5)
            if (r7 == 0) goto L_0x00c3
            com.sec.ims.volte2.IImsCallSession r0 = r5.mSession     // Catch:{ all -> 0x00c5 }
            if (r0 != 0) goto L_0x0009
            goto L_0x00c3
        L_0x0009:
            com.sec.ims.volte2.data.CallProfile r0 = new com.sec.ims.volte2.data.CallProfile     // Catch:{ all -> 0x00c5 }
            r0.<init>()     // Catch:{ all -> 0x00c5 }
            r1 = 0
            r0.setCallType(r1)     // Catch:{ all -> 0x00c5 }
            int r2 = r7.getVideoState()     // Catch:{ all -> 0x00c5 }
            boolean r2 = android.telecom.VideoProfile.isAudioOnly(r2)     // Catch:{ all -> 0x00c5 }
            r3 = 1
            if (r2 == 0) goto L_0x0021
            r0.setCallType(r3)     // Catch:{ all -> 0x00c5 }
            goto L_0x004d
        L_0x0021:
            int r2 = r7.getVideoState()     // Catch:{ all -> 0x00c5 }
            boolean r2 = android.telecom.VideoProfile.isBidirectional(r2)     // Catch:{ all -> 0x00c5 }
            if (r2 == 0) goto L_0x0030
            r2 = 2
            r0.setCallType(r2)     // Catch:{ all -> 0x00c5 }
            goto L_0x004d
        L_0x0030:
            int r2 = r7.getVideoState()     // Catch:{ all -> 0x00c5 }
            boolean r2 = android.telecom.VideoProfile.isTransmissionEnabled(r2)     // Catch:{ all -> 0x00c5 }
            if (r2 == 0) goto L_0x003f
            r2 = 3
            r0.setCallType(r2)     // Catch:{ all -> 0x00c5 }
            goto L_0x004d
        L_0x003f:
            int r2 = r7.getVideoState()     // Catch:{ all -> 0x00c5 }
            boolean r2 = android.telecom.VideoProfile.isReceptionEnabled(r2)     // Catch:{ all -> 0x00c5 }
            if (r2 == 0) goto L_0x004d
            r2 = 4
            r0.setCallType(r2)     // Catch:{ all -> 0x00c5 }
        L_0x004d:
            int r2 = r0.getCallType()     // Catch:{ all -> 0x00c5 }
            if (r2 != 0) goto L_0x0055
            monitor-exit(r5)     // Catch:{ all -> 0x00c5 }
            return
        L_0x0055:
            com.sec.ims.volte2.data.MediaProfile r2 = r0.getMediaProfile()     // Catch:{ all -> 0x00c5 }
            int r4 = r7.getQuality()     // Catch:{ all -> 0x00c5 }
            int r4 = r5.convertQualityFromVideoProfile(r4)     // Catch:{ all -> 0x00c5 }
            r2.setVideoQuality(r4)     // Catch:{ all -> 0x00c5 }
            r5.mIsDummyCamera = r1     // Catch:{ all -> 0x00c5 }
            com.sec.ims.volte2.IImsCallSession r2 = r5.mSession     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            com.sec.ims.volte2.data.CallProfile r2 = r2.getCallProfile()     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            int r2 = r2.getCallType()     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            int r4 = r0.getCallType()     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            if (r2 == r4) goto L_0x007e
            com.sec.ims.volte2.IImsCallSession r2 = r5.mSession     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            java.lang.String r3 = ""
            r2.update(r0, r1, r3)     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            goto L_0x00a5
        L_0x007e:
            int r2 = r7.getVideoState()     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            boolean r2 = android.telecom.VideoProfile.isPaused(r2)     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            if (r2 == 0) goto L_0x0090
            r5.mIsVideoPause = r3     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            com.sec.ims.volte2.IImsCallSession r1 = r5.mSession     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            r1.holdVideo()     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            goto L_0x00a5
        L_0x0090:
            int r2 = r7.getVideoState()     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            boolean r2 = android.telecom.VideoProfile.isPaused(r2)     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            if (r2 != 0) goto L_0x00a5
            boolean r2 = r5.mIsVideoPause     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            if (r2 == 0) goto L_0x00a5
            r5.mIsVideoPause = r1     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            com.sec.ims.volte2.IImsCallSession r1 = r5.mSession     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
            r1.resumeVideo()     // Catch:{ RemoteException | NullPointerException -> 0x00a6 }
        L_0x00a5:
            goto L_0x00c1
        L_0x00a6:
            r1 = move-exception
            java.lang.String r2 = "ImsVTProviderImpl"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c5 }
            r3.<init>()     // Catch:{ all -> 0x00c5 }
            java.lang.String r4 = "Couldn't notify due to "
            r3.append(r4)     // Catch:{ all -> 0x00c5 }
            java.lang.String r4 = r1.getMessage()     // Catch:{ all -> 0x00c5 }
            r3.append(r4)     // Catch:{ all -> 0x00c5 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x00c5 }
            android.util.Log.e(r2, r3)     // Catch:{ all -> 0x00c5 }
        L_0x00c1:
            monitor-exit(r5)     // Catch:{ all -> 0x00c5 }
            return
        L_0x00c3:
            monitor-exit(r5)     // Catch:{ all -> 0x00c5 }
            return
        L_0x00c5:
            r0 = move-exception
            monitor-exit(r5)     // Catch:{ all -> 0x00c5 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.google.ImsVideoCallProviderImpl.sendSessionModifyRequest(android.telecom.VideoProfile, android.telecom.VideoProfile):void");
    }

    public void sendSessionModifyResponse(VideoProfile responseProfile) {
        if (responseProfile != null && this.mSession != null) {
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(0);
            if (VideoProfile.isAudioOnly(responseProfile.getVideoState())) {
                callProfile.setCallType(1);
                if (this.mIsVideoPause) {
                    this.mIsVideoPause = false;
                }
            } else if (VideoProfile.isBidirectional(responseProfile.getVideoState())) {
                callProfile.setCallType(2);
            } else if (VideoProfile.isTransmissionEnabled(responseProfile.getVideoState())) {
                callProfile.setCallType(3);
            } else if (VideoProfile.isReceptionEnabled(responseProfile.getVideoState())) {
                callProfile.setCallType(4);
            }
            if (callProfile.getCallType() != 0) {
                callProfile.getMediaProfile().setVideoQuality(convertQualityFromVideoProfile(responseProfile.getQuality()));
                try {
                    if (this.mSession.getCallProfile().getCallType() == callProfile.getCallType()) {
                        this.mSession.reject(0);
                    } else {
                        this.mSession.accept(callProfile);
                    }
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void requestCameraCapabilities() throws RemoteException {
        int sessionId = this.mSession.getSessionId();
        int width = this.mSession.getCallProfile().getMediaProfile().getWidth();
        int height = this.mSession.getCallProfile().getMediaProfile().getHeight();
        String videoSize = this.mSession.getCallProfile().getMediaProfile().getVideoSize();
        for (IVideoServiceEventListener listener : this.mRelay) {
            if (!videoSize.contains("LAND") || videoSize.contains("QCIF")) {
                listener.changeCameraCapabilities(sessionId, width, height);
            } else {
                listener.changeCameraCapabilities(sessionId, height, width);
            }
        }
    }

    public void requestCallDataUsage() throws RemoteException {
        IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            iImsCallSession.requestCallDataUsage();
        }
    }

    public void setPauseImage(Uri uri) throws RemoteException {
        if (this.mSession != null && this.mMediaController != null) {
            IMSLog.c(LogClass.VOLTE_SET_PAUSE_IMAGE, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
            if (uri != null) {
                this.mMediaController.sendStillImage(this.mSession.getSessionId(), uri.toString(), 256, "VGA", 0);
                return;
            }
            if (!this.mSession.getUsingCamera()) {
                this.mSession.startCameraForProvider(-1);
            }
            this.mMediaController.sendLiveVideo(this.mSession.getSessionId());
        }
    }

    private int convertQualityFromVideoProfile(int quality) {
        if (quality == 1) {
            return 15;
        }
        if (quality != 2) {
            if (quality == 3) {
                return 12;
            }
            if (quality != 4) {
                return 13;
            }
        }
        return 13;
    }

    private class ImsVideoCallEventListener extends IVideoServiceEventListener.Stub {
        private IImsVideoCallCallback mCallback = null;

        public ImsVideoCallEventListener(IImsVideoCallCallback callback) {
            this.mCallback = callback;
        }

        public IImsCallSession getSession() {
            return ImsVideoCallProviderImpl.this.mSession;
        }

        public void receiveSessionModifyRequest(int sessionId, CallProfile mediaProfile) throws RemoteException {
            VideoProfile videoProfile;
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId() && this.mCallback != null && (videoProfile = convertCallProfileToVideoProfile(mediaProfile)) != null) {
                try {
                    this.mCallback.receiveSessionModifyRequest(videoProfile);
                } catch (RemoteException e) {
                }
            }
        }

        public void receiveSessionModifyResponse(int sessionId, int status, CallProfile requestedProfile, CallProfile responseProfile) throws RemoteException {
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId() && this.mCallback != null) {
                int result = 0;
                VideoProfile reqVideoProfile = convertCallProfileToVideoProfile(requestedProfile);
                VideoProfile resVideoProfile = convertCallProfileToVideoProfile(responseProfile);
                if (status == 200) {
                    result = 1;
                    if (reqVideoProfile != null && resVideoProfile != null) {
                        if (responseProfile.getCallType() == 1) {
                            boolean unused = ImsVideoCallProviderImpl.this.mIsVideoPause = false;
                        }
                    } else {
                        return;
                    }
                } else if (status == ImsVideoCallProviderImpl.RECORD_START_FAILURE_NO_SPACE) {
                    result = 5;
                } else if (status == 1109 || status == 487) {
                    result = 2;
                }
                try {
                    this.mCallback.receiveSessionModifyResponse(result, reqVideoProfile, resVideoProfile);
                } catch (RemoteException e) {
                }
            }
        }

        public void onVideoOrientChanged(int sessionId) throws RemoteException {
        }

        public void onCameraState(int sessionId, int state) throws RemoteException {
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId() && this.mCallback != null) {
                IMSLog.c(LogClass.VOLTE_CHANGE_CAMERA_STATE, ImsVideoCallProviderImpl.this.mSession.getPhoneId() + "," + sessionId + "," + state);
                switch (state) {
                    case 0:
                        this.mCallback.handleCallSessionEvent(3);
                        return;
                    case 1:
                    case 5:
                        this.mCallback.handleCallSessionEvent(6);
                        return;
                    case 2:
                    case 4:
                    case 6:
                    case 7:
                        this.mCallback.handleCallSessionEvent(5);
                        return;
                    case 3:
                        try {
                            this.mCallback.handleCallSessionEvent(4);
                            return;
                        } catch (RemoteException e) {
                            return;
                        }
                    default:
                        return;
                }
            }
        }

        public void onVideoState(int sessionId, int state) throws RemoteException {
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId() && this.mCallback != null) {
                IMSLog.c(LogClass.VOLTE_CHANGE_VIDEO_STATE, ImsVideoCallProviderImpl.this.mSession.getPhoneId() + "," + sessionId + "," + state);
                if (state == 0) {
                    this.mCallback.handleCallSessionEvent(2);
                } else if (state == 1) {
                    this.mCallback.handleCallSessionEvent(1);
                } else if (state == 2) {
                    boolean unused = ImsVideoCallProviderImpl.this.mIsVideoPause = false;
                    this.mCallback.handleCallSessionEvent(1000);
                } else if (state == 3) {
                    try {
                        this.mCallback.handleCallSessionEvent(1001);
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public void onVideoQualityChanged(int sessionId, int quality) throws RemoteException {
            IImsVideoCallCallback iImsVideoCallCallback;
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId() && (iImsVideoCallCallback = this.mCallback) != null) {
                if (quality == 0) {
                    iImsVideoCallCallback.changeVideoQuality(3);
                } else if (quality == 1) {
                    iImsVideoCallCallback.changeVideoQuality(2);
                } else if (quality == 2) {
                    try {
                        iImsVideoCallCallback.changeVideoQuality(1);
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public void onChangePeerDimension(int sessionId, int width, int height) throws RemoteException {
            IImsVideoCallCallback iImsVideoCallCallback;
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId() && (iImsVideoCallCallback = this.mCallback) != null) {
                try {
                    iImsVideoCallCallback.changePeerDimensions(width, height);
                } catch (RemoteException e) {
                }
            }
        }

        public void setVideoPause(int sessionId, boolean isVideoPause) throws RemoteException {
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId()) {
                boolean unused = ImsVideoCallProviderImpl.this.mIsVideoPause = isVideoPause;
            }
        }

        public void changeCameraCapabilities(int sessionId, int width, int height) throws RemoteException {
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId() && this.mCallback != null) {
                try {
                    this.mCallback.changeCameraCapabilities(new VideoProfile.CameraCapabilities(width, height));
                } catch (RemoteException e) {
                }
            }
        }

        public void onRecordState(int sessionId, int state) throws RemoteException {
            IImsVideoCallCallback iImsVideoCallCallback;
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && (iImsVideoCallCallback = this.mCallback) != null) {
                if (state == 0) {
                    iImsVideoCallCallback.handleCallSessionEvent(1100);
                } else if (state == 1) {
                    iImsVideoCallCallback.handleCallSessionEvent(1101);
                } else if (state == 2) {
                    iImsVideoCallCallback.handleCallSessionEvent(ImsVideoCallProviderImpl.RECORD_START_FAILURE_NO_SPACE);
                } else if (state == 3) {
                    iImsVideoCallCallback.handleCallSessionEvent(1102);
                } else if (state == 4) {
                    iImsVideoCallCallback.handleCallSessionEvent(1103);
                } else if (state == 5) {
                    try {
                        iImsVideoCallCallback.handleCallSessionEvent(1111);
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public void onEmojiState(int sessionId, int state) throws RemoteException {
            IImsVideoCallCallback iImsVideoCallCallback;
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && (iImsVideoCallCallback = this.mCallback) != null) {
                if (state == 0) {
                    iImsVideoCallCallback.handleCallSessionEvent(1200);
                } else if (state == 1) {
                    iImsVideoCallCallback.handleCallSessionEvent(1201);
                } else if (state == 2) {
                    iImsVideoCallCallback.handleCallSessionEvent(ImsVideoCallProviderImpl.EMOJI_STOP_SUCCESS);
                } else if (state == 3) {
                    try {
                        iImsVideoCallCallback.handleCallSessionEvent(ImsVideoCallProviderImpl.EMOJI_STOP_FAILURE);
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public void onChangeCallDataUsage(int sessionId, long dataUsage) throws RemoteException {
            IImsVideoCallCallback iImsVideoCallCallback;
            if (ImsVideoCallProviderImpl.this.mSession != null && ImsVideoCallProviderImpl.this.mMediaController != null && sessionId == ImsVideoCallProviderImpl.this.mSession.getSessionId() && (iImsVideoCallCallback = this.mCallback) != null) {
                try {
                    iImsVideoCallCallback.changeCallDataUsage(dataUsage);
                } catch (RemoteException e) {
                }
            }
        }

        private int convertQualityToVideoProfile(int quality) {
            if (quality == 12) {
                return 3;
            }
            if (quality == 13) {
                return 2;
            }
            if (quality != 15) {
                return 4;
            }
            return 1;
        }

        private int convertStateToVideoProfile(int state) {
            if (state == 1) {
                return 0;
            }
            if (state == 2) {
                return 3;
            }
            if (state == 3) {
                return 1;
            }
            if (state != 4) {
                return 0;
            }
            return 2;
        }

        private VideoProfile convertCallProfileToVideoProfile(CallProfile profile) {
            if (profile == null) {
                return null;
            }
            int videoState = convertStateToVideoProfile(profile.getCallType());
            if (profile.getMediaProfile().getVideoPause()) {
                videoState |= 4;
            }
            int quality = convertQualityToVideoProfile(profile.getMediaProfile().getVideoQuality());
            if (profile.getCallType() == 0) {
                return null;
            }
            return new VideoProfile(videoState, quality);
        }
    }
}
