// Premium プランの価格（ASC / アプリの .storekit と揃える）。JPY はゼロ小数通貨なので
// unit_amount はそのままの円額（×100 しない）。
export type PlanKey = "monthly" | "weekly";

export const PLANS: Record<PlanKey, {
  label: string;
  amount: number;
  interval: "month" | "week";
  note: string;
}> = {
  monthly: { label: "プレミアム（月額）", amount: 980, interval: "month", note: "¥980 / 月" },
  weekly: { label: "プレミアム（週額）", amount: 480, interval: "week", note: "¥480 / 週" },
};

// 買い切りプラン（PayPay 等、自動更新なし）。
// grant_days はwebhookが profiles.plan_expires_at の算出に使う（mac3 webhook と共有コントラクト）。
export type OneTimeKey = "once7" | "once30";
export const ONE_TIME_PLANS: Record<OneTimeKey, { label: string; amount: number; days: number; note: string }> = {
  once7:  { label: "プレミアム7日間",  amount: 480, days: 7,  note: "¥480 / 7日間（自動更新なし）" },
  once30: { label: "プレミアム30日間", amount: 980, days: 30, note: "¥980 / 30日間（自動更新なし）" },
};
