import { LegalShell, h2Style, mutedStyle } from "@/lib/ui";
import { PLANS, PlanKey } from "@/lib/plans";

export const metadata = { title: "料金 | Askoo プレミアム" };

export default function Pricing() {
  const keys = Object.keys(PLANS) as PlanKey[];
  return (
    <LegalShell title="料金プラン">
      <p>
        Askoo プレミアムは自動更新サブスクリプションです。いずれのプランも、ご購読により
        1日あたりの質問回数の上限が大きく広がり、広告なしでご利用いただけます。
      </p>

      <div style={{ display: "grid", gap: 14, margin: "20px 0" }}>
        {keys.map((k) => (
          <div
            key={k}
            style={{
              border: "1px solid #d4dde4",
              borderRadius: 12,
              padding: 18,
              background: "#fff",
            }}
          >
            <div style={{ fontSize: 18, fontWeight: 700 }}>{PLANS[k].label}</div>
            <div style={{ fontSize: 22, fontWeight: 800, margin: "6px 0" }}>{PLANS[k].note}</div>
            <div style={mutedStyle}>
              {PLANS[k].interval === "month" ? "毎月" : "毎週"}自動更新 ／ 初回7日間は無料
            </div>
          </div>
        ))}
      </div>

      <h2 style={h2Style}>更新について</h2>
      <p>
        サブスクリプションは、現在の期間が終了する前に自動的に更新され、同額が請求されます。
        料金は購入手続きの確定時にお支払いいただきます。
      </p>

      <h2 style={h2Style}>解約について</h2>
      <p>
        次回の更新日より前に解約手続きを行うことで、自動更新を停止できます。解約後も、
        お支払い済みの期間が終了するまではプレミアム機能をご利用いただけます。解約方法は
        ご購入経路（当サイト＝Stripe カスタマーポータル／iOSアプリ＝App Store のサブスクリプション管理）に従います。
      </p>

      <h2 style={h2Style}>無料トライアル</h2>
      <p>
        初回7日間は無料でお試しいただけます。無料期間中に解約された場合、料金は発生しません。
        無料トライアル期間が残っていても、購読を開始した時点でその未使用分は無効となります。
      </p>

      <p style={{ marginTop: 24 }}>
        <a href="/" style={{ color: "#36a3e0" }}>購読手続きへ進む →</a>
      </p>
    </LegalShell>
  );
}
