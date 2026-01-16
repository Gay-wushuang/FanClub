import { Prisma } from '@prisma/client';
import { NormalizedEventData } from './types';

type Tx = Prisma.TransactionClient;

export async function persistNormalizedEvent(
  tx: Tx,
  normalized: NormalizedEventData,
  rawEventId?: string
): Promise<{ id: string; isDedup: boolean }> {
  // 先尝试查找是否已存在
  const existing = await tx.normalizedEvent.findUnique({
    where: {
      platform_idempotencyKey: {
        platform: normalized.platform,
        idempotencyKey: normalized.idempotencyKey,
      },
    },
  });

  if (existing) {
    // 如果已存在，直接返回现有记录（幂等）
    return { id: existing.id, isDedup: true };
  }

  // 如果不存在，创建新记录
  const result = await tx.normalizedEvent.create({
    data: {
      platform: normalized.platform,
      roomId: normalized.roomId,
      eventType: normalized.eventType,
      occurredAt: normalized.occurredAt,
      receivedAt: new Date(),
      idempotencyKey: normalized.idempotencyKey,
      platformEventId: normalized.platformEventId,
      actorUid: normalized.actorUid,
      giftId: normalized.giftId,
      giftCount: normalized.giftCount,
      amount: normalized.amount ? normalized.amount : null,
      rawEventId: rawEventId || null,
    },
  });

  return { id: result.id, isDedup: false };
}
