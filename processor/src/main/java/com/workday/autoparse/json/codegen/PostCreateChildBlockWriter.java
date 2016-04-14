/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;
import com.workday.autoparse.json.annotations.JsonPostCreateChild;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Handles writing the code that calls methods annotated with {@literal@}{@link
 * JsonPostCreateChild}.
 *
 * @author nathan.taylor
 * @since 2015-03-12
 */
interface PostCreateChildBlockWriter {

    /**
     * Write the code that calls the JsonPostCreateChild methods for a singular child.
     *
     * @param writer The JavaWriter to use.
     * @param parent The variable representing the parent.
     * @param child The variable representing the child.
     */
    void writePostCreateChildBlock(JavaWriter writer, String parent, String child)
            throws IOException;

    /**
     * Write the code that calls the JsonPostCreateChild methods for a {@link Collection}.
     *
     * @param writer The JavaWriter to use.
     * @param parent The variable representing the parent.
     * @param collection The variable representing the Collection.
     */
    void writePostCreateCollectionBlock(JavaWriter writer, String parent, String collection)
            throws IOException;

    /**
     * Write the code that calls the JsonPostCreateChild methods for a {@link Map}.
     *
     * @param writer The JavaWriter to use.
     * @param parent The variable representing the parent.
     * @param map The variable representing the Map.
     */
    void writePostCreateMapBlock(JavaWriter writer, String parent, String map) throws IOException;

    /**
     * Write a method that may be called recursively that will call JsonPostCreateChild methods for
     * a singular child.
     *
     * @param writer The JavaWriter to use.
     */
    void writePostCreateChildMethod(JavaWriter writer) throws IOException;

    /**
     * Write a method that may be called recursively that will call JsonPostCreateChild methods for
     * a {@link Collection}.
     *
     * @param writer The JavaWriter to use.
     */
    void writePostCreateCollectionMethod(JavaWriter writer) throws IOException;

    /**
     * Write a method that may be called recursively that will call JsonPostCreateChild methods for
     * a {@link Map}.
     *
     * @param writer The JavaWriter to use.
     */
    void writePostCreateMapMethod(JavaWriter writer) throws IOException;
}
