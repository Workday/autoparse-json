/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.context;

import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.JsonObjectParserTable;
import com.workday.autoparse.json.updater.InstanceUpdaterTable;
import com.workday.autoparse.json.updater.InstanceUpdaters;

import java.util.Locale;

/**
 * @author nathan.taylor
 * @since 2014-10-16.
 */
public final class GeneratedClassNames {

    private GeneratedClassNames() {
    }

    public static final String CLASS_INSTANCE_UPDATER_TABLE =
            "__" + InstanceUpdaterTable.class.getSimpleName()
                    + "$$GeneratedImpl";
    public static final String PACKAGE_INSTANCE_UPDATER_TABLE =
            InstanceUpdaters.class.getPackage().getName();
    public static final String CLASS_JSON_OBJECT_PARSER_TABLE = "GeneratedJsonObjectParserTable";
    public static final String PACKAGE_JSON_OBJECT_PARSER_TABLE =
            JsonObjectParserTable.class.getPackage().getName();
    public static final String PARSER_SUFFIX = "$$" + JsonObjectParser.class.getSimpleName();

    public static String getQualifiedName(String packageName, String className) {
        return String.format(Locale.US, "%s.%s", packageName, className);
    }
}
