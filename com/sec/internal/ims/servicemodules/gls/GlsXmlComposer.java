package com.sec.internal.ims.servicemodules.gls;

import android.location.Location;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import com.sec.internal.helper.Iso8601;

public class GlsXmlComposer {
    public String compose(GlsData data) throws NullPointerException {
        if (data != null) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<rcsenvelope xmlns=\"urn:gsma:params:xml:ns:rcs:rcs:geolocation\"" + " xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\"" + " xmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\"" + " xmlns:gml=\"http://www.opengis.net/gml\"" + " xmlns:gs=\"http://www.opengis.net/pidflo/1.0\"" + " entity=\"" + data.getSender().toString() + "\">" + addRcsPushLocation(data) + addValidityDate(data) + "<gp:geopriv>" + "<gp:location-info>" + "<gs:Circle srsName=\"urn:ogc:def:crs:EPSG::4326\">" + "<gml:pos>" + addPosition(data.getLocation()) + "</gml:pos>" + "<gs:radius uom=\"urn:ogc:def:uom:EPSG::9001\">" + data.getLocation().getAccuracy() + "</gs:radius>" + "</gs:Circle>" + "</gp:location-info>" + addValidityDateUsageRules(data) + "</gp:geopriv>" + "<timestamp>" + Iso8601.format(data.getDate()) + "</timestamp>" + "</rcspushlocation>" + "</rcsenvelope>";
        }
        throw new NullPointerException("GlsData is null");
    }

    private static String addPosition(Location location) {
        return location.getLatitude() + " " + location.getLongitude();
    }

    private static String addValidityDate(GlsData data) {
        if (data.getValidityDate() == null) {
            return "";
        }
        return "<rpid:time-offset rpid:until=\"" + Iso8601.format(data.getValidityDate().getValidityDate()) + "\">" + data.getValidityDate().getTimeZone() + "</rpid:time-offset>";
    }

    private static String addValidityDateUsageRules(GlsData data) {
        if (data.getValidityDate() == null) {
            return "";
        }
        return "<gp:usage-rules>" + "<gp:retention-expiry>" + Iso8601.format(data.getValidityDate().getValidityDate()) + "</gp:retention-expiry>" + "</gp:usage-rules>";
    }

    private static String addRcsPushLocation(GlsData data) {
        StringBuilder builder = new StringBuilder();
        builder.append("<rcspushlocation id=\"");
        builder.append(data.getId());
        if (data.getLocationType().equals(LocationType.OWN_LOCATION)) {
            builder.append("\">");
        } else if (data.getLabel() != null) {
            builder.append("\" label=\"");
            builder.append(data.getLabel());
            builder.append("\">");
        } else {
            builder.append("\" label=\"\">");
        }
        return builder.toString();
    }
}
