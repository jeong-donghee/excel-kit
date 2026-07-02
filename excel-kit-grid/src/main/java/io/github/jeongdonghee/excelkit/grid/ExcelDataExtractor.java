package io.github.jeongdonghee.excelkit.grid;

import java.util.function.Function;

/**
 * 공통 응답 래퍼(예: {@code ApiResponse<List<T>>})에서 실제 목록을 꺼내는 전략.
 *
 * <p>래퍼 타입당 빈 하나씩 등록하면 되고, {@code List}/{@code Collection}/배열/{@code ResponseEntity}는
 * 기본 내장 처리되므로 등록이 필요 없다. 단일 케이스는 {@link #forType(Class, Function)} 헬퍼로 한 줄이면 된다.
 *
 * <pre>{@code
 * @Bean
 * ExcelDataExtractor apiResponseExtractor() {
 *     return ExcelDataExtractor.forType(ApiResponse.class, ApiResponse::getData);
 * }
 * }</pre>
 */
public interface ExcelDataExtractor {

    /** 이 추출기가 주어진 반환 값을 처리할 수 있는지. */
    boolean supports(Object value);

    /** 반환 값에서 목록(Collection/배열)을 꺼낸다. */
    Object extract(Object value);

    /**
     * 특정 래퍼 타입에 대해 함수로 목록을 꺼내는 추출기를 만든다.
     *
     * @param type 래퍼 타입
     * @param mapper 래퍼 인스턴스 → 목록
     */
    static <T> ExcelDataExtractor forType(Class<T> type, Function<? super T, ?> mapper) {
        return new ExcelDataExtractor() {
            @Override
            public boolean supports(Object value) {
                return type.isInstance(value);
            }

            @Override
            @SuppressWarnings("unchecked")
            public Object extract(Object value) {
                return mapper.apply((T) value);
            }
        };
    }
}
