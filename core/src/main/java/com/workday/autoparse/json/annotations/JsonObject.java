/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.annotations;

import com.workday.autoparse.json.context.JsonParserSettings;
import com.workday.autoparse.json.parser.JsonObjectParser;
import com.workday.autoparse.json.parser.NoJsonObjectParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates to the Autoparse JSON framework that this class can be instantiated and inflated during
 * the parsing of a JSON document. The targeted class must have a non-private, no-argument
 * constructor.
 * <p/>
 * If no arguments are provided to this annotation, then a parser will be generated for this class,
 * and a new instance of this class may be created and inflated when required by another object
 * (e.g. when this class is the type of a field for an object being parsed).
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface JsonObject {

    /**
     * This field tells Autoparse that if any of the values listed here are found paired with the
     * name corresponding to {@link JsonParserSettings#getDiscriminationName()} on an object in a
     * JSON document, then an instance of the target class should be created and inflated from that
     * JSON object.
     * <p/>
     * Note however that if the object in the JSON object is a child of another JSON object whose
     * type is known, then an object of the type of that field will be created instead.
     * <p/>
     * For example, suppose that there are three objects in your project, Parent, ChildA, and ChildB
     * defined as such:
     * <pre>
     *     {@literal@}JsonObject("parent")
     *     public class Parent {
     *         {@literal@}JsonValue("myChildA") ChildA myChildA;
     *     }
     *
     *     {@literal@}JsonObject("childA")
     *     public class ChildA {  }
     *
     *     {@literal@}JsonObject("childB")
     *     public class ChildB {  }
     * </pre>
     * <p/>
     * And suppose that we have the following JSON document to parse, where the discrimination key
     * is "key".
     * <pre>
     *     {
     *         "key":"parent",
     *         "myChildA":{
     *             "key":"childB"
     *         }
     *     }
     * </pre>
     * In this case, the top most object has a discrimination value of "parent" which corresponds to
     * the Parent object, so Autoparse will create an instance of Parent as the root object. The
     * child object has a field name of "myChildA". Autoparse knows that anything corresponding to
     * "myChildA" must be a ChildA object, so Autoparse creates a ChildA object for that object in
     * the JSON document, even though the discrimination value for that object tells Autoparse to
     * create an instance of ChildB. Thus the values in this annotation are only used if there are
     * no other hints about what type the object to be parsed should be.
     * <p/>
     * Because of this, you are not required to provide a value here. Placing this annotation on a
     * class will merely register the class with Autoparse as a known type that can be instantiated
     * when required.
     */
    String[] value() default {};

    /**
     * You may optionally supply your own {@link JsonObjectParser} to parse this class. If a class
     * is supplied here, then Autoparse will not generate a parser for this object, but will instead
     * use an instance of the class supplied to perform the parsing when an object of this type is
     * needed.
     * <p/>
     * <b>NOTE: </b> Any class provided here <b>must</b> have a {@code public static} field named
     * {@code INSTANCE}, which Autoparse will use as the instance of this parser.
     */
    Class<? extends JsonObjectParser<?>> parser() default NoJsonObjectParser.class;
}
