# AI Muscle App 実装計画

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Androidアプリで、自然言語入力から筋トレメニューをAIが自動解析し、オフラインで記録・管理できるアプリを構築する

**Architecture:** Kotlin + Jetpack Compose フロントエンド + Room ローカルDB + TensorFlow Lite NLP処理。オフラインファースト設計。ユーザーは日本語をまとめて入力 → NLP自動解析 → 確認・編集 → 保存という流れで効率的にメニューを記録。

**Tech Stack:**
- **言語**: Kotlin
- **UI**: Jetpack Compose
- **DB**: Room (SQLite)
- **NLP**: TensorFlow Lite + ルールベース正規表現解析
- **ビルド**: Gradle

**Spec:** `docs/superpowers/specs/2026-09-10-muscle-app-design.md`

## Global Constraints

- **プラットフォーム**: Android 8.0 以上（API Level 26+）
- **オフライン対応**: 必須。インターネット不要
- **NLP解析レイテンシ**: 2秒以内に完了
- **初版スコープ**: MVP（ホーム画面、入力、NLP確認・編集、DB保存）
- **データ永続化**: Room + SQLite のみ（Google Drive/Firebase は v2以降）
- **命名規則**: camelCase (Kotlin, Compose), snake_case (DB テーブル/カラム)

---

## ファイル構造

```
app/
├── src/main/kotlin/com/muscleapp/
│   ├── MainActivity.kt                     # アプリエントリーポイント
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── HomeScreen.kt               # ホーム画面
│   │   │   ├── WorkoutInputScreen.kt       # 自然言語入力画面
│   │   │   ├── WorkoutReviewScreen.kt      # NLP確認・編集画面
│   │   │   └── TemplateSelectionScreen.kt  # テンプレート選択画面
│   │   ├── components/
│   │   │   ├── ExerciseCard.kt             # 運動カード UI コンポーネント
│   │   │   ├── ExerciseInputField.kt       # 運動入力フィールド
│   │   │   └── FloatingActionButtons.kt    # FAB群
│   │   ├── theme/
│   │   │   └── Theme.kt                    # Compose theme 定義
│   │   └── navigation/
│   │       └── NavigationGraph.kt          # 画面遷移グラフ
│   ├── viewmodel/
│   │   ├── WorkoutSessionViewModel.kt      # セッション管理 ViewModel
│   │   ├── TemplateViewModel.kt            # テンプレート管理 ViewModel
│   │   └── WorkoutInputViewModel.kt        # 入力・NLP ViewModel
│   ├── data/
│   │   ├── database/
│   │   │   ├── AppDatabase.kt              # Room Database 定義
│   │   │   ├── dao/
│   │   │   │   ├── WorkoutSessionDao.kt
│   │   │   │   ├── WorkoutExerciseDao.kt
│   │   │   │   ├── ExerciseTemplateDao.kt
│   │   │   │   └── TemplateExerciseDao.kt
│   │   │   └── entity/
│   │   │       ├── WorkoutSessionEntity.kt
│   │   │       ├── WorkoutExerciseEntity.kt
│   │   │       ├── ExerciseTemplateEntity.kt
│   │   │       └── TemplateExerciseEntity.kt
│   │   ├── repository/
│   │   │   ├── WorkoutRepository.kt        # Workout ビジネスロジック
│   │   │   └── TemplateRepository.kt       # Template ビジネスロジック
│   │   └── model/
│   │       ├── WorkoutSession.kt           # ドメインモデル
│   │       ├── WorkoutExercise.kt
│   │       ├── ExerciseTemplate.kt
│   │       └── ExerciseParsingResult.kt    # NLP 結果モデル
│   └── nlp/
│       ├── ExerciseParser.kt               # ルールベース NLP 解析
│       ├── JapaneseTokenizer.kt            # 日本語トークナイザー
│       ├── ExerciseKnowledgeBase.kt        # 筋トレ種目辞書
│       └── TensorFlowLiteModel.kt          # TensorFlow Lite 統合（拡張用）
├── src/test/kotlin/com/muscleapp/
│   ├── nlp/
│   │   ├── ExerciseParserTest.kt           # NLP 単体テスト
│   │   └── JapaneseTokenizerTest.kt
│   └── data/
│       ├── repository/
│       │   └── WorkoutRepositoryTest.kt    # Repository テスト
│       └── dao/
│           └── WorkoutSessionDaoTest.kt    # DAO テスト
└── build.gradle.kts                        # 依存関係・ビルド設定
```

---

## Task 1: プロジェクト初期化と依存関係設定

**Files:**
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `app/build.gradle.kts`
- Create: `AndroidManifest.xml`

**Interfaces:**
- Produces: Gradle ビルドシステム、依存関係解決

**Steps:**

- [ ] **Step 1: Android Studio で新規プロジェクト作成**

プロジェクト名: `MuscleApp`
Package: `com.muscleapp`
最小 API Level: 26 (Android 8.0)
テンプレート: Empty Compose Activity

- [ ] **Step 2: `app/build.gradle.kts` に依存関係を追加**

```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "com.muscleapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.muscleapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Hilt DI (optional, for later)
    // implementation("com.google.dagger:hilt-android:2.48")
    // kapt("com.google.dagger:hilt-compiler:2.48")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
```

- [ ] **Step 3: `AndroidManifest.xml` にパーミッション追加**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- ローカルストレージアクセス（オプション） -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        ...
    >
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MuscleApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 4: `MainActivity.kt` 作成（エントリーポイント）**

