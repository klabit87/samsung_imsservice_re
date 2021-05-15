package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import com.sec.internal.constants.ims.entitilement.data.LocAndTcWebSheetData;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceDeactivation;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceImplicitActivation;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import java.util.List;
import java.util.Map;

public interface IMnoNsdsStrategy {
    long calEntitlementCheckExpRetryTime(int i);

    int getBaseOperationMaxRetry();

    String getDeviceGroup(int i);

    long getDeviceInfoTime();

    long getEntitlementCheckExpirationTime();

    IEntitlementCheck getEntitlementCheckImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper);

    int getEntitlementCheckMaxRetry();

    String getEntitlementServerUrl(String str, String str2);

    Map<String, Integer> getEntitlementServicesMap();

    String getGcmSenderId(String str, String str2);

    LocAndTcWebSheetData getLocAndTcWebSheetData(String str, String str2);

    String getNSDSApiVersion();

    String[] getNSDSServices();

    int getNextOperation(int i, int i2, int i3, Bundle bundle);

    List<Integer> getOperationsForBootupInit(String str);

    long getRetryInterval();

    List<String> getServiceListForPushToken();

    String getSimAuthenticationType();

    ISIMDeviceImplicitActivation getSimDeviceActivationImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper);

    ISIMDeviceDeactivation getSimDeviceDeactivationImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper);

    ISimSwapFlow getSimSwapFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper);

    String getUserAgent();

    long getWaitTimeForForcedSimSwap();

    void handleSimNotAvailable(String str, int i);

    boolean isDeviceProvisioned();

    boolean isGcmTokenRequired();

    boolean isNsdsServiceEnabled();

    boolean isNsdsServiceViaXcap();

    boolean isNsdsUIAppSwitchOn(String str);

    boolean isSIMDeviceActivationRequired();

    boolean isSimSupportedForNsds(ISimManager iSimManager);

    boolean needCheckEntitlementPollInterval();

    boolean needGetMSISDNForEntitlement();

    boolean requireRetryBootupProcedure();

    boolean shouldChangedUriTriggerNsdsService(Uri uri);

    boolean shouldIgnoreDeviceConfigValidity();

    boolean supportEntitlementCheck();
}
