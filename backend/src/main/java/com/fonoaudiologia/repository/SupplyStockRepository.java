package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.SupplyStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SupplyStockRepository extends JpaRepository<SupplyStock, Long> {
    Optional<SupplyStock> findBySupplyIdAndUnitId(Long supplyId, Long unitId);
    List<SupplyStock> findBySupplyIdOrderByUnitNameAsc(Long supplyId);
    List<SupplyStock> findByUnitIdOrderBySupplyNameAsc(Long unitId);
}
