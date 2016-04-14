/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.utils.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.workday.autoparse.json.demo.InstanceUpdaterTestUtils.CONTEXT;
import static com.workday.autoparse.json.demo.InstanceUpdaterTestUtils.getUpdateMapFromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author nathan.taylor
 * @since 2015-08-17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class InstanceUpdaterTest {

    private static final float ERROR = 1e-6f;

    @Test
    public void testPrimitives() {
        TestObject testObject = new TestObject();
        testObject.myBoolean = false;
        testObject.myByte = 1;
        testObject.myChar = 'a';
        testObject.myDouble = 1.1;
        testObject.myFloat = 1.1f;
        testObject.myInt = 1;
        testObject.myLong = 1L;
        testObject.myShort = 1;

        Map<String, Object> updates = new HashMap<>();
        updates.put("myBoolean", true);
        updates.put("myByte", (byte) 2);
        updates.put("myChar", 'b');
        updates.put("myDouble", 2.2);
        updates.put("myFloat", 2.2f);
        updates.put("myInt", 2);
        updates.put("myLong", 2L);
        updates.put("myShort", (short) 2);

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myBoolean", true, testObject.myBoolean);
        assertEquals("myByte", 2, testObject.myByte);
        assertEquals("myChar", 'b', testObject.myChar);
        assertEquals("myDouble", 2.2, testObject.myDouble, ERROR);
        assertEquals("myFloat", 2.2f, testObject.myFloat, ERROR);
        assertEquals("myInt", 2, testObject.myInt);
        assertEquals("myLong", 2L, testObject.myLong);
        assertEquals("myShort", (short) 2, testObject.myShort);
    }

    @Test
    public void testPrimitivesFromStrings() {
        TestObject testObject = new TestObject();
        testObject.myBoolean = false;
        testObject.myByte = 1;
        testObject.myChar = 'a';
        testObject.myDouble = 1.1;
        testObject.myFloat = 1.1f;
        testObject.myInt = 1;
        testObject.myLong = 1L;
        testObject.myShort = 1;

        Map<String, Object> updates = new HashMap<>();
        updates.put("myBoolean", true);
        updates.put("myByte", "2");
        updates.put("myChar", "b");
        updates.put("myDouble", "2.2");
        updates.put("myFloat", "2.2");
        updates.put("myInt", "2");
        updates.put("myLong", "2");
        updates.put("myShort", "2");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myBoolean", true, testObject.myBoolean);
        assertEquals("myByte", 2, testObject.myByte);
        assertEquals("myChar", 'b', testObject.myChar);
        assertEquals("myDouble", 2.2, testObject.myDouble, ERROR);
        assertEquals("myFloat", 2.2f, testObject.myFloat, ERROR);
        assertEquals("myInt", 2, testObject.myInt);
        assertEquals("myLong", 2L, testObject.myLong);
        assertEquals("myShort", (short) 2, testObject.myShort);
    }

    @Test
    public void testBoxedPrimitives() {
        TestObject testObject = new TestObject();
        testObject.myBoxedBoolean = false;
        testObject.myBoxedByte = 1;
        testObject.myBoxedChar = 'a';
        testObject.myBoxedDouble = 1.1;
        testObject.myBoxedFloat = 1.1f;
        testObject.myBoxedInt = 1;
        testObject.myBoxedLong = 1L;
        testObject.myBoxedShort = 1;

        Map<String, Object> updates = new HashMap<>();
        updates.put("myBoxedBoolean", true);
        updates.put("myBoxedByte", (byte) 2);
        updates.put("myBoxedChar", 'b');
        updates.put("myBoxedDouble", 2.2);
        updates.put("myBoxedFloat", 2.2f);
        updates.put("myBoxedInt", 2);
        updates.put("myBoxedLong", 2L);
        updates.put("myBoxedShort", (short) 2);

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myBoxedBoolean", true, testObject.myBoxedBoolean);
        assertEquals("myBoxedByte", Byte.valueOf((byte) 2), testObject.myBoxedByte);
        assertEquals("myBoxedChar", Character.valueOf('b'), testObject.myBoxedChar);
        assertEquals("myBoxedDouble", Double.valueOf(2.2), testObject.myBoxedDouble);
        assertEquals("myBoxedFloat", Float.valueOf(2.2f), testObject.myBoxedFloat);
        assertEquals("myBoxedInt", Integer.valueOf(2), testObject.myBoxedInt);
        assertEquals("myBoxedLong", Long.valueOf(2L), testObject.myBoxedLong);
        assertEquals("myBoxedShort", Short.valueOf((short) 2), testObject.myBoxedShort);
    }

    @Test(expected = RuntimeException.class)
    public void testCannotConvertObjectToPrimitive() {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = new HashMap<>();
        updates.put("myBoolean", new JSONObject());

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);
    }

    @Test
    public void testSpecialObjects() {
        TestObject testObject = new TestObject();
        testObject.myString = "antman";
        testObject.myBigDecimal = BigDecimal.valueOf(1.1);
        testObject.myBigInteger = BigInteger.valueOf(1);

        Map<String, Object> updates = new HashMap<>();
        updates.put("myString", "batman");
        updates.put("myBigDecimal", BigDecimal.valueOf(2.2));
        updates.put("myBigInteger", BigInteger.valueOf(2));

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myString", "batman", testObject.myString);
        assertEquals("myBigDecimal", BigDecimal.valueOf(2.2), testObject.myBigDecimal);
        assertEquals("myBigInteger", BigInteger.valueOf(2), testObject.myBigInteger);
    }

    @Test
    public void testBigNumbersFromStrings() {
        TestObject testObject = new TestObject();
        testObject.myBigDecimal = BigDecimal.valueOf(1.1);
        testObject.myBigInteger = BigInteger.valueOf(1);

        Map<String, Object> updates = new HashMap<>();
        updates.put("myBigDecimal", "2.2");
        updates.put("myBigInteger", "2");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myBigDecimal", BigDecimal.valueOf(2.2), testObject.myBigDecimal);
        assertEquals("myBigInteger", BigInteger.valueOf(2), testObject.myBigInteger);
    }

    @Test
    public void testMaps() {
        TestObject testObject = new TestObject();
        testObject.myInterfaceMap = new java.util.LinkedHashMap<>();
        testObject.myInterfaceMap.put("value", new TestObject.InnerTestObject("inside"));

        Map<String, Object> updates = new HashMap<>();

        LinkedHashMap<String, TestObject.InnerTestObject> newInterfaceMap = new LinkedHashMap<>();
        newInterfaceMap.put("value", new TestObject.InnerTestObject("out"));
        updates.put("myInterfaceMap", newInterfaceMap);

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myInterfaceMap", newInterfaceMap, testObject.myInterfaceMap);
    }

    @Test
    public void testMapsFromJsonObjects() throws Exception {
        TestObject testObject = new TestObject();
        testObject.myInterfaceMap = new LinkedHashMap<>();
        testObject.myInterfaceMap.put("value", new TestObject.InnerTestObject("inside"));

        Map<String, Object> updates = getUpdateMapFromFile("update-maps.json");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        LinkedHashMap<String, TestObject.InnerTestObject> expected = new LinkedHashMap<>();
        expected.put("value", new TestObject.InnerTestObject("out"));
        assertEquals("myInterfaceMap", expected, testObject.myInterfaceMap);
    }

    @Test(expected = RuntimeException.class)
    public void testCannotConvertArrayToMap() {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = new HashMap<>();
        updates.put("myInterfaceMap", new JSONArray());

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);
    }

    @Test
    public void testCollections() {
        TestObject testObject = new TestObject();

        HashMap<String, Object> updates = new HashMap<>();
        List<List<Integer>> lists = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        list.add(5);
        lists.add(list);
        updates.put("myCollectionOfCollections", lists);

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals(CollectionUtils.<List>newArrayList(CollectionUtils.newArrayList(5)),
                     testObject.myCollectionOfCollections);
    }

    @Test
    public void testCollectionsFromJsonArrays() throws Exception {
        TestObject testObject = new TestObject();
        testObject.myStringCollection = CollectionUtils.newArrayList("one", "two", "three");

        Map<String, Object> updates = getUpdateMapFromFile("update-collections.json");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals(CollectionUtils.newArrayList("3", "2", "1"), testObject.myStringCollection);
    }

    @Test(expected = RuntimeException.class)
    public void testCannotConvertStringToCollection() {
        TestObject testObject = new TestObject();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("myStringCollection", "I don't belong :`( ");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);
    }

    @Test
    public void testCustomObjects() {
        TestObject testObject = new TestObject();
        testObject.myInnerObject = new TestObject.InnerTestObject("Remus");

        Map<String, Object> updates = new HashMap<>();
        updates.put("myInnerObject", new TestObject.InnerTestObject("Romulus"));

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myInnerObject.string", "Romulus", testObject.myInnerObject.string);
    }

    @Test
    public void testCustomObjectWithNullValueInMap() {
        TestObject testObject = new TestObject();
        testObject.myInnerObject = new TestObject.InnerTestObject("Malcom Reynolds");

        Map<String, Object> updates = new HashMap<>();
        updates.put("myInnerObject", null);

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myInnerObject", null, testObject.myInnerObject);
    }

    @Test
    public void testCustomObjectsFromJson() throws Exception {
        TestObject testObject = new TestObject();
        testObject.myInnerObject = new TestObject.InnerTestObject("Remus");

        Map<String, Object> updates = getUpdateMapFromFile("update-custom-object.json");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("myInnerObject.string", "Romulus", testObject.myInnerObject.string);
    }

    @Test(expected = RuntimeException.class)
    public void testCannotConvertStringToObject() {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = new HashMap<>();
        updates.put("myInnerObject", "Nobody loves me.");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);
    }

    @Test
    public void testJSONObject() throws Exception {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = getUpdateMapFromFile("update-json-object.json");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertNotNull("testObject.myJsonObject", testObject.myJsonObject);
        assertEquals("Klingon", testObject.myJsonObject.optString("string"));
    }

    @Test(expected = RuntimeException.class)
    public void testCannotConvertJsonArrayToJsonObject() {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = new HashMap<>();
        updates.put("myJsonObject", new JSONArray());

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);
    }

    @Test
    public void testJSONArray() throws Exception {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = getUpdateMapFromFile("update-json-array.json");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertNotNull("testObject.myJsonArray", testObject.myJsonArray);
        assertEquals(3, testObject.myJsonArray.length());
        assertEquals("one", testObject.myJsonArray.optString(0));
        assertEquals(2, testObject.myJsonArray.optInt(1));
        assertTrue("myJsonArray[2] instanceof JSONObject", testObject.myJsonArray.opt(2) instanceof JSONObject);
    }

    @Test(expected = RuntimeException.class)
    public void testCannotConvertJsonObjectToJsonArray() {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = new HashMap<>();
        updates.put("myJsonArray", new JSONObject());

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);
    }

    @Test
    public void testJSONObjectCollection() throws Exception {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = getUpdateMapFromFile("update-json-object-collection.json");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertNotNull(testObject.myJsonObjectCollection);
        assertEquals(2, testObject.myJsonObjectCollection.size());
        assertNotNull(testObject.myJsonObjectCollection.get(0));
        assertNotNull(testObject.myJsonObjectCollection.get(1));
    }

    @Test
    public void testSelfValuesMap() {
        SelfMapObject selfMapObject = new SelfMapObject();
        selfMapObject.string = "a";
        selfMapObject.selfValues = new HashMap<>();
        selfMapObject.selfValues.put("key1", "value 1");
        selfMapObject.selfValues.put("key2", "value 2");

        Map<String, Object> updates = new HashMap<>();
        updates.put("string", "b");
        updates.put("key1", "value 1 updated");

        SelfMapObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(selfMapObject, updates, CONTEXT);

        assertEquals("string", "b", selfMapObject.string);
        assertEquals("selfValues.size", 2, selfMapObject.selfValues.size());
        assertEquals("selfValues[key1]", "value 1 updated", selfMapObject.selfValues.get("key1"));
        assertEquals("selfValues[key2]", "value 2", selfMapObject.selfValues.get("key2"));
    }

    @Test
    public void testSelfValuesMapFromJsonObject() throws Exception {
        SelfMapObject selfMapObject = new SelfMapObject();
        selfMapObject.string = "a";
        selfMapObject.selfValues = new HashMap<>();
        selfMapObject.selfValues.put("key1", "value 1");
        selfMapObject.selfValues.put("key2", "value 2");

        Map<String, Object> updates = getUpdateMapFromFile("update-self-values.json");

        SelfMapObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(selfMapObject, updates, CONTEXT);

        assertEquals("string", "b", selfMapObject.string);
        assertEquals("selfValues.size", 3, selfMapObject.selfValues.size());
        assertEquals("selfValues[key1]", "value 1 updated", selfMapObject.selfValues.get("key1"));
        assertEquals("selfValues[key2]", "value 2", selfMapObject.selfValues.get("key2"));
        assertEquals("selfValues[key3]", new SimpleTestObject("simple string", "simpleTestObject"),
                     selfMapObject.selfValues.get("key3"));
    }

    @Test
    public void testSelfValuesMapUnconverted() throws Exception {
        SelfMapObjectUnconverted selfMapObject = new SelfMapObjectUnconverted();
        selfMapObject.string = "a";
        selfMapObject.selfValuesUnconverted = new HashMap<>();
        selfMapObject.selfValuesUnconverted.put("key1", "value 1");
        selfMapObject.selfValuesUnconverted.put("key2", "value 2");

        Map<String, Object> updates = getUpdateMapFromFile("update-self-values.json");

        SelfMapObjectUnconverted$$JsonObjectParser.INSTANCE.updateInstanceFromMap(selfMapObject, updates, CONTEXT);

        assertEquals("string", "b", selfMapObject.string);
        assertEquals("selfValuesUnconverted.size", 3, selfMapObject.selfValuesUnconverted.size());
        assertEquals("selfValuesUnconverted[key1]", "value 1 updated", selfMapObject.selfValuesUnconverted.get("key1"));
        assertEquals("selfValuesUnconverted[key2]", "value 2", selfMapObject.selfValuesUnconverted.get("key2"));
        assertThat(selfMapObject.selfValuesUnconverted.get("key3")).isInstanceOf(JSONObject.class);
    }

    @Test
    public void testNoChanges() {
        TestObject testObject = new TestObject();
        testObject.myString = "C-3PO";

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, new HashMap<String, Object>(0),
                                                                    CONTEXT);

        assertEquals("myString", "C-3PO", testObject.myString);
    }

    @Test
    public void testStringWithNullValueInMap() {
        TestObject testObject = new TestObject();
        testObject.myString = "BB-8";

        Map<String, Object> updates = new HashMap<>();
        updates.put("myString", null);

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals(null, testObject.myString);
    }

    @Test
    public void testSetters() {
        TestObject testObject = new TestObject();
        testObject.stringFromSetter = "B-4";

        Map<String, Object> updates = new HashMap<>();
        updates.put("stringSetter", "Data");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("stringSetter", "Data", testObject.stringFromSetter);
    }

    @Test
    public void testPostCreateChildMethodCalled() throws Exception {
        TestObject testObject = new TestObject();

        Map<String, Object> updates = getUpdateMapFromFile("update-collection-of-collections.json");

        TestObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(testObject, updates, CONTEXT);

        assertEquals("post-parse:original", testObject.mySetsOfTestObjects.get(0).iterator().next().myString);
    }
}
