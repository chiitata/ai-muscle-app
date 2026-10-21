import { LegalShell, h2Style, mutedStyle } from "@/lib/ui";
import { BUSINESS, PLACEHOLDER } from "@/lib/business";

export const metadata = { title: "お問い合わせ | Askoo" };

export default function Contact() {
  const emailSet = BUSINESS.email !== PLACEHOLDER;
  return (
    <LegalShell title="お問い合わせ">
      <p>
        {BUSINESS.serviceName} に関するご質問・ご要望・不具合のご報告、サブスクリプションの解約・返金の
        ご相談は、以下の窓口までご連絡ください。
      </p>

      <h2 style={h2Style}>連絡先</h2>
      <ul>
        <li>
          メール：{" "}
          {emailSet ? (
            <a href={`mailto:${BUSINESS.email}`} style={{ color: "#36a3e0" }}>{BUSINESS.email}</a>
          ) : (
            <span style={{ color: "#c0392b" }}>（要記入：問い合わせ用メールアドレス）</span>
          )}
        </li>
        <li>
          サポートページ：{" "}
          <a href={BUSINESS.supportUrl} style={{ color: "#36a3e0" }} target="_blank" rel="noreferrer">
            {BUSINESS.supportUrl}
          </a>
        </li>
      </ul>

      <p style={mutedStyle}>
        事業者情報の詳細は<a href="/tokushoho" style={{ color: "#36a3e0" }}>特定商取引法に基づく表記</a>をご覧ください。
      </p>
    </LegalShell>
  );
}
