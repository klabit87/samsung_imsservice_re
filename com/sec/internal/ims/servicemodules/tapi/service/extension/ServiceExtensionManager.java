package com.sec.internal.ims.servicemodules.tapi.service.extension;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.ValidationHelper;
import com.sec.internal.ims.servicemodules.tapi.service.extension.validation.PackageInfoValidator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceExtensionManager {
    private static final String CLIENT_JOYN_NAME = "gsma.joyn.client";
    private static final String LOG_TAG = ServiceExtensionManager.class.getSimpleName();
    private static ServiceExtensionManager mInstance;
    private final Hashtable<String, ApplicationInfo> clientAppInfo = new Hashtable<>();
    private final Context mContext;
    private RcsClientsMonitor mRcsClientMonitor;
    private final Set<String> processedPackages = new HashSet();

    private ServiceExtensionManager(Context context) {
        this.mContext = context;
    }

    public static synchronized ServiceExtensionManager getInstance(Context context) {
        ServiceExtensionManager serviceExtensionManager;
        synchronized (ServiceExtensionManager.class) {
            if (mInstance == null) {
                mInstance = new ServiceExtensionManager(context);
            }
            serviceExtensionManager = mInstance;
        }
        return serviceExtensionManager;
    }

    public void start() {
        Log.d(LOG_TAG, "start");
        if (this.mRcsClientMonitor == null) {
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REPLACED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            RcsClientsMonitor rcsClientsMonitor = new RcsClientsMonitor(this.mContext, this);
            this.mRcsClientMonitor = rcsClientsMonitor;
            this.mContext.registerReceiver(rcsClientsMonitor, filter);
        }
        loadProcessesPackages();
        loadClients();
        authoriseAllClients();
    }

    public void stop() {
        this.clientAppInfo.clear();
        this.processedPackages.clear();
        RcsClientsMonitor rcsClientsMonitor = this.mRcsClientMonitor;
        if (rcsClientsMonitor != null) {
            this.mContext.unregisterReceiver(rcsClientsMonitor);
            this.mRcsClientMonitor = null;
        }
    }

    private void loadProcessesPackages() {
        for (Map.Entry<String, ?> entry : this.mContext.getSharedPreferences("iari_app_association", 0).getAll().entrySet()) {
            this.processedPackages.add(ValidationHelper.decrypt(entry.getKey()));
        }
    }

    private void loadClients() {
        List<ApplicationInfo> applicationInfos = new ArrayList<>();
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(1);
        if (packages != null) {
            for (PackageInfo pkg : packages) {
                try {
                    applicationInfos.add(getPackageManager().getApplicationInfo(pkg.packageName, 128));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            for (ApplicationInfo app : applicationInfos) {
                if (!isCurrentPackage(this.mContext, app.packageName) && app.metaData != null && app.metaData.getBoolean(CLIENT_JOYN_NAME, false)) {
                    this.clientAppInfo.put(app.packageName, app);
                }
            }
        }
    }

    private void authoriseInternal(String packageName, InputStream is) {
        PackageInfoValidator processor = new PackageInfoValidator(this.mContext);
        processor.setPackageName(packageName);
        String iari = processor.processIARIauthorization(is);
        if (iari == null || iari.isEmpty()) {
            String str = LOG_TAG;
            Log.d(str, "unAuthorised tag or validation error for the package: " + packageName);
            if (Build.IS_DEBUGGABLE) {
                String str2 = LOG_TAG;
                Log.e(str2, "debug binary, ignore" + packageName);
                iari = "default-tag";
            }
        } else {
            String str3 = LOG_TAG;
            Log.d(str3, "tag is authorised for the package: " + packageName);
        }
        if (packageName != null) {
            persistIariTag(packageName, iari);
        }
        this.processedPackages.add(packageName);
        closeInputStream(is);
    }

    private void persistIariTag(String packageName, String iari) {
        SharedPreferences.Editor editor = this.mContext.getSharedPreferences("iari_app_association", 0).edit();
        if (iari != null) {
            editor.putString(ValidationHelper.encrypt(packageName), ValidationHelper.encrypt(iari));
        } else {
            editor.putString(ValidationHelper.encrypt(packageName), "");
        }
        editor.apply();
    }

    private void authoriseAllClients() {
        ApplicationInfo app;
        InputStream is;
        for (Map.Entry<String, ApplicationInfo> entry : this.clientAppInfo.entrySet()) {
            if (!isPackageProcessed(entry.getKey()) && (app = entry.getValue()) != null && !isSystemApp(app.flags) && (is = getXmlResource(app.metaData.getInt("auth"), app.packageName)) != null) {
                authoriseInternal(app.packageName, is);
            }
        }
    }

    /* access modifiers changed from: private */
    public void authorise(String packageName) {
        ApplicationInfo info = null;
        try {
            info = getPackageManager().getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info != null && !isCurrentPackage(this.mContext, info.packageName)) {
            authoriseInternal(info.packageName, getXmlResource(info.metaData.getInt("auth"), packageName));
        }
    }

    /* access modifiers changed from: private */
    public void unAuthorise(String packageName) {
        String str = LOG_TAG;
        Log.d(str, "unAuthorise" + packageName);
        SharedPreferences.Editor editor = this.mContext.getSharedPreferences("iari_app_association", 0).edit();
        editor.remove(ValidationHelper.encrypt(packageName));
        this.clientAppInfo.remove(packageName);
        this.processedPackages.remove(packageName);
        editor.apply();
    }

    private void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isPackageProcessed(String packageName) {
        return this.processedPackages.contains(packageName);
    }

    private static boolean isSystemApp(int flags) {
        return ((flags & 1) == 0 && (flags & 128) == 0) ? false : true;
    }

    private InputStream getXmlResource(int id, String packageName) {
        try {
            Resources res = this.mContext.getPackageManager().getResourcesForApplication(packageName);
            if (this.mContext.getResources().getIdentifier(String.valueOf(id), "raw", packageName) != 0) {
                return res.openRawResource(id);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "no auth doc found in client application");
        return null;
    }

    private static boolean isCurrentPackage(Context context, String packageName) {
        if (context == null) {
            return false;
        }
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 128);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "error retrieving the package details ");
        }
        if (pi == null || packageName == null || !packageName.equals(pi.packageName)) {
            return false;
        }
        return true;
    }

    private PackageManager getPackageManager() {
        return this.mContext.getPackageManager();
    }

    public static boolean isAppAuthorised(Context c, String packageName) {
        ApplicationInfo app = null;
        if (isCurrentPackage(c, packageName)) {
            Log.d(LOG_TAG, "current package: ignore");
            return true;
        }
        try {
            app = c.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (app == null) {
            Log.e(LOG_TAG, "ApplicationInfo is Null");
            return false;
        } else if (isSystemApp(app.flags)) {
            Log.d(LOG_TAG, "system application: ignore");
            return true;
        } else {
            SharedPreferences sp = c.getSharedPreferences("iari_app_association", 0);
            if (sp == null || !sp.contains(ValidationHelper.encrypt(packageName))) {
                return false;
            }
            String iaritag = sp.getString(ValidationHelper.encrypt(packageName), "");
            if (iaritag.isEmpty()) {
                Log.e(LOG_TAG, "Package name not authorized");
                return false;
            }
            String decrypted_iaritag = ValidationHelper.decrypt(iaritag);
            String str = LOG_TAG;
            Log.d(str, "Decrypted iari" + decrypted_iaritag);
            return true;
        }
    }

    public static class RcsClientsMonitor extends BroadcastReceiver {
        private final Context ctx;
        private final ServiceExtensionManager mgr;

        public RcsClientsMonitor(Context context, ServiceExtensionManager extensionManager) {
            this.mgr = extensionManager;
            this.ctx = context;
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x0053  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x007a  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r8, android.content.Intent r9) {
            /*
                r7 = this;
                java.lang.String r0 = r9.getAction()
                java.lang.String r1 = r9.getDataString()
                java.util.Objects.requireNonNull(r1)
                java.lang.String r1 = (java.lang.String) r1
                java.lang.String r2 = "package:"
                java.lang.String r3 = ""
                java.lang.String r1 = r1.replaceFirst(r2, r3)
                java.util.Objects.requireNonNull(r0)
                r2 = r0
                java.lang.String r2 = (java.lang.String) r2
                int r3 = r2.hashCode()
                r4 = -810471698(0xffffffffcfb12eee, float:-5.9452856E9)
                r5 = 2
                r6 = 1
                if (r3 == r4) goto L_0x0046
                r4 = 525384130(0x1f50b9c2, float:4.419937E-20)
                if (r3 == r4) goto L_0x003c
                r4 = 1544582882(0x5c1076e2, float:1.62652439E17)
                if (r3 == r4) goto L_0x0032
            L_0x0031:
                goto L_0x0050
            L_0x0032:
                java.lang.String r3 = "android.intent.action.PACKAGE_ADDED"
                boolean r2 = r2.equals(r3)
                if (r2 == 0) goto L_0x0031
                r2 = 0
                goto L_0x0051
            L_0x003c:
                java.lang.String r3 = "android.intent.action.PACKAGE_REMOVED"
                boolean r2 = r2.equals(r3)
                if (r2 == 0) goto L_0x0031
                r2 = r6
                goto L_0x0051
            L_0x0046:
                java.lang.String r3 = "android.intent.action.PACKAGE_REPLACED"
                boolean r2 = r2.equals(r3)
                if (r2 == 0) goto L_0x0031
                r2 = r5
                goto L_0x0051
            L_0x0050:
                r2 = -1
            L_0x0051:
                if (r2 == 0) goto L_0x007a
                if (r2 == r6) goto L_0x006c
                if (r2 == r5) goto L_0x0058
                goto L_0x008d
            L_0x0058:
                boolean r2 = r7.isJoynClient(r1)
                if (r2 == 0) goto L_0x008d
                com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager r2 = r7.mgr
                boolean r2 = r2.isPackageProcessed(r1)
                if (r2 != 0) goto L_0x008d
                com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager r2 = r7.mgr
                r2.authorise(r1)
                goto L_0x008d
            L_0x006c:
                com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager r2 = r7.mgr
                boolean r2 = r2.isPackageProcessed(r1)
                if (r2 == 0) goto L_0x008d
                com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager r2 = r7.mgr
                r2.unAuthorise(r1)
                goto L_0x008d
            L_0x007a:
                boolean r2 = r7.isJoynClient(r1)
                if (r2 == 0) goto L_0x008d
                com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager r2 = r7.mgr
                boolean r2 = r2.isPackageProcessed(r1)
                if (r2 != 0) goto L_0x008d
                com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager r2 = r7.mgr
                r2.authorise(r1)
            L_0x008d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager.RcsClientsMonitor.onReceive(android.content.Context, android.content.Intent):void");
        }

        private boolean isJoynClient(String packageName) {
            ApplicationInfo appInfo = null;
            try {
                appInfo = this.ctx.getPackageManager().getApplicationInfo(packageName, 128);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return (appInfo == null || appInfo.metaData == null || !appInfo.metaData.getBoolean(ServiceExtensionManager.CLIENT_JOYN_NAME, false)) ? false : true;
        }
    }
}
