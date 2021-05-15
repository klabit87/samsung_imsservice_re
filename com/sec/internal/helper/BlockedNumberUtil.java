package com.sec.internal.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.BlockedNumberContract;
import android.text.TextUtils;
import android.util.Log;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BlockedNumberUtil {
    private static final String LOG_TAG = BlockedNumberUtil.class.getSimpleName();

    public static boolean isBlockedNumber(Context context, String number) {
        try {
            if (BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                return BlockedNumberContract.isBlocked(context, number);
            }
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "isBlockedNumber occur IllegalArgumentException");
            return false;
        }
    }

    public static Set<String> getBlockedNumbersList(Context context) {
        Cursor cursor;
        Set<String> list = new HashSet<>();
        try {
            cursor = context.getContentResolver().query(BlockedNumberContract.BlockedNumbers.CONTENT_URI, (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String address = cursor.getString(cursor.getColumnIndex("original_number"));
                        if (!TextUtils.isEmpty(address)) {
                            list.add(address);
                        }
                    } while (cursor.moveToNext());
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return list;
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "getBlockedNumbersList occur IllegalArgumentException");
            return Collections.emptySet();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }
}
