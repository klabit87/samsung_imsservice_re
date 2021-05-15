package com.google.firebase.internal;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

public interface InternalTokenProvider {
    String getUid();

    Task<GetTokenResult> zzcj(boolean z);
}
