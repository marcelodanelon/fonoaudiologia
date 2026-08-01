package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.ServiceUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceUnitRepository extends JpaRepository<ServiceUnit, Long> {
    List<ServiceUnit> findByActiveTrueOrderByNameAsc();
    List<ServiceUnit> findAllByOrderByNameAsc();
}