```kotlin
package com.muscleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.muscleapp.ui.screens.HomeScreen
import com.muscleapp.ui.theme.MuscleAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuscleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}
```

- [ ] **Step 5: ビルドして初回実行確認**

```bash
./gradlew build
# Android Studio でエミュレータ/デバイスで実行
```

---

## Task 2: Room Database エンティティ＆DAO 作成

**Files:**
- Create: `entity/WorkoutSessionEntity.kt`
- Create: `entity/WorkoutExerciseEntity.kt`
- Create: `entity/ExerciseTemplateEntity.kt`
- Create: `entity/TemplateExerciseEntity.kt`
- Create: `dao/WorkoutSessionDao.kt`
- Create: `dao/WorkoutExerciseDao.kt`
- Create: `dao/ExerciseTemplateDao.kt`
- Create: `dao/TemplateExerciseDao.kt`
- Create: `database/AppDatabase.kt`

**Interfaces:**
- Produces: Room Database entities, DAOs, Database instance

**Steps:**

- [ ] **Step 1: `WorkoutSessionEntity.kt` 作成**

```kotlin
package com.muscleapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "workout_session")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val menuName: String,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

- [ ] **Step 2: `WorkoutExerciseEntity.kt` 作成**

```kotlin
package com.muscleapp.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "workout_exercise",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val name: String,
    val sets: Int = 1,
    val reps: Int,
    val weight: Double? = null, // kg
    val notes: String? = null,
    val orderIndex: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

- [ ] **Step 3: `ExerciseTemplateEntity.kt` 作成**

```kotlin
package com.muscleapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "exercise_template")
data class ExerciseTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateName: String,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

- [ ] **Step 4: `TemplateExerciseEntity.kt` 作成**

```kotlin
package com.muscleapp.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "template_exercise",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId")]
)
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val name: String,
    val sets: Int = 1,
    val reps: Int,
    val weight: Double? = null,
    val notes: String? = null,
    val orderIndex: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

- [ ] **Step 5: `WorkoutSessionDao.kt` 作成**

```kotlin
package com.muscleapp.data.database.dao

import androidx.room.*
import com.muscleapp.data.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkoutSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Delete
    suspend fun delete(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_session WHERE id = :sessionId")
    suspend fun getById(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_session WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getByDate(date: LocalDate): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_session ORDER BY date DESC LIMIT 7")
    fun getRecentSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_session ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>
}
```

- [ ] **Step 6: `WorkoutExerciseDao.kt` 作成**

```kotlin
package com.muscleapp.data.database.dao

import androidx.room.*
import com.muscleapp.data.database.entity.WorkoutExerciseEntity

@Dao
interface WorkoutExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: WorkoutExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<WorkoutExerciseEntity>)

    @Update
    suspend fun update(exercise: WorkoutExerciseEntity)

    @Delete
    suspend fun delete(exercise: WorkoutExerciseEntity)

    @Query("SELECT * FROM workout_exercise WHERE sessionId = :sessionId ORDER BY orderIndex")
    suspend fun getBySessionId(sessionId: Long): List<WorkoutExerciseEntity>

    @Query("DELETE FROM workout_exercise WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long)
}
```

- [ ] **Step 7: `ExerciseTemplateDao.kt` 作成**

```kotlin
package com.muscleapp.data.database.dao

import androidx.room.*
import com.muscleapp.data.database.entity.ExerciseTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: ExerciseTemplateEntity): Long

    @Update
    suspend fun update(template: ExerciseTemplateEntity)

    @Delete
    suspend fun delete(template: ExerciseTemplateEntity)

    @Query("SELECT * FROM exercise_template WHERE id = :templateId")
    suspend fun getById(templateId: Long): ExerciseTemplateEntity?

    @Query("SELECT * FROM exercise_template ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<ExerciseTemplateEntity>>
}
```

- [ ] **Step 8: `TemplateExerciseDao.kt` 作成**

```kotlin
package com.muscleapp.data.database.dao

import androidx.room.*
import com.muscleapp.data.database.entity.TemplateExerciseEntity

@Dao
interface TemplateExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: TemplateExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<TemplateExerciseEntity>)

    @Update
    suspend fun update(exercise: TemplateExerciseEntity)

    @Delete
    suspend fun delete(exercise: TemplateExerciseEntity)

    @Query("SELECT * FROM template_exercise WHERE templateId = :templateId ORDER BY orderIndex")
    suspend fun getByTemplateId(templateId: Long): List<TemplateExerciseEntity>

    @Query("DELETE FROM template_exercise WHERE templateId = :templateId")
    suspend fun deleteByTemplateId(templateId: Long)
}
```

- [ ] **Step 9: `AppDatabase.kt` 作成**

```kotlin
package com.muscleapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.muscleapp.data.database.converter.LocalDateConverter
import com.muscleapp.data.database.converter.LocalDateTimeConverter
import com.muscleapp.data.database.dao.*
import com.muscleapp.data.database.entity.*

@Database(
    entities = [
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        ExerciseTemplateEntity::class,
        TemplateExerciseEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(LocalDateConverter::class, LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun exerciseTemplateDao(): ExerciseTemplateDao
    abstract fun templateExerciseDao(): TemplateExerciseDao

    companion object {
        const val DATABASE_NAME = "muscle_app_db"
    }
}
```

- [ ] **Step 10: TypeConverter 作成（LocalDate/LocalDateTime 対応）**

