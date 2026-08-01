## Objective
- Implementar controle por unidade de atendimento no FonoSystem (fluxo unidade + data + "Carregar" em recepção e atendimento, unidade obrigatória em horários/agendamentos) **e** criar o módulo de **Estoque**: cadastro de insumos, entradas com múltiplos insumos, saídas com validação de saldo, controle por unidade (aba "Estoque por Unidade" em cada insumo) e saída opcional vinculada a paciente.

## Important Details
- Backend: Spring Boot 2.7.18, `javax.persistence`, PostgreSQL (`jdbc:postgresql://localhost:5432/fonoaudiologia`, user `postgres`), JWT, `spring.jpa.hibernate.ddl-auto=update`.
- Frontend: React 18 + Vite, axios com `baseURL: '/api'`, páginas em `frontend/src/pages/`.
- Fluxo de unidades: recepção e atendimento pedem unidade + data e, ao clicar em "Carregar", exibem só os registros daquela unidade. Unidade obrigatória no horário de agendamento.
- Rotas protegidas por permissão: `/unidades` → `systemConfig`; `/estoque` → `inventory` (nova permissão, concedida apenas ao ADMINISTRADOR).
- Migração: coluna `can_access_inventory` em `roles` precisou ser **anulável** (`Boolean` + getter `isCanAccessInventory()` retornando `canAccessInventory != null && canAccessInventory`) — Hibernate não adiciona coluna NOT NULL em tabela com registros.
- psql disponível em `C:\Program Files\PostgreSQL\17\bin\psql.exe` (`-U postgres -h localhost -d fonoaudiologia`, senha vazia).
- Teste de integração adicionado: `spring-boot-starter-test` (scope test) no pom + `InventoryFlowTest`; contexto sobe contra o Postgres dev e faz rollback (não polui o banco).

## Work State
### Completed
- **Fase unidades concluída**: `ServiceUnit` (entity/repository/service/controller `/api/service-units`), campo `unit` em `ScheduleSlot`/`Appointment`/`ReceptionRecord`/`Consultation`, DTOs com `unitId`, repositórios/serviços/controllers com filtro `unitId`; `Reception.jsx` (filtros/tabela só com `loaded`, modal verificado, unidade no modal) e `Consultation.jsx` (unidade + data + "Carregar", `unitId` em loadData/loadReadyPatients/resetForm/handlers, unidade obrigatória no form e coluna na tabela); `pages/ServiceUnits.jsx` + rota `/unidades` + menu; `npm run build` passou.
- **Estoque backend**: entidades `Supply`, `SupplyStock`, `SupplyEntry`, `SupplyEntryItem`, `SupplyExit`, `SupplyExitItem`; repositórios `SupplyRepository`, `SupplyStockRepository`, `SupplyEntryRepository`, `SupplyExitRepository`; DTOs `SupplyRequest`, `InventoryItemRequest`, `SupplyEntryRequest`, `SupplyExitRequest`; serviços `SupplyService` (findAll, create com duplicidade por nome, update soft, delete soft, `findStocksBySupply`, `findStocksByUnit`), `SupplyEntryService.create` (`@Transactional`, soma saldo por unidade), `SupplyExitService.create` (`@Transactional`, valida saldo por unidade e subtrai, paciente opcional); controllers `SupplyController` (`/api/supplies`, `/api/supplies/stock?unitId=`, `/api/supplies/{id}/stocks`), `SupplyEntryController` (`/api/supply-entries`), `SupplyExitController` (`/api/supply-exits`).
- `User.getPassword()` com `@JsonIgnore` (evita expor hash via `operator` no JSON).
- Permissão `inventory`: `Role.canAccessInventory`, `UserService.login` adiciona `"inventory"` às permissões, `DataInitializer.ensurePermissions()` (executa sempre; ADMINISTRADOR = true; roles sem flag = NULL).
- Backend compilou (`BUILD SUCCESS`) e **subiu com sucesso** (Tomcat 8080); schema verificado: 17 tabelas incluindo `supplies`, `supply_stocks`, `supply_entries`(+items), `supply_exits`(+items); `roles.can_access_inventory` = `t` no ADMINISTRADOR.
- **Frontend Estoque**: `pages/Inventory.jsx` (tabs Insumos/Entradas/Saídas, `PAGE_SIZE=8`; modal de insumo com abas "Dados"/"Estoque por Unidade" listando todas as unidades; modal de entrada com múltiplos itens dinâmicos; modal de saída com paciente opcional + busca, exibição de saldo por insumo e validação client+server de saldo; `unitMeasures = ['UN','CX','PC','FR','ML','L','KG','G','MG']`); rota `/estoque` em `App.jsx`; menu "Estoque" com ícone em `Layout.jsx` (visível com permissão inventory); CSS `.modal.modal-lg { max-width: 900px; }` em `index.css`.
- **`npm run build` passou (108 módulos)** após o módulo de Estoque.
- **Testes de integração passaram: `mvnw.cmd test` → 4 testes, 0 falhas, 0 erros** (`InventoryFlowTest`): entrada aumenta saldo por unidade; entrada multi-item soma cada saldo; saída diminui saldo e rejeita saldo insuficiente (mensagem "Saldo insuficiente"); entrada sem itens é rejeitada. Todos com `@Transactional` (rollback).

