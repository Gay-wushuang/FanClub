import { Controller, Get, Query } from '@nestjs/common';
import { DebugService } from './debug.service';

@Controller('debug')
export class DebugController {
  constructor(private readonly debugService: DebugService) {}

  @Get('db')
  async checkDb() {
    return this.debugService.checkDb();
  }

  @Get('rooms')
  async getRooms() {
    return this.debugService.getRooms();
  }

  @Get('events')
  async getEvents(@Query('roomId') roomId?: string) {
    return this.debugService.getEvents(roomId);
  }

  @Get('ledger')
  async getLedger(@Query('creatorId') creatorId?: string) {
    return this.debugService.getLedger(creatorId);
  }
}
