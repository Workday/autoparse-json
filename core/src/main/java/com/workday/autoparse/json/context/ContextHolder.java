/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.context;

import com.workday.autoparse.json.parser.JsonParserUtils;

/**
 * A utility that will return the {@link JsonParserContext} for the current thread.
 *
 * @author nathan.taylor
 * @since 2014-10-09
 */
public class ContextHolder {

    private static ThreadLocal<JsonParserContext> context = new ThreadLocal<JsonParserContext>();

    private ContextHolder() {
    }

    /**
     * Get the Context for the current thread if one has been set or null if one has not been set.
     */
    public static JsonParserContext getContext() {
        return context.get();
    }

    /**
     * Set the Context for the current thread. Any calls to {@link JsonParserUtils} from this thread
     * will use this Context.
     */
    public static void setContext(JsonParserContext context) {
        ContextHolder.context.set(context);
    }

    /**
     * Remove the context of the current thread.
     */
    public static void removeContext() {
        context.remove();
    }
}
