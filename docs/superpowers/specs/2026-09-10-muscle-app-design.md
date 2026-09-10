# 筋トレアプリ設計仕様書（Design Spec）

**プロジェクト名**: AI Muscle App  
**作成日**: 2026-09-10  
**バージョン**: 1.0  
**ステータス**: ブレインストーミング完了、設計承認待ち

---

## 1. プロジェクト概要

個人ユーザー向けのAndroid筋トレ記録アプリ。自然言語入力で筋トレメニューをAIが自動解析し、手軽にメニューを記録・管理・再利用できる。

### 主な特徴
- **オフラインファースト**: インターネット不要、ジム内で使用可能
- **自然言語対応**: 「ベンチプレス 10回 60kg」などをまとめて入力
- **テンプレート管理**: メニューを保存・再利用
- **進捗トラッキング**: 履歴・統計で成長を可視化
- **無料インフラ**: クラウド呼び出し最小限、ローカルデータ保存

### ユーザー属性
個人の筋トレ実践者。週3〜5回程度のワークアウト管理を想定。

---

## 2. 要件定義

### 機能要件
- ✅ 複数の筋トレメニュー保存・管理
- ✅ 日本語自然言語入力（「ベンチプレス 10回」形式）
- ✅ NLP自動解析（種目・回数・重量の抽出）
- ✅ メニューテンプレート化と再利用
- ✅ セッション履歴管理（日付、種目、回数、重量）
- ✅ 簡易統計（最高重量、総セッション数など）

### 非機能要件
- **オフライン対応**: 必須。ジム内で接続できない環境を想定
- **パフォーマンス**: NLP解析は2秒以内に完了
- **データ永続化**: デバイスローカルDB（Room/SQLite）
- **将来拡張**: Google Drive/Firebaseへのバックアップ機能（v2以降）

---

## 3. 技術スタック

### フロントエンド（Android）
| コンポーネント | 選定技術 | 理由 |
|---|---|---|
| **言語** | Kotlin | 型安全、Android標準 |
| **UIフレームワーク** | Jetpack Compose | モダン、保守性高い |
| **ローカルDB** | Room (SQLite) | Jetpack統合、効率的 |
| **NLP処理** | TensorFlow Lite + ルールベース | 軽量、オフライン対応 |

### 自然言語処理（NLP）戦略
1. **ルールベース解析**: 正規表現 + 辞書で日本語を解析
   - 入力例: 「ベンチプレス 10回 60kg」
   - 出力: `{種目: "ベンチプレス", 回数: 10, 重量: 60}`

2. **TensorFlow Lite**: 複雑な日本語表現への対応（拡張用）
   - 初版はルールベース、後に軽量MLモデル追加

### バックアップ・同期（将来実装）
- Google Drive API（無料枠 100GB）
- Firebase Realtime DB（Spark プラン無料）
- 初版はローカル保存のみ

---

## 4. データベース設計

### テーブル構成（Room ORM）

#### 4.1 WorkoutSession（筋トレセッション）
```sql
CREATE TABLE workout_session (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  date DATE NOT NULL,
  menu_name TEXT NOT NULL,              -- 例: "月曜の上半身"
  notes TEXT,                            -- 「疲労感あり」など
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

-- インデックス
CREATE INDEX idx_workout_session_date ON workout_session(date);
```

#### 4.2 WorkoutExercise（各運動の詳細）
```sql
CREATE TABLE workout_exercise (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  session_id INTEGER NOT NULL,          -- FK → workout_session
  name TEXT NOT NULL,                   -- 「ベンチプレス」
  sets INTEGER NOT NULL DEFAULT 1,
  reps INTEGER NOT NULL,                -- 回数
  weight REAL,                          -- kg（オプション）
  notes TEXT,
  order_index INTEGER NOT NULL,         -- 表示順
  created_at DATETIME NOT NULL,
  FOREIGN KEY (session_id) REFERENCES workout_session(id)
);

-- インデックス
CREATE INDEX idx_workout_exercise_session ON workout_exercise(session_id);
```

#### 4.3 ExerciseTemplate（テンプレート・再利用メニュー）
```sql
CREATE TABLE exercise_template (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  template_name TEXT NOT NULL,          -- 「月曜の上半身」
  description TEXT,                     -- テンプレートの説明
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);
```

#### 4.4 TemplateExercise（テンプレート内の運動）
```sql
CREATE TABLE template_exercise (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  template_id INTEGER NOT NULL,         -- FK → exercise_template
  name TEXT NOT NULL,
  sets INTEGER NOT NULL DEFAULT 1,
  reps INTEGER NOT NULL,
  weight REAL,
  notes TEXT,
  order_index INTEGER NOT NULL,
  created_at DATETIME NOT NULL,
  FOREIGN KEY (template_id) REFERENCES exercise_template(id)
);

-- インデックス
CREATE INDEX idx_template_exercise_template ON template_exercise(template_id);
```

### データフロー
1. ユーザー入力（日本語）→ NLP解析
2. 解析結果 → WorkoutExercise 保存
3. セッション完成 → 「テンプレート化」オプション
4. テンプレート化 → ExerciseTemplate + TemplateExercise 作成
5. 次回ジム → テンプレートから復元

