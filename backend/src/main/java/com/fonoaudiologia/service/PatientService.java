package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.PatientRequest;
import com.fonoaudiologia.entity.Patient;
import com.fonoaudiologia.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado"));
    }

    public List<Patient> search(String query) {
        if (query == null || query.isEmpty()) {
            return patientRepository.findAll();
        }
        return patientRepository.search(query);
    }

    public Patient create(PatientRequest request) {
        Patient patient = new Patient();
        mapRequestToEntity(request, patient);
        return patientRepository.save(patient);
    }

    public Patient update(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado"));
        mapRequestToEntity(request, patient);
        return patientRepository.save(patient);
    }

    public void delete(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado"));
        patient.setActive(false);
        patientRepository.save(patient);
    }

    public long countActive() {
        return patientRepository.countByActiveTrue();
    }

    private void mapRequestToEntity(PatientRequest request, Patient patient) {
        patient.setName(request.getName());
        patient.setCpf(request.getCpf());
        patient.setRg(request.getRg());
        patient.setBirthDate(request.getBirthDate());
        patient.setPhone(request.getPhone());
        patient.setPhone2(request.getPhone2());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setObservations(request.getObservations());
        patient.setActive(request.isActive());
    }
}
