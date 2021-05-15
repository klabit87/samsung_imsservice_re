package com.sec.internal.ims.entitlement.nsds;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSModule;
import com.sec.internal.interfaces.ims.core.ISimManager;

public class NSDSModuleFactory {
    private static final String LOG_TAG = NSDSModuleFactory.class.getSimpleName();
    private static final Mno[] mnos = {Mno.ATT, Mno.TMOUS, Mno.TELEFONICA_UK, Mno.TELEFONICA_UK_LAB, Mno.GCI};
    private static NSDSModuleFactory sInstance = null;
    private Context mContext;
    private Looper mServiceLooper;

    private NSDSModuleFactory(Looper serviceLooper, Context context) {
        this.mServiceLooper = serviceLooper;
        this.mContext = context;
    }

    public static synchronized void createInstance(Looper serviceLooper, Context context) {
        synchronized (NSDSModuleFactory.class) {
            if (sInstance == null) {
                sInstance = new NSDSModuleFactory(serviceLooper, context);
            }
        }
    }

    public static NSDSModuleFactory getInstance() {
        return sInstance;
    }

    public NSDSModuleBase getNsdsModule(ISimManager simManager) {
        if (simManager == null) {
            Log.e(LOG_TAG, "getNsdsModule: simManager null");
            return null;
        }
        Mno simmno = simManager.getSimMno();
        for (Mno mno : mnos) {
            if (mno == simmno) {
                Log.i(LOG_TAG, "getNsdsModule: Mno = " + simmno);
                return new NSDSModule(this.mServiceLooper, this.mContext, simManager);
            }
        }
        return null;
    }
}
