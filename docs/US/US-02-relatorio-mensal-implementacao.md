# [US-02] Implementação — Relatório Mensal de Reservas Tarifadas

> Documento técnico de como a US-02 foi implementada no backend (Spring Boot) e no frontend (Ionic/Angular). A especificação original está em [`docs/US/us2.md`](us2.md).

## 1. Visão geral da solução

A US-02 automatiza a extração mensal das reservas **consumadas** nos espaços **tarifados** (Salão de Festas e Churrasqueira) para repasse à empresa terceirizada de cobrança.

Fluxo ponta a ponta:

```
Admin (app)
   │  GET /lunaLink/reservation/report/monthly?month=5&year=2026
   ▼
ReservationController (autenticação JWT + role ADMIN_ROLE)
   ▼
ReservationServiceFacade (porta de entrada)
   ▼
ReservationService.generateMonthlyReport(month, year)  @Transactional(readOnly = true)
   │   valida mês (1-12) e ano (>= 2020)
   ▼
ReservationRepository.findReservationsForReport(month, year, statuses, spaceTypes)
   │   JPQL com JOIN FETCH user + space; filtra status APPROVED e SpaceType SALAO_FESTAS/CHURRASQUEIRA
   ▼
List<MonthlyReservationReportDTO> { residentName, apartment, date, spaceType }
   ▼
200 OK (lista JSON) → tela de relatório
```

---

## 2. Backend (Spring Boot 3.5)

### 2.1 Filtros de negócio (CA01 e CA02)

Arquivo: `application/src/main/java/com/LunaLink/application/application/service/reservation/ReservationService.java`

```java
private static final List<ReservationStatus> VALID_STATUSES_FOR_REPORT = List.of(ReservationStatus.APPROVED);
private static final List<SpaceType> BILLABLE_SPACE_TYPES = List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA);
```

- **CA01**: só reservas com status **`APPROVED`** entram no relatório. `REJECTED`/`CANCELLED`/`PENDING` são ignorados.
  - **Decisão**: o domínio (`domain/enums/ReservationStatus.java`) não possui status `COMPLETED`; `APPROVED` é o único status de confirmação válido, então o critério "APPROVED **ou** COMPLETED" foi atendido mantendo apenas `APPROVED`.
- **CA02**: o filtro por `SpaceType` restringe exclusivamente aos espaços tarifados `SALAO_FESTAS` e `CHURRASQUEIRA`.

Validação de entrada (`generateMonthlyReport`): mês fora de `1..12` → `IllegalArgumentException("Mês inválido")`; ano `< 2020` → `IllegalArgumentException("Ano inválido")`. O `GlobalExceptionHandler` converte para **400 Bad Request**.

### 2.2 Query de persistência

A query existe na **porta de saída** e na **implementação JPA**:

- Porta: `application/ports/output/ReservationRepositoryPort.java`
- Impl: `infrastructure/repository/reservation/ReservationRepository.java`

```java
@Query("""
    SELECT r FROM Reservation r
    JOIN FETCH r.user
    JOIN FETCH r.space
    WHERE MONTH(r.date) = :month AND YEAR(r.date) = :year
    AND r.status IN :statuses
    AND r.space.type IN :spaceTypes
""")
List<Reservation> findReservationsForReport(
        @Param("month") int month,
        @Param("year") int year,
        @Param("statuses") List<ReservationStatus> statuses,
        @Param("spaceTypes") List<SpaceType> spaceTypes);
```

Por que `JOIN FETCH`:
- `Reservation.user` e `Reservation.space` são **LAZY**; o `fetch join` carrega ambos na **mesma query**, evitando `LazyInitializationException` e consultas N+1.
- Em conjunto com `@Transactional(readOnly = true)` no serviço, a extração independe do OSIV (`spring.jpa.open-in-view`).

> Nota de evolução: `MONTH()`/`YEAR()` não aproveitam índice de data. Se o volume crescer, migrar para `r.date BETWEEN :start AND :end` (início/fim do mês).

### 2.3 DTO de resposta (CA03)

Arquivo: `application/src/main/java/com/LunaLink/application/web/dto/ReservationsDTO/MonthlyReservationReportDTO.java`

```java
public record MonthlyReservationReportDTO(
        @JsonProperty("residentName") String residentName,
        @JsonProperty("apartment")   String apartment,
        @JsonProperty("date")        LocalDate date,
        @JsonProperty("spaceType")   String spaceType
) {}
```

Devolve **apenas** os dados do CA03 — não expõe `Users` (senha, email, tokenVersion) nem dados de infraestrutura de `Reservation`.

### 2.4 Serviço e Facade

`ReservationService.generateMonthlyReport`:

