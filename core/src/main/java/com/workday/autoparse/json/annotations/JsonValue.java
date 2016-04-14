/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.annotations;

import com.workday.autoparse.json.context.JsonParserSettingsBuilder;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.NoJsonObjectParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * Indicate to Autoparse that this field or setter corresponds to a name-value pair in a JSON
 * object. This annotation is only valid on non-private, non-final fields and non-private, single
 * argument methods (setters).
 * <p/>
 * When a matching name is found, Autoparse will attempt to coerce the value into the type required
 * by the field or setter (e.g. if a String is required, a String will be returned, if a float, a
 * float, if a custom object, that custom object, provided that Autoparse knows of an appropriate
 * parser. If the value cannot be coerced into the appropriate type, an exception will be thrown. If
 * the type is unknown to Autoparse, then Autoparse will attempt to parse the object using the
 * discrimination value. If none is found, then a {@link JSONObject} or {@link JSONArray} will be
 * created. If the resulting object cannot be converted into the type required by the field, then an
 * exception will be thrown.
 * <p/>
 * If the type of this field or the parameter of the method is a {@link Collection}, then Autoparse
 * will create an instance of the appropriate collection populate it with values from the matching
 * array in the JSON document. If the value in the JSON document is not an array, then an exception
 * will be thrown. Nested collections are allowed, so long as the structure matches the JSON
 * document.
 * <p/>
 * A value of {@code null} is ignored, meaning that if this field has a value assigned at
 * construction, then that value will not change.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonValue {

    /**
     * When the name corresponding to one of the values listed here is encountered when parsing the
     * containing object, the value will be parsed and assigned to this field or passed to this
     * setter. If two or more names provided here match name-value pairs in the JSON object, then
     * the behavior is undefined. Any of the values may be assigned.
     */
    String[] value();

    /**
     * You may optionally supply your own {@link JsonObjectParser} to parse this object. The
     * corresponding value in the JSON document must be an object. The class supplied must have a
     * {@code public static} field named {@code INSTANCE}, which Autoparse will use as the instance
     * of this parser.
     * <p/>
     * If the type of the target field or the parameter of the setter is a {@link Collection} (or
     * Collection of Collections), then the parser will be used to parse the final objects in the
     * JSON array (final meaning the deepest objects in an array of arrays).
     */
    Class<? extends JsonObjectParser<?>> parser() default NoJsonObjectParser.class;

    /**
     * If a super class also has a field or setter than maps to a name you wish to use for this
     * field or setter, you may override the {@literal@}JsonValue annotation in the super class. If
     * set to {@code true}, when the name indicated is encountered, this field or setter will be
     * invoked rather than the one in the super class.
     * <p/>
     * Note that it is an error to have more than one field or setter map to the same name. Setting
     * this value to {@code true} tells Autoparse to make an exception in the case described above.
     */
    boolean override() default false;

    /**
     * For wildcard fields and setters (i.e. those that take a value of type {@link Object}), you
     * may specify whether or not you would like objects and arrays in the json document to be
     * converted to known types and {@link Collection}s.
     * <p/>
     * If set to {@code true} (the default), objects will be converted via the normal means by first
     * checking the discrimination value, then converting to the unknown object type if one is
     * configured (see {@link JsonParserSettingsBuilder#withUnknownObjectClass(Class)}), or if none
     * of the above work, returning a {@link JSONObject} or {@link JSONArray}.
     * <p/>
     * If set to {@code false}, conversions are skipped entirely and a JSONObject or JSONArray is
     * returned directly.
     * <p/>
     * Note that setting this option to {@code false} is only legal on fields or setters that take a
     * value of type Object, JSONObject, or JSONArray. A compilation error will be generated in all
     * other cases.
     */
    boolean convertJsonTypes() default true;
}
