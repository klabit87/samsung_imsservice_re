package com.sec.internal.ims.servicemodules.csh;

import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import com.sec.internal.ims.csh.IVshRemoteClient;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.csh.event.ICshSuccessCallback;
import com.sec.internal.ims.servicemodules.csh.event.VideoDisplay;
import com.sec.internal.ims.servicemodules.csh.event.VshOrientation;
import com.sec.internal.ims.servicemodules.csh.event.VshVideoDisplayParams;
import com.sec.internal.ims.servicemodules.csh.event.VshViewType;

public class VshBinderFuntions extends IVshRemoteClient.Stub {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = VshBinderFuntions.class.getSimpleName();
    private final VideoShareModule mServiceModule;
    private final SparseArray<Surface> surfaceArray = new SparseArray<>();

    public VshBinderFuntions(ServiceModuleBase service) {
        this.mServiceModule = (VideoShareModule) service;
    }

    private int open(long videoShareId, Surface surface, int width, int height, int orientation, int color) {
        VshViewType vt;
        Log.i(LOG_TAG, "Calling open in initialized state.");
        if (videoShareId < 0 || surface == null) {
            release(surface);
            return -1;
        }
        VideoShare session = this.mServiceModule.getSession(videoShareId);
        if (session == null) {
            Log.e(LOG_TAG, "Session is not found");
            release(surface);
            return 0;
        }
        this.surfaceArray.put(session.getSessionId(), surface);
        VideoDisplay vd = new VideoDisplay(surface, color);
        if (session.getContent().shareDirection == 1) {
            vt = VshViewType.LOCAL;
        } else {
            vt = VshViewType.REMOTE;
        }
        this.mServiceModule.setVshVideoDisplay(new VshVideoDisplayParams(session.getSessionId(), vt, vd, new ICshSuccessCallback() {
            public void onSuccess() {
            }

            public void onFailure() {
                Log.d(VshBinderFuntions.LOG_TAG, "setVshVideoDisplay onFailure");
            }
        }));
        return 0;
    }

    private int close(long videoShareId, Surface surface, boolean endShare) {
        VshViewType vt;
        Log.i(LOG_TAG, "Calling close in initialized state.");
        if (videoShareId < 0) {
            return -1;
        }
        VideoShare session = this.mServiceModule.getSession(videoShareId);
        if (session == null) {
            Log.e(LOG_TAG, "Session is not found");
            return 0;
        }
        int sessionId = session.getSessionId();
        release(this.surfaceArray.get(sessionId));
        this.surfaceArray.delete(sessionId);
        VideoDisplay vd = new VideoDisplay(surface, 0);
        if (session.getContent().shareDirection == 1) {
            vt = VshViewType.LOCAL;
        } else {
            vt = VshViewType.REMOTE;
        }
        this.mServiceModule.resetVshVideoDisplay(new VshVideoDisplayParams(session.getSessionId(), vt, vd, new ICshSuccessCallback() {
            public void onSuccess() {
            }

            public void onFailure() {
                Log.d(VshBinderFuntions.LOG_TAG, "resetVshVideoDisplay onFailure");
            }
        }));
        return 0;
    }

    public int openVshSource(long videoShareId, Surface surface, int width, int height, int orientation, int color) throws RemoteException {
        return open(videoShareId, surface, width, height, orientation, color);
    }

    public int closeVshSource(long videoShareId, Surface surface, boolean endShare) throws RemoteException {
        int ret = close(videoShareId, surface, endShare);
        release(surface);
        return ret;
    }

    public int setOrientationListenerType(int type, int orientation) throws RemoteException {
        VshOrientation vshOrientation;
        if (orientation == 1) {
            vshOrientation = VshOrientation.LANDSCAPE;
        } else if (orientation == 2) {
            vshOrientation = VshOrientation.PORTRAIT;
        } else if (orientation == 3) {
            vshOrientation = VshOrientation.FLIPPED_LANDSCAPE;
        } else if (orientation != 4) {
            vshOrientation = VshOrientation.LANDSCAPE;
        } else {
            vshOrientation = VshOrientation.REVERSE_PORTRAIT;
        }
        this.mServiceModule.setVshPhoneOrientation(vshOrientation);
        return 0;
    }

    private void release(Surface surface) {
        if (surface != null) {
            surface.release();
        }
    }
}
