package com.example.usecase10;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UploadStateTest {

    @Test
    void idleLabelDescribesNoUpload() {
        UploadState state = new UploadState.Idle();
        assertEquals("Idle — no upload in progress", state.label());
    }

    @Test
    void inProgressLabelShowsPercentage() {
        UploadState state = new UploadState.InProgress("photo.png", 50, 100);
        assertEquals("Uploading photo.png… 50%", state.label());
    }

    @Test
    void inProgressPercentZeroWhenTotalBytesIsZero() {
        UploadState.InProgress state = new UploadState.InProgress("file.txt",
                50, 0);
        assertEquals(0, state.progressPercent());
    }

    @Test
    void inProgressPercentZeroWhenTotalBytesIsNegative() {
        UploadState.InProgress state = new UploadState.InProgress("file.txt",
                50, -1);
        assertEquals(0, state.progressPercent());
    }

    @Test
    void inProgressPercentCalculatesCorrectly() {
        UploadState.InProgress state = new UploadState.InProgress("data.csv",
                750, 1000);
        assertEquals(75, state.progressPercent());
    }

    @Test
    void succeededLabelShowsFileNameAndSizeInBytes() {
        UploadState state = new UploadState.Succeeded("tiny.txt", 512);
        assertEquals("Succeeded — tiny.txt (512 B)", state.label());
    }

    @Test
    void succeededLabelShowsKilobytes() {
        UploadState state = new UploadState.Succeeded("doc.pdf", 5120);
        assertEquals("Succeeded — doc.pdf (5.0 KB)", state.label());
    }

    @Test
    void succeededLabelShowsMegabytes() {
        UploadState state = new UploadState.Succeeded("video.mp4",
                3 * 1024 * 1024);
        assertEquals("Succeeded — video.mp4 (3.0 MB)", state.label());
    }

    @Test
    void failedLabelShowsFileNameAndReason() {
        UploadState state = new UploadState.Failed("broken.zip",
                "Connection reset");
        assertEquals("Failed — broken.zip: Connection reset", state.label());
    }
}
