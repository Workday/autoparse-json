/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.autoparse.json.annotations.DiscrimValue;
import com.workday.autoparse.json.annotations.JsonPostCreateChild;
import com.workday.autoparse.json.annotations.JsonSelfValues;
import com.workday.autoparse.json.annotations.JsonValue;
import com.workday.autoparse.json.context.ContextHolder;
import com.workday.autoparse.json.context.GeneratedClassNames;
import com.workday.autoparse.json.context.JsonParserContext;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.JsonParserUtils;
import com.workday.autoparse.json.updater.InstanceUpdater;
import com.workday.autoparse.json.updater.MapValueGetter;
import com.workday.autoparse.json.utils.CollectionUtils;
import com.workday.meta.CodeAnalysisUtils;
import com.workday.meta.Initializers;
import com.workday.meta.InvalidTypeException;
import com.workday.meta.MetaTypeNames;
import com.workday.meta.MetaTypes;
import com.workday.meta.Modifiers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Generates code for {@link JsonObjectParser}s.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
class JsonObjectParserGenerator {

    private static final String SELF_VALUE_NAME_SUFFIX = "$$Value";

    private final ProcessingEnvironment processingEnv;
    private final MetaTypes metaTypes;
    private final Initializers initializers;
    private final TypeElement classElement;
    private final Map<String, String> classNameToParserNameMap;
    private List<AssignmentInfo> assignments;
    private String parsedClassName;
    private List<TypeElement> elementHierarchy;
    private Collection<String> discriminationValueAssignmentPatterns;
    private PostCreateChildBlockWriter postCreateChildBlockWriter;
    private List<ExecutableElement> postCreateChildMethods;
    private SelfValueAssignmentInfo selfValueAssignmentInfo;

    /**
     * @param classElement The class element for which we are generating the parser.
     * @param classNameToParserNameMap All known classes that can be parsed, along with their parsers for use when
     * attempting to find the right parser for a field or setter that takes a concrete type.
     */
    public JsonObjectParserGenerator(ProcessingEnvironment processingEnv,
                                     TypeElement classElement,
                                     Map<String, String> classNameToParserNameMap) {
        this.processingEnv = processingEnv;
        metaTypes = new MetaTypes(processingEnv);
        initializers = new Initializers(metaTypes);
        this.classElement = classElement;
        this.classNameToParserNameMap = classNameToParserNameMap;
    }

    private List<TypeElement> generateElementHierarchy(TypeElement element) {
        LinkedList<TypeElement> hierarchy = new LinkedList<>();
        while (element != null) {
            hierarchy.push(element);
            element = (TypeElement) processingEnv.getTypeUtils().asElement(element.getSuperclass());
        }
        return hierarchy;
    }

    private void initializeAssignments(JavaWriter writer) {
        elementHierarchy = generateElementHierarchy(classElement);
        List<? extends Element> allMembers = processingEnv.getElementUtils().getAllMembers(classElement);
        postCreateChildMethods = getPostCreateChildMethods(allMembers);
        postCreateChildBlockWriter = postCreateChildMethods.isEmpty()
                ? DoNothingPostCreateChildBlockWriter.INSTANCE
                : new StandardPostCreateChildBlockWriter(parsedClassName, postCreateChildMethods);

        ValueAssignerFactory valueAssignerFactory = new ValueAssignerFactory(processingEnv, classNameToParserNameMap,
                                                                             writer, postCreateChildBlockWriter);
        assignments = getAssignments(valueAssignerFactory, allMembers);
        discriminationValueAssignmentPatterns = getDiscriminationValueAssignmentPatterns(valueAssignerFactory,
                                                                                         allMembers);
        selfValueAssignmentInfo = getSelfValueAssignmentInfo(valueAssignerFactory, allMembers);
    }

    private Collection<String> getDiscriminationValueAssignmentPatterns(ValueAssignerFactory valueAssignerFactory,
                                                                        List<? extends Element> allMembers) {
        Collection<String> results = new ArrayList<>();

        for (Element member : allMembers) {
            DiscrimValue annotation = member.getAnnotation(DiscrimValue.class);
            if (annotation != null) {
                TypeMirror type = valueAssignerFactory.getAssignmentType(member);
                if (!metaTypes.isAssignable(String.class, type)) {
                    processingEnv.getMessager()
                                 .printMessage(Diagnostic.Kind.ERROR, String.format(
                                         "Fields or setters annotated with @%s must be able to " + "take a String.",
                                         DiscrimValue.class.getSimpleName()), member);
                    continue;
                }

                results.add(valueAssignerFactory.getAssignmentPattern(member));
            }
        }

        return results;
    }

