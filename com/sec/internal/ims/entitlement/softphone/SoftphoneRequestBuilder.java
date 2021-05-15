package com.sec.internal.ims.entitlement.softphone;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.sec.internal.constants.ims.XmlCreator;
import com.sec.internal.constants.ims.XmlElement;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.ims.cmstore.CloudMessageIntent;
import com.sec.internal.ims.entitlement.softphone.requests.AddAddressRequest;
import com.sec.internal.ims.entitlement.softphone.requests.AddressValidationRequest;
import com.sec.internal.ims.entitlement.softphone.requests.ExchangeForAccessTokenRequest;
import com.sec.internal.ims.entitlement.softphone.requests.ProvisionAccountRequest;
import com.sec.internal.ims.entitlement.softphone.requests.ReleaseImsNetworkIdentifiersRequest;
import com.sec.internal.ims.entitlement.softphone.requests.RevokeTokenRequest;
import com.sec.internal.ims.entitlement.softphone.requests.SendSMSRequest;
import com.sec.internal.ims.entitlement.util.EncryptionHelper;
import com.sec.internal.ims.entitlement.util.SharedPrefHelper;
import com.sec.internal.log.IMSLog;
import com.sec.vsim.attsoftphone.data.CallForwardingInfo;
import com.sec.vsim.attsoftphone.data.CallWaitingInfo;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import javax.crypto.SecretKey;

public class SoftphoneRequestBuilder {
    private static final String LOG_TAG = SoftphoneRequestBuilder.class.getSimpleName();
    private final Context mContext;

    public SoftphoneRequestBuilder(Context context) {
        this.mContext = context;
    }

    public static ExchangeForAccessTokenRequest buildExchangeForAccessTokenRequest(String appKey, String appSecret, String accountId, String password) {
        return new ExchangeForAccessTokenRequest(appKey, appSecret, accountId, CloudMessageProviderContract.VVMAccountInfoColumns.PASSWORD, password, SoftphoneNamespaces.SoftphoneSettings.SCOPE);
    }

    public static RevokeTokenRequest buildRevokeTokenRequest(String appKey, String appSecret, String token, String tokenType) {
        return new RevokeTokenRequest(appKey, appSecret, token, tokenType);
    }

    public static ProvisionAccountRequest buildProvisionAccountRequest() {
        return new ProvisionAccountRequest("Yes");
    }

