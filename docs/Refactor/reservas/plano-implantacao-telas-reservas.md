# Plano de Implementação — Reformulação da Tela de Reservas

## Contexto

O aplicativo (Ionic/Angular) possui hoje um fluxo de reservas com dois problemas centrais:

1. **UX/UI insatisfatória:** a interface não comunica bem em qual etapa do workflow cada reserva está.
2. **Acoplamento entre espaços comuns e equipamento:** a reserva da TV comunitária está misturada na mesma tela/fluxo das reservas de espaço.

Esta especificação define o plano de implementação para redesenhar as telas de reservas, separando visualmente e estruturalmente a gestão de **reservas de espaço** da gestão de **reserva de equipamento (TV)**.

**Especificação de referência:** `application/docs/Refactor/specScreen.md`

---

## Decisões Confirmadas

- Tab "Reservas" permanece como atalho (redireciona conforme role do usuário)
- Aprovação/rejeição do admin integrada na listagem geral do funcionário
- Academia removida (apenas Salão, Churrasqueira, Campo de Futebol)
- Implementação completa de uma vez (todas as fases)

---

## Estrutura Atual (Problemas Identificados)

| Problema | Detalhes |
|----------|----------|
| `ReservationsTabPage` (867 linhas) | Mistura space + equipment numa `UnifiedReservation` |
| Sem tela de detalhe | Todas as ações acontecem inline nos cards da listagem |
| Sem componentes reutilizáveis | `shared/components/` está vazio; lógica duplicada em 5+ páginas |
| Rotas duplicadas | `/reservations` e `/tabs/reservations` coexistem |
| Sem timeline de etapas | Workflow multi-etapa não é visualizado como timeline |
| Chips de status inconsistentes | `global.scss` usa classes lowercase, pages usam uppercase; lógica duplicada |

---

## Fluxos de Trabalho (Workflows)

### Salão de Festas / Churrasqueira (US-05)

```
PENDING → (Admin aprova) → AWAITING_INSPECTION → (Funcionário vistoria pré) → AWAITING_SIGNATURE → (Morador assina termo) → CONFIRMED → (Funcionário vistoria pós no dia seguinte)
         \→ (Admin rejeita) → REJECTED
         \→ (Cancelamento) → CANCELLED
```

### Campo de Futebol (US-05)

```
PENDING → (Admin aprova) → CONFIRMED
         \→ (Admin rejeita) → REJECTED
         \→ (Cancelamento) → CANCELLED
```

### Televisão Comunitária (US-04)

```
CONFIRMED (auto) → (Funcionário entrega) → IN_USE → (Funcionário devolve) → RETURNED
                  \→ (Cancelamento) → CANCELED
```

---

## Fase 1 — Fundação: Extrair Código Duplicado

**Objetivo:** Eliminar as 12+ duplicações identificadas entre arquivos, criando utilitários compartilhados.

### 1a. Utilitários de data (`shared/utils/date.utils.ts`)

Expandir o arquivo existente, extraindo de todos os 8 arquivos atuais:

| Função | Origem | Descrição |
|--------|--------|-----------|
| `formatDate(date)` | todos os 8 arquivos | Formata `LocalDate` em pt-BR (ex: "15/08/2026") |
| `formatDateTime(dateTime)` | reservations-tab, equipment-reservations, equipment-reservation-create, guest-check-in | Formata `LocalDateTime` em pt-BR |
| `getDay(date)` | reservations-tab, employee-reservations | Extrai dia do mês |
| `getMonth(date)` | reservations-tab, employee-reservations | Extrai nome do mês abreviado |
| `checkIfEventDay(date)` | guest-check-in | Compara data com hoje |
| `normalizeTime(timeValue)` | equipment-reservation-create | Normaliza valor do `ion-datetime` |

### 1b. Constantes de domínio (`shared/constants/`)

#### `space.constants.ts`

```typescript
// Remover ACADEMIA conforme decisão confirmada
SPACE_LABELS: Record<SpaceType, string>    // { SALAO_FESTAS: 'Salão de Festas', ... }
SPACE_ICONS: Record<SpaceType, string>     // { SALAO_FESTAS: 'balloon-outline', ... }
SPACE_EQUIPMENT_CATALOG: Record<SpaceType, string[]>  // Apenas SALAO e CHURRASQUEIRA
EXCLUSIVE_SPACE_TYPES: SpaceType[]         // [SALAO_FESTAS, CHURRASQUEIRA, CAMPO_FUTEBOL]
```

