package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.SupplyExit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplyExitRepository extends JpaRepository<SupplyExit, Long> {
    boolean existsByPatientId(Long patientId);

    List<SupplyExit> findAllByOrderByExitDateDescIdDesc();
    List<SupplyExit> findByUnitIdOrderByExitDateDescIdDesc(Long unitId);
}
