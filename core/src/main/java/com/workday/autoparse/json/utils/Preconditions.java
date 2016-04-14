/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.utils;

/**
 * @author nathan.taylor
 * @since 2016-04-07.
 */
public class Preconditions {

    private Preconditions() {
    }

    public static void checkArgument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkNotNull(Object reference, String errorMessage) {
        if (reference == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