### Active
- (none)

### Blocked
- (none)

## Next Move
1. Teste manual pela interface: subir backend (`mvnw.cmd spring-boot:run`) e frontend (`npm run dev`), logar como admin, cadastrar insumo em `/estoque`, fazer entrada com múltiplos itens, conferir saldo na aba "Estoque por Unidade", fazer saída com paciente e tentar saída acima do saldo (deve bloquear).
2. Atualizar o README.md (está desatualizado: menciona Spring Boot 3.2/H2/Java 17; real é Spring Boot 2.7.18/PostgreSQL) e incluir as telas Unidades e Estoque.
3. Conferir nas outras telas que a unidade não quebrou o fluxo (Dashboard, Agendamentos, Horarios) após as mudanças de fase unidades.

## Relevant Files
- `...\backend\src\main\java\com\fonoaudiologia\entity\Supply.java`, `SupplyStock.java`, `SupplyEntry.java`, `SupplyEntryItem.java`, `SupplyExit.java`, `SupplyExitItem.java` — entidades do estoque.
- `...\backend\src\main\java\com\fonoaudiologia\service\SupplyService.java`, `SupplyEntryService.java`, `SupplyExitService.java` — regras de saldo por unidade.
- `...\backend\src\main\java\com\fonoaudiologia\controller\SupplyController.java`, `SupplyEntryController.java`, `SupplyExitController.java` — endpoints `/api/supplies*`, `/api/supply-entries`, `/api/supply-exits`.
- `...\backend\src\main\java\com\fonoaudiologia\entity\Role.java` e `...\config\DataInitializer.java` — permissão `inventory` (coluna anulável `Boolean`).
- `...\backend\src\main\java\com\fonoaudiologia\entity\User.java` — `@JsonIgnore` em `getPassword()`.
- `...\backend\src\test\java\com\fonoaudiologia\InventoryFlowTest.java` — testes de integração do fluxo de estoque (rollback).
- `C:\Backup\D\Arquivos\JavaScript\Java_IA\Fonoaudiologia\frontend\src\pages\Inventory.jsx` — tela completa do módulo Estoque.
- `C:\Backup\D\Arquivos\JavaScript\Java_IA\Fonoaudiologia\frontend\src\App.jsx` e `...\components\Layout.jsx` — rota `/estoque` e menu.
- `C:\Backup\D\Arquivos\JavaScript\Java_IA\Fonoaudiologia\frontend\src\index.css` — `.modal.modal-lg`.
- `C:\Backup\D\Arquivos\JavaScript\Java_IA\Fonoaudiologia\frontend\src\pages\Reception.jsx` e `Consultation.jsx` — fluxo unidade + data concluído.
- `C:\Backup\D\Arquivos\JavaScript\Java_IA\Fonoaudiologia\frontend\src\pages\ServiceUnits.jsx`, `Horarios.jsx`, `Agendamentos.jsx` — cadastro de unidades e agendamento por unidade.
- `C:\Backup\D\Arquivos\JavaScript\Java_IA\Fonoaudiologia\backend\pom.xml` — adicionada dependência `spring-boot-starter-test` (scope test).
