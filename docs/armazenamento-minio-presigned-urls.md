# Armazenamento de Arquivos - MinIO (Presigned URLs)

## Visão Geral

O sistema utiliza **MinIO** (compatível com S3) para armazenamento de vouchers/comprovantes de encomendas. Os arquivos **não passam pelo backend** — o frontend faz upload direto ao MinIO usando URLs assinadas (presigned URLs).

```
┌──────────┐    1. POST /upload-url    ┌──────────┐
│          │ ──────────────────────────>│          │
│ Frontend │    { uploadUrl, key }      │ Backend  │
│          │ <──────────────────────────│          │
│          │                            │          │
│          │    2. PUT {uploadUrl}      │          │
│          │ ──────────────────────────>│  MinIO   │
│          │    (upload direto)         │          │
│          │                            │          │
│          │    3. POST /create         │          │
│          │    { voucherKey: key, ...} │          │
│          │ ──────────────────────────>│ Postgres │
│          │                            │          │
│          │    4. GET /download-url    │          │
│          │ ──────────────────────────>│          │
│          │    { downloadUrl }         │          │
│          │ <──────────────────────────│          │
└──────────┘                            └──────────┘
```

## Configuração

### Variáveis de Ambiente (Docker)

```yaml
MINIO_ENDPOINT: http://minIO:9000
MINIO_ACCESS_KEY: minioadmin
MINIO_SECRET_KEY: minioadmin
MINIO_BUCKET: lunalink
```

### application.yml

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: lunalink
  presigned:
    upload-expiration-minutes: 15
    download-expiration-minutes: 15
```

### MinIO Console

- **API:** `http://localhost:9000`
- **Console (UI):** `http://localhost:9001`
- **Credenciais:** `minioadmin` / `minioadmin`

---

## Endpoints da API

### 1. Gerar Presigned URL de Upload

Gera uma URL assinada para upload direto ao MinIO e retorna a key que deve ser usada na criação da encomenda.

```
POST /lunaLink/delivery/upload-url?userId={uuid}&fileName=comprovante.jpg
```

**Parâmetros (query):**
| Parâmetro  | Tipo   | Descrição                    |
|------------|--------|------------------------------|
| `userId`   | UUID   | ID do usuário dono da encomenda |
| `fileName` | String | Nome do arquivo (ex: `comprovante.jpg`) |

**Response 200:**
```json
{
  "uploadUrl": "http://minIO:9000/lunalink/encomendas/550e8400-e29b-41d4-a716-446655440000/a1b2c3d4-comprovante.jpg?X-Amz-Algorithm=...",
  "key": "encomendas/550e8400-e29b-41d4-a716-446655440000/a1b2c3d4-comprovante.jpg"
}
```

> **Importante:** Guarde o valor de `key` — ele será enviado como `voucherKey` na criação da encomenda.

---

### 2. Upload Direto ao MinIO

Após receber a `uploadUrl`, faça um **PUT** direto ao MinIO (sem passar pelo backend):

```javascript
const { uploadUrl, key } = await fetch('/lunaLink/delivery/upload-url?userId=...&fileName=comprovante.jpg')
  .then(res => res.json());

// Upload direto ao MinIO
await fetch(uploadUrl, {
  method: 'PUT',
  headers: {
    'Content-Type': 'image/jpeg'  // ou o tipo correto do arquivo
  },
  body: file  // ArrayBuffer ou Blob do arquivo
});
```

**Content-Type:** O tipo MIME do arquivo deve ser especificado no header `Content-Type` do PUT. Tipos aceitos:
- `image/jpeg`
- `image/png`
- `image/webp`
- `application/pdf`

---

### 3. Criar Encomenda (com voucherKey)

Após o upload, crie a encomenda enviando a `key` recebida no passo 1:

```
POST /lunaLink/delivery/create
```

**Body:**
```json
{
  "user": "550e8400-e29b-41d4-a716-446655440000",
  "protocolNumber": "PROTO-2026-001",
  "discrimination": "Encomenda frágil",
  "voucherKey": "encomendas/550e8400-e29b-41d4-a716-446655440000/a1b2c3d4-comprovante.jpg",
  "otherRecipient": null
}
```

> O campo `voucherKey` é **obrigatório** (`@NotNull`).

---

### 4. Gerar Presigned URL de Leitura (Download)

Para exibir a imagem, gere uma URL de leitura com expiração curta:

```
GET /lunaLink/delivery/{id}/download-url
```

**Response 200:**
```json
{
  "downloadUrl": "http://minIO:9000/lunalink/encomendas/...?X-Amz-Algorithm=..."
}
```

> A URL expira em **15 minutos** por padrão.

---

## Fluxo Completo no Frontend

### Upload de Voucher

```javascript
async function uploadVoucher(userId, file) {
  // 1. Pedir presigned URL ao backend
  const urlResponse = await fetch(
    `/lunaLink/delivery/upload-url?userId=${userId}&fileName=${file.name}`,
    { method: 'POST' }
  );
  const { uploadUrl, key } = await urlResponse.json();

  // 2. Upload direto ao MinIO
  await fetch(uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': file.type },
    body: file
  });

  // 3. Criar encomenda com a key
  const createResponse = await fetch('/lunaLink/delivery/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      user: userId,
      protocolNumber: 'PROTO-001',
      discrimination: 'Descrição',
      voucherKey: key,  // ← a key recebida no passo 1
      otherRecipient: null
    })
  });

  return createResponse.json();
}
```

### Exibir Voucher

```javascript
async function getVoucherDownloadUrl(deliveryId) {
  const response = await fetch(`/lunaLink/delivery/${deliveryId}/download-url`);
  const { downloadUrl } = await response.json();
  return downloadUrl;
}

// Uso:
const url = await getVoucherDownloadUrl('delivery-uuid');
// Renderizar: <img src={url} />
// ou: window.open(url)
```

---

## Estrutura de Keys no MinIO

```
lunalink/                          ← bucket
└── encomendas/                    ← pasta
    ├── {userId}/                  ← pasta por usuário
    │   ├── {uuid}-comprovante.jpg
    │   ├── {uuid}-nota-fiscal.pdf
    │   └── ...
    └── ...
```

**Formato da key:** `encomendas/{userId}/{uuid-random}-{nomeArquivo}`

---

## Entidade Delivery (Referência)

| Campo        | Tipo     | Descrição                                  |
|-------------|----------|--------------------------------------------|
| `id`        | UUID     | PK auto-gerado                             |
| `userId`    | UUID     | ID do proprietário                         |
| `voucherKey`| String   | Key do arquivo no MinIO (NOT NULL)         |
| `status`    | Enum     | `PENDING` ou `DELIVERED`                   |
| ...         | ...      | Outros campos (protocolNumber, etc)        |

> O campo `image byte[]` foi **removido**. O arquivo é armazenado apenas no MinIO; o Postgres mantém apenas a referência (`voucherKey`).

---

## Configurações Importantes

| Config                        | Valor padrão | Descrição                          |
|-------------------------------|--------------|------------------------------------|
| `minio.bucket`               | `lunalink`   | Nome do bucket                     |
| `minio.presigned.upload-expiration-minutes` | `15` | Expiração da URL de upload |
| `minio.presigned.download-expiration-minutes` | `15` | Expiração da URL de leitura |

O bucket é criado **automaticamente** na inicialização da aplicação se não existir.
