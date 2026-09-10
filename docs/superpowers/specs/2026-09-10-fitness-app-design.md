# Fitness Training App — Design Specification

**Date:** 2026-09-10  
**Project:** AI Muscle App  
**Status:** Design approved by Fizz, Honey, Pollen  
**Target Platform:** Android  
**Infrastructure:** Free tier (offline-first)

---

## 1. Overview & Goals

### Purpose
Personal fitness training app for individuals to:
1. Record daily workout sessions with natural language input
2. Save and reuse workout templates ("Monday upper body," "Friday lower body")
3. Track progress with simple statistics

### Success Criteria
- ✅ Users can input exercises in natural Japanese (e.g., "ベンチプレス 10回 60kg") without cumbersome form filling
- ✅ AI converts natural language to structured exercise data
- ✅ Users can save and reuse workout templates
- ✅ App works offline (gym environments without Wi-Fi)
- ✅ Free infrastructure tier (no significant cloud costs)

### User Flow Philosophy
**Problem:** "All natural language input is cumbersome"  
**Solution:** Hybrid approach
- **Step 1:** Quick template selection (simplicity)
- **Step 2:** Bulk natural language input (efficiency)
- **Step 3:** AI auto-parsing + user confirmation (accuracy)
- **Step 4:** Template creation + reuse (scalability)

---

## 2. Technical Stack

### Frontend (Android)
- **Language:** Kotlin (type-safe, Android standard)
- **UI Framework:** Jetpack Compose (modern, maintainable)
- **Local Database:** Room (SQLite wrapper, Jetpack-integrated)
- **State Management:** ViewModel + StateFlow (Jetpack)
- **NLP Processing:** TensorFlow Lite Lite (on-device, offline)
  - Rule-based parsing with regex + dictionary lookup
  - Example: "ベンチプレス 10回 60kg" → extract exercise name, reps, weight
  - Lightweight enough to run on Android devices
  - Future optimization: fine-tuned lightweight model if needed

### Backend (Optional, Post-MVP)
- Google Drive API (100GB free tier) or Firebase Realtime DB (Spark plan free)
- For backup/sync only — not required for MVP
- MVP will use local-only storage

### Development Tools
- Android Studio (latest)
- Gradle for build
- JUnit + Compose UI tests for testing

---

## 3. Database Design

### Entity Relationship Diagram

```
WorkoutSession (1) ──── (Many) WorkoutExercise
ExerciseTemplate (1) ──── (Many) TemplateExercise
```

### Table Schemas

#### **WorkoutSession**
Represents a single workout session on a given date.

| Column       | Type          | Constraints  | Notes                                   |
|--------------|---------------|--------------|----------------------------------------|
| id           | Long          | PK, AUTO     | Unique session identifier              |
| date         | LocalDate     | NOT NULL     | Date of workout                        |
| menuName     | String        | NOT NULL     | User-facing name (e.g., "Monday Upper") |
| notes        | String        | NULLABLE     | Additional session notes               |
| createdAt    | LocalDateTime | NOT NULL     | Timestamp of creation                  |

---

#### **WorkoutExercise**
Individual exercises within a session.

| Column    | Type          | Constraints        | Notes                        |
|-----------|---------------|-------------------|------------------------------|
| id        | Long          | PK, AUTO          | Unique exercise identifier  |
| sessionId | Long          | FK→WorkoutSession  | Parent session              |
| name      | String        | NOT NULL          | Exercise name ("ベンチプレス") |
| sets      | Int           | NOT NULL          | Number of sets              |
| reps      | Int           | NOT NULL          | Reps per set                |
| weight    | Double?       | NULLABLE          | Weight in kg                |
| notes     | String?       | NULLABLE          | Exercise-specific notes     |
| order     | Int           | NOT NULL          | Display/execution order     |

---

#### **ExerciseTemplate**
Reusable workout templates (e.g., "Monday Upper Body").

| Column       | Type          | Constraints | Notes                     |
|--------------|---------------|-------------|--------------------------|
| id           | Long          | PK, AUTO    | Unique template id        |
| templateName | String        | NOT NULL    | User-friendly name        |
| createdAt    | LocalDateTime | NOT NULL    | Template creation date    |

---

#### **TemplateExercise**
Exercises that belong to a template.

