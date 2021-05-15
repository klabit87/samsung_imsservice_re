package com.sec.internal.ims.entitlement.storagehelper;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class MigrationHelper {
    private static final String ENTITLEMENT_CONFIG_DATABASE_NAME = "entitlement_config.db";
    private static final String LOG_TAG = MigrationHelper.class.getSimpleName();
    private static final String NSDS_DATABASE_NAME = "ericsson_nsds.db";

    public static void migrateDBToCe(Context context) {
        Log.i(LOG_TAG, "Migrate entitlement DB/SP");
        NSDSDatabaseHelper.migrationToCe(context, NSDS_DATABASE_NAME);
        EntitlementConfigDBHelper.migrationToCe(context, ENTITLEMENT_CONFIG_DATABASE_NAME);
        NSDSSharedPrefHelper.migrationToCe(context);
        NSDSSharedPrefHelper.saveInDe(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF_CONFIG, NSDSNamespaces.NSDSMigration.DEFAULT_KEY, NSDSNamespaces.NSDSSharedPref.PREF_MIGRATE_DB_TO_CE, NSDSNamespaces.NSDSMigration.MIGRATED);
        reconnectDB(context);
    }

    public static boolean checkMigrateDB(Context context) {
        String migrateToCe = NSDSSharedPrefHelper.getInDe(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF_CONFIG, NSDSNamespaces.NSDSMigration.DEFAULT_KEY, NSDSNamespaces.NSDSSharedPref.PREF_MIGRATE_DB_TO_CE);
        if (!TextUtils.isEmpty(migrateToCe) && NSDSNamespaces.NSDSMigration.MIGRATED.equals(migrateToCe)) {
            return true;
        }
        NSDSSharedPrefHelper.saveInDe(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF_CONFIG, NSDSNamespaces.NSDSMigration.DEFAULT_KEY, NSDSNamespaces.NSDSSharedPref.PREF_MIGRATE_DB_TO_CE, NSDSNamespaces.NSDSMigration.MIGRATING);
        return false;
    }

    public static void reconnectDB(Context context) {
        Context ceContext = context.createCredentialProtectedStorageContext();
        ceContext.getContentResolver().update(Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "reconnect_db"), new ContentValues(), (String) null, (String[]) null);
        ceContext.getContentResolver().update(Uri.withAppendedPath(EntitlementConfigContract.AUTHORITY_URI, "reconnect_db"), new ContentValues(), (String) null, (String[]) null);
    }
}
