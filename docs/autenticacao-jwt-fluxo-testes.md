# Autenticação JWT — Guia de Teste no Insomnia

> Aplicável após o endurecimento da autenticação JWT (ciclo de vida completo de token: login, refresh com rotação, logout e revogação).

## 1. Visão geral

| Item | Valor |
|---|---|
| Access token | JWT HS256, expira em **2h** |
| Refresh token | Opaco (hash SHA-256 no banco), dura **30 dias** |
| Response do login/refresh | `{ accessToken, refreshToken, expiresIn, tokenType }` |
| Erros | `400` validação, `401` credenciais/token inválidos, `429` rate limit/lockout |

## 2. Login

`POST /lunaLink/auth/login`

Body:
```json
{
  "email": "usuario@email.com",
  "password": "senha"
}
```

Resposta 200:
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "AbCdEf...",
  "expiresIn": 7200,
  "tokenType": "Bearer"
}
```

## 3. Requisições autenticadas

Em cada request, adicione o header:

```
Authorization: Bearer <accessToken>
```

Ex.: `GET /lunaLink/users`

## 4. Refresh (rotação)

`POST /lunaLink/auth/refresh`

Body:
```json
{
  "refreshToken": "<refreshToken do login>"
}
```

Resposta 200: novo par `{ accessToken, refreshToken, expiresIn, tokenType }`.

**Importante:** o refresh anterior é revogado a cada rotação. Reutilizar um refresh já revogado faz o backend **revogar a família inteira** de tokens daquele usuário (re-login obrigatório). Sempre use o `refreshToken` recém-gerado.

## 5. Logout

`POST /lunaLink/auth/logout`

Header: `Authorization: Bearer <accessToken atual>`

Body:
```json
{
  "refreshToken": "<refreshToken atual>"
}
```

Revoga o refresh token e coloca o `jti` do access token na blacklist. A partir daí, usar o mesmo access token retorna `401`.

## 6. Fluxo end-to-end

1. **Login** → copiar `accessToken` e `refreshToken`
2. **Testar endpoint autenticado** com `Authorization: Bearer <accessToken>` → 200
3. **Refresh** com o `refreshToken` → novo par de tokens
4. **Testar de novo** com o novo `accessToken` → 200 (o token antigo foi revogado)
5. **Logout** com o `accessToken` atual + `refreshToken` atual → 200
6. **Reusar o access token do logout** → `401`

## 7. Erros esperados

| Cenário | Status |
|---|---|
| Credenciais inválidas | `401` |
| Access token expirado/inválido/assinatura errada | `401` |
| Access token revogado (blacklist) | `401` |
| Refresh token inválido/expirado/reutilizado | `401` |
| Excesso de tentativas de login (rate limit) | `429` |
| Conta bloqueada por lockout (5 falhas seguidas) | `429` |

## 8. Avisos

- O login aceita ~10 tentativas por minuto por IP+email (bucket4j).
- **5 senhas erradas → bloqueio de 10 min** para aquele email. O lockout e os buckets são **em memória** — reiniciar a API zera o estado.
- `refresh` e `logout` não exigem access token válido no header (o `SecurityFilter` ignora esses paths), então o refresh funciona mesmo com o access expirado. Para o logout, porém, o access deve estar no header para que o `jti` seja blacklistado.
