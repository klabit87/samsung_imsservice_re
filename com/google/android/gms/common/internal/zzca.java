package com.google.android.gms.common.internal;

import android.content.Context;
import android.content.res.Resources;
import com.google.android.gms.R;

public final class zzca {
    private final Resources zzgid;
    private final String zzgie;

    public zzca(Context context) {
        zzbq.checkNotNull(context);
        Resources resources = context.getResources();
        this.zzgid = resources;
        this.zzgie = resources.getResourcePackageName(R.string.common_google_play_services_unknown_issue);
    }

    public final String getString(String str) {
        int identifier = this.zzgid.getIdentifier(str, "string", this.zzgie);
        if (identifier == 0) {
            return null;
        }
        return this.zzgid.getString(identifier);
    }
}