| Column     | Type   | Constraints        | Notes                      |
|------------|---------|--------------------|--------------------------|
| id         | Long    | PK, AUTO          | Unique record id          |
| templateId | Long    | FK→ExerciseTemplate | Parent template           |
| name       | String  | NOT NULL          | Exercise name             |
| sets       | Int     | NOT NULL          | Default sets              |
| reps       | Int     | NOT NULL          | Default reps              |
| weight     | Double? | NULLABLE          | Default weight in kg      |
| order      | Int     | NOT NULL          | Display order in template |

---

### Normalized Design Rationale
- **1NF:** All columns are atomic (no repeating groups)
- **2NF & 3NF:** Foreign keys properly establish relationships without redundancy
- **Scalability:** Easy to extend (add sets tracking, rest times, body weight exercises, etc.)

### Usage Example
1. User creates **ExerciseTemplate** named "月曜の上半身" with exercises (ベンチプレス, ダンベル)
2. Next Monday, user selects template → creates new **WorkoutSession**
3. User inputs natural language → NLP parses into **WorkoutExercise** records
4. User can edit/confirm → saves to DB
5. Can save this session as a new template or update existing one

---

## 4. UI/UX Flow (5-Screen Architecture)

### Screen 1️⃣: Home Screen
**Purpose:** Quick access to today's workout or template selection

**Layout:**
- Header: "Today's Session" (if exists)
- Central CTA: "Start New Session" button (floating action)
- Bottom navigation: "Templates" tab | "History" tab

**Interactions:**
- Tap "Start New Session" → go to Screen 2
- Select template → quick-load it
- View today's workout summary

---

### Screen 2️⃣: Natural Language Input Screen
**Purpose:** Bulk input of exercises in Japanese

**Layout:**
- Title: "Enter exercises"
- Large multi-line text field with placeholder: "ベンチプレス 10回 60kg、ダンベル 12回 30kg"
- "Parse" button (green)
- Suggested exercises below (optional autocomplete)

**Interactions:**
- Type or paste multiple exercises (newline or comma-separated)
- Tap "Parse" → NLP processing → next screen
- Support batch input (efficiency)

**NLP Input Examples:**
```
ベンチプレス 10回 60kg
ダンベルカール 12回 30kg
スクワット 15回 (重量なし)
```

---

### Screen 3️⃣: NLP Confirmation & Edit Screen
**Purpose:** Validate AI-extracted data and allow manual correction

**Layout:**
- Title: "Confirm exercises"
- List of extracted exercises:
  - [ ] Exercise Name | Sets | Reps | Weight
  - [ ] (repeat for each)
- "Add Exercise" button
- "Save Session" button

**Interactions:**
- Swipe to delete an exercise
- Tap exercise to edit (name, sets, reps, weight)
- Add new exercise manually if AI missed one
- Tap "Save Session" → persists to WorkoutSession + WorkoutExercise tables

**Error Handling:**
- If NLP fails entirely, show empty list → user manually adds all exercises
- Toast notification for parse errors (non-blocking)

---

### Screen 4️⃣: Template Management Screen
**Purpose:** Create, view, edit, and delete reusable templates

**Layout:**
- Title: "My Templates"
- List of templates:
  - Template name (tap to view details)
  - Long-press options: Edit | Delete | Use
- "New Template" FAB

**Interactions:**
- Tap template → shows exercises in that template
- Tap "Use Template" → create new session from it
- Long-press → delete or edit
- "New Template" → option to create from scratch or from today's session

**Template Persistence:**
- After saving a session, offer "Save as Template" dialog
- Template name input → persists to ExerciseTemplate + TemplateExercise

---

### Screen 5️⃣: History & Statistics Screen
**Purpose:** View past sessions and simple metrics

**Layout:**
- Top: Calendar view (tap date to filter)
- Middle: Filtered session list (date, template name, exercise count)
- Bottom: Simple stats (e.g., "Bench Press max: 80kg", "Sessions this month: 12")

**Interactions:**
- Tap session → view exercise details
- Tap date range → update stats
- Swipe session → delete option

**Statistics MVP:**
- Total workouts this month
- Most recent exercises per muscle group
- Personal records (max weight lifted)

---

## 5. Implementation Architecture

### Component Breakdown

```
┌─────────────────────────────────────────┐
│      Jetpack Compose UI Layer           │
│  (5 screens, state management)          │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  ViewModel + StateFlow                   │
│  (business logic, state holders)        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Repository Pattern                     │
│  (data access abstraction)              │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴────────┐
       │                │
   ┌───▼────┐     ┌─────▼─────┐
   │  Room   │     │ NLP Layer │
   │  (DB)   │     │ (TF Lite) │
   └─────────┘     └───────────┘
```