    public void generateParser() throws IOException {
        String parserName = MetaTypeNames.constructTypeName(classElement, GeneratedClassNames.PARSER_SUFFIX);

        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(parserName);

        JavaWriter writer = new JavaWriter(sourceFile.openWriter());
        writer.setIndent("    ");
        writer.emitPackage(processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString());
        writer.emitImports(getStandardImports());
        writer.emitEmptyLine();

        parsedClassName = writer.compressType(classElement.getQualifiedName().toString());
        String jsonObjectParserInterfaceName = JavaWriter.type(JsonObjectParser.class, parsedClassName);
        String fromMapUpdaterInterfaceName = JavaWriter.type(InstanceUpdater.class, parsedClassName);
        writer.beginType(parserName, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), null,
                         jsonObjectParserInterfaceName, fromMapUpdaterInterfaceName);
        writer.emitEmptyLine();

        writer.emitField(parserName, "INSTANCE", Modifiers.PUBLIC_CONSTANT,
                         String.format("new %s()", writer.compressType(parserName)));
        writer.emitEmptyLine();

        // Constructor
        writer.beginMethod(null, parserName, Modifiers.PRIVATE);
        writer.endMethod();
        writer.emitEmptyLine();

        initializeAssignments(writer);

        writePublicParseJsonObjectMethod(writer);
        writer.emitEmptyLine();
        writeParseFromJsonObjectMethod(writer);
        writer.emitEmptyLine();
        writeParseFromReaderMethod(writer);
        writer.emitEmptyLine();
        writeUpdateFromMapMethod(writer);
        writer.emitEmptyLine();
        writeGetFieldMethod(writer);
        writer.emitEmptyLine();
        writeInitializeAndGetFieldMethod(writer);
        writer.emitEmptyLine();
        writeDoInitializeAndGetFieldMethod(writer);

        if (!postCreateChildMethods.isEmpty()) {
            // TODO: Only write the methods that we need.
            // TODO: Tell block writer whether to write the map or collection parts.
            writer.emitEmptyLine();
            postCreateChildBlockWriter.writePostCreateChildMethod(writer);
            writer.emitEmptyLine();
            postCreateChildBlockWriter.writePostCreateCollectionMethod(writer);
            writer.emitEmptyLine();
            postCreateChildBlockWriter.writePostCreateMapMethod(writer);
        }

