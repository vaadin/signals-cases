package com.example.usecase23;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;

import jakarta.annotation.PreDestroy;

@Service
public class SchedulerService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * Schedules a dashboard data update task to run periodically.
     * Generates mock data and delivers it to the callback via UI.access.
     *
     * @param taskId Unique identifier for this task
     * @param ui The UI instance to use for thread-safe access
     * @param dataCallback The callback to receive generated data (called within UI.access)
     * @param initialDelay Initial delay before first execution
     * @param period Period between executions
     * @param unit Time unit for delays and period
     */
    public void scheduleDashboardDataUpdate(String taskId, UI ui,
            Consumer<DashboardData> dataCallback,
            long initialDelay, long period, TimeUnit unit) {

        // Cancel existing task if present
        cancelTask(taskId);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            DashboardData data = generateDashboardData();
            ui.access(() -> dataCallback.accept(data));
        }, initialDelay, period, unit);

        tasks.put(taskId, future);
    }

    /**
     * Generates mock dashboard data.
     */
    private DashboardData generateDashboardData() {
        int currentUsers = randomBetween(650, 820);
        int viewEvents = randomBetween(42000, 62000);
        double conversionRate = randomBetween(12, 24);
        double customMetric = randomBetween(-200, 200);

        DashboardData.TimelineData timelineData = new DashboardData.TimelineData(
            LocalTime.now().format(TIME_FORMATTER),
            randomBetween(480, 920),
            randomBetween(420, 820),
            randomBetween(220, 520),
            randomBetween(260, 600)
        );

        List<ServiceHealth> serviceHealthList = List.of(
            new ServiceHealth(randomStatus(), "Münster",
                randomBetween(280, 360), randomBetween(1200, 1700)),
            new ServiceHealth(randomStatus(), "Cluj-Napoca",
                randomBetween(260, 340), randomBetween(1100, 1600)),
            new ServiceHealth(randomStatus(), "Ciudad Victoria",
                randomBetween(240, 320), randomBetween(1000, 1500))
        );

        List<Double> responseTimes = List.of(
            (double) randomBetween(6, 22),
            (double) randomBetween(6, 22),
            (double) randomBetween(6, 22),
            (double) randomBetween(6, 22),
            (double) randomBetween(6, 22),
            (double) randomBetween(6, 22)
        );

        return new DashboardData(currentUsers, viewEvents, conversionRate,
            customMetric, timelineData, serviceHealthList, responseTimes);
    }

    private ServiceHealth.Status randomStatus() {
        int pick = random.nextInt(3);
        if (pick == 0) {
            return ServiceHealth.Status.EXCELLENT;
        } else if (pick == 1) {
            return ServiceHealth.Status.OK;
        }
        return ServiceHealth.Status.FAILING;
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Cancels a scheduled task.
     *
     * @param taskId The task identifier
     */
    public void cancelTask(String taskId) {
        ScheduledFuture<?> future = tasks.remove(taskId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }

    /**
     * Checks if a task is currently scheduled.
     *
     * @param taskId The task identifier
     * @return true if task exists and is not cancelled
     */
    public boolean isTaskScheduled(String taskId) {
        ScheduledFuture<?> future = tasks.get(taskId);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    @PreDestroy
    public void shutdown() {
        tasks.values().forEach(future -> future.cancel(false));
        tasks.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
