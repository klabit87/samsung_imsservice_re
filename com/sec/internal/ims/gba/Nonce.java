package com.sec.internal.ims.gba;

import android.util.Base64;
import android.util.Log;
import com.sec.internal.helper.StrUtil;
import java.util.Arrays;

public class Nonce {
    private static final int AUTN_SIZE = 16;
    private static final int RAND_SIZE = 16;
    private static final String TAG = "Nonce";
    private byte[] autn;
    private byte[] rand;
    private byte[] randAutn;
    private byte[] serverData;
    private String strNonce;

    public byte[] getAutn() {
        return this.autn;
    }

    public void setAutn(byte[] autn2) {
        this.autn = autn2;
    }

    public byte[] getRand() {
        return this.rand;
    }

    public void setRand(byte[] rand2) {
        this.rand = rand2;
    }

    public void setServerData(byte[] serverData2) {
        this.serverData = serverData2;
    }

    public void setStrNonce(String strNonce2) {
        this.strNonce = strNonce2;
    }

    public String getStrNonce() {
        return this.strNonce;
    }

    public byte[] getAutnRand() {
        return this.randAutn;
    }

    public void setAutnRand(byte[] autnRand) {
        this.randAutn = autnRand;
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        StringBuilder sb = new StringBuilder();
        sb.append("Nonce [");
        String str5 = "";
        if (this.autn != null) {
            str = "autn=" + Arrays.toString(this.autn) + ", ";
        } else {
            str = str5;
        }
        sb.append(str);
        if (this.rand != null) {
            str2 = "rand=" + Arrays.toString(this.rand) + ", ";
        } else {
            str2 = str5;
        }
        sb.append(str2);
        if (this.serverData != null) {
            str3 = "serverData=" + Arrays.toString(this.serverData) + ", ";
        } else {
            str3 = str5;
        }
        sb.append(str3);
        if (this.strNonce != null) {
            str4 = "strNonce=" + this.strNonce + ", ";
        } else {
            str4 = str5;
        }
        sb.append(str4);
        if (this.randAutn != null) {
            str5 = "autnRand=" + Arrays.toString(this.randAutn);
        }
        sb.append(str5);
        sb.append("]");
        return sb.toString();
    }

    public void parseNonce(String nonce) {
        byte[] nonceCompR = new byte[17];
        byte[] nonceCompA = new byte[17];
        byte[] randAutn2 = new byte[34];
        setStrNonce(nonce);
        byte[] decodedNonce = Base64.decode(nonce, 0);
        if (decodedNonce.length >= 16) {
            nonceCompR[0] = 16;
            System.arraycopy(decodedNonce, 0, nonceCompR, 1, 16);
            setRand(nonceCompR);
            System.arraycopy(getRand(), 0, randAutn2, 0, 17);
        }
        Log.i(TAG, "HexRAND is: " + StrUtil.bytesToHexString(getRand()));
        if (decodedNonce.length >= 32) {
            nonceCompA[0] = 16;
            System.arraycopy(decodedNonce, 16, nonceCompA, 1, 16);
            setAutn(nonceCompA);
            System.arraycopy(getAutn(), 0, randAutn2, 17, 17);
        }
        Log.i(TAG, "Hex Autn is: " + StrUtil.bytesToHexString(getAutn()));
        if (decodedNonce.length > 32) {
            setServerData(Arrays.copyOfRange(decodedNonce, 32, decodedNonce.length - 1));
        }
        Log.i(TAG, "Hex RandAutn is: " + StrUtil.bytesToHexString(randAutn2));
        setAutnRand(randAutn2);
        Log.d(TAG, toString());
    }
}
