package com.sec.internal.helper.picturetool;

import android.util.Log;
import android.util.Pair;

public class PngLazyCompressionDescriptor implements ICompressionDescriptor {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = PngLazyCompressionDescriptor.class.getSimpleName();
    private static final int STUB_IMAGE_QUALITY = 100;
    /* access modifiers changed from: private */
    public ICompressionDescriptor mDelegate;
    private final ICompressionDescriptor mInitial;
    /* access modifiers changed from: private */
    public final ICompressionDescriptor mPanic;
    /* access modifiers changed from: private */
    public final int mScale;

    public PngLazyCompressionDescriptor(int width, int height, int maxWidth, int maxHeight, ICompressionDescriptor mPanicDescriptor) {
        AnonymousClass1 r0 = new ICompressionDescriptor() {
            public Pair<Integer, Integer> next(long currentSize) {
                String access$000 = PngLazyCompressionDescriptor.LOG_TAG;
                Log.d(access$000, "mInitial::nex" + currentSize);
                PngLazyCompressionDescriptor pngLazyCompressionDescriptor = PngLazyCompressionDescriptor.this;
                ICompressionDescriptor unused = pngLazyCompressionDescriptor.mDelegate = pngLazyCompressionDescriptor.mPanic;
                return Pair.create(100, Integer.valueOf(PngLazyCompressionDescriptor.this.mScale));
            }
        };
        this.mInitial = r0;
        this.mDelegate = r0;
        this.mScale = Math.max(getStartWidthScale(width, maxWidth), getStartHeightScale(height, maxHeight));
        this.mPanic = mPanicDescriptor;
    }

    public Pair<Integer, Integer> next(long currentSize) {
        return this.mDelegate.next(currentSize);
    }

    private int getStartScale(int dimension, int maxDimension) {
        int i = 1;
        int startScale = Math.max(dimension / maxDimension, 1);
        if (maxDimension >= dimension) {
            return startScale;
        }
        if (dimension % (startScale * maxDimension) == 0) {
            i = 0;
        }
        return startScale + i;
    }

    private int getStartWidthScale(int width, int maxWidth) {
        return getStartScale(width, maxWidth);
    }

    private int getStartHeightScale(int height, int maxHeight) {
        return getStartScale(height, maxHeight);
    }
}
