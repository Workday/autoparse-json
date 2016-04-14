/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import android.util.JsonReader;

import com.workday.autoparse.json.context.ContextHolder;
import com.workday.autoparse.json.context.JsonParserContext;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author nathan.taylor
 * @since 2014-10-09
 */
class StandardJsonStreamParser implements JsonStreamParser {

    private final JsonParserContext context;

    StandardJsonStreamParser(JsonParserContext context) {
        this.context = context;
    }

    @Override
    public Object parseJsonStream(InputStream in) throws Exception {
        Object result = null;
        JsonReader reader = null;
        JsonParserContext oldContext = null;
        try {
            oldContext = ContextHolder.getContext();
            reader = new JsonReader(new InputStreamReader(in));
            ContextHolder.setContext(context);
            result = JsonParserUtils.parseNextValue(reader);
        } finally {
            ContextHolder.removeContext();
            if (reader != null) {
                reader.close();
            }
            if (oldContext != null) {
                ContextHolder.setContext(oldContext);
            }
        }
        return result;
    }
}
