# [US-04] Implementação — Reserva e Gestão de Empréstimo da Televisão Comunitária

> Documento técnico de como a US-04 foi implementada no backend (Spring Boot) e no frontend (Ionic/Angular). A especificação original está em [`docs/US/us4.md`](us4.md). Atualizado com a integração da TV na página de reservas, cancelamento pelo morador e tela unificada com filtros.

## 1. Visão geral da solução

A US-04 permite ao morador reservar a televisão comunitária de forma **self-service e gratuita** (auto-aprovação, sem moderação), e ao funcionário/administrador controlar a **custódia do controle remoto**: registrar a retirada (`IN_USE`) e a devolução (`RETURNED`) com precisão de horários para auditoria.

Além do fluxo original, foram adicionados:
- **TV como opção na página de reservas existente** (`/reservations/new`): o card "Televisão" fica junto dos espaços (Salão, Churrasqueira, Academia, Campo) e direciona ao fluxo de reserva da TV.
- **Cancelamento pelo morador**: `PATCH /lunaLink/equipment-reservation/{id}/cancel` → status `CANCELED` (somente reservas `CONFIRMED`). Morador cancela apenas as próprias; Admin/Funcionário cancelam por gestão (qualquer reserva).
- **Tela unificada de reservas** (aba Reservas): mescla reservas de espaço + TV com filtro por tipo (Todas / Salão / Churrasqueira / Academia / Campo / Televisão) e cancelamento da TV.

Fluxo ponta a ponta:

```
Morador / Admin / Funcionário (app)
   │  POST /lunaLink/equipment-reservation  { equipmentId, date, startTime, endTime }
   ▼
EquipmentReservationController (@Valid + autenticação)
   ▼
EquipmentReservationService (validação + conflito de horário + persistência @Transactional)
   │
   ├── CONFLITO? → IllegalStateException → 409 Conflict (CA04)
   ├── ok → status CONFIRMED (auto-aprovação, sem faturamento) → 201 Created (CA01)
   ▼
Morador (próprias) / Admin (gestão)
   │  PATCH /lunaLink/equipment-reservation/{id}/cancel → CANCELED + canceledAt
   ▼
Funcionário/Admin
   │  PATCH /lunaLink/equipment-reservation/{id}/handover → IN_USE + pickedUpAt (CA02)
   │  PATCH /lunaLink/equipment-reservation/{id}/return    → RETURNED + returnedAt (CA03)
   ▼
GET /lunaLink/equipment-reservation (filtros date/status) → listagem para gestão
GET /lunaLink/equipment-reservation/mine → listagem do morador
```

---

## 2. Backend (Spring Boot)

### 2.1 Entidades de domínio

Arquivos: `application/src/main/java/com/LunaLink/application/domain/model/equipment/`

**`Equipment`** — o equipamento físico (a TV comunitária).

| Campo | Tipo | Observação |
|---|---|---|
| `id` | `Long` | `@GeneratedValue(IDENTITY)` |
| `name` | `String` | `@Column(nullable = false, unique = true)` |
| `active` | `boolean` | flag para manutenção/inativação |

**`EquipmentReservation`** — agregado **separado** do `Reservation` (não acoplado à lógica de faturamento).

| Campo | Tipo | Observação |
|---|---|---|
| `id` | `UUID` | `@GeneratedValue(AUTO)` |
| `equipment` | `Equipment` | `@ManyToOne(optional = false)` |
| `user` | `Users` | `@ManyToOne(optional = false)` — morador responsável |
| `date` | `LocalDate` | dia da reserva |
| `startTime` / `endTime` | `LocalTime` | bloco de horário |
| `status` | `EquipmentReservationStatus` | máquina de estados própria |
| `createdAt` | `LocalDateTime` | `@CreatedDate` + `AuditingEntityListener` — preenchido no POST (CA05) |
| `pickedUpAt` | `LocalDateTime` | `LocalDateTime.now()` no handover (CA05) |
| `returnedAt` | `LocalDateTime` | `LocalDateTime.now()` na devolução (CA05) |
| `canceledAt` | `LocalDateTime` | `LocalDateTime.now()` no cancelamento (auditoria) |

Máquina de estados — `domain/enums/EquipmentReservationStatus.java`:

```
CONFIRMED  ──(handover)──▶  IN_USE  ──(return)──▶  RETURNED
     └──(cancel)──▶ CANCELED  (apenas reservas CONFIRMED)
```

Atende ao **CA05**: o histórico mostra quando a reserva foi solicitada (`createdAt`), a hora real da retirada na portaria (`pickedUpAt`), a hora exata da devolução (`returnedAt`) e, quando aplicável, o cancelamento (`canceledAt`), todos preenchidos automaticamente.

### 2.2 DTOs

Arquivos: `application/src/main/java/com/LunaLink/application/web/dto/EquipmentDTO/`

