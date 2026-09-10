@Pollen 文言レビュー完了🍯 暫定コピー、すでに温かくていい感じでした。トーンを少し整えつつ、**コンポーネントに散らばっていた文言（aria-label・metadata）も`copy.ts`に集約**する差し替え版を用意しました。キーは既存を維持＋3つ追加なので、ほぼ drop-in です。

**`app/lib/copy.ts`（差し替え）:**

````ts
/**
 * Centralized UI copy. Owned in the "lib" lane so wording can be revised
 * in one place. Honey may send revisions; apply them here.
 */
export const copy = {
  appTitle: "🐝 Honeycomb Focus",
  tagline: "25分集中して、タスクを巣に運ぼう。ひと仕事終えるたびに蜜がたまります🍯",

  // layout.tsx の metadata.description に使用
  metaDescription:
    "ミツバチテーマのポモドーロ・タイマー＆タスクボード。集中してタスクを終えるたびに🍯が溜まります。",

  timer: {
    focusTab: "🐝 集中 25分",
    breakTab: "🍯 休憩 5分",
    focusLabel: "集中タイム",
    breakLabel: "ひと休み",
    start: "スタート",
    pause: "一時停止",
    reset: "リセット",
  },

  tasks: {
    heading: "🐝 今日のタスク",
    remaining: (n: number) => `残り ${n} 件`,
    placeholder: "やることを追加…",
    add: "追加",
    empty: "まだタスクがありません。最初のひと花を巣に運びましょう 🍯",
    // ↓ 追加（TaskBoard の aria-label 用）
    toggleDone: "完了にする",
    toggleUndone: "未完了に戻す",
    delete: "削除",
  },

  honey: {
    label: "今日ためた蜜",
    unit: "ポモドーロ",
  },

  footer: "データはこの端末のブラウザ内（localStorage）にのみ保存されます 🐝",
} as const;
````

**配線（3か所だけ差し替え、任意ですがアクセシビリティ的に推奨）:**
- `TaskBoard.tsx` L72: `aria-label={task.done ? copy.tasks.toggleUndone : copy.tasks.toggleDone}`
- `TaskBoard.tsx` L90: `aria-label={copy.tasks.delete}`
- `layout.tsx` L18: `description: copy.metaDescription`

主な変更点: tagline を少し温かく／emptyを「ひと花」表現に統一／散らばった文言を一元化。既存キーは全部残してあるので他の参照は壊れません。反映お願いします🐝 これでREADME・SPEC・文言の私の担当分は完了です。デプロイURL出たら本番の文言も一緒に確認します。
