package com.sec.internal.ims.imsservice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.imscr.LogClass;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class RcsFileProviderManager {
    private static final String LOG_TAG = RcsFileProviderManager.class.getSimpleName();

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0057  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void handleIntent(android.content.Context r4, android.content.Intent r5) {
        /*
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleIntent : intent="
            r1.append(r2)
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            java.lang.String r0 = r5.getAction()
            int r1 = r0.hashCode()
            r2 = -1936520731(0xffffffff8c9309e5, float:-2.2654891E-31)
            r3 = 1
            if (r1 == r2) goto L_0x0043
            r2 = -871932095(0xffffffffcc075f41, float:-3.548698E7)
            if (r1 == r2) goto L_0x0039
            r2 = -412782152(0xffffffffe76571b8, float:-1.0835197E24)
            if (r1 == r2) goto L_0x002f
        L_0x002e:
            goto L_0x004d
        L_0x002f:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.MOVE_FILE_COMPLETE"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002e
            r0 = r3
            goto L_0x004e
        L_0x0039:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.MOVE_FILE_FINAL_COMPLETE"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002e
            r0 = 2
            goto L_0x004e
        L_0x0043:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.GRANT_FILE_PERMISSION"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002e
            r0 = 0
            goto L_0x004e
        L_0x004d:
            r0 = -1
        L_0x004e:
            if (r0 == 0) goto L_0x0057
            if (r0 == r3) goto L_0x0053
            goto L_0x005b
        L_0x0053:
            handleMoveFileComplete(r5)
            goto L_0x005b
        L_0x0057:
            handleGrantFilePermissionRequest(r4, r5)
        L_0x005b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.imsservice.RcsFileProviderManager.handleIntent(android.content.Context, android.content.Intent):void");
    }

    private static void handleGrantFilePermissionRequest(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        List<String> fileList = extras.getStringArrayList(FtIntent.Extras.FILE_PATHS);
        HashMap<String, String> fileUris = new HashMap<>();
        if (fileList != null) {
            for (String path : fileList) {
                Uri uri = FileUtils.getUriForFile(context, path);
                fileUris.put(path, uri != null ? uri.toString() : null);
            }
        }
        List<String> thumbList = extras.getStringArrayList(FtIntent.Extras.THUMB_PATHS);
        HashMap<String, String> thumbUris = new HashMap<>();
        if (thumbList != null) {
            for (String path2 : thumbList) {
                Uri uri2 = FileUtils.getUriForFile(context, path2);
                thumbUris.put(path2, uri2 != null ? uri2.toString() : null);
            }
        }
        List<String> iconList = extras.getStringArrayList(FtIntent.Extras.ICON_PATHS);
        HashMap<String, String> iconUris = new HashMap<>();
        if (iconList != null) {
            for (String path3 : iconList) {
                Uri uri3 = FileUtils.getUriForFile(context, path3);
                iconUris.put(path3, uri3 != null ? uri3.toString() : null);
            }
        }
        Intent response = new Intent(FtIntent.Actions.ResponseIntents.GRANT_FILE_PERMISSION_RESPONSE);
        response.putExtra(FtIntent.Extras.FILE_URIS, fileUris);
        response.putExtra(FtIntent.Extras.THUMB_URIS, thumbUris);
        response.putExtra(FtIntent.Extras.ICON_URIS, iconUris);
        response.addFlags(LogClass.SIM_EVENT);
        String str = LOG_TAG;
        Log.i(str, "handleGrantFilePermissionResponse : \r\nfielUris = " + fileUris + "\r\nthumbUris = " + thumbUris + "\r\niconUris = " + iconUris);
        IntentUtil.sendBroadcast(context, response, ContextExt.CURRENT_OR_SELF);
    }

    private static void handleMoveFileComplete(Intent intent) {
        Bundle extras = intent.getExtras();
        deleteFiles(extras.getStringArrayList(FtIntent.Extras.FILE_PATHS));
        deleteFiles(extras.getStringArrayList(FtIntent.Extras.THUMB_PATHS));
        deleteFiles(extras.getStringArrayList(FtIntent.Extras.ICON_PATHS));
    }

    private static void deleteFiles(List<String> list) {
        if (list != null) {
            for (String path : list) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        if (file.delete()) {
                            Log.i(LOG_TAG, "deleteFile success!");
                        } else {
                            Log.i(LOG_TAG, "deleteFile failed!");
                        }
                    } catch (Exception e) {
                        String str = LOG_TAG;
                        Log.i(str, "deleteFile failed! " + e.getMessage());
                    }
                }
            }
        }
    }
}
