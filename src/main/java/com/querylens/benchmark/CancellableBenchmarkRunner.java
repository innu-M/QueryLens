package com.querylens.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/** Runs one candidate with timeout, cancellation, and per-run progress updates. */
public final class CancellableBenchmarkRunner implements AutoCloseable {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public List<Long> run(String label, BenchmarkTask task, BenchmarkSettings settings, BenchmarkProgressListener listener) {
        List<Long> samples = new ArrayList<>();
        try {
            for (int warmup = 1; warmup <= settings.warmupRuns(); warmup++) {
                if (cancelled.get()) return cancel(label, warmup - 1, settings.warmupRuns(), listener);
                listener.onProgress(new BenchmarkProgress(label, warmup - 1, settings.warmupRuns(), BenchmarkProgress.Status.WARMING_UP));
                execute(task, settings);
            }
            for (int run = 1; run <= settings.measuredRuns(); run++) {
                if (cancelled.get()) return cancel(label, run - 1, settings.measuredRuns(), listener);
                listener.onProgress(new BenchmarkProgress(label, run - 1, settings.measuredRuns(), BenchmarkProgress.Status.MEASURING));
                long started = System.nanoTime();
                execute(task, settings);
                samples.add(System.nanoTime() - started);
            }
            listener.onProgress(new BenchmarkProgress(label, samples.size(), settings.measuredRuns(), BenchmarkProgress.Status.COMPLETED));
            return List.copyOf(samples);
        } catch (TimeoutException exception) {
            listener.onProgress(new BenchmarkProgress(label, samples.size(), settings.measuredRuns(), BenchmarkProgress.Status.TIMED_OUT));
            return List.copyOf(samples);
        } catch (Exception exception) {
            listener.onProgress(new BenchmarkProgress(label, samples.size(), settings.measuredRuns(), BenchmarkProgress.Status.FAILED));
            throw new IllegalStateException("Benchmark task failed.", exception);
        }
    }

    public void cancel() { cancelled.set(true); }

    private List<Long> cancel(String label, int completed, int total, BenchmarkProgressListener listener) {
        listener.onProgress(new BenchmarkProgress(label, completed, total, BenchmarkProgress.Status.CANCELLED));
        return List.of();
    }

    private void execute(BenchmarkTask task, BenchmarkSettings settings) throws Exception {
        Future<Void> future = executor.submit((Callable<Void>) () -> { task.execute(); return null; });
        try {
            future.get(settings.queryTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            future.cancel(true);
            throw exception;
        }
    }

    @Override
    public void close() { executor.shutdownNow(); }
}
