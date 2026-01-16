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

  async getRooms() {
    return this.prisma.room.findMany({
      where: { isEnabled: true },
      include: {
        creator: {
          select: { id: true, name: true },
        },
      },
      orderBy: { createdAt: 'desc' },
    });
  }

  async getEvents(roomId?: string) {
    const where = roomId ? { roomId } : {};

    const [rawEvents, normalizedEvents] = await Promise.all([
      this.prisma.rawEvent.findMany({
        where,
        take: 50,
        orderBy: { receivedAt: 'desc' },
        include: {
          room: {
            select: { id: true, platformRoomId: true, platform: true },
          },
        },
      }),
      this.prisma.normalizedEvent.findMany({
        where,
        take: 50,
        orderBy: { occurredAt: 'desc' },
        include: {
          room: {
            select: { id: true, platformRoomId: true, platform: true },
          },
        },
      }),
    ]);

    return {
      rawEvents,
      normalizedEvents,
    };
  }

  async getLedger(creatorId?: string) {
    const where = creatorId ? { creatorId } : {};

    return this.prisma.ledgerBEntry.findMany({
      where,
      take: 50,
      orderBy: { occurredAt: 'desc' },
      include: {
        room: {
          select: { id: true, platformRoomId: true, platform: true },
        },
        creator: {
          select: { id: true, name: true },
        },
        normalizedEvent: {
          select: { id: true, eventType: true, idempotencyKey: true },
        },
      },
    });
  }
}
