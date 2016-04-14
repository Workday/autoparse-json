/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

/**
 * Converts a {@link String} to another type.
 *
 * @author nathan.taylor
 * @since 2014-10-13.
 */
interface Converter<T> {

    public T convert(String value);
}
