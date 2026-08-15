# [US-02] Implementação — Exportação do Relatório Mensal (DOCX/PDF)

> Documento técnico da implementação da exportação do relatório mensal de reservas tarifadas em **DOCX** e **PDF**, com foco em **streaming**, **paginação keyset** e **processamento assíncrono**. A especificação original está em [`docs/Exec/US-02-exportacao-relatorio-docx-pdf.md`](../Exec/US-02-exportacao-relatorio-docx-pdf.md).

## 1. Visão geral da solução

A US-02 já entregava o relatório mensal em **JSON** (`GET /lunaLink/reservation/report/monthly`). A exportação adiciona a entrega do **mesmo conteúdo como documento formatado** (DOCX/PDF), seguindo três diretrizes de arquitetura:

1. **Streaming / paginação** — o relatório nunca é carregado inteiro em memória: as reservas são lidas **em páginas** do banco (cursor keyset) e o arquivo final é servido via **`StreamingResponseBody`** (sem `byte[]` em heap).
2. **Processamento assíncrono** — a geração roda numa **task assíncrona** (`@Async("taskExecutor")`); o `POST` responde **`202 Accepted` + `jobId`** imediatamente e o frontend **consulta o status** até o arquivo estar pronto.
3. **Autorização** — os 3 endpoints da exportação são protegidos com **`ADMIN_ROLE`**.

Fluxo ponta a ponta:

```
Admin (app)                                   Backend
   │  POST /lunaLink/reservation/report/monthly/export?month=5&year=2026&format=PDF
   ▼
ReservationController ──▶ ReservationServiceFacade ──▶ ReservationService
                                                          └─▶ ReportExportService.createJob()
                                                                    job criado (PROCESSING, UUID)
   ◀────────────────────────────── 202 Accepted { jobId, status: "PROCESSING" }
   │
   │  taskExecutor (@Async) ── ReportExportService.generate(job)
   │     │  TransactionTemplate (readOnly)
   │     │     página 1: keyset afterId = UUID(0)  ──▶ ReservationRepository.findReservationsForReportPage
   │     │     session.addRows(página)  ──▶ ReportExporter (DOCX: XWPFTable / PDF: HTML acumulado)
   │     │     página 2: afterId = última r.id da página 1
   │     │     ... até página < pageSize (500)
   │     │  ReportExporterSession.finish() ──▶ arquivo temporário em disco
   │     └─ job.markCompleted() (READY, contentLength)
   │
   │  GET .../export/{jobId}/status  ──▶ { status: "READY" } (polling 1,5s)
   ▼
   GET .../export/{jobId} ──▶ StreamingResponseBody (arquivo temporário) ──▶ 200 attachment
```

---

## 2. Arquitetura de streaming

### 2.1 Sem materializar o relatório em memória

O requisito central: **nenhum `byte[]` do arquivo final em heap**. A implementação separa a **produção** do arquivo (assíncrona, para disco) da **entrega** ao cliente (streaming a partir do disco):

| Fase | Onde vive | Como |
|---|---|---|
| Produção | `OutputStream` para **arquivo temporário** (`ReportExportJobStore.tempDir`) | Job assíncrono escreve o documento por páginas |
| Entrega | `StreamingResponseBody` lendo o **arquivo temporário** | O container responde em chunks; pico de heap ≈ tamanho de 1 chunk, não do arquivo |

```java
// ReservationController.downloadMonthlyReportExport
@GetMapping("/report/monthly/export/{jobId}")
public ResponseEntity<StreamingResponseBody> downloadMonthlyReportExport(@PathVariable String jobId) {
    ReportExportJob job = facade.getMonthlyReportExportFile(jobId);
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(job.getContentType()))
            .contentLength(job.getContentLength())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + job.getFileName() + "\"")
            .body(output -> copy(job, output));   // StreamingResponseBody (lambda)
}

private void copy(ReportExportJob job, OutputStream output) {
    try (InputStream in = Files.newInputStream(job.getTempFile())) {
        in.transferTo(output);                    // IO copy em chunks
    } catch (IOException e) {
        throw new UncheckedIOException("Falha ao enviar o arquivo do relatório.", e);
    }
}
```