        writer.endType();
        writer.close();
    }

    private Set<String> getStandardImports() {
        Set<String> results = new HashSet<>();
        results.add(AndroidNames.JSON_ARRAY_FULL);
        results.add(AndroidNames.JSON_OBJECT_FULL);
        results.add(AndroidNames.JSON_READER_FULL);
        results.add(ContextHolder.class.getCanonicalName());
        results.add(JsonObjectParser.class.getCanonicalName());
        results.add(JsonParserContext.class.getCanonicalName());
        results.add(JsonParserUtils.class.getCanonicalName());
        results.add(IOException.class.getCanonicalName());
        results.add(Map.class.getCanonicalName());
        results.add(MapValueGetter.class.getCanonicalName());
        return results;
    }

    private void writePublicParseJsonObjectMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.beginMethod(parsedClassName, "parseJsonObject", Modifiers.PUBLIC,
                           CollectionUtils.newArrayList(AndroidNames.JSON_OBJECT, "jsonObject",
                                                        AndroidNames.JSON_READER, "reader", "String",
                                                        "discriminationName", "String", "discriminationValue"),
                           CollectionUtils.newArrayList(IOException.class.getSimpleName()));

        writer.emitField(parsedClassName, "result", Modifiers.NONE, "new " + parsedClassName + "()");
        if (!discriminationValueAssignmentPatterns.isEmpty()) {
            writer.beginControlFlow("if (discriminationValue != null)");
            for (String assignmentPattern : discriminationValueAssignmentPatterns) {
                writer.emitStatement(assignmentPattern, "result", "discriminationValue");
            }
            writer.endControlFlow();
        }

        String selfValuesMapName = null;
        if (selfValueAssignmentInfo != null) {
            Element member = selfValueAssignmentInfo.member;
            String initializer = null;
            selfValuesMapName = member.getSimpleName().toString() + SELF_VALUE_NAME_SUFFIX;

            try {
                initializer = initializers.findMapInitializer((DeclaredType) member.asType());
            } catch (InvalidTypeException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), member);
            }

            writer.emitField(member.asType().toString(), selfValuesMapName, Modifiers.NONE, initializer);

        }
        writer.beginControlFlow("if (jsonObject != null)");
        if (selfValueAssignmentInfo != null) {
            writer.emitStatement("parseFromJsonObject(result, jsonObject, discriminationName, %s)", selfValuesMapName);
        } else {
            writer.emitStatement("parseFromJsonObject(result, jsonObject, discriminationName)");
        }
        writer.endControlFlow();
        writer.beginControlFlow("if (reader != null)");
        if (selfValueAssignmentInfo != null) {
            writer.emitStatement("parseFromReader(result, reader, discriminationName, %s)", selfValuesMapName);
        } else {
            writer.emitStatement("parseFromReader(result, reader, discriminationName)");
        }
        writer.endControlFlow();

        if (selfValueAssignmentInfo != null) {
            writer.emitStatement(selfValueAssignmentInfo.assignmentPattern, "result", selfValuesMapName);
        }

        writer.emitStatement("return result");
        writer.endMethod();
    }

    private void writeParseFromJsonObjectMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation(SuppressWarnings.class, JavaWriter.stringLiteral("rawtypes"));
        List<String> parameters = CollectionUtils.newArrayList(parsedClassName, "out", AndroidNames.JSON_OBJECT,
                                                               "jsonObject", "String", "discriminationName");
        if (selfValueAssignmentInfo != null) {
            parameters.add("java.util.Map<String, Object>");
            parameters.add("selfValuesMap");
        }

        writer.beginMethod("void", "parseFromJsonObject", Modifiers.PRIVATE, parameters,
                           CollectionUtils.newArrayList(IOException.class.getSimpleName()));

        for (AssignmentInfo assignmentInfo : assignments) {
            writer.beginControlFlow("if (jsonObject.has(\"%s\"))", assignmentInfo.name);
            assignmentInfo.assigner.writeFromJsonObjectAssignment(writer, "out", "jsonObject", assignmentInfo.name);
            writer.emitStatement("jsonObject.remove(\"%s\")", assignmentInfo.name);
            writer.endControlFlow();
        }

        if (!discriminationValueAssignmentPatterns.isEmpty()) {
            writer.beginControlFlow("if (jsonObject.has(discriminationName))");
            writer.emitField("String", "discriminationValue", Modifiers.FINAL,
                             "jsonObject.optString(discriminationName)");
            writer.emitStatement("jsonObject.remove(discriminationName)");
            for (String assignmentPattern : discriminationValueAssignmentPatterns) {
                writer.emitStatement(assignmentPattern, "out", "discriminationValue");
            }
            writer.endControlFlow();
        }

        if (selfValueAssignmentInfo != null) {
            writer.emitAnnotation(SuppressWarnings.class, JavaWriter.stringLiteral("unchecked"));
            writer.emitField(JavaWriter.type(Iterator.class, "String"), "keys", Modifiers.NONE, "jsonObject.keys()");
            writer.beginControlFlow("while (keys.hasNext())");
            writer.emitField("String", "key", Modifiers.NONE, "keys.next()");
            if (selfValueAssignmentInfo.convertJsonTypes) {
                writer.emitStatement(
                        "selfValuesMap.put(key, JsonParserUtils.getAndConvertValue(jsonObject, " + "key))");
            } else {
                writer.emitStatement("selfValuesMap.put(key, jsonObject.opt(key))");
            }
            writer.endControlFlow();
        }
        writer.endMethod();
    }

    private void writeUpdateFromMapMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.emitAnnotation(SuppressWarnings.class, JavaWriter.stringLiteral("rawtypes"));
        List<String> parameters = CollectionUtils.newArrayList(parsedClassName, "instance",
                                                               JavaWriter.type(Map.class, "String", "Object"), "map",
                                                               JsonParserContext.class.getSimpleName(), "context");

        writer.beginMethod("void", "updateInstanceFromMap", Modifiers.PUBLIC, parameters, null);

        writeUpdateAssignmentsBlock(writer);
        if (selfValueAssignmentInfo != null) {
            writeUpdateSelfValuesBlock(writer);
        }
        writer.endMethod();
    }

    private void writeUpdateAssignmentsBlock(JavaWriter writer) throws IOException {
        for (AssignmentInfo assignmentInfo : assignments) {
            writer.beginControlFlow("if (map.containsKey(\"%s\"))", assignmentInfo.name);
            assignmentInfo.assigner.writeFromMapAssignment(writer, "instance", "map", assignmentInfo.name);
            writer.emitStatement("map.remove(\"%s\")", assignmentInfo.name);
            writer.endControlFlow();
        }
    }

    private void writeUpdateSelfValuesBlock(final JavaWriter writer) throws IOException {
        if (selfValueAssignmentInfo.convertJsonTypes) {
            ErrorWriter.surroundWithIoTryCatch(writer, new ErrorWriter.ContentWriter() {
                @Override
                public void writeContent() throws IOException {
                    writer.emitStatement("map = JsonParserUtils.convertMapValues(map, context)");
                }
            });
        }

        if (selfValueAssignmentInfo.member instanceof ExecutableElement) {
            writer.emitStatement(selfValueAssignmentInfo.assignmentPattern, "instance", "map");
        } else {
            Name fieldName = selfValueAssignmentInfo.member.getSimpleName();
            writer.beginControlFlow("if (instance.%s == null)", fieldName);

            String mapInitializer = null;
            try {
                mapInitializer = initializers.findMapInitializer(
                        (DeclaredType) selfValueAssignmentInfo.member.asType());
            } catch (InvalidTypeException e) {
                processingEnv.getMessager()
                             .printMessage(Diagnostic.Kind.ERROR, e.getMessage(), selfValueAssignmentInfo.member);
            }
            writer.emitStatement("instance.%s = %s", fieldName, mapInitializer);
            writer.endControlFlow();
            writer.emitStatement("instance.%s.putAll(map)", fieldName);
        }
    }

    private void writeGetFieldMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.beginMethod("Object", "getField", Modifiers.PUBLIC, parsedClassName, "instance", "String", "name");
        writer.emitStatement("return doInitializeAndGetField(instance, name, false)");
        writer.endMethod();
    }

    private void writeInitializeAndGetFieldMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.beginMethod("Object", "initializeAndGetField", Modifiers.PUBLIC, parsedClassName, "instance", "String",
                           "name");
        writer.emitStatement("return doInitializeAndGetField(instance, name, true)");
        writer.endMethod();
    }

    private void writeDoInitializeAndGetFieldMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("Object", "doInitializeAndGetField", Modifiers.PRIVATE, parsedClassName, "instance",
                           "String", "name", "boolean", "initializeCollections");
        writer.beginControlFlow("switch (name)");
        for (AssignmentInfo assignmentInfo : assignments) {
            if (assignmentInfo.member instanceof VariableElement) {
                writeInitializeAndGetBlock(writer, (VariableElement) assignmentInfo.member, assignmentInfo.name);
            }
        }
        writer.beginControlFlow("default:");
        writer.emitStatement("return null");
        writer.endControlFlow();
        writer.endControlFlow();
        writer.endMethod();
    }

    private void writeInitializeAndGetBlock(JavaWriter writer, VariableElement field, String name) throws IOException {
        writer.beginControlFlow("case (\"%s\"):", name);
        if (metaTypes.isSubtypeErasure(field.asType(), Collection.class) || metaTypes.isSubtypeErasure(field.asType(),
                                                                                                       Map.class)) {
            writer.beginControlFlow("if (initializeCollections && instance.%s == null)", field.getSimpleName());
            writeInitializeField(writer, field);
            writer.endControlFlow();
        }
        writer.emitStatement("return instance.%s", field.getSimpleName());
        writer.endControlFlow();
    }

    private void writeInitializeField(JavaWriter writer, VariableElement field) throws IOException {
        String initialization = null;
        DeclaredType type = (DeclaredType) field.asType();
        try {
            if (metaTypes.isSubtypeErasure(type, Collection.class)) {
                initialization = initializers.findCollectionInitializer(type);
            } else if (metaTypes.isSubtypeErasure(type, Map.class)) {
                initialization = initializers.findMapInitializer(type);
            } else {
                throw new IllegalArgumentException("Expected field to be of type Collection or Map, but found " + type);
            }
        } catch (InvalidTypeException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), field);
        }
        writer.emitStatement("instance.%s = %s", field.getSimpleName(), initialization);
    }

    private void writeParseFromReaderMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation(SuppressWarnings.class, "{ \"rawtypes\", \"unchecked\"}");
        ArrayList<String> parameters = CollectionUtils.newArrayList(parsedClassName, "out", AndroidNames.JSON_READER,
                                                                    "reader", "String", "discriminationName");

        if (selfValueAssignmentInfo != null) {
            parameters.add("java.util.Map<String, Object>");
            parameters.add("selfValuesMap");
        }

        writer.beginMethod("void", "parseFromReader", Modifiers.PRIVATE, parameters,
                           CollectionUtils.newArrayList(IOException.class.getSimpleName()));

        writer.beginControlFlow("while (reader.hasNext())");
        writer.emitField(String.class.getSimpleName(), "name", Modifiers.NONE, "reader.nextName()");

        if (!discriminationValueAssignmentPatterns.isEmpty()) {
            writer.beginControlFlow("if (discriminationName.equals(name) && !JsonParserUtils.handleNull(reader))");
            writer.emitField("String", "discriminationValue", Modifiers.FINAL,
                             "JsonParserUtils.nextString(reader, discriminationName)");
            for (String assignmentPattern : discriminationValueAssignmentPatterns) {
                writer.emitStatement(assignmentPattern, "out", "discriminationValue");
            }
            writer.emitStatement("continue");
            writer.endControlFlow();
        }

        writer.beginControlFlow("switch (name)");

        for (AssignmentInfo assignmentInfo : assignments) {
            writer.beginControlFlow("case \"%s\":", assignmentInfo.name);
            assignmentInfo.assigner.writeFromReaderAssignment(writer, "out", "reader", assignmentInfo.name);
            writer.emitStatement("break");
            writer.endControlFlow();
        }
        writer.beginControlFlow("default:");
        if (selfValueAssignmentInfo != null) {
            writer.emitStatement("selfValuesMap.put(name, JsonParserUtils.parseNextValue(reader, %s))",
                                 selfValueAssignmentInfo.convertJsonTypes);
        } else {
            writer.emitStatement("reader.skipValue()");
        }
        writer.endControlFlow();

        writer.endControlFlow();
        writer.endControlFlow();
        writer.endMethod();
    }

    private List<AssignmentInfo> getAssignments(ValueAssignerFactory valueAssignerFactory,
                                                List<? extends Element> allMembers) {
        Map<String, List<MemberInfo>> keyToMemberMap = groupMembersByKey(allMembers);
        return createAssignerListFromMembers(valueAssignerFactory, keyToMemberMap);
    }

    private List<ExecutableElement> getPostCreateChildMethods(List<? extends Element> allMembers) {
        List<ExecutableElement> results = new ArrayList<>();
        for (Element element : allMembers) {
            if (element instanceof ExecutableElement && element.getAnnotation(JsonPostCreateChild.class) != null) {
                ExecutableElement method = (ExecutableElement) element;
                if (CodeAnalysisUtils.isPrivate(method)) {
                    processingEnv.getMessager()
                                 .printMessage(Diagnostic.Kind.ERROR,
                                               String.format("Methods annotated with @%s must be " + "non-private.",
                                                             JsonPostCreateChild.class.getSimpleName()), method);
                    continue;
                }
                if (method.getParameters().size() != 1 || !metaTypes.isSameType(method.getParameters().get(0).asType(),
                                                                                Object.class)) {
                    processingEnv.getMessager()
                                 .printMessage(Diagnostic.Kind.ERROR, String.format(
                                         "Methods annotated with @%s must take exactly one "
                                                 + "parameter of type Object.",
                                         JsonPostCreateChild.class.getSimpleName()));
                    continue;
                }
                results.add(method);
            }
        }
        return results;
    }

    private SelfValueAssignmentInfo getSelfValueAssignmentInfo(ValueAssignerFactory valueAssignerFactory,
                                                               List<? extends Element> allMembers) {
        final List<SelfValueAssignmentInfo> assignments = new ArrayList<>();
        for (Element member : allMembers) {
            final JsonSelfValues annotation = member.getAnnotation(JsonSelfValues.class);
            if (annotation != null) {
                final DeclaredType assignmentType = (DeclaredType) valueAssignerFactory.getAssignmentType(member);

                final boolean isMap = metaTypes.isSubtypeErasure(assignmentType, Map.class);

                final List<? extends TypeMirror> typeArguments = assignmentType.getTypeArguments();
                final boolean isStringToObject = typeArguments.size() == 2 && metaTypes.isString(typeArguments.get(0))
                        && metaTypes.isSameType(typeArguments.get(1), Object.class);

                if (!isMap || !isStringToObject) {
                    final String message = String.format(Locale.US, "Methods and fields annotated with @%s "
                                                                 + "must take an object " + "that implements "
                            + "Map<String, " + "Object>.",
                                                         JsonSelfValues.class.getSimpleName());
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, member);
                }

                final String assignmentPattern = valueAssignerFactory.getAssignmentPattern(member);
                assignments.add(new SelfValueAssignmentInfo(member, assignmentPattern, annotation.convertJsonTypes()));
            }
        }

        if (assignments.size() > 1) {
            StringBuilder message = new StringBuilder().append(
                    "Only one field or one method is allowed to be annotated with @")
                                                       .append(JsonSelfValues.class.getSimpleName())
                                                       .append(". The following are all annotated" + " with @")
                                                       .append(JsonSelfValues.class.getSimpleName())
                                                       .append(".\n");
            for (SelfValueAssignmentInfo assignment : assignments) {
                Element member = assignment.member;
                TypeElement enclosingElement = (TypeElement) member.getEnclosingElement();
                message.append("    ")
                       .append(enclosingElement.getQualifiedName())
                       .append(".")
                       .append(member.getSimpleName())
                       .append("\n");
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, classElement);
        }

        return assignments.isEmpty() ? null : assignments.get(0);
    }

    private Map<String, List<MemberInfo>> groupMembersByKey(List<? extends Element> allMembers) {
        Map<String, List<MemberInfo>> keyToMemberMap = new LinkedHashMap<>();
        for (Element member : allMembers) {
            JsonValue annotation = member.getAnnotation(JsonValue.class);
            if (annotation != null) {
                MemberInfo memberInfo = createMemberInfo(member);

                String[] names = annotation.value();

                Collection<String> duplicates = findDuplicates(names);
                if (!duplicates.isEmpty()) {
                    processingEnv.getMessager()
                                 .printMessage(Diagnostic.Kind.ERROR,
                                               "Duplicate names found in @JsonValue: " + collectionContentsToString(
                                                       duplicates, STRING_TO_STRING_FUNCTION) + ".", member);
                    continue;
                }

                for (String name : names) {
                    if (StringUtils.isBlank(name)) {
                        processingEnv.getMessager()
                                     .printMessage(Diagnostic.Kind.ERROR, "Name cannot be blank.", member);
                    }

                    putInList(keyToMemberMap, name, memberInfo, MemberInfo.COMPARATOR);
                }
            }
        }
        return keyToMemberMap;
    }

    /*
     * This method handles several errors associated with multiple members mapping to the same key.
     *      - If there is only one candidate member for a key, use that member.
     *      - If multiple members in the final level (the last class in the hierarchy that
     *      contains the member) map
     *        to the same name, generate an error.
     *      - If a member in an earlier level (a super class) is found with the same name,
     *        and the member in the later level does not indicate an override, generate an error.
     *      - If a member in an earlier level (a super class) is found with the same name,
     *        but the member in the later level indicates an override, use the member in the
     *        later level.
     */
    private List<AssignmentInfo> createAssignerListFromMembers(ValueAssignerFactory valueAssignerFactory,
                                                               Map<String, List<MemberInfo>> keyToMemberMap) {
        List<AssignmentInfo> assignerList = new ArrayList<>();
        for (Map.Entry<String, List<MemberInfo>> entry : keyToMemberMap.entrySet()) {
            Element memberToUse;
            List<MemberInfo> candidates = entry.getValue();
            int size = candidates.size();

            if (size == 1) {
                final MemberInfo memberInfo = candidates.get(0);
                memberToUse = memberInfo.member;
                if (memberInfo.override) {
                    final String msg = "Member does not override a mapping in a super class. Check the key "
                            + "or remove `override = true`.";
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, memberToUse);
                }
            } else {
                MemberInfo lastMember = candidates.get(size - 1);
                MemberInfo secondToLastMember = candidates.get(size - 2);
                if (lastMember.hierarchicalPosition > secondToLastMember.hierarchicalPosition && lastMember.override) {
                    memberToUse = lastMember.member;
                } else {
                    memberToUse = null;
                    generateErrorForInvalidMemberList(entry.getKey(), candidates);
                }
            }
            if (memberToUse != null) {
                ValueAssigner valueAssigner = valueAssignerFactory.createValueAssigner(memberToUse);
                assignerList.add(new AssignmentInfo(memberToUse, valueAssigner, entry.getKey()));
            }
        }
        return assignerList;
    }

    private void generateErrorForInvalidMemberList(String name, List<MemberInfo> members) {
        final int size = members.size();
        final int finalLevel = members.get(size - 1).hierarchicalPosition;

        Collection<MemberInfo> membersInFinalLevel = getMembersAtLevel(members, finalLevel);

        if (membersInFinalLevel.size() > 1) {
            String duplicatesString = collectionContentsToString(membersInFinalLevel, memberInfoToStringFunction);
            String message = String.format(Locale.US, "The following all tried to map to the name \"%s\", "
                                                   + "however only one field or one setter within a class is allowed "
                    + "to" + " map to a given name: %s.",
                                           name, duplicatesString);

            for (MemberInfo memberInfo : membersInFinalLevel) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, memberInfo.member);
            }
        } else {
            final int previousLevel = members.get(size - 2).hierarchicalPosition;
            Collection<MemberInfo> membersInPreviousLevel = getMembersAtLevel(members, previousLevel);

            Element superClass = elementHierarchy.get(previousLevel);
            String tooManyInPreviousLevelMessage = "";
            if (membersInPreviousLevel.size() > 1) {
                tooManyInPreviousLevelMessage = String.format(Locale.US,
                                                              "Note that more than one field and / or setter in %s maps"
                                                                      + " to \"%s\", which is an error, but it will be "
                                                                      + "ignored. You should probably fix it anyway "
                                                                      + "though. ", superClass.getSimpleName(), name);
            }

            String membersInPreviousLevelString = collectionContentsToString(membersInPreviousLevel,
                                                                             memberInfoToStringFunction);

            Element offendingMember = members.get(size - 1).member;
            String message = String.format(Locale.US, "The following elements in the super class %s also map"
                                                   + " to the name \"%s\": %s. %sIf you wish to " + "override the "
                    + "elements in the super class, "
                                                   + "then add the phrase \"override = true\" to " + "the @JsonValue "
                    + "annotation on this element.",
                                           superClass.getSimpleName(), name, membersInPreviousLevelString,
                                           tooManyInPreviousLevelMessage);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, offendingMember);

        }

    }

    private Collection<MemberInfo> getMembersAtLevel(List<MemberInfo> members, int level) {
        Collection<MemberInfo> membersInFinalLevel = new ArrayList<>();
        for (int position = members.size() - 1; position >= 0; position--) {
            MemberInfo member = members.get(position);
            if (member.hierarchicalPosition == level) {
                membersInFinalLevel.add(member);
            }
        }
        return membersInFinalLevel;
    }

    private <T> String collectionContentsToString(Iterable<T> collection, ToStringFunction<T> toStringFunction) {
        StringBuilder builder = new StringBuilder();
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            builder.append(toStringFunction.toString(iterator.next()));
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private Collection<String> findDuplicates(String[] collection) {
        Set<String> uniques = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (String item : collection) {
            if (!uniques.add(item)) {
                duplicates.add(item);
            }
        }
        return duplicates;
    }

    private MemberInfo createMemberInfo(Element member) {
        Element enclosingElement = member.getEnclosingElement();
        int hierarchicalPosition = elementHierarchy.indexOf(enclosingElement);
        if (hierarchicalPosition < 0) {
            processingEnv.getMessager()
                         .printMessage(Diagnostic.Kind.ERROR,
                                       "Member's enclosing element (" + enclosingElement.getSimpleName()
                                               + ") not found in the element hierarchy. " + elementHierarchy, member);
        }
        boolean override = member.getAnnotation(JsonValue.class).override();
        return new MemberInfo(member, hierarchicalPosition, override);
    }

    private <K, V> void putInList(Map<K, List<V>> map, K key, V value, Comparator<? super V> comparator) {
        List<V> list = map.get(key);
        if (list == null) {
            list = new LinkedList<>();
            map.put(key, list);
        }
        int insertionPoint = -(Collections.binarySearch(list, value, comparator) + 1);
        if (insertionPoint >= 0) {
            // The item is not already in the list, so add it.
            list.add(insertionPoint, value);
        }
    }

    private static final ToStringFunction<String> STRING_TO_STRING_FUNCTION = new ToStringFunction<String>() {
        @Override
        public String toString(String object) {
            return object;
        }
    };

    private final ToStringFunction<MemberInfo> memberInfoToStringFunction = new ToStringFunction<MemberInfo>() {
        @Override
        public String toString(MemberInfo object) {
            return String.format(Locale.US, "%s.%s", classElement.getQualifiedName(), object.member.getSimpleName());
        }
    };

    static class MemberInfo {

        static final int HIERARCHY_MULTIPLIER = 100000;
        static final int KIND_MULTIPLIER = 1000;

        // Warning: Do not use with a sorted set or map, because this is not consistent with
        // .equals().
        //
        // We want members to be sorted by hierarchical position (that is, which class they're in
        // – a field in a
        // super class has a lower hierarchical position than one in a subclass). Beyond that we
        // don't care,
        // but we don't want two members in the same hierarchical level to be treated as "equal",
        // because then we won't realize that two members are trying to map to the same name. So
        // we also include name
        // and kind in the comparison. The multipliers are there so that no matter what the
        // comparison contribution
        // of kind and name are, hierarchical position will always win (unless they differ in
        // name length by more
        // than 50000 characters, which I think is unlikely).
        public static final Comparator<MemberInfo> COMPARATOR = new Comparator<MemberInfo>() {
            @Override
            public int compare(MemberInfo o1, MemberInfo o2) {
                int hierarchicalComparison = HIERARCHY_MULTIPLIER * (o1.hierarchicalPosition - o2.hierarchicalPosition);
                int kindComparison = KIND_MULTIPLIER * o1.member.getKind().compareTo(o2.member.getKind());
                int nameComparison = o1.member.getSimpleName()
                                              .toString()
                                              .compareTo(o2.member.getSimpleName().toString());
                return kindComparison + hierarchicalComparison + nameComparison;
            }
        };

        public final Element member;
        public final int hierarchicalPosition;
        public final boolean override;

        public MemberInfo(Element member, int hierarchicalPosition, boolean override) {
            this.member = member;
            this.hierarchicalPosition = hierarchicalPosition;
            this.override = override;
        }
    }

    private static class AssignmentInfo {

        public final Element member;
        public final ValueAssigner assigner;
        public final String name;

        private AssignmentInfo(Element member, ValueAssigner assigner, String name) {
            this.member = member;
            this.assigner = assigner;
            this.name = name;
        }

    }

    private static class SelfValueAssignmentInfo {

        public final Element member;
        public final String assignmentPattern;
        public final boolean convertJsonTypes;

        public SelfValueAssignmentInfo(Element member, String assignmentPattern, boolean convertJsonTypes) {
            this.member = member;
            this.assignmentPattern = assignmentPattern;
            this.convertJsonTypes = convertJsonTypes;
        }
    }

    private interface ToStringFunction<T> {

        String toString(T object);
    }

}
