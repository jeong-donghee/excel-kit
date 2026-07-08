package io.github.jeongdonghee.excelkit.canvas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 요소(행)별 색을 <b>데이터로</b> 지정하는 필드. 값은 hex 문자열({@code "#4E79A7"} 또는 {@code "4E79A7"}).
 *
 * <p>색이 고정 상수가 아니라 데이터(사용자가 고른 색, DB 값 등)일 때 쓴다. 파이/도넛의 슬라이스별 색,
 * 단일 시리즈 막대의 막대별 색에 적용된다. 값이 비었거나 필드가 없으면 기본 팔레트가 자동 배정한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ChartColor {
}
