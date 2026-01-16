import { PrismaClient, Room } from '@prisma/client';
import { ReplayEvent } from '../adapters/replayAdapter';
import { persistRawEvent } from './persistRawEvent';
import { normalizeEvent } from './normalize';
import { persistNormalizedEvent } from './persistNormalizedEvent';
import { applyLedgerB } from './applyLedgerB';
import { logger } from '../utils/logger';

export class EventPipeline {
  private prisma: PrismaClient;

  constructor() {
    this.prisma = new PrismaClient();
  }

  async processEvent(room: Room, event: ReplayEvent): Promise<void> {
    try {
      // Step 1: 保存 RawEvent
      const rawEventId = await persistRawEvent(this.prisma, room, event);
      logger.debug(`📝 RawEvent 已保存`, { roomId: room.id, rawEventId });

      // Step 2: 标准化事件
      const normalized = normalizeEvent(room, event);

      // Step 3 & 4: 在一个事务中保存 NormalizedEvent 和 LedgerBEntry
      await this.prisma.$transaction(async (tx) => {
        // 保存 NormalizedEvent（带幂等检查）
        const normalizedResult = await persistNormalizedEvent(tx, normalized, rawEventId);

        if (normalizedResult.isDedup) {
          logger.debug(`♻️ NormalizedEvent 已存在（幂等）`, {
            roomId: room.id,
            idempotencyKey: normalized.idempotencyKey,
          });
        } else {
          logger.debug(`✅ NormalizedEvent 已保存`, {
            roomId: room.id,
            normalizedEventId: normalizedResult.id,
          });
        }

        // 应用账本（带幂等检查）
        try {
          const ledgerResult = await applyLedgerB(tx, room, normalizedResult.id, normalized);

          if (ledgerResult.isDedup) {
            logger.debug(`♻️ LedgerBEntry 已存在（幂等）`, {
              roomId: room.id,
              normalizedEventId: normalizedResult.id,
            });
          } else {
            logger.debug(`💰 LedgerBEntry 已保存`, {
              roomId: room.id,
              ledgerId: ledgerResult.id,
              delta: normalized.amount,
            });
          }
        } catch (error: unknown) {
          // 如果是因为金额为 0 跳过，不算错误
          if (
            error instanceof Error &&
            error.message &&
            error.message.includes('No amount change')
          ) {
            logger.debug(`⏭️ 跳过 LedgerBEntry（金额为 0）`, {
              roomId: room.id,
              eventType: normalized.eventType,
            });
          } else {
            throw error;
          }
        }
      });
    } catch (error) {
      logger.error(`❌ Pipeline 处理失败`, {
        roomId: room.id,
        error,
      });
      throw error;
    }
  }

  async disconnect(): Promise<void> {
    await this.prisma.$disconnect();
  }
}
