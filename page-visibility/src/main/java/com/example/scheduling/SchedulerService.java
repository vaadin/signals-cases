package com.example.scheduling;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;

/**
 * Shared scheduled executor for views that need timed work. The pool is owned
 * by Spring and shut down with the application context, so views only need to
 * cancel their own returned futures on detach.
 */
@Service
public class SchedulerService {

    private final ScheduledExecutorService scheduler = Executors
            .newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "page-visibility-scheduler");
                t.setDaemon(true);
                return t;
            });

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }

    public ScheduledFuture<?> schedule(UI ui, Runnable command, long delay,
            TimeUnit unit) {
        return scheduler.schedule(() -> ui.access(command::run), delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(UI ui, Runnable command,
            long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(() -> ui.access(command::run),
                initialDelay, period, unit);
    }
}
