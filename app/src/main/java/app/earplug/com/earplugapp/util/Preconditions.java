/*
 * Copyright (C) 2017 YotaDevices, LLC - All Rights Reserved.
 *
 * Confidential and Proprietary.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package app.earplug.com.earplugapp.util;

/**
 * Optional from Java 8
 * Don't want to add Guava dependency
 */
public class Preconditions {

    /**
     * Tests whether provided reference is null.
     * If it's null {@link NullPointerException} will be thrown
     *
     * @param reference to check
     * @param msg       optional message for {@link NullPointerException}
     * @param <T>       type of reference
     * @return reference
     */
    public static <T> T checkNotNull(T reference, String msg) {
        if (reference == null) {
            throw new NullPointerException(msg);
        } else {
            return reference;
        }
    }

    /**
     * Tests whether provided reference is null.
     * If it's null {@link NullPointerException} will be thrown
     *
     * @param reference to check
     * @param <T>       type of reference
     * @return reference
     */
    public static <T> T checkNotNull(T reference) {
        return checkNotNull(reference, null);
    }


}
