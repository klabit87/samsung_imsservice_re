package com.sec.internal.ims.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseDataConnectionState;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import com.sec.ims.ImsManager;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.extensions.ServiceStateExt;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.EmcBsIndication;
import com.sec.internal.constants.ims.os.NetworkState;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsPhoneStateManager;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.NetworkStateListener;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PdnController extends Handler implements IPdnController {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    private static final int EVENT_DEFAULT_NETWORK_CHANGED = 110;
    private static final int EVENT_EPDG_CONNECTION_CHANGED = 104;
    private static final int EVENT_EPDG_IKEERROR = 109;
    private static final int EVENT_LINKPROPERTIES_CHANGED = 111;
    private static final int EVENT_PDN_CONNECTED = 108;
    private static final int EVENT_PDN_DISCONNECTED = 103;
    private static final int EVENT_REQUEST_NETWORK = 101;
    private static final int EVENT_REQUEST_STOP_PDN = 107;
    private static final int EVENT_STOP_PDN_COMPLETED = 102;
    private static final int EVENT_WIFI_CONNECTED = 105;
    private static final int EVENT_WIFI_DISCONNECTED = 106;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = PdnController.class.getSimpleName();
    private static Map<Integer, Integer> mDataState = new HashMap();
    private final String ECC_IWLAN = "IWLAN";
    private final String PROPERTY_ECC_PATH = "ril.subtype";
    protected final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final ConnectivityManager.NetworkCallback mDefaultNetworkListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            String access$000 = PdnController.LOG_TAG;
            Log.i(access$000, "mDefaultNetworkListener: onAvailable network=" + network);
            Mno mno = SimUtil.getSimMno(PdnController.this.mDefaultPhoneId);
            if ((mno.isKor() || mno == Mno.TMOBILE) && !PdnController.this.hasMessages(110)) {
                PdnController.this.sendEmptyMessage(110);
            }
        }

        public void onLost(Network network) {
            String access$000 = PdnController.LOG_TAG;
            Log.i(access$000, "mDefaultNetworkListener: onLost network=" + network);
        }
    };
    /* access modifiers changed from: private */
    public int mDefaultPhoneId = 0;
    private String[] mEPDNintfName;
    private final ImsManager.EpdgListener mEpdgHandoverListener = makeEpdgHandoverListener();
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog;
    /* access modifiers changed from: private */
    public final IImsFramework mImsFramework;
    protected boolean mIsDisconnecting = false;
    /* access modifiers changed from: private */
    public boolean mNeedCellLocationUpdate = false;
    /* access modifiers changed from: private */
    public final Map<PdnEventListener, NetworkCallback> mNetworkCallbacks = new ArrayMap();
    /* access modifiers changed from: private */
    public final Set<NetworkStateListener> mNetworkStateListeners = new ArraySet();
    protected List<NetworkState> mNetworkStates = new ArrayList();
    private final Set<Pair<Pair<Integer, Integer>, PdnEventListener>> mPendingRequests = new ArraySet();
    private final List<PhoneStateListenerInternal> mPhoneStateListener = new ArrayList();
    private final ImsPhoneStateManager mPhoneStateManager;
    private ITelephonyManager mTelephonyManager = null;
    protected WfcEpdgManager mWfcEpdgMgr = null;
    private final ConnectivityManager.NetworkCallback mWifiStateListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            PdnController.this.sendEmptyMessage(105);
        }

        public void onLost(Network network) {
            PdnController.this.sendEmptyMessage(106);
        }
    };
    private List<? extends ISimManager> simmanagers;

    private ImsManager.EpdgListener makeEpdgHandoverListener() {
        return new ImsManager.EpdgListener() {
            public void onEpdgAvailable(int phoneId, int isAvailable, int wifiState) {
                NetworkState ns = PdnController.this.getNetworkState(phoneId);
                if (ns != null) {
                    boolean z = false;
                    boolean mIsAvailable = isAvailable == 1;
                    IMSLog.i(PdnController.LOG_TAG, phoneId, "onEpdgAvailable :  isAvailable : " + mIsAvailable + " wifiState : " + wifiState);
                    ns.setEpdgAvailable(mIsAvailable);
                    PdnController pdnController = PdnController.this;
                    if (!mIsAvailable) {
                        z = true;
                    }
                    pdnController.setPendedEPDGWeakSignal(phoneId, z);
                    IGeolocationController geolocationCon = PdnController.this.mImsFramework.getGeolocationController();
                    if (geolocationCon != null) {
                        geolocationCon.notifyEpdgAvailable(phoneId, isAvailable);
                    }
                }
            }

            public void onEpdgHandoverResult(int phoneId, int isL2WHandover, int result, String apnType) {
                boolean mResult = result == 1;
                String mDirection = isL2WHandover == 1 ? "LTE_TO_WLAN" : "WLAN_TO_LTE";
                String access$000 = PdnController.LOG_TAG;
                IMSLog.i(access$000, phoneId, "onEpdgHandoverResult :  Direction : " + mDirection + " result : " + mResult);
                if (mResult) {
                    PdnController.this.setPendedEPDGWeakSignal(phoneId, false);
                    PdnController pdnController = PdnController.this;
                    pdnController.sendMessage(pdnController.obtainMessage(104, phoneId, isL2WHandover, apnType));
                }
            }

            public void onEpdgDeregister(int phoneId) {
                IMSLog.i(PdnController.LOG_TAG, phoneId, "onEpdgDeregister");
                PdnController.this.setPendedEPDGWeakSignal(phoneId, true);
                PdnController.this.notifyEpdgRequest(phoneId, false, false);
            }

            public void onEpdgIpsecConnection(int phoneId, String apnType, int ikeError, int throttleCount) {
                String access$000 = PdnController.LOG_TAG;
                IMSLog.i(access$000, phoneId, "onEpdgIpsecConnection :  ikeError : " + ikeError + " apnType : " + apnType);
                if ((ikeError == 0) && PdnController.this.isWifiConnected()) {
                    PdnController.this.setPendedEPDGWeakSignal(phoneId, false);
                    PdnController pdnController = PdnController.this;
                    pdnController.sendMessage(pdnController.obtainMessage(104, phoneId, 1, apnType));
                } else if (ikeError == 24) {
                    PdnController pdnController2 = PdnController.this;
                    pdnController2.sendMessage(pdnController2.obtainMessage(109, Integer.valueOf(phoneId)));
                }
            }

            public void onEpdgIpsecDisconnection(int phoneId, String apnType) {
                String access$000 = PdnController.LOG_TAG;
                IMSLog.i(access$000, phoneId, "onEpdgIpsecDisconnection :  apnType : " + apnType);
                if (TextUtils.equals(apnType, DeviceConfigManager.IMS)) {
                    PdnController.this.notifyEpdgIpsecDisconnected(phoneId);
                }
            }

            public void onEpdgRegister(int phoneId, boolean cdmaAvailability) {
                IMSLog.i(PdnController.LOG_TAG, phoneId, "onEpdgRegister");
                PdnController.this.notifyEpdgRequest(phoneId, cdmaAvailability, true);
            }

            public void onEpdgShowPopup(int phoneId, int popupType) {
            }
        };
    }

    public boolean isPendedEPDGWeakSignal(int phoneId) {
        return getNetworkState(phoneId).isPendedEPDGWeakSignal();
    }

    public void setPendedEPDGWeakSignal(int phoneId, boolean status) {
        IMSLog.i(LOG_TAG, phoneId, "setPendedEPDGWeakSignal");
        if (status) {
            Mno mno = SimUtil.getSimMno(phoneId);
            if (mno != Mno.VZW && mno != Mno.ATT && mno != Mno.TMOUS) {
                return;
            }
            if (getNetworkState(phoneId) == null) {
                IMSLog.i(LOG_TAG, phoneId, "setPendedEPDGWeakSignal, networkState is not exist.");
            } else if (getNetworkState(phoneId).getDataRegState() == 1 || getNetworkState(phoneId).getDataRegState() == 3 || !(getNetworkState(phoneId).getDataNetworkType() == 13 || getNetworkState(phoneId).getDataNetworkType() == 14 || getNetworkState(phoneId).getDataRegState() != 0)) {
                IMSLog.i(LOG_TAG, phoneId, "VzW/ATT/TMOUS : LOST_LTE_WIFI_CONNECTION:12");
                getNetworkState(phoneId).setPendedEpdgWeakSignal(true);
            }
        } else {
            getNetworkState(phoneId).setPendedEpdgWeakSignal(false);
        }
    }

    public boolean isEpsOnlyReg(int phoneId) {
        NetworkState ns = getNetworkState(phoneId);
        return ns.isPsOnlyReg() && (ns.getDataNetworkType() == 13 || ns.getDataNetworkType() == 20);
    }

    public boolean hasEmergencyServiceOnly(int phoneId) {
        return this.mTelephonyManager.getDataServiceState(SimUtil.getSubId(phoneId)) != 0;
    }

    public VoPsIndication getVopsIndication(int phoneId) {
        return getNetworkState(phoneId).getVopsIndication();
    }

    public boolean isVoiceRoaming(int phoneId) {
        return getNetworkState(phoneId).isVoiceRoaming();
    }

    public boolean isDataRoaming(int phoneId) {
        return getNetworkState(phoneId).isDataRoaming();
    }

    public int getVoiceRegState(int phoneId) {
        return getNetworkState(phoneId).getVoiceRegState();
    }

    public int getMobileDataRegState(int phoneId) {
        return getNetworkState(phoneId).getMobileDataRegState();
    }

    public boolean isPsOnlyReg(int phoneId) {
        return getNetworkState(phoneId).isPsOnlyReg();
    }

    public EmcBsIndication getEmcBsIndication(int phoneId) {
        return getNetworkState(phoneId).getEmcBsIndication();
    }

    public CellLocation getCellLocation(int phoneId, boolean forceSync) {
        CellLocation cl = getNetworkState(phoneId).getCellLocation();
        if (cl != null && !cl.isEmpty() && !forceSync && !this.mNeedCellLocationUpdate) {
            return cl;
        }
        int subId = SimUtil.getSubId(phoneId);
        CellLocation cl2 = this.mTelephonyManager.getCellLocationBySubId(subId);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "get latest celllocation and store, subId = " + subId);
        getNetworkState(phoneId).setCellLocation(cl2);
        this.mNeedCellLocationUpdate = false;
        return cl2;
    }

    public boolean isInternationalRoaming(int phoneId) {
        return getNetworkState(phoneId).isInternationalRoaming();
    }

    public NetworkState getNetworkState(int phoneId) {
        for (NetworkState ns : this.mNetworkStates) {
            if (ns.getSimSlot() == phoneId) {
                return ns;
            }
        }
        IMSLog.e(LOG_TAG, phoneId, "NetworkState is not exist. Return null..");
        return null;
    }

    public void resetNetworkState(int phoneId) {
        NetworkState ns = getNetworkState(phoneId);
        if (ns != null) {
            ns.setDataNetworkType(0);
            ns.setMobileDataNetworkType(0);
            ns.setDataRegState(1);
            ns.setVoiceRegState(1);
            ns.setMobileDataRegState(1);
            ns.setSnapshotState(ServiceStateExt.SNAPSHOT_STATUS_DEACTIVATED);
            ns.setLastRequestedNetworkType(0);
            ns.setCellLocation(CellLocation.getEmpty());
        }
    }

    private class PhoneStateListenerInternal extends PhoneStateListener {
        int mInternalSimSlot;

        public PhoneStateListenerInternal(int simSlot) {
            this.mInternalSimSlot = simSlot;
        }

        public int getInternalSimSlot() {
            return this.mInternalSimSlot;
        }

        /* JADX WARNING: Removed duplicated region for block: B:51:0x0217  */
        /* JADX WARNING: Removed duplicated region for block: B:52:0x023c  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x02a7  */
        /* JADX WARNING: Removed duplicated region for block: B:57:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onServiceStateChanged(android.telephony.ServiceState r22) {
            /*
                r21 = this;
                r0 = r21
                r1 = r22
                com.sec.internal.ims.core.PdnController r2 = com.sec.internal.ims.core.PdnController.this
                int r3 = r0.mInternalSimSlot
                com.sec.internal.constants.ims.os.NetworkState r2 = r2.getNetworkState(r3)
                com.sec.internal.helper.os.ServiceStateWrapper r3 = new com.sec.internal.helper.os.ServiceStateWrapper
                r3.<init>(r1)
                java.util.ArrayList r4 = new java.util.ArrayList
                r4.<init>()
                r5 = 0
                int r6 = r3.getDataRegState()
                r8 = 1
                r9 = 2
                if (r6 == r9) goto L_0x0024
                if (r6 != r8) goto L_0x0022
                goto L_0x0024
            L_0x0022:
                r9 = 0
                goto L_0x0025
            L_0x0024:
                r9 = r8
            L_0x0025:
                boolean r10 = r2.isEmergencyOnly()
                java.lang.String r11 = "=>"
                if (r9 == r10) goto L_0x0056
                java.lang.StringBuilder r10 = new java.lang.StringBuilder
                r10.<init>()
                java.lang.String r12 = "EmergencyOnlyReg:"
                r10.append(r12)
                boolean r12 = r2.isEmergencyOnly()
                r10.append(r12)
                r10.append(r11)
                r10.append(r9)
                java.lang.String r10 = r10.toString()
                r4.add(r10)
                com.sec.internal.ims.core.PdnController r10 = com.sec.internal.ims.core.PdnController.this
                int r12 = r0.mInternalSimSlot
                com.sec.internal.constants.ims.os.NetworkState r10 = r10.getNetworkState(r12)
                r10.setEmergencyOnly(r9)
            L_0x0056:
                int r10 = r3.getLteImsVoiceAvail()
                com.sec.internal.constants.ims.os.VoPsIndication r10 = com.sec.internal.constants.ims.os.VoPsIndication.translateVops((int) r10)
                com.sec.internal.constants.ims.os.VoPsIndication r12 = r2.getVopsIndication()
                if (r10 == r12) goto L_0x0086
                java.lang.StringBuilder r12 = new java.lang.StringBuilder
                r12.<init>()
                java.lang.String r13 = "VoPS:"
                r12.append(r13)
                com.sec.internal.constants.ims.os.VoPsIndication r13 = r2.getVopsIndication()
                r12.append(r13)
                r12.append(r11)
                r12.append(r10)
                java.lang.String r12 = r12.toString()
                r4.add(r12)
                r2.setVopsIndication(r10)
                r5 = 1
            L_0x0086:
                int r12 = r3.getLteIsEbSupported()
                com.sec.internal.constants.ims.os.EmcBsIndication r12 = com.sec.internal.constants.ims.os.EmcBsIndication.translateEmcbs(r12)
                int r13 = r3.getRilVoiceRadioTechnology()
                r14 = 14
                if (r13 != r14) goto L_0x0097
                goto L_0x00a2
            L_0x0097:
                int r13 = r3.getRilVoiceRadioTechnology()
                if (r13 != 0) goto L_0x00a0
                com.sec.internal.constants.ims.os.EmcBsIndication r12 = com.sec.internal.constants.ims.os.EmcBsIndication.UNKNOWN
                goto L_0x00a2
            L_0x00a0:
                com.sec.internal.constants.ims.os.EmcBsIndication r12 = com.sec.internal.constants.ims.os.EmcBsIndication.NOT_SUPPORTED
            L_0x00a2:
                com.sec.internal.constants.ims.os.EmcBsIndication r13 = r2.getEmcBsIndication()
                if (r13 == r12) goto L_0x00c9
                java.lang.StringBuilder r13 = new java.lang.StringBuilder
                r13.<init>()
                java.lang.String r14 = "EmcBsIndi:"
                r13.append(r14)
                com.sec.internal.constants.ims.os.EmcBsIndication r14 = r2.getEmcBsIndication()
                r13.append(r14)
                r13.append(r11)
                r13.append(r12)
                java.lang.String r13 = r13.toString()
                r4.add(r13)
                r2.setEmcBsIndication(r12)
            L_0x00c9:
                int r13 = r3.getVoiceRegState()
                int r14 = r2.getVoiceRegState()
                if (r14 == r13) goto L_0x00f5
                java.lang.StringBuilder r14 = new java.lang.StringBuilder
                r14.<init>()
                java.lang.String r15 = "VoiceReg:"
                r14.append(r15)
                int r15 = r2.getVoiceRegState()
                r14.append(r15)
                r14.append(r11)
                r14.append(r13)
                java.lang.String r14 = r14.toString()
                r4.add(r14)
                r2.setVoiceRegState(r13)
                r5 = 1
            L_0x00f5:
                int r14 = r3.getVoiceNetworkType()
                int r15 = r2.getVoiceNetworkType()
                if (r15 == r14) goto L_0x0121
                java.lang.StringBuilder r15 = new java.lang.StringBuilder
                r15.<init>()
                java.lang.String r7 = "VoiceNet:"
                r15.append(r7)
                int r7 = r2.getVoiceNetworkType()
                r15.append(r7)
                r15.append(r11)
                r15.append(r14)
                java.lang.String r7 = r15.toString()
                r4.add(r7)
                r2.setVoiceNetworkType(r14)
                r5 = 1
            L_0x0121:
                boolean r7 = r3.isPsOnlyReg()
                boolean r15 = r2.isPsOnlyReg()
                if (r7 == r15) goto L_0x014d
                java.lang.StringBuilder r15 = new java.lang.StringBuilder
                r15.<init>()
                java.lang.String r8 = "PsOnly:"
                r15.append(r8)
                boolean r8 = r2.isPsOnlyReg()
                r15.append(r8)
                r15.append(r11)
                r15.append(r7)
                java.lang.String r8 = r15.toString()
                r4.add(r8)
                r2.setPsOnlyReg(r7)
                r5 = 1
            L_0x014d:
                int r8 = r3.getVoiceRoamingType()
                r15 = 3
                if (r8 != r15) goto L_0x0159
                r8 = 1
                r2.setInternationalRoaming(r8)
                goto L_0x015d
            L_0x0159:
                r8 = 0
                r2.setInternationalRoaming(r8)
            L_0x015d:
                boolean r8 = r3.getDataRoaming()
                boolean r15 = r2.isDataRoaming()
                if (r8 == r15) goto L_0x018c
                java.lang.StringBuilder r15 = new java.lang.StringBuilder
                r15.<init>()
                r16 = r5
                java.lang.String r5 = "DataRoaming:"
                r15.append(r5)
                boolean r5 = r2.isDataRoaming()
                r15.append(r5)
                r15.append(r11)
                r15.append(r8)
                java.lang.String r5 = r15.toString()
                r4.add(r5)
                r2.setDataRaoming(r8)
                r5 = 1
                goto L_0x018e
            L_0x018c:
                r16 = r5
            L_0x018e:
                boolean r15 = r3.getVoiceRoaming()
                r16 = r5
                boolean r5 = r2.isVoiceRoaming()
                if (r15 == r5) goto L_0x01bf
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                r17 = r7
                java.lang.String r7 = "VoiceRoaming:"
                r5.append(r7)
                boolean r7 = r2.isVoiceRoaming()
                r5.append(r7)
                r5.append(r11)
                r5.append(r15)
                java.lang.String r5 = r5.toString()
                r4.add(r5)
                r2.setVoiceRoaming(r15)
                r5 = 1
                goto L_0x01c3
            L_0x01bf:
                r17 = r7
                r5 = r16
            L_0x01c3:
                java.lang.String r7 = r3.getOperatorNumeric()
                boolean r16 = android.text.TextUtils.isEmpty(r7)
                if (r16 != 0) goto L_0x0201
                r16 = r5
                java.lang.String r5 = r2.getOperatorNumeric()
                boolean r5 = android.text.TextUtils.equals(r5, r7)
                if (r5 != 0) goto L_0x01fe
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                r18 = r8
                java.lang.String r8 = "Operator:"
                r5.append(r8)
                java.lang.String r8 = r2.getOperatorNumeric()
                r5.append(r8)
                r5.append(r11)
                r5.append(r7)
                java.lang.String r5 = r5.toString()
                r4.add(r5)
                r2.setOperatorNumeric(r7)
                r5 = 1
                goto L_0x0207
            L_0x01fe:
                r18 = r8
                goto L_0x0205
            L_0x0201:
                r16 = r5
                r18 = r8
            L_0x0205:
                r5 = r16
            L_0x0207:
                com.sec.internal.ims.core.PdnController r8 = com.sec.internal.ims.core.PdnController.this
                r16 = r5
                int r5 = r0.mInternalSimSlot
                boolean r5 = r8.isMobileDataConnected(r5)
                boolean r8 = r2.isDataConnectedState()
                if (r5 == r8) goto L_0x023c
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                r8.<init>()
                r19 = r7
                java.lang.String r7 = "DataConnState:"
                r8.append(r7)
                boolean r7 = r2.isDataConnectedState()
                r8.append(r7)
                r8.append(r11)
                r8.append(r5)
                java.lang.String r7 = r8.toString()
                r4.add(r7)
                r2.setDataConnectionState(r5)
                r7 = 1
                goto L_0x0240
            L_0x023c:
                r19 = r7
                r7 = r16
            L_0x0240:
                int r8 = r3.getRilMobileDataRadioTechnology()
                int r8 = com.sec.internal.helper.os.ServiceStateWrapper.rilRadioTechnologyToNetworkType(r8)
                r2.setMobileDataNetworkType(r8)
                int r8 = r3.getMobileDataRegState()
                r2.setMobileDataRegState(r8)
                java.lang.String r8 = com.sec.internal.ims.core.PdnController.LOG_TAG
                int r11 = r0.mInternalSimSlot
                r16 = r2
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                r20 = r5
                java.lang.String r5 = "onServiceStateChanged: state="
                r2.append(r5)
                r2.append(r1)
                java.lang.String r5 = "Changed="
                r2.append(r5)
                java.lang.String r5 = ", "
                java.lang.String r5 = java.lang.String.join(r5, r4)
                r2.append(r5)
                java.lang.String r2 = r2.toString()
                com.sec.internal.log.IMSLog.i(r8, r11, r2)
                com.sec.internal.ims.core.PdnController r2 = com.sec.internal.ims.core.PdnController.this
                boolean unused = r2.mNeedCellLocationUpdate = r7
                com.sec.internal.ims.core.PdnController r2 = com.sec.internal.ims.core.PdnController.this
                int r5 = r3.getDataNetworkType()
                int r8 = r0.mInternalSimSlot
                r2.notifyDataConnectionState(r5, r6, r7, r8)
                com.sec.internal.ims.core.PdnController r2 = com.sec.internal.ims.core.PdnController.this
                int r5 = r3.getSnapshotStatus()
                int r8 = r0.mInternalSimSlot
                r2.notifySnapshotState(r5, r8)
                com.sec.internal.ims.core.PdnController r2 = com.sec.internal.ims.core.PdnController.this
                com.sec.internal.interfaces.ims.IImsFramework r2 = r2.mImsFramework
                com.sec.internal.interfaces.ims.core.IGeolocationController r2 = r2.getGeolocationController()
                if (r2 == 0) goto L_0x02ac
                int r5 = r0.mInternalSimSlot
                r2.notifyServiceStateChanged(r5, r3)
            L_0x02ac:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.PhoneStateListenerInternal.onServiceStateChanged(android.telephony.ServiceState):void");
        }

        public void onCellLocationChanged(CellLocation location) {
            NetworkState ns = PdnController.this.getNetworkState(this.mInternalSimSlot);
            String access$000 = PdnController.LOG_TAG;
            int i = this.mInternalSimSlot;
            StringBuilder sb = new StringBuilder();
            sb.append("onCellLocationChanged: ns=");
            sb.append(ns != null ? "not null" : "null");
            IMSLog.i(access$000, i, sb.toString());
            if (location != null || ns == null || ns.getCellLocation() != null) {
                if (ns != null) {
                    ns.setCellLocation(location);
                }
                for (NetworkStateListener listener : PdnController.this.mNetworkStateListeners) {
                    listener.onCellLocationChanged(location, this.mInternalSimSlot);
                }
            }
        }

        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) {
            String access$000 = PdnController.LOG_TAG;
            int i = this.mInternalSimSlot;
            IMSLog.i(access$000, i, "onPreciseDataConnectionStateChanged: state=" + dataConnectionState);
            for (NetworkStateListener listener : PdnController.this.mNetworkStateListeners) {
                listener.onPreciseDataConnectionStateChanged(this.mInternalSimSlot, dataConnectionState);
            }
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            String access$000 = PdnController.LOG_TAG;
            int i = this.mInternalSimSlot;
            IMSLog.s(access$000, i, "onDataConnectionStateChanged: state " + state + ", networkType " + networkType);
            PdnController.this.setDataState(this.mInternalSimSlot, state);
        }
    }

    /* access modifiers changed from: private */
    public boolean isMobileDataConnected(int phoneId) {
        boolean mobilecConnected = false;
        if (phoneId == SimUtil.getDefaultPhoneId()) {
            ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            Network[] netInfo = cm.getAllNetworks();
            int length = netInfo.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                NetworkInfo ni = cm.getNetworkInfo(netInfo[i]);
                if (ni == null || ((ni.getType() != 11 || !ni.isConnected()) && (ni.getType() != 0 || !ni.isConnected()))) {
                    i++;
                }
            }
            mobilecConnected = true;
        }
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "Default Phone Id = :" + SimUtil.getDefaultPhoneId() + "MobileConnected=" + mobilecConnected);
        return mobilecConnected;
    }

    /* access modifiers changed from: protected */
    public List<String> readPcscfFromLinkProperties(LinkPropertiesWrapper lp) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("readPcscfFromLinkProperties: lp=");
        sb.append(lp == null ? "null" : "not null");
        Log.i(str, sb.toString());
        List<String> ret = new ArrayList<>();
        if (lp == null) {
            return ret;
        }
        List<InetAddress> pcscfServers = lp.getPcscfServers();
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) pcscfServers)) {
            for (InetAddress ia : pcscfServers) {
                String addr = ia.getHostAddress();
                if (!TextUtils.isEmpty(addr) && !"0.0.0.0".equals(addr) && !"0:0:0:0:0:0:0:0".equals(addr) && !"::".equals(addr)) {
                    String str2 = LOG_TAG;
                    Log.i(str2, "pcscfList: " + ia.getHostAddress());
                    ret.add(ia.getHostAddress());
                }
            }
        }
        return ret;
    }

    protected class PdnConnectedEvent {
        /* access modifiers changed from: private */
        public PdnEventListener mListener;
        /* access modifiers changed from: private */
        public Network mNetwork;

        public PdnConnectedEvent(PdnEventListener listener, Network network) {
            this.mListener = listener;
            this.mNetwork = network;
        }
    }

    protected class LinkpropertiesChangedEvent {
        /* access modifiers changed from: private */
        public LinkProperties mLinkProperties;
        /* access modifiers changed from: private */
        public PdnEventListener mListener;

        public LinkpropertiesChangedEvent(PdnEventListener listener, LinkProperties linkProperties) {
            this.mListener = listener;
            this.mLinkProperties = linkProperties;
        }
    }

    private class NetworkCallback extends ConnectivityManager.NetworkCallback {
        private static final int LOCAL_IP_CHANGED = 1;
        private static final int LOCAL_STACKED_IP_CHANGED = 2;
        private boolean mDisconnectRequested = false;
        /* access modifiers changed from: private */
        public LinkPropertiesWrapper mLinkProperties = new LinkPropertiesWrapper();
        /* access modifiers changed from: private */
        public final PdnEventListener mListener;
        /* access modifiers changed from: private */
        public Network mNetwork = null;
        /* access modifiers changed from: private */
        public final int mNetworkType;
        /* access modifiers changed from: private */
        public int mPhoneId = 0;
        /* access modifiers changed from: private */
        public boolean mSuspended = false;

        public NetworkCallback(int networkType, PdnEventListener listener, int phoneId) {
            this.mListener = listener;
            this.mNetworkType = networkType;
            this.mPhoneId = phoneId;
        }

        public void setDisconnectRequested() {
            this.mDisconnectRequested = true;
        }

        public boolean isDisconnectRequested() {
            return this.mDisconnectRequested;
        }

        public void onAvailable(Network network) {
            if (!SimUtil.getSimMno(this.mPhoneId).isRjil() || this.mNetworkType != 15 || !PdnController.this.mIsDisconnecting || PdnController.this.isNetworkRequested(this.mListener)) {
                PdnConnectedEvent event = new PdnConnectedEvent(this.mListener, network);
                PdnController pdnController = PdnController.this;
                pdnController.sendMessage(pdnController.obtainMessage(108, this.mNetworkType, this.mPhoneId, event));
                return;
            }
            String access$000 = PdnController.LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(access$000, i, "ignore onAvailable: network " + network);
        }

        public void onLost(Network network) {
            SimpleEventLog access$1000 = PdnController.this.mEventLog;
            int i = this.mPhoneId;
            access$1000.logAndAdd(i, "onLost" + network + " " + PdnController.this);
            NetworkInfo ni = PdnController.this.mConnectivityManager.getNetworkInfo(network);
            if (ni != null) {
                String access$000 = PdnController.LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(access$000, i2, "onLost: " + ni.toString());
            }
            PdnController pdnController = PdnController.this;
            pdnController.sendMessage(pdnController.obtainMessage(103, this.mNetworkType, this.mPhoneId, this.mListener));
        }

        public void onLosing(Network network, int maxMsToLive) {
            String access$000 = PdnController.LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(access$000, i, "onLosing: network " + network + " maxMsToLive " + maxMsToLive);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0067, code lost:
            if (r0 == null) goto L_0x006d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x006b, code lost:
            if (r0.mNetwork != null) goto L_0x007a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x006d, code lost:
            android.util.Log.i(com.sec.internal.ims.core.PdnController.access$000(), "onLinkPropertiesChanged: null callback");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0076, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onLinkPropertiesChanged(android.net.Network r6, android.net.LinkProperties r7) {
            /*
                r5 = this;
                java.lang.String r0 = com.sec.internal.ims.core.PdnController.LOG_TAG
                int r1 = r5.mPhoneId
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "onLinkPropertiesChanged: network "
                r2.append(r3)
                r2.append(r6)
                java.lang.String r3 = " lp "
                r2.append(r3)
                r2.append(r7)
                java.lang.String r3 = " old "
                r2.append(r3)
                com.sec.internal.helper.os.LinkPropertiesWrapper r3 = r5.mLinkProperties
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                com.sec.internal.log.IMSLog.i(r0, r1, r2)
                int r0 = r5.mPhoneId
                com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)
                boolean r0 = r0.isKor()
                if (r0 == 0) goto L_0x007a
                r0 = 0
                com.sec.internal.ims.core.PdnController r1 = com.sec.internal.ims.core.PdnController.this
                java.util.Map r1 = r1.mNetworkCallbacks
                monitor-enter(r1)
                com.sec.internal.ims.core.PdnController r2 = com.sec.internal.ims.core.PdnController.this     // Catch:{ all -> 0x0077 }
                java.util.Map r2 = r2.mNetworkCallbacks     // Catch:{ all -> 0x0077 }
                boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0077 }
                if (r2 == 0) goto L_0x0057
                java.lang.String r2 = com.sec.internal.ims.core.PdnController.LOG_TAG     // Catch:{ all -> 0x0077 }
                java.lang.String r3 = "onLinkPropertiesChanged: No callback exists"
                android.util.Log.i(r2, r3)     // Catch:{ all -> 0x0077 }
                monitor-exit(r1)     // Catch:{ all -> 0x0077 }
                return
            L_0x0057:
                com.sec.internal.ims.core.PdnController r2 = com.sec.internal.ims.core.PdnController.this     // Catch:{ all -> 0x0077 }
                java.util.Map r2 = r2.mNetworkCallbacks     // Catch:{ all -> 0x0077 }
                com.sec.internal.interfaces.ims.core.PdnEventListener r3 = r5.mListener     // Catch:{ all -> 0x0077 }
                java.lang.Object r2 = r2.get(r3)     // Catch:{ all -> 0x0077 }
                com.sec.internal.ims.core.PdnController$NetworkCallback r2 = (com.sec.internal.ims.core.PdnController.NetworkCallback) r2     // Catch:{ all -> 0x0077 }
                r0 = r2
                monitor-exit(r1)     // Catch:{ all -> 0x0077 }
                if (r0 == 0) goto L_0x006d
                android.net.Network r1 = r0.mNetwork
                if (r1 != 0) goto L_0x007a
            L_0x006d:
                java.lang.String r1 = com.sec.internal.ims.core.PdnController.LOG_TAG
                java.lang.String r2 = "onLinkPropertiesChanged: null callback"
                android.util.Log.i(r1, r2)
                return
            L_0x0077:
                r2 = move-exception
                monitor-exit(r1)     // Catch:{ all -> 0x0077 }
                throw r2
            L_0x007a:
                com.sec.internal.ims.core.PdnController$LinkpropertiesChangedEvent r0 = new com.sec.internal.ims.core.PdnController$LinkpropertiesChangedEvent
                com.sec.internal.ims.core.PdnController r1 = com.sec.internal.ims.core.PdnController.this
                com.sec.internal.interfaces.ims.core.PdnEventListener r2 = r5.mListener
                r0.<init>(r2, r7)
                com.sec.internal.ims.core.PdnController r1 = com.sec.internal.ims.core.PdnController.this
                r2 = 111(0x6f, float:1.56E-43)
                int r3 = r5.mNetworkType
                int r4 = r5.mPhoneId
                android.os.Message r2 = r1.obtainMessage(r2, r3, r4, r0)
                r1.sendMessage(r2)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.NetworkCallback.onLinkPropertiesChanged(android.net.Network, android.net.LinkProperties):void");
        }

        public void onNetworkSuspended(Network network) {
            IMSLog.i(PdnController.LOG_TAG, this.mPhoneId, "suspend!");
            this.mSuspended = true;
            this.mListener.onSuspended(this.mNetworkType);
        }

        public void onNetworkResumed(Network network) {
            IMSLog.i(PdnController.LOG_TAG, this.mPhoneId, "resume!");
            this.mSuspended = false;
            this.mListener.onResumed(this.mNetworkType);
        }

        /* access modifiers changed from: private */
        public int isLocalIpChanged(LinkPropertiesWrapper linkProperties) {
            List<InetAddress> oldAddresses = PdnController.this.filterAddresses(this.mLinkProperties.getAddresses());
            List<InetAddress> newAddresses = PdnController.this.filterAddresses(linkProperties.getAddresses());
            if (oldAddresses == null || newAddresses == null || (oldAddresses.size() == 0 && newAddresses.size() == 0)) {
                return 0;
            }
            if (!this.mLinkProperties.isIdenticalInterfaceName(linkProperties) || oldAddresses.size() != newAddresses.size() || !oldAddresses.containsAll(newAddresses)) {
                return 1;
            }
            List<InetAddress> oldAddresses2 = PdnController.this.filterAddresses(this.mLinkProperties.getAllAddresses());
            List<InetAddress> newAddresses2 = PdnController.this.filterAddresses(linkProperties.getAllAddresses());
            if (oldAddresses2.size() != newAddresses2.size() || !oldAddresses2.containsAll(newAddresses2)) {
                return 2;
            }
            return 0;
        }

        /* access modifiers changed from: private */
        public boolean isPcscfAddressChanged(LinkPropertiesWrapper linkProperties) {
            List<InetAddress> oldPcscfAddresses = PdnController.this.filterAddresses(this.mLinkProperties.getPcscfServers());
            List<InetAddress> newPcscfAddresses = PdnController.this.filterAddresses(linkProperties.getPcscfServers());
            if (oldPcscfAddresses == null || newPcscfAddresses == null || (oldPcscfAddresses.size() == 0 && newPcscfAddresses.size() == 0)) {
                return false;
            }
            if (oldPcscfAddresses.size() != 0 && newPcscfAddresses.size() == 0) {
                return false;
            }
            if (!this.mLinkProperties.isIdenticalInterfaceName(linkProperties) || oldPcscfAddresses.size() != newPcscfAddresses.size() || !oldPcscfAddresses.containsAll(newPcscfAddresses)) {
                return true;
            }
            return false;
        }
    }

    public PdnController(Context context, Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = imsFramework;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        List<? extends ISimManager> allSimManagers = SimManagerFactory.getAllSimManagers();
        this.simmanagers = allSimManagers;
        int phoneCount = allSimManagers.size();
        this.mWfcEpdgMgr = ImsServiceStub.getInstance().getWfcEpdgManager();
        this.mEPDNintfName = new String[phoneCount];
        this.mDefaultPhoneId = SimUtil.getDefaultPhoneId();
        this.mPhoneStateManager = new ImsPhoneStateManager(this.mContext, 4177);
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 200);
    }

    protected PdnController(Context context) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        this.mPhoneStateManager = new ImsPhoneStateManager(this.mContext, 0);
        this.mImsFramework = null;
    }

    public void initSequentially() {
        for (ISimManager sm : this.simmanagers) {
            NetworkState ns = new NetworkState(sm.getSimSlotIndex());
            ns.setEmcBsIndication(EmcBsIndication.UNKNOWN);
            ns.setVopsIndication(VoPsIndication.UNKNOWN);
            ns.setOperatorNumeric("");
            ns.setCellLocation(CellLocation.getEmpty());
            this.mNetworkStates.add(ns);
            if (!SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) || sm.getSimSlotIndex() == SimUtil.getDefaultPhoneId()) {
                PhoneStateListenerInternal psli = new PhoneStateListenerInternal(sm.getSimSlotIndex());
                this.mPhoneStateListener.add(psli);
                this.mPhoneStateManager.registerListener(psli, sm.getSubscriptionId(), sm.getSimSlotIndex());
            } else {
                Log.i(LOG_TAG, "do not make PhoneStateListenerInternal with non-DDS slot");
            }
        }
        this.mWfcEpdgMgr.registerEpdgHandoverListener(this.mEpdgHandoverListener);
        this.mConnectivityManager.registerNetworkCallback(new NetworkRequest.Builder().addTransportType(1).addCapability(12).build(), this.mWifiStateListener);
        this.mConnectivityManager.registerDefaultNetworkCallback(this.mDefaultNetworkListener);
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        Log.i(str, "handleMessage: what " + msg.what);
        switch (msg.what) {
            case 101:
                requestNetwork(msg.arg1, msg.arg2, (PdnEventListener) msg.obj);
                return;
            case 102:
                onStopPdnCompleted();
                return;
            case 103:
                onPdnDisconnected(msg.arg1, msg.arg2, (PdnEventListener) msg.obj);
                return;
            case 104:
                int i = msg.arg1;
                String str2 = (String) msg.obj;
                boolean z = true;
                if (msg.arg2 != 1) {
                    z = false;
                }
                onEpdgConnected(i, str2, z);
                return;
            case 105:
                onWifiConnected();
                return;
            case 106:
                onWifiDisconnected();
                return;
            case 107:
                requestStopNetwork(msg.arg1, msg.arg2, (PdnEventListener) msg.obj);
                return;
            case 108:
                PdnConnectedEvent pdnEvent = (PdnConnectedEvent) msg.obj;
                onPdnConnected(msg.arg1, msg.arg2, pdnEvent.mListener, pdnEvent.mNetwork);
                return;
            case 109:
                onEpdgIkeError(msg.arg1);
                return;
            case 110:
                onDefaultNetworkChanged();
                return;
            case 111:
                LinkpropertiesChangedEvent lpEvent = (LinkpropertiesChangedEvent) msg.obj;
                onLinkPropertiesChanged(msg.arg1, lpEvent.mListener, lpEvent.mLinkProperties);
                return;
            default:
                return;
        }
    }

    private int getNetworkCapability(int network) {
        if (network == 11) {
            return 4;
        }
        if (network == 15) {
            return 10;
        }
        if (network != 27) {
            return 12;
        }
        return 9;
    }

    public void registerForNetworkState(NetworkStateListener listener) {
        this.mNetworkStateListeners.add(listener);
    }

    public void unregisterForNetworkState(NetworkStateListener listener) {
        this.mNetworkStateListeners.remove(listener);
    }

    public void registerPhoneStateListener(int phoneId) {
        int subId;
        boolean validPhoneId = !SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) || phoneId == SimUtil.getDefaultPhoneId();
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "registerPhoneStateListener: validPhoneId=" + validPhoneId);
        if (validPhoneId && (subId = SimUtil.getSubId(phoneId)) >= 0) {
            PhoneStateListenerInternal psli = new PhoneStateListenerInternal(phoneId);
            if (getPhoneStateListener(phoneId) == null) {
                this.mPhoneStateListener.add(psli);
            }
            this.mPhoneStateManager.registerListener(psli, subId, phoneId);
        }
    }

    public PhoneStateListenerInternal getPhoneStateListener(int phoneId) {
        for (PhoneStateListenerInternal psli : this.mPhoneStateListener) {
            if (psli.getInternalSimSlot() == phoneId) {
                return psli;
            }
        }
        IMSLog.i(LOG_TAG, phoneId, "getPhoneStateListener: psli is not exist.");
        return null;
    }

    public void unRegisterPhoneStateListener(int simSlot) {
        IMSLog.i(LOG_TAG, simSlot, "unRegisterPhoneStateListener:");
        this.mPhoneStateManager.unRegisterListener(simSlot);
        PhoneStateListenerInternal removeObj = getPhoneStateListener(simSlot);
        if (removeObj != null) {
            this.mPhoneStateListener.remove(removeObj);
            mDataState.remove(Integer.valueOf(simSlot));
        }
    }

    public int startPdnConnectivity(int networkType, PdnEventListener listener, int phoneId) {
        NetworkInfo ni = this.mConnectivityManager.getNetworkInfo(networkType);
        boolean networkAvailable = ni == null || ni.isAvailable();
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "startPdnConnectivity: networkType " + networkType + " networkAvailable=" + networkAvailable);
        if (!networkAvailable) {
            Log.i(LOG_TAG, "startPdnConnectivity: not available");
            return 2;
        }
        sendMessage(obtainMessage(101, networkType, phoneId, listener));
        return 1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0082, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0084, code lost:
        if (r13 != 1) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0086, code lost:
        r3 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0088, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0089, code lost:
        r4 = getNetworkCapability(r13);
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0096, code lost:
        if (com.sec.internal.helper.SimUtil.getSimMno(r14).isKor() == false) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0098, code lost:
        r8 = translateNetworkBearer(getDefaultNetworkBearer());
        r9 = getNetworkState(r14).getDataRegState();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ac, code lost:
        if (isDataRoaming(r14) != false) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b4, code lost:
        if (com.sec.internal.helper.NetworkUtil.isMobileDataOn(r12.mContext) == false) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bc, code lost:
        if (com.sec.internal.helper.NetworkUtil.isMobileDataPressed(r12.mContext) == false) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00be, code lost:
        if (r9 == 1) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ca, code lost:
        if (com.sec.internal.constants.ims.ImsConstants.SystemSettings.AIRPLANE_MODE.get(r12.mContext, 0) == com.sec.internal.constants.ims.ImsConstants.SystemSettings.AIRPLANE_MODE_ON) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00cc, code lost:
        if (r13 != 0) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ce, code lost:
        if (r8 != 1) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d0, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d1, code lost:
        if (r6 == false) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00d3, code lost:
        r4 = 8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00d5, code lost:
        r7 = r4;
        com.sec.internal.log.IMSLog.i(LOG_TAG, r14, "startPdnConnectivity: transport " + r3 + " capability " + r7 + " needRequestMobileNetwork " + r6);
        r8 = new android.net.NetworkRequest.Builder();
        r8.addTransportType(r3).addCapability(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0109, code lost:
        if (r3 != 0) goto L_0x0144;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x010b, code lost:
        r9 = com.sec.internal.helper.SimUtil.getSubId(r14);
        r10 = com.sec.internal.helper.SimUtil.getConfigDualIMS();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0119, code lost:
        if (com.sec.internal.constants.ims.core.SimConstants.DSDS_DI.equals(r10) != false) goto L_0x012f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0121, code lost:
        if (com.sec.internal.constants.ims.core.SimConstants.DSDA_DI.equals(r10) != false) goto L_0x012f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0129, code lost:
        if (com.sec.internal.constants.ims.core.SimConstants.DSDS_SI_DDS.equals(r10) == false) goto L_0x0130;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x012d, code lost:
        if (r13 != com.sec.ims.extensions.ConnectivityManagerExt.TYPE_MOBILE_XCAP) goto L_0x0130;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x012f, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0130, code lost:
        if (r9 <= 0) goto L_0x0144;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0132, code lost:
        if (r1 == false) goto L_0x0144;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0134, code lost:
        r8.setNetworkSpecifier(new android.net.TelephonyNetworkSpecifier.Builder().setSubscriptionId(r9).build());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0144, code lost:
        r1 = r8.build();
        r9 = new com.sec.internal.ims.core.PdnController.NetworkCallback(r12, r13, r15, r14);
        r12.mNetworkCallbacks.put(r15, r9);
        getNetworkState(r14).setLastRequestedNetworkType();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0159, code lost:
        if (r13 == 1) goto L_0x015d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x015b, code lost:
        if (r13 != 0) goto L_0x015f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x015d, code lost:
        if (r6 == false) goto L_0x0167;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        r12.mConnectivityManager.requestNetwork(r1, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0165, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0167, code lost:
        r12.mConnectivityManager.registerNetworkCallback(r1, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x016d, code lost:
        android.util.Log.e(LOG_TAG, r2.toString());
        r15.onNetworkRequestFail();
        r12.mNetworkCallbacks.remove(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestNetwork(final int r13, int r14, final com.sec.internal.interfaces.ims.core.PdnEventListener r15) {
        /*
            r12 = this;
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r0 = r12.mNetworkCallbacks
            java.lang.Object r0 = r0.get(r15)
            com.sec.internal.ims.core.PdnController$NetworkCallback r0 = (com.sec.internal.ims.core.PdnController.NetworkCallback) r0
            com.sec.internal.helper.SimpleEventLog r1 = r12.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "requestNetwork: networkType "
            r2.append(r3)
            r2.append(r13)
            java.lang.String r3 = ", callback="
            r2.append(r3)
            if (r0 != 0) goto L_0x0022
            java.lang.String r3 = "null"
            goto L_0x002a
        L_0x0022:
            int r3 = r0.mNetworkType
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
        L_0x002a:
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r14, r2)
            if (r0 == 0) goto L_0x005c
            int r1 = r0.mNetworkType
            if (r1 == r13) goto L_0x004d
            android.net.ConnectivityManager r1 = r12.mConnectivityManager     // Catch:{ IllegalArgumentException -> 0x0042 }
            r1.unregisterNetworkCallback(r0)     // Catch:{ IllegalArgumentException -> 0x0042 }
            goto L_0x004c
        L_0x0042:
            r1 = move-exception
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = r1.getMessage()
            android.util.Log.e(r2, r3)
        L_0x004c:
            goto L_0x005c
        L_0x004d:
            boolean r1 = r12.isConnected(r13, r15)
            if (r1 == 0) goto L_0x005b
            com.sec.internal.ims.core.PdnController$4 r1 = new com.sec.internal.ims.core.PdnController$4
            r1.<init>(r15, r13, r0)
            r12.post(r1)
        L_0x005b:
            return
        L_0x005c:
            java.util.Set<android.util.Pair<android.util.Pair<java.lang.Integer, java.lang.Integer>, com.sec.internal.interfaces.ims.core.PdnEventListener>> r1 = r12.mPendingRequests
            monitor-enter(r1)
            boolean r2 = r12.mIsDisconnecting     // Catch:{ all -> 0x017f }
            if (r2 == 0) goto L_0x0081
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x017f }
            java.lang.String r3 = "Wait until ongoing stop request done."
            android.util.Log.i(r2, r3)     // Catch:{ all -> 0x017f }
            java.util.Set<android.util.Pair<android.util.Pair<java.lang.Integer, java.lang.Integer>, com.sec.internal.interfaces.ims.core.PdnEventListener>> r2 = r12.mPendingRequests     // Catch:{ all -> 0x017f }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x017f }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r13)     // Catch:{ all -> 0x017f }
            android.util.Pair r3 = android.util.Pair.create(r3, r4)     // Catch:{ all -> 0x017f }
            android.util.Pair r3 = android.util.Pair.create(r3, r15)     // Catch:{ all -> 0x017f }
            r2.add(r3)     // Catch:{ all -> 0x017f }
            monitor-exit(r1)     // Catch:{ all -> 0x017f }
            return
        L_0x0081:
            monitor-exit(r1)     // Catch:{ all -> 0x017f }
            r1 = 0
            r2 = 1
            if (r13 != r2) goto L_0x0088
            r3 = r2
            goto L_0x0089
        L_0x0088:
            r3 = r1
        L_0x0089:
            int r4 = r12.getNetworkCapability(r13)
            com.sec.internal.constants.Mno r5 = com.sec.internal.helper.SimUtil.getSimMno(r14)
            r6 = 0
            boolean r7 = r5.isKor()
            if (r7 == 0) goto L_0x00d1
            int r7 = r12.getDefaultNetworkBearer()
            int r8 = r12.translateNetworkBearer(r7)
            com.sec.internal.constants.ims.os.NetworkState r9 = r12.getNetworkState(r14)
            int r9 = r9.getDataRegState()
            boolean r10 = r12.isDataRoaming(r14)
            if (r10 != 0) goto L_0x00d1
            android.content.Context r10 = r12.mContext
            boolean r10 = com.sec.internal.helper.NetworkUtil.isMobileDataOn(r10)
            if (r10 == 0) goto L_0x00d1
            android.content.Context r10 = r12.mContext
            boolean r10 = com.sec.internal.helper.NetworkUtil.isMobileDataPressed(r10)
            if (r10 == 0) goto L_0x00d1
            if (r9 == r2) goto L_0x00d1
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r10 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.AIRPLANE_MODE
            android.content.Context r11 = r12.mContext
            int r10 = r10.get(r11, r1)
            int r11 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.AIRPLANE_MODE_ON
            if (r10 == r11) goto L_0x00d1
            if (r13 != 0) goto L_0x00d1
            if (r8 != r2) goto L_0x00d1
            r6 = 1
        L_0x00d1:
            if (r6 == 0) goto L_0x00d5
            r4 = 8
        L_0x00d5:
            r7 = r4
            java.lang.String r8 = LOG_TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "startPdnConnectivity: transport "
            r9.append(r10)
            r9.append(r3)
            java.lang.String r10 = " capability "
            r9.append(r10)
            r9.append(r7)
            java.lang.String r10 = " needRequestMobileNetwork "
            r9.append(r10)
            r9.append(r6)
            java.lang.String r9 = r9.toString()
            com.sec.internal.log.IMSLog.i(r8, r14, r9)
            android.net.NetworkRequest$Builder r8 = new android.net.NetworkRequest$Builder
            r8.<init>()
            android.net.NetworkRequest$Builder r9 = r8.addTransportType(r3)
            r9.addCapability(r7)
            if (r3 != 0) goto L_0x0144
            int r9 = com.sec.internal.helper.SimUtil.getSubId(r14)
            java.lang.String r10 = com.sec.internal.helper.SimUtil.getConfigDualIMS()
            java.lang.String r11 = "DSDS_DI"
            boolean r11 = r11.equals(r10)
            if (r11 != 0) goto L_0x012f
            java.lang.String r11 = "DSDA_DI"
            boolean r11 = r11.equals(r10)
            if (r11 != 0) goto L_0x012f
            java.lang.String r11 = "DSDS_SI_DDS"
            boolean r11 = r11.equals(r10)
            if (r11 == 0) goto L_0x0130
            int r11 = com.sec.ims.extensions.ConnectivityManagerExt.TYPE_MOBILE_XCAP
            if (r13 != r11) goto L_0x0130
        L_0x012f:
            r1 = r2
        L_0x0130:
            if (r9 <= 0) goto L_0x0144
            if (r1 == 0) goto L_0x0144
            android.net.TelephonyNetworkSpecifier$Builder r11 = new android.net.TelephonyNetworkSpecifier$Builder
            r11.<init>()
            android.net.TelephonyNetworkSpecifier$Builder r11 = r11.setSubscriptionId(r9)
            android.net.TelephonyNetworkSpecifier r11 = r11.build()
            r8.setNetworkSpecifier(r11)
        L_0x0144:
            android.net.NetworkRequest r1 = r8.build()
            com.sec.internal.ims.core.PdnController$NetworkCallback r9 = new com.sec.internal.ims.core.PdnController$NetworkCallback
            r9.<init>(r13, r15, r14)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r10 = r12.mNetworkCallbacks
            r10.put(r15, r9)
            com.sec.internal.constants.ims.os.NetworkState r10 = r12.getNetworkState(r14)
            r10.setLastRequestedNetworkType()
            if (r13 == r2) goto L_0x015d
            if (r13 != 0) goto L_0x015f
        L_0x015d:
            if (r6 == 0) goto L_0x0167
        L_0x015f:
            android.net.ConnectivityManager r2 = r12.mConnectivityManager     // Catch:{ TooManyRequestsException | IllegalArgumentException -> 0x0165 }
            r2.requestNetwork(r1, r9)     // Catch:{ TooManyRequestsException | IllegalArgumentException -> 0x0165 }
            goto L_0x016c
        L_0x0165:
            r2 = move-exception
            goto L_0x016d
        L_0x0167:
            android.net.ConnectivityManager r2 = r12.mConnectivityManager     // Catch:{ TooManyRequestsException | IllegalArgumentException -> 0x0165 }
            r2.registerNetworkCallback(r1, r9)     // Catch:{ TooManyRequestsException | IllegalArgumentException -> 0x0165 }
        L_0x016c:
            goto L_0x017e
        L_0x016d:
            java.lang.String r10 = LOG_TAG
            java.lang.String r11 = r2.toString()
            android.util.Log.e(r10, r11)
            r15.onNetworkRequestFail()
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r10 = r12.mNetworkCallbacks
            r10.remove(r15)
        L_0x017e:
            return
        L_0x017f:
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x017f }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.requestNetwork(int, int, com.sec.internal.interfaces.ims.core.PdnEventListener):void");
    }

    private void requestStopNetwork(int network, int phoneId, PdnEventListener listener) {
        if (network == 15) {
            this.mEPDNintfName[phoneId] = null;
        }
        synchronized (this.mPendingRequests) {
            this.mPendingRequests.remove(Pair.create(Pair.create(Integer.valueOf(phoneId), Integer.valueOf(network)), listener));
        }
        synchronized (this.mNetworkCallbacks) {
            ConnectivityManager.NetworkCallback callback = this.mNetworkCallbacks.get(listener);
            SimpleEventLog simpleEventLog = this.mEventLog;
            StringBuilder sb = new StringBuilder();
            sb.append("requestStopNetwork: network ");
            sb.append(network);
            sb.append(", callback is ");
            sb.append(callback != null ? "exist" : "null");
            simpleEventLog.logAndAdd(phoneId, sb.toString());
            if (callback != null) {
                listener.onResumed(network);
                listener.onResumedBySnapshot(network);
                try {
                    this.mConnectivityManager.unregisterNetworkCallback(callback);
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, e.toString());
                }
                this.mNetworkCallbacks.remove(listener);
                this.mIsDisconnecting = true;
                removeMessages(102);
                PreciseAlarmManager.getInstance(this.mContext).sendMessageDelayed(getClass().getSimpleName(), obtainMessage(102), 1000);
            }
        }
        NetworkState ns = getNetworkState(phoneId);
        if (network == 11 && ns.isEpdgConnected()) {
            ns.setEpdgConnected(false);
            if (getNetworkState(phoneId).getDataNetworkType() != 18) {
                notifyDataConnectionState(getNetworkState(phoneId).getDataNetworkType(), getNetworkState(phoneId).getDataRegState(), true, phoneId);
            }
        } else if (network == 15 && ns.isEmergencyEpdgConnected()) {
            ns.setEmergencyEpdgConnected(false);
        }
    }

    private void onStopPdnCompleted() {
        synchronized (this.mPendingRequests) {
            this.mIsDisconnecting = false;
            for (Pair<Pair<Integer, Integer>, PdnEventListener> p : this.mPendingRequests) {
                requestNetwork(((Integer) ((Pair) p.first).second).intValue(), ((Integer) ((Pair) p.first).first).intValue(), (PdnEventListener) p.second);
            }
            this.mPendingRequests.clear();
        }
    }

    private void onPdnConnected(int networkType, int phoneId, PdnEventListener listener, Network network) {
        NetworkCallback callback;
        synchronized (this.mNetworkCallbacks) {
            callback = this.mNetworkCallbacks.get(listener);
        }
        if (callback == null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("ignore onPdnConnected: network " + network + " as requestStopNetwork preceded this");
            return;
        }
        LinkProperties lp = this.mConnectivityManager.getLinkProperties(network);
        if (lp == null || lp.getInterfaceName() == null) {
            IMSLog.i(LOG_TAG, phoneId, "onPdnConnected: linkProperties or interface name is null, wait for next onPdnConnected()");
            return;
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("onPdnConnected: networkType , network=" + network + ", " + lp.getInterfaceName() + ", " + this);
        Network unused = callback.mNetwork = network;
        NetworkInfo ni = this.mConnectivityManager.getNetworkInfo(network);
        if (ni != null) {
            boolean unused2 = callback.mSuspended = isSuspended(ni);
        }
        LinkPropertiesWrapper linkProperties = new LinkPropertiesWrapper(lp);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "onPdnConnected: " + ni + " mSuspended=" + callback.mSuspended + " link properties " + linkProperties);
        handleConnectedPdnType(networkType, phoneId, callback, linkProperties.getInterfaceName());
        if (callback.mLinkProperties.getInterfaceName() != null) {
            int type = callback.isLocalIpChanged(linkProperties);
            boolean z = true;
            if (type >= 1) {
                if (type != 2) {
                    z = false;
                }
                listener.onLocalIpChanged(networkType, z);
            }
            if (callback.isPcscfAddressChanged(linkProperties)) {
                listener.onPcscfAddressChanged(networkType, readPcscfFromLinkProperties(linkProperties));
            }
            LinkPropertiesWrapper unused3 = callback.mLinkProperties = linkProperties;
            return;
        }
        LinkPropertiesWrapper unused4 = callback.mLinkProperties = linkProperties;
        callback.mListener.onConnected(networkType, network);
    }

    private void handleConnectedPdnType(int networkType, int phoneId, NetworkCallback callback, String ifaceName) {
        NetworkState ns = getNetworkState(phoneId);
        if (networkType == 11 && callback.mLinkProperties.getInterfaceName() == null && ns.getLastRequestedNetworkType() == 18 && ns.getDataNetworkType() == 18) {
            if (!ns.isEpdgConnected()) {
                IMSLog.i(LOG_TAG, phoneId, "onPdnConnected epdg network for ims pdn");
                ns.setEpdgConnected(true);
                synchronized (this.mNetworkStateListeners) {
                    for (NetworkStateListener nslistener : this.mNetworkStateListeners) {
                        nslistener.onDataConnectionStateChanged(ns.getDataNetworkType(), true, phoneId);
                        nslistener.onEpdgConnected(phoneId);
                    }
                }
            }
        } else if (networkType == 15) {
            String eccPath = SemSystemProperties.get("ril.subtype", "");
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("eccPath : " + eccPath);
            if (eccPath.equalsIgnoreCase("IWLAN")) {
                ns.setEmergencyEpdgConnected(true);
            }
            this.mEPDNintfName[phoneId] = ifaceName;
            String str = LOG_TAG;
            IMSLog.i(str, phoneId, "handleConnectedPdnType: eccPath=" + eccPath + "mEPDNintfName : " + this.mEPDNintfName[phoneId]);
        }
        if (SimUtil.getSimMno(phoneId).isEur() && networkType == 11 && !ns.isEpdgConnected()) {
            notifyDataConnectionState(ns.getDataNetworkType(), ns.getDataRegState(), true, phoneId);
        }
    }

    private void onPdnDisconnected(int networkType, int phoneId, PdnEventListener listener) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onPdnDisconnected: networkType " + networkType);
        NetworkState ns = getNetworkState(phoneId);
        listener.onResumed(networkType);
        if (networkType == 11 && ns.isEpdgConnected()) {
            ns.setEpdgConnected(false);
            notifyDataConnectionState(getNetworkState(phoneId).getDataNetworkType(), getNetworkState(phoneId).getDataRegState(), true, phoneId);
        } else if (networkType == 15 && ns.isEmergencyEpdgConnected()) {
            ns.setEmergencyEpdgConnected(false);
        }
        if (networkType == 15) {
            this.mEPDNintfName[phoneId] = null;
        }
        synchronized (this.mNetworkCallbacks) {
            if (this.mNetworkCallbacks.containsKey(listener)) {
                listener.onDisconnected(networkType, isConnected(networkType, listener));
                NetworkCallback callback = this.mNetworkCallbacks.get(listener);
                LinkPropertiesWrapper unused = callback.mLinkProperties = new LinkPropertiesWrapper();
                Network unused2 = callback.mNetwork = null;
            }
        }
    }

    private void onLinkPropertiesChanged(int networkType, PdnEventListener listener, LinkProperties linkProperties) {
        NetworkCallback callback;
        synchronized (this.mNetworkCallbacks) {
            callback = this.mNetworkCallbacks.get(listener);
        }
        if (callback == null) {
            this.mEventLog.logAndAdd("ignore onLinkPropertiesChanged as requestStopNetwork preceded this");
            return;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onLinkPropertiesChanged: networkType=" + networkType + ", linkProperties=" + linkProperties);
        LinkPropertiesWrapper lp = new LinkPropertiesWrapper(linkProperties);
        int type = callback.isLocalIpChanged(lp);
        boolean pcscfAddressChanged = callback.isPcscfAddressChanged(lp);
        boolean z = true;
        if (type >= 1 || pcscfAddressChanged) {
            LinkPropertiesWrapper unused = callback.mLinkProperties = lp;
            Log.i(LOG_TAG, "onLinkPropertiesChanged: LinkProperties changed");
            if (type >= 1) {
                if (type != 2) {
                    z = false;
                }
                listener.onLocalIpChanged(networkType, z);
            }
            if (pcscfAddressChanged) {
                listener.onPcscfAddressChanged(networkType, readPcscfFromLinkProperties(lp));
            }
        }
    }

    private void onEpdgIkeError(int phoneId) {
        synchronized (this.mNetworkStateListeners) {
            for (NetworkStateListener listener : this.mNetworkStateListeners) {
                listener.onIKEAuthFAilure(phoneId);
            }
        }
    }

    private void onEpdgConnected(int phoneId, String apnType, boolean connected) {
        NetworkState ns = getNetworkState(phoneId);
        if (TextUtils.equals(apnType, DeviceConfigManager.IMS) && ns != null) {
            String str = LOG_TAG;
            IMSLog.i(str, phoneId, "EpdgEvent onEpdgConnected: apnType=" + apnType + " connected=" + connected + " mIsEpdgConnected=" + ns.isEpdgConnected());
            boolean existCallBack = false;
            Iterator<NetworkCallback> it = this.mNetworkCallbacks.values().iterator();
            while (true) {
                if (it.hasNext()) {
                    if (it.next().mNetworkType == 11) {
                        existCallBack = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            String str2 = LOG_TAG;
            IMSLog.i(str2, phoneId, "onEpdgConnected: existCallBack=" + existCallBack + " connected=" + connected + " dataRat=" + ns.getDataNetworkType() + " mobileDataRat=" + ns.getMobileDataNetworkType() + " voiceRat =" + ns.getVoiceNetworkType());
            if (!existCallBack) {
                ns.setEpdgConnected(false);
            } else if (connected) {
                if (!ns.isEpdgConnected()) {
                    ns.setEpdgConnected(true);
                    synchronized (this.mNetworkStateListeners) {
                        for (NetworkStateListener listener : this.mNetworkStateListeners) {
                            listener.onDataConnectionStateChanged(18, true, phoneId);
                            listener.onEpdgConnected(phoneId);
                        }
                    }
                }
            } else if (ns.isEpdgConnected()) {
                ns.setEpdgConnected(false);
                int targetMobileRat = ns.getDataNetworkType();
                if (targetMobileRat == 18) {
                    targetMobileRat = ns.getMobileDataNetworkType();
                }
                synchronized (this.mNetworkStateListeners) {
                    for (NetworkStateListener listener2 : this.mNetworkStateListeners) {
                        listener2.onDataConnectionStateChanged(targetMobileRat, isWifiConnected(), phoneId);
                        listener2.onEpdgDisconnected(phoneId);
                    }
                }
            }
        }
    }

    private void onWifiConnected() {
        Log.i(LOG_TAG, "onWifiConnected:");
        synchronized (this.mNetworkStateListeners) {
            if (SimUtil.isDualIMS()) {
                for (NetworkStateListener listener : this.mNetworkStateListeners) {
                    for (ISimManager sm : this.simmanagers) {
                        NetworkState ns = getNetworkState(sm.getSimSlotIndex());
                        if (ns != null) {
                            listener.onDataConnectionStateChanged(ns.getDataNetworkType(), true, sm.getSimSlotIndex());
                        }
                    }
                }
            } else {
                for (NetworkStateListener listener2 : this.mNetworkStateListeners) {
                    int defaultPhoneId = SimUtil.getDefaultPhoneId();
                    NetworkState ns2 = getNetworkState(defaultPhoneId);
                    if (ns2 != null) {
                        listener2.onDataConnectionStateChanged(ns2.getDataNetworkType(), true, defaultPhoneId);
                    }
                }
            }
        }
    }

    private void onWifiDisconnected() {
        Log.i(LOG_TAG, "onWifiDisConnected:");
        synchronized (this.mNetworkStateListeners) {
            if (SimUtil.isDualIMS()) {
                for (NetworkStateListener listener : this.mNetworkStateListeners) {
                    for (ISimManager sm : this.simmanagers) {
                        NetworkState ns = getNetworkState(sm.getSimSlotIndex());
                        if (ns != null) {
                            listener.onDataConnectionStateChanged(ns.getDataNetworkType(), false, sm.getSimSlotIndex());
                        }
                    }
                }
            } else {
                for (NetworkStateListener listener2 : this.mNetworkStateListeners) {
                    int defaultPhoneId = SimUtil.getDefaultPhoneId();
                    NetworkState ns2 = getNetworkState(defaultPhoneId);
                    if (ns2 != null) {
                        listener2.onDataConnectionStateChanged(ns2.getDataNetworkType(), false, defaultPhoneId);
                    }
                }
            }
        }
    }

    private void onDefaultNetworkChanged() {
        Log.i(LOG_TAG, "onDefaultNetworkChanged:");
        synchronized (this.mNetworkStateListeners) {
            if (SimUtil.isDualIMS()) {
                for (NetworkStateListener listener : this.mNetworkStateListeners) {
                    for (ISimManager sm : this.simmanagers) {
                        listener.onDefaultNetworkStateChanged(sm.getSimSlotIndex());
                    }
                }
            } else {
                for (NetworkStateListener listener2 : this.mNetworkStateListeners) {
                    listener2.onDefaultNetworkStateChanged(SimUtil.getDefaultPhoneId());
                }
            }
        }
    }

    public int stopPdnConnectivity(int network, PdnEventListener listener) {
        return stopPdnConnectivity(network, this.mDefaultPhoneId, listener);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004d, code lost:
        sendMessage(obtainMessage(107, r6, r7, r8));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0057, code lost:
        return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int stopPdnConnectivity(int r6, int r7, com.sec.internal.interfaces.ims.core.PdnEventListener r8) {
        /*
            r5 = this;
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r0 = r5.mNetworkCallbacks
            monitor-enter(r0)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r1 = r5.mNetworkCallbacks     // Catch:{ all -> 0x0058 }
            java.lang.Object r1 = r1.get(r8)     // Catch:{ all -> 0x0058 }
            com.sec.internal.ims.core.PdnController$NetworkCallback r1 = (com.sec.internal.ims.core.PdnController.NetworkCallback) r1     // Catch:{ all -> 0x0058 }
            com.sec.internal.helper.SimpleEventLog r2 = r5.mEventLog     // Catch:{ all -> 0x0058 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0058 }
            r3.<init>()     // Catch:{ all -> 0x0058 }
            java.lang.String r4 = "stopPdnConnectivity: network "
            r3.append(r4)     // Catch:{ all -> 0x0058 }
            r3.append(r6)     // Catch:{ all -> 0x0058 }
            java.lang.String r4 = ", callback is "
            r3.append(r4)     // Catch:{ all -> 0x0058 }
            if (r1 == 0) goto L_0x0025
            java.lang.String r4 = "exist"
            goto L_0x0027
        L_0x0025:
            java.lang.String r4 = "null"
        L_0x0027:
            r3.append(r4)     // Catch:{ all -> 0x0058 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0058 }
            r2.logAndAdd(r7, r3)     // Catch:{ all -> 0x0058 }
            if (r1 == 0) goto L_0x0037
            r1.setDisconnectRequested()     // Catch:{ all -> 0x0058 }
            goto L_0x004c
        L_0x0037:
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x0058 }
            java.lang.String r3 = "requestStopNetwork: callback not found"
            com.sec.internal.log.IMSLog.e(r2, r7, r3)     // Catch:{ all -> 0x0058 }
            com.sec.internal.constants.Mno r2 = com.sec.internal.helper.SimUtil.getSimMno(r7)     // Catch:{ all -> 0x0058 }
            boolean r2 = r2.isKor()     // Catch:{ all -> 0x0058 }
            if (r2 == 0) goto L_0x004c
            r2 = 2
            monitor-exit(r0)     // Catch:{ all -> 0x0058 }
            return r2
        L_0x004c:
            monitor-exit(r0)     // Catch:{ all -> 0x0058 }
            r0 = 107(0x6b, float:1.5E-43)
            android.os.Message r0 = r5.obtainMessage(r0, r6, r7, r8)
            r5.sendMessage(r0)
            r0 = 1
            return r0
        L_0x0058:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0058 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.stopPdnConnectivity(int, int, com.sec.internal.interfaces.ims.core.PdnEventListener):int");
    }

    public List<InetAddress> filterAddresses(Iterable<InetAddress> inetAddresses) {
        List<InetAddress> ret = new ArrayList<>();
        if (inetAddresses != null) {
            for (InetAddress inetAddress : inetAddresses) {
                if (DBG) {
                    String str = LOG_TAG;
                    Log.i(str, "getIpAddressList: inetAddress: " + inetAddress);
                }
                if (inetAddress != null && !inetAddress.isAnyLocalAddress() && !inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress() && !inetAddress.isMulticastAddress()) {
                    if (DBG) {
                        String str2 = LOG_TAG;
                        Log.i(str2, "getIpAddressList:  inetAddress IP:" + inetAddress.getHostAddress());
                    }
                    if (NetworkUtil.isIPv4Address(inetAddress.getHostAddress()) || NetworkUtil.isIPv6Address(inetAddress.getHostAddress())) {
                        ret.add(inetAddress);
                    }
                }
            }
        }
        return ret;
    }

    private InetAddress determineIpAddress(String hostAddress) {
        if (hostAddress == null || hostAddress.length() == 0) {
            Log.e(LOG_TAG, "determineIpAddress: empty address.");
            return null;
        }
        try {
            return InetAddress.getByName(hostAddress);
        } catch (UnknownHostException e) {
            String str = LOG_TAG;
            Log.e(str, "determineIpAddress: invalid address -  " + hostAddress);
            return null;
        }
    }

    public boolean requestRouteToHostAddress(int networkType, String hostAddress) {
        boolean result = false;
        InetAddress address = determineIpAddress(hostAddress);
        if (address != null) {
            result = ConnectivityManagerExt.requestRouteToHostAddress(this.mConnectivityManager, networkType, address);
        }
        String str = LOG_TAG;
        Log.i(str, "requestRouteToHostAddress: hostAddress=" + hostAddress + " networkType=" + networkType + " address=" + IMSLog.checker(address) + " result : " + result);
        return result;
    }

    public boolean removeRouteToHostAddress(int networkType, String hostAddress) {
        String str = LOG_TAG;
        Log.i(str, "removeRouteToHostAddress: hostAddress " + hostAddress + " networkType " + networkType);
        InetAddress address = determineIpAddress(hostAddress);
        if (address != null) {
            return ConnectivityManagerExt.removeRouteToHostAddress(this.mConnectivityManager, networkType, address);
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
        if (r2 == null) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003b, code lost:
        if (com.sec.internal.ims.core.PdnController.NetworkCallback.access$1700(r2) == null) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0041, code lost:
        if (com.sec.internal.ims.core.PdnController.NetworkCallback.access$1600(r2) == r7) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0049, code lost:
        if (r2.isDisconnectRequested() == false) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004b, code lost:
        r6.mEventLog.logAndAdd("isConnected: Disconnect msg is in queue for networkType [" + r7 + "]");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0066, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0067, code lost:
        if (r7 == 0) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0069, code lost:
        if (r7 != 1) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006c, code lost:
        r3 = r6.mConnectivityManager.getNetworkInfo(com.sec.internal.ims.core.PdnController.NetworkCallback.access$1700(r2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0076, code lost:
        if (r3 != null) goto L_0x0094;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0078, code lost:
        android.util.Log.e(LOG_TAG, "isConnected: Failed to find NetworkInfo for networkType [" + r7 + "]");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0093, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009a, code lost:
        if (r3.getState() == android.net.NetworkInfo.State.CONNECTED) goto L_0x00b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a0, code lost:
        if (isSuspended(r3) == false) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a3, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a5, code lost:
        r0 = true ^ android.text.TextUtils.isEmpty(com.sec.internal.ims.core.PdnController.NetworkCallback.access$1900(r2).getInterfaceName());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b2, code lost:
        android.util.Log.i(LOG_TAG, "isConnected:  [" + r0 + "] networktype [" + r7 + "]");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00d5, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d6, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isConnected(int r7, com.sec.internal.interfaces.ims.core.PdnEventListener r8) {
        /*
            r6 = this;
            r0 = 1
            r1 = 0
            r2 = 15
            if (r7 != r2) goto L_0x0016
            r2 = r8
            com.sec.internal.ims.core.RegisterTask r2 = (com.sec.internal.ims.core.RegisterTask) r2
            int r2 = r2.getPhoneId()
            java.lang.String[] r3 = r6.mEPDNintfName
            r3 = r3[r2]
            if (r3 == 0) goto L_0x0014
            goto L_0x0015
        L_0x0014:
            r0 = r1
        L_0x0015:
            return r0
        L_0x0016:
            r2 = 0
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r3 = r6.mNetworkCallbacks
            monitor-enter(r3)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r4 = r6.mNetworkCallbacks     // Catch:{ all -> 0x00d7 }
            boolean r4 = r4.isEmpty()     // Catch:{ all -> 0x00d7 }
            if (r4 == 0) goto L_0x002b
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00d7 }
            java.lang.String r4 = "isConnected: No callback exists"
            android.util.Log.i(r0, r4)     // Catch:{ all -> 0x00d7 }
            monitor-exit(r3)     // Catch:{ all -> 0x00d7 }
            return r1
        L_0x002b:
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r4 = r6.mNetworkCallbacks     // Catch:{ all -> 0x00d7 }
            java.lang.Object r4 = r4.get(r8)     // Catch:{ all -> 0x00d7 }
            com.sec.internal.ims.core.PdnController$NetworkCallback r4 = (com.sec.internal.ims.core.PdnController.NetworkCallback) r4     // Catch:{ all -> 0x00d7 }
            r2 = r4
            monitor-exit(r3)     // Catch:{ all -> 0x00d7 }
            if (r2 == 0) goto L_0x00d6
            android.net.Network r3 = r2.mNetwork
            if (r3 == 0) goto L_0x00d6
            int r3 = r2.mNetworkType
            if (r3 == r7) goto L_0x0045
            goto L_0x00d6
        L_0x0045:
            boolean r3 = r2.isDisconnectRequested()
            if (r3 == 0) goto L_0x0067
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "isConnected: Disconnect msg is in queue for networkType ["
            r3.append(r4)
            r3.append(r7)
            java.lang.String r4 = "]"
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            r0.logAndAdd(r3)
            return r1
        L_0x0067:
            if (r7 == 0) goto L_0x00a5
            if (r7 != r0) goto L_0x006c
            goto L_0x00a5
        L_0x006c:
            android.net.ConnectivityManager r3 = r6.mConnectivityManager
            android.net.Network r4 = r2.mNetwork
            android.net.NetworkInfo r3 = r3.getNetworkInfo(r4)
            if (r3 != 0) goto L_0x0094
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "isConnected: Failed to find NetworkInfo for networkType ["
            r4.append(r5)
            r4.append(r7)
            java.lang.String r5 = "]"
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.e(r0, r4)
            return r1
        L_0x0094:
            android.net.NetworkInfo$State r4 = r3.getState()
            android.net.NetworkInfo$State r5 = android.net.NetworkInfo.State.CONNECTED
            if (r4 == r5) goto L_0x00a4
            boolean r4 = r6.isSuspended(r3)
            if (r4 == 0) goto L_0x00a3
            goto L_0x00a4
        L_0x00a3:
            r0 = r1
        L_0x00a4:
            goto L_0x00b2
        L_0x00a5:
            com.sec.internal.helper.os.LinkPropertiesWrapper r1 = r2.mLinkProperties
            java.lang.String r1 = r1.getInterfaceName()
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            r0 = r0 ^ r1
        L_0x00b2:
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "isConnected:  ["
            r3.append(r4)
            r3.append(r0)
            java.lang.String r4 = "] networktype ["
            r3.append(r4)
            r3.append(r7)
            java.lang.String r4 = "]"
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r1, r3)
            return r0
        L_0x00d6:
            return r1
        L_0x00d7:
            r0 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x00d7 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.isConnected(int, com.sec.internal.interfaces.ims.core.PdnEventListener):boolean");
    }

    public LinkPropertiesWrapper getLinkProperties(PdnEventListener listener) {
        NetworkCallback callback = this.mNetworkCallbacks.get(listener);
        if (callback != null) {
            return callback.mLinkProperties;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isSuspended(NetworkInfo networkInfo) {
        boolean suspended = false;
        if (networkInfo == null) {
            return false;
        }
        int networkType = networkInfo.getType();
        if (networkInfo.getState() == NetworkInfo.State.SUSPENDED) {
            suspended = true;
        }
        String str = LOG_TAG;
        Log.i(str, "isSuspended [" + suspended + "] networktype [" + networkType + "]");
        return suspended;
    }

    public boolean isEpdgConnected(int phoneId) {
        if (getNetworkState(phoneId) != null) {
            return getNetworkState(phoneId).isEpdgConnected();
        }
        return false;
    }

    public boolean isEpdgAvailable(int phoneId) {
        return getNetworkState(phoneId).isEpdgAVailable();
    }

    public boolean isEmergencyEpdgConnected(int phoneId) {
        return getNetworkState(phoneId).isEmergencyEpdgConnected();
    }

    public boolean isWifiConnected() {
        NetworkInfo ni;
        Network[] allNetworks = this.mConnectivityManager.getAllNetworks();
        int length = allNetworks.length;
        int i = 0;
        while (i < length) {
            Network network = allNetworks[i];
            NetworkCapabilities nc = this.mConnectivityManager.getNetworkCapabilities(network);
            if (nc == null || !nc.hasTransport(1) || ((!nc.hasCapability(12) && !nc.hasCapability(4)) || (ni = this.mConnectivityManager.getNetworkInfo(network)) == null)) {
                i++;
            } else {
                Log.i(LOG_TAG, "isWifiConnected: " + ni);
                return ni.isConnected();
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0020, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getInterfaceName(com.sec.internal.interfaces.ims.core.PdnEventListener r4) {
        /*
            r3 = this;
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r0 = r3.mNetworkCallbacks
            monitor-enter(r0)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r1 = r3.mNetworkCallbacks     // Catch:{ all -> 0x0022 }
            java.lang.Object r1 = r1.get(r4)     // Catch:{ all -> 0x0022 }
            com.sec.internal.ims.core.PdnController$NetworkCallback r1 = (com.sec.internal.ims.core.PdnController.NetworkCallback) r1     // Catch:{ all -> 0x0022 }
            if (r1 == 0) goto L_0x001f
            com.sec.internal.helper.os.LinkPropertiesWrapper r2 = r1.mLinkProperties     // Catch:{ all -> 0x0022 }
            if (r2 == 0) goto L_0x001f
            com.sec.internal.helper.os.LinkPropertiesWrapper r2 = r1.mLinkProperties     // Catch:{ all -> 0x0022 }
            java.lang.String r2 = r2.getInterfaceName()     // Catch:{ all -> 0x0022 }
            if (r2 == 0) goto L_0x001f
            monitor-exit(r0)     // Catch:{ all -> 0x0022 }
            return r2
        L_0x001f:
            monitor-exit(r0)     // Catch:{ all -> 0x0022 }
            r0 = 0
            return r0
        L_0x0022:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0022 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.getInterfaceName(com.sec.internal.interfaces.ims.core.PdnEventListener):java.lang.String");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<java.lang.String> getDnsServers(com.sec.internal.interfaces.ims.core.PdnEventListener r4) {
        /*
            r3 = this;
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r0 = r3.mNetworkCallbacks
            monitor-enter(r0)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.PdnController$NetworkCallback> r1 = r3.mNetworkCallbacks     // Catch:{ all -> 0x0024 }
            java.lang.Object r1 = r1.get(r4)     // Catch:{ all -> 0x0024 }
            com.sec.internal.ims.core.PdnController$NetworkCallback r1 = (com.sec.internal.ims.core.PdnController.NetworkCallback) r1     // Catch:{ all -> 0x0024 }
            if (r1 == 0) goto L_0x0021
            com.sec.internal.helper.os.LinkPropertiesWrapper r2 = r1.mLinkProperties     // Catch:{ all -> 0x0024 }
            if (r2 == 0) goto L_0x0021
            com.sec.internal.helper.os.LinkPropertiesWrapper r2 = r1.mLinkProperties     // Catch:{ all -> 0x0024 }
            android.net.LinkProperties r2 = r2.getLinkProperties()     // Catch:{ all -> 0x0024 }
            java.util.List r2 = r3.getDnsServers((android.net.LinkProperties) r2)     // Catch:{ all -> 0x0024 }
            monitor-exit(r0)     // Catch:{ all -> 0x0024 }
            return r2
        L_0x0021:
            monitor-exit(r0)     // Catch:{ all -> 0x0024 }
            r0 = 0
            return r0
        L_0x0024:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0024 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.getDnsServers(com.sec.internal.interfaces.ims.core.PdnEventListener):java.util.List");
    }

    public List<String> getDnsServersByNetType() {
        return getDnsServers(this.mConnectivityManager.getLinkProperties(this.mConnectivityManager.getActiveNetwork()));
    }

    private List<String> getDnsServers(LinkProperties linkProperties) {
        if (linkProperties == null) {
            return null;
        }
        List<InetAddress> dnses = linkProperties.getDnsServers();
        List<String> dnsServerList = new ArrayList<>();
        List<String> ipv4DnsServerList = new ArrayList<>();
        List<String> ipv6DnsServerList = new ArrayList<>();
        if (dnses != null) {
            for (InetAddress dns : dnses) {
                if (NetworkUtil.isIPv4Address(dns.getHostAddress())) {
                    ipv4DnsServerList.add(dns.getHostAddress());
                } else if (NetworkUtil.isIPv6Address(dns.getHostAddress())) {
                    ipv6DnsServerList.add(dns.getHostAddress());
                }
            }
        }
        dnsServerList.addAll(ipv6DnsServerList);
        dnsServerList.addAll(ipv4DnsServerList);
        return dnsServerList;
    }

    public String getIntfNameByNetType() {
        return getIntfNameByNetType(this.mConnectivityManager.getActiveNetwork());
    }

    public String getIntfNameByNetType(Network network) {
        LinkProperties linkProperties = this.mConnectivityManager.getLinkProperties(network);
        if (linkProperties != null) {
            return linkProperties.getInterfaceName();
        }
        return null;
    }

    public boolean isNetworkAvailable(int network, int pdn, int phoneId) {
        if (pdn == 15 || pdn == -1) {
            return true;
        }
        NetworkInfo ni = this.mConnectivityManager.getNetworkInfo(pdn);
        NetworkState ns = getNetworkState(phoneId);
        if (ni == null) {
            IMSLog.i(LOG_TAG, phoneId, "isNetworkAvailable: NetworkInfo is not exist. return false..");
            return false;
        }
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "isNetworkAvailable: isEpdgConnected=" + ns.isEpdgConnected() + " getDataNetworkType()=" + getNetworkState(phoneId).getDataNetworkType() + " ni.isAvailable()=" + ni.isAvailable());
        if (network != 18 || pdn == 1) {
            if (!ni.isAvailable() || ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                return false;
            }
            return true;
        } else if ((ns.isEpdgConnected() || getNetworkState(phoneId).getDataNetworkType() == 18) && ni.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isNetworkRequested(PdnEventListener listener) {
        return this.mNetworkCallbacks.containsKey(listener);
    }

    /* access modifiers changed from: private */
    public void notifyEpdgRequest(int phoneId, boolean cdmaAvailablity, boolean isRegisterRequest) {
        IMSLog.i(LOG_TAG, phoneId, "notifyEpdgRequest:");
        for (NetworkStateListener listener : this.mNetworkStateListeners) {
            if (isRegisterRequest) {
                listener.onEpdgRegisterRequested(phoneId, cdmaAvailablity);
            } else {
                listener.onEpdgDeregisterRequested(phoneId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyEpdgIpsecDisconnected(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "notifyEpdgIpsecDisconnected:");
        for (NetworkStateListener listener : this.mNetworkStateListeners) {
            listener.onEpdgIpsecDisconnected(phoneId);
        }
    }

    /* access modifiers changed from: private */
    public void notifySnapshotState(int snapshotState, int phoneId) {
        if (getNetworkState(phoneId) != null) {
            String str = LOG_TAG;
            IMSLog.i(str, phoneId, "notifySnapshotState: snapshotState=" + snapshotState + " old=" + getNetworkState(phoneId).getSnapshotState());
            if (getNetworkState(phoneId).getSnapshotState() != snapshotState) {
                getNetworkState(phoneId).setSnapshotState(snapshotState);
                boolean suspendedBySnapshot = getNetworkState(phoneId).getSnapshotState() == ServiceStateExt.SNAPSHOT_STATUS_ACTIVATED;
                synchronized (this.mNetworkCallbacks) {
                    for (PdnEventListener listener : this.mNetworkCallbacks.keySet()) {
                        NetworkCallback callback = this.mNetworkCallbacks.get(listener);
                        if (callback.mPhoneId == phoneId) {
                            if (callback.mNetworkType != 1) {
                                if (suspendedBySnapshot) {
                                    listener.onSuspendedBySnapshot(callback.mNetworkType);
                                } else {
                                    listener.onResumedBySnapshot(callback.mNetworkType);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyDataConnectionState(int networkType, int dataRegState, boolean needNotify, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "notifyDataConnectionState");
        NetworkState ns = getNetworkState(phoneId);
        if (ns != null) {
            if (networkType == 13) {
                IMSLog.i(LOG_TAG, phoneId, "initialize PendedEPDGWeakSignal flag");
                setPendedEPDGWeakSignal(phoneId, false);
            }
            String str = LOG_TAG;
            IMSLog.i(str, phoneId, "notifyDataConnectionState: needNotify=" + needNotify + " networkType=" + networkType + " isEpdgConnected=" + ns.isEpdgConnected() + " dataNetType=" + ns.getDataNetworkType() + "=>" + networkType + " dataRegState=" + ns.getDataRegState() + "=>" + dataRegState);
            if (!(!ns.isEpdgConnected() || networkType == 18 || SimUtil.getSimMno(phoneId) == Mno.TMOUS)) {
                ns.setDataNetworkType(networkType);
                ns.setDataRegState(dataRegState);
            }
            if (needNotify || networkType != ns.getDataNetworkType() || dataRegState != ns.getDataRegState()) {
                ns.setDataNetworkType(networkType);
                ns.setDataRegState(dataRegState);
                synchronized (this.mNetworkStateListeners) {
                    for (NetworkStateListener listener : this.mNetworkStateListeners) {
                        listener.onDataConnectionStateChanged(getNetworkState(phoneId).getDataNetworkType(), isWifiConnected(), phoneId);
                    }
                }
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (NetworkState ns : this.mNetworkStates) {
            builder.append(" phoneId: " + ns.getSimSlot());
            builder.append(" mIsEpdgConnected: " + ns.isEpdgConnected());
            builder.append(" isWifiConnected: " + isWifiConnected());
            builder.append(" mVopsIndication: " + ns.getVopsIndication());
            builder.append(" mDataRoaming:  " + ns.isDataRoaming());
            builder.append(" mDataConnectionState: " + ns.isDataConnectedState());
            builder.append(" mVoiceRoaming: " + ns.isVoiceRoaming());
            builder.append(" mEmergencyOnly: " + ns.isEmergencyOnly());
            builder.append(" mIsDisconnecting: " + this.mIsDisconnecting);
            builder.append(" mPendedEPDGWeakSignal: " + ns.isPendedEPDGWeakSignal());
            builder.append(" mEmcbsIndication: " + ns.getEmcBsIndication());
        }
        return builder.toString();
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName());
        IMSLog.increaseIndent(LOG_TAG);
        String str2 = LOG_TAG;
        IMSLog.dump(str2, "State: " + toString());
        IMSLog.dump(LOG_TAG, "History of PdnController:");
        IMSLog.increaseIndent(LOG_TAG);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(LOG_TAG);
        IMSLog.decreaseIndent(LOG_TAG);
    }

    public int getDefaultNetworkBearer() {
        int defaultNetworkBearer = 0;
        NetworkCapabilities nc = this.mConnectivityManager.getNetworkCapabilities(this.mConnectivityManager.getActiveNetwork());
        if (nc != null && nc.hasTransport(1) && nc.hasCapability(12)) {
            defaultNetworkBearer = 1;
        }
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("getDefaultNetworkBearer: ");
        sb.append(defaultNetworkBearer == 0 ? "CELLULAR" : " WIFI");
        Log.i(str, sb.toString());
        return defaultNetworkBearer;
    }

    public int translateNetworkBearer(int bearer) {
        if (bearer == 0) {
            return 0;
        }
        if (1 == bearer) {
            return 1;
        }
        String str = LOG_TAG;
        Log.i(str, "Invalid bearer: " + bearer);
        return -1;
    }

    public int getDataState(int phoneId) {
        if (mDataState.containsKey(Integer.valueOf(phoneId))) {
            return mDataState.get(Integer.valueOf(phoneId)).intValue();
        }
        return -1;
    }

    public void setDataState(int phoneId, int state) {
        mDataState.put(Integer.valueOf(phoneId), Integer.valueOf(state));
    }
}
