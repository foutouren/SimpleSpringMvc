package com.dongnao.mvc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyQualifier {

    /**
     * 默认值
     * @return
     */
    String value() default "";
}