- `Files.newInputStream(...).transferTo(output)` copia em **buffers** (8 KB) — sem carregar o arquivo.
- `Content-Length` é setado a partir de `job.getContentLength()` (tamanho real em disco), então o cliente conhece o tamanho antes do stream terminar.
- O `Content-Disposition: attachment` força o download com o nome do arquivo (`relatorio-reservas-05-2026.pdf`).

### 2.2 Porta de streaming: `ReportExporterSession`

A porta de saída `ReportExporterPort` modela a geração como **sessão incremental** — as linhas são adicionadas **por página**, sem acumular o documento inteiro antes da escrita:

```java
public interface ReportExporterPort {
    ReportExporterSession begin(ReportContext context, OutputStream output) throws IOException;

    interface ReportExporterSession {
        void addRows(List<MonthlyReservationReportDTO> rows) throws IOException;
        void finish() throws IOException;
    }
}
```

`ReportExporter` (`infrastructure/report/ReportExporter.java`) implementa a porta com **uma sessão por job** (`begin` retorna `PdfSession` ou `DocxSession`). Uma sessão por job evita estado compartilhado entre jobs concorrentes.

**DOCX** (`DocxSession`, Apache POI `XWPFDocument`):

- Título centralizado + subtítulo (condomínio, data de geração) + tabela `XWPFTable` com cabeçalho **Morador | Apartamento | Data | Espaço**.
- `addRows` cria linhas via `table.createRow()` incrementalmente.
- `finish` escreve o documento de uma vez no `OutputStream`: `document.write(output)`. O POI mantém o documento em memória, mas o **pico** é proporcional ao documento e ele é gravado **no arquivo temporário**, nunca no heap da resposta.

**PDF** (`PdfSession`, openhtmltopdf `PdfRendererBuilder`):

- Cada página é convertida em **HTML** (`<tr>`s) e acumulada num `StringBuilder` (apenas o HTML das linhas, não o documento).
- `finish` injeta `title`, `subtitle` e `rowsHtml` no template Thymeleaf `templates/report/reservation-monthly.html` e renderiza o PDF **diretamente para o `OutputStream`** via `builder.toStream(output) + builder.run()`.
- **Fonte**: `resolveFontFile()` localiza uma TTF do sistema (macOS/Linux) ou usa `-Dapp.report.font-file=...`. A fonte é registrada como família `ReportFont` para o HTML (`font-family: "ReportFont"`), garantindo acentuação pt-BR correta.
- **Escape de HTML** (`escapeHtml`) nos valores vindos do banco para evitar injeção de markup no template.

> Decisão de design: **`openhtmltopdf-pdfbox`** (não `openhtmltopdf-pdf`) — o artefato `-pdf` não existe no Maven Central; no `pom.xml` as dependências transitivas `bcprov-jdk18on`/`bcpkix-jdk18on` são excluídas para não conflitar com o `bouncycastle` já usado na assinatura JWT.

---

## 3. Paginação keyset (cursor)

### 3.1 Por que não `Pageable` simples

O filtro de negócio usa `JOIN FETCH r.user` e `JOIN FETCH r.space`. Combinar `JOIN FETCH` com `Pageable` do Spring Data faz o Hibernate paginar **em memória** (limite aplicado após carregar tudo). A solução é **pagination por cursor (keyset)** sobre `Reservation.id`:

```java
@Query("""
    SELECT r FROM Reservation r
    JOIN FETCH r.user
    JOIN FETCH r.space
    WHERE MONTH(r.date) = :month AND YEAR(r.date) = :year
    AND r.status IN :statuses
    AND r.space.type IN :spaceTypes
    AND r.id > :afterId
    ORDER BY r.id
""")
List<Reservation> findReservationsForReportPage(
        @Param("month") int month, @Param("year") int year,
        @Param("statuses") List<ReservationStatus> statuses,
        @Param("spaceTypes") List<SpaceType> spaceTypes,
        @Param("afterId") UUID afterId,
        Pageable pageable);
```

