/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.annotations;

import com.workday.autoparse.json.context.JsonParserSettingsBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

/**
 * This annotation may be placed on a field or setter that takes a value of type {@link Map}<{@link
 * String}, {@link Object}> to indicate to the Autoparse framework that all values not assigned to
 * fields or setters with {@link JsonValue} annotations should be put into the target Map.
 * <p/>
 * This can be useful for instance in your unknown object (see {@link
 * JsonParserSettingsBuilder#withUnknownObjectClass(Class)}), so that you can inspect all the
 * children of that object.
 * <p/>
 * Values that have already been assigned to fields or setters annotated with {@literal@}JsonValue
 * will not make it into this Map, nor will the discrimination value. The Map will only ever contain
 * the "leftovers".
 * <p/>
 * The target field or setter must take an object that implements Map{@literal<}String, Object>
 * exactly. If it does not, a compilation error will be generated.
 *
 * @author nathan.taylor
 * @since 2015-04-14.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonSelfValues {

    /**
     * You may specify whether or not you would like objects and arrays in the json document to be
     * converted to known types and {@link Collection}s.
     * <p/>
     * If set to {@code true} (the default), objects will be converted via the normal means by first
     * checking the discrimination value, then converting to the unknown object type if one is
     * configured, or if none of the above work, a {@link JSONObject} or {@link JSONArray} will be
     * put into the Map.
     * <p/>
     * If set to {@code false}, conversions are skipped entirely and a JSONObject or JSONArray is
     * put into the Map directly.
     */
    boolean convertJsonTypes() default true;
}
