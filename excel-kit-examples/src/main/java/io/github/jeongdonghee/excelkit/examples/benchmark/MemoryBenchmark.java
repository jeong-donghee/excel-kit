package io.github.jeongdonghee.excelkit.examples.benchmark;

import java.io.OutputStream;

import io.github.jeongdonghee.excelkit.examples.SessionRow;
import io.github.jeongdonghee.excelkit.grid.ExcelDownloadOptions;
import io.github.jeongdonghee.excelkit.grid.GridExcelWriter;
import io.github.jeongdonghee.excelkit.grid.OverflowPolicy;
import io.github.jeongdonghee.excelkit.grid.SheetNumbering;

/**
 * excel-kit-grid(SXSSF)로 행 수를 늘려가며 익스포트할 때 <b>피크 힙이 거의 일정</b>하게 유지됨을 보여준다.
 *
 * <p>실행: {@code ./mvnw -pl excel-kit-examples -q exec:java -Dexec.mainClass=io.github.jeongdonghee.excelkit.examples.benchmark.MemoryBenchmark}
 * 또는 낮은 힙으로 대용량 확인: {@code java -Xmx256m -cp ... MemoryBenchmark}
 *
 * <p>비교 관점: 순진하게 전체 워크북을 인메모리(XSSF)로 만들면 행 수에 비례해 힙이 늘고 결국 OOM 난다.
 * SXSSF는 최근 N행만 유지하고 나머지는 임시 파일로 흘려보내므로 힙이 일정하다.
 */
public final class MemoryBenchmark {

    private MemoryBenchmark() {
    }

    public static void main(String[] args) throws Exception {
        int[] sizes = {100_000, 500_000, 1_000_000};
        System.out.printf("%-12s %-18s %-10s%n", "rows", "peak heap (MB)", "time (ms)");
        System.out.println("-".repeat(42));
        // 워밍업
        export(10_000);
        for (int rows : sizes) {
            Result result = export(rows);
            System.out.printf("%-12d %-18.1f %-10d%n", rows, result.peakMb(), result.millis());
        }
    }

    static Result export(int rows) throws Exception {
        ExcelDownloadOptions options = new ExcelDownloadOptions(
                "bench", "sheet", SheetNumbering.SUFFIX_UNDERSCORE, "",
                1_048_575, Integer.MAX_VALUE, OverflowPolicy.TRUNCATE);

        System.gc();
        HeapSampler sampler = new HeapSampler();
        sampler.start();
        long start = System.nanoTime();
        try (OutputStream out = OutputStream.nullOutputStream()) {
            GridExcelWriter.write(new LazyRows(rows), SessionRow.class, options, out);
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        sampler.stopAndJoin();
        return new Result(sampler.peakUsedBytes() / 1024.0 / 1024.0, millis);
    }

    record Result(double peakMb, long millis) {
    }
}
