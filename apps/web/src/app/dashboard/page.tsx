'use client';

import { useEffect, useState } from 'react';
import { fetchDashboardData } from '@/lib/api';

interface DashboardData {
  rooms: Array<{
    id: string;
    platform: string;
    platformRoomId: string;
    isEnabled: boolean;
  }>;
  fanclubs: Array<{
    id: string;
    name: string;
  }>;
}

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchDashboardData()
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <main style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <p>加载中...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <p style={{ color: 'red' }}>错误: {error}</p>
        <p style={{ marginTop: '1rem', fontSize: '0.9rem', color: '#666' }}>
          提示: 请确保 API 服务正在运行 (http://localhost:3001)
        </p>
      </main>
    );
  }

  return (
    <main style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <h1>Dashboard</h1>

      <section style={{ marginTop: '2rem' }}>
        <h2>房间列表</h2>
        {data?.rooms && data.rooms.length > 0 ? (
          <ul style={{ listStyle: 'none', marginTop: '1rem' }}>
            {data.rooms.map((room) => (
              <li
                key={room.id}
                style={{
                  padding: '1rem',
                  marginBottom: '0.5rem',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                }}
              >
                <div>
                  <strong>平台:</strong> {room.platform}
                </div>
                <div>
                  <strong>房间ID:</strong> {room.platformRoomId}
                </div>
                <div>
                  <strong>状态:</strong> {room.isEnabled ? '启用' : '禁用'}
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p style={{ marginTop: '1rem', color: '#666' }}>暂无房间数据</p>
        )}
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>粉丝团列表</h2>
        {data?.fanclubs && data.fanclubs.length > 0 ? (
          <ul style={{ listStyle: 'none', marginTop: '1rem' }}>
            {data.fanclubs.map((fanclub) => (
              <li
                key={fanclub.id}
                style={{
                  padding: '1rem',
                  marginBottom: '0.5rem',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                }}
              >
                <div>
                  <strong>名称:</strong> {fanclub.name}
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p style={{ marginTop: '1rem', color: '#666' }}>暂无粉丝团数据</p>
        )}
      </section>
    </main>
  );
}
