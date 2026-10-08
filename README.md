# Payment SMS Gateway

Lightweight Android app that reads mobile-money payment SMS (bKash, Nagad, Rocket, Upay) and forwards them to your webhook URL.

## Direct app download

**[Download SMSPaymentForwarder-v1.1.0.apk](https://github.com/feehabcore/sms-to-sky-webhook/releases/download/v1.1.0/SMSPaymentForwarder-v1.1.0.apk)**

Release page: https://github.com/feehabcore/sms-to-sky-webhook/releases/tag/v1.1.0

1. Download the APK on your Android phone  
2. Allow install from unknown sources if asked  
3. Open the app → **Settings** → set webhook URL, API token, HMAC secret  
4. Grant SMS + notification permissions  
5. Use **Simulate SMS** to test, or wait for a real payment SMS  

Min Android: **8.0 (API 26)**

## What the app does

- Listens for payment SMS  
- Parses provider, amount, transaction id, sender  
- Queues locally (Room) with fingerprint dedupe  
- POSTs to your webhook with HMAC-SHA256 signing + retries  
- Simple dashboard: queue / sent / failed  

No account, no Play Store dependency — install the APK and point it at your URL.

## Project layout

```
android/   # Lightweight Android app (this is the product)
shared/    # Optional TypeScript types for your own backend
server/    # Optional local test receiver (not required to use the app)
```

## Webhook the app sends

`POST` JSON to your URL with headers:

- `X-Api-Token`
- `X-Device-Id`
- `X-Timestamp`
- `X-Signature` = `hex(HMAC-SHA256(secret, "{timestamp}.{rawJsonBody}"))`

## Build from source (optional)

Open `android/` in Android Studio (JDK 17, SDK 34) and Run.

Or:

```bash
cd android
./gradlew.bat assembleRelease
```

APK output: `android/app/build/outputs/apk/release/app-release.apk`

## License

MIT — see [LICENSE](LICENSE)
