package com.sec.internal.helper.picturetool;

import android.util.Log;
import android.util.Pair;

public class FullCompressionDescriptor implements ICompressionDescriptor {
    private static final int DEDICATED_IMAGE_QUALITY = 90;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = FullCompressionDescriptor.class.getSimpleName();
    /* access modifiers changed from: private */
    public ICompressionDescriptor mDelegate = this.mInitial;
    /* access modifiers changed from: private */
    public ICompressionDescriptor mFinal;
    private final ICompressionDescriptor mInitial = new ICompressionDescriptor() {
        public Pair<Integer, Integer> next(long currentSize) {
            String access$000 = FullCompressionDescriptor.LOG_TAG;
            Log.d(access$000, "mInitial mScale=" + FullCompressionDescriptor.this.mScale);
            FullCompressionDescriptor fullCompressionDescriptor = FullCompressionDescriptor.this;
            ICompressionDescriptor unused = fullCompressionDescriptor.mDelegate = fullCompressionDescriptor.mSecond;
            return Pair.create(90, Integer.valueOf(FullCompressionDescriptor.this.mScale));
        }
    };
    /* access modifiers changed from: private */
    public final long mMaxSize;
    /* access modifiers changed from: private */
    public final int mMinDimension;
    /* access modifiers changed from: private */
    public int mScale = 1;
    /* access modifiers changed from: private */
    public final ICompressionDescriptor mSecond = new ICompressionDescriptor() {
        public Pair<Integer, Integer> next(long currentSize) {
            FullCompressionDescriptor fullCompressionDescriptor = FullCompressionDescriptor.this;
            ICompressionDescriptor unused = fullCompressionDescriptor.mDelegate = fullCompressionDescriptor.mStandard;
            FullCompressionDescriptor fullCompressionDescriptor2 = FullCompressionDescriptor.this;
            int unused2 = fullCompressionDescriptor2.mScale = Math.max(fullCompressionDescriptor2.mScale + 1, (int) Math.sqrt((((double) currentSize) * Math.pow((double) FullCompressionDescriptor.this.mScale, 2.0d)) / ((double) FullCompressionDescriptor.this.mMaxSize)));
            return Pair.create(90, Integer.valueOf(FullCompressionDescriptor.this.mScale));
        }
    };
    /* access modifiers changed from: private */
    public final ICompressionDescriptor mStandard = new ICompressionDescriptor() {
        public Pair<Integer, Integer> next(long currentSize) {
            int scale = FullCompressionDescriptor.this.mScale;
            int previousScaledMinDimension = FullCompressionDescriptor.this.mMinDimension / FullCompressionDescriptor.this.mScale;
            while (true) {
                scale++;
                int scaledMinDimension = FullCompressionDescriptor.this.mMinDimension / scale;
                if (scaledMinDimension != previousScaledMinDimension) {
                    if (scaledMinDimension == 0) {
                        FullCompressionDescriptor.this.mFinal.next(currentSize);
                    } else {
                        int unused = FullCompressionDescriptor.this.mScale = scale;
                        Log.d(FullCompressionDescriptor.LOG_TAG, "mStandard: Exit");
                        return Pair.create(90, Integer.valueOf(FullCompressionDescriptor.this.mScale));
                    }
                }
            }
        }
    };

    public FullCompressionDescriptor(long size, int width, int height, long maxSize, int maxWidth, int maxHeight, ICompressionDescriptor mPanicDescriptor) throws NullPointerException {
        int i = width;
        int i2 = height;
        this.mFinal = mPanicDescriptor;
        this.mMaxSize = maxSize;
        int startWidth = Math.min(i, maxWidth);
        int startHeight = Math.min(i2, maxHeight);
        this.mMinDimension = Math.min(width, height);
        int max = Math.max(Math.max(i / startWidth, i2 / startHeight), Math.max((int) Math.sqrt(((double) size) / ((double) this.mMaxSize)), 1));
        this.mScale = max;
        if (this.mMinDimension / max == 0) {
            this.mDelegate = this.mFinal;
        }
    }

    public Pair<Integer, Integer> next(long currentSize) {
        String str = LOG_TAG;
        Log.d(str, "FullCompressionDescriptor::next size=" + currentSize);
        return this.mDelegate.next(currentSize);
    }
}
