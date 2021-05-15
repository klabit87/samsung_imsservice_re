package com.sec.internal.ims.servicemodules.euc.workflow;

public interface IModuleLifecycleListener {
    void discard(String str);

    void load(String str);

    void start();

    void stop();
}
