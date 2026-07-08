package io.github.jeongdonghee.excelkit.canvas;

/**
 * 지원하는 2D 차트 종류.
 *
 * <ul>
 *   <li>카테고리형: {@link #BAR}, {@link #LINE}, {@link #AREA}, {@link #RADAR}
 *       — {@code @ChartCategory} + 1개 이상의 {@code @ChartSeries}</li>
 *   <li>파이형: {@link #PIE}, {@link #DOUGHNUT}
 *       — {@code @ChartCategory} + {@code @ChartSeries} 1개</li>
 *   <li>XY형: {@link #SCATTER} — {@code @ChartX} + 1개 이상의 {@code @ChartY}</li>
 * </ul>
 */
public enum ChartType {
    BAR, LINE, AREA, RADAR, PIE, DOUGHNUT, SCATTER;

    boolean isCategory() {
        return this == BAR || this == LINE || this == AREA || this == RADAR;
    }

    boolean isPie() {
        return this == PIE || this == DOUGHNUT;
    }

    boolean isScatter() {
        return this == SCATTER;
    }
}
