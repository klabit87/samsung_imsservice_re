package com.sec.internal.ims.cmstore.params;

import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import com.sec.internal.log.IMSLog;

public class ParamVvmUpdate {
    @SerializedName("duration")
    public int mDuration;
    @SerializedName("email1")
    public String mEmail1;
    @SerializedName("email2")
    public String mEmail2;
    @SerializedName("greeting_type")
    public String mGreetingType;
    @SerializedName("filePath")
    public String mGreetingUri;
    @SerializedName("id")
    public int mId;
    @SerializedName("preferred_line")
    public String mLine;
    @SerializedName("mimeType")
    public String mMimeType;
    @SerializedName("new")
    public String mNewPwd;
    @SerializedName("old")
    public String mOldPwd;
    @SerializedName("type")
    public String mType;
    public VvmTypeChange mVvmChange;
    @SerializedName("fileName")
    public String mfileName;

    public enum VvmTypeChange {
        ACTIVATE(0),
        DEACTIVATE(1),
        VOICEMAILTOTEXT(2),
        GREETING(3),
        PIN(4),
        FULLPROFILE(5);
        
        private final int mId;

        private VvmTypeChange(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum GreetingOnFlag {
        GreetingOff(0),
        GreetingOn(1);
        
        private final int mId;

        private GreetingOnFlag(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum VvmGreetingType {
        Default(0),
        Name(1),
        Custom(2),
        Busy(3),
        ExtendAbsence(4),
        Fun(5);
        
        private final int mId;

        private VvmGreetingType(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }

        public static VvmGreetingType valueOf(int id) {
            for (VvmGreetingType r : values()) {
                if (r.mId == id) {
                    return r;
                }
            }
            return null;
        }
    }

    public String toString() {
        String oldpwd = null;
        String newpwd = null;
        if (!TextUtils.isEmpty(this.mOldPwd)) {
            oldpwd = "xxxx";
        }
        if (!TextUtils.isEmpty(this.mNewPwd)) {
            newpwd = "****";
        }
        return "ParamVvmUpdate [mVvmChange= " + this.mVvmChange + " mGreetingUri = " + IMSLog.checker(this.mGreetingUri) + " mLine = " + IMSLog.checker(this.mLine) + " mOldPwd = " + oldpwd + " mNewPwd = " + newpwd + " mEmail1 = " + IMSLog.checker(this.mEmail1) + " mEmail2 = " + IMSLog.checker(this.mEmail2) + " mDuration = " + this.mDuration + " mType = " + this.mType + " mId = " + this.mId + " mMimeType = " + this.mMimeType + " mfileName = " + this.mfileName + " mGreetingType = " + this.mGreetingType + "]";
    }
}
