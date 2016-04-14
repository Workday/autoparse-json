/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.context;

import com.workday.autoparse.json.parser.CompositeJsonObjectParserTable;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.JsonObjectParserTable;
import com.workday.autoparse.json.parser.JsonObjectParserTables;
import com.workday.autoparse.json.parser.JsonParserUtils;
import com.workday.autoparse.json.parser.KeyCollisionTester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A holder of information required by the {@link JsonObjectParser}s and {@link JsonParserUtils}.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public class JsonParserContext {

    private JsonObjectParserTable parserTable;
    private JsonParserSettings settings;

    public JsonParserContext(JsonParserSettings settings) {
        this.settings = settings;
        parserTable = constructParserTable(settings.getPartitionPackages());
    }

    private JsonObjectParserTable constructParserTable(Collection<String> packageNames) {
        if (packageNames.isEmpty()) {
            packageNames =
                    Collections.singletonList(JsonParserSettingsBuilder
                                                      .DEFAULT_OBJECT_PARSER_PACKAGE);
        }
        if (packageNames.size() == 1) {
            return JsonObjectParserTables.getParserTable(packageNames.iterator().next());
        }

        final List<JsonObjectParserTable> components = new ArrayList<>();
        for (String packageName : packageNames) {
            components.add(JsonObjectParserTables.getParserTable((packageName)));
        }
        KeyCollisionTester.validateMaps(components);
        return new CompositeJsonObjectParserTable(components);
    }

    public JsonParserSettings getSettings() {
        return settings;
    }

    public JsonObjectParserTable getJsonObjectParserTable() {
        return parserTable;
    }
}
