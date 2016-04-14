/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.annotations;

/**
 * Indicate to Autoparse that this field (or setter) should be populated (or called) with the
 * discrimination value for this object. The field will be populated or the setter called only if
 * the discrimination value occurs in the json document. Autoparse will not attempt to pull the
 * discrimination value from the {@literal@}{@link JsonObject} annotation on the class. This
 * annotation is only valid on non-private, non-final fields and non-private, single argument
 * methods (setters) which can accept a value of type {@link String}.
 *
 * @author nathan.taylor
 * @since 2015-02-18
 */
public @interface DiscrimValue {

}