`converter/LocalDateConverter.kt`:
```kotlin
package com.muscleapp.data.database.converter

import androidx.room.TypeConverter
import java.time.LocalDate

object LocalDateConverter {
    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? {
        return date?.toString()
    }
}
```

`converter/LocalDateTimeConverter.kt`:
```kotlin
package com.muscleapp.data.database.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime

object LocalDateTimeConverter {
    @TypeConverter
    fun fromString(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateTimeToString(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
}
```

- [ ] **Step 11: テスト: DAO 基本動作確認**

`test/dao/WorkoutSessionDaoTest.kt`:
```kotlin
package com.muscleapp.data.database.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.muscleapp.data.database.AppDatabase
import com.muscleapp.data.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class WorkoutSessionDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: WorkoutSessionDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.workoutSessionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieve() = runBlocking {
        val session = WorkoutSessionEntity(
            date = LocalDate.now(),
            menuName = "テストセッション"
        )
        val id = dao.insert(session)
        val retrieved = dao.getById(id)
        assertEquals(session.menuName, retrieved?.menuName)
    }
}
```

- [ ] **Step 12: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/data/database/
git add app/src/androidTest/
git commit -m "feat: add Room database schema with entities and DAOs"
```

---

## Task 3: NLP 処理層の実装（自然言語解析）

**Files:**
- Create: `nlp/ExerciseKnowledgeBase.kt`
- Create: `nlp/JapaneseTokenizer.kt`
- Create: `nlp/ExerciseParser.kt`
- Create: `data/model/ExerciseParsingResult.kt`
- Create: `test/nlp/ExerciseParserTest.kt`

**Interfaces:**
- Consumes: なし
- Produces: `ExerciseParser.parse(input: String): List<ExerciseParsingResult>`

**Steps:**

- [ ] **Step 1: `ExerciseKnowledgeBase.kt` 作成（筋トレ種目辞書）**

```kotlin
package com.muscleapp.nlp

object ExerciseKnowledgeBase {
    // 日本語の筋トレ種目辞書
    private val exerciseNames = setOf(
        "ベンチプレス", "ダンベルプレス", "バーベルロウ", "ラットプルダウン",
        "スクワット", "レッグプレス", "レッグエクステンション", "レッグカール",
        "デッドリフト", "ショルダープレス", "アップライトロウ", "バイセップスカール",
        "トライセップスプレス", "フライ", "クロスオーバー", "プッシュアップ",
        "ニーレイズ", "シットアップ", "クランチ", "プランク",
        "懸垂", "チンニング", "ダイプス", "チェストフライ"
    )

    fun isValidExercise(name: String): Boolean {
        return exerciseNames.any { it.contains(name) || name.contains(it) }
    }

    fun normalizeExerciseName(name: String): String {
        return exerciseNames.firstOrNull { it.contains(name) || name.contains(it) }
            ?: name.trim()
    }
}
```

- [ ] **Step 2: `JapaneseTokenizer.kt` 作成（日本語トークナイザー）**

```kotlin
package com.muscleapp.nlp

object JapaneseTokenizer {
    // シンプルな日本語トークナイザー（正規表現ベース）
    fun tokenize(text: String): List<String> {
        // スペースで分割、空白削除
        return text.split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
    }

    fun extractNumbers(text: String): List<Int> {
        val regex = Regex("""(\d+)""")
        return regex.findAll(text)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .toList()
    }

    fun extractWeight(text: String): Double? {
        // "60kg", "60", "60 kg" パターン対応
        val regex = Regex("""(\d+(?:\.\d+)?)\s*kg|(\d+(?:\.\d+)?)""")
        return regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: regex.find(text)?.groupValues?.get(2)?.toDoubleOrNull()
    }
}
```

- [ ] **Step 3: `ExerciseParsingResult.kt` 作成（NLP 結果モデル）**

```kotlin
package com.muscleapp.data.model

data class ExerciseParsingResult(
    val name: String,
    val sets: Int = 1,
    val reps: Int,
    val weight: Double? = null,
    val confidence: Float = 1.0f // 1.0 = 高信頼度
)
```

- [ ] **Step 4: `ExerciseParser.kt` 作成（ルールベース NLP）**

```kotlin
package com.muscleapp.nlp

import com.muscleapp.data.model.ExerciseParsingResult

object ExerciseParser {
    
    fun parse(input: String): List<ExerciseParsingResult> {
        val results = mutableListOf<ExerciseParsingResult>()
        val lines = input.split("\n").filter { it.isNotEmpty() }

        for (line in lines) {
            val result = parseLine(line)
            if (result != null) {
                results.add(result)
            }
        }
        return results
    }

    private fun parseLine(line: String): ExerciseParsingResult? {
        val tokens = JapaneseTokenizer.tokenize(line)
        if (tokens.isEmpty()) return null

        // 種目名の抽出（最初の1-2トークン）
        val exerciseName = extractExerciseName(tokens)
            ?: return null

        // 回数、セット数、重量の抽出
        val numbers = JapaneseTokenizer.extractNumbers(line)
        val weight = JapaneseTokenizer.extractWeight(line)

        // パターンマッチ：「種目 10回」「種目 10回 60kg」など
        val (sets, reps) = when (numbers.size) {
            0 -> Pair(1, 10) // デフォルト
            1 -> Pair(1, numbers[0]) // "10回" → 1set x 10 reps
            else -> Pair(numbers[0], numbers[1]) // "3 10" → 3 sets x 10 reps
        }

        return ExerciseParsingResult(
            name = ExerciseKnowledgeBase.normalizeExerciseName(exerciseName),
            sets = sets.coerceIn(1, 10),
            reps = reps.coerceIn(1, 100),
            weight = weight,
            confidence = if (ExerciseKnowledgeBase.isValidExercise(exerciseName)) 1.0f else 0.7f
        )
    }

