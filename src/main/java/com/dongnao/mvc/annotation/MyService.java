package com.dongnao.mvc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyService {

    /**
     * 默认值
     * @return
     */
    String value() default "";
}
