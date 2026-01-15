import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'FanClub',
  description: '多房间多主播 open-live 事件系统',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
