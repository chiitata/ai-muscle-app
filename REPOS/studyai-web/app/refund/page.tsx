import { LegalShell, h2Style } from "@/lib/ui";

export const metadata = { title: "返金・キャンセルポリシー | Askoo" };

export default function Refund() {
  return (
    <LegalShell title="返金・キャンセルポリシー">
      <h2 style={h2Style}>解約（自動更新の停止）</h2>
      <p>
        サブスクリプションは、次回の更新日より前にいつでも解約できます。解約すると、次回以降の自動更新と
        請求が停止します。解約後も、すでにお支払い済みの期間が終了するまではプレミアム機能をご利用いただけます。
      </p>
      <ul>
        <li>
          <strong>当サイト（Web）でご購入の場合</strong>：Stripeのカスタマーポータル、または
          <a href="/contact" style={{ color: "#36a3e0" }}>お問い合わせ</a>から解約手続きができます。
        </li>
        <li>
          <strong>iOSアプリ内でご購入の場合</strong>：App Store の「サブスクリプション」設定から解約してください
          （Appleのポリシーが適用されます）。
        </li>
      </ul>

      <h2 style={h2Style}>返金について</h2>
      <p>
        デジタルサービスの性質上、提供開始後の返金は原則として承っておりません。ただし、以下の場合は
        個別に対応いたします。
      </p>
      <ul>
        <li>当方の重大な不具合によりサービスが利用できなかった場合。</li>
        <li>法令（消費者保護関連法令等）により返金が求められる場合。</li>
      </ul>
      <p>
        返金のご相談は<a href="/contact" style={{ color: "#36a3e0" }}>お問い合わせ</a>よりご連絡ください。
        なお、iOSアプリ内課金（App Store決済）の返金は、Appleの返金ポリシー・手続きに従います。
      </p>

      <h2 style={h2Style}>無料トライアル</h2>
      <p>
        初回7日間の無料トライアル期間中に解約された場合、料金は発生しません。
      </p>
    </LegalShell>
  );
}
