# Authentication

## Login

```
POST /lunaLink/auth/login
Content-Type: application/json
(No Authorization header required)
```

**Request Body:**
```json
{
  "email": "user@email.com",
  "password": "password123"
}
```

**Response:** Plain text JWT token (not JSON-wrapped)
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**On failure:** HTTP 400 with standard error body

---

## Using the Token

Include the token in every protected request:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token lifetime:** 2 hours from generation.  
When you receive HTTP 401, the token is expired or invalid — redirect to login.

---

## JWT Payload

The token encodes:
- **sub** (subject): user's email
- **roles**: list of user roles (e.g., `["ADMIN_ROLE"]`)
- **exp**: expiration timestamp

You can decode the payload client-side (base64) to read the role without an extra API call.

---

## User Roles

| Role | Description |
|------|-------------|
| `ADMIN_ROLE` | Full access — manages users, approves/rejects reservations, operates equipment handover |
| `RESIDENT_ROLE` | Standard resident — creates reservations, deliveries, occurrences |
| `EMPLOYEE` | Operational staff — equipment management |

Use the role to conditionally show admin UI (approve/reject buttons, user management, etc.).

---

## Get Authenticated User Info

```
GET /lunaLink/users/me
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": "uuid",
  "name": "Bruno Casé",
  "apartment": "101",
  "email": "bruno@email.com",
  "role": "RESIDENT_ROLE",
  "reservation": []
}
```

Call this once after login to populate the user session in the frontend.

---

## Recommended Token Storage

| Storage | Pros | Cons |
|---------|------|------|
| Memory (variable) | XSS-safe, no persistence | Lost on page refresh |
| `sessionStorage` | Survives navigation, cleared on tab close | Vulnerable to XSS |
| `httpOnly` cookie | Most secure | Requires backend cookie config |

**Avoid `localStorage`** for tokens in security-sensitive apps.

---

## Suggested Axios Interceptor

```typescript
// Request interceptor — attach token
axios.interceptors.request.use((config) => {
  const token = getToken(); // your storage strategy
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor — handle token expiry
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearToken();
      router.push('/login');
    }
    return Promise.reject(error);
  }
);
```
