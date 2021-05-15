package com.sec.internal.ims.core.sim;

import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: MnoMap */
class CscNetwork {
    private static final String LOG_TAG = "CscNetwork";
    public ArrayList<NetworkIdentifier> mIdentifiers = new ArrayList<>();
    public String mNetworkName;

    public CscNetwork(String networkName) {
        this.mNetworkName = networkName;
    }

    public void addIdentifier(String mccmnc, String subset, String gid1, String gid2, String spname) {
        Log.i(LOG_TAG, "addIdentifier for " + this.mNetworkName + " - mccmnc:" + mccmnc + ", subset: " + subset + ", gid1: " + gid1 + ", gid2: " + gid2 + ", spname: " + spname);
        this.mIdentifiers.add(new NetworkIdentifier(mccmnc, subset, gid1, gid2, spname));
    }

    public void setMnoName(String mnoName) {
        Log.i(LOG_TAG, "setMnoName for " + this.mNetworkName + " " + mnoName);
        Iterator<NetworkIdentifier> it = this.mIdentifiers.iterator();
        while (it.hasNext()) {
            it.next().setMnoName(mnoName);
        }
    }
}
