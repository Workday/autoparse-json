/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.annotations;

import com.workday.autoparse.json.context.JsonParserSettingsBuilder;
import com.workday.autoparse.json.parser.JsonStreamParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a package as the root of a parser partition. All classes annotated with {@link JsonObject}
 * that exist in this package and its subpackages (excluding subpackages also annotated with
 * {@literal@}JsonParserPartition) will be included in this partition. Subpackages similarly
 * annotated will generate their own partition. All classes annotated with {@literal@}JsonObject
 * that do not have an ancestor package annotated with {@literal@}JsonParserPartition) will be
 * placed into a default partition.
 * <p/>
 * It is recommended that you place this annotation in the package-info.java file of that package,
 * like so:
 * <pre>
 *     {@literal@}JsonParserPartition
 *     package my.very.own.package;
 *
 *     import com.workday.autoparse.json.annotation.JsonParserPartition;
 * </pre>
 * <p/>
 * You have the option to partition your model classes into disjoint groups. This can be useful if
 * you have distinct groups of models you wish to create depending on the situation, and there are
 * name collisions between those groups. For example, suppose in Partition 1 you have a class that
 * maps to JsonObject "MyObject" and in Partition 2 you have another class that also maps to
 * "MyObject". The two cannot exist in the same partition, but Autoparse will allow them to exist in
 * separate partitions. You may then create separate {@link JsonStreamParser}s for these partitions.
 * See {@link JsonParserSettingsBuilder#withPartitions(String...)}.
 * <p/>
 * Another situation where this might be useful is if you have multiple projects that use
 * Autoparse's code generation. Without explicit partitions, the two projects will place all parsers
 * into two separate default partitions, which will cause a duplicate class compilation error. If
 * you create an explicit partition in at least one of those two projects, the error is avoided. If
 * you intend to use Autoparse in a library, it is recommended that you create an explicit partition
 * to avoid collisions with other projects.
 *
 * @author nathan.taylor
 * @since 2015-02-27
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface JsonParserPartition {

}