### NLP Module
- **Input:** Raw text ("ベンチプレス 10回 60kg")
- **Processing:**
  1. Tokenize by delimiter (space, comma, newline)
  2. Regex patterns for reps ("〜回", "x〜", "〜rep"), weight ("〜kg", "〜kgs")
  3. Dictionary lookup for exercise names (Bench press variants, etc.)
  4. Return structured output: `{exercise, sets, reps, weight}`
- **Output:** List of Exercise objects

---

## 6. Data Flow (User Perspective)

```
User selects template
       ↓
User enters natural language
       ↓
NLP parses text
       ↓
Confirmation screen (user edits if needed)
       ↓
Save to WorkoutSession + WorkoutExercise
       ↓
Option: "Save as Template" → ExerciseTemplate + TemplateExercise
       ↓
History view shows new session
```

---

## 7. Error Handling & Edge Cases

### NLP Parsing Failures
- **No matches:** Show empty exercise list, user adds manually
- **Partial matches:** Show what was parsed, highlight uncertain fields
- **Invalid format:** Graceful degradation (don't crash)

### User Validation
- User can always edit/delete extracted exercises before saving
- Confirmation screen acts as final validation gate

### Database Constraints
- Foreign key constraints prevent orphaned records
- Room handles transaction safety

---

## 8. Future Enhancements (Post-MVP)

- Cloud sync (Google Drive/Firebase)
- Social sharing of templates
- Advanced statistics (graphs, trends)
- Body weight exercises (no weight field)
- Rest time tracking
- Video/photo attachments
- Integration with fitness wearables

---

## 9. Success Metrics

- ✅ App launches in under 2 seconds (offline-first)
- ✅ NLP correctly parses 90%+ of common exercise inputs
- ✅ Users can save/reuse templates within 2 taps
- ✅ Zero crashes related to NLP failures
- ✅ Session data persists across app restarts

---

## 10. Google Play Store Release

### Pre-Release Checklist
- [ ] App signing certificate
- [ ] App listing (screenshots, description)
- [ ] Privacy policy
- [ ] Terms of service
- [ ] Developer account registration ($25 one-time fee)
- [ ] Beta testing (optional, 2-4 weeks recommended)
- [ ] Rollout strategy (staged vs. immediate)

### Post-Release
- Monitor crash reports
- Collect user feedback
- Iterate on NLP accuracy
- Plan v2 features

---

## 11. Decisions & Trade-offs

| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| **Offline-first** | Gym = no Wi-Fi | Can't sync real-time |
| **TensorFlow Lite** | Free, on-device | Lower accuracy vs. cloud API |
| **Rule-based NLP MVP** | Fast to ship | Handle only simple inputs initially |
| **Room (SQLite)** | Built-in to Jetpack | Limited to single device |
| **Jetpack Compose** | Modern, maintainable | Learning curve for some devs |
| **Template reuse** | Solves "full NLP is tedious" | Requires UI sophistication |

---

## 12. Scope Boundaries (MVP)

### IN SCOPE
- ✅ Natural language input for exercises
- ✅ Batch entry (multiple exercises at once)
- ✅ Offline data persistence
- ✅ Template creation and reuse
- ✅ Simple statistics (max weight, count)
- ✅ Android app only

### OUT OF SCOPE (v2+)
- ❌ iOS version
- ❌ Cloud sync
- ❌ Social features
- ❌ Wearable integration
- ❌ Video tutorials
- ❌ Real-time web dashboard

---

## 13. Role Assignments (Agent Collaboration)

| Role | Responsibility |
|------|-----------------|
| **Fizz** | Android native development, Jetpack integration, Play Store setup |
| **Honey** | NLP module, TensorFlow Lite integration, algorithm refinement |
| **Pollen** | Architecture planning, spec writing, integration testing |

---

## Approval Checklist

- [x] All 3 design sections reviewed
- [x] Fizz approved architecture
- [x] Honey approved architecture
- [x] Pollen approved architecture
- [x] No contradictions in scope/design
- [x] Database design supports user flows
- [x] Deliverables defined
- [ ] taichi reviews spec (pending)

---

**Next Step:** taichi reviews this spec, then invoke `writing-plans` skill to create detailed implementation plan.
