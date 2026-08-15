# WebSocket — Correção de Autorização no Handshake (AuthorizationDeniedException)

> Documento técnico do bug de segurança observado no WebSocket (SockJS + STOMP) durante a geração do relatório mensal (US-02), da causa raiz à solução aplicada.

## 1. Problema

O frontend (Ionic/Angular) conecta ao backend via WebSocket com SockJS + STOMP para receber notificações em tempo real (`/topic/notifications/{userId}`). Ao gerar o relatório mensal pela tela de reports, o log do Spring Boot registrava:

```
org.springframework.security.authorization.AuthorizationDeniedException: Access Denied
```

Apesar do erro no log, o relatório era gerado normalmente — o **fallback do SockJS** (XHR polling) mascarava o problema. O diagnóstico revelou que **toda requisição de upgrade WebSocket estava sendo negada pelo Spring Security**; o SockJS caía para transporte XHR e a conexão "funcionava", mas por caminho alternativo.

## 2. Causa raiz

### 2.1 O matcher de URL não casava upgrades WebSocket

O `SecurityConfiguration` liberava o WebSocket com o matcher padrão baseado em string:

```java
.requestMatchers("/ws-lunalink/**").permitAll()
```

Com Spring MVC no classpath, `.requestMatchers(String...)` cria um **`MvcRequestMatcher`**, que resolve o padrão via `HandlerMappingIntrospector` — ou seja, exige um **handler mapping** que case com o padrão. As requisições de upgrade do WebSocket são roteadas pelo `SimpleUrlHandlerMapping`/`HttpRequestHandlerServlet` do SockJS e **não eram casadas** pelo `MvcRequestMatcher`.

Resultado: o upgrade caía no catch-all `anyRequest().authenticated()`.

### 2.2 O token não chegava ao contexto de segurança

O `SecurityFilter` (filtro JWT) lê apenas o header:

```
Authorization: Bearer <accessToken>
```

Já o SockJS envia o token pela **query string** (`?access_token=...`), que o SockJS replica em todas as requisições de transporte. Sem o header, o `SecurityFilter` não autenticava nada → contexto anônimo → `AuthorizationFilter` negava → `AuthorizationDeniedException`.

### 2.3 Confirmação empírica

Com `logging.level.org.springframework.security=TRACE`, o log confirmou o fluxo da negação:

```
Securing GET /ws-lunalink/xyz/abc/websocket
Invoking AuthorizationFilter (13/13)      ← sem match de matcher anterior → nega
JwtHandshakeInterceptor returns false from beforeHandshake - precluding handshake
```

O mesmo teste com o fix aplicado mostra o `AuthorizationFilter` **passando** o upgrade (sem deny), e o `JwtHandshakeInterceptor` assumindo a autenticação.

## 3. Solução aplicada

### 3.1 Matcher por path: `PathPatternRequestMatcher`

`SecurityConfiguration.java`:

```java
// WebSocket (SockJS): usa PathPatternRequestMatcher (match por path) porque o
// MvcRequestMatcher não casa requisições de upgrade WebSocket (caem em
// anyRequest().authenticated() e geram AuthorizationDeniedException no log + fallback do SockJS)
.requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/ws-lunalink/**")).permitAll()
.requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/ws-lunalink")).permitAll() // Handshake (GET/PUT)
```

- O `PathPatternRequestMatcher` (Spring Security 6.5) casa **somente pelo path**, via `PathPatternParser`, sem depender de handler mapping.
- É o **substituto oficial** do `AntPathRequestMatcher`, que está `@Deprecated(forRemoval = true)` desde o Spring Security 6.1 (a primeira tentativa de fix usou `AntPathRequestMatcher.antMatcher(...)` e foi trocada para evitar a deprecação).
- O `permitAll()` no `AuthorizationFilter` vale para todos os paths do SockJS (`/info`, transportes `/websocket`, `/xhr*`, `/eventsource`, `/iframe.html`). **A autenticação real do WebSocket é feita pelo `JwtHandshakeInterceptor`** (abaixo), não pelo Spring Security de servlet.

### 3.2 Autenticação no handshake (`JwtHandshakeInterceptor`)

A autenticação do WebSocket é delegada ao interceptor registrado no endpoint (não ao `AuthorizationFilter`):

```java
addEndpoint("/ws-lunalink")
        .setAllowedOriginPatterns(allowedOrigins)
        .addInterceptors(jwtHandshakeInterceptor)
        .setHandshakeHandler(jwtHandshakeHandler)
        .withSockJS();
```

`JwtHandshakeInterceptor.beforeHandshake`:

