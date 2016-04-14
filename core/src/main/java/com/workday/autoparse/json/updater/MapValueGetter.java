/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.updater;

import com.workday.autoparse.json.parser.JsonParserUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * A utility class that will get values from a {@link Map} of Objects and attempt to convert the
 * value to the desired type.
 * <p/>
 * Methods that return numbers or boxed primitives will attempt to parse String values into the
 * desired type. If the actual value cannot be converted to the desired type, these methods will
 * throw a {@link WrongTypeException}.
 *
 * @author nathan.taylor
 * @since 2015-08-13.
 */
public class MapValueGetter {

    private MapValueGetter() {
    }

    public static <T> T getAsType(Map<String, Object> map, String key, Class<T> objectType) {
        com.workday.autoparse.json.utils.Preconditions.checkNotNull(map, "map");
        com.workday.autoparse.json.utils.Preconditions.checkNotNull(key, "key");
        com.workday.autoparse.json.utils.Preconditions.checkNotNull(objectType, "objectType");

        Object value = map.get(key);

        if (value == null) {
            return null;
        }

        if (objectType.isInstance(value)) {
            @SuppressWarnings("unchecked")
            T castedValue = (T) value;
            return castedValue;
        }

        throw new WrongTypeException(key, objectType, value);
    }

    public static BigDecimal getAsBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof String) {
            return new BigDecimal((String) value);
        }

        throw new WrongTypeException(key, BigDecimal.class, value);
    }

    public static BigInteger getAsBigInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value == null) {
            return BigInteger.ZERO;
        }

        if (value instanceof String) {
            return new BigInteger((String) value);
        }

        throw new WrongTypeException(key, BigInteger.class, value);
    }

    public static boolean getAsBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Boolean) {
            return (boolean) value;
        }

        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            return Boolean.valueOf((String) value);
        }

        throw new WrongTypeException(key, "boolean", value);
    }

    public static byte getAsByte(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Byte) {
            return (byte) value;
        }

        if (value == null) {
            return 0;
        }

        if (value instanceof String) {
            return Byte.valueOf((String) value);
        }

        throw new WrongTypeException(key, "byte", value);
    }

    public static char getAsChar(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Character) {
            return (char) value;
        }

        if (value == null) {
            return 0;
        }

        if (value instanceof String) {
            return JsonParserUtils.getCharFromString((String) value);
        }

        throw new WrongTypeException(key, "char", value);
    }

    public static double getAsDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Double) {
            return (double) value;
        }

        if (value == null) {
            return 0;
        }

        if (value instanceof String) {
            return Double.valueOf((String) value);
        }

        throw new WrongTypeException(key, "double", value);
    }

    public static float getAsFloat(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Float) {
            return (float) value;
        }

        if (value == null) {
            return 0;
        }

        if (value instanceof String) {
            return Float.valueOf((String) value);
        }

        throw new WrongTypeException(key, "float", value);
    }

    public static int getAsInt(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Integer) {
            return (int) value;
        }

        if (value == null) {
            return 0;
        }

        if (value instanceof String) {
            return Integer.valueOf((String) value);
        }

        throw new WrongTypeException(key, "int", value);
    }

    public static long getAsLong(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Long) {
            return (long) value;
        }

        if (value == null) {
            return 0;
        }

        if (value instanceof String) {
            return Long.valueOf((String) value);
        }

        throw new WrongTypeException(key, "long", value);
    }

    public static short getAsShort(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Short) {
            return (short) value;
        }

        if (value == null) {
            return 0;
        }

        if (value instanceof String) {
            return Short.valueOf((String) value);
        }

        throw new WrongTypeException(key, "short", value);
    }

    public static String getAsString(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof String) {
            return (String) value;
        }

        if (value == null) {
            return null;
        }

        return value.toString();
    }
}
