package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.InventoryItemRequest;
import com.fonoaudiologia.dto.SupplyExitRequest;
import com.fonoaudiologia.entity.*;
import com.fonoaudiologia.repository.SupplyExitRepository;
import com.fonoaudiologia.repository.SupplyStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupplyExitService {

    private final SupplyExitRepository exitRepository;
    private final SupplyStockRepository stockRepository;
    private final SupplyService supplyService;
    private final ServiceUnitService unitService;
    private final PatientService patientService;

    public SupplyExitService(SupplyExitRepository exitRepository,
                             SupplyStockRepository stockRepository,
                             SupplyService supplyService,
                             ServiceUnitService unitService,
                             PatientService patientService) {
        this.exitRepository = exitRepository;
        this.stockRepository = stockRepository;
        this.supplyService = supplyService;
        this.unitService = unitService;
        this.patientService = patientService;
    }

    public List<SupplyExit> findAll(Long unitId) {
        if (unitId != null) {
            return exitRepository.findByUnitIdOrderByExitDateDescIdDesc(unitId);
        }
        return exitRepository.findAllByOrderByExitDateDescIdDesc();
    }

    @Transactional
    public SupplyExit create(SupplyExitRequest request, User operator) {
        if (request.getUnitId() == null) {
            throw new RuntimeException("A unidade de atendimento é obrigatória");
        }
        ServiceUnit unit = unitService.findById(request.getUnitId());

        SupplyExit exit = new SupplyExit();
        exit.setUnit(unit);
        exit.setExitDate(request.getExitDate() != null ? request.getExitDate() : LocalDate.now());
        exit.setNotes(request.getNotes());
        exit.setOperator(operator);
        if (request.getPatientId() != null) {
            exit.setPatient(patientService.findById(request.getPatientId()));
        }
        exit.setItems(buildItems(exit, unit, request.getItems()));
        return exitRepository.save(exit);
    }

    @Transactional
    public SupplyExit update(Long id, SupplyExitRequest request, User operator) {
        SupplyExit exit = exitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Saída não encontrada"));
        ServiceUnit unit = exit.getUnit();

        if (exit.getItems() != null) {
            for (SupplyExitItem item : exit.getItems()) {
                addStock(item.getSupply(), unit, item.getQuantity());
            }
        }

        exit.setExitDate(request.getExitDate() != null ? request.getExitDate() : exit.getExitDate());
        exit.setNotes(request.getNotes());
        exit.setOperator(operator);
        if (request.getPatientId() != null) {
            exit.setPatient(patientService.findById(request.getPatientId()));
        } else {
            exit.setPatient(null);
        }
        exit.getItems().clear();
        exit.getItems().addAll(buildItems(exit, unit, request.getItems()));
        return exitRepository.save(exit);
    }

    @Transactional
    public void delete(Long id) {
        SupplyExit exit = exitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Saída não encontrada"));
        if (exit.getItems() != null) {
            for (SupplyExitItem item : exit.getItems()) {
                addStock(item.getSupply(), exit.getUnit(), item.getQuantity());
            }
        }
        exitRepository.delete(exit);
    }

    private List<SupplyExitItem> buildItems(SupplyExit exit, ServiceUnit unit, List<InventoryItemRequest> rawItems) {
        if (rawItems == null || rawItems.isEmpty()) {
            throw new RuntimeException("Informe ao menos um insumo na saída");
        }
        List<SupplyExitItem> items = new ArrayList<>();
        for (InventoryItemRequest item : rawItems) {
            if (item.getSupplyId() == null) {
                throw new RuntimeException("Selecione o insumo em todos os itens da saída");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("A quantidade deve ser maior que zero");
            }
            Supply supply = supplyService.findById(item.getSupplyId());
            SupplyStock stock = stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId())
                    .orElseThrow(() -> new RuntimeException("Saldo insuficiente para o insumo: " + supply.getName()));
            if (stock.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Saldo insuficiente para o insumo: " + supply.getName()
                        + " (disponível: " + stock.getQuantity() + " " + supply.getUnitMeasure() + ")");
            }
            SupplyExitItem exitItem = new SupplyExitItem();
            exitItem.setExit(exit);
            exitItem.setSupply(supply);
            exitItem.setQuantity(item.getQuantity());
            items.add(exitItem);
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stockRepository.save(stock);
        }
        return items;
    }

    private void addStock(Supply supply, ServiceUnit unit, Double quantity) {
        SupplyStock stock = stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId())
                .orElseThrow(() -> new RuntimeException("Saldo não encontrado para o insumo: " + supply.getName()));
        stock.setQuantity(stock.getQuantity() + quantity);
        stockRepository.save(stock);
    }
}
