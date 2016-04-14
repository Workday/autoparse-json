/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.JsonObject;

/**
 * @author nathan.taylor
 * @since 2015-08-21.
 */
@JsonObject(parser = InstanceUpdaterParserAnnotatedObjectParser.class)
public class InstanceUpdaterParserAnnotatedObject {

}
