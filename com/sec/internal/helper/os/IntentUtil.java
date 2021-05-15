package com.sec.internal.helper.os;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

public class IntentUtil {
    private static final int FLAG_RECEIVER_INCLUDE_BACKGROUND = 16777216;

    public static void sendBroadcast(Context context, Intent intent) {
        intent.addFlags(FLAG_RECEIVER_INCLUDE_BACKGROUND);
        context.sendBroadcast(intent);
    }

    public static void sendBroadcast(Context context, Intent intent, UserHandle userHandle) {
        intent.addFlags(FLAG_RECEIVER_INCLUDE_BACKGROUND);
        context.sendBroadcastAsUser(intent, userHandle);
    }
}
