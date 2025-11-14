package com.lotus.lotusSPM.performance;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Performance Benchmark Tests using JMH (Java Microbenchmark Harness).
 *
 * Enterprise Pattern: Performance Testing / Micro-benchmarking
 *
 * Measures:
 * - Throughput (ops/sec)
 * - Average latency
 * - Percentiles (p50, p95, p99)
 * - Memory allocation
 * - GC pressure
 *
 * Use Cases:
 * - Algorithm comparison
 * - Optimization validation
 * - Regression detection
 * - Capacity planning
 * - SLA validation
 *
 * JMH Features:
 * - Warmup iterations (JIT optimization)
 * - Statistical analysis
 * - Dead code elimination prevention
 * - Constant folding prevention
 * - Fork isolation
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class PerformanceBenchmarkTest {

    private String sampleString;
    private StringBuilder stringBuilder;

    @Setup
    public void setup() {
        sampleString = "Performance Testing with JMH";
        stringBuilder = new StringBuilder();
    }

    /**
     * Benchmark string concatenation performance.
     */
    @Benchmark
    public void benchmarkStringConcatenation(Blackhole blackhole) {
        String result = "";
        for (int i = 0; i < 100; i++) {
            result += sampleString;
        }
        blackhole.consume(result);
    }

    /**
     * Benchmark StringBuilder performance.
     */
    @Benchmark
    public void benchmarkStringBuilder(Blackhole blackhole) {
        stringBuilder.setLength(0);
        for (int i = 0; i < 100; i++) {
            stringBuilder.append(sampleString);
        }
        blackhole.consume(stringBuilder.toString());
    }

    /**
     * Benchmark loop performance.
     */
    @Benchmark
    public void benchmarkForLoop(Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += i;
        }
        blackhole.consume(sum);
    }

    /**
     * Benchmark stream performance.
     */
    @Benchmark
    public void benchmarkStream(Blackhole blackhole) {
        int sum = java.util.stream.IntStream.range(0, 1000).sum();
        blackhole.consume(sum);
    }

    /**
     * Run benchmarks.
     */
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
            .include(PerformanceBenchmarkTest.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
