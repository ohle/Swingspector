package de.eudaemon.ideaswag;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.intellij.openapi.diagnostic.Logger;

public class Cached<T> {
    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(
                    r -> new Thread(r, "Swingspector Agent Communication"));

    private static final Logger LOG = Logger.getInstance(Cached.class);
    private final Callable<T> source;
    private T lastSeenValue;

    public Cached(Callable<T> source_, T defaultValue_) {
        source = source_;
        lastSeenValue = defaultValue_;
    }

    public T getLastSeenValue() {
        return lastSeenValue;
    }

    public T get() {
        try {
            T result = executor.submit(source).get(500, TimeUnit.MILLISECONDS);
            lastSeenValue = result;
            return result;
        } catch (InterruptedException | TimeoutException | ExecutionException e_) {
            LOG.warn(e_);
            return lastSeenValue;
        }
    }
}
