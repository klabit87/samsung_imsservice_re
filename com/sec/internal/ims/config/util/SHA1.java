package com.sec.internal.ims.config.util;

public class SHA1 {
    protected int H0;
    protected int H1;
    protected int H2;
    protected int H3;
    protected int H4;
    private long currentLen;
    private int currentPos;
    private final int[] w = new int[80];

    public SHA1() {
        reset();
    }

    public final void reset() {
        this.H0 = 1732584193;
        this.H1 = -271733879;
        this.H2 = -1732584194;
        this.H3 = 271733878;
        this.H4 = -1009589776;
        this.currentPos = 0;
        this.currentLen = 0;
    }

    public final void update(byte[] b) {
        update(b, 0, b.length);
    }

    public final void update(byte[] b, int off, int len) {
        int off2;
        int len2 = len;
        if (len2 >= 4) {
            int i = this.currentPos;
            int idx = i >> 2;
            int i2 = i & 3;
            if (i2 == 0) {
                int[] iArr = this.w;
                int off3 = off + 1;
                int off4 = off3 + 1;
                int off5 = off4 + 1;
                byte b2 = ((b[off3] & 255) << 16) | ((b[off] & 255) << 24) | ((b[off4] & 255) << 8);
                off2 = off5 + 1;
                iArr[idx] = b2 | (b[off5] & 255);
                len2 -= 4;
                int i3 = i + 4;
                this.currentPos = i3;
                this.currentLen += 32;
                if (i3 == 64) {
                    perform();
                    this.currentPos = 0;
                }
            } else if (i2 == 1) {
                int[] iArr2 = this.w;
                int off6 = off + 1;
                int off7 = off6 + 1;
                int off8 = off7 + 1;
                iArr2[idx] = (b[off7] & 255) | ((b[off6] & 255) << 8) | ((b[off] & 255) << 16) | (iArr2[idx] << 24);
                len2 -= 3;
                int i4 = i + 3;
                this.currentPos = i4;
                this.currentLen += 24;
                if (i4 == 64) {
                    perform();
                    this.currentPos = 0;
                }
                off2 = off8;
            } else if (i2 == 2) {
                int[] iArr3 = this.w;
                int off9 = off + 1;
                int off10 = off9 + 1;
                iArr3[idx] = (iArr3[idx] << 16) | (b[off9] & 255) | ((b[off] & 255) << 8);
                len2 -= 2;
                int i5 = i + 2;
                this.currentPos = i5;
                this.currentLen += 16;
                if (i5 == 64) {
                    perform();
                    this.currentPos = 0;
                }
                off2 = off10;
            } else if (i2 != 3) {
                off2 = off;
            } else {
                int[] iArr4 = this.w;
                off2 = off + 1;
                iArr4[idx] = (iArr4[idx] << 8) | (b[off] & 255);
                len2--;
                int i6 = i + 1;
                this.currentPos = i6;
                this.currentLen += 8;
                if (i6 == 64) {
                    perform();
                    this.currentPos = 0;
                }
            }
            while (len2 >= 8) {
                int[] iArr5 = this.w;
                int i7 = this.currentPos;
                int off11 = off2 + 1;
                int off12 = off11 + 1;
                byte b3 = ((b[off11] & 255) << 16) | ((b[off2] & 255) << 24);
                int off13 = off12 + 1;
                byte b4 = b3 | ((b[off12] & 255) << 8);
                int off14 = off13 + 1;
                iArr5[i7 >> 2] = b4 | (b[off13] & 255);
                int i8 = i7 + 4;
                this.currentPos = i8;
                if (i8 == 64) {
                    perform();
                    this.currentPos = 0;
                }
                int[] iArr6 = this.w;
                int i9 = this.currentPos;
                int off15 = off14 + 1;
                int off16 = off15 + 1;
                int off17 = off16 + 1;
                byte b5 = ((b[off15] & 255) << 16) | ((b[off14] & 255) << 24) | ((b[off16] & 255) << 8);
                int off18 = off17 + 1;
                iArr6[i9 >> 2] = b5 | (b[off17] & 255);
                int i10 = i9 + 4;
                this.currentPos = i10;
                if (i10 == 64) {
                    perform();
                    this.currentPos = 0;
                }
                this.currentLen += 64;
                len2 -= 8;
                off2 = off18;
            }
        } else {
            off2 = off;
        }
        while (len2 > 0) {
            int i11 = this.currentPos;
            int idx2 = i11 >> 2;
            int[] iArr7 = this.w;
            int off19 = off2 + 1;
            iArr7[idx2] = (iArr7[idx2] << 8) | (b[off2] & 255);
            this.currentLen += 8;
            int i12 = i11 + 1;
            this.currentPos = i12;
            if (i12 == 64) {
                perform();
                this.currentPos = 0;
            }
            len2--;
            off2 = off19;
        }
    }