    private fun extractExerciseName(tokens: List<String>): String? {
        // 各トークンに対して、筋トレ種目かどうかチェック
        for (i in tokens.indices) {
            val currentToken = tokens[i]
            
            // 現在のトークンが種目名の一部か
            if (ExerciseKnowledgeBase.isValidExercise(currentToken)) {
                return currentToken
            }

            // 複合単語チェック（例：「ダンベル」+「プレス」）
            if (i + 1 < tokens.size) {
                val combined = currentToken + tokens[i + 1]
                if (ExerciseKnowledgeBase.isValidExercise(combined)) {
                    return combined
                }
            }
        }
        return null
    }
}
```

- [ ] **Step 5: `ExerciseParserTest.kt` 作成（単体テスト）**

```kotlin
package com.muscleapp.nlp

import com.muscleapp.data.model.ExerciseParsingResult
import org.junit.Test
import kotlin.test.assertEquals

class ExerciseParserTest {
    
    @Test
    fun testSimpleParsing() {
        val input = "ベンチプレス 10回"
        val results = ExerciseParser.parse(input)
        
        assertEquals(1, results.size)
        assertEquals("ベンチプレス", results[0].name)
        assertEquals(10, results[0].reps)
    }

    @Test
    fun testWithWeight() {
        val input = "ベンチプレス 10回 60kg"
        val results = ExerciseParser.parse(input)
        
        assertEquals(1, results.size)
        assertEquals("ベンチプレス", results[0].name)
        assertEquals(60.0, results[0].weight)
    }

    @Test
    fun testMultipleExercises() {
        val input = """
            ベンチプレス 10回 60kg
            ダンベルプレス 12回 20kg
            バーベルロウ 8回 80kg
        """.trimIndent()
        val results = ExerciseParser.parse(input)
        
        assertEquals(3, results.size)
        assertEquals("ベンチプレス", results[0].name)
        assertEquals("ダンベルプレス", results[1].name)
        assertEquals("バーベルロウ", results[2].name)
    }

    @Test
    fun testInvalidInput() {
        val input = "ランニング 30分"
        val results = ExerciseParser.parse(input)
        
        // 筋トレ種目ではない
        assertEquals(0, results.size)
    }
}
```

- [ ] **Step 6: テスト実行**

```bash
./gradlew test
```

期待: 全テスト PASS

- [ ] **Step 7: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/nlp/
git add app/src/main/kotlin/com/muscleapp/data/model/ExerciseParsingResult.kt
git add app/src/test/kotlin/com/muscleapp/nlp/
git commit -m "feat: add NLP exercise parser with rule-based Japanese tokenizer"
```

---

## Task 4: Repository レイヤー実装

**Files:**
- Create: `data/repository/WorkoutRepository.kt`
- Create: `data/repository/TemplateRepository.kt`

**Interfaces:**
- Consumes: `AppDatabase`, DAOs
- Produces: `WorkoutRepository`, `TemplateRepository`

**Steps:**

- [ ] **Step 1: `WorkoutRepository.kt` 作成**

```kotlin
package com.muscleapp.data.repository

import com.muscleapp.data.database.AppDatabase
import com.muscleapp.data.database.entity.WorkoutExerciseEntity
import com.muscleapp.data.database.entity.WorkoutSessionEntity
import com.muscleapp.data.model.WorkoutSession
import com.muscleapp.data.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WorkoutRepository(private val database: AppDatabase) {
    
    private val sessionDao = database.workoutSessionDao()
    private val exerciseDao = database.workoutExerciseDao()

    suspend fun createSession(
        date: LocalDate,
        menuName: String,
        exercises: List<WorkoutExercise>,
        notes: String? = null
    ): Long {
        val sessionEntity = WorkoutSessionEntity(
            date = date,
            menuName = menuName,
            notes = notes
        )
        val sessionId = sessionDao.insert(sessionEntity)

        val exerciseEntities = exercises.mapIndexed { index, exercise ->
            WorkoutExerciseEntity(
                sessionId = sessionId,
                name = exercise.name,
                sets = exercise.sets,
                reps = exercise.reps,
                weight = exercise.weight,
                notes = exercise.notes,
                orderIndex = index
            )
        }
        exerciseDao.insertAll(exerciseEntities)

        return sessionId
    }

    suspend fun getSessionById(sessionId: Long): WorkoutSession? {
        val sessionEntity = sessionDao.getById(sessionId) ?: return null
        val exercises = exerciseDao.getBySessionId(sessionId)
            .map { it.toWorkoutExercise() }
        return sessionEntity.toWorkoutSession(exercises)
    }

    fun getRecentSessions(): Flow<List<WorkoutSession>> {
        return sessionDao.getRecentSessions().map { sessions ->
            sessions.map { session ->
                val exercises = exerciseDao.getBySessionId(session.id)
                    .map { it.toWorkoutExercise() }
                session.toWorkoutSession(exercises)
            }
        }
    }

    suspend fun updateSession(session: WorkoutSession) {
        sessionDao.update(session.toEntity())
    }

    suspend fun deleteSession(sessionId: Long) {
        sessionDao.delete(sessionDao.getById(sessionId) ?: return)
    }

    // Mapper methods
    private fun WorkoutSessionEntity.toWorkoutSession(exercises: List<WorkoutExercise>): WorkoutSession {
        return WorkoutSession(
            id = id,
            date = date,
            menuName = menuName,
            exercises = exercises,
            notes = notes,
            createdAt = createdAt
        )
    }

    private fun WorkoutExerciseEntity.toWorkoutExercise(): WorkoutExercise {
        return WorkoutExercise(
            id = id,
            name = name,
            sets = sets,
            reps = reps,
            weight = weight,
            notes = notes
        )
    }

    private fun WorkoutSession.toEntity(): WorkoutSessionEntity {
        return WorkoutSessionEntity(
            id = id,
            date = date,
            menuName = menuName,
            notes = notes,
            updatedAt = java.time.LocalDateTime.now()
        )
    }
}
```

