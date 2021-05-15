package com.sec.internal.interfaces.ims.config;

import android.content.Context;
import android.database.Cursor;
import java.util.Map;

public interface IStorageAdapter {
    void close();

    int delete(String str);

    boolean deleteAll();

    String getIdentity();

    int getState();

    void open(Context context, String str, int i);

    Cursor query(String[] strArr);

    String read(String str);

    Map<String, String> readAll(String str);

    void setDBTableMax(int i);

    boolean write(String str, String str2);

    boolean writeAll(Map<String, String> map);
}
