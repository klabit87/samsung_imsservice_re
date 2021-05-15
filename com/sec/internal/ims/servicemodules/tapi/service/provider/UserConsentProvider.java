package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.tapi.UserConsentProviderContract;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.persistence.UserConsentPersistence;
import com.sec.internal.ims.servicemodules.euc.persistence.UserConsentPersistenceNotifier;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.tapi.IUserConsentListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class UserConsentProvider extends ContentProvider {
    private static final String LOG_TAG = UserConsentProvider.class.getSimpleName();
    private static final UriMatcher URI_MATCHER;
    private static final int USER_CONSENT_LIST = 1;
    private UserConsentPersistence mPersistence = null;
    private UserConsentPersistenceNotifier mUserConsentPersistenceNotifier;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        URI_MATCHER = uriMatcher;
        uriMatcher.addURI(UserConsentProviderContract.AUTHORITY, "#", 1);
    }

    public boolean onCreate() {
        UserConsentPersistenceNotifier instance = UserConsentPersistenceNotifier.getInstance();
        this.mUserConsentPersistenceNotifier = instance;
        instance.setListener(new IUserConsentListener() {
            public void notifyChanged(int phoneId) {
                if (UserConsentProvider.this.getContext() != null) {
                    UserConsentProvider.this.getContext().getContentResolver().notifyChange(Uri.withAppendedPath(UserConsentProviderContract.CONTENT_URI, Integer.toString(phoneId)), (ContentObserver) null);
                }
            }
        });
        this.mPersistence = new UserConsentPersistence(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String str;
        Uri uri2 = uri;
        String[] strArr = selectionArgs;
        Log.d(LOG_TAG, "query(Uri, String[], String, String[], String) uri: " + uri2);
        if (URI_MATCHER.match(uri2) == 1) {
            int slot = Integer.parseInt(uri.getLastPathSegment());
            ISimManager simManager = SimManagerFactory.getSimManagerFromSimSlot(slot);
            String imsi = null;
            boolean isSimAvailable = false;
            if (simManager != null) {
                imsi = simManager.getImsi();
                isSimAvailable = simManager.isSimAvailable();
            }
            IMSLog.s(LOG_TAG, "query: slot=" + slot + ", imsi=" + imsi + ", isSimAvailable=" + isSimAvailable);
            if (imsi == null || imsi.isEmpty()) {
                String str2 = selection;
                return null;
            } else if (!isSimAvailable) {
                String str3 = selection;
                return null;
            } else {
                List<EucType> types = new ArrayList<>();
                for (String type : strArr) {
                    char c = 65535;
                    switch (type.hashCode()) {
                        case -1408244262:
                            if (type.equals(UserConsentProviderContract.EUCR_ACKNOWLEDGEMENT_LABEL)) {
                                c = 3;
                                break;
                            }
                            break;
                        case -1382453013:
                            if (type.equals(UserConsentProviderContract.EUCR_NOTIFICATION_LABEL)) {
                                c = 2;
                                break;
                            }
                            break;
                        case -1105400420:
                            if (type.equals(UserConsentProviderContract.EUCR_VOLATILE_LABEL)) {
                                c = 1;
                                break;
                            }
                            break;
                        case 2139685:
                            if (type.equals(UserConsentProviderContract.EULA_LABEL)) {
                                c = 4;
                                break;
                            }
                            break;
                        case 997554839:
                            if (type.equals(UserConsentProviderContract.EUCR_PERSISTENT_LABEL)) {
                                c = 0;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        types.add(EucType.PERSISTENT);
                    } else if (c == 1) {
                        types.add(EucType.VOLATILE);
                    } else if (c == 2) {
                        types.add(EucType.NOTIFICATION);
                    } else if (c == 3) {
                        types.add(EucType.ACKNOWLEDGEMENT);
                    } else if (c == 4) {
                        types.add(EucType.EULA);
                    }
                }
                UserConsentPersistence userConsentPersistence = this.mPersistence;
                if (sortOrder != null) {
                    str = sortOrder;
                } else {
                    str = UserConsentProviderContract.UserConsentList.SORT_ORDER_DEFAULT;
                }
                return userConsentPersistence.getEucList(selection, types, str, imsi);
            }
        } else {
            String str4 = selection;
            throw new IllegalArgumentException("Unsupported URI: " + uri2);
        }
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (URI_MATCHER.match(uri) == 1) {
            int slot = Integer.parseInt(uri.getLastPathSegment());
            int numberOfDeletedRows = this.mPersistence.removeEuc(selection, selectionArgs);
            this.mUserConsentPersistenceNotifier.notifyListener(slot);
            return numberOfDeletedRows;
        }
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
