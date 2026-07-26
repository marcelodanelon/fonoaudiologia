package com.fonoaudiologia.controller;

import com.fonoaudiologia.entity.*;
import com.fonoaudiologia.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final PatientRepository patientRepository;
    private final ConsultationRepository consultationRepository;
    private final ReceptionRecordRepository receptionRepository;

    public ExportController(PatientRepository patientRepository,
                            ConsultationRepository consultationRepository,
                            ReceptionRecordRepository receptionRepository) {
        this.patientRepository = patientRepository;
        this.consultationRepository = consultationRepository;
        this.receptionRepository = receptionRepository;
    }

    @GetMapping("/patients/csv")
    public ResponseEntity<String> exportPatientsCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nome,CPF,Telefone,Email,Data Nascimento,Criado em\n");
        for (Patient p : patientRepository.findAll()) {
            csv.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    p.getId(), p.getName(), p.getCpf(), p.getPhone(),
                    p.getEmail() != null ? p.getEmail() : "",
                    p.getBirthDate() != null ? p.getBirthDate().toString() : "",
                    p.getCreatedAt() != null ? p.getCreatedAt().toString() : ""));
        }
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=pacientes.csv")
                .body(csv.toString());
    }

    @GetMapping("/consultations/csv")
    public ResponseEntity<String> exportConsultationsCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Paciente,Profissional,Tipo,Status,Criado em\n");
        for (Consultation c : consultationRepository.findAll()) {
            csv.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    c.getId(),
                    c.getPatient() != null ? c.getPatient().getName() : "",
                    c.getProfessional() != null ? c.getProfessional().getName() : "",
                    c.getType(), c.getStatus(),
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""));
        }
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=consultas.csv")
                .body(csv.toString());
    }

    @GetMapping("/patients/json")
    public ResponseEntity<List<Patient>> exportPatientsJson() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

    @GetMapping("/consultations/json")
    public ResponseEntity<List<Consultation>> exportConsultationsJson() {
        return ResponseEntity.ok(consultationRepository.findAll());
    }
}
