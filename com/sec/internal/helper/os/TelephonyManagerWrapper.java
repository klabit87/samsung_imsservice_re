package com.sec.internal.helper.os;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SemSystemProperties;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.samsung.android.cmcsetting.CmcSettingManager;
import com.samsung.android.cmcsetting.CmcSettingManagerConstants;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.ims.core.cmc.CmcAccountManager;
import com.sec.internal.log.IMSLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class TelephonyManagerWrapper implements ITelephonyManager {
    public static final int DEFAULT_ID = -1;
    private static final String LOG_TAG = "TelephonyManagerWrapper";
    private static volatile TelephonyManagerWrapper mInstance = null;
    private Context mContext = null;
    private String mDeviceType = "";
    private SparseArray<String> mGid1 = new SparseArray<>();
    private SparseArray<String> mGid2 = new SparseArray<>();
    private SparseArray<String> mHomeDomain = new SparseArray<>();
    private SparseArray<String> mImei = new SparseArray<>();
    private SparseArray<String> mImpi = new SparseArray<>();
    private SparseArray<String[]> mImpus = new SparseArray<>();
    private SparseArray<String> mImsi = new SparseArray<>();
    private SparseArray<String> mOperatorCode = new SparseArray<>();

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public static synchronized com.sec.internal.helper.os.ITelephonyManager getInstance(android.content.Context r2) {
        /*
            java.lang.Class<com.sec.internal.helper.os.TelephonyManagerWrapper> r0 = com.sec.internal.helper.os.TelephonyManagerWrapper.class
            monitor-enter(r0)
            com.sec.internal.helper.os.TelephonyManagerWrapper r1 = mInstance     // Catch:{ all -> 0x001c }
            if (r1 != 0) goto L_0x0018
            monitor-enter(r0)     // Catch:{ all -> 0x001c }
            com.sec.internal.helper.os.TelephonyManagerWrapper r1 = mInstance     // Catch:{ all -> 0x0015 }
            if (r1 != 0) goto L_0x0013
            com.sec.internal.helper.os.TelephonyManagerWrapper r1 = new com.sec.internal.helper.os.TelephonyManagerWrapper     // Catch:{ all -> 0x0015 }
            r1.<init>(r2)     // Catch:{ all -> 0x0015 }
            mInstance = r1     // Catch:{ all -> 0x0015 }
        L_0x0013:
            monitor-exit(r0)     // Catch:{ all -> 0x0015 }
            goto L_0x0018
        L_0x0015:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0015 }
            throw r1     // Catch:{ all -> 0x001c }
        L_0x0018:
            com.sec.internal.helper.os.TelephonyManagerWrapper r1 = mInstance     // Catch:{ all -> 0x001c }
            monitor-exit(r0)
            return r1
        L_0x001c:
            r2 = move-exception
            monitor-exit(r0)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.TelephonyManagerWrapper.getInstance(android.content.Context):com.sec.internal.helper.os.ITelephonyManager");
    }

    public TelephonyManagerWrapper(Context context) {
        this.mContext = context;
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
    }

    private TelephonyManager getTelephonyManager(int subId) {
        return ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).createForSubscriptionId(subId);
    }

    public String getMsisdn(int subscriptionId) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return (String) ReflectionUtils.invoke2(cls.getMethod("getMsisdn", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subscriptionId)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isNetworkRoaming() {
        return getTelephonyManager().isNetworkRoaming();
    }

    public boolean isNetworkRoaming(int subId) {
        return getTelephonyManager(subId).isNetworkRoaming();
    }

    public String getNetworkOperator(int subId) {
        return getTelephonyManager(subId).getNetworkOperator();
    }

    public String getNetworkOperatorForPhone(int phoneId) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return (String) ReflectionUtils.invoke2(cls.getMethod("getNetworkOperatorForPhone", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(phoneId)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getNetworkCountryIso() {
        return getTelephonyManager().getNetworkCountryIso();
    }

    public String getNetworkCountryIso(int subId) {
        return getTelephonyManager(subId).getNetworkCountryIso();
    }

    public int getVoiceNetworkType() {
        return getTelephonyManager().getVoiceNetworkType();
    }

    public int getVoiceNetworkType(int subId) {
        return getTelephonyManager(subId).getVoiceNetworkType();
    }

    public void clearCache() {
        this.mImei.clear();
        this.mImsi.clear();
        this.mImpi.clear();
        this.mImpus.clear();
        this.mHomeDomain.clear();
        this.mOperatorCode.clear();
        this.mGid1.clear();
        this.mGid2.clear();
    }

    public int getServiceState() {
        ServiceState state = getTelephonyManager().getServiceState();
        if (state != null) {
            return state.getState();
        }
        return -1;
    }

    public int getServiceStateForSubscriber(int subId) {
        ServiceState ss = getTelephonyManager(subId).getServiceState();
        if (ss != null) {
            return ss.getState();
        }
        return -1;
    }

    public int getDataNetworkType() {
        return getTelephonyManager().getDataNetworkType();
    }

    public int getDataNetworkType(int subId) {
        return getTelephonyManager(subId).getDataNetworkType();
    }

    public int getNetworkType() {
        return getTelephonyManager().getNetworkType();
    }

    public int getPhoneCount() {
        int phoneCount = getTelephonyManager().getPhoneCount();
        if (phoneCount != 0 || !isCmcSecondaryDevice()) {
            return phoneCount;
        }
        return 1;
    }

    public int getSimState() {
        return getTelephonyManager().getSimState();
    }

    public int getSimState(int slotIdx) {
        return getTelephonyManager().getSimState(slotIdx);
    }

    public boolean isGbaSupported() {
        try {
            return ((Boolean) TelephonyManager.class.getMethod("isGbaSupported", new Class[0]).invoke(getTelephonyManager(), new Object[0])).booleanValue();
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
            Log.d(LOG_TAG, "isGbaSupported: Method not supported.");
            return false;
        }
    }

    public boolean isGbaSupported(int subId) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return ((Boolean) cls.getMethod("isGbaSupported", new Class[]{Integer.TYPE}).invoke(getTelephonyManager(), new Object[]{Integer.valueOf(subId)})).booleanValue();
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
            Log.d(LOG_TAG, "isGbaSupported(subId): Method not supported.");
            return false;
        }
    }

    public String getIccAuthentication(int subId, int appType, int authType, String nonce) {
        return getTelephonyManager(subId).getIccAuthentication(appType, authType, nonce);
    }

    public String getApnOperatorCode(String operatorCode, int simslot) {
        if (TextUtils.isEmpty(operatorCode)) {
            return "";
        }
        String omcNWCode = OmcCode.getNWCode(simslot);
        if ("LRA".equalsIgnoreCase(omcNWCode) || "ACG".equalsIgnoreCase(omcNWCode)) {
            String apnOperatorCode = SemSystemProperties.get("gsm.apn.sim.operator.numeric", "");
            if (!TextUtils.isEmpty(apnOperatorCode)) {
                Log.e(LOG_TAG, "for " + omcNWCode + "use apnOperatorCode " + apnOperatorCode);
                return apnOperatorCode;
            }
        }
        return operatorCode;
    }

    public String getSimOperator() {
        String operatorCode = getTelephonyManager().getSimOperator();
        int phoneId = Extensions.SubscriptionManager.getDefaultDataPhoneId(SubscriptionManager.from(this.mContext));
        if (TextUtils.isEmpty(operatorCode)) {
            operatorCode = this.mOperatorCode.get(-1);
            Log.e(LOG_TAG, "use backup operatorCode : " + IMSLog.checker(operatorCode));
        } else {
            this.mOperatorCode.put(-1, operatorCode);
        }
        return getApnOperatorCode(operatorCode, phoneId);
    }

    public String getSimOperator(int subscriptionId) {
        String operatorCode = "";
        int phoneId = Extensions.SubscriptionManager.getSlotId(subscriptionId);
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            operatorCode = (String) ReflectionUtils.invoke2(cls.getMethod("getSimOperator", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subscriptionId)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        String simOp = getTelephonyProperty(phoneId, "ril.simoperator", "ETC");
        if (simOp.contains("CTC")) {
            if ("46003".equals(operatorCode) || "46001".equals(operatorCode) || "20404".equals(operatorCode) || "45502".equals(operatorCode) || "45403".equals(operatorCode) || "45431".equals(operatorCode)) {
                operatorCode = "46011";
            }
            if ("46011".equals(operatorCode)) {
                String iccid = getSimSerialNumber(subscriptionId);
                if (!TextUtils.isEmpty(iccid) && (iccid.startsWith("8985307") || iccid.startsWith("8985302"))) {
                    operatorCode = "45507";
                }
            }
        } else if ("APT".equals(simOp) && "52505".equals(operatorCode)) {
            operatorCode = "46605";
        }
        if (TextUtils.isEmpty(operatorCode)) {
            operatorCode = this.mOperatorCode.get(subscriptionId);
            Log.e(LOG_TAG, "use backup operatorCode : " + IMSLog.checker(operatorCode));
        } else {
            this.mOperatorCode.put(subscriptionId, operatorCode);
        }
        if (phoneId == -1) {
            phoneId = Extensions.SubscriptionManager.getDefaultDataPhoneId(SubscriptionManager.from(this.mContext));
        }
        return getApnOperatorCode(operatorCode, phoneId);
    }

    public String getIsimImpi(int subscriptionId) {
        String impi = "";
        try {
            impi = (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getIsimImpi", new Class[0]), getTelephonyManager(subscriptionId), new Object[0]);
        } catch (IllegalStateException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(impi)) {
            String impi2 = this.mImpi.get(subscriptionId);
            Log.e(LOG_TAG, "use backup impi : " + IMSLog.checker(impi2));
            return impi2;
        }
        this.mImpi.put(subscriptionId, impi);
        return impi;
    }

    public String getIsimDomain(int subscriptionId) {
        String domain = "";
        try {
            domain = (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getIsimDomain", new Class[0]), getTelephonyManager(subscriptionId), new Object[0]);
        } catch (IllegalStateException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(domain)) {
            String domain2 = this.mHomeDomain.get(subscriptionId);
            Log.e(LOG_TAG, "use backup domain : " + IMSLog.checker(domain2));
            return domain2;
        }
        this.mHomeDomain.put(subscriptionId, domain);
        return domain;
    }

    public String[] getIsimImpu() {
        String[] impus = null;
        try {
            impus = (String[]) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getIsimImpu", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (impus == null || impus.length == 0) {
            String[] impus2 = this.mImpus.get(-1);
            Log.e(LOG_TAG, "use backup domain : " + IMSLog.checker(Arrays.toString(impus2)));
            return impus2;
        }
        this.mImpus.put(-1, impus);
        return impus;
    }

    public String[] getIsimImpu(int subscriptionId) {
        String[] impus = null;
        try {
            impus = (String[]) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getIsimImpu", new Class[0]), getTelephonyManager(subscriptionId), new Object[0]);
        } catch (IllegalStateException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (impus == null || impus.length == 0) {
            String[] impus2 = this.mImpus.get(subscriptionId);
            Log.e(LOG_TAG, "use backup impu : " + IMSLog.checker(Arrays.toString(impus2)));
            return impus2;
        }
        this.mImpus.put(subscriptionId, impus);
        return impus;
    }

    public String getLine1Number() {
        return getTelephonyManager().getLine1Number();
    }

    public String getSubscriberId(int subscriptionId) {
        if (getTelephonyProperty(Extensions.SubscriptionManager.getSlotId(subscriptionId), "ril.simoperator", "ETC").contains("CTC")) {
            String imsi = getSubscriberIdForUiccAppType(subscriptionId, 2);
            if (!TextUtils.isEmpty(imsi)) {
                return imsi;
            }
        }
        String imsi2 = getTelephonyManager(subscriptionId).getSubscriberId();
        if (TextUtils.isEmpty(imsi2)) {
            String imsi3 = this.mImsi.get(subscriptionId);
            Log.e(LOG_TAG, "use backup imsi : " + IMSLog.checker(imsi3));
            return imsi3;
        }
        this.mImsi.put(subscriptionId, imsi2);
        return imsi2;
    }

    public String getSubscriberIdForUiccAppType(int subscriptionId, int UiccAppType) {
        String imsi = "";
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            imsi = (String) ReflectionUtils.invoke2(cls.getMethod("getSubscriberIdForUiccAppType", new Class[]{Integer.TYPE, Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subscriptionId), Integer.valueOf(UiccAppType)});
        } catch (IllegalStateException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(imsi)) {
            String imsi2 = this.mImsi.get(subscriptionId);
            Log.e(LOG_TAG, "use backup imsi : " + IMSLog.checker(imsi2));
            return imsi2;
        }
        this.mImsi.put(subscriptionId, imsi);
        return imsi;
    }

    public String getSimSerialNumber() {
        return getTelephonyManager().getSimSerialNumber();
    }

    public String getSimSerialNumber(int subscriptionId) {
        return getTelephonyManager(subscriptionId).getSimSerialNumber();
    }

    public boolean validateMsisdn(int subscriptionId) {
        if (TextUtils.isEmpty(getMsisdn())) {
            Log.e(LOG_TAG, "empty msisdn");
            return false;
        } else if ("0000000000".equals(getCdmaMdn())) {
            Log.e(LOG_TAG, "empty mdn");
            return false;
        } else if (isValidIsimMsisdn(subscriptionId)) {
            return true;
        } else {
            Log.e(LOG_TAG, "empty iSimMsisdn");
            return false;
        }
    }

    private boolean isValidIsimMsisdn(int subscriptionId) {
        String[] impus = getIsimImpu(subscriptionId);
        String iSimMsisdn = "";
        if (!(impus == null || impus.length == 0)) {
            for (String impu : impus) {
                if (impu != null && (impu.contains("+") || impu.startsWith("tel"))) {
                    iSimMsisdn = extractNumber(impu);
                }
            }
        }
        return !"+8200000000000".equals(iSimMsisdn);
    }

    private String extractNumber(String number) {
        String msisdn = URI.create(number.trim()).getSchemeSpecificPart().toLowerCase();
        int idx = msisdn.indexOf("@");
        if (idx != -1) {
            return msisdn.substring(0, idx);
        }
        return msisdn;
    }

    public int getCallState() {
        return getTelephonyManager().getCallState();
    }

    public int getCallState(int phoneId) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return ((Integer) ReflectionUtils.invoke2(cls.getMethod("getCallStateForSlot", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(phoneId)})).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getCurrentPhoneTypeForSlot(int slotId) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return ((Integer) ReflectionUtils.invoke2(cls.getMethod("getCurrentPhoneTypeForSlot", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(slotId)})).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getGroupIdLevel1() {
        String gid = getTelephonyManager().getGroupIdLevel1();
        if (TextUtils.isEmpty(gid)) {
            String gid2 = this.mGid1.get(-1);
            Log.e(LOG_TAG, "use backup gid : " + IMSLog.checker(gid2));
            return gid2;
        }
        this.mGid1.put(-1, gid);
        return gid;
    }

    public String getGroupIdLevel1(int subId) {
        String gid = "";
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            gid = (String) ReflectionUtils.invoke2(cls.getMethod("getGroupIdLevel1", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subId)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(gid)) {
            String gid2 = this.mGid1.get(subId);
            Log.e(LOG_TAG, "use backup gid : " + IMSLog.checker(gid2));
            return gid2;
        }
        this.mGid1.put(subId, gid);
        return gid;
    }

    public String getGid2(int subId) {
        String gid2 = "";
        IMSLog.d(LOG_TAG, "getGid2:");
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            gid2 = (String) ReflectionUtils.invoke2(cls.getMethod("getGroupIdLevel2", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subId)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(gid2)) {
            String gid22 = this.mGid2.get(subId);
            Log.e(LOG_TAG, "use backup gid2 : " + IMSLog.checker(gid22));
            return gid22;
        }
        this.mGid2.put(subId, gid2);
        return gid2;
    }

    public String[] getIsimPcscf() {
        try {
            return (String[]) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getIsimPcscf", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getDataServiceState() {
        try {
            return ((Integer) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("semGetDataServiceState", new Class[0]), getTelephonyManager(), new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int getDataServiceState(int subId) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return ((Integer) ReflectionUtils.invoke2(cls.getMethod("semGetDataServiceState", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subId)})).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public String getDeviceId() {
        String deviceId = getTelephonyManager().getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            String deviceId2 = this.mImei.get(-1);
            Log.e(LOG_TAG, "use backup deviceId : " + IMSLog.checker(deviceId2));
            return deviceId2;
        }
        this.mImei.put(-1, deviceId);
        return deviceId;
    }

    public String getDeviceId(int slotId) {
        String deviceId = getTelephonyManager().getDeviceId(slotId);
        if (TextUtils.isEmpty(deviceId)) {
            String deviceId2 = this.mImei.get(slotId);
            Log.e(LOG_TAG, "use backup deviceId : " + IMSLog.checker(deviceId2));
            return deviceId2;
        }
        this.mImei.put(slotId, deviceId);
        return deviceId;
    }

    public void setGbaBootstrappingParams(byte[] bRand, String btid, String keyLifetime) {
        try {
            ReflectionUtils.invoke(TelephonyManager.class.getMethod("setGbaBootstrappingParams", new Class[]{byte[].class, String.class, String.class}), getTelephonyManager(), new Object[]{bRand, btid, keyLifetime});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public String getAidForAppType(int appType) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return (String) ReflectionUtils.invoke2(cls.getMethod("getAidForAppType", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(appType)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAidForAppType(int subId, int appType) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return (String) ReflectionUtils.invoke2(cls.getMethod("getAidForAppType", new Class[]{Integer.TYPE, Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subId), Integer.valueOf(appType)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getRand() {
        try {
            return (byte[]) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getRand", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getBtid() {
        try {
            return (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getBtid", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getKeyLifetime() {
        try {
            return (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getKeyLifetime", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setCallState(int state) {
    }

    private CellLocation getCellLocation() {
        try {
            return (CellLocation) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getCellLocation", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CellLocation getCellLocationBySubId(int subId) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return (CellLocation) ReflectionUtils.invoke2(cls.getMethod("getCellLocationBySubId", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subId)});
        } catch (NoSuchMethodException e) {
            return getCellLocation();
        }
    }

    public List<CellInfo> getAllCellInfo() {
        return getTelephonyManager().getAllCellInfo();
    }

    public String getSimCountryIso() {
        return getTelephonyManager().getSimCountryIso();
    }

    public String getSimCountryIsoForPhone(int phoneId) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return (String) ReflectionUtils.invoke2(cls.getMethod("getSimCountryIsoForPhone", new Class[]{Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(phoneId)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setPreferredNetworkType(int subId, int networkType) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return ((Boolean) ReflectionUtils.invoke2(cls.getMethod("setPreferredNetworkType", new Class[]{Integer.TYPE, Integer.TYPE}), getTelephonyManager(), new Object[]{Integer.valueOf(subId), Integer.valueOf(networkType)})).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getMsisdn() {
        try {
            return (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getMsisdn", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getCdmaMdn() {
        try {
            return (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getCdmaMdn", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getImei() {
        return getTelephonyManager().getImei();
    }

    public String getImei(int slotId) {
        return getTelephonyManager().getImei(slotId);
    }

    public String getMeid() {
        return getTelephonyManager().getMeid();
    }

    public String getMeid(int slotId) {
        return getTelephonyManager().getMeid(slotId);
    }

    public String getSubscriberId() {
        String imsi = getTelephonyManager().getSubscriberId();
        if (TextUtils.isEmpty(imsi)) {
            String imsi2 = this.mImsi.get(-1);
            Log.e(LOG_TAG, "use backup imsi : " + IMSLog.checker(imsi2));
            return imsi2;
        }
        this.mImsi.put(-1, imsi);
        return imsi;
    }

    public String getIsimDomain() {
        try {
            return (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getIsimDomain", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            if (TextUtils.isEmpty("")) {
                String domain = this.mHomeDomain.get(-1);
                Log.e(LOG_TAG, "use backup domain : " + IMSLog.checker(domain));
                return domain;
            }
            this.mHomeDomain.put(-1, "");
            return "";
        }
    }

    public String getIsimImpi() {
        String impi = "";
        try {
            impi = (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getIsimImpi", new Class[0]), getTelephonyManager(), new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(impi)) {
            String impi2 = this.mImpi.get(-1);
            Log.e(LOG_TAG, "use backup impi : " + IMSLog.checker(impi2));
            return impi2;
        }
        this.mImpi.put(-1, impi);
        return impi;
    }

    public void setRadioPower(boolean turnOn) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            Method method = cls.getDeclaredMethod("setRadioPower", new Class[]{Boolean.TYPE});
            method.setAccessible(true);
            method.invoke(getTelephonyManager(), new Object[]{Boolean.valueOf(turnOn)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
        }
    }

    public String getTelephonyProperty(int phoneId, String property, String defaultVal) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return (String) ReflectionUtils.invoke2(cls.getMethod("getTelephonyProperty", new Class[]{Integer.TYPE, String.class, String.class}), getTelephonyManager(), new Object[]{Integer.valueOf(phoneId), property, defaultVal});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setTelephonyProperty(int phoneId, String property, String value) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            ReflectionUtils.invoke(cls.getMethod("setTelephonyProperty", new Class[]{Integer.TYPE, String.class, String.class}), getTelephonyManager(), new Object[]{Integer.valueOf(phoneId), property, value});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int getIntAtIndex(ContentResolver cr, String name, int index) {
        Class<TelephonyManager> cls = TelephonyManager.class;
        try {
            return ((Integer) ReflectionUtils.invoke2(cls.getMethod("getIntAtIndex", new Class[]{ContentResolver.class, String.class, Integer.TYPE}), getTelephonyManager(), new Object[]{cr, name, Integer.valueOf(index)})).intValue();
        } catch (IllegalStateException | NoSuchMethodException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getSimOperatorName(int subId) {
        return getTelephonyManager(subId).getSimOperatorName();
    }

    private boolean isCmcSecondaryDevice() {
        if (!TextUtils.isEmpty(this.mDeviceType)) {
            IMSLog.d(LOG_TAG, "getPhoneCount: isCmcSecondaryDevice: cache " + this.mDeviceType);
            return "sd".equalsIgnoreCase(this.mDeviceType);
        }
        CmcSettingManager cmcSettingMgr = new CmcSettingManager();
        cmcSettingMgr.init(this.mContext);
        CmcSettingManagerConstants.DeviceType type = cmcSettingMgr.getOwnDeviceType();
        cmcSettingMgr.deInit();
        IMSLog.d(LOG_TAG, "getPhoneCount: isCmcSecondaryDevice: api: " + type);
        if (type == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD) {
            this.mDeviceType = "sd";
            return true;
        } else if (type == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            this.mDeviceType = "pd";
            return false;
        } else {
            if (TextUtils.isEmpty(this.mDeviceType)) {
                String deviceType_prop = SemSystemProperties.get(CmcAccountManager.CMC_DEVICE_TYPE_PROP, "");
                if (!TextUtils.isEmpty(deviceType_prop)) {
                    this.mDeviceType = deviceType_prop;
                    IMSLog.d(LOG_TAG, "getPhoneCount: isCmcSecondaryDevice: prop " + this.mDeviceType);
                    return "sd".equalsIgnoreCase(deviceType_prop);
                }
                this.mDeviceType = NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
            }
            return false;
        }
    }

    public boolean isVoiceCapable() {
        return getTelephonyManager().isVoiceCapable();
    }

    public boolean hasCall(String callType) {
        return getTelephonyManager().hasCall(callType);
    }

    public void setImsRegistrationState(int phoneId, boolean registered) {
        int subId;
        try {
            int[] subIdArray = Extensions.SubscriptionManager.getSubId(phoneId);
            if (subIdArray == null) {
                IMSLog.e(LOG_TAG, phoneId, "subIdArray is null");
                subId = -1;
            } else {
                subId = subIdArray[0];
            }
            ReflectionUtils.invoke(TelephonyManager.class.getMethod("setImsRegistrationStateForSlot", new Class[]{Integer.TYPE, Boolean.TYPE}), getTelephonyManager(subId), new Object[]{Integer.valueOf(phoneId), Boolean.valueOf(registered)});
            IMSLog.d(LOG_TAG, phoneId, "setImsRegistrationStateForSlot : subId:" + subId + ", registered:" + registered);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int iccOpenLogicalChannelAndGetChannel(int subId, String AID) {
        return getTelephonyManager().iccOpenLogicalChannel(subId, AID, 4).getChannel();
    }

    public String iccTransmitApduLogicalChannel(int subId, int channel, int cla, int instruction, int p1, int p2, int p3, String data) {
        return getTelephonyManager().iccTransmitApduLogicalChannel(subId, channel, cla, instruction, p1, p2, p3, data);
    }

    public boolean iccCloseLogicalChannel(int subId, int channel) {
        return getTelephonyManager().iccCloseLogicalChannel(subId, channel);
    }
}
