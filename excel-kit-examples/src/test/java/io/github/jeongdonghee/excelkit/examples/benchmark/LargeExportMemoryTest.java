package io.github.jeongdonghee.excelkit.examples.benchmark;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;

import io.github.jeongdonghee.excelkit.examples.SessionRow;
import io.github.jeongdonghee.excelkit.grid.ExcelDownloadOptions;
import io.github.jeongdonghee.excelkit.grid.GridExcelWriter;
import io.github.jeongdonghee.excelkit.grid.OverflowPolicy;
import io.github.jeongdonghee.excelkit.grid.SheetNumbering;
import org.junit.jupiter.api.Test;

/**
 * 대용량(10만 행)을 지연 생성 컬렉션으로 익스포트해도 OOM 없이 완료됨을 확인한다.
 * (SXSSF가 워크북 메모리를 일정하게 유지)
 */
class LargeExportMemoryTest {

    @Test
    void exportsLargeDatasetWithoutRetainingRows() {
        ExcelDownloadOptions options = new ExcelDownloadOptions(
                "bench", "sheet", SheetNumbering.SUFFIX_UNDERSCORE, "",
                1_048_575, Integer.MAX_VALUE, OverflowPolicy.TRUNCATE);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GridExcelWriter.write(new LazyRows(100_000), SessionRow.class, options, out);

        assertTrue(out.size() > 0, "xlsx 바이트가 생성되어야 한다");
    }
}
