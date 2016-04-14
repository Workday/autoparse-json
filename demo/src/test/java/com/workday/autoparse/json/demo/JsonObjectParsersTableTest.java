/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.context.JsonParserSettings;
import com.workday.autoparse.json.context.JsonParserSettingsBuilder;
import com.workday.autoparse.json.demo.duplicatepartition.DuplicatePartitionedModel;
import com.workday.autoparse.json.demo.partition.PartitionedModel;
import com.workday.autoparse.json.parser.JsonStreamParser;
import com.workday.autoparse.json.parser.JsonStreamParserFactory;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedInputStream;
import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author travis.westbrook
 * @since 2016-01-06.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class JsonObjectParsersTableTest {

    JsonStreamParser parser;

    @Before
    public void setUp() {
        parser = getParserForPartitions(JsonParserSettingsBuilder.DEFAULT_OBJECT_PARSER_PACKAGE);
    }

    @Test
    public void testOtherPackage() throws Exception {
        InputStream in = getInputStreamOf("other-package-input.json");
        ThisPackageModel root = (ThisPackageModel) parser.parseJsonStream(in);
        assertNotNull(root.otherPackageModel);

    }

    @Test
    public void testPartitionedPackageModelNotFoundWhenNotIncluded() throws Exception {
        InputStream in = getInputStreamOf("partitioned-model.json");
        Object result = parser.parseJsonStream(in);

        assertTrue(result instanceof JSONObject);
    }

    @Test
    public void testPartitionedPackageModelFoundWhenIncluded() throws Exception {
        JsonStreamParser parser =
                getParserForPartitions(PartitionedModel.class.getPackage().getName());
        InputStream in = getInputStreamOf("partitioned-model.json");
        PartitionedModel partitionedModel = (PartitionedModel) parser.parseJsonStream(in);

        assertEquals("a string", partitionedModel.string);
    }

    @Test
    public void testDefaultNotFoundWhenNotIncluded() throws Exception {
        JsonStreamParser parser =
                getParserForPartitions(PartitionedModel.class.getPackage().getName());
        InputStream in = getInputStreamOf("single-object.json");
        Object result = parser.parseJsonStream(in);

        assertThat(result).isInstanceOf(JSONObject.class);
    }

    @Test
    public void testMultiplePartitions() throws Exception {
        JsonStreamParser parser =
                getParserForPartitions(JsonParserSettingsBuilder.DEFAULT_OBJECT_PARSER_PACKAGE);
        InputStream in = getInputStreamOf("multiple-partitions.json");

        RootPartitionedModel rootPartitionModel = (RootPartitionedModel) parser.parseJsonStream(in);

        assertNotNull(rootPartitionModel.thisPackageModel);
        assertNotNull(rootPartitionModel.thisPackageModel.otherPackageModel);
        assertNotNull(rootPartitionModel.partitionedModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateMappingInSeparatePartitionsThrowsException() {
        getParserForPartitions(PartitionedModel.class.getPackage().getName(),
                               DuplicatePartitionedModel.class.getPackage().getName());
    }

    private InputStream getInputStreamOf(String fileName) {
        return new BufferedInputStream(JsonObjectParsersTableTest.class.getResourceAsStream
                (fileName));
    }

    private JsonStreamParser getParserForPartitions(String... partitions) {
        JsonParserSettings settings = new JsonParserSettingsBuilder().withPartitions(partitions)
                                                                     .withDiscriminationName(
                                                                             "object")
                                                                     .build();
        return JsonStreamParserFactory.newJsonStreamParser(settings);
    }

}
