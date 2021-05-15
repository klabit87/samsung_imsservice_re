package com.google.android.gms.common.internal;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.google.android.gms.common.zzs;

public final class zzak {
    private static final Uri zzghe;
    private static final Uri zzghf;

    static {
        Uri parse = Uri.parse("https://plus.google.com/");
        zzghe = parse;
        zzghf = parse.buildUpon().appendPath("circles").appendPath("find").build();
    }

    public static Intent zzamw() {
        Intent intent = new Intent("com.google.android.clockwork.home.UPDATE_ANDROID_WEAR_ACTION");
        intent.setPackage("com.google.android.wearable.app");
        return intent;
    }

    public static Intent zzgt(String str) {
        Uri fromParts = Uri.fromParts("package", str, (String) null);
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(fromParts);
        return intent;
    }

    public static Intent zzt(String str, String str2) {
        Intent intent = new Intent("android.intent.action.VIEW");
        Uri.Builder appendQueryParameter = Uri.parse("market://details").buildUpon().appendQueryParameter("id", str);
        if (!TextUtils.isEmpty(str2)) {
            appendQueryParameter.appendQueryParameter("pcampaignid", str2);
        }
        intent.setData(appendQueryParameter.build());
        intent.setPackage(zzs.GOOGLE_PLAY_STORE_PACKAGE);
        intent.addFlags(524288);
        return intent;
    }
}
