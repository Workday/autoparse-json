/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author nathan.taylor
 * @since 2016-04-07.
 */
public class CollectionUtils {

    /**
     * The largest power of two that can be represented as an {@code int}.
     */
    public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

    private CollectionUtils() {
    }

    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        Preconditions.checkNotNull(elements, "elements");
        ArrayList<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    @SafeVarargs
    public static <E> HashSet<E> newHashSet(E... elements) {
        Preconditions.checkNotNull(elements, "elements");
        HashSet<E> set = newHashSetWithExpectedSize(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    private static <E> HashSet<E> newHashSetWithExpectedSize(int expectedSize) {
        return new HashSet<>(mapCapacity(expectedSize));
    }

    /**
     * Returns a capacity that is sufficient to keep the map from being resized as long as it grows no larger than
     * expectedSize and the load factor is >= its default (0.75).
     */
    private static int mapCapacity(int expectedSize) {
        if (expectedSize < 3) {
            Preconditions.checkArgument(expectedSize >= 0, "Size must be nonnegative but was " + expectedSize);
            return expectedSize + 1;
        }
        if (expectedSize < MAX_POWER_OF_TWO) {
            return expectedSize + expectedSize / 3;
        }
        return Integer.MAX_VALUE; // any large value
    }

}
