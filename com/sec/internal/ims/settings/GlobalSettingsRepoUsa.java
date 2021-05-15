package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import com.sec.internal.constants.Mno;

public class GlobalSettingsRepoUsa extends GlobalSettingsRepoBase {
    public GlobalSettingsRepoUsa(Context context, int phoneId) {
        super(context, phoneId);
    }

    /* access modifiers changed from: protected */
    public boolean needResetVolteAsDefault(int prevVolteDefaultEnabled, int newVolteDefaultEnabled) {
        return prevVolteDefaultEnabled != newVolteDefaultEnabled;
    }

    /* access modifiers changed from: protected */
    public void updateSystemSettings(Mno mno, ContentValues mnoinfo, String newMnoname, String prevMnoname, int spValueVolte, int spValueVideo) {
        boolean isNeedToSetVoLTE = isNeedToBeSetVoLTE(mnoinfo);
        if (this.mCscChanged && mno == Mno.TMOUS) {
            this.mCscChanged = false;
            this.mEventLog.logAndAdd("TMO requires forced enable voicecall_type after FOTA.");
            isNeedToSetVoLTE = true;
            spValueVolte = 0;
        }
        setSettingsFromSp(isNeedToSetVoLTE, spValueVolte, isNeedToBeSetViLTE(mnoinfo), spValueVideo, false, -1);
    }
}
