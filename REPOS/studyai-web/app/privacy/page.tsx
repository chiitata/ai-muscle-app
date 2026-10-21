import { LegalShell, h2Style } from "@/lib/ui";
import { BUSINESS } from "@/lib/business";

export const metadata = { title: "プライバシーポリシー | Askoo" };

export default function Privacy() {
  return (
    <LegalShell title="プライバシーポリシー">
      <p>
        {BUSINESS.serviceName}（以下「本サービス」）は、お客様の個人情報・プライバシーを尊重し、適切に取り扱います。
        詳細な全文は以下の公式プライバシーポリシーをご確認ください。
      </p>
      <p>
        <a href={BUSINESS.externalPrivacyUrl} style={{ color: "#36a3e0" }} target="_blank" rel="noreferrer">
          プライバシーポリシー（全文）→ {BUSINESS.externalPrivacyUrl}
        </a>
      </p>

      <h2 style={h2Style}>取得する情報</h2>
      <ul>
        <li>アカウント情報（メールアドレス等のログインに必要な情報）。</li>
        <li>サブスクリプション・決済に関する情報（決済はStripeが処理し、カード番号は当方で保持しません）。</li>
        <li>サービス利用状況（質問回数など、機能提供・品質改善のための情報）。</li>
      </ul>

      <h2 style={h2Style}>学習コンテンツの取り扱い</h2>
      <p>
        アプリでの質問・回答・撮影画像は端末内に保存され、サーバーには残さない設計です（アプリの仕様に準じます）。
      </p>

      <h2 style={h2Style}>第三者提供</h2>
      <p>
        決済処理（Stripe）、認証・データ基盤（Supabase）、広告（アプリ内のGoogle AdMob）など、
        サービス提供に必要な範囲で外部サービスを利用します。法令に基づく場合を除き、お客様の同意なく
        個人情報を第三者に販売・提供することはありません。
      </p>

      <h2 style={h2Style}>お問い合わせ</h2>
      <p>
        個人情報の開示・訂正・削除のご請求や、本ポリシーに関するお問い合わせは
        <a href="/contact" style={{ color: "#36a3e0" }}>お問い合わせページ</a>よりご連絡ください。
        アプリ内からアカウント（および学習データ）の削除も可能です。
      </p>
    </LegalShell>
  );
}
