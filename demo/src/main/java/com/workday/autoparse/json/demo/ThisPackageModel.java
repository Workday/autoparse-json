/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonValue;
import com.workday.autoparse.json.demo.other.OtherPackageModel;

/**
 * @author travis.westbrook
 * @since 2016-01-06.
 */
@JsonObject("thisPackageModel")
public class ThisPackageModel {

    @JsonValue("otherPackageModel")
    OtherPackageModel otherPackageModel;
}
