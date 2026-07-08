package io.github.jeongdonghee.excelkit.canvas;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.jeongdonghee.excelkit.core.ExcelKitException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.BarGrouping;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 어노테이션이 붙은 데이터 클래스의 리스트를 넘기면 차트를 그려 .xlsx로 내보내는 진입점.
 *
 * <pre>{@code
 * ExcelCanvas.create()
 *     .chart(salesList)     // @ExcelChart 붙은 클래스의 리스트
 *     .chart(shareList)
 *     .writeTo(Path.of("dashboard.xlsx"));
 * }</pre>
 *
 * 차트 데이터는 숨은 시트에 기록되고 차트가 그 범위를 참조한다(XSSF 인메모리).
 */
public final class ExcelCanvas {

    private static final Map<Class<?>, ChartDef> CACHE = new ConcurrentHashMap<>();

    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;
    private final XSSFSheet dataSheet;
    private XSSFDrawing drawing;
    private int autoRow = 0;   // 자동 배치 커서
    private int dataCol = 0;   // 숨은 데이터 시트의 다음 빈 열

    private ExcelCanvas() {
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet("Charts");
        this.dataSheet = workbook.createSheet("_data");
        workbook.setSheetHidden(workbook.getSheetIndex(dataSheet), true);
    }

    public static ExcelCanvas create() {
        return new ExcelCanvas();
    }