```java
@Transactional(readOnly = true)
@Override
public List<MonthlyReservationReportDTO> generateMonthlyReport(int month, int year) {
    if (month < 1 || month > 12) throw new IllegalArgumentException("Mês inválido: " + month);
    if (year < 2020) throw new IllegalArgumentException("Ano inválido: " + year);

    List<Reservation> reservations = reservationRepository.findReservationsForReport(
            month, year, VALID_STATUSES_FOR_REPORT, BILLABLE_SPACE_TYPES);

    return reservations.stream()
            .map(this::convertToReportDTO)
            .collect(Collectors.toList());
}
```

- Porta de entrada: `application/ports/input/ReservationServicePort.java` (assinatura `generateMonthlyReport`).
- Facade: `application/facades/reservation/ReservationServiceFacade.java` (repasse simples).

### 2.5 Controller e segurança

`web/controller/ReservationController.java`:

```java
@GetMapping("/report/monthly")
public ResponseEntity<List<MonthlyReservationReportDTO>> getMonthlyReport(
        @RequestParam int month, @RequestParam int year) {
    List<MonthlyReservationReportDTO> report = facade.generateMonthlyReport(month, year);
    return ResponseEntity.ok(report);
}
```

`infrastructure/security/SecurityConfiguration.java` (regra declarada **antes** do catch-all de reservas):

```java
.requestMatchers(HttpMethod.GET, "/lunaLink/reservation/report/**").hasRole("ADMIN_ROLE")
```

A ordem dos matchers garante que `/report/**` seja interceptado pela regra de Admin antes do `GET /lunaLink/reservation/**` (autenticado).

### 2.6 Testes backend

| Teste | Cobertura |
|---|---|
| `ReservationServiceTest` | happy path; mês inválido (0/13); ano inválido (< 2020); lista vazia; `verify` dos filtros exatos (`[APPROVED]`, `[SALAO_FESTAS, CHURRASQUEIRA]`) |
| `ReservationControllerTest` | `getMonthlyReport` retorna 200 e delega mês/ano corretos |
| `ReservationReportControllerWebTest` | MockMvc standalone: 200 com os 4 campos do CA03; mês inválido → 400; sem reservas → 200 `[]` |
| `ReservationReportSecurityTest` | `@WebMvcTest` + chain real: `ADMIN_ROLE` → 200; `RESIDENT_ROLE` → 403; anônimo → 401 |

Suíte completa: **135 testes, 0 falhas** (`./mvnw test`).

---

## 3. Frontend (Ionic / Angular)

### 3.1 Modelo

`client/luna-link/src/app/core/models/reservation.model.ts`:

```ts
export interface MonthlyReservationReportDTO {
  residentName: string;
  apartment: string;
  date: string;
  spaceType: SpaceType | string;
}
```

### 3.2 Serviço

`client/luna-link/src/app/services/reservation.service.ts`:

```ts
getMonthlyReport(month: number, year: number): Observable<MonthlyReservationReportDTO[]> {
  return this.http.get<MonthlyReservationReportDTO[]>(
    `${this.baseUrl}/reservation/report/monthly?month=${month}&year=${year}`
  );
}
```

### 3.3 Página de relatório

`client/luna-link/src/app/pages/reports/reports.page.ts`

- Filtros: `ion-select` de mês (12 opções em pt-BR) + `ion-input` numérico de ano, disparando `loadReport()` no `ionChange`.
- Resumo: card com total de reservas do período.
- Lista: nome do morador, apartamento, rótulo do espaço (`SALAO_FESTAS` → "Salão de Festas", `CHURRASQUEIRA` → "Churrasqueira") e data formatada em `pt-BR`.
- Estados: spinner de carregamento, lista vazia ("Nenhuma reserva neste período") e erro silencioso (`catchError(() => of([]))`).
- **Rota protegida**: `app.routes.ts` → `path: 'reports'` com `canActivate: [adminGuard]`; acesso pela home do admin (`home.page.ts`).

---

## 4. Correções feitas na implementação

| Problema | Correção |
|---|---|
| `generateMonthlyReport` sem transação + associações LAZY | `@Transactional(readOnly = true)` + `JOIN FETCH r.user/r.space` (sem depender do OSIV, sem N+1) |
| `jakarta.transaction.Transactional` não suporta `readOnly` | Import trocado para `org.springframework.transaction.annotation.Transactional` em `ReservationService` |
| Sem validação documentada de mês/ano | `IllegalArgumentException` (400 via `GlobalExceptionHandler`) |
| Sem cobertura de segurança do endpoint | `ReservationReportSecurityTest` (200/403/401) |
| CA01 citava `COMPLETED` | Decisão: manter `APPROVED` (único status de confirmação do domínio) |

---

## 5. Como testar

- Backend: `cd application && ./mvnw test`.
- Frontend: `cd client/luna-link && npm run build` (ou `npm start` para dev).

> Nota: o lint do frontend está pendente de migração do ESLint para flat config (`eslint.config.js`) — issue pré-existente, fora do escopo da US-02.
