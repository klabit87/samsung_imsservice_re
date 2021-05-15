package org.xbill.DNS;

import java.io.IOException;

public class GPOSRecord extends Record {
    private static final long serialVersionUID = -6349714958085750705L;
    private byte[] altitude;
    private byte[] latitude;
    private byte[] longitude;

    GPOSRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new GPOSRecord();
    }

    private void validate(double longitude2, double latitude2) throws IllegalArgumentException {
        if (longitude2 < -90.0d || longitude2 > 90.0d) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("illegal longitude ");
            stringBuffer.append(longitude2);
            throw new IllegalArgumentException(stringBuffer.toString());
        } else if (latitude2 < -180.0d || latitude2 > 180.0d) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("illegal latitude ");
            stringBuffer2.append(latitude2);
            throw new IllegalArgumentException(stringBuffer2.toString());
        }
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public GPOSRecord(Name name, int dclass, long ttl, double longitude2, double latitude2, double altitude2) {
        super(name, 27, dclass, ttl);
        double d = longitude2;
        double d2 = latitude2;
        validate(longitude2, latitude2);
        this.longitude = Double.toString(longitude2).getBytes();
        this.latitude = Double.toString(latitude2).getBytes();
        this.altitude = Double.toString(altitude2).getBytes();
    }

    public GPOSRecord(Name name, int dclass, long ttl, String longitude2, String latitude2, String altitude2) {
        super(name, 27, dclass, ttl);
        try {
            this.longitude = byteArrayFromString(longitude2);
            this.latitude = byteArrayFromString(latitude2);
            validate(getLongitude(), getLatitude());
            this.altitude = byteArrayFromString(altitude2);
        } catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.longitude = in.readCountedString();
        this.latitude = in.readCountedString();
        this.altitude = in.readCountedString();
        try {
            validate(getLongitude(), getLatitude());
        } catch (IllegalArgumentException e) {
            throw new WireParseException(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        try {
            this.longitude = byteArrayFromString(st.getString());
            this.latitude = byteArrayFromString(st.getString());
            this.altitude = byteArrayFromString(st.getString());
            try {
                validate(getLongitude(), getLatitude());
            } catch (IllegalArgumentException e) {
                throw new WireParseException(e.getMessage());
            }
        } catch (TextParseException e2) {
            throw st.exception(e2.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(byteArrayToString(this.longitude, true));
        sb.append(" ");
        sb.append(byteArrayToString(this.latitude, true));
        sb.append(" ");
        sb.append(byteArrayToString(this.altitude, true));
        return sb.toString();
    }

    public String getLongitudeString() {
        return byteArrayToString(this.longitude, false);
    }

    public double getLongitude() {
        return Double.parseDouble(getLongitudeString());
    }

    public String getLatitudeString() {
        return byteArrayToString(this.latitude, false);
    }

    public double getLatitude() {
        return Double.parseDouble(getLatitudeString());
    }

    public String getAltitudeString() {
        return byteArrayToString(this.altitude, false);
    }

    public double getAltitude() {
        return Double.parseDouble(getAltitudeString());
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeCountedString(this.longitude);
        out.writeCountedString(this.latitude);
        out.writeCountedString(this.altitude);
    }
}
