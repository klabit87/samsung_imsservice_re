package com.sec.internal.constants.ims.servicemodules.volte2;

public class CallConstants {

    public static class ComposerData {
        public static final String IMAGE = "image";
        public static final String IMPORTANCE = "importance";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String RADIUS = "radius";
        public static final String SUBJECT = "subject";
        public static final String TAG = "composer_data";
    }

    public enum STATE {
        Idle,
        ReadyToCall,
        IncomingCall,
        OutGoingCall,
        AlertingCall,
        InCall,
        HoldingCall,
        HeldCall,
        ResumingCall,
        ModifyingCall,
        ModifyRequested,
        HoldingVideo,
        VideoHeld,
        ResumingVideo,
        EndingCall,
        EndedCall
    }
}
