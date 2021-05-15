package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Location {
    @SerializedName("3gpp-networks")
    public ArrayList<Network3gpp> Networks3gpp;
    public String country;
    public Boolean indoor;
    public Double latitude;
    public Double longitude;
    public String metadata;
    public Integer radius;
}
