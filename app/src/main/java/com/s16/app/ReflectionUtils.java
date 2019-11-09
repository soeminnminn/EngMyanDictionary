package com.s16.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.text.TextUtils;
import android.util.Log;

public class ReflectionUtils {
	
	private static final String TAG = ReflectionUtils.class.getSimpleName();
	
	public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Method getMethod(Class<?> targetClass, String name,
            Class<?>... parameterTypes) {
        if (targetClass == null || TextUtils.isEmpty(name)) return null;
        try {
            return targetClass.getMethod(name, parameterTypes);
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            // ignore
        }
        return null;
    }

    public static Field getField(Class<?> targetClass, String name) {
        if (targetClass == null || TextUtils.isEmpty(name)) return null;
        try {
            return targetClass.getField(name);
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchFieldException e) {
            // ignore
        }
        return null;
    }

    public static Constructor<?> getConstructor(Class<?> targetClass, Class<?> ... types) {
        if (targetClass == null || types == null) return null;
        try {
            return targetClass.getConstructor(types);
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            // ignore
        }
        return null;
    }

    public static Object newInstance(Constructor<?> constructor, Object ... args) {
        if (constructor == null) return null;
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            Log.e(TAG, "Exception in newInstance: " + e.getClass().getSimpleName());
        }
        return null;
    }

	@SuppressWarnings("unchecked")
	public static <T> T invoke(
            Object receiver, T defaultValue, Method method, Object... args) {
        if (method == null) return defaultValue;
        try {
        	Object retValue = method.invoke(receiver, args);
        	if (retValue != null) {
        		return (T)retValue;
        	}
        } catch (Exception e) {
            Log.e(TAG, "Exception in invoke: " + e.getClass().getSimpleName());
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object receiver, T defaultValue, Field field) {
        if (field == null) return defaultValue;
        try {
        	Object retValue = field.get(receiver);
        	if (retValue != null) {
        		return (T)retValue;
        	}
        } catch (Exception e) {
            Log.e(TAG, "Exception in getFieldValue: " + e.getClass().getSimpleName());
        }
        return defaultValue;
    }

    public static void setFieldValue(Object receiver, Field field, Object value) {
        if (field == null) return;
        try {
            field.set(receiver, value);
        } catch (Exception e) {
            Log.e(TAG, "Exception in setFieldValue: " + e.getClass().getSimpleName());
        }
    }
}
