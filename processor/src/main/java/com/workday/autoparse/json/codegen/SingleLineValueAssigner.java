/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link ValueAssigner} where the complete read and assignment are defined by the
 * creator.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
class SingleLineValueAssigner implements ValueAssigner {

    private final String readerAssignmentPattern;
    private final String jsonObjectAssignmentPattern;
    private final String mapAssignmentPattern;

    /**
     * @param assignmentPattern Must accept three arguments: (1) objectName and (2) value
     * @param readerValuePattern The string that extracts a value from the JsonReader. Must accept 2
     * arguments: (1) readerName and (2) key (i.e. the name in the name-value pair).
     * @param jsonObjectValuePattern The string that extracts a value from a JSONObject. Must accept
     * two arguments: (1) the jsonObject variable name and (2) key (i.e. the name in the name-value
     * pair).
     * @param mapValuePattern The string that extracts a value from a {@link Map}. Must accept two
     * arguments: (1) the map variable name and (2) the key.
     */
    public SingleLineValueAssigner(String assignmentPattern,
                                   String readerValuePattern,
                                   String jsonObjectValuePattern,
                                   String mapValuePattern) {
        this.readerAssignmentPattern =
                String.format(Locale.US, assignmentPattern, "%1$s", readerValuePattern);
        this.jsonObjectAssignmentPattern =
                String.format(Locale.US, assignmentPattern, "%1$s", jsonObjectValuePattern,
                              "%3$s");
        this.mapAssignmentPattern =
                String.format(Locale.US, assignmentPattern, "%1$s", mapValuePattern, "%3$s");
    }

    @Override
    public void writeFromReaderAssignment(JavaWriter writer,
                                          String objectName,
                                          String readerName,
                                          String key)
            throws IOException {
        writer.beginControlFlow("if (!JsonParserUtils.handleNull(%s))", readerName);
        writer.emitStatement(readerAssignmentPattern, objectName, readerName, key);
        writer.endControlFlow();
    }

    @Override
    public void writeFromJsonObjectAssignment(JavaWriter writer,
                                              String objectName,
                                              String jsonObjectName,
                                              String name)
            throws IOException {
        writer.emitStatement(jsonObjectAssignmentPattern, objectName, jsonObjectName, name);
    }

    @Override
    public void writeFromMapAssignment(JavaWriter writer,
                                       String objectName,
                                       String mapName,
                                       String key)
            throws IOException {
        writer.emitStatement(mapAssignmentPattern, objectName, mapName, key);
    }

}