    /** 리스트를 차트로 그린다. 원소 타입은 첫 원소로 판별. 빈 리스트면 {@link #chart(Class, List)}를 쓰라. */
    public ExcelCanvas chart(List<?> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new ExcelKitException("빈(또는 null) 리스트로는 차트 타입을 알 수 없습니다. chart(Class, list)를 사용하세요.");
        }
        return chart(rows.get(0).getClass(), rows);
    }

    /** 원소 타입을 명시해 차트를 그린다(빈 리스트에도 안전). */
    public ExcelCanvas chart(Class<?> type, List<?> rows) {
        ChartDef def = CACHE.computeIfAbsent(type, ExcelCanvas::resolve);
        render(def, rows == null ? List.of() : rows);
        return this;
    }

    public void writeTo(OutputStream out) {
        try {
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            throw new ExcelKitException("failed to write canvas", e);
        }
    }

    public void writeTo(Path path) {
        try (OutputStream out = Files.newOutputStream(path)) {
            writeTo(out);
        } catch (IOException e) {
            throw new ExcelKitException("failed to write canvas to " + path, e);
        }
    }

    /** 프리뷰 등 세밀 제어를 위한 원본 워크북. */
    public XSSFWorkbook workbook() {
        return workbook;
    }

    // ── 렌더링 ────────────────────────────────────────────────

    private void render(ChartDef def, List<?> rows) {
        XSSFChart chart = newChart(def);
        int n = rows.size();
        if (n == 0) {
            return; // 데이터 없으면 제목/범례만 있는 빈 차트
        }
        if (def.type.isCategory()) {
            renderCategory(chart, def, rows, n);
        } else if (def.type.isPie()) {
            renderPie(chart, def, rows, n);
        } else {
            renderScatter(chart, def, rows, n);
        }
    }

    private XSSFChart newChart(ChartDef def) {
        ExcelChart a = def.ann;
        boolean explicit = a.col() >= 0 && a.row() >= 0;
        int w = a.width() > 0 ? a.width() : 8;
        int h = a.height() > 0 ? a.height() : 15;
        int col = explicit ? a.col() : 0;
        int row = explicit ? a.row() : autoRow;
        if (!explicit) {
            autoRow += h + 1;
        }
        XSSFClientAnchor anchor = drawing().createAnchor(0, 0, 0, 0, col, row, col + w, row + h);
        XSSFChart chart = drawing().createChart(anchor);
        if (!a.title().isBlank()) {
            chart.setTitleText(a.title());
            chart.setTitleOverlay(false);
        }
        if (a.legend() != Legend.NONE) {
            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(legendPosition(a.legend()));
        }
        return chart;
    }

    private void renderCategory(XSSFChart chart, ChartDef def, List<?> rows, int n) {
        int catCol = dataCol++;
        for (int r = 0; r < n; r++) {
            putString(r, catCol, str(read(def.category, rows.get(r))));
        }
        XDDFCategoryDataSource cats = XDDFDataSourcesFactory.fromStringCellRange(
                dataSheet, new CellRangeAddress(0, n - 1, catCol, catCol));

        XDDFCategoryAxis catAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis valAxis = chart.createValueAxis(AxisPosition.LEFT);
        valAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        axisTitle(catAxis, def.ann.categoryAxisTitle());
        axisTitle(valAxis, def.ann.valueAxisTitle());

        XDDFChartData data = chart.createData(chartTypes(def.type), catAxis, valAxis);
        if (data instanceof XDDFBarChartData bar) {
            bar.setBarDirection(BarDirection.COL);
            bar.setBarGrouping(barGrouping(def.ann.grouping()));
            // setOverlap은 poi-ooxml-lite에 없는 스키마 타입을 요구하므로 생략.
            // STACKED는 grouping만으로 정상 렌더된다.
        }
        int i = 0;
        for (SeriesField sf : def.series) {
            int col = dataCol++;
            for (int r = 0; r < n; r++) {
                putNumber(r, col, toDouble(read(sf.field, rows.get(r))));
            }
            XDDFNumericalDataSource<Double> vals = XDDFDataSourcesFactory.fromNumericCellRange(
                    dataSheet, new CellRangeAddress(0, n - 1, col, col));
            XDDFChartData.Series series = data.addSeries(cats, vals);
            series.setTitle(sf.name, null);
            applyColor(series, def.type, color(i));
            i++;
        }
        chart.plot(data);
        dataCol++; // 다음 차트와의 간격
    }

    private void renderPie(XSSFChart chart, ChartDef def, List<?> rows, int n) {
        int catCol = dataCol++;
        SeriesField sf = def.series.get(0);
        int valCol = dataCol++;
        for (int r = 0; r < n; r++) {
            putString(r, catCol, str(read(def.category, rows.get(r))));
            putNumber(r, valCol, toDouble(read(sf.field, rows.get(r))));
        }
        XDDFCategoryDataSource cats = XDDFDataSourcesFactory.fromStringCellRange(
                dataSheet, new CellRangeAddress(0, n - 1, catCol, catCol));
        XDDFNumericalDataSource<Double> vals = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(0, n - 1, valCol, valCol));

        XDDFChartData data = chart.createData(chartTypes(def.type), null, null);
        data.setVaryColors(true);
        XDDFChartData.Series series = data.addSeries(cats, vals);
        series.setTitle(sf.name, null);
        chart.plot(data);
        dataCol++;
    }

    private void renderScatter(XSSFChart chart, ChartDef def, List<?> rows, int n) {
        int xCol = dataCol++;
        for (int r = 0; r < n; r++) {
            putNumber(r, xCol, toDouble(read(def.x, rows.get(r))));
        }
        XDDFNumericalDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(0, n - 1, xCol, xCol));

        XDDFValueAxis xAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.LEFT);
        yAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        axisTitle(xAxis, def.ann.categoryAxisTitle());
        axisTitle(yAxis, def.ann.valueAxisTitle());

        XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, xAxis, yAxis);
        int i = 0;
        for (SeriesField sf : def.ys) {
            int col = dataCol++;
            for (int r = 0; r < n; r++) {
                putNumber(r, col, toDouble(read(sf.field, rows.get(r))));
            }
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(
                    dataSheet, new CellRangeAddress(0, n - 1, col, col));
            XDDFScatterChartData.Series series = (XDDFScatterChartData.Series) data.addSeries(xs, ys);
            series.setTitle(sf.name, null);
            series.setMarkerStyle(MarkerStyle.CIRCLE);
            series.setSmooth(false);
            i++;
        }
        chart.plot(data);
        dataCol++;
    }

    // ── 헬퍼 ─────────────────────────────────────────────────

    private XSSFDrawing drawing() {
        if (drawing == null) {
            drawing = sheet.createDrawingPatriarch();
        }
        return drawing;
    }

    private XSSFRow row(int r) {
        XSSFRow row = dataSheet.getRow(r);
        return row != null ? row : dataSheet.createRow(r);
    }

    private void putString(int r, int c, String v) {
        row(r).createCell(c).setCellValue(v);
    }

    private void putNumber(int r, int c, double v) {
        row(r).createCell(c).setCellValue(v);
    }

    private static void axisTitle(org.apache.poi.xddf.usermodel.chart.XDDFChartAxis axis, String title) {
        if (title != null && !title.isBlank()) {
            axis.setTitle(title);
        }
    }

    private static void applyColor(XDDFChartData.Series series, ChartType type, byte[] rgb) {
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(rgb));
        if (type == ChartType.LINE || type == ChartType.RADAR) {
            XDDFLineProperties line = new XDDFLineProperties();
            line.setFillProperties(fill);
            series.setLineProperties(line);
        } else {
            series.setFillProperties(fill);
        }
    }

    private static byte[] color(int index) {
        return Palette.rgb(Palette.at(index));
    }

    private static ChartTypes chartTypes(ChartType type) {
        return switch (type) {
            case BAR -> ChartTypes.BAR;
            case LINE -> ChartTypes.LINE;
            case AREA -> ChartTypes.AREA;
            case RADAR -> ChartTypes.RADAR;
            case PIE -> ChartTypes.PIE;
            case DOUGHNUT -> ChartTypes.DOUGHNUT;
            case SCATTER -> ChartTypes.SCATTER;
        };
    }

    private static BarGrouping barGrouping(Grouping g) {
        return switch (g) {
            case CLUSTERED -> BarGrouping.CLUSTERED;
            case STACKED -> BarGrouping.STACKED;
            case PERCENT_STACKED -> BarGrouping.PERCENT_STACKED;
        };
    }

    private static LegendPosition legendPosition(Legend l) {
        return switch (l) {
            case TOP -> LegendPosition.TOP;
            case BOTTOM -> LegendPosition.BOTTOM;
            case LEFT -> LegendPosition.LEFT;
            case RIGHT -> LegendPosition.RIGHT;
            case NONE -> LegendPosition.RIGHT; // NONE은 여기 안 옴
        };
    }

    private static Object read(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new ExcelKitException("필드 읽기 실패: " + field.getName(), e);
        }
    }

    private static String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private static double toDouble(Object v) {
        if (v == null) {
            return 0d;
        }
        if (v instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            throw new ExcelKitException("숫자 시리즈에 숫자가 아닌 값: " + v);
        }
    }

    // ── 모델 해석 + 검증 ──────────────────────────────────────

    private static ChartDef resolve(Class<?> type) {
        ExcelChart ann = type.getAnnotation(ExcelChart.class);
        if (ann == null) {
            throw new ExcelKitException(type.getName() + " 에 @ExcelChart 애노테이션이 없습니다.");
        }
        Field category = null;
        Field x = null;
        List<SeriesField> series = new ArrayList<>();
        List<SeriesField> ys = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(ChartCategory.class)) {
                    f.setAccessible(true);
                    category = f;
                } else if (f.isAnnotationPresent(ChartSeries.class)) {
                    f.setAccessible(true);
                    ChartSeries s = f.getAnnotation(ChartSeries.class);
                    series.add(new SeriesField(s.name().isBlank() ? f.getName() : s.name(), f));
                } else if (f.isAnnotationPresent(ChartX.class)) {
                    f.setAccessible(true);
                    x = f;
                } else if (f.isAnnotationPresent(ChartY.class)) {
                    f.setAccessible(true);
                    ChartY y = f.getAnnotation(ChartY.class);
                    ys.add(new SeriesField(y.name().isBlank() ? f.getName() : y.name(), f));
                }
            }
        }

        ChartType t = ann.type();
        if (t.isScatter()) {
            if (x == null || ys.isEmpty()) {
                throw new ExcelKitException(type.getName() + ": " + t + " 차트는 @ChartX 1개와 @ChartY 1개 이상이 필요합니다.");
            }
        } else {
            if (category == null || series.isEmpty()) {
                throw new ExcelKitException(type.getName() + ": " + t + " 차트는 @ChartCategory 1개와 @ChartSeries 1개 이상이 필요합니다.");
            }
            if (t.isPie() && series.size() != 1) {
                throw new ExcelKitException(type.getName() + ": " + t + " 차트는 @ChartSeries가 정확히 1개여야 합니다(현재 " + series.size() + "개).");
            }
        }
        return new ChartDef(t, ann, category, series, x, ys);
    }

    private record SeriesField(String name, Field field) {
    }

    private record ChartDef(ChartType type, ExcelChart ann, Field category,
                            List<SeriesField> series, Field x, List<SeriesField> ys) {
    }
}
