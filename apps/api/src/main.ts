import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // 启用 CORS（开发环境）
  app.enableCors({
    origin: 'http://localhost:3000', // Next.js 默认端口
    credentials: true,
  });

  const port = process.env.API_PORT || 3001;
  await app.listen(port);
  console.log(`🚀 API 服务运行在 http://localhost:${port}`);
}

bootstrap();
