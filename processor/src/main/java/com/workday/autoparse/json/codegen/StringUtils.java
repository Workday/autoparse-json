/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

/**
 * @author nathan.taylor
 * @since 2016-04-07.
 */
class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(CharSequence cs) {
        if (cs == null) {
            return true;
        }

        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
