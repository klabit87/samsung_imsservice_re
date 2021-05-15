package com.sec.internal.ims.servicemodules.euc.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.sec.internal.constants.tapi.UserConsentProviderContract;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class UserConsentPersistence implements IUserConsentPersistence {
    private static final String LOG_TAG = UserConsentPersistence.class.getSimpleName();
    private final EucSQLiteHelper mEucSQLiteHelper;

    public UserConsentPersistence(Context context) {
        this.mEucSQLiteHelper = EucSQLiteHelper.getInstance((Context) Preconditions.checkNotNull(context));
    }

    public Cursor getEucList(String lang, List<EucType> types, String sortOrder, String ownIdentity) throws IllegalArgumentException {
        String str = lang;
        String str2 = LOG_TAG;
        Log.d(str2, "getEucList, thread=" + Thread.currentThread().getName() + ", lang=" + str);
        if (!types.isEmpty()) {
            try {
                SQLiteDatabase db = this.mEucSQLiteHelper.getWritableDatabase();
                StringBuilder queryBuilder = new StringBuilder("SELECT EUCRDATA.ROWID, EUCRDATA.ID, EUCRDATA.TIMESTAMP, EUCRDATA.STATE, EUCRDATA.TYPE, DIALOG.SUBJECT, DIALOG.TEXT, DIALOG.ACCEPT_BUTTON, DIALOG.REJECT_BUTTON, EUCRDATA.REMOTE_URI, EUCRDATA.SUBSCRIBER_IDENTITY FROM EUCRDATA, DIALOG WHERE ((EXISTS (" + "SELECT * FROM DIALOG d WHERE EUCRDATA.ID = d.ID AND d.LANGUAGE = ?" + ") AND " + "LANGUAGE" + " = ?)OR (not EXISTS (" + "SELECT * FROM DIALOG d WHERE EUCRDATA.ID = d.ID AND d.LANGUAGE = ?" + ") AND " + "LANGUAGE" + " = '" + DeviceLocale.DEFAULT_LANG_VALUE + "')) AND " + "EUCRDATA" + "." + UserConsentProviderContract.UserConsentList.ID + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + "DIALOG" + "." + UserConsentProviderContract.UserConsentList.ID + " AND " + "EUCRDATA" + "." + "TYPE" + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + "DIALOG" + "." + "TYPE" + " AND " + "EUCRDATA" + "." + UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + "DIALOG" + "." + UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY + " ");
                Iterator<EucType> it = types.iterator();
                if (it.hasNext()) {
                    queryBuilder.append("AND (");
                    queryBuilder.append("EUCRDATA");
                    queryBuilder.append(".");
                    queryBuilder.append("TYPE");
                    queryBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    queryBuilder.append(it.next().getId());
                    while (it.hasNext()) {
                        queryBuilder.append(" OR ");
                        queryBuilder.append("EUCRDATA");
                        queryBuilder.append(".");
                        queryBuilder.append("TYPE");
                        queryBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                        queryBuilder.append(it.next().getId());
                    }
                    queryBuilder.append(") ");
                }
                queryBuilder.append("AND (");
                queryBuilder.append("EUCRDATA");
                queryBuilder.append(".");
                queryBuilder.append(UserConsentProviderContract.UserConsentList.STATE);
                queryBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                queryBuilder.append(EucState.ACCEPTED.getId());
                queryBuilder.append(" OR ");
                queryBuilder.append("EUCRDATA");
                queryBuilder.append(".");
                queryBuilder.append(UserConsentProviderContract.UserConsentList.STATE);
                queryBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                queryBuilder.append(EucState.REJECTED.getId());
                queryBuilder.append(") AND ");
                queryBuilder.append("EUCRDATA");
                queryBuilder.append(".");
                queryBuilder.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
                queryBuilder.append("= '");
                queryBuilder.append(ownIdentity);
                queryBuilder.append("' AND (");
                queryBuilder.append("DIALOG");
                queryBuilder.append(".");
                queryBuilder.append("SUBJECT");
                queryBuilder.append(" != '' OR ");
                queryBuilder.append("DIALOG");
                queryBuilder.append(".");
                queryBuilder.append("TEXT");
                queryBuilder.append(" != '') ");
                queryBuilder.append("ORDER BY ");
                queryBuilder.append(sortOrder);
                String[] args = {str, str, str};
                String query = queryBuilder.toString();
                String str3 = LOG_TAG;
                IMSLog.s(str3, "query: " + query);
                try {
                    return db.rawQueryWithFactory(this.mEucSQLiteHelper.getCursorFactory(), query, args, (String) null);
                } catch (SQLException e) {
                    logSqlException(e);
                    return null;
                }
            } catch (SQLiteException e2) {
                String str4 = sortOrder;
                String str5 = ownIdentity;
                logSqlException(e2);
                return null;
            }
        } else {
            String str6 = sortOrder;
            String str7 = ownIdentity;
            throw new IllegalArgumentException("types list is empty");
        }
    }

    public int removeEuc(String whereClause, String[] whereArgs) throws IllegalArgumentException {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("removeEuc, whereClause: ");
        sb.append(whereClause);
        sb.append(", number of whereArgs: ");
        sb.append(whereArgs == null ? 0 : whereArgs.length);
        Log.d(str, sb.toString());
        String str2 = LOG_TAG;
        IMSLog.s(str2, "removeEuc, whereClause: " + whereClause + " whereArgs: " + Arrays.toString(whereArgs));
        int numberOfDeletedRows = 0;
        try {
            SQLiteDatabase db = this.mEucSQLiteHelper.getWritableDatabase();
            try {
                numberOfDeletedRows = db.delete("EUCRDATA", whereClause, whereArgs);
            } catch (SQLException e) {
                logSqlException(e);
            } catch (Throwable th) {
                this.mEucSQLiteHelper.close();
                db.close();
                throw th;
            }
            this.mEucSQLiteHelper.close();
            db.close();
            return numberOfDeletedRows;
        } catch (SQLiteException e2) {
            logSqlException(e2);
            return 0;
        }
    }

    private void logSqlException(Exception e) {
        String str = LOG_TAG;
        Log.e(str, "SQL Exception " + e);
    }
}