O `Pageable` é usado **apenas como `LIMIT`** (`PageRequest.of(0, pageSize)`), nunca para offset — o corte real é pelo cursor `r.id > :afterId`.

### 3.2 Loop de paginação

`ReportExportService.generateFile`:

```java
UUID afterId = MIN_UUID;                    // new UUID(0L, 0L) — menor id possível
int fetched;
do {
    List<Reservation> page = reservationRepository.findReservationsForReportPage(
            job.getMonth(), job.getYear(),
            ReportFilters.VALID_STATUSES, ReportFilters.BILLABLE_SPACE_TYPES,
            afterId, PageRequest.of(0, pageSize));   // pageSize = 500
    List<MonthlyReservationReportDTO> rows = page.stream().map(this::toReportDTO).toList();
    session.addRows(rows);
    fetched = page.size();
    if (fetched > 0) {
        afterId = page.get(fetched - 1).getId();     // avança o cursor
    }
} while (fetched == pageSize);                       // página cheia ⇒ ainda há mais
session.finish();
```

Regras:

1. Página 1: `afterId = UUID(0)` (menor id possível) → primeira página.
2. Se a página veio **cheia** (`size == pageSize`), há mais dados → avança o cursor para o **último id** retornado.
3. Se `size < pageSize`, o conjunto acabou → termina.
4. Memória constante por página: só `pageSize` (500) entidades + DTOs transitam por iteração.

**Filtros de negócio** (`ReportFilters`, constantes reaproveitadas pelo serviço síncrono e pela exportação):

```java
VALID_STATUSES       = List.of(ReservationStatus.APPROVED);   // CA01
BILLABLE_SPACE_TYPES = List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA);  // CA02
```

> Nota: como `r.id` é `UUID` aleatório e o PostgreSQL o compara **byte-a-byte** (diferente do `UUID.compareTo` do Java), a ordem do cursor é definida **pelo banco** de ponta a ponta (`ORDER BY r.id` + `r.id > :afterId` usam a mesma comparação). Código Java **não** deve reordenar por `compareTo` as páginas.

---

## 4. Processamento assíncrono

### 4.1 Ciclo de vida do job

`ReportExportJob` (`application/service/report/ReportExportJob.java`) é o núcleo imutável do job: `id` (UUID), `month`, `year`, `format`, `fileName`, `contentType`, `createdAt` (finais) + estado mutável (`status`, `tempFile`, `contentLength`, `errorMessage`, `completedAt`).

Máquina de estados:

```
PROCESSING ── sucesso ──▶ READY  (tempFile + contentLength preenchidos)
     │
     └─────── erro ─────▶ ERROR  (errorMessage preenchido)
```

- `create(...)` → `PROCESSING`; nome do arquivo: `relatorio-reservas-%02d-%d.%s` (ex.: `relatorio-reservas-05-2026.pdf`).
- `markCompleted()` → `READY`; `markFailed(message)` → `ERROR`.
- `ReportFormat` enum: `PDF("pdf", "application/pdf")` e `DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")`.

### 4.2 `@Async` com o `taskExecutor`

```java
@Async("taskExecutor")
public void generate(ReportExportJob job) {
    try {
        transactionTemplate.executeWithoutResult(status -> generateFile(job));
        jobStore.update(job.markCompleted());
    } catch (Exception e) {
        jobStore.update(job.markFailed(e.getMessage()));
    }
}
```

- O bean `taskExecutor` (`AsyncConfig`: `ThreadPoolTaskExecutor`, core **2**, max **8**, queue **100**, prefixo `async-`) — reutilizado com `@EnableAsync`/`@EnableScheduling`.
- **Chamada**: `ReservationService.createMonthlyReportExport` chama `reportExportService.generate(job)` e retorna `202` imediatamente; o trabalho real roda na thread `async-*`.
- **Transação**: `TransactionTemplate` com `readOnly = true` envolve toda a leitura paginada — a transação só abre quando a thread do executor processa o job, e cada página consulta dentro dela (sem `@Transactional` vazando para o request).
- **Erros**: qualquer exceção (banco, IO, fonte faltando) marca `ERROR` com a mensagem — nunca deixa o job preso em `PROCESSING`.

