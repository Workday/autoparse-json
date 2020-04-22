/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.workday.autoparse.json.annotations.DiscrimValue;
import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonPostCreateChild;
import com.workday.autoparse.json.annotations.JsonSelfValues;
import com.workday.autoparse.json.annotations.JsonValue;
import com.workday.autoparse.json.context.GeneratedClassNames;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.NoJsonObjectParser;
import com.workday.meta.AnnotationUtils;
import com.workday.meta.MetaTypeNames;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

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

            if (isCustomParser(customParserCanonicalName)) {
                classNameToParserNameMap.put(classQualifiedName, customParserCanonicalName);
            } else {
                String parserQualifiedName = classQualifiedName + GeneratedClassNames.PARSER_SUFFIX;
                classNameToParserNameMap.put(classQualifiedName, parserQualifiedName);
                classesRequiringGeneratedParsers.add((TypeElement) element);
            }
        }

        for (TypeElement classElement : classesRequiringGeneratedParsers) {
            generateClassParser(classElement, classNameToParserNameMap);
        }

        return true;
    }

    private boolean isCustomParser(String parserName) {
        return !NoJsonObjectParser.class.getCanonicalName().equals(parserName);
    }

    private void generateClassParser(TypeElement classElement, Map<String, String> classNameToParserNameMap) {
        try {
            new JsonObjectParserGenerator(processingEnv, classElement, classNameToParserNameMap).generateParser();
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), classElement);
        }

    }
}
