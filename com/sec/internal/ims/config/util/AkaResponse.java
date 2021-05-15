package com.sec.internal.ims.config.util;

import java.util.Arrays;

public class AkaResponse {
    private final byte[] auts;
    private final byte[] ck;
    private final byte[] ik;
    private final byte[] res;

    public AkaResponse(byte[] ck2, byte[] ik2, byte[] auts2, byte[] res2) {
        this.ck = ck2;
        this.ik = ik2;
        this.auts = auts2;
        this.res = res2;
    }

    public byte[] getCk() {
        return this.ck;
    }

    public byte[] getIk() {
        return this.ik;
    }

    public byte[] getAuts() {
        return this.auts;
    }

    public byte[] getRes() {
        return this.res;
    }

    public String toString() {
        return "AkaResponse [ck=" + Arrays.toString(this.ck) + ", ik=" + Arrays.toString(this.ik) + ", auts=" + Arrays.toString(this.auts) + ", res=" + Arrays.toString(this.res) + "]";
    }
}
