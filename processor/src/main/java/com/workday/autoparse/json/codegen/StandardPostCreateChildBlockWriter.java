/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.meta.Modifiers;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;

/**
 * @author nathan.taylor
 * @since 2015-03-12
 */
class StandardPostCreateChildBlockWriter implements PostCreateChildBlockWriter {

    private final List<ExecutableElement> postCreateChildMethods;
    private final String parentType;

    public StandardPostCreateChildBlockWriter(String parentType,
                                              List<ExecutableElement> postCreateChildMethods) {
        this.postCreateChildMethods = postCreateChildMethods;
        this.parentType = parentType;
    }

    @Override
    public void writePostCreateChildBlock(JavaWriter writer, String parent, String child)
            throws IOException {
        writer.emitStatement("onPostCreateChild(%s, %s)", parent, child);
    }

    @Override
    public void writePostCreateCollectionBlock(JavaWriter writer, String parent, String collection)
            throws IOException {
        writer.emitStatement("onPostCreateCollection(%s, %s)", parent, collection);
    }

    @Override
    public void writePostCreateMapBlock(JavaWriter writer, String parent, String map)
            throws IOException {
        writer.emitStatement("onPostCreateMap(%s, %s)", parent, map);
    }

    @Override
    public void writePostCreateChildMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("void",
                           "onPostCreateChild",
                           Modifiers.PRIVATE,
                           parentType,
                           "parent",
                           "Object",
                           "child");
        for (ExecutableElement method : postCreateChildMethods) {
            writer.emitStatement("parent.%s(child)", method.getSimpleName());
        }
        writer.endMethod();
    }

    @Override
    public void writePostCreateCollectionMethod(JavaWriter writer) throws IOException {
        String collectionType = writer.compressType(JavaWriter.type(Collection.class, "?"));

        writer.beginMethod("void",
                           "onPostCreateCollection",
                           Modifiers.PRIVATE,
                           parentType,
                           "parent",
                           collectionType,
                           "collection");
        writer.beginControlFlow("for (Object o : collection)");
        writeItemSwitch(writer, "parent", "o");
        writer.endControlFlow();
        writer.endMethod();
    }

    @Override
    public void writePostCreateMapMethod(JavaWriter writer) throws IOException {
        String mapType = writer.compressType(JavaWriter.type(Map.class, "?", "?"));

        writer.beginMethod("void",
                           "onPostCreateMap",
                           Modifiers.PRIVATE,
                           parentType,
                           "parent",
                           mapType,
                           "map");
        writer.beginControlFlow("for (Object o : map.values())");
        writeItemSwitch(writer, "parent", "o");
        writer.endControlFlow();
        writer.endMethod();

    }

    private void writeItemSwitch(JavaWriter writer, String parent, String item) throws IOException {
        String mapType = writer.compressType(JavaWriter.type(Map.class, "?", "?"));
        String collectionType = writer.compressType(JavaWriter.type(Collection.class, "?"));

        writer.beginControlFlow("if (%s instanceof %s)", item, JavaWriter.rawType(collectionType));
        writer.emitStatement("onPostCreateCollection(%s, (%s) %s)", parent, collectionType, item);
        writer.nextControlFlow("else if (%s instanceof %s)", item, JavaWriter.rawType(mapType));
        writer.emitStatement("onPostCreateMap(%s, (%s) %s)", parent, mapType, item);
        writer.nextControlFlow("else");
        writer.emitStatement("onPostCreateChild(%s, %s)", parent, item);
        writer.endControlFlow();
    }
}
