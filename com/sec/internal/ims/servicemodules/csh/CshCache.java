package com.sec.internal.ims.servicemodules.csh;

import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.IshFileTransfer;
import com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface;
import com.sec.internal.ims.util.StorageEnvironment;

public class CshCache {
    private static String LOG_TAG = CshCache.class.getSimpleName();
    private static IIshServiceInterface imsServiceForIsh = null;
    private static IvshServiceInterface imsServiceForVsh = null;
    private static CshCache sInstance = null;
    private final SparseArray<IContentShare> mSessions = new SparseArray<>();

    private CshCache() {
    }

    public static CshCache getInstance(IIshServiceInterface imsService) {
        if (imsServiceForIsh == null) {
            imsServiceForIsh = imsService;
        }
        return getInstance();
    }

    public static CshCache getInstance(IvshServiceInterface imsService) {
        if (imsServiceForVsh == null) {
            imsServiceForVsh = imsService;
        }
        return getInstance();
    }

    public static CshCache getInstance() {
        if (sInstance == null) {
            sInstance = new CshCache();
        }
        return sInstance;
    }

    public void init() {
        this.mSessions.clear();
    }

    public IContentShare getSessionAt(int idx) {
        return this.mSessions.valueAt(idx);
    }

    public IContentShare getSession(int sessionId) {
        return this.mSessions.get(sessionId);
    }

    public IContentShare getSession(long sharedId) {
        for (int i = 0; i < this.mSessions.size(); i++) {
            IContentShare session = this.mSessions.valueAt(i);
            if (session != null && sharedId == session.getContent().shareId) {
                return session;
            }
        }
        return null;
    }

    public void putSession(IContentShare session) {
        this.mSessions.append(session.getSessionId(), session);
        String str = LOG_TAG;
        Log.d(str, "Added share [" + session.getContent() + "]");
    }

    public void deleteSession(int sessionId) {
        String str = LOG_TAG;
        Log.d(str, "Remove share sessionId " + sessionId);
        this.mSessions.delete(sessionId);
    }

    public int getSize() {
        return this.mSessions.size();
    }

    public ImageShare newOutgoingImageShare(ImageShareModule service, ImsUri contactUri, String filePath) {
        CshInfo info = new CshInfo();
        info.shareDirection = 1;
        info.shareType = 1;
        info.shareContactUri = contactUri;
        info.dataPath = filePath;
        return new ImageShare(imsServiceForIsh, service, info);
    }

    public ImageShare newIncommingImageShare(ImageShareModule service, int sessionId, ImsUri contactUri, IshFileTransfer ft) {
        CshInfo info = new CshInfo();
        info.shareDirection = 0;
        info.shareType = 1;
        info.shareContactUri = contactUri;
        info.dataPath = StorageEnvironment.generateStorePath(ft.getPath());
        info.dataSize = ft.getSize();
        info.mimeType = ft.getMimeType();
        ImageShare session = new ImageShare(imsServiceForIsh, service, info);
        session.setSessionId(sessionId);
        return session;
    }

    public VideoShare newOutgoingVideoShare(VideoShareModule service, ImsUri contactUri, String videoPath) {
        CshInfo info = new CshInfo();
        info.shareDirection = 1;
        info.shareType = 2;
        info.shareContactUri = contactUri;
        info.dataPath = videoPath;
        return new VideoShare(imsServiceForVsh, service, info);
    }

    public VideoShare newIncommingVideoShare(VideoShareModule service, int sessionId, ImsUri contactUri, String videoPath) {
        CshInfo info = new CshInfo();
        info.shareDirection = 0;
        info.shareType = 2;
        info.shareContactUri = contactUri;
        info.dataPath = videoPath;
        VideoShare session = new VideoShare(imsServiceForVsh, service, info);
        session.setSessionId(sessionId);
        return session;
    }
}
