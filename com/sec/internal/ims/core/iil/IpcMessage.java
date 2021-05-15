package com.sec.internal.ims.core.iil;

import com.sec.internal.log.IMSLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IpcMessage {
    public static final int IPC_CMD_CFRM = 4;
    public static final int IPC_CMD_EVENT = 5;
    public static final int IPC_CMD_EXEC = 1;
    public static final int IPC_CMD_GET = 2;
    public static final int IPC_CMD_INDI = 1;
    public static final int IPC_CMD_NOTI = 3;
    public static final int IPC_CMD_RESP = 2;
    public static final int IPC_CMD_SET = 3;
    public static final int IPC_DEBUG_HDR_SIZE = 12;
    public static final int IPC_FROM_IIL = 1;
    public static final int IPC_FROM_RIL = 0;
    public static final int IPC_GEN_CMD = 128;
    public static final int IPC_GEN_ERR_INVALID_STATE = 32773;
    public static final int IPC_GEN_ERR_NONE = 32768;
    public static final int IPC_GEN_ERR_SIM_PIN2_PERM_BLOCKED = 32782;
    public static final int IPC_GEN_PHONE_RES = 1;
    public static final int IPC_HDR_SIZE = 7;
    public static final int IPC_IIL_CHANGE_PREFERRED_NETWORK_TYPE = 21;
    public static final int IPC_IIL_CMD = 112;
    public static final int IPC_IIL_IIL_CONNECTED = 18;
    public static final int IPC_IIL_IMS_SUPPORT_STATE = 16;
    public static final int IPC_IIL_ISIM_LOADED = 17;
    public static final int IPC_IIL_PREFERENCE = 6;
    public static final int IPC_IIL_REGISTRATION = 1;
    public static final int IPC_IIL_RETRYOVER = 12;
    public static final int IPC_IIL_SET_DEREGISTRATION = 11;
    public static final int IPC_IIL_SIP_SUSPEND = 22;
    public static final int IPC_IIL_SSAC_INFO = 14;
    public static final int IPC_IMS_ERR_403_FORBIDDEN = 34049;
    public static final int IPC_IMS_ERR_MAX_RANGE = 34303;
    public static final int IPC_SS_ERR_MISTYPED_PARAM = 33298;
    private static final String LOG_TAG = "IpcMessage";
    public static final int MAX_IPC_HEADER = 19;
    protected int mAsequence;
    protected int mCmdType;
    protected int mDir;
    protected byte[] mIpcBody;
    protected byte[] mIpcData;
    protected byte[] mIpcHeader;
    protected int mLength;
    protected int mMainCmd;
    protected int mNetworkType;
    protected int mSequence;
    protected int mSubCmd;

    public IpcMessage() {
    }

    public IpcMessage(int mainCmd, int subCmd, int cmdType) {
        this.mMainCmd = mainCmd;
        this.mSubCmd = subCmd;
        this.mCmdType = cmdType;
    }

    public boolean makeHeader() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.write(0);
            dos.write(0);
            dos.write(this.mMainCmd);
            dos.write(this.mSubCmd);
            dos.write(this.mCmdType);
            dos.close();
            this.mIpcHeader = baos.toByteArray();
            return true;
        } catch (IOException ex) {
            IMSLog.e(LOG_TAG, "failed in makeHeader() " + ex);
            return false;
        }
    }

    public byte[] createIpcMessage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
        DataOutputStream dos = new DataOutputStream(baos);
        int totalLength = 7;
        byte[] bArr = this.mIpcBody;
        if (bArr != null) {
            totalLength = 7 + bArr.length;
        }
        makeHeader();
        try {
            dos.write(totalLength);
            dos.write(totalLength >> 8);
            dos.write(this.mIpcHeader, 0, 5);
            if (this.mIpcBody != null) {
                dos.write(this.mIpcBody, 0, this.mIpcBody.length);
            }
            this.mIpcData = baos.toByteArray();
            dos.close();
            return this.mIpcData;
        } catch (IOException ex) {
            IMSLog.e(LOG_TAG, "failed in createIpcMessage() " + ex);
            return null;
        }
    }

    public void setDir(int dir) {
        this.mDir = dir;
    }

    public int getMainCmd() {
        return this.mMainCmd;
    }

    public int getSubCmd() {
        return this.mSubCmd;
    }

    public int getLength() {
        return this.mLength;
    }

    public int getCmdType() {
        return this.mCmdType;
    }

    public byte[] getBody() {
        return this.mIpcBody;
    }

    public byte[] getData() {
        return this.mIpcData;
    }

    public int getNetworkType() {
        return this.mNetworkType;
    }

    public String dumpHex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        if (data == null) {
            return "";
        }
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02X ", new Object[]{Byte.valueOf(data[i])}));
        }
        return sb.toString();
    }

    public String typeStr() {
        if (this.mDir == 0) {
            int i = this.mCmdType;
            if (i == 1) {
                return "EXEC";
            }
            if (i == 2) {
                return "GET";
            }
            if (i == 3) {
                return "SET";
            }
            if (i == 4) {
                return "CFRM";
            }
            if (i == 5) {
                return "EVENT";
            }
            return "UNKNOWN(" + Integer.toHexString(this.mCmdType) + ")";
        }
        int i2 = this.mCmdType;
        if (i2 == 1) {
            return "INDI";
        }
        if (i2 == 2) {
            return "RESP";
        }
        if (i2 == 3) {
            return "NOTI";
        }
        return "UNKNOWN(" + Integer.toHexString(this.mCmdType) + ")";
    }

    public String mainCmdStr() {
        int i = this.mMainCmd;
        if (i == 112) {
            return "IPC_IIL_CMD";
        }
        if (i == 128) {
            return "IPC_GEN_CMD";
        }
        return "Unknown: " + this.mMainCmd;
    }

    private String subIilCmdStr() {
        int i = this.mSubCmd;
        if (i == 1) {
            return "IPC_IIL_REGISTRATION";
        }
        if (i == 6) {
            return "IPC_IIL_PREFERENCE";
        }
        if (i == 14) {
            return "IPC_IIL_SSAC_INFO";
        }
        if (i == 11) {
            return "IPC_IIL_SET_DEREGISTRATION";
        }
        if (i == 12) {
            return "IPC_IIL_RETRYOVER";
        }
        if (i == 21) {
            return "IPC_IIL_CHANGE_PREFERRED_NETWORK_TYPE";
        }
        if (i == 22) {
            return "IPC_IIL_SIP_SUSPEND";
        }
        switch (i) {
            case 16:
                return "IPC_IIL_IMS_SUPPORT_STATE";
            case 17:
                return "IPC_IIL_ISIM_LOADED";
            case 18:
                return "IPC_IIL_IIL_CONNECTED";
            default:
                return "Unknown: " + this.mSubCmd;
        }
    }

    private String subGenCmdStr() {
        if (this.mSubCmd == 1) {
            return "IPC_GEN_PHONE_RES";
        }
        return "Unknown: " + this.mSubCmd;
    }

    public String subCmdStr() {
        int i = this.mMainCmd;
        if (i == 112) {
            return subIilCmdStr();
        }
        if (i == 128) {
            return subGenCmdStr();
        }
        return "Unknown Main: " + this.mMainCmd;
    }

    public static IpcMessage parseIpc(byte[] data, int length) {
        IpcMessage ipcMsg = null;
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        try {
            int sequence = dis.readByte();
            int asequence = dis.readByte();
            int mainCmd = dis.readByte();
            if (mainCmd == 112) {
                ipcMsg = new IilIpcMessage();
            }
            if (ipcMsg != null) {
                ipcMsg.mSequence = sequence;
                ipcMsg.mAsequence = asequence;
                ipcMsg.mMainCmd = mainCmd;
                ipcMsg.mSubCmd = dis.readUnsignedByte();
                ipcMsg.mCmdType = dis.readByte();
                ipcMsg.mLength = length;
                if (length > 7) {
                    byte[] bArr = new byte[(length - 7)];
                    ipcMsg.mIpcBody = bArr;
                    if (dis.read(bArr, 0, length - 7) < 0) {
                        IMSLog.s(LOG_TAG, "parseIpc: ipcMsg.mIpcBody - the end of the stream has been reached.");
                    }
                    ipcMsg.mNetworkType = ipcMsg.mIpcBody[0];
                }
                ipcMsg.mDir = 0;
            }
            dis.close();
            return ipcMsg;
        } catch (IOException ex) {
            IMSLog.e(LOG_TAG, ex.getMessage());
            try {
                dis.close();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public byte[] encode() {
        return new byte[0];
    }

    public boolean encodeGeneralResponse(int ipcErrorCause, IpcMessage msg) {
        byte[] data = new byte[5];
        data[0] = (byte) msg.mMainCmd;
        data[1] = (byte) msg.mSubCmd;
        data[2] = (byte) msg.mCmdType;
        if ((ipcErrorCause < 32768 || ipcErrorCause > 32782) && ((ipcErrorCause < 34049 || ipcErrorCause > 34303) && ipcErrorCause != 33298)) {
            IMSLog.e(LOG_TAG, "encodeGeneralResponse(): ipcErrorCause is out of range with value ( " + String.format("%04X ", new Object[]{Integer.valueOf(ipcErrorCause)}) + " ), but keep going. ");
            ipcErrorCause = IPC_GEN_ERR_INVALID_STATE;
        }
        data[3] = (byte) (ipcErrorCause & 255);
        data[4] = (byte) ((ipcErrorCause >> 8) & 255);
        this.mIpcBody = data;
        createIpcMessage();
        return true;
    }
}
