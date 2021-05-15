package com.sec.sve;

import android.os.Parcel;
import android.os.Parcelable;

public class TimeInfo implements Parcelable {
    public static final Parcelable.Creator<TimeInfo> CREATOR = new Parcelable.Creator<TimeInfo>() {
        public TimeInfo createFromParcel(Parcel in) {
            return new TimeInfo(in);
        }

        public TimeInfo[] newArray(int size) {
            return new TimeInfo[size];
        }
    };
    private long ntpTimestamp;
    private long rtpTimestamp;

    public TimeInfo() {
        this.rtpTimestamp = 0;
        this.ntpTimestamp = 0;
    }

    public TimeInfo(long rtp, long ntp) {
        this.rtpTimestamp = rtp;
        this.ntpTimestamp = ntp;
    }

    private TimeInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (out != null) {
            out.writeLong(this.rtpTimestamp);
            out.writeLong(this.ntpTimestamp);
        }
    }

    private void readFromParcel(Parcel in) {
        this.rtpTimestamp = in.readLong();
        this.ntpTimestamp = in.readLong();
    }

    public long getRtpTimestamp() {
        return this.rtpTimestamp;
    }

    public long getNtpTimestamp() {
        return this.ntpTimestamp;
    }

    public String toString() {
        return "TimeInfo RTP [" + this.rtpTimestamp + "] NTP [" + this.ntpTimestamp + "]";
    }
}
