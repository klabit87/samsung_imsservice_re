package com.sec.internal.constants.ims.servicemodules.im;

import android.util.SparseArray;
import com.sec.internal.constants.ims.servicemodules.im.IEnumerationWithId;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import java.lang.Enum;

public class ReverseEnumMap<E extends Enum<E> & IEnumerationWithId<E>> {
    private final SparseArray<E> map = new SparseArray<>();

    public ReverseEnumMap(Class<E> type) {
        if (type.getEnumConstants() != null) {
            E[] eArr = (Enum[]) type.getEnumConstants();
            int length = eArr.length;
            int i = 0;
            while (i < length) {
                E enumConstant = eArr[i];
                E e = (Enum) this.map.get(((IEnumerationWithId) enumConstant).getId());
                if (e == null) {
                    this.map.put(((IEnumerationWithId) enumConstant).getId(), enumConstant);
                    i++;
                } else {
                    throw new IllegalStateException(Constants.ID + ((IEnumerationWithId) enumConstant).getId() + " already set to constant " + e.name());
                }
            }
            return;
        }
        throw new IllegalStateException("Trying to make ReverseEnumMap with non-enum class: " + type);
    }

    public E get(Integer id) {
        E e = (Enum) this.map.get(id.intValue());
        if (e != null) {
            return e;
        }
        throw new IllegalArgumentException("Id " + id + " unknown in reverse enumeration map");
    }
}
