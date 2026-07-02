package io.github.jeongdonghee.excelkit.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 셀 값 설정 유틸. 자바 타입을 POI 셀 타입으로 매핑한다.
 */
public final class Cells {

    private Cells() {
    }

    /**
     * 값의 런타임 타입에 따라 적절한 셀 타입으로 기록한다.
     * {@code null}은 빈 셀, 숫자/불리언/날짜 외에는 문자열로 처리한다.
     */
    public static void setValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date);
        } else if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(dateTime);
        } else if (value instanceof Date date) {
            cell.setCellValue(date);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }
}
