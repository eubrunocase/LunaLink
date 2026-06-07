# Error Handling

## Standard Error Response

All non-2xx responses follow this shape:

```typescript
interface StandardErrorDTO {
  timestamp: string;    // ISO-8601 datetime
  status: number;       // HTTP status code
  error: string;        // Short description
  message: string;      // Detailed human-readable message
  path: string;         // Request path that caused the error
}
```

**Example:**
```json
{
  "timestamp": "2026-06-07T12:34:56",
  "status": 404,
  "error": "Not Found",
  "message": "Reservation not found",
  "path": "/lunaLink/reservation/abc123"
}
```

---

## Validation Error Response

When request body fields fail `@Valid` validation, the response includes per-field errors:

```typescript
interface ValidationErrorDTO {
  timestamp: string;
  status: 400;
  error: string;        // "Bad Request - Falha na Validação"
  message: string;      // "Os dados enviados são inválidos. Verifique os campos."
  validationErrors: Record<string, string>;  // fieldName -> error message
  path: string;
}
```

**Example:**
```json
{
  "timestamp": "2026-06-07T12:34:56",
  "status": 400,
  "error": "Bad Request - Falha na Validação",
  "message": "Os dados enviados são inválidos. Verifique os campos.",
  "validationErrors": {
    "description": "must not be blank",
    "incidentDate": "must not be null"
  },
  "path": "/lunaLink/occurrences"
}
```

---

## HTTP Status Codes

| Status | Meaning | When it happens |
|--------|---------|-----------------|
| `200` | OK | Successful GET/PUT/PATCH |
| `201` | Created | Successful POST (new resource created) |
| `204` | No Content | Successful DELETE |
| `400` | Bad Request | Validation errors, illegal arguments |
| `401` | Unauthorized | Missing or expired JWT token |
| `403` | Forbidden | Valid token but insufficient role |
| `404` | Not Found | Resource doesn't exist |
| `409` | Conflict | Business rule violation (e.g. duplicate reservation) |
| `500` | Internal Server Error | Unexpected server-side failure |

---

## Common Business Rule Errors (409 Conflict)

These are thrown as `IllegalStateException` and return `status: 409`:

- Trying to create a reservation that already exists for that user/date/space combination
- Approving/rejecting an already-processed reservation
- Equipment already reserved for the requested time slot

---

## Frontend Error Handling Strategy

```typescript
async function apiCall<T>(request: () => Promise<T>): Promise<T> {
  try {
    return await request();
  } catch (error) {
    if (!error.response) {
      throw new Error('Sem conexão com o servidor');
    }

    const { status, data } = error.response;

    switch (status) {
      case 400:
        if (data.validationErrors) {
          // Show per-field errors in form
          handleValidationErrors(data.validationErrors);
        } else {
          showToast(data.message || 'Dados inválidos');
        }
        break;

      case 401:
        clearToken();
        router.push('/login');
        break;

      case 403:
        showToast('Você não tem permissão para esta ação');
        break;

      case 404:
        showToast('Recurso não encontrado');
        break;

      case 409:
        showToast(data.message || 'Conflito: operação não permitida');
        break;

      default:
        showToast('Erro interno. Tente novamente.');
    }

    throw error;
  }
}
```

---

## Validation Rules Reference

### User
- `email`: required, valid email format, must be unique
- `password`: required
- `name`: required
- `role`: required, must be a valid `UserRoles` value

### Reservation
- `date`: required, `yyyy-MM-dd`
- `spaceId`: required

### Delivery
- `userId`: required (UUID)

### Equipment Reservation
- `equipmentId`: required
- `date`: required
- `startTime`: required (`HH:mm:ss`)
- `endTime`: required (`HH:mm:ss`), must be after `startTime`

### Occurrence
- `description`: required, not blank
- `incidentDate`: required, ISO-8601 datetime
