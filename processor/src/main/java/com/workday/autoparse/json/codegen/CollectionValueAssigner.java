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
import java.util.List;

/**
 * @author nathan.taylor
 * @since 2014-10-09
 */
class CollectionValueAssigner implements ValueAssigner {

    private final Builder state;

    static class Builder {

        private String collectionType;
        private String collectionTypeErasure;
        private String assignmentPattern;
        private String collectionDeclarationPattern;
        private String parser;
        private List<String> nestedCollectionParameters;
        private PostCreateChildBlockWriter postCreateChildBlockWriter;
        private String itemType;

        /**
         * The reference type of the collection.
         */
        public Builder withCollectionType(String collectionType) {
            this.collectionType = collectionType;
            return this;
        }

        /**
         * The raw reference type of the collections.
         */
        public Builder withCollectionTypeErasure(String collectionTypeErasure) {
            this.collectionTypeErasure = collectionTypeErasure;
            return this;
        }

        /**
         * Must accept two arguments: (1) objectName and (2) value. The result should be of the form
         * {@code objectName.field = value} or {@code objectName.setter(value)}.
         */
        public Builder withAssignmentPattern(String assignmentPattern) {
            this.assignmentPattern = assignmentPattern;
            return this;
        }

        /**
         * Must accept one argument: variableName. The result should be of the form {@code
         * Collection<T> variableName = new Collection<>()}.
         */
        public Builder withCollectionDeclarationPattern(String collectionDeclarationPattern) {
            this.collectionDeclarationPattern = collectionDeclarationPattern;
            return this;
        }

        /**
         * A simple String that is the instance of the parser to use for items or "null". This
         * String should take no arguments. This should be of the form {@code
         * com.package.Parser.INSTANCE}.
         */
        public Builder withParser(String parser) {
            this.parser = parser;
            return this;
        }

        /**
         * A List of the parameters of the collection. For instance, if the list contains {Set,
         * List, String}, then the declared collection type would be {@code
         * Collection<Set<List<String>>>}.
         */
        public Builder withNestedCollectionParameters(List<String> nestedCollectionParameters) {
            this.nestedCollectionParameters = nestedCollectionParameters;
            itemType = nestedCollectionParameters.get(nestedCollectionParameters.size() - 1);
            return this;
        }

        /**
         * The object that will handle writing post parse calls.
         */
        public Builder withPostCreateChildBlockWriter(PostCreateChildBlockWriter
                                                              postCreateChildBlockWriter) {
            this.postCreateChildBlockWriter = postCreateChildBlockWriter;
            return this;
        }

        public CollectionValueAssigner build() {
            Preconditions.checkNotNull(collectionType, "collectionType");
            Preconditions.checkNotNull(collectionTypeErasure, "collectionTypeErasure");
            Preconditions.checkNotNull(assignmentPattern, "assignmentPattern");
            Preconditions.checkNotNull(collectionDeclarationPattern,
                                       "collectionDeclarationPattern");
            Preconditions.checkNotNull(parser, "parser");
            Preconditions.checkNotNull(nestedCollectionParameters, "nestedCollectionParameters");
            Preconditions.checkNotNull(postCreateChildBlockWriter, "postCreateChildBlockWriter");
            Preconditions.checkNotNull(itemType, "itemType");

            return new CollectionValueAssigner(this);
        }
    }

    private CollectionValueAssigner(Builder builder) {
        state = builder;
    }

    @Override
    public void writeFromReaderAssignment(JavaWriter writer,
                                          String objectName,
                                          String readerName,
                                          String key)
            throws IOException {

        writer.beginControlFlow("if (!JsonParserUtils.handleNull(%s))", readerName);
        writer.emitStatement(state.collectionDeclarationPattern, "collection");
        writeParameterList(writer);
        writer.emitStatement(
                "JsonParserUtils.parseJsonArray(%1$s, collection, %2$s, %3$s.class, "
                        + "parameterList, \"%4$s\")",
                readerName,
                state.parser,
                writer.compressType(state.itemType),
                key);
        writer.emitStatement(state.assignmentPattern, objectName, "collection");
        state.postCreateChildBlockWriter.writePostCreateCollectionBlock(writer,
                                                                        objectName,
                                                                        "collection");
        writer.endControlFlow();
    }

