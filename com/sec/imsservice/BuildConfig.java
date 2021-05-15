package com.sec.imsservice;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;

public final class BuildConfig {
    public static final String APPLICATION_ID = "com.sec.imsservice";
    public static final String ARTIFACT_ID = "1.1.0.72";
    public static final String BUILD_TYPE = "debug";
    public static final boolean DEBUG = Boolean.parseBoolean(CloudMessageProviderContract.JsonData.TRUE);
    public static final String FLAVOR = "";
    public static final boolean PRELOADED = true;
    public static final int VERSION_CODE = 1;
    public static final String VERSION_NAME = "1.0";
}
