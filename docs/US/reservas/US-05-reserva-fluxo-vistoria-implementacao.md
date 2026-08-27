# [US-05] Implementação — Exclusividade Diária, Vistoria e Termo de Responsabilidade

> Documento técnico de como a US-05 foi implementada no backend (Spring Boot) e no frontend (Ionic/Angular). A especificação original está em [`docs/US/us5.md`](us5.md).

## 1. Visão geral da solução

A US-05 evolui o fluxo de reserva de espaços comuns com três pilares:

1. **Exclusividade diária entre espaços (RN01/RN02):** ao solicitar reserva, o sistema valida que não existe outra reserva ativa nos três espaços exclusivos (Salão de Festas, Churrasqueira, Campo de Futebol) para a mesma data, salvo quando todas as reservas conflitantes pertencem ao mesmo morador.

2. **Fluxo de vistoria e termo de responsabilidade:** para Salão de Festas e Churrasqueira, o ciclo de vida da reserva ganha novos status: `AWAITING_INSPECTION` → `AWAITING_SIGNATURE` → `CONFIRMED`. Para Campo de Futebol, a aprovação leva direto a `CONFIRMED` (sem vistoria — RN03).

3. **Gestão de convidados:** o morador cadastra nomes ao solicitar a reserva; no dia do evento, o funcionário marca a presença de cada convidado (check-in irreversível).

Fluxo ponta a ponta:

```
Residente solicita (data, notes, guestList)
   │  POST /lunaLink/reservation
   ▼
ReservationService.validateDailyExclusivity (RN01/RN02)
   │  Valida nos 3 espaços exclusivos como conjunto
   ▼
Reserva criada com status PENDING → evento ReservationRequestedEvent
   │
   ▼
Admin aprova (PUT /reservation/{id}/approve)
   │
   ├── Salão/Churrasqueira → AWAITING_INSPECTION + eventos (Approved + AwaitingInspection)
   │      ▼
   │  Funcionário submete vistoria pré-evento (POST /reservations/{id}/inspection?type=PRE_EVENT)
   │      ▼
   │  AWAITING_SIGNATURE + evento AwaitingSignatureEvent → morador notificado
   │      ▼
   │  Morador assina termo (POST /reservations/{id}/liability-term/sign)
   │      ▼
   │  CONFIRMED + evento ConfirmedEvent → morador notificado
   │      ▼
   │  Dia do evento: funcionário consulta convidados e faz check-in
   │      ▼
   │  Dia seguinte: PostInspectionScheduler notifica sobre vistoria pós-evento
   │
   └── Campo de Futebol → CONFIRMED (direto, sem vistoria)
```

---

## 2. Backend (Spring Boot)

### 2.1 Enums e configuração

**`ReservationStatus`** — status removido/adicionados:

| Status | Ação |
|--------|------|
| `APPROVED` | **Removido** (substituído por `CONFIRMED`) |
| `AWAITING_INSPECTION` | **Novo** — Salão/Churrasqueira após aprovação |
| `AWAITING_SIGNATURE` | **Novo** — Após vistoria pré-evento |
| `CONFIRMED` | **Novo** — Estado final para todos os fluxos |

**`InspectionType`** — enum novo:

| Valor | Descrição |
|-------|-----------|
| `PRE_EVENT` | Vistoria antes do evento |
| `POST_EVENT` | Vistoria depois do evento |

**`SpaceEquipmentCatalog`** — configuração estática de equipamentos por espaço:

| Espaço | Equipamentos |
|--------|-------------|
| `SALAO_FESTAS` | Mesas, Cadeiras, Freezer 1, Freezer 2, Fogão, Televisão |
| `CHURRASQUEIRA` | Grelhas, Aparatos de churrasco, Cadeiras, Tábuas, Freezer |
| `CAMPO_FUTEBOL` | _(vazio — sem vistoria)_ |

### 2.2 Entidades de domínio

#### `Reservation` (atualizada)

Arquivo: `domain/model/reservation/Reservation.java`

Novos campos adicionados:

| Campo | Tipo | Observação |
|-------|------|------------|
| `notes` | `String` | Observações do morador (opcional) |
| `guestList` | `List<Guest>` | `@OneToMany(cascade=ALL, orphanRemoval=true)` |
| `liabilityTerm` | `LiabilityTerm` | `@OneToOne(cascade=ALL, orphanRemoval=true)` |
| `inspections` | `List<SpaceInspection>` | `@OneToMany(cascade=ALL, orphanRemoval=true)` |

