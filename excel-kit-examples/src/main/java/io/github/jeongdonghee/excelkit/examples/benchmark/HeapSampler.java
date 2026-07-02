package io.github.jeongdonghee.excelkit.examples.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * 백그라운드에서 힙 사용량을 주기적으로 샘플링해 최댓값을 추적하는 간단한 도구.
 * (JMH급 엄밀함은 아니지만, "메모리가 N에 비례해 늘지 않는다"를 보여주기엔 충분)
 */
final class HeapSampler {

    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final Thread thread;
    private volatile boolean running = true;
    private volatile long peak = 0;

    HeapSampler() {
        this.thread = new Thread(this::loop, "heap-sampler");
        this.thread.setDaemon(true);
    }

    void start() {
        thread.start();
    }

    private void loop() {
        while (running) {
            long used = memory.getHeapMemoryUsage().getUsed();
            if (used > peak) {
                peak = used;
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    void stopAndJoin() {
        running = false;
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    long peakUsedBytes() {
        return peak;
    }
}
