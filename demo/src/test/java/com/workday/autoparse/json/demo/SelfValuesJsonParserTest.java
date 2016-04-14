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

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author nathan.taylor
 * @since 2015-04-14.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SelfValuesJsonParserTest {

    private JsonStreamParser parser;

    @Before
    public void setUp() {
        parser = JsonStreamParserFactory.newJsonStreamParser(
                new JsonParserSettingsBuilder().withDiscriminationName("object").build());
    }

    @Test
    public void testParseSelfValuesObject() throws Exception {
        testParse("self-values.json");
    }

    @Test
    public void testParseSelfValuesObjectDelayed() throws Exception {
        testParse("self-values-delayed.json");
    }

    private void testParse(String fileName) throws Exception {
        SelfMapObject object = (SelfMapObject) parser.parseJsonStream(getInputStream(fileName));
        assertEquals("object.string", "a string value", object.string);
        assertNotNull("object.selfValues", object.selfValues);
        assertEquals("object.selfValues.size", 4, object.selfValues.size());
        assertEquals("object.selfValues.string2",
                     "another string",
                     object.selfValues.get("string2"));
        assertEquals("object.selfValues.int", "5", object.selfValues.get("int"));
        assertEquals("object.selfValues.simpleTestObject",
                     new SimpleTestObject("simple string", "simpleTestObject"),
                     object.selfValues.get("simpleTestObject"));
        JsonTestUtils.assertJSONObjectsEqual("object.selfValues.jsonObject",
                                             new JSONObject("{\"name\":\"bob\"}"),
                                             object.selfValues.get("jsonObject"));
    }

    @Test
    public void testParseSelfValuesObjectUnconverted() throws Exception {
        testParseUnconverted("self-values-unconverted.json");
    }

    @Test
    public void testParseSelfValuesObjectUnconvertedDelayed() throws Exception {
        testParseUnconverted("self-values-unconverted-delayed.json");
    }

    private void testParseUnconverted(String fileName) throws Exception {
        SelfMapObjectUnconverted object =
                (SelfMapObjectUnconverted) parser.parseJsonStream(getInputStream(fileName));
        assertEquals("object.string", "a string value", object.string);
        assertNotNull("object.selfValuesUnconverted", object.selfValuesUnconverted);
        assertEquals("object.selfValuesUnconverted.size", 4, object.selfValuesUnconverted.size());
        assertEquals("object.selfValuesUnconverted.string2", "another string",
                     object.selfValuesUnconverted.get("string2"));
        assertEquals("object.selfValuesUnconverted.int",
                     "5",
                     object.selfValuesUnconverted.get("int"));

        JSONObject expected =
                new JSONObject("{\"object\":\"simpleTestObject\",\"myString\":\"simple string\"}");
        JsonTestUtils.assertJSONObjectsEqual("object.selfValuesUnconverted.simpleTestObject",
                                             expected,
                                             object.selfValuesUnconverted.get("simpleTestObject"));
        JsonTestUtils.assertJSONObjectsEqual("object.selfValues.jsonObject",
                                             new JSONObject("{\"name\":\"bob\"}"),
                                             object.selfValuesUnconverted.get("jsonObject"));
    }

    private InputStream getInputStream(String fileName) {
        return new BufferedInputStream(SelfValuesJsonParserTest.class.getResourceAsStream(fileName));
    }
}
