/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.context.GeneratedClassNames;
import com.workday.autoparse.json.context.JsonParserSettingsBuilder;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.JsonObjectParserTable;
import com.workday.meta.AnnotationUtils;
import com.workday.meta.MetaTypeNames;
import com.workday.meta.Modifiers;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

/**
 * Generates an implementation of {@link JsonObjectParserTable}.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
class JsonObjectParserTableGenerator {

    private static final String MAP_TYPE =
            String.format("Map<String, %s<?>>", JsonObjectParser.class.getSimpleName());

    private final ProcessingEnvironment processingEnv;
    private final Map<String, TypeElement> discrimValueToClassRequiringGeneratedParserMap;
    private final Map<String, TypeElement> discrimValueToClassWithCustomParserMap;
    private final PackageElement packageElement;

    public JsonObjectParserTableGenerator(ProcessingEnvironment processingEnv,
                                          Map<String, TypeElement>
                                                  discrimValueToClassRequiringGeneratedParserMap,
                                          Map<String, TypeElement>
                                                  discrimValueToClassWithCustomParserMap,
                                          PackageElement packageElement) {
        this.processingEnv = processingEnv;
        this.discrimValueToClassRequiringGeneratedParserMap =
                discrimValueToClassRequiringGeneratedParserMap;
        this.discrimValueToClassWithCustomParserMap = discrimValueToClassWithCustomParserMap;
        this.packageElement = packageElement;
    }

    public void generateParserMap() throws IOException {

        String packageName = packageElement != null
                             ? packageElement.getQualifiedName().toString()
                             : JsonParserSettingsBuilder.DEFAULT_OBJECT_PARSER_PACKAGE;

        String qualifiedClassName = GeneratedClassNames.getQualifiedName(packageName,
                                                                         GeneratedClassNames
                                                                                 .CLASS_JSON_OBJECT_PARSER_TABLE);

        Map<String, TypeElement> parserMap = new HashMap<>();
        parserMap.putAll(discrimValueToClassRequiringGeneratedParserMap);
        parserMap.putAll(discrimValueToClassWithCustomParserMap);

        JavaFileObject sourceFile = processingEnv.getFiler()
                .createSourceFile(qualifiedClassName, parserMap.values().toArray(new Element[parserMap.size()]));

        JavaWriter writer = new JavaWriter(sourceFile.openWriter());
        writer.emitPackage(packageName);
        writer.emitEmptyLine();
        writer.emitImports(getImports());
        writer.emitEmptyLine();

        writer.beginType(GeneratedClassNames.CLASS_JSON_OBJECT_PARSER_TABLE, "class",
                         EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), null,
                         JsonObjectParserTable.class.getCanonicalName());
        writer.emitEmptyLine();

        writeMapField(writer);
        writer.emitEmptyLine();
        writeGetter(writer);
        writer.emitEmptyLine();
        writeKeySet(writer);
        writer.endType();
        writer.close();
    }

    private void writeKeySet(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.beginMethod(JavaWriter.type(Set.class, "String"), "keySet", Modifiers.PUBLIC);
        writer.emitStatement("return MAP.keySet()");
        writer.endMethod();
    }

    private Collection<String> getImports() {
        Set<String> results = new HashSet<>();
        results.add(HashMap.class.getCanonicalName());
        results.add(Map.class.getCanonicalName());
        results.add(JsonObjectParser.class.getCanonicalName());
        if (packageElement != null) {
            results.add(JsonObjectParserTable.class.getCanonicalName());
        }
        return results;
    }

    private void writeMapField(JavaWriter writer) throws IOException {
        writer.emitField(MAP_TYPE, "MAP", Modifiers.PRIVATE_CONSTANT,
                         String.format("new HashMap<String, %s<?>>()",
                                       JsonObjectParser.class.getSimpleName()));

        writer.beginInitializer(true);

        for (Map.Entry<String, TypeElement> entry :
                discrimValueToClassRequiringGeneratedParserMap.entrySet()) {
            String discriminationValue = entry.getKey();
            String parserQualifiedName = MetaTypeNames.constructTypeName(entry.getValue(),
                    GeneratedClassNames.PARSER_SUFFIX);
            writer.emitStatement("MAP.put(\"%s\", %s.INSTANCE)",
                                 discriminationValue,
                                 parserQualifiedName);
        }

        for (final Map.Entry<String, TypeElement> entry : discrimValueToClassWithCustomParserMap
                .entrySet()) {
            final TypeElement classElement = entry.getValue();
            TypeMirror parserClassMirror = AnnotationUtils.getClassTypeMirrorFromAnnotationValue(
                    new AnnotationUtils.Getter() {
                        @Override
                        public void get() {
                            classElement.getAnnotation(JsonObject.class).parser();
                        }
                    });

            String customParserCanonicalName = parserClassMirror.toString();
            writer.emitStatement("MAP.put(\"%s\", %s.INSTANCE)",
                                 entry.getKey(),
                                 customParserCanonicalName);

        }
        writer.endInitializer();
    }

    private void writeGetter(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.beginMethod(JavaWriter.type(JsonObjectParser.class, "?"),
                           "get",
                           Modifiers.PUBLIC,
                           "String",
                           "discriminationValue");
        writer.emitStatement("return MAP.get(discriminationValue)");
        writer.endMethod();
    }

}
