package io.github.jeongdonghee.excelkit.examples;

import java.util.List;

import io.github.jeongdonghee.excelkit.grid.ExcelDownload;
import io.github.jeongdonghee.excelkit.grid.SheetNumbering;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * excel-kit-grid 사용 예제 컨트롤러. 세 가지 대표 패턴을 보여준다.
 */
@RestController
@RequestMapping("/sessions")
public class SessionController {

    /** ① 가장 단순 — List 반환 + 행번호 컬럼. */
    @ExcelDownload(filename = "sessions", rowNumberColumn = "No")
    @GetMapping("/excel")
    public List<SessionRow> basic() {
        return SampleData.sessions(25);
    }

    /** ② 공통 응답 래퍼 — 컨트롤러는 래퍼 그대로 반환(extractor 빈이 처리). */
    @ExcelDownload(filename = "sessions-wrapped", rowNumberColumn = "No")
    @GetMapping("/wrapped/excel")
    public ApiResponse<List<SessionRow>> wrapped() {
        return ApiResponse.ok(SampleData.sessions(25));
    }

    /** ③ 대용량 — 시트 분할 + 번호 표기. 25만 행 → 시트 3개(세션목록_1..3). */
    @ExcelDownload(
            filename = "sessions-large",
            sheetName = "세션목록",
            sheetNumbering = SheetNumbering.SUFFIX_UNDERSCORE,
            maxRowsPerSheet = 100_000)
    @GetMapping("/large/excel")
    public List<SessionRow> large() {
        return SampleData.sessions(250_000);
    }
}
