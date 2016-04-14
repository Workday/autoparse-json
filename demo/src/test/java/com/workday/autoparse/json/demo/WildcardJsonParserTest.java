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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author nathan.taylor
 * @since 2015-02-26
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class WildcardJsonParserTest {

    JsonStreamParser parser;

    @Before
    public void setUp() {
        parser = JsonStreamParserFactory.newJsonStreamParser(
                new JsonParserSettingsBuilder().withDiscriminationName("object").build());
    }

    @Test
    public void testStringWildcard() throws Exception {
        testStringWildcard("wildcard-child-string.json");
    }

    @Test
    public void testStringWildcardDelayed() throws Exception {
        testStringWildcard("wildcard-child-string-delayed.json");
    }

    private void testStringWildcard(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        assertEquals("a string", object.child);
    }

    @Test
    public void testStringWildcardUnconverted() throws Exception {
        testStringWildcardUnconverted("wildcard-child-string-unconverted.json");
    }

    @Test
    public void testStringWildcardUnconvertedDelayed() throws Exception {
        testStringWildcardUnconverted("wildcard-child-string-unconverted-delayed.json");
    }

    private void testStringWildcardUnconverted(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        assertEquals("a string", object.unconvertedChild);
    }

    @Test
    public void testNumberWildcard() throws Exception {
        testNumberWildcard("wildcard-child-number.json");
    }

    @Test
    public void testNumberWildcardDelayed() throws Exception {
        testNumberWildcard("wildcard-child-number-delayed.json");
    }

    private void testNumberWildcard(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        assertEquals("123", object.child);
    }

    @Test
    public void testBooleanWildcard() throws Exception {
        testBooleanWildcard("wildcard-child-boolean.json");
    }

    @Test
    public void testBooleanWildcardDelayed() throws Exception {
        testBooleanWildcard("wildcard-child-boolean-delayed.json");
    }

    private void testBooleanWildcard(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        assertEquals(true, object.child);
    }

    @Test
    public void testObjectWildcard() throws Exception {
        testObjectWildcard("wildcard-child-testobject.json");
    }

    @Test
    public void testObjectWildcardDelayed() throws Exception {
        testObjectWildcard("wildcard-child-testobject-delayed.json");
    }

    private void testObjectWildcard(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        assertEquals(new SimpleTestObject("a test object", "simpleTestObject"), object.child);
    }

    @Test
    public void testObjectWildcardUnconverted() throws Exception {
        testObjectWildcardUnconverted("wildcard-child-testobject-unconverted.json");
    }

    @Test
    public void testObjectWildcardUnconvertedDelayed() throws Exception {
        testObjectWildcardUnconverted("wildcard-child-testobject-unconverted-delayed.json");
    }

    private void testObjectWildcardUnconverted(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        JSONObject expected =
                new JSONObject("{\"object\":\"simpleTestObject\", \"myString\":\"a test object\"}");
        JsonTestUtils.assertJSONObjectsEqual("object.unconvertedChild",
                expected,
                object.unconvertedChild);
    }

    @Test
    public void testArrayWildcard() throws Exception {
        testArrayWildcard("wildcard-child-array.json");
    }

    @Test
    public void testArrayWildcardDelayed() throws Exception {
        testArrayWildcard("wildcard-child-array-delayed.json");
    }

    private void testArrayWildcard(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        List<Object> expected =
                CollectionUtils.<Object>newArrayList(new SimpleTestObject("test object 0",
                                "simpleTestObject"),
                        new SimpleTestObject("test object 1",
                                "simpleTestObject"));
        assertEquals(expected, object.child);
    }

    @Test
    public void testArrayWildcardUnconverted() throws Exception {
        testArrayWildCardUnconverted("wildcard-child-array-unconverted.json");
    }

    @Test
    public void testArrayWildcardUnconvertedDelayed() throws Exception {
        testArrayWildCardUnconverted("wildcard-child-array-unconverted-delayed.json");
    }

    private void testArrayWildCardUnconverted(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        assertTrue(object.unconvertedChild instanceof JSONArray);
        JSONArray unconvertedChild = (JSONArray) object.unconvertedChild;
        assertEquals(2, unconvertedChild.length());
        assertEquals("unconvertedChild[0]", "a string", unconvertedChild.opt(0));

        JSONObject expected =
                new JSONObject("{\"object\":\"simpleTestObject\", \"myString\":\"a test object\"}");
        JsonTestUtils.assertJSONObjectsEqual("unconvertedChild[1]",
                expected,
                unconvertedChild.optJSONObject(1));
    }

    @Test
    public void testComplexArrayWildCard() throws Exception {
        testComplexArrayWildcard("wildcard-child-array-complex.json");
    }

    @Test
    public void testComplexArrayWildCardDelayed() throws Exception {
        testComplexArrayWildcard("wildcard-child-array-complex-delayed.json");
    }

    private void testComplexArrayWildcard(String filename) throws Exception {
        WildcardChildObject object = parseFile(filename);
        assertTrue(object.child instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) object.child;
        assertEquals(6, list.size());
        assertEquals(new SimpleTestObject("test object 0", "simpleTestObject"), list.get(0));
        assertEquals("a string", list.get(1));
        assertEquals("123", list.get(2));
        assertEquals(true, list.get(3));
        assertEquals(CollectionUtils.newArrayList("another string"), list.get(4));
        assertTrue("list[5] instanceof JSONObject", list.get(5) instanceof JSONObject);
        assertEquals("an unknown object", ((JSONObject) list.get(5)).get("string"));
    }

    private WildcardChildObject parseFile(String filename) throws Exception {
        return (WildcardChildObject) parser.parseJsonStream(getInputStream(filename));
    }

    private InputStream getInputStream(String fileName) {
        return new BufferedInputStream(JsonParserTest.class.getResourceAsStream(fileName));
    }
}
