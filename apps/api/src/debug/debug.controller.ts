import { Controller, Get } from '@nestjs/common';
import { DebugService } from './debug.service';

@Controller('debug')
export class DebugController {
  constructor(private readonly debugService: DebugService) {}

  @Get('db')
  async checkDb() {
    return this.debugService.checkDb();
  }
}


