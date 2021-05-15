package com.sec.internal.ims.core.sim;

import android.content.Context;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.ims.core.sim.MnoMap;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: MnoMap */
class RssNetParser {
    private static final String LOG_TAG = "NetworkIdentifier";
    private Context mContext;
    private ArrayList<String> mGcBlockMccList = new ArrayList<>();
    private ArrayList<NetworkIdentifier> mIdentifiers = new ArrayList<>();
    private int mPhoneId;

    public RssNetParser(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
    }

    /* access modifiers changed from: protected */
    public ArrayList<NetworkIdentifier> getRssNetwokrInfo() {
        parseNetworkInfo();
        return this.mIdentifiers;
    }

    public ArrayList<String> getGcBlockList() {
        return this.mGcBlockMccList;
    }

    private void parseNetworkInfo() {
        Log.i(LOG_TAG, "parseNetworkInfo: getResources from mnomap.json");
        InputStream inputStream = this.mContext.getResources().openRawResource(R.raw.mnomap);
        try {
            JsonParser parser = new JsonParser();
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
            JsonElement element = parser.parse(reader);
            reader.close();
            JsonObject object = element.getAsJsonObject();
            JsonArray mnomapArray = object.getAsJsonArray(MnoMap.Param.MNOMAP);
            JsonArray gcMccBlockArray = object.getAsJsonArray(ImsAutoUpdate.TAG_GC_BLOCK_MCC_LIST);
            if (gcMccBlockArray != null && !gcMccBlockArray.isJsonNull() && gcMccBlockArray.size() > 0) {
                this.mGcBlockMccList.clear();
                Iterator it = gcMccBlockArray.iterator();
                while (it.hasNext()) {
                    this.mGcBlockMccList.add(((JsonElement) it.next()).getAsString());
                }
            }
            if (mnomapArray == null || !mnomapArray.isJsonArray()) {
                Log.e(LOG_TAG, "array is null. Check your mnomap.json.");
            } else {
                Iterator it2 = mnomapArray.iterator();
                while (it2.hasNext()) {
                    JsonObject obj = ((JsonElement) it2.next()).getAsJsonObject();
                    String mccmnc = obj.get(MnoMap.Param.MCCMNC).getAsString();
                    String subset = obj.get(MnoMap.Param.SUBSET).getAsString();
                    String gid1 = obj.get(MnoMap.Param.GID1).getAsString().toUpperCase();
                    String gid2 = obj.get(MnoMap.Param.GID2).getAsString().toUpperCase();
                    String spname = obj.get(MnoMap.Param.SPNAME).getAsString();
                    String mnoName = obj.get("mnoname").getAsString();
                    ArrayList<NetworkIdentifier> arrayList = this.mIdentifiers;
                    NetworkIdentifier networkIdentifier = r12;
                    NetworkIdentifier networkIdentifier2 = new NetworkIdentifier(mccmnc, subset, gid1, gid2, spname, mnoName);
                    arrayList.add(networkIdentifier);
                }
                ImsAutoUpdate autoCache = ImsAutoUpdate.getInstance(this.mContext, this.mPhoneId);
                autoCache.loadMnomapAutoUpdate();
                if (ImsSharedPrefHelper.getBoolean(this.mPhoneId, this.mContext, ImsSharedPrefHelper.CARRIER_ID, "needMnoUpdate", false)) {
                    autoCache.loadCarrierFeature(this.mPhoneId);
                }
                if (autoCache.isForceSMKUpdate().booleanValue()) {
                    applyAutoUpdate(autoCache, 1);
                    applyAutoUpdate(autoCache, 2);
                    applyAutoUpdate(autoCache, 4);
                    applyAutoUpdate(autoCache, 0);
                } else {
                    applyAutoUpdate(autoCache, 0);
                    applyAutoUpdate(autoCache, 1);
                    applyAutoUpdate(autoCache, 2);
                    applyAutoUpdate(autoCache, 4);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th2;
        }
    }

    private void applyAutoUpdate(ImsAutoUpdate autoCache, int resource) {
        String str;
        JsonArray addMnomap;
        JsonElement add;
        String spname;
        Iterator it;
        JsonArray removeMnomap;
        JsonElement remove;
        String spname2;
        ImsAutoUpdate imsAutoUpdate = autoCache;
        int i = resource;
        if (imsAutoUpdate != null) {
            JsonElement remove2 = imsAutoUpdate.getMnomap(i, ImsAutoUpdate.TAG_MNOMAP_REMOVE);
            boolean isJsonNull = remove2.isJsonNull();
            String str2 = MnoMap.Param.SPNAME;
            if (isJsonNull || !remove2.isJsonArray()) {
            } else {
                JsonArray removeMnomap2 = remove2.getAsJsonArray();
                if (removeMnomap2.isJsonNull() || removeMnomap2.size() <= 0) {
                    JsonArray jsonArray = removeMnomap2;
                } else {
                    Iterator it2 = removeMnomap2.iterator();
                    while (it2.hasNext()) {
                        JsonObject obj = ((JsonElement) it2.next()).getAsJsonObject();
                        if (!obj.has(MnoMap.Param.MCCMNC) || !obj.has(MnoMap.Param.SUBSET)) {
                            remove = remove2;
                            removeMnomap = removeMnomap2;
                            it = it2;
                        } else if (!obj.has(MnoMap.Param.GID1) || !obj.has(MnoMap.Param.GID2)) {
                            remove = remove2;
                            removeMnomap = removeMnomap2;
                            it = it2;
                        } else if (obj.has("mnoname")) {
                            String mccmnc = obj.get(MnoMap.Param.MCCMNC).getAsString();
                            String subset = obj.get(MnoMap.Param.SUBSET).getAsString();
                            String gid1 = obj.get(MnoMap.Param.GID1).getAsString().toUpperCase();
                            String gid2 = obj.get(MnoMap.Param.GID2).getAsString().toUpperCase();
                            if (obj.has(str2)) {
                                spname2 = obj.get(str2).getAsString();
                            } else {
                                spname2 = "";
                            }
                            remove = remove2;
                            String mnoName = obj.get("mnoname").getAsString();
                            removeMnomap = removeMnomap2;
                            it = it2;
                            this.mIdentifiers.remove(new NetworkIdentifier(mccmnc, subset, gid1, gid2, spname2, mnoName));
                            Log.i(LOG_TAG, "AutoUpdate : remove MnoMap" + mnoName);
                        } else {
                            remove = remove2;
                            removeMnomap = removeMnomap2;
                            it = it2;
                        }
                        remove2 = remove;
                        removeMnomap2 = removeMnomap;
                        it2 = it;
                    }
                    JsonArray jsonArray2 = removeMnomap2;
                }
            }
            JsonElement add2 = imsAutoUpdate.getMnomap(i, ImsAutoUpdate.TAG_MNOMAP_ADD);
            if (add2.isJsonNull() || !add2.isJsonArray()) {
            } else {
                JsonArray addMnomap2 = add2.getAsJsonArray();
                if (addMnomap2.isJsonNull() || addMnomap2.size() <= 0) {
                    JsonArray jsonArray3 = addMnomap2;
                } else {
                    Iterator it3 = addMnomap2.iterator();
                    while (it3.hasNext()) {
                        JsonObject obj2 = ((JsonElement) it3.next()).getAsJsonObject();
                        if (!obj2.has(MnoMap.Param.MCCMNC) || !obj2.has(MnoMap.Param.SUBSET)) {
                            add = add2;
                            addMnomap = addMnomap2;
                            str = str2;
                        } else if (!obj2.has(MnoMap.Param.GID1)) {
                            add = add2;
                            addMnomap = addMnomap2;
                            str = str2;
                        } else if (!obj2.has(MnoMap.Param.GID2) || !obj2.has("mnoname")) {
                            add = add2;
                            addMnomap = addMnomap2;
                            str = str2;
                        } else {
                            String mccmnc2 = obj2.get(MnoMap.Param.MCCMNC).getAsString();
                            String subset2 = obj2.get(MnoMap.Param.SUBSET).getAsString();
                            String gid12 = obj2.get(MnoMap.Param.GID1).getAsString().toUpperCase();
                            String gid22 = obj2.get(MnoMap.Param.GID2).getAsString().toUpperCase();
                            add = add2;
                            String mnoName2 = obj2.get("mnoname").getAsString();
                            if (obj2.has(str2)) {
                                spname = obj2.get(str2).getAsString();
                            } else {
                                spname = "";
                            }
                            addMnomap = addMnomap2;
                            str = str2;
                            this.mIdentifiers.add(new NetworkIdentifier(mccmnc2, subset2, gid12, gid22, spname, mnoName2));
                            Log.i(LOG_TAG, "AutoUpdate : add MnoMap : " + mnoName2);
                        }
                        add2 = add;
                        addMnomap2 = addMnomap;
                        str2 = str;
                    }
                    JsonArray jsonArray4 = addMnomap2;
                }
            }
            JsonElement gcBlockReplaceList = imsAutoUpdate.getMnomap(i, ImsAutoUpdate.TAG_REPLACE_GC_BLOCK_MCC_LIST);
            if (!gcBlockReplaceList.isJsonNull() && gcBlockReplaceList.isJsonArray()) {
                JsonArray gcBlocReplacekListArray = gcBlockReplaceList.getAsJsonArray();
                if (!gcBlocReplacekListArray.isJsonNull() && gcBlocReplacekListArray.size() > 0) {
                    this.mGcBlockMccList.clear();
                    Iterator it4 = gcBlocReplacekListArray.iterator();
                    while (it4.hasNext()) {
                        this.mGcBlockMccList.add(((JsonElement) it4.next()).getAsString());
                    }
                }
            }
        }
    }
}