- [ ] **Step 2: `TemplateRepository.kt` 作成**

```kotlin
package com.muscleapp.data.repository

import com.muscleapp.data.database.AppDatabase
import com.muscleapp.data.database.entity.ExerciseTemplateEntity
import com.muscleapp.data.database.entity.TemplateExerciseEntity
import com.muscleapp.data.model.ExerciseTemplate
import com.muscleapp.data.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TemplateRepository(private val database: AppDatabase) {
    
    private val templateDao = database.exerciseTemplateDao()
    private val exerciseDao = database.templateExerciseDao()

    suspend fun createTemplate(template: ExerciseTemplate): Long {
        val templateEntity = ExerciseTemplateEntity(
            templateName = template.templateName,
            description = template.description
        )
        val templateId = templateDao.insert(templateEntity)

        val exerciseEntities = template.exercises.mapIndexed { index, exercise ->
            TemplateExerciseEntity(
                templateId = templateId,
                name = exercise.name,
                sets = exercise.sets,
                reps = exercise.reps,
                weight = exercise.weight,
                notes = exercise.notes,
                orderIndex = index
            )
        }
        exerciseDao.insertAll(exerciseEntities)

        return templateId
    }

    suspend fun getTemplateById(templateId: Long): ExerciseTemplate? {
        val templateEntity = templateDao.getById(templateId) ?: return null
        val exercises = exerciseDao.getByTemplateId(templateId)
            .map { it.toWorkoutExercise() }
        return templateEntity.toExerciseTemplate(exercises)
    }

    fun getAllTemplates(): Flow<List<ExerciseTemplate>> {
        return templateDao.getAllTemplates().map { templates ->
            templates.map { template ->
                val exercises = exerciseDao.getByTemplateId(template.id)
                    .map { it.toWorkoutExercise() }
                template.toExerciseTemplate(exercises)
            }
        }
    }

    suspend fun deleteTemplate(templateId: Long) {
        templateDao.delete(templateDao.getById(templateId) ?: return)
        exerciseDao.deleteByTemplateId(templateId)
    }

    // Mapper methods
    private fun ExerciseTemplateEntity.toExerciseTemplate(exercises: List<WorkoutExercise>): ExerciseTemplate {
        return ExerciseTemplate(
            id = id,
            templateName = templateName,
            description = description,
            exercises = exercises
        )
    }

    private fun TemplateExerciseEntity.toWorkoutExercise(): WorkoutExercise {
        return WorkoutExercise(
            id = id,
            name = name,
            sets = sets,
            reps = reps,
            weight = weight,
            notes = notes
        )
    }
}
```

- [ ] **Step 3: `WorkoutRepository.kt` テスト**

```bash
./gradlew test
```

- [ ] **Step 4: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/data/repository/
git commit -m "feat: add WorkoutRepository and TemplateRepository with data mapping"
```

---

## Task 5: ドメインモデル作成

**Files:**
- Create: `data/model/WorkoutSession.kt`
- Create: `data/model/WorkoutExercise.kt`
- Create: `data/model/ExerciseTemplate.kt`

**Interfaces:**
- Consumes: なし
- Produces: ドメインモデルクラス

**Steps:**

- [ ] **Step 1: `WorkoutExercise.kt` 作成**

```kotlin
package com.muscleapp.data.model

data class WorkoutExercise(
    val id: Long = 0,
    val name: String,
    val sets: Int = 1,
    val reps: Int,
    val weight: Double? = null, // kg
    val notes: String? = null
)
```

- [ ] **Step 2: `WorkoutSession.kt` 作成**

```kotlin
package com.muscleapp.data.model

import java.time.LocalDate
import java.time.LocalDateTime

