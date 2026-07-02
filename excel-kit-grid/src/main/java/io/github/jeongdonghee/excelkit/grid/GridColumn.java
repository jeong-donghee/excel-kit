package io.github.jeongdonghee.excelkit.grid;

import java.util.function.Function;

/**
 * 해석된 컬럼 하나 — 헤더와, 대상 객체에서 값을 꺼내는 getter.
 */
final class GridColumn {

    private final String header;
    private final int order;
    private final Function<Object, Object> getter;

    GridColumn(String header, int order, Function<Object, Object> getter) {
        this.header = header;
        this.order = order;
        this.getter = getter;
    }

    String header() {
        return header;
    }

    int order() {
        return order;
    }

    Object get(Object target) {
        return getter.apply(target);
    }
}
