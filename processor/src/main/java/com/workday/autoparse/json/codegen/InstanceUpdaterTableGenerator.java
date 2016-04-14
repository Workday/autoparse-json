/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.autoparse.json.context.GeneratedClassNames;
import com.workday.autoparse.json.context.JsonParserSettingsBuilder;
import com.workday.autoparse.json.updater.InstanceUpdater;
import com.workday.autoparse.json.updater.InstanceUpdaterTable;
import com.workday.meta.MetaTypes;
import com.workday.meta.Modifiers;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * @author nathan.taylor
 * @since 2015-08-21.
 */
public class InstanceUpdaterTableGenerator {

    private static final String MAP_TYPE =
            String.format("Map<Class<?>, %s<?>>", InstanceUpdater.class.getSimpleName());

    private final ProcessingEnvironment processingEnv;
    private final Map<String, String> classNameToParserNameMap;
    private final PackageElement packageElement;
    private final MetaTypes metaTypes;

    public InstanceUpdaterTableGenerator(ProcessingEnvironment processingEnv,
                                         Map<String, String> classNameToParserNameMap,
                                         PackageElement packageElement) {
        this.processingEnv = processingEnv;
        this.classNameToParserNameMap = classNameToParserNameMap;
        this.packageElement = packageElement;
        this.metaTypes = new MetaTypes(processingEnv);
    }

    public void generateTable() throws IOException {
        String packageName = packageElement != null
                             ? packageElement.getQualifiedName().toString()
                             : JsonParserSettingsBuilder.DEFAULT_INSTANCE_UPDATER_PACKAGE;

        String qualifiedClassName = GeneratedClassNames.getQualifiedName(packageName,
                                                                         GeneratedClassNames
                                                                                 .CLASS_INSTANCE_UPDATER_TABLE);

        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qualifiedClassName);

        JavaWriter writer = new JavaWriter(sourceFile.openWriter());
        writer.emitPackage(packageName);
        writer.emitEmptyLine();
        writer.emitImports(getImports());
        writer.emitEmptyLine();

        writer.beginType(GeneratedClassNames.CLASS_INSTANCE_UPDATER_TABLE, "class",
                         EnumSet.of(Modifier.PUBLIC, Modifier.FINAL),
                         null, InstanceUpdaterTable.class.getCanonicalName());
        writer.emitEmptyLine();

        writeMapField(writer);
        writer.emitEmptyLine();
        writeGetter(writer);

        writer.endType();
        writer.close();
    }

    private Collection<String> getImports() {
        Set<String> results = new HashSet<>();
        results.add(HashMap.class.getCanonicalName());
        results.add(Map.class.getCanonicalName());
        if (packageElement != null) {
            results.add(InstanceUpdaterTable.class.getCanonicalName());
            results.add(InstanceUpdater.class.getCanonicalName());
        }
        return results;
    }

    private void writeMapField(JavaWriter writer) throws IOException {
        writer.emitField(MAP_TYPE, "MAP", Modifiers.PRIVATE_CONSTANT,
                         String.format("new HashMap<Class<?>, %s<?>>()",
                                       InstanceUpdater.class.getSimpleName()));

        writer.beginInitializer(true);

        for (Map.Entry<String, String> entry : classNameToParserNameMap.entrySet()) {
            String className = entry.getKey();
            String parserQualifiedName = entry.getValue();
            TypeElement parserType =
                    processingEnv.getElementUtils().getTypeElement(parserQualifiedName);
            if (parserType == null
                    || metaTypes.isSubtypeErasure(parserType.asType(), InstanceUpdater.class)) {
                writer.emitStatement("MAP.put(%s.class, %s.INSTANCE)",
                                     className,
                                     parserQualifiedName);
            }
        }

        writer.endInitializer();
    }

    private void writeGetter(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.emitAnnotation(SuppressWarnings.class, JavaWriter.stringLiteral("unchecked"));
        writer.beginMethod("<T>" + JavaWriter.type(InstanceUpdater.class, "T"),
                           "getInstanceUpdaterForClass",
                           Modifiers.PUBLIC,
                           "Class<T>",
                           "clazz");
        writer.emitStatement("return (InstanceUpdater<T>) MAP.get(clazz)");
        writer.endMethod();
    }

}
