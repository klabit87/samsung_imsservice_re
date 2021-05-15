package com.sec.internal.ims.servicemodules.euc.locale;

import java.util.Locale;

public interface IDeviceLocale {

    public interface ICallback {
        void onResult(Locale locale);
    }

    public interface IDeviceLocaleListener {
        void onLocaleChanged(Locale locale);
    }

    void getDeviceLocale(ICallback iCallback);

    String getLanguageCode(Locale locale);

    void start(IDeviceLocaleListener iDeviceLocaleListener) throws IllegalStateException;

    void stop() throws IllegalStateException;
}
