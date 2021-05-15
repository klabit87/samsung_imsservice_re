package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.capability.Capabilities;
import com.gsma.services.rcs.capability.CapabilitiesLog;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.tapi.service.api.CapabilityServiceImpl;
import com.sec.internal.ims.servicemodules.tapi.service.api.ServerApiException;
import com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager;
import java.util.Map;
import java.util.Set;

public class CapsProvider extends ContentProvider {
    public static final String AUTHORITY = CapabilitiesLog.CONTENT_URI.getAuthority();
    private static final String LOG_TAG = CapsProvider.class.getSimpleName();
    private static final int RCSAPI = 2;
    private static final int RCSAPI_ID = 1;
    private static final int RCSAPI_OWN = 3;
    public static final String[] SERVICE_PROJECTION = {"_id", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "capability_image_sharing", "capability_video_sharing", "capability_im_session", "capability_file_transfer", "capability_geoloc_push", "capability_extensions", "automata", "timestamp"};
    private static final UriMatcher uriMatcher;
    private CapabilityServiceImpl mService = null;

    static {
        UriMatcher uriMatcher2 = new UriMatcher(-1);
        uriMatcher = uriMatcher2;
        uriMatcher2.addURI(AUTHORITY, "capability/*", 1);
        uriMatcher.addURI(AUTHORITY, "capability", 2);
        uriMatcher.addURI(AUTHORITY, "capability/own", 3);
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        boolean bRetry = true;
        while (this.mService == null && bRetry) {
            this.mService = TapiServiceManager.getCapService();
            bRetry = false;
        }
        if (this.mService == null) {
            return null;
        }
        int match = uriMatcher.match(uri);
        if (match == 1) {
            try {
                Capabilities cap = this.mService.getContactCapabilities(new ContactId(uri.getLastPathSegment()));
                if (cap == null) {
                    return null;
                }
                MatrixCursor mc = new MatrixCursor(SERVICE_PROJECTION);
                buildCursor(uri.getLastPathSegment(), cap, mc);
                return mc;
            } catch (ServerApiException e) {
                e.printStackTrace();
                return null;
            }
        } else if (match == 2) {
            Map<String, Capabilities> capMap = this.mService.getAllContactCapabilities();
            if (capMap == null) {
                return null;
            }
            MatrixCursor mc2 = new MatrixCursor(SERVICE_PROJECTION);
            String str = LOG_TAG;
            Log.d(str, "capMap.size() = " + capMap.size());
            for (Map.Entry<String, Capabilities> entry : capMap.entrySet()) {
                buildCursor(entry.getKey(), entry.getValue(), mc2);
            }
            return mc2;
        } else if (match != 3) {
            return null;
        } else {
            try {
                Capabilities cap2 = this.mService.getMyCapabilities();
                if (cap2 == null) {
                    return null;
                }
                MatrixCursor mc3 = new MatrixCursor(SERVICE_PROJECTION);
                buildCursor(uri.getLastPathSegment(), cap2, mc3);
                return mc3;
            } catch (ServerApiException e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void buildCursor(String contact, Capabilities cap, MatrixCursor mc) {
        Set<String> set = cap.getSupportedExtensions();
        StringBuffer sb = new StringBuffer();
        for (String s : set) {
            sb.append(s);
            sb.append(";");
        }
        String ss = sb.toString();
        if (ss.length() > 0) {
            ss = ss.substring(0, ss.length() - 1);
        }
        mc.addRow(new Object[]{0, contact, Integer.valueOf(cap.hasCapabilities(8) ? 1 : 0), Integer.valueOf(cap.hasCapabilities(16) ? 1 : 0), Integer.valueOf(cap.hasCapabilities(2) ? 1 : 0), Integer.valueOf(cap.hasCapabilities(1) ? 1 : 0), Integer.valueOf(cap.hasCapabilities(4) ? 1 : 0), ss, Integer.valueOf(cap.isAutomata() ? 1 : 0), Long.valueOf(cap.getTimestamp())});
    }
}
