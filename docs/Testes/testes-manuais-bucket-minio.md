# Testes Manuais — Bucket MinIO (Insomnia / Swagger)

## Pré-requisitos

A aplicação e o MinIO devem estar rodando via Docker Compose:

```bash
docker-compose up -d
```

| Serviço | URL |
|---------|-----|
| API (Backend) | `http://localhost:8080` |
| MinIO API | `http://localhost:9000` |
| MinIO Console (UI) | `http://localhost:9001` |

---

## Passo 0 — Autenticar

Todos os endpoints de `/lunaLink/delivery/**` exigem autenticação. Você precisa de um token JWT:

**POST** `http://localhost:8080/lunaLink/auth/login`

```json
{
  "email": "usuario@email.com",
  "password": "senha"
}
```

Copie o `accessToken` do response. Em cada requisição abaixo, adicione o header:

```
Authorization: Bearer <seu-token>
```

---

## Passo 1 — Gerar Presigned URL de Upload

**POST** `http://localhost:8080/lunaLink/delivery/upload-url?userId={uuid}&fileName=comprovante.jpg`

| Param | Valor |
|-------|-------|
| `userId` | UUID do usuário (ex: `550e8400-e29b-41d4-a716-446655440000`) |
| `fileName` | Nome do arquivo (ex: `comprovante.jpg`) |

**Response 200:**
```json
{
  "uploadUrl": "http://minIO:9000/lunalink/encomendas/550e8400.../a1b2c3-comprovante.jpg?X-Amz-Algorithm=...",
  "key": "encomendas/550e8400.../a1b2c3-comprovante.jpg"
}
```

> **Guarde o `key`** — será usado no Passo 3.
> **Guarde o `uploadUrl`** — será usado no Passo 2.

---

## Passo 2 — Upload Direto ao MinIO

**PUT** `{uploadUrl}` (a URL recebida no passo anterior)

> **Atenção:** Este PUT é direto ao MinIO, **não ao backend**. No Insomnia, crie uma nova requisição PUT apontando para a URL do MinIO.

| Header | Valor |
|--------|-------|
| `Content-Type` | `image/jpeg` (ou o tipo correto do arquivo) |

| Body | Tipo |
|------|------|
| Body raw → Binary | Selecione o arquivo (`.jpg`, `.png`, `.pdf`) |

**Response:** `200 OK` (sem body)

> **Dica no Insomnia:** Na aba Body, selecione "Binary" e escolha o arquivo local.

---

## Passo 3 — Criar Encomenda (com voucherKey)

**POST** `http://localhost:8080/lunaLink/delivery/create`

```json
{
  "user": "550e8400-e29b-41d4-a716-446655440000",
  "protocolNumber": "PROTO-2026-001",
  "discrimination": "Encomenda frágil",
  "voucherKey": "encomendas/550e8400.../a1b2c3-comprovante.jpg",
  "otherRecipient": null
}
```

> O campo `voucherKey` é **obrigatório** — use a `key` do Passo 1.

**Response 200:** Delivery criada com `id`, `status: "PENDING"`, etc.

---

## Passo 4 — Gerar Presigned URL de Download

**GET** `http://localhost:8080/lunaLink/delivery/{id}/download-url`

> `{id}` = UUID da delivery criada no Passo 3.

**Response 200:**
```json
{
  "downloadUrl": "http://minIO:9000/lunalink/encomendas/...?X-Amz-Algorithm=..."
}
```

> A URL expira em **15 minutos**.

---

## Passo 5 — Visualizar/Baixar o Arquivo

Cole o `downloadUrl` no navegador ou faça um **GET** no Insomnia:

**GET** `{downloadUrl}`

> Novamente, é direto ao MinIO, não ao backend.

Deve retornar o arquivo original (imagem/PDF).

---

## Fluxo Resumido no Insomnia

```
1. POST /lunaLink/auth/login           → copia token
2. POST /lunaLink/delivery/upload-url   → copia uploadUrl + key
3. PUT  {uploadUrl}                     → upload binário ao MinIO
4. POST /lunaLink/delivery/create       → envia voucherKey = key
5. GET  /lunaLink/delivery/{id}/download-url → copia downloadUrl
6. GET  {downloadUrl}                   → visualiza arquivo
```

---

## Swagger (UI)

Acesse `http://localhost:8080/swagger-ui.html` para documentação interativa. Os endpoints de delivery estão documentados. O Swagger permite testar os endpoints 1, 4 e 5 diretamente. Para o **Passo 2** (PUT direto ao MinIO), o Swagger **não funciona** — é necessário usar o Insomnia ou curl porque o upload é direto ao MinIO, não ao backend.

---

## MinIO Console (verificação visual)

Acesse `http://localhost:9001` (credenciais: `minioadmin` / `minioadmin`). Na aba **Browse**, você verá a estrutura:

```
lunalink/
└── encomendas/
    └── {userId}/
        └── {uuid}-comprovante.jpg
```

---

## Dicas de Debug

| Problema | Solução |
|----------|---------|
| 401/403 no upload-url | Token JWT ausente ou expirado — refaça login |
| PUT 403 no MinIO | Presigned URL expirada (15 min) — gere uma nova |
| PUT 411 Length Required | Escolha "Binary" no Insomnia (não "Text") |
| `voucherKey` nulo na create | O campo é `@NotNull` — envie a key do Passo 1 |
| Download URL retorna 404 | Verifique se o `id` da delivery está correto |
