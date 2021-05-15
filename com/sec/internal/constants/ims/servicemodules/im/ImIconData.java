package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class ImIconData {
    private String mIconLocation;
    private final IconType mIconType;
    private String mIconUri;
    private final ImsUri mParticipant;
    private final Date mTimestamp;

    public enum IconType {
        ICON_TYPE_NONE,
        ICON_TYPE_FILE,
        ICON_TYPE_URI
    }

    public ImIconData(IconType iconType, ImsUri participant, Date timestamp, String iconLocation, String iconUri) {
        this.mIconType = iconType;
        this.mParticipant = participant;
        this.mTimestamp = timestamp;
        this.mIconLocation = iconLocation;
        this.mIconUri = iconUri;
    }

    public IconType getIconType() {
        return this.mIconType;
    }

    public String getIconLocation() {
        return this.mIconLocation;
    }

    public void setIconLocation(String iconLocation) {
        this.mIconLocation = iconLocation;
    }

    public String getIconUri() {
        return this.mIconUri;
    }

    public ImsUri getParticipant() {
        return this.mParticipant;
    }

    public Date getTimestamp() {
        return this.mTimestamp;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImIconData other = (ImIconData) obj;
        if (!this.mIconLocation.equals(other.mIconLocation) || !this.mParticipant.equals(other.mParticipant) || !this.mTimestamp.equals(other.mTimestamp)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "ImIconData [mIconType=" + this.mIconType + ", mParticipant=" + IMSLog.checker(this.mParticipant) + ", mTimestamp=" + this.mTimestamp + ", mIconLocation=" + this.mIconLocation + ", mIconUri=" + this.mIconUri + ']';
    }
}
