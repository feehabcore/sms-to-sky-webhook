import type {
  AuditLogRecord,
  OrderRecord,
  PaymentEventRecord,
  PaymentRecord,
  WebhookDeviceRecord
} from '@smsforwarder/shared';

export interface DataStore {
  devices: WebhookDeviceRecord[];
  events: PaymentEventRecord[];
  payments: PaymentRecord[];
  orders: OrderRecord[];
  audit: AuditLogRecord[];
}

export function emptyStore(): DataStore {
  return {
    devices: [],
    events: [],
    payments: [],
    orders: [],
    audit: []
  };
}
