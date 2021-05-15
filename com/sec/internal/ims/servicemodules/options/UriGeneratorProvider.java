package com.sec.internal.ims.servicemodules.options;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.helper.MccTable;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Locale;

public class UriGeneratorProvider extends ContentProvider {
    private static final String AUTHORITY = "com.samsung.rcs.urigenerator.provider";
    private static final String DEFAULT_COUNTRY_CODE = "US";
    private static final String DEFAULT_MCC = "310";
    private static final IntentFilter IMS_REGISTRATION_INTENT_FILTER = new IntentFilter(ImsConstants.Intents.ACTION_IMS_STATE);
    private static final IntentFilter INTENT_ACTION_DEFAULT_DATA_SUB_CHANGED = new IntentFilter(ImsConstants.Intents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
    /* access modifiers changed from: private */
    public static final String LOG_TAG = UriGeneratorProvider.class.getSimpleName();
    private static final int N_IMS = 1;
    private static final String[] PROJECTION = {"_id", "generated_uri"};
    private static final IntentFilter SIM_STATE_CHANGED_INTENT_FILTER = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
    private static UriMatcher sMatcher;
    private String mCountryCode = DEFAULT_COUNTRY_CODE;
    private DDSIntentReceiver mDDSIntentReceiver;
    private ImsIntentReceiver mImsIntentReceiver;
    private String mMcc = DEFAULT_MCC;
    private String mOwnAreaCode;
    /* access modifiers changed from: private */
    public SimEventListener mSimEventListener = new SimEventListener();
    private SimIntentReceiver mSimIntentReceiver;
    /* access modifiers changed from: private */
    public ISimManager mSimManager = null;

    static {
        UriMatcher uriMatcher = new UriMatcher(0);
        sMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "ims/*", 1);
    }

    public boolean onCreate() {
        Log.i(LOG_TAG, "onCreate()");
        this.mSimIntentReceiver = new SimIntentReceiver();
        this.mImsIntentReceiver = new ImsIntentReceiver();
        this.mDDSIntentReceiver = new DDSIntentReceiver();
        getContext().registerReceiver(this.mSimIntentReceiver, SIM_STATE_CHANGED_INTENT_FILTER);
        getContext().registerReceiver(this.mImsIntentReceiver, IMS_REGISTRATION_INTENT_FILTER);
        getContext().registerReceiver(this.mDDSIntentReceiver, INTENT_ACTION_DEFAULT_DATA_SUB_CHANGED);
        return true;
    }

