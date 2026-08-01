package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.SupplyRequest;
import com.fonoaudiologia.dto.SupplyStockResponse;
import com.fonoaudiologia.entity.Supply;
import com.fonoaudiologia.entity.SupplyStock;
import com.fonoaudiologia.repository.SupplyEntryItemRepository;
import com.fonoaudiologia.repository.SupplyExitItemRepository;
import com.fonoaudiologia.repository.SupplyRepository;
import com.fonoaudiologia.repository.SupplyStockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final SupplyStockRepository stockRepository;
    private final SupplyEntryItemRepository entryItemRepository;
    private final SupplyExitItemRepository exitItemRepository;

    public SupplyService(SupplyRepository supplyRepository, SupplyStockRepository stockRepository,
                         SupplyEntryItemRepository entryItemRepository, SupplyExitItemRepository exitItemRepository) {
        this.supplyRepository = supplyRepository;
        this.stockRepository = stockRepository;
        this.entryItemRepository = entryItemRepository;
        this.exitItemRepository = exitItemRepository;
    }

    public List<Supply> findAll() {
        return supplyRepository.findByActiveTrueOrderByNameAsc();
    }

    public List<Supply> findAllIncludingInactive() {
        return supplyRepository.findAllByOrderByNameAsc();
    }

    public Supply findById(Long id) {
        return supplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo não encontrado"));
    }

    public Supply create(SupplyRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("O nome do insumo é obrigatório");
        }
        if (request.getUnitMeasure() == null || request.getUnitMeasure().trim().isEmpty()) {
            throw new RuntimeException("A unidade de medida é obrigatória");
        }
        String name = request.getName().trim();
        if (supplyRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Já existe um insumo com este nome");
        }
        Supply supply = new Supply();
        supply.setName(name);
        supply.setDescription(request.getDescription());
        supply.setUnitMeasure(request.getUnitMeasure().trim());
        supply.setCategory(request.getCategory());
        supply.setMinimumQuantity(request.getMinimumQuantity());
        supply.setActive(true);
        return supplyRepository.save(supply);
    }

    public Supply update(Long id, SupplyRequest request) {
        Supply supply = findById(id);
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String name = request.getName().trim();
            if (supplyRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new RuntimeException("Já existe um insumo com este nome");
            }
            supply.setName(name);
        }
        if (request.getUnitMeasure() != null && !request.getUnitMeasure().trim().isEmpty()) {
            supply.setUnitMeasure(request.getUnitMeasure().trim());
        }
        if (request.getDescription() != null) supply.setDescription(request.getDescription());
        if (request.getCategory() != null) supply.setCategory(request.getCategory());
        if (request.getMinimumQuantity() != null) supply.setMinimumQuantity(request.getMinimumQuantity());
        supply.setActive(request.isActive());
        return supplyRepository.save(supply);
    }

    public void delete(Long id) {
        Supply supply = findById(id);
        List<SupplyStock> stocks = stockRepository.findBySupplyIdOrderByUnitNameAsc(id);
        for (SupplyStock stock : stocks) {
            if (stock.getQuantity() != null && stock.getQuantity() > 0) {
                throw new RuntimeException("Não é possível desativar o insumo " + supply.getName()
                        + ": há saldo de " + stock.getQuantity() + " " + supply.getUnitMeasure()
                        + " na unidade " + stock.getUnit().getName());
            }
        }
        supply.setActive(false);
        supplyRepository.save(supply);
    }

    public List<SupplyStockResponse> findStocksBySupply(Long supplyId) {
        return stockRepository.findBySupplyIdOrderByUnitNameAsc(supplyId).stream()
                .map(this::toStockResponse)
                .collect(Collectors.toList());
    }

    public List<SupplyStockResponse> findStocksByUnit(Long unitId) {
        return stockRepository.findByUnitIdOrderBySupplyNameAsc(unitId).stream()
                .map(this::toStockResponse)
                .collect(Collectors.toList());
    }

    private SupplyStockResponse toStockResponse(SupplyStock stock) {
        Double initial = entryItemRepository.sumQuantityBySupplyAndUnit(stock.getSupply().getId(), stock.getUnit().getId());
        Double used = exitItemRepository.sumQuantityBySupplyAndUnit(stock.getSupply().getId(), stock.getUnit().getId());
        return new SupplyStockResponse(stock,
                initial != null ? initial : 0.0,
                used != null ? used : 0.0);
    }
}
