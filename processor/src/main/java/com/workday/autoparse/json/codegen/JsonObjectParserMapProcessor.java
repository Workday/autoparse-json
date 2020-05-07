package com.workday.autoparse.json.codegen;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonParserPartition;
import com.workday.autoparse.json.annotations.codegen.JsonParser;
import com.workday.autoparse.json.context.GeneratedClassNames;
import com.workday.autoparse.json.parser.NoJsonObjectParser;
import com.workday.autoparse.json.utils.CollectionUtils;
import com.workday.meta.AnnotationUtils;
import com.workday.meta.MetaTypeNames;
import com.workday.meta.PackageTree;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

public class JsonObjectParserMapProcessor extends AbstractProcessor {

    private Map<PackageElement, PartitionComponentInfo> partitionComponents = new HashMap<>();

    private Set<PackageElement> partitionPackageElements = new HashSet<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return CollectionUtils.newHashSet(
                JsonParserPartition.class.getCanonicalName(),
                JsonObject.class.getCanonicalName(),
                JsonParser.class.getCanonicalName()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return false;
        }

        final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(JsonObject.class);
        if (elements.isEmpty()) {
            return false;
        }

        Set<PackageElement> partitionPackageElementsInRound =
                ElementFilter.packagesIn(roundEnvironment.getElementsAnnotatedWith(JsonParserPartition.class));
        if (!partitionPackageElementsInRound.isEmpty()) {
            partitionPackageElements.addAll(partitionPackageElementsInRound);
        }

        PackageTree packageTree = new PackageTree(processingEnv.getElementUtils(), partitionPackageElements);

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
                partitionComponentInfo.codeClassNameToParserNameMap.put(codeClassQualifiedName,
                        customParserCanonicalName);
            } else {
                addElementToMap((TypeElement) element,
                        partitionComponentInfo.discrimValueToClassRequiringGeneratedParserMap);
                String parserQualifiedName = classQualifiedName + GeneratedClassNames.PARSER_SUFFIX;
                partitionComponentInfo.codeClassNameToParserNameMap.put(codeClassQualifiedName, parserQualifiedName);

            }
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
