package io.github.jeongdonghee.excelkit.examples;

import io.github.jeongdonghee.excelkit.grid.ExcelColumn;

/**
 * 엑셀로 내보낼 행 DTO. 필드 컬럼과 파생(메서드) 컬럼을 함께 보여준다.
 */
public class SessionRow {

    @ExcelColumn(header = "세션 ID", order = 1)
    private final String sessionId;

    private final String user;
    private final String dept;

    @ExcelColumn(header = "쿼리 수", order = 3)
    private final int queryCount;

    public SessionRow(String sessionId, String user, String dept, int queryCount) {
        this.sessionId = sessionId;
        this.user = user;
        this.dept = dept;
        this.queryCount = queryCount;
    }

    /** 두 필드를 합친 파생 컬럼. */
    @ExcelColumn(header = "사용자", order = 2)
    public String userLabel() {
        return user + " (" + dept + ")";
    }

    /** 로직으로 계산하는 컬럼. */
    @ExcelColumn(header = "상태", order = 4)
    public String status() {
        return queryCount > 100 ? "warning" : "normal";
    }
}
