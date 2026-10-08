# SMS Payment Forwarder (sms-to-sky-webhook)

Android SMS → webhook gateway for Bangladesh mobile-money payment notifications (bKash, Nagad, Rocket, Upay).

## Features

- Receives payment SMS and normalizes them into a shared webhook payload
- Local Room queue with fingerprint-based deduplication
- WorkManager delivery with HMAC-SHA256 signing and retries
- Dashboard, event history, settings, and SMS simulator
- Shared TypeScript contracts in `shared/` for backend consumers

## Project layout

```
android/   # Kotlin Android app (SMSPaymentForwarder)
shared/    # @smsforwarder/shared TypeScript types & constants
```

## Requirements

- Android Studio Hedgehog+ (or equivalent)
- JDK 17
- Android SDK 34
- Phone or emulator (minSdk 26)

## Run the app

1. Open `android/` in Android Studio
2. Let Gradle sync finish
3. Run on a device/emulator
4. Open **Settings** and set:
   - Webhook URL
   - API token
   - HMAC secret
5. Grant SMS + notification permissions
6. Use **Simulate SMS** or wait for a real payment SMS

## Webhook contract

`POST` JSON body (`WebhookPaymentPayload`):

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

## Shared package

```bash
cd shared
npm install
npm run build
```

## License

MIT — see [LICENSE](LICENSE)
