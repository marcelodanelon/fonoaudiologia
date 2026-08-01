package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByPatientId(Long patientId);

    List<Appointment> findByDateOrderByTimeAsc(LocalDate date);
    List<Appointment> findByUnitIdAndDateOrderByTimeAsc(Long unitId, LocalDate date);
    List<Appointment> findByDateAndStatusOrderByTimeAsc(LocalDate date, String status);
    List<Appointment> findByPatientIdOrderByDateDescTimeDesc(Long patientId);
    List<Appointment> findByProfessionalIdAndDateOrderByTimeAsc(Long professionalId, LocalDate date);
    List<Appointment> findByDateBetween(LocalDate start, LocalDate end);
    List<Appointment> findByStatusInAndDate(List<String> statuses, LocalDate date);
    List<Appointment> findByUnitIdAndStatusInAndDate(Long unitId, List<String> statuses, LocalDate date);
    List<Appointment> findByScheduleSlotIdAndDate(Long scheduleSlotId, LocalDate date);
    List<Appointment> findByPatientIdAndDateAndStatusNot(Long patientId, LocalDate date, String status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.scheduleSlot.id = :slotId AND a.status <> 'CANCELADO'")
    long countActiveBySlotId(@Param("slotId") Long slotId);
}
