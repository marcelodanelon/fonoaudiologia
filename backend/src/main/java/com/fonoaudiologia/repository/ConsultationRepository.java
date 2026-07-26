package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Consultation> findByProfessionalIdOrderByCreatedAtDesc(Long professionalId);

    List<Consultation> findByOperatorIdOrderByCreatedAtDesc(Long operatorId);

    long countByStatus(String status);

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.status = 'CONCLUIDA' AND c.createdAt >= :date")
    long countCompletedAfter(LocalDateTime date);

    @Query("SELECT c.receptionRecordId FROM Consultation c WHERE c.status = 'EM_ANDAMENTO' AND c.receptionRecordId IS NOT NULL")
    List<Long> findReceptionRecordIdsWithActiveConsultations();

    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.createdAt >= :start AND c.createdAt < :end")
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<Consultation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
