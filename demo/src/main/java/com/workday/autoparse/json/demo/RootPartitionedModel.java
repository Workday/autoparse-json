/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonValue;
import com.workday.autoparse.json.demo.partition.PartitionedModel;
import com.workday.autoparse.json.demo.partition2.PartitionedModel2;

/**
 * @author travis.westbrook
 * @since 2016-01-06.
 */
@JsonObject("rootPartitionedModel")
public class RootPartitionedModel {

    @JsonValue("partitionedModel")
    public PartitionedModel partitionedModel;

    @JsonValue("thisPackageModel")
    public ThisPackageModel thisPackageModel;

    @JsonValue("partitionedModel2")
    public PartitionedModel2 partitionedModel2;

}
