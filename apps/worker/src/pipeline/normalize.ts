import { Room } from '@prisma/client';
import { Platform, EventType } from '@fanclub/shared';
import { ReplayEvent } from '../adapters/replayAdapter';
import { NormalizedEventData } from './types';
import { createHash } from 'crypto';

export function normalizeEvent(room: Room, rawEvent: ReplayEvent): NormalizedEventData {
  const platform: Platform = room.platform as Platform;
  const eventType = parseEventType(rawEvent.type);

  // 计算幂等键
  const idempotencyKey = computeIdempotencyKey(platform, room.platformRoomId, eventType, rawEvent);

  // 提取通用字段
  const occurredAt = new Date(rawEvent.occurredAt);
  const payload = rawEvent.payload;

  // 安全提取 payload 属性的辅助函数
  const getPayloadValue = (key: string): unknown => {
    if (typeof payload === 'object' && payload !== null) {
      return (payload as Record<string, unknown>)[key];
    }
    return undefined;
  };

  // 提取事件特定字段
  let actorUid: string | undefined;
  let giftId: string | undefined;
  let giftCount: number | undefined;
  let amount: string | undefined;
  let platformEventId: string | undefined;

  if (eventType === 'GIFT') {
    actorUid =
      (getPayloadValue('uid') as string) ||
      (getPayloadValue('userId') as string) ||
      (getPayloadValue('actorUid') as string);
    giftId = (getPayloadValue('giftId') as string) || (getPayloadValue('gift_id') as string);
    const countValue =
      (getPayloadValue('count') as number) || (getPayloadValue('giftCount') as number) || 1;
    giftCount = typeof countValue === 'number' ? countValue : 1;
    const priceValue = getPayloadValue('price') as number;
    amount = priceValue ? String(priceValue * giftCount) : undefined;
    platformEventId = (getPayloadValue('id') as string) || (getPayloadValue('eventId') as string);
  } else if (eventType === 'SUPERCHAT') {
    actorUid =
      (getPayloadValue('uid') as string) ||
      (getPayloadValue('userId') as string) ||
      (getPayloadValue('actorUid') as string);
    const priceValue = getPayloadValue('price') as number;
    amount = priceValue ? String(priceValue) : undefined;
    platformEventId = (getPayloadValue('id') as string) || (getPayloadValue('eventId') as string);
  }

  return {
    platform,
    roomId: room.id,
    eventType,
    occurredAt,
    idempotencyKey,
    platformEventId,
    actorUid,
    giftId,
    giftCount,
    amount,
  };
}

function parseEventType(type: string): EventType {
  const upper = type.toUpperCase();
  if (upper === 'GIFT' || upper === '礼物') {
    return 'GIFT';
  }
  if (upper === 'SUPERCHAT' || upper === '醒目留言' || upper === 'SC') {
    return 'SUPERCHAT';
  }
  if (upper === 'GUARD' || upper === '舰长') {
    return 'GUARD';
  }
  if (upper === 'ENTER' || upper === '进入') {
    return 'ENTER';
  }
  if (upper === 'DANMU' || upper === '弹幕') {
    return 'DANMU';
  }
  // 默认返回 GIFT
  return 'GIFT';
}

function computeIdempotencyKey(
  platform: Platform,
  platformRoomId: string,
  eventType: EventType,
  event: ReplayEvent
): string {
  const payload = event.payload;

  // 安全提取 payload 属性的辅助函数
  const getPayloadValue = (key: string): unknown => {
    if (typeof payload === 'object' && payload !== null) {
      return (payload as Record<string, unknown>)[key];
    }
    return undefined;
  };

  // 优先级 1: 有平台事件唯一 ID
  const eventId = (getPayloadValue('id') as string) || (getPayloadValue('eventId') as string);
  if (eventId) {
    return `${platform}:${platformRoomId}:${eventType}:${eventId}`;
  }

  // 优先级 2: 没有唯一 ID，使用关键字段计算
  const keyParts = [
    platform,
    platformRoomId,
    eventType,
    (getPayloadValue('uid') as string) || (getPayloadValue('userId') as string) || '',
    (getPayloadValue('giftId') as string) || (getPayloadValue('gift_id') as string) || '',
    (() => {
      const priceValue = getPayloadValue('price') as number;
      return typeof priceValue === 'number' ? String(priceValue) : '';
    })(),
    (() => {
      const countValue = getPayloadValue('count') as number;
      return typeof countValue === 'number' ? String(countValue) : '';
    })(),
    event.occurredAt,
  ];

  const keyString = keyParts.join(':');
  return createHash('sha256').update(keyString).digest('hex').substring(0, 32);

  // 优先级 3: 最差情况，使用整个 payload（已在上面的 keyParts 中覆盖）
}
