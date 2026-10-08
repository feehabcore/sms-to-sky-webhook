import path from 'path';
import dotenv from 'dotenv';
import { v4 as uuidv4 } from 'uuid';
import { JsonStore } from '../store/jsonStore';

dotenv.config({ path: path.resolve(process.cwd(), '.env') });

const dataDir = path.resolve(process.cwd(), process.env.DATA_DIR || './data');
const store = new JsonStore(dataDir);

const deviceId = process.env.SEED_DEVICE_ID || 'android-demo-device';
const existing = store.snapshot.devices.find((d) => d.device_id === deviceId);

if (existing) {
  console.log('Device already seeded:');
  console.log(JSON.stringify(existing, null, 2));
  process.exit(0);
}

const device = {
  _id: uuidv4(),
  device_id: deviceId,
  device_name: process.env.SEED_DEVICE_NAME || 'Demo Gateway',
  api_token: process.env.SEED_API_TOKEN || 'demo-api-token',
  hmac_secret: process.env.SEED_HMAC_SECRET || 'demo-hmac-secret',
  is_active: true,
  last_seen_at: null as string | null,
  registered_at: new Date().toISOString(),
  metadata: { seeded: true }
};

store.update((draft) => {
  draft.devices.push(device);
});

console.log('Seeded device — use these values in the Android Settings screen:');
console.log(JSON.stringify(device, null, 2));
console.log(`\nWebhook URL: http://<your-pc-ip>:${process.env.PORT || 8787}/webhook/payments`);
console.log('(Emulator host loopback: http://10.0.2.2:8787/webhook/payments)');
