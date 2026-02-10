# Implementation Plan - Bug Fixes and Code Improvements for AntiProcast

This plan addresses several identified bugs, data inconsistencies, and navigation issues found during the code review of the AntiProcast project.

## 1. Navigation Fixes

- [x] **Bug**: App shortcuts for "Focus" and "My Day" are ignored.
- [x] **Action**: Update `MainScreen.kt` to handle `OPEN_FOCUS` and `OPEN_MY_DAY` intents in the `LaunchedEffect`.

## 2. Data Consistency & Logic Fixes

- [x] **Bug**: `TaskRepositoryImpl` uses `System.currentTimeMillis()` for completed dates while `PlannerViewModel` expects `startOfDay`. This breaks completion status for repeating tasks.
- [x] **Action**: Update `TaskRepositoryImpl.kt` (Logic moved to ViewModel) to use start-of-day timestamps.

## 3. Code Cleanup

- **Optimization**: Remove redundant document ID copying in `TaskRepositoryImpl.kt`.
- **Typo**: Fix "Syync" typo in `PlannerScreen.kt`.

## 4. UI Improvements

- [x] **Improvement**: Enhance the theme toggle in `SettingsScreen.kt` or `PlannerScreen.kt` to be more dynamic if possible.

---

### Files to Modify

1. `app/src/main/java/com/katchy/focuslive/ui/main/MainScreen.kt`
2. `app/src/main/java/com/katchy/focuslive/data/repository/TaskRepository.kt`
3. `app/src/main/java/com/katchy/focuslive/ui/planner/PlannerScreen.kt`
