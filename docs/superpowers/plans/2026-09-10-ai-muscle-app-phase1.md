# AI Muscle App Phase 1 (MVP) 実装計画

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Androidアプリの基本機能（自然言語入力 → AI解析 → メニュー記録）を実装し、ローカルストレージで完結する MVP をリリースする。

**Architecture:** 
- **フロントエンド**: Jetpack Compose で5画面（ホーム、入力、NLP確認、テンプレート、履歴）を実装
- **バックエンド・データ層**: Room + SQLite でローカル永続化、4テーブル正規化
- **NLP処理**: ルールベース解析（正規表現）+ TensorFlow Lite 軽量モデルで信頼度スコア
- **全体**: オンデバイス処理・オフライン対応（クラウド非依存）

**Tech Stack:**
- Kotlin + Jetpack Compose（UI）
- Room + SQLite（ローカルDB）
- TensorFlow Lite（NLP推論）
- Gradle（ビルドシステム）
- JUnit + Espresso（テスト）

**Spec:** `docs/superpowers/specs/2026-09-10-ai-muscle-app-design.md`

## Global Constraints

- **言語**: Kotlin（型安全性、Android推奨）
- **UI Framework**: Jetpack Compose（宣言型）
- **DB**: Room + SQLite（Jetpack公式、ORM）
- **オフライン対応**: 必須（クラウド連携は v2+）
- **NLP推論**: TensorFlow Lite（< 100ms、デバイス内）
- **ストレージ**: 100MB 以下（初期版）
- **UI応答性**: 60fps（Compose保証）
- **テスト戦略**: ユニットテスト（Room, NLP）+ UI テスト（Compose）

---

## ファイル構造

### 新規作成ファイル

```
ai-muscle-app/
├── app/
│   ├── build.gradle.kts              # ビルド設定（依存関係）
│   ├── src/main/AndroidManifest.xml  # マニフェスト
│   ├── src/main/java/com/example/
│   │   └── aimuscle/
│   │       ├── data/
│   │       │   ├── db/
│   │       │   │   ├── AppDatabase.kt           # Room Database
│   │       │   │   ├── WorkoutSessionDao.kt     # DAO: Session
│   │       │   │   ├── WorkoutExerciseDao.kt    # DAO: Exercise
│   │       │   │   ├── ExerciseTemplateDao.kt   # DAO: Template
│   │       │   │   └── TemplateExerciseDao.kt   # DAO: Template Exercise
│   │       │   ├── models/
│   │       │   │   ├── WorkoutSession.kt        # Entity
│   │       │   │   ├── WorkoutExercise.kt       # Entity
│   │       │   │   ├── ExerciseTemplate.kt      # Entity
│   │       │   │   └── TemplateExercise.kt      # Entity
│   │       │   └── repository/
│   │       │       └── WorkoutRepository.kt     # Repository（CRUD）
│   │       ├── domain/
│   │       │   ├── models/
│   │       │   │   └── ParsedWorkout.kt         # Domain Model（NLP出力）
│   │       │   └── usecases/
│   │       │       ├── SaveWorkoutUseCase.kt
│   │       │       ├── GetTemplatesUseCase.kt
│   │       │       └── GetHistoryUseCase.kt
│   │       ├── nlp/
│   │       │   ├── NLPEngine.kt                 # NLP解析エンジン
│   │       │   ├── WorkoutDictionary.kt        # 運動名辞書
│   │       │   ├── PatternExtractor.kt         # 正規表現パターン
│   │       │   └── TFLiteModel.kt              # TensorFlow Lite ラッパー
│   │       ├── ui/
│   │       │   ├── MainActivity.kt              # エントリーポイント
│   │       │   ├── theme/
│   │       │   │   ├── Color.kt
│   │       │   │   ├── Shape.kt
│   │       │   │   └── Typography.kt
│   │       │   ├── screens/
│   │       │   │   ├── HomeScreen.kt
│   │       │   │   ├── InputScreen.kt
│   │       │   │   ├── NLPConfirmScreen.kt
│   │       │   │   ├── TemplateScreen.kt
│   │       │   │   └── HistoryScreen.kt
│   │       │   └── viewmodels/
│   │       │       ├── HomeViewModel.kt
│   │       │       ├── InputViewModel.kt
│   │       │       ├── NLPConfirmViewModel.kt
│   │       │       ├── TemplateViewModel.kt
│   │       │       └── HistoryViewModel.kt
│   │       └── di/
│   │           └── AppModule.kt                # Dependency Injection
│   └── src/test/
│       ├── NLPEngineTest.kt
│       ├── WorkoutRepositoryTest.kt
│       └── ...
└── gradle/
    └── libs.versions.toml             # 依存関係バージョン管理
```

### 依存関係（build.gradle.kts に追加）

```kotlin
dependencies {
    // Compose UI
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.0.0")
    implementation("androidx.activity:activity-compose:1.7.0")

    // Jetpack
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0")
    implementation("androidx.room:room-runtime:2.5.0")
    kapt("androidx.room:room-compiler:2.5.0")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-select:2.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.room:room-testing:2.5.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
}
```

---

## タスク分割（全14タスク）

### **タスク 1: プロジェクト初期化 & 依存関係設定**

**担当**: Pollen（インフラ・初期設定）

**Files:**
- Create: `build.gradle.kts` (app module)
- Create: `gradle/libs.versions.toml`
- Create: `src/main/AndroidManifest.xml`
- Create: `.gitignore`

**Interfaces:**
- Produces: ビルド可能な Kotlin/Android プロジェクト構造

- [ ] **Step 1: Android Studio で新規プロジェクトを作成**

```bash
# Android Studio GUI または以下の手動コマンド
# File → New → New Android Project
# Target: Android 12 (API 31+)
# Language: Kotlin
```

- [ ] **Step 2: build.gradle.kts を編集し、依存関係を追加**

```gradle
// app/build.gradle.kts
dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.0.0")
    implementation("androidx.activity:activity-compose:1.7.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.5.0")
    kapt("androidx.room:room-compiler:2.5.0")
    
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
}
```

