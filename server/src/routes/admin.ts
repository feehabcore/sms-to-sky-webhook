import { Router, Response, Request } from 'express';
import { v4 as uuidv4 } from 'uuid';
import {
  DEFAULT_CURRENCY,
  ORDER_STATUS,
  OrderRecord,
  WebhookDeviceRecord
} from '@smsforwarder/shared';
import { JsonStore } from '../store/jsonStore';
import { createAdminAuth } from '../middleware/auth';
import crypto from 'crypto';

export function adminRouter(store: JsonStore, adminToken: string): Router {
  const router = Router();
  router.use(createAdminAuth(adminToken));

  router.get('/health-detail', (_req, res: Response) => {
    const s = store.snapshot;
    res.json({
      success: true,
      counts: {
        devices: s.devices.length,
        events: s.events.length,
        payments: s.payments.length,
        orders: s.orders.length,
        audit: s.audit.length
      }
    });
  });

  router.get('/devices', (_req, res: Response) => {
    res.json({ success: true, devices: store.snapshot.devices });
  });

  router.post('/devices', (req: Request, res: Response) => {
    const device_id = String(req.body.device_id || '').trim();
    const device_name = String(req.body.device_name || 'Gateway').trim();
    if (!device_id) {
      res.status(422).json({ success: false, message: 'device_id required' });
      return;
    }
    if (store.snapshot.devices.some((d) => d.device_id === device_id)) {
      res.status(409).json({ success: false, message: 'device_id already exists' });
      return;
    }

    const record: WebhookDeviceRecord = {
      _id: uuidv4(),
      device_id,
      device_name,
      api_token: String(req.body.api_token || crypto.randomBytes(24).toString('hex')),
      hmac_secret: String(req.body.hmac_secret || crypto.randomBytes(32).toString('hex')),
      is_active: true,
      last_seen_at: null,
      registered_at: new Date().toISOString(),
      metadata: req.body.metadata || {}
    };

    store.update((draft) => {
      draft.devices.push(record);
      draft.audit.unshift({
        _id: uuidv4(),
        entity_type: 'DEVICE',
        entity_id: record._id,
        action: 'REGISTERED',
        details: { device_id },
        timestamp: new Date().toISOString()
      });
    });

    res.status(201).json({ success: true, device: record });
  });

  router.get('/events', (_req, res: Response) => {
    res.json({ success: true, events: store.snapshot.events.slice(0, 200) });
  });

  router.get('/payments', (_req, res: Response) => {
    res.json({ success: true, payments: store.snapshot.payments.slice(0, 200) });
  });

  router.get('/orders', (_req, res: Response) => {
    res.json({ success: true, orders: store.snapshot.orders.slice(0, 200) });
  });

  router.post('/orders', (req: Request, res: Response) => {
    const amount = Number(req.body.amount);
    const customer = req.body.customer || {};
    if (!Number.isFinite(amount) || amount <= 0) {
      res.status(422).json({ success: false, message: 'Valid amount required' });
      return;
    }
    if (!customer.name || !customer.phone) {
      res.status(422).json({ success: false, message: 'customer.name and customer.phone required' });
      return;
    }

    const order: OrderRecord = {
      _id: uuidv4(),
      order_number: String(req.body.order_number || `ORD-${Date.now()}`),
      customer: {
        name: String(customer.name),
        phone: String(customer.phone),
        email: customer.email ? String(customer.email) : undefined
      },
      amount,
      currency: String(req.body.currency || DEFAULT_CURRENCY),
      status: ORDER_STATUS.PENDING,
      delivery_type: req.body.delivery_type || 'MANUAL',
      delivery_info: req.body.delivery_info || {},
      createdAt: new Date().toISOString(),
      paidAt: null,
      deliveredAt: null
    };

    store.update((draft) => {
      draft.orders.unshift(order);
      draft.audit.unshift({
        _id: uuidv4(),
        entity_type: 'ORDER',
        entity_id: order._id,
        action: 'CREATED',
        details: { order_number: order.order_number, amount },
        timestamp: new Date().toISOString()
      });
    });

    res.status(201).json({ success: true, order });
  });

  router.get('/audit', (_req, res: Response) => {
    res.json({ success: true, audit: store.snapshot.audit.slice(0, 200) });
  });

  return router;
}
