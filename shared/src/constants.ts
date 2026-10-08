export const SUPPORTED_PROVIDERS = [
  'bkash',
  'nagad',
  'rocket',
  'upay',
  'generic',
  'test'
] as const;

export type PaymentProvider = (typeof SUPPORTED_PROVIDERS)[number];

export const ORDER_STATUS = {
  PENDING: 'PENDING',
  PAYMENT_RECEIVED: 'PAYMENT_RECEIVED',
  VERIFIED: 'VERIFIED',
  PAID: 'PAID',
  DELIVERY_PENDING: 'DELIVERY_PENDING',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED'
} as const;

export type OrderStatus = (typeof ORDER_STATUS)[keyof typeof ORDER_STATUS];

export const PAYMENT_STATUS = {
  PENDING: 'PENDING',
  VERIFIED: 'VERIFIED',
  FAILED: 'FAILED',
  DUPLICATE: 'DUPLICATE',
  UNMATCHED: 'UNMATCHED',
  UNKNOWN: 'UNKNOWN'
} as const;

export type PaymentStatus = (typeof PAYMENT_STATUS)[keyof typeof PAYMENT_STATUS];

export const EVENT_STATUS = {
  PENDING: 'PENDING',
  PROCESSED: 'PROCESSED',
  DUPLICATE: 'DUPLICATE',
  FAILED: 'FAILED',
  UNKNOWN: 'UNKNOWN'
} as const;

export type EventStatus = (typeof EVENT_STATUS)[keyof typeof EVENT_STATUS];

export const RETRY_DELAYS_SECONDS = [10, 30, 60, 300, 900, 1800] as const;

export const DEFAULT_CURRENCY = 'BDT';
