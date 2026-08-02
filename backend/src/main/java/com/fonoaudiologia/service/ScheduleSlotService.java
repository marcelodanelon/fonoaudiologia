package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.ScheduleSlotRequest;
import com.fonoaudiologia.entity.Appointment;
import com.fonoaudiologia.entity.ScheduleSlot;
import com.fonoaudiologia.entity.ServiceUnit;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.AppointmentRepository;
import com.fonoaudiologia.repository.ScheduleSlotRepository;
import com.fonoaudiologia.repository.ServiceUnitRepository;
import com.fonoaudiologia.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleSlotService {

    private final ScheduleSlotRepository repository;
    private final UserRepository userRepository;
    private final ServiceUnitRepository unitRepository;
    private final AppointmentRepository appointmentRepository;

    public ScheduleSlotService(ScheduleSlotRepository repository, UserRepository userRepository,
                               ServiceUnitRepository unitRepository,
                               AppointmentRepository appointmentRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<ScheduleSlot> findAll() {
        return repository.findByActiveTrueOrderByProfessionalNameAscStartTimeAsc();
    }

    public List<ScheduleSlot> findByUnit(Long unitId) {
        return repository.findByUnitIdAndActiveTrueOrderByProfessionalNameAscStartTimeAsc(unitId);
    }

    public List<ScheduleSlot> findForDate(LocalDate date) {
        return repository.findActiveForDate(date);
    }

    public List<ScheduleSlot> findForUnitAndDate(Long unitId, LocalDate date) {
        return repository.findActiveForUnitAndDate(unitId, date);
    }

    public List<ScheduleSlot> findForProfessionalAndDate(Long professionalId, LocalDate date) {
        return repository.findActiveForProfessionalAndDate(professionalId, date);
    }

    public List<ScheduleSlot> findForUnitAndProfessionalAndDate(Long unitId, Long professionalId, LocalDate date) {
        return repository.findActiveForUnitAndProfessionalAndDate(unitId, professionalId, date);
    }

    public List<LocalDate> findAvailableDates(Long unitId, Long professionalId, LocalDate from, LocalDate to) {
        LocalDate start = (from != null) ? from : LocalDate.now();
        LocalDate end = (to != null) ? to : start.plusDays(90);

        List<ScheduleSlot> slots;
        if (unitId != null && professionalId != null) {
            slots = repository.findByUnitIdAndProfessionalIdAndActiveTrueOrderByStartTimeAsc(unitId, professionalId);
        } else if (unitId != null) {
            slots = repository.findByUnitIdAndActiveTrueOrderByProfessionalNameAscStartTimeAsc(unitId);
        } else if (professionalId != null) {
            slots = repository.findByProfessionalIdAndActiveTrueOrderByStartTimeAsc(professionalId);
        } else {
            slots = repository.findByActiveTrueOrderByProfessionalNameAscStartTimeAsc();
        }

        List<LocalDate> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            for (ScheduleSlot s : slots) {
                if (!d.isBefore(s.getStartDate()) && !d.isAfter(s.getEndDate())) {
                    String dow = d.getDayOfWeek().toString();
                    if (s.getWeekdays().contains(dow)) {
                        int remaining = remainingForSlotOnDate(s, d);
                        if (remaining > 0) {
                            result.add(d);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    public List<String> generateTimes(ScheduleSlot slot) {
        List<String> times = new ArrayList<>();
        if (!"TEMPO".equals(slot.getSlotType()) || slot.getDurationMinutes() == null || slot.getDurationMinutes() < 1) {
            return times;
        }
        LocalTime start = LocalTime.parse(slot.getStartTime());
        LocalTime end = LocalTime.parse(slot.getEndTime());
        int duration = slot.getDurationMinutes();
        LocalTime t = start;
        while (t.isBefore(end)) {
            times.add(t.toString());
            t = t.plusMinutes(duration);
        }
        return times;
    }

    public int remainingForSlotOnDate(ScheduleSlot slot, LocalDate date) {
        if ("TEMPO".equals(slot.getSlotType())) {
            List<String> booked = appointmentRepository.findBookedTimesForSlotOnDate(slot.getId(), date);
            return generateTimes(slot).size() - booked.size();
        }
        return slot.getCapacity() - countActiveAppointmentsForSlotOnDate(slot.getId(), date);
    }

    public Map<String, Object> getAvailability(Long id, LocalDate date) {
        ScheduleSlot slot = findById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("slotType", slot.getSlotType());
        result.put("durationMinutes", slot.getDurationMinutes());
        result.put("startTime", slot.getStartTime());
        result.put("endTime", slot.getEndTime());

        if ("TEMPO".equals(slot.getSlotType())) {
            List<String> times = generateTimes(slot);
            List<String> booked = (date != null) ? appointmentRepository.findBookedTimesForSlotOnDate(id, date) : new ArrayList<>();
            List<String> available = new ArrayList<>();
            for (String t : times) {
                if (!booked.contains(t)) available.add(t);
            }
            result.put("times", times);
            result.put("availableTimes", available);
            result.put("capacity", times.size());
            result.put("occupied", booked.size());
            result.put("remaining", available.size());
        } else {
            int occupied = (date != null) ? countActiveAppointmentsForSlotOnDate(id, date) : countActiveAppointmentsForSlot(id);
            result.put("capacity", slot.getCapacity());
            result.put("occupied", occupied);
            result.put("remaining", slot.getCapacity() - occupied);
        }
        return result;
    }

    public ScheduleSlot findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado"));
    }

    public ScheduleSlot create(ScheduleSlotRequest request) {
        User professional = userRepository.findById(request.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        if (request.getUnitId() == null) {
            throw new RuntimeException("A unidade de atendimento é obrigatória");
        }
        ServiceUnit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new RuntimeException("Unidade de atendimento não encontrada"));

        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Data final não pode ser anterior a data inicial");
        }

        String weekdays = request.getWeekdays();
        if (weekdays == null || weekdays.trim().isEmpty()) {
            throw new RuntimeException("Selecione pelo menos um dia da semana");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Horários de início e fim sao obrigatorios");
        }

        Integer capacity = request.getCapacity();
        if (capacity == null || capacity < 1) {
            throw new RuntimeException("Capacidade deve ser pelo menos 1");
        }

        String slotType = request.getSlotType();
        if (slotType == null || slotType.isEmpty()) slotType = "QUANTIDADE";
        if (!"QUANTIDADE".equals(slotType) && !"TEMPO".equals(slotType)) {
            throw new RuntimeException("Tipo de vaga inválido");
        }
        if ("TEMPO".equals(slotType) && (request.getDurationMinutes() == null || request.getDurationMinutes() < 1)) {
            throw new RuntimeException("Informe a duração em minutos de cada consulta");
        }

        List<String> requestedDays = Arrays.asList(weekdays.split(","));
        for (String day : requestedDays) {
            if (DayOfWeek.valueOf(day.trim()) == null) {
                throw new RuntimeException("Dia da semana inválido: " + day);
            }
        }

        validateNoOverlap(professional.getId(), unit.getId(), startDate, endDate, requestedDays, request.getStartTime(), request.getEndTime(), null);

        ScheduleSlot slot = new ScheduleSlot();
        slot.setProfessional(professional);
        slot.setUnit(unit);
        slot.setStartDate(startDate);
        slot.setEndDate(endDate);
        slot.setWeekdays(weekdays);
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacity(capacity);
        slot.setSlotType(slotType);
        slot.setDurationMinutes(request.getDurationMinutes());
        slot.setActive(true);
        return repository.save(slot);
    }

    public ScheduleSlot update(Long id, ScheduleSlotRequest request) {
        ScheduleSlot slot = findById(id);

        if (hasActiveAppointments(id)) {
            throw new RuntimeException("Não é possível editar este horário pois possui agendamentos ativos");
        }

        if (request.getProfessionalId() != null) {
            User professional = userRepository.findById(request.getProfessionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            slot.setProfessional(professional);
        }
        if (request.getUnitId() != null) {
            ServiceUnit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unidade de atendimento não encontrada"));
            slot.setUnit(unit);
        }
        if (request.getStartDate() != null) slot.setStartDate(LocalDate.parse(request.getStartDate()));
        if (request.getEndDate() != null) slot.setEndDate(LocalDate.parse(request.getEndDate()));
        if (request.getWeekdays() != null) slot.setWeekdays(request.getWeekdays());
        if (request.getStartTime() != null) slot.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) slot.setEndTime(request.getEndTime());
        if (request.getCapacity() != null && request.getCapacity() >= 1) slot.setCapacity(request.getCapacity());
        if (request.getSlotType() != null) {
            if (!"QUANTIDADE".equals(request.getSlotType()) && !"TEMPO".equals(request.getSlotType())) {
                throw new RuntimeException("Tipo de vaga inválido");
            }
            slot.setSlotType(request.getSlotType());
        }
        if (request.getDurationMinutes() != null) slot.setDurationMinutes(request.getDurationMinutes());

        if ("TEMPO".equals(slot.getSlotType()) && (slot.getDurationMinutes() == null || slot.getDurationMinutes() < 1)) {
            throw new RuntimeException("Informe a duração em minutos de cada consulta");
        }

        if (slot.getUnit() == null) {
            throw new RuntimeException("A unidade de atendimento é obrigatória");
        }

        List<String> days = Arrays.asList(slot.getWeekdays().split(","));
        validateNoOverlap(slot.getProfessional().getId(), slot.getUnit().getId(), slot.getStartDate(), slot.getEndDate(),
                days, slot.getStartTime(), slot.getEndTime(), id);

        return repository.save(slot);
    }

    public void delete(Long id) {
        ScheduleSlot slot = findById(id);

        if (hasActiveAppointments(id)) {
            throw new RuntimeException("Não é possível remover este horário pois possui agendamentos ativos");
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

    private void validateNoOverlap(Long professionalId, Long unitId, LocalDate startDate, LocalDate endDate,
                                   List<String> weekdays, String startTime, String endTime, Long excludeSlotId) {
        List<ScheduleSlot> existing = repository.findOverlappingSlotsByUnit(professionalId, unitId, startDate, endDate);

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
                throw new RuntimeException("Já existe um horário para este profissional no mesmo dia e período. "
                        + "Horário existente: " + existingSlot.getStartTime() + " - " + existingSlot.getEndTime());
            }
        }
    }
}
