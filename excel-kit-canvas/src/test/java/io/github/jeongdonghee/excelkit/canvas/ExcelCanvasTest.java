package io.github.jeongdonghee.excelkit.canvas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import io.github.jeongdonghee.excelkit.core.ExcelKitException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExcelCanvasTest {

    // ── 카테고리형 DTO (계열별 타입) ──
    @ExcelChart(type = ChartType.BAR, title = "바", grouping = Grouping.STACKED, legend = Legend.BOTTOM)
    static class Bar {
        @ChartCategory String month;
        @ChartSeries(name = "매출") int revenue;
        @ChartSeries(name = "비용") int cost;
        Bar(String m, int r, int c) { month = m; revenue = r; cost = c; }
    }

    @ExcelChart(type = ChartType.LINE, title = "라인")
    static class Line {
        @ChartCategory String month;
        @ChartSeries int value;
        Line(String m, int v) { month = m; value = v; }
    }

    @ExcelChart(type = ChartType.AREA)
    static class Area {
        @ChartCategory String month;
        @ChartSeries int value;
        Area(String m, int v) { month = m; value = v; }
    }

    @ExcelChart(type = ChartType.RADAR)
    static class Radar {
        @ChartCategory String axis;
        @ChartSeries int value;
        Radar(String a, int v) { axis = a; value = v; }
    }

    @ExcelChart(type = ChartType.PIE, title = "파이")
    static class Pie {
        @ChartCategory String name;
        @ChartSeries int value;
        Pie(String n, int v) { name = n; value = v; }
    }

    @ExcelChart(type = ChartType.DOUGHNUT)
    static class Dough {
        @ChartCategory String name;
        @ChartSeries int value;
        Dough(String n, int v) { name = n; value = v; }
    }

    @ExcelChart(type = ChartType.SCATTER, title = "분산")
    static class Sc {
        @ChartX double x;
        @ChartY(name = "표본") double y;
        Sc(double x, double y) { this.x = x; this.y = y; }
    }

    // ── 잘못된 DTO ──
    static class NoAnn { }

    @ExcelChart(type = ChartType.SCATTER)
    static class BadScatter {
        @ChartCategory String c;
        @ChartSeries int v;
        BadScatter() { }
    }

    @ExcelChart(type = ChartType.PIE)
    static class BadPie {
        @ChartCategory String c;
        @ChartSeries int a;
        @ChartSeries int b;
        BadPie() { }
    }

    private static XSSFWorkbook reload(byte[] bytes) throws Exception {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    private static int chartCount(XSSFWorkbook wb) {
        XSSFSheet charts = wb.getSheet("Charts");
        return charts.getDrawingPatriarch().getCharts().size();
    }

    @Test
    void allSevenChartTypesRenderIntoOneWorkbook() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCanvas.create()
                .chart(List.of(new Bar("1월", 100, 80), new Bar("2월", 150, 90)))
                .chart(List.of(new Line("1월", 100), new Line("2월", 150)))
                .chart(List.of(new Area("1월", 100), new Area("2월", 150)))
                .chart(List.of(new Radar("A", 3), new Radar("B", 5), new Radar("C", 4)))
                .chart(List.of(new Pie("A", 30), new Pie("B", 70)))
                .chart(List.of(new Dough("A", 30), new Dough("B", 70)))
                .chart(List.of(new Sc(1, 2), new Sc(2, 4), new Sc(3, 5)))
                .writeTo(out);

        try (XSSFWorkbook wb = reload(out.toByteArray())) {
            assertEquals(7, chartCount(wb), "7종 차트가 모두 생성되어야 한다");
            assertNotNull(wb.getSheet("_data"));
        }
    }

    @Test
    void categoryDataIsWrittenToHiddenSheet() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCanvas.create()
                .chart(List.of(new Bar("1월", 100, 80), new Bar("2월", 150, 90)))
                .writeTo(out);

        try (XSSFWorkbook wb = reload(out.toByteArray())) {
            XSSFSheet data = wb.getSheet("_data");
            assertTrue(wb.isSheetHidden(wb.getSheetIndex(data)), "데이터 시트는 숨김이어야 한다");
            assertEquals("1월", data.getRow(0).getCell(0).getStringCellValue());
            assertEquals("2월", data.getRow(1).getCell(0).getStringCellValue());
            assertEquals(100.0, data.getRow(0).getCell(1).getNumericCellValue()); // 매출
            assertEquals(90.0, data.getRow(1).getCell(2).getNumericCellValue());  // 비용
            assertFalse(wb.getSheet("Charts").getDrawingPatriarch().getCharts().isEmpty());
        }
    }

    @Test
    void missingAnnotationThrows() {
        assertThrows(ExcelKitException.class, () -> ExcelCanvas.create().chart(List.of(new NoAnn())));
    }

    @Test
    void scatterWithoutXyThrows() {
        assertThrows(ExcelKitException.class, () -> ExcelCanvas.create().chart(List.of(new BadScatter())));
    }

    @Test
    void pieWithMultipleSeriesThrows() {
        assertThrows(ExcelKitException.class, () -> ExcelCanvas.create().chart(List.of(new BadPie())));
    }

    @Test
    void emptyListWithoutClassThrows() {
        assertThrows(ExcelKitException.class, () -> ExcelCanvas.create().chart(List.of()));
    }

    @Test
    void emptyListWithClassProducesChartShell() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCanvas.create().chart(Bar.class, List.of()).writeTo(out);
        try (XSSFWorkbook wb = reload(out.toByteArray())) {
            assertEquals(1, chartCount(wb));
        }
    }

    @Test
    void explicitAndAutoPlacementBothWork() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCanvas.create()
                .chart(List.of(new Line("1월", 100)))          // 자동 배치
                .chart(List.of(new Pie("A", 30), new Pie("B", 70))) // 자동 배치
                .writeTo(out);
        try (XSSFWorkbook wb = reload(out.toByteArray())) {
            assertEquals(2, chartCount(wb));
        }
    }
}
