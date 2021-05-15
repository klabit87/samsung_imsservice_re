package com.sec.internal.helper.translate;

import java.util.Map;

public class MapTranslator<T, S> {
    private Map<T, S> mMap;

    public MapTranslator(Map<T, S> m) {
        this.mMap = m;
    }

    public S translate(T t) {
        return this.mMap.get(t);
    }
}
