package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.ScheduleSlotRequest;
import com.fonoaudiologia.entity.Appointment;
import com.fonoaudiologia.entity.ScheduleSlot;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.AppointmentRepository;
import com.fonoaudiologia.repository.ScheduleSlotRepository;
import com.fonoaudiologia.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class ScheduleSlotService {

    private final ScheduleSlotRepository repository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public ScheduleSlotService(ScheduleSlotRepository repository, UserRepository userRepository,
                               AppointmentRepository appointmentRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<ScheduleSlot> findAll() {
        return repository.findByActiveTrueOrderByProfessionalNameAscStartTimeAsc();
    }

    public List<ScheduleSlot> findForDate(LocalDate date) {
        return repository.findActiveForDate(date);
    }

    public List<ScheduleSlot> findForProfessionalAndDate(Long professionalId, LocalDate date) {
        return repository.findActiveForProfessionalAndDate(professionalId, date);
    }

    public ScheduleSlot findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario nao encontrado"));
    }

    public ScheduleSlot create(ScheduleSlotRequest request) {
        User professional = userRepository.findById(request.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profissional nao encontrado"));

        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Data final nao pode ser anterior a data inicial");
        }

        String weekdays = request.getWeekdays();
        if (weekdays == null || weekdays.trim().isEmpty()) {
            throw new RuntimeException("Selecione pelo menos um dia da semana");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Horarios de inicio e fim sao obrigatorios");
        }

        Integer capacity = request.getCapacity();
        if (capacity == null || capacity < 1) {
            throw new RuntimeException("Capacidade deve ser pelo menos 1");
        }

        List<String> requestedDays = Arrays.asList(weekdays.split(","));
        for (String day : requestedDays) {
            if (DayOfWeek.valueOf(day.trim()) == null) {
                throw new RuntimeException("Dia da semana invalido: " + day);
            }
        }

        validateNoOverlap(professional.getId(), startDate, endDate, requestedDays, request.getStartTime(), request.getEndTime(), null);

        ScheduleSlot slot = new ScheduleSlot();
        slot.setProfessional(professional);
        slot.setStartDate(startDate);
        slot.setEndDate(endDate);
        slot.setWeekdays(weekdays);
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacity(capacity);
        slot.setActive(true);
        return repository.save(slot);
    }

    public ScheduleSlot update(Long id, ScheduleSlotRequest request) {
        ScheduleSlot slot = findById(id);

        if (hasActiveAppointments(id)) {
            throw new RuntimeException("Nao e possivel editar este horario pois possui agendamentos ativos");
        }

        if (request.getProfessionalId() != null) {
            User professional = userRepository.findById(request.getProfessionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional nao encontrado"));
            slot.setProfessional(professional);
        }
        if (request.getStartDate() != null) slot.setStartDate(LocalDate.parse(request.getStartDate()));
        if (request.getEndDate() != null) slot.setEndDate(LocalDate.parse(request.getEndDate()));
        if (request.getWeekdays() != null) slot.setWeekdays(request.getWeekdays());
        if (request.getStartTime() != null) slot.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) slot.setEndTime(request.getEndTime());
        if (request.getCapacity() != null && request.getCapacity() >= 1) slot.setCapacity(request.getCapacity());

        List<String> days = Arrays.asList(slot.getWeekdays().split(","));
        validateNoOverlap(slot.getProfessional().getId(), slot.getStartDate(), slot.getEndDate(),
                days, slot.getStartTime(), slot.getEndTime(), id);

        return repository.save(slot);
    }

    public void delete(Long id) {
        ScheduleSlot slot = findById(id);

        if (hasActiveAppointments(id)) {
            throw new RuntimeException("Nao e possivel remover este horario pois possui agendamentos ativos");
        }

        slot.setActive(false);
        repository.save(slot);
    }

    public int countActiveAppointmentsForSlotOnDate(Long slotId, LocalDate date) {
        ScheduleSlot slot = findById(slotId);
        List<Appointment> apts = appointmentRepository.findByScheduleSlotIdAndDate(slotId, date);
        int count = 0;
        for (Appointment apt : apts) {
            if (!"CANCELADO".equals(apt.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int countActiveAppointmentsForSlot(Long slotId) {
        return (int) appointmentRepository.countActiveBySlotId(slotId);
    }

    private boolean hasActiveAppointments(Long slotId) {
        ScheduleSlot slot = findById(slotId);
        LocalDate checkDate = slot.getStartDate();
        while (!checkDate.isAfter(slot.getEndDate())) {
            DayOfWeek dow = checkDate.getDayOfWeek();
            String dowStr = dow.toString();
            if (slot.getWeekdays().contains(dowStr)) {
                int count = countActiveAppointmentsForSlotOnDate(slotId, checkDate);
                if (count > 0) return true;
            }
            checkDate = checkDate.plusDays(1);
        }
        return false;
    }

    private void validateNoOverlap(Long professionalId, LocalDate startDate, LocalDate endDate,
                                   List<String> weekdays, String startTime, String endTime, Long excludeSlotId) {
        List<ScheduleSlot> existing = repository.findOverlappingSlots(professionalId, startDate, endDate);

        for (ScheduleSlot existingSlot : existing) {
            if (excludeSlotId != null && existingSlot.getId().equals(excludeSlotId)) continue;

            boolean weekdaysOverlap = false;
            for (String wd : weekdays) {
                if (existingSlot.getWeekdays().contains(wd.trim())) {
                    weekdaysOverlap = true;
                    break;
                }
            }

            if (!weekdaysOverlap) continue;

            boolean timeOverlap = startTime.compareTo(existingSlot.getEndTime()) < 0
                    && endTime.compareTo(existingSlot.getStartTime()) > 0;

            if (timeOverlap) {
                throw new RuntimeException("Ja existe um horario para este profissional no mesmo dia e periodo. "
                        + "Horario existente: " + existingSlot.getStartTime() + " - " + existingSlot.getEndTime());
            }
        }
    }
}
