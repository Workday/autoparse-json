/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonSelfValues;

import java.util.Map;

/**
 * @author nathan.taylor
 * @since 2015-10-13.
 */
@JsonObject("update")
public class UpdateObject {

    @JsonSelfValues(convertJsonTypes = false)
    public Map<String, Object> values;
}
