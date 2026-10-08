import { Response, NextFunction } from 'express';
import { JsonStore } from '../store/jsonStore';
import { verifyWebhookSignature } from '../security/hmac';
import { RawBodyRequest } from './rawBody';

export function createWebhookAuth(store: JsonStore, toleranceMs: number) {
  return (req: RawBodyRequest, res: Response, next: NextFunction): void => {
    const apiToken = String(req.header('X-Api-Token') || '');
    const signature = String(req.header('X-Signature') || '');
    const deviceId = String(req.header('X-Device-Id') || '');
    const timestamp = String(req.header('X-Timestamp') || '');
    const rawBody = req.rawBody || '';

    if (!apiToken || !signature || !deviceId || !timestamp || !rawBody) {
      res.status(401).json({ success: false, message: 'Missing auth headers or body' });
      return;
    }

    const ts = Number(timestamp);
    if (!Number.isFinite(ts) || Math.abs(Date.now() - ts) > toleranceMs) {
      res.status(401).json({ success: false, message: 'Timestamp outside allowed window' });
      return;
    }

    const device = store.snapshot.devices.find(
      (d) => d.device_id === deviceId && d.api_token === apiToken && d.is_active
    );
    if (!device) {
      res.status(401).json({ success: false, message: 'Unknown or inactive device' });
      return;
    }

    if (!verifyWebhookSignature(device.hmac_secret, timestamp, rawBody, signature)) {
      store.update((draft) => {
        draft.audit.unshift({
          _id: `aud_${Date.now()}`,
          entity_type: 'SECURITY',
          entity_id: deviceId,
          action: 'HMAC_FAILED',
          details: { ip: req.ip },
          timestamp: new Date().toISOString()
        });
      });
      res.status(401).json({ success: false, message: 'Invalid signature' });
      return;
    }

    (req as RawBodyRequest & { device?: typeof device }).device = device;
    next();
  };
}

export function createAdminAuth(adminToken: string) {
  return (req: RawBodyRequest, res: Response, next: NextFunction): void => {
    const token = String(req.header('X-Admin-Token') || req.header('Authorization')?.replace(/^Bearer\s+/i, '') || '');
    if (!adminToken || token !== adminToken) {
      res.status(401).json({ success: false, message: 'Admin auth required' });
      return;
    }
    next();
  };
}
