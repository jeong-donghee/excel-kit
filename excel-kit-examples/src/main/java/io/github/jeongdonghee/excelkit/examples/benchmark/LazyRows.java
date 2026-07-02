package io.github.jeongdonghee.excelkit.examples.benchmark;

import java.util.AbstractCollection;
import java.util.Iterator;

import io.github.jeongdonghee.excelkit.examples.SampleData;
import io.github.jeongdonghee.excelkit.examples.SessionRow;

/**
 * 고정 크기의 행 컬렉션이지만 원소를 <b>지연 생성</b>한다(보관하지 않음).
 * 벤치마크에서 "데이터 자체가 차지하는 메모리"를 배제하고 <b>워크북 메모리</b>만 관찰하기 위함.
 */
public final class LazyRows extends AbstractCollection<SessionRow> {

    private final int size;

    public LazyRows(int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<SessionRow> iterator() {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public SessionRow next() {
                return SampleData.sessionAt(++i);
            }
        };
    }
}
