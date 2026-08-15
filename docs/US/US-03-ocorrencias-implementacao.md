# [US-03] Implementação — Registro Digital de Ocorrências e Notificação em Tempo Real

> Documento técnico de como a US-03 foi implementada no backend (Spring Boot) e no frontend (Ionic/Angular). A especificação original está em [`docs/US/us1.md`](us1.md).

## 1. Visão geral da solução

A US-03 digitaliza o "livro de ocorrências": o morador (`RESIDENT_ROLE`) registra uma ocorrência pelo aplicativo e a administração (`ADMIN_ROLE`) é alertada **instantaneamente** via WebSocket (STOMP) e Web Push.

Fluxo ponta a ponta:

```
Morador (app)
   │  POST /lunaLink/occurrences  { description, incidentDate }
   ▼
OccurrenceController (validação @Valid + autenticação)
   ▼
OccurrenceService (regras de negócio + persistência @Transactional)
   │
   ├── save() → 201 Created → resposta ao morador
   ▼
eventPublisher.publishEvent(new OccurrenceCreatedEvent(...))   // após o commit
   ▼
OccurrenceEventListener (@Async + @TransactionalEventListener AFTER_COMMIT)
   ▼
SimpMessagingTemplate → /topic/notifications/{adminId}  (STOMP/WebSocket)
   ▼
WebPushService → notificação push para cada admin
```

---

## 2. Backend (Spring Boot 3.5)

### 2.1 Entidade de domínio `Occurrence`

Arquivo: `application/src/main/java/com/LunaLink/application/domain/model/occurrence/Occurrence.java`

| Campo | Tipo | Observação |
|---|---|---|
| `id` | `UUID` | `@GeneratedValue(strategy = GenerationType.AUTO)` |
| `user` | `Users` | `@ManyToOne(optional = false)` → coluna `user_id` |
| `description` | `String` | `@Column(columnDefinition = "TEXT")` — texto livre longo |
| `incidentDate` | `LocalDateTime` | **Data/hora do acontecimento (input do usuário)** |
| `createdAt` | `LocalDateTime` | **Data/hora da criação do registro (gerado automaticamente)** |

Atende ao **CA03 (Rastreabilidade)**: há separação clara entre `incidentDate` (informado pelo morador) e `createdAt` (auditoria de criação, preenchido pelo `@CreatedDate` + `AuditingEntityListener`).

### 2.2 DTOs

Arquivos: `application/src/main/java/com/LunaLink/application/web/dto/OccurrenceDTO/`

- **`OccurrenceCreateRequestDTO`** (entrada):
  - `description` — `@NotBlank`
  - `incidentDate` — `@NotNull`, tipo `LocalDateTime`

  > O backend espera `LocalDateTime` no formato ISO `YYYY-MM-DDTHH:mm:ss` (ex.: `2026-08-13T20:30:00`). Formatos com offset/`Z` (ex.: `toISOString()`) não são aceitos pelo deserializer padrão do Jackson.

- **`OccurrenceResponseDTO`** (saída): `id`, `userName`, `description`, `incidentDate`, `createdAt`.

### 2.3 Controller e rotas

Arquivo: `application/src/main/java/com/LunaLink/application/web/controller/OccurrenceController.java`
Base: `@RequestMapping("/lunaLink/occurrences")`

| Método | Rota | Ação |
|---|---|---|
| `POST` | `/lunaLink/occurrences` | Cria ocorrência → **201 Created** |
| `GET` | `/lunaLink/occurrences/findAll` | Lista (filtrada por role, ver §2.4) |
| `GET` | `/lunaLink/occurrences/find/{uuid}` | Busca por UUID (`@PathVariable`) |
| `DELETE` | `/lunaLink/occurrences/delete/{uuid}` | Remove |

O usuário autenticado é obtido via parâmetro `Authentication authentication` (`authentication.getName()` → email) e repassado ao serviço para o filtro por role.

### 2.4 Serviço `OccurrenceService`

Arquivo: `application/src/main/java/com/LunaLink/application/application/service/occurrence/OccurrenceService.java`

- `createOccurrence`:
  1. Rejeita `incidentDate` no futuro → `IllegalArgumentException` (400 via `GlobalExceptionHandler`).
  2. Carrega o usuário pelo email do token; inexistente → `IllegalArgumentException`.
  3. Persiste a entidade.
  4. Publica `OccurrenceCreatedEvent` (id, nome do morador, snippet da descrição ≤ 50 chars) via `EventPublisher`.
