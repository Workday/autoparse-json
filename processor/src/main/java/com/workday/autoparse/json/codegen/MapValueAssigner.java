/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.autoparse.json.utils.Preconditions;

import java.io.IOException;

/**
 * @author nathan.taylor
 * @since 2014-11-17.
 */
class MapValueAssigner implements ValueAssigner {

    private final Builder state;

    static class Builder {

        private String mapType;
        private String mapTypeErasure;
        private String mapDeclarationPattern;
        private String assignmentPattern;
        private String valueParameterTypeErasure;
        private String parserInstance;
        private PostCreateChildBlockWriter postCreateChildBlockWriter;

        /**
         * The reference type of the Map.
         */
        public Builder withMapType(String mapType) {
            this.mapType = mapType;
            return this;
        }

        /**
         * The raw reference type of the Map.
         */
        public Builder withMapTypeErasure(String mapTypeErasure) {
            this.mapTypeErasure = mapTypeErasure;
            return this;
        }

        /**
         * Must accept two arguments: (1) the jsonObject variable name and (2) key (i.e. the name in the name-value
         * pair).
         */
        public Builder withMapDeclarationPattern(String mapDeclarationPattern) {
            this.mapDeclarationPattern = mapDeclarationPattern;
            return this;
        }

        /**
         * Must accept two arguments: (1) objectName and (2) value. The result should be of the form {@code
         * objectName.field = value} or {@code objectName.setter(value)}.
         */
        public Builder withAssignmentPattern(String assignmentPattern) {
            this.assignmentPattern = assignmentPattern;
            return this;
        }

        /**
         * The erasure of the value parameter, e.g. "java.lang.String" or "java.util.List".
         */
        public Builder withValueParameterTypeErasure(String valueParameterTypeErasure) {
            this.valueParameterTypeErasure = valueParameterTypeErasure;
            return this;
        }

        /**
         * A simple String that is the instance of the parser to use for items or "null". This String should take no
         * arguments. This should be of the form {@code com.package.Parser.INSTANCE}.
         */
        public Builder withParserInstance(String parserInstance) {
            this.parserInstance = parserInstance;
            return this;
        }

        /**
         * The object that will handle writing post parse calls.
         */
        public Builder withPostCreateChildBlockWriter(PostCreateChildBlockWriter postCreateChildBlockWriter) {
            this.postCreateChildBlockWriter = postCreateChildBlockWriter;
            return this;
        }

        public MapValueAssigner build() {
            Preconditions.checkNotNull(mapType, "mapType");
            Preconditions.checkNotNull(mapTypeErasure, "mapTypeErasure");
            Preconditions.checkNotNull(mapDeclarationPattern, "mapDeclarationPattern");
            Preconditions.checkNotNull(assignmentPattern, "assignmentPattern");
            Preconditions.checkNotNull(valueParameterTypeErasure, "valueParameterTypeErasure");
            Preconditions.checkNotNull(parserInstance, "parserInstance");
            Preconditions.checkNotNull(postCreateChildBlockWriter, "postCreateChildBlockWriter");
            return new MapValueAssigner(this);
        }
    }

    private MapValueAssigner(Builder builder) {
        state = builder;
    }

    @Override
    public void writeFromReaderAssignment(JavaWriter writer, String objectName, String readerName, String key)
            throws IOException {
        writer.emitStatement(state.mapDeclarationPattern, "map");
        writer.emitStatement("JsonParserUtils.parseAsMap(%s, map, %s.class, %s, \"%s\")", readerName,
                             state.valueParameterTypeErasure, state.parserInstance, key);
        writer.emitStatement(state.assignmentPattern, objectName, "map");
        state.postCreateChildBlockWriter.writePostCreateMapBlock(writer, objectName, "map");
    }

    @Override
    public void writeFromJsonObjectAssignment(JavaWriter writer, String objectName, String jsonObjectName, String name)
            throws IOException {
        writer.emitStatement(state.mapDeclarationPattern, "map");
        writer.emitStatement("JsonParserUtils.convertJsonObjectToMap(%1$s.optJSONObject(\"%2$s\"), map, %3$s"
                                     + ".class, %4$s, \"%2$s\")", jsonObjectName, name, state.valueParameterTypeErasure,
                             state.parserInstance);
        writer.emitStatement(state.assignmentPattern, objectName, "map");
        state.postCreateChildBlockWriter.writePostCreateMapBlock(writer, objectName, "map");
    }

    @Override
    public void writeFromMapAssignment(final JavaWriter writer, String objectName, String mapName, final String key)
            throws IOException {

        // Instantiate the new map.
        writer.emitAnnotation(SuppressWarnings.class, JavaWriter.stringLiteral("unchecked"));
        writer.emitStatement(state.mapDeclarationPattern, "value");

        // Switch based on the type of the actual object in the map.
        writer.emitStatement("Object o = map.get(\"%s\")", key);
        writer.beginControlFlow("if (o instanceof Map)");
        writer.emitStatement("value.putAll((Map) o)");
        writer.nextControlFlow("else if (o instanceof JSONObject)");
        ErrorWriter.surroundWithIoTryCatch(writer, new ErrorWriter.ContentWriter() {
            @Override
            public void writeContent() throws IOException {
                writer.emitStatement(
                        "JsonParserUtils.convertJsonObjectToMap((JSONObject) o, value, %s.class, " + "%s, \"%s\", "
                                + "context)", state.valueParameterTypeErasure, state.parserInstance, key);
            }
        });
        writer.nextControlFlow("else");
        ErrorWriter.writeConversionException(writer, state.mapType, "o");
        writer.endControlFlow();

        // Assign the new collection the instance's field.
        writer.emitStatement(state.assignmentPattern, objectName, "value");
        state.postCreateChildBlockWriter.writePostCreateMapBlock(writer, objectName, "value");
    }
}
