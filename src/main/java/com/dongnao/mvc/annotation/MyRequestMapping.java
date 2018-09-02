package com.dongnao.mvc.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRequestMapping {

    /**
     * 默认值
     * @return
     */
    String value() default "";
}
