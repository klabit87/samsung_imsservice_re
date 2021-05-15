package com.google.firebase.messaging;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.internal.zzbgl;
import com.google.android.gms.internal.zzbgo;
import java.util.Map;

public final class RemoteMessage extends zzbgl {
    public static final Parcelable.Creator<RemoteMessage> CREATOR = new zzf();
    Bundle mBundle;
    private Map<String, String> zzdvf;
    private Notification zzomb;

    public static class Builder {
        private final Bundle mBundle = new Bundle();
        private final Map<String, String> zzdvf = new ArrayMap();

        public Builder(String str) {
            if (TextUtils.isEmpty(str)) {
                String valueOf = String.valueOf(str);
                throw new IllegalArgumentException(valueOf.length() != 0 ? "Invalid to: ".concat(valueOf) : new String("Invalid to: "));
            } else {
                this.mBundle.putString("google.to", str);
            }
        }

        public Builder addData(String str, String str2) {
            this.zzdvf.put(str, str2);
            return this;
        }

        public RemoteMessage build() {
            Bundle bundle = new Bundle();
            for (Map.Entry next : this.zzdvf.entrySet()) {
                bundle.putString((String) next.getKey(), (String) next.getValue());
            }
            bundle.putAll(this.mBundle);
            this.mBundle.remove("from");
            return new RemoteMessage(bundle);
        }

        public Builder clearData() {
            this.zzdvf.clear();
            return this;
        }

        public Builder setCollapseKey(String str) {
            this.mBundle.putString("collapse_key", str);
            return this;
        }

        public Builder setData(Map<String, String> map) {
            this.zzdvf.clear();
            this.zzdvf.putAll(map);
            return this;
        }

        public Builder setMessageId(String str) {
            this.mBundle.putString("google.message_id", str);
            return this;
        }

        public Builder setMessageType(String str) {
            this.mBundle.putString("message_type", str);
            return this;
        }

        public Builder setTtl(int i) {
            this.mBundle.putString("google.ttl", String.valueOf(i));
            return this;
        }
    }

    public static class Notification {
        private final String mTag;
        private final String zzbxx;
        private final String zzesj;
        private final String zzhfl;
        private final String zzomc;
        private final String[] zzomd;
        private final String zzome;
        private final String[] zzomf;
        private final String zzomg;
        private final String zzomh;
        private final String zzomi;
        private final Uri zzomj;

        private Notification(Bundle bundle) {
            this.zzesj = zza.zzd(bundle, "gcm.n.title");
            this.zzomc = zza.zzh(bundle, "gcm.n.title");
            this.zzomd = zzk(bundle, "gcm.n.title");
            this.zzbxx = zza.zzd(bundle, "gcm.n.body");
            this.zzome = zza.zzh(bundle, "gcm.n.body");
            this.zzomf = zzk(bundle, "gcm.n.body");
            this.zzhfl = zza.zzd(bundle, "gcm.n.icon");
            this.zzomg = zza.zzak(bundle);
            this.mTag = zza.zzd(bundle, "gcm.n.tag");
            this.zzomh = zza.zzd(bundle, "gcm.n.color");
            this.zzomi = zza.zzd(bundle, "gcm.n.click_action");
            this.zzomj = zza.zzaj(bundle);
        }

        private static String[] zzk(Bundle bundle, String str) {
            Object[] zzi = zza.zzi(bundle, str);
            if (zzi == null) {
                return null;
            }
            String[] strArr = new String[zzi.length];
            for (int i = 0; i < zzi.length; i++) {
                strArr[i] = String.valueOf(zzi[i]);
            }
            return strArr;
        }

        public String getBody() {
            return this.zzbxx;
        }

        public String[] getBodyLocalizationArgs() {
            return this.zzomf;
        }

        public String getBodyLocalizationKey() {
            return this.zzome;
        }

        public String getClickAction() {
            return this.zzomi;
        }

        public String getColor() {
            return this.zzomh;
        }

        public String getIcon() {
            return this.zzhfl;
        }

        public Uri getLink() {
            return this.zzomj;
        }

        public String getSound() {
            return this.zzomg;
        }

        public String getTag() {
            return this.mTag;
        }

        public String getTitle() {
            return this.zzesj;
        }

        public String[] getTitleLocalizationArgs() {
            return this.zzomd;
        }

        public String getTitleLocalizationKey() {
            return this.zzomc;
        }
    }

    RemoteMessage(Bundle bundle) {
        this.mBundle = bundle;
    }

    public final String getCollapseKey() {
        return this.mBundle.getString("collapse_key");
    }

    public final Map<String, String> getData() {
        if (this.zzdvf == null) {
            this.zzdvf = new ArrayMap();
            for (String str : this.mBundle.keySet()) {
                Object obj = this.mBundle.get(str);
                if (obj instanceof String) {
                    String str2 = (String) obj;
                    if (!str.startsWith("google.") && !str.startsWith("gcm.") && !str.equals("from") && !str.equals("message_type") && !str.equals("collapse_key")) {
                        this.zzdvf.put(str, str2);
                    }
                }
            }
        }
        return this.zzdvf;
    }

    public final String getFrom() {
        return this.mBundle.getString("from");
    }

    public final String getMessageId() {
        String string = this.mBundle.getString("google.message_id");
        return string == null ? this.mBundle.getString("message_id") : string;
    }

    public final String getMessageType() {
        return this.mBundle.getString("message_type");
    }

    public final Notification getNotification() {
        if (this.zzomb == null && zza.zzai(this.mBundle)) {
            this.zzomb = new Notification(this.mBundle);
        }
        return this.zzomb;
    }

    public final long getSentTime() {
        Object obj = this.mBundle.get("google.sent_time");
        if (obj instanceof Long) {
            return ((Long) obj).longValue();
        }
        if (!(obj instanceof String)) {
            return 0;
        }
        try {
            return Long.parseLong((String) obj);
        } catch (NumberFormatException e) {
            String valueOf = String.valueOf(obj);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 19);
            sb.append("Invalid sent time: ");
            sb.append(valueOf);
            Log.w("FirebaseMessaging", sb.toString());
            return 0;
        }
    }

    public final String getTo() {
        return this.mBundle.getString("google.to");
    }

    public final int getTtl() {
        Object obj = this.mBundle.get("google.ttl");
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        if (!(obj instanceof String)) {
            return 0;
        }
        try {
            return Integer.parseInt((String) obj);
        } catch (NumberFormatException e) {
            String valueOf = String.valueOf(obj);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 13);
            sb.append("Invalid TTL: ");
            sb.append(valueOf);
            Log.w("FirebaseMessaging", sb.toString());
            return 0;
        }
    }

    public final void writeToParcel(Parcel parcel, int i) {
        int zze = zzbgo.zze(parcel);
        zzbgo.zza(parcel, 2, this.mBundle, false);
        zzbgo.zzai(parcel, zze);
    }
}
