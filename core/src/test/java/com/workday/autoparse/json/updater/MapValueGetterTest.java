/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.updater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

/**
 * @author nathan.taylor
 * @since 2015-08-19.
 */
@RunWith(JUnit4.class)
public class MapValueGetterTest {

    Map<String, Object> map;

    @Before
    public void setUp() {
        map = new HashMap<>();
    }

    @Test
    public void testBigDecimalAsBigDecimal() {
        map.put("value", BigDecimal.valueOf(1.1));
        BigDecimal actual = MapValueGetter.getAsBigDecimal(map, "value");
        BigDecimal expected = BigDecimal.valueOf(1.1);
        assertEquals(expected, actual);
    }

    @Test
    public void testBigDecimalFromString() {
        map.put("value", "1.1");
        BigDecimal actual = MapValueGetter.getAsBigDecimal(map, "value");
        BigDecimal expected = BigDecimal.valueOf(1.1);
        assertEquals(expected, actual);
    }

    @Test
    public void testBigDecimalFromNull() {
        BigDecimal actual = MapValueGetter.getAsBigDecimal(map, "value");
        BigDecimal expected = BigDecimal.ZERO;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testBigDecimalWrongType() {
        map.put("value", true);
        MapValueGetter.getAsBigDecimal(map, "value");
    }

    @Test
    public void testBigIntegerAsBigInteger() {
        map.put("value", BigInteger.valueOf(1));
        BigInteger actual = MapValueGetter.getAsBigInteger(map, "value");
        BigInteger expected = BigInteger.valueOf(1);
        assertEquals(expected, actual);
    }

    @Test
    public void testBigIntegerFromString() {
        map.put("value", "1");
        BigInteger actual = MapValueGetter.getAsBigInteger(map, "value");
        BigInteger expected = BigInteger.valueOf(1);
        assertEquals(expected, actual);
    }

    @Test
    public void testBigIntegerFromNull() {
        BigInteger actual = MapValueGetter.getAsBigInteger(map, "value");
        BigInteger expected = BigInteger.ZERO;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testBigIntegerWrongType() {
        map.put("value", true);
        MapValueGetter.getAsBigInteger(map, "value");
    }

    @Test
    public void testBooleanAsBoolean() {
        map.put("value", true);
        Boolean actual = MapValueGetter.getAsBoolean(map, "value");
        Boolean expected = true;
        assertEquals(expected, actual);
    }

    @Test
    public void testBooleanFromString() {
        map.put("value", "true");
        Boolean actual = MapValueGetter.getAsBoolean(map, "value");
        Boolean expected = true;
        assertEquals(expected, actual);
    }

    @Test
    public void testBooleanFromNull() {
        Boolean actual = MapValueGetter.getAsBoolean(map, "value");
        Boolean expected = false;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testBooleanWrongType() {
        map.put("value", 25);
        MapValueGetter.getAsBoolean(map, "value");
    }

    @Test
    public void testByteAsByte() {
        map.put("value", (byte) 1);
        Byte actual = MapValueGetter.getAsByte(map, "value");
        Byte expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void testByteFromString() {
        map.put("value", "1");
        Byte actual = MapValueGetter.getAsByte(map, "value");
        Byte expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void testByteFromNull() {
        Byte actual = MapValueGetter.getAsByte(map, "value");
        Byte expected = 0;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testByteWrongType() {
        map.put("value", true);
        MapValueGetter.getAsByte(map, "value");
    }

    @Test(expected = NumberFormatException.class)
    public void testByteBadString() {
        map.put("value", "bob");
        MapValueGetter.getAsByte(map, "value");
    }

    @Test
    public void testCharAsChar() {
        map.put("value", 'a');
        Character actual = MapValueGetter.getAsChar(map, "value");
        Character expected = 'a';
        assertEquals(expected, actual);
    }

    @Test
    public void testCharFromString() {
        map.put("value", "a");
        Character actual = MapValueGetter.getAsChar(map, "value");
        Character expected = 'a';
        assertEquals(expected, actual);
    }

    @Test
    public void testCharFromNull() {
        Character actual = MapValueGetter.getAsChar(map, "value");
        Character expected = 0;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testCharWrongType() {
        map.put("value", true);
        MapValueGetter.getAsChar(map, "value");
    }

    @Test
    public void testDoubleAsDouble() {
        map.put("value", 1.1);
        Double actual = MapValueGetter.getAsDouble(map, "value");
        Double expected = 1.1;
        assertEquals(expected, actual);
    }

    @Test
    public void testDoubleFromString() {
        map.put("value", "1.1");
        Double actual = MapValueGetter.getAsDouble(map, "value");
        Double expected = 1.1;
        assertEquals(expected, actual);
    }

    @Test
    public void testDoubleFromNull() {
        Double actual = MapValueGetter.getAsDouble(map, "value");
        Double expected = 0d;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testDoubleWrongType() {
        map.put("value", true);
        MapValueGetter.getAsDouble(map, "value");
    }

    @Test
    public void testFloatAsFloat() {
        map.put("value", 1.1f);
        Float actual = MapValueGetter.getAsFloat(map, "value");
        Float expected = 1.1f;
        assertEquals(expected, actual);
    }

    @Test
    public void testFloatFromString() {
        map.put("value", "1.1");
        Float actual = MapValueGetter.getAsFloat(map, "value");
        Float expected = 1.1f;
        assertEquals(expected, actual);
    }

    @Test
    public void testFloatFromNull() {
        Float actual = MapValueGetter.getAsFloat(map, "value");
        Float expected = 0f;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testFloatWrongType() {
        map.put("value", true);
        MapValueGetter.getAsFloat(map, "value");
    }

    @Test
    public void testIntAsInt() {
        map.put("value", 1);
        Integer actual = MapValueGetter.getAsInt(map, "value");
        Integer expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void testIntFromString() {
        map.put("value", "1");
        Integer actual = MapValueGetter.getAsInt(map, "value");
        Integer expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void testIntFromNull() {
        Integer actual = MapValueGetter.getAsInt(map, "value");
        Integer expected = 0;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testIntWrongType() {
        map.put("value", true);
        MapValueGetter.getAsInt(map, "value");
    }

    @Test
    public void testLongAsLong() {
        map.put("value", 1L);
        Long actual = MapValueGetter.getAsLong(map, "value");
        Long expected = 1L;
        assertEquals(expected, actual);
    }

    @Test
    public void testLongFromString() {
        map.put("value", "1");
        Long actual = MapValueGetter.getAsLong(map, "value");
        Long expected = 1L;
        assertEquals(expected, actual);
    }

    @Test
    public void testLongFromNull() {
        Long actual = MapValueGetter.getAsLong(map, "value");
        Long expected = 0L;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testLongWrongType() {
        map.put("value", true);
        MapValueGetter.getAsLong(map, "value");
    }

    @Test
    public void testShortAsShort() {
        map.put("value", (short) 1);
        Short actual = MapValueGetter.getAsShort(map, "value");
        Short expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void testShortFromString() {
        map.put("value", "1");
        Short actual = MapValueGetter.getAsShort(map, "value");
        Short expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void testShortFromNull() {
        Short actual = MapValueGetter.getAsShort(map, "value");
        Short expected = 0;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testShortWrongType() {
        map.put("value", true);
        MapValueGetter.getAsShort(map, "value");
    }

    @Test
    public void testStringAsString() {
        map.put("value", "R2-D2");
        String actual = MapValueGetter.getAsString(map, "value");
        String expected = "R2-D2";
        assertEquals(expected, actual);
    }

    @Test
    public void testStringFromNull() {
        String actual = MapValueGetter.getAsString(map, "value");
        String expected = null;
        assertEquals(expected, actual);
    }

    @Test
    public void testStringFromOtherType() {
        map.put("value", 1);
        String actual = MapValueGetter.getAsString(map, "value");
        String expected = "1";
        assertEquals(expected, actual);
    }

    @Test
    public void testTypeAsType() {
        AtomicBoolean expected = new AtomicBoolean(true);
        map.put("value", expected);
        AtomicBoolean actual = MapValueGetter.getAsType(map, "value", AtomicBoolean.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testTypeFromNull() {
        AtomicBoolean actual = MapValueGetter.getAsType(map, "value", AtomicBoolean.class);
        AtomicBoolean expected = null;
        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void testTypeWrongType() {
        map.put("value", true);
        MapValueGetter.getAsType(map, "value", AtomicBoolean.class);
    }
}
