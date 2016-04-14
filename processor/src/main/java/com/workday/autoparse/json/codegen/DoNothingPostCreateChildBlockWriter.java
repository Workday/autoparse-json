/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

/**
 * @author nathan.taylor
 * @since 2015-03-12
 */
class DoNothingPostCreateChildBlockWriter implements PostCreateChildBlockWriter {

    public static final DoNothingPostCreateChildBlockWriter INSTANCE =
            new DoNothingPostCreateChildBlockWriter();

    private DoNothingPostCreateChildBlockWriter() {
    }

    @Override
    public void writePostCreateChildBlock(JavaWriter writer, String parent, String child)
            throws IOException {
        // do nothing
    }

    @Override
    public void writePostCreateCollectionBlock(JavaWriter writer, String parent, String collection)
            throws IOException {
        // do nothing
    }

    @Override
    public void writePostCreateMapBlock(JavaWriter writer, String parent, String map)
            throws IOException {
        // do nothing
    }

    @Override
    public void writePostCreateChildMethod(JavaWriter writer) throws IOException {
        // do nothing
    }

    @Override
    public void writePostCreateCollectionMethod(JavaWriter writer) throws IOException {
        // do nothing
    }

    @Override
    public void writePostCreateMapMethod(JavaWriter writer) throws IOException {
        // do nothing
    }
}