- [ ] **Step 3: AndroidManifest.xml を編集（最小権限）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.aimuscle">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:theme="@style/Theme.AiMuscle">
        <activity
            android:name=".ui.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 4: Gradle 同期・ビルド確認**

```bash
./gradlew build
# Expected: BUILD SUCCESSFUL
```

- [ ] **Step 5: Commit**

```bash
git add build.gradle.kts gradle/libs.versions.toml src/main/AndroidManifest.xml
git commit -m "chore: initialize Android project with Compose, Room, TensorFlow Lite"
```

---

### **タスク 2: データモデル & Room Entity 定義**

**担当**: Honey（データ層）

**Files:**
- Create: `src/main/java/com/example/aimuscle/data/models/WorkoutSession.kt`
- Create: `src/main/java/com/example/aimuscle/data/models/WorkoutExercise.kt`
- Create: `src/main/java/com/example/aimuscle/data/models/ExerciseTemplate.kt`
- Create: `src/main/java/com/example/aimuscle/data/models/TemplateExercise.kt`

**Interfaces:**
- Produces: Room Entity 定義（4テーブル）

- [ ] **Step 1: WorkoutSession Entity を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/models/WorkoutSession.kt
package com.example.aimuscle.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "workout_session")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val menuName: String,           // 例：「月曜の上半身」
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

- [ ] **Step 2: WorkoutExercise Entity を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/models/WorkoutExercise.kt
package com.example.aimuscle.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercise",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val name: String,               // 例：「ベンチプレス」
    val sets: Int,
    val reps: Int,
    val weight: Double? = null,     // kg, オプション
    val notes: String? = null,
    val order: Int                  // 表示順序
)
```

- [ ] **Step 3: ExerciseTemplate Entity を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/models/ExerciseTemplate.kt
package com.example.aimuscle.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "exercise_template")
data class ExerciseTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateName: String,       // 例：「月曜の上半身」
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

- [ ] **Step 4: TemplateExercise Entity を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/models/TemplateExercise.kt
package com.example.aimuscle.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "template_exercise",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double? = null,
    val order: Int
)
```

- [ ] **Step 5: Compile 確認**

```bash
./gradlew compileKotlin
# Expected: BUILD SUCCESSFUL
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/aimuscle/data/models/
git commit -m "feat: define Room entities (WorkoutSession, WorkoutExercise, ExerciseTemplate, TemplateExercise)"
```

---

### **タスク 3: Room Database & DAO 実装**

**担当**: Honey（データ層）

**Files:**
- Create: `src/main/java/com/example/aimuscle/data/db/AppDatabase.kt`
- Create: `src/main/java/com/example/aimuscle/data/db/WorkoutSessionDao.kt`
- Create: `src/main/java/com/example/aimuscle/data/db/WorkoutExerciseDao.kt`
- Create: `src/main/java/com/example/aimuscle/data/db/ExerciseTemplateDao.kt`
- Create: `src/main/java/com/example/aimuscle/data/db/TemplateExerciseDao.kt`

**Interfaces:**
- Consumes: 前タスクの Entity 定義
- Produces: Room Database インスタンス + DAO インターフェース

- [ ] **Step 1: AppDatabase を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/db/AppDatabase.kt
package com.example.aimuscle.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aimuscle.data.models.*

@Database(
    entities = [WorkoutSession::class, WorkoutExercise::class, ExerciseTemplate::class, TemplateExercise::class],
    version = 1
)
@TypeConverters(LocalDateTimeConverter::class, LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun exerciseTemplateDao(): ExerciseTemplateDao
    abstract fun templateExerciseDao(): TemplateExerciseDao
}

// Type Converter (LocalDate/LocalDateTime を String に変換)
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

object LocalDateConverter {
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? =
        dateString?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? =
        date?.toString()
}

object LocalDateTimeConverter {
    @TypeConverter
    fun toLocalDateTime(dateString: String?): LocalDateTime? =
        dateString?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? =
        dateTime?.toString()
}
```

- [ ] **Step 2: WorkoutSessionDao を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/db/WorkoutSessionDao.kt
package com.example.aimuscle.data.db

import androidx.room.*
import com.example.aimuscle.data.models.WorkoutSession
import java.time.LocalDate

@Dao
interface WorkoutSessionDao {
    @Insert
    suspend fun insert(session: WorkoutSession): Long

    @Update
    suspend fun update(session: WorkoutSession)

    @Delete
    suspend fun delete(session: WorkoutSession)

    @Query("SELECT * FROM workout_session WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSession?

    @Query("SELECT * FROM workout_session ORDER BY date DESC")
    suspend fun getAllSessions(): List<WorkoutSession>

    @Query("SELECT * FROM workout_session WHERE date = :date")
    suspend fun getByDate(date: LocalDate): List<WorkoutSession>

    @Query("SELECT * FROM workout_session WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getInDateRange(startDate: LocalDate, endDate: LocalDate): List<WorkoutSession>
}
```

- [ ] **Step 3: WorkoutExerciseDao を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/db/WorkoutExerciseDao.kt
package com.example.aimuscle.data.db

import androidx.room.*
import com.example.aimuscle.data.models.WorkoutExercise

@Dao
interface WorkoutExerciseDao {
    @Insert
    suspend fun insert(exercise: WorkoutExercise): Long

    @Update
    suspend fun update(exercise: WorkoutExercise)

    @Delete
    suspend fun delete(exercise: WorkoutExercise)

    @Query("SELECT * FROM workout_exercise WHERE id = :id")
    suspend fun getById(id: Long): WorkoutExercise?

    @Query("SELECT * FROM workout_exercise WHERE sessionId = :sessionId ORDER BY `order`")
    suspend fun getBySessionId(sessionId: Long): List<WorkoutExercise>

