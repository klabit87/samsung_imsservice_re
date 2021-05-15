package com.sec.internal.ims.servicemodules.presence;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.presence.IPresenceService;
import com.sec.ims.presence.PresenceInfo;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.servicemodules.presence.SocialPresenceStorage;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PresenceProvider extends ContentProvider {
    private static final int EVENT_INITIALIZE = 106;
    private static final String LOG_TAG = "PresenceProvider";
    private static final int N_LOOKUP_URI_ID = 3;
    private static final Pattern OPTIONS_PATTERN = Pattern.compile("\\?");
    private static final int OWN_INFO = 1;
    private static final String PROVIDER_NAME = "com.samsung.rcs.presence";
    private static final int RCS_STATE = 2;
    private static final String[] SERVICE_PROJECTION = {SocialPresenceStorage.PresenceTable.DISPLAY_NAME, SocialPresenceStorage.PresenceTable.MOOD, SocialPresenceStorage.PresenceTable.HOMEPAGE, "email", SocialPresenceStorage.PresenceTable.BIRTHDAY, SocialPresenceStorage.PresenceTable.FACEBOOK, SocialPresenceStorage.PresenceTable.TWITTER, SocialPresenceStorage.PresenceTable.CYWORLD};
    private static final UriMatcher sUriMatcher;
    private Context mContext = null;
    private Messenger mMessenger;
    /* access modifiers changed from: private */
    public IPresenceService mService = null;
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(PROVIDER_NAME, "own", 1);
        sUriMatcher.addURI(PROVIDER_NAME, ConfigConstants.PNAME.RCS_STATE, 2);
        sUriMatcher.addURI(PROVIDER_NAME, "lookup/*/#", 3);
    }

    public boolean onCreate() {
        this.mContext = getContext();
        HandlerThread thread = new HandlerThread("PresenceService", 10);
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        this.mMessenger = new Messenger(this.mServiceHandler);
        try {
            Message msg = new Message();
            msg.what = 106;
            this.mMessenger.send(msg);
            return false;
        } catch (RemoteException e) {
            Log.i(LOG_TAG, "initialize failed");
            return false;
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 106) {
                Log.i(PresenceProvider.LOG_TAG, "could not match any message type:" + msg.what);
                return;
            }
            PresenceProvider.this.initPresecenceService();
        }
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        IMSLog.s(LOG_TAG, "query - uri: " + uri + ", selection: " + selection + ", args: " + Arrays.toString(selectionArgs));
        if (this.mService == null) {
            return new MatrixCursor(SERVICE_PROJECTION);
        }
        Uri uri2 = Uri.parse(OPTIONS_PATTERN.split(uri.toString())[0]);
        List<String> pathList = uri2.getPathSegments();
        int match = sUriMatcher.match(uri2);
        if (match == 1) {
            return serveOwnPresenceInfo();
        }
        if (match == 3) {
            return serveContactPresenceInfo(pathList.get(pathList.size() - 1));
        }
        IMSLog.s(LOG_TAG, "UNDEFINED CATEGORY! | Operation for uri: " + uri2.toString());
        return null;
    }

    /* access modifiers changed from: private */
    public void initPresecenceService() {
        Log.i(LOG_TAG, "initPresecenceService: ");
        Log.i(LOG_TAG, "Connecting to SocialPresenceService.");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.PresenceService");
        ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(PresenceProvider.LOG_TAG, "Connected to SocialPresenceService.");
                IPresenceService unused = PresenceProvider.this.mService = IPresenceService.Stub.asInterface(service);
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.i(PresenceProvider.LOG_TAG, "Disconnected.");
                IPresenceService unused = PresenceProvider.this.mService = null;
            }
        }, 1, ContextExt.CURRENT_OR_SELF);
    }

    private Cursor serveOwnPresenceInfo() {
        MatrixCursor mc = new MatrixCursor(SERVICE_PROJECTION);
        PresenceInfo info = null;
        try {
            info = this.mService.getOwnPresenceInfo();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (info == null) {
            Log.e(LOG_TAG, "serveOwnPresenceInfo: not found");
            return mc;
        }
        mc.addRow(new Object[]{info.getDisplayName(), info.getMoodText(), info.getHomepage(), info.getEmail(), info.getBirthday(), info.getFacebook(), info.getTwitter(), info.getCyworld()});
        return mc;
    }

    private Cursor serveContactPresenceInfo(String contactId) {
        MatrixCursor mc = new MatrixCursor(SERVICE_PROJECTION);
        PresenceInfo info = null;
        try {
            info = this.mService.getPresenceInfoByContactId(contactId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (info == null) {
            Log.e(LOG_TAG, "serveContactPresenceInfo: not found - contactId " + contactId);
            return mc;
        }
        mc.addRow(new Object[]{info.getDisplayName(), info.getMoodText(), info.getHomepage(), info.getEmail(), info.getBirthday(), info.getFacebook(), info.getTwitter(), info.getCyworld()});
        return mc;
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }
}
