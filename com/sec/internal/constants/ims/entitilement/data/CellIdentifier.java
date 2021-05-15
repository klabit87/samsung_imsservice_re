package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class CellIdentifier {
    @SerializedName("eutran-cid")
    public Integer eutranCid;
    @SerializedName("geran-cid")
    public Integer geranCid;
    public Integer lac;
    public Integer tac;
    @SerializedName("utran-cid")
    public Integer utranCid;
}
