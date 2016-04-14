/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.context.JsonParserContext;
import com.workday.autoparse.json.context.JsonParserSettings;
import com.workday.autoparse.json.context.JsonParserSettingsBuilder;
import com.workday.autoparse.json.parser.JsonStreamParser;
import com.workday.autoparse.json.parser.JsonStreamParserFactory;

import java.io.BufferedInputStream;
import java.util.Map;

/**
 * @author nathan.taylor
 * @since 2015-10-20.
 */
public class InstanceUpdaterTestUtils {

    private static final JsonParserSettings SETTINGS =
            new JsonParserSettingsBuilder().withDiscriminationName("object")
                                           .build();
    private static final JsonStreamParser PARSER =
            JsonStreamParserFactory.newJsonStreamParser(SETTINGS);

    public static final JsonParserContext CONTEXT = new JsonParserContext(SETTINGS);

    public static Map<String, Object> getUpdateMapFromFile(String fileName) throws Exception {
        UpdateObject updateObject = (UpdateObject) PARSER.parseJsonStream(
                new BufferedInputStream(InstanceUpdaterTest.class.getResourceAsStream(fileName)));
        return updateObject.values;
    }
}
