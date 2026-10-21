import React from "react";
import { BUSINESS } from "@/lib/business";

// 事業者サイト共通の体裁（サーバーコンポーネントで使えるプレーンな要素）。

export const pageWrap: React.CSSProperties = {
  maxWidth: 720,
  margin: "0 auto",
  padding: "40px 20px 72px",
  lineHeight: 1.8,
};

export const h1Style: React.CSSProperties = { fontSize: 26, fontWeight: 800, margin: "0 0 20px" };
export const h2Style: React.CSSProperties = { fontSize: 18, fontWeight: 700, margin: "28px 0 8px" };
export const mutedStyle: React.CSSProperties = { color: "#5a6b78", fontSize: 13 };

const footLink: React.CSSProperties = { color: "#36a3e0", textDecoration: "none", fontSize: 13 };

/** 全ページ共通フッター。法務ページ・問い合わせへの導線。 */
export function SiteFooter() {
  const links: [string, string][] = [
    ["/", "ホーム"],
    ["/pricing", "料金"],
    ["/terms", "利用規約"],
    ["/tokushoho", "特定商取引法に基づく表記"],
    ["/refund", "返金・キャンセル"],
    ["/privacy", "プライバシー"],
    ["/contact", "お問い合わせ"],
  ];
  return (
    <footer
      style={{
        borderTop: "1px solid #dbe6ee",
        marginTop: 40,
        padding: "20px 16px 32px",
        textAlign: "center",
      }}
    >
      <div style={{ display: "flex", flexWrap: "wrap", gap: 14, justifyContent: "center", marginBottom: 10 }}>
        {links.map(([href, label]) => (
          <a key={href} href={href} style={footLink}>
            {label}
          </a>
        ))}
      </div>
      <div style={{ color: "#8a99a6", fontSize: 12 }}>
        © {new Date().getFullYear()} {BUSINESS.serviceName} — {BUSINESS.tagline}
      </div>
    </footer>
  );
}

/** 法務／説明ページの共通枠（見出し＋本文＋フッター）。 */
export function LegalShell({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <>
      <main style={pageWrap}>
        <a href="/" style={{ ...footLink, display: "inline-block", marginBottom: 16 }}>
          ← {BUSINESS.serviceName} トップ
        </a>
        <h1 style={h1Style}>{title}</h1>
        {children}
        <p style={{ ...mutedStyle, marginTop: 32 }}>最終更新日: {BUSINESS.lastUpdated}</p>
      </main>
    </>
  );
}
