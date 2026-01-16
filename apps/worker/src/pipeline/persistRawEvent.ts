import { PrismaClient, Room } from '@prisma/client';
import { createHash } from 'crypto';
import { ReplayEvent } from '../adapters/replayAdapter';

export async function persistRawEvent(
  prisma: PrismaClient,
  room: Room,
  event: ReplayEvent
): Promise<string> {
  const traceId = createHash('sha256')
    .update(JSON.stringify(event.payload) + Date.now().toString())
    .digest('hex')
    .substring(0, 16);

  const rawEvent = await prisma.rawEvent.create({
    data: {
      platform: room.platform,
      roomId: room.id,
      receivedAt: new Date(),
      payload: event.payload as import('@prisma/client').Prisma.InputJsonValue,
      traceId,
    },
  });

  return rawEvent.id;
}
