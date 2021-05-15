package com.sec.internal.ims.imsservice;

import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import java.util.function.Consumer;

/* renamed from: com.sec.internal.ims.imsservice.-$$Lambda$Coz7SzymPQHD4TahSkxD2V2ic9w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Coz7SzymPQHD4TahSkxD2V2ic9w implements Consumer {
    public static final /* synthetic */ $$Lambda$Coz7SzymPQHD4TahSkxD2V2ic9w INSTANCE = new $$Lambda$Coz7SzymPQHD4TahSkxD2V2ic9w();

    private /* synthetic */ $$Lambda$Coz7SzymPQHD4TahSkxD2V2ic9w() {
    }

    public final void accept(Object obj) {
        ((ISequentialInitializable) obj).initSequentially();
    }
}