### 4.3 Polling de status e TTL

- `GET /report/monthly/export/{jobId}/status` → `ReportExportJobResponseDTO(jobId, status, errorMessage)`.
- `GET /report/monthly/export/{jobId}` → `getReadyJob(jobId)`: lança `IllegalStateException` (→ 400) se o job não está `READY`, e 404/400 se não existe/expirado.
- `ReportExportJobStore`: `ConcurrentHashMap<String, ReportExportJob>` (in-memory) + arquivos temporários em `java.io.tmpdir/lunalink-reports`.
- **TTL de 30 min**: `@Scheduled(fixedDelay = 60_000)` remove jobs com `createdAt > 30min` e apaga o arquivo temporário (`Files.deleteIfExists`, melhor esforço).

```java
@Scheduled(fixedDelay = 60_000)
public void cleanupExpired() {
    Instant cutoff = Instant.now().minus(REPORT_TTL);   // 30 min
    jobs.values().removeIf(job -> {
        boolean expired = job.getCreatedAt().isBefore(cutoff);
        if (expired) deleteTempFile(job);
        return expired;
    });
}
```

### 4.4 Endpoints e segurança

| Método | Path | Resposta | Regra |
|---|---|---|---|
| `POST` | `/lunaLink/reservation/report/monthly/export?month&year&format` | **202** + `{jobId, status}` | `ADMIN_ROLE` |
| `GET` | `/lunaLink/reservation/report/monthly/export/{jobId}/status` | 200 + status | `ADMIN_ROLE` |
| `GET` | `/lunaLink/reservation/report/monthly/export/{jobId}` | **200** `StreamingResponseBody` | `ADMIN_ROLE` |

`SecurityConfiguration.java` (matchers explícitos por método, cobrindo GET e POST):

```java
.requestMatchers(HttpMethod.GET,  "/lunaLink/reservation/report/**").hasRole("ADMIN_ROLE")
.requestMatchers(HttpMethod.POST, "/lunaLink/reservation/report/**").hasRole("ADMIN_ROLE")
```

---

## 5. Estrutura de camadas (hexagonal)

| Camada | Pacote / Arquivo | Responsabilidade |
|---|---|---|
| Porta de entrada | `application/ports/input/ReservationServicePort.java` | `createMonthlyReportExport`, `getMonthlyReportExportStatus`, `getMonthlyReportExportFile` |
| Serviço | `application/service/reservation/ReservationService.java` | Delega para `ReportExportService` e monta `ReportExportJobResponseDTO` |
| Facade | `application/facades/reservation/ReservationServiceFacade.java` | Repasse simples ao controller |
| Núcleo do job | `application/service/report/ReportExportJob.java`, `ReportContext.java`, `ReportFilters.java` | Estado do job, metadados do relatório, filtros CA01/CA02 |
| Orquestração assíncrona | `application/service/report/ReportExportService.java` | `createJob`, `@Async generate`, `getJob`, `getReadyJob`, loop keyset |
| Porta de saída (streaming) | `application/ports/output/ReportExporterPort.java` | `begin`/`addRows`/`finish` |
| Implementação do documento | `infrastructure/report/ReportExporter.java` | `DocxSession` (POI) e `PdfSession` (openhtmltopdf + Thymeleaf) |
| Store (jobs + TTL) | `infrastructure/report/ReportExportJobStore.java` | `ConcurrentHashMap`, temp dir, cleanup agendado |
| Persistência | `infrastructure/repository/reservation/ReservationRepository.java` | `findReservationsForReportPage` (keyset) |
| Controller | `web/controller/ReservationController.java` | 202/polling/download + `StreamingResponseBody` |
| Config | `infrastructure/config/AsyncConfig.java` | `taskExecutor`, `@EnableAsync`, `@EnableScheduling` |

**Dependências novas no `pom.xml`**:

