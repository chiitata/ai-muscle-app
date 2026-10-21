import { LegalShell, mutedStyle } from "@/lib/ui";
import { BUSINESS, PLACEHOLDER } from "@/lib/business";
import { PLANS } from "@/lib/plans";

export const metadata = { title: "特定商取引法に基づく表記 | Askoo" };

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  const isPlaceholder = typeof value === "string" && value === PLACEHOLDER;
  return (
    <tr>
      <th
        style={{
          textAlign: "left",
          verticalAlign: "top",
          padding: "12px 14px",
          width: 180,
          background: "#f3f8fb",
          border: "1px solid #dbe6ee",
          fontWeight: 700,
          fontSize: 14,
        }}
      >
        {label}
      </th>
      <td
        style={{
          padding: "12px 14px",
          border: "1px solid #dbe6ee",
          fontSize: 14,
          color: isPlaceholder ? "#c0392b" : "#1f2933",
        }}
      >
        {value}
      </td>
    </tr>
  );
}

export default function Tokushoho() {
  const price = `プレミアム週額 ${PLANS.weekly.note}／プレミアム月額 ${PLANS.monthly.note}（いずれも税込・初回7日間無料）`;
  return (
    <LegalShell title="特定商取引法に基づく表記">
      <p style={mutedStyle}>
        赤字の項目は公開前に事業者の実値へ差し替えてください（現在はプレースホルダです）。
      </p>
      <table style={{ borderCollapse: "collapse", width: "100%", marginTop: 12 }}>
        <tbody>
          <Row label="販売事業者" value={BUSINESS.operatorName} />
          <Row label="運営統括責任者" value={BUSINESS.operatorManager} />
          <Row
            label="所在地"
            value={
              BUSINESS.address === PLACEHOLDER
                ? "（要記入：所在地。個人事業主の場合、ご請求があれば遅滞なく開示する旨の記載も可）"
                : BUSINESS.address
            }
          />
          <Row
            label="電話番号"
            value={
              BUSINESS.phone === PLACEHOLDER
                ? "（要記入：電話番号。ご請求があれば遅滞なく開示する旨の記載も可）"
                : BUSINESS.phone
            }
          />
          <Row label="メールアドレス" value={BUSINESS.email} />
          <Row label="販売価格" value={price} />
          <Row label="商品代金以外の必要料金" value="なし（通信料はお客様のご負担となります）" />
          <Row label="支払方法" value="クレジットカード（Stripe による決済）／iOSアプリ内はApp Store決済" />
          <Row label="支払時期" value="購入手続きの確定時。以降は各期間の更新日に自動で請求されます。" />
          <Row label="サービスの提供時期" value="決済完了後、ただちにご利用いただけます。" />
          <Row
            label="返品・キャンセル"
            value="デジタルサービスの性質上、提供開始後の返金は原則承っておりません。次回更新日より前の解約で自動更新を停止できます（詳細は返金・キャンセルポリシーをご参照ください）。"
          />
          <Row label="動作環境" value="iOS（対応端末）／最新のWebブラウザ" />
        </tbody>
      </table>
    </LegalShell>
  );
}
