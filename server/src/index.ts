import path from 'path';
import dotenv from 'dotenv';
import express from 'express';
import cors from 'cors';
import { JsonStore } from './store/jsonStore';
import { jsonWithRawBody, RawBodyRequest } from './middleware/rawBody';
import { webhookRouter } from './routes/webhook';
import { adminRouter } from './routes/admin';

dotenv.config({ path: path.resolve(process.cwd(), '.env') });

const PORT = Number(process.env.PORT || 8787);
const HOST = process.env.HOST || '0.0.0.0';
const ADMIN_TOKEN = process.env.ADMIN_TOKEN || 'change-me-admin-token';
const DATA_DIR = path.resolve(process.cwd(), process.env.DATA_DIR || './data');
const TIMESTAMP_TOLERANCE_MS = Number(process.env.TIMESTAMP_TOLERANCE_MS || 300000);

const store = new JsonStore(DATA_DIR);
const app = express();

app.use(cors());
app.use(
  express.json({
    verify: (req, _res, buf, encoding) => {
      jsonWithRawBody(req as RawBodyRequest, _res as express.Response, buf, encoding as BufferEncoding);
    }
  })
);

app.get('/health', (_req, res) => {
  res.json({
    success: true,
    service: 'sms-to-sky-webhook',
    time: new Date().toISOString()
  });
});

app.use('/webhook', webhookRouter(store, TIMESTAMP_TOLERANCE_MS));
app.use('/admin', adminRouter(store, ADMIN_TOKEN));

app.use((_req, res) => {
  res.status(404).json({ success: false, message: 'Not found' });
});

app.listen(PORT, HOST, () => {
  console.log(`SMS webhook server listening on http://${HOST}:${PORT}`);
  console.log(`Webhook endpoint: POST /webhook/payments`);
  console.log(`Admin API: /admin/* (header X-Admin-Token)`);
  console.log(`Data dir: ${DATA_DIR}`);
});
