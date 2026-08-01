package com.fonoaudiologia;

import com.fonoaudiologia.dto.InventoryItemRequest;
import com.fonoaudiologia.dto.SupplyEntryRequest;
import com.fonoaudiologia.dto.SupplyExitRequest;
import com.fonoaudiologia.dto.SupplyRequest;
import com.fonoaudiologia.entity.ServiceUnit;
import com.fonoaudiologia.entity.Supply;
import com.fonoaudiologia.entity.SupplyEntry;
import com.fonoaudiologia.entity.SupplyExit;
import com.fonoaudiologia.entity.SupplyStock;
import com.fonoaudiologia.repository.SupplyStockRepository;
import com.fonoaudiologia.service.ServiceUnitService;
import com.fonoaudiologia.service.SupplyEntryService;
import com.fonoaudiologia.service.SupplyExitService;
import com.fonoaudiologia.service.SupplyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InventoryFlowTest {

    @Autowired
    private SupplyService supplyService;

    @Autowired
    private SupplyEntryService entryService;

    @Autowired
    private SupplyExitService exitService;

    @Autowired
    private SupplyStockRepository stockRepository;

    @Autowired
    private ServiceUnitService unitService;

    private ServiceUnit newUnit() {
        ServiceUnit unit = new ServiceUnit();
        unit.setName("Unidade Teste " + UUID.randomUUID().toString().substring(0, 8));
        return unitService.create(unit);
    }

    private Supply newSupply(String name) {
        SupplyRequest request = new SupplyRequest();
        request.setName(name + " " + UUID.randomUUID().toString().substring(0, 8));
        request.setUnitMeasure("UN");
        request.setMinimumQuantity(5.0);
        return supplyService.create(request);
    }

    private SupplyEntryRequest entryRequest(ServiceUnit unit, List<InventoryItemRequest> items) {
        SupplyEntryRequest request = new SupplyEntryRequest();
        request.setUnitId(unit.getId());
        request.setEntryDate(LocalDate.now());
        request.setSupplier("Fornecedor Teste");
        request.setItems(items);
        return request;
    }

    private SupplyExitRequest exitRequest(ServiceUnit unit, List<InventoryItemRequest> items) {
        SupplyExitRequest request = new SupplyExitRequest();
        request.setUnitId(unit.getId());
        request.setExitDate(LocalDate.now());
        request.setItems(items);
        return request;
    }

    private InventoryItemRequest item(Supply supply, Double quantity) {
        InventoryItemRequest item = new InventoryItemRequest();
        item.setSupplyId(supply.getId());
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void entryIncreasesStockForUnit() {
        ServiceUnit unit = newUnit();
        Supply supply = newSupply("Otoescopo Entrada");

        SupplyEntry entry = entryService.create(entryRequest(unit, Arrays.asList(item(supply, 10.0))), null);

        assertNotNull(entry.getId());
        assertEquals(1, entry.getItems().size());
        SupplyStock stock = stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow();
        assertEquals(10.0, stock.getQuantity());
    }

    @Test
    void multipleItemsEntryIncreasesEachStock() {
        ServiceUnit unit = newUnit();
        Supply a = newSupply("Insumo Multi A");
        Supply b = newSupply("Insumo Multi B");

        SupplyEntry entry = entryService.create(entryRequest(unit, Arrays.asList(item(a, 3.0), item(b, 7.0))), null);

        assertEquals(2, entry.getItems().size());
        assertEquals(3.0, stockRepository.findBySupplyIdAndUnitId(a.getId(), unit.getId()).orElseThrow().getQuantity());
        assertEquals(7.0, stockRepository.findBySupplyIdAndUnitId(b.getId(), unit.getId()).orElseThrow().getQuantity());
    }

    @Test
    void exitDecreasesStockAndRejectsInsufficientBalance() {
        ServiceUnit unit = newUnit();
        Supply supply = newSupply("Otoscopio Saída");
        entryService.create(entryRequest(unit, Arrays.asList(item(supply, 10.0))), null);

        exitService.create(exitRequest(unit, Arrays.asList(item(supply, 4.0))), null);
        assertEquals(6.0, stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow().getQuantity());

        SupplyExitRequest over = exitRequest(unit, Arrays.asList(item(supply, 100.0)));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> exitService.create(over, null));
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        assertEquals(6.0, stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow().getQuantity());
    }

    @Test
    void entryWithoutItemsIsRejected() {
        ServiceUnit unit = newUnit();
        SupplyEntryRequest request = entryRequest(unit, null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> entryService.create(request, null));
        assertTrue(ex.getMessage().contains("insumo"));
    }

    @Test
    void exitDeletionReturnsStockToBalance() {
        ServiceUnit unit = newUnit();
        Supply supply = newSupply("Insumo Exclusão Saída");
        entryService.create(entryRequest(unit, Arrays.asList(item(supply, 10.0))), null);
        SupplyExit exit = exitService.create(exitRequest(unit, Arrays.asList(item(supply, 4.0))), null);
        assertEquals(6.0, stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow().getQuantity());

        exitService.delete(exit.getId());

        assertEquals(10.0, stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow().getQuantity());
    }

    @Test
    void exitUpdateAdjustsStock() {
        ServiceUnit unit = newUnit();
        Supply supply = newSupply("Insumo Edição Saída");
        entryService.create(entryRequest(unit, Arrays.asList(item(supply, 10.0))), null);
        SupplyExit exit = exitService.create(exitRequest(unit, Arrays.asList(item(supply, 4.0))), null);
        assertEquals(6.0, stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow().getQuantity());

        SupplyExit updated = exitService.update(exit.getId(), exitRequest(unit, Arrays.asList(item(supply, 6.0))), null);

        assertEquals(1, updated.getItems().size());
        assertEquals(4.0, stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow().getQuantity());
    }

    @Test
    void exitUpdateRejectsInsufficientBalance() {
        ServiceUnit unit = newUnit();
        Supply supply = newSupply("Insumo Edição Saída Bloqueio");
        entryService.create(entryRequest(unit, Arrays.asList(item(supply, 10.0))), null);
        SupplyExit exit = exitService.create(exitRequest(unit, Arrays.asList(item(supply, 4.0))), null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> exitService.update(exit.getId(), exitRequest(unit, Arrays.asList(item(supply, 20.0))), null));

        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
    }

    @Test
    void entryUpdateAllowedWhenNoMovement() {
        ServiceUnit unit = newUnit();
        Supply supply = newSupply("Insumo Edição Entrada");
        SupplyEntry entry = entryService.create(entryRequest(unit, Arrays.asList(item(supply, 10.0))), null);

        SupplyEntryRequest request = entryRequest(unit, Arrays.asList(item(supply, 15.0)));
        SupplyEntry updated = entryService.update(entry.getId(), request, null);

        assertEquals(15.0, stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow().getQuantity());
        assertEquals(1, updated.getItems().size());
        assertEquals(15.0, updated.getItems().get(0).getQuantity());
    }

    @Test
    void entryUpdateRejectedWhenMovementExists() {
        ServiceUnit unit = newUnit();
        Supply supply = newSupply("Insumo Bloqueado Entrada");
        SupplyEntry entry = entryService.create(entryRequest(unit, Arrays.asList(item(supply, 10.0))), null);
        exitService.create(exitRequest(unit, Arrays.asList(item(supply, 4.0))), null);

        SupplyEntryRequest request = entryRequest(unit, Arrays.asList(item(supply, 20.0)));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> entryService.update(entry.getId(), request, null));
        assertTrue(ex.getMessage().contains("movimentação"));
        assertEquals(6.0, stockRepository.findBySupplyIdAndUnitId(supply.getId(), unit.getId()).orElseThrow().getQuantity());
    }

    @Test
    void supplyDeactivationBlockedWhenStockExists() {
        ServiceUnit unit = newUnit();
        Supply supply = newSupply("Insumo Com Saldo");
        entryService.create(entryRequest(unit, Arrays.asList(item(supply, 10.0))), null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> supplyService.delete(supply.getId()));
        assertTrue(ex.getMessage().contains("saldo"));
        assertTrue(supplyService.findById(supply.getId()).isActive());
    }

    @Test
    void supplyDeactivationAllowedWhenNoStock() {
        Supply supply = newSupply("Insumo Sem Saldo");
        supplyService.delete(supply.getId());
        assertFalse(supplyService.findById(supply.getId()).isActive());
    }
}
