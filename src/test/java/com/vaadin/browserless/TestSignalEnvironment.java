/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.browserless;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.signals.SignalEnvironment;

/**
 * Test-only {@link SignalEnvironment} that records submitted tasks instead of
 * executing them asynchronously. This allows unit tests to deterministically
 * drive and observe Signal processing by explicitly flushing the task queue.
 *
 * <p>
 * How it works:
 * <ul>
 * <li>{@link #getEffectDispatcher()} returns an executor that enqueues tasks
 * into an internal queue. {@link #getResultNotifier()} returns {@code null}
 * so that result notifications fall through to the next environment or run
 * immediately.</li>
 * <li>Tests call {@link #runPendingTasks(long, TimeUnit)} to dequeue and run
 * all pending tasks on the calling thread.</li>
 * <li>If the current thread holds a {@link VaadinSession} lock, the lock is
 * temporarily released so that background threads have a chance of locking the
 * session.</li>
 * </ul>
 *
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public class TestSignalEnvironment extends SignalEnvironment {

    private final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private Runnable cleanup;

    protected TestSignalEnvironment() {
    }

    /**
     * Registers this test environment as active {@link SignalEnvironment} and
     * returns the created instance.
     *
     * @return a new registered {@link TestSignalEnvironment} instance
     */
    public static TestSignalEnvironment register() {
        TestSignalEnvironment environment = new TestSignalEnvironment();
        environment.cleanup = SignalEnvironment.registerFirst(environment);
        return environment;
    }

    /**
     * Unregisters this test environment if it was registered.
     */
    public void unregister() {
        if (cleanup != null) {
            cleanup.run();
            cleanup = null;
        }
    }

    @Override
    protected boolean isActive() {
        return true;
    }

    @Override
    protected Executor getResultNotifier() {
        // Return null so result notifications fall through to the next
        // environment (e.g. VaadinServiceEnvironment) or to the immediate
        // executor. This keeps result processing synchronous on the calling
        // thread, which is important for deterministic test behavior when
        // signal operations are triggered on the test thread.
        return null;
    }

    @Override
    protected Executor getEffectDispatcher() {
        return tasks::offer;
    }

    /**
     * Executes pending tasks from the queue, continuously polling for new tasks
     * until the timeout expires with no new task arriving.
     *
     * @param maxWaitTime
     *            the maximum time to wait for the next task to arrive in the
     *            given time unit. If &lt;= 0, returns immediately if no tasks
     *            are available.
     * @param unit
     *            the time unit of the timeout value
     * @return {@code true} if any pending Signals tasks were processed.
     */
    public boolean runPendingTasks(long maxWaitTime, TimeUnit unit) {
        long deadlineNanos = System.nanoTime() + unit.toNanos(maxWaitTime);
        VaadinSession session = VaadinSession.getCurrent();
        boolean hadLock = false;
        if (session != null && session.hasLock()) {
            hadLock = true;
            session.unlock();
        }
        try {
            boolean processedAny = false;
            while (true) {
                long remainingNanos = deadlineNanos - System.nanoTime();
                Runnable task;
                try {
                    task = remainingNanos > 0
                            ? tasks.poll(remainingNanos, TimeUnit.NANOSECONDS)
                            : tasks.poll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(
                            "Thread interrupted while waiting for pending Signals tasks");
                }
                if (task == null) {
                    if (!processedAny) {
                        LoggerFactory.getLogger(TestSignalEnvironment.class)
                                .debug("No pending Signals tasks found after waiting for {} {}",
                                        maxWaitTime, unit);
                    }
                    break;
                }
                // Re-acquire the session lock before running the task so
                // that DOM operations (which assert the lock is held) work
                // correctly when the effect runs directly on the test
                // thread instead of going through ui.access().
                if (hadLock) {
                    session.lock();
                }
                try {
                    task.run();
                } finally {
                    if (hadLock) {
                        session.unlock();
                    }
                }
                processedAny = true;
            }
            return processedAny;
        } finally {
            if (hadLock) {
                session.lock();
            }
        }
    }

}
