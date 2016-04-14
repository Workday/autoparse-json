/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

/**
 * A names of classes found in the Android SDK which are not available when the {@link
 * AutoparseJsonProcessor} is running.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
final class AndroidNames {

    private AndroidNames() {
    }

    public static final String JSON_ARRAY_FULL = "org.json.JSONArray";
    public static final String JSON_EXCEPTION_FULL = "org.json.JSONException";
    public static final String JSON_OBJECT = "JSONObject";
    public static final String JSON_OBJECT_FULL = "org.json.JSONObject";
    public static final String JSON_READER = "JsonReader";
    public static final String JSON_READER_FULL = "android.util.JsonReader";
}
