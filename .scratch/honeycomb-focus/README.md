# 🐝 Honeycomb Focus

集中を積み重ねると、はちみつ🍯が溜まっていく — ミツバチテーマのポモドーロ・タイマー＋軽量タスクボード。

アカウント登録不要・バックエンドなし。開いてすぐ使え、状態はブラウザ内（localStorage）に保存されます。

## ✨ 機能
- ⏱ **ポモドーロタイマー** — 集中25分 / 休憩5分、自動切替。
- ✅ **タスクボード** — 追加・完了・削除。実行中タスクをタイマーに紐付け。
- 🍯 **はちみつジャー** — 1ポモドーロ完了で1滴。満タンで祝福。
- 💾 **自動保存** — localStorage に保存、リロードしても消えない。

## 🛠 技術スタック
- [Next.js](https://nextjs.org/) (App Router)
- TypeScript
- Tailwind CSS
- デプロイ: [Vercel](https://vercel.com/)

## 🚀 ローカルで動かす
```bash
npm install
npm run dev
# http://localhost:3000 を開く
```

本番ビルドの確認:
```bash
npm run build
npm run start
```

## ☁️ デプロイ（Vercel）
```bash
npm i -g vercel   # 未インストールの場合
vercel            # プレビュー
vercel --prod     # 本番
```

## 📁 主なディレクトリ
```
app/                  Next.js App Router のエントリ
  page.tsx            メイン画面（1画面完結）
components/           タイマー・タスク・ジャー等のコンポーネント
lib/                  localStorage 永続化・タイマーロジック
```

## 🤝 作った人たち
Buzz のミツバチエージェント3匹の共同制作 🐝
- **Fizz** — リポジトリ / 進行管理 / Vercelデプロイ
- **Pollen** — コア実装（タイマー・状態管理・コンポーネント）
- **Honey** — 仕様 / README / UIコピー / 文言

## 📄 ライセンス
MIT
