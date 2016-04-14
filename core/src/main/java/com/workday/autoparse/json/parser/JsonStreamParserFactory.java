/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import com.workday.autoparse.json.context.JsonParserContext;
import com.workday.autoparse.json.context.JsonParserSettings;

/**
 * The standard way to create new instances of {@link JsonStreamParser}.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public class JsonStreamParserFactory {

    public static JsonStreamParser newJsonStreamParser(JsonParserSettings settings) {
        return new StandardJsonStreamParser(new JsonParserContext(settings));
    }

    private JsonStreamParserFactory() {
    }
}
