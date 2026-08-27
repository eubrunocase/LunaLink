# Implementação: Telas de Funcionário (EMPLOYEE) para Vistorias e Convidados

## Contexto

Esta documentação detalha o plano de implementação para as telas de funcionário solicitadas pela User Story 5 (Evolução do Fluxo de Reserva de Espaços Comuns). O funcionário (EMPLOYEE_ROLE) precisa realizar vistorias pré e pós-evento e gerenciar a lista de convidados no dia do evento, para os espaços SALAO_FESTAS, CAMPO_FUTEBOL e CHURRASQUEIRA.

**Referência:** `refactorEmployeeScreens.md`

---

## Decisão Arquitetural

**Recomendação:** Reaproveitar componentes e serviços existentes, criando novas páginas isoladas para as telas do funcionário.

**Justificativa:**
- O formulário de vistoria já existe em `inspection.page.ts` e funciona — basta garantir que o funcionário consiga acessá-lo
- A lista de reservas e a lista de convidados são telas com fluxo e dados distintos, mas compartilham o serviço `ReservationService` já existente
- Criar componentes isolados mantém o padrão arquitetural do projeto (standalone pages em `pages/`)
- O backend já possui todos os endpoints necessários para vistoria e convidados

---

## Escopo

- Espaços: SALAO_FESTAS, CAMPO_FUTEBOL, CHURRASQUEIRA
- CAMPO_FUTEBOL: não passa por vistoria (filtrado pelo backend em todos os pontos)
- Os demais espaços/equipamentos devem ser mantidos da forma que está

---

## Fluxo de Vistoria (Pós aprovação do ADM)

```
PRÉ-EVENTO:
  Reserva com status AWAITING_INSPECTION → Funcionário acessa lista → Preenche vistoria → Status muda para AWAITING_SIGNATURE

PÓS-EVENTO:
  Reserva com status CONFIRMED de ontem (SALAO_FESTAS/CHURRASQUEIRA) → Funcionário recebe notificação às 8h → Acessa lista → Preenche vistoria → Sem mudança de status
```

---

## Implementação Backend

### 1. Criar DTO de Resposta

**Arquivo:** `application/src/main/java/com/LunaLink/application/web/dto/ReservationsDTO/InspectionPendingReservationDTO.java`

```java
package com.LunaLink.application.web.dto.ReservationsDTO;

import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.domain.enums.SpaceType;
import java.time.LocalDate;
import java.util.UUID;

public record InspectionPendingReservationDTO(
    UUID reservationId,
    LocalDate date,
    SpaceType spaceType,
    String spaceName,
    InspectionType pendingType,
    String residentName
) {}
```

### 2. Adicionar Query no Repository

**Arquivo:** `application/.../application/ports/output/ReservationRepositoryPort.java`

Adicionar método na interface:

```java
List<Reservation> findByDateAndStatusInAndSpaceTypes(
    LocalDate date,
    List<ReservationStatus> statuses,
    List<SpaceType> spaceTypes
);
```

**Arquivo:** `application/.../infrastructure/repository/reservation/ReservationRepository.java`

Implementar com `@Query`:

```java
@Query("SELECT r FROM Reservation r " +
       "JOIN FETCH r.user u " +
       "JOIN FETCH r.space s " +
       "WHERE r.date = :date " +
       "AND r.status IN :statuses " +
       "AND s.type IN :spaceTypes")
List<Reservation> findByDateAndStatusInAndSpaceTypes(
    @Param("date") LocalDate date,
    @Param("statuses") List<ReservationStatus> statuses,
    @Param("spaceTypes") List<SpaceType> spaceTypes
);
```

### 3. Adicionar Método no Service

**Arquivo:** `application/.../application/ports/input/ReservationServicePort.java`

Adicionar método na interface:

```java
List<InspectionPendingReservationDTO> findPendingInspectionReservations();
```

**Arquivo:** `application/.../application/service/reservation/ReservationService.java`

Implementar:

```java
@Override
public List<InspectionPendingReservationDTO> findPendingInspectionReservations() {
    List<SpaceType> inspectionSpaces = List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA);

    // Pré-evento: reservas com status AWAITING_INSPECTION hoje
    List<Reservation> preEventReservations = reservationRepository
        .findByDateAndStatusInAndSpaceTypes(
            LocalDate.now(),
            List.of(ReservationStatus.AWAITING_INSPECTION),
            inspectionSpaces
        );

    // Pós-evento: reservas CONFIRMED de ontem
    List<Reservation> postEventReservations = reservationRepository
        .findByDateAndStatusInAndSpaceTypes(
            LocalDate.now().minusDays(1),
            List.of(ReservationStatus.CONFIRMED),
            inspectionSpaces
        );

    List<InspectionPendingReservationDTO> result = new ArrayList<>();

    preEventReservations.forEach(r -> result.add(new InspectionPendingReservationDTO(
        r.getId(),
        r.getDate(),
        r.getSpace().getType(),
        r.getSpace().getName(),
        InspectionType.PRE_EVENT,
        r.getUser().getName()
    )));

    postEventReservations.forEach(r -> result.add(new InspectionPendingReservationDTO(
        r.getId(),
        r.getDate(),
        r.getSpace().getType(),
        r.getSpace().getName(),
        InspectionType.POST_EVENT,
        r.getUser().getName()
    )));

    return result;
}
```

