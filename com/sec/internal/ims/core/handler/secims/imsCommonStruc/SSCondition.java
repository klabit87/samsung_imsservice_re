package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class SSCondition {
    public static final int CONDITION_ANONYMOUS = 4;
    public static final int CONDITION_BUSY = 0;
    public static final int CONDITION_COMMUNICATINO_DIVERTED = 13;
    public static final int CONDITION_EXTERNAL_LIST = 10;
    public static final int CONDITION_IDENTITY = 3;
    public static final int CONDITION_INTERNATIONAL = 15;
    public static final int CONDITION_INTERNATIONAL_EXHC = 16;
    public static final int CONDITION_MEDIA = 7;
    public static final int CONDITION_NOREPLYTIMER = 17;
    public static final int CONDITION_NOT_LOGGED_IN = 18;
    public static final int CONDITION_NOT_REACHABLE = 12;
    public static final int CONDITION_NOT_REGISTERED = 1;
    public static final int CONDITION_NO_ANSWER = 8;
    public static final int CONDITION_OTHER_IDENTITY = 11;
    public static final int CONDITION_PRESENCE_STATUS = 2;
    public static final int CONDITION_ROAMING = 14;
    public static final int CONDITION_RULE_DEATIVATED = 9;
    public static final int CONDITION_SPHERE = 5;
    public static final int CONDITION_VALIDITY = 6;
    public static final String[] names = {"CONDITION_BUSY", "CONDITION_NOT_REGISTERED", "CONDITION_PRESENCE_STATUS", "CONDITION_IDENTITY", "CONDITION_ANONYMOUS", "CONDITION_SPHERE", "CONDITION_VALIDITY", "CONDITION_MEDIA", "CONDITION_NO_ANSWER", "CONDITION_RULE_DEATIVATED", "CONDITION_EXTERNAL_LIST", "CONDITION_OTHER_IDENTITY", "CONDITION_NOT_REACHABLE", "CONDITION_COMMUNICATINO_DIVERTED", "CONDITION_ROAMING", "CONDITION_INTERNATIONAL", "CONDITION_INTERNATIONAL_EXHC", "CONDITION_NOREPLYTIMER", "CONDITION_NOT_LOGGED_IN"};

    private SSCondition() {
    }

    public static String name(int e) {
        return names[e];
    }
}
