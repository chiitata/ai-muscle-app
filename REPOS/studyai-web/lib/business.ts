// 事業者サイト（特定商取引法に基づく表記・問い合わせ・各ポリシー）で使う事業者情報。
//
// ⚠️ 「要記入」の項目は、必ず taichi 確認の実値に差し替えること。
//    プレースホルダ（PLACEHOLDER）が残ったまま本番公開しない。
//    Stripe の本番有効化審査では、ここが実在の事業者情報であることが前提になる。

export const PLACEHOLDER = "（要記入）";

export const BUSINESS = {
  /** サービス名（表示用）。 */
  serviceName: "Askoo（アスク）",
  /** サービスの短い説明。トップ／各ページ共通で使う。 */
  tagline: "学生のための質問AIアプリ",

  // --- 以下 ★要taichi記入★（特商法表記で必須）---
  /** 販売事業者名（個人事業主なら氏名、屋号があれば屋号＋氏名）。 */
  operatorName: "仙波泰治",
  /** 運営統括責任者（氏名）。 */
  operatorManager: "仙波泰治",
  /** 所在地。個人の場合、請求があれば遅滞なく開示する旨の運用も可。 */
  address: "ご請求をいただいた場合、遅滞なく開示いたします。",
  /** 電話番号。同上（請求時遅滞なく開示の運用も可）。 */
  phone: "080-2455-6752",
  /** 問い合わせメールアドレス（例: support@example.com）。 */
  email: "chiitata3326@gmail.com",
  // --- ここまで要記入 ---

  /** 既存の公開ポリシーURL（流用）。 */
  supportUrl: "https://chiitata.github.io/askoo-site/support.html",
  externalPrivacyUrl: "https://chiitata.github.io/askoo-site/privacy.html",
  /** Apple 標準 EULA（アプリ内課金で使用しているもの）。 */
  appleEulaUrl: "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/",

  /** 各ポリシーの最終更新日。 */
  lastUpdated: "2026-10-18",
};

/** 特商法表記で「請求に応じて遅滞なく開示します」と出したい場合のヘルパ表示。 */
export function discloseOnRequest(value: string): string {
  return value === PLACEHOLDER
    ? "ご請求いただければ遅滞なく開示いたします。"
    : value;
}
