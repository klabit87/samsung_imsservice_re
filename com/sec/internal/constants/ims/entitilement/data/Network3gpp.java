package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Network3gpp {
    @SerializedName("cell-identifiers")
    public ArrayList<CellIdentifier> cellIdentifiers;
    @SerializedName("enable-data-roaming")
    public Boolean enableDataRoaming;
    public Integer plmn;
    @SerializedName("rat-bands")
    public ArrayList<RatBand> ratBands;
    @SerializedName("rat-type")
    public Integer ratType;
}