- `findAll` / `findById` / `deleteOccurrence`: **filtro por role** — `ADMIN_ROLE` enxerga todas; morador só as próprias (respeito à privacidade/auditoria).

### 2.5 Evento, Async e timing da notificação

- **Evento:** `domain/events/occurrenceEvents/OccurrenceCreatedEvent.java`
- **Publicador:** `infrastructure/eventPublisher/EventPublisher.java` (encapsula `ApplicationEventPublisher`)
- **Async:** `infrastructure/config/AsyncConfig.java` — `@EnableAsync` + bean `taskExecutor` (`ThreadPoolTaskExecutor`, core 2, max 8, queue 100, prefixo `async-`)

Listener — `application/listeners/OccurrenceEventListener.java`:

```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
public void handleOccurrenceCreatedEvent(OccurrenceCreatedEvent event) { ... }
```

Por que `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`:
- **AFTER_COMMIT**: a notificação só é disparada após o commit da transação, garantindo que o registro realmente foi persistido (**CA02**).
- **`fallbackExecution = true`**: mesmo sem transação ativa o listener executa (robustez).
- **`@Async`**: a entrega roda numa thread do pool `async-`, sem atrasar o `201 Created` de volta ao morador.

O listener busca todos os usuários `ADMIN_ROLE` e, para cada um:
1. Envia via STOMP para `/topic/notifications/{adminId}` com `SimpMessagingTemplate.convertAndSend`.
2. Envia Web Push (`WebPushService.sendPushNotificationToUser`).

### 2.6 WebSocket / STOMP

Arquivo: `infrastructure/config/WebSocketConfig.java`

- Endpoint SockJS: `/ws-lunalink` (com `JwtHandshakeInterceptor` + `JwtHandshakeHandler` e CORS restrito a `api.security.cors.allowed-origins`).
- Broker simples: `/topic` e `/queue`; prefixo de aplicação `/app`.

Autorização por tópico — `infrastructure/security/WebSocketAuthChannelInterceptor.java`:
- Exige usuário autenticado em `CONNECT`, `SUBSCRIBE` e `SEND`.
- Em `SUBSCRIBE`, valida a **posse do tópico**: só permite assinar `/topic/notifications/{uuid}` se o `uuid` for o `id` do próprio usuário autenticado (o `principal` do STOMP é o `Users` do JWT). Evita que um morador assine o canal de outro usuário/admin.

### 2.7 Segurança HTTP

Arquivo: `infrastructure/security/SecurityConfiguration.java`

```java
.requestMatchers(HttpMethod.POST, "/lunaLink/occurrences").hasRole("RESIDENT_ROLE")
.requestMatchers(HttpMethod.GET,  "/lunaLink/occurrences").authenticated()
.requestMatchers(HttpMethod.GET,  "/lunaLink/occurrences/{uuid}").authenticated()
.requestMatchers(HttpMethod.DELETE,"/lunaLink/occurrences/{uuid}").authenticated()
```

Abertura de ocorrência é restrita ao morador (**CA01**); leitura/remoção exigem apenas login (com o filtro por role do serviço em §2.4).

### 2.8 Tratamento de erros

Arquivo: `web/exception/GlobalExceptionHandler.java`

| Exceção | Status | Corpo |
|---|---|---|
| `MethodArgumentNotValidException` | **400** | `ValidationErrorDTO` com `validationErrors` por campo (**CA04**) |
| `IllegalArgumentException` | **400** | `StandardErrorDTO` (regras de negócio: data futura, não encontrado, sem permissão) |
| `IllegalStateException` | **409** | `StandardErrorDTO` |
| `Exception` (genérica) | **500** | `StandardErrorDTO` "Ocorreu um erro inesperado no servidor." |

### 2.9 Testes backend

- `web/controller/OccurrenceControllerWebTest` — MockMvc standalone (`201`, payload do frontend `2026-08-13T20:30:00`, `400` sem descrição, `400` sem data, `400` data futura).
- `application/service/occurrence/OccurrenceServiceTest` — criação com sucesso e data futura.
- `application/listeners/OccurrenceEventListenerTest`.
- Suíte completa: **124 testes, 0 falhas** (`./mvnw test`).

---

## 3. Frontend (Ionic / Angular)

### 3.1 Formulário de ocorrência (data + hora)

