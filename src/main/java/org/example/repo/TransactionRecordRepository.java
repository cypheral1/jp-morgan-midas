package org.example.repo;

import org.example.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
}
