# SMS Payment Forwarder (sms-to-sky-webhook)

Android SMS → webhook gateway for Bangladesh mobile-money payment notifications (bKash, Nagad, Rocket, Upay), plus a Node.js receiver that verifies HMAC signatures and matches payments to orders.

## Features

- Receives payment SMS and normalizes them into a shared webhook payload
- Local Room queue with fingerprint-based deduplication
- WorkManager delivery with HMAC-SHA256 signing and retries
- Dashboard, event history, settings, and SMS simulator
- Node webhook server: device auth, event ingest, order matching, admin API
- Shared TypeScript contracts in `shared/`

## Project layout

```
android/   # Kotlin Android app (SMSPaymentForwarder)
shared/    # @smsforwarder/shared TypeScript types & constants
server/    # Express webhook + admin API
```

## Quick start — server

```bash
cd shared && npm install && npm run build && cd ..
cd server
cp .env.example .env
npm install
npm run seed
npm run dev
```

Server listens on `http://0.0.0.0:8787`.

| Endpoint | Auth | Purpose |
|----------|------|---------|
| `GET /health` | none | Liveness |
| `POST /webhook/payments` | device HMAC | Ingest payment SMS events |
| `GET/POST /admin/devices` | `X-Admin-Token` | List / register devices |
| `GET/POST /admin/orders` | `X-Admin-Token` | List / create orders |
| `GET /admin/events` | `X-Admin-Token` | Recent webhook events |
| `GET /admin/payments` | `X-Admin-Token` | Matched/unmatched payments |

After `npm run seed`, use these in the Android **Settings** screen:

- **Webhook URL** (emulator): `http://10.0.2.2:8787/webhook/payments`
- **Webhook URL** (LAN phone): `http://<your-pc-ip>:8787/webhook/payments`
- **API token** / **HMAC secret**: printed by the seed script (defaults in `.env.example`)

## Quick start — Android app

1. Open `android/` in Android Studio (JDK 17, SDK 34, minSdk 26)
2. Run on a device/emulator
3. Configure Settings with the seeded credentials above
4. Grant SMS + notification permissions
5. Use **Simulate SMS** or wait for a real payment SMS

## Webhook contract

`POST /webhook/payments` JSON body (`WebhookPaymentPayload`):

| Field | Type | Notes |
|-------|------|--------|
| `event_id` | string | UUID |
| `provider` | string | `bkash` / `nagad` / `rocket` / `upay` / `generic` / `test` |
| `event_type` | string | `payment_received` |
| `amount` | number | |
| `currency` | string | `BDT` |
| `transaction_id` | string | |
| `sender_identifier` | string | |
| `timestamp` | string | ISO-8601 |
| `device_id` | string | |
| `fingerprint` | string | Dedup hash |

### Auth headers

- `X-Api-Token`: device API token
- `X-Device-Id`: device id
- `X-Timestamp`: unix millis
- `X-Signature`: `hex(HMAC-SHA256(secret, "{timestamp}.{rawJsonBody}"))`

Retry delays (seconds): `10, 30, 60, 300, 900, 1800`

## Order matching

Pending orders are matched when a payment arrives by:

1. `reference` ↔ `order_number` / order id  
2. else sender phone ↔ customer phone  
3. else first pending order with the same amount + currency  

Matched orders move to `PAID`.

## Shared package

```bash
cd shared
npm install
npm run build
```

## License

MIT — see [LICENSE](LICENSE)
