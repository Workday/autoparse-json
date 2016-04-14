/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.context.JsonParserSettingsBuilder;
import com.workday.autoparse.json.demo.duplicatepartition.DuplicatePartitionedModel;
import com.workday.autoparse.json.demo.other.OtherPackageModel;
import com.workday.autoparse.json.demo.other.OtherPackageModel$$JsonObjectParser;
import com.workday.autoparse.json.demo.partition.PartitionedModel;
import com.workday.autoparse.json.demo.partition.PartitionedModel$$JsonObjectParser;
import com.workday.autoparse.json.updater.InstanceUpdaterTable;
import com.workday.autoparse.json.updater.InstanceUpdaters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author nathan.taylor
 * @since 2015-08-21.
 */
@RunWith(JUnit4.class)
public class InstanceUpdatersTableTest {

    private String defaultPackageName;
    private InstanceUpdaterTable defaultUpdaterTable;

    @Before
    public void setUp() throws Exception {
        defaultPackageName = JsonParserSettingsBuilder.DEFAULT_INSTANCE_UPDATER_PACKAGE;
        defaultUpdaterTable =
                InstanceUpdaters.getInstanceUpdaterTableForPackage(defaultPackageName);
    }

    @Test
    public void testCustomParserNotInstanceUpdaterIsNotInMap() {
        String packageName = JsonParserSettingsBuilder.DEFAULT_INSTANCE_UPDATER_PACKAGE;
        assertNull(InstanceUpdaters.getInstanceUpdaterTableForPackage(packageName)
                                   .getInstanceUpdaterForClass(ParserAnnotatedObject.class));
    }

    @Test
    public void testCustomInstanceUpdaterParserIsInMap() {

        InstanceUpdaterParserAnnotatedObjectParser expectedParser =
                InstanceUpdaterParserAnnotatedObjectParser.INSTANCE;

        assertEquals(expectedParser,
                     defaultUpdaterTable.getInstanceUpdaterForClass(
                             InstanceUpdaterParserAnnotatedObject.class));
    }

    @Test
    public void testCustomInstanceUpdaterParserNonStandardPartitionIsInMap() {

        InstanceUpdaterParserAnnotatedObjectParser expectedParser =
                InstanceUpdaterParserAnnotatedObjectParser.INSTANCE;

        assertEquals(expectedParser,
                     defaultUpdaterTable.getInstanceUpdaterForClass(
                             InstanceUpdaterParserAnnotatedObject.class));
    }

    @Test
    public void testCustomInstanceUpdaterParserNonStandardPartitionIsNotInMap() {
        String packageName = PartitionedModel.class.getPackage().getName();
        InstanceUpdaterTable table =
                InstanceUpdaters.getInstanceUpdaterTableForPackage(packageName);

        assertNull(table.getInstanceUpdaterForClass(InstanceUpdaterParserAnnotatedObject.class));
    }

    @Test
    public void testGeneratedParserAddedToMap() {
        assertEquals(TestObject$$JsonObjectParser.INSTANCE,
                     defaultUpdaterTable.getInstanceUpdaterForClass(TestObject.class));
    }

    @Test
    public void testParserFromAnotherPackageInSamePartitionAddedToMap() {
        OtherPackageModel$$JsonObjectParser expectedParser =
                OtherPackageModel$$JsonObjectParser.INSTANCE;

        assertEquals(expectedParser,
                     defaultUpdaterTable.getInstanceUpdaterForClass(OtherPackageModel.class));
    }

    @Test
    public void testParserFromNestedPartitionAddedToMap() {
        String packageName = PartitionedModel.class.getPackage().getName();
        InstanceUpdaterTable table =
                InstanceUpdaters.getInstanceUpdaterTableForPackage(packageName);
        PartitionedModel$$JsonObjectParser expectedParser =
                PartitionedModel$$JsonObjectParser.INSTANCE;
        assertEquals(expectedParser, table.getInstanceUpdaterForClass(PartitionedModel.class));
    }

    @Test
    public void testNotFoundInWrongPartition() {
        assertNull(defaultUpdaterTable.getInstanceUpdaterForClass(DuplicatePartitionedModel.class));
    }
}
