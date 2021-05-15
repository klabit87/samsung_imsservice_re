package com.sec.internal.ims.gba;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Gba {
    private static final String LOG_TAG = Gba.class.getSimpleName();
    private GbaStore gbaStore;
    private SimpleDateFormat sdf;
    private int threshold;

    public Gba(int t) {
        this.sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        this.threshold = 0;
        this.threshold = t;
        this.gbaStore = new GbaStore();
    }

    public Gba() {
        this(0);
    }

    public void storeGbaKey(byte[] gbaType, byte[] nafid, byte[] Ks_ext_Naf, String lifeTime, String Btid) {
        storeGbaKey(gbaType, nafid, Ks_ext_Naf, lifeTime, Btid, 0);
    }

    public void storeGbaKey(byte[] gbaType, byte[] nafid, byte[] Ks_ext_Naf, String lifeTime, String Btid, int phoneId) {
        this.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date keyLifeTime = null;
        try {
            keyLifeTime = this.sdf.parse(lifeTime);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        if (!isKeyExpired(keyLifeTime)) {
            this.gbaStore.putKeys(new Gbakey(nafid, gbaType, phoneId), new GbaValue(Ks_ext_Naf, keyLifeTime, Btid));
        }
    }

    public GbaValue getGbaValue(byte[] nafid, byte[] gbaType) {
        return getGbaValue(nafid, gbaType, 0);
    }

    public GbaValue getGbaValue(byte[] nafid, byte[] gbaType, int phoneId) {
        if (nafid == null || gbaType == null) {
            return null;
        }
        Gbakey storeKey = new Gbakey(nafid, gbaType, phoneId);
        if (!this.gbaStore.hasKey(storeKey)) {
            return null;
        }
        GbaValue storeValue = this.gbaStore.getKeys(storeKey);
        if (!isKeyExpired(storeValue.getValidity())) {
            return storeValue;
        }
        this.gbaStore.removeKey(storeKey);
        return null;
    }

    public boolean isKeyExpired(Date keyLifeTime) {
        Date currentdate = new Date();
        this.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String str = LOG_TAG;
        Log.d(str, "Current Date and time in GMT: " + this.sdf.format(currentdate) + "  key life time in GMT: " + this.sdf.format(keyLifeTime));
        this.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (keyLifeTime == null || keyLifeTime.getTime() <= currentdate.getTime() + (((long) this.threshold) * 1000)) {
            return true;
        }
        return false;
    }

    public void removeGbaKey(byte[] nafid, byte[] gbaType, int phoneId) {
        if (nafid != null && gbaType != null) {
            Gbakey storeKey = new Gbakey(nafid, gbaType, phoneId);
            if (this.gbaStore.hasKey(storeKey)) {
                this.gbaStore.removeKey(storeKey);
            }
        }
    }

    public int hashCode() {
        int i = 1 * 31;
        GbaStore gbaStore2 = this.gbaStore;
        return i + (gbaStore2 == null ? 0 : gbaStore2.hashCode());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Gba)) {
            return false;
        }
        Gba other = (Gba) obj;
        GbaStore gbaStore2 = this.gbaStore;
        if (gbaStore2 == null) {
            if (other.gbaStore != null) {
                return false;
            }
        } else if (!gbaStore2.equals(other.gbaStore)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "Gba [gbaStore=" + this.gbaStore + "]";
    }
}