data class WorkoutSession(
    val id: Long = 0,
    val date: LocalDate,
    val menuName: String,
    val exercises: List<WorkoutExercise> = emptyList(),
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

- [ ] **Step 3: `ExerciseTemplate.kt` 作成**

```kotlin
package com.muscleapp.data.model

data class ExerciseTemplate(
    val id: Long = 0,
    val templateName: String,
    val description: String? = null,
    val exercises: List<WorkoutExercise> = emptyList()
)
```

- [ ] **Step 4: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/data/model/
git commit -m "feat: add domain models (WorkoutSession, WorkoutExercise, ExerciseTemplate)"
```

---

## Task 6: ViewModel 実装

**Files:**
- Create: `viewmodel/WorkoutInputViewModel.kt`
- Create: `viewmodel/WorkoutSessionViewModel.kt`

**Interfaces:**
- Consumes: `ExerciseParser`, `WorkoutRepository`, `TemplateRepository`
- Produces: `WorkoutInputViewModel`, `WorkoutSessionViewModel`

**Steps:**

- [ ] **Step 1: `WorkoutInputViewModel.kt` 作成**

```kotlin
package com.muscleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muscleapp.data.model.ExerciseParsingResult
import com.muscleapp.data.repository.TemplateRepository
import com.muscleapp.nlp.ExerciseParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutInputViewModel(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _parsedExercises = MutableStateFlow<List<ExerciseParsingResult>>(emptyList())
    val parsedExercises: StateFlow<List<ExerciseParsingResult>> = _parsedExercises.asStateFlow()

    private val _selectedTemplateId = MutableStateFlow<Long?>(null)
    val selectedTemplateId: StateFlow<Long?> = _selectedTemplateId.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun parseExercises() {
        val results = ExerciseParser.parse(_inputText.value)
        _parsedExercises.value = results
    }

    fun selectTemplate(templateId: Long) {
        _selectedTemplateId.value = templateId
        viewModelScope.launch {
            val template = templateRepository.getTemplateById(templateId)
            if (template != null) {
                _parsedExercises.value = template.exercises.map {
                    ExerciseParsingResult(
                        name = it.name,
                        sets = it.sets,
                        reps = it.reps,
                        weight = it.weight
                    )
                }
            }
        }
    }

    fun updateExercise(index: Int, updated: ExerciseParsingResult) {
        val current = _parsedExercises.value.toMutableList()
        if (index in current.indices) {
            current[index] = updated
            _parsedExercises.value = current
        }
    }

    fun addExercise(exercise: ExerciseParsingResult) {
        _parsedExercises.value += exercise
    }

    fun removeExercise(index: Int) {
        val current = _parsedExercises.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _parsedExercises.value = current
        }
    }

    fun resetForm() {
        _inputText.value = ""
        _parsedExercises.value = emptyList()
        _selectedTemplateId.value = null
    }
}
```

- [ ] **Step 2: `WorkoutSessionViewModel.kt` 作成**

```kotlin
package com.muscleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muscleapp.data.model.WorkoutSession
import com.muscleapp.data.model.WorkoutExercise
import com.muscleapp.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class WorkoutSessionViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _recentSessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val recentSessions: StateFlow<List<WorkoutSession>> = _recentSessions.asStateFlow()

    private val _currentSession = MutableStateFlow<WorkoutSession?>(null)
    val currentSession: StateFlow<WorkoutSession?> = _currentSession.asStateFlow()

    init {
        loadRecentSessions()
    }

    private fun loadRecentSessions() {
        viewModelScope.launch {
            workoutRepository.getRecentSessions().collect { sessions ->
                _recentSessions.value = sessions
            }
        }
    }

    fun saveSession(
        date: LocalDate,
        menuName: String,
        exercises: List<WorkoutExercise>,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val sessionId = workoutRepository.createSession(date, menuName, exercises, notes)
            val savedSession = workoutRepository.getSessionById(sessionId)
            _currentSession.value = savedSession
            loadRecentSessions()
        }
    }

    fun loadSessionById(sessionId: Long) {
        viewModelScope.launch {
            val session = workoutRepository.getSessionById(sessionId)
            _currentSession.value = session
        }
    }
}
```

- [ ] **Step 3: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/viewmodel/
git commit -m "feat: add ViewModels for input and session management"
```

---

## Task 7: UI コンポーネント実装（Jetpack Compose）

**Files:**
- Create: `ui/theme/Theme.kt`
- Create: `ui/components/ExerciseCard.kt`
- Create: `ui/components/ExerciseInputField.kt`

**Interfaces:**
- Consumes: ドメインモデル
- Produces: Compose コンポーネント

**Steps:**

- [ ] **Step 1: `Theme.kt` 作成**

```kotlin
package com.muscleapp.ui.theme

import androidx.compose.foundation.isSystemInDarkMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF03DAC6)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF03DAC6)
)

@Composable
fun MuscleAppTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

- [ ] **Step 2: `ExerciseCard.kt` 作成**

```kotlin
package com.muscleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleapp.data.model.WorkoutExercise

@Composable
fun ExerciseCard(
    exercise: WorkoutExercise,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, fontSize = 16.sp)
                Text("${exercise.sets}set x ${exercise.reps}reps", fontSize = 12.sp)
                if (exercise.weight != null) {
                    Text("${exercise.weight}kg", fontSize = 12.sp)
                }
            }

            Row {
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = vectorResource(id = android.R.drawable.ic_menu_edit),
                            contentDescription = "Edit"
                        )
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = vectorResource(id = android.R.drawable.ic_menu_delete),
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 3: `ExerciseInputField.kt` 作成**

```kotlin
package com.muscleapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExerciseInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("運動を入力（複数行対応）") },
        minLines = 4,
        maxLines = 8,
        placeholder = { 
            Text(
                """ベンチプレス 10回 60kg
ダンベルプレス 12回 20kg
バーベルロウ 8回 80kg""".trimIndent()
            )
        }
    )
}
```

- [ ] **Step 4: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/ui/
git commit -m "feat: add Jetpack Compose theme and UI components"
```

---

## Task 8: ホーム画面実装

**Files:**
- Create: `ui/screens/HomeScreen.kt`

**Interfaces:**
- Consumes: `WorkoutSessionViewModel`
- Produces: Composable Home Screen

**Steps:**

- [ ] **Step 1: `HomeScreen.kt` 作成**

```kotlin
package com.muscleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleapp.viewmodel.WorkoutSessionViewModel
import com.muscleapp.ui.components.ExerciseCard

