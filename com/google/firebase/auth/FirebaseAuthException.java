package com.google.firebase.auth;

import com.google.android.gms.common.internal.zzbq;
import com.google.firebase.FirebaseException;

public class FirebaseAuthException extends FirebaseException {
    private final String zzmpq;

    public FirebaseAuthException(String str, String str2) {
        super(str2);
        this.zzmpq = zzbq.zzgv(str);
    }

    public String getErrorCode() {
        return this.zzmpq;
    }
}
