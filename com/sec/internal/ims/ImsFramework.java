package com.sec.internal.ims;

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
import com.sec.internal.ims.registry.ImsRegistry;
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
import com.sec.internal.interfaces.ims.core.iil.IIilManager;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import java.util.List;

public class ImsFramework implements IImsFramework {
    private IImsFramework mImsFramework;

    public ImsFramework(IImsFramework imsFramework) {
        this.mImsFramework = imsFramework;
        ImsRegistry.init(this);
    }

    public IPdnController getPdnController() {
        return this.mImsFramework.getPdnController();
    }

    public ICmcAccountManager getCmcAccountManager() {
        return this.mImsFramework.getCmcAccountManager();
    }

    public IRcsPolicyManager getRcsPolicyManager() {
        return this.mImsFramework.getRcsPolicyManager();
    }

    public IRegistrationManager getRegistrationManager() {
        return this.mImsFramework.getRegistrationManager();
    }

    public IConfigModule getConfigModule() {
        return this.mImsFramework.getConfigModule();
    }

    public IGbaServiceModule getGbaService() {
        return this.mImsFramework.getGbaService();
    }

    public IHandlerFactory getHandlerFactory() {
        return this.mImsFramework.getHandlerFactory();
    }

    public IGoogleImsService getGoogleImsAdaptor() {
        return this.mImsFramework.getGoogleImsAdaptor();
    }

    public IImsNotifier getImsNotifier() {
        return this.mImsFramework.getImsNotifier();
    }

    public IAECModule getAECModule() {
        return this.mImsFramework.getAECModule();
    }

    public ICmcConnectivityController getP2pCC() {
        return this.mImsFramework.getP2pCC();
    }

    public IGeolocationController getGeolocationController() {
        return this.mImsFramework.getGeolocationController();
    }

    public INtpTimeController getNtpTimeController() {
        return this.mImsFramework.getNtpTimeController();
    }

    public IImsDiagMonitor getImsDiagMonitor() {
        return this.mImsFramework.getImsDiagMonitor();
    }

    public IFcmHandler getFcmHandler() {
        return this.mImsFramework.getFcmHandler();
    }

    public IIilManager getIilManager(int phoneId) {
        return this.mImsFramework.getIilManager(phoneId);
    }

    public List<ServiceModuleBase> getAllServiceModules() {
        return this.mImsFramework.getAllServiceModules();
    }

    public IServiceModuleManager getServiceModuleManager() {
        return this.mImsFramework.getServiceModuleManager();
    }

    public Context getContext() {
        return this.mImsFramework.getContext();
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener, boolean broadcast, int phoneId) {
        this.mImsFramework.registerImsRegistrationListener(listener, broadcast, phoneId);
    }

    public String getString(int phoneId, String projection, String defVal) {
        return this.mImsFramework.getString(phoneId, projection, defVal);
    }

    public String[] getStringArray(int phoneId, String projection, String[] defVal) {
        return this.mImsFramework.getStringArray(phoneId, projection, (String[]) null);
    }

    public int getInt(int phoneId, String projection, int defVal) {
        return this.mImsFramework.getInt(phoneId, projection, defVal);
    }

    public boolean getBoolean(int phoneId, String projection, boolean defVal) {
        return this.mImsFramework.getBoolean(phoneId, projection, defVal);
    }

    public ContentValues getConfigValues(String[] fields, int phoneId) {
        return this.mImsFramework.getConfigValues(fields, phoneId);
    }

    public boolean isServiceAvailable(String service, int rat, int phoneId) throws RemoteException {
        return this.mImsFramework.isServiceAvailable(service, rat, phoneId);
    }

    public void setRttMode(int phoneId, int mode) {
        this.mImsFramework.setRttMode(phoneId, mode);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        this.mImsFramework.registerImsRegistrationListener(listener);
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        this.mImsFramework.unregisterImsRegistrationListener(listener);
    }

    public ImsRegistration[] getRegistrationInfoByPhoneId(int phoneId) throws RemoteException {
        return this.mImsFramework.getRegistrationInfoByPhoneId(phoneId);
    }

    public int getNetworkType(int handle) {
        return this.mImsFramework.getNetworkType(handle);
    }

    public boolean isRcsEnabledByPhoneId(int phoneId) {
        return this.mImsFramework.isRcsEnabledByPhoneId(phoneId);
    }

    public void startAutoConfig(boolean force, Message onComplete) {
        this.mImsFramework.startAutoConfig(force, onComplete);
    }

    public Binder getBinder(String service) {
        return this.mImsFramework.getBinder(service);
    }

    public Binder getBinder(String service, String aux) {
        return this.mImsFramework.getBinder(service, aux);
    }

    public String getRcsProfileType(int phoneId) throws RemoteException {
        return this.mImsFramework.getRcsProfileType(phoneId);
    }

    public void enableRcsByPhoneId(boolean enable, int phoneId) throws RemoteException {
        this.mImsFramework.enableRcsByPhoneId(enable, phoneId);
    }

    public boolean isServiceEnabledByPhoneId(String service, int phoneId) throws RemoteException {
        return this.mImsFramework.isServiceEnabledByPhoneId(service, phoneId);
    }

    public void triggerAutoConfigurationForApp(int phoneId) throws RemoteException {
        this.mImsFramework.triggerAutoConfigurationForApp(phoneId);
    }

    public boolean isDefaultDmValue(String dm, int phoneId) {
        return this.mImsFramework.isDefaultDmValue(dm, phoneId);
    }

    public boolean setDefaultDmValue(String dm, int phoneId) {
        return this.mImsFramework.setDefaultDmValue(dm, phoneId);
    }

    public int[] getCallCount(int phoneId) throws RemoteException {
        return this.mImsFramework.getCallCount(phoneId);
    }

    public void notifyImsReady(boolean readiness, int phoneId) {
        this.mImsFramework.notifyImsReady(readiness, phoneId);
    }

    public void sendDeregister(int cause, int phoneId) {
        this.mImsFramework.sendDeregister(cause, phoneId);
    }

    public void suspendRegister(boolean suspend, int phoneId) {
        this.mImsFramework.suspendRegister(suspend, phoneId);
    }

    public void setIsimLoaded() {
        this.mImsFramework.setIsimLoaded();
    }
}
