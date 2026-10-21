import type { Metadata } from "next";
import { SiteFooter } from "@/lib/ui";

export const metadata: Metadata = {
  title: "Askoo プレミアム",
  description: "Askoo（学生のための質問AIアプリ）のプレミアムをブラウザで購読する",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ja">
      <body
        style={{
          margin: 0,
          fontFamily:
            "-apple-system, BlinkMacSystemFont, 'Hiragino Sans', 'Noto Sans JP', sans-serif",
          background: "#eef6fb",
          color: "#1f2933",
          minHeight: "100vh",
          display: "flex",
          flexDirection: "column",
        }}
      >
        <div style={{ flex: 1 }}>{children}</div>
        <SiteFooter />
      </body>
    </html>
  );
}