Novos métodos: `addGuest()`, `addInspection()`.

#### `Guest` (nova)

Arquivo: `domain/model/reservation/Guest.java`

| Campo | Tipo | Observação |
|-------|------|------------|
| `id` | `UUID` | `@GeneratedValue(AUTO)` |
| `name` | `String` | `@Column(nullable=false)` |
| `checkedIn` | `boolean` | Default `false` |
| `checkedInAt` | `LocalDateTime` | Nullable |
| `reservation` | `Reservation` | `@ManyToOne(LAZY, optional=false)` |

Método `checkIn()` define `checkedIn=true` e `checkedInAt=now()`.

#### `LiabilityTerm` (nova)

Arquivo: `domain/model/reservation/LiabilityTerm.java`

| Campo | Tipo | Observação |
|-------|------|------------|
| `id` | `UUID` | `@GeneratedValue(AUTO)` |
| `content` | `String` | `TEXT` — placeholder do termo |
| `signedByResident` | `boolean` | Default `false` |
| `signedAt` | `LocalDateTime` | Nullable |
| `reservation` | `Reservation` | `@OneToOne(LAZY, optional=false, unique=true)` |

Método `sign()` define `signedByResident=true` e `signedAt=now()`.

#### `SpaceInspection` (nova)

Arquivo: `domain/model/inspection/SpaceInspection.java`

| Campo | Tipo | Observação |
|-------|------|------------|
| `id` | `UUID` | `@GeneratedValue(AUTO)` |
| `type` | `InspectionType` | `PRE_EVENT` ou `POST_EVENT` |
| `notes` | `String` | `TEXT` — observações gerais |
| `inspectedAt` | `LocalDateTime` | Registrado no construtor |
| `reservation` | `Reservation` | `@ManyToOne(LAZY, optional=false)` |
| `employee` | `Users` | `@ManyToOne(LAZY, optional=false)` |
| `items` | `List<SpaceInspectionItem>` | `@OneToMany(cascade=ALL, orphanRemoval=true)` |

#### `SpaceInspectionItem` (nova)

Arquivo: `domain/model/inspection/SpaceInspectionItem.java`

| Campo | Tipo | Observação |
|-------|------|------------|
| `id` | `UUID` | `@GeneratedValue(AUTO)` |
| `equipmentName` | `String` | Nome do equipamento |
| `okConfirmed` | `boolean` | `true` = ok |
| `photoUrl` | `String` | URL da foto obrigatória |
| `inspection` | `SpaceInspection` | `@ManyToOne(LAZY)` |

### 2.3 Ports

#### Input (Application → Domain)

| Port | Método | Descrição |
|------|--------|-----------|
| `InspectionServicePort` | `submitInspection(reservationId, type, dto, employeeId)` | Submete vistoria pré/pós-evento |
| `LiabilityTermServicePort` | `signTerm(reservationId, residentId)` | Assina termo de responsabilidade |
| `GuestServicePort` | `getGuestsByReservation(reservationId)` | Lista convidados |
| | `checkInGuest(reservationId, guestId)` | Marca presença (irreversível) |

#### Output (Application → Infrastructure)

| Port | Método |
|------|--------|
| `InspectionRepositoryPort` | `save()`, `findById()`, `findByReservationIdAndType()`, `findByReservationId()` |
| `LiabilityTermRepositoryPort` | `save()`, `findById()`, `findByReservationId()` |
| `GuestRepositoryPort` | `save()`, `findById()`, `findByReservationId()`, `countByReservationId()` |
| `ReservationRepositoryPort` | Novo: `findByDateAndStatusAndSpaceTypes()` |

### 2.4 Services

#### `ReservationService`

Método `validateDailyExclusivity()` (novo, privado):
- Executado na criação de reserva (antes de persistir)
- Consulta reservas ativas (`PENDING`, `AWAITING_INSPECTION`, `AWAITING_SIGNATURE`, `CONFIRMED`) nos três tipos de espaço exclusivos
- Se existir reserva conflitante de outro morador → `IllegalStateException`
- Se todas conflitantes pertencerem ao mesmo morador → permitido

