package io.github.jeongdonghee.excelkit.grid;

/**
 * {@link ExcelDownload} 애노테이션에서 뽑아낸, 스프링과 무관한 쓰기 옵션.
 * (writer를 프레임워크 독립적으로 테스트하기 위해 분리)
 */
public record ExcelDownloadOptions(
        String filename,
        String sheetName,
        SheetNumbering sheetNumbering,
        String rowNumberColumn,
        int maxRowsPerSheet,
        int maxSheets,
        OverflowPolicy overflowPolicy) {

    public static ExcelDownloadOptions from(ExcelDownload annotation) {
        return new ExcelDownloadOptions(
                annotation.filename(),
                annotation.sheetName(),
                annotation.sheetNumbering(),
                annotation.rowNumberColumn(),
                annotation.maxRowsPerSheet(),
                annotation.maxSheets(),
                annotation.overflowPolicy());
    }

    boolean hasRowNumber() {
        return rowNumberColumn != null && !rowNumberColumn.isBlank();
    }
}
