package com.sec.internal.ims.aec.util;

import android.os.Environment;
import android.os.SemSystemProperties;
import com.sec.internal.constants.ims.config.ConfigConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ExternalStorage {
    static final String FILE_NAME_ENTITLEMENT_URL = "entitlementURL.txt";
    static final String FILE_NAME_NOTIF_TOKEN = "notification registration token.txt";
    private static final boolean NO_SHIP_BUILD = ConfigConstants.VALUE.INFO_COMPLETED.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));

    public static String getLabHttpUrl() {
        String labHttpUrl = null;
        if (!NO_SHIP_BUILD) {
            return null;
        }
        File file = new File(Environment.getExternalStorageDirectory(), FILE_NAME_ENTITLEMENT_URL);
        if (!file.exists()) {
            return null;
        }
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            labHttpUrl = br.readLine();
            fr.close();
            br.close();
            return labHttpUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return labHttpUrl;
        }
    }

    public static void setNotifToken(int phoneId, String token) {
        FileWriter fw;
        if (NO_SHIP_BUILD) {
            try {
                fw = new FileWriter(new File(Environment.getExternalStorageDirectory(), FILE_NAME_NOTIF_TOKEN));
                fw.write(phoneId + ":" + token);
                fw.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            return;
        }
        throw th;
    }
}
