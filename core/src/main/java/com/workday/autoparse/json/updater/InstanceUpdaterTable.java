/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.updater;

/**
 * @author nathan.taylor
 * @since 2015-08-21.
 */
public interface InstanceUpdaterTable {

    <T> InstanceUpdater<T> getInstanceUpdaterForClass(Class<T> clazz);
}
