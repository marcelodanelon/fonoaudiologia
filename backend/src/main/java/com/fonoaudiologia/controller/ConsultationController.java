package com.fonoaudiologia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fonoaudiologia.dto.ConsultationRequest;
import com.fonoaudiologia.entity.Consultation;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.entity.Audiogram;
import com.fonoaudiologia.repository.ConsultationRepository;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.AudiogramService;
import com.fonoaudiologia.service.ConsultationService;
import com.fonoaudiologia.service.SystemConfigService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ConsultationRepository consultationRepository;
    private final AuditService auditService;
    private final AudiogramService audiogramService;
    private final SystemConfigService systemConfigService;

    public ConsultationController(ConsultationService consultationService, ConsultationRepository consultationRepository, AuditService auditService, AudiogramService audiogramService, SystemConfigService systemConfigService) {
        this.consultationService = consultationService;
        this.consultationRepository = consultationRepository;
        this.auditService = auditService;
        this.audiogramService = audiogramService;
        this.systemConfigService = systemConfigService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long unitId) {
        try {
            List<Consultation> list;
            java.time.LocalDateTime start = startDate != null ? java.time.LocalDate.parse(startDate).atStartOfDay() : java.time.LocalDateTime.of(2000, 1, 1, 0, 0);
            java.time.LocalDateTime end = endDate != null ? java.time.LocalDate.parse(endDate).atTime(23, 59, 59) : java.time.LocalDateTime.now().plusYears(1);
            if (unitId != null) {
                list = consultationRepository.findByUnitIdAndCreatedAtBetween(unitId, start, end);
            } else {
                list = consultationRepository.findByCreatedAtBetween(start, end);
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consultation> findById(@PathVariable Long id) {
        return ResponseEntity.ok(consultationService.findById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Consultation>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(consultationService.findByPatient(patientId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ConsultationRequest request, HttpServletRequest httpRequest) {
        try {
            User user = auditService.getCurrentUser();
            Consultation consultation = consultationService.create(request, user != null ? user.getId() : null);
            auditService.log("CREATE", "CONSULTATION", consultation.getId(),
                    "Consulta criada para paciente: " + consultation.getPatient().getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(consultation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<String> generateReport(@PathVariable Long id) {
        try {
            Consultation c = consultationService.findById(id);
            String clinicName = systemConfigService.getValue("clinic_name", "FonoSystem");

            String patientName = c.getPatient() != null ? c.getPatient().getName() : "";
            String patientCpf = c.getPatient() != null ? c.getPatient().getCpf() : null;
            String patientPhone = c.getPatient() != null ? c.getPatient().getPhone() : null;
            String patientBirthDate = c.getPatient() != null ? c.getPatient().getBirthDate() != null ? c.getPatient().getBirthDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null : null;
            String professionalName = c.getProfessional() != null ? c.getProfessional().getName() : null;
            String consultationDate = c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            String generationDate = java.time.LocalDateTime.now().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            html.append("<title>Laudo - ").append(patientName).append("</title>");
            html.append("<style>");
            html.append("@page { margin: 15mm; size: A4; }");
            html.append("@media print {");
            html.append("  body { margin: 0; }");
            html.append("  .page-break { page-break-before: always; }");
            html.append("  .card { break-inside: avoid; }");
            html.append("}");
            html.append("*, *::before, *::after { box-sizing: border-box; }");
            html.append("body { font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif; margin: 0; padding: 20px; color: #1a1a2e; background: #fff; line-height: 1.5; }");
            html.append(".header { border-bottom: 3px solid #4361ee; padding-bottom: 16px; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: flex-end; }");
            html.append(".header h1 { font-size: 22px; color: #4361ee; margin: 0; letter-spacing: -0.3px; }");
            html.append(".header .clinic-name { font-size: 13px; color: #666; margin-top: 4px; font-weight: 500; }");
            html.append(".header .date { font-size: 13px; color: #666; text-align: right; }");
            html.append(".info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }");
            html.append(".info-box { background: #f8f9ff; border: 1px solid #e0e5f6; border-radius: 8px; padding: 16px; }");
            html.append(".info-box h3 { font-size: 12px; text-transform: uppercase; letter-spacing: 0.8px; color: #4361ee; margin: 0 0 10px 0; padding-bottom: 6px; border-bottom: 1px solid #e0e5f6; }");
            html.append(".info-row { display: flex; margin-bottom: 4px; font-size: 13px; }");
            html.append(".info-row .info-label { font-weight: 600; min-width: 100px; color: #444; }");
            html.append(".info-row .info-value { color: #1a1a2e; }");
            html.append(".card { border: 1px solid #e0e5f6; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }");
            html.append(".card-header { background: #f8f9ff; padding: 10px 16px; border-bottom: 1px solid #e0e5f6; }");
            html.append(".card-header h2 { font-size: 13px; text-transform: uppercase; letter-spacing: 0.8px; color: #4361ee; margin: 0; }");
            html.append(".card-body { padding: 14px 16px; font-size: 13px; color: #333; white-space: pre-wrap; }");
            html.append(".audiogram-table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 8px; }");
            html.append(".audiogram-table th { background: #4361ee; color: #fff; padding: 8px 6px; text-align: center; font-weight: 600; }");
            html.append(".audiogram-table td { padding: 6px; text-align: center; border: 1px solid #e0e5f6; }");
            html.append(".audiogram-table tr:nth-child(even) td { background: #f8f9ff; }");
            html.append(".footer { margin-top: 32px; border-top: 1px solid #e0e5f6; padding-top: 12px; display: flex; justify-content: space-between; font-size: 11px; color: #888; }");
            html.append(".status-badge { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; }");
            html.append(".status-ATIVO { background: #d4edda; color: #155724; }");
            html.append(".status-CONCLUIDO { background: #cce5ff; color: #004085; }");
            html.append(".status-CANCELADO { background: #f8d7da; color: #721c24; }");
            html.append(".status-default { background: #e2e3e5; color: #383d41; }");
            html.append("</style></head><body>");

            html.append("<div class='header'>");
            html.append("<div><h1>Laudo de Atendimento Fonoaudiologico</h1>");
            html.append("<div class='clinic-name'>").append(clinicName).append("</div></div>");
            html.append("<div class='date'><div style='font-weight:600;'>Data do Atendimento</div>");
            html.append("<div>").append(consultationDate).append("</div></div>");
            html.append("</div>");

            html.append("<div class='info-grid'>");

            html.append("<div class='info-box'><h3>Dados do Paciente</h3>");
            if (c.getPatient() != null) {
                html.append("<div class='info-row'><span class='info-label'>Nome:</span><span class='info-value'>").append(patientName).append("</span></div>");
                if (patientCpf != null && !patientCpf.isEmpty()) html.append("<div class='info-row'><span class='info-label'>CPF:</span><span class='info-value'>").append(patientCpf).append("</span></div>");
                if (patientPhone != null && !patientPhone.isEmpty()) html.append("<div class='info-row'><span class='info-label'>Telefone:</span><span class='info-value'>").append(patientPhone).append("</span></div>");
                if (patientBirthDate != null) html.append("<div class='info-row'><span class='info-label'>Nascimento:</span><span class='info-value'>").append(patientBirthDate).append("</span></div>");
            } else {
                html.append("<div class='info-row'><span class='info-value'>Paciente não informado</span></div>");
            }
            html.append("</div>");

            html.append("<div class='info-box'><h3>Informações do Atendimento</h3>");
            if (professionalName != null) {
                html.append("<div class='info-row'><span class='info-label'>Profissional:</span><span class='info-value'>").append(professionalName).append("</span></div>");
            }
            html.append("<div class='info-row'><span class='info-label'>Tipo:</span><span class='info-value'>").append(c.getType() != null ? c.getType() : "-").append("</span></div>");
            html.append("<div class='info-row'><span class='info-label'>Status:</span><span class='info-value'>");
            String status = c.getStatus() != null ? c.getStatus() : "";
            String statusClass = "status-default";
            if ("ATIVO".equals(status)) statusClass = "status-ATIVO";
            else if ("CONCLUIDO".equals(status)) statusClass = "status-CONCLUIDO";
            else if ("CANCELADO".equals(status)) statusClass = "status-CANCELADO";
            html.append("<span class='status-badge ").append(statusClass).append("'>").append(status).append("</span></span></div>");
            html.append("<div class='info-row'><span class='info-label'>Data:</span><span class='info-value'>").append(consultationDate).append("</span></div>");
            html.append("</div>");

            html.append("</div>");

            String[][] sections = {
                {"Queixa Principal", c.getChiefComplaint()},
                {"Anamnese", c.getAnamnesis()},
                {"Histórico Clinico", c.getClinicalHistory()},
                {"Exame Fisico", c.getPhysicalExam()},
                {"Diagnóstico", c.getDiagnosis()},
                {"Conduta / Plano de Tratamento", c.getConduct()},
                {"Observações", c.getObservations()}
            };

            for (String[] section : sections) {
                String title = section[0];
                String content = section[1];
                if (content != null && !content.trim().isEmpty()) {
                    html.append("<div class='card'><div class='card-header'><h2>").append(title).append("</h2></div>");
                    html.append("<div class='card-body'>").append(content).append("</div></div>");
                }
            }

            try {
                java.util.Optional<Audiogram> audiogramOpt = audiogramService.findByConsultationId(id);
                if (audiogramOpt.isPresent()) {
                    Audiogram aud = audiogramOpt.get();
                    String lossType = aud.getHearingLossType() != null ? aud.getHearingLossType() : "N/A";
                    html.append("<div class='card'><div class='card-header'><h2>Audiograma");
                    if (!"N/A".equals(lossType)) html.append(" &mdash; ").append(lossType);
                    html.append("</h2></div><div class='card-body'>");

                    html.append("<table class='audiogram-table'>");
                    html.append("<thead><tr><th>Frequência</th><th>250 Hz</th><th>500 Hz</th><th>1000 Hz</th><th>2000 Hz</th><th>3000 Hz</th><th>4000 Hz</th><th>6000 Hz</th><th>8000 Hz</th></tr></thead><tbody>");
                    html.append("<tr><td style='font-weight:600;'>OD (Direita)</td>");
                    html.append("<td>").append(aud.getRight250() != null ? aud.getRight250() : "-").append("</td>");
                    html.append("<td>").append(aud.getRight500() != null ? aud.getRight500() : "-").append("</td>");
                    html.append("<td>").append(aud.getRight1000() != null ? aud.getRight1000() : "-").append("</td>");
                    html.append("<td>").append(aud.getRight2000() != null ? aud.getRight2000() : "-").append("</td>");
                    html.append("<td>").append(aud.getRight3000() != null ? aud.getRight3000() : "-").append("</td>");
                    html.append("<td>").append(aud.getRight4000() != null ? aud.getRight4000() : "-").append("</td>");
                    html.append("<td>").append(aud.getRight6000() != null ? aud.getRight6000() : "-").append("</td>");
                    html.append("<td>").append(aud.getRight8000() != null ? aud.getRight8000() : "-").append("</td></tr>");
                    html.append("<tr><td style='font-weight:600;'>OE (Esquerda)</td>");
                    html.append("<td>").append(aud.getLeft250() != null ? aud.getLeft250() : "-").append("</td>");
                    html.append("<td>").append(aud.getLeft500() != null ? aud.getLeft500() : "-").append("</td>");
                    html.append("<td>").append(aud.getLeft1000() != null ? aud.getLeft1000() : "-").append("</td>");
                    html.append("<td>").append(aud.getLeft2000() != null ? aud.getLeft2000() : "-").append("</td>");
                    html.append("<td>").append(aud.getLeft3000() != null ? aud.getLeft3000() : "-").append("</td>");
                    html.append("<td>").append(aud.getLeft4000() != null ? aud.getLeft4000() : "-").append("</td>");
                    html.append("<td>").append(aud.getLeft6000() != null ? aud.getLeft6000() : "-").append("</td>");
                    html.append("<td>").append(aud.getLeft8000() != null ? aud.getLeft8000() : "-").append("</td></tr>");
                    html.append("</tbody></table>");

                    if (aud.getObservations() != null && !aud.getObservations().trim().isEmpty()) {
                        html.append("<div style='margin-top:10px;'><div style='font-weight:600;font-size:12px;color:#444;margin-bottom:4px;'>Observações do Audiograma:</div>");
                        html.append("<div style='font-size:13px;'>").append(aud.getObservations()).append("</div></div>");
                    }
                    html.append("</div></div>");
                }
            } catch (Exception ignored) {}

            html.append("<div class='footer'><div>Documento gerado em: ").append(generationDate).append("</div>");
            html.append("<div>").append(clinicName).append("</div></div>");

            html.append("</body></html>");

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(html.toString());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("<html><body><p>Erro: " + e.getMessage() + "</p></body></html>");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ConsultationRequest request, HttpServletRequest httpRequest) {
        try {
            Consultation old = consultationService.findById(id);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode changes = mapper.createObjectNode();

            if (request.getStatus() != null && !request.getStatus().equals(old.getStatus())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getStatus());
                fieldChange.put("new", request.getStatus());
                changes.set("status", fieldChange);
            }
            if (request.getType() != null && !request.getType().equals(old.getType())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getType());
                fieldChange.put("new", request.getType());
                changes.set("type", fieldChange);
            }
            if (request.getPatientId() != null && !request.getPatientId().equals(old.getPatient().getId())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getPatient().getId());
                fieldChange.put("new", request.getPatientId());
                changes.set("patientId", fieldChange);
            }
            if (request.getProfessionalId() != null && !request.getProfessionalId().equals(old.getProfessional().getId())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getProfessional().getId());
                fieldChange.put("new", request.getProfessionalId());
                changes.set("professionalId", fieldChange);
            }
            if (request.getUnitId() != null && (old.getUnit() == null || !request.getUnitId().equals(old.getUnit().getId()))) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getUnit() != null ? old.getUnit().getId() : null);
                fieldChange.put("new", request.getUnitId());
                changes.set("unitId", fieldChange);
            }
            if (request.getChiefComplaint() != null && !request.getChiefComplaint().equals(old.getChiefComplaint())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getChiefComplaint());
                fieldChange.put("new", request.getChiefComplaint());
                changes.set("chiefComplaint", fieldChange);
            }
            if (request.getAnamnesis() != null && !request.getAnamnesis().equals(old.getAnamnesis())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getAnamnesis());
                fieldChange.put("new", request.getAnamnesis());
                changes.set("anamnesis", fieldChange);
            }
            if (request.getClinicalHistory() != null && !request.getClinicalHistory().equals(old.getClinicalHistory())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getClinicalHistory());
                fieldChange.put("new", request.getClinicalHistory());
                changes.set("clinicalHistory", fieldChange);
            }
            if (request.getPhysicalExam() != null && !request.getPhysicalExam().equals(old.getPhysicalExam())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getPhysicalExam());
                fieldChange.put("new", request.getPhysicalExam());
                changes.set("physicalExam", fieldChange);
            }
            if (request.getDiagnosis() != null && !request.getDiagnosis().equals(old.getDiagnosis())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getDiagnosis());
                fieldChange.put("new", request.getDiagnosis());
                changes.set("diagnosis", fieldChange);
            }
            if (request.getConduct() != null && !request.getConduct().equals(old.getConduct())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getConduct());
                fieldChange.put("new", request.getConduct());
                changes.set("conduct", fieldChange);
            }
            if (request.getObservations() != null && !request.getObservations().equals(old.getObservations())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getObservations());
                fieldChange.put("new", request.getObservations());
                changes.set("observations", fieldChange);
            }
            if (request.getReceptionRecordId() != null && !request.getReceptionRecordId().equals(old.getReceptionRecordId())) {
                ObjectNode fieldChange = mapper.createObjectNode();
                fieldChange.put("old", old.getReceptionRecordId());
                fieldChange.put("new", request.getReceptionRecordId());
                changes.set("receptionRecordId", fieldChange);
            }

            Consultation consultation = consultationService.update(id, request);
            String changesJson = changes.toString();
            auditService.logWithChanges("UPDATE", "CONSULTATION", consultation.getId(),
                    changesJson, httpRequest.getRemoteAddr());
            return ResponseEntity.ok(consultation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
