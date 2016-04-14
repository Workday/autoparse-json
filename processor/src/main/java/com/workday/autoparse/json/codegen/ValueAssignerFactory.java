/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.autoparse.json.annotations.JsonValue;
import com.workday.autoparse.json.parser.NoJsonObjectParser;
import com.workday.meta.AnnotationUtils;
import com.workday.meta.CodeAnalysisUtils;
import com.workday.meta.Initializers;
import com.workday.meta.InvalidTypeException;
import com.workday.meta.MetaTypeNames;
import com.workday.meta.MetaTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Assesses {@link Element}s (fields and setters) and creates an appropriate {@link ValueAssigner}
 * for it.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
class ValueAssignerFactory {

    private final ProcessingEnvironment processingEnv;
    private final JavaWriter writer;
    private final PostCreateChildBlockWriter postCreateChildBlockWriter;
    private final MetaTypes metaTypes;
    private final Initializers initializers;
    private final Map<String, String> classNameToParserNameMap;
    private final Types typeUtils;

    ValueAssignerFactory(ProcessingEnvironment processingEnv,
                         Map<String, String> classNameToParserNameMap,
                         JavaWriter writer,
                         PostCreateChildBlockWriter postCreateChildBlockWriter) {
        this.processingEnv = processingEnv;
        this.writer = writer;
        this.postCreateChildBlockWriter = postCreateChildBlockWriter;
        typeUtils = processingEnv.getTypeUtils();
        this.classNameToParserNameMap = classNameToParserNameMap;

        metaTypes = new MetaTypes(processingEnv);
        initializers = new Initializers(metaTypes);
    }

    /**
     * Returns the type that an element takes. If this is a field, then it is the type of the field.
     * If this is a method, then it is the type of the first argument.
     */
    public TypeMirror getAssignmentType(Element element) {
        if (element instanceof ExecutableElement) {
            // This is a setter
            ExecutableElement method = (ExecutableElement) element;
            assertMethodHasSingleParameter(method);
            return method.getParameters().get(0).asType();
        } else {
            // This is a field
            return element.asType();
        }
    }

    /**
     * Creates an appropriate assignment pattern for an element, based on whether it is a field or a
     * method. The will take two arguments: 1) objectName and 2) value.
     */
    public String getAssignmentPattern(Element element) {
        if (element instanceof ExecutableElement) {
            // objectName.methodName(value)
            return "%1$s." + element.getSimpleName() + "(%2$s)";
        } else {
            // objectName.fieldName = value
            return "%1$s." + element.getSimpleName() + " = %2$s";
        }
    }

