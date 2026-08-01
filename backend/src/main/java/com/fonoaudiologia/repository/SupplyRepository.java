package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
    List<Supply> findByActiveTrueOrderByNameAsc();
    List<Supply> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
