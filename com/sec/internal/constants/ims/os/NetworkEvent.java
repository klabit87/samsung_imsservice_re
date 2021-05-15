package com.sec.internal.constants.ims.os;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.TelephonyManagerExt;
import java.util.Objects;

public class NetworkEvent {
    private static final String LOG_TAG = "NetworkEvent";
    public boolean csOutOfService;
    public boolean isDataRoaming;
    public boolean isDataStateConnected;
    public boolean isEpdgAvailable;
    public boolean isEpdgConnected;
    public boolean isPsOnlyReg;
    public boolean isVoiceRoaming;
    public boolean isVopsUpdated;
    public boolean isWifiConnected;
    public int network;
    public String operatorNumeric;
    public boolean outOfService;
    public int voiceNetwork;
    public VoPsIndication voiceOverPs;

    public enum VopsState {
        KEEP,
        ENABLED,
        DISABLED
    }

    public NetworkEvent() {
        this.network = 0;
        this.voiceNetwork = 0;
        this.outOfService = true;
        this.csOutOfService = true;
        this.isDataRoaming = false;
        this.isVoiceRoaming = false;
        this.voiceOverPs = VoPsIndication.UNKNOWN;
        this.isWifiConnected = false;
        this.isEpdgConnected = false;
        this.isEpdgAvailable = false;
        this.operatorNumeric = "";
        this.isPsOnlyReg = false;
        this.isDataStateConnected = false;
    }

    public NetworkEvent(NetworkEvent event) {
        this.network = event.network;
        this.voiceNetwork = event.voiceNetwork;
        this.voiceOverPs = event.voiceOverPs;
        this.outOfService = event.outOfService;
        this.csOutOfService = event.csOutOfService;
        this.isDataRoaming = event.isDataRoaming;
        this.isDataStateConnected = event.isDataStateConnected;
        this.isVoiceRoaming = event.isVoiceRoaming;
        this.isWifiConnected = event.isWifiConnected;
        this.isEpdgConnected = event.isEpdgConnected;
        this.isEpdgAvailable = event.isEpdgAvailable;
        this.operatorNumeric = event.operatorNumeric;
        this.isPsOnlyReg = event.isPsOnlyReg;
        this.isVopsUpdated = event.isVopsUpdated;
    }

    public NetworkEvent(int network2, int voiceNetwork2, boolean oos, boolean csOos, boolean dataRoaming, boolean voiceRoaming, VoPsIndication vops, boolean wifi, boolean epdgConn, boolean epdgAvail, String operatornumeric, boolean psOnlyReg, boolean dataStateConnected) {
        this.network = network2;
        this.voiceNetwork = voiceNetwork2;
        this.outOfService = oos;
        this.isDataRoaming = dataRoaming;
        this.isVoiceRoaming = voiceRoaming;
        this.voiceOverPs = vops;
        this.csOutOfService = csOos;
        this.isWifiConnected = wifi;
        this.isEpdgConnected = epdgConn;
        this.isEpdgAvailable = epdgAvail;
        this.operatorNumeric = operatornumeric;
        this.isPsOnlyReg = psOnlyReg;
        this.isDataStateConnected = dataStateConnected;
    }

    public NetworkEvent(int network2) {
        this(network2, false, false, false, false, VoPsIndication.SUPPORTED, false, false, "00101");
    }

