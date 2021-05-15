package com.sec.internal.ims.servicemodules.euc.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.tapi.UserConsentProviderContract;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EucPersistence implements IEucPersistence {
    private static final String LOG_TAG = EucPersistence.class.getSimpleName();
    private SQLiteDatabase mDb = null;
    private final IEucFactory mEucFactory;
    private final EucSQLiteHelper mEucSQLiteHelper;
    private boolean mIsDbOpened;

    public EucPersistence(Context context, IEucFactory eucFactory) {
        this.mEucSQLiteHelper = EucSQLiteHelper.getInstance((Context) Preconditions.checkNotNull(context));
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(eucFactory);
    }

    public void updateEuc(EucMessageKey key, EucState state, String pin) throws EucPersistenceException {
        String str = LOG_TAG;
        Log.d(str, "updateEuc with " + key + " to state=" + state + " or PIN=" + pin);
        if (this.mDb != null) {
            StringBuilder sb = new StringBuilder(UserConsentProviderContract.UserConsentList.ID);
            sb.append("='");
            sb.append(key.getEucId());
            sb.append("' AND ");
            sb.append("TYPE");
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(key.getEucType().getId());
            sb.append(" AND ");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("='");
            sb.append(key.getOwnIdentity());
            sb.append("' AND ");
            sb.append(UserConsentProviderContract.UserConsentList.REMOTE_URI);
            sb.append("='");
            sb.append(key.getRemoteUri().encode());
            String whereCondition = sb.append("'").toString();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "update EUCData where " + whereCondition);
            ContentValues updateEucrValues = new ContentValues();
            updateEucrValues.put(UserConsentProviderContract.UserConsentList.STATE, Integer.valueOf(state.getId()));
            if (pin != null) {
                updateEucrValues.put("USER_PIN", pin);
            }
            if (this.mDb.update("EUCRDATA", updateEucrValues, whereCondition, (String[]) null) == 0) {
                throw new EucPersistenceException("No records were updated");
            }
            return;
        }
        throw new EucPersistenceException("db instance is null, no access to EUCR database");
    }

    public void insertEuc(IEucData eucData) throws EucPersistenceException {
        if (eucData == null) {
            throw new EucPersistenceException("eucData is null");
        } else if (this.mDb != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "insert EUCData to database for User Identity" + eucData.getOwnIdentity());
            ContentValues cv = new ContentValues();
            cv.put(UserConsentProviderContract.UserConsentList.ID, eucData.getId());
            cv.put(CloudMessageProviderContract.DataTypes.VVMPIN, Integer.valueOf(eucData.getPin() ? 1 : 0));
            cv.put("EXTERNAL", Integer.valueOf(eucData.getExternal() ? 1 : 0));
            cv.put(UserConsentProviderContract.UserConsentList.STATE, Integer.valueOf(eucData.getState().getId()));
            cv.put("TYPE", Integer.valueOf(eucData.getType().getId()));
            cv.put(UserConsentProviderContract.UserConsentList.REMOTE_URI, eucData.getRemoteUri().encode());
            cv.put(UserConsentProviderContract.UserConsentList.TIMESTAMP, Long.valueOf(eucData.getTimestamp()));
            cv.put("TIMEOUT", eucData.getTimeOut());
            cv.put(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY, eucData.getOwnIdentity());
            if (this.mDb.insert("EUCRDATA", (String) null, cv) == -1) {
                throw new EucPersistenceException("No records were inserted");
            }
        } else {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public void insertDialogs(IEucQuery query) throws EucPersistenceException {
        Log.d(LOG_TAG, "insert DialogData to database");
        if (query != null) {
            SQLiteDatabase sQLiteDatabase = this.mDb;
            if (sQLiteDatabase != null) {
                sQLiteDatabase.beginTransaction();
                try {
                    Iterator it = query.iterator();
                    while (it.hasNext()) {
                        IDialogData dialog = (IDialogData) it.next();
                        if (dialog != null) {
                            ContentValues cv = new ContentValues();
                            cv.put(UserConsentProviderContract.UserConsentList.ID, dialog.getKey().getEucId());
                            cv.put("TYPE", Integer.valueOf(query.getEucData().getType().getId()));
                            cv.put("LANGUAGE", dialog.getLanguage());
                            cv.put("TEXT", dialog.getText());
                            cv.put("SUBJECT", dialog.getSubject());
                            cv.put("ACCEPT_BUTTON", dialog.getAcceptButton());
                            cv.put("REJECT_BUTTON", dialog.getRejectButton());
                            cv.put(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY, query.getEucData().getOwnIdentity());
                            cv.put(UserConsentProviderContract.UserConsentList.REMOTE_URI, dialog.getKey().getRemoteUri().encode());
                            if (this.mDb.insert("DIALOG", (String) null, cv) == -1) {
                                throw new EucPersistenceException("No records were inserted");
                            }
                        }
                    }
                    this.mDb.setTransactionSuccessful();
                } finally {
                    this.mDb.endTransaction();
                }
            } else {
                throw new EucPersistenceException("db instance is null, no access to EUCR database");
            }
        } else {
            throw new EucPersistenceException("DialogData is null");
        }
    }

    public void insertAutoconfUserConsent(AutoconfUserConsentData userConsentData) throws EucPersistenceException {
        int i;
        Log.d(LOG_TAG, "insertAutoconfUserConsent");
        if (userConsentData == null) {
            throw new EucPersistenceException("userConsentData is null");
        } else if (this.mDb != null) {
            String userConsentId = "config" + userConsentData.getTimestamp();
            ContentValues cv = new ContentValues();
            cv.put(UserConsentProviderContract.UserConsentList.ID, userConsentId);
            if (userConsentData.isUserAccept()) {
                i = EucState.ACCEPTED.getId();
            } else {
                i = EucState.REJECTED.getId();
            }
            cv.put(UserConsentProviderContract.UserConsentList.STATE, Integer.valueOf(i));
            cv.put("TYPE", Integer.valueOf(EucType.EULA.getId()));
            cv.put(UserConsentProviderContract.UserConsentList.TIMESTAMP, Long.valueOf(userConsentData.getTimestamp()));
            cv.put(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY, userConsentData.getOwnIdentity());
            if (this.mDb.insert("EUCRDATA", (String) null, cv) != -1) {
                cv.clear();
                cv.put(UserConsentProviderContract.UserConsentList.ID, userConsentId);
                cv.put("TYPE", Integer.valueOf(EucType.EULA.getId()));
                cv.put("LANGUAGE", DeviceLocale.DEFAULT_LANG_VALUE);
                cv.put("SUBJECT", userConsentData.getConsentMsgTitle());
                cv.put("TEXT", userConsentData.getConsentMsgMessage());
                cv.put(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY, userConsentData.getOwnIdentity());
                if (this.mDb.insert("DIALOG", (String) null, cv) == -1) {
                    throw new EucPersistenceException("No records were inserted");
                }
                return;
            }
            throw new EucPersistenceException("No records were inserted");
        } else {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 20 */
    public List<IDialogData> getDialogs(List<String> eucIds, EucType type, String lang, List<String> ownIdentities) throws EucPersistenceException, IllegalArgumentException {
        Cursor cursor;
        Throwable th;
        String str = lang;
        String str2 = LOG_TAG;
        IMSLog.s(str2, "getDialogsForId: ids: " + Arrays.toString(eucIds.toArray()) + ", type: " + type.getId() + " lang: " + str + " ownIdentity: " + Arrays.toString(ownIdentities.toArray()));
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (eucIds.isEmpty() || ownIdentities.isEmpty()) {
            throw new EucPersistenceException("eucIds list (size=" + eucIds.size() + ") or ownIdentities list (size =" + ownIdentities.size() + ") is empty");
        } else {
            List<IDialogData> dialogsList = new ArrayList<>();
            StringBuilder whereConditionBuilder = new StringBuilder();
            Iterator<String> it = eucIds.iterator();
            whereConditionBuilder.append("(");
            whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.ID);
            whereConditionBuilder.append("='");
            whereConditionBuilder.append(it.next());
            whereConditionBuilder.append("'");
            while (it.hasNext()) {
                whereConditionBuilder.append(" OR ");
                whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.ID);
                whereConditionBuilder.append("='");
                whereConditionBuilder.append(it.next());
                whereConditionBuilder.append("'");
            }
            whereConditionBuilder.append(")");
            whereConditionBuilder.append(" AND (");
            whereConditionBuilder.append("LANGUAGE");
            whereConditionBuilder.append("='");
            whereConditionBuilder.append(str);
            whereConditionBuilder.append("' OR ");
            whereConditionBuilder.append("LANGUAGE");
            whereConditionBuilder.append("='def')");
            whereConditionBuilder.append(" AND ");
            whereConditionBuilder.append("TYPE");
            whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            whereConditionBuilder.append(type.getId());
            Iterator<String> it2 = ownIdentities.iterator();
            whereConditionBuilder.append(" AND (");
            whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            whereConditionBuilder.append("='");
            whereConditionBuilder.append(it2.next());
            whereConditionBuilder.append("'");
            while (it2.hasNext()) {
                whereConditionBuilder.append(" OR ");
                whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
                whereConditionBuilder.append("='");
                whereConditionBuilder.append(it2.next());
                whereConditionBuilder.append("'");
            }
            whereConditionBuilder.append(")");
            String whereCondition = whereConditionBuilder.toString();
            String str3 = LOG_TAG;
            IMSLog.s(str3, "select from DIALOG table where " + whereCondition);
            try {
                cursor = this.mDb.query("DIALOG", (String[]) null, whereCondition, (String[]) null, (String) null, (String) null, (String) null, (String) null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        dialogsList.add(createDialogData(cursor));
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return dialogsList;
            } catch (SQLException e) {
                String str4 = LOG_TAG;
                IMSLog.e(str4, "SQL Exception " + e);
                throw new EucPersistenceException("SQL Exception occured!");
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    public List<IDialogData> getDialogsByTypes(EucState state, List<EucType> types, String lang, String ownIdentity) throws EucPersistenceException, IllegalArgumentException {
        Cursor cursor;
        String str = LOG_TAG;
        IMSLog.s(str, "getDialogsByTypes: state: " + state.getId() + "type: " + Arrays.toString(types.toArray()) + " lang: " + lang + " ownIdentity: " + ownIdentity);
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (!types.isEmpty()) {
            StringBuilder sb = new StringBuilder("SELECT * FROM ");
            sb.append("DIALOG");
            sb.append(" JOIN ");
            sb.append("EUCRDATA");
            sb.append(" ON ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.ID);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.ID);
            sb.append(" AND ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append(" AND ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.REMOTE_URI);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.REMOTE_URI);
            sb.append(" AND ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append("TYPE");
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append("TYPE");
            sb.append(" WHERE ");
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.STATE);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(state.getId());
            sb.append(" AND ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("='");
            sb.append(ownIdentity);
            sb.append("'");
            sb.append(" AND (");
            sb.append("DIALOG");
            sb.append(".");
            sb.append("LANGUAGE");
            sb.append("='");
            sb.append(DeviceLocale.DEFAULT_LANG_VALUE);
            StringBuilder query = sb.append("'");
            if (!lang.equals(DeviceLocale.DEFAULT_LANG_VALUE)) {
                query.append(" OR ");
                query.append("DIALOG");
                query.append(".");
                query.append("LANGUAGE");
                query.append("='");
                query.append(lang);
                query.append("'");
            }
            query.append(") AND (");
            Iterator<EucType> it = types.iterator();
            if (it.hasNext()) {
                query.append("DIALOG");
                query.append(".");
                query.append("TYPE");
                query.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                query.append(it.next().getId());
                while (it.hasNext()) {
                    query.append(" OR ");
                    query.append("DIALOG");
                    query.append(".");
                    query.append("TYPE");
                    query.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    query.append(it.next().getId());
                }
            }
            query.append(");");
            String getDialogsByTypesQuery = query.toString();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "getDialogsByTypes query: " + getDialogsByTypesQuery);
            List<IDialogData> dialogsList = new ArrayList<>();
            try {
                cursor = this.mDb.rawQuery(getDialogsByTypesQuery, (String[]) null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        dialogsList.add(createDialogData(cursor));
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                String str3 = LOG_TAG;
                Log.d(str3, "getDialogsByTypes return list size: " + dialogsList.size());
                return dialogsList;
            } catch (SQLException e) {
                String str4 = LOG_TAG;
                Log.e(str4, "SQL Exception " + e);
                throw new EucPersistenceException("SQL Exception occured!");
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            throw new IllegalArgumentException("types list is empty");
        }
        throw th;
    }

    public List<IEucData> getAllEucs(EucState state, EucType type, String ownIdentity) throws EucPersistenceException {
        String str = LOG_TAG;
        IMSLog.s(str, "getAllEucs: state: " + state.getId() + " type: " + type.getId() + " ownIdentity: " + ownIdentity);
        if (this.mDb != null) {
            StringBuilder sb = new StringBuilder(UserConsentProviderContract.UserConsentList.STATE);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(state.getId());
            sb.append(" AND ");
            sb.append("TYPE");
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(type.getId());
            sb.append(" AND ");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("=\"");
            sb.append(ownIdentity);
            String whereCondition = sb.append("\"").toString();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "getAllEucs where " + whereCondition);
            return queryEucDataUsingSelection(whereCondition);
        }
        throw new EucPersistenceException("db instance is null, no access to EUCR database");
    }

    public List<IEucData> getAllEucs(List<EucState> states, EucType type, String ownIdentity) throws EucPersistenceException, IllegalArgumentException {
        String str = LOG_TAG;
        IMSLog.s(str, "getAllEucs: state: " + states.toString() + " type: " + type.getId() + " ownIdentity: " + ownIdentity);
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (!states.isEmpty()) {
            StringBuilder whereConditionBuilder = new StringBuilder("(");
            Iterator<EucState> it = states.iterator();
            whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.STATE);
            whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            whereConditionBuilder.append(it.next().getId());
            while (it.hasNext()) {
                whereConditionBuilder.append(" OR ");
                whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.STATE);
                whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                whereConditionBuilder.append(it.next().getId());
            }
            whereConditionBuilder.append(") AND ");
            whereConditionBuilder.append("TYPE");
            whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            whereConditionBuilder.append(type.getId());
            whereConditionBuilder.append(" AND ");
            whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            whereConditionBuilder.append("=\"");
            whereConditionBuilder.append(ownIdentity);
            whereConditionBuilder.append("\"");
            String str2 = LOG_TAG;
            IMSLog.s(str2, "getAllEucs where " + whereConditionBuilder);
            return queryEucDataUsingSelection(whereConditionBuilder.toString());
        } else {
            throw new IllegalArgumentException("states list is empty");
        }
    }

    public List<IEucData> getAllEucs(EucState state, List<EucType> types, String ownIdentity) throws EucPersistenceException, IllegalArgumentException {
        String str = LOG_TAG;
        IMSLog.s(str, "getAllEucs: states: " + state.getId() + " types: " + types.toString() + " ownIdentity: " + ownIdentity);
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (!types.isEmpty()) {
            StringBuilder whereConditionBuilder = new StringBuilder("(");
            Iterator<EucType> itType = types.iterator();
            if (itType.hasNext()) {
                whereConditionBuilder.append("TYPE");
                whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                whereConditionBuilder.append(itType.next().getId());
                while (itType.hasNext()) {
                    whereConditionBuilder.append(" OR ");
                    whereConditionBuilder.append("TYPE");
                    whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    whereConditionBuilder.append(itType.next().getId());
                }
            }
            whereConditionBuilder.append(") AND ");
            whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.STATE);
            whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            whereConditionBuilder.append(state.getId());
            whereConditionBuilder.append(" AND ");
            whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            whereConditionBuilder.append("=\"");
            whereConditionBuilder.append(ownIdentity);
            whereConditionBuilder.append("\"");
            String str2 = LOG_TAG;
            IMSLog.s(str2, "getAllEucs where " + whereConditionBuilder);
            return queryEucDataUsingSelection(whereConditionBuilder.toString());
        } else {
            throw new IllegalArgumentException("types list is empty");
        }
    }

    public List<IEucData> getAllEucs(List<EucState> states, List<EucType> types, String ownIdentity) throws EucPersistenceException, IllegalArgumentException {
        String str = LOG_TAG;
        IMSLog.s(str, "getAllEucs: states: " + states.toString() + " types: " + types.toString() + " ownIdentity: " + ownIdentity);
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (types.isEmpty() || states.isEmpty()) {
            throw new EucPersistenceException("types list (size=" + types.size() + ") or states list (size =" + states.size() + ") is empty");
        } else {
            StringBuilder whereConditionBuilder = new StringBuilder("(");
            Iterator<EucType> itType = types.iterator();
            if (itType.hasNext()) {
                whereConditionBuilder.append("TYPE");
                whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                whereConditionBuilder.append(itType.next().getId());
                while (itType.hasNext()) {
                    whereConditionBuilder.append(" OR ");
                    whereConditionBuilder.append("TYPE");
                    whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    whereConditionBuilder.append(itType.next().getId());
                }
            }
            whereConditionBuilder.append(") AND (");
            Iterator<EucState> itState = states.iterator();
            if (itState.hasNext()) {
                whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.STATE);
                whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                whereConditionBuilder.append(itState.next().getId());
                while (itState.hasNext()) {
                    whereConditionBuilder.append(" OR ");
                    whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.STATE);
                    whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    whereConditionBuilder.append(itState.next().getId());
                }
            }
            whereConditionBuilder.append(") AND ");
            whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            whereConditionBuilder.append("=\"");
            whereConditionBuilder.append(ownIdentity);
            whereConditionBuilder.append("\"");
            String str2 = LOG_TAG;
            IMSLog.s(str2, "getAllEucs where " + whereConditionBuilder);
            return queryEucDataUsingSelection(whereConditionBuilder.toString());
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    private List<IEucData> queryEucDataUsingSelection(String selection) throws EucPersistenceException {
        Cursor cursor;
        List<IEucData> eucrDataList = new ArrayList<>();
        try {
            cursor = this.mDb.query("EUCRDATA", (String[]) null, selection, (String[]) null, (String) null, (String) null, (String) null, (String) null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        eucrDataList.add(createEucData(cursor));
                        cursor.moveToNext();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return eucrDataList;
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL Exception " + e);
            throw new EucPersistenceException("SQL Exception occured!");
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    public IEucData getEucByKey(EucMessageKey key) throws EucPersistenceException {
        Cursor cursor;
        String str = LOG_TAG;
        IMSLog.s(str, "getEucByKey: eucMessageKey=" + key);
        if (this.mDb != null) {
            IEucData eucrData = null;
            StringBuilder sb = new StringBuilder(UserConsentProviderContract.UserConsentList.ID);
            sb.append("='");
            sb.append(key.getEucId());
            sb.append("' AND ");
            sb.append("TYPE");
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(key.getEucType().getId());
            sb.append(" AND ");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("='");
            sb.append(key.getOwnIdentity());
            sb.append("'");
            sb.append(" AND ");
            sb.append(UserConsentProviderContract.UserConsentList.REMOTE_URI);
            sb.append("='");
            sb.append(key.getRemoteUri());
            String whereCondition = sb.append("'").toString();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "getEucByKey where " + whereCondition);
            try {
                cursor = this.mDb.query("EUCRDATA", (String[]) null, whereCondition, (String[]) null, (String) null, (String) null, (String) null, (String) null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        eucrData = createEucData(cursor);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return eucrData;
            } catch (SQLException e) {
                String str3 = LOG_TAG;
                Log.e(str3, "SQL Exception " + e);
                throw new EucPersistenceException("SQL Exception occured!");
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        }
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 19 */
    public IEucData getVolatileEucByMostRecentTimeout(List<String> identities) throws EucPersistenceException {
        Cursor cursor;
        Throwable th;
        String str = LOG_TAG;
        IMSLog.s(str, "getVolatileEucByMostRecentTimeout for identities: " + Arrays.toString(identities.toArray()));
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (!identities.isEmpty()) {
            StringBuilder sb = new StringBuilder("(");
            sb.append(UserConsentProviderContract.UserConsentList.STATE);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(EucState.ACCEPTED_NOT_SENT.getId());
            sb.append(" OR ");
            sb.append(UserConsentProviderContract.UserConsentList.STATE);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(EucState.REJECTED_NOT_SENT.getId());
            sb.append(" OR ");
            sb.append(UserConsentProviderContract.UserConsentList.STATE);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(EucState.NONE.getId());
            sb.append(")");
            sb.append(" AND ");
            sb.append("TYPE");
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            StringBuilder whereConditionBuilder = sb.append(EucType.VOLATILE.getId());
            Iterator<String> it = identities.iterator();
            if (it.hasNext()) {
                whereConditionBuilder.append(" AND (");
                whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
                whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                whereConditionBuilder.append("=\"");
                whereConditionBuilder.append(it.next());
                whereConditionBuilder.append("\"");
                while (it.hasNext()) {
                    whereConditionBuilder.append(" OR ");
                    whereConditionBuilder.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
                    whereConditionBuilder.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    whereConditionBuilder.append("=\"");
                    whereConditionBuilder.append(it.next());
                    whereConditionBuilder.append("\"");
                }
                whereConditionBuilder.append(")");
            }
            String whereCondition = whereConditionBuilder.toString();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "getVolatileEucByMostRecentTimeout where " + whereCondition);
            IEucData eucrData = null;
            try {
                cursor = this.mDb.query("EUCRDATA", (String[]) null, whereCondition, (String[]) null, (String) null, (String) null, "TIMEOUT", (String) null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        eucrData = createEucData(cursor);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return eucrData;
            } catch (SQLException e) {
                String str3 = LOG_TAG;
                Log.e(str3, "SQL Exception " + e);
                throw new EucPersistenceException("SQL Exception occured!");
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            throw new IllegalArgumentException("identities list is empty");
        }
        throw th;
    }

    public void open() throws IllegalStateException, EucPersistenceException {
        Log.i(LOG_TAG, "open()");
        Preconditions.checkState(!this.mIsDbOpened, "EucPersistence is already opened!");
        try {
            this.mDb = this.mEucSQLiteHelper.getWritableDatabase();
            this.mIsDbOpened = true;
        } catch (SQLiteException e) {
            throw new EucPersistenceException("Failure, unable to open persistence!", e);
        }
    }

    public void close() throws IllegalStateException {
        Log.i(LOG_TAG, "close()");
        Preconditions.checkState(this.mIsDbOpened, "EucPersistence is already closed!");
        this.mEucSQLiteHelper.close();
        this.mDb = null;
        this.mIsDbOpened = false;
    }

    private IEucData createEucData(Cursor cursor) {
        return this.mEucFactory.createEucData(new EucMessageKey(cursor.getString(0), cursor.getString(9), ((EucType[]) EucType.class.getEnumConstants())[0].getFromId(cursor.getInt(4)), ImsUri.parse(cursor.getString(5))), cursor.getInt(1) == 1, cursor.getString(8), cursor.getInt(2) == 1, ((EucState[]) EucState.class.getEnumConstants())[0].getFromId(cursor.getInt(3)), cursor.getLong(6), Long.valueOf(cursor.getLong(7)));
    }

    private IDialogData createDialogData(Cursor cursor) {
        return this.mEucFactory.createDialogData(new EucMessageKey(cursor.getString(0), cursor.getString(6), ((EucType[]) EucType.class.getEnumConstants())[0].getFromId(cursor.getInt(7)), ImsUri.parse(cursor.getString(8))), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
    }
}
