package com.sec.internal.helper;

import android.content.ContentValues;
import android.text.TextUtils;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {
    public static boolean isNullOrEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNullOrEmpty(Map<?, ?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNullOrEmpty(Object[] obj) {
        return obj == null || obj.length == 0;
    }

    public static boolean isNullOrEmpty(ContentValues cv) {
        return cv == null || cv.size() == 0;
    }

    public static boolean getBooleanValue(ContentValues values, String field, boolean defaultVal) {
        if (values == null || !values.containsKey(field)) {
            return defaultVal;
        }
        Boolean result = values.getAsBoolean(field);
        return result == null ? defaultVal : result.booleanValue();
    }

    public static int getIntValue(ContentValues values, String field, int defaultVal) {
        if (values == null || !values.containsKey(field)) {
            return defaultVal;
        }
        Integer result = values.getAsInteger(field);
        return result == null ? defaultVal : result.intValue();
    }

    public static String getStringValue(ContentValues values, String field, String defaultVal) {
        if (values == null || !values.containsKey(field)) {
            return defaultVal;
        }
        String result = values.getAsString(field);
        return result == null ? defaultVal : result;
    }

    public static class ArrayListMultimap<K, V> {
        private Map<K, Collection<V>> map = new HashMap();

        public Collection<V> get(Object key) {
            Collection<V> collection = this.map.get(key);
            if (collection == null) {
                return new ArrayList<>();
            }
            return collection;
        }

        public void put(K key, V value) {
            Collection<V> collection = this.map.get(key);
            if (collection == null) {
                collection = new ArrayList<>();
            }
            collection.add(value);
            this.map.put(key, collection);
        }
    }

    public static <K, V> ArrayListMultimap<K, V> createArrayListMultimap() {
        return new ArrayListMultimap<>();
    }

    public static class Partition<T> extends AbstractList<List<T>> {
        final List<T> list;
        final int size;

        Partition(List<T> list2, int size2) {
            this.list = list2;
            this.size = size2;
        }

        public List<T> get(int index) {
            int i = this.size;
            int start = index * i;
            return this.list.subList(start, Math.min(i + start, this.list.size()));
        }

        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        public int size() {
            int listSize = this.list.size();
            int i = this.size;
            if (listSize % i == 0) {
                return listSize / i;
            }
            return (listSize / i) + 1;
        }
    }

    public static <T> Partition<T> partition(List<T> list, int size) {
        return new Partition<>(list, size);
    }
}
