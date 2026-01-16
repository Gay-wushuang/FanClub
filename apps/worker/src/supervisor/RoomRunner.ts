import { Room } from '@prisma/client';
import { ReplayAdapter } from '../adapters/replayAdapter';
import { EventPipeline } from '../pipeline/EventPipeline';
import { logger } from '../utils/logger';
import { sleep } from '../utils/backoff';

export class RoomRunner {
  private room: Room & { creator: { id: string; name: string | null } };
  private mode: 'replay' | 'live';
  private repeat: number;
  private adapter?: ReplayAdapter;
  private pipeline: EventPipeline;
  private isRunning = false;
  private currentRun?: Promise<void>;

  constructor(
    room: Room & { creator: { id: string; name: string | null } },
    mode: 'replay' | 'live' = 'replay',
    repeat = 1
  ) {
    this.room = room;
    this.mode = mode;
    this.repeat = repeat;
    this.pipeline = new EventPipeline();
  }

  async start() {
    if (this.isRunning) {
      logger.warn('⚠️ Runner 已在运行', { roomId: this.room.id });
      return;
    }

    this.isRunning = true;
    logger.info(`🎬 RoomRunner 启动`, {
      roomId: this.room.id,
      platform: this.room.platform,
      platformRoomId: this.room.platformRoomId,
      mode: this.mode,
      repeat: this.repeat,
    });

    if (this.mode === 'replay') {
      this.adapter = new ReplayAdapter(this.room);
      this.currentRun = this.runReplayLoop();
    } else {
      logger.warn('⚠️ Live 模式暂未实现', { roomId: this.room.id });
    }
  }

  async stop() {
    if (!this.isRunning) {
      return;
    }

    logger.info(`🛑 RoomRunner 停止`, { roomId: this.room.id });
    this.isRunning = false;

    if (this.currentRun) {
      await this.currentRun;
    }

    // 断开 pipeline 连接
    await this.pipeline.disconnect();
  }

  private async runReplayLoop() {
    if (!this.adapter) {
      logger.error('❌ Adapter 未初始化', { roomId: this.room.id });
      return;
    }

    try {
      for (let iteration = 0; iteration < this.repeat; iteration++) {
        if (!this.isRunning) break;

        logger.info(`🔄 开始第 ${iteration + 1}/${this.repeat} 次 replay`, {
          roomId: this.room.id,
        });

        const events = this.adapter.getEvents();
        for (const event of events) {
          if (!this.isRunning) break;

          try {
            await this.pipeline.processEvent(this.room, event);
            logger.debug(`✅ 事件已处理`, {
              roomId: this.room.id,
              eventType: event.type,
            });
          } catch (error) {
            logger.error(`❌ 处理事件失败`, {
              roomId: this.room.id,
              error,
            });
          }

          // 每 500ms~1s 处理一条事件
          await sleep(500 + Math.random() * 500);
        }

        if (iteration < this.repeat - 1) {
          // 重复之间稍作停顿
          await sleep(2000);
        }
      }

      logger.info(`✅ Replay 完成`, {
        roomId: this.room.id,
        repeat: this.repeat,
      });
    } catch (error) {
      logger.error(`❌ Replay 循环失败`, {
        roomId: this.room.id,
        error,
      });
    }
  }
}
