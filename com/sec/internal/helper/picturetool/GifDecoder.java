package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Vector;

public class GifDecoder {
    public static final String LOG_TAG = GifDecoder.class.getSimpleName();
    private static final int MAX_FRAMES = 50;
    protected static final int MAX_STACK_SIZE = 4096;
    public static final int STATUS_FORMAT_ERROR = 1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_OPEN_ERROR = 2;
    protected int[] act;
    protected int bgColor;
    protected int bgIndex;
    protected byte[] block = new byte[256];
    protected int blockSize = 0;
    protected int delay = 0;
    protected int dispose = 0;
    protected int frameCount;
    protected Vector<GifFrame> frames;
    protected int[] gct;
    protected boolean gctFlag;
    protected int gctSize;
    protected int height;
    protected int ih;
    protected Bitmap image;
    protected InputStream in;
    protected boolean interlace;
    protected int iw;
    protected int ix;
    protected int iy;
    protected int lastBgColor;
    protected Bitmap lastBitmap;
    protected int lastDispose = 0;
    protected int[] lct;
    protected boolean lctFlag;
    protected int lctSize;
    protected int loopCount = 1;
    protected int lrh;
    protected int lrw;
    protected int lrx;
    protected int lry;
    protected int pixelAspect;
    protected byte[] pixelStack;
    protected byte[] pixels;
    protected short[] prefix;
    protected int status;
    protected byte[] suffix;
    protected int transIndex;
    protected boolean transparency = false;
    protected int width;

    public static class GifFrame {
        public int delay;
        public Bitmap image;

        public GifFrame(Bitmap im, int del) {
            this.image = im;
            this.delay = del;
        }
    }

    /* access modifiers changed from: protected */
    public void setPixels() {
        int[] dest = new int[(this.image.getWidth() * this.image.getHeight())];
        int i = this.lastDispose;
        if (i > 0) {
            if (i == 3) {
                int n = this.frameCount - 2;
                if (n > 0) {
                    this.lastBitmap = getFrame(n - 1);
                } else {
                    this.lastBitmap = null;
                }
            }
            Bitmap bitmap = this.lastBitmap;
            if (bitmap != null) {
                int i2 = this.width;
                bitmap.getPixels(dest, 0, i2, 0, 0, i2, this.height);
                if (this.lastDispose == 2) {
                    int c = 0;
                    if (!this.transparency) {
                        c = this.lastBgColor;
                    }
                    for (int i3 = 0; i3 < this.lrh; i3++) {
                        int n1 = ((this.lry + i3) * this.width) + this.lrx;
                        int n2 = this.lrw + n1;
                        for (int k = n1; k < n2; k++) {
                            dest[k] = c;
                        }
                    }
                }
            }
        }
        int i4 = 0;
        int pass = 1;
        int inc = 8;
        int iline = 0;
        while (true) {
            int pass2 = this.ih;
            if (i4 < pass2) {
                int line = i4;
                if (this.interlace) {
                    if (iline >= pass2) {
                        pass++;
                        if (pass == 2) {
                            iline = 4;
                        } else if (pass == 3) {
                            iline = 2;
                            inc = 4;
                        } else if (pass == 4) {
                            iline = 1;
                            inc = 2;
                        }
                    }
                    line = iline;
                    iline += inc;
                }
                int line2 = line + this.iy;
                if (line2 < this.height) {
                    int i5 = this.width;
                    int k2 = line2 * i5;
                    int dx = this.ix + k2;
                    int dlim = this.iw + dx;
                    if (k2 + i5 < dlim) {
                        dlim = k2 + i5;
                    }
                    int sx = this.iw * i4;
                    while (dx < dlim) {
                        int sx2 = sx + 1;
                        int c2 = this.act[this.pixels[sx] & 255];
                        if (c2 != 0) {
                            dest[dx] = c2;
                        }
                        dx++;
                        sx = sx2;
                    }
                }
                i4++;
            } else {
                Bitmap bitmap2 = this.image;
                bitmap2.setPixels(dest, 0, bitmap2.getWidth(), 0, 0, this.image.getWidth(), this.image.getHeight());
                return;
            }
        }
    }

    public Bitmap getFrame(int n) {
        int n2;
        int i = this.frameCount;
        if (i > 0 && (n2 = n % i) >= 0 && n2 < this.frames.size()) {
            return this.frames.elementAt(n2).image;
        }
        return null;
    }

