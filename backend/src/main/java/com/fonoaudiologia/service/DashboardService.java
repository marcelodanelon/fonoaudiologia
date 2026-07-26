package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.DashboardResponse;
import com.fonoaudiologia.dto.DashboardStatsResponse;
import com.fonoaudiologia.repository.ConsultationRepository;
import com.fonoaudiologia.repository.PatientRepository;
import com.fonoaudiologia.repository.ReceptionRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private static final String[] MONTH_NAMES = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

    private final PatientService patientService;
    private final ConsultationService consultationService;
    private final ReceptionService receptionService;
    private final UserService userService;
    private final PatientRepository patientRepository;
    private final ConsultationRepository consultationRepository;
    private final ReceptionRecordRepository receptionRecordRepository;

    public DashboardService(PatientService patientService, ConsultationService consultationService,
                            ReceptionService receptionService, UserService userService,
                            PatientRepository patientRepository, ConsultationRepository consultationRepository,
                            ReceptionRecordRepository receptionRecordRepository) {
        this.patientService = patientService;
        this.consultationService = consultationService;
        this.receptionService = receptionService;
        this.userService = userService;
        this.patientRepository = patientRepository;
        this.consultationRepository = consultationRepository;
        this.receptionRecordRepository = receptionRecordRepository;
    }

    public DashboardResponse getDashboard() {
        DashboardResponse response = new DashboardResponse();
        response.setTotalPatients(patientService.countActive());
        response.setTotalOperators(userService.countActive());
        response.setScheduledConsultations(consultationService.countScheduled());
        response.setCompletedConsultations(consultationService.countCompleted());
        response.setCheckins(receptionService.countByType("CHECKIN"));
        response.setPhoneContacts(receptionService.countByType("PHONE_CONTACT"));
        response.setWalkins(receptionService.countByType("WALKIN"));
        response.setTotalReceptions(response.getCheckins() + response.getPhoneContacts() + response.getWalkins());
        return response;
    }

    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse response = new DashboardStatsResponse();

        response.setTotalPatients(patientRepository.countByActiveTrue());
        response.setTotalConsultations(consultationRepository.count());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        response.setConsultationsThisMonth(consultationRepository.countByCreatedAtBetween(startOfMonth, now.plusNanos(1)));

        response.setPendingReception(receptionRecordRepository.countPending());

        List<DashboardStatsResponse.MonthlyCount> monthlyData = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            LocalDateTime monthStart = month.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = month.plusMonths(1).atDay(1).atStartOfDay();
            long count = consultationRepository.countByCreatedAtBetween(monthStart, monthEnd);
            monthlyData.add(new DashboardStatsResponse.MonthlyCount(MONTH_NAMES[month.getMonthValue() - 1], count));
        }
        response.setMonthlyData(monthlyData);

        return response;
    }
}
