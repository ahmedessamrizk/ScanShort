package com.demo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
// @Target → where can this annotation be used?
@Target({ElementType.METHOD, ElementType.TYPE})

// @Retention → how long does this annotation live?
// Other options:
//   RetentionPolicy.SOURCE  → only in source code, removed after compilation (like @Override)
//   RetentionPolicy.CLASS   → kept in .class file but not at runtime (rarely used)
@Retention(RetentionPolicy.RUNTIME)
//@interface → way to declare annotation
public @interface RateLimit {
    int maxAttempts() default 3;
    int windowSeconds() default 60; //max 3 attempts in 1 minute.
    int blockSeconds() default 300;
}
