package com.sec.internal.ims.servicemodules.euc.persistence;

import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.tapi.IUserConsentListener;

public class UserConsentPersistenceNotifier {
    private IUserConsentListener mListener;

    private UserConsentPersistenceNotifier() {
        this.mListener = null;
    }

    private static class UserConsentPersistenceNotifierHolder {
        /* access modifiers changed from: private */
        public static UserConsentPersistenceNotifier mUserConsentPersistenceNotifier = new UserConsentPersistenceNotifier();

        private UserConsentPersistenceNotifierHolder() {
        }
    }

    public static UserConsentPersistenceNotifier getInstance() {
        return UserConsentPersistenceNotifierHolder.mUserConsentPersistenceNotifier;
    }

    public void setListener(IUserConsentListener listener) {
        this.mListener = listener;
    }

    public void notifyListener(int phoneId) {
        IUserConsentListener iUserConsentListener = this.mListener;
        if (iUserConsentListener != null) {
            iUserConsentListener.notifyChanged(phoneId);
        }
    }

    public void notifyListener(String ownIdentity) {
        if (this.mListener != null) {
            for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
                if (ownIdentity.equals(sm.getImsi())) {
                    this.mListener.notifyChanged(sm.getSimSlotIndex());
                    return;
                }
            }
        }
    }
}
