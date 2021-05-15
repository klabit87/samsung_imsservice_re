package com.sec.internal.ims.servicemodules.volte2;

import android.net.Network;
import android.view.Surface;
import com.sec.ims.volte2.IVideoServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;

public interface IImsMediaController {
    void bindToNetwork(Network network);

    void changeCameraCapabilities(int i, int i2, int i3);

    int getDefaultCameraId();

    void holdVideo(int i);

    boolean isSupportingCameraMotor();

    void onCallDowngraded(IMSMediaEvent iMSMediaEvent);

    void onChangeCallDataUsage(int i, long j);

    void receiveSessionModifyRequest(int i, CallProfile callProfile);

    void receiveSessionModifyResponse(int i, int i2, CallProfile callProfile, CallProfile callProfile2);

    void registerForMediaEvent(ImsCallSession imsCallSession);

    void registerForVideoServiceEvent(IVideoServiceEventListener iVideoServiceEventListener);

    void requestCallDataUsage();

    void resetCameraId();

    void resumeVideo(int i);

    void sendLiveVideo(int i);

    void sendStillImage(int i, String str, int i2, String str2, int i3);

    void setCameraEffect(int i);

    void setDeviceOrientation(int i);

    void setDisplaySurface(Surface surface);

    void setDisplaySurfaceForPhoneId(int i, Surface surface);

    void setPreviewResolution(int i, int i2);

    void setPreviewSurface(Surface surface);

    void setPreviewSurfaceForPhoneId(int i, Surface surface);

    void setVideoPause(int i, boolean z);

    void setZoom(float f);

    void startCamera(int i, int i2);

    void startCameraForActiveExcept(int i);

    void startEmoji(String str);

    void startRecord(String str);

    void stopActiveCamera();

    void stopCamera(int i);

    void stopEmoji(int i);

    void stopRecord();

    void switchCamera();

    void unregisterForMediaEvent(ImsCallSession imsCallSession);

    void unregisterForVideoServiceEvent(IVideoServiceEventListener iVideoServiceEventListener);
}