    public AddressValidationRequest buildAddressValidationRequest(int addressId, boolean confirmed) {
        int i = addressId;
        boolean z = confirmed;
        String str = LOG_TAG;
        Log.i(str, "buildAddressValidationRequest [addressId: " + i + ", confirmed: " + z + "]");
        Cursor cursor = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAddress.buildAddressUri((long) i), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            AddressValidationRequest request = new AddressValidationRequest(new AddressValidationRequest.Address("ATT WiFi Calling", cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.HOUSE_NUMBER)), cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.HOUSE_NUMBER_EXTENSION)), cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.STREET_DIRECTION_PREFIX)), cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.STREET_NAME)), cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.STREET_NAME_SUFFIX)), cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.STREET_DIRECTION_SUFFIX)), cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.CITY)), cursor.getString(cursor.getColumnIndex("state")), cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.ZIP)), cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.ADDITIONAL_ADDRESS_INFO))), z ? CloudMessageProviderContract.JsonData.TRUE : ConfigConstants.VALUE.INFO_COMPLETED);
            cursor.close();
            return request;
        }
        cursor.close();
        return null;
    }

    public AddAddressRequest buildAddAddressRequest(int addressId) {
        String str = LOG_TAG;
        Log.i(str, "buildAddAddressRequest [addressId: " + addressId + "]");
        Cursor cursor = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAddress.buildAddressUri((long) addressId), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            AddAddressRequest request = new AddAddressRequest(getDelimitedAddressString(cursor));
            cursor.close();
            return request;
        }
        cursor.close();
        return null;
    }

    public static String getDelimitedAddressString(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex("name")) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.HOUSE_NUMBER)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.HOUSE_NUMBER_EXTENSION)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.STREET_DIRECTION_PREFIX)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.STREET_NAME)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.STREET_NAME_SUFFIX)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.STREET_DIRECTION_SUFFIX)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.CITY)) + ";" + cursor.getString(cursor.getColumnIndex("state")) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.ZIP)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.ADDITIONAL_ADDRESS_INFO)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.E911AID)) + ";" + cursor.getString(cursor.getColumnIndex(SoftphoneContract.AddressColumns.EXPIRE_DATE));
    }

    public static ReleaseImsNetworkIdentifiersRequest buildReleaseImsNetworkIdentifiersRequest(String impi, String impu) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildReleaseImsNetworkIdentifiersRequest [IMPI: " + impi + ", IMPU: " + impu + "]");
        return new ReleaseImsNetworkIdentifiersRequest(impi, impu);
    }

    public static String buildSetCallWaitingInfoRequest(CallWaitingInfo info) {
        return XmlCreator.toXml(new XmlElement("communication-waiting", (String) null, "ss").addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, info.mActive ? CloudMessageProviderContract.JsonData.TRUE : ConfigConstants.VALUE.INFO_COMPLETED), "1.0", "UTF-8");
    }

    public static String buildSetCallForwardingInfoRequest(CallForwardingInfo info) {
        XmlElement rule;
        XmlElement callForwarding = new XmlElement("communication-diversion", (String) null, "ss").addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, info.mActive ? CloudMessageProviderContract.JsonData.TRUE : ConfigConstants.VALUE.INFO_COMPLETED);
        if (info.mActive && info.mNoReplyTimer > 0) {
            callForwarding = callForwarding.addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.NO_REPLY_TIMER, Integer.toString(info.mNoReplyTimer)));
        }
        XmlElement rule2 = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.RULE).addAttribute("id", SoftphoneNamespaces.SoftphoneCallHandling.getId(info.mForwardCondition));
        if (info.mActive) {
            rule = rule2.addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.CONDITIONS).addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.getCondition(info.mForwardCondition), (String) null, "ss")));
            if (!info.mRetained) {
                XmlElement xmlElement = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.ACTIONS);
                XmlElement xmlElement2 = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.FORWARD_TO, (String) null, "ss");
                rule = rule.addChildElement(xmlElement.addChildElement(xmlElement2.addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.TARGET, "tel:" + info.mForwardNumber))));
            }
        } else {
            rule = rule2.addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.CONDITIONS).addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.getCondition(info.mForwardCondition), (String) null, "ss")).addChildElement(new XmlElement("rule-deactivated", (String) null, "ss")));
            if (info.mRetained) {
                rule = rule.addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.ACTIONS).addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.FORWARD_TO, (String) null, "ss").addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.TARGET, ""))));
            }
        }
        return XmlCreator.toXml(callForwarding.addChildElement(new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.RULESET, (String) null, SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX).addChildElement(rule)), "1.0", "UTF-8");
    }

    public LinkedHashMap<String, String> buildObtainPdCookiesQueryParams(String accountId, int userId, SecretKey secretKey, String domain) {
        String str = LOG_TAG;
        Log.i(str, "buildObtainPdCookiesQueryParams [accountId: " + accountId + "]");
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(SoftphoneNamespaces.SoftphoneSharedPref.SHARED_PREF_NAME);
        EncryptionHelper encryptionHelper = EncryptionHelper.getInstance(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
        Context context = this.mContext;
        String encodedTGaurdAppId = sharedPrefHelper.get(context, accountId + ":" + userId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_APPID);
        Context context2 = this.mContext;
        String encodedTGaurdToken = sharedPrefHelper.get(context2, accountId + ":" + userId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_TOKEN);
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        try {
            params.put("TG_OP", SoftphoneNamespaces.SoftphoneSettings.TGUARD_MSIP_OPERATION);
            params.put("appID", URLEncoder.encode(encryptionHelper.decrypt(encodedTGaurdAppId, secretKey), Charset.defaultCharset().name()));
            params.put("atsToken", URLEncoder.encode(encryptionHelper.decrypt(encodedTGaurdToken, secretKey), Charset.defaultCharset().name()));
            params.put(CloudMessageIntent.ExtrasAMBSUI.STYLE, domain);
            params.put("returnErrorCode", CloudMessageProviderContract.JsonData.TRUE);
            params.put("targetURL", URLEncoder.encode(SoftphoneNamespaces.SoftphoneSettings.MSIP_REDICRECT_URL, Charset.defaultCharset().name()));
            params.put("errorURL", URLEncoder.encode(SoftphoneNamespaces.SoftphoneSettings.MSIP_ERROR_URL, Charset.defaultCharset().name()));
        } catch (UnsupportedEncodingException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "exception" + e.getMessage());
        }
        return params;
    }

    public static SendSMSRequest buildSendSMSRequest(String calleeNumber) {
        return new SendSMSRequest(false, "AT&T Msg: You have activated NumberSync. NumberSync allows you to make and receive calls on your other device using the same mobile number as your smartphone, even when your smartphone is not nearby or connected to the same Wi-Fi network. Visit att.com/numbersync for more info.", calleeNumber);
    }
}