Arquivo: `client/luna-link/src/app/pages/occurrences/occurrence-create.page.ts`

- `ion-datetime` com `presentation="date-time"` e `[max]` = "agora" (impede selecionar futuro).
- O valor selecionado é normalizado pelo helper `shared/utils/date.utils.ts`:

```ts
export function toLocalDateTimeString(value: string | Date): string {
  const date = value instanceof Date ? value : new Date(value);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}
```

Produz `2026-08-13T20:30:00` (data/hora local, **sem** `Z`/offset) — exatamente o formato que o `LocalDateTime` do backend aceita.

- A página legada `pages/occurrences/occurrences.page.ts` usa o mesmo helper (substituiu o `toISOString()` que enviava `Z`).

### 3.2 Serviço de API

Arquivo: `client/luna-link/src/app/services/occurrence.service.ts`

- `create(payload)` → `POST {API_URL}/occurrences`
- `getAll()` → `GET {API_URL}/occurrences/findAll`
- `getById(uuid)` → `GET {API_URL}/occurrences/find/{uuid}`
- `delete(uuid)` → `DELETE {API_URL}/occurrences/delete/{uuid}`

### 3.3 Cliente WebSocket (STOMP + SockJS)

Arquivo: `client/luna-link/src/app/services/websocket.service.ts`

- Deps: `@stomp/stompjs`, `sockjs-client` (+ `@types/sockjs-client`).
- URL: `wsBaseUrl()` (= `API_URL` sem o sufixo `/lunaLink`) + `/ws-lunalink?access_token=<token>`.
- `connect()`: cria `Client` com `webSocketFactory: () => new SockJS(url)`, `reconnectDelay: 5000` e heartbeats de 10s; no `onConnect`, assina `/topic/notifications/{userId}`.
- `notifications$: Subject<NotificationDTO>` expõe as mensagens recebidas.
- Retry simples: se ainda não houver token/usuário, tenta novamente (5 tentativas, 1s).

`AppComponent` (`app/app.component.ts`):
- Assina `isAuthenticated$` → conecta/desconecta o WebSocket no login/logout.
- Assina `notifications$` → `uiService.showToast(notification.message, 'warning', 6000)` (alerta in-app do admin).

### 3.4 Ajustes de build/runtime

- `src/polyfills.ts`: `(window as any).global = window;` — o `sockjs-client` espera a variável Node `global`, que não existe no navegador (Vite/Angular 20). Sem isso, `ReferenceError: global is not defined`.
- `angular.json`: o diretório `public/` (contém `manifest.webmanifest` e ícones) foi adicionado a `assets`, pois o builder `@angular/build:application` do Angular 20 não copia `public/` automaticamente.

---

## 4. Correções relevantes feitas na implementação

| Problema | Correção |
|---|---|
| Endpoint era `POST /lunaLink/occurrences/create` | Passou a `POST /lunaLink/occurrences` (conforme US) |
| `findById` usava `@RequestParam` na rota `/find/{uuid}` | Troca para `@PathVariable` |
| Não havia `@EnableAsync` (o `@Async` do listener era ignorado) | Criado `AsyncConfig` com `taskExecutor` |
| Ocorrência visível a qualquer usuário (sem isolamento) | Filtro por role em `findAll`/`findById`/`delete` |
| Tópico de notificação podia ser assinado por qualquer um | `WebSocketAuthChannelInterceptor` valida posse do tópico |
| `incidentDate` quebrado no front (enviava `Z`/data sem hora) | `LocalDateTime` no DTO + helper `toLocalDateTimeString` e picker `date-time` |

---

## 5. Fluxo de validação (pontas a ponta)

1. Morador loga e abre **Nova Ocorrência**.
2. Seleciona data **e hora** do acontecimento, descreve e envia.
3. Backend valida (`@Valid` + regra de data futura) e persiste → **201 Created** + toast de sucesso.
4. Após o commit, o evento dispara a notificação assíncrona.
5. Admin com o app aberto recebe o alerta em tempo real (`/topic/notifications/{adminId}`) exibido como toast de aviso (e Web Push, se assinado).

## 6. Como testar

- Reinicie/rebuild o backend (a instância em debug do IntelliJ não recarrega classes alteradas automaticamente).
- Backend: `cd application && ./mvnw test`.
- Frontend: `cd client/luna-link && npm run build` (ou `npm start` para dev).
