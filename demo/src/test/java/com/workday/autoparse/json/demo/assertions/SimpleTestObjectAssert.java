/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo.assertions;

import com.workday.autoparse.json.demo.SimpleTestObject;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.util.Objects;

/**
 * @author nathan.taylor
 * @since 2015-10-20.
 */
public class SimpleTestObjectAssert
        extends AbstractObjectAssert<SimpleTestObjectAssert, SimpleTestObject> {

    protected SimpleTestObjectAssert(SimpleTestObject actual) {
        super(actual, SimpleTestObjectAssert.class);
    }

    public static SimpleTestObjectAssert assertThat(SimpleTestObject simpleTestObject) {
        return new SimpleTestObjectAssert(simpleTestObject);
    }

    public SimpleTestObjectAssert hasString(String myString) {
        isNotNull();

        if (!Objects.areEqual(actual.myString, myString)) {
            failWithMessage("Expected myString to be <%s> but was <%s>", myString, actual.myString);
        }

        return this;
    }

    public SimpleTestObjectAssert hasDiscrimValue(String discrimValue) {
        isNotNull();

        if (!Objects.areEqual(actual.discrimValue, discrimValue)) {
            failWithMessage("Expected discrimValue to be <%s> but was <%s>",
                            discrimValue,
                            actual.discrimValue);
        }

        return this;
    }
}
