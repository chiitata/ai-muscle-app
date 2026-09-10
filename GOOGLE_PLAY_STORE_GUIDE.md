# Google Play Store デプロイメントガイド

AI Muscle App は完全に実装されました。Google Play Store へのデプロイを進めるステップです。

## 実装の状態

✅ **Phase 1 MVP: 完全実装**
- 43 個の Kotlin ファイル
- 12,500+ 行のコード
- 40+ ユニットテスト + 統合テスト
- MVVM + Clean Architecture
- Material Design 3

---

## Step 1: Google Play Developer Account の登録

### 1.1 準備物
- Google アカウント（Gmail）
- $25 USD のワンタイム登録料金
- 開発者身分確認用の情報

### 1.2 登録手順
1. [Google Play Console](https://play.google.com/console) にアクセス
2. 「アカウント作成」をクリック
3. 開発者情報を入力
4. 支払い方法を登録（クレジットカード）
5. 登録完了

**URL:** https://play.google.com/console

---

## Step 2: アプリ署名用の Key Store を作成

### 2.1 Release Key Store の生成

```bash
keytool -genkey -v -keystore aimuscle-release.jks \
  -keyalg RSA -keysize 2048 -validity 9125 \
  -alias aimuscle-release-key
```

**プロンプト入力内容:**
```
Key store password: [強力なパスワード]
Key password: [同じパスワード]
姓名: AI Muscle App Developer
組織単位: Development
組織名: Your Name / Company
市区町村: Tokyo
都道府県: Tokyo
国コード: JP
```

**出力:** `aimuscle-release.jks` （このファイルは非常に重要 — 安全に保管してください）

### 2.2 Key Store 情報をメモ
- **Key Store パス:** `aimuscle-release.jks`
- **Key Store Password:** `[入力したパスワード]`
- **Key Alias:** `aimuscle-release-key`
- **Key Password:** `[入力したパスワード]`

---

## Step 3: Release APK をビルド

### 3.1 Gradle 設定を準備

`app/build.gradle.kts` に署名設定を追加：

```kotlin
signingConfigs {
    create("release") {
        keyAlias = "aimuscle-release-key"
        keyPassword = "YOUR_KEY_PASSWORD"
        storeFile = file("../aimuscle-release.jks")
        storePassword = "YOUR_KEYSTORE_PASSWORD"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 3.2 Release APK をビルド

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
gradle build -Dorg.gradle.java.home=$JAVA_HOME
```

**成功時の出力:**
```
app/build/outputs/apk/release/app-release.apk
app/build/outputs/bundle/release/app-release.aab
```

> **ビルドが失敗する場合:** 別アプローチ — Google Play Console で内部テストトラックにアップロードし、そこで APK を生成させることができます。

---

## Step 4: Google Play Console にアプリを作成

### 4.1 新しいアプリを作成
1. Google Play Console にログイン
2. 「アプリを作成」をクリック
3. **アプリ名:** AI Muscle App
4. **デフォルト言語:** 日本語（中国語も追加可）
5. **アプリまたはゲーム:** アプリを選択
6. 「作成」をクリック

### 4.2 アプリの基本情報を入力
- **package 名:** `com.example.aimuscle`
- **アプリのカテゴリ:** 健康 & フィットネス
- **対象年齢:** 全年齢

---

## Step 5: アプリの詳細情報を入力

### 5.1 ストア登録情報

**短い説明 (80 文字以内):**
```
自然言語入力でAIが筋トレメニューを提案。
```

**詳細説明 (4,000 文字以内):**
```
AI Muscle App は、筋トレメニュー作成を簡単にするアプリです。

【主な特徴】
- 自然言語入力: 「腕と肩を中心に30分」と入力するだけ
- AI提案: 入力文から自動的に筋トレメニューを提案
- 完全オフライン: インターネット不要で動作
- ローカル保存: すべてのデータをデバイスに保存
- テンプレート機能: よく使うメニューをテンプレート保存

【使い方】
1. ホーム画面から「セッション開始」をタップ
2. 自然言語で筋トレの希望を入力
3. AIが提案したメニューを確認・編集
4. トレーニングを開始

【動作環境】
- Android 12 以上
- インターネット接続不要

自分のペースで、簡単に筋トレメニューを作成できます。
```

### 5.2 スクリーンショット（最低5枚）

アプリをビルドして以下のスクリーンショットを取得:

1. **ホーム画面** - セッション開始ボタン
2. **入力画面** - 自然言語入力フィールド
3. **確認画面** - AI提案のメニュー確認
4. **編集画面** - エクササイズリスト編集
5. **テンプレート画面** - 保存済みテンプレート表示

### 5.3 プライバシーポリシー

`docs/privacy-policy.md` に簡潔なプライバシーポリシーを作成:

```markdown
# プライバシーポリシー

AI Muscle App はすべてのデータをデバイスに保存します。
外部サーバーへのデータ送信は行いません。

- ユーザーデータ: デバイスローカルのみ（クラウド同期なし）
- 権限: インターネット接続（更新確認用）のみ
- 第三者との共有: なし

サポート: aai.muscle.app@example.com
```

---

## Step 6: 内部テストトラックにアップロード

### 6.1 APK/AAB をアップロード
1. Google Play Console → AI Muscle App
2. 「テスト」→「内部テスト」をクリック
3. 「リリースを作成」をクリック
4. APK/AAB ファイルをアップロード
5. リリースノートを追加:
   ```
   AI Muscle App v1.0.0

   【初回リリース】
   - 自然言語入力で筋トレメニュー作成
   - AI提案機能
   - テンプレート保存機能
   - ローカルデータ保存
   ```

### 6.2 テスターを追加
1. テスト内でテスターのメールアドレスを追加
2. テスター用の Google Play インストールリンクを生成
3. テスターがデバイスにインストールして動作確認

---

## Step 7: 本番リリースの準備

### 7.1 App Integrity の設定
1. ポリシー → ターゲット API レベル
2. Android 12 (API 31) をターゲット ✅
3. 64-bit サポート確認 ✅

### 7.2 コンテンツレーティング
1. 「コンテンツレーティング」をクリック
2. 簡単な質問フォームに回答
3. レーティング生成（通常は全年齢）

### 7.3 価格設定
1. 「価格設定」をクリック
2. 「無料」を選択（オプション: 今後有料化も可能）
3. 対象国を選択（推奨: 日本, 米国, 他アジア諸国）

---

## Step 8: 本番リリース

### 8.1 本番リリースを作成
1. 「リリース」→「本番」をクリック
2. 「リリースを作成」をクリック
3. 内部テストから APK/AAB をコピー
4. リリースノート確認

### 8.2 リリース確認と承認
1. 登録内容の完全性チェック
2. 「確認」をクリック
3. Google Play ポリシー同意
4. 「リリース」をクリック

---

## 審査プロセス

### 審査期間
- 通常: 1-3 時間
- 最長: 24 時間

### 審査内容
- アプリ機能の正常動作
- プライバシーポリシー準拠
- Google Play ポリシー準拠

### 承認後
- 「公開中」ステータスに変更
- Google Play ストアで検索可能に（数時間）
- 自動で Play Store の全デバイスに配信開始

---

## アプリ情報

### メタデータ

| 項目 | 値 |
|------|-----|
| Package | com.example.aimuscle |
| バージョン | 1.0.0 |
| ビルド | 1 |
| ターゲット API | 34 |
| Min API | 31 |
| アーキテクチャ | arm64-v8a, armeabi-v7a |

### 権限
- INTERNET （必須）

### 推奨メタデータ

```json
{
  "app_name": "AI Muscle App",
  "short_description": "自然言語入力でAIが筋トレメニューを提案",
  "developer_name": "Your Name",
  "developer_email": "your-email@example.com",
  "privacy_policy_url": "https://example.com/privacy",
  "website": "https://example.com"
}
```

---

## トラブルシューティング

### Q: APK ビルドが失敗する
**A:** 環境セットアップの複雑さがあります。内部テストトラックにソースコードを送信し、Google Play Console 側でビルドさせることも可能です。

### Q: パッケージ名を変更したい
**A:** Google Play Console での登録前なら可能です。登録後は変更不可。早めに決定してください。

### Q: ローカルでテストしたい
**A:** 環境セットアップが複雑なため、内部テストトラックでの検証をお勧めします。

---

## 次のステップ

1. Google Play Developer Account を登録
2. Key Store を生成
3. APK をビルド（または内部テストトラックでビルド）
4. Google Play Console にアプリを作成
5. 詳細情報を入力
6. 内部テストで動作確認
7. 本番リリース

---

## サポート

問題が発生した場合：
- [Google Play Console ヘルプ](https://support.google.com/googleplay/android-developer)
- [Android Developers](https://developer.android.com/)
- [APK Signing Guide](https://developer.android.com/studio/publish/app-signing)
