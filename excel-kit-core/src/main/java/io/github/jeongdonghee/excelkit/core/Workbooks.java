package io.github.jeongdonghee.excelkit.core;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 워크북 생성 팩토리.
 *
 * <ul>
 *   <li>{@link #xssf()} — 인메모리(.xlsx). 차트/이미지 등 풀 기능이 필요할 때.</li>
 *   <li>{@link #streaming(int)} — SXSSF 스트리밍(.xlsx). 대용량을 저메모리로 쓸 때.</li>
 * </ul>
 */
public final class Workbooks {

    /** SXSSF 기본 행 윈도우 크기(메모리에 유지하는 행 수). */
    public static final int DEFAULT_WINDOW_SIZE = 100;

    private Workbooks() {
    }

    public static XSSFWorkbook xssf() {
        return new XSSFWorkbook();
    }

    public static SXSSFWorkbook streaming() {
        return streaming(DEFAULT_WINDOW_SIZE);
    }

    /**
     * @param rowAccessWindowSize 메모리에 유지할 행 수(초과분은 임시 파일로 플러시). 양수여야 한다.
     */
    public static SXSSFWorkbook streaming(int rowAccessWindowSize) {
        if (rowAccessWindowSize <= 0) {
            throw new ExcelKitException("rowAccessWindowSize must be positive: " + rowAccessWindowSize);
        }
        return new SXSSFWorkbook(rowAccessWindowSize);
    }
}
