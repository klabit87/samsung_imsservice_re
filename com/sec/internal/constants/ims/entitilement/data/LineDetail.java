package com.sec.internal.constants.ims.entitilement.data;

import java.io.Serializable;

public class LineDetail implements Serializable {
    public String e911AddressId;
    public String e911AidExpiration;
    public long lineId;
    public int locationStatus;
    public String msisdn;
    public String serviceFingerPrint;
    public String serviceInstanceId;
    public String serviceTokenExpiryTime;
    public int status;
    public int tcStatus;
}
