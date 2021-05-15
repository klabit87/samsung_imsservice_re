package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.os.UserManager;
import android.provider.Settings;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class KtUPStrategy extends SecUPStrategy {
    private static final String TAG = KtUPStrategy.class.getSimpleName();

    public KtUPStrategy(Context context, int phoneId) {
        super(context, phoneId);
    }

    public boolean isBMode(boolean checkSettingOnly) {
        int isKtTwoPhoneServiceRegistered = Settings.Global.getInt(this.mContext.getContentResolver(), "two_register", 0);
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "isKtTwoPhoneServiceRegistered: " + isKtTwoPhoneServiceRegistered);
        if (isKtTwoPhoneServiceRegistered != 1) {
            return false;
        }
        if (isKtTwoPhoneServiceRegistered == 1 && checkSettingOnly) {
            return true;
        }
        List<Object> userInfos = Extensions.UserManagerRef.getUsers((UserManager) this.mContext.getSystemService("user"));
        if (userInfos != null) {
            int i2 = 0;
            while (i2 < userInfos.size()) {
                Object nowUser = userInfos.get(i2);
                if (!Extensions.UserInfo.isBMode(nowUser) || Extensions.UserInfo.getUserId(nowUser) != Extensions.ActivityManager.getCurrentUser()) {
                    i2++;
                } else {
                    IMSLog.i(TAG, this.mPhoneId, "Current user set BMode.");
                    return true;
                }
            }
        }
        return false;
    }
}
