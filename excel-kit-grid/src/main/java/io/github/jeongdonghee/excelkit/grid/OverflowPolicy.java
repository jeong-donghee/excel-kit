package io.github.jeongdonghee.excelkit.grid;

/**
 * 데이터가 {@code maxSheets} 가드를 넘겼을 때의 동작.
 */
public enum OverflowPolicy {

    /** 가드를 넘는 나머지는 버리고 경고 로그를 남긴다(기본). */
    TRUNCATE,

    /** 예외를 던져 익스포트를 중단한다. */
    FAIL
}
