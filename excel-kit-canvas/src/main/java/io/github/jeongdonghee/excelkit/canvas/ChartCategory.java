package io.github.jeongdonghee.excelkit.canvas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 카테고리(막대/선/영역/레이더의 x축 라벨, 파이의 슬라이스 라벨) 필드. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ChartCategory {
}
