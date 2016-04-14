/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import com.workday.autoparse.json.context.GeneratedClassNames;

/**
 * @author nathan.taylor
 * @since 2015-08-21.
 */
public class JsonObjectParserTables {

    private JsonObjectParserTables() {
    }

    public static JsonObjectParserTable getParserTable(String packageName) {
        try {
            String parserMapSimpleName = GeneratedClassNames.CLASS_JSON_OBJECT_PARSER_TABLE;
            String parserMapFullName =
                    GeneratedClassNames.getQualifiedName(packageName, parserMapSimpleName);
            return (JsonObjectParserTable) Class.forName(parserMapFullName).newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
