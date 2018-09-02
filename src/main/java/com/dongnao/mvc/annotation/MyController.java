package com.dongnao.mvc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyController {

    /**
     * 默认值
     * @return
     */
    String value() default "";
}
