/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.updater;

import java.util.Locale;

/**
 * @author nathan.taylor
 * @since 2015-08-13.
 */
public class WrongTypeException extends RuntimeException {

    private static final long serialVersionUID = -5591922306586144783L;

    public WrongTypeException(String message) {
        super(message);
    }

    public WrongTypeException(String key, Object expectedType, Object actual) {
        super(String.format(Locale.US,
                            "Expected object mapped to \"%s\" to be of type %s but found %s.",
                            key,
                            expectedType,
                            actual));
    }
}
