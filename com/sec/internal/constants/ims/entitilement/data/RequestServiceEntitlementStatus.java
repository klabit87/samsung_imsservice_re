package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class RequestServiceEntitlementStatus extends NSDSRequest {
    @SerializedName("service-list")
    public ArrayList<String> serviceList;
}