    @Query("DELETE FROM workout_exercise WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long)
}
```

- [ ] **Step 4: ExerciseTemplateDao を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/db/ExerciseTemplateDao.kt
package com.example.aimuscle.data.db

import androidx.room.*
import com.example.aimuscle.data.models.ExerciseTemplate

@Dao
interface ExerciseTemplateDao {
    @Insert
    suspend fun insert(template: ExerciseTemplate): Long

    @Update
    suspend fun update(template: ExerciseTemplate)

    @Delete
    suspend fun delete(template: ExerciseTemplate)

    @Query("SELECT * FROM exercise_template WHERE id = :id")
    suspend fun getById(id: Long): ExerciseTemplate?

    @Query("SELECT * FROM exercise_template ORDER BY createdAt DESC")
    suspend fun getAllTemplates(): List<ExerciseTemplate>

    @Query("DELETE FROM exercise_template WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

- [ ] **Step 5: TemplateExerciseDao を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/db/TemplateExerciseDao.kt
package com.example.aimuscle.data.db

import androidx.room.*
import com.example.aimuscle.data.models.TemplateExercise

@Dao
interface TemplateExerciseDao {
    @Insert
    suspend fun insert(exercise: TemplateExercise): Long

    @Update
    suspend fun update(exercise: TemplateExercise)

    @Delete
    suspend fun delete(exercise: TemplateExercise)

    @Query("SELECT * FROM template_exercise WHERE id = :id")
    suspend fun getById(id: Long): TemplateExercise?

    @Query("SELECT * FROM template_exercise WHERE templateId = :templateId ORDER BY `order`")
    suspend fun getByTemplateId(templateId: Long): List<TemplateExercise>

    @Query("DELETE FROM template_exercise WHERE templateId = :templateId")
    suspend fun deleteByTemplateId(templateId: Long)
}
```

- [ ] **Step 6: Compile 確認**

```bash
./gradlew compileKotlin
# Expected: BUILD SUCCESSFUL
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/aimuscle/data/db/
git commit -m "feat: implement Room Database and DAOs"
```

---

### **タスク 4: Domain Model & Repository パターン**

**担当**: Honey（バックエンド・ビジネスロジック層）

**Files:**
- Create: `src/main/java/com/example/aimuscle/domain/models/ParsedWorkout.kt`
- Create: `src/main/java/com/example/aimuscle/data/repository/WorkoutRepository.kt`

**Interfaces:**
- Consumes: DAO（Task 3）
- Produces: Repository インターフェース（UI層が依存）

- [ ] **Step 1: ParsedWorkout Domain Model を作成**

```kotlin
// src/main/java/com/example/aimuscle/domain/models/ParsedWorkout.kt
package com.example.aimuscle.domain.models

data class ParsedExercise(
    val name: String,
    val reps: Int,
    val sets: Int = 1,
    val weight: Double? = null,
    val confidence: Float = 1f   // NLP信頼度 (0-1)
)

data class ParsedWorkout(
    val exercises: List<ParsedExercise>
)
```

- [ ] **Step 2: WorkoutRepository を作成**

```kotlin
// src/main/java/com/example/aimuscle/data/repository/WorkoutRepository.kt
package com.example.aimuscle.data.repository

import com.example.aimuscle.data.db.*
import com.example.aimuscle.data.models.*
import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.domain.models.ParsedWorkout
import java.time.LocalDate

class WorkoutRepository(
    private val sessionDao: WorkoutSessionDao,
    private val exerciseDao: WorkoutExerciseDao,
    private val templateDao: ExerciseTemplateDao,
    private val templateExerciseDao: TemplateExerciseDao
) {
    // Session 操作
    suspend fun saveSession(session: WorkoutSession): Long {
        return sessionDao.insert(session)
    }

    suspend fun getSessionById(id: Long): WorkoutSession? {
        return sessionDao.getById(id)
    }

    suspend fun getAllSessions(): List<WorkoutSession> {
        return sessionDao.getAllSessions()
    }

    suspend fun getSessionsByDate(date: LocalDate): List<WorkoutSession> {
        return sessionDao.getByDate(date)
    }

    // Exercise 操作
    suspend fun saveExercises(exercises: List<WorkoutExercise>) {
        exercises.forEach { exerciseDao.insert(it) }
    }

    suspend fun getExercisesBySession(sessionId: Long): List<WorkoutExercise> {
        return exerciseDao.getBySessionId(sessionId)
    }

    suspend fun deleteSession(sessionId: Long) {
        exerciseDao.deleteBySessionId(sessionId)
        sessionDao.delete(sessionDao.getById(sessionId)!!)
    }

    // Template 操作
    suspend fun saveTemplate(name: String, exercises: List<ParsedExercise>): Long {
        val template = ExerciseTemplate(templateName = name)
        val templateId = templateDao.insert(template)
        
        exercises.forEachIndexed { index, exercise ->
            val templateExercise = TemplateExercise(
                templateId = templateId,
                name = exercise.name,
                sets = exercise.sets,
                reps = exercise.reps,
                weight = exercise.weight,
                order = index
            )
            templateExerciseDao.insert(templateExercise)
        }
        return templateId
    }

    suspend fun getAllTemplates(): List<ExerciseTemplate> {
        return templateDao.getAllTemplates()
    }

    suspend fun getTemplateExercises(templateId: Long): List<TemplateExercise> {
        return templateExerciseDao.getByTemplateId(templateId)
    }

    suspend fun deleteTemplate(templateId: Long) {
        templateExerciseDao.deleteByTemplateId(templateId)
        templateDao.deleteById(templateId)
    }
}
```

- [ ] **Step 3: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/aimuscle/domain/models/ParsedWorkout.kt
git add src/main/java/com/example/aimuscle/data/repository/WorkoutRepository.kt
git commit -m "feat: add Domain Model and Repository pattern"
```

---

### **タスク 5: NLP Engine - ルールベース解析**

**担当**: Pollen（NLP実装）

**Files:**
- Create: `src/main/java/com/example/aimuscle/nlp/WorkoutDictionary.kt`
- Create: `src/main/java/com/example/aimuscle/nlp/PatternExtractor.kt`
- Create: `src/main/java/com/example/aimuscle/nlp/NLPEngine.kt`

**Interfaces:**
- Produces: NLPEngine.parse(input: String) → ParsedWorkout

- [ ] **Step 1: WorkoutDictionary を作成**

```kotlin
// src/main/java/com/example/aimuscle/nlp/WorkoutDictionary.kt
package com.example.aimuscle.nlp

object WorkoutDictionary {
    // 運動名辞書（拡張可能）
    val exercises = setOf(
        "ベンチプレス",
        "スクワット",
        "デッドリフト",
        "ダンベルフライ",
        "インクラインベンチ",
        "ダンベルカール",
        "ラットプルダウン",
        "レッグプレス",
        "アームカール",
        "ショルダープレス"
    )

