package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SemSystemProperties;
import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.PaniConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PaniGenerator {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = PaniGenerator.class.getSimpleName();
    private static final String PLANIINTIME_PREF = "PLANIINTIME";
    protected static final String PLANI_PREF = "PLANI";
    /* access modifiers changed from: private */
    public boolean isSoftPhone;
    /* access modifiers changed from: private */
    public Context mContext;
    protected PaniGeneratorBase mGenerator;
    protected List<String> mLastPaniList = new ArrayList();
    /* access modifiers changed from: private */
    public IPdnController mPdnController;
    protected List<String> mPrevLastPaniList = new ArrayList();
    protected ITelephonyManager mTelephonyManager;

    public PaniGenerator(Context context, IPdnController pdnController) {
        this.mContext = context;
        this.mPdnController = pdnController;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        this.isSoftPhone = SimUtil.isSoftphoneEnabled();
        for (int i = 0; i < this.mTelephonyManager.getPhoneCount(); i++) {
            this.mPrevLastPaniList.add(i, ImsSharedPrefHelper.getString(i, this.mContext, ImsSharedPrefHelper.IMS_CONFIG, PLANI_PREF, (String) null));
            this.mLastPaniList.add(i, "");
        }
        this.mGenerator = new PaniGeneratorBase();
    }

    public String generate(int pdn, String fallbackPlmn, int phoneId) {
        int subId = SimUtil.getSubId(phoneId);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "generate: subId - " + subId);
        int network = this.mTelephonyManager.getDataNetworkType(subId);
        if (pdn != 15 || this.isSoftPhone) {
            if (pdn == 1 || pdn == ConnectivityManagerExt.TYPE_WIFI_P2P || (this.mPdnController.isEpdgConnected(phoneId) && this.mPdnController.isWifiConnected())) {
                network = 18;
            } else if (network == 18 && !this.mPdnController.isEpdgConnected(phoneId)) {
                network = this.mTelephonyManager.getVoiceNetworkType(subId);
            }
            if (network != 0) {
                return this.mGenerator.generate(network, fallbackPlmn, phoneId);
            }
            Log.e(LOG_TAG, "network is unknown.");
            return null;
        } else if (this.mPdnController.isEmergencyEpdgConnected(phoneId)) {
            return this.mGenerator.generate(18, phoneId);
        } else {
            return this.mGenerator.generate(13, fallbackPlmn, phoneId);
        }
    }

    protected class PaniGeneratorBase {
        protected static final String IWLAN_COUNTRY_TAG = "country=";
        protected static final String IWLAN_NODE_ID_TAG = "i-wlan-node-id=";
        protected static final String TEMPLATE_COUNTRY = "<COUNTRY>";
        protected static final String TEMPLATE_NODE_ID = "<NODE_ID>";
        protected static final String TEMPLATE_PREFIX = "<PREFIX>";
        protected static final String TEMPLATE_TIMESTAMP = "<TIMESTAMP>";

        protected PaniGeneratorBase() {
        }

        public String generate(int network, int phoneId) {
            return generate(network, (String) null, phoneId);
        }

        public String generate(int network, String fallbackPlmn, int phoneId) {
            String str;
            String access$000 = PaniGenerator.LOG_TAG;
            IMSLog.i(access$000, phoneId, "generate: network=" + network + ", fallbackPlmn=" + fallbackPlmn);
            if (network == 18) {
                return generateWifiPani(phoneId);
            }
            String plmn = (String) Optional.ofNullable(getPsPlmn(phoneId)).orElse(PaniGenerator.this.mTelephonyManager.getNetworkOperatorForPhone(phoneId));
            if (plmn.length() < 5 || "00000".equals(plmn)) {
                String access$0002 = PaniGenerator.LOG_TAG;
                Log.e(access$0002, "generate: invalid network operator " + plmn);
                if (!TextUtils.isEmpty(fallbackPlmn)) {
                    plmn = fallbackPlmn;
                } else if (!CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("persist.ril.ims.sipserverDebug", ConfigConstants.VALUE.INFO_COMPLETED))) {
                    return null;
                } else {
                    ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
                    if (sm == null) {
                        str = "";
                    } else {
                        str = sm.getSimOperator();
                    }
                    plmn = str;
                    String access$0003 = PaniGenerator.LOG_TAG;
                    Log.e(access$0003, "generate: use SIM operator " + plmn);
                }
            } else {
                String access$0004 = PaniGenerator.LOG_TAG;
                Log.i(access$0004, "generate: change to NW PLMN(" + plmn + ")");
            }
            if (network == 13 && "TDD".equals(PaniGenerator.this.mTelephonyManager.getTelephonyProperty(phoneId, "ril.ltenetworktype", ""))) {
                network = 31;
            }
            if (!(network == 1 || network == 2)) {
                if (network != 3) {
                    if (network == 20) {
                        return generateNrPani(phoneId, plmn);
                    }
                    if (network != 30) {
                        if (network == 31) {
                            return generateTdLtePani(phoneId, plmn);
                        }
                        switch (network) {
                            case 8:
                            case 9:
                            case 10:
                                break;
                            default:
                                switch (network) {
                                    case 13:
                                        return generateLtePani(phoneId, plmn);
                                    case 14:
                                        return generateEhrpdPani();
                                    case 15:
                                    case 17:
                                        break;
                                    case 16:
                                        break;
                                    default:
                                        String access$0005 = PaniGenerator.LOG_TAG;
                                        Log.e(access$0005, "PaniGenerator: Not supported network." + network);
                                        return "";
                                }
                        }
                    }
                }
                return generateUmtsPani(phoneId, plmn);
            }
            return generateGeranPani(phoneId, plmn);
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x007a, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
            r2.printStackTrace();
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x007a A[ExcHandler: IllegalAccessException | IllegalArgumentException | InvocationTargetException (r2v3 'e' java.lang.Exception A[CUSTOM_DECLARE]), PHI: r0 
          PHI: (r0v3 'retPlmn' java.lang.String) = (r0v0 'retPlmn' java.lang.String), (r0v4 'retPlmn' java.lang.String), (r0v4 'retPlmn' java.lang.String), (r0v4 'retPlmn' java.lang.String) binds: [B:5:0x001f, B:12:0x0041, B:15:0x0047, B:13:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:5:0x001f] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.String getPsPlmn(int r10) {
            /*
                r9 = this;
                r0 = 0
                com.sec.internal.ims.core.handler.secims.PaniGenerator r1 = com.sec.internal.ims.core.handler.secims.PaniGenerator.this
                com.sec.internal.interfaces.ims.core.IPdnController r1 = r1.mPdnController
                r2 = 0
                android.telephony.CellLocation r1 = r1.getCellLocation(r10, r2)
                boolean r3 = r1 instanceof android.telephony.cdma.CdmaCellLocation
                if (r3 == 0) goto L_0x0083
                r3 = r1
                android.telephony.cdma.CdmaCellLocation r3 = (android.telephony.cdma.CdmaCellLocation) r3
                java.lang.Class r4 = r3.getClass()     // Catch:{ NoSuchMethodException -> 0x007f }
                java.lang.String r5 = "getDataPlmn"
                java.lang.Class[] r6 = new java.lang.Class[r2]     // Catch:{ NoSuchMethodException -> 0x007f }
                java.lang.reflect.Method r4 = r4.getMethod(r5, r6)     // Catch:{ NoSuchMethodException -> 0x007f }
                java.lang.Object[] r2 = new java.lang.Object[r2]     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                java.lang.Object r2 = r4.invoke(r3, r2)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                r0 = r2
                boolean r2 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                r5 = 0
                if (r2 != 0) goto L_0x0061
                int r2 = r0.length()     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                r6 = 5
                if (r2 < r6) goto L_0x0061
                java.lang.String r2 = "00000"
                boolean r2 = r2.equals(r0)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                if (r2 == 0) goto L_0x0041
                goto L_0x0061
            L_0x0041:
                java.lang.Integer.parseInt(r0)     // Catch:{ NumberFormatException -> 0x0046, IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a, IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                goto L_0x007e
            L_0x0046:
                r2 = move-exception
                java.lang.String r6 = com.sec.internal.ims.core.handler.secims.PaniGenerator.LOG_TAG     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                r7.<init>()     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                java.lang.String r8 = "rePlmn by RIL is not guaranteed to be a numeric String. : "
                r7.append(r8)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                r7.append(r2)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                java.lang.String r7 = r7.toString()     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                android.util.Log.e(r6, r7)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                return r5
            L_0x0061:
                java.lang.String r2 = com.sec.internal.ims.core.handler.secims.PaniGenerator.LOG_TAG     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                r6.<init>()     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                java.lang.String r7 = "getDataPlmn from RIL returns invalid dataPlmn: "
                r6.append(r7)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                r6.append(r0)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                java.lang.String r6 = r6.toString()     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                android.util.Log.e(r2, r6)     // Catch:{ IllegalAccessException | IllegalArgumentException | InvocationTargetException -> 0x007a }
                return r5
            L_0x007a:
                r2 = move-exception
                r2.printStackTrace()     // Catch:{ NoSuchMethodException -> 0x007f }
            L_0x007e:
                goto L_0x0083
            L_0x007f:
                r2 = move-exception
                r2.printStackTrace()
            L_0x0083:
                java.lang.String r2 = com.sec.internal.ims.core.handler.secims.PaniGenerator.LOG_TAG
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                java.lang.String r4 = "getDataPlmn returns "
                r3.append(r4)
                r3.append(r0)
                java.lang.String r3 = r3.toString()
                android.util.Log.i(r2, r3)
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.PaniGenerator.PaniGeneratorBase.getPsPlmn(int):java.lang.String");
        }

        /* access modifiers changed from: protected */
        public String generateNrPani(int phoneId, String plmn) {
            int i = phoneId;
            int tac = PaniGenerator.this.getTac(i);
            long nrCid = PaniGenerator.this.getNrCid(i);
            if (tac == -1 || nrCid == -1) {
                IMSLog.i(PaniGenerator.LOG_TAG, i, "Invalid tac or nrCid : return empty.");
                return "";
            }
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            if (sm == null || !sm.getSimMno().isKor()) {
                String accessType = PaniGenerator.this.mTelephonyManager.getTelephonyProperty(i, "ril.nrnetworktype", "");
                String access$000 = PaniGenerator.LOG_TAG;
                Log.i(access$000, "NR Access Type " + accessType);
                if ("TDD".equals(accessType)) {
                    return PaniConstants.NR_PANI_PREFIX_TDD + String.format(Locale.US, "%s%06x%09x", new Object[]{plmn, Integer.valueOf(tac), Long.valueOf(nrCid)});
                } else if ("FDD".equals(accessType)) {
                    return PaniConstants.NR_PANI_PREFIX_FDD + String.format(Locale.US, "%s%06x%09x", new Object[]{plmn, Integer.valueOf(tac), Long.valueOf(nrCid)});
                } else {
                    return PaniConstants.NR_PANI_PREFIX + String.format(Locale.US, "%s%06x%09x", new Object[]{plmn, Integer.valueOf(tac), Long.valueOf(nrCid)});
                }
            } else {
                return PaniConstants.NR_PANI_PREFIX_TDD + String.format(Locale.US, "%s%06x%09x", new Object[]{plmn, Integer.valueOf(tac), Long.valueOf(nrCid)});
            }
        }

        /* access modifiers changed from: protected */
        public String generateLtePani(int phoneId, String plmn) {
            if (PaniGenerator.this.getTac(phoneId) == -1 || PaniGenerator.this.getCid(phoneId) == -1) {
                IMSLog.i(PaniGenerator.LOG_TAG, phoneId, "Invalid Cell Id : return empty.");
                return "";
            }
            String cellLocationStr = String.format(Locale.US, "%s%04x%07x", new Object[]{plmn, Integer.valueOf(PaniGenerator.this.getTac(phoneId)), Integer.valueOf(PaniGenerator.this.getCid(phoneId))});
            return PaniConstants.LTE_PANI_PREFIX + cellLocationStr;
        }

        /* access modifiers changed from: protected */
        public String generateUmtsPani(int phoneId, String plmn) {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            if (sm != null && sm.getSimMno() == Mno.ORANGE) {
                if ("7fffffff".equals(String.format("%07x", new Object[]{Integer.valueOf(PaniGenerator.this.getCid(phoneId))}))) {
                    PaniGenerator.this.queryCellInfoForQualcomm();
                }
            }
            if (PaniGenerator.this.getLac(phoneId) == -1 || PaniGenerator.this.getCid(phoneId) == -1) {
                IMSLog.i(PaniGenerator.LOG_TAG, phoneId, "Invalid Cell Id : return empty.");
                return "";
            }
            return PaniConstants.UMTS_PANI_PREFIX + String.format(Locale.US, "%s%04x%07x", new Object[]{plmn, Integer.valueOf(PaniGenerator.this.getLac(phoneId)), Integer.valueOf(PaniGenerator.this.getCid(phoneId))});
        }

        /* access modifiers changed from: protected */
        public String generateGeranPani(int phoneId, String plmn) {
            if (PaniGenerator.this.getLac(phoneId) == -1 || PaniGenerator.this.getCid(phoneId) == -1) {
                IMSLog.i(PaniGenerator.LOG_TAG, phoneId, "Invalid Cell Id : return empty.");
                return "";
            }
            return PaniConstants.EDGE_PANI_PREFIX + String.format(Locale.US, "%s%04x%04x", new Object[]{plmn, Integer.valueOf(PaniGenerator.this.getLac(phoneId)), Integer.valueOf(PaniGenerator.this.getCid(phoneId))});
        }

        /* access modifiers changed from: protected */
        public String generateTdLtePani(int phoneId, String plmn) {
            if (PaniGenerator.this.getTac(phoneId) == -1 || PaniGenerator.this.getCid(phoneId) == -1) {
                IMSLog.i(PaniGenerator.LOG_TAG, phoneId, "Invalid Cell Id : return empty.");
                return "";
            }
            return PaniConstants.TDLTE_PANI_PREFIX + String.format(Locale.US, "%s%04x%07x", new Object[]{plmn, Integer.valueOf(PaniGenerator.this.getTac(phoneId)), Integer.valueOf(PaniGenerator.this.getCid(phoneId))});
        }

        /* access modifiers changed from: protected */
        public String generateEhrpdPani() {
            byte[] currentUATI = TelephonyManagerExt.getCurrentUATI(PaniGenerator.this.mContext);
            if (currentUATI != null) {
                Log.i(PaniGenerator.LOG_TAG, "generateEhrpdPaniHeaderString(SectorId+SubnetLen) len= " + currentUATI.length);
                String bytesToHexString = StrUtil.bytesToHexString(currentUATI);
                if (currentUATI.length != 17) {
                    return null;
                }
                byte[] bArrSectorId = new byte[16];
                byte[] bArrSubLen = {currentUATI[0]};
                System.arraycopy(currentUATI, 1, bArrSectorId, 0, 16);
                return PaniConstants.EHRPD_PANI_PREFIX + (StrUtil.bytesToHexString(bArrSectorId) + StrUtil.bytesToHexString(bArrSubLen));
            }
            Log.i(PaniGenerator.LOG_TAG, "Got NULL UATI from RIL!!!");
            return null;
        }

        /* access modifiers changed from: protected */
        public String generateWifiPani(int phoneId) {
            String paniFormat = PaniConstants.DEFAULT_IWLAN_PANI_FORMAT;
            String paniFormatFromGS = ImsRegistry.getString(phoneId, GlobalSettingsConstants.Registration.IWLAN_PANI_FORMAT, PaniConstants.DEFAULT_IWLAN_PANI_FORMAT);
            if (paniFormatFromGS != null) {
                paniFormat = paniFormatFromGS;
            }
            if (PaniGenerator.this.isSoftPhone) {
                paniFormat = paniFormat.replace(TEMPLATE_PREFIX, "<PREFIX><COUNTRY>");
            }
            String paniFormat2 = paniFormat.replaceAll("><", ">;<");
            IMSLog.i(PaniGenerator.LOG_TAG, phoneId, "generateWiFiPani: Format for generating PANI - " + paniFormat2);
            String normalizedPani = paniFormat2.replace(TEMPLATE_PREFIX, PaniConstants.IWLAN_PANI_PREFIX).replace(TEMPLATE_NODE_ID, generateIwlanNodeId()).replace(TEMPLATE_COUNTRY, generateCountryCode()).replace(TEMPLATE_TIMESTAMP, generateTimeStamp(phoneId));
            while (normalizedPani.contains(";;")) {
                normalizedPani = normalizedPani.replace(";;", ";");
            }
            int lastIdx = normalizedPani.length() - 1;
            if (normalizedPani.charAt(lastIdx) == ';') {
                normalizedPani = normalizedPani.substring(0, lastIdx);
            }
            IMSLog.i(PaniGenerator.LOG_TAG, phoneId, "generateWiFiPani: normalized PANI: " + normalizedPani);
            return normalizedPani;
        }

        /* access modifiers changed from: protected */
        public String generateIwlanNodeId() {
            String bssid = PaniGenerator.this.getWifiBssid();
            if (bssid == null) {
                return "";
            }
            return IWLAN_NODE_ID_TAG + bssid.replaceAll(":", "");
        }

        /* access modifiers changed from: protected */
        public String generateTimeStamp(int phoneId) {
            String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            if (sm != null && sm.getSimMno() == Mno.TMOUS) {
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
            return "local-time-zone=\"" + sdf.format(new Date()) + "\"";
        }

        /* access modifiers changed from: protected */
        public String generateCountryCode() {
            IGeolocationController geolocationCon = ImsRegistry.getGeolocationController();
            if (geolocationCon == null) {
                return "";
            }
            LocationInfo currentLocation = geolocationCon.getGeolocation();
            String countryCode = "";
            if (currentLocation != null) {
                countryCode = currentLocation.mCountry;
            }
            if (TextUtils.isEmpty(countryCode)) {
                return "";
            }
            return IWLAN_COUNTRY_TAG + countryCode.toUpperCase();
        }
    }

    public boolean isChangedPlani(int phoneId, String plani) {
        String prevPlani = getPrevioutPlani(phoneId);
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "isChangedPlani: prev plani " + prevPlani + ", curr plani " + plani);
        if (TextUtils.equals(prevPlani, plani)) {
            return false;
        }
        setPrevioutPlani(phoneId, plani);
        return true;
    }

    public void removePreviousPlani(int phoneId) {
        String prevPlani = getPrevioutPlani(phoneId);
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "removePreviousPlani: prev plani " + prevPlani);
        setPrevioutPlani(phoneId, "");
    }

    public void setTimeInPlani(int phoneId, long time) {
        ImsSharedPrefHelper.save(phoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, PLANIINTIME_PREF, time);
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "setTimeInPlani: " + time);
    }

    public long getTimeInPlani(int phoneId) {
        long time = ImsSharedPrefHelper.getLong(phoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, PLANIINTIME_PREF, 0);
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "getTimeInPlani: " + time);
        return time;
    }

    public boolean needCellInfoAge(ImsProfile profile) {
        return profile != null && TextUtils.equals(profile.getLastPaniHeader(), PaniConstants.HEADER_CELL_NET_INFO_CIA);
    }

    public boolean needTimeStampForLastPani(ImsProfile profile) {
        return (profile == null || TextUtils.equals(profile.getLastPaniHeader(), PaniConstants.HEADER_CELL_NET_INFO_CIA) || Mno.fromName(profile.getMnoName()) == Mno.BOG) ? false : true;
    }

    /* access modifiers changed from: private */
    public int getLac(int phoneId) {
        int lac = 0;
        CellLocation cellLocation = this.mPdnController.getCellLocation(phoneId, false);
        if (cellLocation instanceof GsmCellLocation) {
            lac = ((GsmCellLocation) cellLocation).getLac();
        } else if (cellLocation instanceof CdmaCellLocation) {
            CdmaCellLocation location = (CdmaCellLocation) cellLocation;
            try {
                try {
                    lac = ((Integer) location.getClass().getMethod("getLteTac", new Class[0]).invoke(location, new Object[0])).intValue();
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        }
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "getLac:" + lac);
        return lac;
    }

    public int getCid(int phoneId) {
        int cid = 0;
        CellLocation cellLocation = this.mPdnController.getCellLocation(phoneId, false);
        if (cellLocation instanceof GsmCellLocation) {
            cid = ((GsmCellLocation) cellLocation).getCid();
        } else if (cellLocation instanceof CdmaCellLocation) {
            CdmaCellLocation location = (CdmaCellLocation) cellLocation;
            try {
                try {
                    cid = ((Integer) location.getClass().getMethod("getLteCellId", new Class[0]).invoke(location, new Object[0])).intValue();
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        }
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "getCid:" + cid);
        return cid;
    }

    /* access modifiers changed from: private */
    public long getNrCid(int phoneId) {
        long nrCid = 0;
        CellLocation cellLocation = this.mPdnController.getCellLocation(phoneId, false);
        if (cellLocation instanceof GsmCellLocation) {
            GsmCellLocation location = (GsmCellLocation) cellLocation;
            try {
                try {
                    nrCid = ((Long) location.getClass().getMethod("getNrCid", new Class[0]).invoke(location, new Object[0])).longValue();
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        } else if (cellLocation instanceof CdmaCellLocation) {
            IMSLog.i(LOG_TAG, "getNrCid(),  CdmaCellloaction...");
        }
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "getNrCid:" + nrCid);
        return nrCid;
    }

    /* access modifiers changed from: protected */
    public void queryCellInfoForQualcomm() {
        IMSLog.i(LOG_TAG, "queryCellInfoForQualcomm");
        if (this.mTelephonyManager.getAllCellInfo() == null) {
            IMSLog.i(LOG_TAG, "cellInfo is null.");
        }
    }

    /* access modifiers changed from: private */
    public int getTac(int phoneId) {
        int tac = 0;
        CellLocation cellLocation = this.mPdnController.getCellLocation(phoneId, false);
        if (cellLocation instanceof GsmCellLocation) {
            tac = ((GsmCellLocation) cellLocation).getLac();
        } else if (cellLocation instanceof CdmaCellLocation) {
            CdmaCellLocation location = (CdmaCellLocation) cellLocation;
            try {
                try {
                    tac = ((Integer) location.getClass().getMethod("getLteTac", new Class[0]).invoke(location, new Object[0])).intValue();
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        }
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "getTac:" + tac);
        return tac;
    }

    public void setLkcForLastPani(int phoneId, String pani, ImsProfile profile, Date currentTime) {
        if (TextUtils.isEmpty(profile.getLastPaniHeader())) {
            String str = LOG_TAG;
            Log.i(str, "setLkcForLastPani: No Last PANI header for " + profile.getName());
        } else if (!isValidPani(pani, phoneId)) {
            Log.i(LOG_TAG, "setLkcForLastPani: current PANI is not valid!");
        } else {
            if (!pani.contains(PaniConstants.IWLAN_PANI_PREFIX)) {
                storeLastPani(phoneId, pani);
            } else {
                String underlyingCellularPani = this.mGenerator.generate(this.mTelephonyManager.getVoiceNetworkType(SimUtil.getSubId(phoneId)), phoneId);
                if (!isValidPani(underlyingCellularPani, phoneId)) {
                    Log.i(LOG_TAG, "setLkcForLastPani: underlyingCellularPani is not valid!");
                    return;
                }
                storeLastPani(phoneId, underlyingCellularPani);
            }
            String storedPani = getStoredLastPani(phoneId);
            if (needTimeStampForLastPani(profile) && !TextUtils.isEmpty(storedPani)) {
                String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
                Mno mno = Mno.fromName(profile.getMnoName());
                if (mno == Mno.TMOUS) {
                    pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
                } else if (mno == Mno.CELLC_SOUTHAFRICA) {
                    pattern = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
                }
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                storeLastPani(phoneId, storedPani + ";\"" + sdf.format(currentTime) + "\"");
            }
            String storedPani2 = getStoredLastPani(phoneId);
            ImsSharedPrefHelper.save(phoneId, this.mContext, ImsSharedPrefHelper.IMS_CONFIG, PLANI_PREF, storedPani2);
            String str2 = LOG_TAG;
            IMSLog.s(str2, phoneId, "setLkcForLastPani: " + storedPani2);
        }
    }

    public String getLastPani(int phoneId, ImsProfile profile, Date currentTime) {
        if (TextUtils.isEmpty(getStoredLastPani(phoneId))) {
            String sp = ImsSharedPrefHelper.getString(phoneId, this.mContext, ImsSharedPrefHelper.IMS_CONFIG, PLANI_PREF, (String) null);
            if (TextUtils.isEmpty(sp)) {
                return sp;
            }
            storeLastPani(phoneId, sp);
        }
        String plani = getStoredLastPani(phoneId);
        boolean needTimeStamp = needTimeStampForLastPani(profile);
        if (Mno.fromName(profile.getMnoName()) == Mno.TMOUS) {
            int subId = SimUtil.getSubId(phoneId);
            if (this.mTelephonyManager.getVoiceNetworkType(subId) != 0 && this.mTelephonyManager.getServiceStateForSubscriber(subId) == 0) {
                plani = plani.replaceAll(";\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*$", "");
                needTimeStamp = false;
            }
        }
        if (needTimeStamp) {
            String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
            Mno mno = Mno.fromName(profile.getMnoName());
            if (mno == Mno.TMOUS) {
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
            } else if (mno == Mno.CELLC_SOUTHAFRICA) {
                pattern = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
            plani = plani + ";\"" + sdf.format(currentTime) + "\"";
        }
        IMSLog.s(LOG_TAG, phoneId, "getLastPani: " + plani);
        return plani;
    }

    /* access modifiers changed from: protected */
    public boolean isValidPani(String pani, int phoneId) {
        if (TextUtils.isEmpty(pani)) {
            return false;
        }
        int subId = SimUtil.getSubId(phoneId);
        int network = this.mTelephonyManager.getDataNetworkType(subId);
        int voiceSvcState = this.mTelephonyManager.getServiceStateForSubscriber(subId);
        int dataSvcState = this.mTelephonyManager.getDataServiceState(subId);
        String str = LOG_TAG;
        IMSLog.s(str, phoneId, "isValidPani: PANI [" + pani + "] network [" + network + "] voiceSvcState [" + voiceSvcState + "] dataSvcState [" + dataSvcState + "]");
        if (!pani.contains(PaniConstants.IWLAN_PANI_PREFIX)) {
            if ((network != 18 || voiceSvcState == 0) && (network == 18 || dataSvcState == 0)) {
                return true;
            }
            return false;
        } else if (voiceSvcState != 0) {
            return false;
        } else {
            return pani.contains("i-wlan-node-id=");
        }
    }

    /* access modifiers changed from: private */
    public String getWifiBssid() {
        WifiInfo wifiInfo;
        WifiManager wifiManager = (WifiManager) this.mContext.getApplicationContext().getSystemService("wifi");
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null) {
            return null;
        }
        String bssid = wifiInfo.getBSSID();
        String str = LOG_TAG;
        Log.i(str, "WifiManager.getBSSID(): [" + bssid + "]");
        return bssid;
    }

    private String getPrevioutPlani(int phoneId) {
        try {
            return this.mPrevLastPaniList.get(phoneId);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private String getStoredLastPani(int phoneId) {
        try {
            return this.mLastPaniList.get(phoneId);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private void setPrevioutPlani(int phoneId, String pani) {
        try {
            this.mPrevLastPaniList.set(phoneId, pani);
        } catch (IndexOutOfBoundsException e) {
            IMSLog.s(LOG_TAG, phoneId, "setPrevioutPlani: IndexOutOfBoundsException");
        }
    }

    private void storeLastPani(int phoneId, String pani) {
        try {
            this.mLastPaniList.set(phoneId, pani);
        } catch (IndexOutOfBoundsException e) {
            IMSLog.s(LOG_TAG, phoneId, "storeLastPani: IndexOutOfBoundsException");
        }
    }
}
