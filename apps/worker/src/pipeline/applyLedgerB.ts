import { Prisma, Room } from '@prisma/client';
import { NormalizedEventData } from './types';

type Tx = Prisma.TransactionClient;

export async function applyLedgerB(
  tx: Tx,
  room: Room,
  normalizedEventId: string,
  normalized: NormalizedEventData
): Promise<{ id: string; isDedup: boolean }> {
  // 计算 delta（金额变化）
  const amount = normalized.amount ? parseFloat(normalized.amount) : 0;
  const delta = Math.round(amount * 100); // 转换为分

  if (delta === 0) {
    // 如果没有金额变化，不创建账本记录
    throw new Error('No amount change, skipping ledger entry');
  }

  // 确定 reason
  let reason = `${normalized.eventType}`;
  if (normalized.giftId && normalized.giftCount) {
    reason = `${normalized.eventType}:${normalized.giftId} x${normalized.giftCount}`;
  }

  // 先尝试查找是否已存在（基于 normalizedEventId 的唯一约束）
  const existing = await tx.ledgerBEntry.findUnique({
    where: {
      normalizedEventId,
    },
  });

  if (existing) {
    // 如果已存在，直接返回现有记录（幂等）
    return { id: existing.id, isDedup: true };
  }

  // 如果不存在，创建新记录
  const result = await tx.ledgerBEntry.create({
    data: {
      roomId: room.id,
      creatorId: room.creatorId,
      fanUid: normalized.actorUid || null,
      delta,
      reason,
      normalizedEventId,
      occurredAt: normalized.occurredAt,
    },
  });

  return { id: result.id, isDedup: false };
}
