import { Room } from '@prisma/client';
import { readFileSync } from 'fs';
import { join } from 'path';
import { Platform } from '@fanclub/shared';
import { logger } from '../utils/logger';

export type ReplayEvent = {
  type: string;
  platform: Platform;
  platformRoomId: string;
  occurredAt: string;
  payload: unknown;
};

export class ReplayAdapter {
  private room: Room;
  private events: ReplayEvent[] = [];

  constructor(room: Room) {
    this.room = room;
    this.loadEvents();
  }

  private loadEvents() {
    try {
      const fixturesPath = join(__dirname, '../../fixtures');

      // 加载 gift 事件
      try {
        const giftData = JSON.parse(readFileSync(join(fixturesPath, 'gift.sample.json'), 'utf-8'));
        this.events.push({
          ...giftData,
          platform: this.room.platform,
          platformRoomId: this.room.platformRoomId,
        });
      } catch (error) {
        logger.warn('⚠️ 无法加载 gift fixture', { error });
      }

      // 加载 superchat 事件
      try {
        const superchatData = JSON.parse(
          readFileSync(join(fixturesPath, 'superchat.sample.json'), 'utf-8')
        );
        this.events.push({
          ...superchatData,
          platform: this.room.platform,
          platformRoomId: this.room.platformRoomId,
        });
      } catch (error) {
        logger.warn('⚠️ 无法加载 superchat fixture', { error });
      }

      logger.info(`📦 加载了 ${this.events.length} 个 replay 事件`, {
        roomId: this.room.id,
      });
    } catch (error) {
      logger.error('❌ 加载 fixtures 失败', { error });
    }
  }

  getEvents(): ReplayEvent[] {
    return this.events;
  }
}