@Composable
fun HomeScreen(
    viewModel: WorkoutSessionViewModel = WorkoutSessionViewModel(
        workoutRepository = TODO() // Dependency Injection予定
    ),
    onStartNewSession: () -> Unit = {},
    onSelectTemplate: () -> Unit = {}
) {
    val recentSessions = viewModel.recentSessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ヘッダー
        Text(
            text = "筋トレアプリ",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 新規セッション開始ボタン
        Button(
            onClick = onStartNewSession,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("新規セッションを開始")
        }

        // テンプレート選択ボタン
        Button(
            onClick = onSelectTemplate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("テンプレートから開始")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // 最近のセッション一覧
        Text(
            text = "最近のセッション",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn {
            items(recentSessions.value) { session ->
                ExerciseCard(
                    exercise = session.exercises.firstOrNull()
                        ?: com.muscleapp.data.model.WorkoutExercise(
                            name = session.menuName,
                            reps = 0
                        ),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
```

- [ ] **Step 2: `MainActivity.kt` 更新**

```kotlin
package com.muscleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.muscleapp.ui.screens.HomeScreen
import com.muscleapp.ui.theme.MuscleAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuscleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}
```

- [ ] **Step 3: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/ui/screens/HomeScreen.kt
git commit -m "feat: add HomeScreen with session list and navigation buttons"
```

---

## Task 9: 自然言語入力画面実装

**Files:**
- Create: `ui/screens/WorkoutInputScreen.kt`

**Interfaces:**
- Consumes: `WorkoutInputViewModel`, `ExerciseParser`
- Produces: Composable Input Screen

**Steps:**

- [ ] **Step 1: `WorkoutInputScreen.kt` 作成**

```kotlin
package com.muscleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleapp.viewmodel.WorkoutInputViewModel
import com.muscleapp.ui.components.ExerciseInputField

@Composable
fun WorkoutInputScreen(
    viewModel: WorkoutInputViewModel = WorkoutInputViewModel(
        templateRepository = TODO()
    ),
    onNext: (List<String>) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val inputText = viewModel.inputText.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("運動を入力", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        ExerciseInputField(
            value = inputText.value,
            onValueChange = { viewModel.updateInputText(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = { viewModel.parseExercises() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("解析")
        }

        Button(
            onClick = { onNext(emptyList()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("次へ")
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("戻る")
        }
    }
}
```

- [ ] **Step 2: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/ui/screens/WorkoutInputScreen.kt
git commit -m "feat: add WorkoutInputScreen with NLP parsing button"
```

---

## Task 10: NLP確認・編集画面実装

**Files:**
- Create: `ui/screens/WorkoutReviewScreen.kt`

**Interfaces:**
- Consumes: `WorkoutInputViewModel`, `ExerciseParsingResult`
- Produces: Composable Review Screen

**Steps:**

- [ ] **Step 1: `WorkoutReviewScreen.kt` 作成**

```kotlin
package com.muscleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleapp.viewmodel.WorkoutInputViewModel
import com.muscleapp.ui.components.ExerciseCard
import com.muscleapp.data.model.WorkoutExercise

@Composable
fun WorkoutReviewScreen(
    viewModel: WorkoutInputViewModel = WorkoutInputViewModel(
        templateRepository = TODO()
    ),
    onSave: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val parsedExercises = viewModel.parsedExercises.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("確認・編集", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(parsedExercises.value) { index, exercise ->
                ExerciseCard(
                    exercise = WorkoutExercise(
                        name = exercise.name,
                        sets = exercise.sets,
                        reps = exercise.reps,
                        weight = exercise.weight
                    ),
                    modifier = Modifier.padding(8.dp),
                    onDelete = { viewModel.removeExercise(index) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text("保存")
            }
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("戻る")
            }
        }
    }
}
```

- [ ] **Step 2: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/ui/screens/WorkoutReviewScreen.kt
git commit -m "feat: add WorkoutReviewScreen with exercise editing capability"
```

---

## Task 11: 画面遷移（Navigation）実装

**Files:**
- Create: `ui/navigation/NavigationGraph.kt`
- Modify: `MainActivity.kt`

**Interfaces:**
- Consumes: 全ての Screen Composable
- Produces: Navigation Graph

**Steps:**

- [ ] **Step 1: `NavigationGraph.kt` 作成**

```kotlin
package com.muscleapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muscleapp.ui.screens.HomeScreen
import com.muscleapp.ui.screens.WorkoutInputScreen
import com.muscleapp.ui.screens.WorkoutReviewScreen

@Composable
fun MuscleAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onStartNewSession = { navController.navigate("input") },
                onSelectTemplate = { navController.navigate("template") }
            )
        }

        composable("input") {
            WorkoutInputScreen(
                onNext = { navController.navigate("review") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("review") {
            WorkoutReviewScreen(
                onSave = { navController.popBackStack("home", false) },
                onBack = { navController.popBackStack() }
            )
        }

        composable("template") {
            // TemplateSelectionScreen（Task 12）
            HomeScreen()
        }
    }
}
```

- [ ] **Step 2: `MainActivity.kt` 更新**

```kotlin
package com.muscleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.muscleapp.ui.navigation.MuscleAppNavigation
import com.muscleapp.ui.theme.MuscleAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuscleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MuscleAppNavigation()
                }
            }
        }
    }
}
```

- [ ] **Step 3: `build.gradle.kts` に Navigation 依存関係追加**

```kotlin
implementation("androidx.navigation:navigation-compose:2.7.0")
```

- [ ] **Step 4: ビルド＆テスト**

```bash
./gradlew build
```

- [ ] **Step 5: コミット**

```bash
git add app/src/main/kotlin/com/muscleapp/ui/navigation/
git add app/src/main/kotlin/com/muscleapp/MainActivity.kt
git commit -m "feat: add navigation graph and screen routing"
```

---

## Task 12: エンドツーエンドテスト＆統合テスト

**Files:**
- Create: `test/integration/WorkoutIntegrationTest.kt`

**Interfaces:**
- Consumes: 全モジュール
- Produces: 統合テスト

**Steps:**

- [ ] **Step 1: `WorkoutIntegrationTest.kt` 作成**

```kotlin
package com.muscleapp.integration

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.muscleapp.data.database.AppDatabase
import com.muscleapp.data.repository.WorkoutRepository
import com.muscleapp.data.model.WorkoutExercise
import com.muscleapp.nlp.ExerciseParser
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class WorkoutIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: WorkoutRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorkoutRepository(database)
    }

    @Test
    fun testEndToEndWorkout() = runBlocking {
        // 1. NLP で入力文を解析
        val input = "ベンチプレス 10回 60kg\nダンベルプレス 12回 20kg"
        val parsed = ExerciseParser.parse(input)
        assertEquals(2, parsed.size)

        // 2. DBに保存
        val exercises = listOf(
            WorkoutExercise(
                name = "ベンチプレス",
                sets = 1,
                reps = 10,
                weight = 60.0
            ),
            WorkoutExercise(
                name = "ダンベルプレス",
                sets = 1,
                reps = 12,
                weight = 20.0
            )
        )
        val sessionId = repository.createSession(
            date = LocalDate.now(),
            menuName = "月曜の上半身",
            exercises = exercises
        )

        // 3. 取得して確認
        val retrievedSession = repository.getSessionById(sessionId)
        assertEquals("月曜の上半身", retrievedSession?.menuName)
        assertEquals(2, retrievedSession?.exercises?.size)
    }
}
```

- [ ] **Step 2: テスト実行**

```bash
./gradlew connectedAndroidTest
```

- [ ] **Step 3: コミット**

```bash
git add app/src/androidTest/kotlin/com/muscleapp/
git commit -m "test: add end-to-end integration tests for workout flow"
```

---

## Task 13: MVP ビルド＆動作確認

**Files:**
- Modify: `build.gradle.kts`（必要に応じて）

**Interfaces:**
- Consumes: 全コンポーネント
- Produces: 動作可能な APK

**Steps:**

- [ ] **Step 1: 最終ビルド**

```bash
./gradlew clean build
```

- [ ] **Step 2: エミュレータで実行**

Android Studio でプロジェクトを開き、実行（▶️ ボタン）

- [ ] **Step 3: 動作確認（マニュアルテスト）**

- [ ] ホーム画面が表示される
- [ ] 「新規セッションを開始」ボタンで入力画面に遷移
- [ ] 入力画面で「ベンチプレス 10回 60kg」を入力
- [ ] 「解析」ボタンをタップ
- [ ] 確認画面でメニューが表示される
- [ ] 「保存」で DBに保存
- [ ] ホーム画面で最近のセッションが表示される

- [ ] **Step 4: Logcat で エラー確認**

Logcat が ERROR や CRASH を出していないことを確認

- [ ] **Step 5: コミット（Release タグ）**

```bash
git tag v1.0.0-mvp
git push origin v1.0.0-mvp
```

---

## Task 14: Google Play Store 開発者登録準備

**Files:**
- Create: `docs/GOOGLE_PLAY_SETUP.md`

**Interfaces:**
- 外部: Google Play Developer Console

**Steps:**

- [ ] **Step 1: Google Play Developer アカウント作成**

1. https://play.google.com/console にアクセス
2. 「Play Developer アカウントを作成」
3. Google アカウントでログイン
4. 開発者名、メールアドレス、利用規約同意
5. 登録手数料（¥2,500）支払い

- [ ] **Step 2: `docs/GOOGLE_PLAY_SETUP.md` 作成**

```markdown
# Google Play Store リリース準備

## 開発者登録
1. Play Developer Console にアカウント作成
2. アプリプロフィール（スクリーンショット、説明など）作成
3. ストア掲載情報設定

## アプリの署名
```bash
keytool -genkey -v -keystore release-key.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias muscle-app-key
```

## リリースビルド
```bash
./gradlew bundleRelease
```

出力ファイル: `app/build/outputs/bundle/release/app-release.aab`

## アップロード
1. Google Play Console → アプリ選択
2. テストのリリース → 本番環境へ提出
3. AAB ファイルをアップロード
4. アプリ プロフィール完成
5. コンテンツ レーティング入力
6. 価格とプログラムを設定
7. 送信
```

- [ ] **Step 3: ドキュメント コミット**

```bash
git add docs/GOOGLE_PLAY_SETUP.md
git commit -m "docs: add Google Play Store release setup guide"
```

---

## 次のステップ

✅ **MVP フェーズ完了**

- ホーム画面 ✅
- 自然言語入力 ✅
- NLP 確認・編集 ✅
- ローカル DB 保存 ✅

🎯 **Phase 2（テンプレート管理＆統計）**：別の実装計画で進める

🚀 **Google Play リリース**: 開発者登録後、AAB をアップロード