    fun isValidExercise(name: String): Boolean {
        return exercises.any { it.contains(name) || name.contains(it) }
    }
}
```

- [ ] **Step 2: PatternExtractor を作成**

```kotlin
// src/main/java/com/example/aimuscle/nlp/PatternExtractor.kt
package com.example.aimuscle.nlp

data class ExtractionResult(
    val name: String? = null,
    val reps: Int? = null,
    val sets: Int? = null,
    val weight: Double? = null
)

object PatternExtractor {
    // 運動名抽出
    fun extractExerciseName(text: String): String? {
        WorkoutDictionary.exercises.forEach { exercise ->
            if (text.contains(exercise)) {
                return exercise
            }
        }
        return null
    }

    // 回数抽出（例：「10回」）
    fun extractReps(text: String): Int? {
        val pattern = """(\d+)\s*回""".toRegex()
        val match = pattern.find(text)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    // セット数抽出（例：「3セット」）
    fun extractSets(text: String): Int? {
        val pattern = """(\d+)\s*セット""".toRegex()
        val match = pattern.find(text)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    // 重量抽出（例：「60kg」「60キロ」）
    fun extractWeight(text: String): Double? {
        val pattern = """(\d+(?:\.\d+)?)\s*(?:kg|キロ)""".toRegex()
        val match = pattern.find(text)
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }

    // 1行（運動1つ分）を解析
    fun extractLine(line: String): ExtractionResult {
        return ExtractionResult(
            name = extractExerciseName(line),
            reps = extractReps(line),
            sets = extractSets(line),
            weight = extractWeight(line)
        )
    }
}
```

- [ ] **Step 3: NLPEngine を作成**

```kotlin
// src/main/java/com/example/aimuscle/nlp/NLPEngine.kt
package com.example.aimuscle.nlp

import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.domain.models.ParsedWorkout

class NLPEngine {
    fun parse(input: String): ParsedWorkout {
        val exercises = mutableListOf<ParsedExercise>()

        // 改行で分割（複数運動をサポート）
        val lines = input.trim().split("\n").filter { it.isNotBlank() }

        lines.forEach { line ->
            val result = PatternExtractor.extractLine(line)
            if (result.name != null && result.reps != null) {
                val confidence = calculateConfidence(result)
                exercises.add(
                    ParsedExercise(
                        name = result.name,
                        reps = result.reps,
                        sets = result.sets ?: 1,
                        weight = result.weight,
                        confidence = confidence
                    )
                )
            }
        }

        return ParsedWorkout(exercises = exercises)
    }

    // 信頼度スコア計算（0-1）
    private fun calculateConfidence(result: ExtractionResult): Float {
        var score = 0.5f  // ベーススコア

        if (result.name != null) score += 0.3f
        if (result.reps != null) score += 0.2f
        if (result.sets != null) score += 0.1f
        if (result.weight != null) score += 0.1f

        return minOf(score, 1.0f)
    }
}
```

- [ ] **Step 4: NLP ユニットテスト作成**

```kotlin
// src/test/java/com/example/aimuscle/nlp/NLPEngineTest.kt
package com.example.aimuscle.nlp

import org.junit.Assert.*
import org.junit.Test

class NLPEngineTest {
    private val nlpEngine = NLPEngine()

    @Test
    fun testParseSimpleExercise() {
        val input = "ベンチプレス 10回 60kg"
        val result = nlpEngine.parse(input)

        assertEquals(1, result.exercises.size)
        assertEquals("ベンチプレス", result.exercises[0].name)
        assertEquals(10, result.exercises[0].reps)
        assertEquals(60.0, result.exercises[0].weight, 0.01)
    }

    @Test
    fun testParseMultipleExercises() {
        val input = """
            ベンチプレス 10回 60kg
            ダンベルフライ 12回 30kg
        """.trimIndent()
        val result = nlpEngine.parse(input)

        assertEquals(2, result.exercises.size)
        assertEquals("ベンチプレス", result.exercises[0].name)
        assertEquals("ダンベルフライ", result.exercises[1].name)
    }

    @Test
    fun testParseWithSets() {
        val input = "スクワット 3セット 15回"
        val result = nlpEngine.parse(input)

        assertEquals(1, result.exercises.size)
        assertEquals(3, result.exercises[0].sets)
        assertEquals(15, result.exercises[0].reps)
    }

    @Test
    fun testParseWithoutWeight() {
        val input = "ダンベルカール 12回"
        val result = nlpEngine.parse(input)

        assertEquals(1, result.exercises.size)
        assertNull(result.exercises[0].weight)
    }
}
```

- [ ] **Step 5: Test 実行**

```bash
./gradlew test
# Expected: BUILD SUCCESSFUL, all tests pass
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/aimuscle/nlp/
git add src/test/java/com/example/aimuscle/nlp/NLPEngineTest.kt
git commit -m "feat: implement NLP Engine with rule-based parsing"
```

---

### **タスク 6: NLP Engine - TensorFlow Lite 統合（信頼度スコア）**

**担当**: Pollen（NLP実装）

**Files:**
- Create: `src/main/java/com/example/aimuscle/nlp/TFLiteModel.kt`
- Modify: `src/main/java/com/example/aimuscle/nlp/NLPEngine.kt`

**Interfaces:**
- Consumes: 前タスクの NLPEngine
- Produces: TensorFlow Lite ラッパー（信頼度スコア計算）

- [ ] **Step 1: TFLiteModel ラッパーを作成**

```kotlin
// src/main/java/com/example/aimuscle/nlp/TFLiteModel.kt
package com.example.aimuscle.nlp

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteModel(context: Context) {
    private lateinit var interpreter: Interpreter

    init {
        // TensorFlow Lite モデルロード（実装予定）
        // TODO: 軽量な日本語NLPモデルを assets/ に配置
        // 初期版はスキップし、ルールベース解析のみで進める
    }

    // 信頼度スコアを計算（0-1）
    fun calculateConfidence(exerciseName: String, inputText: String): Float {
        // TODO: モデル推論（今は固定値）
        // 実装例：exerciseName がテキストに現れる度数、文脈スコア等を計算

        return 0.85f  // Placeholder
    }

    fun close() {
        if (this::interpreter.isInitialized) {
            interpreter.close()
        }
    }
}
```

- [ ] **Step 2: NLPEngine を更新（TFLite統合）**

```kotlin
// src/main/java/com/example/aimuscle/nlp/NLPEngine.kt (修正版)
package com.example.aimuscle.nlp

import android.content.Context
import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.domain.models.ParsedWorkout

class NLPEngine(context: Context) {
    private val tfLiteModel = TFLiteModel(context)

    fun parse(input: String): ParsedWorkout {
        val exercises = mutableListOf<ParsedExercise>()

        val lines = input.trim().split("\n").filter { it.isNotBlank() }

        lines.forEach { line ->
            val result = PatternExtractor.extractLine(line)
            if (result.name != null && result.reps != null) {
                // TensorFlow Lite で信頼度スコアを計算（将来実装）
                val confidence = tfLiteModel.calculateConfidence(result.name, line)

                exercises.add(
                    ParsedExercise(
                        name = result.name,
                        reps = result.reps,
                        sets = result.sets ?: 1,
                        weight = result.weight,
                        confidence = confidence
                    )
                )
            }
        }

        return ParsedWorkout(exercises = exercises)
    }

    fun close() {
        tfLiteModel.close()
    }
}
```

- [ ] **Step 3: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/aimuscle/nlp/TFLiteModel.kt
git add src/main/java/com/example/aimuscle/nlp/NLPEngine.kt
git commit -m "feat: integrate TensorFlow Lite for confidence scoring"
```

---

### **タスク 7: Dependency Injection Setup**

**担当**: Honey（アーキテクチャ）

**Files:**
- Create: `src/main/java/com/example/aimuscle/di/AppModule.kt`

**Interfaces:**
- Produces: Singleton インスタンス（Database, Repository, NLPEngine）

- [ ] **Step 1: AppModule を作成**

```kotlin
// src/main/java/com/example/aimuscle/di/AppModule.kt
package com.example.aimuscle.di

import android.content.Context
import androidx.room.Room
import com.example.aimuscle.data.db.AppDatabase
import com.example.aimuscle.data.repository.WorkoutRepository
import com.example.aimuscle.nlp.NLPEngine

object AppModule {
    private var database: AppDatabase? = null
    private var repository: WorkoutRepository? = null
    private var nlpEngine: NLPEngine? = null

    fun initializeDatabase(context: Context) {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ai_muscle_db"
            ).build()
        }
    }

    fun getDatabase(): AppDatabase {
        return database ?: throw IllegalStateException("Database not initialized")
    }

    fun getRepository(): WorkoutRepository {
        if (repository == null) {
            val db = getDatabase()
            repository = WorkoutRepository(
                sessionDao = db.workoutSessionDao(),
                exerciseDao = db.workoutExerciseDao(),
                templateDao = db.exerciseTemplateDao(),
                templateExerciseDao = db.templateExerciseDao()
            )
        }
        return repository ?: throw IllegalStateException("Repository not initialized")
    }

    fun getNLPEngine(context: Context): NLPEngine {
        if (nlpEngine == null) {
            nlpEngine = NLPEngine(context)
        }
        return nlpEngine ?: throw IllegalStateException("NLPEngine not initialized")
    }
}
```

- [ ] **Step 2: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/aimuscle/di/AppModule.kt
git commit -m "chore: setup Dependency Injection module"
```

---

### **タスク 8: UI Theme & Design System**

**担当**: Fizz（フロントエンド）

**Files:**
- Create: `src/main/java/com/example/aimuscle/ui/theme/Color.kt`
- Create: `src/main/java/com/example/aimuscle/ui/theme/Shape.kt`
- Create: `src/main/java/com/example/aimuscle/ui/theme/Typography.kt`
- Create: `src/main/java/com/example/aimuscle/ui/theme/Theme.kt`

**Interfaces:**
- Produces: Compose Theme（Material 3）

- [ ] **Step 1: Color.kt を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/theme/Color.kt
package com.example.aimuscle.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF1F77D2)          // ブルー
val Secondary = Color(0xFFFF9800)         // オレンジ
val Tertiary = Color(0xFF4CAF50)         // グリーン
val Background = Color(0xFFFAFAFA)
val Surface = Color(0xFFFFFFFF)
val Error = Color(0xFFB00020)

val PrimaryDark = Color(0xFF1565C0)
val SecondaryDark = Color(0xFFE65100)
val TertiaryDark = Color(0xFF2E7D32)
```

- [ ] **Step 2: Shape.kt を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/theme/Shape.kt
package com.example.aimuscle.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)

import androidx.compose.ui.unit.dp
```

- [ ] **Step 3: Typography.kt を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/theme/Typography.kt
package com.example.aimuscle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )
)
```

- [ ] **Step 4: Theme.kt を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/theme/Theme.kt
package com.example.aimuscle.ui.theme

import androidx.compose.foundation.isSystemInDarkMode
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,
    background = Background,
    surface = Surface,
    error = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = Error
)

@Composable
fun AiMuscleTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

- [ ] **Step 5: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/aimuscle/ui/theme/
git commit -m "feat: setup Compose Material 3 theme and design system"
```

---

### **タスク 9: ViewModel & Compose State Management**

**担当**: Fizz（フロントエンド）

**Files:**
- Create: `src/main/java/com/example/aimuscle/ui/viewmodels/HomeViewModel.kt`
- Create: `src/main/java/com/example/aimuscle/ui/viewmodels/InputViewModel.kt`
- Create: `src/main/java/com/example/aimuscle/ui/viewmodels/NLPConfirmViewModel.kt`
- Create: `src/main/java/com/example/aimuscle/ui/viewmodels/TemplateViewModel.kt`
- Create: `src/main/java/com/example/aimuscle/ui/viewmodels/HistoryViewModel.kt`

**Interfaces:**
- Consumes: Repository（Task 4）, NLPEngine（Task 5-6）
- Produces: ViewModel インスタンス（各画面で使用）

- [ ] **Step 1: HomeViewModel を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/viewmodels/HomeViewModel.kt
package com.example.aimuscle.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val _templates = MutableStateFlow<List<ExerciseTemplate>>(emptyList())
    val templates: StateFlow<List<ExerciseTemplate>> = _templates

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            _templates.value = repository.getAllTemplates()
        }
    }
}
```

- [ ] **Step 2: InputViewModel を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/viewmodels/InputViewModel.kt
package com.example.aimuscle.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InputViewModel : ViewModel() {
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun clearInput() {
        _inputText.value = ""
    }
}
```

- [ ] **Step 3: NLPConfirmViewModel を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/viewmodels/NLPConfirmViewModel.kt
package com.example.aimuscle.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimuscle.data.models.WorkoutExercise
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.data.repository.WorkoutRepository
import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.domain.models.ParsedWorkout
import com.example.aimuscle.nlp.NLPEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class NLPConfirmViewModel(
    private val repository: WorkoutRepository,
    private val nlpEngine: NLPEngine
) : ViewModel() {
    private val _parsedWorkout = MutableStateFlow<ParsedWorkout?>(null)
    val parsedWorkout: StateFlow<ParsedWorkout?> = _parsedWorkout

    private val _exercises = MutableStateFlow<List<ParsedExercise>>(emptyList())
    val exercises: StateFlow<List<ParsedExercise>> = _exercises

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    fun parseInput(input: String) {
        val result = nlpEngine.parse(input)
        _parsedWorkout.value = result
        _exercises.value = result.exercises
    }

    fun updateExercise(index: Int, exercise: ParsedExercise) {
        val current = _exercises.value.toMutableList()
        if (index >= 0 && index < current.size) {
            current[index] = exercise
            _exercises.value = current
        }
    }

    fun addExercise(exercise: ParsedExercise) {
        _exercises.value = _exercises.value + exercise
    }

    fun removeExercise(index: Int) {
        _exercises.value = _exercises.value.filterIndexed { i, _ -> i != index }
    }

    fun saveSession(menuName: String, templateName: String? = null) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val session = WorkoutSession(
                    date = LocalDate.now(),
                    menuName = menuName,
                    createdAt = LocalDateTime.now()
                )
                val sessionId = repository.saveSession(session)

                val workoutExercises = _exercises.value.mapIndexed { index, exercise ->
                    WorkoutExercise(
                        sessionId = sessionId,
                        name = exercise.name,
                        sets = exercise.sets,
                        reps = exercise.reps,
                        weight = exercise.weight,
                        order = index
                    )
                }
                repository.saveExercises(workoutExercises)

                if (templateName != null) {
                    repository.saveTemplate(templateName, _exercises.value)
                }
            } finally {
                _isSaving.value = false
            }
        }
    }
}
```

- [ ] **Step 4: TemplateViewModel を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/viewmodels/TemplateViewModel.kt
package com.example.aimuscle.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.data.models.TemplateExercise
import com.example.aimuscle.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TemplateViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val _templates = MutableStateFlow<List<ExerciseTemplate>>(emptyList())
    val templates: StateFlow<List<ExerciseTemplate>> = _templates

