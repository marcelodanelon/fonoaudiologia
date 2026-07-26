package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.AppointmentRequest;
import com.fonoaudiologia.entity.Appointment;
import com.fonoaudiologia.entity.Patient;
import com.fonoaudiologia.entity.ScheduleSlot;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.AppointmentRepository;
import com.fonoaudiologia.repository.PatientRepository;
import com.fonoaudiologia.repository.ScheduleSlotRepository;
import com.fonoaudiologia.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;

    public AppointmentService(AppointmentRepository repository, PatientRepository patientRepository,
                              UserRepository userRepository, ScheduleSlotRepository scheduleSlotRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.scheduleSlotRepository = scheduleSlotRepository;
    }

    public List<Appointment> findAll() {
        return repository.findAll();
    }

    public List<Appointment> findByDate(LocalDate date) {
        return repository.findByDateOrderByTimeAsc(date);
    }

    public List<Appointment> findScheduledByDate(LocalDate date) {
        return repository.findByStatusInAndDate(
                Arrays.asList("AGENDADO"), date);
    }

    public List<Appointment> findByPatient(Long patientId) {
        return repository.findByPatientIdOrderByDateDescTimeDesc(patientId);
    }

    public Appointment findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento nao encontrado"));
    }

    public Appointment create(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado"));
        User professional = userRepository.findById(request.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profissional nao encontrado"));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setDate(LocalDate.parse(request.getDate()));
        appointment.setTime(request.getTime());
        appointment.setType(request.getType() != null ? request.getType() : "CONSULTA");
        appointment.setStatus(request.getStatus() != null ? request.getStatus() : "AGENDADO");
        appointment.setObservations(request.getObservations());

        if (request.getScheduleSlotId() != null) {
            ScheduleSlot slot = scheduleSlotRepository.findById(request.getScheduleSlotId())
                    .orElseThrow(() -> new RuntimeException("Horario nao encontrado"));

            LocalDate aptDate = LocalDate.parse(request.getDate());
            if (aptDate.isBefore(slot.getStartDate()) || aptDate.isAfter(slot.getEndDate())) {
                throw new RuntimeException("Data do agendamento fora do periodo do horario selecionado");
            }

            String aptDow = aptDate.getDayOfWeek().toString();
            if (!slot.getWeekdays().contains(aptDow)) {
                throw new RuntimeException("O dia selecionado nao faz parte dos dias do horario");
            }

            int occupied = 0;
            List<Appointment> existing = repository.findByScheduleSlotIdAndDate(slot.getId(), aptDate);
            for (Appointment apt : existing) {
                if (!"CANCELADO".equals(apt.getStatus())) {
                    occupied++;
                }
            }

            if (occupied >= slot.getCapacity()) {
                throw new RuntimeException("Este horario nao possui vagas disponiveis para esta data");
            }

            appointment.setScheduleSlot(slot);
            if (appointment.getProfessional() == null) {
                appointment.setProfessional(slot.getProfessional());
            }
        }

        return repository.save(appointment);
    }

    public Appointment update(Long id, AppointmentRequest request) {
        Appointment appointment = findById(id);
        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Paciente nao encontrado"));
            appointment.setPatient(patient);
        }
        if (request.getProfessionalId() != null) {
            User professional = userRepository.findById(request.getProfessionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional nao encontrado"));
            appointment.setProfessional(professional);
        }
        if (request.getScheduleSlotId() != null) {
            ScheduleSlot slot = scheduleSlotRepository.findById(request.getScheduleSlotId())
                    .orElseThrow(() -> new RuntimeException("Horario nao encontrado"));
            appointment.setScheduleSlot(slot);
        }
        if (request.getDate() != null) appointment.setDate(LocalDate.parse(request.getDate()));
        if (request.getTime() != null) appointment.setTime(request.getTime());
        if (request.getType() != null) appointment.setType(request.getType());
        if (request.getStatus() != null) appointment.setStatus(request.getStatus());
        if (request.getObservations() != null) appointment.setObservations(request.getObservations());
        return repository.save(appointment);
    }

    public void delete(Long id) {
        Appointment appointment = findById(id);
        appointment.setStatus("CANCELADO");
        repository.save(appointment);
    }
}
