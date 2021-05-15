package com.sec.internal.ims.servicemodules.sms;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.att.iqi.lib.BuildConfig;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.settings.DmProfileLoader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class GsmSmsUtil {
    public static final int BIT_TP_DCS_CLASS2_SIM_MSG = 2;
    public static final int BIT_TP_PID_SIM_DATA_DOWNLOAD = 63;
    public static final String CONTENT_TYPE_3GPP = "application/vnd.3gpp.sms";
    private static final int IPC_ERR_MEM_CAP_EXCEED = 32790;
    private static final int IPC_ERR_SMS_ME_FULL = 32896;
    private static final int IPC_ERR_SMS_SIM_FULL = 32897;
    private static final String LOG_TAG = SmsServiceModule.class.getSimpleName();
    public static final int MAX_DATA_LEN = 255;
    private static final int NANP_LENGTH = 10;
    private static final String PREFIX_NUMBER_PLUS = "011";
    public static final int RIL_CODE_RP_ERROR = 32768;
    public static final int RIL_CODE_SMS_OK_ = 0;
    public static final int RP_ACK_N_MS = 3;
    public static final int RP_DATA_MS_N = 0;
    public static final int RP_DATA_N_MS = 1;
    public static final int RP_ERROR_N_MS = 5;
    public static final int RP_ERR_INVALID_MSG = 95;
    public static final int RP_SMMA = 6;
    public static final int TP_PID_SIM_DATA_DOWNLOAD = 127;

    public static byte[] get3gppPduFromTpdu(byte[] tPdu, int msgRef, String smsc, String rpOA) {
        byte[] bArr = tPdu;
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream(255);
        DataOutputStream dosOut = new DataOutputStream(baosOut);
        DataInputStream disIn = new DataInputStream(new ByteArrayInputStream(tPdu));
        try {
            byte scaLen = bArr[0];
            byte[] homeSMSC = new byte[(scaLen + 1)];
            if (disIn.read(homeSMSC) >= homeSMSC.length) {
                byte[] pdu = new byte[(bArr.length - (scaLen + 1))];
                if (disIn.read(pdu) >= 0) {
                    byte tpdu_len = (byte) pdu.length;
                    dosOut.write(0);
                    try {
                        dosOut.write(msgRef);
                        if (TextUtils.isEmpty(rpOA)) {
                            dosOut.write(0);
                        } else {
                            byte[] rp_oa = PhoneNumberUtils.numberToCalledPartyBCD(rpOA);
                            if (rp_oa != null) {
                                dosOut.write(rp_oa.length);
                                dosOut.write(rp_oa);
                            } else {
                                throw new RuntimeException("rp_oa is null");
                            }
                        }
                        byte[] bcdSmsc = PhoneNumberUtils.numberToCalledPartyBCD(smsc);
                        if (bcdSmsc != null) {
                            dosOut.write(bcdSmsc.length);
                            dosOut.write(bcdSmsc);
                            dosOut.write(tpdu_len);
                            dosOut.write(pdu);
                            disIn.close();
                            dosOut.close();
                            return baosOut.toByteArray();
                        }
                        throw new RuntimeException("smsc is null");
                    } catch (IOException e) {
                        e = e;
                        IOException e2 = e;
                        try {
                            disIn.close();
                            dosOut.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        e2.printStackTrace();
                        return null;
                    }
                } else {
                    int i = msgRef;
                    throw new RuntimeException("Exception : Fail to Read TPDU from PDU");
                }
            } else {
                int i2 = msgRef;
                throw new RuntimeException("Exception : Fail to Read Sca from PDU");
            }
        } catch (IOException e3) {
            e = e3;
            int i3 = msgRef;
            IOException e22 = e;
            disIn.close();
            dosOut.close();
            e22.printStackTrace();
            return null;
        }
    }

    public static String getSCAFromPdu(byte[] pdu) {
        int len;
        if (pdu == null || (len = pdu[0] & 255) <= 0 || pdu.length < len) {
            return "";
        }
        String sca = PhoneNumberUtils.calledPartyBCDToString(pdu, 1, len);
        if (sca != null) {
            return sca;
        }
        throw new RuntimeException("[getSCAFromPdu] Exception : sca is null");
    }

    public static int get3gppRPError(String contentType, byte[] data) {
        if (contentType == null || !contentType.equals(CONTENT_TYPE_3GPP) || data == null || data.length < 4) {
            return -1;
        }
        if (5 == data[0]) {
            return data[3] & 127;
        }
        return 0;
    }

    public static boolean isAck(String contentType, byte[] data) {
        if (contentType == null && data == null) {
            return true;
        }
        if (contentType != null) {
            if (data != null) {
                String str = LOG_TAG;
                Log.i(str, "isAck: contentType=" + contentType + " data[0]=" + data[0]);
            }
            if (contentType.equals(CONTENT_TYPE_3GPP)) {
                if ((data == null || 3 != data[0]) && data != null) {
                    return false;
                }
                return true;
            } else if (data == null) {
                return true;
            }
            return false;
        }
        throw new RuntimeException("contentType is null");
    }

    public static byte[] get3gppTpduFromPdu(byte[] pdu) {
        if (pdu.length < 4) {
            return null;
        }
        return get3gppTpdu(pdu);
    }

    private static byte[] get3gppTpdu(byte[] pdu) {
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream(255);
        DataOutputStream dosOut = new DataOutputStream(baosOut);
        DataInputStream disIn = new DataInputStream(new ByteArrayInputStream(pdu));
        try {
            if (3 == pdu[0]) {
                byte readByte = disIn.readByte();
                byte tmpByte = disIn.readByte();
                byte tmpByte2 = disIn.readByte();
                int len = disIn.readByte() & 255;
                if (disIn.available() < len) {
                    try {
                        disIn.close();
                        dosOut.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
                byte[] tb = new byte[len];
                if (disIn.read(tb) >= 0) {
                    dosOut.write(tb);
                    byte[] byteArray = baosOut.toByteArray();
                    try {
                        disIn.close();
                        dosOut.close();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                    return byteArray;
                }
                throw new RuntimeException("Fail to read TPDU from PDU");
            } else if (1 == pdu[0]) {
                byte readByte2 = disIn.readByte();
                byte tmpByte3 = disIn.readByte();
                byte tmpByte4 = disIn.readByte();
                int len2 = tmpByte4 & 255;
                if (disIn.available() < len2) {
                    try {
                        disIn.close();
                        dosOut.close();
                    } catch (IOException ex3) {
                        ex3.printStackTrace();
                    }
                    return null;
                }
                byte[] ori_addr = new byte[(len2 + 1)];
                if (disIn.read(ori_addr, 1, tmpByte4) < 0) {
                    try {
                        disIn.close();
                        dosOut.close();
                    } catch (IOException ex4) {
                        ex4.printStackTrace();
                    }
                    return null;
                }
                ori_addr[0] = tmpByte4;
                if (disIn.available() > 0) {
                    int len3 = disIn.readByte() & 255;
                    if (len3 > 0) {
                        if (disIn.available() < len3) {
                            try {
                                disIn.close();
                                dosOut.close();
                            } catch (IOException ex5) {
                                ex5.printStackTrace();
                            }
                            return null;
                        }
                        disIn.skipBytes(len3);
                    }
                    if (disIn.available() <= 0) {
                        try {
                            disIn.close();
                            dosOut.close();
                        } catch (IOException ex6) {
                            ex6.printStackTrace();
                        }
                        return null;
                    }
                    int len4 = disIn.readByte() & 255;
                    if (disIn.available() < len4) {
                        try {
                            disIn.close();
                            dosOut.close();
                        } catch (IOException ex7) {
                            ex7.printStackTrace();
                        }
                        return null;
                    }
                    byte[] tpdu = new byte[len4];
                    if (disIn.read(tpdu) >= 0) {
                        dosOut.write(ori_addr);
                        dosOut.write(tpdu);
                        byte[] byteArray2 = baosOut.toByteArray();
                        try {
                            disIn.close();
                            dosOut.close();
                        } catch (IOException ex8) {
                            ex8.printStackTrace();
                        }
                        return byteArray2;
                    }
                    throw new RuntimeException("Exception : fail to read tpdu");
                }
                throw new RuntimeException("EOF RPDU. before reading RP-DA len");
            } else if (5 == pdu[0]) {
                byte readByte3 = disIn.readByte();
                byte tmpByte5 = disIn.readByte();
                int len5 = disIn.readByte() & 255;
                if (disIn.available() < len5) {
                    try {
                        disIn.close();
                        dosOut.close();
                    } catch (IOException ex9) {
                        ex9.printStackTrace();
                    }
                    return null;
                }
                for (int i = 0; i < len5; i++) {
                    byte tmpByte6 = disIn.readByte();
                }
                int len6 = disIn.available();
                if (len6 > 0) {
                    byte[] tpdu2 = new byte[len6];
                    if (disIn.read(tpdu2) >= 0) {
                        dosOut.write(tpdu2);
                    } else {
                        throw new RuntimeException("Exception : Reading TPDU from RIL PDU");
                    }
                }
                byte[] tpdu3 = baosOut.toByteArray();
                try {
                    disIn.close();
                    dosOut.close();
                } catch (IOException ex10) {
                    ex10.printStackTrace();
                }
                return tpdu3;
            } else {
                try {
                    disIn.close();
                    dosOut.close();
                } catch (IOException ex11) {
                    ex11.printStackTrace();
                }
                return null;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            disIn.close();
            dosOut.close();
        } catch (IOException e2) {
            e2.printStackTrace();
            disIn.close();
            dosOut.close();
        } catch (Throwable th) {
            try {
                disIn.close();
                dosOut.close();
            } catch (IOException ex12) {
                ex12.printStackTrace();
            }
            throw th;
        }
    }

    public static byte[] makeRPErrorPdu(byte[] pdu) {
        int rpRef = 255;
        if (pdu.length >= 2) {
            rpRef = pdu[1] & 255;
        }
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream(255);
        DataOutputStream dosOut = new DataOutputStream(baosOut);
        try {
            dosOut.writeByte(5);
            dosOut.writeByte(rpRef);
            dosOut.writeByte(1);
            dosOut.writeByte(95);
            byte[] byteArray = baosOut.toByteArray();
            try {
                dosOut.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                dosOut.close();
                return null;
            } catch (IOException ex2) {
                ex2.printStackTrace();
                return null;
            }
        } catch (Throwable th) {
            try {
                dosOut.close();
            } catch (IOException ex3) {
                ex3.printStackTrace();
            }
            throw th;
        }
    }

    public static String trimSipAddr(String remoteAddr) {
        if (remoteAddr == null) {
            return null;
        }
        String trimAddr = remoteAddr;
        if (trimAddr.startsWith("<")) {
            trimAddr = trimAddr.substring(1);
        }
        if (trimAddr.endsWith(">")) {
            return trimAddr.substring(0, trimAddr.length() - 1);
        }
        return trimAddr;
    }

    public static byte[] getRpSMMAPdu(int rpRef) {
        return new byte[]{6, (byte) (rpRef & 255)};
    }

    public static int getTPMRFromPdu(byte[] pdu) {
        if (pdu == null) {
            return -1;
        }
        int addr_len = pdu[0] & 255;
        if (pdu.length < addr_len + 2) {
            return -1;
        }
        return pdu[addr_len + 2] & 255;
    }

    public static String removeSipPrefix(String addr) {
        if (addr == null) {
            return null;
        }
        if (addr.length() <= 4) {
            return addr;
        }
        String trimAddr = addr;
        if (trimAddr.startsWith("sip:") || trimAddr.startsWith("tel:")) {
            return trimAddr.substring(4);
        }
        return trimAddr;
    }

    public static String removeDisplayName(String remoteAddr) {
        int index;
        if (remoteAddr == null) {
            return null;
        }
        String trimmedAddr = remoteAddr.trim();
        if (trimmedAddr.length() <= 1) {
            return remoteAddr;
        }
        if (trimmedAddr.startsWith("sip") || trimmedAddr.startsWith("<sip") || trimmedAddr.startsWith("tel") || trimmedAddr.startsWith("<tel")) {
            return trimmedAddr;
        }
        if (!trimmedAddr.startsWith("\"")) {
            return remoteAddr;
        }
        if ((trimmedAddr.indexOf("sip:") >= 0 || trimmedAddr.indexOf("tel:") >= 0) && -1 != (index = trimmedAddr.indexOf("\"", 1)) && index < trimmedAddr.length() + 1) {
            return trimmedAddr.substring(index + 1).trim();
        }
        return remoteAddr;
    }

    public static boolean isStatusReport(byte[] pdu) {
        if (pdu == null) {
            return false;
        }
        byte addr_len = pdu[0];
        if (pdu.length >= addr_len + 1 && (pdu[addr_len + 1] & 2) == 2) {
            return true;
        }
        return false;
    }

    public static void set3gppTPRD(byte[] pdu) {
        int index;
        int index2;
        if (pdu.length >= 4 && pdu.length >= (index = pdu[2] + 3) && pdu.length >= (index2 = index + 1 + pdu[index])) {
            int index3 = index2 + 1;
            pdu[index3] = (byte) (pdu[index3] | 4);
        }
    }

    public static int getRilRPErrCode(int rpErr) {
        return 32768 + rpErr;
    }

    public static byte[] getDeliverReportFromPdu(int phoneId, int rpRef, byte[] data, int tpPid, int tpDcs) {
        if (data == null || data.length < 4) {
            return null;
        }
        int reason = ((data[1] & 255) * 256) + (data[0] & 255);
        String str = LOG_TAG;
        Log.i(str, "getDeliverReportFromPdu - reason : " + reason);
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream(255);
        DataOutputStream dosOut = new DataOutputStream(baosOut);
        if (reason == 0 || reason < 32768) {
            dosOut.write(2);
            dosOut.write((byte) rpRef);
            dosOut.write(65);
            if (data[3] <= 0 || data.length < data[3]) {
                if (reason != 0) {
                    dosOut.write(3);
                } else {
                    int tpduLen = 2;
                    if (tpPid != 0) {
                        tpduLen = 2 + 1;
                    }
                    if (tpDcs != 0) {
                        tpduLen++;
                    }
                    dosOut.write((byte) tpduLen);
                }
                dosOut.write(0);
                if (reason != 0) {
                    dosOut.write(reason & 255);
                }
                byte tpPI = 0;
                if (tpPid != 0) {
                    tpPI = (byte) (0 | 1);
                }
                if (tpDcs != 0) {
                    tpPI = (byte) (tpPI | 2);
                }
                dosOut.write(tpPI);
                if (tpPid != 0) {
                    dosOut.write((byte) tpPid);
                }
                if (tpDcs != 0) {
                    dosOut.write((byte) tpDcs);
                }
            } else {
                dosOut.write(data, 3, data.length - 3);
            }
        } else if (reason > 32768) {
            try {
                dosOut.write(4);
                dosOut.write((byte) rpRef);
                dosOut.write(1);
                dosOut.write(getRPErrCause(reason));
                Mno mno = SimUtil.getSimMno(phoneId);
                if (mno == Mno.DOCOMO) {
                    dosOut.write(65);
                    dosOut.write(3);
                    dosOut.write(0);
                    dosOut.write(getTPErrCause(reason));
                    dosOut.write(0);
                } else if (!mno.isEur()) {
                    dosOut.write(0);
                }
            } catch (IOException e) {
                try {
                    dosOut.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                return null;
            }
        }
        dosOut.close();
        return baosOut.toByteArray();
    }

    private static byte getRPErrCause(int ipcReasonCode) {
        switch (ipcReasonCode) {
            case IPC_ERR_MEM_CAP_EXCEED /*32790*/:
            case IPC_ERR_SMS_ME_FULL /*32896*/:
                return 22;
            case IPC_ERR_SMS_SIM_FULL /*32897*/:
                return 111;
            default:
                return (byte) (ipcReasonCode & 255);
        }
    }

    private static byte getTPErrCause(int ipcReasonCode) {
        switch (ipcReasonCode) {
            case IPC_ERR_MEM_CAP_EXCEED /*32790*/:
            case IPC_ERR_SMS_SIM_FULL /*32897*/:
                return -48;
            case IPC_ERR_SMS_ME_FULL /*32896*/:
                return -45;
            default:
                return 0;
        }
    }

    public static byte[] getTPPidDcsFromPdu(byte[] tPdu) {
        if (tPdu == null) {
            return null;
        }
        byte[] tpPidDcs = {0, 0};
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tPdu));
        try {
            int rpOriAddrLen = dis.read() & 255;
            if (tPdu.length < rpOriAddrLen + 2) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            dis.skipBytes(rpOriAddrLen);
            dis.skipBytes(1);
            int tpOriAddrLen = (((dis.read() & 255) + 1) / 2) + 1;
            if (dis.available() < tpOriAddrLen + 2) {
                try {
                    dis.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                return tpPidDcs;
            }
            dis.skip((long) tpOriAddrLen);
            tpPidDcs[0] = dis.readByte();
            tpPidDcs[1] = dis.readByte();
            try {
                dis.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            return tpPidDcs;
        } catch (IOException e4) {
            e4.printStackTrace();
            dis.close();
        } catch (Throwable th) {
            try {
                dis.close();
            } catch (IOException e5) {
                e5.printStackTrace();
            }
            throw th;
        }
    }

    public static boolean isISODigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isTwoToNine(char c) {
        if (c < '2' || c > '9') {
            return false;
        }
        return true;
    }

    public static boolean isNanp(String dialStr) {
        if (dialStr == null || dialStr.length() != 10 || !isTwoToNine(dialStr.charAt(0)) || !isTwoToNine(dialStr.charAt(3))) {
            return false;
        }
        for (int i = 1; i < 10; i++) {
            if (!isISODigit(dialStr.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isRPErrorForRetransmission(int rpErrCause) {
        if (rpErrCause == 41 || rpErrCause == 42 || rpErrCause == 47 || rpErrCause == 98 || rpErrCause == 111) {
            return true;
        }
        return false;
    }

    public static boolean isAdminMsg(byte[] tPdu) {
        byte[] tpPidDcs = getTPPidDcsFromPdu(tPdu);
        if (tpPidDcs == null || tpPidDcs[0] != Byte.MAX_VALUE) {
            return false;
        }
        return true;
    }

    protected static String getScaForRpDa(boolean isSMMA, byte[] pdu, String destAddr, Mno mno) {
        String sca;
        if (!isSMMA) {
            sca = getSCAFromPdu(pdu);
        } else {
            sca = destAddr;
        }
        if (TextUtils.isEmpty(sca)) {
            if (mno == Mno.RJIL || mno == Mno.CTC || mno == Mno.CTCMO) {
                sca = BuildConfig.VERSION_NAME;
            } else {
                Log.e(LOG_TAG, "pdu is malformed. no SCA");
                return "noSCA";
            }
        }
        String str = LOG_TAG;
        Log.i(str, "sendSMSOverIMS: SmscAddr FromPdu=" + sca);
        return sca;
    }

    protected static String getSca(String sca, String destAddr, Mno mno, ImsRegistration regInfo) {
        if (mno == Mno.VZW) {
            if (destAddr != null && destAddr.length() > PREFIX_NUMBER_PLUS.length() && destAddr.startsWith(PREFIX_NUMBER_PLUS)) {
                return "+" + destAddr.substring(PREFIX_NUMBER_PLUS.length());
            } else if (destAddr != null) {
                return destAddr;
            } else {
                return sca;
            }
        } else if (!DeviceUtil.getGcfMode().booleanValue()) {
            return sca;
        } else {
            if (regInfo != null) {
                sca = regInfo.getImsProfile().getSmscSet();
            }
            if (sca == null) {
                return "4444";
            }
            return sca;
        }
    }

    protected static String getScaFromPsismscPSI(Context context, String sca, Mno mno, TelephonyManager tm, int phoneId, ImsRegistration regInfo) {
        if (mno == Mno.ATT || mno == Mno.VZW || mno == Mno.KDDI || mno == Mno.SPRINT) {
            byte[] psismsc = TelephonyManagerExt.getPsismsc(tm, phoneId);
            if (psismsc != null) {
                String sca2 = new String(psismsc, Charset.defaultCharset());
                String str = LOG_TAG;
                Log.d(str, "PSISMSC: " + sca2);
                return sca2;
            } else if (mno != Mno.SPRINT) {
                return sca;
            } else {
                String psi = DmProfileLoader.getProfile(context, regInfo.getImsProfile(), phoneId).getSmsPsi();
                if (psi != null && !"".equalsIgnoreCase(psi)) {
                    return psi;
                }
                Log.e(LOG_TAG, "there is no SMS_PSI");
                return sca;
            }
        } else if (mno == Mno.LGU) {
            String psi2 = DmProfileLoader.getProfile(context, regInfo.getImsProfile(), phoneId).getSmsPsi();
            if (psi2 != null && !"".equalsIgnoreCase(psi2)) {
                return psi2;
            }
            Log.e(LOG_TAG, "there is no SMS_PSI");
            return "noPSI";
        } else if (mno != Mno.KT) {
            return sca;
        } else {
            byte[] psismsc2 = TelephonyManagerExt.getPsismsc(tm, phoneId);
            if (psismsc2 == null) {
                Log.e(LOG_TAG, "there is no PSISMSC");
                return sca;
            }
            String t_sca = new String(psismsc2, Charset.defaultCharset());
            if (t_sca.length() > 0 && t_sca.indexOf(";") > 0) {
                t_sca = t_sca.substring(0, t_sca.indexOf(";"));
            }
            if (t_sca.length() <= 0) {
                return sca;
            }
            String sca3 = t_sca;
            String str2 = LOG_TAG;
            Log.d(str2, "PSISMSC: " + sca3);
            return sca3;
        }
    }
}