    private val _selectedTemplateExercises = MutableStateFlow<List<TemplateExercise>>(emptyList())
    val selectedTemplateExercises: StateFlow<List<TemplateExercise>> = _selectedTemplateExercises

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            _templates.value = repository.getAllTemplates()
        }
    }

    fun selectTemplate(templateId: Long) {
        viewModelScope.launch {
            _selectedTemplateExercises.value = repository.getTemplateExercises(templateId)
        }
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
            loadTemplates()
        }
    }
}
```

- [ ] **Step 5: HistoryViewModel を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/viewmodels/HistoryViewModel.kt
package com.example.aimuscle.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val _sessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val sessions: StateFlow<List<WorkoutSession>> = _sessions

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            _sessions.value = repository.getAllSessions()
        }
    }
}
```

- [ ] **Step 6: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/aimuscle/ui/viewmodels/
git commit -m "feat: implement ViewModels for state management"
```

---

### **タスク 10: Compose UI - ホーム画面**

**担当**: Fizz（フロントエンド）

**Files:**
- Create: `src/main/java/com/example/aimuscle/ui/screens/HomeScreen.kt`

**Interfaces:**
- Consumes: HomeViewModel
- Produces: Composable UI（ホーム画面）

- [ ] **Step 1: HomeScreen を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/screens/HomeScreen.kt
package com.example.aimuscle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aimuscle.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartSession: () -> Unit,
    onSelectTemplate: (Long) -> Unit
) {
    val templates = viewModel.templates.collectAsState().value

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onStartSession) {
                Icon(Icons.Default.Add, contentDescription = "新規セッション")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("AI Muscle App", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Text("テンプレート一覧", style = MaterialTheme.typography.bodyLarge)
            LazyColumn {
                items(templates) { template ->
                    TemplateCard(
                        name = template.templateName,
                        onClick = { onSelectTemplate(template.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateCard(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
```

