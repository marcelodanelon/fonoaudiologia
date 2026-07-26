package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.ConsultationRequest;
import com.fonoaudiologia.entity.Appointment;
import com.fonoaudiologia.entity.Consultation;
import com.fonoaudiologia.entity.Patient;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.AppointmentRepository;
import com.fonoaudiologia.repository.ConsultationRepository;
import com.fonoaudiologia.repository.PatientRepository;
import com.fonoaudiologia.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public ConsultationService(ConsultationRepository consultationRepository,
                               PatientRepository patientRepository, UserRepository userRepository,
                               AppointmentRepository appointmentRepository) {
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<Consultation> findAll() {
        return consultationRepository.findAll();
    }

    public Consultation findById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta nao encontrada"));
    }

    public List<Consultation> findByPatient(Long patientId) {
        return consultationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<Consultation> findByProfessional(Long professionalId) {
        return consultationRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalId);
    }

    public Consultation create(ConsultationRequest request, Long operatorId) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado"));
        User professional = userRepository.findById(request.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profissional nao encontrado"));

        Consultation consultation = new Consultation();
        consultation.setPatient(patient);
        consultation.setProfessional(professional);
        consultation.setType(request.getType());
        consultation.setStatus(request.getStatus() != null ? request.getStatus() : "AGENDADA");
        consultation.setChiefComplaint(request.getChiefComplaint());
        consultation.setAnamnesis(request.getAnamnesis());
        consultation.setClinicalHistory(request.getClinicalHistory());
        consultation.setPhysicalExam(request.getPhysicalExam());
        consultation.setDiagnosis(request.getDiagnosis());
        consultation.setConduct(request.getConduct());
        consultation.setObservations(request.getObservations());
        consultation.setReceptionRecordId(request.getReceptionRecordId());

        if (operatorId != null) {
            User operator = userRepository.findById(operatorId)
                    .orElseThrow(() -> new RuntimeException("Operador nao encontrado"));
            consultation.setOperator(operator);
        }

        Consultation saved = consultationRepository.save(consultation);

        if ("CONCLUIDA".equals(saved.getStatus()) && patient != null) {
            updateAppointmentStatus(patient.getId(), "ATENDIDO");
        }

        return saved;
    }

    public Consultation update(Long id, ConsultationRequest request) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta nao encontrada"));

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Paciente nao encontrado"));
            consultation.setPatient(patient);
        }
        if (request.getProfessionalId() != null) {
            User professional = userRepository.findById(request.getProfessionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional nao encontrado"));
            consultation.setProfessional(professional);
        }
        if (request.getType() != null) consultation.setType(request.getType());
        if (request.getStatus() != null) consultation.setStatus(request.getStatus());
        if (request.getChiefComplaint() != null) consultation.setChiefComplaint(request.getChiefComplaint());
        if (request.getAnamnesis() != null) consultation.setAnamnesis(request.getAnamnesis());
        if (request.getClinicalHistory() != null) consultation.setClinicalHistory(request.getClinicalHistory());
        if (request.getPhysicalExam() != null) consultation.setPhysicalExam(request.getPhysicalExam());
        if (request.getDiagnosis() != null) consultation.setDiagnosis(request.getDiagnosis());
        if (request.getConduct() != null) consultation.setConduct(request.getConduct());
        if (request.getObservations() != null) consultation.setObservations(request.getObservations());

        Consultation saved = consultationRepository.save(consultation);

        if ("CONCLUIDA".equals(saved.getStatus()) && saved.getPatient() != null) {
            updateAppointmentStatus(saved.getPatient().getId(), "ATENDIDO");
        }

        return saved;
    }

    private void updateAppointmentStatus(Long patientId, String status) {
        List<Appointment> appointments = appointmentRepository
                .findByPatientIdAndDateAndStatusNot(patientId, LocalDate.now(), "CANCELADO");
        for (Appointment apt : appointments) {
            if (!status.equals(apt.getStatus())) {
                apt.setStatus(status);
                appointmentRepository.save(apt);
            }
        }
    }

    public long countScheduled() {
        return consultationRepository.countByStatus("AGENDADA");
    }

    public long countCompleted() {
        return consultationRepository.countByStatus("CONCLUIDA");
    }
}
