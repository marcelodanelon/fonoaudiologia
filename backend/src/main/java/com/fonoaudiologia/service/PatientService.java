package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.PatientRequest;
import com.fonoaudiologia.entity.Patient;
import com.fonoaudiologia.repository.AppointmentRepository;
import com.fonoaudiologia.repository.ConsultationRepository;
import com.fonoaudiologia.repository.PatientRepository;
import com.fonoaudiologia.repository.ReceptionRecordRepository;
import com.fonoaudiologia.repository.SupplyExitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReceptionRecordRepository receptionRecordRepository;
    private final SupplyExitRepository supplyExitRepository;

    public PatientService(PatientRepository patientRepository,
                          ConsultationRepository consultationRepository,
                          AppointmentRepository appointmentRepository,
                          ReceptionRecordRepository receptionRecordRepository,
                          SupplyExitRepository supplyExitRepository) {
        this.patientRepository = patientRepository;
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.receptionRecordRepository = receptionRecordRepository;
        this.supplyExitRepository = supplyExitRepository;
    }

    public List<Patient> findAll() {
        return patientRepository.findAllByOrderByNameAsc();
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
    }

    public List<Patient> search(String query) {
        if (query == null || query.isEmpty()) {
            return patientRepository.findAllByOrderByNameAsc();
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
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        mapRequestToEntity(request, patient);
        return patientRepository.save(patient);
    }

    public void delete(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        boolean usedElsewhere = consultationRepository.existsByPatientId(id)
                || appointmentRepository.existsByPatientId(id)
                || receptionRecordRepository.existsByPatientId(id)
                || supplyExitRepository.existsByPatientId(id);
        if (usedElsewhere) {
            throw new RuntimeException("Não é possível excluir o paciente " + patient.getName()
                    + ": ele já foi utilizado em outros registros do sistema. É possível apenas desativá-lo.");
        }
        patientRepository.delete(patient);
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
