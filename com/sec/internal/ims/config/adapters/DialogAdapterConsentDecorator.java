package com.sec.internal.ims.config.adapters;

import android.os.Handler;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.ims.servicemodules.euc.EucModule;
import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.interfaces.ims.config.IDialogAdapter;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;

public class DialogAdapterConsentDecorator implements IDialogAdapter {
    private static final String LOG_TAG = DialogAdapterConsentDecorator.class.getSimpleName();
    private final IDialogAdapter mDialogAdapter;
    private final int mPhoneId;

    public DialogAdapterConsentDecorator(IDialogAdapter dialogAdapter, int phoneId) {
        this.mDialogAdapter = (IDialogAdapter) Preconditions.checkNotNull(dialogAdapter);
        this.mPhoneId = phoneId;
    }

    public boolean getAcceptReject(String title, String message, String accept, String reject) {
        boolean userAccept = this.mDialogAdapter.getAcceptReject(title, message, accept, reject);
        String ownIdentity = getOwnIdentity();
        if (ownIdentity != null) {
            ModuleChannel moduleChannel = ModuleChannel.createChannel(EucModule.class.getSimpleName(), (Handler) null);
            AutoconfUserConsentData autoconfUserConsentData = r2;
            AutoconfUserConsentData autoconfUserConsentData2 = new AutoconfUserConsentData(System.currentTimeMillis(), userAccept, title, message, ownIdentity);
            moduleChannel.sendEvent(7, autoconfUserConsentData, (ModuleChannel.Listener) null);
        } else {
            IMSLog.i(LOG_TAG, "Could not obtain own identity! Ignoring user consent for EULA!");
        }
        return userAccept;
    }

    public boolean getAcceptReject(String title, String message, String accept, String reject, int phoneId) {
        boolean userAccept = this.mDialogAdapter.getAcceptReject(title, message, accept, reject, phoneId);
        String ownIdentity = getOwnIdentity();
        if (ownIdentity != null) {
            ModuleChannel.createChannel(EucModule.class.getSimpleName(), (Handler) null).sendEvent(7, new AutoconfUserConsentData(System.currentTimeMillis(), userAccept, title, message, ownIdentity), (ModuleChannel.Listener) null);
        } else {
            IMSLog.i(LOG_TAG, "Could not obtain own identity! Ignoring user consent for EULA!");
        }
        return userAccept;
    }

    public String getMsisdn(String countryCode) {
        return this.mDialogAdapter.getMsisdn(countryCode);
    }

    public String getMsisdn(String countryCode, String oldMsisdn) {
        return this.mDialogAdapter.getMsisdn(countryCode, oldMsisdn);
    }

    public boolean getNextCancel() {
        return this.mDialogAdapter.getNextCancel();
    }

    private String getOwnIdentity() {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (sm != null) {
            return sm.getImsi();
        }
        return null;
    }

    public void cleanup() {
        this.mDialogAdapter.cleanup();
    }
}