#### `status.constants.ts`

```typescript
RESERVATION_STATUS_LABELS: Record<ReservationStatus, string>
RESERVATION_STATUS_CLASSES: Record<ReservationStatus, string>  // Classes CSS unificadas
EQUIPMENT_STATUS_LABELS: Record<EquipmentReservationStatus, string>
EQUIPMENT_STATUS_CLASSES: Record<EquipmentReservationStatus, string>
```

### 1c. Atualizar `models/enums.ts`

- Remover `ACADEMIA` do enum `SpaceType`

### Arquivos a modificar

- `shared/utils/date.utils.ts` (expandir)
- Criar `shared/constants/space.constants.ts`
- Criar `shared/constants/status.constants.ts`
- `core/models/enums.ts` (remover ACADEMIA)

---

## Fase 2 — Componentes Visuais Compartilhados

Todos os componentes em `shared/components/`, standalone, com `standalone: true`.

### 2a. `ReservationStatusChip`

- **Arquivo:** `shared/components/reservation-status-chip/reservation-status-chip.component.ts`
- **Inputs:** `status: ReservationStatus | EquipmentReservationStatus`, `type: 'space' | 'equipment'`
- **Template:** `<ion-chip [class]="'chip-' + status">` com label do mapa de constantes
- **Estilos:** Usar as classes CSS unificadas do `global.scss`

### 2b. `ReservationTimeline`

- **Arquivo:** `shared/components/reservation-timeline/reservation-timeline.component.ts`
- **Inputs:** `status: ReservationStatus`, `spaceType: SpaceType`
- **Template:** Timeline vertical com etapas:
  - SALAO_FESTAS/CHURRASQUEIRA: Solicitado → Aprovação → Vistoria Pré → Assinatura → Confirmado → Vistoria Pós
  - CAMPO_FUTEBOL: Solicitado → Aprovação → Confirmado (sem vistoria, sem termo)
- Cada etapa: ícone (✓ concluído, → em andamento, ○ pendente) + label + cor semântica
- Estilo inspirado em apps de delivery (iFood/Uber Eats) — "acompanhar pedido"

### 2c. `ReservationCard`

- **Arquivo:** `shared/components/reservation-card/reservation-card.component.ts`
- **Inputs:** `reservation`, `viewMode: 'resident' | 'employee' | 'admin'`, `actions: CardAction[]`
- **Template:**
  - Badge de data (dia/mês) no canto esquerdo
  - Tipo do espaço/equipamento + nome do morador (se admin/employee)
  - `ReservationStatusChip`
  - Timestamps relevantes
  - Slot de ações (botões contextuais)
- **Output:** `actionClick` event

### 2d. `InspectionItemForm`

- **Arquivo:** `shared/components/inspection-item-form/inspection-item-form.component.ts`
- **Inputs:** `equipmentName: string`, `photoUrl: string`, `checked: boolean`
- **Outputs:** `photoUrlChange`, `checkedChange`
- **Template:** Card com nome do equipamento, toggle/checkbox, campo de upload de foto

### 2e. `GuestListItem`

- **Arquivo:** `shared/components/guest-list-item/guest-list-item.component.ts`
- **Inputs:** `guest: GuestResponseDTO`, `isEventDay: boolean`, `canCheckIn: boolean`
- **Outputs:** `checkIn`
- **Template:** Item com nome, timestamp de check-in, botão de check-in (habilitado só no dia do evento), estado "riscado" visual

### 2f. `ReservationFilters`

- **Arquivo:** `shared/components/reservation-filters/reservation-filters.component.ts`
- **Inputs:** `showSpaceFilter: boolean`, `showDateFilter: boolean`, `spaceOptions: SpaceType[]`
- **Outputs:** `filtersChange` com `{ dateFrom?, dateTo?, spaceType? }`
- **Template:** Filtros de data (período) e espaço (segment/chips)

### 2g. `EmptyState`

- **Arquivo:** `shared/components/empty-state/empty-state.component.ts`
- **Inputs:** `icon: string`, `title: string`, `message: string`
- **Template:** Ícone + título + mensagem centralizada

### 2h. `SkeletonList`

