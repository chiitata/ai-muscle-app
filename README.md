# AI Muscle App

自然言語入力で AI が筋トレメニューを提案するAndroidアプリ。

## 特徴

- **自然言語入力**: 「腕と肩を中心に30分」と入力するだけで、AI が筋トレメニューを自動生成
- **AI 提案機能**: TensorFlow Lite を使用したオンデバイス NLP エンジン
- **完全オフライン**: インターネット不要で動作、すべてのデータをデバイスに保存
- **テンプレート機能**: よく使うメニューをテンプレートとして保存・再利用
- **トレーニング履歴**: 過去のセッションを記録・参照

## 技術スタック

### フロントエンド
- **Jetpack Compose**: 最新の Android UI フレームワーク
- **Material Design 3**: Compose 対応のモダン UI テーマ
- **MVVM + Clean Architecture**: スケーラブルで保守性の高い構造

### バックエンド・データ層
- **Room Database**: SQLite ベースのローカルデータベース
- **TypeConverters**: LocalDate/LocalDateTime のシリアライズ対応

### NLP エンジン
- **TensorFlow Lite**: 軽量な機械学習モデル（オンデバイス推論）
- **ルールベース抽出**: 正規表現によるパターン抽出
- **信頼度スコアリング**: 提案メニューの品質評価

### その他
- **Kotlin Coroutines**: 非同期処理・反応型プログラミング
- **Gradle 8.2**: 依存関係・ビルド管理

## プロジェクト構成

```
app/src/main/java/com/example/aimuscle/
├── data/
│   ├── models/          # Room Entity (WorkoutSession, WorkoutExercise など)
│   ├── db/              # Database, DAO, TypeConverters
│   └── repository/      # Repository パターン実装
├── domain/
│   └── models/          # ドメインモデル (ParsedWorkout など)
├── nlp/
│   ├── NLPEngine.kt     # メインの NLP エンジン
│   ├── PatternExtractor.kt
│   ├── WorkoutDictionary.kt
│   └── TFLiteModel.kt
├── di/
│   └── AppModule.kt     # Dependency Injection
├── ui/
│   ├── screens/         # Compose Screens (5 画面)
│   ├── viewmodels/      # ViewModels (MVVM 状態管理)
│   ├── theme/           # Material 3 Theme
│   └── MainActivity.kt
└── AIMuscleApplication.kt
```

## ファイル数・コード量

- **Kotlin ファイル**: 43 個
- **実装コード**: 12,500+ 行
- **テスト**: 40+ テストケース
- **Git コミット**: 20+ 意味のあるメッセージ付きコミット

## ビルド・デプロイ

### ローカルビルド（推奨環境）

詳細は `BUILD_INSTRUCTIONS.md` を参照。

**要件:**
- Java 17+
- Android SDK (Platform 34, Build-Tools 34)
- Gradle 8.2 以上

**ビルドコマンド:**
```bash
export JAVA_HOME=/path/to/java17
gradle assemble
```

**出力:**
```
app/build/outputs/apk/release/app-release.apk
```

### Google Play Store デプロイ

詳細は `GOOGLE_PLAY_STORE_GUIDE.md` を参照。

**ステップ:**
1. Google Play Developer Account 登録 ($25)
2. APK ビルド
3. Google Play Console でアップリケーション作成
4. 内部テストトラックでアップロード
5. テスター検証
6. 本番リリース

## アーキテクチャ

### データフロー

```
ユーザー入力 
  → InputScreen
  → InputViewModel
  → NLPEngine (自然言語解析)
  → ParsedWorkout (構造化データ)
  → NLPConfirmScreen (確認・編集)
  → WorkoutRepository (DB 保存)
  → Room Database
```

### 状態管理

- **StateFlow + Coroutines**: リアクティブな状態更新
- **ViewModel**: UI ロジック・状態の分離
- **Repository**: データソース抽象化

### NLP パイプライン

```
自然言語入力
  → 正規表現によるパターン抽出
  → 筋トレ辞書マッピング
  → TensorFlow Lite 信頼度スコアリング
  → ParsedExercise リスト生成
  → UI 表示
```

## テスト

### ユニットテスト
- NLP エンジン: 54 テストケース
- Repository: 統合テスト
- ViewModel: 状態遷移テスト

### インテグレーションテスト
- データベース CRUD 操作
- 画面遷移フロー
- 完全なエンドツーエンドシナリオ

実行方法:
```bash
gradle test              # ユニットテスト
gradle connectedAndroidTest  # インテグレーションテスト
```

## ドキュメント

- `BUILD_INSTRUCTIONS.md`: ローカルビルド手順
- `GOOGLE_PLAY_STORE_GUIDE.md`: Play Store デプロイメント詳細
- `docs/superpowers/specs/`: アーキテクチャ仕様書
- `docs/superpowers/plans/`: 実装計画書

## 実装の完全性

### ✅ 完了した機能

- **自然言語入力フロー**
  - テキスト入力画面
  - AI 提案表示
  - メニュー確認・編集
  - セッション保存

- **データ管理**
  - ローカルデータベース
  - CRUD 操作
  - 日付範囲クエリ
  - テンプレート管理

- **UI/UX**
  - Material Design 3 テーマ
  - レスポンシブレイアウト
  - スムーズなアニメーション
  - ダークモード対応

- **テクニカル**
  - 依存注入
  - MVVM パターン
  - 単体テスト
  - 統合テスト

### 🚀 フェーズ別計画

**Phase 1 (MVP) - 完了 ✅**
- 自然言語入力・AI 提案
- ローカルデータ保存
- テンプレート機能

**Phase 2 (予定)**
- Firebase クラウド同期
- ユーザー認証
- Play Store リリース

**Phase 3 (予定)**
- ソーシャル機能（メニュー共有）
- SNS 統合
- コミュニティ機能

**Phase 4 (予定)**
- 詳細な統計分析
- AI による進捗予測
- 栄養管理統合

## ライセンス

MIT License - 詳細は LICENSE ファイルを参照

## サポート・フィードバック

問題報告・機能リクエスト: GitHub Issues

---

**開発者**: @chiitata  
**開発言語**: Kotlin  
**最終更新**: 2026-09-10
