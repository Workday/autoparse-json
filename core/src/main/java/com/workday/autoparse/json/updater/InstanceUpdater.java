/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.updater;

import com.workday.autoparse.json.annotations.JsonValue;
import com.workday.autoparse.json.context.JsonParserContext;

import java.util.Collection;
import java.util.Map;

/**
 * Given a custom object of type {code T} that can be created by parsing a json object, the class
 * implementing this interface should be able to update the fields of that object with new values
 * provided in a {@link Map}. The keys of the Map will correspond to the keys in the json object.
 * The process should be nearly identical to parsing a json document.
 *
 * @author nathan.taylor
 * @since 2015-08-13.
 */
public interface InstanceUpdater<T> {

    /**
     * Update {@code instance} so that the fields are set with new values from the {@link Map}. The
     * map should be treated as if it were a json object that is being parsed to create an object of
     * type {@code T}.
     *
     * @param instance The object to update.
     * @param map The Map from which to get the updated values.
     */
    void updateInstanceFromMap(T instance, Map<String, Object> map, JsonParserContext context);

    /**
     * Get the value of a field by the name it corresponds to when parsing json.
     * <p/>
     * Note that for generated implementations of this interface, the mapping is done according to
     * {@link JsonValue} annotations; however, the generated implementation will not be able to
     * locate fields corresponding to setters annotated with JsonValue.
     *
     * @param instance The object from which to get the field.
     * @param name The name for which to find the corresponding field.
     *
     * @return The value of the field, or null if there is no matching field.
     */
    Object getField(T instance, String name);

    /**
     * Get the value of a field by the name it corresponds to when parsing json. If the field takes
     * a {@link Collection} or {@link Map} and the value is currently null, initialize the field
     * before returning it.
     * <p/>
     * Note that for generated implementations of this interface, the mapping is done according to
     * {@link JsonValue} annotations; however, the generated implementation will not be able to
     * locate fields corresponding to setters annotated with JsonValue.
     *
     * @param instance The object from which to get the field.
     * @param name The name for which to find the corresponding field.
     *
     * @return The value of the field, or null if there is no matching field.
     */
    Object initializeAndGetField(T instance, String name);
}