- **`EquipmentReservationRequestDTO`** (entrada): `equipmentId` (`@NotNull`), `date` (`@NotNull`), `startTime` (`@NotNull`), `endTime` (`@NotNull`).
- **`EquipmentReservationResponseDTO`** (saída): `id`, `equipmentName`, `userName`, `userApartment`, `date`, `startTime`, `endTime`, `status`, `createdAt`, `pickedUpAt`, `returnedAt`, `canceledAt`.

> Backend espera `LocalDate`/`LocalTime` no formato ISO (`2026-09-01`, `14:00:00`).

### 2.3 Controller e rotas

Arquivo: `application/src/main/java/com/LunaLink/application/web/controller/EquipmentReservationController.java`
Base: `@RequestMapping("/lunaLink/equipment-reservation")`

| Método | Rota | Acesso | Ação |
|---|---|---|---|
| `POST` | `/lunaLink/equipment-reservation` | autenticado (morador self-service; admin/funcionário em nome do morador) | Cria reserva → **201 Created** (CA01) |
| `PATCH` | `/lunaLink/equipment-reservation/{id}/cancel` | **autenticado** (morador cancela as próprias; Admin/Funcionário por gestão) | Cancela reserva `CONFIRMED` → **CANCELED** |
| `PATCH` | `/lunaLink/equipment-reservation/{id}/handover` | **Admin/Funcionário** | Entrega do controle → **IN_USE** (CA02) |
| `PATCH` | `/lunaLink/equipment-reservation/{id}/return` | **Admin/Funcionário** | Devolução do controle → **RETURNED** (CA03) |
| `GET` | `/lunaLink/equipment-reservation/mine` | autenticado | Listagem das reservas do morador logado |
| `GET` | `/lunaLink/equipment-reservation` | **Admin/Funcionário** | Listagem com filtros opcionais `date` e `status` |

O usuário autenticado é obtido via `Authentication.getName()` (email) e repassado ao serviço para atrelar a reserva ao morador.

### 2.4 Serviço `EquipmentReservationService`

Arquivo: `application/src/main/java/com/LunaLink/application/application/service/equipment/EquipmentReservationService.java`

- `createReservation(dto, email)`:
  1. Valida data não passada e `endTime > startTime` → `IllegalArgumentException` (400).
  2. Carrega morador (`userRepository.findByEmail`) e equipamento (`equipmentRepository.findById`); equipamento inativo → `IllegalStateException`.
  3. Verifica **conflito de horário** via `hasConflict(...)` considerando status ativos (`CONFIRMED`, `IN_USE`) — **CA04**: sobreposição de bloco (`startTime < r.endTime AND endTime > r.startTime`) → `IllegalStateException` → **409 Conflict** no `GlobalExceptionHandler`.
  4. Persiste com status `CONFIRMED` (auto-aprovação, sem etapa de moderação e sem registro de faturamento) — **CA01**.
- `handoverEquipment(id)`: só `CONFIRMED` → `IN_USE` + `pickedUpAt = LocalDateTime.now()` — **CA02**.
- `returnEquipment(id)`: só `IN_USE` → `RETURNED` + `returnedAt = LocalDateTime.now()` — **CA03**.
- `cancelEquipmentReservation(id, userEmail)`: só `CONFIRMED` → `CANCELED` + `canceledAt = LocalDateTime.now()`. Validação de **propriedade**: se o usuário autenticado não é o dono e não tem role `ADMIN_ROLE`/`EMPLOYEE` → `AccessDeniedException` (**403**).
- `listReservations(date, status)`: combina filtros opcionais.
- `listMyReservations(userEmail)`: lista apenas as reservas do morador logado.

### 2.5 Persistência

Arquivo: `application/src/main/java/com/LunaLink/application/infrastructure/repository/equipment/EquipmentReservationRepository.java`

- `hasConflict(equipmentId, date, startTime, endTime, activeStatuses)` — query JPQL customizada para detecção de sobreposição (CA04).
- `findAllByDate`, `findAllByStatus`, `findAllByDateAndStatus` — filtros da listagem.

Ports: `application/ports/input/EquipmentReservationServicePort.java` e `application/ports/output/EquipmentReservationRepositoryPort.java` mantêm a arquitetura hexagonal usada no projeto. O Facade (`EquipmentReservationFacade`) faz a ponte Controller → Service.

### 2.6 Segurança HTTP

Arquivo: `infrastructure/security/SecurityConfiguration.java`