**Arquivo:** `application/.../application/facades/reservation/ReservationServiceFacade.java`

Adicionar método no facade:

```java
public List<InspectionPendingReservationDTO> findPendingInspectionReservations() {
    return service.findPendingInspectionReservations();
}
```

### 4. Criar Endpoint no Controller

**Arquivo:** `application/.../web/controller/ReservationController.java`

Adicionar endpoint:

```java
@GetMapping("/pending-inspection")
@PreAuthorize("hasAnyRole('ADMIN_ROLE', 'EMPLOYEE')")
public ResponseEntity<List<InspectionPendingReservationDTO>> getPendingInspection() {
    List<InspectionPendingReservationDTO> reservations =
        facade.findPendingInspectionReservations();
    return ResponseEntity.ok(reservations);
}
```

### 5. Liberar Endpoint no SecurityConfig

**Arquivo:** `application/.../infrastructure/security/SecurityConfiguration.java`

Adicionar regra de autorização:

```java
.requestMatchers(HttpMethod.GET, "/lunaLink/reservation/pending-inspection")
    .hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
```

---

## Implementação Frontend

### 6. Adicionar Método no Serviço

**Arquivo:** `client/luna-link/src/app/services/reservation.service.ts`

Adicionar interface:

```typescript
export interface InspectionPendingReservationDTO {
  reservationId: string;
  date: string;
  spaceType: SpaceType;
  spaceName: string;
  pendingType: 'PRE_EVENT' | 'POST_EVENT';
  residentName: string;
}
```

Adicionar método:

```typescript
getPendingInspectionReservations(): Observable<InspectionPendingReservationDTO[]> {
  return this.http.get<InspectionPendingReservationDTO[]>(
    `${this.apiUrl}/reservation/pending-inspection`
  );
}
```

### 7. Criar Página de Reservas do Funcionário

**Arquivo:** `client/luna-link/src/app/pages/employee/employee-reservations.page.ts` (novo)

Componente standalone que:
- Busca dados via `ReservationService.getPendingInspectionReservations()`
- Exibe lista com: nome do espaço, data, tipo de vistoria (Pré/Pré-evento), nome do morador
- Clicar em um item navega para `/reservations/:id/inspection?type=PRE_EVENT` ou `?type=POST_EVENT`
- Acesso: `EMPLOYEE` e `ADMIN_ROLE`
- Segue o padrão de componentes standalone existentes no projeto

### 8. Criar Página de Lista de Convidados

**Arquivo:** `client/luna-link/src/app/pages/guests/guest-check-in.page.ts` (novo)

Componente standalone que:
- Rota: `/reservations/:id/guests`
- Exibe lista de convidados via `ReservationService.getGuests()`
- No dia do evento: exibe botão de check-in por convidado
- Fora do dia do evento: exibe apenas consulta (sem botão)
- Check-in irreversível: convidado marcado fica com visual desabilitado/riscado, sem opção de reverter
- Usa `ReservationService.checkInGuest()`

### 9. Adicionar Rotas

**Arquivo:** `client/luna-link/src/app/app.routes.ts`

Adicionar novas rotas:

```typescript
{
  path: 'employee/reservations',
  loadComponent: () => import('./pages/employee/employee-reservations.page')
    .then(m => m.EmployeeReservationsPage),
  canActivate: [roleGuard([UserRoles.ADMIN_ROLE, UserRoles.EMPLOYEE])]
},
{
  path: 'reservations/:id/guests',
  loadComponent: () => import('./pages/guests/guest-check-in.page')
    .then(m => m.GuestCheckInPage),
  canActivate: [roleGuard([UserRoles.ADMIN_ROLE, UserRoles.EMPLOYEE])]
},
```

### 10. Atualizar Home Tab

**Arquivo:** `client/luna-link/src/app/pages/tabs/home-tab/home-tab.page.ts`

