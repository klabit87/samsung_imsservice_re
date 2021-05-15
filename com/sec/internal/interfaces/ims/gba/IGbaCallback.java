package com.sec.internal.interfaces.ims.gba;

import com.sec.internal.helper.httpclient.HttpResponseParams;
import java.io.IOException;

public interface IGbaCallback {
    void onComplete(String str, String str2, boolean z, HttpResponseParams httpResponseParams);

    void onFail(IOException iOException);
}
