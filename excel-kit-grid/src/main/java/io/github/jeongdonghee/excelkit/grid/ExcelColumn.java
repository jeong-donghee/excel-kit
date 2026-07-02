package io.github.jeongdonghee.excelkit.grid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 엑셀 컬럼으로 내보낼 필드 또는 무인자 메서드를 표시한다.
 *
 * <p>필드 하나로 떨어지지 않는 파생/조합 컬럼은 <b>무인자 메서드</b>에 붙인다.
 * <pre>{@code
 * @ExcelColumn(header = "사용자", order = 2)
 * public String userLabel() { return user + " (" + dept + ")"; }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ExcelColumn {

    /** 헤더 텍스트. 비우면 필드/메서드 이름을 사용한다. */
    String header() default "";

    /** 컬럼 순서(오름차순). 지정하지 않으면 맨 뒤로 간다. */
    int order() default Integer.MAX_VALUE;
}
