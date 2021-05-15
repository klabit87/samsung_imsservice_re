package com.sec.internal.constants.ims.servicemodules.options;

import com.sec.internal.log.IMSLog;
import java.util.HashMap;

public class BotServiceIdTranslator {
    private static final String TAG = "BotServiceIdTranslator";
    private static BotServiceIdTranslator mInstance;
    private HashMap<String, String> mBotServiceIdMap = new HashMap<>();

    private BotServiceIdTranslator() {
    }

    public static BotServiceIdTranslator getInstance() {
        if (mInstance == null) {
            synchronized (BotServiceIdTranslator.class) {
                if (mInstance == null) {
                    mInstance = new BotServiceIdTranslator();
                }
            }
        }
        return mInstance;
    }

    public void register(String msisdn, String serviceId) {
        if (serviceId != null) {
            IMSLog.s(TAG, "register: msisdn = " + msisdn + ", serviceId = " + serviceId);
            this.mBotServiceIdMap.put(msisdn, serviceId);
        }
    }

    public String translate(String msisdn) {
        IMSLog.s(TAG, "translate: msisdn = " + msisdn + ", serviceId = " + this.mBotServiceIdMap.get(msisdn));
        return this.mBotServiceIdMap.get(msisdn);
    }

    public Boolean contains(String value) {
        IMSLog.s(TAG, "contains: serviceId = " + value);
        return Boolean.valueOf(this.mBotServiceIdMap.containsValue(value));
    }
}
