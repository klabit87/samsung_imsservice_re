package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.sharing.video.VideoSharingLog;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.CshCache;
import com.sec.internal.ims.servicemodules.csh.VideoShare;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.tapi.service.api.ServerApiException;
import com.sec.internal.ims.servicemodules.tapi.service.api.VideoSharingImpl;
import com.sec.internal.ims.servicemodules.tapi.service.api.VideoSharingServiceImpl;
import com.sec.internal.ims.util.PhoneUtils;

public class VideoSharingProvider extends ContentProvider {
    public static final String AUTHORITY;
    private static final String LOG_TAG = VideoSharingProvider.class.getSimpleName();
    private static final int RCSAPI = 1;
    private static final int RCSAPI_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private CshCache mCache;
    private final String[] session_columns = {"_id", "sharing_id", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "direction", "timestamp", "state", "reason_code", "duration", "video_encoding", "width", "height", "orientation"};

    static {
        String authority = VideoSharingLog.CONTENT_URI.getAuthority();
        AUTHORITY = authority;
        sUriMatcher.addURI(authority, "videoshare", 1);
        sUriMatcher.addURI(AUTHORITY, "videoshare/#", 2);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        Log.d(LOG_TAG, "VshProvider : onCreate()");
        this.mCache = CshCache.getInstance();
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri uri2 = uri;
        int uriKind = sUriMatcher.match(uri);
        MatrixCursor cm = new MatrixCursor(this.session_columns);
        Log.d(LOG_TAG, "mCache.getSize() = " + this.mCache.getSize());
        if (uriKind == 1) {
            int rowId = 0;
            for (int i = 0; i < this.mCache.getSize(); i++) {
                IContentShare share = this.mCache.getSessionAt(i);
                if (share instanceof VideoShare) {
                    CshInfo info = share.getContent();
                    Log.d(LOG_TAG, info.toString());
                    VideoSharingImpl vshSession = ((VideoSharingServiceImpl) ImsRegistry.getBinder("vsh_tapi")).getVideoSharingByID(String.valueOf(info.shareId));
                    try {
                        int rowId2 = rowId + 1;
                        try {
                            cm.newRow().add(Integer.valueOf(rowId)).add(String.valueOf(info.shareId)).add(PhoneUtils.extractNumberFromUri(info.shareContactUri.toString())).add(vshSession.getDirection()).add(Long.valueOf(vshSession.getTimeStamp())).add(vshSession.getState()).add(vshSession.getVideoEncoding()).add(Integer.valueOf(info.videoWidth)).add(Integer.valueOf(info.videoHeight)).add(Integer.valueOf(vshSession.getOrientation()));
                            rowId = rowId2;
                        } catch (ServerApiException e) {
                            e = e;
                            rowId = rowId2;
                            e.printStackTrace();
                        } catch (RemoteException e2) {
                            e = e2;
                            rowId = rowId2;
                            e.printStackTrace();
                        }
                    } catch (ServerApiException e3) {
                        e = e3;
                        e.printStackTrace();
                    } catch (RemoteException e4) {
                        e = e4;
                        e.printStackTrace();
                    }
                }
            }
            int i2 = rowId;
        } else if (uriKind == 2) {
            String shareid = uri.getPathSegments().get(1);
            int i3 = 0;
            while (true) {
                if (i3 >= this.mCache.getSize()) {
                    break;
                }
                CshInfo info2 = this.mCache.getSessionAt(i3).getContent();
                if (!(shareid == null || info2 == null)) {
                    VideoSharingImpl vshSession2 = ((VideoSharingServiceImpl) ImsRegistry.getBinder("vsh_tapi")).getVideoSharingByID(String.valueOf(info2.shareId));
                    if (shareid.equals(String.valueOf(info2.shareId))) {
                        Log.d(LOG_TAG, info2.toString());
                        try {
                            int rowId3 = 0 + 1;
                            try {
                                cm.newRow().add(0).add(String.valueOf(info2.shareId)).add(PhoneUtils.extractNumberFromUri(info2.shareContactUri.toString())).add(vshSession2.getDirection()).add(Long.valueOf(vshSession2.getTimeStamp())).add(vshSession2.getState()).add(vshSession2.getVideoEncoding()).add(Integer.valueOf(info2.videoWidth)).add(Integer.valueOf(info2.videoHeight)).add(Integer.valueOf(vshSession2.getOrientation()));
                                int i4 = rowId3;
                                break;
                            } catch (ServerApiException e5) {
                                e = e5;
                                int i5 = rowId3;
                                e.printStackTrace();
                                Log.d(LOG_TAG, "cm.getCount() = " + cm.getCount());
                                return cm;
                            } catch (RemoteException e6) {
                                e = e6;
                                int i6 = rowId3;
                                e.printStackTrace();
                                Log.d(LOG_TAG, "cm.getCount() = " + cm.getCount());
                                return cm;
                            }
                        } catch (ServerApiException e7) {
                            e = e7;
                            e.printStackTrace();
                            Log.d(LOG_TAG, "cm.getCount() = " + cm.getCount());
                            return cm;
                        } catch (RemoteException e8) {
                            e = e8;
                            e.printStackTrace();
                            Log.d(LOG_TAG, "cm.getCount() = " + cm.getCount());
                            return cm;
                        }
                    }
                }
                i3++;
            }
        }
        Log.d(LOG_TAG, "cm.getCount() = " + cm.getCount());
        return cm;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
