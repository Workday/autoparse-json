/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author nathan.taylor
 * @since 2014-11-07.
 */
@RunWith(MockitoJUnitRunner.class)
public class MemberInfoComparitorTest {

    @Mock
    Element e1;
    @Mock
    Element e2;
    @Mock
    Name name1;
    @Mock
    Name name2;

    @Before
    public void setUp() {
        when(e1.getSimpleName()).thenReturn(name1);
        when(e2.getSimpleName()).thenReturn(name2);
    }

    @Test
    public void testEqualMembers() {

        when(name1.toString()).thenReturn("field");
        when(name2.toString()).thenReturn("field");

        when(e1.getKind()).thenReturn(ElementKind.FIELD);
        when(e2.getKind()).thenReturn(ElementKind.FIELD);

        JsonObjectParserGenerator.MemberInfo m1 =
                new JsonObjectParserGenerator.MemberInfo(e1, 1, false);
        JsonObjectParserGenerator.MemberInfo m2 =
                new JsonObjectParserGenerator.MemberInfo(e2, 1, false);

        assertEquals(0, JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m1, m2));
    }

    @Test
    public void testUnequalHierarchyEqualNames() {
        when(name1.toString()).thenReturn("field");
        when(name2.toString()).thenReturn("field");

        when(e1.getKind()).thenReturn(ElementKind.FIELD);
        when(e2.getKind()).thenReturn(ElementKind.FIELD);

        JsonObjectParserGenerator.MemberInfo m1 =
                new JsonObjectParserGenerator.MemberInfo(e1, 1, false);
        JsonObjectParserGenerator.MemberInfo m2 =
                new JsonObjectParserGenerator.MemberInfo(e2, 2, false);

        int expected = -1 * JsonObjectParserGenerator.MemberInfo.HIERARCHY_MULTIPLIER;
        int actual = JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m1, m2);
        assertEquals(expected, actual);
        assertTrue(actual < 0);

        expected = JsonObjectParserGenerator.MemberInfo.HIERARCHY_MULTIPLIER;
        actual = JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m2, m1);
        assertEquals(expected, actual);
        assertTrue(actual > 0);

        m1 = new JsonObjectParserGenerator.MemberInfo(e1, 3, false);
        m2 = new JsonObjectParserGenerator.MemberInfo(e2, 1, false);

        expected = 2 * JsonObjectParserGenerator.MemberInfo.HIERARCHY_MULTIPLIER;
        actual = JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m1, m2);
        assertEquals(expected, actual);
        assertTrue(actual > 0);

        expected = -2 * JsonObjectParserGenerator.MemberInfo.HIERARCHY_MULTIPLIER;
        actual = JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m2, m1);
        assertEquals(expected, actual);
        assertTrue(actual < 0);
    }

    @Test
    public void testEqualHierarchyEqualNamesUnequalKind() {
        when(name1.toString()).thenReturn("field");
        when(name2.toString()).thenReturn("field");

        when(e1.getKind()).thenReturn(ElementKind.FIELD);
        when(e2.getKind()).thenReturn(ElementKind.METHOD);

        JsonObjectParserGenerator.MemberInfo m1 =
                new JsonObjectParserGenerator.MemberInfo(e1, 1, false);
        JsonObjectParserGenerator.MemberInfo m2 =
                new JsonObjectParserGenerator.MemberInfo(e2, 1, false);

        int kindDiff = ElementKind.FIELD.compareTo(ElementKind.METHOD);

        int expected = kindDiff * JsonObjectParserGenerator.MemberInfo.KIND_MULTIPLIER;
        int actual = JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m1, m2);
        assertEquals(expected, actual);
        assertTrue(actual != 0);

        expected = -kindDiff * JsonObjectParserGenerator.MemberInfo.KIND_MULTIPLIER;
        actual = JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m2, m1);
        assertEquals(expected, actual);
        assertTrue(actual != 0);
    }

    @Test
    public void testEqualHierarchyEqualKindUnequalNames() {
        when(name1.toString()).thenReturn("field");
        when(name2.toString()).thenReturn("field2");

        when(e1.getKind()).thenReturn(ElementKind.FIELD);
        when(e2.getKind()).thenReturn(ElementKind.FIELD);

        JsonObjectParserGenerator.MemberInfo m1 =
                new JsonObjectParserGenerator.MemberInfo(e1, 1, false);
        JsonObjectParserGenerator.MemberInfo m2 =
                new JsonObjectParserGenerator.MemberInfo(e2, 1, false);

        int diff = "field".compareTo("field2");
        assertEquals(diff, JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m1, m2));
        assertEquals(-diff, JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m2, m1));
        assertTrue(diff != 0);
    }

    @Test
    public void testAllUnequal() {
        when(name1.toString()).thenReturn("method");
        when(name2.toString()).thenReturn("field");

        when(e1.getKind()).thenReturn(ElementKind.METHOD);
        when(e2.getKind()).thenReturn(ElementKind.FIELD);

        JsonObjectParserGenerator.MemberInfo m1 =
                new JsonObjectParserGenerator.MemberInfo(e1, 1, false);
        JsonObjectParserGenerator.MemberInfo m2 =
                new JsonObjectParserGenerator.MemberInfo(e2, 2, false);

        int m1ToM2 = JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m1, m2);
        int m2ToM1 = JsonObjectParserGenerator.MemberInfo.COMPARATOR.compare(m2, m1);

        assertTrue(m1ToM2 < 0);
        assertEquals(-m1ToM2, m2ToM1);
    }
}
