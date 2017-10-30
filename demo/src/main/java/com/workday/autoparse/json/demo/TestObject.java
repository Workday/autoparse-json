/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.demo;

import com.workday.autoparse.json.annotations.DiscrimValue;
import com.workday.autoparse.json.annotations.JsonObject;
import com.workday.autoparse.json.annotations.JsonPostCreateChild;
import com.workday.autoparse.json.annotations.JsonValue;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonObject(value = {"testObject", "testObject2"})
public class TestObject extends AbstractTestObject {

    @DiscrimValue
    public String discriminationValue;

    // Primitives
    @JsonValue("myBoolean")
    public boolean myBoolean;
    @JsonValue("myByte")
    public byte myByte;
    @JsonValue("myChar")
    public char myChar;
    @JsonValue("myDouble")
    public double myDouble;
    @JsonValue("myFloat")
    public float myFloat;
    @JsonValue("myInt")
    public int myInt;
    @JsonValue("myLong")
    public long myLong;
    @JsonValue("myShort")
    public short myShort;

    // Boxed Primitives
    @JsonValue("myBoxedBoolean")
    public Boolean myBoxedBoolean;
    @JsonValue("myBoxedByte")
    public Byte myBoxedByte;
    @JsonValue("myBoxedChar")
    public Character myBoxedChar;
    @JsonValue("myBoxedDouble")
    public Double myBoxedDouble;
    @JsonValue("myBoxedFloat")
    public Float myBoxedFloat;
    @JsonValue("myBoxedInt")
    public Integer myBoxedInt;
    @JsonValue("myBoxedLong")
    public Long myBoxedLong;
    @JsonValue("myBoxedShort")
    public Short myBoxedShort;

    // Special Objects
    @JsonValue({"myString", "myString2"})
    public String myString = "default";
    @JsonValue("myBigDecimal")
    public BigDecimal myBigDecimal;
    @JsonValue("myBigInteger")
    public BigInteger myBigInteger;
    @JsonValue(value = "overriddenThing", override = true)
    public int overriddenThing;

    // Maps
    @JsonValue("myStringMap")
    public Map<String, String> myStringMap;
    @JsonValue("myTestObjectMap")
    public HashMap<String, SimpleTestObject> myTestObjectMap;
    @JsonValue("myInterfaceMap")
    public LinkedHashMap<String, TestObjectInterface> myInterfaceMap;
    @JsonValue("myObjectMap")
    public Map<String, Object> myObjectMap;

    // Collections
    @JsonValue("myBooleanCollection")
    public Set<Boolean> myBooleanCollection;
    @JsonValue("myByteCollection")
    public HashSet<Byte> myByteCollection;
    @JsonValue("myCharCollection")
    public LinkedHashSet<Character> myCharCollection;
    @JsonValue("myDoubleCollection")
    public LinkedList<Double> myDoubleCollection;
    @JsonValue("myFloatCollection")
    public Collection<Float> myFloatCollection;
    @JsonValue("myIntCollection")
    public Collection<Integer> myIntCollection;
    @JsonValue("myLongCollection")
    public Collection<Long> myLongCollection;
    @JsonValue("myShortCollection")
    public Collection<Short> myShortCollection;

    @JsonValue("myStringCollection")
    public ArrayList<String> myStringCollection;
    @JsonValue("myBigDecimalCollection")
    public Collection<BigDecimal> myBigDecimalCollection;
    @JsonValue("myBigIntegerCollection")
    public Collection<BigInteger> myBigIntegerCollection;

    @JsonValue("myCollectionOfCollections")
    public List<List<Integer>> myCollectionOfCollections;

    // Custom Objects
    @JsonValue("mySingularChild")
    public SimpleTestObject mySingularChild;

    @JsonValue("mySingularChildByInterface")
    public TestObjectInterface mySingularChildByInterface;

    @JsonValue("myInnerObject")
    public InnerTestObject myInnerObject;

    @JsonValue("myList")
    public List<SimpleTestObject> myList;

    @JsonValue("myListByInterface")
    public List<TestObjectInterface> myListByInterface;

    @JsonValue("myCollectionOfSetsOfTestObjects")
    public List<Set<SimpleTestObject>> mySetsOfTestObjects;

    @JsonValue(value = "myUnannotatedObject", parser = UnannotatedObjectParser.class)
    public UnannotatedObject myUnannotatedObject;

    @JsonValue(value = "myUnannotatedObjectCollection", parser = UnannotatedObjectParser.class)
    public Collection<UnannotatedObject> myUnannotatedObjectCollection;

    @JsonValue("myJsonObject")
    public JSONObject myJsonObject;

    @JsonValue("myJsonArray")
    public JSONArray myJsonArray;

    @JsonValue("myJsonObjectCollection")
    public List<JSONObject> myJsonObjectCollection;

    @JsonValue("myEmptyObject")
    public SimpleTestObject myEmptyObject;

    @JsonValue("myEmptyCollection")
    public Collection<String> myEmptyCollection;

    @JsonValue("myNullInt")
    public int myNullInt = 1;

    @JsonValue("myNullString")
    public String myNullString;

    @JsonValue("myNullObject")
    public TestObject myNullTestObject;

    @JsonValue("myNullCollection")
    public Collection<String> myNullCollection;
    @JsonValue("myDefaultCollection")
    public Collection<String> myDefaultCollection = Collections.singleton("the one");
    @JsonValue("myCollectionWithSingleNullValue")
    public Collection<String> myCollectionWithSingleNullValue;
    @JsonValue("myCollectionWithNullValues")
    public Collection<String> myCollectionWithNullValues;

    // Fields for setters
    public String stringFromSetter;
    public UnannotatedObject unannotatedObjectFromSetter;
    public List<Set<Integer>> integerCollectionsFromSetter;
    public Collection<ParserAnnotatedObject> testObjectCollectionFromSetter;

    public TestObject() {
    }

    public TestObject(String myString) {
        this.myString = myString;
    }

    public TestObject(String myString, String discriminationValue) {
        this.myString = myString;
        this.discriminationValue = discriminationValue;
        this.superDiscriminationValue = discriminationValue;
    }

    // Setters

    @JsonValue("stringSetter")
    public void setString(String string) {
        stringFromSetter = string;
    }

    @JsonValue(value = "unannotatedObjectSetter", parser = UnannotatedObjectParser.class)
    public void setUnannotatedObject(UnannotatedObject unannotatedObject) {
        unannotatedObjectFromSetter = unannotatedObject;
    }

    @JsonValue("collectionSetter")
    public void setCollection(Collection<ParserAnnotatedObject> testObjectCollection) {
        this.testObjectCollectionFromSetter = testObjectCollection;
    }

    @JsonValue("collectionOfCollectionsSetter")
    public void setCollectionOfCollections(List<Set<Integer>> integerCollections) {
        this.integerCollectionsFromSetter = integerCollections;
    }

    @JsonPostCreateChild
    void onPostCreateChild(Object child) {
        if (child instanceof SimpleTestObject) {
            SimpleTestObject simpleChild = (SimpleTestObject) child;
            if (simpleChild.myString != null) {
                simpleChild.myString = "post-parse:" + simpleChild.myString;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonObject("innerTestObject")
    public static class InnerTestObject implements TestObjectInterface {

        @JsonValue("string")
        public String string;

        public InnerTestObject() {
        }

        public InnerTestObject(String string) {
            this.string = string;
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
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
}