Método `approveReservation()` (atualizado):
- Verifica `SpaceEquipmentCatalog.requiresInspection(spaceType)`
- `true` (Salão/Churrasqueira) → status `AWAITING_INSPECTION`, publica `ReservationApprovedEvent` + `ReservationAwaitingInspectionEvent`
- `false` (Campo de Futebol) → status `CONFIRMED`, publica apenas `ReservationApprovedEvent`

#### `InspectionService`

Método `submitInspection()`:
- Valida status conforme tipo de vistoria
- Valida itens contra `SpaceEquipmentCatalog` (quantidade, nomes, foto obrigatória)
- Cria `SpaceInspection` com `SpaceInspectionItem`s
- Se `PRE_EVENT` → muda status para `AWAITING_SIGNATURE`, publica `ReservationAwaitingSignatureEvent`
- Se `POST_EVENT` → mantém status `CONFIRMED`, não publica evento

#### `LiabilityTermService`

Método `signTerm()`:
- Valida que o morador é o dono da reserva
- Valida que status é `AWAITING_SIGNATURE`
- Cria `LiabilityTerm` se não existir, ou valida que não está assinado
- Chama `term.sign()`
- Muda status para `CONFIRMED`, publica `ReservationConfirmedEvent`

#### `GuestService`

Método `getGuestsByReservation()`:
- Retorna lista de `GuestResponseDTO` vinculados à reserva

Método `checkInGuest()`:
- Valida que data atual == data da reserva (RN07)
- Valida que convidado pertence à reserva
- Valida que ainda não fez check-in (RN08 — irreversível)
- Chama `guest.checkIn()`, persiste

### 2.5 Eventos de domínio

| Evento | Publicado por | Destinatário |
|--------|--------------|-------------|
| `ReservationApprovedEvent` | `ReservationService.approveReservation()` | Admin + Morador |
| `ReservationAwaitingInspectionEvent` | `ReservationService.approveReservation()` | Funcionários |
| `ReservationAwaitingSignatureEvent` | `InspectionService.submitInspection()` | Morador |
| `ReservationConfirmedEvent` | `LiabilityTermService.signTerm()` | Morador |

### 2.6 Listeners

`ReservationEventListener` (atualizado):

- `handleReservationApprovedEvent()` — notifica admin e morador sobre aprovação
- `handleReservationAwaitingInspectionEvent()` — notifica funcionários para vistoria
- `handleReservationAwaitingSignatureEvent()` — notifica morador sobre termo disponível
- `handleReservationConfirmedEvent()` — notifica morador sobre confirmação final

### 2.7 Scheduled task

`PostInspectionScheduler`:
- `@Scheduled(cron = "0 0 8 * * *")` — executa todo dia às 8h
- Busca reservas `CONFIRMED` de Salão/Churrasqueira cuja data == ontem
- Notifica todos os funcionários sobre vistoria pós-evento pendente

### 2.8 Controllers

| Controller | Endpoint | HTTP | Acesso |
|-----------|----------|------|--------|
| `InspectionController` | `/{id}/inspection?type=PRE_EVENT\|POST_EVENT` | POST | EMPLOYEE |
| `LiabilityTermController` | `/{id}/liability-term/sign` | POST | Autenticado (valida dono) |
| `GuestController` | `/{id}/guests` | GET | ADMIN, EMPLOYEE |
| `GuestController` | `/{id}/guests/{guestId}/check-in` | PATCH | EMPLOYEE |

Base path: `/lunaLink/reservations`

### 2.9 Repositórios

| Repositório | Método novo |
|-------------|------------|
| `ReservationRepositoryPort` | `findByDateAndStatusAndSpaceTypes(date, status, spaceTypes)` — `@Query` com JOIN FETCH user + space |

### 2.10 Security

`SecurityConfiguration` atualizada com regras de acesso para os 5 novos endpoints.

---

## 3. Frontend (Angular/Ionic)

### 3.1 Modelos

**`enums.ts`** — `ReservationStatus` atualizado:

```typescript
AWAITING_INSPECTION = 'AWAITING_INSPECTION',
AWAITING_SIGNATURE = 'AWAITING_SIGNATURE',
CONFIRMED = 'CONFIRMED'
```

**`reservation.model.ts`** — interfaces adicionadas:

