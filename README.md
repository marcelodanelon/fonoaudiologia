# FonoSystem - Sistema de Gestao de Fonoaudiologia

Sistema completo para gestao de clinica de fonoaudiologia com autenticacao JWT,
controle de acesso por perfis, registro de auditoria e logout automatico por inatividade.

## Arquitetura

- **Backend:** Spring Boot 3.2 + Java 17 + Spring Security + JWT
- **Frontend:** React 18 + Vite + React Router
- **Banco:** H2 (embebido - dados em memoria)
- **Autenticacao:** JWT com persistencia no localStorage

## Funcionalidades

### Autenticacao e Seguranca
- Login com JWT
- Sessao persistente (sobrevive reload da pagina)
- Logout automatico apos tempo configuravel de inatividade
- Alerta visual 1 minuto antes do logout
- Controle de acesso por perfil (RBAC)

### Perfis de Acesso
| Perfil | Descricao |
|--------|-----------|
| ADMINISTRADOR | Acesso total ao sistema |
| RECEPCIONISTA | Recepcao, pacientes, dashboard |
| FONOAUDIOLOGO | Consultas, audiogramas, pacientes |

### Telas

1. **Dashboard** - Visao geral com metricas
2. **Recepcao** - Check-in, contato telefonico, visita porta
3. **Consultas** - Cadastro e historico de atendimentos
4. **Audiograma** - Tabela interativa com frequencias 250-8000Hz
5. **Pacientes** - Cadastro completo com historico e auditoria
6. **Operadores** - Gerenciamento de usuarios e perfis
7. **Configuracoes** - Timeout de sessao e dados da clinica
8. **Auditoria** - Registro completo de todas as acoes

## Credenciais de Teste

| Usuario | Senha | Perfil |
|---------|-------|--------|
| admin | admin123 | Administrador |
| recepcionista | recep123 | Recepcionista |
| fonoaudiologo | fono123 | Fonoaudiologo |

## Como Executar

### Backend (porta 8080)

```bash
cd backend
mvn spring-boot:run
```

### Frontend (porta 5173)

```bash
cd frontend
npm install
npm run dev
```

Acesse: http://localhost:5173

## Estrutura do Projeto

```
Fonoaudiologia/
├── backend/                          # API REST Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/fonoaudiologia/
│       ├── FonoaudiologiaApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   └── DataInitializer.java
│       ├── entity/
│       │   ├── User.java
│       │   ├── Role.java
│       │   ├── Patient.java
│       │   ├── Consultation.java
│       │   ├── Audiogram.java
│       │   ├── ReceptionRecord.java
│       │   ├── AuditLog.java
│       │   └── SystemConfig.java
│       ├── repository/               # JPA Repositories
│       ├── dto/                      # Data Transfer Objects
│       ├── security/                 # JWT + Spring Security
│       ├── service/                  # Business Logic
│       └── controller/               # REST Controllers
├── frontend/                         # React SPA
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/axios.js              # HTTP client com JWT
│       ├── context/AuthContext.jsx    # Auth + inactivity timer
│       ├── components/Layout.jsx     # Sidebar + layout
│       └── pages/
│           ├── Login.jsx
│           ├── Dashboard.jsx
│           ├── Reception.jsx
│           ├── Consultation.jsx      # + Audiograma interativo
│           ├── PatientHistory.jsx
│           ├── Operators.jsx
│           ├── SystemConfig.jsx
│           └── AuditLog.jsx
└── README.md
```

## Auditoria

Todas as acoes sao registradas automaticamente:
- Login/Logout
- Criacao, edicao, exclusao de registros
- Quem fez, quando, de qual IP
