package com.sec.internal.ims.core.handler;

import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;

public abstract class MediaHandler extends BaseHandler implements IMediaServiceInterface {
    protected final RegistrantList mMediaEventRegistrants = new RegistrantList();

    protected MediaHandler(Looper looper) {
        super(looper);
    }

    public void registerForMediaEvent(Handler handler, int what, Object obj) {
        this.mMediaEventRegistrants.addUnique(handler, what, obj);
    }

    public void unregisterForMediaEvent(Handler handler) {
        this.mMediaEventRegistrants.remove(handler);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }

    public void holdVideo(int phoneId, int sessionId) {
    }

    public void resumeVideo(int phoneId, int sessionId) {
    }

    public void setCamera(int id) {
    }

    public void startCamera(Surface surface) {
    }

    public void startCamera(int phoneId, int sessionId, int cameraId) {
    }

    public void stopCamera(int phoneId) {
    }

    public void switchCamera() {
    }

    public void setOrientation(int orientation) {
    }

    public void resetCameraId() {
    }

    public void getCameraInfo(int id) {
    }

    public void startRender(boolean isNearEnd) {
    }

    public void startVideoRenderer(Surface surface) {
    }

    public void stopVideoRenderer() {
    }

    public void setPreviewSurface(Object windowHandle, int color) {
    }

    public void setDisplaySurface(Object windowHandle, int color) {
    }

    public void swipeVideoSurface() {
    }

    public void deinitSurface(boolean isNearEnd) {
    }

    public void startRecord(int phoneId, int sessionId, String filePath) {
    }

    public void stopRecord(int phoneId, int sessionId) {
    }

    public void startEmoji(int phoneId, int sessionId, String emojiInfo) {
    }

    public void stopEmoji(int phoneId, int sessionId) {
    }

    public void restartEmoji(int phoneId, int sessionId) {
    }

    public void getMaxZoom() {
    }

    public void setZoom(float value) {
    }

    public void getZoom() {
    }

    public void sendStillImage(int sessionId, boolean enable, String filePath, String frameSize) {
    }

    public void setCameraEffect(int value) {
    }

    public void setPreviewResolution(int width, int height) {
    }

    public void requestCallDataUsage() {
    }

    public void bindToNetwork(Network network) {
    }

    public int startLocalRingBackTone(int streamType, int volume, int toneType) {
        return -1;
    }

    public int stopLocalRingBackTone() {
        return -1;
    }

    public void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats rtpStats) {
    }

    public boolean isSupportingCameraMotor() {
        return false;
    }

    public String getHwSupportedVideoCodecs(String profileCodecs) {
        return profileCodecs;
    }
}
