package com.google.android.gms.internal;

public enum zzfld {
    INT(0),
    LONG(0L),
    FLOAT(Float.valueOf(0.0f)),
    DOUBLE(Double.valueOf(0.0d)),
    BOOLEAN(false),
    STRING(""),
    BYTE_STRING(zzfgs.zzpnw),
    ENUM((String) null),
    MESSAGE((String) null);
    
    private final Object zzpvc;

    private zzfld(Object obj) {
        this.zzpvc = obj;
    }
}
