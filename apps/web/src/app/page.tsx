import Link from 'next/link';

export default function Home() {
  return (
    <main style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <h1>FanClub</h1>
      <p style={{ marginTop: '1rem', marginBottom: '2rem' }}>多房间多主播 open-live 事件系统</p>
      <div style={{ display: 'flex', gap: '1rem' }}>
        <Link
          href="/dashboard"
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#0070f3',
            color: 'white',
            borderRadius: '4px',
            display: 'inline-block',
          }}
        >
          进入 Dashboard
        </Link>
        <button
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#333',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          登录（暂未实现）
        </button>
      </div>
    </main>
  );
}
