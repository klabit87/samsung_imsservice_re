package com.sec.internal.interfaces.ims.servicemodules.base;

import com.sec.ims.ImsRegistration;

public interface IServiceModule {
    String getName();

    String[] getServicesRequiring();

    long getSupportFeature(int i);

    boolean isReady();

    boolean isRunning();

    void onDeregistered(ImsRegistration imsRegistration, int i);

    void onRegistered(ImsRegistration imsRegistration);

    void start();

    void updateCapabilities(int i);
}
