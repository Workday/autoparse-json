/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.utils.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author nathan.taylor
 * @since 2015-09-03.
 */
@RunWith(JUnit4.class)
public class GetFieldTest {

    private TestObject testObject;

    @Before
    public void setUp() {
        testObject = new TestObject();
    }

    @Test
    public void testGetNonCollectionField() {
        testObject.myString = "Picard";
        Object actual = TestObject$$JsonObjectParser.INSTANCE.initializeAndGetField(testObject, "myString");
        assertEquals("Picard", actual);
    }

    @Test
    public void testGetFieldFromSetter() {
        testObject.stringFromSetter = "Riker";
        Object actual = TestObject$$JsonObjectParser.INSTANCE.initializeAndGetField(testObject, "stringSetter");
        assertNull(actual);
    }

    @Test
    public void testGetAndInitializeCollectionField() {
        Object actual = TestObject$$JsonObjectParser.INSTANCE.initializeAndGetField(testObject, "myStringCollection");
        assertTrue(actual instanceof ArrayList);
    }

    @Test
    public void testGetAndDoNotInitializeCollectionField() {
        assertNull(TestObject$$JsonObjectParser.INSTANCE.getField(testObject, "myStringCollection"));
    }

    @Test
    public void testGetCollectionField() {
        testObject.myStringCollection = CollectionUtils.newArrayList("Data", "La Forge");
        Object actual = TestObject$$JsonObjectParser.INSTANCE.initializeAndGetField(testObject, "myStringCollection");
        assertEquals(CollectionUtils.newArrayList("Data", "La Forge"), actual);
    }

    @Test
    public void testGetUnknownField() {
        assertNull(TestObject$$JsonObjectParser.INSTANCE.initializeAndGetField(testObject, "I don't exists."));
    }

}
