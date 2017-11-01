/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.context.JsonParserSettingsBuilder;
import com.workday.autoparse.json.parser.JsonStreamParser;
import com.workday.autoparse.json.parser.JsonStreamParserFactory;
import com.workday.autoparse.json.utils.CollectionUtils;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author nathan.taylor
 * @since 2014-09-09
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class JsonParserTest {

    private static final float ERROR = 1e-6f;

    JsonStreamParser parser;

    @Before
    public void setUp() {
        parser = JsonStreamParserFactory.newJsonStreamParser(
                new JsonParserSettingsBuilder().withDiscriminationName("object").build());
    }

    @Test
    public void testImmediateParse() throws Exception {
        testParse("single-object.json");
    }

    @Test
    public void testFullDelayParse() throws Exception {
        testParse("delayed-object.json");
    }

    @Test
    public void testPartialDelayParse() throws Exception {
        testParse("partially-delayed-object.json");
    }

    @Test
    public void testFullDelayNullMap() throws Exception {
        TestObject testObject = (TestObject) parser.parseJsonStream(getInputStream("null-in-maps.json"));

        assertNotNull("testObject.myStringMapWithSingleNullValue", testObject.myStringMapWithSingleNullValue);
        assertEquals("testObject.myStringMapWithSingleNullValue.size", 1, testObject.myStringMapWithSingleNullValue.size());
        assertEquals("testObject.myStringMapWithSingleNullValue[key1]", null, testObject.myStringMapWithSingleNullValue.get("key1"));

        assertNotNull("testObject.myStringMapWithNullValues", testObject.myStringMapWithNullValues);
        assertEquals("testObject.myStringMapWithNullValues.size", 3, testObject.myStringMapWithNullValues.size());
        assertEquals("testObject.myStringMapWithNullValues[key1]", null, testObject.myStringMapWithNullValues.get("key1"));
        assertEquals("testObject.myStringMapWithNullValues[key2]", "value2", testObject.myStringMapWithNullValues.get("key2"));
        assertEquals("testObject.myStringMapWithNullValues[key3]", "null", testObject.myStringMapWithNullValues.get("key3"));

        assertNotNull("testObject.myObjectMapWithNullValues", testObject.myObjectMapWithNullValues);
        assertEquals("testObject.myObjectMapWithNullValues.size", 3, testObject.myObjectMapWithNullValues.size());
        assertEquals("testObject.myObjectMapWithNullValues[key1]", new SimpleTestObject(null), testObject.myObjectMapWithNullValues.get("key1"));
        assertEquals("testObject.myObjectMapWithNullValues[key2]", new SimpleTestObject("post-parse:value2"), testObject.myObjectMapWithNullValues.get("key2"));
        assertEquals("testObject.myObjectMapWithNullValues[key3]", new SimpleTestObject("post-parse:null"), testObject.myObjectMapWithNullValues.get("key3"));
    }

    private void testParse(String fileName) throws Exception {
        TestObject testObject = (TestObject) parser.parseJsonStream(getInputStream(fileName));
        assertNotNull("testObject", testObject);

        assertEquals("testObject.discriminationValue", "testObject", testObject.discriminationValue);
        assertEquals("testObject.superDiscriminationValue", "testObject", testObject.superDiscriminationValue);

        // Basic Types
        assertTrue("testObject.myBoolean", testObject.myBoolean);
        assertEquals("testObject.myByte", (byte) 65, testObject.myByte);
        assertEquals("testObject.myChar", 'c', testObject.myChar);
        assertEquals("testObject.myDouble", 12.34, testObject.myDouble, ERROR);
        assertEquals("testObject.myFloat", 123.45f, testObject.myFloat, ERROR);
        assertEquals("testObject.myInt", 12, testObject.myInt);
        assertEquals("testObject.myLong", 123456, testObject.myLong);
        assertEquals("testObject.myShort", 17, testObject.myShort);

        assertTrue("testObject.myBoxedBoolean", testObject.myBoxedBoolean);
        assertEquals("testObject.myBoxedByte", Byte.valueOf("63"), testObject.myBoxedByte);
        assertEquals("testObject.myBoxedChar", Character.valueOf('d'), testObject.myBoxedChar);
        assertEquals("testObject.myBoxedDouble", Double.valueOf("12.345"), testObject.myBoxedDouble);
        assertEquals("testObject.myBoxedFloat", Float.valueOf("123.456"), testObject.myBoxedFloat);
        assertEquals("testObject.myBoxedInt", Integer.valueOf("123"), testObject.myBoxedInt);
        assertEquals("testObject.myBoxedLong", Long.valueOf(1234567), testObject.myBoxedLong);
        assertEquals("testObject.myBoxedShort", new Short("18"), testObject.myBoxedShort);

        assertEquals("testObject.myString", "hello", testObject.myString);
        assertEquals("testObject.myBigDecimal", new BigDecimal("123456789.0123456789"), testObject.myBigDecimal);
        assertEquals("testObject.myBigInteger", new BigInteger("1234567891011"), testObject.myBigInteger);
        assertEquals("testObject.overriddenThing", 123, testObject.overriddenThing);
        assertNull("testObject.superOverriddenThing", testObject.superOverriddenThing);

        // Maps
        assertNotNull("testObject.myStringMap", testObject.myStringMap);
        assertEquals("testObject.myStringMap.size", 2, testObject.myStringMap.size());
        assertEquals("testObject.myStringMap[key1]", "value1", testObject.myStringMap.get("key1"));
        assertEquals("testObject.myStringMap[key2]", "value2", testObject.myStringMap.get("key2"));

        assertNotNull("testObject.myTestObjectMap", testObject.myTestObjectMap);
        assertEquals("testObject.myTestObjectMap.size", 2, testObject.myTestObjectMap.size());
        assertEquals("testObject.myTestObjectMap[key1]", new SimpleTestObject("post-parse:string 1"),
                     testObject.myTestObjectMap.get("key1"));
        assertEquals("testObject.myTestObjectMap[key2]", new SimpleTestObject("post-parse:string 2"),
                     testObject.myTestObjectMap.get("key2"));

        assertNotNull("testObject.myInterfaceMap", testObject.myInterfaceMap);
        assertEquals("testObject.myInterfaceMap.size", 2, testObject.myInterfaceMap.size());
        assertEquals("testObject.myInterfaceMap[key1]", new TestObject.InnerTestObject("string 1"),
                     testObject.myInterfaceMap.get("key1"));
        assertEquals("testObject.myInterfaceMap[key2]", new TestObject.InnerTestObject("string 2"),
                     testObject.myInterfaceMap.get("key2"));

        assertNotNull("testObject.myObjectMap", testObject.myObjectMap);
        assertEquals("testObject.myObjectMap.size", 2, testObject.myTestObjectMap.size());
        assertEquals("testObject.myObjectMap[key1]", new SimpleTestObject("post-parse:string 1", "simpleTestObject"),
                     testObject.myObjectMap.get("key1"));
        assertEquals("testObject.myObjectMap[key2]", "25", testObject.myObjectMap.get("key2"));

        // Collections
        assertEquals("testObject.myBooleanCollection", CollectionUtils.newHashSet(true, false, true),
                     testObject.myBooleanCollection);
        assertEquals("testObject.myByteCollection", CollectionUtils.newHashSet((byte) 63, (byte) 64),
                     testObject.myByteCollection);
        assertEquals("testObject.myCharCollection", new LinkedHashSet<>(CollectionUtils.newArrayList('d', 'e')),
                     testObject.myCharCollection);
        assertEquals("testObject.myDoubleCollection",
                     new LinkedList<>(CollectionUtils.newArrayList(12.345, 13.345)),
                     testObject.myDoubleCollection);
        assertEquals("testObject.myFloatCollection", CollectionUtils.newArrayList(123.456f, 234.56f),
                     testObject.myFloatCollection);
        assertEquals("testObject.myIntCollection", CollectionUtils.newArrayList(123, 456), testObject.myIntCollection);
        assertEquals("testObject.myLongCollection", CollectionUtils.newArrayList(1234567L, 2345678L),
                     testObject.myLongCollection);
        assertEquals("testObject.myShortCollection", CollectionUtils.newArrayList((short) 18, (short) 19),
                     testObject.myShortCollection);
        assertEquals("testObject.myStringCollection", CollectionUtils.newArrayList("hello", "there"),
                     testObject.myStringCollection);
        assertEquals("testObject.myBigDecimalCollection",
                     CollectionUtils.newArrayList(new BigDecimal("123456789.0123456789"),
                                                  new BigDecimal("23456789.0123456789")),
                     testObject.myBigDecimalCollection);
        assertEquals("testObject.myBigIntegerCollection",
                     CollectionUtils.newArrayList(new BigInteger("1234567891011"), new BigInteger("234567891011")),
                     testObject.myBigIntegerCollection);

        // Custom Objects
        SimpleTestObject singularChild = testObject.mySingularChild;
        assertNotNull("testObject.mySingularChild", singularChild);
        assertEquals("testObject.mySingularChild.myString", "post-parse:a singular child", singularChild.myString);
        assertTrue("testObject.mySingularChildByInterface instanceOf InnerTestObject",
                   testObject.mySingularChildByInterface instanceof TestObject.InnerTestObject);
        assertEquals("testObject.mySingularChildByInterface.string", "an object",
                     ((TestObject.InnerTestObject) (testObject.mySingularChildByInterface)).string);

        assertEquals("testObject.myInnerObject", new TestObject.InnerTestObject("an InnerTestObject"),
                     testObject.myInnerObject);

        List<SimpleTestObject> list = testObject.myList;
        assertNotNull("testObject.myList", list);
        assertEquals("testObject.myList.size()", 2, list.size());
        assertEquals("testObject.myList[0].myString", "post-parse:list child 0", list.get(0).myString);
        assertEquals("testObject.myList[1].myString", "post-parse:list child 1", list.get(1).myString);

        assertNotNull("testObject.myListByInterface", testObject.myListByInterface);
        assertEquals("testObject.myListByInterface", 2, testObject.myListByInterface.size());
        assertTrue("testObject.myListByInterface[0] instanceOf InnerTestObject",
                   testObject.myListByInterface.get(0) instanceof TestObject.InnerTestObject);
        assertEquals("testObject.myListByInterface[0]", "object 0",
                     ((TestObject.InnerTestObject) (testObject.myListByInterface.get(0))).string);
        assertTrue("testObject.myListByInterface[1] instanceOf InnerTestObject",
                   testObject.myListByInterface.get(1) instanceof TestObject.InnerTestObject);
        assertEquals("testObject.myListByInterface[1]", "object 1",
                     ((TestObject.InnerTestObject) (testObject.myListByInterface.get(1))).string);

        List<List<Integer>> collectionOfCollections = testObject.myCollectionOfCollections;
        assertNotNull("testObject.collectionOfCollections", collectionOfCollections);
        assertEquals("testObject.collectionOfCollections.size()", 2, collectionOfCollections.size());
        assertEquals("testObject.collectionOfCollection[0]", CollectionUtils.newArrayList(1, 2),
                     collectionOfCollections.get(0));
        assertEquals("testObject.collectionOfCollection[1]", CollectionUtils.newArrayList(3, 4),
                     collectionOfCollections.get(1));

        List<Set<SimpleTestObject>> collectionOfCollectionUtilsOfTestObjects = testObject.mySetsOfTestObjects;
        assertNotNull("testObject.myCollectionUtilsOfTestObjects", collectionOfCollectionUtilsOfTestObjects);
        assertEquals("testObject.myCollectionUtilsOfTestObjects[0][0]",
                     CollectionUtils.newHashSet(new SimpleTestObject("post-parse:set 0 child 0", "simpleTestObject"),
                                                new SimpleTestObject("post-parse:set 0 child 1", "simpleTestObject")),
                     collectionOfCollectionUtilsOfTestObjects.get(0));
        assertEquals("testObject.myCollectionUtilsOfTestObjects[0][0]",
                     CollectionUtils.newHashSet(new SimpleTestObject("post-parse:set 1 child 0", "simpleTestObject"),
                                                new SimpleTestObject("post-parse:set 1 child 1", "simpleTestObject")),
                     collectionOfCollectionUtilsOfTestObjects.get(1));

        assertEquals("testObject.myUnannotatedObject", new UnannotatedObject("singular unannotated object"),
                     testObject.myUnannotatedObject);

        assertEquals("testObject.myUnannotatedObjectCollection",
                     CollectionUtils.newArrayList(new UnannotatedObject("unannotated item 0"),
                                                  new UnannotatedObject("unannotated item 1")),
                     testObject.myUnannotatedObjectCollection);

        // JSON Natives
        assertNotNull("testObject.myJsonObject", testObject.myJsonObject);
        assertEquals("testObject.myJsonObject.getString(\"name\")", "value", testObject.myJsonObject.getString("name"));

        assertNotNull("testObject.myJsonArray", testObject.myJsonArray);
        assertEquals("testObject.myJsonArray.length()", 2, testObject.myJsonArray.length());
        assertEquals("testObject.myJsonArray[0].(\"name 0\")", "value 0",
                     ((JSONObject) testObject.myJsonArray.get(0)).getString("name 0"));
        assertEquals("testObject.myJsonArray[1].(\"name 1\")", "value 1",
                     ((JSONObject) testObject.myJsonArray.get(1)).getString("name 1"));

        assertNotNull("testObject.myJsonObjectCollection", testObject.myJsonObjectCollection);
        assertEquals("testObject.myJsonObjectCollection.size()", 2, testObject.myJsonObjectCollection.size());
        assertEquals("testObject.myJsonObjectCollection[0].(\"list name 0\")", "list value 0",
                     testObject.myJsonObjectCollection.get(0).getString("list name 0"));
        assertEquals("testObject.myJsonObjectCollection[1].(\"list name 1\")", "list value 1",
                     testObject.myJsonObjectCollection.get(1).getString("list name 1"));

        // Setters
        assertEquals("testObject.stringFromSetter", "string for setter", testObject.stringFromSetter);
        assertEquals("testObject.unannotatedObjectFromSetter", new UnannotatedObject("unannotated object for setter"),
                     testObject.unannotatedObjectFromSetter);
        assertEquals("testObject.testObjectCollectionFromSetter",
                     CollectionUtils.newArrayList(new ParserAnnotatedObject("object for list setter 0", null),
                                                  new ParserAnnotatedObject("object for list setter 1", null)),
                     testObject.testObjectCollectionFromSetter);

        assertNotNull("testObject.integerCollectionsFromSetter", testObject.integerCollectionsFromSetter);
        assertEquals("testObject.integerCollectionsFromSetter.size()", 2,
                     testObject.integerCollectionsFromSetter.size());
        assertEquals("testObject.integerCollectionsFromSetter.get(0)", CollectionUtils.newHashSet(1, 2),
                     testObject.integerCollectionsFromSetter.get(0));
        assertEquals("testObject.integerCollectionsFromSetter.get(1)", CollectionUtils.newHashSet(3, 4),
                     testObject.integerCollectionsFromSetter.get(1));

        // Empty Objects
        assertEquals("testObject.myEmptyObject", new SimpleTestObject(), testObject.myEmptyObject);
        assertNotNull("testObject.myEmptyCollection", testObject.myEmptyCollection);
        assertTrue("testObject.myEmptyCollection.isEmpty()", testObject.myEmptyCollection.isEmpty());

        // Nulls
        assertEquals("testObject.myNullInt", 1, testObject.myNullInt);
        assertNull("testObject.myNullString", testObject.myNullString);
        assertNull("testObject.myNullTestObject", testObject.myNullTestObject);
        assertNull("testObject.myNullCollection", testObject.myNullCollection);
        assertEquals("testObject.myDefaultCollection", Collections.singleton("the one"),
                     testObject.myDefaultCollection);

        assertEquals("testObject.myCollectionWithSingleNullValue", CollectionUtils.newArrayList((String) null), testObject.myCollectionWithSingleNullValue);
        assertEquals("testObject.myCollectionWithNullValues",
                     CollectionUtils.newArrayList(null, "string", null, "null"), testObject.myCollectionWithNullValues);

        assertNotNull("testObject.myStringMapWithSingleNullValue", testObject.myStringMapWithSingleNullValue);
        assertEquals("testObject.myStringMapWithSingleNullValue.size", 1, testObject.myStringMapWithSingleNullValue.size());
        assertEquals("testObject.myStringMapWithSingleNullValue[key1]", null, testObject.myStringMapWithSingleNullValue.get("key1"));

        assertNotNull("testObject.myStringMapWithNullValues", testObject.myStringMapWithNullValues);
        assertEquals("testObject.myStringMapWithNullValues.size", 3, testObject.myStringMapWithNullValues.size());
        assertEquals("testObject.myStringMapWithNullValues[key1]", null, testObject.myStringMapWithNullValues.get("key1"));
        assertEquals("testObject.myStringMapWithNullValues[key2]", "value2", testObject.myStringMapWithNullValues.get("key2"));
        assertEquals("testObject.myStringMapWithNullValues[key3]", "null", testObject.myStringMapWithNullValues.get("key3"));

        assertNotNull("testObject.myObjectMapWithNullValues", testObject.myObjectMapWithNullValues);
        assertEquals("testObject.myObjectMapWithNullValues.size", 3, testObject.myObjectMapWithNullValues.size());
        assertEquals("testObject.myObjectMapWithNullValues[key1]", new SimpleTestObject(null), testObject.myObjectMapWithNullValues.get("key1"));
        assertEquals("testObject.myObjectMapWithNullValues[key2]", new SimpleTestObject("post-parse:value2"), testObject.myObjectMapWithNullValues.get("key2"));
        assertEquals("testObject.myObjectMapWithNullValues[key3]", new SimpleTestObject("post-parse:null"), testObject.myObjectMapWithNullValues.get("key3"));
    }

    @Test
    public void testAssignmentByTypeNoDiscriminationValue() throws Exception {
        TestObject testObject = (TestObject) parser.parseJsonStream(getInputStream("no-discrimination-value.json"));
        assertNotNull("testObject", testObject);
        assertNotNull("testObject.myList", testObject.myList);
        assertEquals("testObject.myList.size()", 2, testObject.myList.size());
        assertEquals("testObject.myList[0]", new SimpleTestObject("post-parse:first"), testObject.myList.get(0));
        assertEquals("testObject.myList[1]", new SimpleTestObject("post-parse:second"), testObject.myList.get(1));
        assertEquals("testObject.mySingularChild", new SimpleTestObject("post-parse:a singular child"),
                     testObject.mySingularChild);
    }

    @Test
    public void testParserAnnotatedObject() throws Exception {
        ParserAnnotatedObject object = (ParserAnnotatedObject) parser.parseJsonStream(
                getInputStream("parser-annotated-object.json"));
        assertEquals(new ParserAnnotatedObject("value", "parserAnnotatedObject"), object);
    }

    @Test
    public void testAlternateNames() throws Exception {
        TestObject testObject = (TestObject) parser.parseJsonStream(getInputStream("alternate-name.json"));
        assertNotNull("testObject", testObject);
        assertEquals("testObject.myString", "a string", testObject.myString);
        assertEquals("testObject.discriminationValue", "testObject2", testObject.discriminationValue);
        assertEquals("testObject.superDiscriminationValue", "testObject2", testObject.superDiscriminationValue);
    }

    @Test
    public void testUnknown() throws Exception {
        JsonStreamParser parser = JsonStreamParserFactory.newJsonStreamParser(
                new JsonParserSettingsBuilder().withDiscriminationName("object")
                                               .withUnknownObjectClass(TestObject.class)
                                               .build());
        TestObject actual = (TestObject) parser.parseJsonStream(getInputStream("unknown-object.json"));
        assertEquals(new TestObject("the string value"), actual);
    }

    @Test
    public void testUnknowns() throws Exception {
        testWithUnknowns("unknowns.json");
    }

    @Test
    public void testUnknownsDelayed() throws Exception {
        testWithUnknowns("unknowns-delayed.json");
    }

    private void testWithUnknowns(String fileName) throws Exception {
        JsonStreamParser parser = JsonStreamParserFactory.newJsonStreamParser(
                new JsonParserSettingsBuilder().withDiscriminationName("object")
                                               .withUnknownObjectClass(TestObject.InnerTestObject.class)
                                               .build());

        TestObject actual = (TestObject) parser.parseJsonStream(getInputStream(fileName));
        assertNotNull("myListByInterface", actual.myListByInterface);
        assertEquals("myListByInterface.size", 2, actual.myListByInterface.size());
        assertEquals("myListByInterface[0]", new TestObject.InnerTestObject("object 0"),
                     actual.myListByInterface.get(0));
        assertEquals("myListByInterface[1]", new TestObject.InnerTestObject("object 1"),
                     actual.myListByInterface.get(1));
        assertEquals("mySingularChildByInterface", new TestObject.InnerTestObject("a singular unknown"),
                     actual.mySingularChildByInterface);

        assertNotNull("myInterfaceMap", actual.myInterfaceMap);
        assertEquals("myInterfaceMap.size", 2, actual.myInterfaceMap.size());
        assertEquals("myInterfaceMap[key1]", new TestObject.InnerTestObject("string 1"),
                     actual.myInterfaceMap.get("key1"));
        assertEquals("myInterfaceMap[key2]", new TestObject.InnerTestObject("string 2"),
                     actual.myInterfaceMap.get("key2"));

    }

    @Test
    public void testWrongTypeThrowsException() throws Exception {
        String expectedMessage = "Expected value of \"mySingularChild\" to be one of \"[BEGIN_OBJECT]\" but found "
                + "\"STRING\".";
        testExceptionThrownOnTestObject("wrong-type.json", expectedMessage);
    }

    @Test
    public void testWrongConversionThrowsException() throws Exception {
        String expectedMessage = "Could not convert value at \"mySingularChildByInterface\" to "
                + "com.workday.autoparse.json.demo.TestObjectInterface from org.json.JSONObject.";
        testExceptionThrownOnTestObject("wrong-conversion.json", expectedMessage);
    }

    @Test
    public void testWrongConversionDelayedThrowsException() throws Exception {
        String expectedMessage = "Could not convert value at \"mySingularChildByInterface\" to "
                + "com.workday.autoparse.json.demo.TestObjectInterface from org.json.JSONObject.";
        testExceptionThrownOnTestObject("wrong-conversion-delayed.json", expectedMessage);
    }

    @Test
    public void testWrongConversionInArrayThrowsException() throws Exception {
        String expectedMessage = "Could not convert value in array at \"myListByInterface\" to com.workday"
                + ".autoparse.json.demo" + ".TestObjectInterface from org.json.JSONObject.";
        testExceptionThrownOnTestObject("wrong-conversion-array.json", expectedMessage);
    }

    @Test
    public void testWrongConversionInArrayDelayedThrowsException() throws Exception {
        String expectedMessage = "Could not convert value in array at \"myListByInterface\" to com.workday"
                + ".autoparse.json.demo" + ".TestObjectInterface from org.json.JSONObject.";
        testExceptionThrownOnTestObject("wrong-conversion-array-delayed.json", expectedMessage);
    }

    @Test
    public void testWrongConversionInMapThrowsException() throws Exception {
        String expectedMessage = "Could not convert value at \"key2\" in \"myInterfaceMap\" to com.workday"
                + ".autoparse.json.demo" + ".TestObjectInterface from java.lang.String.";
        testExceptionThrownOnTestObject("wrong-conversion-map.json", expectedMessage);
    }

    @Test
    public void testWrongConversionInMapDelayedThrowsException() throws Exception {
        String expectedMessage =
                "Could not convert value at \"key2\" in \"myInterfaceMap\" to com.workday.autoparse.json.demo"
                        + ".TestObjectInterface from java.lang.String.";
        testExceptionThrownOnTestObject("wrong-conversion-map-delayed.json", expectedMessage);
    }

    private void testExceptionThrownOnTestObject(String fileName, String expectedMessage) throws Exception {
        boolean exceptionCaught = false;
        try {
            parser.parseJsonStream(getInputStream(fileName));
        } catch (RuntimeException e) {
            exceptionCaught = true;
            assertEquals(expectedMessage, e.getMessage());
        }
        assertTrue("Expected an IllegalStateException", exceptionCaught);
    }

    @Test
    public void testChildrenOfJsonObjectDoNotGetConverted() throws Exception {
        JSONObject jsonObject = (JSONObject) parser.parseJsonStream(getInputStream("json-object.json"));
        Object testObject = jsonObject.get("testObject");
        assertNotNull("testObject", testObject);
        assertTrue("expected testObject to be JSONObject but found " + testObject.getClass().getCanonicalName(),
                   testObject instanceof JSONObject);
    }

    private InputStream getInputStream(String fileName) {
        return new BufferedInputStream(JsonParserTest.class.getResourceAsStream(fileName));
    }

}
