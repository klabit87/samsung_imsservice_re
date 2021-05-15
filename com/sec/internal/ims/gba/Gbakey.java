package com.sec.internal.ims.gba;

import android.util.Log;
import com.sec.internal.helper.SimUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Gbakey {
    private static final String LOG_TAG = Gbakey.class.getSimpleName();
    private byte[] id;
    private byte[] phoneId;
    private byte[] type;

    public Gbakey(byte[] id2, byte[] type2) {
        this.type = type2;
        this.id = id2;
        this.phoneId = (String.valueOf(0) + String.valueOf(SimUtil.getSubId(0))).getBytes(StandardCharsets.UTF_8);
        String str = LOG_TAG;
        Log.d(str, "Gbakey: " + toString());
    }

    public Gbakey(byte[] id2, byte[] type2, int phoneId2) {
        this.type = type2;
        this.id = id2;
        this.phoneId = (String.valueOf(phoneId2) + String.valueOf(SimUtil.getSubId(phoneId2))).getBytes(StandardCharsets.UTF_8);
        String str = LOG_TAG;
        Log.d(str, "Gbakey: " + toString());
    }

    public int hashCode() {
        return (((((1 * 31) + Arrays.hashCode(this.id)) * 31) + Arrays.hashCode(this.type)) * 31) + Arrays.hashCode(this.phoneId);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Gbakey)) {
            return false;
        }
        Gbakey other = (Gbakey) obj;
        if (Arrays.equals(this.id, other.id) && Arrays.equals(this.type, other.type) && Arrays.equals(this.phoneId, other.phoneId)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "Gbakey [type=" + Arrays.toString(this.type) + ", id=" + Arrays.toString(this.id) + ", phoneId=" + Arrays.toString(this.phoneId) + "]";
    }
}
