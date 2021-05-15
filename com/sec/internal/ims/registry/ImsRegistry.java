package com.sec.internal.ims.registry;

import android.content.ContentValues;
import android.content.Context;
import android.os.Binder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.internal.google.cmc.ICmcConnectivityController;
import com.sec.internal.ims.entitlement.fcm.interfaces.IFcmHandler;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.interfaces.google.IGoogleImsService;
import com.sec.internal.interfaces.google.IImsNotifier;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.INtpTimeController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IHandlerFactory;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import java.util.List;

public class ImsRegistry {
    private static final String LOG_TAG = ImsRegistry.class.getSimpleName();
    private static boolean mIsReady = false;
    private static IImsFramework sImsFrameworkInstance;

    private ImsRegistry() {
    }

    public static void init(IImsFramework imsFramework) {
        sImsFrameworkInstance = imsFramework;
        mIsReady = true;
    }

    private static IImsFramework getImsFramwork() {
        return sImsFrameworkInstance;
    }

    public static boolean isReady() {
        return mIsReady;
    }

    public static IPdnController getPdnController() {
        return getImsFramwork().getPdnController();
    }

    public static ICmcAccountManager getCmcAccountManager() {
        return getImsFramwork().getCmcAccountManager();
    }

    public static IRcsPolicyManager getRcsPolicyManager() {
        return getImsFramwork().getRcsPolicyManager();
    }

    public static IRegistrationManager getRegistrationManager() {
        return getImsFramwork().getRegistrationManager();
    }

    public static IConfigModule getConfigModule() {
        return getImsFramwork().getConfigModule();
    }

    public static IGbaServiceModule getGbaService() {
        return getImsFramwork().getGbaService();
    }

    public static IHandlerFactory getHandlerFactory() {
        return getImsFramwork().getHandlerFactory();
    }

    public static IGoogleImsService getGoogleImsAdaptor() {
        return getImsFramwork().getGoogleImsAdaptor();
    }

    public static IImsNotifier getImsNotifier() {
        return getImsFramwork().getImsNotifier();
    }

    public static IAECModule getAECModule() {
        return getImsFramwork().getAECModule();
    }

    public static ICmcConnectivityController getP2pCC() {
        return getImsFramwork().getP2pCC();
    }

    public static IGeolocationController getGeolocationController() {
        return getImsFramwork().getGeolocationController();
    }

    public static INtpTimeController getNtpTimeController() {
        return getImsFramwork().getNtpTimeController();
    }

    public static IImsDiagMonitor getImsDiagMonitor() {
        return getImsFramwork().getImsDiagMonitor();
    }

    public static IFcmHandler getFcmHandler() {
        return getImsFramwork().getFcmHandler();
    }

    public static List<ServiceModuleBase> getAllServiceModules() {
        return getImsFramwork().getAllServiceModules();
    }

    public static IServiceModuleManager getServiceModuleManager() {
        return getImsFramwork().getServiceModuleManager();
    }

    public static Context getContext() {
        return getImsFramwork().getContext();
    }

    public static void registerImsRegistrationListener(IImsRegistrationListener listener, boolean broadcast, int phoneId) throws RemoteException {
        getImsFramwork().registerImsRegistrationListener(listener, broadcast, phoneId);
    }

    public static int getInt(int phoneId, String projection, int defVal) {
        return getImsFramwork().getInt(phoneId, projection, defVal);
    }

    public static boolean getBoolean(int phoneId, String projection, boolean defVal) {
        return getImsFramwork().getBoolean(phoneId, projection, defVal);
    }

    public static String getString(int phoneId, String projection, String defVal) {
        return getImsFramwork().getString(phoneId, projection, defVal);
    }

    public static String[] getStringArray(int phoneId, String projection, String[] defVal) {
        return getImsFramwork().getStringArray(phoneId, projection, defVal);
    }

    public static ContentValues getConfigValues(String[] fields, int phoneId) {
        return getImsFramwork().getConfigValues(fields, phoneId);
    }

    public static boolean isServiceAvailable(String service, int rat, int phoneId) throws RemoteException {
        return getImsFramwork().isServiceAvailable(service, rat, phoneId);
    }

    public static void setRttMode(int phoneId, int mode) {
        getImsFramwork().setRttMode(phoneId, mode);
    }

    public static void registerImsRegistrationListener(IImsRegistrationListener listener) {
        try {
            getImsFramwork().registerImsRegistrationListener(listener);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "registerImsRegistrationListener RemoteException do nothing : " + e.getMessage());
        }
    }

    public static void unregisterImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        getImsFramwork().unregisterImsRegistrationListener(listener);
    }

    public static ImsRegistration[] getRegistrationInfoByPhoneId(int phoneId) throws RemoteException {
        return getImsFramwork().getRegistrationInfoByPhoneId(phoneId);
    }

    public static int getNetworkType(int handle) throws RemoteException {
        return getImsFramwork().getNetworkType(handle);
    }

    public static boolean isRcsEnabledByPhoneId(int phoneId) {
        return getImsFramwork().isRcsEnabledByPhoneId(phoneId);
    }

    public static void startAutoConfig(boolean force, Message onComplete) {
        getImsFramwork().startAutoConfig(force, onComplete);
    }

    public static Binder getBinder(String service) {
        return getImsFramwork().getBinder(service);
    }

    public static Binder getBinder(String service, String aux) {
        return getImsFramwork().getBinder(service, aux);
    }

    public static String getRcsProfileType(int phoneId) {
        try {
            return getImsFramwork().getRcsProfileType(phoneId);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "getRcsProfileType RemoteException return empty String : " + e.getMessage());
            return "";
        }
    }

    public static void enableRcsByPhoneId(boolean enable, int phoneId) {
        try {
            getImsFramwork().enableRcsByPhoneId(enable, phoneId);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "enableRcsByPhoneId RemoteException: " + e.getMessage());
        }
    }

    public static boolean isServiceEnabledByPhoneId(String service, int phoneId) {
        try {
            return getImsFramwork().isServiceEnabledByPhoneId(service, phoneId);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "isServiceEnabledByPhoneId RemoteException: " + e.getMessage());
            return false;
        }
    }

    public static void triggerAutoConfigurationForApp(int phoneId) {
        try {
            getImsFramwork().triggerAutoConfigurationForApp(phoneId);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "triggerAutoConfigurationForApp RemoteException: " + e.getMessage());
        }
    }

    public static boolean isDefaultDmValue(String dm, int phoneId) {
        return getImsFramwork().isDefaultDmValue(dm, phoneId);
    }

    public static boolean setDefaultDmValue(String dm, int phoneId) {
        return getImsFramwork().setDefaultDmValue(dm, phoneId);
    }

    public static int[] getCallCount(int phoneId) {
        try {
            return getImsFramwork().getCallCount(phoneId);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "getCallCount RemoteException: " + e.getMessage());
            return new int[0];
        }
    }
}
