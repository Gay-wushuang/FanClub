import { Platform, EventType } from '@fanclub/shared';

export type RawEventPayload = {
  platform: Platform;
  roomId: string;
  receivedAt: Date;
  payload: unknown;
  traceId: string;
};

export type NormalizedEventData = {
  platform: Platform;
  roomId: string;
  eventType: EventType;
  occurredAt: Date;
  idempotencyKey: string;
  platformEventId?: string;
  actorUid?: string;
  giftId?: string;
  giftCount?: number;
  amount?: string;
};
