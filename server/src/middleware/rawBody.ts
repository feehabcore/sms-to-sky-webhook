import { Request, Response, NextFunction } from 'express';

export interface RawBodyRequest extends Request {
  rawBody?: string;
}

/** Capture exact JSON bytes used for HMAC verification. */
export function jsonWithRawBody(
  req: RawBodyRequest,
  res: Response,
  buf: Buffer,
  encoding: BufferEncoding
): void {
  if (buf?.length) {
    req.rawBody = buf.toString(encoding || 'utf8');
  }
}
