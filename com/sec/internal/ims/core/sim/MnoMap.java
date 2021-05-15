package com.sec.internal.ims.core.sim;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.sec.internal.constants.Mno;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MnoMap {
    public static final String LOG_TAG = "MnoMap";
    private Context mContext;
    private CscNetParser mCscNetParser;
    private int mPhoneId = 0;
    private RssNetParser mRssNetParser;
    private Map<String, List<NetworkIdentifier>> mTable = new ArrayMap();

    public static class Param {
        public static final String GID1 = "gid1";
        public static final String GID2 = "gid2";
        public static final String MCCMNC = "mccmnc";
        public static final String MNOMAP = "mnomap";
        public static final String MNONAME = "mnoname";
        public static final String SPNAME = "spname";
        public static final String SUBSET = "subset";
    }

    public MnoMap(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mCscNetParser = new CscNetParser(phoneId);
        this.mRssNetParser = new RssNetParser(this.mContext, this.mPhoneId);
        createTable();
    }

    public void createTable() {
        Log.i(LOG_TAG, "createTable: init");
        Iterator<NetworkIdentifier> it = this.mRssNetParser.getRssNetwokrInfo().iterator();
        while (it.hasNext()) {
            NetworkIdentifier rssNetid = it.next();
            String key = rssNetid.mMccMnc;
            List<NetworkIdentifier> netIdList = this.mTable.get(key);
            if (netIdList == null) {
                netIdList = new ArrayList<>();
            }
            netIdList.add(rssNetid);
            this.mTable.put(key, netIdList);
        }
        Log.i(LOG_TAG, "from RSS : ");
        Iterator<CscNetwork> it2 = this.mCscNetParser.getCscNetwokrInfo().iterator();
        while (it2.hasNext()) {
            CscNetwork cscNetwork = it2.next();
            boolean found = false;
            Iterator<NetworkIdentifier> it3 = cscNetwork.mIdentifiers.iterator();
            while (it3.hasNext()) {
                NetworkIdentifier cscNetid = it3.next();
                List<NetworkIdentifier> netIdList2 = this.mTable.get(cscNetid.mMccMnc);
                if (netIdList2 != null) {
                    Iterator<NetworkIdentifier> it4 = netIdList2.iterator();
                    while (true) {
                        if (!it4.hasNext()) {
                            break;
                        }
                        NetworkIdentifier tableNetid = it4.next();
                        if (tableNetid.equalsWithoutMnoName(cscNetid)) {
                            cscNetwork.setMnoName(tableNetid.mMnoName);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
            if (found) {
                Log.i(LOG_TAG, "createTable merge: " + cscNetwork.mNetworkName);
                Iterator<NetworkIdentifier> it5 = cscNetwork.mIdentifiers.iterator();
                while (it5.hasNext()) {
                    NetworkIdentifier cscNetid2 = it5.next();
                    boolean merged = false;
                    List<NetworkIdentifier> netIdList3 = this.mTable.get(cscNetid2.mMccMnc);
                    if (netIdList3 != null) {
                        Iterator<NetworkIdentifier> it6 = netIdList3.iterator();
                        while (true) {
                            if (!it6.hasNext()) {
                                break;
                            }
                            NetworkIdentifier tableNetid2 = it6.next();
                            if (tableNetid2.contains(cscNetid2)) {
                                Log.i(LOG_TAG, "skip: " + tableNetid2.toString() + "contains " + cscNetid2.toString());
                                merged = true;
                                break;
                            }
                        }
                    }
                    if (!merged) {
                        Log.i(LOG_TAG, "add new networkd identifier: " + cscNetid2.toString());
                        List<NetworkIdentifier> netIdList4 = this.mTable.get(cscNetid2.mMccMnc);
                        if (netIdList4 == null) {
                            netIdList4 = new ArrayList<>();
                        }
                        netIdList4.add(cscNetid2);
                        this.mTable.put(cscNetid2.mMccMnc, netIdList4);
                    }
                }
            } else {
                Log.i(LOG_TAG, "not found Mno for " + cscNetwork.mNetworkName);
            }
        }
        Log.i(LOG_TAG, "createTable: result");
    }

    public boolean isGcBlockListContains(String mccmnc) {
        if (mccmnc == null || mccmnc.length() < 3 || this.mRssNetParser.getGcBlockList().contains("*")) {
            return true;
        }
        return this.mRssNetParser.getGcBlockList().contains(mccmnc.substring(0, 3));
    }

    public String getMnoName(String mccmnc, String imsi, String gid1, String gid2, String spname) {
        List<NetworkIdentifier> netIdList = this.mTable.get(mccmnc);
        String mnoName = (isGcBlockListContains(mccmnc) ? Mno.DEFAULT : Mno.GOOGLEGC).getName();
        if (netIdList == null) {
            return mnoName;
        }
        String simSubset = imsi.substring(mccmnc.length());
        Iterator<NetworkIdentifier> it = netIdList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            NetworkIdentifier tableNetid = it.next();
            if (!TextUtils.isEmpty(tableNetid.mSubset)) {
                int count_x = 0;
                while (true) {
                    try {
                        if (tableNetid.mSubset.charAt(count_x) != 'x') {
                            if (tableNetid.mSubset.charAt(count_x) != 'X') {
                                break;
                            }
                        }
                        count_x++;
                    } catch (StringIndexOutOfBoundsException e) {
                        Log.e(LOG_TAG, "invalid subset - mnomap:" + tableNetid.mSubset + ", SIM:" + simSubset);
                        e.printStackTrace();
                    }
                }
                if (simSubset.startsWith(tableNetid.mSubset.substring(count_x), count_x)) {
                    mnoName = tableNetid.mMnoName;
                    break;
                }
            }
            if (TextUtils.isEmpty(tableNetid.mGid1) || TextUtils.isEmpty(gid1) || !gid1.toUpperCase().startsWith(tableNetid.mGid1.toUpperCase())) {
                if (!TextUtils.isEmpty(tableNetid.mGid2) && !TextUtils.isEmpty(gid2) && gid2.toUpperCase().startsWith(tableNetid.mGid2.toUpperCase())) {
                    mnoName = tableNetid.mMnoName;
                    break;
                }
                if (!TextUtils.isEmpty(tableNetid.mSpname) && !TextUtils.isEmpty(spname)) {
                    tableNetid.mSpname = tableNetid.mSpname.trim();
                    spname = spname.trim();
                    if (!TextUtils.isEmpty(tableNetid.mSpname) && !TextUtils.isEmpty(spname) && spname.equalsIgnoreCase(tableNetid.mSpname)) {
                        mnoName = tableNetid.mMnoName;
                        break;
                    }
                }
                if (TextUtils.isEmpty(tableNetid.mSubset) && TextUtils.isEmpty(tableNetid.mGid1) && TextUtils.isEmpty(tableNetid.mGid2) && TextUtils.isEmpty(tableNetid.mSpname)) {
                    mnoName = tableNetid.mMnoName;
                }
            } else {
                mnoName = tableNetid.mMnoName;
                break;
            }
        }
        Log.i(LOG_TAG, "getMnoName: (" + mccmnc + "," + gid1 + "," + gid2 + "," + spname + ") => " + mnoName);
        return mnoName;
    }
}
