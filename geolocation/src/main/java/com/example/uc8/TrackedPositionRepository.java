package com.example.uc8;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedPositionRepository
        extends JpaRepository<TrackedPosition, Long> {

    List<TrackedPosition> findBySessionIdOrderByTimestampAsc(String sessionId);

    void deleteBySessionId(String sessionId);
}
