package io.github.jeongdonghee.excelkit.canvas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 분산형(SCATTER)의 Y 값 시리즈 필드. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ChartY {

    /** 시리즈 이름(범례 라벨). 비우면 필드 이름. */
    String name() default "";
}
