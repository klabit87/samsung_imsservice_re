package com.sec.internal.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PhoneIdKeyMap<E> {
    private final E mDefaultValue;
    private final Map<Integer, E> mMap = new HashMap();
    private final int mSize;

    public PhoneIdKeyMap(int size, E defaultValue) {
        this.mSize = size;
        this.mDefaultValue = defaultValue;
    }

    public void put(int phoneId, E value) {
        if (phoneId >= 0 && phoneId < this.mSize) {
            this.mMap.put(Integer.valueOf(phoneId), value);
        }
    }

    public E get(int phoneId) {
        E value = this.mMap.get(Integer.valueOf(phoneId));
        if (value != null || this.mDefaultValue == null) {
            return value;
        }
        return this.mDefaultValue;
    }

    public E remove(int phoneId) {
        return this.mMap.remove(Integer.valueOf(phoneId));
    }

    public int getKey(E value, int defaultKey) {
        for (Map.Entry<Integer, E> entry : this.mMap.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey().intValue();
            }
        }
        return defaultKey;
    }

    public void clear() {
        this.mMap.clear();
    }

    public Collection<E> values() {
        return this.mMap.values();
    }
}