- **Arquivo:** `shared/components/skeleton-list/skeleton-list.component.ts`
- **Inputs:** `count: number` (default 3)
- **Template:** Lista de skeleton cards

### 2i. Atualizar `global.scss`

Unificar classes de chip de status — remover duplicações entre `global.scss` e scoped styles:

```css
/* Status chips unificados */
.chip-PENDING        { --background: var(--ion-color-tertiary); --color: #000; }
.chip-APPROVED       { --background: var(--ion-color-success); --color: #fff; }
.chip-REJECTED       { --background: var(--ion-color-danger); --color: #fff; }
.chip-CANCELLED      { --background: var(--ion-color-danger); --color: #fff; }
.chip-CANCELED       { --background: var(--ion-color-danger); --color: #fff; }
.chip-CONFIRMED      { --background: var(--ion-color-primary); --color: #fff; }
.chip-AWAITING_INSPECTION  { --background: #7c4dff; --color: #fff; }
.chip-AWAITING_SIGNATURE   { --background: #ff9100; --color: #000; }
.chip-IN_USE         { --background: var(--ion-color-warning); --color: #000; }
.chip-RETURNED       { --background: var(--ion-color-success); --color: #fff; }
```

---

## Fase 3 — Rotas e Navegação

### 3a. Nova estrutura de rotas (`app.routes.ts`)

```typescript
// === RESERVAS DE ESPAÇOS (MORADOR) ===
{ path: 'reservas/espacos', component: ReservasEspacosPage,
  canActivate: [roleGuard([ADMIN_ROLE, RESIDENT_ROLE])] },

{ path: 'reservas/espacos/nova', component: NovaReservaEspacoPage,
  canActivate: [roleGuard([ADMIN_ROLE, RESIDENT_ROLE])] },

{ path: 'reservas/espacos/:id', component: ReservaEspacoDetalhePage,
  canActivate: [roleGuard([ADMIN_ROLE, RESIDENT_ROLE])] },

{ path: 'reservas/espacos/:id/termo', component: TermoResponsabilidadePage,
  canActivate: [roleGuard([ADMIN_ROLE, RESIDENT_ROLE])] },

// === RESERVAS DE EQUIPAMENTOS (MORADOR) ===
{ path: 'reservas/equipamentos', component: ReservasEquipamentosPage,
  canActivate: [roleGuard([ADMIN_ROLE, RESIDENT_ROLE])] },

{ path: 'reservas/equipamentos/nova', component: NovaReservaEquipamentoPage,
  canActivate: [roleGuard([ADMIN_ROLE, RESIDENT_ROLE])] },

// === FUNCIONÁRIO / ADMIN ===
{ path: 'funcionario/reservas', component: FuncionarioReservasPage,
  canActivate: [roleGuard([ADMIN_ROLE, EMPLOYEE])] },

{ path: 'funcionario/reservas/:id/vistoria', component: VistoriaPage,
  canActivate: [roleGuard([ADMIN_ROLE, EMPLOYEE])] },

{ path: 'funcionario/reservas/:id/convidados', component: FuncionarioConvidadosPage,
  canActivate: [roleGuard([ADMIN_ROLE, EMPLOYEE])] },

{ path: 'funcionario/equipamentos', component: FuncionarioEquipamentosPage,
  canActivate: [roleGuard([ADMIN_ROLE, EMPLOYEE])] },

// === TAB ATALHO ===
{ path: 'tabs/reservations', redirectTo: 'reservas/espacos', pathMatch: 'full' },
```

### 3b. Atualizar `TabsPage`

Tab "Reservas" funciona como atalho:
- Admin/Resident → `/reservas/espacos`
- Employee → `/funcionario/reservas`

### 3c. Atualizar `HomePage`

- Links de "Reservas" e "Equipamentos" apontando para as novas rotas
- Stats do dashboard atualizados

---

## Fase 4 — Telas do Morador

### Tela 1 — Listagem de Reservas de Espaços

- **Arquivo:** `pages/reservas/espacos/reservas-espacos.page.ts`
- **Rota:** `/reservas/espacos`
- **Serviços:** `ReservationService.getByUser()`
- **Componentes:** `ReservationCard`, `ReservationFilters`, `ReservationStatusChip`, `SkeletonList`, `EmptyState`
- **Funcionalidades:**
  - Lista reservas do morador (passadas e futuras)
  - Filtros: período (data) e espaço (Salão, Churrasqueira, Campo)
  - FAB button "Nova Reserva"
  - Pull-to-refresh
  - Card click → detalhe (`/reservas/espacos/:id`)

