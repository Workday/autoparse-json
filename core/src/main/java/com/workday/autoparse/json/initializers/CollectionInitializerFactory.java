/*
 * Copyright 2016 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.json.initializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author nathan.taylor
 * @since 2014-10-09
 */
public final class CollectionInitializerFactory {

    /**
     * Creates a {@link CollectionInitializer} appropriate for the class provided. A default
     * concrete implementation of a Collection will be assumed for non-concrete types.
     */
    public static CollectionInitializer getCollectionInitializerForClass(
            // Type is not required here. The caller will ensure that only the correct types of
            // items are added.
            @SuppressWarnings("rawtypes") Class<? extends Collection> clazz) {
        if (Collection.class.equals(clazz)
                || List.class.equals(clazz)
                || ArrayList.class.equals(clazz)) {
            return ArrayListInitializer.INSTANCE;
        } else if (LinkedList.class.equals(clazz)) {
            return LinkedListInitializer.INSTANCE;
        } else if (Set.class.equals(clazz) || HashSet.class.equals(clazz)) {
            return HashSetInitializer.INSTANCE;
        } else if (LinkedHashSet.class.equals(clazz)) {
            return LinkedHashSetInitializer.INSTANCE;
        } else {
            throw new IllegalArgumentException("Cannot initialize collection of type "
                                                       + clazz.getCanonicalName());
        }
    }

    public static class ArrayListInitializer implements CollectionInitializer {

        public static final ArrayListInitializer INSTANCE = new ArrayListInitializer();

        private ArrayListInitializer() {
        }

        @Override
        public Collection<?> newInstance() {
            return new ArrayList<>();
        }
    }

    public static class LinkedListInitializer implements CollectionInitializer {

        public static final LinkedListInitializer INSTANCE = new LinkedListInitializer();

        private LinkedListInitializer() {
        }

        @Override
        public Collection<?> newInstance() {
            return new LinkedList<>();
        }
    }

    public static class HashSetInitializer implements CollectionInitializer {

        public static final HashSetInitializer INSTANCE = new HashSetInitializer();

        private HashSetInitializer() {
        }

        @Override
        public Collection<?> newInstance() {
            return new HashSet<>();
        }
    }

    public static class LinkedHashSetInitializer implements CollectionInitializer {

        public static final LinkedHashSetInitializer INSTANCE = new LinkedHashSetInitializer();

        private LinkedHashSetInitializer() {
        }

        @Override
        public Collection<?> newInstance() {
            return new LinkedHashSet<>();
        }
    }

    private CollectionInitializerFactory() {
    }

}
