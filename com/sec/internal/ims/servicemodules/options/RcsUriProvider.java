package com.sec.internal.ims.servicemodules.options;

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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.ICapabilityService;
import com.sec.internal.helper.UriUtil;

public class RcsUriProvider extends ContentProvider {
    private static final String AUTHORITY = "com.sec.ims.android.rcsuriprovider";
    private static final String[] ENABLED_PROJECTION = {"_id", "sip_uri", Columns.IS_ENABLED};
    private static final String LOG_TAG = "RcsUriProvider";
    private static final int N_RCSENABLE_URIS = 1;
    private static final UriMatcher mMatcher;
    private Context mContext = null;
    /* access modifiers changed from: private */
    public ICapabilityService mService = null;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        mMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "rcsenableduri", 1);
    }

    public boolean onCreate() {
        Log.i(LOG_TAG, "onCreate()");
        this.mContext = getContext();
        Log.i(LOG_TAG, "Connecting to CapabilityDiscoveryService.");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.CapabilityService");
        ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(RcsUriProvider.LOG_TAG, "Connected.");
                ICapabilityService unused = RcsUriProvider.this.mService = ICapabilityService.Stub.asInterface(service);
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.i(RcsUriProvider.LOG_TAG, "Disconnected.");
                ICapabilityService unused = RcsUriProvider.this.mService = null;
            }
        }, 1, ContextExt.CURRENT_OR_SELF);
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        Uri uri2 = uri;
        if (mMatcher.match(uri) == 1) {
            Log.i(LOG_TAG, "N_RCSENABLE_URIS | Operation for uri: ".concat(uri.toString()));
            MatrixCursor mc = new MatrixCursor(ENABLED_PROJECTION);
            ICapabilityService iCapabilityService = this.mService;
            if (iCapabilityService == null) {
                Log.e(LOG_TAG, "Binder is not initialized! Returning empty response");
                return mc;
            }
            try {
                Capabilities[] list = iCapabilityService.getAllCapabilities(phoneId);
                if (list == null) {
                    return mc;
                }
                if (list.length == 0) {
                    Log.i(LOG_TAG, "N_RCSENABL_URIS: not found.");
                    return mc;
                }
                int id = 1;
                int length = list.length;
                int i = 0;
                while (i < length) {
                    int id2 = id + 1;
                    mc.addRow(new Object[]{Integer.valueOf(id), list[i].getUri().toString(), 1});
                    i++;
                    id = id2;
                }
                return mc;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG, "UNDEFINED CATEGORY! | Operation for uri: ".concat(uri.toString()));
            throw new UnsupportedOperationException("Operation not supported for uri: ".concat(uri.toString()));
        }
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }
}
