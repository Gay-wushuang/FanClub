import Link from 'next/link';

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <nav
        style={{
          padding: '1rem 2rem',
          borderBottom: '1px solid #ddd',
          marginBottom: '2rem',
        }}
      >
        <Link href="/" style={{ marginRight: '1rem' }}>
          首页
        </Link>
        <Link href="/dashboard">Dashboard</Link>
      </nav>
      {children}
    </>
  );
}
