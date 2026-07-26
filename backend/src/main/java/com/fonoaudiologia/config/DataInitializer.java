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
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           SystemConfigRepository configRepository, PatientRepository patientRepository,
                           ConsultationRepository consultationRepository,
                           ReceptionRecordRepository receptionRecordRepository,
                           AudiogramRepository audiogramRepository,
                           AppointmentRepository appointmentRepository,
                           ScheduleSlotRepository scheduleSlotRepository,
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
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Role adminRole = roleRepository.save(Role.admin());
        Role recepRole = roleRepository.save(Role.recepcionista());
        Role fonoRole = roleRepository.save(Role.fonoaudiologo());

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
                "Maria Recepcao", "recep@fono.com", "111.111.111-11", "(11)98888-1111", recepRole);
        userRepository.save(recepcionista);

        User fono = new User("fonoaudiologo", passwordEncoder.encode(fonoPass),
                "Dr. Joao Fono", "fono@fono.com", "222.222.222-22", "(11)97777-2222", fonoRole);
        userRepository.save(fono);

        configRepository.save(new SystemConfig("session_timeout_minutes", "30",
                "Tempo de inatividade em minutos antes do logout automatico"));
        configRepository.save(new SystemConfig("clinic_name", "Clinica Fonoaudiologia",
                "Nome da clinica exibido no sistema"));
        configRepository.save(new SystemConfig("reception_poll_interval", "10000",
                "Intervalo em milissegundos para verificar novos pacientes na recepcao"));

        seedTestData(admin, recepcionista, fono);
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

    private List<Patient> createPatients() {
        String[][] data = {
            {"Maria Silva",        "321.546.870-00", "RG-1234561", "1985-03-15", "(11)99123-4567", "(11)3456-7890", "maria.silva@email.com",      "Rua das Flores, 123",        "Sao Paulo",      "SP"},
            {"Joao Santos",        "842.139.670-50", "RG-2345672", "1978-07-22", "(11)98234-5678", null,            "joao.santos@email.com",      "Av. Paulista, 1000",          "Sao Paulo",      "SP"},
            {"Ana Oliveira",       "513.278.490-30", "RG-3456783", "1990-11-08", "(19)97345-6789", "(19)3234-5678", "ana.oliveira@email.com",     "Rua Barao, 456",              "Campinas",       "SP"},
            {"Pedro Costa",        "174.902.380-60", "RG-4567894", "1965-01-30", "(11)96456-7890", null,            null,                          "Rua da Paz, 789",             "Guarulhos",      "SP"},
            {"Luciana Lima",       "698.305.710-20", "RG-5678905", "1982-05-17", "(11)95567-8901", "(11)3567-8901", "luciana.lima@email.com",     "Av. Brasil, 2000",             "Sao Paulo",      "SP"},
            {"Fernando Souza",     "285.741.030-90", "RG-6789016", "1973-09-03", "(19)94678-9012", null,            "fernando.souza@email.com",   "Rua Treze de Maio, 300",      "Campinas",       "SP"},
            {"Camila Ferreira",    "430.862.150-70", "RG-7890127", "1995-12-25", "(11)93789-0123", "(11)3678-0123", null,                          "Rua XV de Novembro, 500",     "Sao Paulo",      "SP"},
            {"Rafael Almeida",     "961.207.450-80", "RG-8901238", "1988-04-12", "(11)92890-1234", null,            "rafael.almeida@email.com",   "Av. Getulio Vargas, 800",     "Guarulhos",      "SP"},
            {"Juliana Pereira",    "374.581.920-10", "RG-9012349", "1992-08-19", "(13)91901-2345", "(13)3456-1234", "juliana.pereira@email.com",  "Rua XV de Novembro, 150",     "Santos",         "SP"},
            {"Marcos Ribeiro",     "608.123.570-40", "RG-0123450", "1970-06-08", "(11)90012-3456", null,            null,                          "Rua da Consolacao, 1200",     "Sao Paulo",      "SP"},
            {"Beatriz Rodrigues",  "125.479.830-60", "RG-1122331", "1998-02-14", "(19)98123-4567", null,            "beatriz.rodrigues@email.com", "Rua Jose Paulino, 250",       "Campinas",       "SP"},
            {"Lucas Martins",      "793.056.240-80", "RG-2233442", "1980-10-01", "(11)97234-5678", "(11)3789-4567", null,                          "Rua Vergueiro, 3000",         "Sao Paulo",      "SP"},
            {"Priscila Araujo",    "462.398.510-30", "RG-3344553", "1987-07-28", "(11)96345-6789", null,            "priscila.araujo@email.com",  "Av. Reboucas, 1500",          "Sao Paulo",      "SP"},
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
            "Dificuldade de audicao em ambientes ruidosos",
            "Zumbido no ouvido esquerdo",
            "Dor de ouvido recorrente",
            "Alteracao na fala da crianca",
            "Avaliacao pre-cirurgica",
            "Perda de audicao subita",
            "Sensacao de ouvido tampado",
            "Dificuldade de concentração em sala de aula",
            "Zumbido bilateral persistente",
            "Otite recorrente na infancia",
            "Avaliacao audiologica para admissao",
            "Mudanca na qualidade da voz",
            "Sensacao de excesso de cera no ouvido",
            "Dificuldade para ouvir TV em volume alto",
            "Avaliacao de adaptacao de AASI",
            "Queixa de otite media cronica",
            "Retorno para reavaliacao audiologica",
            "Dificuldade de fala em criancas",
            "Acompanhamento pos-cirurgico otologico",
            "Avaliacao para implante coclear"
        };

        String[] anamneses = {
            "Paciente relata dificuldade de audicao ha 6 meses, especialmente em locais com muito barulho. Nega uso de protetores auditivos no trabalho.",
            "Relata zumbido constante no ouvido esquerdo ha 3 meses, de intensidade moderada, piorando ao final do dia.",
            "Paciente com historico de dor de ouvido desde a infancia, com episodios mais frequentes nos ultimos 2 meses.",
            "Pais relatam que a crianca de 4 anos nao responde quando chamada e apresenta atraso na fala em relacao a crianca da mesma idade.",
            "Paciente agendado para avaliacao audiologica completa previa a procedimento cirurgico de timpanoplastia.",
            "Paciente relata perda audicao subita no ouvido direito ha 2 semanas, sem causa aparente.",
            "Sensacao de ouvido tampado ha 1 mes, associada a sensacao de pressao e leve diminuicao da audicao.",
            "Professora relata que o aluno de 8 anos tem dificuldade de concentracao na sala de aula e pede para sentar na frente.",
            "Paciente relata zumbido bilateral ha 1 ano, com piora progressiva, associado a exposicao ocupacional a ruido.",
            "Adulto com historico de otites recorrentes na infancia, agora com queixa de perda auditiva leve.",
            "Candidato a emprego em empresa de seguranca, necessita avaliacao audiologica conforme ASO.",
            "Paciente relata mudanca na qualidade da voz apos cirurgia de tireoide, com rouquidao persistente.",
            "Paciente queixa-se de dificuldade para limpar cera do ouvido e sensacao de bloqueio auditivo.",
            "Idoso de 75 anos relata que familiares reclamam que assiste TV com volume excessivamente alto.",
            "Paciente em uso de AASI ha 2 anos, retorna para avaliacao e ajuste do equipamento.",
            "Adulto com historico de otite media cronica esquerda, em acompanhamento otorrinolaringologico.",
            "Paciente retorna para nova avaliacao audiologica apos 6 meses de rehabilitacao auditiva.",
            "Pais de crianca de 5 anos relatam que a mesma apresenta dificuldade na articulacao de palavras.",
            "Adulto de 50 anos retorna para avaliacao pos-cirurgica de otosclerose.",
            "Paciente encaminhado para avaliacao de implante coclear apos perda auditiva profunda bilateral."
        };

        String[] clinicalHistories = {
            "Hipertensao arterial controlada com medicacao. Nao relata alergias. Ex-cocaineiro por 15 anos.",
            "Nega comorbidades. Trabalha como motorista de onibus ha 10 anos. Exposicao continua a ruido.",
            "Historico familiar de perda auditiva. Mae e avo com presbicusie. Nao relata uso de ototoxicos.",
            "Prematuro com 32 semanas, 1800g. Historico de internacao na UTI neonatal com uso de aminoglicosideos.",
            "Historico de otite media cronica desde os 5 anos. Cirurgia de timpanoplastia no ouvido esquerdo em 2018.",
            "Paciente com historico de trauma acustico agudo por exposicao a explosao em show musical.",
            "Sinusite cronica associada a disfuncao tubaria. Faz uso de sprays nasais intermitentemente.",
            "Aluno com diagnostico previo de TDAH em uso de metilfenidato. Historico familiar de problemas auditivos.",
            "Trabalhador metalurgico com 20 anos de exposicao ocupacional a ruido intensos. Usa protetor auditivo parcialmente.",
            "Paciente com historico de meningite na infancia, com suspeita de sequela auditiva bilaterally.",
            "Paciente saudavel, sem comorbidades significativas. Apenas exame admissional de rotina.",
            "Paciente em pos-operatorio de tireoidectomia total ha 3 meses. Recuperacao adequada.",
            "Historico de cerume impactado recorrente. Realizou microscopia de ouvido por 2x nos ultimos 5 anos.",
            "Idoso com presbicusie progressiva familiar. Uso de AASI bilateral ha 3 anos.",
            "Paciente com otite media cronica com supuracao ativa ha 8 anos, em acompanhamento ORL.",
            "Paciente em fase de rehabilitacao auditiva pos-cirurgica, com melhora progressiva da compreensao.",
            "Crianca com historico de atraso no desenvolvimento da linguagem oral. Fonoaudiologia em andamento.",
            "Adulto pos-cirurgia de otosclerose (estapedotomia) ha 1 ano, com resultado auditivo satisfatorio.",
            "Paciente com perda auditiva progressiva bilateral nos ultimos 5 anos, piora significativa recentemente.",
            "Historico de exposicao ocupacional a ruido e uso pregresso de ototoxicos (cisplatina)."
        };

        String[] physicalExams = {
            "Otoscopia: Meato acustico externo livre, timpano intacto, reflexo de luz presente bilateral. Pavimento timpanico normal.",
            "Otoscopia OD: Cerumen impactado obstruindo canal auditivo. OE: Normal com timpano translucido.",
            "Otoscopia: Timpanos esclerosados bilateralmente, com retracao moderada e ausencia de reflexo de luz.",
            "Avaliacao da fala: Articulacao de fonemas distorcida, especialmente /r/ e /s/. Vocabulario reduzido para a idade.",
            "Otoscopia: Timpano com cicatriz central no OE. Canal auditivo externo normal bilateral.",
            "Otoscopia bilateral normal. Reflexos acusticos presentes. Imitanciometria dentro da normalidade.",
            "Otoscopia: Erameno amarelo-acinzentado em ambos os canais, sem impactacao significativa.",
            "Avaliacao da fala e linguagem: Linguagem compreensiva adequada para a idade, expressiva com imaturidade fonologica.",
            "Otoscopia: Timpanos com retracao tipo B1 bilateral e nivel de liquido nivel horizontal.",
            "Otoscopia: Timpano cicatricial direito, restante do examen normal.",
            "Otoscopia bilateral normal. CAE permeavel. Timpanos translucidos com reflexo de luz presente.",
            "Palpacao de glandulas salivares: Sem alteracoes. Avaliacao da mobilidade da lingua e palato: normal.",
            "Otoscopia: Presenca de cerumen amarelado no OE, canal parcialmente obstruido. OD normal.",
            "Otoscopia bilateral: Timpanos esbranquecidos com perda de translucidez. Presbicusie compativel.",
            "Otoscopia OE: Perfuracao central com supuracao mucopurulenta ativa. OD: Timpano normal.",
            "Otoscopia: Pos-operatorio timpanoplastia OE com cicatrizacao evoluindo satisfatoriamente.",
            "Avaliacao orofacial: Labio leporino reparado. Palato intacto. Fonação com escape nasal minimo.",
            "Otoscopia: Membrana timpanica transparente bilateral. Ausculta com sonda de Seigle positiva bilateral.",
            "Otoscopia: Placa timpanoesclerotica em posterossuperior OE. OD normal.",
            "Otoscopia: Timpanos opacos bilateralmente com nivel de liquido-ares. Protese de transmissao no OE."
        };

        String[] diagnoses = {
            "Perda auditiva neurosensorial leve bilateral",
            "Condutiva unilateral direita por impactacao de cerume",
            "Perda auditiva condutiva bilateral por otosclerose",
            "Atraso no desenvolvimento da linguagem oral",
            "Perda auditiva condutiva leve esquerda pos-timpanoplastia",
            "Audicao dentro da normalidade",
            "Diminuicao da acuidade auditiva por obstrucao do CAE",
            "Imaturidade fonologica - alteracao da fala",
            "Perda auditiva mista bilateral leve a moderada",
            "Perda auditiva neurosensorial moderada direita",
            "Audicao normal - apto para funcao que exige acuidade auditiva",
            "Disfonia pos-tireoidectomia - paralisia de corda vocal",
            "Cerume impactado bilateral",
            "Presbicusie bilateral moderada",
            "Otite media cronica com supuracao ativa esquerda",
            "Perda auditiva neurosensorial leve - em reabilitacao",
            "Atraso de linguagem oral com imaturidade fonologica",
            "Otosclerose pos-estapedotomia - resultado satisfactory",
            "Perda auditiva neurosensorial profunda bilateral",
            "Perda auditiva mista severa bilateral"
        };

        String[] conducts = {
            "Reabilitacao auditiva com AASI bilateral. Retorno em 30 dias para reavaliacao.",
            "Orientacao sobre higiene auditiva. Remocao de cerume. Retorno em 15 dias para verificacao.",
            "Encaminhamento para otolaringologia para avaliacao cirurgica de otosclerose.",
            "Encaminhamento para neurologia pediatrica e fonoaudiologia para reabilitacao da linguagem.",
            "Acompanhamento pos-cirurgico. Ajuste de AASI se necessario. Retorno em 2 meses.",
            "Paciente orientado sobre cuidados auditivos. Nenhuma conduta medica necessaria.",
            "Orientacao sobre higiene dos ouvidos. Encaminhamento ao ORL para avaliacao da cera.",
            "Intervencao fonoaudiologica em linguagem. Atividades de estimulacao em casa.",
            "AASI bilateral com moldes personalizados. Acompanhamento trimestral.",
            "AASI monaural direito. Reparo com AASI se perda progredir. Retorno em 6 meses.",
            "Emissao emitida com resultado normal. Apto para a funcao pleiteada.",
            "Terapia vocal e orientacao vocal. Repouso vocal relativo por 2 semanas.",
            "Remocao de cerume com pinca e lavagem auricular. Orientacao sobre higiene.",
            "AASI bilateral adaptado. Retorno em 1 mes para acolhimento e ajuste.",
            "Tratamento com antibioticoterapia topica e oral. Retorno em 2 semanas.",
            "Manutencao de AASI. Exercicios de treinamento auditivo. Retorno trimestral.",
            "Atividades terapeuticas de linguagem 2x por semana. Reavaliacao em 6 meses.",
            "Acompanhamento pos-operatorio com audiometria seriada a cada 3 meses.",
            "Encaminhamento para avaliacao de implante coclear em centro de referencia.",
            "Reabilitacao auditiva com AASI de alto ganho. Acompanhamento mensal."
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
                    c.setObservations("Consulta agendada - aguardando confirmacao do paciente.");
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
            "Paciente encaminhado pelo SUS para avaliacao audiologica.",
            "Paciente ligou para remarcar consulta. Reagendado para proxima semana.",
            "Paciente walk-in solicitando avaliacao de audicao.",
            "Paciente compareceu para retorno. Ja encaminhado ao consultorio.",
            "Paciente compareceu com encaminhamento medico para audiometria.",
            "Paciente nao compareceu na hora marcada. Tentativa de contato por telefone.",
            "Paciente chegou para adaptacao de AASI. Equipamento disponivel.",
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
                r.setNotes(notes[i] + " Consulta ja realizada.");
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
            "Audicao dentro dos limites normais para todas as frequencias.",
            "Audiograma normal. Queixa nao confirmada por exame objetivo.",
            "Audicao normal bilateral. Sugerida avaliacao complementar de processamento auditivo."
        };
        String[] obsNeuro = {
            "Configuracao tipica de presbicusie. Perda leve em altas frequencias bilateral.",
            "Perda neurosensorial moderada a severa com configuracao descendente bilateral."
        };
        String[] obsCondutiva = {
            "Perda condutiva bilateral leve a moderada. Configuracao plana sugestiva de otosclerose.",
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
