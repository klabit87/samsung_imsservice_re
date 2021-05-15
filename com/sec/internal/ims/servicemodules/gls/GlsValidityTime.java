package com.sec.internal.ims.servicemodules.gls;

import java.util.Date;

public class GlsValidityTime {
    private final int mTimeZone;
    private final Date mValidityDate;

    public GlsValidityTime(Date validityDate, int timeZone) {
        this.mValidityDate = validityDate;
        this.mTimeZone = timeZone;
    }

    public Date getValidityDate() {
        return this.mValidityDate;
    }

    public int getTimeZone() {
        return this.mTimeZone;
    }

    public String toString() {
        return "Validity DateTime(" + "date=" + this.mValidityDate.toString() + ", time zone=" + this.mTimeZone + ')';
    }
}
