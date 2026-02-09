package com.example.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for generating analytics reports asynchronously.
 *
 * Demonstrates proper Spring Boot async service pattern with @Async annotation.
 * In a real application, this would connect to databases, external APIs, or
 * perform complex calculations to generate reports.
 */
@Service
public class AnalyticsService {

    /**
     * Represents raw analytics data fetched from data sources. This simulates
     * the result of querying databases, APIs, or data warehouses.
     */
    public static class RawAnalyticsData {
        private final int totalSales;
        private final int orderCount;
        private final int totalVisitors;
        private final int conversions;

        public RawAnalyticsData(int totalSales, int orderCount,
                int totalVisitors, int conversions) {
            this.totalSales = totalSales;
            this.orderCount = orderCount;
            this.totalVisitors = totalVisitors;
            this.conversions = conversions;
        }

        public int getTotalSales() {
            return totalSales;
        }

        public int getOrderCount() {
            return orderCount;
        }

        public int getTotalVisitors() {
            return totalVisitors;
        }

        public int getConversions() {
            return conversions;
        }
    }

    /**
     * Represents an analytics report with key business metrics.
     */
    public static class AnalyticsReport {
        private String period;
        private int totalRevenue;
        private int totalOrders;
        private double conversionRate;
        private int activeUsers;

        // Sentinel value for empty report
        private static final AnalyticsReport EMPTY = new AnalyticsReport("", 0,
                0, 0.0, 0);

        public AnalyticsReport() {
        }

        public AnalyticsReport(String period, int totalRevenue, int totalOrders,
                double conversionRate, int activeUsers) {
            this.period = period;
            this.totalRevenue = totalRevenue;
            this.totalOrders = totalOrders;
            this.conversionRate = conversionRate;
            this.activeUsers = activeUsers;
        }

        public static AnalyticsReport empty() {
            return EMPTY;
        }

        public boolean isEmpty() {
            return this == EMPTY || (period == null || period.isEmpty());
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public int getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(int totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public int getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }

        public double getConversionRate() {
            return conversionRate;
        }

        public void setConversionRate(double conversionRate) {
            this.conversionRate = conversionRate;
        }

        public int getActiveUsers() {
            return activeUsers;
        }

        public void setActiveUsers(int activeUsers) {
            this.activeUsers = activeUsers;
        }
    }

    /**
     * Fetches relevant data for analytics report generation (Step 1).
     *
     * The @Async annotation causes Spring to execute this method in a separate
     * thread from a configured thread pool. This simulates fetching raw data
     * from databases, APIs, or data warehouses before processing.
     *
     * @param simulateError
     *            If true, simulates a service failure
     * @return CompletableFuture containing the raw analytics data
     */
    @Async
    public CompletableFuture<RawAnalyticsData> fetchReportData(
            boolean simulateError) {
        try {
            // Simulate data fetching: database queries, API calls, data
            // warehouse access
            Thread.sleep(2000);

            if (simulateError) {
                throw new RuntimeException("Failed to fetch analytics data");
            }

            // In a real application, this would fetch data from:
            // - Database queries (SELECT statements)
            // - External API calls
            // - Data warehouse aggregations
            // - Real-time data streams
            RawAnalyticsData rawData = new RawAnalyticsData(1_234_567, // Total
                                                                       // sales
                                                                       // amount
                                                                       // from
                                                                       // orders
                                                                       // table
                    15_432, // Order count from orders table
                    260_784, // Total visitors from analytics
                    8_921 // Conversion events from tracking
            );

            return CompletableFuture.completedFuture(rawData);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Data fetch interrupted", e);
        }
    }

    /**
     * Generates an analytics report from fetched data (Step 2).
     *
     * The @Async annotation causes Spring to execute this method in a separate
     * thread from a configured thread pool. This is the proper way to handle
     * long-running operations in Spring Boot applications.
     *
     * @param rawData
     *            The raw data fetched in step 1
     * @param simulateError
     *            If true, simulates a service failure
     * @return CompletableFuture containing the generated report
     */
    @Async
    public CompletableFuture<AnalyticsReport> generateReportFromData(
            RawAnalyticsData rawData, boolean simulateError) {
        try {
            // Simulate heavy processing: complex calculations, aggregations,
            // transformations
            Thread.sleep(2000);

            if (simulateError) {
                throw new RuntimeException(
                        "Failed to generate analytics report");
            }

            // In a real application, this would:
            // - Process the fetched data
            // - Calculate metrics and aggregations (conversion rate, etc.)
            // - Apply business logic and transformations
            // - Format results for presentation
            double conversionRate = (rawData.getConversions() * 100.0)
                    / rawData.getTotalVisitors();

            AnalyticsReport report = new AnalyticsReport("Q4 2025",
                    rawData.getTotalSales(), rawData.getOrderCount(),
                    conversionRate, rawData.getConversions());

            return CompletableFuture.completedFuture(report);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Report generation interrupted", e);
        }
    }
}
