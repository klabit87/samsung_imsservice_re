package com.sec.internal.interfaces.ims;

import android.content.ContentValues;
import android.content.Context;
import android.os.Binder;
import android.os.Message;
import android.os.RemoteException;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.internal.google.cmc.ICmcConnectivityController;
import com.sec.internal.ims.entitlement.fcm.interfaces.IFcmHandler;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.interfaces.google.IGoogleImsService;
import com.sec.internal.interfaces.google.IImsNotifier;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.INtpTimeController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IHandlerFactory;
import com.sec.internal.interfaces.ims.core.iil.IIilManager;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import java.util.List;

public interface IImsFramework {
    void enableRcsByPhoneId(boolean z, int i) throws RemoteException;

    IAECModule getAECModule();

    List<ServiceModuleBase> getAllServiceModules();

    Binder getBinder(String str);

    Binder getBinder(String str, String str2);

    boolean getBoolean(int i, String str, boolean z);

    int[] getCallCount(int i) throws RemoteException;

    ICmcAccountManager getCmcAccountManager();

    IConfigModule getConfigModule();

    ContentValues getConfigValues(String[] strArr, int i);

    Context getContext();

    IFcmHandler getFcmHandler();

    IGbaServiceModule getGbaService();

    IGeolocationController getGeolocationController();

    IGoogleImsService getGoogleImsAdaptor();

    IHandlerFactory getHandlerFactory();

    IIilManager getIilManager(int i);

    IImsDiagMonitor getImsDiagMonitor();

    IImsNotifier getImsNotifier();

    int getInt(int i, String str, int i2);

    int getNetworkType(int i);

    INtpTimeController getNtpTimeController();

    ICmcConnectivityController getP2pCC();

    IPdnController getPdnController();

    IRcsPolicyManager getRcsPolicyManager();

    String getRcsProfileType(int i) throws RemoteException;

    ImsRegistration[] getRegistrationInfoByPhoneId(int i) throws RemoteException;

    IRegistrationManager getRegistrationManager();

    IServiceModuleManager getServiceModuleManager();

    String getString(int i, String str, String str2);

    String[] getStringArray(int i, String str, String[] strArr);

    boolean isDefaultDmValue(String str, int i);

    boolean isRcsEnabledByPhoneId(int i);

    boolean isServiceAvailable(String str, int i, int i2) throws RemoteException;

    boolean isServiceEnabledByPhoneId(String str, int i) throws RemoteException;

    void notifyImsReady(boolean z, int i);

    String registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener, boolean z, int i);

    void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    void sendDeregister(int i, int i2);

    boolean setDefaultDmValue(String str, int i);

    void setIsimLoaded();

    void setRttMode(int i, int i2);

    void startAutoConfig(boolean z, Message message);

    void suspendRegister(boolean z, int i);

    void triggerAutoConfigurationForApp(int i) throws RemoteException;

    void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException;
}
