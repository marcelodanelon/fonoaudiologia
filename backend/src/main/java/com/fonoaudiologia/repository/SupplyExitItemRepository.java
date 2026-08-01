package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.SupplyExitItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplyExitItemRepository extends JpaRepository<SupplyExitItem, Long> {

    @Query("select count(i) > 0 from SupplyExitItem i where i.supply.id = :supplyId and i.exit.unit.id = :unitId")
    boolean existsMovementForSupply(@Param("supplyId") Long supplyId, @Param("unitId") Long unitId);

    @Query("select coalesce(sum(i.quantity), 0) from SupplyExitItem i where i.supply.id = :supplyId and i.exit.unit.id = :unitId")
    Double sumQuantityBySupplyAndUnit(@Param("supplyId") Long supplyId, @Param("unitId") Long unitId);
}
