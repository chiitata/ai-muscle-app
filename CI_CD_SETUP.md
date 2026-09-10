# CI/CD パイプライン構築ガイド

このガイドでは、GitHub Actions + Google Cloud Build を使用した自動ビルドパイプラインを設定します。

## 概要

```
GitHub Push (main branch)
  ↓
GitHub Actions トリガー
  ↓
Google Cloud Build で自動ビルド
  ↓
APK/AAB 生成
  ↓
Cloud Storage へアップロード
  ↓
Buzz へ通知
```

## 前提条件

- GitHub リポジトリへのアクセス
- Google Cloud プロジェクト
- gcloud CLI がインストール済み

## セットアップ手順

### Step 1: Google Cloud プロジェクトの作成

```bash
gcloud projects create ai-muscle-app --name="AI Muscle App"
gcloud config set project ai-muscle-app
```

### Step 2: 必要な API を有効化

```bash
gcloud services enable cloudbuild.googleapis.com
gcloud services enable storage-api.googleapis.com
gcloud services enable cloudkms.googleapis.com
```

### Step 3: サービスアカウントを作成

```bash
# サービスアカウント作成
gcloud iam service-accounts create github-actions \
  --display-name="GitHub Actions Service Account"

# 必要な権限を付与
gcloud projects add-iam-policy-binding ai-muscle-app \
  --member="serviceAccount:github-actions@ai-muscle-app.iam.gserviceaccount.com" \
  --role="roles/cloudbuild.builds.editor"

gcloud projects add-iam-policy-binding ai-muscle-app \
  --member="serviceAccount:github-actions@ai-muscle-app.iam.gserviceaccount.com" \
  --role="roles/storage.admin"
```

### Step 4: サービスアカウントキーを生成

```bash
gcloud iam service-accounts keys create ./github-actions-key.json \
  --iam-account=github-actions@ai-muscle-app.iam.gserviceaccount.com
```

### Step 5: GitHub Secrets を設定

GitHub リポジトリ Settings → Secrets and variables → Actions → New repository secret

以下の環境変数を追加：

| Secret 名 | 値 |
|-----------|-----|
| `GCP_PROJECT_ID` | `ai-muscle-app` |
| `GCP_SA_KEY` | `github-actions-key.json` の内容（JSON 全体） |
| `BUZZ_TOKEN` | Buzz API トークン |
| `BUZZ_CHANNEL_ID` | Buzz チャンネル UUID |

**設定方法:**

1. GitHub リポジトリを開く
2. Settings → Secrets and variables → Actions
3. New repository secret をクリック
4. 名前と値を入力して Save

### Step 6: Cloud Storage バケットを作成

```bash
gsutil mb gs://ai-muscle-app-builds/
gsutil versioning set on gs://ai-muscle-app-builds/
```

### Step 7: Docker イメージの準備（オプション）

Cloud Build はデフォルトで Android イメージをサポートしていないため、カスタムイメージを使用します。

```bash
# Dockerfile を使用してカスタムイメージをビルド
docker build -t gcr.io/ai-muscle-app/android-builder .
docker push gcr.io/ai-muscle-app/android-builder
```

**Dockerfile サンプル:**

```dockerfile
FROM android:34

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    gradle \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
```

## 実行方法

### 自動実行（推奨）

main ブランチに push すると、自動的にパイプラインが実行されます。

```bash
git commit -am "feat: add new feature"
git push origin main
```

**GitHub Actions で実行状況を確認:**

1. リポジトリの Actions タブをクリック
2. "Cloud Build Pipeline" をクリック
3. 実行ログを確認

### 手動実行

```bash
gcloud builds submit \
  --config=cloudbuild.yaml \
  --project=ai-muscle-app
```

## ビルド成果物の確認

### Cloud Storage で確認

```bash
gsutil ls -r gs://ai-muscle-app-builds/
```

### ダウンロード

```bash
gsutil cp gs://ai-muscle-app-builds/app-release.apk ./
```

## 問題解決

### 1. ビルドが失敗する場合

```bash
# ビルドログを確認
gcloud builds log <BUILD_ID> --project=ai-muscle-app

# 最新のビルドログ
gcloud builds log $(gcloud builds list --project=ai-muscle-app --limit=1 --format='value(id)')
```

### 2. 権限エラー

```bash
# サービスアカウントの権限を確認
gcloud projects get-iam-policy ai-muscle-app \
  --flatten="bindings[].members" \
  --format='table(bindings.role)' \
  --filter="bindings.members:github-actions*"
```

### 3. Docker イメージの問題

```bash
# カスタムイメージをテスト
docker run -it gcr.io/ai-muscle-app/android-builder bash
```

## Cloud Build トリガー（代替方法）

cloudbuild.yaml で GitHub リポジトリを直接連携する場合：

1. [Google Cloud Console](https://console.cloud.google.com) を開く
2. Cloud Build → Triggers
3. Create Trigger をクリック
4. GitHub リポジトリを連携
5. main ブランチへの push でトリガー設定

## 本番環境への展開

ビルドが成功したら、Google Play Console へ自動アップロード：

```yaml
# cloudbuild.yaml の最後に追加
- name: 'gcr.io/cloud-builders/gke-deploy'
  args:
    - 'run'
    - '--'
    - 'upload-apk-to-play-store.sh'
```

**upload-apk-to-play-store.sh:**

```bash
#!/bin/bash

# Google Play API 認証
export PLAYSTORE_KEY=$GOOGLE_PLAY_API_KEY

# APK をアップロード
bundletool upload-apk \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --track=internal
```

## 環境変数リファレンス

| 変数 | 説明 |
|------|------|
| `GCP_PROJECT_ID` | Google Cloud プロジェクト ID |
| `GCP_SA_KEY` | サービスアカウント JSON キー |
| `BUZZ_TOKEN` | Buzz API トークン |
| `BUZZ_CHANNEL_ID` | Buzz チャンネル UUID |

## ビルド設定のカスタマイズ

cloudbuild.yaml を編集して、ビルドプロセスをカスタマイズできます：

### タイムアウト設定

```yaml
timeout: '3600s'  # 1 時間
```

### マシンタイプ変更

```yaml
options:
  machineType: 'N1_STANDARD_4'  # より高性能
```

### ステップの追加

```yaml
steps:
  - name: 'gcr.io/cloud-builders/gradle'
    args: ['build', '-x', 'test']
  - name: 'gcr.io/cloud-builders/docker'
    args: ['run', '--rm', '-v', '/workspace:/workspace', ...']
```

## セキュリティベストプラクティス

1. **サービスアカウントキー**: GitHub Secrets で管理、リポジトリにコミットしない
2. **API キー**: 環境変数として安全に保管
3. **バケット権限**: 最小限の権限で設定
4. **監査ログ**: Cloud Build のログを定期的に確認

## 参考資料

- [Google Cloud Build Documentation](https://cloud.google.com/build/docs)
- [GitHub Actions with Google Cloud](https://github.com/google-github-actions/setup-gcloud)
- [Android Gradle Plugin Release Notes](https://developer.android.com/studio/releases/gradle-plugin)

---

**最終更新**: 2026-09-10