    public final void update(byte b) {
        int i = this.currentPos;
        int idx = i >> 2;
        int[] iArr = this.w;
        iArr[idx] = (iArr[idx] << 8) | (b & 255);
        this.currentLen += 8;
        int i2 = i + 1;
        this.currentPos = i2;
        if (i2 == 64) {
            perform();
            this.currentPos = 0;
        }
    }

    private final void putInt(byte[] b, int pos, int val) {
        b[pos] = (byte) (val >> 24);
        b[pos + 1] = (byte) (val >> 16);
        b[pos + 2] = (byte) (val >> 8);
        b[pos + 3] = (byte) val;
    }

    public final void digest(byte[] out) {
        digest(out, 0);
    }

    public final void digest(byte[] out, int off) {
        int i = this.currentPos;
        int idx = i >> 2;
        int[] iArr = this.w;
        iArr[idx] = ((iArr[idx] << 8) | 128) << ((3 - (i & 3)) << 3);
        int i2 = (i & -4) + 4;
        this.currentPos = i2;
        if (i2 == 64) {
            this.currentPos = 0;
            perform();
        } else if (i2 == 60) {
            this.currentPos = 0;
            iArr[15] = 0;
            perform();
        }
        for (int i3 = this.currentPos >> 2; i3 < 14; i3++) {
            this.w[i3] = 0;
        }
        int[] iArr2 = this.w;
        long j = this.currentLen;
        iArr2[14] = (int) (j >> 32);
        iArr2[15] = (int) j;
        perform();
        putInt(out, off, this.H0);
        putInt(out, off + 4, this.H1);
        putInt(out, off + 8, this.H2);
        putInt(out, off + 12, this.H3);
        putInt(out, off + 16, this.H4);
    }

