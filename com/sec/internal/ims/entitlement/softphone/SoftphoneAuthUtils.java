package com.sec.internal.ims.entitlement.softphone;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.util.Base64;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsProfileLoader;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.ims.entitlement.softphone.responses.AkaAuthenticationResponse;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SoftphoneAuthUtils {
    private static final String AUTH_NO_ERROR = "DB";
    private static final String LOG_TAG = SoftphoneAuthUtils.class.getSimpleName();
    private static final Map<String, String> mProdAppKeyMap;
    private static final Map<String, String> mProdAppSecretMap;

    static {
        HashMap hashMap = new HashMap();
        mProdAppKeyMap = hashMap;
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.DAVINCI, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_DAVINCI);
        mProdAppKeyMap.put(SoftphoneNamespaces.SoftphoneModels.RENOIR, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_RENOIR);
        mProdAppKeyMap.put(SoftphoneNamespaces.SoftphoneModels.CHAGALL, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_CHAGALL);
        mProdAppKeyMap.put(SoftphoneNamespaces.SoftphoneModels.KLIMT, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_KLIMT);
        mProdAppKeyMap.put(SoftphoneNamespaces.SoftphoneModels.S2, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S2);
        mProdAppKeyMap.put(SoftphoneNamespaces.SoftphoneModels.S4, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S4);
        mProdAppKeyMap.put(SoftphoneNamespaces.SoftphoneModels.A8, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_A8);
        mProdAppKeyMap.put(SoftphoneNamespaces.SoftphoneModels.A4S, "dyp77kwaauqxx6aalgpjjq3ctoq1dzwk");
        mProdAppKeyMap.put(SoftphoneNamespaces.SoftphoneModels.S7L, "dyp77kwaauqxx6aalgpjjq3ctoq1dzwk");
        HashMap hashMap2 = new HashMap();
        mProdAppSecretMap = hashMap2;
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.DAVINCI, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_DAVINCI);
        mProdAppSecretMap.put(SoftphoneNamespaces.SoftphoneModels.RENOIR, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_RENOIR);
        mProdAppSecretMap.put(SoftphoneNamespaces.SoftphoneModels.CHAGALL, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_CHAGALL);
        mProdAppSecretMap.put(SoftphoneNamespaces.SoftphoneModels.KLIMT, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_KLIMT);
        mProdAppSecretMap.put(SoftphoneNamespaces.SoftphoneModels.S2, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S2);
        mProdAppSecretMap.put(SoftphoneNamespaces.SoftphoneModels.S4, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S4);
        mProdAppSecretMap.put(SoftphoneNamespaces.SoftphoneModels.A8, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_A8);
        mProdAppSecretMap.put(SoftphoneNamespaces.SoftphoneModels.A4S, "tuqlgat1pdra8x1mjxdnawt7psgxzsgo");
        mProdAppSecretMap.put(SoftphoneNamespaces.SoftphoneModels.S7L, "tuqlgat1pdra8x1mjxdnawt7psgxzsgo");
    }

    private SoftphoneAuthUtils() {
    }

    public static String setupAppKey(int environment, String productName) {
        return (1 != environment || productName == null) ? SoftphoneNamespaces.SoftphoneSettings.STAGE_APP_KEY : mProdAppKeyMap.get(productName);
    }

    public static String setupAppSecret(int environment, String productName) {
        return (1 != environment || productName == null) ? SoftphoneNamespaces.SoftphoneSettings.STAGE_APP_SECRET : mProdAppSecretMap.get(productName);
    }

    public static String[] splitRandAutn(String nonce) {
        String rand = "";
        String autn = "";
        String str = LOG_TAG;
        IMSLog.i(str, "nonce: " + nonce + " length: " + nonce.length());
        if (nonce.length() > 2) {
            int len = Integer.parseInt(nonce.substring(0, 2), 16) * 2;
            String str2 = LOG_TAG;
            IMSLog.i(str2, "rand length: " + len);
            rand = nonce.substring(2, len + 2);
            if (nonce.length() > len + 4) {
                String nextPart = nonce.substring(len + 2);
                int len2 = Integer.parseInt(nextPart.substring(0, 2), 16) * 2;
                autn = nextPart.substring(2, len2 + 2);
                String str3 = LOG_TAG;
                IMSLog.i(str3, "autn length: " + len2);
            }
            String str4 = LOG_TAG;
            IMSLog.i(str4, "rand: " + rand);
            String str5 = LOG_TAG;
            IMSLog.i(str5, "autn: " + autn);
        } else {
            IMSLog.i(LOG_TAG, "wrong nonce format");
        }
        byte[] randBytes = StrUtil.hexStringToBytes(rand);
        byte[] autnBytes = StrUtil.hexStringToBytes(autn);
        String randBase64 = Base64.encodeToString(randBytes, 2);
        String autnBase64 = Base64.encodeToString(autnBytes, 2);
        String str6 = LOG_TAG;
        IMSLog.i(str6, "base64 randStr: " + randBase64);
        String str7 = LOG_TAG;
        IMSLog.i(str7, "base64 autnStr: " + autnBase64);
        return new String[]{randBase64, autnBase64};
    }

    public static String processAkaAuthenticationResponse(AkaAuthenticationResponse response) {
        IMSLog.i(LOG_TAG, "processAkaAuthenticationResponse()");
        if (response == null || response.mChallengeResponse == null) {
            return "";
        }
        return ((AUTH_NO_ERROR + StrUtil.convertByteToHexWithLength(Base64.decode(response.mChallengeResponse.mAuthenticationResponse, 0))) + StrUtil.convertByteToHexWithLength(Base64.decode(response.mChallengeResponse.mCipherKey, 0))) + StrUtil.convertByteToHexWithLength(Base64.decode(response.mChallengeResponse.mIntegrityKey, 0));
    }

    private static List<ImsProfile> getSoftphoneProfileList(Context ctx) {
        ArrayList<ImsProfile> profiles = new ArrayList<>();
        Cursor cursor = ctx.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/profile"), (String[]) null, "mdmn_type=Softphone", (String[]) null, (String) null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    profiles.add(ImsProfileLoader.getImsProfileFromRow(ctx, cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return profiles;
    }

    public static String getDeviceType(Context ctx) {
        return (ctx.getResources().getConfiguration().screenLayout & 15) >= 3 ? "Tablet" : "Phone";
    }

    private static String getSccLabel(Context ctx, String accountId, int userid) {
        IMSLog.i(LOG_TAG, "getSccLabel()");
        Cursor cursor = ctx.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildAccountLabelUri(accountId, (long) userid), (String[]) null, (String) null, (String[]) null, (String) null);
        String sccLabel = "";
        if (cursor != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "getSccLabel: found " + cursor.getCount() + " records");
            if (cursor.moveToFirst()) {
                sccLabel = cursor.getString(cursor.getColumnIndex("label"));
                if (sccLabel != null && sccLabel.length() > 40) {
                    sccLabel = sccLabel.substring(0, 40);
                } else if (sccLabel == null) {
                    sccLabel = "";
                }
            }
            cursor.close();
        }
        return sccLabel;
    }

    public static ImsProfile createProfileFromTemplate(Context ctx, ImsNetworkIdentity identity, String accountId, int userId) {
        IMSLog.i(LOG_TAG, "createProfileFromTemplate:");
        if (!SimUtil.isSoftphoneEnabled()) {
            return null;
        }
        List<ImsProfile> profiles = getSoftphoneProfileList(ctx);
        if (!profiles.isEmpty()) {
            ImsProfile profileTemplate = profiles.get(0);
            String str = LOG_TAG;
            IMSLog.s(str, "profileTemplate=" + profileTemplate);
            if (!identity.impiEmpty()) {
                String str2 = LOG_TAG;
                IMSLog.s(str2, "identity: " + identity.toString());
                Parcel template = Parcel.obtain();
                profileTemplate.writeToParcel(template, 0);
                template.setDataPosition(0);
                ImsProfile profile = (ImsProfile) ImsProfile.CREATOR.createFromParcel(template);
                profile.setImpi(identity.getImpi());
                profile.setImpuList(identity.getImpu());
                List<String> addressList = identity.getAddressList();
                if (addressList != null && addressList.size() > 0) {
                    profile.setPcscfList(addressList);
                    profile.setPcscfPreference(2);
                }
                profile.setAppId(identity.getAppId());
                profile.setDisplayName(getSccLabel(ctx, accountId, userId));
                template.recycle();
                String str3 = LOG_TAG;
                IMSLog.s(str3, "inject profile=" + profile);
                return profile;
            }
        }
        return null;
    }
}
