package com.sec.internal.ims.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.SimUtil;
import java.util.ArrayList;
import java.util.List;

public class ImsProfileLoaderInternal {
    public static final String SETTING_DB_CREATED = "setting_db_created";

    public static List<ImsProfile> getProfileList(Context context, int phoneId) {
        List<ImsProfile> list = new ArrayList<>();
        String mnoName = SemSystemProperties.get(Mno.MOCK_MNONAME_PROPERTY, "");
        if (TextUtils.isEmpty(mnoName)) {
            mnoName = SimUtil.getSimMno(phoneId).getName();
        }
        if (TextUtils.isEmpty(mnoName)) {
            return list;
        }
        return getProfileListWithMnoName(context, mnoName, phoneId);
    }

    public static List<ImsProfile> getProfileListWithMnoName(Context ctx, String mnoName, int simSlot) {
        ArrayList<ImsProfile> profiles = new ArrayList<>();
        Uri.Builder buildUpon = Uri.parse("content://com.sec.ims.settings/profile").buildUpon();
        Uri uri = buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + simSlot).build();
        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(uri, (String[]) null, "mnoname=" + mnoName, (String[]) null, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        ImsProfile profile = getImsProfileFromRow(ctx, cursor, simSlot);
                        if (profile != null) {
                            profiles.add(profile);
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
        return profiles;
        throw th;
    }

    public static ImsProfile getProfile(Context context, int id, int phoneId) {
        ImsProfile profile = null;
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/profile/" + id), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    profile = getImsProfileFromRow(context, cursor, phoneId);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return profile;
        throw th;
    }

    public static ImsProfile getImsProfileFromRow(Context ctx, Cursor cursor, int phoneId) {
        return ImsSimMobilityUpdate.getInstance(ctx).applySimMobilityProfileUpdate(new ImsProfile(cursor.getString(cursor.getColumnIndex("profile"))), phoneId);
    }

    public static String getRcsProfile(Context ctx, String mnoName, int simSlot) {
        List<ImsProfile> profiles = getProfileListWithMnoName(ctx, mnoName, simSlot);
        if (profiles.isEmpty()) {
            return "";
        }
        for (ImsProfile p : profiles) {
            String rcsProfile = p.getRcsProfile();
            if (p.getEnableStatus() == 2 && !TextUtils.isEmpty(rcsProfile)) {
                return rcsProfile;
            }
        }
        return "";
    }
}
