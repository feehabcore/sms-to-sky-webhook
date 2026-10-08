import crypto from 'crypto';

export function hmacSha256Hex(secret: string, payload: string): string {
  return crypto.createHmac('sha256', secret).update(payload, 'utf8').digest('hex');
}

export function timingSafeEqualHex(a: string, b: string): boolean {
  try {
    const ba = Buffer.from(a, 'utf8');
    const bb = Buffer.from(b, 'utf8');
    if (ba.length !== bb.length) return false;
    return crypto.timingSafeEqual(ba, bb);
  } catch {
    return false;
  }
}

/** Matches Android WebhookClient: HMAC-SHA256(secret, "{timestamp}.{rawJsonBody}") */
export function verifyWebhookSignature(
  secret: string,
  timestamp: string,
  rawBody: string,
  signature: string
): boolean {
  const expected = hmacSha256Hex(secret, `${timestamp}.${rawBody}`);
  return timingSafeEqualHex(expected.toLowerCase(), signature.trim().toLowerCase());
}
