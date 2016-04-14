/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import com.workday.autoparse.json.annotations.JsonObject;

import java.io.InputStream;

/**
 * An object that takes a JSON input stream and parses it into an object as determined by the {@link
 * JsonObject} annotations.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public interface JsonStreamParser {

    Object parseJsonStream(InputStream in) throws Exception;
}
