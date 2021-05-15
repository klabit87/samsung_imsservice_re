package com.sec.internal.ims.servicemodules.euc.persistence;

import android.database.Cursor;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import java.util.List;

public interface IUserConsentPersistence {
    Cursor getEucList(String str, List<EucType> list, String str2, String str3);

    int removeEuc(String str, String[] strArr) throws IllegalArgumentException;
}
