/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.workday.autoparse.json.annotations.DiscrimValue;
import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonParserPartition;
import com.workday.autoparse.json.annotations.JsonPostCreateChild;
import com.workday.autoparse.json.annotations.JsonSelfValues;
import com.workday.autoparse.json.annotations.JsonValue;
import com.workday.autoparse.json.context.GeneratedClassNames;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.NoJsonObjectParser;
import com.workday.meta.AnnotationUtils;
import com.workday.meta.MetaTypeNames;
import com.workday.meta.PackageTree;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is is the root of the code generation. It scans through every class annotated with {@link JsonObject} and
 * generates a {@link JsonObjectParser} via {@link JsonObjectParserGenerator} for each class requiring a parser (i.e. if
 * a custom parser is not declared). An actual static map from discrimination value to parser instance is then generated
 * in code by the {@link JsonObjectParserTableGenerator}.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public class AutoparseJsonProcessor extends AbstractProcessor {

    private Map<PackageElement, PartitionComponentInfo> partitionComponents = new HashMap<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(DiscrimValue.class.getCanonicalName());
        types.add(JsonObject.class.getCanonicalName());
        types.add(JsonPostCreateChild.class.getCanonicalName());
        types.add(JsonValue.class.getCanonicalName());
        types.add(JsonSelfValues.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(JsonObject.class);

        Set<PackageElement> partitionPackageElements = ElementFilter.packagesIn(
                roundEnv.getElementsAnnotatedWith(JsonParserPartition.class));

        PackageTree packageTree = new PackageTree(processingEnv.getElementUtils(), partitionPackageElements);
        final Set<TypeElement> classesRequiringGeneratedParsers = new HashSet<>();
        final Map<String, String> classNameToParserNameMap = new HashMap<>();

        for (final Element element : elements) {
            TypeMirror customParserClassMirror = AnnotationUtils.getClassTypeMirrorFromAnnotationValue(
                    new AnnotationUtils.Getter() {
                        @Override
                        public void get() {
                            element.getAnnotation(JsonObject.class).parser();
                        }
                    });

            String customParserCanonicalName = customParserClassMirror.toString();
            String classQualifiedName = MetaTypeNames.constructTypeName((TypeElement) element);
            String codeClassQualifiedName = ((TypeElement) element).getQualifiedName().toString();

            PackageElement matchingPackage = packageTree.getMatchingPackage(element);
            PartitionComponentInfo partitionComponentInfo = getPartitionComponentInfoForPackage(matchingPackage);

            if (isCustomParser(customParserCanonicalName)) {
                addElementToMap((TypeElement) element, partitionComponentInfo.discrimValueToClassWithCustomParserMap);
                classNameToParserNameMap.put(classQualifiedName, customParserCanonicalName);
                partitionComponentInfo.codeClassNameToParserNameMap.put(codeClassQualifiedName,
                                                                        customParserCanonicalName);
            } else {
                addElementToMap((TypeElement) element,
                                partitionComponentInfo.discrimValueToClassRequiringGeneratedParserMap);
                String parserQualifiedName = classQualifiedName + GeneratedClassNames.PARSER_SUFFIX;
                classNameToParserNameMap.put(classQualifiedName, parserQualifiedName);
                classesRequiringGeneratedParsers.add((TypeElement) element);
                partitionComponentInfo.codeClassNameToParserNameMap.put(codeClassQualifiedName, parserQualifiedName);

            }
        }

        for (TypeElement classElement : classesRequiringGeneratedParsers) {
            generateClassParser(classElement, classNameToParserNameMap);
        }

        generateParserMaps();
        return true;
    }

    private PartitionComponentInfo getPartitionComponentInfoForPackage(PackageElement packageElement) {
        PartitionComponentInfo partitionComponentInfo = partitionComponents.get(packageElement);
        if (partitionComponentInfo == null) {
            partitionComponentInfo = new PartitionComponentInfo();
            partitionComponents.put(packageElement, partitionComponentInfo);
        }
        return partitionComponentInfo;
    }

    private boolean isCustomParser(String parserName) {
        return !NoJsonObjectParser.class.getCanonicalName().equals(parserName);
    }

    private void addElementToMap(TypeElement element, Map<String, TypeElement> map) {
        JsonObject annotation = element.getAnnotation(JsonObject.class);
        for (String discrimValue : annotation.value()) {
            if (StringUtils.isBlank(discrimValue)) {
                processingEnv.getMessager()
                             .printMessage(Diagnostic.Kind.ERROR, "Discrimination values cannot be blank.", element);
            } else {
                TypeElement previousValue = map.put(discrimValue, element);
                if (previousValue != null) {
                    String errorMessage = String.format("%s and %s both tried to map to discrimination value \"%s\"",
                                                        element.getQualifiedName(), previousValue.getQualifiedName(),
                                                        discrimValue);
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorMessage, element);
                }
            }
        }
    }

    private void generateClassParser(TypeElement classElement, Map<String, String> classNameToParserNameMap) {
        try {
            new JsonObjectParserGenerator(processingEnv, classElement, classNameToParserNameMap).generateParser();
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), classElement);
        }

    }

    private void generateParserMaps() {
        for (Map.Entry<PackageElement, PartitionComponentInfo> packageMapEntry : partitionComponents.entrySet()) {
            final PackageElement packageElement = packageMapEntry.getKey();
            final PartitionComponentInfo partitionComponentInfo = packageMapEntry.getValue();
            try {
                new InstanceUpdaterTableGenerator(processingEnv, partitionComponentInfo.codeClassNameToParserNameMap,
                                                  packageElement).generateTable();
                new JsonObjectParserTableGenerator(processingEnv,
                                                   partitionComponentInfo
                                                           .discrimValueToClassRequiringGeneratedParserMap,
                                                   partitionComponentInfo.discrimValueToClassWithCustomParserMap,
                                                   packageElement).generateParserMap();
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
    }
}