```java
// Morador cria reserva (self-service); Admin/Funcionário também podem registrar em nome do morador
.requestMatchers(HttpMethod.POST, "/lunaLink/equipment-reservation").authenticated()
// Morador visualiza as próprias reservas de equipamento
.requestMatchers(HttpMethod.GET, "/lunaLink/equipment-reservation/mine").authenticated()
// Cancelamento: matcher específico ANTES do genérico (ordem importa) — morador cancela as próprias, Admin/Funcionário por gestão
.requestMatchers(HttpMethod.PATCH, "/lunaLink/equipment-reservation/{id}/cancel").authenticated()
// Apenas Admin/Funcionário faz check-in/check-out (handover/return) e lista
.requestMatchers(HttpMethod.PATCH, "/lunaLink/equipment-reservation/**").hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
.requestMatchers(HttpMethod.GET,  "/lunaLink/equipment-reservation/**").hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
```

> **Correção aplicada nesta implementação:** os matchers `PATCH`/`GET` estavam com `hasRole("ADMIN_ROLE")` apenas, o que gerava **403** para o funcionário — quebrando o fluxo da portaria. Passaram a `hasAnyRole("ADMIN_ROLE", "EMPLOYEE")`, conforme a US ("Acesso: Admin/Funcionário"). O `POST` permanece `.authenticated()` para manter o self-service do morador.

### 2.7 Tratamento de erros

Arquivo: `web/exception/GlobalExceptionHandler.java`

| Exceção | Status | Cenário |
|---|---|---|
| `IllegalStateException` | **409 Conflict** | conflito de horário (CA04) e transições de estado inválidas |
| `AccessDeniedException` | **403 Forbidden** | tentativa de cancelar reserva de outro morador |
| `IllegalArgumentException` | **400** | dados inválidos, equipamento não encontrado, data passada |
| `MethodArgumentNotValidException` | **400** | falha de `@Valid` no DTO |
| `Exception` (genérica) | **500** | erro inesperado (sem vazar stacktrace) |

### 2.8 Testes backend

- `web/controller/EquipmentReservationControllerTest` — unitários do controller (201, handover, return, cancel, listagem).
- `application/service/equipment/EquipmentReservationServiceTest` — criação com sucesso, conflito de horário, handover, return, cancelamento (dono, gestor, não-dono → 403, status inválido, não encontrada).
- `application/facades/equipment/EquipmentReservationFacadeTest` — delegação ao service port.
- `infrastructure/security/EquipmentReservationSecurityTest` — regressão de roles:
  - `EMPLOYEE` e `ADMIN_ROLE`: `GET` + `PATCH` (handover/return/cancel) → **200**.
  - `RESIDENT_ROLE`: `POST` → **201**; `GET`/`PATCH` (handover/return) → **403**; `PATCH cancel` → **200**.
  - Não autenticado → **401**.

---

## 3. Frontend (Ionic / Angular)

### 3.1 Tela de gestão (EMPLOYEE + ADMIN)

Arquivo: `client/luna-link/src/app/pages/equipment-reservations/equipment-reservations.page.ts`

- Lista as reservas com status colorido (Confirmado/Em Uso/Devolvido/Cancelado).
- Para status `CONFIRMED` → botão **Entregar** (`handover`); para `IN_USE` → botão **Devolver** (`return`).
- Os botões usam `canManage = isAdmin || isEmployee` (**correção:** antes usavam `isAdmin`, o que impedia o `EMPLOYEE` de executar a portaria na interface).
- Exibe os timestamps de auditoria (CA05): **Criado em**, **Retirado em** (`pickedUpAt`) e **Devolvido em** (`returnedAt`).
- Botão `+` abre o formulário de nova reserva (admin/funcionário em nome do morador).

### 3.2 Tela do morador (RESIDENT_ROLE)

**Reserva da TV com "Minhas Reservas" + cancelamento**
Arquivo: `client/luna-link/src/app/pages/equipment-reservations/equipment-reservation-create.page.ts`

- Fluxo self-service: data, horário de início/fim e confirmação (`POST` → `CONFIRMED`).
- Seção **Minhas Reservas** (`GET /equipment-reservation/mine`) com botão **Cancelar** para reservas `CONFIRMED` (confirmação via alert → `PATCH /cancel`). Exibe `canceledAt` quando cancelada.

**TV como opção na página de reservas existente**
Arquivo: `client/luna-link/src/app/pages/reservations/reservation-create.page.ts`

- Card **Televisão** adicionado ao grid de espaços (Salão, Churrasqueira, Academia, Campo). Ao selecionar, navega para `/equipment-reservations/new`.

**Tela unificada de reservas com filtros por tipo**
Arquivo: `client/luna-link/src/app/pages/tabs/reservations-tab/reservations-tab.page.ts`

- Mescla as reservas de **espaço** (`GET /reservation/findByUser/{id}` ou `GET /reservation` no admin) com as de **televisão** (`GET /equipment-reservation/mine` ou `list()` no admin) em uma lista única ordenada por data.
- Novo filtro por **tipo**: Todas / Salão / Churrasqueira / Academia / Campo / Televisão. O filtro de status se adapta (espaços: Pendentes/Aprovadas; TV: Confirmadas/Em uso/Devolvidas/Canceladas).
- Reservas da TV com status `CONFIRMED` exibem botão **Cancelar** (morador cancela as próprias; admin também, por gestão).
- Fluxo do admin (aprovar/rejeitar/cancelar espaços) mantido.

