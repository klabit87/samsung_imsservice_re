package com.sec.internal.ims.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.log.IMSLog;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.regex.Pattern;

public class TimeBasedUuidGenerator {
    private static final String LOG_TAG = TimeBasedUuidGenerator.class.getSimpleName();
    protected static final String SHAREDPREF_INSTANCE_ID_UUID_KEY = "instanceIdUuid";
    protected static final String UUID_CORE_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    protected static final Pattern UUID_PURE_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final Pattern UUID_STRIP = Pattern.compile("(<)|(urn:uuid:)|(>)");
    private Context mContext;
    private int mPhoneId;
    private UuidSource mUuidSource;

    private enum UuidSource {
        AUTOCONFIG,
        SHAREDPREFS,
        GENERATOR
    }

    public TimeBasedUuidGenerator(int phoneId, Context context) {
        this.mPhoneId = phoneId;
        this.mContext = context;
    }

    private String generate() {
        return generate(compute100nsTimestamp(), randSeq(), getWifiMacAddr());
    }

    /* access modifiers changed from: protected */
    public String generate(long timeStamp, long clockSeq, long node) {
        return new UUID(((timeStamp & 4294967295L) << 32) | (((timeStamp & 281470681743360L) >>> 32) << 16) | (1 << 12) | ((timeStamp & 1152640029630136320L) >>> 48), (2 << 62) | (clockSeq << 48) | node).toString();
    }

    private long getWifiMacAddr() {
        WifiInfo wifiInfo;
        WifiManager wifiManager = (WifiManager) ImsServiceStub.getInstance().getContext().getSystemService("wifi");
        String macAddr = "000000000000";
        if (!(wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null || wifiInfo.getMacAddress() == null)) {
            macAddr = wifiInfo.getMacAddress().replace(":", "");
        }
        String str = LOG_TAG;
        IMSLog.s(str, "getWifiMacAddr: [" + macAddr + "]");
        return Long.decode("0x" + macAddr).longValue();
    }

    private long compute100nsTimestamp() {
        long current100ns = System.currentTimeMillis() * 10000;
        String str = LOG_TAG;
        Log.d(str, "compute100nsTimestamp: " + (current100ns + 122192928000000000L));
        return 122192928000000000L + current100ns;
    }

    private long randSeq() {
        byte[] seqByte = new byte[2];
        new SecureRandom().nextBytes(seqByte);
        return ((((long) seqByte[1]) * 256) + ((long) seqByte[0])) & 16383;
    }

    public String getUuidInstanceId() {
        String instanceId = obtainUuid();
        if (!instanceId.isEmpty() && this.mUuidSource == UuidSource.GENERATOR) {
            ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, SHAREDPREF_INSTANCE_ID_UUID_KEY, instanceId);
        }
        return "<urn:uuid:" + instanceId + ">";
    }

    private String obtainUuid() {
        String uuid = UUID_STRIP.matcher(RcsConfigurationHelper.getUuid(this.mContext, this.mPhoneId).toLowerCase()).replaceAll("");
        String str = LOG_TAG;
        IMSLog.s(str, "selectUuidInstanceId from config: " + uuid);
        if (UUID_PURE_PATTERN.matcher(uuid).matches()) {
            this.mUuidSource = UuidSource.AUTOCONFIG;
            return uuid;
        }
        String uuid2 = ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, SHAREDPREF_INSTANCE_ID_UUID_KEY, "");
        if (!uuid2.isEmpty()) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "selectUuidInstanceId from sharedPref: " + uuid2);
            this.mUuidSource = UuidSource.SHAREDPREFS;
            return uuid2;
        }
        Log.d(LOG_TAG, "selectUuidInstanceId from sharedPref Empty");
        String uuid3 = generate();
        String str3 = LOG_TAG;
        IMSLog.s(str3, "selectUuidInstanceId from Generator: " + uuid3);
        this.mUuidSource = UuidSource.GENERATOR;
        return uuid3;
    }
}
