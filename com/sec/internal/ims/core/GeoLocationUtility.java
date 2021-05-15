package com.sec.internal.ims.core;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

class GeoLocationUtility {
    private static final String LOG_TAG = GeoLocationUtility.class.getSimpleName();
    private static LocationInfo mLocationInfo = null;

    GeoLocationUtility() {
    }

    private static synchronized void updateLocationInfo(LocationInfo locationInfo) {
        synchronized (GeoLocationUtility.class) {
            mLocationInfo = locationInfo;
        }
    }

    static LocationInfo constructData(String countryIso, String providerType) {
        String str = LOG_TAG;
        IMSLog.s(str, "constructData, countryIso : " + countryIso);
        if (TextUtils.isEmpty(countryIso)) {
            return null;
        }
        LocationInfo locationInfo = mLocationInfo;
        if (locationInfo != null && countryIso.equalsIgnoreCase(locationInfo.mCountry)) {
            return mLocationInfo;
        }
        LocationInfo locationInfo2 = new LocationInfo();
        long locationtime = System.currentTimeMillis();
        locationInfo2.mProviderType = providerType;
        locationInfo2.mRetentionExpires = getInternetDateTimeFormat(locationtime);
        locationInfo2.mSRSName = "urn:ogc:def:crs:EPSG::4326";
        locationInfo2.mRadiusUOM = "urn:ogc:def:uom:EPSG::9001";
        locationInfo2.mOS = "Android " + Build.VERSION.RELEASE;
        locationInfo2.mLocationTime = String.valueOf(locationtime / 1000);
        locationInfo2.mDeviceId = "urn:uuid:" + UUID.randomUUID().toString();
        locationInfo2.mCountry = countryIso.toUpperCase();
        updateLocationInfo(locationInfo2);
        return locationInfo2;
    }

    static LocationInfo constructData(Location location, String providerType, Context mContext) {
        String str;
        String str2;
        String str3 = providerType;
        double latitude = location.getLatitude();
        String slatitude = Location.convert(latitude, 2) + (latitude > 0.0d ? "N" : "S");
        double longitude = location.getLongitude();
        String EW = longitude > 0.0d ? "E" : "W";
        String slongitude = Location.convert(longitude, 2) + EW;
        float accuracy = location.getAccuracy();
        long locationtime = location.getTime() / 1000;
        IMSLog.s(LOG_TAG, "constructData: providerType=" + str3 + " slatitude=" + slatitude + " slongitude=" + slongitude + " accuracy " + accuracy + " locationtime " + locationtime);
        NumberFormat mNumberFormat = NumberFormat.getInstance();
        mNumberFormat.setMinimumFractionDigits(5);
        mNumberFormat.setMaximumFractionDigits(340);
        LocationInfo locationInfo = new LocationInfo();
        String str4 = EW;
        locationInfo.mLatitude = mNumberFormat.format(latitude);
        locationInfo.mLongitude = mNumberFormat.format(longitude);
        String str5 = slongitude;
        locationInfo.mAltitude = mNumberFormat.format(location.getAltitude());
        locationInfo.mAccuracy = String.valueOf(accuracy);
        locationInfo.mProviderType = str3;
        locationInfo.mRetentionExpires = getInternetDateTimeFormat(location.getTime());
        locationInfo.mSRSName = "urn:ogc:def:crs:EPSG::4326";
        locationInfo.mRadiusUOM = "urn:ogc:def:uom:EPSG::9001";
        locationInfo.mOS = "Android " + Build.VERSION.RELEASE;
        locationInfo.mLocationTime = String.valueOf(locationtime);
        locationInfo.mDeviceId = "urn:uuid:" + UUID.randomUUID().toString();
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        if (Geocoder.isPresent()) {
            List<Address> addresses = getAddressUsingGeocoder(latitude, longitude, geocoder);
            if (addresses == null || addresses.size() <= 0) {
            } else {
                Address address = addresses.get(0);
                double d = latitude;
                locationInfo.mCountry = address.getCountryCode();
                locationInfo.mA1 = address.getAdminArea();
                locationInfo.mA3 = address.getLocality();
                if (address.getThoroughfare() != null) {
                    str = address.getThoroughfare();
                } else {
                    str = address.getSubLocality();
                }
                locationInfo.mA6 = str;
                if (address.getFeatureName() != null) {
                    str2 = address.getFeatureName();
                } else {
                    str2 = address.getPremises();
                }
                locationInfo.mHNO = str2;
                locationInfo.mPC = address.getPostalCode();
            }
            IMSLog.s(LOG_TAG, "constructData getAddressUsingGeocoder: mCountry=" + locationInfo.mCountry + " mA1=" + locationInfo.mA1 + " mA3=" + locationInfo.mA3 + " mA6=" + locationInfo.mA6 + " mHNO=" + locationInfo.mHNO + " mPC=" + locationInfo.mPC);
            if (locationInfo.mCountry != null) {
                locationInfo.mCountry = locationInfo.mCountry.toUpperCase();
            }
            if (!(locationInfo.mLatitude == null || locationInfo.mLongitude == null)) {
                locationInfo.mLatitude = locationInfo.mLatitude.replace(",", ".");
                locationInfo.mLongitude = locationInfo.mLongitude.replace(",", ".");
            }
            updateLocationInfo(locationInfo);
            return locationInfo;
        }
        Log.e(LOG_TAG, "geocoder is not created");
        return null;
    }

    static String getInternetDateTimeFormat(long millis) {
        long j = millis;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (j != 0) {
            calendar.setTimeInMillis(j);
        }
        calendar.add(11, 24);
        return String.format("%2d-%02d-%02dT%02d%s%02d%s%02d.%02dZ", new Object[]{Integer.valueOf(calendar.get(1)), Integer.valueOf(calendar.get(2) + 1), Integer.valueOf(calendar.get(5)), Integer.valueOf(calendar.get(11)), ":", Integer.valueOf(calendar.get(12)), ":", Integer.valueOf(calendar.get(13)), Integer.valueOf(calendar.get(14) / 100)});
    }

    static List<Address> getAddressUsingGeocoder(double latitude, double longitude, Geocoder geocoder) {
        try {
            return geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException | IllegalArgumentException e) {
            String str = LOG_TAG;
            IMSLog.i(str, "getAddressUsingGeocoder: " + e.getMessage());
            return null;
        }
    }
}
