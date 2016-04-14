/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import android.util.JsonReader;

import com.workday.autoparse.json.context.JsonParserContext;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.updater.InstanceUpdater;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * @author nathan.taylor
 * @since 2015-08-21.
 */
public class InstanceUpdaterParserAnnotatedObjectParser
        implements JsonObjectParser<InstanceUpdaterParserAnnotatedObject>,
        InstanceUpdater<InstanceUpdaterParserAnnotatedObject> {

    public static InstanceUpdaterParserAnnotatedObjectParser INSTANCE
            = new InstanceUpdaterParserAnnotatedObjectParser();

    private InstanceUpdaterParserAnnotatedObjectParser() {
    }

    @Override
    public void updateInstanceFromMap(InstanceUpdaterParserAnnotatedObject instance,
                                      Map<String, Object> map,
                                      JsonParserContext context) {
        // do nothing
    }

    @Override
    public Object getField(InstanceUpdaterParserAnnotatedObject instance, String name) {
        return null;
    }

    @Override
    public Object initializeAndGetField(InstanceUpdaterParserAnnotatedObject instance,
                                        String name) {
        return null;
    }

    @Override
    public InstanceUpdaterParserAnnotatedObject parseJsonObject(JSONObject jsonObject,
                                                                JsonReader reader,
                                                                String discriminationName,
                                                                String discriminationValue)
            throws IOException {
        return new InstanceUpdaterParserAnnotatedObject();
    }
}
