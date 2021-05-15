package com.samsung.android.cmcnsd.extension;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.UserHandle;
import java.lang.reflect.Field;

public class ContextExt {
    public static final UserHandle ALL = ((UserHandle) ReflectionUtils.getValueOf("ALL", (Class<?>) UserHandle.class));
    public static final UserHandle CURRENT = ((UserHandle) ReflectionUtils.getValueOf("CURRENT", (Class<?>) UserHandle.class));
    public static final UserHandle CURRENT_OR_SELF = ((UserHandle) ReflectionUtils.getValueOf("CURRENT_OR_SELF", (Class<?>) UserHandle.class));
    public static final String HQM_SERVICE = getStringFromField("HQM_SERVICE", "HqmManagerService");
    public static final UserHandle OWNER = ((UserHandle) ReflectionUtils.getValueOf("OWNER", (Class<?>) UserHandle.class));
    public static final String STATUS_BAR_SERVICE = ((String) ReflectionUtils.getValueOf("STATUS_BAR_SERVICE", (Class<?>) Context.class));

    public static boolean bindServiceAsUser(Context context, Intent intent, ServiceConnection serviceConnection, int i, UserHandle userHandle) {
        try {
            Class<?> cls = context.getClass();
            Object invoke2 = ReflectionUtils.invoke2(cls.getMethod("bindServiceAsUser", new Class[]{Intent.class, ServiceConnection.class, Integer.TYPE, UserHandle.class}), context, intent, serviceConnection, Integer.valueOf(i), userHandle);
            if (invoke2 == null || !((Boolean) invoke2).booleanValue()) {
                return false;
            }
            return true;
        } catch (IllegalStateException | NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Intent registerReceiverAsUser(Context context, BroadcastReceiver broadcastReceiver, UserHandle userHandle, IntentFilter intentFilter, String str, Handler handler) {
        try {
            return (Intent) ReflectionUtils.invoke2(context.getClass().getMethod("registerReceiverAsUser", new Class[]{BroadcastReceiver.class, UserHandle.class, IntentFilter.class, String.class, Handler.class}), context, broadcastReceiver, userHandle, intentFilter, str, handler);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendBroadcastAsUser(Context context, Intent intent, UserHandle userHandle) {
        try {
            ReflectionUtils.invoke(context.getClass().getMethod("sendBroadcastAsUser", new Class[]{Intent.class, UserHandle.class}), context, intent, userHandle);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static String getStringFromField(String str, String str2) {
        try {
            Field field = ReflectionUtils.getField(Context.class, str);
            if (field != null) {
                return (String) field.get((Object) null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return str2;
    }
}
