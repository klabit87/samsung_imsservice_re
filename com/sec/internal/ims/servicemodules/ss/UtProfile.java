package com.sec.internal.ims.servicemodules.ss;

public class UtProfile {
    public static final int ACTION_ACTIVATION = 1;
    public static final int ACTION_DEACTIVATION = 0;
    public static final int ACTION_ERASURE = 4;
    public static final int ACTION_REGISTRATION = 3;
    public static final int ACTION_SHOW = 2;
    public static final int CB_BAIC = 1;
    public static final int CB_BAOC = 2;
    public static final int CB_BA_ALL = 7;
    public static final int CB_BA_MO = 8;
    public static final int CB_BA_MT = 9;
    public static final int CB_BIC_ACR = 6;
    public static final int CB_BIC_WR = 5;
    public static final int CB_BOIC = 3;
    public static final int CB_BOIC_EXHC = 4;
    public static final int CB_BS_MT = 10;
    public static final int CDIV_CF_ALL = 4;
    public static final int CDIV_CF_ALL_CONDITIONAL = 5;
    public static final int CDIV_CF_BUSY = 1;
    public static final int CDIV_CF_NOT_LOGGED_IN = 6;
    public static final int CDIV_CF_NOT_REACHABLE = 3;
    public static final int CDIV_CF_NO_REPLY = 2;
    public static final int CDIV_CF_UNCONDITIONAL = 0;
    public static final int CDIV_CF_UNKNOWN = -1;
    public static final int CLIP_DISABLE = 0;
    public static final int CLIP_ENABLE = 1;
    public static final int CLIP_MODE_NOT_PROVISIONED = 0;
    public static final int CLIP_MODE_PROVISIONED = 1;
    public static final int CLIP_MODE_UNKNOWN = 2;
    public static final int CLIR_DEFAULT = 0;
    public static final int CLIR_INVOCATION = 1;
    public static final int CLIR_MODE_NOT_PROVISIONED = 0;
    public static final int CLIR_MODE_PROVISIONED_PERMANENT = 1;
    public static final int CLIR_MODE_TEMPORARY_ALLOW = 4;
    public static final int CLIR_MODE_TEMPORARY_RESTRICTED = 3;
    public static final int CLIR_MODE_UNKNOWN = 2;
    public static final int CLIR_SUPPRESSION = 2;
    public static final int SS_BARR_TYPE_BAIC_bit = 8;
    public static final int SS_BARR_TYPE_BAOC_bit = 1;
    public static final int SS_BARR_TYPE_BIC_ROAM_bit = 10;
    public static final int SS_BARR_TYPE_BOIC_NOT_HC_bit = 4;
    public static final int SS_BARR_TYPE_BOIC_bit = 2;
    public static final int SS_GET_ACB = 118;
    public static final int SS_GET_CF = 100;
    public static final int SS_GET_CLIP = 106;
    public static final int SS_GET_CLIR = 108;
    public static final int SS_GET_COLP = 110;
    public static final int SS_GET_COLR = 112;
    public static final int SS_GET_CW = 114;
    public static final int SS_GET_ICB = 102;
    public static final int SS_GET_OCB = 104;
    public static final int SS_GET_SD = 116;
    public static final int SS_PUT_ACB = 119;
    public static final int SS_PUT_CF = 101;
    public static final int SS_PUT_CLIP = 107;
    public static final int SS_PUT_CLIR = 109;
    public static final int SS_PUT_COLP = 111;
    public static final int SS_PUT_COLR = 113;
    public static final int SS_PUT_CW = 115;
    public static final int SS_PUT_ICB = 103;
    public static final int SS_PUT_OCB = 105;
    int action;
    int condition;
    boolean enable;
    String number;
    String password;
    int requestId;
    int serviceClass;
    int timeSeconds;
    int type;
    String[] valueList;

    public UtProfile(int type2, int requestId2) {
        this.type = type2;
        this.requestId = requestId2;
    }

    public UtProfile(int type2, int condition2, String number2, int requestId2) {
        this.type = type2;
        this.condition = condition2;
        this.number = number2;
        this.requestId = requestId2;
        this.serviceClass = 255;
    }

    public UtProfile(int type2, int action2, int condition2, String number2, int serviceClass2, int timeSeconds2, int requestId2) {
        this.type = type2;
        this.action = action2;
        this.condition = condition2;
        this.number = number2;
        this.serviceClass = serviceClass2;
        this.timeSeconds = timeSeconds2;
        this.requestId = requestId2;
    }

    public UtProfile(int type2, boolean enable2, int serviceClass2, int requestId2) {
        this.type = type2;
        this.enable = enable2;
        this.serviceClass = serviceClass2;
        this.requestId = requestId2;
    }

    public UtProfile(int type2, int condition2, int action2, int serviceClass2, String[] barrList, int requestId2, String password2) {
        this.type = type2;
        this.condition = condition2;
        this.action = action2;
        this.serviceClass = serviceClass2;
        this.valueList = barrList;
        this.requestId = requestId2;
        this.password = password2;
    }

    public UtProfile(int type2, boolean enable2, int requestId2) {
        this.type = type2;
        this.enable = enable2;
        this.requestId = requestId2;
    }

    public UtProfile(int type2, int condition2, int requestId2) {
        this.type = type2;
        this.condition = condition2;
        this.requestId = requestId2;
        this.serviceClass = 255;
    }
}
