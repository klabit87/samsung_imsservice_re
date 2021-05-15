package com.sec.internal.ims.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.gls.GlsIntent;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Date;

public class GeolocationController extends Handler implements IGeolocationController {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    private static final int EVENT_EPDG_AVAILABLE = 5;
    private static final int EVENT_SERVICE_STATE_CHANGED = 4;
    public static final int EVENT_START_LOCATION_UPDATE = 1;
    private static final int EVENT_START_PERIODIC_LOCATION_UPDATE = 3;
    public static final int EVENT_STOP_LOCATION_UPDATE = 2;
    private static final String INTENT_EPDG_SSID_CHANGED = "com.sec.epdg.EPDG_SSID_CHANGED";
    private static final String INTENT_PERIODIC_LOCATION_UPDATE_TIMER_EXPD = "com.sec.internal.ims.imsservice.periodic_lu";
    private static final String INTENT_PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED";
    private static final String LOG_TAG = "GeolocationCon";
    private final int LOCATION_REQUEST_TIMEOUT = 45000;
    private final int PERIODIC_LOCATION_TIME = 1800000;
    private AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    private String mCountryIso = "";
    private int[] mDataRegState;
    ContentObserver mDtLocUserConsentObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange, Uri uri) {
            Context access$700 = GeolocationController.this.mContext;
            Context unused = GeolocationController.this.mContext;
            int dtLocUserConsent = ImsSharedPrefHelper.getSharedPref(-1, access$700, "dtlocuserconsent", 0, false).getInt("dtlocation", -1);
            Log.i(GeolocationController.LOG_TAG, "onChange- dtlocuserconsent : " + dtLocUserConsent);
            for (int i = 0; i < GeolocationController.this.mTelephonyManager.getPhoneCount(); i++) {
                Mno mno = SimUtil.getSimMno(i);
                if ((mno == Mno.TMOBILE || mno == Mno.TMOBILE_NED) && GeolocationController.this.mIsLocationUserConsent[i] != dtLocUserConsent) {
                    GeolocationController.this.mIsLocationUserConsent[i] = dtLocUserConsent;
                    GeolocationController.this.mIsForceEpdgAvailUpdate[i] = true;
                    GeolocationController geolocationController = GeolocationController.this;
                    geolocationController.sendMessage(geolocationController.obtainMessage(5, i, geolocationController.mIsEpdgAvaialble[i] ? 1 : 0));
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public LocationInfo mGeolocation = null;
    private GeolocationListener mGeolocationListener = null;
    private final IntentListener mIntentListener;
    protected boolean[] mIsEpdgAvaialble;
    /* access modifiers changed from: private */
    public boolean[] mIsForceEpdgAvailUpdate;
    /* access modifiers changed from: private */
    public boolean mIsLocationEnabled = false;
    /* access modifiers changed from: private */
    public boolean mIsLocationEnabledToRestore = false;
    /* access modifiers changed from: private */
    public int[] mIsLocationUserConsent;
    /* access modifiers changed from: private */
    public boolean mIsRequested = false;
    /* access modifiers changed from: private */
    public final LocationManager mLocationManager;
    private Handler mLocationUpdateHandler;
    private HandlerThread mLocationUpdateThread;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private final RegistrationManagerBase mRegistrationManager;
    /* access modifiers changed from: private */
    public final ITelephonyManager mTelephonyManager;
    protected int[] mVoiceRegState;

    public GeolocationController(Context context, Looper looper, RegistrationManagerBase registrationManager) {
        super(looper);
        this.mContext = context;
        ITelephonyManager instance = TelephonyManagerWrapper.getInstance(context);
        this.mTelephonyManager = instance;
        int phoneCount = instance.getPhoneCount();
        this.mRegistrationManager = registrationManager;
        this.mVoiceRegState = new int[phoneCount];
        this.mLocationManager = (LocationManager) this.mContext.getSystemService(GlsIntent.Extras.EXTRA_LOCATION);
        this.mGeolocationListener = new GeolocationListener();
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mDataRegState = new int[phoneCount];
        this.mIsEpdgAvaialble = new boolean[phoneCount];
        this.mIsLocationUserConsent = new int[phoneCount];
        this.mIsForceEpdgAvailUpdate = new boolean[phoneCount];
        Arrays.fill(this.mVoiceRegState, 1);
        Arrays.fill(this.mDataRegState, 1);
        Arrays.fill(this.mIsEpdgAvaialble, false);
        Arrays.fill(this.mIsLocationUserConsent, -1);
        Arrays.fill(this.mIsForceEpdgAvailUpdate, false);
        this.mIntentListener = new IntentListener();
        registerDtLocUserConsentObserver();
    }

    public void initSequentially() {
        Log.i(LOG_TAG, "init");
        this.mIntentListener.init();
        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        this.mLocationUpdateThread = handlerThread;
        handlerThread.start();
        this.mLocationUpdateHandler = new Handler(this.mLocationUpdateThread.getLooper());
    }

    public void handleMessage(Message msg) {
        Log.i(LOG_TAG, "handleMessage : what = " + msgToString(msg.what));
        int i = msg.what;
        boolean z = false;
        if (i != 1) {
            if (i == 2) {
                releaseLocationUpdate();
                this.mIsRequested = false;
            } else if (i == 3) {
                startPeriodicLocationUpdate(msg.arg1);
            } else if (i == 4) {
                onServiceStateChanged(msg.arg1, (ServiceStateWrapper) msg.obj);
            } else if (i == 5) {
                int i2 = msg.arg1;
                if (msg.arg2 == 1) {
                    z = true;
                }
                onEpdgAvailable(i2, z);
            }
        } else if (hasMessages(1)) {
        } else {
            if (this.mIsRequested) {
                Log.i(LOG_TAG, "Already Requested, Don't request location");
                return;
            }
            final int phoneId = msg.arg1;
            this.mPhoneId = phoneId;
            if (msg.arg2 == 1) {
                z = true;
            }
            final boolean isEmergency = z;
            this.mLocationUpdateHandler.post(new Runnable() {
                public void run() {
                    GeolocationController geolocationController = GeolocationController.this;
                    boolean unused = geolocationController.mIsRequested = geolocationController.requestLocationUpdate(phoneId, isEmergency);
                }
            });
        }
    }

    public boolean startGeolocationUpdate(int phoneId, boolean isEmergency) {
        return startGeolocationUpdate(phoneId, isEmergency, 0);
    }

    public boolean startGeolocationUpdate(int phoneId, boolean isEmergency, int delayMills) {
        boolean trigger;
        Log.i(LOG_TAG, "startGeoLocationUpdate isEmergency = " + isEmergency);
        if (SimUtil.isSoftphoneEnabled() || isEmergency) {
            trigger = true;
        } else {
            trigger = !isValidLocation(phoneId, this.mGeolocation);
        }
        if (trigger) {
            sendMessageDelayed(obtainMessage(1, phoneId, isEmergency), (long) delayMills);
        }
        return trigger;
    }

    /* access modifiers changed from: private */
    public boolean requestLocationUpdate(int phoneId, boolean isEmergency) {
        Log.i(LOG_TAG, "requestLocationUpdate : isEmergency = " + isEmergency);
        enableLocationSettings();
        if (isEmergency || !updateGeolocationFromLastKnown(phoneId)) {
            try {
                this.mLocationManager.requestLocationUpdates(new LocationRequest().setInterval(0).setFastestInterval(0).setProvider("fused").setQuality(100), this.mGeolocationListener, getLooper());
                sendMessageDelayed(obtainMessage(2), 45000);
                return true;
            } catch (IllegalArgumentException | SecurityException e) {
                e.printStackTrace();
                return true;
            }
        } else {
            restoreLocationSettings();
            return false;
        }
    }

    public void stopGeolocationUpdate() {
        Log.i(LOG_TAG, "stopGeolocationUpdate");
        sendEmptyMessage(2);
    }

    private void releaseLocationUpdate() {
        Log.e(LOG_TAG, "releaseLocationUpdate");
        try {
            this.mLocationManager.removeUpdates(this.mGeolocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            removeMessages(2);
            restoreLocationSettings();
            throw th;
        }
        removeMessages(2);
        restoreLocationSettings();
    }

    private boolean isValidLocation(int phoneId, LocationInfo geolocation) {
        LocationInfo locationInfo = geolocation;
        boolean isValid = true;
        String reason = "";
        if (locationInfo == null) {
            reason = "geolocation null";
            isValid = false;
        } else if (TextUtils.isEmpty(locationInfo.mLocationTime)) {
            reason = "mLocationTime is empty";
            isValid = false;
        } else if (TextUtils.isEmpty(locationInfo.mCountry)) {
            reason = "mCountry  is empty";
            isValid = false;
        } else if (TextUtils.isEmpty(locationInfo.mLatitude)) {
            reason = "mLatitude  is empty";
            isValid = false;
        } else if (TextUtils.isEmpty(locationInfo.mA1)) {
            reason = "mA1  is empty";
            isValid = false;
        }
        boolean z = false;
        if (!isValid) {
            Log.i(LOG_TAG, "isValidLocation: " + reason);
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long locationTime = Long.parseLong(locationInfo.mLocationTime) * 1000;
        int validLocationTime = ImsRegistry.getInt(phoneId, GlobalSettingsConstants.Call.T_VALID_LOCATION_TIME, 0);
        if (SimUtil.getSimMno(phoneId) == Mno.VODAFONE_AUSTRALIA) {
            validLocationTime *= 2;
        }
        if (currentTime - locationTime <= ((long) validLocationTime)) {
            z = true;
        }
        boolean isValid2 = z;
        Log.i(LOG_TAG, "isValidLocation(mGeolocation) (" + validLocationTime + "ms): " + isValid2 + "(Current: " + new Date(currentTime) + ") (Loc. Info received: " + new Date(locationTime));
        return isValid2;
    }

    private boolean isValidLocation(int phoneId, Location location) {
        boolean isValid = false;
        if (location == null) {
            Log.e(LOG_TAG, "isValidLocation : location is null");
            return false;
        } else if (location.isFromMockProvider()) {
            Log.e(LOG_TAG, "isValidLocation : location from Mock Provider");
            this.mCountryIso = "";
            this.mGeolocation = null;
            this.mRegistrationManager.sendDeregister(41, phoneId);
            return false;
        } else {
            long currentTime = System.currentTimeMillis();
            long locationTime = location.getTime();
            int validLocationTime = ImsRegistry.getInt(phoneId, GlobalSettingsConstants.Call.T_VALID_LOCATION_TIME, 0);
            if (currentTime - locationTime <= ((long) validLocationTime)) {
                isValid = true;
            }
            Log.i(LOG_TAG, "isValidLocation(location) (" + validLocationTime + "ms): " + isValid + "(Current: " + new Date(currentTime) + ") (Loc. Info received: " + new Date(locationTime) + "from provider [" + location.getProvider() + "])");
            return isValid;
        }
    }

    private Location getLastKnownLocation() {
        String str;
        Location lastKnownLocation = null;
        try {
            lastKnownLocation = this.mLocationManager.getLastKnownLocation("fused");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (lastKnownLocation == null) {
            try {
                lastKnownLocation = this.mLocationManager.getLastKnownLocation("network");
            } catch (SecurityException e2) {
                e2.printStackTrace();
            }
        }
        if (lastKnownLocation == null) {
            try {
                lastKnownLocation = this.mLocationManager.getLastKnownLocation("gps");
            } catch (SecurityException e3) {
                e3.printStackTrace();
            }
        }
        if (lastKnownLocation == null) {
            str = "can not find lastKnownLocation";
        } else {
            str = "lastKnownLocation from " + lastKnownLocation.getProvider();
        }
        Log.i(LOG_TAG, str);
        return lastKnownLocation;
    }

    private void updateGeolocation(int phoneId, String iso) {
        LocationInfo geolocation = GeoLocationUtility.constructData(iso, "DHCP");
        if (geolocation == null) {
            Log.i(LOG_TAG, "updateGeolocation(iso) : geolocation is null. Don't update and maintain previous one");
            return;
        }
        LocationInfo locationInfo = this.mGeolocation;
        if (locationInfo == null || !iso.equalsIgnoreCase(locationInfo.mCountry)) {
            this.mGeolocation = geolocation;
            Log.i(LOG_TAG, "updateGeolocation(iso) : mGeolocation = " + this.mGeolocation.toString());
            Mno mno = SimUtil.getSimMno(phoneId);
            if (mno == Mno.VODAFONE_AUSTRALIA || mno == Mno.TELIA_SWE) {
                this.mRegistrationManager.notifyGeolocationUpdate(this.mGeolocation, false);
            } else {
                this.mRegistrationManager.notifyGeolocationUpdate(this.mGeolocation, true);
            }
        } else {
            Log.i(LOG_TAG, "updateGeolocation(iso) : iso is same as before. Don't update and maintain previous one");
        }
    }

    /* access modifiers changed from: private */
    public void updateGeolocation(Location location) {
        LocationInfo geolocation;
        String provider;
        if (location == null || !location.isFromMockProvider()) {
            if (location == null) {
                Log.i(LOG_TAG, "updateGeolocation : country = " + this.mCountryIso);
                geolocation = GeoLocationUtility.constructData(this.mCountryIso, "DHCP");
            } else {
                if ("network".equals(location.getProvider())) {
                    provider = "DHCP";
                } else if ("gps".equals(location.getProvider())) {
                    provider = "GPS";
                } else if ("fused".equals(location.getProvider())) {
                    provider = "FUSED";
                } else {
                    provider = "DHCP";
                }
                Log.i(LOG_TAG, "updateGeolocation : provider = " + provider + ", country = " + this.mCountryIso);
                LocationInfo geolocation2 = GeoLocationUtility.constructData(location, provider, this.mContext);
                if (geolocation2 == null || !TextUtils.isEmpty(geolocation2.mCountry)) {
                    if (geolocation2 == null) {
                        Log.i(LOG_TAG, "geolocation is null!");
                    } else if (!TextUtils.isEmpty(this.mCountryIso) && !this.mCountryIso.equalsIgnoreCase(geolocation2.mCountry)) {
                        geolocation = GeoLocationUtility.constructData(this.mCountryIso, "DHCP");
                    }
                } else if (!TextUtils.isEmpty(this.mCountryIso)) {
                    geolocation2.mCountry = this.mCountryIso;
                } else if (TextUtils.isEmpty(geolocation2.mLatitude) || TextUtils.isEmpty(geolocation2.mLongitude)) {
                    geolocation = null;
                } else {
                    Log.i(LOG_TAG, "updateGeolocation :  latitude = " + geolocation2.mLatitude + ", longitude = " + geolocation2.mLongitude);
                }
                geolocation = geolocation2;
            }
            if (geolocation == null) {
                Log.i(LOG_TAG, "updateGeolocation(loc) : geolocation is null. Don't update and maintain previous one");
                return;
            }
            this.mGeolocation = geolocation;
            Log.i(LOG_TAG, "updateGeolocation(loc) : mGeolocation = " + this.mGeolocation.toString());
            this.mRegistrationManager.notifyGeolocationUpdate(this.mGeolocation, false);
            return;
        }
        Log.e(LOG_TAG, "ignore mock location");
    }

    public boolean updateGeolocationFromLastKnown(int phoneId) {
        Log.i(LOG_TAG, "updateGeolocationFromLastKnown");
        Location lastKnownLocation = getLastKnownLocation();
        if (isValidLocation(phoneId, lastKnownLocation)) {
            IMSLog.c(LogClass.VOLTE_LAST_LOCATION_PRO, "" + phoneId);
            updateGeolocation(lastKnownLocation);
            return true;
        } else if (!isValidLocation(phoneId, this.mGeolocation)) {
            return false;
        } else {
            IMSLog.c(LogClass.VOLTE_LAST_LOCATION_GEO, "" + phoneId);
            this.mRegistrationManager.notifyGeolocationUpdate(this.mGeolocation, false);
            return true;
        }
    }

    public LocationInfo getGeolocation() {
        return this.mGeolocation;
    }

    private void enableLocationSettings() {
        this.mIsLocationEnabledToRestore = this.mLocationManager.isLocationEnabledForUser(UserHandle.SEM_CURRENT);
        this.mLocationManager.setLocationEnabledForUser(true, UserHandle.SEM_CURRENT);
        this.mIsLocationEnabled = this.mLocationManager.isLocationEnabledForUser(UserHandle.SEM_CURRENT);
        Log.i(LOG_TAG, "enableLocationSettings : restore = " + this.mIsLocationEnabledToRestore);
    }

    private void restoreLocationSettings() {
        Log.i(LOG_TAG, "restoreLocationSettings : restore = " + this.mIsLocationEnabledToRestore);
        this.mLocationManager.setLocationEnabledForUser(this.mIsLocationEnabledToRestore, UserHandle.SEM_CURRENT);
        this.mIsLocationEnabled = this.mLocationManager.isLocationEnabledForUser(UserHandle.SEM_CURRENT);
    }

    public boolean isLocationServiceEnabled() {
        boolean isLocationEnabled = this.mLocationManager.isLocationEnabledForUser(UserHandle.SEM_CURRENT);
        Log.i(LOG_TAG, "isLocationServiceEnabled : " + isLocationEnabled);
        return isLocationEnabled;
    }

    public boolean isCountryCodeLoaded(int phoneId) {
        if (this.mGeolocation == null) {
            return false;
        }
        if (SimUtil.getSimMno(phoneId) != Mno.SPRINT || this.mTelephonyManager.getDataNetworkType(SimUtil.getSubId(phoneId)) == 13 || isValidLocation(phoneId, this.mGeolocation)) {
            return !TextUtils.isEmpty(this.mGeolocation.mCountry);
        }
        Log.i(LOG_TAG, "isCountryCodeLoaded : location expired, return false");
        this.mGeolocation = null;
        this.mCountryIso = "";
        return false;
    }

    private PendingIntent getRetryRequestLocationIntent(int phoneId) {
        Intent intent = new Intent();
        intent.setAction(INTENT_PERIODIC_LOCATION_UPDATE_TIMER_EXPD);
        intent.putExtra("phoneId", phoneId);
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
    }

    /* access modifiers changed from: private */
    public void startPeriodicLocationUpdate(int phoneId) {
        Log.i(LOG_TAG, "startPeriodicLocationUpdate(" + phoneId + "), mIsEpdgAvaialble: " + this.mIsEpdgAvaialble[phoneId] + " mVoiceRegState: " + this.mVoiceRegState[phoneId]);
        if (this.mIsEpdgAvaialble[phoneId] && this.mVoiceRegState[phoneId] != 0) {
            this.mAlarmManager.cancel(getRetryRequestLocationIntent(phoneId));
            this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + 900000, getRetryRequestLocationIntent(phoneId));
        }
    }

    public void stopPeriodicLocationUpdate(int phoneId) {
        Log.i(LOG_TAG, "stopPeriodicLocationUpdate(" + phoneId + ")");
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno.isHk() || mno == Mno.H3G_SE || mno == Mno.VODAFONE_AUSTRALIA) {
            this.mAlarmManager.cancel(getRetryRequestLocationIntent(phoneId));
        }
    }

    private class GeolocationListener implements LocationListener {
        private GeolocationListener() {
        }

        public void onLocationChanged(Location location) {
            Log.i(GeolocationController.LOG_TAG, "onLocationChanged : location = " + IMSLog.checker(location));
            Mno mno = SimUtil.getSimMno(GeolocationController.this.mPhoneId);
            if (location == null) {
                return;
            }
            if (mno != Mno.ATT || GeoLocationUtility.isLocationValid(location)) {
                try {
                    Log.i(GeolocationController.LOG_TAG, "onLocationChanged : removing location listener");
                    IMSLog.c(LogClass.VOLTE_UPDATE_LOCATION_PRO, "" + location.getProvider());
                    GeolocationController.this.updateGeolocation(location);
                    GeolocationController.this.sendEmptyMessage(2);
                } catch (IllegalArgumentException e) {
                    IMSLog.s(GeolocationController.LOG_TAG, "onLocationChanged ex: " + e.getMessage());
                }
            }
        }

        public void onStatusChanged(String var1, int var2, Bundle var3) {
        }

        public void onProviderEnabled(String var1) {
        }

        public void onProviderDisabled(String var1) {
        }
    }

    private String msgToString(int msg) {
        if (msg == 1) {
            return "START_LOCATION_UPDATE";
        }
        if (msg == 2) {
            return "STOP_LOCATION_UPDATE";
        }
        if (msg == 3) {
            return "START_PERIODIC_LOCATION_UPDATE";
        }
        if (msg == 4) {
            return "SERVICE_STATE_CHANGED";
        }
        if (msg == 5) {
            return "EPDG_AVAILABLE";
        }
        return "UNKNOWN(" + msg + ")";
    }

    private class IntentListener {
        private final BroadcastReceiver mReceiver;

        private IntentListener() {
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.i(GeolocationController.LOG_TAG, "Received Intent : " + action);
                    int phoneId = intent.getIntExtra("phoneId", 0);
                    if (GeolocationController.INTENT_EPDG_SSID_CHANGED.equals(action)) {
                        if (ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.Call.VOWIFI_NEED_LOCATION_MENU, false) && GeolocationController.this.mVoiceRegState[phoneId] != 0) {
                            LocationInfo unused = GeolocationController.this.mGeolocation = null;
                        }
                    } else if (GeolocationController.INTENT_PERIODIC_LOCATION_UPDATE_TIMER_EXPD.equals(action)) {
                        GeolocationController.this.sendMessage(GeolocationController.this.obtainMessage(1, phoneId, 0));
                        GeolocationController.this.startPeriodicLocationUpdate(phoneId);
                    } else if (GeolocationController.INTENT_PROVIDERS_CHANGED.equals(action)) {
                        boolean isLocationEnabled = GeolocationController.this.mLocationManager.isLocationEnabledForUser(UserHandle.SEM_CURRENT);
                        Log.i(GeolocationController.LOG_TAG, "prev loc : " + GeolocationController.this.mIsLocationEnabled + ", cur loc : " + isLocationEnabled);
                        if (GeolocationController.this.mIsLocationEnabled != isLocationEnabled) {
                            boolean unused2 = GeolocationController.this.mIsLocationEnabled = isLocationEnabled;
                            boolean unused3 = GeolocationController.this.mIsLocationEnabledToRestore = isLocationEnabled;
                        }
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(GeolocationController.INTENT_EPDG_SSID_CHANGED);
            intentFilter.addAction(GeolocationController.INTENT_PERIODIC_LOCATION_UPDATE_TIMER_EXPD);
            intentFilter.addAction(GeolocationController.INTENT_PROVIDERS_CHANGED);
            GeolocationController.this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }
    }

    public void notifyServiceStateChanged(int phoneId, ServiceStateWrapper serviceState) {
        sendMessage(obtainMessage(4, phoneId, 0, serviceState));
    }

    public void onServiceStateChanged(int phoneId, ServiceStateWrapper serviceState) {
        Log.i(LOG_TAG, "onServiceStateChanged(" + serviceState + ")");
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno != Mno.SPRINT || serviceState.getDataNetworkType() == 13) {
            if (mno.isHk() || mno == Mno.H3G_SE || mno == Mno.VODAFONE_AUSTRALIA) {
                if (this.mVoiceRegState[phoneId] == 0 && serviceState.getVoiceRegState() != 0) {
                    sendMessageDelayed(obtainMessage(3, Integer.valueOf(phoneId)), 1800000);
                } else if (this.mVoiceRegState[phoneId] != 0 && serviceState.getVoiceRegState() == 0) {
                    stopPeriodicLocationUpdate(phoneId);
                }
            }
            this.mVoiceRegState[phoneId] = serviceState.getVoiceRegState();
            this.mDataRegState[phoneId] = serviceState.getDataRegState();
            if (this.mVoiceRegState[phoneId] == 0 || this.mDataRegState[phoneId] == 0) {
                String iso = this.mTelephonyManager.getNetworkCountryIso();
                Log.i(LOG_TAG, "onServiceStateChanged[" + phoneId + "] : mCountryIso = " + this.mCountryIso + ", iso = " + iso);
                if (!TextUtils.isEmpty(iso) && !this.mCountryIso.equalsIgnoreCase(iso)) {
                    this.mCountryIso = iso;
                    if (!SimUtil.isSoftphoneEnabled()) {
                        updateGeolocation(phoneId, this.mCountryIso);
                        return;
                    }
                    return;
                }
                return;
            }
            this.mCountryIso = "";
            return;
        }
        Log.e(LOG_TAG, "ignore phone state listener");
    }

    public void notifyEpdgAvailable(int phoneId, int isAvailable) {
        sendMessage(obtainMessage(5, phoneId, isAvailable));
    }

    public void onEpdgAvailable(int phoneId, boolean isAvailable) {
        Log.i(LOG_TAG, "setEpdgAvailable : phoneId : " + phoneId + ", prevEpdgState =  " + this.mIsEpdgAvaialble[phoneId] + " curEpdgState : " + isAvailable + " mIsForceEpdgAvailUpdate :" + this.mIsForceEpdgAvailUpdate[phoneId]);
        Mno mno = SimUtil.getSimMno(phoneId);
        if (this.mIsForceEpdgAvailUpdate[phoneId] || isAvailable != this.mIsEpdgAvaialble[phoneId]) {
            this.mIsEpdgAvaialble[phoneId] = isAvailable;
            boolean vowifiNeedLocationMenu = ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.Call.VOWIFI_NEED_LOCATION_MENU, false);
            boolean vowifiDtLocUserConsent = mno == Mno.TMOBILE || mno == Mno.TMOBILE_NED;
            if (vowifiNeedLocationMenu || vowifiDtLocUserConsent) {
                this.mIsForceEpdgAvailUpdate[phoneId] = false;
                if (!this.mIsEpdgAvaialble[phoneId]) {
                    sendEmptyMessage(2);
                    if (mno.isHk() || mno == Mno.H3G_SE || mno == Mno.VODAFONE_AUSTRALIA) {
                        stopPeriodicLocationUpdate(phoneId);
                    }
                    if ((mno == Mno.HK3 || mno == Mno.SMARTONE || mno == Mno.CMHK || mno == Mno.CSL || mno == Mno.PCCW || mno == Mno.H3G_SE) && this.mVoiceRegState[phoneId] != 0) {
                        this.mGeolocation = null;
                    }
                } else if (!vowifiDtLocUserConsent || this.mIsLocationUserConsent[phoneId] == 1) {
                    sendMessage(obtainMessage(1, phoneId, 0));
                    if (mno.isHk() || mno == Mno.H3G_SE || mno == Mno.VODAFONE_AUSTRALIA) {
                        sendMessageDelayed(obtainMessage(3), 45000);
                    }
                }
            }
        }
    }

    private void registerDtLocUserConsentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.sec.ims.settings/dtlocuserconsent"), true, this.mDtLocUserConsentObserver);
    }
}