- `spring-boot-starter-thymeleaf` — template HTML do PDF.
- `com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10` — renderização HTML → PDF (excluindo `bcprov-jdk18on`/`bcpkix-jdk18on`).
- `org.apache.poi:poi-ooxml:5.3.0` — geração DOCX.

---

## 6. Frontend (Ionic/Angular)

### 6.1 Serviço

`client/luna-link/src/app/services/reservation.service.ts`:

```ts
createMonthlyReportExport(month, year, format): Observable<ReportExportJobResponse>
  // POST .../report/monthly/export?month=&year=&format=

getMonthlyReportExportStatus(jobId): Observable<ReportExportJobResponse>
  // GET  .../report/monthly/export/${jobId}/status

downloadMonthlyReportExport(jobId): Observable<Blob>
  // GET  .../report/monthly/export/${jobId}   { responseType: 'blob' }
```

### 6.2 Página de relatório

`client/luna-link/src/app/pages/reports/reports.page.ts`:

- Botões **PDF** e **DOCX** no card "Exportar relatório" (desabilitados durante geração).
- `exportReport(format)`:
  1. `createMonthlyReportExport(...)` → recebe `{jobId}`.
  2. **Polling** a cada **1,5 s** (`interval` + `switchMap` + `takeWhile`) até `status != PROCESSING`.
  3. **Timeout de 5 min** — excede → erro "Tempo de geração do relatório excedido.".
  4. `status == READY` → `downloadMonthlyReportExport(jobId)` (blob); `status == ERROR` → toast com `errorMessage`.
  5. **Download**: `URL.createObjectURL(blob)` → `<a download>` clicado → `URL.revokeObjectURL`. Nome: `relatorio-reservas-MM-YYYY.{pdf|docx}`.
- Estados: spinner "Gerando relatório, aguarde...", erro vermelho, toast de sucesso.

---

## 7. Testes

| Teste | Cobertura |
|---|---|
| `ReportExportServiceTest` | cria job (validação de mês/ano/formato); `generate` completa → `READY` + `tempFile`; erro → `ERROR`; polling; TTL/cleanup |
| `ReportExporterTest` | DOCX/PDF gerados e gravados no `OutputStream`; cabeçalho/título; fonte resolvida |
| `ReservationRepositoryTest` | keyset: página ordenada, cursor avança, vazio após o último id; disjunção de páginas (ordem UUID do banco ≠ `UUID.compareTo` do Java) |
| `ReservationReportSecurityTest` | `ADMIN_ROLE` → 200/202; `RESIDENT_ROLE` → 403; anônimo → 401 nos endpoints |
| Frontend | `ng build` sem erros |

**Resultado**: `Tests run: 168, Failures: 0, Errors: 0, Skipped: 1` (`ApplicationTests` `@Disabled` por `.env`).

---

## 8. Decisões de design e evolução

- **Sessão por job** em `ReportExporterPort` (em vez da assinatura `export(...)` do plano): isolamento por job e escrita incremental garantida pela API.
- **`openhtmltopdf-pdfbox`** no lugar de `openhtmltopdf-pdf` (não publicado no Maven Central); exclusão de bouncycastle transitivo para não quebrar o JWT.
- **TTL in-memory + temp file** como armazenamento: simples e suficiente; para multi-instância, migrar para armazenamento externo (Redis/S3) mantendo o mesmo contrato de porta.
- **Evolução (backpressure)**: com fila do `taskExecutor` cheia (100), considerar limite de jobs concorrentes no `ReportExportJobStore` e resposta `429`/fila cheia.
- **Volume grande de PDF**: o HTML das linhas é acumulado no `StringBuilder` — se houver centenas de milhares de linhas, migrar para renderização por páginas de documento (openhtmltopdf suporta adicionar páginas incrementalmente via `addPage`).

## 9. Como testar

- Backend: `cd application && ./mvnw test`.
- Geração manual (PDF): `POST .../report/monthly/export?month=5&year=2026&format=PDF` com `Authorization: Bearer <admin>` → `202 {jobId}` → polling de status → `GET .../export/{jobId}` baixa o arquivo.
- Frontend: `cd client/luna-link && npm run build`.
