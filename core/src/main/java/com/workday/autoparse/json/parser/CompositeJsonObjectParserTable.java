/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author travis.westbrook
 * @since 2016-01-07.
 */
public class CompositeJsonObjectParserTable implements JsonObjectParserTable {
    private final Collection<JsonObjectParserTable> components;

    public CompositeJsonObjectParserTable(Collection<JsonObjectParserTable> components) {
        this.components = Collections.unmodifiableCollection(new ArrayList<>(components));
    }

    @Override
    public JsonObjectParser<?> get(String name) {
        for (JsonObjectParserTable parserMap : components) {
            JsonObjectParser<?> parser = parserMap.get(name);
            if (parser != null) {
                return parser;
            }
        }
        return null;
    }

    @Override
    public Set<String> keySet() {
        Set<String> keySet = new HashSet<>();
        for (JsonObjectParserTable component : components) {
            keySet.addAll(component.keySet());
        }
        return keySet;
    }
}
