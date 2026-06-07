# LunaLink — Frontend Integration Overview

**Backend:** Spring Boot 3.5.0 · Java 23  
**Database:** PostgreSQL 14  
**Base URL:** `http://localhost:8080`  
**API Prefix:** `/lunaLink`

---

## Quick Start

1. All protected endpoints require `Authorization: Bearer <token>` header
2. Obtain the token via `POST /lunaLink/auth/login`
3. Token expires in **2 hours** — handle 401 responses and re-authenticate
4. All dates use `yyyy-MM-dd` format; datetimes use ISO-8601

---

## Documentation Index

| File | Contents |
|------|----------|
| [01-authentication.md](./01-authentication.md) | Login, JWT, token handling, roles |
| [02-api-reference.md](./02-api-reference.md) | All endpoints with request/response shapes |
| [03-data-models.md](./03-data-models.md) | TypeScript-ready types for all entities |
| [04-error-handling.md](./04-error-handling.md) | Error formats, status codes, validation errors |
| [05-realtime.md](./05-realtime.md) | WebSocket (STOMP) and Web Push Notifications |

---

## Tech Stack Recommendations for Frontend

- **HTTP Client:** Axios or Fetch — intercept 401 for token refresh/logout
- **State Management:** Store JWT in memory or `httpOnly` cookie (never `localStorage` in production)
- **WebSocket:** `@stomp/stompjs` + `sockjs-client`
- **Push Notifications:** Web Push API with VAPID public key from backend

---

## Swagger UI

When the backend is running locally:  
`http://localhost:8080/swagger-ui.html`

---

## CORS

The backend allows requests from:
- `http://localhost:8100`
- `capacitor://localhost`
- `http://localhost`

If your dev server runs on a different port, the backend CORS config will need to be updated.
