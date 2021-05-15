package com.sec.internal.ims.servicemodules.gls;

public class GlsGeoSmsComposer {
    public String compose(GlsData data, int byteLimit) throws NullPointerException {
        if (data != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("geo:");
            builder.append(data.getLocation().getLatitude());
            builder.append(",");
            builder.append(data.getLocation().getLongitude());
            builder.append(";");
            builder.append("crs=gcj02;");
            builder.append("u=");
            builder.append(data.getLocation().getAccuracy());
            builder.append(";");
            builder.append("rcs-l=");
            builder.append(data.getLabel());
            if (byteLimit > 0) {
                return subStr(builder.toString(), byteLimit);
            }
            return builder.toString();
        }
        throw new NullPointerException("GlsData is null");
    }

    private static String subStr(String str, int num) {
        if (str == null || str.length() == 0 || num <= 0) {
            return "";
        }
        if (str.getBytes().length <= num) {
            return str;
        }
        int len = str.length();
        int sum = 0;
        StringBuilder sb = new StringBuilder(num);
        for (int i = 0; i < len; i++) {
            String temp = String.valueOf(str.charAt(i));
            sum += temp.getBytes().length;
            if (sum > num) {
                break;
            }
            sb.append(temp);
        }
        return sb.toString();
    }
}
