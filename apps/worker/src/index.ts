import { RoomSupervisor } from './supervisor/RoomSupervisor';
import { logger } from './utils/logger';

const MODE = process.argv.includes('--mode=replay') ? 'replay' : 'replay';
const REPEAT = process.argv.find((arg) => arg.startsWith('--repeat='))?.split('=')[1]
  ? parseInt(process.argv.find((arg) => arg.startsWith('--repeat='))?.split('=')[1] || '1')
  : 1;

async function main() {
  logger.info('🚀 Worker 启动中...', { mode: MODE, repeat: REPEAT });

  const supervisor = new RoomSupervisor(MODE, REPEAT);

  try {
    await supervisor.start();

    // 优雅关闭
    process.on('SIGINT', async () => {
      logger.info('🛑 收到 SIGINT，正在关闭...');
      await supervisor.stop();
      process.exit(0);
    });

    process.on('SIGTERM', async () => {
      logger.info('🛑 收到 SIGTERM，正在关闭...');
      await supervisor.stop();
      process.exit(0);
    });
  } catch (error) {
    logger.error('❌ Worker 启动失败', error);
    process.exit(1);
  }
}

main();
