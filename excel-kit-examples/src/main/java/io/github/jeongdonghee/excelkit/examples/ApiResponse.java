package io.github.jeongdonghee.excelkit.examples;

/**
 * 흔한 공통 응답 래퍼 예시. 컨트롤러가 이걸 그대로 반환해도
 * {@code ExcelDataExtractor} 빈(→ {@link ExcelKitConfig}) 하나만 등록하면 엑셀로 내려간다.
 */
public class ApiResponse<T> {

    private final boolean success;
    private final T data;

    private ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }
}
