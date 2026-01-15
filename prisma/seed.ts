import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 开始种子数据...');

  // 创建 1 个 creator
  const creator = await prisma.creator.upsert({
    where: { id: 'creator-1' },
    update: {},
    create: {
      id: 'creator-1',
      name: '测试创作者',
    },
  });

  console.log('✅ 创建 Creator:', creator);

  // 创建 2 个 room（不同 platformRoomId）
  const room1 = await prisma.room.upsert({
    where: {
      platform_platformRoomId: {
        platform: 'BILIBILI',
        platformRoomId: '123456',
      },
    },
    update: {},
    create: {
      platform: 'BILIBILI',
      platformRoomId: '123456',
      creatorId: creator.id,
      isEnabled: true,
    },
  });

  const room2 = await prisma.room.upsert({
    where: {
      platform_platformRoomId: {
        platform: 'BILIBILI',
        platformRoomId: '789012',
      },
    },
    update: {},
    create: {
      platform: 'BILIBILI',
      platformRoomId: '789012',
      creatorId: creator.id,
      isEnabled: true,
    },
  });

  console.log('✅ 创建 Room 1:', room1);
  console.log('✅ 创建 Room 2:', room2);

  // 创建 1 个 fanclub
  const fanclub = await prisma.fanclub.upsert({
    where: { id: 'fanclub-1' },
    update: {},
    create: {
      id: 'fanclub-1',
      creatorId: creator.id,
      name: '测试粉丝团',
    },
  });

  console.log('✅ 创建 Fanclub:', fanclub);

  console.log('🎉 种子数据完成！');
}

main()
  .catch((e) => {
    console.error('❌ 种子数据失败:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });


