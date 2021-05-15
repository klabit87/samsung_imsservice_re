package com.sec.internal.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImsSharedPrefHelper {
    public static final String CARRIER_ID = "carrierId";
    public static final String CSC_INFO_PREF = "CSC_INFO_PREF";
    public static final String DEBUG_CONFIG = "Debug_config";
    public static final String DRPT = "DRPT";
    public static final String GLOBAL_GC_SETTINGS = "globalgcsettings";
    public static final String GLOBAL_SETTINGS = "globalsettings";
    public static final String IMS_CONFIG = "imsconfig";
    public static final String IMS_FEATURE = "imsfeature";
    public static final String IMS_PROFILE = "imsprofile";
    public static final String IMS_SWITCH = "imsswitch";
    public static final String IMS_USER_DATA = "ims_user_data";
    private static final String LOG_TAG = ImsSharedPrefHelper.class.getSimpleName();
    public static final String PREF = "pref";
    public static final String PRE_COMMON_HEADER = "previous_common_header";
    public static final String PROFILE = "profile";
    public static final String SAVED_IMPU = "saved_impu";
    public static final String USER_CONFIG = "user_config";
    public static final String VALID_RCS_CONFIG = "validrcsconfig";
    private static final List<String> migrationListForCe = Arrays.asList(new String[]{IMS_USER_DATA, "profile", PREF, SAVED_IMPU});
    private static final List<String> saveWithPhoneIdList = Arrays.asList(new String[]{USER_CONFIG, IMS_USER_DATA, CSC_INFO_PREF, GLOBAL_SETTINGS, IMS_FEATURE, IMS_PROFILE, "imsswitch"});

    public static SharedPreferences getSharedPref(int phoneId, Context context, String prefName, int mode, boolean storeCe) {
        if (context == null || TextUtils.isEmpty(prefName)) {
            return null;
        }
        if ((storeCe || DeviceUtil.isUserUnlocked(context)) && migrationListForCe.contains(prefName)) {
            String str = LOG_TAG;
            IMSLog.d(str, phoneId, "getSharedPref from CE : " + prefName);
            Context ceContext = context.createCredentialProtectedStorageContext();
            if (phoneId < 0) {
                return ceContext.getSharedPreferences(prefName, mode);
            }
            return ceContext.getSharedPreferences(prefName + "_" + phoneId, mode);
        } else if (phoneId < 0) {
            return context.getSharedPreferences(prefName, mode);
        } else {
            return context.getSharedPreferences(prefName + "_" + phoneId, mode);
        }
    }

    private static Optional<SharedPreferences> getSpAsOptional(int phoneId, Context context, String prefName) {
        return Optional.ofNullable(getSharedPref(phoneId, context, prefName, 0, false));
    }

    public static void save(int phoneId, Context context, String prefName, String key, String value) {
        getSpAsOptional(phoneId, context, prefName).ifPresent(new Consumer(key, value) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ String f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ((SharedPreferences) obj).edit().putString(this.f$0, this.f$1).apply();
            }
        });
    }

    public static void save(int phoneId, Context context, String prefName, String key, boolean value) {
        getSpAsOptional(phoneId, context, prefName).ifPresent(new Consumer(key, value) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ((SharedPreferences) obj).edit().putBoolean(this.f$0, this.f$1).apply();
            }
        });
    }

    public static void save(int phoneId, Context context, String prefName, String key, int value) {
        getSpAsOptional(phoneId, context, prefName).ifPresent(new Consumer(key, value) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ((SharedPreferences) obj).edit().putInt(this.f$0, this.f$1).apply();
            }
        });
    }

    public static void save(int phoneId, Context context, String prefName, String key, long value) {
        getSpAsOptional(phoneId, context, prefName).ifPresent(new Consumer(key, value) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ long f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ((SharedPreferences) obj).edit().putLong(this.f$0, this.f$1).apply();
            }
        });
    }

    public static void save(int phoneId, Context context, String prefName, String key, Set<String> value) {
        getSpAsOptional(phoneId, context, prefName).ifPresent(new Consumer(key, value) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ Set f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ((SharedPreferences) obj).edit().putStringSet(this.f$0, this.f$1).apply();
            }
        });
    }

    public static String getString(int phoneId, Context context, String prefName, String key, String defaultValue) {
        return (String) getSpAsOptional(phoneId, context, prefName).map(new Function(key, defaultValue) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ String f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final Object apply(Object obj) {
                return ((SharedPreferences) obj).getString(this.f$0, this.f$1);
            }
        }).orElse(defaultValue);
    }

    public static Set<String> getStringSet(int phoneId, Context context, String prefName, String key, Set<String> defaultValue) {
        return (Set) getSpAsOptional(phoneId, context, prefName).map(new Function(key, defaultValue) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ Set f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final Object apply(Object obj) {
                return ((SharedPreferences) obj).getStringSet(this.f$0, this.f$1);
            }
        }).orElse(defaultValue);
    }

    public static boolean getBoolean(int phoneId, Context context, String prefName, String key, boolean defaultValue) {
        return ((Boolean) getSpAsOptional(phoneId, context, prefName).map(new Function(key, defaultValue) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final Object apply(Object obj) {
                return Boolean.valueOf(((SharedPreferences) obj).getBoolean(this.f$0, this.f$1));
            }
        }).orElse(Boolean.valueOf(defaultValue))).booleanValue();
    }

    public static int getInt(int phoneId, Context context, String prefName, String key, int defaultValue) {
        return ((Integer) getSpAsOptional(phoneId, context, prefName).map(new Function(key, defaultValue) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final Object apply(Object obj) {
                return Integer.valueOf(((SharedPreferences) obj).getInt(this.f$0, this.f$1));
            }
        }).orElse(Integer.valueOf(defaultValue))).intValue();
    }

    public static long getLong(int phoneId, Context context, String prefName, String key, long defaultValue) {
        return ((Long) getSpAsOptional(phoneId, context, prefName).map(new Function(key, defaultValue) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ long f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final Object apply(Object obj) {
                return Long.valueOf(((SharedPreferences) obj).getLong(this.f$0, this.f$1));
            }
        }).orElse(Long.valueOf(defaultValue))).longValue();
    }

    public static void remove(int phoneId, Context context, String prefName, String key) {
        getSpAsOptional(phoneId, context, prefName).ifPresent(new Consumer(key) {
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((SharedPreferences) obj).edit().remove(this.f$0).apply();
            }
        });
    }

    public static void clear(int phoneId, Context context, String prefName) {
        getSpAsOptional(phoneId, context, prefName).ifPresent($$Lambda$ImsSharedPrefHelper$jzZQp_dmoLTzDfTUq6iLcvCJCNA.INSTANCE);
    }

    public static Map<String, String> getStringArray(int phoneId, Context context, String prefName, String[] keys) {
        Map<String, String> values = new ArrayMap<>(keys.length);
        getSpAsOptional(phoneId, context, prefName).ifPresent(new Consumer(keys, values) {
            public final /* synthetic */ String[] f$0;
            public final /* synthetic */ Map f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ImsSharedPrefHelper.lambda$getStringArray$12(this.f$0, this.f$1, (SharedPreferences) obj);
            }
        });
        return values;
    }

    static /* synthetic */ void lambda$getStringArray$12(String[] keys, Map values, SharedPreferences pref) {
        for (String key : keys) {
            values.put(key, pref.getString(key, ""));
        }
    }

    public static void put(int phoneId, Context context, String prefName, ContentValues values) {
        getSpAsOptional(phoneId, context, prefName).ifPresent(new Consumer(values) {
            public final /* synthetic */ ContentValues f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ImsSharedPrefHelper.lambda$put$13(this.f$0, (SharedPreferences) obj);
            }
        });
    }

    static /* synthetic */ void lambda$put$13(ContentValues values, SharedPreferences pref) {
        SharedPreferences.Editor editor = pref.edit();
        for (String key : values.keySet()) {
            editor.putString(key, values.getAsString(key));
        }
        editor.apply();
    }

    public static void migrateToCeStorage(Context context) {
        IMSLog.d(LOG_TAG, "migrate shared preferences to CE storage");
        if (context == null) {
            IMSLog.d(LOG_TAG, "context is null ");
            return;
        }
        int phoneCount = ((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).getPhoneCount();
        Context ceContext = context.createCredentialProtectedStorageContext();
        for (String prefName : migrationListForCe) {
            if (saveWithPhoneIdList.contains(prefName)) {
                for (int i = 0; i < phoneCount; i++) {
                    if (!ceContext.moveSharedPreferencesFrom(context, prefName + "_" + i)) {
                        IMSLog.e(LOG_TAG, "Failed to move shared preferences.");
                        return;
                    }
                    if (!context.deleteSharedPreferences(prefName + "_" + i)) {
                        IMSLog.e(LOG_TAG, "Failed delete shared preferences on DE.");
                        return;
                    }
                }
                continue;
            } else if (!ceContext.moveSharedPreferencesFrom(context, prefName)) {
                IMSLog.e(LOG_TAG, "Failed to move shared preferences.");
                return;
            } else if (!context.deleteSharedPreferences(prefName)) {
                IMSLog.e(LOG_TAG, "Failed delete shared preferences on DE.");
                return;
            }
        }
    }
}
