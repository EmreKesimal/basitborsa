package com.basitborsa.repository;

import com.basitborsa.entity.DataSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DataSyncLogRepository extends JpaRepository<DataSyncLog, Long> {
    List<DataSyncLog> findTop10ByOrderByStartedAtDesc();
}
