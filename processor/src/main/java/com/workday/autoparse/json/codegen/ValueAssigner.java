/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.autoparse.json.parser.JsonObjectParser;

import java.io.IOException;
import java.util.Map;

/**
 * Classes implementing this interface encapsulate the code generation of assigning a value to a
 * field or invoking a setter in a {@link JsonObjectParser}.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
interface ValueAssigner {

    /**
     * Write an assignment that extracts the value from a JsonReader.
     *
     * @param writer The JavaWriter to use.
     * @param objectName The variable name of the object to which the field is being assigned or
     * whose method is being called.
     * @param readerName The variable name of the JsonReader from which to extract values.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     *
     * @throws IOException If the JavaWriter throws an exception.
     */
    void writeFromReaderAssignment(JavaWriter writer,
                                   String objectName,
                                   String readerName,
                                   String key)
            throws IOException;

    /**
     * Write an assignment that extracts the value from a JSONObject.
     *
     * @param writer The JavaWriter to use.
     * @param objectName The variable name of the object to which the field is being assigned or
     * whose method is being called.
     * @param jsonObjectName The variable name of the jsonObject from which to extract values.
     * @param name The name to use to extract a value from the JSONObject.
     *
     * @throws IOException If the JavaWriter throws an exception.
     */
    void writeFromJsonObjectAssignment(JavaWriter writer,
                                       String objectName,
                                       String jsonObjectName,
                                       String name)
            throws IOException;

    /**
     * Write an assignment that extracts the value from a {@link Map}<{@link String}, {@link
     * Object}>.
     *
     * @param writer The JavaWriter to use.
     * @param objectName The variable name of the object to which the field is being assigned or
     * whose method is being called.
     * @param mapName The variable name of the map from which to extract values.
     * @param key The key to use to extract a value from the Map.
     */
    void writeFromMapAssignment(JavaWriter writer, String objectName, String mapName, String key)
            throws IOException;
}
