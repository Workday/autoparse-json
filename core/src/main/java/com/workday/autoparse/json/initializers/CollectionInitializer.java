/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.initializers;

import java.util.Collection;

/**
 * A simple object that creates a new instance of a particular {@link Collection}.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public interface CollectionInitializer {

    /**
     * Create a new instance of the particular {@link Collection}.
     */
    Object newInstance();
}
