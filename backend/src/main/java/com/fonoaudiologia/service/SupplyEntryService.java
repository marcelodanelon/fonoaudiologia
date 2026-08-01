package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.InventoryItemRequest;
import com.fonoaudiologia.dto.SupplyEntryRequest;
import com.fonoaudiologia.entity.*;
import com.fonoaudiologia.repository.SupplyEntryRepository;
import com.fonoaudiologia.repository.SupplyExitItemRepository;
import com.fonoaudiologia.repository.SupplyStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SupplyEntryService {

    private final SupplyEntryRepository entryRepository;
    private final SupplyStockRepository stockRepository;
    private final SupplyExitItemRepository exitItemRepository;
    private final SupplyService supplyService;
    private final ServiceUnitService unitService;

    public SupplyEntryService(SupplyEntryRepository entryRepository,
                              SupplyStockRepository stockRepository,
                              SupplyExitItemRepository exitItemRepository,
                              SupplyService supplyService,
                              ServiceUnitService unitService) {
        this.entryRepository = entryRepository;
        this.stockRepository = stockRepository;
        this.exitItemRepository = exitItemRepository;
        this.supplyService = supplyService;
        this.unitService = unitService;
    }

    public List<SupplyEntry> findAll(Long unitId) {
        if (unitId != null) {
            return entryRepository.findByUnitIdOrderByEntryDateDescIdDesc(unitId);
        }
        return entryRepository.findAllByOrderByEntryDateDescIdDesc();
    }

    @Transactional
    public SupplyEntry create(SupplyEntryRequest request, User operator) {
        if (request.getUnitId() == null) {
            throw new RuntimeException("A unidade de atendimento é obrigatória");
        }
        ServiceUnit unit = unitService.findById(request.getUnitId());

        SupplyEntry entry = new SupplyEntry();
        entry.setUnit(unit);
        entry.setEntryDate(request.getEntryDate() != null ? request.getEntryDate() : LocalDate.now());
        entry.setSupplier(request.getSupplier());
        entry.setReference(request.getReference());
        entry.setNotes(request.getNotes());
        entry.setOperator(operator);
        entry.setItems(buildItems(entry, unit, request.getItems()));
        return entryRepository.save(entry);
    }

    @Transactional
    public SupplyEntry update(Long id, SupplyEntryRequest request, User operator) {
        SupplyEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrada não encontrada"));
        ServiceUnit unit = entry.getUnit();

        Set<Long> affectedSupplies = new HashSet<>();
        if (entry.getItems() != null) {
            for (SupplyEntryItem item : entry.getItems()) {
                affectedSupplies.add(item.getSupply().getId());
            }
        }
        if (request.getItems() != null) {
            for (InventoryItemRequest item : request.getItems()) {
                if (item.getSupplyId() != null) affectedSupplies.add(item.getSupplyId());
            }
        }
        for (Long supplyId : affectedSupplies) {
            if (exitItemRepository.existsMovementForSupply(supplyId, unit.getId())) {
                throw new RuntimeException("Não é possível alterar esta entrada: já houve movimentação"
                        + " de saldo para o insumo " + supplyService.findById(supplyId).getName());
            }
        }

        if (entry.getItems() != null) {
            for (SupplyEntryItem item : entry.getItems()) {
                subtractStock(item.getSupply(), unit, item.getQuantity());
            }
        }

        entry.setEntryDate(request.getEntryDate() != null ? request.getEntryDate() : entry.getEntryDate());
        entry.setSupplier(request.getSupplier());
        entry.setReference(request.getReference());
        entry.setNotes(request.getNotes());
        entry.setOperator(operator);
        entry.getItems().clear();
        entry.getItems().addAll(buildItems(entry, unit, request.getItems()));
        return entryRepository.save(entry);
    }

    private List<SupplyEntryItem> buildItems(SupplyEntry entry, ServiceUnit unit, List<InventoryItemRequest> rawItems) {
        if (rawItems == null || rawItems.isEmpty()) {
            throw new RuntimeException("Informe ao menos um insumo na entrada");
        }
        List<SupplyEntryItem> items = new ArrayList<>();
        for (InventoryItemRequest item : rawItems) {
            if (item.getSupplyId() == null) {
                throw new RuntimeException("Selecione o insumo em todos os itens da entrada");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("A quantidade deve ser maior que zero");
            }
            Supply supply = supplyService.findById(item.getSupplyId());
            SupplyEntryItem entryItem = new SupplyEntryItem();
            entryItem.setEntry(entry);
            entryItem.setSupply(supply);
            entryItem.setQuantity(item.getQuantity());
            items.add(entryItem);
            addStock(supply, unit, item.getQuantity());
        }
        return items;
    }

    private void addStock(Supply supply, ServiceUnit unit, Double quantity) {
        SupplyStock stock = findOrCreateStock(supply, unit);
        stock.setQuantity(stock.getQuantity() + quantity);
        stockRepository.save(stock);
    }

    private void subtractStock(Supply supply, ServiceUnit unit, Double quantity) {
        SupplyStock stock = stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId())
                .orElseThrow(() -> new RuntimeException("Saldo não encontrado para o insumo: " + supply.getName()));
        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
    }

    private SupplyStock findOrCreateStock(Supply supply, ServiceUnit unit) {
        return stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId())
                .orElseGet(() -> {
                    SupplyStock s = new SupplyStock();
                    s.setSupply(supply);
                    s.setUnit(unit);
                    s.setQuantity(0.0);
                    return s;
                });
    }
}
