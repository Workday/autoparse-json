/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.parser;

import android.util.JsonReader;
import android.util.JsonToken;

import com.workday.autoparse.json.context.ContextHolder;
import com.workday.autoparse.json.context.JsonParserContext;
import com.workday.autoparse.json.context.JsonParserSettings;
import com.workday.autoparse.json.initializers.CollectionInitializer;
import com.workday.autoparse.json.initializers.CollectionInitializerFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class does most of the heavy lifting in parsing a JSON document.
 * <p/>
 * This class contains a series of methods of the form {@code next[Object]}. Each of these will
 * parse the next value into one of the basic types, or return a default value for the case of
 * {@link JsonToken#NULL}. For instance, if a call to {@link #nextInt(JsonReader, String)} and the
 * next value is null, then a value of 0 will be returned. The {@code key} parameter is used to
 * generate more useful error messages in the case that the next value pointed to by the JsonReader
 * is not of the correct type.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public class JsonParserUtils {

    private JsonParserUtils() {
    }

    /**
     * Determines what the next value is and returns it as the appropriate basic type or a custom
     * object, a {@link Collection}, or a {@link JSONObject}. If the next object is a {@link
     * JSONArray}, it will be converted to Collection.
     *
     * @param reader The JsonReader to use. The next token ({@link JsonReader#peek()} must be a
     * value.
     *
     * @return The next value. If the next value is {@link JsonToken#NULL}, then {@code null} is
     * returned.
     */
    public static Object parseNextValue(JsonReader reader) throws IOException {
        return parseNextValue(reader, true);
    }

    /**
     * Determines what the next value is and returns it as the appropriate basic type or a custom
     * object, a collection, a {@link JSONObject}, or {@link JSONArray}.
     *
     * @param reader The JsonReader to use. The next token ({@link JsonReader#peek()} must be a
     * value.
     * @param convertJsonTypes If {@code true}, and the next value is a JSONArray, it will be
     * converted to a Collection, and if the next value is a JSONObject, it will be parsed into the
     * appropriate object type. If {@code false}, a raw JSONArray or JSONObject will be returned.
     *
     * @return The next value. If the next value is {@link JsonToken#NULL}, then {@code null} is
     * returned.
     */
    public static Object parseNextValue(JsonReader reader, boolean convertJsonTypes)
            throws IOException {
        JsonToken nextToken = reader.peek();
        switch (nextToken) {
            case BEGIN_ARRAY:
                if (convertJsonTypes) {
                    Collection<Object> collection = new ArrayList<>();
                    parseJsonArray(reader, collection, null, Object.class, null, null);
                    return collection;
                } else {
                    return parseAsJsonArray(reader, null);
                }
            case BEGIN_OBJECT:
                if (convertJsonTypes) {
                    return parseJsonObject(reader, null, null, null);
                } else {
                    return parseAsJsonObject(reader, null);
                }
            case BOOLEAN:
                return reader.nextBoolean();
            case NUMBER:
            case STRING:
                return reader.nextString();
            case NULL:
                reader.nextNull();
                return null;

            default:
                throw new IllegalStateException("Unexpected token: " + nextToken);
        }
    }

    /**
     * Parse the next value as an object, but do not attempt to convert it or any children to a
     * known type. The returned object will be a {@link JSONObject} and all children will be
     * JSONObjects, JSONArrays, and primitives.
     *
     * @param reader The JsonReader to use. Calls to {@link JsonReader#beginObject()} and {@link
     * JsonReader#endObject()} will be taken care of by this method.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     */
    public static JSONObject parseAsJsonObject(JsonReader reader, String key) throws IOException {
        if (handleNull(reader)) {
            return null;
        }
        assertType(reader, key, JsonToken.BEGIN_OBJECT);
        JSONObject result = new JSONObject();
        reader.beginObject();
        while (reader.hasNext()) {
            try {
                result.put(reader.nextName(), parseNextValue(reader, false));
            } catch (JSONException e) {
                throw new RuntimeException("This should be impossible.", e);
            }
        }
        reader.endObject();
        return result;
    }

    /**
     * Parse the next value as a {@link Map}. Children will be converted to a known type. In
     * general, this method does not handle {@link Set}s as children.
     *
     * @param reader The JsonReader to use. Calls to {@link JsonReader#beginObject()} and {@link
     * JsonReader#endObject()} will be taken care of by this method.
     * @param map The Map to populate.
     * @param valueClass The type of the Map value, corresponding to V in Map{@literal<}K,
     * V{@literal>}.
     * @param parser The parser to use, or null if this method should find an appropriate one on its
     * own.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     * @param <T> The value type of the Map, corresponding to V in Map{@literal<}K, V{@literal>}.
     */
    public static <T> void parseAsMap(JsonReader reader, Map<String, T> map, Class<T> valueClass,
                                      JsonObjectParser<T> parser, String key) throws IOException {
        if (handleNull(reader)) {
            return;
        }

        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();
        assertType(reader, key, JsonToken.BEGIN_OBJECT);
        reader.beginObject();
        while (reader.hasNext()) {
            T value;
            String name = reader.nextName();
            if (parser != null) {
                reader.beginObject();
                value = parser.parseJsonObject(null, reader, discriminationName, null);
                reader.endObject();
            } else {
                Object o = parseNextValue(reader, true);
                if (!valueClass.isInstance(o)) {
                    throwMapException(name, key, valueClass, o);
                }
                value = cast(o);

            }
            map.put(name, value);
        }
        reader.endObject();
    }

    /**
     * Convert a {@link JSONObject} possibly containing child JSONObjects into a {@link Map}. Child
     * JSONObjects will be converted into known types.
     *
     * @param jsonObject The JSONObject to convert.
     * @param map The Map to populate.
     * @param valueClass The type of the Map value, corresponding to V in Map{@literal<}K,
     * V{@literal>}.
     * @param parser The parser to use, or null if this method should find an appropriate one on its
     * own.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     * @param <T> The value type of the Map, corresponding to V in Map{@literal<}K, V{@literal>}.
     */
    public static <T> void convertJsonObjectToMap(JSONObject jsonObject,
                                                  Map<String, T> map,
                                                  Class<T> valueClass,
                                                  JsonObjectParser<T> parser,
                                                  String key) throws IOException {

        final Class<?> unknownObjectClass =
                ContextHolder.getContext().getSettings().getUnknownObjectClass();
        final JsonObjectParser<?> unknownObjectParser = ContextHolder.getContext()
                                                                     .getSettings()
                                                                     .getUnknownObjectParser();
        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();
        final JsonObjectParserTable parserTable =
                ContextHolder.getContext().getJsonObjectParserTable();

        convertJsonObjectToMap(jsonObject,
                               map,
                               valueClass,
                               parser,
                               key,
                               unknownObjectClass,
                               unknownObjectParser,
                               discriminationName,
                               parserTable);
    }

    /**
     * Convert a {@link JSONObject} possibly containing child JSONObjects into a {@link Map}. Child
     * JSONObjects will be converted into known types.
     *
     * @param jsonObject The JSONObject to convert.
     * @param map The Map to populate.
     * @param valueClass The type of the Map value, corresponding to V in Map{@literal<}K,
     * V{@literal>}.
     * @param parser The parser to use, or null if this method should find an appropriate one on its
     * own.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     * @param context The context object that holds the settings and parser map to use.
     * @param <T> The value type of the Map, corresponding to V in Map{@literal<}K, V{@literal>}.
     */
    public static <T> void convertJsonObjectToMap(JSONObject jsonObject,
                                                  Map<String, T> map,
                                                  Class<T> valueClass,
                                                  JsonObjectParser<T> parser,
                                                  String key,
                                                  JsonParserContext context)
            throws IOException {
        final JsonParserSettings settings = context.getSettings();
        convertJsonObjectToMap(jsonObject,
                               map,
                               valueClass,
                               parser,
                               key,
                               settings.getUnknownObjectClass(),
                               settings.getUnknownObjectParser(),
                               settings.getDiscriminationName(),
                               context.getJsonObjectParserTable());
    }

    private static <T> void convertJsonObjectToMap(JSONObject jsonObject,
                                                   Map<String, T> map,
                                                   Class<T> valueClass,
                                                   JsonObjectParser<T> parser,
                                                   String key,
                                                   Class<?> unknownObjectClass,
                                                   JsonObjectParser<?> unknownObjectParser,
                                                   String discriminationName,
                                                   JsonObjectParserTable parserTable)
            throws IOException {

        @SuppressWarnings("unchecked")
        Iterator<String> names = jsonObject.keys();
        while (names.hasNext()) {
            T result = null;
            String name = names.next();
            Object o = jsonObject.opt(name);

            if (o instanceof JSONObject) {
                result = convertJsonObject((JSONObject) o,
                                           valueClass,
                                           parser,
                                           unknownObjectClass,
                                           unknownObjectParser,
                                           discriminationName,
                                           parserTable);
            } else if (valueClass.isInstance(o)) {
                result = cast(o);
            }

            if (result != null) {
                map.put(name, result);
            } else {
                throwMapException(name, key, valueClass, o);
            }
        }
    }

    /**
     * Get the value from a {@link JSONObject} corresponding to the provided key and convert the
     * value to a known type if possible. If there is no value mapped to the provided key, null is
     * returned.
     * <p/>
     * The usual conversion rules apply. If the value is a string or number, a string is returned.
     * If the value is a boolean, a boolean is returned. If the value is an object, an instance of
     * the known type is returned, else an instance of the unknown object type if one is configured,
     * else a JSONObject. If the value is an array, it is converted into a {@link Collection} and
     * then the conversion rules are applied recursively to the values in the array.
     *
     * @param jsonObject The JSONObject from which to pull the value.
     * @param key The key with which to query {@code jsonObject}.
     *
     * @return The value corresponding to {@code key}, converted to a know type if possible.
     */
    public static Object getAndConvertValue(JSONObject jsonObject, String key) throws IOException {
        Object o = jsonObject.opt(key);
        Object result = o;
        if (o instanceof JSONObject) {
            Object converted = convertJsonObject((JSONObject) o, Object.class, null);
            if (converted != null) {
                result = converted;
            }
        }
        return result;
    }

    /**
     * Given a map that contains various objects, some of which might be {@link JSONObject}s and
     * {@link JSONArray}s, convert those JSONObjects and JSONArrays as per {@link
     * #convertJsonObject(JSONObject, Class, JsonObjectParser)} and {@link
     * #convertArbitraryJsonArray(JSONArray)}.
     *
     * @param original The map possibly containing JSONObjects and JSONArrays.
     * @param context The context object holding the parser settings and parser table used to conver
     * the json types.
     *
     * @return A new map that contains the original mappings, except that values that were
     * JSONObjects and JSONArrays are converted.
     */
    public static Map<String, Object> convertMapValues(Map<String, Object> original,
                                                       JsonParserContext context)
            throws IOException {
        final Map<String, Object> result = new HashMap<>(original);
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            if (entry.getValue() instanceof JSONObject) {
                result.put(entry.getKey(),
                           convertJsonObject(((JSONObject) entry.getValue()),
                                             Object.class,
                                             null,
                                             context));
            } else if (entry.getValue() instanceof JSONArray) {
                result.put(entry.getKey(),
                           convertArbitraryJsonArray((JSONArray) entry.getValue(), context));
            }
        }
        return result;
    }

    public static <T> T convertJsonObject(JSONObject jsonObject,
                                          Class<T> desiredClass,
                                          JsonObjectParser<T> parser)
            throws IOException {
        final Class<?> unknownObjectClass =
                ContextHolder.getContext().getSettings().getUnknownObjectClass();
        final JsonObjectParser<?> unknownObjectParser =
                ContextHolder.getContext().getSettings().getUnknownObjectParser();
        return convertJsonObject(jsonObject,
                                 desiredClass,
                                 parser,
                                 unknownObjectClass,
                                 unknownObjectParser);
    }

    public static <T> T convertJsonObject(JSONObject jsonObject,
                                          Class<T> desiredClass,
                                          JsonObjectParser<T> parser,
                                          Class<?> unknownObjectClass,
                                          JsonObjectParser<?> unknownObjectParser)
            throws IOException {

        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();
        final JsonObjectParserTable parserTable =
                ContextHolder.getContext().getJsonObjectParserTable();
        return convertJsonObject(jsonObject,
                                 desiredClass,
                                 parser,
                                 unknownObjectClass,
                                 unknownObjectParser,
                                 discriminationName,
                                 parserTable);
    }

    public static <T> T convertJsonObject(JSONObject jsonObject,
                                          Class<T> desiredClass,
                                          JsonObjectParser<T> parser,
                                          JsonParserContext context) throws IOException {
        return convertJsonObject(jsonObject,
                                 desiredClass,
                                 parser,
                                 context.getSettings().getUnknownObjectClass(),
                                 context.getSettings().getUnknownObjectParser(),
                                 context.getSettings().getDiscriminationName(),
                                 context.getJsonObjectParserTable());
    }

    public static <T> T convertJsonObject(JSONObject jsonObject,
                                          Class<T> desiredClass,
                                          JsonObjectParser<T> parser,
                                          Class<?> unknownObjectClass,
                                          JsonObjectParser<?> unknownObjectParser,
                                          String discriminationName,
                                          JsonObjectParserTable parserTable)
            throws IOException {

        if (desiredClass.equals(JSONObject.class)) {
            return cast(jsonObject);
        }

        final String discriminationValue = getDiscriminationValue(jsonObject, discriminationName);

        if (parser != null) {
            return parser.parseJsonObject(jsonObject,
                                          null,
                                          discriminationName,
                                          discriminationValue);
        }

        final JsonObjectParser<?> parserFromDiscriminationValue =
                parserTable.get(discriminationValue);
        if (parserFromDiscriminationValue != null) {
            Object parsedObject = parserFromDiscriminationValue
                    .parseJsonObject(jsonObject, null, discriminationName, discriminationValue);
            if (desiredClass.isInstance(parsedObject)) {
                return cast(parsedObject);
            } else {
                return null;
            }
        }

        if (unknownObjectClass != null && desiredClass.isAssignableFrom(unknownObjectClass)) {
            return cast(unknownObjectParser.parseJsonObject(jsonObject,
                                                            null,
                                                            discriminationName,
                                                            discriminationValue));
        }

        return null;
    }

    /**
     * Use this method when you have already checked that the cast was safe. This is suppress the
     * "unchecked" warning.
     */
    @SuppressWarnings("unchecked")
    private static <T> T cast(Object o) {
        return (T) o;
    }

    private static void throwMapException(String name,
                                          String key,
                                          Class<?> valueClass,
                                          Object value) {
        throw new IllegalStateException(
                String.format(Locale.US,
                              "Could not convert value at \"%s\" in \"%s\" to %s from %s.",
                              name,
                              key,
                              valueClass.getCanonicalName(),
                              getClassName(value)));
    }

    private static void throwDiscriminationValueException(String discriminationName,
                                                          Object discriminationObject) {
        throw new IllegalStateException(String.format(Locale.US,
                                                      "The value corresponding to the "
                                                              + "discrimination key name "
                                                              + "(%s) must be a String, but "
                                                              + "instead found %s.",
                                                      discriminationName,
                                                      getClassName(discriminationObject)));
    }

    private static String getDiscriminationValue(JSONObject jsonObject, String discriminationName) {
        if (jsonObject == null) {
            return null;
        }

        if (jsonObject.has(discriminationName)) {
            Object discriminationObject = jsonObject.opt(discriminationName);
            if (!(discriminationObject instanceof String)) {
                throwDiscriminationValueException(discriminationName, discriminationObject);
            }
            jsonObject.remove(discriminationName);
            return (String) discriminationObject;
        }
        return null;
    }

    /**
     * Parse the next value as an object. If the next value is {@link JsonToken#NULL}, returns
     * null.
     * <p/>
     * This method will use the provide parser, or if none is provided, will attempt find an
     * appropriate parser based on the discrimination value found in the next object. If none is
     * found, then this method returns a {@link JSONObject}.
     *
     * @param reader The JsonReader to use. Calls to {@link JsonReader#beginObject()} and {@link
     * JsonReader#endObject()} will be taken care of by this method.
     * @param parser The parser to use, or null if this method should find an appropriate one on its
     * own.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     * @param expectedType The expected class of the resulting object. If the result is not an
     * instance of this class, an exception is thrown.
     *
     * @throws IllegalStateException if the resulting object is not an instance of {@code
     * expectedType}.
     */
    public static Object parseJsonObject(JsonReader reader, JsonObjectParser<?> parser, String key,
                                         Class<?> expectedType)
            throws IOException, IllegalStateException {
        if (handleNull(reader)) {
            return null;
        }
        assertType(reader, key, JsonToken.BEGIN_OBJECT);

        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();
        String discriminationValue = null;
        Object result = null;
        reader.beginObject();
        if (parser != null) {
            result = parser.parseJsonObject(null, reader, discriminationName, null);
        } else if (reader.hasNext()) {
            String firstName = reader.nextName();
            final String discriminationKeyName =
                    ContextHolder.getContext().getSettings().getDiscriminationName();
            if (discriminationKeyName.equals(firstName)) {
                discriminationValue = reader.nextString();
                parser = ContextHolder.getContext()
                                      .getJsonObjectParserTable()
                                      .get(discriminationValue);
                if (parser != null) {
                    result = parser.parseJsonObject(null,
                                                    reader,
                                                    discriminationName,
                                                    discriminationValue);
                } else {
                    result = parseSpecificJsonObjectDelayed(reader,
                                                            discriminationKeyName,
                                                            discriminationValue);
                }
            } else {
                result = parseSpecificJsonObjectDelayed(reader, firstName, null);
            }

        }
        reader.endObject();

        if (result == null) {
            result = new JSONObject();
        }

        JsonObjectParser<?> unknownObjectParser =
                ContextHolder.getContext().getSettings().getUnknownObjectParser();
        if (result instanceof JSONObject && unknownObjectParser != null) {
            result = unknownObjectParser.parseJsonObject((JSONObject) result,
                                                         null,
                                                         discriminationName,
                                                         discriminationValue);
        }

        if (expectedType != null && !(expectedType.isInstance(result))) {
            throw new IllegalStateException(
                    String.format(Locale.US,
                                  "Could not convert value at \"%s\" to %s from %s.",
                                  key,
                                  expectedType.getCanonicalName(),
                                  result.getClass().getCanonicalName()));
        }
        return result;
    }

    /**
     * Call this method when parsing an object, the parser type is unknown, and the first name
     * inside the object was the discrimination name. This method will add all values to a {@link
     * JSONObject} until the discrimination name and a matching parser are found. If no matching
     * parser is ever found, then this method returns the JSONObject.
     *
     * @param reader The reader to use.
     * @param firstName The first name parsed in this object so far. May be null, but the next toke
     * in the JsonReader should be a {@link JsonToken#NAME}.
     * @param firstValue The first value parse in this object so far. May be null, and if {@code
     * firstName} is not null, the next token in the JsonReader should be a value type.
     *
     * @return A custom object or a JSONObject if no appropriate parser was found.
     */
    private static Object parseSpecificJsonObjectDelayed(JsonReader reader,
                                                         String firstName,
                                                         Object firstValue)
            throws IOException {
        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();
        JSONObject jsonObject = new JSONObject();
        String name = firstName;
        Object value = firstValue;

        if (name == null && reader.hasNext()) {
            name = reader.nextName();
        }
        if (value == null && name != null) {
            value = parseNextValue(reader, false);
        }

        while (name != null) {
            if (discriminationName.equals(name)) {
                if (!(value instanceof String)) {
                    throwDiscriminationValueException(discriminationName, value);
                }
                final String discriminationValue = (String) value;
                JsonObjectParser<?> parser = ContextHolder.getContext()
                                                          .getJsonObjectParserTable()
                                                          .get(discriminationValue);
                if (parser != null) {
                    return parser.parseJsonObject(jsonObject,
                                                  reader,
                                                  discriminationName,
                                                  discriminationValue);
                }
            }

            // No matching parser has been found yet; save the current name and value to the
            // jsonObject.
            try {
                jsonObject.put(name, value);
            } catch (JSONException e) {
                // this should only happen if the name is null, which is impossible here.
                throw new RuntimeException("This should be impossible.", e);
            }

            if (reader.hasNext()) {
                name = reader.nextName();
                value = parseNextValue(reader, false);
            } else {
                name = null;
                value = null;
            }
        }
        return jsonObject;
    }

    /**
     * Parse the next value as an array, but do not attempt to convert it into a {@link Collection},
     * or to convert any children into known types. The returned object will be a {@link JSONArray}
     * and all children will be JSONObjects, JSONArrays, and primitives.
     *
     * @param reader The JsonReader to use. Calls to {@link JsonReader#beginArray()} and {@link
     * JsonReader#endArray()} will be taken care of by this method.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     */
    public static JSONArray parseAsJsonArray(JsonReader reader, String key) throws IOException {
        if (handleNull(reader)) {
            return null;
        }
        assertType(reader, key, JsonToken.BEGIN_ARRAY);

        JSONArray jsonArray = new JSONArray();
        reader.beginArray();
        while (reader.hasNext()) {
            jsonArray.put(parseNextValue(reader, false));
        }
        reader.endArray();
        return jsonArray;
    }

    /**
     * Parse an array that has only non-array children into a {@link Collection}.
     *
     * @param reader The reader to use, whose next token should either be {@link JsonToken#NULL} or
     * {@link JsonToken#BEGIN_ARRAY}.
     * @param collection The Collection to populate. The parametrization should match {@code
     * typeClass}.
     * @param itemParser The parser to use for items of the array. May be null.
     * @param typeClass The type of items to expect in the array. May not be null, but may be
     * Object.class.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     */
    private static <T> void parseFlatJsonArray(JsonReader reader,
                                               Collection<T> collection,
                                               JsonObjectParser<T> itemParser,
                                               Class<T> typeClass,
                                               String key)
            throws IOException {
        if (handleNull(reader)) {
            return;
        }

        Converter<T> converter = null;
        if (Converters.isConvertibleFromString(typeClass)) {
            converter = Converters.getConverter(typeClass);
        }

        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();

        reader.beginArray();
        while (reader.hasNext()) {
            Object nextValue;
            final JsonToken nextToken = reader.peek();
            if (itemParser != null && nextToken == JsonToken.BEGIN_OBJECT) {
                reader.beginObject();
                nextValue = itemParser.parseJsonObject(null, reader, discriminationName, null);
                reader.endObject();
            } else if (converter != null && (nextToken == JsonToken.NUMBER
                    || nextToken == JsonToken.STRING)) {
                nextValue = converter.convert(reader.nextString());
            } else {
                nextValue = parseNextValue(reader);
            }

            if (typeClass.isInstance(nextValue)) {
                // This is safe since we are calling class.isInstance()
                @SuppressWarnings("unchecked")
                T toAdd = (T) nextValue;
                collection.add(toAdd);
            } else if (nextToken == JsonToken.NULL) {
                collection.add(null);
            } else {
                throw new IllegalStateException(
                        String.format(Locale.US,
                                      "Could not convert value in array at \"%s\" to %s from %s.",
                                      key,
                                      typeClass.getCanonicalName(),
                                      getClassName(nextValue)));
            }
        }
        reader.endArray();
    }

    /**
     * Parse an array that may have arrays as children into a {@link Collection}.
     *
     * @param reader The reader to use, whose next token should either be {@link JsonToken#NULL} or
     * {@link JsonToken#BEGIN_ARRAY}.
     * @param collection The Collection to populate. If nested, the parametrization must match
     * {@code innerCollectionClasses} with the parametrization of the last collection matching
     * {@code itemType}. If not nested, the parametrization should match {@code itemType}.
     * @param itemParser The parser to use for the items of the most deeply nested Collections. May
     * be null.
     * @param itemType The type of the most deeply nested Collections. May not be null, but may be
     * Object.class.
     * @param innerCollectionClasses A flattened list of Collection classes that are nested within
     * {@code collection}. May be null.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     */
    // Suppress rawtypes and unchecked on Collection and operations. We are depending on
    // innerCollectionClasses to
    // provide us with the types of all nested collections, and typeClass to be the parameter of
    // the deepest
    // collection. Assuming these are correct, all other operations are safe.
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> void parseJsonArray(JsonReader reader,
                                          Collection collection,
                                          JsonObjectParser<T> itemParser,
                                          Class<T> itemType,
                                          List<Class<?>> innerCollectionClasses,
                                          String key) throws IOException {
        if (handleNull(reader)) {
            return;
        }
        assertType(reader, key, JsonToken.BEGIN_ARRAY);

        if (innerCollectionClasses != null && !innerCollectionClasses.isEmpty()) {
            CollectionInitializer nextCollectionInitializer
                    = CollectionInitializerFactory.getCollectionInitializerForClass(
                    innerCollectionClasses.get(0));
            reader.beginArray();
            while (reader.hasNext()) {
                Object nextCollection = nextCollectionInitializer.newInstance();
                if (nextCollection instanceof Collection) {
                    parseJsonArray(reader,
                            ((Collection) nextCollection),
                            itemParser,
                            itemType,
                            innerCollectionClasses.subList(1, innerCollectionClasses.size()),
                            key);
                } else if (nextCollection instanceof Map) {
                    parseCollectionMap(reader,
                            ((Map) nextCollection),
                            itemParser,
                            itemType,
                            key);
                } else {
                    throw new IllegalStateException(
                            String.format(Locale.US,
                                    "Could not convert value in array at \"%s\" to %s from %s.",
                                    key,
                                    itemType.getName(),
                                    getClassName(nextCollection)));
                }

                collection.add(nextCollection);
            }
            reader.endArray();
        } else {
            parseFlatJsonArray(reader, collection, itemParser, itemType, key);
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> void parseCollectionMap(JsonReader reader,
                                     Map map,
                                     JsonObjectParser<T> itemParser,
                                     Class<T> itemType,
                                     String key) throws IOException {
        final String discriminationName =
                ContextHolder.getContext().getSettings().getDiscriminationName();
        assertType(reader, key, JsonToken.BEGIN_OBJECT);
        reader.beginObject();
        while (reader.hasNext()) {
            T value;
            String name = reader.nextName();
            if (itemParser != null) {
                reader.beginObject();
                value = itemParser.parseJsonObject(null, reader, discriminationName, null);
                reader.endObject();
            } else {
                Object o = parseNextValue(reader, true);
                if (!itemType.isInstance(o)) {
                    throwMapException(name, key, itemType, o);
                }
                value = cast(o);

            }
            map.put(name, value);
        }
        reader.endObject();
    }

    /**
     * Convert a {@link JSONArray}, possibly containing child JSONArrays, into a {@link Collection}
     * with the same level of nesting.
     *
     * @param jsonArray The array to convert.
     * @param collection The Collection to populate. If nested, the parametrization must match
     * {@code innerCollectionClasses} with the parametrization of the last collection matching
     * {@code itemType}. If not nested, the parametrization should match {@code itemType}.
     * @param itemParser The parser to use for the items of the most deeply nested Collections. May
     * be null.
     * @param itemType The type of the most deeply nested Collections. May not be null, but may be
     * Object.class.
     * @param innerCollectionClasses A flattened list of Collection classes that are nested within
     * {@code collection}. May be null.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     */
    // Suppress rawtypes and unchecked on Collection and operations. We are depending on
    // innerCollectionClasses to
    // provide us with the types of all nested collections, and itemType to be the parameter of
    // the deepest
    // collection. Assuming these are correct, all other operations are safe.
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> void convertJsonArrayToCollection(JSONArray jsonArray,
                                                        Collection collection,
                                                        JsonObjectParser<T> itemParser,
                                                        Class<T> itemType,
                                                        List<Class<?>> innerCollectionClasses,
                                                        String key) throws IOException {

        convertJsonArrayToCollection(jsonArray,
                                     collection,
                                     itemParser,
                                     itemType,
                                     innerCollectionClasses,
                                     key,
                                     ContextHolder.getContext());
    }

    /**
     * Convert a {@link JSONArray}, possibly containing child JSONArrays, into a {@link Collection}
     * with the same level of nesting.
     *
     * @param jsonArray The array to convert.
     * @param collection The Collection to populate. If nested, the parametrization must match
     * {@code innerCollectionClasses} with the parametrization of the last collection matching
     * {@code itemType}. If not nested, the parametrization should match {@code itemType}.
     * @param itemParser The parser to use for the items of the most deeply nested Collections. May
     * be null.
     * @param itemType The type of the most deeply nested Collections. May not be null, but may be
     * Object.class.
     * @param innerCollectionClasses A flattened list of Collection classes that are nested within
     * {@code collection}. May be null.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     */
    // Suppress rawtypes and unchecked on Collection and operations. We are depending on
    // innerCollectionClasses to
    // provide us with the types of all nested collections, and itemType to be the parameter of
    // the deepest
    // collection. Assuming these are correct, all other operations are safe.
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> void convertJsonArrayToCollection(JSONArray jsonArray,
                                                        Object collection,
                                                        JsonObjectParser<T> itemParser,
                                                        Class<T> itemType,
                                                        List<Class<?>> innerCollectionClasses,
                                                        String key,
                                                        JsonParserContext context)
            throws IOException {

        if (innerCollectionClasses != null && !innerCollectionClasses.isEmpty()) {
            CollectionInitializer nextCollectionInitializer
                    = CollectionInitializerFactory.getCollectionInitializerForClass(
                    innerCollectionClasses.get(0));
            for (int i = 0; i < jsonArray.length(); i++) {
                Object nextCollection = nextCollectionInitializer.newInstance();
                List<Class<?>> nextInnerCollectionClasses = innerCollectionClasses.subList(1, innerCollectionClasses.size());

                if(jsonArray.optJSONArray(i) != null) {
                    convertJsonArrayToCollection(
                            jsonArray.optJSONArray(i),
                            nextCollection,
                            itemParser,
                            itemType,
                            nextInnerCollectionClasses,
                            key,
                            context);
                } else if(jsonArray.optJSONObject(i) != null) {
                    convertJsonObjectToMap(
                            jsonArray.getJSONObject(i),
                            ((Map) nextCollection),
                            itemType,
                            itemParser,
                            key
                    );
                } else {
                    throw new IllegalStateException(
                            String.format(Locale.US,
                                    "Could not convert value in array at \"%s\" to %s from %s.",
                                    key,
                                    jsonArray.opt(i).toString(),
                                    getClassName(nextCollection)));
                }
                ((Collection) collection).add(nextCollection);
            }
        } else {
                convertFlatJsonArrayToCollection(jsonArray,
                        ((Collection) collection),
                        itemParser,
                        itemType,
                        key,
                        context);
        }
    }

    /**
     * Convert a {@link JSONArray} with only non-array children into a {@link Collection}.
     *
     * @param jsonArray The array to convert.
     * @param collection The Collection to populate. The parametrization should match {@code
     * typeClass}.
     * @param itemParser The parser to use for items of the array. May be null.
     * @param typeClass The type of items to expect in the array. May not be null, but may be
     * Object.class.
     * @param key The key corresponding to the current value. This is used to make more useful error
     * messages.
     */
    private static <T> void convertFlatJsonArrayToCollection(JSONArray jsonArray,
                                                             Collection<T> collection,
                                                             JsonObjectParser<T> itemParser,
                                                             Class<T> typeClass,
                                                             String key,
                                                             JsonParserContext context)
            throws IOException {
        Converter<T> converter = null;
        if (Converters.isConvertibleFromString(typeClass)) {
            converter = Converters.getConverter(typeClass);
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            Object o = jsonArray.opt(i);
            T parsedItem = null;
            if (typeClass.isInstance(o)) {
                @SuppressWarnings("unchecked")
                T castItem = (T) o;
                parsedItem = castItem;
            } else if (o instanceof JSONObject) {
                parsedItem = convertJsonObject((JSONObject) o, typeClass, itemParser, context);
            } else if (o instanceof String && converter != null) {
                parsedItem = converter.convert((String) o);
            } else if (o == null) {
                //The parsed array has an explicit null, so add a null to the collection.
                collection.add(null);
                continue;
            }

            if (parsedItem != null) {
                collection.add(parsedItem);
            } else {
                throw new IllegalStateException(
                        String.format(Locale.US,
                                      "Could not convert value in array at \"%s\" to %s from %s.",
                                      key,
                                      typeClass.getName(),
                                      getClassName(o)));
            }
        }
    }

    /**
     * Convert a {@link JSONArray} into a {@link Collection} of known objects based on
     * discrimination value, primitives, Collections, and / or {@link JSONObject}s. The structure
     * will match the json document exactly. This method creates objects based on the following
     * rules:
     * <pre>
     *     <ul>
     *         <li>Strings and numbers are parsed into {@link String}s.</li>
     *         <li>Booleans are parsed into {@link Boolean}s.</li>
     *         <li>Arrays are parsed into Collections recursively.</li>
     *         <li>Objects are parsed into (a) known objects if the object has a known
     *             discrimination value, (b) the unknown object type if one has been set (see
     *             {@link JsonParserSettings#getUnknownObjectClass()}), or otherwise (c) a
     *             JSONObject.</li>
     *     </ul>
     * </pre>
     *
     * @param jsonArray The array to convert.
     *
     * @return A Collection of various objects.
     */
    public static Collection<Object> convertArbitraryJsonArray(JSONArray jsonArray)
            throws IOException {
        final JsonParserContext context = ContextHolder.getContext();
        final JsonParserSettings settings = context.getSettings();

        return convertArbitraryJsonArray(jsonArray,
                                         settings.getUnknownObjectClass(),
                                         settings.getUnknownObjectParser(),
                                         settings.getDiscriminationName(),
                                         context.getJsonObjectParserTable());
    }

    /**
     * Convert a {@link JSONArray} into a {@link Collection} of known objects based on
     * discrimination value, primitives, Collections, and / or {@link JSONObject}s. The structure
     * will match the json document exactly. This method creates objects based on the following
     * rules:
     * <pre>
     *     <ul>
     *         <li>Strings and numbers are parsed into {@link String}s.</li>
     *         <li>Booleans are parsed into {@link Boolean}s.</li>
     *         <li>Arrays are parsed into Collections recursively.</li>
     *         <li>Objects are parsed into (a) known objects if the object has a known
     *             discrimination value, (b) the unknown object type if one has been set (see
     *             {@link JsonParserSettings#getUnknownObjectClass()}), or otherwise (c) a
     *             JSONObject.</li>
     *     </ul>
     * </pre>
     *
     * @param jsonArray The array to convert.
     *
     * @return A Collection of various objects.
     */
    public static Collection<Object> convertArbitraryJsonArray(JSONArray jsonArray,
                                                               JsonParserContext context)
            throws IOException {
        final JsonParserSettings settings = context.getSettings();

        return convertArbitraryJsonArray(jsonArray,
                                         settings.getUnknownObjectClass(),
                                         settings.getUnknownObjectParser(),
                                         settings.getDiscriminationName(),
                                         context.getJsonObjectParserTable());
    }

    private static Collection<Object> convertArbitraryJsonArray(JSONArray jsonArray,
                                                                Class<?> unknownObjectClass,
                                                                JsonObjectParser<?>
                                                                        unknownObjectParser,
                                                                String discriminationName,
                                                                JsonObjectParserTable parserTable)
            throws IOException {
        final Collection<Object> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object child;
            Object o = jsonArray.opt(i);
            if (o instanceof JSONArray) {
                child = convertArbitraryJsonArray((JSONArray) o,
                                                  unknownObjectClass,
                                                  unknownObjectParser,
                                                  discriminationName,
                                                  parserTable);
            } else if (o instanceof JSONObject) {
                child = convertJsonObject((JSONObject) o,
                                          Object.class,
                                          null,
                                          unknownObjectClass,
                                          unknownObjectParser,
                                          discriminationName,
                                          parserTable);
                if (child == null) {
                    child = o;
                }
            } else {
                child = o;
            }

            result.add(child);
        }

        return result;
    }

    public static BigDecimal nextBigDecimal(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return BigDecimal.ZERO;
        }
        assertType(reader, name, JsonToken.NUMBER, JsonToken.STRING);
        return new BigDecimal(reader.nextString());
    }

    public static BigInteger nextBigInteger(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return BigInteger.ZERO;
        }
        assertType(reader, name, JsonToken.NUMBER, JsonToken.STRING);
        return new BigInteger(reader.nextString());
    }

    public static Boolean nextBoolean(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return false;
        }
        assertType(reader, name, JsonToken.BOOLEAN);
        return reader.nextBoolean();
    }

    public static Byte nextByte(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return 0;
        }
        assertType(reader, name, JsonToken.NUMBER, JsonToken.STRING);
        return Byte.valueOf(reader.nextString());
    }

    public static char nextChar(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return 0;
        }
        assertType(reader, name, JsonToken.STRING);
        return getCharFromString(reader.nextString());
    }

    public static Double nextDouble(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return 0d;
        }
        assertType(reader, name, JsonToken.NUMBER, JsonToken.STRING);
        return Double.valueOf(reader.nextString());
    }

    public static Float nextFloat(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return 0f;
        }
        assertType(reader, name, JsonToken.NUMBER, JsonToken.STRING);
        return Float.valueOf(reader.nextString());
    }

    public static Integer nextInt(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return 0;
        }
        assertType(reader, name, JsonToken.NUMBER, JsonToken.STRING);
        return Integer.valueOf(reader.nextString());
    }

    public static Long nextLong(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return 0L;
        }
        assertType(reader, name, JsonToken.NUMBER, JsonToken.STRING);
        return Long.valueOf(reader.nextString());
    }

    public static Short nextShort(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return 0;
        }
        assertType(reader, name, JsonToken.NUMBER, JsonToken.STRING);
        return Short.valueOf(reader.nextString());
    }

    public static String nextString(JsonReader reader, String name) throws IOException {
        if (handleNull(reader)) {
            return null;
        }
        assertType(reader, name, JsonToken.STRING, JsonToken.NUMBER, JsonToken.BOOLEAN);

        if (reader.peek() == JsonToken.BOOLEAN) {
            return String.valueOf(reader.nextBoolean());
        }

        return reader.nextString();
    }

    public static char getCharFromString(String stringValue) {
        if (stringValue != null && stringValue.length() == 1) {
            return stringValue.charAt(0);
        }
        return 0;
    }

    /**
     * If the next value is {@link JsonToken#NULL}, consume it and return {@code true}. Otherwise
     * return {@code false}.
     */
    public static boolean handleNull(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return true;
        }
        return false;
    }

    public static void assertType(JsonReader reader, String name, JsonToken... expectedTypes)
            throws IOException {
        JsonToken actualType = reader.peek();
        if (!arrayContains(expectedTypes, actualType)) {
            throw new IllegalStateException(
                    String.format(Locale.US,
                                  "Expected value of \"%s\" to be one of \"%s\" but found \"%s\".",
                                  name,
                                  Arrays.toString(expectedTypes),
                                  actualType));
        }
    }

    private static boolean arrayContains(JsonToken[] expectedTypes, JsonToken actualType) {
        for (JsonToken item : expectedTypes) {
            if (item == actualType) {
                return true;
            }
        }
        return false;
    }

    private static String getClassName(Object o) {
        if (o == null) {
            return "null";
        }
        return o.getClass().getCanonicalName();
    }

}
