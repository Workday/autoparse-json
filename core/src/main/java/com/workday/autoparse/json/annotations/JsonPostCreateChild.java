/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

/**
 * Indicates that the target method should be called when a new child has been parsed. The target
 * method must be non-private and take a single argument of type {@link Object}, which will be the
 * child object that has just been created.
 * <p/>
 * If a {@link Collection} or {@link Map} has been parsed, then each leaf item will be passed to
 * this method individually. You should only expect complex objects to be passed into this method
 * (anything that is not, for example, a String, boolean, or number).
 * <p/>
 * You may mark multiple methods with this annotation, but the order in which they are called is
 * nondeterministic, so the different methods should not be interdependent.
 *
 * @author nathan.taylor
 * @since 2015-03-12
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface JsonPostCreateChild {

}
