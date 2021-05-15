package com.sec.internal.helper.os;

import com.samsung.android.feature.SemCscFeature;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.util.Hashtable;

public class ImsCscFeature {
    private static volatile ImsCscFeature sInstance;
    private SemCscFeature mCscFeature = SemCscFeature.getInstance();
    private Hashtable<String, String> mFeatureList = new Hashtable<>();
    private Hashtable<String, String> mFeatureList_2 = new Hashtable<>();

    public static ImsCscFeature getInstance() {
        if (sInstance == null) {
            synchronized (ImsCscFeature.class) {
                if (sInstance == null) {
                    sInstance = new ImsCscFeature();
                }
            }
        }
        return sInstance;
    }

    public String getString(String tag) {
        if (this.mFeatureList.containsKey(tag)) {
            return this.mFeatureList.get(tag);
        }
        return this.mCscFeature.getString(tag);
    }

    public String getString(int slotId, String tag) {
        if (slotId != 1) {
            return getString(tag);
        }
        if (this.mFeatureList_2.containsKey(tag)) {
            return this.mFeatureList_2.get(tag);
        }
        return this.mCscFeature.getString(slotId, tag);
    }

    public boolean getBoolean(String tag) {
        if (this.mFeatureList.containsKey(tag)) {
            return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(this.mFeatureList.get(tag));
        }
        return this.mCscFeature.getBoolean(tag);
    }

    public boolean getBoolean(int slotId, String tag) {
        if (slotId != 1) {
            return getBoolean(tag);
        }
        if (this.mFeatureList_2.containsKey(tag)) {
            return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(this.mFeatureList_2.get(tag));
        }
        return this.mCscFeature.getBoolean(slotId, tag);
    }

    public void put(String tag, String value) {
        this.mFeatureList.put(tag, value);
    }

    public void put(int slotId, String tag, String value) {
        if (slotId != 1) {
            put(tag, value);
        } else {
            this.mFeatureList_2.put(tag, value);
        }
    }

    public void remove(String tag) {
        this.mFeatureList.remove(tag);
    }

    public void remove(int slotId, String tag) {
        if (slotId != 1) {
            remove(tag);
        } else {
            this.mFeatureList_2.remove(tag);
        }
    }

    public void clear() {
        this.mFeatureList.clear();
    }

    public void clear(int slotId) {
        if (slotId != 1) {
            clear();
        } else {
            this.mFeatureList_2.clear();
        }
    }
}
