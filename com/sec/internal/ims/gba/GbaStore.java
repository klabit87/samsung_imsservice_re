package com.sec.internal.ims.gba;

import java.util.HashMap;
import java.util.Map;

public class GbaStore {
    private static final String LOG_TAG = GbaStore.class.getSimpleName();
    private Map<Gbakey, GbaValue> map = new HashMap();

    protected GbaStore() {
    }

    public void putKeys(Gbakey key, GbaValue value) {
        this.map.put(key, value);
    }

    public GbaValue getKeys(Gbakey key) {
        return this.map.get(key);
    }

    public boolean hasKey(Gbakey key) {
        return this.map.containsKey(key);
    }

    public void removeKey(Gbakey key) {
        this.map.remove(key);
    }

    public String toString() {
        return "GbaStore [map=" + this.map + "]";
    }
}
