package com.sec.internal.ims.cmstore.ambs.provision;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.log.IMSLog;

public class ProvisionHelper {
    public static final String TAG = ProvisionHelper.class.getSimpleName();

    static void readAndSaveSimInformation(TelephonyManager telephonyManager) {
        String accountNumber = AmbsUtils.convertPhoneNumberToUserAct(telephonyManager.getLine1Number());
        String newImsi = telephonyManager.getSubscriberId();
        String str = TAG;
        Log.i(str, "Phone number == " + IMSLog.checker(accountNumber));
        CloudMessagePreferenceManager.getInstance().saveSimImsi(newImsi);
        if (TextUtils.isEmpty(accountNumber)) {
            Log.d(TAG, "empty CTN");
        }
        CloudMessagePreferenceManager.getInstance().saveUserCtn(accountNumber, false);
    }

    public static boolean isSimOrCtnChanged(TelephonyManager telephonyManager) {
        boolean isSimChanged = isSimChanged(telephonyManager);
        boolean isCtnChanged = isCtnChanged(telephonyManager);
        String str = TAG;
        Log.d(str, "isSimChanged: " + isSimChanged + " isCtnChanged: " + isCtnChanged);
        return isSimChanged || isCtnChanged;
    }

    public static boolean isSimChanged(TelephonyManager telephonyManager) {
        String oldImsi = CloudMessagePreferenceManager.getInstance().getSimImsi();
        if (TextUtils.isEmpty(oldImsi) || telephonyManager.getSimState() != 1) {
            String newImsi = telephonyManager.getSubscriberId();
            String str = TAG;
            Log.d(str, "isSimChanged oldImsi: " + IMSLog.checker(oldImsi) + " newImsi: " + IMSLog.checker(newImsi));
            if (TextUtils.isEmpty(oldImsi) || oldImsi.equalsIgnoreCase(newImsi)) {
                return TextUtils.isEmpty(oldImsi) && !TextUtils.isEmpty(newImsi);
            }
            return true;
        }
        Log.d(TAG, "no SIM card");
        return false;
    }

    static boolean isCtnChanged(TelephonyManager telephonyManager) {
        String accountNumber = AmbsUtils.convertPhoneNumberToUserAct(telephonyManager.getLine1Number());
        if (!TextUtils.isEmpty(accountNumber)) {
            String oldCtn = CloudMessagePreferenceManager.getInstance().getUserCtn();
            String str = TAG;
            Log.d(str, "oldCtn: " + IMSLog.checker(oldCtn) + " accountNumber: " + IMSLog.checker(accountNumber));
            return true ^ accountNumber.equals(oldCtn);
        }
        Log.d(TAG, "accountNumber is empty");
        return true;
    }

    public static boolean isCtnChangedByNetwork(TelephonyManager telephonyManager) {
        String accountNumber = AmbsUtils.convertPhoneNumberToUserAct(telephonyManager.getLine1Number());
        if (TextUtils.isEmpty(accountNumber)) {
            Log.d(TAG, "empty accountNumber");
            return false;
        }
        String oldCtn = CloudMessagePreferenceManager.getInstance().getUserCtn();
        String str = TAG;
        Log.d(str, "oldCtn: " + IMSLog.checker(oldCtn) + " accountNumber: " + IMSLog.checker(accountNumber));
        if (TextUtils.isEmpty(oldCtn) || accountNumber.equals(oldCtn)) {
            return false;
        }
        Log.d(TAG, "change and save user ctn");
        CloudMessagePreferenceManager.getInstance().saveUserCtn(accountNumber, false);
        return true;
    }

    static boolean isOOBE() {
        return CloudMessagePreferenceManager.getInstance().isEmptyPref();
    }
}
