package com.fonoaudiologia.config;

import com.fonoaudiologia.entity.*;
import com.fonoaudiologia.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SystemConfigRepository configRepository;
    private final PatientRepository patientRepository;
    private final ConsultationRepository consultationRepository;
    private final ReceptionRecordRepository receptionRecordRepository;
    private final AudiogramRepository audiogramRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final ServiceUnitRepository serviceUnitRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           SystemConfigRepository configRepository, PatientRepository patientRepository,
                           ConsultationRepository consultationRepository,
                           ReceptionRecordRepository receptionRecordRepository,
                           AudiogramRepository audiogramRepository,
                           AppointmentRepository appointmentRepository,
                           ScheduleSlotRepository scheduleSlotRepository,
                           ServiceUnitRepository serviceUnitRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.configRepository = configRepository;
        this.patientRepository = patientRepository;
        this.consultationRepository = consultationRepository;
        this.receptionRecordRepository = receptionRecordRepository;
        this.audiogramRepository = audiogramRepository;
        this.appointmentRepository = appointmentRepository;
        this.scheduleSlotRepository = scheduleSlotRepository;
        this.serviceUnitRepository = serviceUnitRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensurePermissions();
        if (userRepository.count() > 0) {
            return;
        }

        Role adminRole = roleRepository.save(Role.admin());
        Role recepRole = roleRepository.save(Role.recepcionista());
        Role fonoRole = roleRepository.save(Role.fonoaudiólogo());

        String adminPass = System.getenv("ADMIN_PASSWORD");
        String recepPass = System.getenv("RECEPCIONISTA_PASSWORD");
        String fonoPass = System.getenv("FONOAUDIOLOGO_PASSWORD");

        if (adminPass == null || adminPass.isEmpty()) adminPass = UUID.randomUUID().toString();
        if (recepPass == null || recepPass.isEmpty()) recepPass = UUID.randomUUID().toString();
        if (fonoPass == null || fonoPass.isEmpty()) fonoPass = UUID.randomUUID().toString();

        User admin = new User("admin", passwordEncoder.encode(adminPass),
                "Administrador do Sistema", "admin@fono.com", "000.000.000-00", "(11)99999-0000", adminRole);
        userRepository.save(admin);

        User recepcionista = new User("recepcionista", passwordEncoder.encode(recepPass),
                "Maria Recepção", "recep@fono.com", "111.111.111-11", "(11)98888-1111", recepRole);
        userRepository.save(recepcionista);

        User fono = new User("fonoaudiólogo", passwordEncoder.encode(fonoPass),
                "Dr. Joao Fono", "fono@fono.com", "222.222.222-22", "(11)97777-2222", fonoRole);
        userRepository.save(fono);

        configRepository.save(new SystemConfig("session_timeout_minutes", "30",
                "Tempo de inatividade em minutos antes do logout automatico"));
        configRepository.save(new SystemConfig("clinic_name", "Clínica Fonoaudiologia",
                "Nome da clínica exibido no sistema"));
        configRepository.save(new SystemConfig("reception_poll_interval", "10000",
                "Intervalo em milissegundos para verificar novos pacientes na recepção"));

        if (serviceUnitRepository.count() == 0) {
            ServiceUnit unit = new ServiceUnit();
            unit.setName("Unidade Central");
            unit.setAddress("Rua das Flores, 123 - Centro");
            unit.setPhone("(11) 3456-7890");
            unit.setActive(true);
            serviceUnitRepository.save(unit);
        }
    }

    private void ensurePermissions() {
        roleRepository.findAll().forEach(role -> {
            if ("ADMINISTRADOR".equals(role.getName())) {
                boolean changed = false;
                if (!role.isCanAccessDashboard()) { role.setCanAccessDashboard(true); changed = true; }
                if (!role.isCanAccessReception()) { role.setCanAccessReception(true); changed = true; }
                if (!role.isCanAccessConsultation()) { role.setCanAccessConsultation(true); changed = true; }
                if (!role.isCanAccessPatients()) { role.setCanAccessPatients(true); changed = true; }
                if (!role.isCanAccessOperators()) { role.setCanAccessOperators(true); changed = true; }
                if (!role.isCanAccessAuditLog()) { role.setCanAccessAuditLog(true); changed = true; }
                if (!role.isCanAccessSystemConfig()) { role.setCanAccessSystemConfig(true); changed = true; }
                if (!role.isCanAccessInventory()) { role.setCanAccessInventory(true); changed = true; }
                if (changed) roleRepository.save(role);
            }
        });
    }

    private void seedTestData(User admin, User recepcionista, User fono) {
        Random rng = new Random(42);

        List<Patient> patients = createPatients();
        List<Consultation> consultations = createConsultations(patients, admin, fono, rng);
        createReceptionRecords(patients, recepcionista, consultations, rng);
        createAudiograms(consultations, fono);
        createAppointments(patients, fono);
    }

    private void createAppointments(List<Patient> patients, User fono) {
        LocalDate today = LocalDate.now();
        String[] times = {"08:00", "09:00", "10:00", "11:00", "14:00", "15:00"};
        String[] types = {"CONSULTA", "RETORNO", "AVALIACAO"};
        String[] statuses = {"AGENDADO", "RECEPCIONADO"};

        ScheduleSlot slot1 = new ScheduleSlot();
        slot1.setProfessional(fono);
        slot1.setUnit(defaultUnit());
        slot1.setStartDate(today.minusDays(3));
        slot1.setEndDate(today.plusDays(5));
        slot1.setWeekdays("MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY");
        slot1.setStartTime("08:00");
        slot1.setEndTime("12:00");
        slot1.setCapacity(5);
        slot1.setActive(true);
        scheduleSlotRepository.save(slot1);

        ScheduleSlot slot2 = new ScheduleSlot();
        slot2.setProfessional(fono);
        slot2.setUnit(defaultUnit());
        slot2.setStartDate(today.minusDays(3));
        slot2.setEndDate(today.plusDays(5));
        slot2.setWeekdays("MONDAY,WEDNESDAY,FRIDAY");
        slot2.setStartTime("14:00");
        slot2.setEndTime("18:00");
        slot2.setCapacity(3);
        slot2.setActive(true);
        scheduleSlotRepository.save(slot2);

        for (int i = 0; i < 4; i++) {
            Patient p = patients.get(i % patients.size());
            Appointment apt = new Appointment();
            apt.setPatient(p);
            apt.setProfessional(fono);
            apt.setDate(today.minusDays(i % 3));
            apt.setTime(times[i]);
            apt.setType(types[i % types.length]);
            apt.setStatus(statuses[i % statuses.length]);
            apt.setObservations("Agendamento de teste");
            apt.setScheduleSlot(i < 2 ? slot1 : slot2);
            appointmentRepository.save(apt);
        }
    }

    private ServiceUnit defaultUnit() {
        ServiceUnit unit = serviceUnitRepository.findByActiveTrueOrderByNameAsc().stream()
                .findFirst().orElse(null);
        if (unit == null) {
            unit = new ServiceUnit();
            unit.setName("Unidade Central");
            unit.setActive(true);
            unit = serviceUnitRepository.save(unit);
        }
        return unit;
    }

    private List<Patient> createPatients() {
        String[][] data = {
            {"Maria Silva",        "321.546.870-00", "RG-1234561", "1985-03-15", "(11)99123-4567", "(11)3456-7890", "maria.silva@email.com",      "Rua das Flores, 123",        "São Paulo",      "SP"},
            {"Joao Santos",        "842.139.670-50", "RG-2345672", "1978-07-22", "(11)98234-5678", null,            "joao.santos@email.com",      "Av. Paulista, 1000",          "São Paulo",      "SP"},
            {"Ana Oliveira",       "513.278.490-30", "RG-3456783", "1990-11-08", "(19)97345-6789", "(19)3234-5678", "ana.oliveira@email.com",     "Rua Barao, 456",              "Campinas",       "SP"},
            {"Pedro Costa",        "174.902.380-60", "RG-4567894", "1965-01-30", "(11)96456-7890", null,            null,                          "Rua da Paz, 789",             "Guarulhos",      "SP"},
            {"Luciana Lima",       "698.305.710-20", "RG-5678905", "1982-05-17", "(11)95567-8901", "(11)3567-8901", "luciana.lima@email.com",     "Av. Brasil, 2000",             "São Paulo",      "SP"},
            {"Fernando Souza",     "285.741.030-90", "RG-6789016", "1973-09-03", "(19)94678-9012", null,            "fernando.souza@email.com",   "Rua Treze de Maio, 300",      "Campinas",       "SP"},
            {"Camila Ferreira",    "430.862.150-70", "RG-7890127", "1995-12-25", "(11)93789-0123", "(11)3678-0123", null,                          "Rua XV de Novembro, 500",     "São Paulo",      "SP"},
            {"Rafael Almeida",     "961.207.450-80", "RG-8901238", "1988-04-12", "(11)92890-1234", null,            "rafael.almeida@email.com",   "Av. Getulio Vargas, 800",     "Guarulhos",      "SP"},
            {"Juliana Pereira",    "374.581.920-10", "RG-9012349", "1992-08-19", "(13)91901-2345", "(13)3456-1234", "juliana.pereira@email.com",  "Rua XV de Novembro, 150",     "Santos",         "SP"},
            {"Marcos Ribeiro",     "608.123.570-40", "RG-0123450", "1970-06-08", "(11)90012-3456", null,            null,                          "Rua da Consolação, 1200",     "São Paulo",      "SP"},
            {"Beatriz Rodrigues",  "125.479.830-60", "RG-1122331", "1998-02-14", "(19)98123-4567", null,            "beatriz.rodrigues@email.com", "Rua Jose Paulino, 250",       "Campinas",       "SP"},
            {"Lucas Martins",      "793.056.240-80", "RG-2233442", "1980-10-01", "(11)97234-5678", "(11)3789-4567", null,                          "Rua Vergueiro, 3000",         "São Paulo",      "SP"},
            {"Priscila Araujo",    "462.398.510-30", "RG-3344553", "1987-07-28", "(11)96345-6789", null,            "priscila.araujo@email.com",  "Av. Reboucas, 1500",          "São Paulo",      "SP"},
            {"Thiago Gomes",       "831.672.040-90", "RG-4455664", "1993-03-05", "(19)95456-7890", "(19)3567-8901", null,                          "Rua Barao de Jaguara, 900",   "Campinas",       "SP"},
            {"Isabela Barbosa",    "547.283.190-50", "RG-5566775", "1976-11-20", "(11)94567-8901", null,            "isabela.barbosa@email.com",  "Rua Coronel Oliveira Lima, 400", "Sao Caetano do Sul", "SP"}
        };

        List<Patient> patients = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (String[] d : data) {
            Patient p = new Patient();
            p.setName(d[0]);
            p.setCpf(d[1]);
            p.setRg(d[2]);
            p.setBirthDate(LocalDate.parse(d[3]));
            p.setPhone(d[4]);
            p.setPhone2(d[5]);
            p.setEmail(d[6]);
            p.setAddress(d[7]);
            p.setCity(d[8]);
            p.setState(d[9]);
            p.setActive(true);
            patients.add(patientRepository.save(p));
        }
        return patients;
    }

    private List<Consultation> createConsultations(List<Patient> patients, User admin, User fono, Random rng) {
        String[] chiefComplaints = {
            "Dificuldade de audição em ambientes ruidosos",
            "Zumbido no ouvido esquerdo",
            "Dor de ouvido recorrente",
            "Alteração na fala da criança",
            "Avaliação pre-cirúrgica",
            "Perda de audição súbita",
            "Sensação de ouvido tampado",
            "Dificuldade de concentração em sala de aula",
            "Zumbido bilateral persistente",
            "Otite recorrente na infância",
            "Avaliação audiológica para admissao",
            "Mudança na qualidade da voz",
            "Sensação de excesso de cera no ouvido",
            "Dificuldade para ouvir TV em volume alto",
            "Avaliação de adaptação de AASI",
            "Queixa de otite média crônica",
            "Retorno para reavaliação audiológica",
            "Dificuldade de fala em crianças",
            "Acompanhamento pós-cirúrgico otológico",
            "Avaliação para implante coclear"
        };

        String[] anamneses = {
            "Paciente relata dificuldade de audição há 6 meses, especialmente em locais com muito barulho. Nega uso de protetores auditivos no trabalho.",
            "Relata zumbido constante no ouvido esquerdo há 3 meses, de intensidade moderada, piorando ao final do dia.",
            "Paciente com histórico de dor de ouvido desde a infância, com episódios mais frequentes nos últimos 2 meses.",
            "Pais relatam que a criança de 4 anos não responde quando chamada e apresenta atraso na fala em relação à criança da mesma idade.",
            "Paciente agendado para avaliação audiológica completa prévia a procedimento cirúrgico de timpanoplastia.",
            "Paciente relata perda de audição súbita no ouvido direito há 2 semanas, sem causa aparente.",
            "Sensação de ouvido tampado há 1 mês, associada à sensação de pressão e leve diminuição da audição.",
            "Professora relata que o aluno de 8 anos tem dificuldade de concentração na sala de aula e pede para sentar na frente.",
            "Paciente relata zumbido bilateral há 1 ano, com piora progressiva, associado à exposição ocupacional ao ruído.",
            "Adulto com histórico de otites recorrentes na infância, agora com queixa de perda auditiva leve.",
            "Candidato a emprego em empresa de segurança, necessita avaliação audiológica conforme ASO.",
            "Paciente relata mudança na qualidade da voz após cirurgia de tireoide, com rouquidão persistente.",
            "Paciente queixa-se de dificuldade para limpar cera do ouvido e sensação de bloqueio auditivo.",
            "Idoso de 75 anos relata que familiares reclamam que assiste TV com volume excessivamente alto.",
            "Paciente em uso de AASI há 2 anos, retorna para avaliação e ajuste do equipamento.",
            "Adulto com histórico de otite média crônica esquerda, em acompanhamento otorrinolaringológico.",
            "Paciente retorna para nova avaliação audiológica após 6 meses de reabilitação auditiva.",
            "Pais de criança de 5 anos relatam que a mesma apresenta dificuldade na articulação de palavras.",
            "Adulto de 50 anos retorna para avaliação pós-cirúrgica de otosclerose.",
            "Paciente encaminhado para avaliação de implante coclear após perda auditiva profunda bilateral."
        };

        String[] clinicalHistories = {
            "Hipertensão arterial controlada com medicação. Não relata alergias. Ex-cocainômano por 15 anos.",
            "Nega comorbidades. Trabalha como motorista de ônibus há 10 anos. Exposição contínua a ruído.",
            "Histórico familiar de perda auditiva. Mãe e avó com presbiacusia. Não relata uso de ototóxicos.",
            "Prematuro com 32 semanas, 1800g. Histórico de internação na UTI neonatal com uso de aminoglicosídeos.",
            "Histórico de otite média crônica desde os 5 anos. Cirurgia de timpanoplastia no ouvido esquerdo em 2018.",
            "Paciente com histórico de trauma acústico agudo por exposição a explosão em show musical.",
            "Sinusite crônica associada à disfunção tubária. Faz uso de sprays nasais intermitentemente.",
            "Aluno com diagnóstico previo de TDAH em uso de metilfenidato. Histórico familiar de problemas auditivos.",
            "Trabalhador metalurgico com 20 anos de exposição ocupacional ao ruído intensos. Usa protetor auditivo parcialmente.",
            "Paciente com histórico de meningite na infância, com suspeita de sequela auditiva bilateral.",
            "Paciente saudável, sem comorbidades significativas. Apenas exame admissional de rotina.",
            "Paciente em pós-operatório de tireoidectomia total há 3 meses. Recuperação adequada.",
            "Histórico de cerúmen impactado recorrente. Realizou microscopia de ouvido por 2x nos últimos 5 anos.",
            "Idoso com presbiacusia progressiva familiar. Uso de AASI bilateral há 3 anos.",
            "Paciente com otite média crônica com supuração ativa há 8 anos, em acompanhamento ORL.",
            "Paciente em fase de reabilitação auditiva pós-cirúrgica, com melhora progressiva da compreensão.",
            "Criança com histórico de atraso no desenvolvimento da linguagem oral. Fonoaudiologia em andamento.",
            "Adulto pós-cirurgia de otosclerose (estapedotomia) há 1 ano, com resultado auditivo satisfatório.",
            "Paciente com perda auditiva progressiva bilateral nos últimos 5 anos, piora significativa recentemente.",
            "Histórico de exposição ocupacional ao ruído e uso pregresso de ototóxicos (cisplatina)."
        };

        String[] physicalExams = {
            "Otoscopia: Meato acústico externo livre, tímpano intacto, reflexo de luz presente bilateral. Pavimento timpânico normal.",
            "Otoscopia OD: Cerumen impactado obstruindo canal auditivo. OE: Normal com tímpano translúcido.",
            "Otoscopia: Tímpanos esclerosados bilateralmente, com retração moderada e ausência de reflexo de luz.",
            "Avaliação da fala: Articulação de fonemas distorcida, especialmente /r/ e /s/. Vocabulário reduzido para a idade.",
            "Otoscopia: Tímpano com cicatriz central no OE. Canal auditivo externo normal bilateral.",
            "Otoscopia bilateral normal. Reflexos acústicos presentes. Imitanciometria dentro da normalidade.",
            "Otoscopia: Cerúmen amarelo-acinzentado em ambos os canais, sem impactação significativa.",
            "Avaliação da fala e linguagem: Linguagem compreensiva adequada para a idade, expressiva com imaturidade fonológica.",
            "Otoscopia: Tímpanos com retração tipo B1 bilateral e nível de líquido horizontal.",
            "Otoscopia: Tímpano cicatricial direito, restante do exame normal.",
            "Otoscopia bilateral normal. CAE permeavel. Tímpanos translucidos com reflexo de luz presente.",
            "Palpação de glândulas salivares: Sem alterações. Avaliação da mobilidade da língua e palato: normal.",
            "Otoscopia: Presença de cerumen amarelado no OE, canal parcialmente obstruído. OD normal.",
            "Otoscopia bilateral: Tímpanos esbranquecidos com perda de translucidez. Presbiacusia compatível.",
            "Otoscopia OE: Perfuração central com supuração mucopurulenta ativa. OD: Tímpano normal.",
            "Otoscopia: Pós-operatório timpanoplastia OE com cicatrização evoluindo satisfatoriamente.",
            "Avaliação orofacial: Labio leporino reparado. Palato intacto. Fonação com escape nasal mínimo.",
            "Otoscopia: Membrana timpânica transparente bilateral. Ausculta com sonda de Seigle positiva bilateral.",
            "Otoscopia: Placa timpanoesclerótica em póstero-superior OE. OD normal.",
            "Otoscopia: Tímpanos opacos bilateralmente com nível de líquido-ar. Prótese de transmissão no OE."
        };

        String[] diagnoses = {
            "Perda auditiva neurosensorial leve bilateral",
            "Condutiva unilateral direita por impactação de cerúmen",
            "Perda auditiva condutiva bilateral por otosclerose",
            "Atraso no desenvolvimento da linguagem oral",
            "Perda auditiva condutiva leve esquerda pós-timpanoplastia",
            "Audição dentro da normalidade",
            "Diminuição da acuidade auditiva por obstrução do CAE",
            "Imaturidade fonológica - alteração da fala",
            "Perda auditiva mista bilateral leve a moderada",
            "Perda auditiva neurosensorial moderada direita",
            "Audição normal - apto para função que exige acuidade auditiva",
            "Disfonia pós-tireoidectomia - paralisia de corda vocal",
            "Cerúmen impactado bilateral",
            "Presbiacusia bilateral moderada",
            "Otite média crônica com supuração ativa esquerda",
            "Perda auditiva neurosensorial leve - em reabilitação",
            "Atraso de linguagem oral com imaturidade fonológica",
            "Otosclerose pós-estapedotomia - resultado satisfatório",
            "Perda auditiva neurosensorial profunda bilateral",
            "Perda auditiva mista severa bilateral"
        };

        String[] conducts = {
            "Reabilitação auditiva com AASI bilateral. Retorno em 30 dias para reavaliação.",
            "Orientação sobre higiene auditiva. Remoção de cerúmen. Retorno em 15 dias para verificação.",
            "Encaminhamento para otolaringologia para avaliação cirúrgica de otosclerose.",
            "Encaminhamento para neurologia pediátrica e fonoaudiologia para reabilitação da linguagem.",
            "Acompanhamento pós-cirúrgico. Ajuste de AASI se necessário. Retorno em 2 meses.",
            "Paciente orientado sobre cuidados auditivos. Nenhuma conduta médica necessária.",
            "Orientação sobre higiene dos ouvidos. Encaminhamento ao ORL para avaliação da cera.",
            "Intervenção fonoaudiológica em linguagem. Atividades de estimulação em casa.",
            "AASI bilateral com moldes personalizados. Acompanhamento trimestral.",
            "AASI monaural direito. Reparo com AASI se perda progredir. Retorno em 6 meses.",
            "Emissão emitida com resultado normal. Apto para a função pleiteada.",
            "Terapia vocal e orientação vocal. Repouso vocal relativo por 2 semanas.",
            "Remoção de cerúmen com pinca e lavagem auricular. Orientação sobre higiene.",
            "AASI bilateral adaptado. Retorno em 1 mês para acolhimento e ajuste.",
            "Tratamento com antibioticoterapia topica e oral. Retorno em 2 semanas.",
            "Manutenção de AASI. Exercícios de treinamento auditivo. Retorno trimestral.",
            "Atividades terapêuticas de linguagem 2x por semana. Reavaliação em 6 meses.",
            "Acompanhamento pós-operatório com audiometria seriada a cada 3 meses.",
            "Encaminhamento para avaliação de implante coclear em centro de referência.",
            "Reabilitação auditiva com AASI de alto ganho. Acompanhamento mensal."
        };

        String[] types = {"CONSULTA", "RETORNO", "AVALIACAO"};
        int[] typeDistribution = {12, 5, 3};
        String[] statusOrder = {"CONCLUIDA", "CONCLUIDA", "CONCLUIDA", "CONCLUIDA", "CONCLUIDA", "CONCLUIDA", "CONCLUIDA", "CONCLUIDA",
                                "EM_ANDAMENTO", "EM_ANDAMENTO", "EM_ANDAMENTO", "EM_ANDAMENTO", "EM_ANDAMENTO",
                                "AGENDADA", "AGENDADA", "AGENDADA", "AGENDADA",
                                "CANCELADA", "CANCELADA", "CANCELADA"};

        List<Consultation> allConsultations = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        int typeIdx = 0;
        int typeCount = 0;
        int typeRemaining = typeDistribution[0];

        for (int i = 0; i < 20; i++) {
            String status = statusOrder[i];

            if (typeRemaining == 0) {
                typeIdx++;
                typeRemaining = typeDistribution[typeIdx];
            }
            String type = types[typeIdx];
            typeRemaining--;

            Patient patient = patients.get(i % patients.size());
            User professional = (i % 2 == 0) ? fono : admin;

            int daysAgo = rng.nextInt(30);
            int hoursAgo = rng.nextInt(12);
            LocalDateTime consultationDate = now.minusDays(daysAgo).withHour(9 + rng.nextInt(9)).withMinute(0).withSecond(0);

            Consultation c = new Consultation();
            c.setPatient(patient);
            c.setProfessional(professional);
            c.setOperator(professional);
            c.setType(type);
            c.setStatus(status);
            c.setCreatedAt(consultationDate);

            switch (status) {
                case "CONCLUIDA":
                    c.setChiefComplaint(chiefComplaints[i]);
                    c.setAnamnesis(anamneses[i]);
                    c.setClinicalHistory(clinicalHistories[i]);
                    c.setPhysicalExam(physicalExams[i]);
                    c.setDiagnosis(diagnoses[i]);
                    c.setConduct(conducts[i]);
                    c.setObservations("Consulta concluida com sucesso. " + (rng.nextBoolean() ? "Paciente orientado." : "Encaminhamentos realizados."));
                    c.setUpdatedAt(consultationDate.plusMinutes(30 + rng.nextInt(60)));
                    break;
                case "EM_ANDAMENTO":
                    c.setChiefComplaint(chiefComplaints[i]);
                    c.setAnamnesis(anamneses[i]);
                    c.setPhysicalExam(physicalExams[i]);
                    c.setObservations("Consulta em andamento - aguardando exames complementares.");
                    break;
                case "AGENDADA":
                    c.setChiefComplaint(chiefComplaints[i]);
                    c.setObservations("Consulta agendada - aguardando confirmação do paciente.");
                    break;
                case "CANCELADA":
                    c.setChiefComplaint(chiefComplaints[i]);
                    String[] cancelReasons = {"Paciente faltou sem justificativa", "Reagendado a pedido do paciente", "Cancelado por motivo de saude"};
                    c.setObservations("Cancelada: " + cancelReasons[rng.nextInt(cancelReasons.length)]);
                    break;
            }

            allConsultations.add(consultationRepository.save(c));
        }
        return allConsultations;
    }

    private void createReceptionRecords(List<Patient> patients, User recepcionista, List<Consultation> consultations, Random rng) {
        LocalDateTime now = LocalDateTime.now();
        String[] notes = {
            "Paciente chegou para consulta agendada. Documentos verificados.",
            "Paciente encaminhado pelo SUS para avaliação audiológica.",
            "Paciente ligou para remarcar consulta. Reagendado para próxima semana.",
            "Paciente walk-in solicitando avaliação de audição.",
            "Paciente compareceu para retorno. Já encaminhado ao consultório.",
            "Paciente compareceu com encaminhamento médico para audiometria.",
            "Paciente não compareceu na hora marcada. Tentativa de contato por telefone.",
            "Paciente chegou para adaptação de AASI. Equipamento disponível.",
            "Paciente compareceu para primeira consulta. Cadastro atualizado.",
            "Paciente ligou informando que vai cancelar. Motivo: viagem de trabalho."
        };

        String[] contactTypes = {"AGENDAMENTO", "PORTA", "TELEFONE", "PORTA", "AGENDAMENTO", "AGENDAMENTO", "TELEFONE", "PORTA", "PORTA", "TELEFONE"};
        String[] recTypes =     {"CHECKIN",     "CHECKIN", "CHECKIN", "CHECKIN", "CHECKIN",        "CHECKIN",        "CHECKIN",  "CHECKIN", "CHECKIN", "CHECKIN"};

        for (int i = 0; i < 10; i++) {
            int daysAgo = rng.nextInt(7);
            LocalDateTime recordDate = now.minusDays(daysAgo).withHour(8 + rng.nextInt(8)).withMinute(rng.nextInt(60));

            ReceptionRecord r = new ReceptionRecord();
            r.setPatient(patients.get(i % patients.size()));
            r.setOperator(recepcionista);
            r.setContactType(contactTypes[i]);
            r.setNotes(notes[i]);
            r.setCreatedAt(recordDate);

            if (i < 3 && i < consultations.size()) {
                r.setType("CHECKIN");
                r.setNotes(notes[i] + " Consulta já realizada.");
            } else if (i < 4) {
                r.setType("CHECKIN");
            } else if (i < 7) {
                r.setType("CHECKIN");
                r.setNotes(notes[i] + " Check-in processado e consulta iniciada.");
            } else {
                r.setType("CHECKIN");
                r.setNotes(notes[i]);
            }

            receptionRecordRepository.save(r);
        }
    }

    private void createAudiograms(List<Consultation> consultations, User fono) {
        List<Consultation> completedConsultations = new ArrayList<>();
        for (Consultation c : consultations) {
            if ("CONCLUIDA".equals(c.getStatus())) {
                completedConsultations.add(c);
            }
        }

        if (completedConsultations.size() < 8) return;

        LocalDateTime now = LocalDateTime.now();
        String[] obsNormal = {
            "Audição dentro dos limites normais para todas as frequências.",
            "Audiograma normal. Queixa não confirmada por exame objetivo.",
            "Audição normal bilateral. Sugerida avaliação complementar de processamento auditivo."
        };
        String[] obsNeuro = {
            "Configuração tipica de presbiacusia. Perda leve em altas frequências bilateral.",
            "Perda neurosensorial moderada a severa com configuração descendente bilateral."
        };
        String[] obsCondutiva = {
            "Perda condutiva bilateral leve a moderada. Configuração plana sugestiva de otosclerose.",
            "Perda condutiva unilateral direita. Complemento com timpanometria tipo A bilateral."
        };
        String[] obsMista = {
            "Perda mista severa bilateral com componente condutiva e neurosensorial. Sugere otosclerose avancada."
        };

        createNormalAudiogram(completedConsultations.get(0), fono, now, obsNormal[0],
            10, 5, 10, 10, 15, 10, 15, 15,
            5, 10, 5, 10, 10, 15, 10, 15);

        createNormalAudiogram(completedConsultations.get(1), fono, now.minusDays(2), obsNormal[1],
            0, 5, 5, 10, 5, 10, 10, 10,
            5, 0, 5, 5, 10, 5, 10, 10);

        createNormalAudiogram(completedConsultations.get(2), fono, now.minusDays(5), obsNormal[2],
            15, 10, 10, 15, 10, 15, 10, 20,
            10, 10, 15, 10, 15, 10, 15, 20);

        createNeurosensorialAudiogram(completedConsultations.get(3), fono, now.minusDays(3), obsNeuro[0],
            20, 25, 30, 35, 40, 45, 50, 55,
            20, 20, 25, 30, 40, 45, 50, 55);

        createNeurosensorialAudiogram(completedConsultations.get(4), fono, now.minusDays(7), obsNeuro[1],
            30, 35, 40, 45, 55, 55, 60, 60,
            25, 30, 35, 45, 50, 55, 55, 60);

        createCondutivaAudiogram(completedConsultations.get(5), fono, now.minusDays(10), obsCondutiva[0],
            20, 25, 25, 20, 25, 25, 20, 25,
            25, 20, 25, 20, 25, 20, 25, 20);

        createCondutivaAudiogram(completedConsultations.get(6), fono, now.minusDays(15), obsCondutiva[1],
            15, 20, 20, 25, 20, 25, 20, 25,
            10, 10, 10, 10, 10, 10, 10, 10);

        createMistaAudiogram(completedConsultations.get(7), fono, now.minusDays(12), obsMista[0],
            35, 40, 45, 50, 60, 60, 65, 70,
            30, 35, 40, 45, 55, 60, 60, 65);
    }

    private void createNormalAudiogram(Consultation consultation, User professional, LocalDateTime createdAt, String obs,
                                       int r250, int r500, int r1000, int r2000, int r3000, int r4000, int r6000, int r8000,
                                       int l250, int l500, int l1000, int l2000, int l3000, int l4000, int l6000, int l8000) {
        Audiogram a = new Audiogram();
        a.setConsultation(consultation);
        a.setProfessional(professional);
        a.setHearingLossType("NORMAL");
        a.setObservations(obs);
        a.setCreatedAt(createdAt);
        setAudiogramValues(a, r250, r500, r1000, r2000, r3000, r4000, r6000, r8000,
                l250, l500, l1000, l2000, l3000, l4000, l6000, l8000);
        audiogramRepository.save(a);
    }

    private void createNeurosensorialAudiogram(Consultation consultation, User professional, LocalDateTime createdAt, String obs,
                                               int r250, int r500, int r1000, int r2000, int r3000, int r4000, int r6000, int r8000,
                                               int l250, int l500, int l1000, int l2000, int l3000, int l4000, int l6000, int l8000) {
        Audiogram a = new Audiogram();
        a.setConsultation(consultation);
        a.setProfessional(professional);
        a.setHearingLossType("NEUROSENSORIAL");
        a.setObservations(obs);
        a.setCreatedAt(createdAt);
        setAudiogramValues(a, r250, r500, r1000, r2000, r3000, r4000, r6000, r8000,
                l250, l500, l1000, l2000, l3000, l4000, l6000, l8000);
        audiogramRepository.save(a);
    }

    private void createCondutivaAudiogram(Consultation consultation, User professional, LocalDateTime createdAt, String obs,
                                          int r250, int r500, int r1000, int r2000, int r3000, int r4000, int r6000, int r8000,
                                          int l250, int l500, int l1000, int l2000, int l3000, int l4000, int l6000, int l8000) {
        Audiogram a = new Audiogram();
        a.setConsultation(consultation);
        a.setProfessional(professional);
        a.setHearingLossType("CONDUTIVA");
        a.setObservations(obs);
        a.setCreatedAt(createdAt);
        setAudiogramValues(a, r250, r500, r1000, r2000, r3000, r4000, r6000, r8000,
                l250, l500, l1000, l2000, l3000, l4000, l6000, l8000);
        audiogramRepository.save(a);
    }

    private void createMistaAudiogram(Consultation consultation, User professional, LocalDateTime createdAt, String obs,
                                      int r250, int r500, int r1000, int r2000, int r3000, int r4000, int r6000, int r8000,
                                      int l250, int l500, int l1000, int l2000, int l3000, int l4000, int l6000, int l8000) {
        Audiogram a = new Audiogram();
        a.setConsultation(consultation);
        a.setProfessional(professional);
        a.setHearingLossType("MISTA");
        a.setObservations(obs);
        a.setCreatedAt(createdAt);
        setAudiogramValues(a, r250, r500, r1000, r2000, r3000, r4000, r6000, r8000,
                l250, l500, l1000, l2000, l3000, l4000, l6000, l8000);
        audiogramRepository.save(a);
    }

    private void setAudiogramValues(Audiogram a,
                                    int r250, int r500, int r1000, int r2000, int r3000, int r4000, int r6000, int r8000,
                                    int l250, int l500, int l1000, int l2000, int l3000, int l4000, int l6000, int l8000) {
        a.setRight250(r250);   a.setRight500(r500);   a.setRight1000(r1000);   a.setRight2000(r2000);
        a.setRight3000(r3000); a.setRight4000(r4000); a.setRight6000(r6000);   a.setRight8000(r8000);
        a.setLeft250(l250);    a.setLeft500(l500);    a.setLeft1000(l1000);     a.setLeft2000(l2000);
        a.setLeft3000(l3000);  a.setLeft4000(l4000);  a.setLeft6000(l6000);     a.setLeft8000(l8000);
    }
}
