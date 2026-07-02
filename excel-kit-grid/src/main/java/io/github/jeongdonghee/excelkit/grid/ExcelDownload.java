package io.github.jeongdonghee.excelkit.grid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 스프링 컨트롤러 메서드에 붙이면, 반환한 목록(또는 공통 응답 래퍼)을 엑셀(.xlsx) 다운로드로 내보낸다.
 *
 * <pre>{@code
 * @ExcelDownload(filename = "sessions", rowNumberColumn = "No")
 * @GetMapping("/sessions/excel")
 * public List<SessionRow> sessions() { return service.findAll(); }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExcelDownload {

    /** 파일 베이스명. 확장자는 쓰지 않는다 — {@code .xlsx}가 자동으로 붙는다. */
    String filename();

    /** 시트 베이스명. 비우면 원소 타입 이름을 사용한다. */
    String sheetName() default "";

    /** 시트 분할 시 번호 표기 방식. */
    SheetNumbering sheetNumbering() default SheetNumbering.SUFFIX_UNDERSCORE;

    /** 지정하면 맨 앞에 행번호 컬럼을 추가한다(값 = 헤더 텍스트). 1부터, 헤더 제외, 시트가 나뉘어도 연속. */
    String rowNumberColumn() default "";

    /** 시트당 최대 데이터 행. 초과 시 자동으로 다음 시트로 분할. 기본은 엑셀 한도(1,048,576)에서 헤더 1행을 뺀 값. */
    int maxRowsPerSheet() default 1_048_575;

    /** 시트 수 상한(가드). 기본은 무제한. */
    int maxSheets() default Integer.MAX_VALUE;

    /** 가드 초과 시 동작. */
    OverflowPolicy overflowPolicy() default OverflowPolicy.TRUNCATE;

    /** 반환 타입에서 원소 타입을 정적으로 알 수 없을 때만(로타입/제네릭 없는 구체 래퍼) 명시한다. */
    Class<?> type() default void.class;
}
