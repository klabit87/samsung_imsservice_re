package com.sec.internal.ims.cmstore.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class JanskyProviderAdapter {
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.samsung.ims.nsds.provider");
    private static final String LOG_TAG = JanskyProviderAdapter.class.getSimpleName();
    public static final String PROVIDER_NAME = "com.samsung.ims.nsds.provider";
    private final Context mContext;
    private ContentResolver mResolver = null;

    public JanskyProviderAdapter(Context context) {
        Log.d(LOG_TAG, "Create JanskyServiceTranslation.");
        this.mContext = context;
        this.mResolver = context.getContentResolver();
    }

    public void onTokenExpired() {
    }

    public String getSIT(String line) {
        if (line == null) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        Uri uri = NSDSContractExt.Lines.buildActiveLinesWithServicveUri();
        String result = "";
        lines.clear();
        Cursor cursor = this.mContext.getContentResolver().query(uri, (String[]) null, "status = ?", new String[]{"1"}, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String msisdn = cursor.getString(cursor.getColumnIndex("msisdn"));
                        String token = cursor.getString(cursor.getColumnIndex(NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_TOKEN));
                        String str = LOG_TAG;
                        Log.i(str, "line: " + IMSLog.checker(line) + " msisdn " + IMSLog.checker(msisdn) + ", token " + IMSLog.checker(token));
                        if (line.contains(msisdn)) {
                            result = token;
                        }
                    } while (cursor.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
        throw th;
    }

    public Cursor getActiveLines() {
        return this.mResolver.query(NSDSContractExt.Lines.buildActiveLinesWithServicveUri(), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public String getNativeLine() {
        List<String> lines = new ArrayList<>();
        Uri uri = NSDSContractExt.Lines.buildActiveLinesWithServicveUri();
        String result = "";
        lines.clear();
        Cursor cursor = this.mContext.getContentResolver().query(uri, (String[]) null, "status = ?", new String[]{"1"}, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (true) {
                        if (!"1".equals(cursor.getString(cursor.getColumnIndex("is_native")))) {
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        } else {
                            String msisdn = cursor.getString(cursor.getColumnIndex("msisdn"));
                            String str = LOG_TAG;
                            Log.i(str, "msisdn: " + IMSLog.checker(msisdn));
                            result = msisdn;
                            break;
                        }
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
        throw th;
    }
}
