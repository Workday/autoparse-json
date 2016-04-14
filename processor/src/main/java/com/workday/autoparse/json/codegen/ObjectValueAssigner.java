/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.meta.MetaTypes;
import com.workday.meta.Modifiers;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

/**
 * A {@link ValueAssigner} for any object that is not a {@link Collection} or one of the natively
 * handled object types.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
class ObjectValueAssigner implements ValueAssigner {

    public static class Info {

        private TypeMirror objectType;
        private String assignmentPattern;
        private String parserInstance;
        private boolean convertJsonTypes;
        private PostCreateChildBlockWriter postCreateChildBlockWriter;

        public Info withObjectType(TypeMirror objectType) {
            this.objectType = objectType;
            return this;
        }

        /**
         * The assignment pattern must accept two parameters: (1) objectName, and (2) value.
         */
        public Info withAssignmentPattern(String assignmentPattern) {
            this.assignmentPattern = assignmentPattern;
            return this;
        }

        /**
         * An instance of the parser to use, or "null".
         */
        public Info withParserInstance(String parserInstance) {
            this.parserInstance = parserInstance;
            return this;
        }

        public Info withConvertJsonTypes(boolean convertJsonTypes) {
            this.convertJsonTypes = convertJsonTypes;
            return this;
        }

        public Info withPostCreateChildBlockWriter(PostCreateChildBlockWriter
                                                           postCreateChildBlockWriter) {
            this.postCreateChildBlockWriter = postCreateChildBlockWriter;
            return this;
        }
    }

    private final TypeMirror objectType;
    private final MetaTypes metaTypes;
    private final PostCreateChildBlockWriter postCreateChildBlockWriter;
    private final String objectTypeString;
    private final String objectTypeErasure;
    private final String assignmentPattern;
    private final String parserInstance;
    private final boolean convertJsonTypes;

    public ObjectValueAssigner(ProcessingEnvironment processingEnv, Info info) {
        this.metaTypes = new MetaTypes(processingEnv);

        this.objectType = info.objectType;
        this.objectTypeString = objectType.toString();
        this.objectTypeErasure = processingEnv.getTypeUtils().erasure(objectType).toString();

        this.assignmentPattern = info.assignmentPattern;
        this.parserInstance = info.parserInstance;
        this.convertJsonTypes = info.convertJsonTypes;

        this.postCreateChildBlockWriter = info.postCreateChildBlockWriter;
    }

    @Override
    public void writeFromReaderAssignment(JavaWriter writer,
                                          String objectName,
                                          String readerName,
                                          String key)
            throws IOException {
        String value;
        switch (objectTypeString) {
            case AndroidNames.JSON_ARRAY_FULL:
                value = String.format(Locale.US,
                                      "JsonParserUtils.parseAsJsonArray(%s, \"%s\")",
                                      readerName,
                                      key);
                break;
            case AndroidNames.JSON_OBJECT_FULL:
                value = String.format(Locale.US,
                                      "JsonParserUtils.parseAsJsonObject(%s, \"%s\")",
                                      readerName,
                                      key);
                break;
            case "java.lang.Object":
                value = String.format(Locale.US,
                                      "JsonParserUtils.parseNextValue(%s, %s)",
                                      readerName,
                                      convertJsonTypes);
                break;
            default:
                value = String.format(Locale.US,
                                      "(%1$s) JsonParserUtils.parseJsonObject(%2$s, %3$s, "
                                              + "\"%4$s\", %5$s.class)",
                                      writer.compressType(objectTypeString),
                                      readerName,
                                      parserInstance,
                                      key,
                                      writer.compressType(objectTypeErasure));
                break;
        }
        writer.emitField(objectTypeString, "value", Modifiers.NONE, value);
        writer.emitStatement(assignmentPattern, objectName, "value");
        postCreateChildBlockWriter.writePostCreateChildBlock(writer, objectName, "value");
    }

    @Override
    public void writeFromJsonObjectAssignment(JavaWriter writer,
                                              String objectName,
                                              String jsonObjectName,
                                              String name)
            throws IOException {

        String objectTypeCompressed = writer.compressType(objectTypeString);
        writer.emitField(objectTypeCompressed, "value", Modifiers.NONE, "null");
        writer.emitField(Object.class.getCanonicalName(), "o", Modifiers.NONE,
                         String.format(Locale.US, "%1$s.opt(\"%2$s\")", jsonObjectName, name));

        if (AndroidNames.JSON_OBJECT_FULL.equals(objectTypeString)
                || AndroidNames.JSON_ARRAY_FULL.equals(
                objectTypeString)) {

            // If the object type is a JSONObject or JSONArray, then 'o' is probably one of those
            // already.
            // Do the instanceof check and then assign 'o' to 'value'.
            writer.beginControlFlow("if (o instanceof %s)", objectTypeCompressed);
            writer.emitStatement("value = (%s) o", objectTypeCompressed);
            writer.endControlFlow();
        } else if ("null".equals(parserInstance)) {

            if (!convertJsonTypes && Object.class.getCanonicalName().equals(objectTypeString)) {
                // A wildcard that where we are not supposed to convert json types, so just
                // assign 'o' to 'value'.
                writer.emitStatement("value = o");
            } else {

                // Attempt a conversion without an explicit parser instance
                writer.beginControlFlow("if (o instanceof %s)", AndroidNames.JSON_OBJECT);
                writer.emitStatement(
                        "value = JsonParserUtils.convertJsonObject((%s) o, %s.class, null)",
                        AndroidNames.JSON_OBJECT,
                        objectTypeCompressed);
                if (metaTypes.isAssignable(Collection.class, objectType)) {
                    writer.nextControlFlow("else if (o instanceof %s)",
                                           AndroidNames.JSON_ARRAY_FULL);
                    writer.emitStatement("value = JsonParserUtils.convertArbitraryJsonArray((%s) "
                                                 + "o)",
                                         AndroidNames.JSON_ARRAY_FULL);
                }
                writer.nextControlFlow("else if (o instanceof %s)", objectTypeCompressed);
                if (Object.class.getCanonicalName().equals(objectTypeString)) {
                    // Do not generate a redundant cast warning
                    writer.emitStatement("value = o");
                } else {
                    writer.emitStatement("value = (%s) o", objectTypeCompressed);
                }
                writer.endControlFlow();
            }
        } else {

            // We were given a parser instance, so use that to perform the conversion.
            writer.beginControlFlow("if (o instanceof %s)", AndroidNames.JSON_OBJECT);
            writer.emitStatement(
                    "value = %s.parseJsonObject((%s) o, null, discriminationName, null)",
                    parserInstance,
                    AndroidNames.JSON_OBJECT);
            writer.endControlFlow();
        }

        // Check to make sure that any conversions were successful
        writer.beginControlFlow("if (value == null)");
        String message = String.format(Locale.US,
                                       "\"Could not convert value at \\\"%s\\\" to %s from \" + o"
                                               + ".getClass().getCanonicalName() + \".\"",
                                       name,
                                       objectTypeString);
        writer.emitStatement("throw new java.lang.RuntimeException(%s)", message);
        writer.endControlFlow();

        // Assign the resulting value to the parent object
        writer.emitStatement(assignmentPattern, objectName, "value");
        postCreateChildBlockWriter.writePostCreateChildBlock(writer, objectName, "value");
    }

    @Override
    public void writeFromMapAssignment(JavaWriter writer,
                                       String objectName,
                                       String mapName,
                                       String key)
            throws IOException {
        if (Object.class.getCanonicalName().equals(objectTypeString)) {
            writeWildcardGetterFromMap(writer, mapName, key);
        } else {
            writeObjectGetterFromMap(writer, mapName, key);
        }

        writer.emitStatement(assignmentPattern, objectName, "value");
        postCreateChildBlockWriter.writePostCreateChildBlock(writer, objectName, "value");
    }

    private void writeWildcardGetterFromMap(final JavaWriter writer, String mapName, String key)
            throws IOException {
        if (convertJsonTypes) {
            writer.emitStatement(("Object value = null"));
            writer.emitStatement("Object o = %s.get(\"%s\")", mapName, key);
            writer.beginControlFlow("if (o instanceof JSONObject)");
            ErrorWriter.surroundWithIoTryCatch(writer, new ErrorWriter.ContentWriter() {
                @Override
                public void writeContent() throws IOException {
                    writer.emitStatement(
                            "value = JsonParserUtils.convertJsonObject((JSONObject) o, Object"
                                    + ".class, null, context)");
                }
            });
            writer.nextControlFlow("else if (o instanceof JSONArray)");
            ErrorWriter.surroundWithIoTryCatch(writer, new ErrorWriter.ContentWriter() {
                @Override
                public void writeContent() throws IOException {
                    writer.emitStatement(
                            "value = JsonParserUtils.convertArbitraryJsonArray((JSONArray) o, "
                                    + "context)");
                }
            });
            writer.nextControlFlow("else");
            writer.emitStatement("value = o");
            writer.endControlFlow();
        } else {
            writer.emitStatement("Object value = %s.get(\"%s\")", mapName, key);
        }
    }

    private void writeObjectGetterFromMap(final JavaWriter writer, String mapName, String key)
            throws IOException {
        String objectTypeCompressed = writer.compressType(objectTypeString);
        writer.emitStatement(("%s value"), objectTypeCompressed);
        writer.emitStatement("Object o = %s.get(\"%s\")", mapName, key);
        writer.beginControlFlow("if (o == null)");
        writer.emitStatement("value = null");
        writer.nextControlFlow("else if (o instanceof %s)", objectTypeCompressed);
        writer.emitStatement("value = (%s) o", objectTypeCompressed);
        if (!AndroidNames.JSON_OBJECT_FULL.equals(objectTypeString)
                && !AndroidNames.JSON_ARRAY_FULL.equals(objectTypeString)) {
            writer.nextControlFlow("else if (o instanceof JSONObject)");
            ErrorWriter.surroundWithIoTryCatch(writer, new ErrorWriter.ContentWriter() {
                @Override
                public void writeContent() throws IOException {
                    writer.emitStatement(
                            "value = JsonParserUtils.convertJsonObject((JSONObject) o, %s.class, "
                                    + "%s, context)",
                            objectTypeErasure,
                            parserInstance);
                }
            });
        }
        writer.nextControlFlow("else");
        ErrorWriter.writeConversionException(writer, objectTypeString, "o");
        writer.endControlFlow();
    }

}
