package com.sec.internal.ims.cmstore;

public interface CloudMessageIntent {
    public static final String CATEGORY_ACTION = "com.samsung.rcs.framework.cloudmessage.category.ACTION";

    public static class Action {
        public static final String CALLLOGDATADELETEFAILURE = "com.samsung.rcs.framework.cloudmessage.action.CALLLOGDATADELETEFAILURE";
        public static final String CALLLOGINTENT = "com.samsung.rcs.framework.cloudmessage.action.CALLLOGDATA";
        public static final String MSGDELETEFAILURE = "com.samsung.rcs.framework.cloudmessage.action.MSGDELETEFAILURE";
        public static final String MSGINTENT = "com.samsung.rcs.framework.cloudmessage.action.MSGDATA";
        public static final String MSGINTENT_INITSYNCEND = "com.samsung.rcs.framework.cloudmessage.action.MSGDATA.INITIALSYNCEND";
        public static final String MSGINTENT_INITSYNCFAIL = "com.samsung.rcs.framework.cloudmessage.action.MSGDATA.INITIALSYNCFAIL";
        public static final String MSGINTENT_INITSYNSTART = "com.samsung.rcs.framework.cloudmessage.action.MSGDATA.INITIALSYNCSTART";
        public static final String MSGUIINTENT = "com.samsung.rcs.framework.cloudmessage.action.MSGUI";
        public static final String VVMDATADELETEFAILURE = "com.samsung.rcs.framework.cloudmessage.action.VVMDATADELETEFAILURE";
        public static final String VVMINTENT = "com.samsung.rcs.framework.cloudmessage.action.VVMDATA";
    }

    public static class Extras {
        public static final String LINENUM = "linenum";
        public static final String MSGTYPE = "msgtype";
        public static final String ROWIDS = "rowids";
    }

    public static class ExtrasAMBSUI {
        public static final String PARAM = "param";
        public static final String SCREENNAME = "screenname";
        public static final String STYLE = "style";
    }

    public static class Permission {
        public static final String MSGAPP = "com.samsung.app.cmstore.MSGDATA_PERMISSION";
    }
}
