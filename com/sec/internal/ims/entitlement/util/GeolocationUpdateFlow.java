package com.sec.internal.ims.entitlement.util;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import com.sec.internal.ims.servicemodules.gls.GlsIntent;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeolocationUpdateFlow extends Handler {
    private static final int EVENT_LOCATION_UPDATED = 3;
    private static final int EVENT_LOCATION_UPDATE_TIMEOUT = 1;
    private static final int EVENT_STOP_LOCATION_UPDATE = 2;
    private static final int GPS_LOCATION_REQUEST_TIMEOUT = 45000;
    public static final int GPS_ONLY = 1;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = GeolocationUpdateFlow.class.getSimpleName();
    public static final int NLP_AND_GPS = 3;
    public static final int NLP_ONLY = 0;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private GPSLocationListener mGPSLocationListener = new GPSLocationListener();
    private final LocationManager mLocationManager;
    private LocationUpdateListener mLocationUpdateListener;
    private NLPLocationListener mNLPLocationListener = new NLPLocationListener();
    private int mStatus = 0;
    private int mUserLocationMode;
    private String mUserLocationProvider;

    public interface LocationUpdateListener {
        void onAddressObtained(Address address);
    }

    public GeolocationUpdateFlow(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mLocationManager = (LocationManager) context.getSystemService(GlsIntent.Extras.EXTRA_LOCATION);
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        IMSLog.i(str, "handle msg event: " + msg.what);
        int i = msg.what;
        if (i == 1) {
            stopGeolocationUpdate();
            restoreUserLocationSettings();
            sendMessage(obtainMessage(3));
        } else if (i == 2) {
            stopGeolocationUpdate();
        } else if (i == 3) {
            if (msg.obj != null) {
                this.mLocationUpdateListener.onAddressObtained((Address) msg.obj);
            } else {
                this.mLocationUpdateListener.onAddressObtained((Address) null);
            }
        }
    }

    public void requestGeolocationUpdate(int updateTime, int accuracy, int provider, LocationUpdateListener listener) {
        String str = LOG_TAG;
        IMSLog.i(str, "requestGeolocationUpdate(): mStatus = " + this.mStatus);
        if (this.mStatus == 0) {
            this.mLocationUpdateListener = listener;
            startGeolocationUpdate(updateTime, accuracy, provider);
            return;
        }
        throw new RuntimeException("Flow has already been started.");
    }

    /* access modifiers changed from: private */
    public void enforceLocationSettings(int provider) {
        String str = LOG_TAG;
        IMSLog.i(str, "enforceLocationSettings(): provider = " + provider);
        Settings.Secure.putInt(this.mContentResolver, "location_mode", 3);
        if (provider == 0) {
            Settings.Secure.putString(this.mContentResolver, "location_providers_allowed", "network");
        } else if (provider == 1) {
            Settings.Secure.putString(this.mContentResolver, "location_providers_allowed", "gps");
        } else if (provider != 3) {
            Settings.Secure.putString(this.mContentResolver, "location_providers_allowed", "network,gps");
        } else {
            Settings.Secure.putString(this.mContentResolver, "location_providers_allowed", "network,gps");
        }
    }

    /* access modifiers changed from: private */
    public void getUserLocationSettings() {
        this.mUserLocationMode = Settings.Secure.getInt(this.mContentResolver, "location_mode", 0);
        this.mUserLocationProvider = Settings.Secure.getString(this.mContentResolver, "location_providers_allowed");
        String str = LOG_TAG;
        IMSLog.i(str, "getUserLocationSettings(): mUserLocationMode: " + this.mUserLocationMode + ", mUserLocationProvider: " + this.mUserLocationProvider);
    }

    private void restoreUserLocationSettings() {
        String str = LOG_TAG;
        IMSLog.i(str, "restoreUserLocationSettings(): mUserLocationMode: " + this.mUserLocationMode + ", mUserLocationProvider: " + this.mUserLocationProvider);
        Settings.Secure.putInt(this.mContentResolver, "location_mode", this.mUserLocationMode);
        Settings.Secure.putString(this.mContentResolver, "location_providers_allowed", this.mUserLocationProvider);
    }

    private void startGeolocationUpdate(final int updateTime, final int accuracy, final int provider) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                GeolocationUpdateFlow.this.getUserLocationSettings();
                GeolocationUpdateFlow.this.enforceLocationSettings(provider);
                GeolocationUpdateFlow.this.requestLocationUpdates(updateTime, accuracy, provider);
                Looper.loop();
            }
        }).start();
    }

    private void stopGeolocationUpdate() {
        try {
            this.mLocationManager.removeUpdates(this.mGPSLocationListener);
            this.mLocationManager.removeUpdates(this.mNLPLocationListener);
        } catch (IllegalArgumentException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "onLocationChanged ex: " + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void requestLocationUpdates(int updateTime, int accuracy, int provider) {
        String str = LOG_TAG;
        IMSLog.i(str, "requestLocationUpdates(): updateTime = " + updateTime + ", accuracy = " + accuracy + ", provider = " + provider);
        if (provider != 0) {
            if (provider != 1) {
                try {
                    if (this.mLocationManager.isProviderEnabled("network") || this.mLocationManager.isProviderEnabled("gps")) {
                        if (this.mLocationManager.isProviderEnabled("network")) {
                            IMSLog.i(LOG_TAG, "requestLocationUpdates(): NLP enabled");
                            this.mLocationManager.requestLocationUpdates("network", (long) updateTime, (float) accuracy, this.mNLPLocationListener);
                        }
                        if (this.mLocationManager.isProviderEnabled("gps")) {
                            IMSLog.i(LOG_TAG, "requestLocationUpdates(): GPS enabled");
                            this.mLocationManager.requestLocationUpdates("gps", (long) updateTime, (float) accuracy, this.mGPSLocationListener);
                        }
                    } else {
                        IMSLog.e(LOG_TAG, "requestLocationUpdates(): providers are all disabled");
                        sendMessage(obtainMessage(3));
                        return;
                    }
                } catch (IllegalArgumentException | SecurityException e) {
                    String str2 = LOG_TAG;
                    IMSLog.s(str2, "ex =" + e.getMessage());
                }
            } else if (this.mLocationManager.isProviderEnabled("gps")) {
                IMSLog.i(LOG_TAG, "requestLocationUpdates(): GPS enabled");
                this.mLocationManager.requestLocationUpdates("gps", (long) updateTime, (float) accuracy, this.mGPSLocationListener);
            } else {
                IMSLog.i(LOG_TAG, "requestLocationUpdates(): GPS disabled");
                sendMessage(obtainMessage(3));
                return;
            }
        } else if (this.mLocationManager.isProviderEnabled("network")) {
            IMSLog.i(LOG_TAG, "requestLocationUpdates(): NLP enabled");
            this.mLocationManager.requestLocationUpdates("network", (long) updateTime, (float) accuracy, this.mNLPLocationListener);
        } else {
            IMSLog.i(LOG_TAG, "requestLocationUpdates(): NLP disabled");
            sendMessage(obtainMessage(3));
            return;
        }
        IMSLog.i(LOG_TAG, "requestLocation(): location req timeout = 45000");
        sendMessageDelayed(obtainMessage(1), 45000);
    }

    /* access modifiers changed from: private */
    public void getLastKnownGPSLocation() {
        IMSLog.i(LOG_TAG, "getLastKnownGPSLocation");
        Location loc = this.mLocationManager.getLastKnownLocation("gps");
        if (loc == null) {
            IMSLog.e(LOG_TAG, "getLastKnownGPSLocation(): No Last Known Location Available");
        }
        sendMessage(obtainMessage(3, getAddressFromLocation(loc)));
        restoreUserLocationSettings();
    }

    /* access modifiers changed from: private */
    public void getLastKnownNLPLocation() {
        IMSLog.i(LOG_TAG, "getLastKnownNLPLocation");
        Location loc = this.mLocationManager.getLastKnownLocation("network");
        if (loc == null) {
            IMSLog.e(LOG_TAG, "getLastKnownNLPLocation(): No Last Known Location Available");
        }
        sendMessage(obtainMessage(3, getAddressFromLocation(loc)));
        restoreUserLocationSettings();
    }

    public Address getAddressFromLocation(Location location) {
        if (!Geocoder.isPresent()) {
            IMSLog.e(LOG_TAG, "Geocoder is not present.");
            return null;
        } else if (location == null) {
            IMSLog.e(LOG_TAG, "Location is null.");
            return null;
        } else {
            List<Address> addresses = null;
            try {
                addresses = new Geocoder(this.mContext, Locale.getDefault()).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException | IllegalArgumentException e) {
                String str = LOG_TAG;
                IMSLog.s(str, "Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude() + e.getMessage());
            }
            if (addresses != null && addresses.size() != 0) {
                return addresses.get(0);
            }
            IMSLog.e(LOG_TAG, "No address is found.");
            return null;
        }
    }

    private class GPSLocationListener implements LocationListener {
        private GPSLocationListener() {
        }

        public void onLocationChanged(Location location) {
            IMSLog.i(GeolocationUpdateFlow.LOG_TAG, "onLocationChanged");
            GeolocationUpdateFlow.this.removeMessages(1);
            GeolocationUpdateFlow geolocationUpdateFlow = GeolocationUpdateFlow.this;
            geolocationUpdateFlow.sendMessage(geolocationUpdateFlow.obtainMessage(2));
            GeolocationUpdateFlow.this.getLastKnownGPSLocation();
        }

        public void onProviderDisabled(String arg0) {
        }

        public void onProviderEnabled(String arg0) {
        }

        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    }

    private class NLPLocationListener implements LocationListener {
        private NLPLocationListener() {
        }

        public void onLocationChanged(Location location) {
            IMSLog.i(GeolocationUpdateFlow.LOG_TAG, "onLocationChanged");
            GeolocationUpdateFlow.this.removeMessages(1);
            GeolocationUpdateFlow geolocationUpdateFlow = GeolocationUpdateFlow.this;
            geolocationUpdateFlow.sendMessage(geolocationUpdateFlow.obtainMessage(2));
            GeolocationUpdateFlow.this.getLastKnownNLPLocation();
        }

        public void onProviderDisabled(String arg0) {
        }

        public void onProviderEnabled(String arg0) {
        }

        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    }
}
