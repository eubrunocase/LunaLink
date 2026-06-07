# Real-time Features

## WebSocket (STOMP)

The backend uses **Spring WebSocket with STOMP** over SockJS.

### Connection

```
WebSocket Endpoint: ws://localhost:8080/ws-lunalink
SockJS Fallback:    http://localhost:8080/ws-lunalink
```

The handshake does **not** require authentication — but subscribe to topics only after the user is logged in.

### STOMP Prefixes

| Prefix | Purpose |
|--------|---------|
| `/topic` | Broadcast to all subscribers |
| `/queue` | Point-to-point (user-specific) |
| `/app` | Send message to a server handler |

### Recommended Client Setup

Install dependencies:
```bash
npm install @stomp/stompjs sockjs-client
```

```typescript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const stompClient = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws-lunalink'),
  reconnectDelay: 5000,
  onConnect: () => {
    console.log('WebSocket connected');

    // Subscribe to topic examples:
    stompClient.subscribe('/topic/reservations', (message) => {
      const data = JSON.parse(message.body);
      // handle reservation update
    });

    stompClient.subscribe('/topic/deliveries', (message) => {
      const data = JSON.parse(message.body);
      // handle delivery update
    });
  },
  onDisconnect: () => {
    console.log('WebSocket disconnected');
  },
});

stompClient.activate();

// Clean up on component unmount
function disconnect() {
  stompClient.deactivate();
}
```

### Sending Messages to Server

```typescript
stompClient.publish({
  destination: '/app/some-destination',
  body: JSON.stringify({ key: 'value' }),
});
```

> Check with the backend team for active STOMP destinations — the WebSocket infrastructure is in place but specific topic/destination bindings depend on what domain events publish to `/topic` channels.

---

## Web Push Notifications

The backend uses **VAPID Web Push**. This allows sending push notifications to the browser even when the app is not open.

### Setup Flow

**1. Get the VAPID public key**
```
GET /lunaLink/push/public-key
Response: { "publicKey": "BG..." }
```

**2. Request notification permission**
```typescript
const permission = await Notification.requestPermission();
if (permission !== 'granted') {
  // User denied — don't subscribe
  return;
}
```

**3. Create a push subscription using the public key**
```typescript
function urlBase64ToUint8Array(base64String: string): Uint8Array {
  const padding = '='.repeat((4 - base64String.length % 4) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  return Uint8Array.from([...rawData].map((char) => char.charCodeAt(0)));
}

const registration = await navigator.serviceWorker.ready;
const subscription = await registration.pushManager.subscribe({
  userVisibleOnly: true,
  applicationServerKey: urlBase64ToUint8Array(publicKey),
});
```

**4. Send the subscription to the backend**
```typescript
const { endpoint, keys } = subscription.toJSON();
await fetch('/lunaLink/push/subscribe', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify({
    endpoint,
    keys: {
      p256dh: keys.p256dh,
      auth: keys.auth,
    },
  }),
});
```

**5. Handle push events in Service Worker**
```javascript
// service-worker.js
self.addEventListener('push', (event) => {
  const data = event.data?.json() ?? {};
  event.waitUntil(
    self.registration.showNotification(data.title ?? 'LunaLink', {
      body: data.body,
      icon: '/icon.png',
    })
  );
});
```

### Unsubscribe

```typescript
const subscription = await registration.pushManager.getSubscription();
if (subscription) {
  await fetch('/lunaLink/push/unsubscribe', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ endpoint: subscription.endpoint }),
  });
  await subscription.unsubscribe();
}
```

### Ionic/Capacitor Notes

Push notifications on Capacitor (mobile) use a different flow — the VAPID push here is browser-native (PWA). For native mobile, consider using Capacitor's `@capacitor/push-notifications` plugin alongside or instead of this.

The CORS config already allows `capacitor://localhost`, indicating this app targets Capacitor.