### 3.3 Serviço de API

Arquivo: `client/luna-link/src/app/services/equipment-reservation.service.ts`

- `create(payload)` → `POST {API_URL}/equipment-reservation`
- `list(params?)` → `GET {API_URL}/equipment-reservation`
- `listMine()` → `GET {API_URL}/equipment-reservation/mine`
- `handover(id)` → `PATCH {API_URL}/equipment-reservation/{id}/handover`
- `returnItem(id)` → `PATCH {API_URL}/equipment-reservation/{id}/return`
- `cancel(id)` → `PATCH {API_URL}/equipment-reservation/{id}/cancel`

Modelo: `client/luna-link/src/app/core/models/equipment-reservation.model.ts` (inclui `canceledAt`) + enum `EquipmentReservationStatus` em `core/models/enums.ts`.

### 3.4 Rotas e navegação

Arquivo: `client/luna-link/src/app/app.routes.ts`

- `/equipment-reservations` → `roleGuard([UserRoles.ADMIN_ROLE, UserRoles.EMPLOYEE])`.
- `/equipment-reservations/new` → `roleGuard([UserRoles.ADMIN_ROLE, UserRoles.RESIDENT_ROLE])`.
- `/reservations/new` → `roleGuard([UserRoles.ADMIN_ROLE, UserRoles.RESIDENT_ROLE])` — agora inclui a opção TV.

Home tab (`pages/tabs/home-tab/home-tab.page.ts`):
- `canManageEquipment = isAdmin || isEmployee` → card "Equipamentos" no acesso rápido (interface do **EMPLOYEE**, com **ADMIN_ROLE** para gestão).
- Card "Reservar TV" mantém o atalho direto para `/equipment-reservations/new`.

---

## 4. Correções relevantes feitas na implementação

| Problema | Correção |
|---|---|
| `PATCH`/`GET` de equipamento restritos a `ADMIN_ROLE` → funcionário (portaria) recebia 403 | `hasAnyRole("ADMIN_ROLE", "EMPLOYEE")` no `SecurityConfiguration` |
| Tela de gestão mostrava Entregar/Devolver apenas para `isAdmin` | Botões passaram a `canManage = isAdmin || isEmployee` |
| Sem cobertura de segurança para as roles do fluxo | Criado `EquipmentReservationSecurityTest` |
| Auditoria de retirada/devolução não visível na tela (CA05) | Exibição de `pickedUpAt`/`returnedAt` no card da reserva |
| TV não era opção na página de reservas existente (ficava em fluxo separado) | Card "Televisão" em `reservation-create.page.ts` navegando para `/equipment-reservations/new` |
| Morador não tinha como cancelar a própria reserva de TV | Endpoint `PATCH /{id}/cancel` + botão Cancelar (status `CONFIRMED`) com `canceledAt` |
| Morador precisava abrir telas separadas para ver espaços e TV | Aba Reservas unificada com filtro por tipo (espaços + televisão) |

---

## 5. Fluxo de validação (pontas a ponta)

1. Morador (ou admin/funcionário) cria a reserva → `POST` → auto-aprovação `CONFIRMED` → **201**.
2. Se o horário já estiver alocado (status ativo), o sistema retorna **409 Conflict**.
3. No horário combinado, o morador vai à portaria; o funcionário abre a tela de equipamentos e aciona **Entregar** → `IN_USE` + `pickedUpAt`.
4. Na devolução, o funcionário aciona **Devolver** → `RETURNED` + `returnedAt`, liberando o equipamento para novas reservas.
5. O morador pode cancelar a reserva (se ainda `CONFIRMED`) pela aba Reservas ou pela própria tela de reserva → `CANCELED` + `canceledAt`, liberando o horário automaticamente (o conflito de horário ignora `CANCELED`).
6. O histórico exibe `createdAt`, `pickedUpAt`, `returnedAt` e `canceledAt` (auditoria CA05).

## 6. Como testar

- Backend: `cd application && ./mvnw test`.
- Frontend: `cd client/luna-link && npm run build` (ou `npm start` para dev).
- Manual (backend): usar um token de `porteiro` (EMPLOYEE) para `GET`/`PATCH` de handover/return, um token de morador para `POST`/`PATCH cancel`, e validar **403** quando o morador tenta cancelar reserva de outro morador.
- Manual (frontend): na aba Reservas, filtrar por **Televisão** e cancelar uma reserva `CONFIRMED`; verificar que o horário volta a ficar livre para novo agendamento.
