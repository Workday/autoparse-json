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
 * @since 2015-10-20.
 */
class ErrorWriter {

    private ErrorWriter() {
    }

    public static void writeConversionException(JavaWriter writer,
                                                Object desiredClassName,
                                                String actualObjectName)
            throws IOException {
        writer.emitStatement(
                "throw new RuntimeException(\"Could not convert to %s from \" + %s.getClass()"
                        + ".getCanonicalName())",
                desiredClassName,
                actualObjectName);
    }

    public static void surroundWithIoTryCatch(JavaWriter writer, ContentWriter contentWriter)
            throws IOException {
        writer.beginControlFlow("try");
        contentWriter.writeContent();
        writer.nextControlFlow("catch (IOException e)");
        writer.emitStatement("throw new RuntimeException(e)");
        writer.endControlFlow();
    }

    interface ContentWriter {

        void writeContent() throws IOException;
    }
}