```java
String token = extractAccessToken(request.getURI());   // lê access_token da query
if (token == null) return reject(response);            // 401
Users user = tokenAuthenticator.authenticate(token);   // valida JWT + blacklist + versão
attributes.put(PRINCIPAL_ATTRIBUTE,
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
return true;
```

- `extractAccessToken` parseia a query string do URI (o SockJS envia `?access_token=...` em **todas** as requisições, inclusive o upgrade).
- `TokenAuthenticator.authenticate` reusa a mesma validação do `SecurityFilter` (assinatura JWT, blacklist de `jti`, `token_version`).
- O principal é gravado no atributo `WS_PRINCIPAL` e consumido pelo `JwtHandshakeHandler` no `WebSocketSession`; o `WebSocketAuthChannelInterceptor` valida comandos STOMP (CONNECT/SUBSCRIBE) e tópicos `/topic/notifications/{userId}`.

### 3.3 Fluxo final do handshake

```
SockJS (frontend) ── GET /ws-lunalink/info?access_token=…  → permitAll → 200
                   ── GET /ws-lunalink/{server}/{session}/websocket?access_token=…
                      (Upgrade: websocket)
                        │  SecurityFilter (sem header → não mexe)
                        ▼  AuthorizationFilter → PathPatternRequestMatcher casa → permitAll
                      SockJsHttpRequestHandler
                        ▼  JwtHandshakeInterceptor.beforeHandshake
                            access_token → TokenAuthenticator → WS_PRINCIPAL
                        ▼  JwtHandshakeHandler → WebSocketSession com principal
                      STOMP CONNECT/SUBSCRIBE → WebSocketAuthChannelInterceptor
```

## 4. Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `infrastructure/security/SecurityConfiguration.java` | Matchers `/ws-lunalink/**` e `/ws-lunalink` migrados para `PathPatternRequestMatcher` |
| `infrastructure/security/JwtHandshakeInterceptor.java` | **Sem alteração** — já autenticava via query; o bug era só no matcher do servlet |
| `test/.../infrastructure/security/WebSocketSecurityRegressionTest.java` | **Novo** — teste de regressão do handshake |
| `test/.../infrastructure/repository/reservation/ReservationRepositoryTest.java` | Correção de asserção frágil (ver §5) |

## 5. Testes

### 5.1 Teste de regressão do WebSocket

`WebSocketSecurityRegressionTest` — `@SpringBootTest` + `@AutoConfigureMockMvc`, com `@MockitoBean TokenAuthenticator` para simular token válido:

| Teste | Cenário | Asserção |
|---|---|---|
| `websocketUpgradeIsNotDenied` | transporte `/ws-lunalink/xyz/abc/websocket` com `Upgrade: websocket` + `access_token` | status **≠ 401** (passa pelo `AuthorizationFilter`) |
| `handshakeIsNotDenied` | `/ws-lunalink` com upgrade + `access_token` | status **≠ 401** |
| `infoIsReachable` | `/ws-lunalink/info` | **200** |

> Observação: no MockMvc, um upgrade real não chega a completar (não há container servlet), então o status final pode ser 400 — artefato do MockMvc, não do app. A propriedade testada é a **não negação** (nunca 401).

### 5.2 Correção no teste keyset do relatório

`findReservationsForReportPage_shouldReturnPagedKeyset` comparava a ordem retornada pelo banco com `UUID.compareTo` do Java. Isso é **incompatível**: o PostgreSQL ordena UUIDs byte-a-byte, enquanto o Java compara os dois longs (signed) — o teste era flaky. A paginação em si está correta (o cursor `r.id > :afterId ORDER BY r.id` usa a mesma ordenação do banco). O teste passou a validar **disjunção das páginas** e o total de registros únicos.

### 5.3 Resultado

```
Tests run: 168, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS
```

O 1 skip é o `ApplicationTests`, `@Disabled` por não carregar o `.env` — pré-existente.

## 6. Notas de arquitetura

- **Divisão de responsabilidades da segurança**: o Spring Security de servlet (filtros) protege a API REST; o WebSocket se autentica no **handshake** (interceptor), porque o upgrade HTTP não carrega header `Authorization` de forma confiável e o SockJS já propaga o token pela query.
- **Por que não mover a autenticação para o `SecurityFilter`**: o `SecurityFilter` só processa o header `Authorization`. Colocar leitura de `access_token` nele acoplaria o filtro ao transporte do SockJS. O interceptor de handshake é o ponto correto.
- **Sem alteração de dependências** (a correção usa apenas API do Spring Security 6.5 já presente).
- **Fallback do SockJS**: mesmo com o bug, o SockJS degradava para XHR e o app seguia funcionando — o erro só era visível no log. O fix elimina o fallback, mantendo o transporte nativo WebSocket.
