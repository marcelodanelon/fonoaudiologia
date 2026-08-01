package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.SupplyEntryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplyEntryItemRepository extends JpaRepository<SupplyEntryItem, Long> {

    @Query("select coalesce(sum(i.quantity), 0) from SupplyEntryItem i where i.supply.id = :supplyId and i.entry.unit.id = :unitId")
    Double sumQuantityBySupplyAndUnit(@Param("supplyId") Long supplyId, @Param("unitId") Long unitId);
}
