package com.fonoaudiologia.service;

import com.fonoaudiologia.entity.ServiceUnit;
import com.fonoaudiologia.repository.ServiceUnitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceUnitService {

    private final ServiceUnitRepository repository;

    public ServiceUnitService(ServiceUnitRepository repository) {
        this.repository = repository;
    }

    public List<ServiceUnit> findAll() {
        return repository.findByActiveTrueOrderByNameAsc();
    }

    public List<ServiceUnit> findAllIncludingInactive() {
        return repository.findAllByOrderByNameAsc();
    }

    public ServiceUnit findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidade de atendimento não encontrada"));
    }

    public ServiceUnit create(ServiceUnit unit) {
        if (unit.getName() == null || unit.getName().trim().isEmpty()) {
            throw new RuntimeException("O nome da unidade de atendimento é obrigatório");
        }
        unit.setName(unit.getName().trim());
        unit.setActive(true);
        return repository.save(unit);
    }

    public ServiceUnit update(Long id, ServiceUnit request) {
        ServiceUnit unit = findById(id);
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            unit.setName(request.getName().trim());
        }
        if (request.getAddress() != null) unit.setAddress(request.getAddress());
        if (request.getPhone() != null) unit.setPhone(request.getPhone());
        return repository.save(unit);
    }

    public void delete(Long id) {
        ServiceUnit unit = findById(id);
        unit.setActive(false);
        repository.save(unit);
    }
}