### Tela 2 — Nova Solicitação de Reserva de Espaço

- **Arquivo:** `pages/reservas/espacos/nova-reserva-espaco.page.ts`
- **Rota:** `/reservas/espacos/nova`
- **Adaptar de:** `reservation-create.page.ts` — remover Academia e TV
- **Funcionalidades:**
  - Grid de 3 espaços (Salão, Churrasqueira, Campo de Futebol)
  - Seleção de data com validação de disponibilidade em tempo real
  - Campo de observações (texto livre)
  - Lista de convidados (input dinâmico para adicionar nomes)
  - Validação de conflito de data (feedback claro)
  - Tela de sucesso após criação

### Tela 3 — Detalhe da Reserva de Espaço (NOVA)

- **Arquivo:** `pages/reservas/espacos/reserva-espaco-detalhe.page.ts`
- **Rota:** `/reservas/espacos/:id`
- **Serviços:** `ReservationService.getById()`, `ReservationService.getGuests()`
- **Componentes:** `ReservationTimeline`, `ReservationStatusChip`, `GuestListItem`
- **Funcionalidades:**
  - Resumo: espaço, data, observações, lista de convidados
  - `ReservationTimeline` completa (etapas visuais)
  - Ações contextuais:
    - `AWAITING_SIGNATURE` → botão "Assinar Termo"
    - Status ativo → botão "Cancelar"
    - `CONFIRMED` + (SALAO/CHURRASQUEIRA) → botão "Lista de Convidados" (employee/admin)

### Tela 3b — Termo de Responsabilidade

- **Arquivo:** `pages/reservas/espacos/termo-responsabilidade.page.ts`
- **Rota:** `/reservas/espacos/:id/termo`
- **Serviços:** `ReservationService.getById()`, `ReservationService.signLiabilityTerm()`
- **Funcionalidades:**
  - Exibe texto do termo para leitura
  - Botão "Assinar e Confirmar"
  - Feedback de sucesso, navega de volta ao detalhe

### Tela 4 — Listagem de Reserva de Equipamento

- **Arquivo:** `pages/reservas/equipamentos/reservas-equipamentos.page.ts`
- **Rota:** `/reservas/equipamentos`
- **Serviços:** `EquipmentReservationService.listMine()`
- **Componentes:** `ReservationCard` (variante equipamento), `ReservationStatusChip`, `SkeletonList`, `EmptyState`
- **Funcionalidades:**
  - Lista reservas de TV do morador
  - Cards: data/horário, status chip
  - Ações: Cancelar (CONFIRMED)
  - FAB "Nova Reserva"

### Tela 5 — Nova Solicitação de Reserva de Equipamento

- **Arquivo:** `pages/reservas/equipamentos/nova-reserva-equipamento.page.ts`
- **Rota:** `/reservas/equipamentos/nova`
- **Adaptar de:** `equipment-reservation-create.page.ts` (apenas formulário)
- **Funcionalidades:**
  - Formulário simples: data + horário início/fim
  - Validação de conflito (409)
  - Confirmação imediata (self-service, sem aprovação)
  - Feedback visual de "Reserva confirmada"

---

## Fase 5 — Telas do Funcionário

### Tela 6 — Listagem Geral de Reservas (INTEGRADA COM APROVAÇÃO DO ADMIN)

- **Arquivo:** `pages/funcionario/funcionario-reservas.page.ts`
- **Rota:** `/funcionario/reservas`
- **Serviços:** `ReservationService.getAll()` (admin) ou `getPendingInspectionReservations()` (employee)
- **Componentes:** `ReservationCard`, `ReservationFilters`, `ReservationStatusChip`, `SkeletonList`, `EmptyState`
- **Funcionalidades:**
  - Lista **todas** as reservas de Salão, Churrasqueira e Campo de Futebol
  - Filtros por data e espaço
  - Seção destacada "Aguardando sua ação":
    - Vistorias pré-evento pendentes (AWAITING_INSPECTION)
    - Vistorias pós-evento pendentes
  - Reservas pendentes de aprovação (PENDING) — **visível apenas para admin**
  - **Ações do admin integradas:** Botões "Aprovar"/"Rejeitar" em cards PENDING
  - Campo de Futebol aparece apenas para consulta

