package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.os.Handler;

public class TelephonyAdapterPrimaryDevice extends TelephonyAdapterPrimaryDeviceBase {
    public TelephonyAdapterPrimaryDevice(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerSmsReceiver();
        initState();
    }
}
