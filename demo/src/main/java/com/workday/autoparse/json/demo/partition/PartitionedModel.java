/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo.partition;

import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonValue;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author travis.westbrook
 * @since 2016-01-06.
 */
@JsonObject("partitionedModel")
public class PartitionedModel {

    @JsonValue("string")
    public String string;

    public PartitionedModel() {
        // do nothing
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, 0);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
