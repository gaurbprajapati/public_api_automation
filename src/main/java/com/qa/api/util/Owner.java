package com.qa.api.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the responsible engineer for a test method.
 * If missing or {@code unknown}, {@link io.rcrm.api.listeners.TestTrackingListener} assigns a fallback owner.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Owner {
    String value();
}
