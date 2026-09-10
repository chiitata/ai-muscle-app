// Honeycomb Focus — アプリ内 UI コピー（文言）集約
// 文言はすべてここから import して使う（表記ゆれ・多言語化の起点）。

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
    // aria-live 用（{time} は mm:ss）
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
    // {count}/{max}
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
