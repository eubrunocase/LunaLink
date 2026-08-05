# Endurecimento da Autenticação JWT — Plano de Refatoração

> **Status:** Planejado (não implementado)
> **Escopo:** Backend apenas. O client frontend será refatorado futuramente — mudanças de contrato são aceitáveis agora.
> **Ambiente:** Local somente.

## 1. Contexto atual

| Aspecto | Situação |
|---|---|
| Fluxo de login | `POST /lunaLink/auth/login` — credencial → JWT HS256 (2h), devolvido como **string crua** |
| Validação | `SecurityFilter` revalida token e busca usuário no DB a cada request |
| Claims do JWT | `iss`, `sub` (email), `roles`, `exp` — sem `iat`, `jti`, `aud`, `nbf` |
| Revogação/Refresh | Não existem |
| Proteção anti-abuso | Não há rate limit nem lockout |
| WebSocket | Handshake `PUT /ws-lunalink` permitAll, frames STOMP não autenticados |
| CORS | Duplicado: `CorsConfig` (restrito) + `corsConfigurationSource` em `SecurityConfiguration` com `*` |

## 2. Objetivos

1. Responder `401` de forma correta a tokens inválidos/expirados (hoje o filtro engole exceções e segue como anônimo).
2. Adicionar ciclo de vida completo: **refresh token com rotação, logout e revogação**.
3. Invalidar sessões automaticamente na troca de senha/role.
4. Proteger o login contra **brute-force** (rate limit + lockout).
5. Autenticar o **WebSocket**.
6. Unificar e restringir **CORS**.
7. Eliminar exposição da senha em serialização/logs.

## 3. Fase 1 — Correções de segurança

### 3.1 `SecurityFilter.java` — 401 em token inválido
- Substituir o `catch` (linhas 63–66) que engole exceções: se houver token e ele for inválido/expirado → lançar `BadCredentialsException`, convertida em `401` pelo `ExceptionTranslationFilter`.
- Criar bean `AuthenticationEntryPoint` no `SecurityConfiguration` respondendo `401` em JSON (`StandardErrorDTO`).
- Corrigir parsing do header: `startsWithIgnoreCase("Bearer ")` + `substring(7)` (hoje usa `replace("Bearer ", "")`).
- Usuário não encontrado → também `BadCredentialsException`.

### 3.2 `TokenService.java` — exceções + claims completos
- `validateToken` passa a **lançar** `JWTVerificationException` (remover string mágica `"Invalid token"`).
- `generateToken` adiciona: `iat`, `jti` (UUID), `aud` (`lunalink-api`), `nbf`, `token_version`.
- Expiração: `Instant.now().plus(2, ChronoUnit.HOURS)` (remover fuso fixo `-03:00`).

### 3.3 `Users.java` — parar exposição de senha
- Remover `@JsonProperty("password")` e `@Data` (mantendo `@Getter @Setter @EqualsAndHashCode(of="id")`).
- `toString()` sem senha/email.
- Adicionar campo `tokenVersion` (int) para a Fase 2.

### 3.4 Logs de debug
- Remover `System.out`/`System.err` de `SecurityFilter`, `AuthenticationService`, `SecurityConfiguration`; usar `@Slf4j`.

## 4. Fase 2 — Ciclo de vida do token

### 4.1 Refresh token + logout + revogação
- **Armazenamento:** Postgres via JPA (sem Redis — 500 usuários não justifica nova infra).
- **Novas entidades:**
  - `RefreshToken`: id, `tokenHash` (SHA-256, nunca o token cru), `userId`, `expiresAt` (30 dias), `revokedAt`, `replacedBy`.
  - `TokenBlacklist`: revoga `jti` de access tokens até o vencimento.
- **Endpoints novos:**
  - `POST /lunaLink/auth/refresh` — rotação: novo access + novo refresh, revoga o anterior; **reuso de refresh revogado revoga a família** (re-login obrigatório).
  - `POST /lunaLink/auth/logout` — revoga refresh + blacklista `jti` do access atual.
- `SecurityFilter` valida também `aud`, `iat` e `jti` contra a blacklist (1 lookup indexado por request).
- **Contrato de resposta muda** para DTO: `{ accessToken, refreshToken, expiresIn, tokenType }` (hoje string crua).

### 4.2 Invalidação por troca de senha/role
- Claim `token_version` no access token vs. `users.token_version` do DB; mismatch → 401.
- `UserService.updateUser`/`deleteUser` incrementam `tokenVersion`.

## 5. Fase 3 — Defesa anti-abuso

### 5.1 Rate limit no `/login` + lockout
- Dependência: `com.bucket4j:bucket4j-core`.
- Buckets em memória por IP+email → `429 Too Many Requests`.
- Lockout: listener de `AuthenticationFailureBadCredentialsEvent`/`AuthenticationSuccessEvent`; 5 falhas → bloqueio de 10 min (mapear em `isAccountLocked`).
- `LoginFacade`/`AuthenticationService`: `401` (`BadCredentials`) e `429` explícitos (hoje 400 genérico).

### 5.2 WebSocket autenticado
- Dependência: `org.springframework.security:spring-security-messaging`.
- `HandshakeInterceptor` valida JWT via query param `access_token` (SockJS não envia header) e registra o `Principal`.
- `ChannelInterceptor` exige autenticação nos frames STOMP.
- `setAllowedOriginPatterns` passa a vir de propriedade configurável.

### 5.3 CORS + chave
- Remover `corsConfigurationSource` com `*` (SecurityConfiguration) — manter apenas `CorsConfig` restrito.
- Origens finais via propriedade (`application.properties`).
- **Chave:** manter HS256 (1 serviço autônomo); documentar rotação do segredo via env. RS256 fica como opção futura.

## 6. Dependências novas
- `com.bucket4j:bucket4j-core` (8.x)
- `org.springframework.security:spring-security-messaging`

## 7. Migração de banco
- `ddl-auto=update` cria novas tabelas/colunas (`refresh_token`, `token_blacklist`, `users.token_version`).

## 8. Testes

**Atualizar (comportamento mudou):**
- `TokenServiceTest` — `validateToken` lança exceção; asserts de `aud`/`iat`/`jti`.
- `AuthenticationControllerTest` — login devolve DTO.

**Novos:**
- `SecurityFilterTest` (401 em token inválido).
- `RefreshTokenServiceTest` (rotação e reuso).
- Rate limiter / lockout.

## 9. Verificação
- `./mvnw test`
- `curl`: login → refresh → logout → acesso com token revogado = 401; 6º login errado = 429/lockout.
- `docker compose up -d --build application`

## 10. Arquivos afetados

**Modificar:** `SecurityFilter`, `TokenService`, `SecurityConfiguration`, `AuthenticationService`, `Users`, `UserService`, `AuthenticationController`, `LoginFacade`, `CorsConfig`, `application.properties`

**Criar:** entidades `RefreshToken`/`TokenBlacklist` + repositórios/portas, `RefreshTokenService`/`TokenRevocationService`, `LoginRateLimiter`, `AuthenticationEntryPoint`, `HandshakeInterceptor`/`ChannelInterceptor`, DTOs de token (`LoginResponseDTO` expandido, `RefreshRequestDTO`, `RefreshResponseDTO`)
