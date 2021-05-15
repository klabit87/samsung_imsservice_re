package com.sec.internal.ims.servicemodules.tapi.service.extension.validation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.ValidationHelper;
import java.io.InputStream;

public class PackageInfoValidator {
    private static final String LOG_TAG = PackageInfoValidator.class.getSimpleName();
    private final Context mContext;
    private String mPackageName;
    private String mPackageSigner = "";

    public PackageInfoValidator(Context context) {
        this.mContext = context;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
        updatePackageSigner();
    }

    private void updatePackageSigner() {
        try {
            this.mPackageSigner = ValidationHelper.getFingerPrint(this.mContext.getPackageManager().getPackageInfo(this.mPackageName, 64).signatures[0]);
            String str = LOG_TAG;
            Log.d(str, "updatePackageSigner: updated package signer" + this.mPackageSigner);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean checkPackageDetails(IARIXmlParser authDocument) {
        if (authDocument.getPackageName() != null && !authDocument.getPackageName().equals(this.mPackageName)) {
            String str = LOG_TAG;
            Log.e(str, "Mismatched package name:mPackageName - " + this.mPackageName);
            return false;
        } else if (!authDocument.getPackageSigner().equals(this.mPackageSigner)) {
            String str2 = LOG_TAG;
            Log.e(str2, "Mismatched package signer:mPackageSigner - " + this.mPackageSigner);
            return false;
        } else {
            Log.d(LOG_TAG, "package details are successfully validated");
            return true;
        }
    }

    public String processIARIauthorization(InputStream is) {
        Log.d(LOG_TAG, "processIARIauthorization");
        IARIXmlProcessor processor = new IARIXmlProcessor();
        processor.parseAuthDoc(is);
        if (processor.getStatus() == 0) {
            IARIXmlParser authDocument = processor.getAuthDocument();
            if (!checkPackageDetails(authDocument)) {
                Log.e(LOG_TAG, "error validating package details");
                return null;
            }
            processor.process();
            if (processor.getStatus() == 0) {
                return authDocument.getIari();
            }
        }
        return null;
    }
}
