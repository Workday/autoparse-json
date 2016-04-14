/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import junit.framework.Assert;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * @author nathan.taylor
 * @since 2015-04-15.
 */
public class JsonTestUtils {

    static void assertJSONObjectsEqual(String message, JSONObject expected, Object actual) {
        if (expected == null) {
            Assert.assertNull(message + " is null", actual);
            return;
        }

        Assert.assertNotNull(message + " not null", actual);
        Assert.assertTrue(message + " expected JSONObject but found " + actual.getClass()
                                                                              .getCanonicalName(),
                          actual instanceof JSONObject);

        JSONObject actualJsonObject = (JSONObject) actual;
        Assert.assertEquals(message + ": size", expected.length(), actualJsonObject.length());

        @SuppressWarnings("unchecked")
        Iterator<String> keys = expected.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Assert.assertEquals(message + ": " + key, expected.opt(key), actualJsonObject.opt(key));
        }
    }
}