    /**
     * Assesses an {@link Element} (either a fields or a setter) and creates an appropriate {@link
     * ValueAssigner} for it.
     */
    public ValueAssigner createValueAssigner(Element element) {

        TypeMirror type = getAssignmentType(element);
        assertViableAccessLevel(element);
        assertAnnotationIsConsistent(element, type);

        // First argument: objectName; second argument: value
        String assignmentPattern = getAssignmentPattern(element);

        String readerValuePattern;
        String jsonObjectValuePattern;
        String mapValuePattern;
        final String keyPattern = "\"%3$s\"";
        final String jsonObjectGetStringValue =
                "%2$s.optString(" + keyPattern + ")"; // jsonObject.getString("name")

        // Basic types
        if (metaTypes.isBoolean(type)) {
            readerValuePattern = "JsonParserUtils.nextBoolean(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "Boolean.valueOf(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsBoolean(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isDouble(type)) {
            readerValuePattern = "JsonParserUtils.nextDouble(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "Double.valueOf(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsDouble(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isInt(type)) {
            readerValuePattern = "JsonParserUtils.nextInt(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "Integer.valueOf(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsInt(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isLong(type)) {
            readerValuePattern = "JsonParserUtils.nextLong(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "Long.valueOf(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsLong(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isString(type)) {
            readerValuePattern = "JsonParserUtils.nextString(%2$s, \"%3$s\")";
            jsonObjectValuePattern = jsonObjectGetStringValue;
            mapValuePattern = "MapValueGetter.getAsString(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isByte(type)) {
            readerValuePattern = "JsonParserUtils.nextByte(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "Byte.valueOf(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsByte(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isSameType(type, BigDecimal.class)) {
            readerValuePattern = "JsonParserUtils.nextBigDecimal(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "new java.math.BigDecimal(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsBigDecimal(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isSameType(type, BigInteger.class)) {
            readerValuePattern = "JsonParserUtils.nextBigInteger(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "new java.math.BigInteger(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsBigInteger(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isFloat(type)) {
            readerValuePattern = "JsonParserUtils.nextFloat(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "Float.valueOf(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsFloat(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isShort(type)) {
            readerValuePattern = "JsonParserUtils.nextShort(%2$s, \"%3$s\")";
            jsonObjectValuePattern = "Short.valueOf(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsShort(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);
        } else if (metaTypes.isChar(type)) {
            readerValuePattern = "JsonParserUtils.nextChar(%2$s, \"%3$s\")";
            jsonObjectValuePattern =
                    "JsonParserUtils.getCharFromString(" + jsonObjectGetStringValue + ")";
            mapValuePattern = "MapValueGetter.getAsChar(map, " + keyPattern + ")";
            return new SingleLineValueAssigner(assignmentPattern,
                                               readerValuePattern,
                                               jsonObjectValuePattern,
                                               mapValuePattern);

        } else if (metaTypes.isSubtypeErasure(type, Collection.class)) {
            return getCollectionValueAssigner(element, type, assignmentPattern);

        } else if (metaTypes.isSubtypeErasure(type, Map.class)) {
            return getMapValueAssigner(element, type, assignmentPattern);

        } else {
            return getFallbackObjectValueAssigner(element, type, assignmentPattern);
        }
    }

    private ValueAssigner getCollectionValueAssigner(Element element,
                                                     TypeMirror type,
                                                     String assignmentPattern) {
        DeclaredType nextNestedClassType = (DeclaredType) type;
        List<String> nestedCollectionParameters = new ArrayList<>();

        // Extract the parameter of each nested collection as a flattened list.
        do {
            try {
                nextNestedClassType = metaTypes.getFirstParameterType(nextNestedClassType);
            } catch (InvalidTypeException e) {
                processingEnv.getMessager()
                             .printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
                nextNestedClassType = (DeclaredType) processingEnv.getElementUtils()
                                                                  .getTypeElement(Object.class
                                                                                          .getCanonicalName())
                                                                  .asType();
            }

            nestedCollectionParameters.add(typeUtils.erasure(nextNestedClassType).toString());
        } while (metaTypes.isSubtypeErasure(nextNestedClassType, Collection.class));

        String collectionInitializer = null;
        try {
            collectionInitializer = initializers.findCollectionInitializer((DeclaredType) type);
        } catch (InvalidTypeException e) {
            processingEnv.getMessager()
                         .printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
        }
        String collectionInitializerPattern =
                type.toString() + " %s" + " = " + collectionInitializer;
        // TODO: assert that parser type matches field type
        String parserInstance = getParserInstance(element, nextNestedClassType);

        return new CollectionValueAssigner.Builder().withCollectionType(type.toString())
                                                    .withCollectionTypeErasure(
                                                            typeUtils.erasure(type).toString())
                                                    .withAssignmentPattern(assignmentPattern)
                                                    .withCollectionDeclarationPattern(
                                                            collectionInitializerPattern)
                                                    .withParser(parserInstance)
                                                    .withNestedCollectionParameters(
                                                            nestedCollectionParameters)
                                                    .withPostCreateChildBlockWriter(
                                                            postCreateChildBlockWriter)
                                                    .build();
    }

    private ValueAssigner getMapValueAssigner(Element element,
                                              TypeMirror type,
                                              String assignmentPattern) {
        DeclaredType valueType;
        try {
            valueType = metaTypes.getParameterType((DeclaredType) type, 1);
        } catch (InvalidTypeException e) {
            processingEnv.getMessager()
                         .printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
            valueType = (DeclaredType) processingEnv.getElementUtils()
                                                    .getTypeElement(Object.class
                                                                            .getCanonicalName())
                                                    .asType();
        }

        String mapInitializer = null;
        try {
            mapInitializer = initializers.findMapInitializer((DeclaredType) type);
        } catch (InvalidTypeException e) {
            processingEnv.getMessager()
                         .printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
        }

        String mapInitializerPattern = type.toString() + " %s = " + mapInitializer;
        String parserInstance = getParserInstance(element, valueType);

        return new MapValueAssigner.Builder()
                .withMapType(type.toString())
                .withMapTypeErasure(typeUtils.erasure(type).toString())
                .withMapDeclarationPattern(mapInitializerPattern)
                .withAssignmentPattern(assignmentPattern)
                .withValueParameterTypeErasure(typeUtils.erasure(valueType).toString())
                .withParserInstance(parserInstance)
                .withPostCreateChildBlockWriter(postCreateChildBlockWriter)
                .build();
    }

    private ValueAssigner getFallbackObjectValueAssigner(Element element,
                                                         TypeMirror type,
                                                         String assignmentPattern) {
        // TODO: assert that parser type matches field type
        String parserInstance = getParserInstance(element, type);
        JsonValue annotation = element.getAnnotation(JsonValue.class);
        ObjectValueAssigner.Info info = new ObjectValueAssigner.Info()
                .withObjectType(type)
                .withAssignmentPattern(assignmentPattern)
                .withParserInstance(parserInstance)
                .withPostCreateChildBlockWriter(postCreateChildBlockWriter)
                .withConvertJsonTypes(annotation.convertJsonTypes());
        return new ObjectValueAssigner(processingEnv, info);
    }

    private String getParserInstance(final Element member, TypeMirror parsedClassType) {
        TypeMirror typeMirror =
                AnnotationUtils.getClassTypeMirrorFromAnnotationValue(new AnnotationUtils.Getter() {
                    @Override
                    public void get() {
                        member.getAnnotation(JsonValue.class).parser();
                    }
                });

        // TODO: make these names more distinctive
        TypeElement parsedClassElement = (TypeElement) typeUtils.asElement(parsedClassType);
        String parsedClassName = MetaTypeNames.constructTypeName(parsedClassElement);

        String parserClassName = typeMirror.toString();

        if (NoJsonObjectParser.class.getCanonicalName().equals(parserClassName)) {
            parserClassName = classNameToParserNameMap.get(parsedClassName);
            // TODO: look for an annotated member for the instance
            return parserClassName == null
                   ? "null"
                   : writer.compressType(parserClassName) + ".INSTANCE";
        } else {
            return writer.compressType(parserClassName) + ".INSTANCE";
        }
    }

    private boolean assertViableAccessLevel(Element element) {
        boolean viable = true;
        if (CodeAnalysisUtils.isPrivate(element)) {
            processingEnv.getMessager()
                         .printMessage(Diagnostic.Kind.ERROR,
                                       "Autoparse cannot access private members.",
                                       element);
            viable = false;
        }

        if (element instanceof VariableElement && CodeAnalysisUtils.isFinal(element)) {
            processingEnv.getMessager()
                         .printMessage(Diagnostic.Kind.ERROR,
                                       "Autoparse cannot assign final fields.",
                                       element);
            viable = false;
        }

        if (element instanceof VariableElement && CodeAnalysisUtils.isStatic(element)) {
            processingEnv.getMessager()
                         .printMessage(Diagnostic.Kind.WARNING,
                                       "Autoparse will assign a value to a static field when "
                                               + "parsing. You probably "
                                               + "don't want this to happen.",
                                       element);
        }

        return viable;
    }

    private boolean assertAnnotationIsConsistent(Element element, TypeMirror type) {
        JsonValue annotation = element.getAnnotation(JsonValue.class);

        if (annotation.convertJsonTypes()) {
            return true;
        }

        if (AndroidNames.JSON_OBJECT_FULL.equals(type.toString())
                || AndroidNames.JSON_ARRAY_FULL.equals(
                type.toString())) {
            return true;
        }

        if (Object.class.getCanonicalName().equals(type.toString())) {
            return true;
        }

        String message = String.format(
                "Specifying 'convertJsonTypes=false' on a field or setter that does not take an "
                        + "object of type %s, %s, or %s is illegal.",
                Object.class.getCanonicalName(),
                AndroidNames.JSON_OBJECT_FULL,
                AndroidNames.JSON_ARRAY_FULL);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
        return false;
    }

    private boolean assertMethodHasSingleParameter(ExecutableElement method) {
        if (method.getParameters().size() != 1) {
            String errorMessage = "AutoParse can only set values on single-parameter methods.";
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorMessage, method);
            return false;
        }
        return true;
    }
}
