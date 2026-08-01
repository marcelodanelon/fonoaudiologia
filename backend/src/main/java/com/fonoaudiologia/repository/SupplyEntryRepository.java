package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.SupplyEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplyEntryRepository extends JpaRepository<SupplyEntry, Long> {
    List<SupplyEntry> findAllByOrderByEntryDateDescIdDesc();
    List<SupplyEntry> findByUnitIdOrderByEntryDateDescIdDesc(Long unitId);
}
