package io.github.jeongdonghee.excelkit.examples;

import java.util.ArrayList;
import java.util.List;

/** 예제/벤치마크용 결정적 샘플 데이터 생성기. */
public final class SampleData {

    private static final String[] DEPTS = {"영업", "개발", "운영", "기획"};

    private SampleData() {
    }

    public static List<SessionRow> sessions(int count) {
        List<SessionRow> rows = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            rows.add(sessionAt(i));
        }
        return rows;
    }

    /** i번째 행을 생성한다(1-based). 상태 저장 없이 결정적으로 만든다. */
    public static SessionRow sessionAt(int i) {
        return new SessionRow("SID-" + i, "user" + (i % 100), DEPTS[i % DEPTS.length], (i * 7) % 200);
    }
}
