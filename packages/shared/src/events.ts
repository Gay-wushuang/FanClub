export type Platform = 'BILIBILI';

export type EventType = 'GIFT' | 'SUPERCHAT' | 'GUARD' | 'ENTER' | 'DANMU';

export type RawEventEnvelope = {
  platform: Platform;
  roomId: string; // internal Room.id
  receivedAt: string; // ISO
  payload: unknown; // 原始 JSON
  traceId: string;
};

export type NormalizedEvent = {
  platform: Platform;
  roomId: string;
  eventType: EventType;
  occurredAt: string; // ISO
  idempotencyKey: string; // 唯一幂等键（Day2 计算）
  platformEventId?: string;

  actorUid?: string;
  giftId?: string;
  giftCount?: number;
  amount?: string; // Decimal as string
};


