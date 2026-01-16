'use client';

import { useEffect, useState } from 'react';
import { fetchDashboardData } from '@/lib/api';

interface DashboardData {
  rooms: Array<{
    id: string;
    platform: string;
    platformRoomId: string;
    isEnabled: boolean;
    creator?: { id: string; name: string | null };
  }>;
  events: {
    rawEvents: Array<{
      id: string;
      platform: string;
      receivedAt: string;
      traceId: string;
      room?: { id: string; platformRoomId: string };
    }>;
    normalizedEvents: Array<{
      id: string;
      platform: string;
      eventType: string;
      occurredAt: string;
      idempotencyKey: string;
      actorUid?: string | null;
      amount?: string | null;
      room?: { id: string; platformRoomId: string };
    }>;
  };
  ledger: Array<{
    id: string;
    delta: number;
    reason: string;
    occurredAt: string;
    fanUid?: string | null;
    room?: { id: string; platformRoomId: string };
    creator?: { id: string; name: string | null };
    normalizedEvent?: { id: string; eventType: string };
  }>;
}

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadData = () => {
      fetchDashboardData()
        .then(setData)
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false));
    };

    loadData();

    // 每 2 秒刷新一次数据
    const interval = setInterval(loadData, 2000);

    return () => clearInterval(interval);
  }, []);

  if (loading && !data) {
    return (
      <main style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <p>加载中...</p>
      </main>
    );
  }

  return (
    <main style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <h1>Dashboard</h1>

      {error && (
        <div
          style={{
            padding: '1rem',
            marginTop: '1rem',
            backgroundColor: '#fee',
            borderRadius: '4px',
          }}
        >
          <p style={{ color: 'red' }}>错误: {error}</p>
          <p style={{ marginTop: '0.5rem', fontSize: '0.9rem', color: '#666' }}>
            提示: 请确保 API 服务正在运行 (http://localhost:3001)
          </p>
        </div>
      )}

      <section style={{ marginTop: '2rem' }}>
        <h2>房间列表 ({data?.rooms.length || 0})</h2>
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
                {room.creator && (
                  <div>
                    <strong>创作者:</strong> {room.creator.name || room.creator.id}
                  </div>
                )}
              </li>
            ))}
          </ul>
        ) : (
          <p style={{ marginTop: '1rem', color: '#666' }}>暂无房间数据</p>
        )}
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>最近事件</h2>
        <div style={{ marginTop: '1rem' }}>
          <h3 style={{ fontSize: '1rem' }}>原始事件 ({data?.events.rawEvents.length || 0})</h3>
          {data?.events.rawEvents && data.events.rawEvents.length > 0 ? (
            <ul style={{ listStyle: 'none', marginTop: '0.5rem' }}>
              {data.events.rawEvents.slice(0, 10).map((event) => (
                <li
                  key={event.id}
                  style={{
                    padding: '0.75rem',
                    marginBottom: '0.5rem',
                    border: '1px solid #eee',
                    borderRadius: '4px',
                    fontSize: '0.9rem',
                  }}
                >
                  <div>
                    <strong>ID:</strong> {event.id.substring(0, 8)}... | <strong>平台:</strong>{' '}
                    {event.platform} | <strong>房间:</strong>{' '}
                    {event.room?.platformRoomId || event.id}
                  </div>
                  <div style={{ marginTop: '0.25rem', color: '#666', fontSize: '0.85rem' }}>
                    {new Date(event.receivedAt).toLocaleString('zh-CN')}
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p style={{ color: '#666' }}>暂无原始事件</p>
          )}
        </div>

        <div style={{ marginTop: '1rem' }}>
          <h3 style={{ fontSize: '1rem' }}>
            标准化事件 ({data?.events.normalizedEvents.length || 0})
          </h3>
          {data?.events.normalizedEvents && data.events.normalizedEvents.length > 0 ? (
            <ul style={{ listStyle: 'none', marginTop: '0.5rem' }}>
              {data.events.normalizedEvents.slice(0, 10).map((event) => (
                <li
                  key={event.id}
                  style={{
                    padding: '0.75rem',
                    marginBottom: '0.5rem',
                    border: '1px solid #eee',
                    borderRadius: '4px',
                    fontSize: '0.9rem',
                  }}
                >
                  <div>
                    <strong>类型:</strong> {event.eventType} | <strong>金额:</strong>{' '}
                    {event.amount || 0} | <strong>用户:</strong> {event.actorUid || 'N/A'}
                  </div>
                  <div style={{ marginTop: '0.25rem', color: '#666', fontSize: '0.85rem' }}>
                    {new Date(event.occurredAt).toLocaleString('zh-CN')} | 幂等键:{' '}
                    {event.idempotencyKey.substring(0, 16)}...
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p style={{ color: '#666' }}>暂无标准化事件</p>
          )}
        </div>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>最近流水 ({data?.ledger.length || 0})</h2>
        {data?.ledger && data.ledger.length > 0 ? (
          <ul style={{ listStyle: 'none', marginTop: '1rem' }}>
            {data.ledger.slice(0, 10).map((entry) => (
              <li
                key={entry.id}
                style={{
                  padding: '0.75rem',
                  marginBottom: '0.5rem',
                  border: '1px solid #eee',
                  borderRadius: '4px',
                  backgroundColor: entry.delta > 0 ? '#f0f9ff' : '#fefefe',
                }}
              >
                <div
                  style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
                >
                  <div>
                    <strong>原因:</strong> {entry.reason} | <strong>用户:</strong>{' '}
                    {entry.fanUid || 'N/A'}
                  </div>
                  <div
                    style={{
                      fontSize: '1.1rem',
                      fontWeight: 'bold',
                      color: entry.delta > 0 ? '#10b981' : '#ef4444',
                    }}
                  >
                    {entry.delta > 0 ? '+' : ''}
                    {entry.delta}
                  </div>
                </div>
                <div style={{ marginTop: '0.25rem', color: '#666', fontSize: '0.85rem' }}>
                  {new Date(entry.occurredAt).toLocaleString('zh-CN')} | 房间:{' '}
                  {entry.room?.platformRoomId || 'N/A'}
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p style={{ marginTop: '1rem', color: '#666' }}>暂无流水数据</p>
        )}
      </section>
    </main>
  );
}
