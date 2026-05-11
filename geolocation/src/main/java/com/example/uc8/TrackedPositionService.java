package com.example.uc8;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackedPositionService {

    private final TrackedPositionRepository repository;

    public TrackedPositionService(TrackedPositionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TrackedPosition save(String sessionId, long timestamp,
            double latitude, double longitude, double accuracy) {
        return repository.save(new TrackedPosition(sessionId, timestamp,
                latitude, longitude, accuracy));
    }

    @Transactional(readOnly = true)
    public List<TrackedPosition> findBySession(String sessionId) {
        return repository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    @Transactional
    public void clearSession(String sessionId) {
        repository.deleteBySessionId(sessionId);
    }
}
