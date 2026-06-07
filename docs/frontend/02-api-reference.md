# API Reference

**Base URL:** `http://localhost:8080`  
All protected endpoints require `Authorization: Bearer <token>`.

---

## Users

### GET /lunaLink/users
List all users.  
**Auth:** Any authenticated user  
**Response:** `ResponseUserDTO[]`

### GET /lunaLink/users/{id}
Get user by ID.  
**Auth:** Any authenticated user  
**Params:** `id` (UUID)  
**Response:** `ResponseUserDTO`

### GET /lunaLink/users/me
Get the currently authenticated user.  
**Auth:** Any authenticated user  
**Response:** `ResponseUserDTO`

### GET /lunaLink/users/summary
Lightweight list for dropdowns/selects.  
**Auth:** Any authenticated user  
**Response:** `UserSummaryDTO[]`

### POST /lunaLink/users/create
Create a new user.  
**Auth:** `ADMIN_ROLE`  
**Body:** `RequestUserDTO`  
**Response:** `ResponseUserDTO` (201)

### PUT /lunaLink/users/update/{id}
Update a user.  
**Auth:** `ADMIN_ROLE`  
**Params:** `id` (UUID)  
**Body:** `RequestUserDTO`  
**Response:** `ResponseUserDTO`

### DELETE /lunaLink/users/delete/{id}
Delete a user.  
**Auth:** `ADMIN_ROLE`  
**Params:** `id` (UUID)  
**Response:** 204 No Content

---

## Reservations (Spaces)

### POST /lunaLink/reservation
Create a reservation. The authenticated user is automatically set as the owner.  
**Auth:** Any authenticated user  
**Body:**
```json
{
  "date": "2026-06-15",
  "spaceId": 1
}
```
**Response:** `ReservationResponseDTO` with `status: "PENDING"`

### GET /lunaLink/reservation
List ALL reservations (system-wide).  
**Auth:** `ADMIN_ROLE`  
**Response:** `ReservationResponseDTO[]`

### GET /lunaLink/reservation/{id}
Get a single reservation.  
**Auth:** Any authenticated user  
**Response:** `ReservationResponseDTO`

### GET /lunaLink/reservation/findByUser/{id}
Get all reservations for a specific user.  
**Auth:** Any authenticated user  
**Params:** `id` (UUID — user ID)  
**Response:** `ReservationResponseDTO[]`

### PUT /lunaLink/reservation/{id}
Update a reservation.  
**Auth:** Any authenticated user  
**Body:** `{ userId, date, spaceId }`  
**Response:** `ReservationResponseDTO`

### DELETE /lunaLink/reservation/{id}
Cancel a reservation (sets status to `CANCELLED`).  
**Auth:** `ADMIN_ROLE`  
**Response:** 204 No Content

### GET /lunaLink/reservation/checkAvaliability/{date}/{spaceId}
Check if a space is available on a specific date for the authenticated user.  
**Auth:** Any authenticated user  
**Params:** `date` (yyyy-MM-dd), `spaceId` (Long)  
**Response:** `boolean`

### PUT /lunaLink/reservation/{id}/approve
Approve a pending reservation.  
**Auth:** `ADMIN_ROLE`  
**Response:** `ReservationResponseDTO` with `status: "APPROVED"`

### PUT /lunaLink/reservation/{id}/reject
Reject a pending reservation.  
**Auth:** `ADMIN_ROLE`  
**Response:** `ReservationResponseDTO` with `status: "REJECTED"`

### GET /lunaLink/reservation/report/monthly
Monthly reservation report.  
**Auth:** `ADMIN_ROLE`  
**Query Params:** `month` (1–12), `year` (e.g. 2026)  
**Response:** `MonthlyReservationReportDTO[]`
```json
[
  {
    "residentName": "Bruno Casé",
    "apartment": "101",
    "date": "2026-06-15",
    "spaceType": "SALAO_FESTAS"
  }
]
```

---

## Spaces

### GET /lunaLink/space
List all available spaces.  
**Auth:** Any authenticated user  
**Response:**
```json
[
  { "id": 1, "type": "SALAO_FESTAS" },
  { "id": 2, "type": "CHURRASQUEIRA" },
  { "id": 3, "type": "ACADEMIA" },
  { "id": 4, "type": "CAMPO_FUTEBOL" }
]
```

### POST /lunaLink/space
Create a new space.  
**Auth:** `ADMIN_ROLE`  
**Body:** `{ "type": "SALAO_FESTAS" }`

---

## Availability

### GET /lunaLink/availabilitySpaces/{spaceId}/availability/status
Check availability for a specific date.  
**Auth:** Any authenticated user  
**Params:** `spaceId` (Long)  
**Query:** `date=yyyy-MM-dd`  
**Response:**
```json
{
  "spaceId": 1,
  "date": "2026-06-15",
  "available": true
}
```

### GET /lunaLink/availabilitySpaces/{spaceId}/availability/month/{year}/{month}
Get all available and unavailable dates in a month (for calendar UIs).  
**Auth:** Any authenticated user  
**Response:**
```json
{
  "spaceId": 1,
  "year": 2026,
  "month": 6,
  "unavailableDates": ["2026-06-10", "2026-06-15"],
  "availableDates": ["2026-06-01", "2026-06-02"],
  "totalDays": 30,
  "unavailableCount": 2,
  "availableCount": 28,
  "occupancyPercentage": 6.67
}
```

