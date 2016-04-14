/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import android.util.JsonReader;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonValue;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Internal class used by Autoparse. This is the default parser for {@link JsonObject#parser()} and
 * {@link JsonValue#parser()}, meaning that for this value, Autoparse will generate a {@link
 * JsonObjectParser}.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public class NoJsonObjectParser implements JsonObjectParser<Object> {

    @Override
    public Object parseJsonObject(JSONObject jsonObject,
                                  JsonReader reader,
                                  String discriminationName,
                                  String discriminationValue)
            throws IOException {
        return null;
    }

}
