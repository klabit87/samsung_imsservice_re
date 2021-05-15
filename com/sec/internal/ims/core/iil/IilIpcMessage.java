package com.sec.internal.ims.core.iil;

import com.sec.internal.log.IMSLog;

public class IilIpcMessage extends IpcMessage {
    private static final String LOG_TAG = "IilIpcMessage";
    private static final int MAX_IIL_REGISTRATION = 268;

    public IilIpcMessage() {
    }

    public IilIpcMessage(int mainCmd, int subCmd, int cmdType) {
        super(mainCmd, subCmd, cmdType);
    }

    public static IilIpcMessage encodeIilConnected() {
        IilIpcMessage msg = new IilIpcMessage(112, 18, 3);
        msg.mIpcBody = null;
        msg.createIpcMessage();
        return msg;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0021, code lost:
        r11 = r8.getBytes(java.nio.charset.Charset.forName("UTF-8"));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.sec.internal.ims.core.iil.IilIpcMessage encodeImsRegisgtrationInfo(int r17, boolean r18, boolean r19, boolean r20, boolean r21, boolean r22, int r23, int r24, int r25, int r26, int r27, int r28, java.lang.String r29, int r30) {
        /*
            r0 = r18
            r1 = r19
            r2 = r20
            r3 = r21
            r4 = r22
            r5 = r25
            r6 = r27
            r7 = r28
            r8 = r29
            r9 = r30
            com.sec.internal.ims.core.iil.IilIpcMessage r10 = new com.sec.internal.ims.core.iil.IilIpcMessage
            r11 = 112(0x70, float:1.57E-43)
            r12 = 1
            r13 = 3
            r10.<init>(r11, r12, r13)
            r11 = 0
            r12 = 0
            if (r8 == 0) goto L_0x0032
            java.lang.String r13 = "UTF-8"
            java.nio.charset.Charset r13 = java.nio.charset.Charset.forName(r13)
            byte[] r11 = r8.getBytes(r13)
            int r12 = r11.length
            r13 = 256(0x100, float:3.59E-43)
            if (r12 <= r13) goto L_0x0032
            r12 = 256(0x100, float:3.59E-43)
        L_0x0032:
            r13 = 268(0x10c, float:3.76E-43)
            byte[] r13 = new byte[r13]
            r14 = 0
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r8 = "rat="
            r15.append(r8)
            r15.append(r9)
            java.lang.String r8 = ", isVoLte="
            r15.append(r8)
            r15.append(r0)
            java.lang.String r8 = ", isSmsIp="
            r15.append(r8)
            r15.append(r1)
            java.lang.String r8 = ", isRcs="
            r15.append(r8)
            r15.append(r2)
            java.lang.String r8 = ", isPsVT="
            r15.append(r8)
            r15.append(r3)
            java.lang.String r8 = ", isCdpn="
            r15.append(r8)
            r15.append(r4)
            java.lang.String r8 = ", ecmp="
            r15.append(r8)
            r15.append(r5)
            java.lang.String r8 = r15.toString()
            java.lang.String r15 = "IilIpcMessage"
            com.sec.internal.log.IMSLog.i(r15, r8)
            if (r0 == 0) goto L_0x0082
            r14 = r14 | 1
        L_0x0082:
            if (r1 == 0) goto L_0x0086
            r14 = r14 | 2
        L_0x0086:
            if (r2 == 0) goto L_0x008a
            r14 = r14 | 4
        L_0x008a:
            if (r3 == 0) goto L_0x008e
            r14 = r14 | 8
        L_0x008e:
            if (r4 == 0) goto L_0x0092
            r14 = r14 | 32
        L_0x0092:
            r8 = 0
            int r15 = r8 + 1
            r0 = r17
            byte r1 = (byte) r0
            r13[r8] = r1
            int r1 = r15 + 1
            byte r8 = (byte) r14
            r13[r15] = r8
            int r8 = r1 + 1
            r15 = r23
            byte r0 = (byte) r15
            r13[r1] = r0
            int r0 = r8 + 1
            r1 = r24
            byte r2 = (byte) r1
            r13[r8] = r2
            int r2 = r0 + 1
            byte r8 = (byte) r5
            r13[r0] = r8
            int r0 = r2 + 1
            r8 = r26
            byte r1 = (byte) r8
            r13[r2] = r1
            int r1 = r0 + 1
            int r2 = r6 >> 8
            byte r2 = (byte) r2
            r13[r0] = r2
            int r0 = r1 + 1
            byte r2 = (byte) r6
            r13[r1] = r2
            int r1 = r0 + 1
            int r2 = r7 >> 8
            byte r2 = (byte) r2
            r13[r0] = r2
            int r0 = r1 + 1
            byte r2 = (byte) r7
            r13[r1] = r2
            int r1 = r0 + 1
            byte r2 = (byte) r12
            r13[r0] = r2
            r0 = 0
        L_0x00d7:
            if (r0 >= r12) goto L_0x00e3
            int r2 = r1 + 1
            byte r16 = r11[r0]
            r13[r1] = r16
            int r0 = r0 + 1
            r1 = r2
            goto L_0x00d7
        L_0x00e3:
            r0 = 267(0x10b, float:3.74E-43)
            if (r1 >= r0) goto L_0x00ee
            int r0 = r1 + 1
            r2 = 0
            r13[r1] = r2
            r1 = r0
            goto L_0x00e3
        L_0x00ee:
            byte r2 = (byte) r9
            r13[r0] = r2
            r10.mIpcBody = r13
            r10.createIpcMessage()
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.iil.IilIpcMessage.encodeImsRegisgtrationInfo(int, boolean, boolean, boolean, boolean, boolean, int, int, int, int, int, int, java.lang.String, int):com.sec.internal.ims.core.iil.IilIpcMessage");
    }

    public static IilIpcMessage encodeImsPreferenceNoti(IilImsPreference imsPreference, int notiType) {
        IilIpcMessage msg = new IilIpcMessage(112, 6, 3);
        IMSLog.i(LOG_TAG, imsPreference.toString() + "NotiType : " + notiType);
        msg.mIpcBody = IilImsPreference.toByteArray(imsPreference, notiType);
        msg.createIpcMessage();
        return msg;
    }

    public static IilIpcMessage encodeImsPreferenceResp(IilImsPreference imsPreference) {
        IilIpcMessage msg = new IilIpcMessage(112, 6, 2);
        IMSLog.i(LOG_TAG, imsPreference.toString());
        msg.mIpcBody = IilImsPreference.toByteArray(imsPreference, 0);
        msg.createIpcMessage();
        return msg;
    }

    public static IilIpcMessage encodeImsRetryOverNoti(int regStatus, boolean isVoLte, boolean isSmsIp, boolean isRcs, boolean isPsVT, boolean isCdpn, int networkType, int ecmp) {
        IilIpcMessage msg = new IilIpcMessage(112, 12, 3);
        byte[] data = new byte[4];
        int type = 0;
        IMSLog.i(LOG_TAG, "isVoLte " + isVoLte + " isSmsIp " + isSmsIp + " isRcs " + isRcs + " isPsVT " + isPsVT + " isCdpn " + isCdpn + " ecmp" + ecmp);
        if (isVoLte) {
            type = 0 | 1;
        }
        if (isSmsIp) {
            type |= 2;
        }
        if (isRcs) {
            type |= 4;
        }
        if (isPsVT) {
            type |= 8;
        }
        if (isCdpn) {
            type |= 32;
        }
        data[0] = (byte) regStatus;
        data[1] = (byte) type;
        data[2] = (byte) networkType;
        data[3] = (byte) ecmp;
        msg.mIpcBody = data;
        msg.createIpcMessage();
        return msg;
    }

    public static IilIpcMessage ImsChangePreferredNetwork() {
        IilIpcMessage msg = new IilIpcMessage(112, 21, 3);
        IMSLog.i(LOG_TAG, "ImsChangePreferredNetwork()");
        msg.mIpcBody = null;
        msg.createIpcMessage();
        return msg;
    }
}
