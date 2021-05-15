package com.samsung.android.cmcnsd.extension;

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionUtils {
    public static List<Field> getAllFields(Class<?> cls) {
        ArrayList arrayList = new ArrayList();
        Collections.addAll(arrayList, cls.getDeclaredFields());
        Class<? super Object> superclass = cls.getSuperclass();
        if (!(superclass == null || superclass == Object.class)) {
            arrayList.addAll(getAllFields(superclass));
        }
        return arrayList;
    }

    public static Field getField(Class<?> cls, String str) {
        Class<? super Object> superclass = cls.getSuperclass();
        try {
            return cls.getDeclaredField(str);
        } catch (NoSuchFieldException e) {
            String simpleName = ReflectionUtils.class.getSimpleName();
            Log.d(simpleName, "Could not find field " + str + " in " + cls);
            if (superclass != null) {
                return getField(superclass, str);
            }
            return null;
        }
    }

    public static <T> T getValueOf(Field field, Object obj) {
        if (field == null) {
            return null;
        }
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not read value from Field!", e);
        }
    }

    public static <T> T getValueOf(String str, Object obj) {
        Field field = getField(obj.getClass(), str);
        if (field != null) {
            return getValueOf(field, obj);
        }
        return null;
    }

    public static <T> T getValueOf(String str, Class<?> cls) {
        Field field = getField(cls, str);
        if (field != null) {
            return getValueOf(field, (Object) null);
        }
        return null;
    }

    public static boolean setValueOf(Field field, Object obj, Object obj2) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(obj, obj2);
            return true;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not read value from Field!", e);
        }
    }

    public static <T> Class<T> getClassOf(T t) {
        return t.getClass();
    }

    public static Class<?> getGenericType(Field field) {
        return (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    public static void invoke(Method method, Object obj, Object... objArr) {
        if (method != null) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            try {
                method.invoke(obj, objArr);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not invoke method!", e);
            }
        }
    }

    public static <T> T invoke2(Method method, Object obj, Object... objArr) {
        if (method == null) {
            return null;
        }
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        try {
            return method.invoke(obj, objArr);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke method!", e);
        }
    }
}
