/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonSelfValues;
import com.workday.autoparse.json.annotations.JsonValue;

import java.util.Map;

/**
 * @author nathan.taylor
 * @since 2015-04-14.
 */
@JsonObject("selfMapUnconverted")
public class SelfMapObjectUnconverted {

    @JsonValue("string")
    public String string;

    @JsonSelfValues(convertJsonTypes = false)
    public Map<String, Object> selfValuesUnconverted;
}