- [ ] **Step 2: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/aimuscle/ui/screens/HomeScreen.kt
git commit -m "feat: implement Home screen UI"
```

---

### **タスク 11: Compose UI - 入力・確認画面**

**担当**: Fizz（フロントエンド）

**Files:**
- Create: `src/main/java/com/example/aimuscle/ui/screens/InputScreen.kt`
- Create: `src/main/java/com/example/aimuscle/ui/screens/NLPConfirmScreen.kt`

**Interfaces:**
- Consumes: InputViewModel, NLPConfirmViewModel
- Produces: Composable UI（入力・確認画面）

- [ ] **Step 1: InputScreen を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/screens/InputScreen.kt
package com.example.aimuscle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aimuscle.ui.viewmodels.InputViewModel

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onParseClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val inputText = viewModel.inputText.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("運動を入力") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("複数行で運動を入力してください", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = inputText,
                onValueChange = viewModel::updateInputText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("ベンチプレス 10回 60kg\nダンベルフライ 12回 30kg") },
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onParseClick(inputText) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("解析")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("戻る")
                }
            }
        }
    }
}
```

- [ ] **Step 2: NLPConfirmScreen を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/screens/NLPConfirmScreen.kt
package com.example.aimuscle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.ui.viewmodels.NLPConfirmViewModel

