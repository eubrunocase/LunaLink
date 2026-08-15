# [US-02] Planejamento — Exportação do Relatório Mensal (DOCX / PDF)

> Complemento à [US-02](../US/us2.md) já implementada (ver [`US-02-relatorio-mensal-implementacao.md`](../US/US-02-relatorio-mensal-implementacao.md)).
> Este documento descreve **o que precisa ser implementado** para que o administrador exporte o relatório de reservas tarifadas como arquivo **`.docx`** ou **`.pdf`**, salvo no dispositivo, para envio posterior (fora do app) à empresa de gestão orçamentária do condomínio.

---

## 1. Contexto e objetivo

O fluxo atual já entrega o relatório em **JSON** (`GET /lunaLink/reservation/report/monthly`). A modificação adiciona a capacidade de **exportar o mesmo conteúdo como documento formatado**:

- **DOCX** (editável) e **PDF** (pronto para envio) — opção do administrador.
- O arquivo é **baixado para o dispositivo do ADM** (o app é Ionic/Angular PWA; o "salvar no dispositivo" é feito pelo download do navegador).
- O envio à empresa de gestão é **fora do app** — não faz parte desta US.

**Não há mudança na lógica de dados**: valem os mesmos filtros já implementados — status `APPROVED` (CA01) e espaços tarifados `SALAO_FESTAS`/`CHURRASQUEIRA` (CA02), com os campos do CA03 (`residentName`, `apartment`, `date`, `spaceType`).

**Duas diretrizes de engenharia desta modificação:**

1. **Streaming / paginação** — o relatório nunca é carregado inteiro em memória: as reservas são lidas **em páginas** do banco (cursor) e o arquivo é entregue ao cliente via **`StreamingResponseBody`** (sem `byte[]` em heap).
2. **Processamento assíncrono** — a geração roda numa **task assíncrona** (`@Async`), com a API retornando **`202 Accepted` + `jobId`** imediatamente; o frontend **consulta o status** e baixa o arquivo quando pronto.

---

## 2. Decisões de arquitetura

| Decisão | Escolha | Motivo |
|---|---|---|
| Onde gerar o arquivo | **Backend** (Spring Boot) | Formatação consistente, reuso da query e filtros já existentes, extensível |
| Lib PDF | **`openhtmltopdf`** (core + pdf, v1.0.10) | HTML/CSS → PDF; relatório "formatado" a partir de template |
| Template PDF | **Thymeleaf** (`spring-boot-starter-thymeleaf`) | `resources/templates/report/reservation-monthly.html` — legível e versionável |
| Lib DOCX | **Apache POI** (`poi-ooxml`, v5.3.0+) | Geração programática de `.docx` (WordprocessingML), Apache 2.0 |
| Modelo de execução | **Assíncrono com job**: `POST` → `202` + `jobId` → polling de status → download | Não trava a thread do request em geração longa; feedback de progresso ao ADM |
| Task executor | Reuso do bean **`taskExecutor`** do `AsyncConfig` (core 2, max 8, queue 100) | Já existe no projeto (`@EnableAsync`) |
| Leitura de dados | **Paginação por cursor (keyset)** em `Reservation.id` | Memória limitada; evita o pitfall de `JOIN FETCH` + `Pageable` (paginação em memória) |
| Entrega do arquivo | **`StreamingResponseBody`** a partir de arquivo temporário | Nunca materializa o arquivo inteiro em heap; `Content-Disposition: attachment` |
| Armazenamento do job | `ConcurrentHashMap<String, ReportExportJob>` + **arquivo temporário** em disco | Simples e suficiente; TTL limpa jobs/files expirados |
| Frontend | Polling de status + download via `Blob`/`URL.createObjectURL` | Sem dependências novas (sem Capacitor) |

> **Alternativa considerada (não recomendada):** geração síncrona no request (todo `byte[]` em memória) — simples, porém bloqueia o request, não escala com volume e não dá feedback de progresso.

---

## 3. Plano Backend

### 3.1 Dependências novas (`application/pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-pdf</artifactId>
    <version>1.0.10</version>
</dependency>

<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.3.0</version>
</dependency>
```

> **Risco/ajuste**: o projeto já usa `org.bouncycastle:bcprov-jdk15on:1.70` (web-push). O `openhtmltopdf-pdf` traz o BouncyCastle transitivo (para assinatura PDF). Se houver conflito, **excluir** o `bcprov` transitivo do `openhtmltopdf` (assinatura não é usada nesta US) ou alinhar a versão.

### 3.2 Fluxo assíncrono (job de exportação)

```
Admin (app)
   │  POST /lunaLink/reservation/report/monthly/export?month=5&year=2026&format=pdf
   ▼
