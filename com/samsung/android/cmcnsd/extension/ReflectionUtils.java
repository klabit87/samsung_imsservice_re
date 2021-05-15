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
        List<Field> fields = new ArrayList<>();
        Collections.addAll(fields, cls.getDeclaredFields());
        Class<? super Object> superclass = cls.getSuperclass();
        if (!(superclass == null || superclass == Object.class)) {
            fields.addAll(getAllFields(superclass));
        }
        return fields;
    }

    public static Field getField(Class<?> cls, String name) {
        Class<? super Object> superclass = cls.getSuperclass();
        try {
            return cls.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            String simpleName = ReflectionUtils.class.getSimpleName();
            Log.d(simpleName, "Could not find field " + name + " in " + cls);
            if (superclass != null) {
                return getField(superclass, name);
            }
            return null;
        }
    }

    public static <T> T getValueOf(Field field, Object item) {
        if (field == null) {
            return null;
        }
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(item);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not read value from Field!", e);
        }
    }

    public static <T> T getValueOf(String name, Object item) {
        Field field = getField(item.getClass(), name);
        if (field != null) {
            return getValueOf(field, item);
        }
        return null;
    }

    public static <T> T getValueOf(String name, Class<?> cls) {
        Field field = getField(cls, name);
        if (field != null) {
            return getValueOf(field, (Object) null);
        }
        return null;
    }

    public static boolean setValueOf(Field field, Object item, Object value) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(item, value);
            return true;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not read value from Field!", e);
        }
    }

    public static <T> Class<T> getClassOf(T obj) {
        return obj.getClass();
    }

    public static Class<?> getGenericType(Field field) {
        return (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    public static void invoke(Method method, Object receiver, Object... arguments) {
        if (method != null) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            try {
                method.invoke(receiver, arguments);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not invoke method!", e);
            }
        }
    }

    public static <T> T invoke2(Method method, Object receiver, Object... arguments) {
        if (method == null) {
            return null;
        }
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        try {
            return method.invoke(receiver, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke method!", e);
        }
    }
}
