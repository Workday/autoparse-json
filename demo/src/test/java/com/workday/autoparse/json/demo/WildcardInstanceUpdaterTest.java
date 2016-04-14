/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.demo.assertions.SimpleTestObjectAssert;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.workday.autoparse.json.demo.InstanceUpdaterTestUtils.CONTEXT;
import static com.workday.autoparse.json.demo.InstanceUpdaterTestUtils.getUpdateMapFromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author nathan.taylor
 * @since 2015-10-19.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class WildcardInstanceUpdaterTest {

    @Test
    public void testStringWildcardConverted() {
        WildcardChildObject object = new WildcardChildObject();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("child", "Gimli");

        WildcardChildObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(object,
                                                                             updates,
                                                                             CONTEXT);

        assertEquals("Gimli", object.child);
    }

    @Test
    public void testStringWildcardUnconverted() {
        WildcardChildObject object = new WildcardChildObject();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("unconvertedChild", "Legolas");

        WildcardChildObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(object,
                                                                             updates,
                                                                             CONTEXT);

        assertEquals("Legolas", object.unconvertedChild);
    }

    @Test
    public void testJsonObjectConverted() throws Exception {
        WildcardChildObject object = new WildcardChildObject();

        Map<String, Object> updates =
                getUpdateMapFromFile("update-wildcard-json-object-converted.json");

        WildcardChildObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(object,
                                                                             updates,
                                                                             CONTEXT);

        assertThat(object.child).isInstanceOf(SimpleTestObject.class);
        SimpleTestObjectAssert.assertThat((SimpleTestObject) object.child)
                              .hasString("Misty Mountains")
                              .hasDiscrimValue("simpleTestObject");
    }

    @Test
    public void testJsonObjectUnconverted() throws Exception {
        WildcardChildObject object = new WildcardChildObject();

        Map<String, Object> updates =
                getUpdateMapFromFile("update-wildcard-json-object-unconverted.json");

        WildcardChildObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(object,
                                                                             updates,
                                                                             CONTEXT);

        assertThat(object.unconvertedChild).isInstanceOf(JSONObject.class);
    }

    @Test
    public void testJsonArrayConverted() throws Exception {
        WildcardChildObject object = new WildcardChildObject();

        Map<String, Object> updates =
                getUpdateMapFromFile("update-wildcard-json-array-converted.json");

        WildcardChildObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(object,
                                                                             updates,
                                                                             CONTEXT);

        assertThat(object.child).isInstanceOf(List.class);
        assertThat((List) object.child).hasSize(2);

        Object first = ((List) object.child).get(0);
        assertThat(first).isInstanceOf(SimpleTestObject.class);
        SimpleTestObjectAssert.assertThat((SimpleTestObject) first).hasString("Rivendell");

        Object second = ((List) object.child).get(1);
        assertThat(second).isInstanceOf(TestObject.class);
        assertEquals("Mirkwood", ((TestObject) second).myString);
    }

    @Test
    public void testJsonArrayUnconverted() throws Exception {
        WildcardChildObject object = new WildcardChildObject();

        Map<String, Object> updates =
                getUpdateMapFromFile("update-wildcard-json-array-unconverted.json");

        WildcardChildObject$$JsonObjectParser.INSTANCE.updateInstanceFromMap(object,
                                                                             updates,
                                                                             CONTEXT);

        assertThat(object.unconvertedChild).isInstanceOf(JSONArray.class);
        JSONArray jsonArray = (JSONArray) object.unconvertedChild;
        assertEquals(2, jsonArray.length());
    }

}
