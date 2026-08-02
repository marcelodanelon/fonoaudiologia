package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.ReceptionRequest;
import com.fonoaudiologia.entity.Appointment;
import com.fonoaudiologia.entity.Patient;
import com.fonoaudiologia.entity.ReceptionRecord;
import com.fonoaudiologia.entity.ServiceUnit;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.AppointmentRepository;
import com.fonoaudiologia.repository.PatientRepository;
import com.fonoaudiologia.repository.ReceptionRecordRepository;
import com.fonoaudiologia.repository.ServiceUnitRepository;
import com.fonoaudiologia.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReceptionService {

    private final ReceptionRecordRepository receptionRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ServiceUnitRepository unitRepository;
    private final AppointmentRepository appointmentRepository;

    public ReceptionService(ReceptionRecordRepository receptionRepository,
                            PatientRepository patientRepository, UserRepository userRepository,
                            ServiceUnitRepository unitRepository,
                            AppointmentRepository appointmentRepository) {
        this.receptionRepository = receptionRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<ReceptionRecord> findAll() {
        return receptionRepository.findAll();
    }

    public ReceptionRecord create(ReceptionRequest request, Long operatorId) {
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operador não encontrado"));

        if (request.getUnitId() == null) {
            throw new RuntimeException("A unidade de atendimento é obrigatória");
        }
        ServiceUnit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new RuntimeException("Unidade de atendimento não encontrada"));

        ReceptionRecord record = new ReceptionRecord();
        record.setOperator(operator);
        record.setUnit(unit);
        record.setType(request.getType());
        record.setContactType(request.getContactType());
        record.setNotes(request.getNotes());
        record.setStatus("PENDENTE");

        Appointment linkedAppointment = null;
        if (request.getAppointmentId() != null) {
            linkedAppointment = appointmentRepository.findById(request.getAppointmentId()).orElse(null);
        }

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
            record.setPatient(patient);

            if ("CHECKIN".equals(request.getType())) {
                if (linkedAppointment != null) {
                    if ("AGENDADO".equals(linkedAppointment.getStatus())) {
                        linkedAppointment.setStatus("RECEPCIONADO");
                        appointmentRepository.save(linkedAppointment);
                    }
                } else {
                    List<Appointment> appointments = appointmentRepository
                            .findByPatientIdAndDateAndStatusNot(patient.getId(), LocalDate.now(), "CANCELADO");
                    for (Appointment apt : appointments) {
                        if ("AGENDADO".equals(apt.getStatus())) {
                            apt.setStatus("RECEPCIONADO");
                            appointmentRepository.save(apt);
                        }
                    }
                }
            }
        }

        if (linkedAppointment != null && linkedAppointment.getDate() != null) {
            LocalTime time = linkedAppointment.getTime() != null
                    ? LocalTime.parse(linkedAppointment.getTime())
                    : LocalTime.NOON;
            record.setCreatedAt(linkedAppointment.getDate().atTime(time));
        }

        return receptionRepository.save(record);
    }

    public long countToday() {
        return receptionRepository.countAfter(java.time.LocalDateTime.now().toLocalDate().atStartOfDay());
    }

    public long countByType(String type) {
        return receptionRepository.countByTypeAfter(type, java.time.LocalDateTime.now().toLocalDate().atStartOfDay());
    }
}