Adicionar card de acesso rápido "Vistorias Pendentes":
- Visível para `EMPLOYEE` e `ADMIN_ROLE`
- Card navega para `/employee/reservations`
- Adicionar card "Lista de Convidados" se aplicável (reservas confirmadas para hoje)

### 11. Atualizar Reservations Tab

**Arquivo:** `client/luna-link/src/app/pages/tabs/reservations-tab/reservations-tab.page.ts`

Adicionar link para lista de convidados na reserva:
- Visível quando a reserva tem status `CONFIRMED` e o usuário é `EMPLOYEE` ou `ADMIN_ROLE`
- Navega para `/reservations/:id/guests`
- Não alterar a estrutura existente do admin/morador

---

## Ordem de Implementação

| # | Tarefa | Camada |
|---|--------|--------|
| 1 | Criar `InspectionPendingReservationDTO` | Backend |
| 2 | Adicionar query `findByDateAndStatusInAndSpaceTypes` no Repository | Backend |
| 3 | Adicionar `findPendingInspectionReservations()` no Service/Port/Facade | Backend |
| 4 | Criar endpoint `GET /reservation/pending-inspection` no Controller | Backend |
| 5 | Liberar endpoint no `SecurityConfiguration` | Backend |
| 6 | Adicionar método `getPendingInspectionReservations()` no `ReservationService` frontend | Frontend |
| 7 | Criar `EmployeeReservationsPage` + rota | Frontend |
| 8 | Criar `GuestCheckInPage` + rota | Frontend |
| 9 | Atualizar `HomeTabPage` com cards de acesso rápido | Frontend |
| 10 | Adicionar link para lista de convidados na `ReservationsTabPage` | Frontend |
| 11 | Testar fluxo completo | Teste |

---

## Arquivos Afetados

| Ação | Arquivo |
|------|---------|
| **Novo** | `application/.../web/dto/ReservationsDTO/InspectionPendingReservationDTO.java` |
| **Novo** | `client/.../pages/employee/employee-reservations.page.ts` |
| **Novo** | `client/.../pages/guests/guest-check-in.page.ts` |
| Editar | `application/.../application/ports/output/ReservationRepositoryPort.java` |
| Editar | `application/.../infrastructure/repository/reservation/ReservationRepository.java` |
| Editar | `application/.../application/ports/input/ReservationServicePort.java` |
| Editar | `application/.../application/service/reservation/ReservationService.java` |
| Editar | `application/.../application/facades/reservation/ReservationServiceFacade.java` |
| Editar | `application/.../web/controller/ReservationController.java` |
| Editar | `application/.../infrastructure/security/SecurityConfiguration.java` |
| Editar | `client/.../app.routes.ts` |
| Editar | `client/.../services/reservation.service.ts` |
| Editar | `client/.../pages/tabs/home-tab/home-tab.page.ts` |
| Editar | `client/.../pages/tabs/reservations-tab/reservations-tab.page.ts` |

---

## Observações Importantes

1. **Post-evento às 8h:** O backend já dispara notificação via `PostInspectionScheduler` às 8h. A query para reservas `CONFIRMED` de ontem retorna exatamente as reservas cuja vistoria pós-evento está pendente a partir desse horário.

2. **CAMPO_FUTEBOL:** Filtrado pelo backend em todos os pontos:
   - `SpaceEquipmentCatalog` não tem entrada → `requiresInspection()` retorna `false`
   - Na aprovação, vai direto para `CONFIRMED` (pula `AWAITING_INSPECTION`)
   - `PostInspectionScheduler` exclui CAMPO_FUTEBOL da lista de notificações
   - Submissão de vistoria para CAMPO_FUTEBOL lança exceção

3. **Sem alterações no backend existente:** Apenas adições (novo DTO, nova query, novo endpoint, novo método no service). Nada do existente é modificado.

4. **Controle de acesso:** Todas as novas rotas usam `roleGuard([ADMIN_ROLE, EMPLOYEE])`. O funcionário não acessa telas de criação, aprovação/rejeição ou visualização geral de reservas.

5. **Convidados:** Check-in irreversível — uma vez marcado, o convidado fica desabilitado na interface sem opção de reverter.

6. **Vistoria pré e pós-evento:** A mesma estrutura de formulário serve para ambos os tipos, diferenciando apenas pelo query param `?type=PRE_EVENT` ou `?type=POST_EVENT`.

---

## Referências

- Documento original: `refactorEmployeeScreens.md`
- Backend: Spring Boot com arquitetura hexagonal (ports/adapters)
- Frontend: Ionic/Angular PWA com componentes standalone
- Controle de acesso: Route guards (`authGuard`, `roleGuard`, `adminGuard`)
- Padrão de rotas: Flat routes em `app.routes.ts` (sem módulos lazy-loaded)
