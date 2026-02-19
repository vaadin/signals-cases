package com.example.usecase10;

/**
 * Sealed interface modeling the upload lifecycle as a state machine. Each
 * variant captures only the data relevant to that state.
 */
public sealed interface UploadState {

    record Idle() implements UploadState {
    }

    record InProgress(String fileName, long bytesRead,
            long totalBytes) implements UploadState {

        public int progressPercent() {
            if (totalBytes <= 0) {
                return 0;
            }
            return (int) (bytesRead * 100 / totalBytes);
        }
    }

    record Succeeded(String fileName, long totalBytes) implements UploadState {
    }

    record Failed(String fileName, String reason) implements UploadState {
    }

    default String label() {
        return switch (this) {
        case Idle ignored -> "Idle — no upload in progress";
        case InProgress p ->
            "Uploading " + p.fileName() + "… " + p.progressPercent() + "%";
        case Succeeded s -> "Succeeded — " + s.fileName() + " ("
                + formatBytes(s.totalBytes()) + ")";
        case Failed f -> "Failed — " + f.fileName() + ": " + f.reason();
        };
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
