/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utility class for creating {@link Converter}s.
 *
 * @author nathan.taylor
 * @since 2014-10-13.
 */
final class Converters {

    private Converters() {
    }

    /**
     * Get the instance of a {@link Converter} that will convert from a String to the provided
     * class.
     */
    // We are checking classes; casting is safe.
    @SuppressWarnings("unchecked")
    public static <T> Converter<T> getConverter(Class<T> toClass) {
        if (Byte.class.equals(toClass)) {
            return (Converter<T>) ByteConverter.INSTANCE;
        } else if (Character.class.equals(toClass)) {
            return (Converter<T>) CharacterConverter.INSTANCE;
        } else if (Double.class.equals(toClass)) {
            return (Converter<T>) DoubleConverter.INSTANCE;
        } else if (Float.class.equals(toClass)) {
            return (Converter<T>) FloatConverter.INSTANCE;
        } else if (Integer.class.equals(toClass)) {
            return (Converter<T>) IntegerConverter.INSTANCE;
        } else if (Long.class.equals(toClass)) {
            return (Converter<T>) LongConverter.INSTANCE;
        } else if (Short.class.equals(toClass)) {
            return (Converter<T>) ShortConverter.INSTANCE;
        } else if (BigDecimal.class.equals(toClass)) {
            return (Converter<T>) BigDecimalConverter.INSTANCE;
        } else if (BigInteger.class.equals(toClass)) {
            return (Converter<T>) BigIntegerConverter.INSTANCE;
        }
        throw new IllegalArgumentException("Cannot convert from java.lang.String to "
                                                   + toClass.getCanonicalName());
    }

    public static boolean isConvertibleFromString(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz) || Character.class.equals(clazz);
    }

    public static class ByteConverter implements Converter<Byte> {

        public static final ByteConverter INSTANCE = new ByteConverter();

        private ByteConverter() {
        }

        @Override
        public Byte convert(String value) {
            return Byte.valueOf(value);
        }
    }

    public static class CharacterConverter implements Converter<Character> {

        public static final CharacterConverter INSTANCE = new CharacterConverter();

        private CharacterConverter() {
        }

        @Override
        public Character convert(String value) {
            return value.charAt(0);
        }
    }

    public static class DoubleConverter implements Converter<Double> {

        public static final DoubleConverter INSTANCE = new DoubleConverter();

        private DoubleConverter() {
        }

        @Override
        public Double convert(String value) {
            return Double.valueOf(value);
        }
    }

    public static class FloatConverter implements Converter<Float> {

        public static final FloatConverter INSTANCE = new FloatConverter();

        private FloatConverter() {
        }

        @Override
        public Float convert(String value) {
            return Float.valueOf(value);
        }
    }

    public static class IntegerConverter implements Converter<Integer> {

        public static final IntegerConverter INSTANCE = new IntegerConverter();

        private IntegerConverter() {
        }

        @Override
        public Integer convert(String value) {
            return Integer.valueOf(value);
        }
    }

    public static class LongConverter implements Converter<Long> {

        public static final LongConverter INSTANCE = new LongConverter();

        private LongConverter() {
        }

        @Override
        public Long convert(String value) {
            return Long.valueOf(value);
        }
    }

    public static class ShortConverter implements Converter<Short> {

        public static final ShortConverter INSTANCE = new ShortConverter();

        private ShortConverter() {
        }

        @Override
        public Short convert(String value) {
            return Short.valueOf(value);
        }
    }

    public static class BigDecimalConverter implements Converter<BigDecimal> {

        public static final BigDecimalConverter INSTANCE = new BigDecimalConverter();

        private BigDecimalConverter() {
        }

        @Override
        public BigDecimal convert(String value) {
            return new BigDecimal(value);
        }
    }

    public static class BigIntegerConverter implements Converter<BigInteger> {

        public static final BigIntegerConverter INSTANCE = new BigIntegerConverter();

        private BigIntegerConverter() {
        }

        @Override
        public BigInteger convert(String value) {
            return new BigInteger(value);
        }
    }

}
