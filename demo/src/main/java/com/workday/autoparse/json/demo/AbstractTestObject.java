/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.DiscrimValue;
import com.workday.autoparse.json.annotations.JsonValue;

/**
 * @author nathan.taylor
 * @since 2014-11-04.
 */
public abstract class AbstractTestObject {

    @JsonValue("overriddenThing")
    public String superOverriddenThing;

    public String superDiscriminationValue;

    @DiscrimValue
    final void setSuperDiscriminationValue(String superDiscriminationValue) {
        this.superDiscriminationValue = superDiscriminationValue;
    }
}
