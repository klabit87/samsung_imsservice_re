package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class DeviceConfiguration {
    @SerializedName("configInfo")
    public ConfigInfo mConfigInfo;

    public class ConfigInfo {
        @SerializedName("version")
        public String mVersion;

        public ConfigInfo() {
        }
    }

    public class RAT {
        @SerializedName("$")
        public String timeout;
        @SerializedName("@type")
        public String type;

        public RAT() {
        }
    }
}
