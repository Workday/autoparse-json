/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.updater;

import com.workday.autoparse.json.annotations.JsonValue;
import com.workday.autoparse.json.parser.JsonObjectParser;

import static com.workday.autoparse.json.context.GeneratedClassNames.CLASS_INSTANCE_UPDATER_TABLE;
import static com.workday.autoparse.json.context.GeneratedClassNames.getQualifiedName;

/**
 * A utility class the provides access to generated InstanceUpdaters.
 *
 * @author nathan.taylor
 * @since 2015-08-21.
 */
public class InstanceUpdaters {

    private InstanceUpdaters() {
    }

    public static InstanceUpdaterTable getInstanceUpdaterTableForPackage(String packageName) {
        try {
            String tableClassName = getQualifiedName(packageName, CLASS_INSTANCE_UPDATER_TABLE);
            return (InstanceUpdaterTable) Class.forName(tableClassName).newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Get the {@link InstanceUpdater} that was generated for the class of type {@code T}. This
     * will also return the
     * {@link JsonObjectParser} specified in {@link JsonValue#parser()} if that class implements
     * InstanceUpdater.
     */
}