### Tela 7 — Formulário de Vistoria

- **Arquivo:** `pages/funcionario/vistoria.page.ts`
- **Rota:** `/funcionario/reservas/:id/vistoria`
- **Adaptar de:** `inspection.page.ts`
- **Componentes:** `InspectionItemForm`
- **Funcionalidades:**
  - Lista equipamentos do espaço (catálogo)
  - Cada item: toggle de confirmação + upload de foto (obrigatório)
  - Campo de observações gerais (opcional)
  - Botão desabilitado até todos os itens completos
  - Reutilizada para pré e pós-evento (query param `?tipo=PRE_EVENT|POST_EVENT`)

### Tela 8 — Lista de Convidados + Check-in

- **Arquivo:** `pages/funcionario/funcionario-convidados.page.ts`
- **Rota:** `/funcionario/reservas/:id/convidados`
- **Adaptar de:** `guest-check-in.page.ts`
- **Componentes:** `GuestListItem`
- **Funcionalidades:**
  - Lista convidados cadastrados pelo morador
  - No dia do evento: check-in habilitado (irreversível)
  - Fora do dia do evento: lista somente leitura
  - Contador de presentes

### Tela 9 — Gestão de Reserva de Equipamento (Entrega/Devolução)

- **Arquivo:** `pages/funcionario/funcionario-equipamentos.page.ts`
- **Rota:** `/funcionario/equipamentos`
- **Adaptar de:** `equipment-reservations.page.ts`
- **Serviços:** `EquipmentReservationService.list()`
- **Funcionalidades:**
  - Lista reservas de TV do dia (ou próximas)
  - Marcar retirada (`IN_USE`) quando morador comparecer
  - Marcar devolução (`RETURNED`) quando morador devolver

---

## Fase 6 — Integração do Administrador

- Admin usa as **mesmas pages** do morador + funcionário
- Na `FuncionarioReservasPage`: admin vê botões de aprovação/rejeição (via `*ngIf="isAdmin"`)
- Nas pages de detalhe/equipamento: admin tem todas as ações disponíveis
- Sem necessidade de telas exclusivas adicionais

---

## Fase 7 — Limpeza

### Arquivos a remover

| Arquivo | Linhas | Substituído por |
|---------|--------|-----------------|
| `pages/tabs/reservations-tab/reservations-tab.page.ts` | 867 | ReservasEspacosPage + FuncionarioReservasPage |
| `pages/reservations/reservations.page.ts` | 366 | ReservasEspacosPage |
| `pages/reservations/reservation-create.page.ts` | 528 | NovaReservaEspacoPage |
| `pages/reservations/inspection.page.ts` | 306 | VistoriaPage |
| `pages/equipment-reservations/equipment-reservations.page.ts` | 276 | FuncionarioEquipamentosPage |
| `pages/equipment-reservations/equipment-reservation-create.page.ts` | 604 | ReservasEquipamentosPage + NovaReservaEquipamentoPage |
| `pages/employee/employee-reservations.page.ts` | 287 | FuncionarioReservasPage |
| `pages/guests/guest-check-in.page.ts` | 307 | FuncionarioConvidadosPage |
| **Total** | **~3.540** | |

### Rotas a remover de `app.routes.ts`

- `/reservations`, `/reservations/new`, `/reservations/:id/inspection`, `/reservations/:id/guests`
- `/equipment-reservations`, `/equipment-reservations/new`
- `/employee/reservations`

---

## Regras de Acesso

| Tela | Morador | Funcionário | Admin |
|------|---------|-------------|-------|
| Listagem de reservas de espaço (próprias) | ✅ | — | ✅ |
| Listagem geral de todas as reservas | ❌ | ✅ | ✅ |
| Nova solicitação (espaço/equipamento) | ✅ | ❌ | ✅ |
| Detalhe da reserva | ✅ (próprias) | ✅ (consulta) | ✅ |
| Assinatura do termo | ✅ (próprias) | ❌ | ✅ |
| Formulário de vistoria (pré/pós) | ❌ | ✅ | ✅ |
| Lista de convidados (consulta) | ✅ (próprias) | ✅ | ✅ |
| Check-in de convidados | ❌ | ✅ (no dia) | ✅ |
| Entrega/devolução de equipamento | ❌ | ✅ | ✅ |
| Aprovação/rejeição de reserva | ❌ | ❌ | ✅ |

