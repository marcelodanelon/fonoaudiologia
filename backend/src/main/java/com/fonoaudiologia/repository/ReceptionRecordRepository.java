package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.ReceptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface ReceptionRecordRepository extends JpaRepository<ReceptionRecord, Long> {
    boolean existsByPatientId(Long patientId);

    List<ReceptionRecord> findByOperatorIdOrderByCreatedAtDesc(Long operatorId);

    @Query("SELECT COUNT(r) FROM ReceptionRecord r WHERE r.createdAt >= :date")
    long countAfter(LocalDateTime date);

    @Query("SELECT COUNT(r) FROM ReceptionRecord r WHERE r.type = :type AND r.createdAt >= :date")
    long countByTypeAfter(String type, LocalDateTime date);

    List<ReceptionRecord> findByTypeOrderByCreatedAtDesc(String type);

    List<ReceptionRecord> findByTypeAndPatientIdIsNotNullOrderByCreatedAtDesc(String type);

    @Query("SELECT COUNT(r) FROM ReceptionRecord r WHERE NOT EXISTS (SELECT 1 FROM Consultation c WHERE c.receptionRecordId = r.id)")
    long countPending();

    List<ReceptionRecord> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<ReceptionRecord> findByUnitIdAndCreatedAtBetween(Long unitId, LocalDateTime start, LocalDateTime end);
}
