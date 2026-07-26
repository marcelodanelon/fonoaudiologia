package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {
    List<ScheduleSlot> findByActiveTrueOrderByProfessionalNameAscStartTimeAsc();

    @Query("SELECT s FROM ScheduleSlot s WHERE s.active = true AND :date BETWEEN s.startDate AND s.endDate ORDER BY s.professional.name ASC, s.startTime ASC")
    List<ScheduleSlot> findActiveForDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM ScheduleSlot s WHERE s.active = true AND s.professional.id = :profId AND :date BETWEEN s.startDate AND s.endDate ORDER BY s.startTime ASC")
    List<ScheduleSlot> findActiveForProfessionalAndDate(@Param("profId") Long professionalId, @Param("date") LocalDate date);

    @Query("SELECT s FROM ScheduleSlot s WHERE s.active = true AND s.professional.id = :profId AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<ScheduleSlot> findOverlappingSlots(@Param("profId") Long professionalId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
