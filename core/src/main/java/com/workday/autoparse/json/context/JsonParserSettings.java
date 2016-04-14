/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.context;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.JsonStreamParser;

import org.json.JSONObject;

import java.util.Collection;

/**
 * Represents a configuration for a particular instance of a {@link JsonStreamParser}. New instances
 * should be created via the {@link JsonParserSettingsBuilder}. The following items may be
 * configured.
 * <p/>
 * <h3>Discrimination Name</h3> The Discrimination Name-Value Pair is a name-value pair that may be
 * found on any object in a JSON document which Autoparse will use to determine what kind of custom
 * object to inflate. See {@link JsonObject#value()} for a more detailed description.
 * <p/>
 * <h3>Unknown Object Parser</h3> When an object is encountered in the JSON document and it either
 * does not contain a Discrimination Name-Value Pair or the Discrimination Value does not match any
 * known one, then this {@link JsonObjectParser} will be used to parse the object. If no such parser
 * is provided, then Autoparse will try to return a {@link JSONObject} for the unknown object.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public class JsonParserSettings {

    private String discriminationKeyName;
    private JsonObjectParser<?> unknownObjectParser;
    private Class<?> unknownObjectClass;
    private Collection<String> partitionPackages;

    JsonParserSettings(String discriminationName,
                       JsonObjectParser<?> unknownObjectParser,
                       Class<?> unknownObjectClass,
                       Collection<String> partitionPackages) {
        this.discriminationKeyName = discriminationName;
        this.unknownObjectParser = unknownObjectParser;
        this.unknownObjectClass = unknownObjectClass;
        this.partitionPackages = partitionPackages;
    }

    public String getDiscriminationName() {
        return discriminationKeyName;
    }

    public JsonObjectParser<?> getUnknownObjectParser() {
        return unknownObjectParser;
    }

    public Class<?> getUnknownObjectClass() {
        return unknownObjectClass;
    }

    public Collection<String> getPartitionPackages() {
        return partitionPackages;
    }
}
