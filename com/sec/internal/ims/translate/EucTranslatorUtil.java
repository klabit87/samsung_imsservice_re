package com.sec.internal.ims.translate;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EucTranslatorUtil {
    private static final String LOG_TAG = EucTranslatorUtil.class.getSimpleName();

    private EucTranslatorUtil() {
    }

    public static String getOwnIdentity(int handle) throws TranslationException {
        String ownIdentity = null;
        if (ImsRegistry.isReady()) {
            IRegistrationManager rm = ImsRegistry.getRegistrationManager();
            ownIdentity = rm != null ? rm.getImsiByUserAgentHandle(handle) : null;
        }
        if (!TextUtils.isEmpty(ownIdentity)) {
            return ownIdentity;
        }
        IMSLog.e(LOG_TAG, "Cannot obtain own identity!");
        throw new TranslationException(Integer.valueOf(handle));
    }

    static void checkTextLangPair(String text, String lang, boolean isLangOptional) throws TranslationException {
        if (!isLangOptional && TextUtils.isEmpty(lang)) {
            throw new TranslationException("RCC.15: A language (lang) attribute must be present with the two letter language codes according to the ISO 639-1");
        } else if (text == null) {
            throw new TranslationException("null text is not allowed");
        }
    }

    static String addLanguage(String lang, Set<String> languages) {
        if (TextUtils.isEmpty(lang)) {
            Log.v(LOG_TAG, "Language is empty, using default!");
            return DeviceLocale.DEFAULT_LANG_VALUE;
        }
        languages.add(lang);
        return lang;
    }

    static String getValue(String lang, Map<String, String> values) {
        String value = values.get(lang);
        if (!TextUtils.isEmpty(value)) {
            return value;
        }
        String str = LOG_TAG;
        Log.v(str, "Value for language = " + lang + " is empty, getting first in values!");
        Iterator<String> it = values.values().iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return value;
    }

    static String nullIfEmpty(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return str;
    }
}