ReservationController ── valida mês/ano/format síncronamente
   ▼
ReportExportService.createJob(...) ── cria ReportExportJob {id, status=PROCESSING} no JobStore
   │   resposta: 202 Accepted { jobId, status: "PROCESSING" }
   ▼
taskExecutor (AsyncConfig)  ── @Async("taskExecutor")
   ▼
ReportExportService.generate(ReportExportJob)
   │  1. Lê reservas por páginas (keyset: r.id > lastId, ORDER BY id, LIMIT n)
   │  2. Para cada página: mapeia → escreve no documento (DOCX: linhas na tabela; PDF: HTML acumulado)
   │  3. Escreve o arquivo final em <tempDir>/relatorio-<jobId>.<ext>
   │  4. status = READY (+ contentLength) | ERRO → status = ERROR (+ mensagem)
   ▼
Admin (polling) → GET .../export/{jobId}/status → PROCESSING | READY | ERROR
Admin (download) → GET .../export/{jobId} → StreamingResponseBody (arquivo temporário)
   ▼
TTL (agendado): remove jobs/files com idade > 30 min
```

### 3.3 Arquivos a criar

1. **`domain/enums/ReportFormat.java`**
   ```java
   public enum ReportFormat { PDF, DOCX }
   ```

2. **`domain/enums/ReportExportStatus.java`**
   ```java
   public enum ReportExportStatus { PROCESSING, READY, ERROR }
   ```

3. **`application/service/report/ReportExportJob.java`** (modelo do job)
   ```java
   public class ReportExportJob {
       String id;              // UUID
       int month, year;
       ReportFormat format;
       ReportExportStatus status;
       String fileName;        // relatorio-reservas-05-2026.pdf
       String contentType;
       Path tempFile;          // preenchido ao finalizar
       long contentLength;
       String errorMessage;
       Instant createdAt, completedAt;
   }
   ```

4. **`application/ports/output/ReportExporterPort.java`** (porta de saída — **streaming**)
   ```java
   public interface ReportExporterPort {
       void export(Page<MonthlyReservationReportDTO> page, // ou Callback
                   ReportFormat format, ReportContext context,
                   OutputStream output) throws IOException;
   }
   // ReportContext: mês, ano, data/hora de geração, nome do condomínio
   ```

5. **`infrastructure/report/ReportExporter.java`** (impl)
   - **DOCX** (`XWPFDocument`): título "Relatório de Reservas Tarifadas — Maio/2026", tabela `XWPFTable` (cabeçalho `Morador | Apartamento | Data | Espaço` em negrito + linhas). As linhas são adicionadas **por página** conforme a paginação avança. `document.write(outputStream)` no final.
   - **PDF** (`openhtmltopdf`): o HTML (via template Thymeleaf) é **acumulado por página** em um `StringBuilder` (cabeçalho + linhas), renderizado **uma única vez** com `PdfRendererBuilder` (com `useFont` — ex. DejaVu — para acentuação).
   - **Streaming na entrega**: o documento pronto é gravado num **arquivo temporário**; o download usa `Files.copy(tempFile, outputStream)` dentro de `StreamingResponseBody`.
   - Lista vazia: gerar documento com cabeçalho + linha "Nenhuma reserva no período" (decisão: arquivo válido, não erro — validar com o PO).

6. **`web/dto/ReservationsDTO/ReportExportJobResponseDTO.java`**
   ```java
   public record ReportExportJobResponseDTO(String jobId, ReportExportStatus status, String errorMessage) {}
   ```

7. **`infrastructure/report/ReportExportJobStore.java`** — `ConcurrentHashMap<String, ReportExportJob>` + métodos `create/get/update`, com **TTL** (`@Scheduled` ou executor agendado) que apaga jobs/files com mais de **30 min** e arquivos temporários.

### 3.4 Paginação (otimização de leitura)

- **Cursor (keyset)** em `Reservation.id` — evita o pitfall de `JOIN FETCH` + `Pageable` (Hibernate paginaria em memória):
  ```java
  // Porta (ReservationRepositoryPort) + Impl (ReservationRepository)
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
      @Param("pageSize") int pageSize);   // via Pageable(PageRequest.of(0, pageSize))
  ```
- **Loop de consumo** (dentro de `@Transactional(readOnly = true)`):
  1. Página 1: `afterId = UUID(0)`; se `size < pageSize` → fim.
  2. Mapeia a página para `MonthlyReservationReportDTO` e escreve no documento.
  3. `afterId = última r.id` da página; repete até página menor que `pageSize`.
- Memória constante por página (`pageSize` sugerido: **500**); o arquivo final vive no **disco** (temp), não em heap.
- Observação: o objeto documento (DOCX/HTML) acumula as linhas por natureza — aceitável para o volume mensal de um condomínio; a paginação elimina o carregamento de **todas as entidades** de uma vez.

### 3.5 Arquivos a modificar

- **`application/ports/input/ReservationServicePort.java`** + **`application/service/reservation/ReservationService.java`** + **`application/facades/reservation/ReservationServiceFacade.java`**
  - `ReportExportJobResponseDTO createMonthlyReportExport(int month, int year, ReportFormat format)` — valida, cria o job e dispara a task assíncrona.
  - `ReportExportJobResponseDTO getMonthlyReportExportStatus(String jobId)`.
  - `Path getMonthlyReportExportFile(String jobId)` (acesso ao temp file do download; lança 404/409 se não `READY`).
- **`web/controller/ReservationController.java`**
  ```java
  @PostMapping("/report/monthly/export")
  public ResponseEntity<ReportExportJobResponseDTO> createExport(
          @RequestParam int month, @RequestParam int year, @RequestParam ReportFormat format) {
      ReportExportJobResponseDTO job = facade.createMonthlyReportExport(month, year, format);
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(job); // 202
  }

  @GetMapping("/report/monthly/export/{jobId}/status")
  public ResponseEntity<ReportExportJobResponseDTO> getExportStatus(@PathVariable String jobId) { ... }

  @GetMapping("/report/monthly/export/{jobId}")
  public ResponseEntity<StreamingResponseBody> downloadExport(@PathVariable String jobId) {
      Path file = facade.getMonthlyReportExportFile(jobId);
      StreamingResponseBody body = out -> Files.copy(file, out);
      return ResponseEntity.ok()
              .contentType(MediaType.parseMediaType(job.contentType()))
              .header(HttpHeaders.CONTENT_DISPOSITION,
                      "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName)
              .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(job.contentLength()))
              .body(body);
  }
  ```
- **`infrastructure/config/AsyncConfig.java`** — reuso do bean `taskExecutor` (sem mudança; pode-se adicionar um `reportExecutor` dedicado com métricas de fila, opcional).
- **`infrastructure/security/SecurityConfiguration.java`** — **nenhuma mudança**: o matcher existente `GET /lunaLink/reservation/report/**` → `ADMIN_ROLE` já cobre os 3 endpoints.

> **Nota**: o `POST` também cai no padrão `/lunaLink/reservation/report/**`. Adicionar explicitamente `.requestMatchers(HttpMethod.POST, "/lunaLink/reservation/report/**").hasRole("ADMIN_ROLE")` para clareza (o matcher atual é `GET`).

---

## 4. Plano Frontend (Ionic/Angular)

- **`client/luna-link/src/app/services/reservation.service.ts`**
  ```ts
  createExportJob(month: number, year: number, format: 'pdf' | 'docx') {
    return this.http.post<ReportExportJobResponse>(`${this.baseUrl}/reservation/report/monthly/export`,
      null, { params: { month, year, format } });
  }
  getExportStatus(jobId: string) {
    return this.http.get<ReportExportJobResponse>(`${this.baseUrl}/reservation/report/monthly/export/${jobId}/status`);
  }
  downloadExport(jobId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/reservation/report/monthly/export/${jobId}`, { responseType: 'blob' });
  }
  ```
- **`client/luna-link/src/app/pages/reports/reports.page.ts`**
  - Dois botões no card de resumo: **"Exportar PDF"** e **"Exportar DOCX"** (`ion-button` com `download-outline`).
  - `exportReport(format)`:
    1. `createExportJob(month, year, format)` → guarda `jobId`; exibe `ion-spinner` + "Gerando relatório...".
    2. **Polling**: `getExportStatus(jobId)` a cada **~1,5s** (com `timer`/`interval` + `takeWhile`).
    3. Quando `READY` → `downloadExport(jobId)` → `URL.createObjectURL(blob)` → `<a download>` → `revokeObjectURL` → toast de sucesso.
    4. Quando `ERROR` → cancela o polling e mostra toast de falha.
    5. Timeout de segurança (ex.: 5 min) para não pollar para sempre.
  - Sem novas dependências (download via navegador; em PWA mobile cai na pasta de downloads do aparelho).

---

## 5. Testes

| Camada | Teste | Verifica |
|---|---|---|
| Infra | `ReportExporterTest` | Arquivo temporário gerado tem **magic bytes** (PDF `%PDF`, DOCX `PK`); contém nome do morador; streaming para `OutputStream` funciona |
| Infra | `ReservationRepositoryTest` (`@DataJpaTest`, H2 já presente) | Paginação por cursor: páginas sem duplicidade/omissão; filtros `APPROVED` + tarifados |
| Serviço | `ReportExportServiceTest` | `createMonthlyReportExport` retorna `PROCESSING` e dispara task; `generate` conclui → `READY` + temp file; exceção → `ERROR`; paginação consome todas as páginas |
| Serviço | `ReservationServiceTest` | Delegação e validações de mês/ano/format |
| Web | `ReservationReportControllerWebTest` | `POST` → **202** + `jobId`; `GET status` → PROCESSING/READY; `GET download` → **200** `attachment` + `Content-Length` + corpo streamado |
| Segurança | `ReservationReportSecurityTest` | `ADMIN_ROLE` → 200/202; `RESIDENT_ROLE` → **403**; anônimo → **401** nos 3 endpoints |
| Manual | Abrir arquivos gerados | PDF e DOCX válidos no Word/Acrobat; acentuação correta; download salvo no dispositivo |

---

## 6. Ordem de implementação sugerida

1. Adicionar dependências no `pom.xml` e validar o build (`./mvnw test-compile`).
2. `ReportFormat` + `ReportExportStatus` + `ReportExportJob` + `ReportExportJobResponseDTO`.
3. `ReportExporterPort` + `ReportExporter` (DOCX via POI → depois PDF via template Thymeleaf + openhtmltopdf) + template HTML.
4. Paginação por cursor no `ReservationRepository` (+ porta) e loop de consumo no serviço.
5. `ReportExportJobStore` (TTL/cleanup agendado).
6. `ReportExportService` assíncrono (`@Async("taskExecutor")`) + `ReservationServicePort`/`Service`/`Facade`.
7. Endpoints no `ReservationController` + regra `POST .../report/**` explícita na segurança.
8. Testes de infra/serviço/web/segurança.
9. Frontend: serviço (job/status/download) + polling e botões na página de relatório.
10. Verificação final: `cd application && ./mvnw test`; build do cliente; abertura manual dos arquivos.

---

## 7. Critérios de aceite da modificação

- **AC1 — Escolha de formato**: o ADM pode escolher exportar como **DOCX** ou **PDF** na tela de relatório.
- **AC2 — Processamento assíncrono**: o `POST` responde **202 Accepted + jobId** imediatamente; o app acompanha o status até o arquivo ficar pronto.
- **AC3 — Download no dispositivo**: o arquivo é baixado com nome descritivo (`relatorio-reservas-MM-yyyy.ext`) e `Content-Disposition: attachment`.
- **AC4 — Mesmo conteúdo do relatório**: o arquivo contém apenas reservas `APPROVED` nos espaços tarifados, com `residentName`, `apartment`, `date`, `spaceType`.
- **AC5 — Otimização (streaming/paginação)**: as reservas são lidas em páginas e a resposta é streamada — sem carregar o relatório inteiro em memória.
- **AC6 — Segurança**: os 3 endpoints (`POST` criação, `GET status`, `GET download`) acessíveis somente com `ADMIN_ROLE`.
- **AC7 — Arquivos válidos**: DOCX/PDF abrem corretamente e preservam acentuação.

---

## 8. Riscos e observações

- **Conflito BouncyCastle** entre `web-push` (bcprov 1.70) e `openhtmltopdf` → excluir o transitivo se necessário (ver §3.1).
- **PDF bufferiza por natureza**: o `openhtmltopdf` precisa do HTML completo para renderizar (a renderização é em memória). A otimização fica na **leitura paginada** + **entrega por streaming** do arquivo resultante; se o PDF for gigante, avaliar lib streaming (ex.: `iText`/`docx4j` streaming) num futuro próximo.
- **DOCX acumula linhas no `XWPFDocument`**: aceitável para o volume mensal; monitorar se o número de reservas crescer muito.
- **Job store em memória**: jobs são perdidos no restart do app (aceitável para exportação de curta duração). Se necessário, evoluir para tabela no banco.
- **Backpressure**: a fila do `taskExecutor` (100) pode encher sob carga; adicionar limite de jobs concorrentes em `ReportExportJobStore` e retornar `429`/fila cheia se necessário.
- **Cleanup (TTL)**: imprescindível — remover temp files de jobs finalizados/expirados (30 min) via `@Scheduled` para não estourar disco.
- **Fonte no PDF**: fontes system não embutem acentuação automaticamente no openhtmltopdf → usar `useFont` com fonte embutida (ex.: DejaVu) ou fallback.
- **`filename*` UTF-8** no `Content-Disposition`: necessário para nome com acentos/trema.
- Lint do frontend segue quebrado (ESLint 9 sem `eslint.config.js`) — issue pré-existente, fora do escopo.
