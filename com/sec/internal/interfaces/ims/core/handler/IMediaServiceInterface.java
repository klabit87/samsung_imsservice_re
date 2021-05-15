package com.sec.internal.interfaces.ims.core.handler;

import android.net.Network;
import android.os.Handler;
import android.view.Surface;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;

public interface IMediaServiceInterface {
    void bindToNetwork(Network network);

    void deinitSurface(boolean z);

    void getCameraInfo(int i);

    String getHwSupportedVideoCodecs(String str);

    void getMaxZoom();

    void getZoom();

    void holdVideo(int i, int i2);

    boolean isSupportingCameraMotor();

    void registerForMediaEvent(Handler handler, int i, Object obj);

    void requestCallDataUsage();

    void resetCameraId();

    void restartEmoji(int i, int i2);

    void resumeVideo(int i, int i2);

    void sendMediaEvent(int i, int i2, int i3, int i4);

    void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats audioRtpStats);

    void sendStillImage(int i, boolean z, String str, String str2);

    void setCamera(int i);

    void setCameraEffect(int i);

    void setDisplaySurface(Object obj, int i);

    void setOrientation(int i);

    void setPreviewResolution(int i, int i2);

    void setPreviewSurface(Object obj, int i);

    void setZoom(float f);

    void startCamera(int i, int i2, int i3);

    void startCamera(Surface surface);

    void startEmoji(int i, int i2, String str);

    int startLocalRingBackTone(int i, int i2, int i3);

    void startRecord(int i, int i2, String str);

    void startRender(boolean z);

    void startVideoRenderer(Surface surface);

    void stopCamera(int i);

    void stopEmoji(int i, int i2);

    int stopLocalRingBackTone();

    void stopRecord(int i, int i2);

    void stopVideoRenderer();

    void swipeVideoSurface();

    void switchCamera();

    void unregisterForMediaEvent(Handler handler);
}
