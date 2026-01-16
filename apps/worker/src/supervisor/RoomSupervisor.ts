import { PrismaClient } from '@prisma/client';
import { RoomRunner } from './RoomRunner';
import { logger } from '../utils/logger';

export class RoomSupervisor {
  private prisma: PrismaClient;
  private mode: 'replay' | 'live';
  private repeat: number;
  private runners: Map<string, RoomRunner> = new Map();
  private isRunning = false;
  private refreshInterval?: NodeJS.Timeout;

  constructor(mode: 'replay' | 'live' = 'replay', repeat = 1) {
    this.prisma = new PrismaClient();
    this.mode = mode;
    this.repeat = repeat;
  }

  async start() {
    logger.info('🏁 RoomSupervisor 启动', { mode: this.mode, repeat: this.repeat });
    this.isRunning = true;

    // 初始加载房间
    await this.refreshRooms();

    // 每 10 秒刷新一次房间列表
    this.refreshInterval = setInterval(async () => {
      if (this.isRunning) {
        await this.refreshRooms();
      }
    }, 10000);

    logger.info('✅ RoomSupervisor 已启动');
  }

  async stop() {
    logger.info('🛑 RoomSupervisor 正在停止...');
    this.isRunning = false;

    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }

    // 停止所有 runner
    const stopPromises = Array.from(this.runners.values()).map((runner) => runner.stop());
    await Promise.all(stopPromises);
    this.runners.clear();

    await this.prisma.$disconnect();
    logger.info('✅ RoomSupervisor 已停止');
  }

  private async refreshRooms() {
    try {
      const rooms = await this.prisma.room.findMany({
        where: { isEnabled: true },
        include: {
          creator: {
            select: { id: true, name: true },
          },
        },
      });

      logger.info(`📋 发现 ${rooms.length} 个启用的房间`);

      // 启动新的房间 runner
      for (const room of rooms) {
        const roomId = room.id;
        if (!this.runners.has(roomId)) {
          logger.info(`🎬 启动房间 runner`, {
            roomId,
            platform: room.platform,
            platformRoomId: room.platformRoomId,
          });

          const runner = new RoomRunner(room, this.mode, this.repeat);
          await runner.start();
          this.runners.set(roomId, runner);
        }
      }

      // 停止已禁用的房间 runner
      const activeRoomIds = new Set(rooms.map((r) => r.id));
      for (const [roomId, runner] of this.runners.entries()) {
        if (!activeRoomIds.has(roomId)) {
          logger.info(`🛑 停止房间 runner`, { roomId });
          await runner.stop();
          this.runners.delete(roomId);
        }
      }
    } catch (error) {
      logger.error('❌ 刷新房间列表失败', error);
    }
  }
}
