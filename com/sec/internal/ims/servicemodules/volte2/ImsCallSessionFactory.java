package com.sec.internal.ims.servicemodules.volte2;

import android.os.Looper;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;

public class ImsCallSessionFactory {
    private static final String LOG_TAG = "ImsCallSessionFactory";
    private static int mCallIdCounter = 0;
    private IVolteServiceModuleInternal mModule;
    private Looper mServiceModuleLooper = null;

    public ImsCallSessionFactory(IVolteServiceModuleInternal module, Looper looper) {
        this.mModule = module;
        this.mServiceModuleLooper = looper;
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    /* JADX WARNING: type inference failed for: r1v2, types: [com.sec.internal.ims.servicemodules.volte2.ImsCallSession] */
    /* JADX WARNING: type inference failed for: r3v16, types: [com.sec.internal.ims.servicemodules.volte2.ImsConfSession] */
    /* JADX WARNING: type inference failed for: r3v17, types: [com.sec.internal.ims.servicemodules.volte2.ImsCallSession] */
    /* JADX WARNING: type inference failed for: r3v18, types: [com.sec.internal.ims.servicemodules.volte2.ImsCallSession] */
    /* JADX WARNING: type inference failed for: r3v19, types: [com.sec.internal.ims.servicemodules.volte2.ImsConfSession] */
    /* JADX WARNING: type inference failed for: r1v18 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.servicemodules.volte2.ImsCallSession create(com.sec.ims.volte2.data.CallProfile r10, com.sec.ims.ImsRegistration r11, boolean r12) {
        /*
            r9 = this;
            monitor-enter(r9)
            if (r11 != 0) goto L_0x000c
            int r0 = r10.getPhoneId()     // Catch:{ all -> 0x0127 }
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)     // Catch:{ all -> 0x0127 }
            goto L_0x0018
        L_0x000c:
            com.sec.ims.settings.ImsProfile r0 = r11.getImsProfile()     // Catch:{ all -> 0x0127 }
            java.lang.String r0 = r0.getMnoName()     // Catch:{ all -> 0x0127 }
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.fromName(r0)     // Catch:{ all -> 0x0127 }
        L_0x0018:
            com.sec.internal.helper.Preconditions.checkNotNull(r10)     // Catch:{ all -> 0x0127 }
            int r1 = r10.getNetworkType()     // Catch:{ all -> 0x0127 }
            r2 = 15
            if (r1 == r2) goto L_0x0028
            if (r12 != 0) goto L_0x0028
            com.sec.internal.helper.Preconditions.checkNotNull(r11)     // Catch:{ all -> 0x0127 }
        L_0x0028:
            int r1 = r10.getNetworkType()     // Catch:{ all -> 0x0127 }
            if (r1 != r2) goto L_0x0046
            java.lang.String r1 = "ImsCallSessionFactory"
            java.lang.String r2 = "createImsCallSession: emergency session."
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession r1 = new com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r2 = r9.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r2 = r2.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r3 = r9.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r4 = r9.mModule     // Catch:{ all -> 0x0127 }
            r1.<init>(r2, r10, r3, r4)     // Catch:{ all -> 0x0127 }
            goto L_0x00cc
        L_0x0046:
            if (r11 != 0) goto L_0x008c
            if (r12 == 0) goto L_0x008c
            int r1 = r10.getCmcType()     // Catch:{ all -> 0x0127 }
            if (r1 != 0) goto L_0x0072
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.SKT     // Catch:{ all -> 0x0127 }
            if (r0 == r1) goto L_0x0058
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.LGU     // Catch:{ all -> 0x0127 }
            if (r0 != r1) goto L_0x0072
        L_0x0058:
            java.lang.String r1 = "ImsCallSessionFactory"
            java.lang.String r2 = "createImsCallSession: conf call session without regi info"
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsConfSession r1 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r2 = r9.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r4 = r2.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r7 = r9.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r8 = r9.mModule     // Catch:{ all -> 0x0127 }
            r3 = r1
            r5 = r10
            r6 = r11
            r3.<init>(r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0127 }
            goto L_0x00cc
        L_0x0072:
            java.lang.String r1 = "ImsCallSessionFactory"
            java.lang.String r2 = "createImsCallSession: normal call session without regi info"
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = new com.sec.internal.ims.servicemodules.volte2.ImsCallSession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r2 = r9.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r4 = r2.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r7 = r9.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r8 = r9.mModule     // Catch:{ all -> 0x0127 }
            r3 = r1
            r5 = r10
            r6 = r11
            r3.<init>(r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0127 }
            goto L_0x00cc
        L_0x008c:
            boolean r1 = r10.isConferenceCall()     // Catch:{ all -> 0x0127 }
            if (r1 != 0) goto L_0x00b3
            boolean r1 = r9.isDefaultConfSession(r11)     // Catch:{ all -> 0x0127 }
            if (r1 == 0) goto L_0x0099
            goto L_0x00b3
        L_0x0099:
            java.lang.String r1 = "ImsCallSessionFactory"
            java.lang.String r2 = "createImsCallSession: normal call session"
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = new com.sec.internal.ims.servicemodules.volte2.ImsCallSession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r2 = r9.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r4 = r2.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r7 = r9.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r8 = r9.mModule     // Catch:{ all -> 0x0127 }
            r3 = r1
            r5 = r10
            r6 = r11
            r3.<init>(r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0127 }
            goto L_0x00cc
        L_0x00b3:
            java.lang.String r1 = "ImsCallSessionFactory"
            java.lang.String r2 = "createImsCallSession: conference session."
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsConfSession r1 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r2 = r9.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r4 = r2.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r7 = r9.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r8 = r9.mModule     // Catch:{ all -> 0x0127 }
            r3 = r1
            r5 = r10
            r6 = r11
            r3.<init>(r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0127 }
        L_0x00cc:
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r2 = com.sec.internal.ims.registry.ImsRegistry.getHandlerFactory()     // Catch:{ all -> 0x0127 }
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r2 = r2.getVolteStackAdaptor()     // Catch:{ all -> 0x0127 }
            com.sec.internal.interfaces.ims.core.IRegistrationManager r3 = com.sec.internal.ims.registry.ImsRegistry.getRegistrationManager()     // Catch:{ all -> 0x0127 }
            r1.init(r2, r3)     // Catch:{ all -> 0x0127 }
            int r2 = r9.createCallId()     // Catch:{ all -> 0x0127 }
            if (r2 >= 0) goto L_0x00e4
            r3 = 0
            monitor-exit(r9)
            return r3
        L_0x00e4:
            if (r11 == 0) goto L_0x0103
            com.sec.ims.settings.ImsProfile r3 = r11.getImsProfile()     // Catch:{ all -> 0x0127 }
            if (r3 == 0) goto L_0x0103
            com.sec.ims.settings.ImsProfile r3 = r11.getImsProfile()     // Catch:{ all -> 0x0127 }
            int r3 = r3.getCmcType()     // Catch:{ all -> 0x0127 }
            r1.setCmcType(r3)     // Catch:{ all -> 0x0127 }
            com.sec.ims.settings.ImsProfile r3 = r11.getImsProfile()     // Catch:{ all -> 0x0127 }
            int r3 = r3.getVideoCrbtSupportType()     // Catch:{ all -> 0x0127 }
            r1.setVideoCrbtSupportType(r3)     // Catch:{ all -> 0x0127 }
            goto L_0x0122
        L_0x0103:
            if (r11 != 0) goto L_0x0122
            int r3 = r10.getCmcType()     // Catch:{ all -> 0x0127 }
            r4 = 2
            if (r3 == r4) goto L_0x011b
            int r3 = r10.getCmcType()     // Catch:{ all -> 0x0127 }
            r4 = 4
            if (r3 == r4) goto L_0x011b
            int r3 = r10.getCmcType()     // Catch:{ all -> 0x0127 }
            r4 = 8
            if (r3 != r4) goto L_0x0122
        L_0x011b:
            int r3 = r10.getCmcType()     // Catch:{ all -> 0x0127 }
            r1.setCmcType(r3)     // Catch:{ all -> 0x0127 }
        L_0x0122:
            r1.setCallId(r2)     // Catch:{ all -> 0x0127 }
            monitor-exit(r9)
            return r1
        L_0x0127:
            r10 = move-exception
            monitor-exit(r9)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsCallSessionFactory.create(com.sec.ims.volte2.data.CallProfile, com.sec.ims.ImsRegistration, boolean):com.sec.internal.ims.servicemodules.volte2.ImsCallSession");
    }

    private boolean isDefaultConfSession(ImsRegistration reg) {
        if (reg == null) {
            return false;
        }
        Mno mno = Mno.fromName(reg.getImsProfile().getMnoName());
        if ((mno == Mno.SKT || mno == Mno.LGU) && !reg.getImsProfile().isSamsungMdmnEnabled()) {
            return true;
        }
        return false;
    }

    private int createCallId() {
        boolean repeated = false;
        while (true) {
            if (mCallIdCounter >= 255) {
                mCallIdCounter = 0;
                if (repeated) {
                    Log.e(LOG_TAG, "All CallId is allocated, session create fail");
                    return -1;
                }
                repeated = true;
            }
            int i = mCallIdCounter + 1;
            mCallIdCounter = i;
            if (this.mModule.getSessionByCallId(i) == null) {
                return mCallIdCounter;
            }
            Log.i(LOG_TAG, "Call " + mCallIdCounter + " is exist");
        }
    }
}
