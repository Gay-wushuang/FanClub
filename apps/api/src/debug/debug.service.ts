import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class DebugService {
  constructor(private readonly prisma: PrismaService) {}

  async checkDb() {
    try {
      const creatorCount = await this.prisma.creator.count();
      const roomCount = await this.prisma.room.count();
      const fanclubCount = await this.prisma.fanclub.count();

      return {
        status: 'ok',
        database: 'connected',
        counts: {
          creators: creatorCount,
          rooms: roomCount,
          fanclubs: fanclubCount,
        },
      };
    } catch (error) {
      return {
        status: 'error',
        database: 'disconnected',
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }
}