    private final void perform() {
        for (int t = 16; t < 80; t++) {
            int[] iArr = this.w;
            int x = ((iArr[t - 3] ^ iArr[t - 8]) ^ iArr[t - 14]) ^ iArr[t - 16];
            iArr[t] = (x << 1) | (x >>> 31);
        }
        int t2 = this.H0;
        int B = this.H1;
        int C = this.H2;
        int D = this.H3;
        int E = this.H4;
        int i = ((t2 << 5) | (t2 >>> 27)) + ((B & C) | ((~B) & D));
        int[] iArr2 = this.w;
        int E2 = E + i + iArr2[0] + 1518500249;
        int B2 = (B << 30) | (B >>> 2);
        int D2 = D + ((E2 << 5) | (E2 >>> 27)) + ((t2 & B2) | ((~t2) & C)) + iArr2[1] + 1518500249;
        int A = (t2 << 30) | (t2 >>> 2);
        int C2 = C + ((D2 << 5) | (D2 >>> 27)) + ((E2 & A) | ((~E2) & B2)) + iArr2[2] + 1518500249;
        int E3 = (E2 << 30) | (E2 >>> 2);
        int B3 = B2 + ((C2 << 5) | (C2 >>> 27)) + ((D2 & E3) | ((~D2) & A)) + iArr2[3] + 1518500249;
        int D3 = (D2 << 30) | (D2 >>> 2);
        int A2 = A + ((B3 << 5) | (B3 >>> 27)) + ((C2 & D3) | ((~C2) & E3)) + iArr2[4] + 1518500249;
        int C3 = (C2 << 30) | (C2 >>> 2);
        int E4 = E3 + ((A2 << 5) | (A2 >>> 27)) + ((B3 & C3) | ((~B3) & D3)) + iArr2[5] + 1518500249;
        int B4 = (B3 << 30) | (B3 >>> 2);
        int D4 = D3 + ((E4 << 5) | (E4 >>> 27)) + ((A2 & B4) | ((~A2) & C3)) + iArr2[6] + 1518500249;
        int A3 = (A2 << 30) | (A2 >>> 2);
        int C4 = C3 + ((D4 << 5) | (D4 >>> 27)) + ((E4 & A3) | ((~E4) & B4)) + iArr2[7] + 1518500249;
        int E5 = (E4 << 30) | (E4 >>> 2);
        int B5 = B4 + ((C4 << 5) | (C4 >>> 27)) + ((D4 & E5) | ((~D4) & A3)) + iArr2[8] + 1518500249;
        int D5 = (D4 << 30) | (D4 >>> 2);
        int A4 = A3 + ((B5 << 5) | (B5 >>> 27)) + ((C4 & D5) | ((~C4) & E5)) + iArr2[9] + 1518500249;
        int C5 = (C4 << 30) | (C4 >>> 2);
        int E6 = E5 + ((A4 << 5) | (A4 >>> 27)) + ((B5 & C5) | ((~B5) & D5)) + iArr2[10] + 1518500249;
        int B6 = (B5 << 30) | (B5 >>> 2);
        int D6 = D5 + ((E6 << 5) | (E6 >>> 27)) + ((A4 & B6) | ((~A4) & C5)) + iArr2[11] + 1518500249;
        int A5 = (A4 << 30) | (A4 >>> 2);
        int C6 = C5 + ((D6 << 5) | (D6 >>> 27)) + ((E6 & A5) | ((~E6) & B6)) + iArr2[12] + 1518500249;
        int E7 = (E6 << 30) | (E6 >>> 2);
        int B7 = B6 + ((C6 << 5) | (C6 >>> 27)) + ((D6 & E7) | ((~D6) & A5)) + iArr2[13] + 1518500249;
        int D7 = (D6 << 30) | (D6 >>> 2);
        int A6 = A5 + ((B7 << 5) | (B7 >>> 27)) + ((C6 & D7) | ((~C6) & E7)) + iArr2[14] + 1518500249;
        int C7 = (C6 << 30) | (C6 >>> 2);
        int E8 = E7 + ((A6 << 5) | (A6 >>> 27)) + ((B7 & C7) | ((~B7) & D7)) + iArr2[15] + 1518500249;
        int B8 = (B7 << 30) | (B7 >>> 2);
        int D8 = D7 + ((E8 << 5) | (E8 >>> 27)) + ((A6 & B8) | ((~A6) & C7)) + iArr2[16] + 1518500249;
        int A7 = (A6 << 30) | (A6 >>> 2);
        int C8 = C7 + ((D8 << 5) | (D8 >>> 27)) + ((E8 & A7) | ((~E8) & B8)) + iArr2[17] + 1518500249;
        int E9 = (E8 << 30) | (E8 >>> 2);
        int B9 = B8 + ((C8 << 5) | (C8 >>> 27)) + ((D8 & E9) | ((~D8) & A7)) + iArr2[18] + 1518500249;
        int D9 = (D8 << 30) | (D8 >>> 2);
        int A8 = A7 + ((B9 << 5) | (B9 >>> 27)) + ((C8 & D9) | ((~C8) & E9)) + iArr2[19] + 1518500249;
        int C9 = (C8 << 30) | (C8 >>> 2);
        int E10 = E9 + ((A8 << 5) | (A8 >>> 27)) + ((B9 ^ C9) ^ D9) + iArr2[20] + 1859775393;
        int B10 = (B9 << 30) | (B9 >>> 2);
        int D10 = D9 + ((E10 << 5) | (E10 >>> 27)) + ((A8 ^ B10) ^ C9) + iArr2[21] + 1859775393;
        int A9 = (A8 << 30) | (A8 >>> 2);
        int C10 = C9 + ((D10 << 5) | (D10 >>> 27)) + ((E10 ^ A9) ^ B10) + iArr2[22] + 1859775393;
        int E11 = (E10 << 30) | (E10 >>> 2);
        int B11 = B10 + ((C10 << 5) | (C10 >>> 27)) + ((D10 ^ E11) ^ A9) + iArr2[23] + 1859775393;
        int D11 = (D10 << 30) | (D10 >>> 2);
        int A10 = A9 + ((B11 << 5) | (B11 >>> 27)) + ((C10 ^ D11) ^ E11) + iArr2[24] + 1859775393;
        int C11 = (C10 << 30) | (C10 >>> 2);
        int E12 = E11 + ((A10 << 5) | (A10 >>> 27)) + ((B11 ^ C11) ^ D11) + iArr2[25] + 1859775393;
        int B12 = (B11 << 30) | (B11 >>> 2);
        int D12 = D11 + ((E12 << 5) | (E12 >>> 27)) + ((A10 ^ B12) ^ C11) + iArr2[26] + 1859775393;
        int A11 = (A10 << 30) | (A10 >>> 2);
        int C12 = C11 + ((D12 << 5) | (D12 >>> 27)) + ((E12 ^ A11) ^ B12) + iArr2[27] + 1859775393;
        int E13 = (E12 << 30) | (E12 >>> 2);
        int B13 = B12 + ((C12 << 5) | (C12 >>> 27)) + ((D12 ^ E13) ^ A11) + iArr2[28] + 1859775393;
        int D13 = (D12 << 30) | (D12 >>> 2);
        int A12 = A11 + ((B13 << 5) | (B13 >>> 27)) + ((C12 ^ D13) ^ E13) + iArr2[29] + 1859775393;
        int C13 = (C12 << 30) | (C12 >>> 2);
        int E14 = E13 + ((A12 << 5) | (A12 >>> 27)) + ((B13 ^ C13) ^ D13) + iArr2[30] + 1859775393;
        int B14 = (B13 << 30) | (B13 >>> 2);
        int D14 = D13 + ((E14 << 5) | (E14 >>> 27)) + ((A12 ^ B14) ^ C13) + iArr2[31] + 1859775393;
        int A13 = (A12 << 30) | (A12 >>> 2);
        int C14 = C13 + ((D14 << 5) | (D14 >>> 27)) + ((E14 ^ A13) ^ B14) + iArr2[32] + 1859775393;
        int E15 = (E14 << 30) | (E14 >>> 2);
        int B15 = B14 + ((C14 << 5) | (C14 >>> 27)) + ((D14 ^ E15) ^ A13) + iArr2[33] + 1859775393;
        int D15 = (D14 << 30) | (D14 >>> 2);
        int A14 = A13 + ((B15 << 5) | (B15 >>> 27)) + ((C14 ^ D15) ^ E15) + iArr2[34] + 1859775393;
        int C15 = (C14 << 30) | (C14 >>> 2);
        int E16 = E15 + ((A14 << 5) | (A14 >>> 27)) + ((B15 ^ C15) ^ D15) + iArr2[35] + 1859775393;
        int B16 = (B15 << 30) | (B15 >>> 2);
        int D16 = D15 + ((E16 << 5) | (E16 >>> 27)) + ((A14 ^ B16) ^ C15) + iArr2[36] + 1859775393;
        int A15 = (A14 << 30) | (A14 >>> 2);
        int C16 = C15 + ((D16 << 5) | (D16 >>> 27)) + ((E16 ^ A15) ^ B16) + iArr2[37] + 1859775393;
        int E17 = (E16 << 30) | (E16 >>> 2);
        int B17 = B16 + ((C16 << 5) | (C16 >>> 27)) + ((D16 ^ E17) ^ A15) + iArr2[38] + 1859775393;
        int D17 = (D16 << 30) | (D16 >>> 2);
        int A16 = A15 + ((B17 << 5) | (B17 >>> 27)) + ((C16 ^ D17) ^ E17) + iArr2[39] + 1859775393;
        int C17 = (C16 << 30) | (C16 >>> 2);
        int E18 = E17 + (((((A16 << 5) | (A16 >>> 27)) + (((B17 & C17) | (B17 & D17)) | (C17 & D17))) + iArr2[40]) - 1894007588);
        int B18 = (B17 << 30) | (B17 >>> 2);
        int D18 = D17 + (((((E18 << 5) | (E18 >>> 27)) + (((A16 & B18) | (A16 & C17)) | (B18 & C17))) + iArr2[41]) - 1894007588);
        int A17 = (A16 << 30) | (A16 >>> 2);
        int C18 = C17 + (((((D18 << 5) | (D18 >>> 27)) + (((E18 & A17) | (E18 & B18)) | (A17 & B18))) + iArr2[42]) - 1894007588);
        int E19 = (E18 << 30) | (E18 >>> 2);
        int B19 = ((int) B18) + (((((C18 << 5) | (C18 >>> 27)) + (((D18 & E19) | (D18 & A17)) | (E19 & A17))) + iArr2[43]) - 1894007588);
        int D19 = (D18 << 30) | (D18 >>> 2);
        int A18 = ((int) A17) + (((((B19 << 5) | (B19 >>> 27) ? 1 : 0) + ((int) (((C18 & D19) | (C18 & E19)) | (D19 & E19)))) + iArr2[44]) - 1894007588);
        int C19 = (C18 << 30) | (C18 >>> 2);
        int E20 = ((int) E19) + (((((A18 << 5) | (A18 >>> 27) ? 1 : 0) + ((int) (((B19 & C19) | (B19 & D19)) | (C19 & D19)))) + iArr2[45]) - 1894007588);
        int B20 = (B19 << 30) | (B19 >>> 2);
        int D20 = ((int) D19) + (((((E20 << 5) | (E20 >>> 27) ? 1 : 0) + ((int) (((A18 & B20) | (A18 & C19)) | (B20 & C19)))) + iArr2[46]) - 1894007588);
        int A19 = (A18 << 30) | (A18 >>> 2);
        int C20 = ((int) C19) + (((((D20 << 5) | (D20 >>> 27) ? 1 : 0) + ((int) (((E20 & A19) | (E20 & B20)) | (A19 & B20)))) + iArr2[47]) - 1894007588);
        int E21 = (E20 << 30) | (E20 >>> 2);
        int B21 = ((int) B20) + (((((C20 << 5) | (C20 >>> 27) ? 1 : 0) + ((int) (((D20 & E21) | (D20 & A19)) | (E21 & A19)))) + iArr2[48]) - 1894007588);
        int D21 = (D20 << 30) | (D20 >>> 2);
        int A20 = ((int) A19) + (((((B21 << 5) | (B21 >>> 27) ? 1 : 0) + ((int) (((C20 & D21) | (C20 & E21)) | (D21 & E21)))) + iArr2[49]) - 1894007588);
        int C21 = (C20 << 30) | (C20 >>> 2);
        int E22 = ((int) E21) + (((((A20 << 5) | (A20 >>> 27) ? 1 : 0) + ((int) (((B21 & C21) | (B21 & D21)) | (C21 & D21)))) + iArr2[50]) - 1894007588);
        int B22 = (B21 << 30) | (B21 >>> 2);
        int D22 = ((int) D21) + (((((E22 << 5) | (E22 >>> 27) ? 1 : 0) + ((int) (((A20 & B22) | (A20 & C21)) | (B22 & C21)))) + iArr2[51]) - 1894007588);
        int A21 = (A20 << 30) | (A20 >>> 2);
        int C22 = ((int) C21) + (((((D22 << 5) | (D22 >>> 27) ? 1 : 0) + ((int) (((E22 & A21) | (E22 & B22)) | (A21 & B22)))) + iArr2[52]) - 1894007588);
        int E23 = (E22 << 30) | (E22 >>> 2);
        int B23 = ((int) B22) + (((((C22 << 5) | (C22 >>> 27) ? 1 : 0) + ((int) (((D22 & E23) | (D22 & A21)) | (E23 & A21)))) + iArr2[53]) - 1894007588);
        int D23 = (D22 << 30) | (D22 >>> 2);
        int A22 = ((int) A21) + (((((B23 << 5) | (B23 >>> 27) ? 1 : 0) + ((int) (((C22 & D23) | (C22 & E23)) | (D23 & E23)))) + iArr2[54]) - 1894007588);
        int C23 = (C22 << 30) | (C22 >>> 2);
        int E24 = (((((A22 << 5) | (A22 >>> 27) ? 1 : 0) + ((int) E23)) + ((int) (((B23 & C23) | (B23 & D23)) | (C23 & D23)))) + iArr2[55]) - 1894007588;
        int B24 = (B23 << 30) | (B23 >>> 2);
        int D24 = ((int) D23) + (((((E24 << 5) | (E24 >>> 27) ? 1 : 0) + ((int) (((A22 & B24) | (A22 & C23)) | (B24 & C23)))) + iArr2[56]) - 1894007588);
        int A23 = (A22 << 30) | (A22 >>> 2);
        int C24 = ((int) C23) + (((((D24 << 5) | (D24 >>> 27) ? 1 : 0) + ((int) (((E24 & A23) | (E24 & B24)) | (A23 & B24)))) + iArr2[57]) - 1894007588);
        int E25 = (E24 << 30) | (E24 >>> 2);
        int B25 = ((int) B24) + (((((C24 << 5) | (C24 >>> 27) ? 1 : 0) + ((int) (((D24 & E25) | (D24 & A23)) | (E25 & A23)))) + iArr2[58]) - 1894007588);
        int D25 = (D24 << 30) | (D24 >>> 2);
        int A24 = ((int) A23) + (((((B25 << 5) | (B25 >>> 27) ? 1 : 0) + ((int) (((C24 & D25) | (C24 & E25)) | (D25 & E25)))) + iArr2[59]) - 1894007588);
        int C25 = (C24 << 30) | (C24 >>> 2);
        int E26 = ((int) E25) + (((((A24 << 5) | (A24 >>> 27) ? 1 : 0) + ((int) ((B25 ^ C25) ^ D25))) + iArr2[60]) - 899497514);
        int B26 = (B25 << 30) | (B25 >>> 2);
        int D26 = ((int) D25) + (((((E26 << 5) | (E26 >>> 27) ? 1 : 0) + ((int) ((A24 ^ B26) ^ C25))) + iArr2[61]) - 899497514);
        int A25 = (A24 << 30) | (A24 >>> 2);
        int C26 = ((int) C25) + (((((D26 << 5) | (D26 >>> 27) ? 1 : 0) + ((int) ((E26 ^ A25) ^ B26))) + iArr2[62]) - 899497514);
        int E27 = (E26 << 30) | (E26 >>> 2);
        int B27 = ((int) B26) + (((((C26 << 5) | (C26 >>> 27) ? 1 : 0) + ((int) ((D26 ^ E27) ^ A25))) + iArr2[63]) - 899497514);
        int D27 = (D26 << 30) | (D26 >>> 2);
        int A26 = ((int) A25) + (((((B27 << 5) | (B27 >>> 27) ? 1 : 0) + ((int) ((C26 ^ D27) ^ E27))) + iArr2[64]) - 899497514);
        int C27 = (C26 << 30) | (C26 >>> 2);
        int E28 = ((int) E27) + (((((A26 << 5) | (A26 >>> 27) ? 1 : 0) + ((int) ((B27 ^ C27) ^ D27))) + iArr2[65]) - 899497514);
        int B28 = (B27 << 30) | (B27 >>> 2);
        int D28 = ((int) D27) + (((((E28 << 5) | (E28 >>> 27) ? 1 : 0) + ((int) ((A26 ^ B28) ^ C27))) + iArr2[66]) - 899497514);
        int A27 = (A26 << 30) | (A26 >>> 2);
        int C28 = ((int) C27) + (((((D28 << 5) | (D28 >>> 27) ? 1 : 0) + ((int) ((E28 ^ A27) ^ B28))) + iArr2[67]) - 899497514);
        int E29 = (E28 << 30) | (E28 >>> 2);
        int B29 = ((int) B28) + (((((C28 << 5) | (C28 >>> 27) ? 1 : 0) + ((int) ((D28 ^ E29) ^ A27))) + iArr2[68]) - 899497514);
        int D29 = (D28 << 30) | (D28 >>> 2);
        int A28 = ((int) A27) + (((((B29 << 5) | (B29 >>> 27) ? 1 : 0) + ((int) ((C28 ^ D29) ^ E29))) + iArr2[69]) - 899497514);
        int C29 = (C28 << 30) | (C28 >>> 2);
        int E30 = ((int) E29) + (((((A28 << 5) | (A28 >>> 27) ? 1 : 0) + ((int) ((B29 ^ C29) ^ D29))) + iArr2[70]) - 899497514);
        int B30 = (B29 << 30) | (B29 >>> 2);
        int D30 = ((int) D29) + (((((E30 << 5) | (E30 >>> 27) ? 1 : 0) + ((int) ((A28 ^ B30) ^ C29))) + iArr2[71]) - 899497514);
        int A29 = (A28 << 30) | (A28 >>> 2);
        int C30 = ((int) C29) + (((((D30 << 5) | (D30 >>> 27) ? 1 : 0) + ((int) ((E30 ^ A29) ^ B30))) + iArr2[72]) - 899497514);
        int E31 = (E30 << 30) | (E30 >>> 2);
        int B31 = ((int) B30) + (((((C30 << 5) | (C30 >>> 27) ? 1 : 0) + ((int) ((D30 ^ E31) ^ A29))) + iArr2[73]) - 899497514);
        int D31 = (D30 << 30) | (D30 >>> 2);
        int A30 = ((int) A29) + (((((B31 << 5) | (B31 >>> 27) ? 1 : 0) + ((int) ((C30 ^ D31) ^ E31))) + iArr2[74]) - 899497514);
        int C31 = (C30 << 30) | (C30 >>> 2);
        int E32 = ((int) E31) + (((((A30 << 5) | (A30 >>> 27) ? 1 : 0) + ((int) ((B31 ^ C31) ^ D31))) + iArr2[75]) - 899497514);
        int B32 = (B31 << 30) | (B31 >>> 2);
        int D32 = ((int) D31) + (((((E32 << 5) | (E32 >>> 27) ? 1 : 0) + ((int) ((A30 ^ B32) ^ C31))) + iArr2[76]) - 899497514);
        int A31 = (A30 << 30) | (A30 >>> 2);
        int C32 = ((int) C31) + (((((D32 << 5) | (D32 >>> 27) ? 1 : 0) + ((int) ((E32 ^ A31) ^ B32))) + iArr2[77]) - 899497514);
        int E33 = (E32 << 30) | (E32 >>> 2);
        int B33 = ((int) B32) + (((((C32 << 5) | (C32 >>> 27) ? 1 : 0) + ((int) ((D32 ^ E33) ^ A31))) + iArr2[78]) - 899497514);
        int D33 = (D32 << 30) | (D32 >>> 2);
        this.H0 += ((int) A31) + (((((B33 << 5) | (B33 >>> 27) ? 1 : 0) + ((int) ((C32 ^ D33) ^ E33))) + iArr2[79]) - 899497514);
        this.H1 += B33;
        this.H2 += (C32 << 30) | (C32 >>> 2);
        this.H3 += (int) D33;
        this.H4 += (int) E33;
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append("0123456789ABCDEF".charAt((b[i] >> 4) & 15));
            sb.append("0123456789ABCDEF".charAt(b[i] & 15));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SHA1 sha = new SHA1();
        byte[] dig1 = new byte[20];
        byte[] dig2 = new byte[20];
        byte[] dig3 = new byte[20];
        sha.update("abc".getBytes());
        sha.digest(dig1);
        sha.update("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes());
        sha.digest(dig2);
        for (int i = 0; i < 1000000; i++) {
            sha.update((byte) 97);
        }
        sha.digest(dig3);
        String dig1_res = toHexString(dig1);
        String dig2_res = toHexString(dig2);
        String dig3_res = toHexString(dig3);
        if (dig1_res.equals("A9993E364706816ABA3E25717850C26C9CD0D89D")) {
            System.out.println("SHA-1 Test 1 OK.");
        } else {
            System.out.println("SHA-1 Test 1 FAILED.");
        }
        if (dig2_res.equals("84983E441C3BD26EBAAE4AA1F95129E5E54670F1")) {
            System.out.println("SHA-1 Test 2 OK.");
        } else {
            System.out.println("SHA-1 Test 2 FAILED.");
        }
        if (dig3_res.equals("34AA973CD4C4DAA4F61EEB2BDBAD27316534016F")) {
            System.out.println("SHA-1 Test 3 OK.");
        } else {
            System.out.println("SHA-1 Test 3 FAILED.");
        }
        if (dig3_res.equals("34AA973CD4C4DAA4F61EEB2BDBAD27316534016F")) {
            System.out.println("SHA-1 Test 3 OK.");
        } else {
            System.out.println("SHA-1 Test 3 FAILED.");
        }
    }
}