---

## Resumo de Arquivos

### Arquivos a criar (~3.480 linhas estimadas)

| Arquivo | Tipo | Linhas Est. |
|---------|------|-------------|
| `shared/utils/date.utils.ts` (expandir) | Utilitário | ~60 |
| `shared/constants/space.constants.ts` | Constantes | ~40 |
| `shared/constants/status.constants.ts` | Constantes | ~50 |
| `shared/components/reservation-status-chip/` | Componente | ~40 |
| `shared/components/reservation-timeline/` | Componente | ~120 |
| `shared/components/reservation-card/` | Componente | ~100 |
| `shared/components/inspection-item-form/` | Componente | ~60 |
| `shared/components/guest-list-item/` | Componente | ~50 |
| `shared/components/reservation-filters/` | Componente | ~80 |
| `shared/components/empty-state/` | Componente | ~30 |
| `shared/components/skeleton-list/` | Componente | ~40 |
| `pages/reservas/espacos/reservas-espacos.page.ts` | Page | ~200 |
| `pages/reservas/espacos/nova-reserva-espaco.page.ts` | Page | ~350 |
| `pages/reservas/espacos/reserva-espaco-detalhe.page.ts` | Page | ~250 |
| `pages/reservas/espacos/termo-responsabilidade.page.ts` | Page | ~80 |
| `pages/reservas/equipamentos/reservas-equipamentos.page.ts` | Page | ~180 |
| `pages/reservas/equipamentos/nova-reserva-equipamento.page.ts` | Page | ~250 |
| `pages/funcionario/funcionario-reservas.page.ts` | Page | ~300 |
| `pages/funcionario/vistoria.page.ts` | Page | ~250 |
| `pages/funcionario/funcionario-convidados.page.ts` | Page | ~200 |
| `pages/funcionario/funcionario-equipamentos.page.ts` | Page | ~200 |

### Arquivos a modificar

- `app.routes.ts` — novas rotas
- `pages/tabs/tabs.page.ts` — tab atalho
- `pages/home/home.page.ts` — links atualizados
- `global.scss` — classes de chip unificadas
- `core/models/enums.ts` — remover ACADEMIA

---

## Ordem de Execução

```
1. shared/utils/date.utils.ts + shared/constants/           ← Fundação
2. shared/components/ (todos os 8)                          ← Componentes base
3. global.scss (unificar chip classes)                       ← Tema
4. app.routes.ts + tabs.page.ts                             ← Navegação
5. pages/reservas/espacos/ (4 pages)                         ← Morador espaço
6. pages/reservas/equipamentos/ (2 pages)                    ← Morador equipamento
7. pages/funcionario/ (4 pages)                              ← Funcionário + admin
8. home.page.ts (atualizar links)                           ← Dashboard
9. Limpeza (remover pages antigas)                          ← Final
```

---

## Backend — Etapa Posterior

Conforme a especificação (`specScreen.md`): **não haverá alterações no backend nesta fase**. Todas as pages consomem os endpoints já existentes.

Após a implementação das telas, a etapa posterior será:

1. Mapear quais endpoints/queries alimentam cada nova tela
2. Revisar queries dos repositórios JPA (possíveis erros de mapeamento, paginação, formato)
3. Corrigir DTOs/queries conforme necessário
4. Verificar se os endpoints atuais retornam exatamente o que as novas pages precisam

---

## Referências de Layout/UX

- **Calendly / Cal.com** — horários disponíveis e status de agendamento
- **Booking.com (app mobile)** — cards de reserva com status colorido, timeline de etapas
- **Google Calendar (mobile)** — visualização de agenda combinada com lista
- **Apps de delivery (iFood/Uber Eats)** — componente de "timeline de status"

### Princípios de UX

- **Status sempre visível:** chip/badge com cor semântica em cada reserva
- **Timeline de etapas:** para Salão/Churrasqueira, exibir visualmente em qual etapa está
- **Ações contextuais:** botões aparecem apenas quando fazem sentido para o ator e status
- **Consistência:** mesmos componentes base em telas de espaço e equipamento
