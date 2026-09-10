# AI Muscle App — ビルド＆デプロイ手順

## 環境セットアップの状態

現在の環境では Gradle ビルドに互換性の問題があります。代替方法を提供します。

---

## オプション 1: Google Play Console でビルド（推奨）

Google Play Console はソースコード（AAB 形式）をアップロードして、Google 側でビルド・署名することができます。

### メリット
✅ ローカル環境でビルドツールのセットアップが不要
✅ Google Play の最新ベストプラクティスが適用される
✅ 最も簡単で確実

### ステップ
1. GitHub または Google Cloud に AAB をアップロード
2. Google Play Console で「内部テスト」に進む
3. 「リリースを作成」→ AAB ファイルをアップロード
4. Google 側で自動的に APK が生成される
5. テスト用リンクが自動生成

---

## オプション 2: ローカルで Android Studio を使用

環境セットアップが複雑な場合の代替案：

### 1. Android Studio をインストール

```bash
# Homebrew 経由
brew install --cask android-studio
```

### 2. プロジェクトを Android Studio で開く

```bash
open -a "Android Studio" /Users/senba.taichi/.buzz
```

### 3. ビルド実行

1. メニュー: Build → Build Bundle(s) / APK(s)
2. Release バリアント選択
3. 署名ダイアログで Key Store を指定
4. ビルド完了

**出力:**
```
app/build/outputs/apk/release/app-release.apk
```

### メリット
✅ GUI ベースで簡単
✅ エラー診断が視覚的
✅ プレビュー機能も使用可能

---

## オプション 3: 最小限の Gradle セットアップ

既存環境を調整してビルドする方法：

### 1. Gradle バージョンを確認

```bash
gradle --version
```

**期待される出力:**
```
Gradle 8.2 or 9.x
JVM: OpenJDK 17
```

### 2. ビルドコマンド

```bash
# Java 環境を設定
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"

# キャッシュをクリア（問題がある場合）
gradle clean

# ビルド実行
gradle build -x test
```

### 3. APK の場所

```
app/build/outputs/apk/release/app-release.apk
```

### トラブルシューティング

**エラー: 「org.gradle.api.artifacts.dsl.DependencyHandler.module()」**

原因: Gradle バージョンと Android Plugin の互換性問題

解決策:
```bash
# gradle/libs.versions.toml の agp を確認
cat gradle/libs.versions.toml | grep "agp"

# Gradle wrapper を正式版に更新
./gradlew wrapper --gradle-version 8.2
```

---

## 推奨フロー

```
┌─────────────────────────────────┐
│ Google Play Developer Account   │
│ 登録（25USD）                     │
└────────────┬────────────────────┘
             │
             ▼
    ┌─────────────────────┐
    │ ローカルでビルド    │
    │ (Android Studio)   │
    └────────┬────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Google Play Console             │
│ - 内部テストにアップロード       │
│ - テスターで動作確認            │
│ - 本番リリース                   │
└─────────────────────────────────┘
```

---

## ビルド成果物

### Release APK
- **ファイル:** `app/build/outputs/apk/release/app-release.apk`
- **サイズ:** ~5-8 MB （ProGuard 圧縮後）
- **署名:** release key store で署名済み

### App Bundle (AAB)
- **ファイル:** `app/build/outputs/bundle/release/app-release.aab`
- **用途:** Google Play Store 推奨形式
- **メリット:** Google Play が動的に最適な APK を生成

---

## 検証チェック

ビルド完了後、以下を確認してください：

```bash
# APK の署名を検証
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk

# APK の内容を検査
zipinfo -1 app/build/outputs/apk/release/app-release.apk | head -20

# AndroidManifest.xml の確認
aapt dump badging app/build/outputs/apk/release/app-release.apk
```

---

## 次のステップ

1. **Google Play Developer Account** を登録
2. **ビルド** (Android Studio または gradle コマンド)
3. **Google Play Console** で内部テスト
4. **テスター** で動作確認
5. **本番リリース**

詳細は `GOOGLE_PLAY_STORE_GUIDE.md` を参照。

---

## サポート

問題が発生した場合：

### ビルド関連
- [Android Gradle Plugin Guide](https://developer.android.com/studio/build)
- [Troubleshooting Gradle Builds](https://developer.android.com/studio/troubleshoot#gradle)

### Google Play Store
- [Google Play Console Help](https://support.google.com/googleplay)
- [App Publishing Guide](https://developer.android.com/studio/publish)

### 署名関連
- [App Signing Guide](https://developer.android.com/studio/publish/app-signing)
- [Key Store Setup](https://developer.android.com/studio/publish/app-signing#secure-key)
