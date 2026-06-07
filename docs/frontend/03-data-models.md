# Data Models & TypeScript Types

All types below can be used directly in a TypeScript frontend.

---

## Enumerations

```typescript
type UserRoles = 'ADMIN_ROLE' | 'RESIDENT_ROLE' | 'EMPLOYEE';

type ReservationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

type DeliveryStatus = 'PENDING' | 'DELIVERED';

type EquipmentReservationStatus = 'CONFIRMED' | 'IN_USE' | 'RETURNED' | 'CANCELED';

type SpaceType =
  | 'SALAO_FESTAS'
  | 'CHURRASQUEIRA'
  | 'ACADEMIA'
  | 'CAMPO_FUTEBOL';
```

**SpaceType display labels:**

| Value | Display |
|-------|---------|
| `SALAO_FESTAS` | Salão de Festas |
| `CHURRASQUEIRA` | Churrasqueira |
| `ACADEMIA` | Academia |
| `CAMPO_FUTEBOL` | Campo de Futebol |

---

## Auth

```typescript
interface LoginRequest {
  email: string;
  password: string;
}

// Response is a plain string (the JWT token), not JSON
type LoginResponse = string;
```

---

## Users

```typescript
// Used in GET responses
interface ResponseUserDTO {
  id: string;           // UUID
  name: string;
  apartment: string;
  email: string;
  role: UserRoles;
  reservation: ReservationSummary[];
}

interface ReservationSummary {
  id: string;           // UUID
  date: string;         // yyyy-MM-dd
  spaceType: SpaceType;
}

// Used in POST /create and PUT /update
interface RequestUserDTO {
  name: string;
  apartment: string;
  email: string;
  password: string;     // plain text — backend hashes it
  role: UserRoles;
}

// Used in dropdowns/selects
interface UserSummaryDTO {
  id: string;           // UUID
  name: string;
  apartment: string;
  email: string;
}
```

---

## Spaces

```typescript
interface Space {
  id: number;           // Long
  type: SpaceType;
}
```

---

## Reservations

```typescript
interface ReservationCreateDTO {
  date: string;         // yyyy-MM-dd
  spaceId: number;      // Long
}

interface ReservationRequestDTO {
  userId: string;       // UUID
  date: string;         // yyyy-MM-dd
  spaceId: number;      // Long
}

interface ReservationResponseDTO {
  id: string;           // UUID
  date: string;         // yyyy-MM-dd
  status: ReservationStatus;
  createdAt: string;    // ISO-8601 datetime
  canceledAt: string | null;
  user: ResponseUserDTO;
  space: Space;
}

interface MonthlyReservationReportDTO {
  residentName: string;
  apartment: string;
  date: string;         // yyyy-MM-dd
  spaceType: SpaceType;
}
```

---

## Availability

```typescript
interface AvailabilityResponseDTO {
  spaceId: number;
  date: string;         // yyyy-MM-dd
  available: boolean;
}

interface AvailabilityMonthDTO {
  spaceId: number;
  year: number;
  month: number;        // 1–12
  unavailableDates: string[];   // yyyy-MM-dd[]
  availableDates: string[];     // yyyy-MM-dd[]
  totalDays: number;
  unavailableCount: number;
  availableCount: number;
  occupancyPercentage: number;
}

interface OccupancyStatsResponseDTO {
  spaceId: number;
  year: number;
  month: number;
  totalDays: number;
  unavailableDays: number;
  availableDays: number;
  occupancyPercentage: number;
}

interface AvailabilityPeriodRequestDTO {
  startDate: string;    // yyyy-MM-dd
  endDate: string;      // yyyy-MM-dd
}

interface AvailabilityPeriodResponseDTO {
  spaceId: number;
  startDate: string;
  endDate: string;
  totalDaysInPeriod: number;
  unavailableDates: string[];
  availableDates: string[];
  fullyAvailable: boolean;
  fullyBooked: boolean;
}
```

---

## Deliveries

```typescript
interface RequestDeliveryDTO {
  userId: string;           // UUID — recipient user
  protocolNumber?: string;
  discrimination?: string;  // description of the package
  image?: string | null;    // base64-encoded bytes or null
  otherRecipient?: string;
}

interface ResponseDeliveryDTO {
  id: string;               // UUID
  userId: string;           // UUID
  protocolNumber: string | null;
  discrimination: string | null;
  image: number[] | null;   // byte array from backend (BYTEA)
  createdAt: string;        // ISO-8601 datetime
  createdBy: string;        // email of the user who registered
  otherRecipient: string | null;
  status: DeliveryStatus;
  deliveredAt: string | null;
  pickedUpBy: string | null;
}
```

> **Image handling note:** The backend stores images as `byte[]` (BYTEA in PostgreSQL).
> When sending an image, convert it to a base64 string or byte array.
> When displaying, convert the received byte array to a Blob/URL: `URL.createObjectURL(new Blob([new Uint8Array(image)]))`

---

## Equipment

```typescript
interface Equipment {
  id: number;           // Long
  name: string;
  active: boolean;
}
```

---

## Equipment Reservations

```typescript
interface EquipmentReservationRequestDTO {
  equipmentId: number;  // Long
  date: string;         // yyyy-MM-dd
  startTime: string;    // HH:mm:ss
  endTime: string;      // HH:mm:ss
}

interface EquipmentReservationResponseDTO {
  id: string;                         // UUID
  equipmentName: string;
  userName: string;
  userApartment: string;
  date: string;                       // yyyy-MM-dd
  startTime: string;                  // HH:mm:ss
  endTime: string;                    // HH:mm:ss
  status: EquipmentReservationStatus;
  createdAt: string;                  // ISO-8601 datetime
  pickedUpAt: string | null;
  returnedAt: string | null;
}
```

---

## Occurrences

```typescript
interface OccurrenceCreateRequestDTO {
  description: string;    // required, not blank
  incidentDate: string;   // ISO-8601 datetime (e.g. "2026-06-07T14:30:00")
}

interface OccurrenceResponseDTO {
  id: string;             // UUID
  userName: string;
  description: string;
  incidentDate: string;   // ISO-8601 datetime
  createdAt: string;      // ISO-8601 datetime
}
```

---

## Push Notifications

```typescript
interface PushSubscriptionRequestDTO {
  endpoint: string;
  keys: {
    p256dh: string;
    auth: string;
  };
}

interface PublicKeyResponseDTO {
  publicKey: string;    // VAPID public key
}
```

---

## Date & Time Formats

| Type | Format | Example |
|------|--------|---------|
| `LocalDate` | `yyyy-MM-dd` | `"2026-06-15"` |
| `LocalTime` | `HH:mm:ss` | `"09:00:00"` |
| `LocalDateTime` | ISO-8601 | `"2026-06-07T14:30:00"` |
| UUID | Standard UUID v4 | `"550e8400-e29b-41d4-a716-446655440000"` |