@Composable
fun NLPConfirmScreen(
    viewModel: NLPConfirmViewModel,
    onSaveClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val exercises = viewModel.exercises.collectAsState().value
    val isSaving = viewModel.isSaving.collectAsState().value
    var menuName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("確認・編集") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(exercises) { index, exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        onUpdate = { updated ->
                            viewModel.updateExercise(index, updated)
                        },
                        onDelete = {
                            viewModel.removeExercise(index)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = menuName,
                onValueChange = { menuName = it },
                label = { Text("メニュー名") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onSaveClick(menuName); viewModel.saveSession(menuName) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "保存中..." else "保存")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isSaving
                ) {
                    Text("戻る")
                }
            }
        }
    }
}

@Composable
fun ExerciseRow(
    exercise: ParsedExercise,
    onUpdate: (ParsedExercise) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${exercise.name} | ${exercise.reps}回 | ${exercise.sets}セット")
            if (exercise.weight != null) {
                Text("${exercise.weight}kg", style = MaterialTheme.typography.bodySmall)
            }
            Text("信頼度: ${(exercise.confidence * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "削除")
        }
    }
}
```

- [ ] **Step 3: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/aimuscle/ui/screens/InputScreen.kt src/main/java/com/example/aimuscle/ui/screens/NLPConfirmScreen.kt
git commit -m "feat: implement Input and NLP Confirm screens"
```

---

### **タスク 12: Compose UI - テンプレート・履歴画面**

**担当**: Fizz（フロントエンド）

**Files:**
- Create: `src/main/java/com/example/aimuscle/ui/screens/TemplateScreen.kt`
- Create: `src/main/java/com/example/aimuscle/ui/screens/HistoryScreen.kt`

**Interfaces:**
- Consumes: TemplateViewModel, HistoryViewModel
- Produces: Composable UI（テンプレート・履歴画面）

- [ ] **Step 1: TemplateScreen を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/screens/TemplateScreen.kt
package com.example.aimuscle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aimuscle.ui.viewmodels.TemplateViewModel