    public NetworkEvent(int network2, boolean oos, boolean csOos, boolean dataRoaming, boolean voiceRoaming, VoPsIndication vops, boolean wifi, boolean epdg, String operatornumeric) {
        this(network2, network2, oos, csOos, dataRoaming, voiceRoaming, vops, wifi, false, epdg, operatornumeric, false, false);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.network), Integer.valueOf(this.voiceNetwork), this.voiceOverPs, Boolean.valueOf(this.outOfService), Boolean.valueOf(this.isDataRoaming), Boolean.valueOf(this.isDataStateConnected), Boolean.valueOf(this.isVoiceRoaming), Boolean.valueOf(this.csOutOfService), Boolean.valueOf(this.isWifiConnected), Boolean.valueOf(this.isEpdgConnected), Boolean.valueOf(this.isEpdgAvailable), this.operatorNumeric, Boolean.valueOf(this.isPsOnlyReg), Boolean.valueOf(this.isVopsUpdated)});
    }

    public boolean equals(Object obj) {
        return equalsInternal(obj, false);
    }

    public boolean equalsIgnoreEpdg(Object obj) {
        return equalsInternal(obj, true);
    }

    private boolean equalsInternal(Object obj, boolean ignoreEpdg) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof NetworkEvent)) {
            return false;
        }
        NetworkEvent other = (NetworkEvent) obj;
        if (blurNetworkType(this.network) != blurNetworkType(other.network) || this.voiceNetwork != other.voiceNetwork || this.isDataRoaming != other.isDataRoaming || this.isVoiceRoaming != other.isVoiceRoaming || this.outOfService != other.outOfService || this.voiceOverPs != other.voiceOverPs || this.csOutOfService != other.csOutOfService || this.isWifiConnected != other.isWifiConnected) {
            return false;
        }
        if ((!ignoreEpdg && (this.isEpdgConnected != other.isEpdgConnected || this.isEpdgAvailable != other.isEpdgAvailable)) || this.isPsOnlyReg != other.isPsOnlyReg) {
            return false;
        }
        if (!TextUtils.isEmpty(this.operatorNumeric) && !TextUtils.equals(this.operatorNumeric, other.operatorNumeric)) {
            return false;
        }
        if (this.isDataStateConnected == other.isDataStateConnected) {
            return true;
        }
        return false;
    }

    public String changedEvent(NetworkEvent newEvent) {
        String ret = "Changed Event: ";
        if (this.isDataRoaming != newEvent.isDataRoaming) {
            ret = ret + "DataRoaming(" + this.isDataRoaming + "=>" + newEvent.isDataRoaming + "), ";
        }
        if (this.isVoiceRoaming != newEvent.isVoiceRoaming) {
            ret = ret + "VoiceRoaming(" + this.isVoiceRoaming + "=>" + newEvent.isVoiceRoaming + "), ";
        }
        if (blurNetworkType(this.network) != blurNetworkType(newEvent.network)) {
            ret = ret + "Network type(" + this.network + "=>" + newEvent.network + "), ";
        }
        if (this.voiceNetwork != newEvent.voiceNetwork) {
            ret = ret + "Voice network(" + this.voiceNetwork + "=>" + newEvent.voiceNetwork + "), ";
        }
        if (this.outOfService != newEvent.outOfService) {
            ret = ret + "OoS(" + this.outOfService + "=>" + newEvent.outOfService + "), ";
        }
        if (this.voiceOverPs != newEvent.voiceOverPs) {
            ret = ret + "VoPS(" + this.voiceOverPs + "=>" + newEvent.voiceOverPs + "), ";
        }
        if (this.csOutOfService != newEvent.csOutOfService) {
            ret = ret + "CS_OoS(" + this.csOutOfService + "=>" + newEvent.csOutOfService + "), ";
        }
        if (this.isWifiConnected != newEvent.isWifiConnected) {
            ret = ret + "isWifiConnected(" + this.isWifiConnected + "=> " + newEvent.isWifiConnected + "), ";
        }
        if (this.isPsOnlyReg != newEvent.isPsOnlyReg) {
            ret = ret + "isPsOnlyReg(" + this.isPsOnlyReg + "=>" + newEvent.isPsOnlyReg + "), ";
        }
        if (this.isDataStateConnected != newEvent.isDataStateConnected) {
            ret = ret + "isDataConnected(" + this.isDataStateConnected + "=>" + newEvent.isDataStateConnected + "), ";
        }
        if (!TextUtils.equals(this.operatorNumeric, newEvent.operatorNumeric)) {
            ret = ret + "Operator(" + this.operatorNumeric + "=>" + newEvent.operatorNumeric + "), ";
        }
        return ret.replaceAll(", $", "");
    }

    public String toString() {
        return "NetworkEvent [network=" + this.network + ", voiceNetwork=" + this.voiceNetwork + ", voiceOverPs=" + this.voiceOverPs + ", outOfService=" + this.outOfService + ", isDataRoaming=" + this.isDataRoaming + ", isVoiceRoaming=" + this.isVoiceRoaming + ", csOutOfService=" + this.csOutOfService + ", isWifiConnected=" + this.isWifiConnected + ", isEpdgConnected=" + this.isEpdgConnected + ", isEpdgAvailable=" + this.isEpdgAvailable + ", operatorNumeric=" + this.operatorNumeric + ", isPsOnlyReg=" + this.isPsOnlyReg + ", isDataConnected=" + this.isDataStateConnected + "]";
    }

    public VopsState isVopsUpdated(NetworkEvent old) {
        VoPsIndication voPsIndication;
        if (((this.network != 13 || old.network != 13) && (this.network != 20 || old.network != 20)) || (voPsIndication = this.voiceOverPs) == old.voiceOverPs || voPsIndication == VoPsIndication.UNKNOWN) {
            return VopsState.KEEP;
        }
        VopsState enabled = this.voiceOverPs == VoPsIndication.SUPPORTED ? VopsState.ENABLED : VopsState.DISABLED;
        Log.d(LOG_TAG, "VoPS changed. enabled = " + enabled);
        return enabled;
    }

    public boolean isEpdgHOEvent(NetworkEvent old) {
        int i;
        int i2 = this.network;
        if ((i2 == 13 || i2 == 14) && old.network == 18 && old.isEpdgConnected) {
            Log.d(LOG_TAG, "isEpdgHOEvent: From IWLAN to LTE.");
            return true;
        } else if (this.network == 18 && ((i = old.network) == 13 || i == 14)) {
            Log.d(LOG_TAG, "isEpdgHOEvent: From LTE to IWLAN.");
            return true;
        } else if (this.network != old.network || this.isWifiConnected == old.isWifiConnected) {
            return false;
        } else {
            Log.d(LOG_TAG, "isEpdgHOEvent: Only wifi connection is changed.");
            return true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0083  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.sec.internal.constants.ims.os.NetworkEvent buildNetworkEvent(int r28, boolean r29, int r30, int r31, int r32, boolean r33, boolean r34, boolean r35, com.sec.internal.constants.ims.os.NetworkEvent r36, com.sec.internal.constants.ims.os.NetworkState r37) {
        /*
            r0 = r36
            r1 = 0
            r2 = 1
            if (r30 == 0) goto L_0x000f
            int r3 = r37.getDataRegState()
            if (r3 == 0) goto L_0x000d
            goto L_0x000f
        L_0x000d:
            r3 = r1
            goto L_0x0010
        L_0x000f:
            r3 = r2
        L_0x0010:
            int r4 = r37.getVoiceRegState()
            if (r4 == 0) goto L_0x0018
            r4 = r2
            goto L_0x0019
        L_0x0018:
            r4 = r1
        L_0x0019:
            int r19 = r37.getVoiceNetworkType()
            boolean r20 = r37.isDataRoaming()
            boolean r21 = r37.isDataConnectedState()
            boolean r22 = r37.isVoiceRoaming()
            java.lang.String r23 = r37.getOperatorNumeric()
            com.sec.internal.constants.ims.os.VoPsIndication r24 = r37.getVopsIndication()
            boolean r25 = r37.isPsOnlyReg()
            if (r30 >= 0) goto L_0x003c
            int r5 = r0.network
            boolean r3 = r0.outOfService
            goto L_0x003e
        L_0x003c:
            r5 = r30
        L_0x003e:
            int r5 = blurNetworkType(r5)
            if (r29 == 0) goto L_0x0056
            r15 = r31
            r14 = r32
            boolean r6 = is2GNetworkInCall(r5, r3, r4, r15, r14)
            if (r6 == 0) goto L_0x005a
            int r5 = blurNetworkType(r31)
            r3 = 0
            r26 = r5
            goto L_0x005c
        L_0x0056:
            r15 = r31
            r14 = r32
        L_0x005a:
            r26 = r5
        L_0x005c:
            com.sec.internal.constants.ims.os.NetworkEvent r27 = new com.sec.internal.constants.ims.os.NetworkEvent
            r5 = r27
            r6 = r26
            r7 = r19
            r8 = r3
            r9 = r4
            r10 = r20
            r11 = r22
            r12 = r24
            r13 = r33
            r14 = r34
            r15 = r35
            r16 = r23
            r17 = r25
            r18 = r21
            r5.<init>(r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18)
            com.sec.internal.constants.ims.os.NetworkEvent$VopsState r6 = r5.isVopsUpdated(r0)
            com.sec.internal.constants.ims.os.NetworkEvent$VopsState r7 = com.sec.internal.constants.ims.os.NetworkEvent.VopsState.KEEP
            if (r6 == r7) goto L_0x0084
            r1 = r2
        L_0x0084:
            r5.isVopsUpdated = r1
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.constants.ims.os.NetworkEvent.buildNetworkEvent(int, boolean, int, int, int, boolean, boolean, boolean, com.sec.internal.constants.ims.os.NetworkEvent, com.sec.internal.constants.ims.os.NetworkState):com.sec.internal.constants.ims.os.NetworkEvent");
    }

    private static boolean is2GNetworkInCall(int network2, boolean outOfService2, boolean csOutOfService2, int voiceNetworkType, int callState) {
        if (network2 != 0 || !outOfService2 || csOutOfService2 || TelephonyManagerExt.getNetworkClass(voiceNetworkType) != 1 || callState == 0) {
            return false;
        }
        return true;
    }

    public static int blurNetworkType(int network2) {
        if (!(network2 == 1 || network2 == 2)) {
            if (network2 == 15) {
                return 10;
            }
            if (network2 != 16) {
                switch (network2) {
                    case 8:
                    case 9:
                    case 10:
                        return 10;
                    default:
                        return network2;
                }
            }
        }
        return 16;
    }
}
