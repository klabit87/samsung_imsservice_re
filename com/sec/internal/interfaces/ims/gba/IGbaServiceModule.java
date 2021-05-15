package com.sec.internal.interfaces.ims.gba;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.gba.params.GbaData;
import java.util.List;

public interface IGbaServiceModule {
    void getBtidAndGbaKey(HttpRequestParams httpRequestParams, String str, HttpResponseParams httpResponseParams, IGbaCallback iGbaCallback);

    List<IGbaCallback> getGbaCallbacks();

    String getImei(int i);

    String getImpi(int i);

    String getNafExternalKeyBase64Decoded(int i, byte[] bArr, byte[] bArr2);

    GbaData getPassword(String str, boolean z, int i);

    boolean initGbaAccessibleObj();

    boolean isGbaUiccSupported(int i);

    void resetGbaKey(String str, int i);

    void storeGbaBootstrapParams(byte[] bArr, String str, String str2);

    String storeGbaDataAndGenerateKey(String str, String str2, String str3, String str4, byte[] bArr, byte[] bArr2, GbaData gbaData, boolean z, int i);
}
