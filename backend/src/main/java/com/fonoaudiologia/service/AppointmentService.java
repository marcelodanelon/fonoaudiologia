package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.AppointmentRequest;
import com.fonoaudiologia.entity.Appointment;
import com.fonoaudiologia.entity.Patient;
import com.fonoaudiologia.entity.ScheduleSlot;
import com.fonoaudiologia.entity.ServiceUnit;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.AppointmentRepository;
import com.fonoaudiologia.repository.PatientRepository;
import com.fonoaudiologia.repository.ScheduleSlotRepository;
import com.fonoaudiologia.repository.ServiceUnitRepository;
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
    private final ServiceUnitRepository unitRepository;
    private final ScheduleSlotService scheduleSlotService;

    public AppointmentService(AppointmentRepository repository, PatientRepository patientRepository,
                              UserRepository userRepository, ScheduleSlotRepository scheduleSlotRepository,
                              ServiceUnitRepository unitRepository, ScheduleSlotService scheduleSlotService) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.scheduleSlotRepository = scheduleSlotRepository;
        this.unitRepository = unitRepository;
        this.scheduleSlotService = scheduleSlotService;
    }

    public List<Appointment> findAll() {
        return repository.findAll();
    }

    public List<Appointment> findByDate(LocalDate date) {
        return repository.findByDateOrderByTimeAsc(date);
    }

    public List<Appointment> findByUnitAndDate(Long unitId, LocalDate date) {
        return repository.findByUnitIdAndDateOrderByTimeAsc(unitId, date);
    }

    public List<Appointment> findScheduledByDate(LocalDate date) {
        return repository.findByStatusInAndDate(
                Arrays.asList("AGENDADO"), date);
    }

    public List<Appointment> findScheduledByUnitAndDate(Long unitId, LocalDate date) {
        return repository.findByUnitIdAndStatusInAndDate(unitId, Arrays.asList("AGENDADO"), date);
    }

    public List<Appointment> findByPatient(Long patientId) {
        return repository.findByPatientIdOrderByDateDescTimeDesc(patientId);
    }

    public Appointment findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
    }

    public Appointment create(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        User professional = userRepository.findById(request.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

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
                    .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

            LocalDate aptDate = LocalDate.parse(request.getDate());
            if (aptDate.isBefore(slot.getStartDate()) || aptDate.isAfter(slot.getEndDate())) {
                throw new RuntimeException("Data do agendamento fora do período do horário selecionado");
            }

            String aptDow = aptDate.getDayOfWeek().toString();
            if (!slot.getWeekdays().contains(aptDow)) {
                throw new RuntimeException("O dia selecionado não faz parte dos dias do horário");
            }

            if ("TEMPO".equals(slot.getSlotType())) {
                String time = request.getTime();
                if (time == null || time.isEmpty()) {
                    throw new RuntimeException("Selecione o horário da consulta");
                }
                if (!scheduleSlotService.generateTimes(slot).contains(time)) {
                    throw new RuntimeException("Horário inválido para esta agenda");
                }
                List<String> booked = repository.findBookedTimesForSlotOnDate(slot.getId(), aptDate);
                if (booked.contains(time)) {
                    throw new RuntimeException("Este horário já está ocupado nesta data");
                }
            } else {
                int occupied = 0;
                List<Appointment> existing = repository.findByScheduleSlotIdAndDate(slot.getId(), aptDate);
                for (Appointment apt : existing) {
                    if (!"CANCELADO".equals(apt.getStatus())) {
                        occupied++;
                    }
                }
                if (occupied >= slot.getCapacity()) {
                    throw new RuntimeException("Este horário não possui vagas disponíveis para esta data");
                }
            }

            appointment.setScheduleSlot(slot);
            appointment.setUnit(slot.getUnit());
            if (appointment.getProfessional() == null) {
                appointment.setProfessional(slot.getProfessional());
            }
        }

        if (request.getUnitId() != null && appointment.getUnit() == null) {
            ServiceUnit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unidade de atendimento não encontrada"));
            appointment.setUnit(unit);
        }

        return repository.save(appointment);
    }

    public Appointment update(Long id, AppointmentRequest request) {
        Appointment appointment = findById(id);
        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
            appointment.setPatient(patient);
        }
        if (request.getProfessionalId() != null) {
            User professional = userRepository.findById(request.getProfessionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            appointment.setProfessional(professional);
        }
        if (request.getScheduleSlotId() != null) {
            ScheduleSlot slot = scheduleSlotRepository.findById(request.getScheduleSlotId())
                    .orElseThrow(() -> new RuntimeException("Horário não encontrado"));
            appointment.setScheduleSlot(slot);
            appointment.setUnit(slot.getUnit());

            LocalDate aptDate = (request.getDate() != null) ? LocalDate.parse(request.getDate()) : appointment.getDate();
            if (aptDate.isBefore(slot.getStartDate()) || aptDate.isAfter(slot.getEndDate())) {
                throw new RuntimeException("Data do agendamento fora do período do horário selecionado");
            }
            String aptDow = aptDate.getDayOfWeek().toString();
            if (!slot.getWeekdays().contains(aptDow)) {
                throw new RuntimeException("O dia selecionado não faz parte dos dias do horário");
            }

            if ("TEMPO".equals(slot.getSlotType())) {
                String time = (request.getTime() != null) ? request.getTime() : appointment.getTime();
                if (time == null || time.isEmpty() || !scheduleSlotService.generateTimes(slot).contains(time)) {
                    throw new RuntimeException("Selecione um horário válido para esta agenda");
                }
                List<String> booked = new java.util.ArrayList<>(repository.findBookedTimesForSlotOnDate(slot.getId(), aptDate));
                booked.remove(appointment.getTime());
                if (booked.contains(time)) {
                    throw new RuntimeException("Este horário já está ocupado nesta data");
                }
            } else {
                int occupied = 0;
                List<Appointment> existing = repository.findByScheduleSlotIdAndDate(slot.getId(), aptDate);
                for (Appointment apt : existing) {
                    if (!"CANCELADO".equals(apt.getStatus()) && !apt.getId().equals(id)) {
                        occupied++;
                    }
                }
                if (occupied >= slot.getCapacity()) {
                    throw new RuntimeException("Este horário não possui vagas disponíveis para esta data");
                }
            }
        }
        if (request.getUnitId() != null) {
            ServiceUnit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unidade de atendimento não encontrada"));
            appointment.setUnit(unit);
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
