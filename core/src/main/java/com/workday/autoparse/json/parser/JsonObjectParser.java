/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import android.util.JsonReader;

import com.workday.autoparse.json.context.JsonParserSettings;

import org.json.JSONObject;

import java.io.IOException;

/**
 * An object that can parse a JSON object into a custom object. Note that in order to be used by
 * Autoparse, implementers must declare a {@code public static} field named {@code INSTANCE}, which
 * will be used as the instance of this parser, rather than creating a new one.
 *
 * @param <T> The type of object that is produced by this parser.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public interface JsonObjectParser<T> {

    /**
     * Produce a new instance of the custom object based on the current object being parsed by the
     * provided JsonReader and / or from the provided {@link JSONObject}.
     * <p/>
     * This method should <i>not</i> call {@link JsonReader#beginObject()} or {@link
     * JsonReader#endObject()}, as the framework will handle those calls automatically.
     * <p/>
     * Implementers should extract all values required from the provided {@link JSONObject} first
     * (if not null). Then, the implemented method should iterate through all values of the current
     * object being read by the JsonReader (if not null), saving the ones that are needed and
     * skipping ({@link JsonReader#skipValue()} the ones that are not, until {@link
     * JsonReader#hasNext()} returns {@code false}.
     *
     * @param jsonObject The object from which to extract values first. May be null.
     * @param reader The JsonReader from which to extract the values for this object after
     * extracting values from {@code jsonObject}. May be null. Implementers should iterate through
     * all values of the current object until {@link JsonReader#hasNext()} returns {@code false}.
     * @param discriminationName The key that corresponds to the discrimination value (see {@link
     * JsonParserSettings#getDiscriminationName()}).
     * @param discriminationValue The discrimination value for the object to be parsed, if it has
     * already been read. Otherwise null. If the object being parsed needs the discrimination value,
     * this parser should still look for it from {@code reader} and {@code jsonObject}.
     * <p/>
     * The discrimination name can be found using {@code ContextHolder.getContext().getSettings()
     * .getDiscriminationName()}. However, you should not cache this value, as this parser may be
     * reused with different settings. Instead, query the discrimination name every time this method
     * is called.
     *
     * @return A fully inflated object of type {@code T}.
     *
     * @throws IOException If the JsonReader throws an exception.
     */
    T parseJsonObject(JSONObject jsonObject,
                      JsonReader reader,
                      String discriminationName,
                      String discriminationValue)
            throws IOException;

}
