/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import com.workday.autoparse.json.context.GeneratedClassNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author travis.westbrook
 * @since 2016-01-07.
 */
public class KeyCollisionTester {

    private KeyCollisionTester() {
    }

    public static void validateMaps(Collection<JsonObjectParserTable> maps) {
        Map<String, Collection<JsonObjectParserTable>> validationMap = new HashMap<>();
        for (JsonObjectParserTable parserMap : maps) {
            for (String key : parserMap.keySet()) {
                putInMap(validationMap, key, parserMap);
            }
        }

        Collection<String> errorMessages = new ArrayList<>();
        for (Map.Entry<String, Collection<JsonObjectParserTable>> entry
                : validationMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                errorMessages.add(getErrorMessage(entry.getKey(), entry.getValue()));
            }
        }

        if (!errorMessages.isEmpty()) {
            StringBuilder sb = new StringBuilder(
                    "Multiple models map to the same key. The following lists all violations:\n");
            for (String errorMessage : errorMessages) {
                sb.append(errorMessage).append("\n");
            }

            throw new IllegalArgumentException(sb.toString());
        }
    }

    private static void putInMap(Map<String, Collection<JsonObjectParserTable>> map,
                                 String key,
                                 JsonObjectParserTable value) {
        Collection<JsonObjectParserTable> collection = map.get(key);
        if (collection == null) {
            collection = new ArrayList<>();
            map.put(key, collection);
        }
        collection.add(value);
    }

    private static String getErrorMessage(String key,
                                          Collection<JsonObjectParserTable> parserMaps) {
        StringBuilder sb = new StringBuilder("'").append(key).append("' =>\n");
        for (JsonObjectParserTable parserMap : parserMaps) {
            String parserName = parserMap.get(key).getClass().getCanonicalName();
            String modelName =
                    parserName.endsWith(GeneratedClassNames.PARSER_SUFFIX)
                    ? parserName.substring(0,
                                           parserName.length()
                                                   - GeneratedClassNames.PARSER_SUFFIX.length())
                    : parserName;
            sb.append("   ").append(modelName).append('\n');
        }
        return sb.toString();
    }
}
