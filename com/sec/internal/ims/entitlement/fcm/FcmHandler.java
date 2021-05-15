package com.sec.internal.ims.entitlement.fcm;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.sec.internal.ims.entitlement.fcm.interfaces.IFcmEventListener;
import com.sec.internal.ims.entitlement.fcm.interfaces.IFcmHandler;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FcmHandler implements IFcmHandler {
    public static final String API_KEY = "AIzaSyC9rGRRr3J16mn510MIjZx0DbCEbwesCbM";
    public static final String FIREBASE_URL = "https://fir-e287d.firebaseio.com";
    private static final String LOG_TAG = FcmHandler.class.getSimpleName();
    public static final String MOBILESDK_APP_ID = "1:907837128383:android:63ec13a18eb17af2";
    public static final String PROJECT_ID = "fir-e287d";
    public static final String PROJECT_NUMBER = "907837128383";
    public static final String STORAGE_BUCKET = "fir-e287d.appspot.com";
    private List<IFcmEventListener> mFcmEventListeners = new ArrayList();

    public FcmHandler(Context context) {
        try {
            FirebaseApp.initializeApp(context, new FirebaseOptions.Builder().setApplicationId(MOBILESDK_APP_ID).setApiKey(API_KEY).setDatabaseUrl(FIREBASE_URL).setGcmSenderId(PROJECT_NUMBER).setProjectId(PROJECT_ID).setStorageBucket(STORAGE_BUCKET).build());
            IMSLog.i(LOG_TAG, "FirebaseApp initialization successful");
        } catch (Exception e) {
            String str = LOG_TAG;
            IMSLog.e(str, "FirebaseApp initialization unsuccessful: " + e.getMessage());
        }
    }

    public void onMessageReceived(Context context, String from, Map data) {
        for (IFcmEventListener listener : this.mFcmEventListeners) {
            listener.onMessageReceived(context, from, data);
        }
    }

    public void registerFcmEventListener(IFcmEventListener fcmEventListener) {
        this.mFcmEventListeners.add(fcmEventListener);
    }

    public void unRegisterFcmEventListener(IFcmEventListener fcmEventListener) {
        this.mFcmEventListeners.remove(fcmEventListener);
    }
}
