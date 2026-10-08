import { v4 as uuidv4 } from 'uuid';
import {
  DEFAULT_CURRENCY,
  EVENT_STATUS,
  ORDER_STATUS,
  PAYMENT_STATUS,
  SUPPORTED_PROVIDERS,
  WebhookPaymentPayload,
  WebhookDeviceRecord
} from '@smsforwarder/shared';
import { JsonStore } from '../store/jsonStore';

export type IngestResult =
  | { ok: true; status: 'PROCESSED' | 'DUPLICATE'; event_id: string; payment_id?: string; order_id?: string | null }
  | { ok: false; statusCode: number; message: string };

export function ingestWebhookEvent(
  store: JsonStore,
  device: WebhookDeviceRecord,
  payload: WebhookPaymentPayload,
  meta: { signature?: string; rawPayload?: string; ip?: string }
): IngestResult {
  if (!payload?.event_id || !payload.transaction_id || payload.amount == null) {
    return { ok: false, statusCode: 422, message: 'Invalid payload: event_id, transaction_id, amount required' };
  }

  const provider = String(payload.provider || 'generic').toLowerCase();
  if (!(SUPPORTED_PROVIDERS as readonly string[]).includes(provider)) {
    return { ok: false, statusCode: 422, message: `Unsupported provider: ${provider}` };
  }

  const fingerprint =
    payload.fingerprint ||
    `${provider}|${payload.transaction_id}|${Number(payload.amount).toFixed(2)}|${payload.sender_identifier || ''}`;

  const existingByFingerprint = store.snapshot.events.find((e) => e.fingerprint === fingerprint);
  const existingByEventId = store.snapshot.events.find((e) => e.event_id === payload.event_id);
  if (existingByFingerprint || existingByEventId) {
    const dup = existingByFingerprint || existingByEventId!;
    store.update((draft) => {
      const ev = draft.events.find((e) => e._id === dup._id);
      if (ev) ev.status = EVENT_STATUS.DUPLICATE;
      draft.audit.unshift({
        _id: uuidv4(),
        entity_type: 'WEBHOOK_EVENT',
        entity_id: dup._id,
        action: 'DUPLICATE',
        details: { incoming_event_id: payload.event_id },
        timestamp: new Date().toISOString()
      });
    });
    return { ok: true, status: 'DUPLICATE', event_id: dup.event_id };
  }

  const now = new Date().toISOString();
  const eventId = payload.event_id;
  const eventRecordId = uuidv4();

  let matchedOrderId: string | null = null;
  let paymentStatus: typeof PAYMENT_STATUS[keyof typeof PAYMENT_STATUS] = PAYMENT_STATUS.UNMATCHED;

  const pendingOrders = store.snapshot.orders.filter(
    (o) =>
      o.status === ORDER_STATUS.PENDING &&
      o.currency === (payload.currency || DEFAULT_CURRENCY) &&
      Math.abs(o.amount - Number(payload.amount)) < 0.001
  );

  // Prefer reference / order_number match, then phone match, then first amount match
  const byRef = pendingOrders.find(
    (o) =>
      payload.reference &&
      (o.order_number === payload.reference || o._id === payload.reference)
  );
  const byPhone = pendingOrders.find(
    (o) =>
      payload.sender_identifier &&
      normalizePhone(o.customer.phone) === normalizePhone(payload.sender_identifier)
  );
  const matched = byRef || byPhone || pendingOrders[0] || null;

  if (matched) {
    matchedOrderId = matched._id;
    paymentStatus = PAYMENT_STATUS.VERIFIED;
  }

  const paymentId = uuidv4();

  store.update((draft) => {
    const deviceRow = draft.devices.find((d) => d.device_id === device.device_id);
    if (deviceRow) deviceRow.last_seen_at = now;

    draft.events.unshift({
      _id: eventRecordId,
      event_id: eventId,
      device_id: device.device_id,
      provider: provider as WebhookPaymentPayload['provider'],
      event_type: payload.event_type || 'payment_received',
      amount: Number(payload.amount),
      currency: payload.currency || DEFAULT_CURRENCY,
      transaction_id: payload.transaction_id,
      sender_identifier: payload.sender_identifier || 'unknown',
      receiver_identifier: payload.receiver_identifier,
      reference: payload.reference,
      timestamp: payload.timestamp || now,
      fingerprint,
      signature: meta.signature,
      status: EVENT_STATUS.PROCESSED,
      raw_payload: meta.rawPayload,
      ip_address: meta.ip,
      createdAt: now
    });

    draft.payments.unshift({
      _id: paymentId,
      transaction_id: payload.transaction_id,
      provider: provider as WebhookPaymentPayload['provider'],
      amount: Number(payload.amount),
      currency: payload.currency || DEFAULT_CURRENCY,
      order_id: matchedOrderId,
      sender_identifier: payload.sender_identifier || 'unknown',
      receiver_identifier: payload.receiver_identifier,
      device_id: device.device_id,
      status: paymentStatus,
      verifiedAt: matchedOrderId ? now : null,
      createdAt: now
    });

    if (matchedOrderId) {
      const order = draft.orders.find((o) => o._id === matchedOrderId);
      if (order) {
        order.status = ORDER_STATUS.PAID;
        order.payment_provider = provider as WebhookPaymentPayload['provider'];
        order.matched_transaction_id = payload.transaction_id;
        order.paidAt = now;
      }
      draft.audit.unshift({
        _id: uuidv4(),
        entity_type: 'ORDER',
        entity_id: matchedOrderId,
        action: 'PAYMENT_MATCHED',
        details: { payment_id: paymentId, transaction_id: payload.transaction_id },
        timestamp: now
      });
    }

    draft.audit.unshift({
      _id: uuidv4(),
      entity_type: 'WEBHOOK_EVENT',
      entity_id: eventRecordId,
      action: 'INGESTED',
      details: { matched_order_id: matchedOrderId, payment_status: paymentStatus },
      timestamp: now
    });
  });

  return {
    ok: true,
    status: 'PROCESSED',
    event_id: eventId,
    payment_id: paymentId,
    order_id: matchedOrderId
  };
}

function normalizePhone(raw: string): string {
  let digits = raw.replace(/[^\d+]/g, '');
  if (digits.startsWith('+880')) digits = '0' + digits.slice(4);
  if (digits.startsWith('880') && digits.length === 13) digits = '0' + digits.slice(3);
  return digits;
}