| Interface | Campos |
|-----------|--------|
| `GuestResponseDTO` | `id`, `name`, `checkedIn`, `checkedInAt?` |
| `LiabilityTermResponseDTO` | `id`, `content`, `signedByResident`, `signedAt?` |
| `ReservationCreateDTO` | Adicionados: `notes?`, `guestList?` |
| `ReservationResponseDTO` | Adicionados: `notes?`, `guestList?`, `liabilityTerm?` |

### 3.2 Serviço de API

**`reservation.service.ts`** — novos métodos:

| Método | HTTP | Endpoint |
|--------|------|----------|
| `submitInspection(id, type, dto)` | POST | `/reservations/{id}/inspection?type=...` |
| `signLiabilityTerm(id)` | POST | `/reservations/{id}/liability-term/sign` |
| `getGuests(id)` | GET | `/reservations/{id}/guests` |
| `checkInGuest(reservationId, guestId)` | PATCH | `/reservations/{id}/guests/{guestId}/check-in` |

### 3.3 Páginas

#### `reservation-create.page.ts` (atualizada)

Novos campos no formulário de criação:
- **Observações** — `ion-textarea` com `[(ngModel)]="notes"`
- **Lista de convidados** — input dinâmico com `ion-input` + botão adicionar, exibição em chips removíveis

#### `inspection.page.ts` (nova)

Rota: `/reservations/:id/inspection?type=PRE_EVENT|POST_EVENT`

- Carrega reserva por ID e exibe resumo (espaço + data)
- Gera lista de equipamentos do catálogo conforme tipo do espaço
- Para cada equipamento: checkbox de status (pré-marcado ok) + input de URL da foto
- Campo de observações gerais
- Validação: todos os equipamentos devem ter foto para habilitar envio
- Acesso: ADMIN, EMPLOYEE

#### `reservations-tab.page.ts` (atualizada)

Novos filtros de status:
- `AWAITING_INSPECTION` — "Vistoria"
- `AWAITING_SIGNATURE` — "Termo"
- `CONFIRMED` — "Confirmadas"

Novas ações nos cards:
- `AWAITING_INSPECTION` (admin) → botão "Realizar Vistoria"
- `AWAITING_SIGNATURE` (morador) → botão "Assinar Termo" com confirmação via AlertController
- `CONFIRMED` (admin) → botão "Cancelar Reserva"

Novos estilos de chip:
- `AWAITING_INSPECTION` — roxo (`#7c4dff`)
- `AWAITING_SIGNATURE` — laranja (`#ff9100`)

#### `reservations.page.ts` (atualizada)

`Record<ReservationStatus, string>` expandido com cores e labels para os 3 novos status.

### 3.4 Rotas

| Rota | Componente | Guard |
|------|-----------|-------|
| `/reservations/:id/inspection?type=...` | `InspectionPage` | `roleGuard([ADMIN, EMPLOYEE])` |

---

## 4. Testes

### 4.1 Backend — 25 novos testes

| Classe | Testes | Cenários principais |
|--------|--------|---------------------|
| `InspectionServiceTest` | 8 | Pré-evento OK, publicação de evento, pós-evento, status inválido, reserva não encontrada, itens inválidos, foto ausente |
| `LiabilityTermServiceTest` | 7 | Assinar termo, publicar evento, criar termo novo, morador errado, status inválido, termo já assinado, reserva não encontrada |
| `GuestServiceTest` | 8 | Listar convidados, reserva não encontrada, check-in OK, data errada, já fez check-in, convidado não pertence, convidado não existe, reserva não encontrada |
| `PostInspectionSchedulerTest` | 2 | Notificar funcionários, não notificar quando vazio |

Total: **218 testes, 0 falhas, BUILD SUCCESS**

### 4.2 Frontend

- `ng build` — BUILD SUCCESSFUL (warnings pré-existentes apenas)
- `ng lint` — OK

---

## 5. Arquivos criados/modificados

### 5.1 Novos (Backend)

