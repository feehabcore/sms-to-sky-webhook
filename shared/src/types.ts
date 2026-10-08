import { PaymentProvider, OrderStatus, PaymentStatus, EventStatus } from './constants';

export interface NormalizedPaymentEvent {
  event_id: string;
  provider: PaymentProvider;
  event_type: 'payment_received';
  amount: number;
  currency: string;
  transaction_id: string;
  sender_identifier: string;
  receiver_identifier?: string;
  reference?: string;
  timestamp: string; // ISO 8601
  device_id: string;
  fingerprint: string;
  raw_sms?: string;
}

export interface WebhookPaymentPayload {
  event_id: string;
  provider: PaymentProvider;
  event_type: string;
  amount: number;
  currency: string;
  transaction_id: string;
  sender_identifier: string;
  receiver_identifier?: string;
  reference?: string;
  timestamp: string;
  device_id: string;
  fingerprint?: string;
}

export interface CustomerInfo {
  name: string;
  phone: string;
  email?: string;
}

export interface OrderRecord {
  _id: string;
  order_number: string;
  customer: CustomerInfo;
  amount: number;
  currency: string;
  status: OrderStatus;
  payment_provider?: PaymentProvider;
  matched_transaction_id?: string;
  delivery_type: 'DIGITAL_KEY' | 'WEBHOOK' | 'MANUAL';
  delivery_info?: Record<string, any>;
  createdAt: string;
  paidAt?: string | null;
  deliveredAt?: string | null;
}

export interface PaymentRecord {
  _id: string;
  transaction_id: string;
  provider: PaymentProvider;
  amount: number;
  currency: string;
  order_id?: string | null;
  sender_identifier: string;
  receiver_identifier?: string;
  device_id: string;
  status: PaymentStatus;
  raw_sms?: string;
  verifiedAt?: string | null;
  createdAt: string;
}

export interface PaymentEventRecord {
  _id: string;
  event_id: string;
  device_id: string;
  provider: PaymentProvider;
  event_type: string;
  amount: number;
  currency: string;
  transaction_id: string;
  sender_identifier: string;
  receiver_identifier?: string;
  reference?: string;
  timestamp: string;
  fingerprint: string;
  signature?: string;
  status: EventStatus;
  error_message?: string;
  raw_payload?: string;
  ip_address?: string;
  createdAt: string;
}

export interface WebhookDeviceRecord {
  _id: string;
  device_id: string;
  device_name: string;
  api_token: string;
  hmac_secret: string;
  is_active: boolean;
  last_seen_at?: string | null;
  registered_at: string;
  metadata?: Record<string, any>;
}

export interface AuditLogRecord {
  _id: string;
  entity_type: 'ORDER' | 'PAYMENT' | 'WEBHOOK_EVENT' | 'DEVICE' | 'SECURITY';
  entity_id: string;
  action: string;
  details: Record<string, any>;
  timestamp: string;
}
