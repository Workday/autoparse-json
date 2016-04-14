/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.DiscrimValue;
import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonValue;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nathan.taylor
 * @since 2015-02-27
 */
@JsonObject("simpleTestObject")
public class SimpleTestObject {

    @JsonValue("myString")
    public String myString;

    @DiscrimValue
    public String discrimValue;

    public SimpleTestObject() {
    }

    public SimpleTestObject(String myString) {
        this.myString = myString;
    }

    public SimpleTestObject(String myString, String discrimValue) {
        this.myString = myString;
        this.discrimValue = discrimValue;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
