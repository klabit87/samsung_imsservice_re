package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.RcsService;
import com.gsma.services.rcs.sharing.video.IVideoPlayer;
import com.gsma.services.rcs.sharing.video.IVideoSharing;
import com.gsma.services.rcs.sharing.video.VideoCodec;
import com.gsma.services.rcs.sharing.video.VideoDescriptor;
import com.gsma.services.rcs.sharing.video.VideoSharing;
import com.sec.internal.ims.servicemodules.csh.VideoShare;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;

public class VideoSharingImpl extends IVideoSharing.Stub {
    private final String LOG_TAG = getClass().getName();
    public CshInfo cshInfo = null;
    private VideoDescriptor descriptor = null;
    private long durationTime = 0;
    private int orientation = 0;
    private IVideoPlayer player = null;
    private int reasonCode = 0;
    private VideoShare vshSession;

    public VideoSharingImpl(VideoShare session, IVideoPlayer player2) {
        this.vshSession = session;
        this.cshInfo = session.getContent();
        this.player = player2;
    }

    public String getSharingId() throws ServerApiException {
        return String.valueOf(this.cshInfo.shareId);
    }

    public String getRemoteContact() throws ServerApiException {
        return this.cshInfo.shareContactUri.toString();
    }

    public VideoSharing.State getState() throws ServerApiException {
        VideoSharing.State state = VideoSharing.State.INVITED;
        switch (this.cshInfo.shareState) {
            case 1:
                return VideoSharing.State.INITIATING;
            case 2:
            case 10:
            case 11:
            case 17:
            case 18:
                if (this.cshInfo.shareDirection == 0) {
                    return VideoSharing.State.ACCEPTING;
                }
                if (1 == this.cshInfo.shareDirection) {
                    return VideoSharing.State.INVITED;
                }
                return state;
            case 3:
                VideoSharing.State state2 = VideoSharing.State.STARTED;
                String str = this.LOG_TAG;
                Log.d(str, "getstate satrted = " + state2);
                return state2;
            case 4:
            case 13:
            case 14:
            case 15:
            case 16:
                return VideoSharing.State.REJECTED;
            case 5:
            case 6:
            case 7:
            case 9:
            case 12:
                return VideoSharing.State.ABORTED;
            default:
                return state;
        }
    }

    public RcsService.Direction getDirection() throws ServerApiException {
        RcsService.Direction direction = RcsService.Direction.IRRELEVANT;
        if (this.cshInfo.shareDirection == 0) {
            return RcsService.Direction.INCOMING;
        }
        return RcsService.Direction.OUTGOING;
    }

    public String getVideoEncoding() throws RemoteException {
        VideoCodec tempCodec = null;
        IVideoPlayer iVideoPlayer = this.player;
        if (iVideoPlayer != null) {
            tempCodec = iVideoPlayer.getCodec();
        }
        if (tempCodec != null) {
            return tempCodec.getEncoding();
        }
        return null;
    }

    public void acceptInvitation(IVideoPlayer player2) throws RemoteException {
        Log.i(this.LOG_TAG, "Accept session invitation");
        this.player = player2;
        try {
            this.vshSession.acceptIncomingSession(Integer.valueOf(player2.getLocalRtpPort()).intValue());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void rejectInvitation() throws ServerApiException {
        Log.i(this.LOG_TAG, "Reject session invitation");
        this.vshSession.cancelByUserSession();
        if (0 != this.cshInfo.shareId) {
            VideoSharingServiceImpl.removeVideoSharingSession(String.valueOf(this.cshInfo.shareId));
        }
    }

    public void abortSharing() throws ServerApiException {
        Log.i(this.LOG_TAG, "Cancel session");
        this.vshSession.cancelByUserSession();
        if (0 != this.cshInfo.shareId) {
            VideoSharingServiceImpl.removeVideoSharingSession(String.valueOf(this.cshInfo.shareId));
        }
    }

    public VideoDescriptor getVideoDescriptor() throws ServerApiException {
        return this.descriptor;
    }

    public void setOrientation(int orientation2) throws RemoteException {
        IVideoShareModule vshModule = VideoSharingServiceImpl.getModule();
        String str = this.LOG_TAG;
        Log.d(str, "receive side || setVshPhoneOrientation vshModule = " + vshModule + "; orientation = " + orientation2);
        if (vshModule != null) {
            this.orientation = orientation2;
            vshModule.changeSurfaceOrientation(this.cshInfo.shareId, orientation2);
        }
    }

    public int getOrientation() {
        return this.orientation;
    }

    public long getDuration() throws RemoteException {
        return this.durationTime;
    }

    public long getTimeStamp() throws RemoteException {
        return 0;
    }

    public VideoSharing.ReasonCode getReasonCode() throws RemoteException {
        return VideoSharing.ReasonCode.valueOf(this.reasonCode);
    }
}
