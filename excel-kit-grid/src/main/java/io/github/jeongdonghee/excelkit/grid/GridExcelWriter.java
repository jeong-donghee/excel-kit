package io.github.jeongdonghee.excelkit.grid;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import io.github.jeongdonghee.excelkit.core.CellStyles;
import io.github.jeongdonghee.excelkit.core.Cells;
import io.github.jeongdonghee.excelkit.core.ExcelKitException;
import io.github.jeongdonghee.excelkit.core.Workbooks;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * 목록을 SXSSF 스트리밍으로 .xlsx에 쓴다. 시트 분할·행번호·overflow 정책을 처리한다.
 * 스프링과 무관하게 동작하므로 단독 테스트 가능하다.
 */
public final class GridExcelWriter {

    private static final Log log = LogFactory.getLog(GridExcelWriter.class);

    private GridExcelWriter() {
    }

    public static void write(Collection<?> rows, Class<?> elementType,
                             ExcelDownloadOptions options, OutputStream out) {
        List<GridColumn> columns = ColumnResolver.resolve(elementType);
        boolean rowNumber = options.hasRowNumber();
        int perSheet = options.maxRowsPerSheet();
        if (perSheet <= 0) {
            throw new ExcelKitException("maxRowsPerSheet must be positive: " + perSheet);
        }

        int total = rows.size();
        int sheetsNeeded = Math.max(1, (int) Math.ceil((double) total / perSheet));
        boolean overflow = sheetsNeeded > options.maxSheets();
        if (overflow && options.overflowPolicy() == OverflowPolicy.FAIL) {
            throw new ExcelKitException("row count " + total + " exceeds maxSheets=" + options.maxSheets()
                    + " x maxRowsPerSheet=" + perSheet);
        }
        int sheetsToWrite = overflow ? options.maxSheets() : sheetsNeeded;
        boolean split = sheetsNeeded > 1;
        long maxWritable = (long) sheetsToWrite * perSheet;
        String base = options.sheetName().isBlank() ? elementType.getSimpleName() : options.sheetName();

        try (SXSSFWorkbook workbook = Workbooks.streaming()) {
            CellStyle headerStyle = CellStyles.header(workbook);
            SXSSFSheet sheet = null;
            int rowInSheet = 0;
            int sheetNo = 0;
            long written = 0;

            for (Object item : rows) {
                if (written >= maxWritable) {
                    break; // truncate
                }
                if (sheet == null || rowInSheet >= perSheet) {
                    sheetNo++;
                    sheet = workbook.createSheet(sheetName(base, sheetNo, split, options.sheetNumbering()));
                    writeHeader(sheet, headerStyle, columns, rowNumber, options.rowNumberColumn());
                    rowInSheet = 0;
                }
                Row row = sheet.createRow(rowInSheet + 1); // +1: 헤더 다음
                int c = 0;
                if (rowNumber) {
                    Cells.setValue(row.createCell(c++), written + 1);
                }
                for (GridColumn column : columns) {
                    Cells.setValue(row.createCell(c++), column.get(item));
                }
                rowInSheet++;
                written++;
            }

            if (sheet == null) { // 데이터가 없으면 헤더만 있는 시트 하나
                sheet = workbook.createSheet(sheetName(base, 1, false, options.sheetNumbering()));
                writeHeader(sheet, headerStyle, columns, rowNumber, options.rowNumberColumn());
            }

            if (overflow) {
                log.warn("excel-kit-grid: truncated " + total + " rows to " + maxWritable
                        + " (maxSheets=" + options.maxSheets() + ", maxRowsPerSheet=" + perSheet + ")");
            }

            workbook.write(out);
        } catch (IOException e) {
            throw new ExcelKitException("failed to write grid excel", e);
        }
    }

    private static void writeHeader(SXSSFSheet sheet, CellStyle style, List<GridColumn> columns,
                                    boolean rowNumber, String rowNumberHeader) {
        Row header = sheet.createRow(0);
        int c = 0;
        if (rowNumber) {
            Cell cell = header.createCell(c++);
            cell.setCellValue(rowNumberHeader);
            cell.setCellStyle(style);
        }
        for (GridColumn column : columns) {
            Cell cell = header.createCell(c++);
            cell.setCellValue(column.header());
            cell.setCellStyle(style);
        }
    }

    private static String sheetName(String base, int number, boolean split, SheetNumbering numbering) {
        String name = split ? numbering.apply(base, number) : base;
        return WorkbookUtil.createSafeSheetName(name);
    }
}