---

## 5. UI/UX フロー

### 画面構成（5画面）

#### 5.1 ホーム画面
- **レイアウト**:
  - 上部: 「今日のセッション開始」ボタン（フローティング）
  - 中央: テンプレート一覧（クイック選択）
  - 下部: 過去7日の履歴ウィジェット
  
- **操作**:
  - テンプレートタップ → NLP入力画面へ
  - 「新規開始」ボタン → 入力画面へ

#### 5.2 自然言語入力画面
- **UI要素**:
  - 複数行テキストフィールド
  - 例文表示: 「ベンチプレス 10回 60kg、ダンベル 12回 30kg」
  - 「解析」ボタン

- **入力例**:
  ```
  ベンチプレス 10回 60kg
  ダンベルプレス 12回 20kg
  バーベルロウ 8回 80kg
  ```

#### 5.3 NLP確認・編集画面
- **自動抽出結果を表示**:
  ```
  [ ] ベンチプレス      | 1set | 10回 | 60kg | [削除]
  [ ] ダンベルプレス    | 1set | 12回 | 20kg | [削除]
  [ ] バーベルロウ      | 1set | 8回  | 80kg | [削除]
  ```

- **操作**:
  - 各行をタップして手動編集（NLP解析ミス時の修正）
  - 「運動を追加」ボタンで新しい行を追加
  - 「保存」ボタン → WorkoutSession に記録

#### 5.4 テンプレート管理画面
- **機能**:
  - 既存テンプレート一覧表示
  - 長押し → 編集・削除メニュー
  - 新規テンプレート保存オプション（確認画面で表示）

#### 5.5 履歴・統計画面
- **コンテンツ**:
  - 過去セッション一覧（日付でソート）
  - 簡易統計グラフ
    - 「過去30日のセッション数」
    - 「ベンチプレス最高重量」など種目別統計

### UX設計の工夫（「全部自然言語だと面倒」対策）
| 課題 | 対策 |
|---|---|
| メニュー入力が面倒 | テンプレート選択でクイックスタート |
| 毎回入力は冗長 | テンプレートの再利用機能 |
| AI精度への不安 | NLP確認・編集画面で手動修正可能 |
| 自然言語だけ面倒 | テンプレート選択 + 一括入力のハイブリッド |

---

## 6. 実装戦略

### Phase 1: MVP（最小実行可能製品）
- ✅ ホーム画面 + テンプレート選択
- ✅ 自然言語入力 + NLP確認・編集
- ✅ ローカルDB（Room）保存
- ✅ 履歴表示

**予想期間**: 2-3週間

### Phase 2: 拡張機能
- ✅ テンプレート管理画面
- ✅ 統計・グラフ表示
- ✅ Google Play登録準備

**予想期間**: 1-2週間

### Phase 3: クラウド連携（v2以降）
- Google Drive/Firebaseバックアップ
- クラウド同期

**予想期間**: 後続スプリント

---

## 7. マイルストーン

| マイルストーン | 内容 | 期限目安 |
|---|---|---|
| M1: 設計承認 | 設計仕様書レビュー完了 | 2026-09-10 |
| M2: MVP開発完了 | ホーム + 入力 + 編集画面 完成 | 2026-09-24 |
| M3: テスト・QA | ローカルテスト、エッジケース確認 | 2026-10-01 |
| M4: Google Play登録 | 開発者登録、ストア申請 | 2026-10-08 |
| M5: v1.0 リリース | 公式リリース | 2026-10-15 |

---

## 8. リスク・制約事項

### 技術リスク
| リスク | 対策 |
|---|---|
| NLP精度不足 | ルールベース + ユーザー編集で補完 |
| TensorFlow Lite モデルサイズ | 軽量モデル選定、圧縮 |
| オフラインデータ同期 | 初版はローカル保存のみ、v2で対応 |

### スケジュール制約
- Google Play申請には5-7営業日の審査時間を要す
- 開発者登録には事前準備（身分証明、支払い情報）が必要

---

## 9. 承認ステータス

| 項目 | 承認者 | ステータス | 日時 |
|---|---|---|---|
| **技術スタック** | Fizz, Pollen | ✅ 承認 | 2026-09-10 12:51 |
| **DB設計** | Fizz, Pollen | ✅ 承認 | 2026-09-10 12:51 |
| **UI/UX フロー** | Fizz, Pollen | ✅ 承認 | 2026-09-10 12:53 |
| **全体設計** | taichi | ⏳ 待ち中 | - |

---

## 10. 次のステップ

1. **taichi によるレビュー**: この設計仕様書をレビュー（承認 or 修正要望）
2. **writing-plans スキル**: 詳細実装計画の作成
3. **開発開始**: Phase 1 MVP の実装着手

---

## 参考・リンク

- TensorFlow Lite: https://www.tensorflow.org/lite
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Room (SQLite): https://developer.android.com/training/data-storage/room
- 正規表現（日本語）: https://www.rexegg.com/

---

**設計作成**: Honey (Brainstorming Agent)  
**レビュー**: Fizz, Pollen  
**承認待ち**: taichi (User)
