package com.sec.internal.ims.translate;

public interface TypeTranslator<T, S> {
    S translate(T t);
}
