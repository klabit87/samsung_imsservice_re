package com.sec.internal.ims.core.sim;

import android.util.Log;

/* compiled from: MnoMap */
class NetworkIdentifier {
    private static final String LOG_TAG = "NetworkIdentifier";
    public String mGid1;
    public String mGid2;
    public String mMccMnc;
    public String mMnoName;
    public String mSpname;
    public String mSubset;

    public NetworkIdentifier(String mccmnc, String subset, String gid1, String gid2, String spname) {
        this.mMccMnc = mccmnc;
        this.mSubset = subset;
        this.mGid1 = gid1;
        this.mGid2 = gid2;
        this.mSpname = spname;
        this.mMnoName = "default";
    }

    public NetworkIdentifier(String mccmnc, String subset, String gid1, String gid2, String spname, String mnoName) {
        this.mMccMnc = mccmnc;
        this.mSubset = subset;
        this.mGid1 = gid1;
        this.mGid2 = gid2;
        this.mSpname = spname;
        this.mMnoName = mnoName;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NetworkIdentifier)) {
            return false;
        }
        NetworkIdentifier netid = (NetworkIdentifier) obj;
        Log.i(LOG_TAG, "equals: L" + toString() + ", R" + netid.toString());
        if (!this.mMccMnc.equals(netid.mMccMnc) || !this.mSubset.equals(netid.mSubset) || !this.mGid1.equals(netid.mGid1) || !this.mGid2.equals(netid.mGid2) || !this.mSpname.equals(netid.mSpname) || !this.mMnoName.equalsIgnoreCase(netid.mMnoName)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int i = 1 * 17;
        String str = this.mMccMnc;
        int i2 = 0;
        int hashVal = (i + (str == null ? 0 : str.hashCode())) * 13;
        String str2 = this.mMnoName;
        int hashVal2 = (hashVal + (str2 == null ? 0 : str2.hashCode())) * 23;
        String str3 = this.mGid1;
        int hashVal3 = (hashVal2 + (str3 == null ? 0 : str3.hashCode())) * 19;
        String str4 = this.mGid2;
        int hashVal4 = (hashVal3 + (str4 == null ? 0 : str4.hashCode())) * 7;
        String str5 = this.mSubset;
        int hashVal5 = (hashVal4 + (str5 == null ? 0 : str5.hashCode())) * 31;
        String str6 = this.mSpname;
        if (str6 != null) {
            i2 = str6.hashCode();
        }
        return hashVal5 + i2;
    }

    public boolean equalsWithoutMnoName(NetworkIdentifier netid) {
        Log.i(LOG_TAG, "equalsWithoutMnoName: L" + toString() + ", R" + netid.toString());
        return this.mMccMnc.equals(netid.mMccMnc) && this.mSubset.equals(netid.mSubset) && this.mGid1.equals(netid.mGid1) && this.mGid2.equals(netid.mGid2) && this.mSpname.equals(netid.mSpname);
    }

    public void setMnoName(String mnoName) {
        this.mMnoName = mnoName;
    }

    public boolean contains(NetworkIdentifier netid) {
        Log.i(LOG_TAG, "contains: L" + toString() + ", R" + netid.toString());
        return this.mMccMnc.equals(netid.mMccMnc) && (this.mGid1.isEmpty() || this.mGid1.equals(netid.mGid1));
    }

    public String toString() {
        return "(" + this.mMccMnc + "," + this.mSubset + "," + this.mGid1 + "," + this.mGid2 + "," + this.mSpname + "=>" + this.mMnoName + ")";
    }
}
