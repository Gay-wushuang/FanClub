const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:3001';

export async function fetchDashboardData() {
  try {
    // 先尝试调用真实的 API
    const debugResponse = await fetch(`${API_BASE_URL}/debug/db`);
    const debugData = await debugResponse.json();

    if (debugData.status === 'ok' && debugData.counts) {
      // 如果有真实数据，返回 mock 数据（Day1 阶段）
      // Day2 可以替换为真实的 API 调用
      return {
        rooms: [
          {
            id: 'room-1',
            platform: 'BILIBILI',
            platformRoomId: '123456',
            isEnabled: true,
          },
          {
            id: 'room-2',
            platform: 'BILIBILI',
            platformRoomId: '789012',
            isEnabled: true,
          },
        ],
        fanclubs: [
          {
            id: 'fanclub-1',
            name: '测试粉丝团',
          },
        ],
      };
    }

    // 如果 API 不可用，返回 mock 数据
    return {
      rooms: [
        {
          id: 'room-1',
          platform: 'BILIBILI',
          platformRoomId: '123456',
          isEnabled: true,
        },
        {
          id: 'room-2',
          platform: 'BILIBILI',
          platformRoomId: '789012',
          isEnabled: true,
        },
      ],
      fanclubs: [
        {
          id: 'fanclub-1',
          name: '测试粉丝团',
        },
      ],
    };
  } catch (error) {
    // API 不可用时返回 mock 数据
    console.warn('API 不可用，使用 mock 数据:', error);
    return {
      rooms: [
        {
          id: 'room-1',
          platform: 'BILIBILI',
          platformRoomId: '123456',
          isEnabled: true,
        },
        {
          id: 'room-2',
          platform: 'BILIBILI',
          platformRoomId: '789012',
          isEnabled: true,
        },
      ],
      fanclubs: [
        {
          id: 'fanclub-1',
          name: '测试粉丝团',
        },
      ],
    };
  }
}