| Caminho | Descrição |
|---------|-----------|
| `domain/enums/InspectionType.java` | Enum PRE_EVENT, POST_EVENT |
| `domain/model/reservation/Guest.java` | Entidade convidado |
| `domain/model/reservation/LiabilityTerm.java` | Entidade termo de responsabilidade |
| `domain/model/inspection/SpaceInspection.java` | Entidade vistoria |
| `domain/model/inspection/SpaceInspectionItem.java` | Entidade item de vistoria |
| `infrastructure/config/SpaceEquipmentCatalog.java` | Catálogo estático de equipamentos |
| `application/ports/input/InspectionServicePort.java` | Porta de entrada |
| `application/ports/input/LiabilityTermServicePort.java` | Porta de entrada |
| `application/ports/input/GuestServicePort.java` | Porta de entrada |
| `application/ports/output/InspectionRepositoryPort.java` | Porta de saída |
| `application/ports/output/LiabilityTermRepositoryPort.java` | Porta de saída |
| `application/ports/output/GuestRepositoryPort.java` | Porta de saída |
| `application/service/inspection/InspectionService.java` | Service de vistoria |
| `application/service/liabilityterm/LiabilityTermService.java` | Service de termo |
| `application/service/guest/GuestService.java` | Service de convidados |
| `application/service/reservation/PostInspectionScheduler.java` | Job agendado |
| `domain/events/reservationEvents/ReservationAwaitingInspectionEvent.java` | Evento |
| `domain/events/reservationEvents/ReservationAwaitingSignatureEvent.java` | Evento |
| `domain/events/reservationEvents/ReservationConfirmedEvent.java` | Evento |
| `web/controller/InspectionController.java` | Controller de vistoria |
| `web/controller/LiabilityTermController.java` | Controller de termo |
| `web/controller/GuestController.java` | Controller de convidados |
| `web/dto/ReservationsDTO/InspectionSubmitDTO.java` | DTO de submissão |
| `web/dto/ReservationsDTO/GuestResponseDTO.java` | DTO de resposta |
| `web/dto/ReservationsDTO/LiabilityTermResponseDTO.java` | DTO de resposta |
| `infrastructure/repository/reservation/GuestRepository.java` | Repositório JPA |
| `infrastructure/repository/reservation/LiabilityTermRepository.java` | Repositório JPA |
| `infrastructure/repository/inspection/SpaceInspectionRepository.java` | Repositório JPA |
| `test/.../inspection/InspectionServiceTest.java` | Teste unitário |
| `test/.../liabilityterm/LiabilityTermServiceTest.java` | Teste unitário |
| `test/.../guest/GuestServiceTest.java` | Teste unitário |
| `test/.../reservation/PostInspectionSchedulerTest.java` | Teste unitário |

### 5.2 Novos (Frontend)

| Caminho | Descrição |
|---------|-----------|
| `pages/reservations/inspection.page.ts` | Página de vistoria |

### 5.3 Modificados (Backend)

| Caminho | Mudança |
|---------|---------|
| `ReservationStatus.java` | Removido `APPROVED`, adicionados `AWAITING_INSPECTION`, `AWAITING_SIGNATURE`, `CONFIRMED` |
| `Reservation.java` | Campos `notes`, `guestList`, `liabilityTerm`, `inspections` |
| `ReservationService.java` | `validateDailyExclusivity()`, `approveReservation()` atualizado |
| `ReservationEventListener.java` | 4 novos handlers |
| `ReservationRepositoryPort.java` | `findByDateAndStatusAndSpaceTypes()` |
| `ReservationRepository.java` | Query JPA herdada da porta |
| `ReservationMapperImpl.java` | Mapeamento de novos campos |
| `ReservationServiceFacade.java` | Passa `notes` e `guestList` |
| `SecurityConfiguration.java` | Regras de acesso novas |
| `ReservationRequestDTO.java` | Campos `notes`, `guestList` |
| `ReservationCreateDTO.java` | Campos `notes`, `guestList` |
| `ReservationResponseDTO.java` | Campos `notes`, `guestList`, `liabilityTerm` |

### 5.4 Modificados (Frontend)

| Caminho | Mudança |
|---------|---------|
| `core/models/enums.ts` | 3 novos status em `ReservationStatus` |
| `core/models/reservation.model.ts` | `GuestResponseDTO`, `LiabilityTermResponseDTO`, campos novos em DTOs |
| `services/reservation.service.ts` | 4 novos métodos |
| `pages/reservations/reservation-create.page.ts` | Campos de observações e convidados |
| `pages/tabs/reservations-tab/reservations-tab.page.ts` | Filtros, labels, ações, estilos novos |
| `pages/reservations/reservations.page.ts` | Cores e labels novos |
| `app.routes.ts` | Rota de vistoria |
