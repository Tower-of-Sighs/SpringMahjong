package com.sighs.springmahjong.game.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Turn timer with stale-callback protection.
 * If a timer fires after cancel() or after the game has moved on,
 * the callback is silently dropped (via generation counter).
 */
public class TurnTimer {
    private static final ScheduledExecutorService SCHEDULER =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Mahjong-Timer");
            t.setDaemon(true);
            return t;
        });

    private ScheduledFuture<?> currentTask;
    private final AtomicBoolean valid = new AtomicBoolean(false);

    /** Start a new timer. Any previous timer is cancelled. */
    public void start(long timeoutMs, Runnable onTimeout) {
        cancel();
        if (timeoutMs <= 0 || timeoutMs >= Long.MAX_VALUE) return;

        valid.set(true);
        boolean myGeneration = valid.get();
        currentTask = SCHEDULER.schedule(() -> {
            // Only fire if this generation is still valid (not cancelled/restarted)
            if (valid.compareAndSet(true, false)) {
                onTimeout.run();
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
    }

    /** Cancel the current timer. Stale fire is prevented by generation check. */
    public void cancel() {
        valid.set(false);
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
        }
        currentTask = null;
    }

    public boolean isRunning() {
        return valid.get() && currentTask != null && !currentTask.isDone();
    }
}
