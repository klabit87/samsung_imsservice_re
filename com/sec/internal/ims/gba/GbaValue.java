package com.sec.internal.ims.gba;

import java.util.Arrays;
import java.util.Date;

public class GbaValue {
    private String Btid;
    private Date validity;
    private byte[] value;

    public GbaValue(byte[] value2, Date validity2, String Btid2) {
        this.value = value2;
        this.validity = validity2;
        this.Btid = Btid2;
    }

    public byte[] getValue() {
        return this.value;
    }

    public Date getValidity() {
        return this.validity;
    }

    public String getBtid() {
        return this.Btid;
    }

    public int hashCode() {
        int i = 1 * 31;
        Date date = this.validity;
        int i2 = 0;
        int result = (((i + (date == null ? 0 : date.hashCode())) * 31) + Arrays.hashCode(this.value)) * 31;
        String str = this.Btid;
        if (str != null) {
            i2 = str.hashCode();
        }
        return result + i2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof GbaValue)) {
            return false;
        }
        GbaValue other = (GbaValue) obj;
        Date date = this.validity;
        if (date == null) {
            if (other.validity != null) {
                return false;
            }
        } else if (!date.equals(other.validity)) {
            return false;
        }
        if (!Arrays.equals(this.value, other.value)) {
            return false;
        }
        String str = this.Btid;
        if (str == null) {
            if (other.Btid != null) {
                return false;
            }
        } else if (!str.equals(other.Btid)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "GbaValue [value=" + Arrays.toString(this.value) + ", validity=" + this.validity + "]";
    }
}
