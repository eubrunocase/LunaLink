#  LunaLink — Documentação da API

> **API REST** para gerenciamento de espaços, residentes e reservas em condomínios.  
> Stack: Java 24 · Spring Boot · Spring Security · JWT · PostgreSQL

---

## Sumário

- [Visão Geral](#visão-geral)
- [Autenticação](#autenticação)
- [Perfis de Acesso (Roles)](#perfis-de-acesso-roles)
- [Enumerações (Enums)](#enumerações-enums)
- [Endpoints](#endpoints)
  - [Auth](#auth)
  - [Usuários](#usuários)
  - [Reservas de Espaços](#reservas-de-espaços)
  - [Espaços](#espaços)
  - [Disponibilidade](#disponibilidade)
  - [Entregas (Delivery)](#entregas-delivery)
  - [Equipamentos](#equipamentos)
  - [Notificações Push (Web Push)](#notificações-push-web-push)
- [Modelos de Dados](#modelos-de-dados)
- [Erros Padrão](#erros-padrão)
- [Configuração e Execução](#configuração-e-execução)

---

## Visão Geral

| Item | Valor |
|---|---|
| Base URL | `http://localhost:8080` |
| Formato | JSON |
| Autenticação | Bearer Token (JWT) |
| Porta padrão | `8080` |
| Documentação interativa | `/swagger-ui.html` · `/v3/api-docs` |

---

## Autenticação

A API utiliza **autenticação stateless com JWT**. O fluxo é:

1. Faça `POST /lunaLink/auth/login` com e-mail e senha.
2. Receba o token JWT no corpo da resposta (string plana).
3. Inclua o token no header `Authorization` de todas as requisições protegidas.

```http
Authorization: Bearer <token_jwt>
```

> ⚠️ Tokens não expiram automaticamente nesta implementação — a expiração depende da configuração de `api.security.token.secret`.

---

## Perfis de Acesso (Roles)

| Role | Descrição |
|---|---|
| `ADMIN_ROLE` | Acesso total — gerencia usuários, aprova/rejeita reservas, administra entregas e equipamentos |
| `RESIDENT_ROLE` | Morador — pode criar reservas, consultar entregas e reservar equipamentos |
| `EMPLOYEE` | Funcionário — acesso operacional conforme regras de negócio |

---

## Enumerações (Enums)

### `ReservationStatus`
| Valor | Descrição |
|---|---|
| `PENDING` | Aguardando aprovação |
| `APPROVED` | Aprovada pelo administrador |
| `REJECTED` | Rejeitada pelo administrador |
| `CANCELLED` | Cancelada pelo morador |

### `DeliveryStatus`
| Valor | Descrição |
|---|---|
| `PENDING` | Encomenda aguardando retirada |
| `DELIVERED` | Encomenda retirada |

### `EquipmentReservationStatus`
| Valor | Descrição |
|---|---|
| `CONFIRMED` | Reserva confirmada |
| `IN_USE` | Equipamento em uso |
| `RETURNED` | Equipamento devolvido |
| `CANCELED` | Reserva cancelada |

### `SpaceType`
| Valor | Descrição |
|---|---|
| `SALAO_FESTAS` | Salão de Festas |
| `CHURRASQUEIRA` | Churrasqueira |
| `ACADEMIA` | Academia |
| `CAMPO_FUTEBOL` | Campo de Futebol |

---

## Endpoints

---

### Auth

#### `POST /lunaLink/auth/login`

Realiza o login e retorna o token JWT.

**Acesso:** Público

**Request Body:**
```json
{
  "email": "morador@email.com",
  "password": "senha123"
}
```

**Resposta de sucesso `200 OK`:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
> A resposta é uma string simples com o token JWT, sem envoltório JSON.

**Respostas de erro:**

| Código | Descrição |
|---|---|
| `400 Bad Request` | Credenciais inválidas ou body malformado |

---

### Usuários

Base path: `/lunaLink/users`

#### `GET /lunaLink/users`

Lista todos os usuários cadastrados.

**Acesso:** Autenticado

**Resposta `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "João Silva",
    "apartment": "101",
    "email": "joao@email.com",
    "role": "RESIDENT_ROLE",
    "reservation": [
      {
        "id": "a1b2c3d4-...",
        "date": "2026-04-15",
        "spaceType": "SALAO_FESTAS"
      }
    ]
  }
]
```

---

#### `GET /lunaLink/users/{id}`

Busca um usuário pelo ID.

**Acesso:** Autenticado

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID do usuário |

**Resposta `200 OK`:** Mesmo schema de `GET /lunaLink/users`.

---

#### `GET /lunaLink/users/summary`

Retorna uma lista resumida de usuários (nome + identificação básica).

**Acesso:** Autenticado

**Resposta `200 OK`:**
```json
[
  {
    "id": "550e8400-...",
    "name": "João Silva",
    "apartment": "101"
  }
]
```

---

#### `POST /lunaLink/users/create`

Cria um novo usuário.

**Acesso:** `ADMIN_ROLE`

**Request Body:**
```json
{
  "name": "Maria Santos",
  "apartment": "202",
  "email": "maria@email.com",
  "password": "senhaSegura123",
  "role": "RESIDENT_ROLE"
}
```

**Resposta `200 OK`:** Schema completo de `ResponseUserDTO`.

---

#### `PUT /lunaLink/users/update/{id}`

Atualiza os dados de um usuário.

**Acesso:** `ADMIN_ROLE`

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID do usuário |

**Request Body:** Mesmo schema do `POST /create`.

**Resposta `200 OK`:** Schema completo de `ResponseUserDTO`.

---

#### `DELETE /lunaLink/users/delete/{id}`

Remove um usuário.

**Acesso:** `ADMIN_ROLE`

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID do usuário |

**Resposta:** `204 No Content`

---

### Reservas de Espaços

Base path: `/lunaLink/reservation`

#### `POST /lunaLink/reservation`

Cria uma nova reserva para o usuário autenticado.

**Acesso:** Autenticado

**Request Body:**
```json
{
  "date": "2026-05-10",
  "space": 1
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `date` | `LocalDate` (`yyyy-MM-dd`) | ✅ | Data da reserva |
| `space` | `Long` | ✅ | ID do espaço |

**Resposta `201 Created`:**
```json
{
  "id": "a1b2c3d4-e5f6-...",
  "date": "2026-05-10",
  "user": {
    "id": "550e8400-...",
    "name": "João Silva",
    "email": "joao@email.com"
  },
  "space": {
    "id": 1,
    "type": "SALAO_FESTAS"
  },
  "status": "PENDING",
  "createdAt": "2026-03-19T10:30:00",
  "canceledAt": null
}
```

---

#### `GET /lunaLink/reservation`

Lista todas as reservas.

**Acesso:** Autenticado

**Resposta `200 OK`:** Array de `ReservationResponseDTO`.

---

#### `GET /lunaLink/reservation/{id}`

Busca uma reserva pelo ID.

**Acesso:** Autenticado

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID da reserva |

**Resposta `200 OK`:** `ReservationResponseDTO`.

---

#### `PUT /lunaLink/reservation/{id}`

Atualiza uma reserva existente.

**Acesso:** Autenticado

**Request Body:**
```json
{
  "date": "2026-06-01",
  "spaceId": 2
}
```

**Resposta `200 OK`:** `ReservationResponseDTO` atualizado.

---

#### `DELETE /lunaLink/reservation/{id}`

Remove uma reserva.

**Acesso:** `ADMIN_ROLE`

**Resposta:** `204 No Content`

---

#### `PUT /lunaLink/reservation/{id}/approve`

Aprova uma reserva pendente.

**Acesso:** `ADMIN_ROLE`

**Resposta `200 OK`:** `ReservationResponseDTO` com `status: "APPROVED"`.

---

#### `PUT /lunaLink/reservation/{id}/reject`

Rejeita uma reserva pendente.

**Acesso:** `ADMIN_ROLE`

**Resposta `200 OK`:** `ReservationResponseDTO` com `status: "REJECTED"`.

---

#### `GET /lunaLink/reservation/checkAvaliability/{date}/{spaceId}`

Verifica se o usuário autenticado pode fazer uma reserva na data e espaço informados.

**Acesso:** Autenticado

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `date` | `LocalDate` (`yyyy-MM-dd`) | Data a verificar |
| `spaceId` | `Long` | ID do espaço |

**Resposta `200 OK`:**
```json
true
```

---

#### `GET /lunaLink/reservation/report/monthly`

Gera relatório mensal de reservas.

**Acesso:** `ADMIN_ROLE`

**Query Parameters:**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `month` | `int` | ✅ | Mês (1–12) |
| `year` | `int` | ✅ | Ano (ex: 2026) |

**Resposta `200 OK`:**
```json
[
  {
    "month": 5,
    "year": 2026,
    "totalReservations": 12,
    "approvedReservations": 9,
    "rejectedReservations": 2,
    "pendingReservations": 1
  }
]
```

---

### Espaços

Base path: `/lunaLink/space`

#### `GET /lunaLink/space`

Lista todos os espaços disponíveis no condomínio.

**Acesso:** Autenticado

**Resposta `200 OK`:**
```json
[
  {
    "id": 1,
    "type": "SALAO_FESTAS",
    "description": "Salão de Festas Principal"
  },
  {
    "id": 2,
    "type": "CHURRASQUEIRA"
  }
]
```

---

### Disponibilidade

Base path: `/lunaLink/availabilitySpaces/{spaceId}/availability`

**Acesso:** Autenticado (todos os endpoints desta seção)

#### `GET /lunaLink/availabilitySpaces/{spaceId}/availability/status`

Verifica a disponibilidade de um espaço em uma data específica.

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `spaceId` | `Long` | ID do espaço |

**Query Parameters:**

| Parâmetro | Tipo | Obrigatório | Formato |
|---|---|---|---|
| `date` | `LocalDate` | ✅ | `yyyy-MM-dd` |

**Resposta `200 OK`:**
```json
{
  "spaceId": 1,
  "date": "2026-05-10",
  "available": true
}
```

---

#### `GET /lunaLink/availabilitySpaces/{spaceId}/availability/month/{year}/{month}`

Retorna disponibilidade completa de um espaço em um mês.

**Path Parameters:**

| Parâmetro | Tipo | Validação | Descrição |
|---|---|---|---|
| `spaceId` | `Long` | — | ID do espaço |
| `year` | `int` | mínimo: 2020 | Ano |
| `month` | `int` | 1–12 | Mês |

**Resposta `200 OK`:**
```json
{
  "spaceId": 1,
  "year": 2026,
  "month": 5,
  "unavailableDates": ["2026-05-05", "2026-05-10"],
  "availableDates": ["2026-05-01", "2026-05-02", "..."],
  "totalDays": 31,
  "unavailableCount": 2,
  "availableCount": 29,
  "occupancyPercentage": 6.45
}
```

---

#### `GET /lunaLink/availabilitySpaces/{spaceId}/availability/stats/{year}/{month}`

Retorna estatísticas de ocupação de um espaço em determinado mês.

**Path Parameters:** Mesmos de `/month/{year}/{month}`.

**Resposta `200 OK`:**
```json
{
  "spaceId": 1,
  "year": 2026,
  "month": 5,
  "totalDays": 31,
  "unavailableDays": 10,
  "availableDays": 21,
  "occupancyPercentage": 32.26
}
```

---

#### `POST /lunaLink/availabilitySpaces/{spaceId}/availability/period`

Verifica disponibilidade de um espaço em um período customizado.

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `spaceId` | `Long` | ID do espaço |

**Request Body:**
```json
{
  "startDate": "2026-05-01",
  "endDate": "2026-05-31"
}
```

**Resposta `200 OK`:**
```json
{
  "spaceId": 1,
  "startDate": "2026-05-01",
  "endDate": "2026-05-31",
  "totalDaysInPeriod": 31,
  "unavailableDates": ["2026-05-05", "2026-05-10"],
  "availableDates": ["2026-05-01", "..."],
  "fullyAvailable": false,
  "fullyBooked": false
}
```

---

### Entregas (Delivery)

Base path: `/lunaLink/delivery`

**Acesso:** Autenticado (todos os endpoints desta seção)

#### `GET /lunaLink/delivery/findAll`

Lista todas as entregas registradas.

**Resposta `200 OK`:**
```json
[
  {
    "id": "uuid-...",
    "user": "uuid-do-morador",
    "protocolNumber": "PKG-2026-001",
    "discrimination": "Caixa Amazon",
    "image": null,
    "createdAt": "2026-03-19T09:00:00",
    "createdBy": "porteiro@email.com",
    "otherRecipient": null,
    "status": "PENDING",
    "deliveredAt": null,
    "pickedUpBy": null
  }
]
```

---

#### `GET /lunaLink/delivery/find/{id}`

Busca uma entrega pelo ID.

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID da entrega |

**Resposta `200 OK`:** Schema de `ResponseDeliveryDTO`.

---

#### `POST /lunaLink/delivery/create`

Registra uma nova entrega recebida na portaria.

**Request Body:**
```json
{
  "user": "uuid-do-morador",
  "protocolNumber": "PKG-2026-002",
  "discrimination": "Envelope Correios",
  "image": null,
  "otherRecipient": null
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `user` | UUID | ✅ | ID do morador destinatário |
| `protocolNumber` | String | — | Número de protocolo ou rastreio |
| `discrimination` | String | — | Descrição do pacote |
| `image` | byte[] | — | Foto da encomenda (Base64) |
| `otherRecipient` | String | — | Nome de outro destinatário (se diferente do morador) |

**Resposta `200 OK`:** Schema de `ResponseDeliveryDTO`.

---

#### `PUT /lunaLink/delivery/update/{id}`

Atualiza os dados de uma entrega.

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID da entrega |

**Request Body:** Mesmo schema do `POST /create`.

**Resposta `200 OK`:** Schema de `ResponseDeliveryDTO` atualizado.

---

#### `PUT /lunaLink/delivery/{id}/confirm-receipt`

Confirma a retirada de uma entrega pelo morador.

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID da entrega |

**Query Parameters:**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `pickedUpBy` | String | — | Nome de quem retirou (se diferente do titular) |

**Resposta `200 OK`:** `ResponseDeliveryDTO` com `status: "DELIVERED"` e `deliveredAt` preenchido.

---

### Equipamentos

Base path: `/lunaLink/equipment-reservation`

#### `POST /lunaLink/equipment-reservation`

Cria uma reserva de equipamento para o usuário autenticado.

**Acesso:** Autenticado

**Request Body:**
```json
{
  "equipmentId": 1,
  "date": "2026-05-15",
  "startTime": "08:00:00",
  "endTime": "10:00:00"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `equipmentId` | Long | ✅ | ID do equipamento |
| `date` | `LocalDate` | ✅ | Data da reserva |
| `startTime` | `LocalTime` | ✅ | Horário de início (`HH:mm:ss`) |
| `endTime` | `LocalTime` | ✅ | Horário de término (`HH:mm:ss`) |

**Resposta `201 Created`:**
```json
{
  "id": "uuid-...",
  "equipmentName": "Projetor",
  "userName": "João Silva",
  "userApartment": "101",
  "date": "2026-05-15",
  "startTime": "08:00:00",
  "endTime": "10:00:00",
  "status": "CONFIRMED",
  "createdAt": "2026-03-19T10:00:00",
  "pickedUpAt": null,
  "returnedAt": null
}
```

---

#### `GET /lunaLink/equipment-reservation`

Lista reservas de equipamentos com filtros opcionais.

**Acesso:** `ADMIN_ROLE`

**Query Parameters:**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `date` | `LocalDate` | — | Filtrar por data |
| `status` | `EquipmentReservationStatus` | — | Filtrar por status |

**Resposta `200 OK`:** Array de `EquipmentReservationResponseDTO`.

---

#### `PATCH /lunaLink/equipment-reservation/{id}/handover`

Registra a entrega (check-in) do equipamento ao usuário.

**Acesso:** `ADMIN_ROLE`

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID da reserva |

**Resposta `200 OK`:** `EquipmentReservationResponseDTO` com `status: "IN_USE"` e `pickedUpAt` preenchido.

---

#### `PATCH /lunaLink/equipment-reservation/{id}/return`

Registra a devolução (check-out) do equipamento.

**Acesso:** `ADMIN_ROLE`

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID da reserva |

**Resposta `200 OK`:** `EquipmentReservationResponseDTO` com `status: "RETURNED"` e `returnedAt` preenchido.

---

### Notificações Push (Web Push)

Base path: `/lunaLink/push`

#### `GET /lunaLink/push/public-key`

Retorna a chave pública VAPID para configuração de Web Push no cliente.

**Acesso:** Público

**Resposta `200 OK`:**
```json
{
  "publicKey": "BNV..."
}
```

---

#### `POST /lunaLink/push/subscribe`

Registra a assinatura de notificações push do usuário autenticado.

**Acesso:** Autenticado

**Request Body:**
```json
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/...",
  "keys": {
    "p256dh": "...",
    "auth": "..."
  }
}
```

**Resposta `200 OK`:** Sem corpo.

---

#### `POST /lunaLink/push/unsubscribe`

Remove a assinatura de notificações push.

**Acesso:** Autenticado

**Request Body:**
```json
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/..."
}
```

**Resposta `200 OK`:** Sem corpo.

---

## Modelos de Dados

### `ResponseUserDTO`
| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador único |
| `name` | String | Nome completo |
| `apartment` | String | Número do apartamento |
| `email` | String | E-mail do usuário |
| `role` | UserRoles | Perfil de acesso |
| `reservation` | Array | Lista resumida de reservas |

### `ReservationResponseDTO`
| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador único |
| `date` | LocalDate | Data da reserva |
| `user` | UserSummaryDTO | Dados básicos do usuário |
| `space` | SpaceSummaryDTO | Dados básicos do espaço |
| `status` | ReservationStatus | Status atual |
| `createdAt` | LocalDateTime | Data/hora de criação |
| `canceledAt` | LocalDateTime | Data/hora de cancelamento (null se não cancelado) |

### `ResponseDeliveryDTO`
| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador único |
| `user` | UUID | ID do morador |
| `protocolNumber` | String | Número de protocolo |
| `discrimination` | String | Descrição da encomenda |
| `image` | byte[] | Foto da encomenda |
| `createdAt` | LocalDateTime | Data/hora de registro |
| `createdBy` | String | Quem registrou (porteiro) |
| `otherRecipient` | String | Outro destinatário |
| `status` | DeliveryStatus | Status da entrega |
| `deliveredAt` | LocalDateTime | Data/hora de retirada |
| `pickedUpBy` | String | Quem retirou |

### `EquipmentReservationResponseDTO`
| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador único |
| `equipmentName` | String | Nome do equipamento |
| `userName` | String | Nome do usuário |
| `userApartment` | String | Apartamento do usuário |
| `date` | LocalDate | Data da reserva |
| `startTime` | LocalTime | Horário de início |
| `endTime` | LocalTime | Horário de término |
| `status` | EquipmentReservationStatus | Status atual |
| `createdAt` | LocalDateTime | Data de criação |
| `pickedUpAt` | LocalDateTime | Data de retirada |
| `returnedAt` | LocalDateTime | Data de devolução |

---

## Erros Padrão

| Código HTTP | Descrição |
|---|---|
| `400 Bad Request` | Parâmetros inválidos ou body malformado |
| `401 Unauthorized` | Token ausente ou inválido |
| `403 Forbidden` | Sem permissão para o recurso |
| `404 Not Found` | Recurso não encontrado |
| `500 Internal Server Error` | Erro interno da aplicação |

**Schema de erro padrão:**
```json
{
  "timestamp": 1742400000000,
  "status": 400,
  "error": "Bad Request",
  "message": "Data é obrigatória",
  "path": "/lunaLink/reservation"
}
```

---

## Configuração e Execução

### Variáveis de Ambiente

| Variável | Descrição |
|---|---|
| `DB_URL` | URL de conexão com o PostgreSQL (ex: `jdbc:postgresql://localhost:5432/lunalink`) |
| `DB_USERNAME` | Usuário do banco de dados |
| `DB_PASSWORD` | Senha do banco de dados |
| `api.security.token.secret` | Chave secreta para assinatura do JWT |
| `VAPID_PUBLIC_KEY` | Chave pública VAPID para Web Push |
| `VAPID_PRIVATE_KEY` | Chave privada VAPID para Web Push |
| `VAPID_SUBJECT` | Subject VAPID (ex: `mailto:admin@lunalink.com`) |

### Executando com Maven

```bash
# Build
mvn clean package

# Execução
java -jar target/application.jar

# Ou diretamente
mvn spring-boot:run
```

### Executando com Docker

```bash
docker-compose up --build
```

### Executando Testes

```bash
mvn test
```

---

## Tabela Resumo de Endpoints

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/lunaLink/auth/login` | Público | Login e geração de token JWT |
| `GET` | `/lunaLink/users` | Autenticado | Listar todos os usuários |
| `GET` | `/lunaLink/users/{id}` | Autenticado | Buscar usuário por ID |
| `GET` | `/lunaLink/users/summary` | Autenticado | Resumo de usuários |
| `POST` | `/lunaLink/users/create` | ADMIN | Criar usuário |
| `PUT` | `/lunaLink/users/update/{id}` | ADMIN | Atualizar usuário |
| `DELETE` | `/lunaLink/users/delete/{id}` | ADMIN | Remover usuário |
| `POST` | `/lunaLink/reservation` | Autenticado | Criar reserva |
| `GET` | `/lunaLink/reservation` | Autenticado | Listar reservas |
| `GET` | `/lunaLink/reservation/{id}` | Autenticado | Buscar reserva por ID |
| `PUT` | `/lunaLink/reservation/{id}` | Autenticado | Atualizar reserva |
| `DELETE` | `/lunaLink/reservation/{id}` | ADMIN | Remover reserva |
| `PUT` | `/lunaLink/reservation/{id}/approve` | ADMIN | Aprovar reserva |
| `PUT` | `/lunaLink/reservation/{id}/reject` | ADMIN | Rejeitar reserva |
| `GET` | `/lunaLink/reservation/checkAvaliability/{date}/{spaceId}` | Autenticado | Verificar disponibilidade |
| `GET` | `/lunaLink/reservation/report/monthly` | ADMIN | Relatório mensal |
| `GET` | `/lunaLink/space` | Autenticado | Listar espaços |
| `GET` | `/lunaLink/availabilitySpaces/{spaceId}/availability/status` | Autenticado | Status de disponibilidade |
| `GET` | `/lunaLink/availabilitySpaces/{spaceId}/availability/month/{year}/{month}` | Autenticado | Disponibilidade mensal |
| `GET` | `/lunaLink/availabilitySpaces/{spaceId}/availability/stats/{year}/{month}` | Autenticado | Estatísticas de ocupação |
| `POST` | `/lunaLink/availabilitySpaces/{spaceId}/availability/period` | Autenticado | Disponibilidade por período |
| `GET` | `/lunaLink/delivery/findAll` | Autenticado | Listar entregas |
| `GET` | `/lunaLink/delivery/find/{id}` | Autenticado | Buscar entrega por ID |
| `POST` | `/lunaLink/delivery/create` | Autenticado | Registrar entrega |
| `PUT` | `/lunaLink/delivery/update/{id}` | Autenticado | Atualizar entrega |
| `PUT` | `/lunaLink/delivery/{id}/confirm-receipt` | Autenticado | Confirmar retirada |
| `POST` | `/lunaLink/equipment-reservation` | Autenticado | Criar reserva de equipamento |
| `GET` | `/lunaLink/equipment-reservation` | ADMIN | Listar reservas de equipamentos |
| `PATCH` | `/lunaLink/equipment-reservation/{id}/handover` | ADMIN | Entregar equipamento |
| `PATCH` | `/lunaLink/equipment-reservation/{id}/return` | ADMIN | Receber devolução |
| `GET` | `/lunaLink/push/public-key` | Público | Obter chave VAPID pública |
| `POST` | `/lunaLink/push/subscribe` | Autenticado | Assinar notificações push |
| `POST` | `/lunaLink/push/unsubscribe` | Autenticado | Cancelar notificações push |

---

*Documentação gerada a partir do código-fonte do repositório LunaLink — Março 2026.*
