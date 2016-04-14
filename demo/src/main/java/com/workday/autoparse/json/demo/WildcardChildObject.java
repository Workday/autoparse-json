/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonValue;

/**
 * @author nathan.taylor
 * @since 2015-02-26
 */
@JsonObject("wildcard")
public class WildcardChildObject {

    @JsonValue("child")
    public Object child;

    @JsonValue(value = "unconvertedChild", convertJsonTypes = false)
    public Object unconvertedChild;
}
