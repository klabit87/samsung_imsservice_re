package com.sec.internal.constants.ims.core;

public class SimConstants {
    public static final String DSDA_DI = "DSDA_DI";
    public static final String DSDS_DI = "DSDS_DI";
    public static final String DSDS_SI_DDS = "DSDS_SI_DDS";
    public static final String SINGLE = "SINGLE";

    public enum SIM_STATE {
        UNKNOWN(0),
        ABSENT(1),
        LOCKED(2),
        INVALID_ISIM(3),
        LOADED(200);
        
        private int mState;

        private SIM_STATE(int state) {
            this.mState = 0;
            this.mState = state;
        }

        public boolean isOneOf(SIM_STATE... states) {
            for (SIM_STATE state : states) {
                if (this == state) {
                    return true;
                }
            }
            return false;
        }
    }
}
