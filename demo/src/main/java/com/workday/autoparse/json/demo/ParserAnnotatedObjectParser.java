/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import android.util.JsonReader;

import com.workday.autoparse.json.context.ContextHolder;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.JsonParserUtils;

import org.json.JSONObject;

import java.io.IOException;

public final class ParserAnnotatedObjectParser implements JsonObjectParser<ParserAnnotatedObject> {

    public static final ParserAnnotatedObjectParser INSTANCE = new ParserAnnotatedObjectParser();

    @Override
    public ParserAnnotatedObject parseJsonObject(JSONObject jsonObject,
                                                 JsonReader reader,
                                                 String discriminationName,
                                                 String discriminationValue)
            throws IOException {
        ParserAnnotatedObject object = new ParserAnnotatedObject();
        object.discriminationValue = discriminationValue;
        if (jsonObject != null) {
            parseFromJsonObject(object, jsonObject);
        }
        if (reader != null) {
            parseFromReader(object, reader);
        }
        return object;
    }

    private void parseFromJsonObject(ParserAnnotatedObject out, JSONObject jsonObject)
            throws IOException {
        if (jsonObject.has("string")) {
            out.string = jsonObject.optString("string");
        }

        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();
        if (jsonObject.has(discriminationName)) {
            out.discriminationValue = jsonObject.optString(discriminationName);
        }
    }

    private void parseFromReader(ParserAnnotatedObject out, JsonReader reader) throws IOException {
        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (discriminationName.equals(name)) {
                out.discriminationValue = JsonParserUtils.nextString(reader, discriminationName);
                continue;
            }

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
