package com.sec.internal.ims.servicemodules.im.interfaces;

import android.content.Context;

public interface IModuleInterface {
    Context getContext();

    boolean isWifiConnected();
}
