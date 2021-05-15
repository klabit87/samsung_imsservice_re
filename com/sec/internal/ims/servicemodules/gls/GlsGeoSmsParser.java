package com.sec.internal.ims.servicemodules.gls;

import android.location.Location;
import android.net.Uri;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.HashMap;

public class GlsGeoSmsParser {
    private static final String LOG_TAG = GlsGeoSmsParser.class.getSimpleName();

    public String getGlsExtInfo(String body) {
        String str = LOG_TAG;
        IMSLog.s(str, "body=" + body);
        try {
            GlsData data = parse(body);
            if (data == null) {
                Log.e(LOG_TAG, "getGlsExtInfo, data is null.");
                return null;
            }
            GlsValidityTime validityDate = data.getValidityDate();
            Location location = data.getLocation();
            LocationType type = data.getLocationType();
            String label = type == LocationType.OWN_LOCATION ? "" : data.getLabel();
            long validitytime = 0;
            if (validityDate != null) {
                if (validityDate.getValidityDate() != null) {
                    validitytime = validityDate.getValidityDate().getTime();
                    return location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + validitytime + "," + label + "," + type.toString();
                }
            }
            if (data.getDate() != null) {
                validitytime = data.getDate().getTime();
            }
            return location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + validitytime + "," + label + "," + type.toString();
        } catch (Exception e) {
            IMSLog.s(LOG_TAG, e.toString());
            return null;
        }
    }

    public GlsData parse(String geoSms) throws Exception {
        LocationType locationType;
        String str = LOG_TAG;
        Log.d(str, "parse enter: geoSms = " + geoSms);
        try {
            Uri uri = Uri.parse(geoSms);
            if (!uri.getScheme().equals("geo")) {
                return null;
            }
            String[] fieldStrs = uri.getSchemeSpecificPart().split(";");
            String[] fullLocationParts = fieldStrs[0].split(",");
            HashMap<String, String> fieldMap = new HashMap<>();
            fieldMap.put("crs", (Object) null);
            fieldMap.put("u", (Object) null);
            fieldMap.put("rcs-l", (Object) null);
            for (int index = 1; index < fieldStrs.length; index++) {
                String[] keyValue = fieldStrs[index].split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                if (fieldMap.containsKey(keyValue[0])) {
                    fieldMap.put(keyValue[0], keyValue[1]);
                }
            }
            String radiusStr = fieldMap.get("u");
            double radius = radiusStr == null ? 0.0d : Double.valueOf(radiusStr).doubleValue();
            double lat = Double.valueOf(fullLocationParts[0]).doubleValue();
            double lon = Double.valueOf(fullLocationParts[1]).doubleValue();
            Location location = new Location("passive");
            location.setLatitude(lat);
            String[] strArr = fieldStrs;
            location.setLongitude(lon);
            location.setAccuracy((float) radius);
            if (!fieldMap.get("crs").equals("gcj02")) {
                Log.d(LOG_TAG, "parse fail: crs is not gcj02.");
                return null;
            }
            String label = fieldMap.get("rcs-l");
            if (label != null) {
                locationType = LocationType.OTHER_LOCATION;
            } else {
                locationType = LocationType.OWN_LOCATION;
            }
            String str2 = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            String str3 = radiusStr;
            sb.append("parse success: location = ");
            sb.append(location);
            sb.append(" label = ");
            sb.append(label);
            Log.d(str2, sb.toString());
            return new GlsData((String) null, (ImsUri) null, location, locationType, (Date) null, label, (GlsValidityTime) null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
