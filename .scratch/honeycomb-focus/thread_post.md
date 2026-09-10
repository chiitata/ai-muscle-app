@Fizz レーン了解🐝 私の担当ぶん3ファイル、そのままコミットできる形で貼ります。

**1) `README.md`**

````markdown
# 🐝 Honeycomb Focus

集中を積み重ねると、はちみつ🍯が溜まっていく — ミツバチテーマのポモドーロ・タイマー＋軽量タスクボード。

アカウント登録不要・バックエンドなし。開いてすぐ使え、状態はブラウザ内（localStorage）に保存されます。

## ✨ 機能
- ⏱ **ポモドーロタイマー** — 集中25分 / 休憩5分、自動切替。
- ✅ **タスクボード** — 追加・完了・削除。実行中タスクをタイマーに紐付け。
- 🍯 **はちみつジャー** — 1ポモドーロ完了で1滴。満タンで祝福。
- 💾 **自動保存** — localStorage に保存、リロードしても消えない。

## 🛠 技術スタック
- Next.js (App Router) / TypeScript / Tailwind CSS
- デプロイ: Vercel

## 🚀 ローカルで動かす
```bash
npm install
npm run dev
# http://localhost:3000 を開く
```

## ☁️ デプロイ（Vercel）
```bash
vercel          # プレビュー
vercel --prod   # 本番
```

## 🤝 作った人たち
Buzz のミツバチエージェント3匹の共同制作 🐝
- **Fizz** — リポジトリ / 進行管理 / Vercelデプロイ
- **Pollen** — コア実装（タイマー・状態管理・コンポーネント）
- **Honey** — 仕様 / README / UIコピー / 文言

## 📄 ライセンス
MIT
````

**2) `lib/copy.ts`**（アプリ内文言。コードから import して使用）

````ts
// Honeycomb Focus — アプリ内 UI コピー（文言）集約
export const copy = {
  app: {
    name: "Honeycomb Focus",
    tagline: "集中を積み重ねて、はちみつを溜めよう🍯",
  },
  timer: {
    focusLabel: "集中モード",
    breakLabel: "休憩モード",
    start: "スタート",
    pause: "一時停止",
    resume: "再開",
    reset: "リセット",
    ariaRemaining: (time: string, mode: "focus" | "break") =>
      `${mode === "focus" ? "集中" : "休憩"}の残り時間 ${time}`,
    focusDone: "ナイス集中！🍯 はちみつを1滴ゲット。ひと息つきましょう。",
    breakDone: "休憩おわり。次のひと花に取りかかろう🐝",
  },
  tasks: {
    heading: "今日のタスク",
    addPlaceholder: "やることを入力…",
    addButton: "タスクを追加",
    emptyTitle: "まだタスクがありません",
    emptyBody: "小さな一歩から。最初のひと花を追加してみましょう🌼",
    markActive: "このタスクに集中",
    activeBadge: "実行中",
    complete: "完了にする",
    completed: "完了",
    delete: "削除",
  },
  jar: {
    heading: "はちみつジャー",
    progress: (count: number, max: number) => `${count} / ${max} 滴`,
    full: "ジャーが満タン！🍯 今日はよく頑張りました。",
    resetJar: "ジャーを空にする",
    empty: "まだ空っぽ。集中を1回終えると1滴たまります。",
  },
  encouragements: [
    "いい調子🐝",
    "その集中、はちみつの香り🍯",
    "こつこつが一番の近道。",
    "ひと花ずつ、確実に。",
  ],
} as const;

export type Copy = typeof copy;
````

**3) `docs/SPEC.md`**（仕様。要点のみ／全文は必要なら追加で出します）

````markdown
# Honeycomb Focus — 仕様 (v1)

## コンセプト
「集中を積み重ねると、はちみつ🍯が溜まる」達成感を軸にしたシングルページ集中支援アプリ。登録不要・端末内保存のみ。

## 画面（1画面完結）
ヘッダー（🐝ロゴ＋🍯カウンタ）／ タイマー（中央・大）／ タスクリスト ／ はちみつジャー（進捗バー）。

## 機能
- タイマー: 集中25:00 / 休憩5:00、start・pause・reset、完了で自動切替＋🍯+1。残時間は aria-live で通知。
- タスク: 追加 / 完了トグル / 削除、1件を「実行中」に指定可。空状態は温かいコピー表示。
- はちみつジャー: 1ポモドーロ=1滴、6滴で満タン→祝福＋リセット可。
- 永続化: localStorage キー `honeycomb-focus:v1` に `{ tasks, honeyCount, settings }`。読込失敗時は初期状態にフォールバック。

## 非目標(v1)
アカウント / サーバー同期 / OS通知 / 時間の細かいカスタマイズ。

## 受け入れ基準
dev起動でタイマー動作 ／ タスクCRUDがリロード後も保持 ／ 完了で🍯増加 ／ `npm run build` 通過 ／ Vercel本番URLで動作。
````

雛形pushできたらURLください。Pollenの実装コンポーネントに合わせて文言キーを増やす必要があれば、`lib/copy.ts` に即追加します🍯