### GET /lunaLink/availabilitySpaces/{spaceId}/availability/stats/{year}/{month}
Occupancy statistics for a month.  
**Auth:** Any authenticated user  
**Response:**
```json
{
  "spaceId": 1,
  "year": 2026,
  "month": 6,
  "totalDays": 30,
  "unavailableDays": 5,
  "availableDays": 25,
  "occupancyPercentage": 16.67
}
```

### POST /lunaLink/availabilitySpaces/{spaceId}/availability/period
Check availability over a date range.  
**Auth:** Any authenticated user  
**Body:**
```json
{
  "startDate": "2026-06-01",
  "endDate": "2026-06-30"
}
```
**Response:**
```json
{
  "spaceId": 1,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30",
  "totalDaysInPeriod": 30,
  "unavailableDates": ["2026-06-10"],
  "availableDates": ["2026-06-01"],
  "fullyAvailable": false,
  "fullyBooked": false
}
```

---

## Deliveries

### GET /lunaLink/delivery/findAll
List all deliveries.  
**Auth:** Any authenticated user  
**Response:** `ResponseDeliveryDTO[]`

### GET /lunaLink/delivery/find/{id}
Get a delivery by ID.  
**Auth:** Any authenticated user  
**Params:** `id` (UUID)  
**Response:** `ResponseDeliveryDTO`

### POST /lunaLink/delivery/create
Register a new delivery.  
**Auth:** Any authenticated user  
**Body:** `RequestDeliveryDTO`
```json
{
  "userId": "uuid-of-recipient",
  "protocolNumber": "PKG-001",
  "discrimination": "Amazon package",
  "otherRecipient": null,
  "image": null
}
```
**Response:** `ResponseDeliveryDTO` with `status: "PENDING"`

### PUT /lunaLink/delivery/update/{id}
Update delivery info.  
**Auth:** Any authenticated user  
**Body:** `RequestDeliveryDTO`

### PUT /lunaLink/delivery/{id}/confirm-receipt
Mark delivery as picked up.  
**Auth:** Any authenticated user  
**Params:** `id` (UUID)  
**Query (optional):** `pickedUpBy=string`  
**Response:** `ResponseDeliveryDTO` with `status: "DELIVERED"`

---

## Equipment Reservations

### POST /lunaLink/equipment-reservation
Create an equipment reservation for the logged-in user.  
**Auth:** Any authenticated user  
**Body:**
```json
{
  "equipmentId": 1,
  "date": "2026-06-15",
  "startTime": "09:00:00",
  "endTime": "11:00:00"
}
```
**Response:** `EquipmentReservationResponseDTO` with `status: "CONFIRMED"`

### PATCH /lunaLink/equipment-reservation/{id}/handover
Hand over equipment to user (sets to `IN_USE`).  
**Auth:** `ADMIN_ROLE`  
**Response:** `EquipmentReservationResponseDTO`

### PATCH /lunaLink/equipment-reservation/{id}/return
Mark equipment as returned (sets to `RETURNED`).  
**Auth:** `ADMIN_ROLE`  
**Response:** `EquipmentReservationResponseDTO`

### GET /lunaLink/equipment-reservation
List equipment reservations with optional filters.  
**Auth:** `ADMIN_ROLE`  
**Query Params (optional):** `date=yyyy-MM-dd`, `status=CONFIRMED|IN_USE|RETURNED|CANCELED`  
**Response:** `EquipmentReservationResponseDTO[]`

---

## Occurrences

### POST /lunaLink/occurrences
Report an incident. Authenticated user is auto-assigned.  
**Auth:** Any authenticated user  
**Body:**
```json
{
  "description": "Barulho excessivo no corredor",
  "incidentDate": "2026-06-07T14:30:00"
}
```
**Response:** `OccurrenceResponseDTO` (201)

### GET /lunaLink/occurrences
List all occurrences.  
**Auth:** Any authenticated user  
**Response:** `OccurrenceResponseDTO[]`

### GET /lunaLink/occurrences/find/{uuid}
Get occurrence by ID.  
**Auth:** Any authenticated user  
**Response:** `OccurrenceResponseDTO`

### DELETE /lunaLink/occurrences/delete/{uuid}
Delete an occurrence.  
**Auth:** Any authenticated user  
**Response:** 204 No Content

---

## Push Notifications

### GET /lunaLink/push/public-key
Get the VAPID public key for subscription setup.  
**Auth:** Public  
**Response:** `{ "publicKey": "string" }`

### POST /lunaLink/push/subscribe
Register a push subscription.  
**Auth:** Any authenticated user  
**Body:**
```json
{
  "endpoint": "https://fcm.googleapis.com/...",
  "keys": {
    "p256dh": "...",
    "auth": "..."
  }
}
```

### POST /lunaLink/push/unsubscribe
Remove a push subscription.  
**Auth:** Any authenticated user  
**Body:** `{ "endpoint": "string" }`