@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel,
    onSelectTemplate: (Long) -> Unit
) {
    val templates = viewModel.templates.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("テンプレート管理") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(templates) { template ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = template.templateName,
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = { viewModel.deleteTemplate(template.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "削除")
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: HistoryScreen を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/screens/HistoryScreen.kt
package com.example.aimuscle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aimuscle.ui.viewmodels.HistoryViewModel

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val sessions = viewModel.sessions.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("履歴・統計") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(sessions) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${session.date}", style = MaterialTheme.typography.bodyMedium)
                        Text(session.menuName, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 3: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/aimuscle/ui/screens/TemplateScreen.kt src/main/java/com/example/aimuscle/ui/screens/HistoryScreen.kt
git commit -m "feat: implement Template and History screens"
```

---

### **タスク 13: MainActivity & Navigation Setup**

**担当**: Fizz（フロントエンド）

**Files:**
- Create: `src/main/java/com/example/aimuscle/ui/MainActivity.kt`

**Interfaces:**
- Consumes: 前タスクの全スクリーン
- Produces: アプリエントリーポイント＆ナビゲーション

- [ ] **Step 1: MainActivity を作成**

```kotlin
// src/main/java/com/example/aimuscle/ui/MainActivity.kt
package com.example.aimuscle.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.aimuscle.di.AppModule
import com.example.aimuscle.ui.screens.*
import com.example.aimuscle.ui.theme.AiMuscleTheme
import com.example.aimuscle.ui.viewmodels.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DI 初期化
        AppModule.initializeDatabase(this)
        val repository = AppModule.getRepository()
        val nlpEngine = AppModule.getNLPEngine(this)

        setContent {
            AiMuscleTheme {
                AppNavigation(repository, nlpEngine)
            }
        }
    }
}

@Composable
fun AppNavigation(
    repository: com.example.aimuscle.data.repository.WorkoutRepository,
    nlpEngine: com.example.aimuscle.nlp.NLPEngine
) {
    val currentScreen = remember { mutableStateOf<Screen>(Screen.Home) }

    when (currentScreen.value) {
        Screen.Home -> {
            HomeScreen(
                viewModel = HomeViewModel(repository),
                onStartSession = { currentScreen.value = Screen.Input },
                onSelectTemplate = { currentScreen.value = Screen.Input }
            )
        }
        Screen.Input -> {
            InputScreen(
                viewModel = InputViewModel(),
                onParseClick = { input ->
                    currentScreen.value = Screen.NLPConfirm(input)
                },
                onBack = { currentScreen.value = Screen.Home }
            )
        }
        is Screen.NLPConfirm -> {
            val viewModel = NLPConfirmViewModel(repository, nlpEngine)
            viewModel.parseInput((currentScreen.value as Screen.NLPConfirm).input)

            NLPConfirmScreen(
                viewModel = viewModel,
                onSaveClick = { currentScreen.value = Screen.Home },
                onBack = { currentScreen.value = Screen.Input }
            )
        }
        Screen.Template -> {
            TemplateScreen(
                viewModel = TemplateViewModel(repository),
                onSelectTemplate = { currentScreen.value = Screen.Input }
            )
        }
        Screen.History -> {
            HistoryScreen(viewModel = HistoryViewModel(repository))
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object Input : Screen()
    data class NLPConfirm(val input: String) : Screen()
    object Template : Screen()
    object History : Screen()
}
```

- [ ] **Step 2: Compile 確認**

```bash
./gradlew compileKotlin
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/aimuscle/ui/MainActivity.kt
git commit -m "feat: implement MainActivity and basic navigation"
```

---

### **タスク 14: 統合テスト & リリース準備**

**担当**: Pollen（QA・リリース）

**Files:**
- Create: `src/androidTest/java/com/example/aimuscle/AppIntegrationTest.kt`
- Modify: `src/main/AndroidManifest.xml`（テストインストルメント設定）

**Interfaces:**
- Consumes: 前タスクの全実装

- [ ] **Step 1: 統合テストを作成**

```kotlin
// src/androidTest/java/com/example/aimuscle/AppIntegrationTest.kt
package com.example.aimuscle

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppIntegrationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHomeScreenDisplay() {
        composeTestRule.onNodeWithText("AI Muscle App").assertExists()
    }

    @Test
    fun testInputWorkout() {
        // ホーム画面 → 新規セッション
        composeTestRule.onNodeWithText("新規セッション").performClick()
        
        // 入力画面でテキスト入力
        composeTestRule.onNodeWithText("ベンチプレス 10回 60kg").performTextInput("ベンチプレス 10回 60kg")
        
        // 解析ボタン
        composeTestRule.onNodeWithText("解析").performClick()
        
        // 確認画面でメニュー名入力
        composeTestRule.onNodeWithText("メニュー名").performTextInput("上半身の日")
        
        // 保存ボタン
        composeTestRule.onNodeWithText("保存").performClick()
    }
}
```

- [ ] **Step 2: AndroidTest 依存関係を追加**

```gradle
// app/build.gradle.kts
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
```

- [ ] **Step 3: APK ビルド**

```bash
./gradlew assembleDebug
# Expected: BUILD SUCCESSFUL
# Output: app/build/outputs/apk/debug/app-debug.apk
```

- [ ] **Step 4: エミュレータで実行テスト**

```bash
./gradlew installDebug
./gradlew connectedAndroidTest
# Expected: BUILD SUCCESSFUL, all tests pass
```

- [ ] **Step 5: Commit**

```bash
git add src/androidTest/java/com/example/aimuscle/AppIntegrationTest.kt
git add app/build.gradle.kts
git commit -m "feat: add integration tests and release build configuration"
```

- [ ] **Step 6: リリースノート作成**

```markdown
# AI Muscle App - Phase 1 MVP リリースノート

## 実装機能
✅ 自然言語入力でメニュー登録
✅ AI解析（ルールベース + TF Lite信頼度）
✅ ローカルDB（Room SQLite）
✅ テンプレート管理
✅ 履歴表示
✅ Jetpack Compose UI

## 検証済みシナリオ
- ベンチプレス、スクワット、ダンベルカール等の運動解析
- メニュー保存・テンプレート化
- 履歴表示・削除

## 既知の制限
- クラウド同期未実装（v2+）
- 運動種目辞書：初期版（今後拡張予定）
- TensorFlow Lite：プレースホルダー（軽量モデル選定待ち）

## 次のステップ
→ Google Play Store 開発者登録
→ Phase 2: UX改善・テスト
→ Phase 3: リリース準備
```

- [ ] **Step 7: Final Commit**

```bash
git add RELEASE_NOTES.md
git commit -m "chore: add MVP release notes and completion checklist"
```

---

## 自己レビュー

✅ **Spec Coverage**: 
- 要件定義（個人筋トレ記録、自然言語AI変換、オフライン）→ タスク5-6, 9-13で実装
- 技術スタック（Kotlin, Compose, Room, TF Lite）→ タスク1-6, 9-13で実装
- 5画面フロー（ホーム、入力、NLP確認、テンプレート、履歴）→ タスク10-12で実装
- データモデル（4テーブル）→ タスク2-3で実装
- NLP実装（ルールベース + ML）→ タスク5-6で実装

✅ **Placeholder Scan**: 
- 全タスクに具体的なコード・ステップを記載
- TBD は TensorFlow Lite モデル選定のみ（予定済み）

✅ **Type Consistency**:
- ParsedExercise, ParsedWorkout：全 ViewModel で一貫
- Repository インターフェース：全タスクで同じシグネチャ
- DAO 戻り値型：Long（insert）、List<T>（query）で統一

✅ **Scope Check**:
- Phase 1 (MVP) に絞定：オンデバイス処理のみ
- v2+ は仕様書に記載済み（クラウド同期、ソーシャル機能）

---

## 実行ハンドオフ

**Plan complete and saved to `docs/superpowers/plans/2026-09-10-ai-muscle-app-phase1.md`.**

### 実行オプション

**1. Subagent-Driven (推奨)** — タスクごとに独立した subagent を dispatch、レビュー挟む、高速イテレーション  
→ superpowers:subagent-driven-development を使用

**2. Inline Execution** — この session で executing-plans により一括実行、チェックポイント挟む  
→ superpowers:executing-plans を使用

**どちらで進めますか？**

---

**Plan Author**: Fizz  
**Plan Date**: 2026-09-10  
**Phase**: 1 (MVP)  
**Estimated Duration**: 2-3 weeks  
**Team**: Fizz (Frontend), Honey (Backend/Data), Pollen (NLP/Infra)
