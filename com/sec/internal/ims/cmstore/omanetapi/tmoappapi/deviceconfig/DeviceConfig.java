package com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig;

import com.google.gson.annotations.SerializedName;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class DeviceConfig {
    @SerializedName("faxConfig")
    public FaxConfig mFaxConfig;
    @SerializedName("janskyConfig")
    public JanskyConfig mJanskyConfig;
    @SerializedName("RCSConfig")
    public RCSConfig mRCSConfig;

    public static class FaxConfig {
        @SerializedName("apiVersion")
        public String mApiVersion;
        @SerializedName("rootURL")
        public String mRootUrl;
        @SerializedName("serviceName")
        public String mServiceName;
    }

    public static class JanskyConfig {
        @SerializedName("WSG_URI")
        public String mWsgUri;
    }

    public static class RCSConfig {
        @SerializedName("wap-provisioningdoc")
        public WapProvisioningDoc mWapProvisioningDoc;

        public static class WapProvisioningDoc {
            @SerializedName("characteristic")
            public List<Characteristic> mCharacteristics;

            public static class Characteristic {
                @SerializedName("characteristic")
                public List<Characteristic> mCharacteristics;
                @SerializedName("parm")
                public List<Parm> mParms;
                @SerializedName("@type")
                public String mType;

                public static class Parm {
                    @SerializedName("@name")
                    public String mName;
                    @SerializedName("@value")
                    public String mValue;

                    public String toString() {
                        return "Parm{mName=" + this.mName + ", mValue=" + IMSLog.checker(this.mValue) + '}';
                    }
                }
            }
        }
    }
}
