package com.sec.internal.ims.servicemodules.csh.event;

import android.view.Surface;

public class VideoDisplay implements IVideoDisplay {
    private final int mColor;
    private final Surface mWindowHandle;

    public VideoDisplay(Surface windowHandle, int color) {
        this.mWindowHandle = windowHandle;
        this.mColor = color;
    }

    public Surface getWindowHandle() {
        return this.mWindowHandle;
    }

    public int getColor() {
        return this.mColor;
    }
}
