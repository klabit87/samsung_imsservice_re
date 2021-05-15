package com.sec.internal.ims.util;

import android.util.Base64;
import android.util.Log;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.ims.config.util.AkaResponse;
import com.sec.internal.ims.config.util.TelephonySupport;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.Locale;

public class AkaAuth {
    public static final String LOG_TAG = AkaAuth.class.getSimpleName();

    public static class AkaAuthenticationResponse {
        String mAuthKey;
        String mEncrKey;
        String mRes;

        public AkaAuthenticationResponse(String res, String encrKey, String authKey) {
            this.mRes = res;
            this.mEncrKey = encrKey;
            this.mAuthKey = authKey;
        }

        public String getRes() {
            return this.mRes;
        }

        public String getEncrKey() {
            return this.mEncrKey;
        }

        public String getAuthKey() {
            return this.mAuthKey;
        }
    }

    public static AkaAuthenticationResponse getAkaResponse(int phoneId, String challenge) {
        String nonce = StrUtil.bytesToHexString(Base64.decode(challenge.getBytes(), 2)).toUpperCase(Locale.US);
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        String isimRes = sm == null ? null : sm.getIsimAuthentication(nonce);
        if (isimRes == null) {
            Log.d(LOG_TAG, "getAkaResponse(): getIsimResponse is null.");
            return null;
        }
        try {
            AkaResponse res = TelephonySupport.buildAkaResponse(isimRes);
            if (res == null) {
                Log.d(LOG_TAG, "getAkaResponse(): response wrongly encoded.");
            } else if (!(res.getRes() == null || res.getCk() == null || res.getIk() == null)) {
                return new AkaAuthenticationResponse(StrUtil.bytesToHexString(res.getRes()), StrUtil.bytesToHexString(res.getCk()), StrUtil.bytesToHexString(res.getIk()));
            }
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "Parsing failed for response");
        }
        return null;
    }
}
