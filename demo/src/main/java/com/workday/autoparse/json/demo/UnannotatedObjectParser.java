/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import android.util.JsonReader;

import com.workday.autoparse.json.parser.JsonObjectParser;

import org.json.JSONObject;

import java.io.IOException;

public final class UnannotatedObjectParser implements JsonObjectParser<UnannotatedObject> {

    public static final UnannotatedObjectParser INSTANCE = new UnannotatedObjectParser();

    @Override
    public UnannotatedObject parseJsonObject(JSONObject jsonObject,
                                             JsonReader reader,
                                             String discriminationName,
                                             String discriminationValue) throws IOException {
        UnannotatedObject result = new UnannotatedObject();
        if (jsonObject != null) {
            parsefromJsonObject(result, jsonObject);
        }
        if (reader != null) {
            parseFromReader(result, reader);
        }
        return result;
    }

    private void parsefromJsonObject(UnannotatedObject out, JSONObject jsonObject)
            throws IOException {
        try {
            if (jsonObject.has("string")) {
                out.string = jsonObject.getString("string");
            }
        } catch (org.json.JSONException e) {
            throw new RuntimeException("This should be impossible.", e);
        }
    }

    private void parseFromReader(UnannotatedObject out, JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "string": {
                    out.string = reader.nextString();
                    break;
                }
                default: {
                    reader.skipValue();
                }
            }
        }
    }
}
