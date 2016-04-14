/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.codegen;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;

/**
 * @author travis.westbrook
 * @since 2016-01-06.
 */
public class PartitionComponentInfo {

    public final Map<String, TypeElement> discrimValueToClassRequiringGeneratedParserMap =
            new HashMap<>();
    public final Map<String, TypeElement> discrimValueToClassWithCustomParserMap = new HashMap<>();
    public final Map<String, String> codeClassNameToParserNameMap = new HashMap<>();

    public PartitionComponentInfo() {
    }
}
