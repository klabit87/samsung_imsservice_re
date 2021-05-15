package com.sec.internal.ims.rcscore;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.util.Iterator;
import java.util.List;

public class RcsPreferencesProvider extends ContentProvider {
    private static final String AUTHORITY = "com.sec.ims.android.rcs";
    private static final String KEY_ENRICHED_CALLING = "rcs_enriched_calling";
    private static final String KEY_MASTER_SWICH_VISIBILITY = "master_switch";
    private static final String KEY_PERMANENT_DISABLE = "permanent_disable_state";
    private static final String KEY_PERMANENT_DISABLE_AVAILABILITY = "permanent_disable_availibility";
    private static final String KEY_RCSPROFILE = "rcsprofile";
    private static final String KEY_RCS_ENABLED = "rcs_enabled";
    private static final String KEY_RCS_NOTIFICATION_SETTING = "rcs_connection_preference";
    private static final String KEY_REGISTRATION_STATUS = "registration_status";
    private static final String KEY_STATIC_ENABLE_RCS = "EnableRCS";
    private static final String KEY_STATIC_ENABLE_RCSCHAT = "EnableRCSchat";
    private static final String KEY_SUPPORT_DUAL_RCS = "support_dual_rcs";
    private static final String KEY_SUPPORT_DUAL_RCS_SETTINGS = "support_dual_rcs_settings";
    private static final String KEY_SUPPORT_DUAL_RCS_SIM1 = "support_dual_rcs_sim1";
    private static final String KEY_SUPPORT_DUAL_RCS_SIM2 = "support_dual_rcs_sim2";
    private static final String KEY_USER_ALIAS = "user_alias";
    private static final String KEY_VANILLA_APPLIED = "vanilla_applied";
    private static final String LOG_TAG = RcsPreferencesProvider.class.getSimpleName();
    private static final int MATCH_ENRICHED_CALLING = 11;
    private static final int MATCH_HOME_NETWORK = 2;
    private static final int MATCH_PERMANENT_DISABLE = 4;
    private static final int MATCH_PERMANENT_DISABLE_AVAILABILITY = 6;
    private static final int MATCH_RCSPROFILE = 8;
    private static final int MATCH_RCS_ENABLED_STATIC = 10;
    private static final int MATCH_REGISTRATION = 7;
    private static final int MATCH_ROAMING = 3;
    private static final int MATCH_SETTINGS = 1;
    private static final int MATCH_SUPPORT_DUAL_RCS = 9;
    private static final int MATCH_SUPPORT_DUAL_RCS_SETTINGS = 12;
    private static final int MATCH_USER_ALIAS = 5;
    private static final String TABLE_ENRICHED_CALLING = "rcs_enriched_calling";
    private static final String TABLE_HOME_NETWORK = "home_network";
    private static final String TABLE_PERMANENT_DISALBE = "permanent_disable_state";
    private static final String TABLE_PERMANENT_DISALBE_AVAILABILITY = "permanent_disable_availibility";
    private static final String TABLE_PREFERENCES = "preferences";
    private static final String TABLE_RCSPROFILE = "rcsprofile";
    private static final String TABLE_RCS_ENABLED_STATIC = "rcs_enabled_static";
    private static final String TABLE_REGISTRATION = "registration";
    private static final String TABLE_ROAMING = "roaming";
    private static final String TABLE_SUPPORT_DUAL_RCS = "support_dual_rcs";
    private static final String TABLE_SUPPORT_DUAL_RCS_SETTINGS = "support_dual_rcs_settings";
    private static final UriMatcher sUriMatcher;
    private Context mContext = null;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, TABLE_PREFERENCES + '/' + String.valueOf(1), 1);
        UriMatcher uriMatcher2 = sUriMatcher;
        uriMatcher2.addURI(AUTHORITY, TABLE_PREFERENCES + '/' + String.valueOf(5), 5);
        sUriMatcher.addURI(AUTHORITY, TABLE_HOME_NETWORK, 2);
        sUriMatcher.addURI(AUTHORITY, TABLE_ROAMING, 3);
        sUriMatcher.addURI(AUTHORITY, "permanent_disable_state", 4);
        sUriMatcher.addURI(AUTHORITY, "permanent_disable_availibility", 6);
        sUriMatcher.addURI(AUTHORITY, TABLE_REGISTRATION, 7);
        sUriMatcher.addURI(AUTHORITY, "rcsprofile", 8);
        sUriMatcher.addURI(AUTHORITY, "support_dual_rcs", 9);
        sUriMatcher.addURI(AUTHORITY, TABLE_RCS_ENABLED_STATIC, 10);
        sUriMatcher.addURI(AUTHORITY, "rcs_enriched_calling", 11);
        sUriMatcher.addURI(AUTHORITY, "support_dual_rcs_settings", 12);
    }

    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Uri insert(Uri arg0, ContentValues arg1) {
        return null;
    }

    public boolean onCreate() {
        this.mContext = getContext();
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = sUriMatcher.match(uri);
        String str = LOG_TAG;
        Log.i(str, "query [" + uri + "] match [" + code + "]");
        int phoneId = 0;
        if ("dsds".equals(SemSystemProperties.get("persist.radio.multisim.config"))) {
            phoneId = Extensions.SubscriptionManager.getDefaultDataPhoneId(SubscriptionManager.from(this.mContext));
        }
        if (uri.getFragment() != null && uri.getFragment().contains(ImsConstants.Uris.FRAGMENT_SIM_SLOT)) {
            phoneId = Character.getNumericValue(uri.getFragment().charAt(7));
        }
        switch (code) {
            case 1:
                return createMultiValueCursor(new String[]{KEY_RCS_ENABLED, KEY_VANILLA_APPLIED, KEY_MASTER_SWICH_VISIBILITY}, readCurrentSettingsValues(phoneId));
            case 2:
                return createSingleValueCursor(KEY_RCS_NOTIFICATION_SETTING, (Integer) 1);
            case 3:
                return createSingleValueCursor(KEY_RCS_NOTIFICATION_SETTING, (Integer) 1);
            case 4:
                return createSingleValueCursor("permanent_disable_state", (Integer) 0);
            case 5:
                return createSingleValueCursor("user_alias", queryUserAlias(phoneId));
            case 6:
                return createSingleValueCursor("permanent_disable_availibility", (Integer) 0);
            case 7:
                return createSingleValueCursor(KEY_REGISTRATION_STATUS, Integer.valueOf(isRcsRegistered(phoneId) ? 1 : 0));
            case 8:
                return createSingleValueCursor("rcsprofile", ImsRegistry.getRcsProfileType(phoneId));
            case 9:
                return createMultiValueCursor(new String[]{"support_dual_rcs", KEY_SUPPORT_DUAL_RCS_SIM1, KEY_SUPPORT_DUAL_RCS_SIM2}, getSupportDualRcs());
            case 10:
                return createMultiValueCursor(new String[]{"EnableRCS", "EnableRCSchat"}, getRcsEnabledStatic(phoneId));
            case 11:
                return createSingleValueCursor("rcs_enriched_calling", Integer.valueOf(queryEnrichedCalling(phoneId)));
            case 12:
                return createSingleValueCursor("support_dual_rcs_settings", Integer.valueOf(getSupportDualRcsSettings()));
            default:
                String str2 = LOG_TAG;
                Log.e(str2, "query: uri not implemented: " + uri);
                return null;
        }
    }

    public int update(Uri uri, ContentValues values, String arg2, String[] arg3) {
        String str = LOG_TAG;
        Log.d(str, "update: " + uri);
        int phoneId = 0;
        if ("dsds".equals(SemSystemProperties.get("persist.radio.multisim.config"))) {
            phoneId = Extensions.SubscriptionManager.getDefaultDataPhoneId(SubscriptionManager.from(this.mContext));
        }
        if (uri.getFragment() != null && uri.getFragment().contains(ImsConstants.Uris.FRAGMENT_SIM_SLOT)) {
            phoneId = Character.getNumericValue(uri.getFragment().charAt(7));
        }
        int match = sUriMatcher.match(uri);
        if (match != 1) {
            if (match != 5) {
                String str2 = LOG_TAG;
                Log.e(str2, "update: uri not implemented: " + uri);
                return 0;
            } else if (!values.containsKey("user_alias")) {
                return 0;
            } else {
                String alias = values.getAsString("user_alias");
                String str3 = LOG_TAG;
                Log.d(str3, "User alias: " + alias);
                updateUserAlias(phoneId, alias);
                return 1;
            }
        } else if (!values.containsKey(KEY_RCS_ENABLED)) {
            return 0;
        } else {
            updateRCSSetting(values.getAsBoolean(KEY_RCS_ENABLED) != null ? values.getAsBoolean(KEY_RCS_ENABLED).booleanValue() : false, phoneId);
            return 1;
        }
    }

    private Integer[] readCurrentSettingsValues(int phoneId) {
        boolean isEnabledByUser = ImsRegistry.isServiceEnabledByPhoneId("rcs_user_setting", phoneId);
        int i = 1;
        boolean isVanillaApplied = !ImsRegistry.isRcsEnabledByPhoneId(phoneId);
        boolean isMainSwitchVisible = false;
        try {
            isMainSwitchVisible = RcsUtils.UiUtils.isMainSwitchVisible(this.mContext, phoneId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Integer[] numArr = new Integer[3];
        numArr[0] = Integer.valueOf(isEnabledByUser);
        numArr[1] = Integer.valueOf(isVanillaApplied ? 1 : 0);
        if (!isMainSwitchVisible) {
            i = 0;
        }
        numArr[2] = Integer.valueOf(i);
        return numArr;
    }

    private Cursor createSingleValueCursor(String key, Integer value) {
        MatrixCursor c = new MatrixCursor(new String[]{key}, 1);
        c.addRow(new Integer[]{value});
        return c;
    }

    private Cursor createSingleValueCursor(String key, String value) {
        MatrixCursor c = new MatrixCursor(new String[]{key}, 1);
        c.addRow(new String[]{value});
        return c;
    }

    private Cursor createMultiValueCursor(String[] keys, Integer[] values) {
        MatrixCursor c = new MatrixCursor(keys, keys.length);
        c.addRow(values);
        return c;
    }

    private Cursor createMultiValueCursor(String[] keys, String[] values) {
        MatrixCursor c = new MatrixCursor(keys, keys.length);
        c.addRow(values);
        return c;
    }

    private void updateUserAlias(int phoneId, String alias) {
        IImModule imModule = ImsRegistry.getServiceModuleManager().getImModule();
        if (imModule != null) {
            imModule.setUserAlias(phoneId, alias);
        }
    }

    private String queryUserAlias(int phoneId) {
        IImModule imModule = ImsRegistry.getServiceModuleManager().getImModule();
        if (imModule != null) {
            return imModule.getUserAliasFromPreference(phoneId);
        }
        return "";
    }

    private boolean isRcsRegistered(int phoneId) {
        return ImsServiceStub.getInstance().getRegistrationManager().isRcsRegistered(phoneId);
    }

    private void updateRCSSetting(boolean enabled, int phoneId) {
        ImsRegistry.enableRcsByPhoneId(enabled, phoneId);
    }

    private Integer[] getSupportDualRcs() {
        return new Integer[]{Integer.valueOf(RcsUtils.DualRcs.isDualRcsReg()), Integer.valueOf(RcsUtils.DualRcs.isRegAllowed(this.mContext, 0)), Integer.valueOf(RcsUtils.DualRcs.isRegAllowed(this.mContext, 1))};
    }

    private String[] getRcsEnabledStatic(int phoneId) {
        int i = phoneId;
        boolean isEnableRcs = false;
        boolean isEnableRcsChat = false;
        boolean isSimMobilityFeatureEnabled = SimUtil.isSimMobilityFeatureEnabled();
        String str = CloudMessageProviderContract.JsonData.TRUE;
        if (!isSimMobilityFeatureEnabled || !ImsUtil.isSimMobilityActivated(phoneId)) {
            String operator = SemSystemProperties.get(Mno.MOCK_MNO_PROPERTY, "");
            if (TextUtils.isEmpty(operator)) {
                operator = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getSimOperator();
            }
            String str2 = LOG_TAG;
            Log.d(str2, "getRcsEnabledStatic: operator = " + operator);
            if (TextUtils.isEmpty(operator)) {
                Log.d(LOG_TAG, "getRcsEnabledStatic: operator is empty, rcs = false, rcschat = false");
                return new String[]{ConfigConstants.VALUE.INFO_COMPLETED, ConfigConstants.VALUE.INFO_COMPLETED};
            }
            if (ConfigUtil.isRcsEur(SimUtil.getMno())) {
                isEnableRcs = RcsUtils.UiUtils.isRcsEnabledinSettings(this.mContext, i);
                isEnableRcsChat = isEnableRcs;
            } else {
                ContentValues cscSettings = CscParser.getCscImsSetting(operator, i);
                if (cscSettings == null || cscSettings.size() <= 0) {
                    Log.d(LOG_TAG, "getRcsEnabledStatic: cscSettings is null, rcs = false, rcschat = false");
                } else {
                    isEnableRcs = CollectionUtils.getBooleanValue(cscSettings, "EnableRCS", false);
                    isEnableRcsChat = CollectionUtils.getBooleanValue(cscSettings, "EnableRCSchat", false);
                    String str3 = LOG_TAG;
                    Log.d(str3, "getRcsEnabledStatic: Customer, rcs = " + isEnableRcs + ", rcschat = " + isEnableRcsChat);
                }
            }
            String[] strArr = new String[2];
            strArr[0] = isEnableRcs ? str : ConfigConstants.VALUE.INFO_COMPLETED;
            if (!isEnableRcsChat) {
                str = ConfigConstants.VALUE.INFO_COMPLETED;
            }
            strArr[1] = str;
            return strArr;
        }
        List<ImsProfile> profileList = ImsProfileLoaderInternal.getProfileList(this.mContext, i);
        if (profileList != null && profileList.size() > 0) {
            Iterator<ImsProfile> it = profileList.iterator();
            while (true) {
                if (it.hasNext()) {
                    ImsProfile profile = it.next();
                    if (profile != null && profile.getEnableRcs()) {
                        isEnableRcs = profile.getEnableRcs();
                        isEnableRcsChat = profile.getEnableRcsChat();
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        String str4 = LOG_TAG;
        Log.d(str4, "getRcsEnabledStatic: SimMobility, rcs = " + isEnableRcs + ", rcschat = " + isEnableRcsChat);
        String[] strArr2 = new String[2];
        strArr2[0] = isEnableRcs ? str : ConfigConstants.VALUE.INFO_COMPLETED;
        if (!isEnableRcsChat) {
            str = ConfigConstants.VALUE.INFO_COMPLETED;
        }
        strArr2[1] = str;
        return strArr2;
    }

    private int queryEnrichedCalling(int phoneId) {
        return RcsUtils.UiUtils.isRcsEnabledEnrichedCalling(phoneId) ? 1 : 0;
    }

    private int getSupportDualRcsSettings() {
        return RcsUtils.DualRcs.isDualRcsSettings() ? 1 : 0;
    }
}
