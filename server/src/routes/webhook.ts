import { Router, Response } from 'express';
import { WebhookPaymentPayload } from '@smsforwarder/shared';
import { JsonStore } from '../store/jsonStore';
import { createWebhookAuth } from '../middleware/auth';
import { RawBodyRequest } from '../middleware/rawBody';
import { ingestWebhookEvent } from '../services/ingest';
import { WebhookDeviceRecord } from '@smsforwarder/shared';

export function webhookRouter(store: JsonStore, toleranceMs: number): Router {
  const router = Router();
  const auth = createWebhookAuth(store, toleranceMs);

  router.post('/payments', auth, (req: RawBodyRequest, res: Response) => {
    const device = (req as RawBodyRequest & { device?: WebhookDeviceRecord }).device;
    if (!device) {
      res.status(401).json({ success: false, message: 'Unauthorized' });
      return;
    }

    const payload = req.body as WebhookPaymentPayload;
    // Allow test provider without failing
    const result = ingestWebhookEvent(store, device, payload, {
      signature: String(req.header('X-Signature') || ''),
      rawPayload: req.rawBody,
      ip: req.ip
    });

    if (!result.ok) {
      res.status(result.statusCode).json({ success: false, message: result.message });
      return;
    }

    res.status(200).json({
      success: true,
      status: result.status,
      event_id: result.event_id,
      payment_id: result.payment_id,
      order_id: result.order_id
    });
  });

  return router;
}
