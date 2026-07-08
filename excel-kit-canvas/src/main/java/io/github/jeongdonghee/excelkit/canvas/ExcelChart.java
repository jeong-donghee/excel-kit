package io.github.jeongdonghee.excelkit.canvas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 차트 '행' 데이터 클래스에 붙여, 그 리스트를 차트로 그린다. 옵션은 전부 기본값이 있어 몰라도 된다.
 *
 * <pre>{@code
 * @ExcelChart(type = ChartType.BAR, title = "월별 매출")
 * class Sales {
 *     @ChartCategory String month;
 *     @ChartSeries(name = "매출") int revenue;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelChart {

    /** 차트 종류. */
    ChartType type();

    /** 차트 제목. */
    String title() default "";

    /** 범례 위치. */
    Legend legend() default Legend.RIGHT;

    /** 막대 그룹핑(BAR에만 적용). {@code STACKED}면 누적 막대. */
    Grouping grouping() default Grouping.CLUSTERED;

    /** 카테고리/X축 제목. */
    String categoryAxisTitle() default "";

    /** 값/Y축 제목. */
    String valueAxisTitle() default "";

    /** 배치 시작 열(0-based). 음수면 자동 배치. */
    int col() default -1;

    /** 배치 시작 행(0-based). 음수면 자동 배치. */
    int row() default -1;

    /** 너비(열 수). */
    int width() default 8;

    /** 높이(행 수). */
    int height() default 15;
}
