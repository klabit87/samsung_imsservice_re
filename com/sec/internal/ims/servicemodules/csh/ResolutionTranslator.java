package com.sec.internal.ims.servicemodules.csh;

import com.sec.internal.ims.servicemodules.csh.event.VshResolution;
import java.util.HashMap;

public class ResolutionTranslator {
    private static final int CIF_HEIGHT = 288;
    private static final int CIF_WIDTH = 352;
    private static final int QCIF_HEIGHT = 144;
    private static final int QCIF_WIDTH = 176;
    private static final int QVGA_HEIGHT = 240;
    private static final int QVGA_WIDTH = 320;
    private static final int VGA_HEIGHT = 480;
    private static final int VGA_WIDTH = 640;
    private static final HashMap<VshResolution, Integer[]> translate;

    static {
        HashMap<VshResolution, Integer[]> hashMap = new HashMap<>();
        translate = hashMap;
        VshResolution vshResolution = VshResolution.CIF;
        Integer valueOf = Integer.valueOf(CIF_WIDTH);
        Integer valueOf2 = Integer.valueOf(CIF_HEIGHT);
        hashMap.put(vshResolution, new Integer[]{valueOf, valueOf2});
        translate.put(VshResolution.CIF_PORTRAIT, new Integer[]{valueOf2, valueOf});
        translate.put(VshResolution.QCIF, new Integer[]{176, 144});
        translate.put(VshResolution.QCIF_PORTRAIT, new Integer[]{144, 176});
        HashMap<VshResolution, Integer[]> hashMap2 = translate;
        VshResolution vshResolution2 = VshResolution.VGA;
        Integer valueOf3 = Integer.valueOf(VGA_WIDTH);
        hashMap2.put(vshResolution2, new Integer[]{valueOf3, 480});
        translate.put(VshResolution.VGA_PORTRAIT, new Integer[]{480, valueOf3});
        HashMap<VshResolution, Integer[]> hashMap3 = translate;
        VshResolution vshResolution3 = VshResolution.QVGA;
        Integer valueOf4 = Integer.valueOf(QVGA_WIDTH);
        hashMap3.put(vshResolution3, new Integer[]{valueOf4, 240});
        translate.put(VshResolution.QVGA_PORTRAIT, new Integer[]{240, valueOf4});
        translate.put(VshResolution.NONE, new Integer[]{0, 0});
    }

    public static int getWidth(VshResolution resolution) {
        return translate.get(resolution)[0].intValue();
    }

    public static int getHeight(VshResolution resolution) {
        return translate.get(resolution)[1].intValue();
    }
}