    @Override
    public void writeFromJsonObjectAssignment(JavaWriter writer,
                                              String objectName,
                                              String jsonObjectName,
                                              String name)
            throws IOException {
        writer.emitStatement(state.collectionDeclarationPattern, "collection");
        writeParameterList(writer);
        writer.emitStatement(
                "JsonParserUtils.convertJsonArrayToCollection(%s.optJSONArray(\"%s\"), "
                        + "collection, %s, %s.class, parameterList, \"%s\")",
                jsonObjectName,
                name,
                state.parser,
                writer.compressType(state.itemType),
                name);
        writer.emitStatement(state.assignmentPattern, objectName, "collection");
        state.postCreateChildBlockWriter.writePostCreateCollectionBlock(writer,
                                                                        objectName,
                                                                        "collection");
    }

    @Override
    public void writeFromMapAssignment(final JavaWriter writer,
                                       String objectName,
                                       String mapName,
                                       final String key)
            throws IOException {

        // Instantiate the new collection.
        writer.emitAnnotation(SuppressWarnings.class, JavaWriter.stringLiteral("unchecked"));
        writer.emitStatement(state.collectionDeclarationPattern, "value");

        // Switch based on the type of the actual object in the map.
        writer.emitStatement("Object o = %s.get(\"%s\")", mapName, key);
        writer.beginControlFlow("if (o instanceof java.util.Collection)",
                                state.collectionTypeErasure);
        writer.emitStatement("value.addAll((java.util.Collection) o)");
        writer.nextControlFlow("else if (o instanceof JSONArray)");
        writeParameterList(writer);
        ErrorWriter.surroundWithIoTryCatch(writer, new ErrorWriter.ContentWriter() {
            @Override
            public void writeContent() throws IOException {
                writer.emitStatement(
                        "JsonParserUtils.convertJsonArrayToCollection((JSONArray) o, value, %s, "
                                + "%s.class, parameterList, \"%s\", context)",
                        state.parser,
                        writer.compressType(state.itemType),
                        key);
            }
        });
        writer.nextControlFlow("else");
        ErrorWriter.writeConversionException(writer, state.collectionType, "o");
        writer.endControlFlow();

        // Assign the new collection the instance's field.
        writer.emitStatement(state.assignmentPattern, objectName, "value");
        state.postCreateChildBlockWriter.writePostCreateCollectionBlock(writer,
                                                                        objectName,
                                                                        "value");
    }

    private void writeParameterList(JavaWriter writer) throws IOException {
        // TODO: make this a constant in the class
        if (state.nestedCollectionParameters.size() > 1) {
            writer.emitStatement("java.util.List<Class<?>> parameterList = new "
                    + "java.util.ArrayList<>()");
            for (int i = 0; i < state.nestedCollectionParameters.size() - 1; i++) {
                writer.emitStatement("parameterList.add(%s.class)",
                        writer.compressType(state.nestedCollectionParameters.get(i)));
            }
        } else {
            writer.emitStatement("java.util.List<Class<?>> parameterList = null");
        }
    }

    @Override
    public String toString() {
        String collectionType = state.collectionType;
        String collectionTypeErasure = state.collectionTypeErasure;
        String assignmentPattern = state.assignmentPattern;
        String collectionDeclarationPattern = state.collectionDeclarationPattern;
        String parser = state.parser;
        List<String> nestedCollectionParameters = state.nestedCollectionParameters;
        PostCreateChildBlockWriter postCreateChildBlockWriter = state.postCreateChildBlockWriter;
        String itemType = state.itemType;
        return "collectionType: " + collectionType + "\n"
                + "collectionTypeErasure: " + collectionTypeErasure + "\n"
                + "assignmentPattern: " + assignmentPattern + "\n"
                + "collectionDeclarationPattern: " + collectionDeclarationPattern + "\n"
                + "parser: " + parser + "\n"
                + "nestedCollectionParameters: " + nestedCollectionParameters + "\n"
                + "postCreateChildBlockWriter: " + postCreateChildBlockWriter + "\n"
                + "itemType: " + itemType + "\n";
    }
}