    public int read(String path) {
        init();
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            this.in = is2;
            readHeader();
            if (!err()) {
                readContents();
                if (this.frameCount < 0) {
                    this.status = 1;
                }
            }
            try {
                is2.close();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Could not close stream", e);
            }
        } catch (FileNotFoundException e2) {
            this.status = 2;
            if (is != null) {
                is.close();
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e3) {
                    Log.e(LOG_TAG, "Could not close stream", e3);
                }
            }
            throw th;
        }
        return this.status;
    }

    private void allcateBitmapData(int npix) {
        byte[] bArr = this.pixels;
        if (bArr == null || bArr.length < npix) {
            this.pixels = new byte[npix];
        }
        if (this.prefix == null) {
            this.prefix = new short[4096];
        }
        if (this.suffix == null) {
            this.suffix = new byte[4096];
        }
        if (this.pixelStack == null) {
            this.pixelStack = new byte[4097];
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v9, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v10, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v0, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v13, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v4, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v4, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v18, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v19, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v27, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v28, resolved type: byte} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void decodeBitmapData() {
        /*
            r28 = this;
            r0 = r28
            r1 = -1
            int r2 = r0.iw
            int r3 = r0.ih
            int r2 = r2 * r3
            r0.allcateBitmapData(r2)
            int r3 = r28.read()
            r4 = 1
            int r5 = r4 << r3
            int r6 = r5 + 1
            int r7 = r5 + 2
            r8 = r1
            int r9 = r3 + 1
            int r10 = r4 << r9
            int r10 = r10 - r4
            r11 = 0
        L_0x001d:
            r12 = 0
            if (r11 >= r5) goto L_0x002c
            short[] r13 = r0.prefix
            r13[r11] = r12
            byte[] r12 = r0.suffix
            byte r13 = (byte) r11
            r12[r11] = r13
            int r11 = r11 + 1
            goto L_0x001d
        L_0x002c:
            r13 = r12
            r14 = r12
            r15 = r12
            r16 = r12
            r17 = r12
            r18 = r12
            r19 = r12
            r20 = 0
            r4 = r18
            r27 = r20
            r20 = r11
            r11 = r27
        L_0x0041:
            if (r11 >= r2) goto L_0x0143
            if (r15 != 0) goto L_0x0124
            if (r4 >= r9) goto L_0x006c
            if (r17 != 0) goto L_0x0058
            int r17 = r28.readBlock()
            if (r17 > 0) goto L_0x0057
            r22 = r1
            r21 = r2
            r23 = r3
            goto L_0x0149
        L_0x0057:
            r13 = 0
        L_0x0058:
            r21 = r2
            byte[] r2 = r0.block
            byte r2 = r2[r13]
            r2 = r2 & 255(0xff, float:3.57E-43)
            int r2 = r2 << r4
            int r19 = r19 + r2
            int r4 = r4 + 8
            r2 = 1
            int r13 = r13 + r2
            int r17 = r17 + -1
            r2 = r21
            goto L_0x0041
        L_0x006c:
            r21 = r2
            r2 = r19 & r10
            int r19 = r19 >> r9
            int r4 = r4 - r9
            if (r2 > r7) goto L_0x0117
            if (r2 != r6) goto L_0x0081
            r22 = r1
            r24 = r2
            r23 = r3
            r25 = r4
            goto L_0x011f
        L_0x0081:
            if (r2 != r5) goto L_0x0093
            int r9 = r3 + 1
            r18 = 1
            int r20 = r18 << r9
            int r10 = r20 + -1
            int r7 = r5 + 2
            r8 = r1
            r20 = r2
            r2 = r21
            goto L_0x0041
        L_0x0093:
            r18 = 1
            if (r8 != r1) goto L_0x00b2
            r22 = r1
            byte[] r1 = r0.pixelStack
            int r20 = r15 + 1
            r23 = r3
            byte[] r3 = r0.suffix
            byte r3 = r3[r2]
            r1[r15] = r3
            r8 = r2
            r12 = r2
            r15 = r20
            r1 = r22
            r3 = r23
            r20 = r2
            r2 = r21
            goto L_0x0041
        L_0x00b2:
            r22 = r1
            r23 = r3
            r1 = r2
            if (r2 != r7) goto L_0x00c6
            byte[] r3 = r0.pixelStack
            int r20 = r15 + 1
            r24 = r2
            byte r2 = (byte) r12
            r3[r15] = r2
            r2 = r8
            r15 = r20
            goto L_0x00c8
        L_0x00c6:
            r24 = r2
        L_0x00c8:
            if (r2 <= r5) goto L_0x00df
            byte[] r3 = r0.pixelStack
            int r20 = r15 + 1
            r25 = r4
            byte[] r4 = r0.suffix
            byte r4 = r4[r2]
            r3[r15] = r4
            short[] r3 = r0.prefix
            short r2 = r3[r2]
            r15 = r20
            r4 = r25
            goto L_0x00c8
        L_0x00df:
            r25 = r4
            byte[] r3 = r0.suffix
            byte r4 = r3[r2]
            r12 = r4 & 255(0xff, float:3.57E-43)
            r4 = 4096(0x1000, float:5.74E-42)
            if (r7 < r4) goto L_0x00f0
            r20 = r2
            r4 = r25
            goto L_0x0149
        L_0x00f0:
            byte[] r4 = r0.pixelStack
            int r24 = r15 + 1
            r26 = r2
            byte r2 = (byte) r12
            r4[r15] = r2
            short[] r2 = r0.prefix
            short r4 = (short) r8
            r2[r7] = r4
            byte r2 = (byte) r12
            r3[r7] = r2
            int r7 = r7 + 1
            r2 = r7 & r10
            if (r2 != 0) goto L_0x010e
            r2 = 4096(0x1000, float:5.74E-42)
            if (r7 >= r2) goto L_0x010e
            int r9 = r9 + 1
            int r10 = r10 + r7
        L_0x010e:
            r2 = r1
            r8 = r2
            r15 = r24
            r4 = r25
            r20 = r26
            goto L_0x012c
        L_0x0117:
            r22 = r1
            r24 = r2
            r23 = r3
            r25 = r4
        L_0x011f:
            r20 = r24
            r4 = r25
            goto L_0x0149
        L_0x0124:
            r22 = r1
            r21 = r2
            r23 = r3
            r18 = 1
        L_0x012c:
            int r15 = r15 + -1
            byte[] r1 = r0.pixels
            int r2 = r14 + 1
            byte[] r3 = r0.pixelStack
            byte r3 = r3[r15]
            r1[r14] = r3
            int r11 = r11 + 1
            r14 = r2
            r2 = r21
            r1 = r22
            r3 = r23
            goto L_0x0041
        L_0x0143:
            r22 = r1
            r21 = r2
            r23 = r3
        L_0x0149:
            r1 = r14
        L_0x014a:
            r2 = r21
            if (r1 >= r2) goto L_0x0158
            byte[] r3 = r0.pixels
            r11 = 0
            r3[r1] = r11
            int r1 = r1 + 1
            r21 = r2
            goto L_0x014a
        L_0x0158:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.picturetool.GifDecoder.decodeBitmapData():void");
    }

    /* access modifiers changed from: protected */
    public boolean err() {
        return this.status != 0;
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.status = 0;
        this.frameCount = 0;
        this.frames = new Vector<>();
        this.gct = null;
        this.lct = null;
    }

    /* access modifiers changed from: protected */
    public int read() {
        try {
            return this.in.read();
        } catch (Exception e) {
            this.status = 1;
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public int readBlock() {
        int read = read();
        this.blockSize = read;
        int n = 0;
        if (read > 0) {
            while (n < this.blockSize) {
                try {
                    int count = this.in.read(this.block, n, this.blockSize - n);
                    if (count == -1) {
                        break;
                    }
                    n += count;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (n < this.blockSize) {
                this.status = 1;
            }
        }
        return n;
    }

    /* access modifiers changed from: protected */
    public int[] readColorTable(int ncolors) {
        int nbytes = ncolors * 3;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int n = 0;
        try {
            n = this.in.read(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (n < nbytes) {
            this.status = 1;
        } else {
            tab = new int[256];
            int r = 0;
            for (int i = 0; i < ncolors; i++) {
                int j = r + 1;
                int j2 = j + 1;
                tab[i] = -16777216 | ((c[r] & 255) << 16) | ((c[j] & 255) << 8) | (c[j2] & 255);
                r = j2 + 1;
            }
        }
        return tab;
    }

    /* access modifiers changed from: protected */
    public void readContents() {
        boolean done = false;
        while (!done && !err()) {
            int code = read();
            String str = LOG_TAG;
            Log.d(str, "code=" + code);
            if (code == 33) {
                int code2 = read();
                if (code2 == 1) {
                    skip();
                } else if (code2 == 249) {
                    readGraphicControlExt();
                } else if (code2 == 254) {
                    skip();
                } else if (code2 != 255) {
                    skip();
                } else {
                    readBlock();
                    char[] appc = new char[11];
                    for (int i = 0; i < 11; i++) {
                        appc[i] = (char) this.block[i];
                    }
                    if ("NETSCAPE2.0".equals(new String(appc))) {
                        readNetscapeExt();
                    } else {
                        skip();
                    }
                }
            } else if (code != 44) {
                if (code != 59) {
                    this.status = 1;
                } else {
                    done = true;
                }
            } else if (this.frameCount < 50) {
                readBitmap();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readGraphicControlExt() {
        read();
        int packed = read();
        int i = (packed & 28) >> 2;
        this.dispose = i;
        boolean z = true;
        if (i == 0) {
            this.dispose = 1;
        }
        if ((packed & 1) == 0) {
            z = false;
        }
        this.transparency = z;
        this.delay = readShort() * 10;
        this.transIndex = read();
        read();
    }

    /* access modifiers changed from: protected */
    public void readHeader() {
        char[] idc = new char[6];
        for (int i = 0; i < 6; i++) {
            idc[i] = (char) read();
        }
        String ids = new String(idc);
        String str = LOG_TAG;
        Log.d(str, "readHeader: start=" + ids);
        if (!ids.startsWith("GIF")) {
            this.status = 1;
            return;
        }
        readLSD();
        if (this.gctFlag && !err()) {
            int[] readColorTable = readColorTable(this.gctSize);
            this.gct = readColorTable;
            if (readColorTable != null) {
                this.bgColor = readColorTable[this.bgIndex];
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readBitmap() {
        this.ix = readShort();
        this.iy = readShort();
        this.iw = readShort();
        this.ih = readShort();
        int packed = read();
        this.lctFlag = (packed & 128) != 0;
        this.lctSize = (int) Math.pow(2.0d, (double) ((packed & 7) + 1));
        this.interlace = (packed & 64) != 0;
        if (this.lctFlag) {
            int[] readColorTable = readColorTable(this.lctSize);
            this.lct = readColorTable;
            this.act = readColorTable;
        } else {
            this.act = this.gct;
            if (this.bgIndex == this.transIndex) {
                this.bgColor = 0;
            }
        }
        int save = 0;
        if (this.act == null) {
            this.status = 1;
        } else if (!err()) {
            if (this.transparency) {
                int[] iArr = this.act;
                int i = this.transIndex;
                save = iArr[i];
                iArr[i] = 0;
            }
            decodeBitmapData();
            skip();
            if (!err()) {
                this.frameCount++;
                this.image = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_4444);
                setPixels();
                this.frames.addElement(new GifFrame(this.image, this.delay));
                if (this.transparency) {
                    this.act[this.transIndex] = save;
                }
                resetFrame();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readLSD() {
        this.width = readShort();
        this.height = readShort();
        Log.d("GifDecoder", "w=" + this.width + ", h=" + this.height);
        int packed = read();
        this.gctFlag = (packed & 128) != 0;
        this.gctSize = 2 << (packed & 7);
        this.bgIndex = read();
        this.pixelAspect = read();
        String str = LOG_TAG;
        Log.d(str, "pixelAspect=" + this.pixelAspect + ", gctSize=" + this.gctSize + ", gctFlag=" + this.gctFlag);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP_START, MTH_ENTER_BLOCK] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readNetscapeExt() {
        /*
            r3 = this;
        L_0x0000:
            r3.readBlock()
            byte[] r0 = r3.block
            r1 = 0
            byte r1 = r0[r1]
            r2 = 1
            if (r1 != r2) goto L_0x0019
            byte r1 = r0[r2]
            r1 = r1 & 255(0xff, float:3.57E-43)
            r2 = 2
            byte r0 = r0[r2]
            r0 = r0 & 255(0xff, float:3.57E-43)
            int r2 = r0 << 8
            r2 = r2 | r1
            r3.loopCount = r2
        L_0x0019:
            int r0 = r3.blockSize
            if (r0 <= 0) goto L_0x0023
            boolean r0 = r3.err()
            if (r0 == 0) goto L_0x0000
        L_0x0023:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.picturetool.GifDecoder.readNetscapeExt():void");
    }

    /* access modifiers changed from: protected */
    public int readShort() {
        return read() | (read() << 8);
    }

    /* access modifiers changed from: protected */
    public void resetFrame() {
        this.lastDispose = this.dispose;
        this.lrx = this.ix;
        this.lry = this.iy;
        this.lrw = this.iw;
        this.lrh = this.ih;
        this.lastBitmap = this.image;
        this.lastBgColor = this.bgColor;
        this.dispose = 0;
        this.transparency = false;
        this.delay = 0;
        this.lct = null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP_START, MTH_ENTER_BLOCK] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void skip() {
        /*
            r1 = this;
        L_0x0000:
            r1.readBlock()
            int r0 = r1.blockSize
            if (r0 <= 0) goto L_0x000d
            boolean r0 = r1.err()
            if (r0 == 0) goto L_0x0000
        L_0x000d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.picturetool.GifDecoder.skip():void");
    }

    public void clean() {
        int n = this.frames.size();
        for (int i = 0; i < n; i++) {
            this.frames.get(i).image.recycle();
            this.frames.get(i).image = null;
        }
        this.frames.clear();
    }

    public Vector<GifFrame> getFrames() {
        return this.frames;
    }
}
