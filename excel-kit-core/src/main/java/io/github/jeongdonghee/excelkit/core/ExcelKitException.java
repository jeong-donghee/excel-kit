package io.github.jeongdonghee.excelkit.core;

/**
 * excel-kit 전반에서 사용하는 런타임 예외.
 * POI의 checked 예외 등을 비검사 예외로 감싸 호출부의 try/catch 부담을 없앤다.
 */
public class ExcelKitException extends RuntimeException {

    public ExcelKitException(String message) {
        super(message);
    }

    public ExcelKitException(String message, Throwable cause) {
        super(message, cause);
    }
}
