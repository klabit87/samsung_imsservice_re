package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class RequestManageConnectivity extends NSDSRequest {
    public String csr;
    @SerializedName("device-group")
    public String deviceGroup;
    public int operation;
    @SerializedName("remote-device-id")
    public String remoteDeviceId;
    public String vimsi;
}
