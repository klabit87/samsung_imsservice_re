package com.sec.internal.ims.servicemodules.tapi.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;

public class PackageEventReceiver extends BroadcastReceiver {
    public void onReceive(Context arg0, Intent arg1) {
        if (arg1.getDataString() != null) {
            String packageName = arg1.getDataString().replaceFirst("package:", "");
            String action = arg1.getAction();
            ISessionModule sm = ImsRegistry.getServiceModuleManager().getSessionModule();
            if (sm != null) {
                if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                    if (sm.needRegister(packageName)) {
                        sm.registerApp();
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    if (sm.needDeRegister(packageName)) {
                        sm.deRegisterApp();
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REPLACED") && sm.needRegister(packageName)) {
                    sm.registerApp();
                }
            }
        }
    }
}