    private ImsUri appendCountryCodePrefix(String telNumber, ImsUri defaultUri) {
        String str = this.mCountryCode;
        if (str == null || !"cn".equalsIgnoreCase(str) || telNumber == null || defaultUri != null) {
            return defaultUri;
        }
        if (!telNumber.startsWith("+86")) {
            telNumber = "+86" + telNumber;
        }
        ImsUri telUri = ImsUri.parse("tel:" + telNumber);
        IMSLog.s(LOG_TAG, "CMCC special number parsed result telUri:  " + telUri + " telNumber: " + telNumber);
        return telUri;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ImsUri normalizedUri;
        Uri uri2 = uri;
        List<String> pathList = uri.getPathSegments();
        String telNumber = Uri.decode(pathList.get(pathList.size() - 1));
        IMSLog.s(LOG_TAG, String.format("query() - uri: %s, number: %s", new Object[]{uri2, telNumber}));
        if (sMatcher.match(uri2) == 1) {
            String ret = null;
            IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority());
            if (telNumber != null && telNumber.length() == 7 && !telNumber.startsWith("+") && this.mOwnAreaCode != null && mnoStrategy != null && !mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_7DIGIT_MSG)) {
                telNumber = this.mOwnAreaCode + telNumber;
                IMSLog.s(LOG_TAG, "local number format, adding own area code " + telNumber);
            }
            Log.i(LOG_TAG, "query()  mCountryCode " + this.mCountryCode + " MCC " + this.mMcc + " CountryCode " + this.mCountryCode);
            ImsUri telUri = UriUtil.parseNumber(telNumber, this.mCountryCode);
            if (mnoStrategy != null && mnoStrategy.getPolicyType() == RcsPolicySettings.RcsPolicyType.CMCC) {
                telUri = appendCountryCodePrefix(telNumber, telUri);
            }
            if (SimUtil.getMno().isRjil() && (normalizedUri = UriGeneratorFactory.getInstance().get().getNormalizedUri(telNumber)) != null) {
                IMSLog.s(LOG_TAG, "converting " + telNumber + "to" + normalizedUri.toString());
                telUri = normalizedUri;
            }
            if (telUri != null) {
                ret = telUri.toString();
            }
            IMSLog.s(LOG_TAG, "query() - generated uri: " + ret);
            MatrixCursor mc = new MatrixCursor(PROJECTION);
            mc.addRow(new Object[]{0, ret});
            return mc;
        }
        throw new UnsupportedOperationException("Unsupported URI!");
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("delete not supported!");
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("getType not supported!");
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("insert not supported!");
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update not supported!");
    }

    private class SimIntentReceiver extends BroadcastReceiver {
        private SimIntentReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String iccState = intent.getStringExtra("ss");
            String access$400 = UriGeneratorProvider.LOG_TAG;
            Log.i(access$400, "sim state intent received - " + iccState);
            if (ImsRegistry.isReady()) {
                if (UriGeneratorProvider.this.mSimManager == null || IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(iccState)) {
                    if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(iccState)) {
                        Log.i(UriGeneratorProvider.LOG_TAG, "Update mSimManager when iccState is 'LOADED' ");
                    }
                    ISimManager unused = UriGeneratorProvider.this.mSimManager = SimManagerFactory.getSimManager();
                    UriGeneratorProvider.this.mSimManager.registerSimCardEventListener(UriGeneratorProvider.this.mSimEventListener);
                }
                if (UriGeneratorProvider.this.mSimManager.isSimLoaded()) {
                    UriGeneratorProvider uriGeneratorProvider = UriGeneratorProvider.this;
                    uriGeneratorProvider.extractOwnAreaCode(uriGeneratorProvider.mSimManager.getSimOperator(), UriGeneratorProvider.this.mSimManager.getMsisdn());
                }
            }
        }
    }

    private class ImsIntentReceiver extends BroadcastReceiver {
        private ImsIntentReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            boolean isRegister = intent.getBooleanExtra(ImsConstants.Intents.EXTRA_REGISTERED, false);
            String access$400 = UriGeneratorProvider.LOG_TAG;
            Log.i(access$400, "IMS register - " + String.valueOf(isRegister));
            if (UriGeneratorProvider.this.mSimManager == null) {
                ISimManager unused = UriGeneratorProvider.this.mSimManager = SimManagerFactory.getSimManager();
            }
            if (UriGeneratorProvider.this.mSimManager.isSimLoaded()) {
                UriGeneratorProvider uriGeneratorProvider = UriGeneratorProvider.this;
                uriGeneratorProvider.extractOwnAreaCode(uriGeneratorProvider.mSimManager.getSimOperator(), UriGeneratorProvider.this.mSimManager.getMsisdn());
            }
        }
    }

    private class DDSIntentReceiver extends BroadcastReceiver {
        private DDSIntentReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.i(UriGeneratorProvider.LOG_TAG, "DDS change intent received");
            ISimManager unused = UriGeneratorProvider.this.mSimManager = SimManagerFactory.getSimManager();
        }
    }

    private class SimEventListener implements ISimEventListener {
        private SimEventListener() {
        }

        public void onReady(int subId, boolean absent) {
            String access$400 = UriGeneratorProvider.LOG_TAG;
            Log.i(access$400, "onReady: subId=" + subId + " absent=" + absent);
            if (!absent) {
                UriGeneratorProvider uriGeneratorProvider = UriGeneratorProvider.this;
                uriGeneratorProvider.extractOwnAreaCode(uriGeneratorProvider.mSimManager.getSimOperator(), UriGeneratorProvider.this.mSimManager.getMsisdn());
            }
        }
    }

    /* access modifiers changed from: private */
    public void extractOwnAreaCode(String operator, String phoneNumber) {
        String str = LOG_TAG;
        IMSLog.s(str, "extractOwnAreaCode phoneNumber" + IMSLog.checker(phoneNumber));
        if (operator != null && operator.length() > 3) {
            String substring = operator.substring(0, 3);
            this.mMcc = substring;
            int mccInt = 0;
            try {
                mccInt = Integer.parseInt(substring);
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "extractOwnAreaCode. mcc is not int");
            }
            String tmpCountryCode = MccTable.countryCodeForMcc(mccInt).toUpperCase(Locale.US);
            String str2 = LOG_TAG;
            Log.i(str2, "extractOwnAreaCode tmpCountryCode " + tmpCountryCode + " operator " + operator);
            if (tmpCountryCode != null) {
                this.mCountryCode = tmpCountryCode;
            }
            String str3 = LOG_TAG;
            Log.i(str3, "extractOwnAreaCode MCC " + this.mMcc + " Country " + this.mCountryCode);
        }
        if (!this.mSimManager.getSimMno().isUSA()) {
            this.mOwnAreaCode = "";
            IMSLog.i(LOG_TAG, "extractOwnAreaCode: KOR operator not use OwnAreaCode");
        } else if (phoneNumber != null) {
            try {
                if (phoneNumber.startsWith("+1")) {
                    this.mOwnAreaCode = phoneNumber.substring(2, 5);
                } else if (phoneNumber.length() == 11) {
                    this.mOwnAreaCode = phoneNumber.substring(1, 4);
                } else if (phoneNumber.length() == 10) {
                    this.mOwnAreaCode = phoneNumber.substring(0, 3);
                }
            } catch (StringIndexOutOfBoundsException e2) {
                this.mOwnAreaCode = "";
            }
        }
    }
}
