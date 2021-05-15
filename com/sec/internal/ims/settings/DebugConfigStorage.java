package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import com.sec.internal.helper.ImsSharedPrefHelper;
import java.util.Map;

public class DebugConfigStorage {
    private Context mContext = null;

    protected DebugConfigStorage(Context context) {
        this.mContext = context;
    }

    public void insert(int phoneId, ContentValues values) {
        ImsSharedPrefHelper.put(phoneId, this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, values);
    }

    public Cursor query(int phoneId, String[] projection) {
        if (projection == null) {
            return null;
        }
        Map<String, String> value = ImsSharedPrefHelper.getStringArray(phoneId, this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, projection);
        MatrixCursor ret = new MatrixCursor((String[]) value.keySet().toArray(new String[0]));
        ret.addRow(value.values());
        return ret;
    }
}
