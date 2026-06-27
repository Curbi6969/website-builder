package com.rise.app.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Tab { HOME, RUST, MOOD, STATS }
enum class Panic { NONE, MENU, BREATHING, WATER, GAME, REASONS, DONE }
enum class Game { NONE, BUBBLES, GROUND, TAP }
enum class HeroStyle { PLANT, NUMBER }

data class TaskItem(
    val id: Int,
    val time: String,
    val label: String,
    val icon: String,
    val done: Boolean,
)

data class Course(
    val title: String,
    val sub: String,
    val len: String,
    val min: Int,
    val icon: String,
    val bg: Color,
)

data class PlayerContent(
    val title: String,
    val sub: String,
    val icon: String,
    val min: Int,
)

data class MoodDef(
    val key: String,
    val face: String,
    val label: String,
    val bg: Color,
    val note: String,
)

data class ReminderDef(
    val key: String,
    val icon: String,
    val label: String,
    val sub: String,
    val editable: Boolean,
)

data class TriggerDef(
    val label: String,
    val icon: String,
    val pct: Int,
    val color: Color,
)

data class GroundStep(
    val n: Int,
    val label: String,
    val icon: String,
)

data class ActivityDef(
    val icon: String,
    val label: String,
)

data class RoutineChip(val id: String, val name: String)

/** Namespaced task id for a catalogue routine's step, so completions stay unique per routine. */
fun routineTaskId(routineId: String, index: Int): Int = routineId.hashCode() * 100 + index

const val PERSONAL_ROUTINE = "personal"

data class Bubble(
    val id: Int,
    val size: Int,
    val x: Int,
    val y: Int,
    val color: Color,
)

data class RiseUiState(
    val tab: Tab = Tab.HOME,
    val heroStyle: HeroStyle = HeroStyle.PLANT,
    val userName: String = "",
    val soberSince: LocalDate? = null,
    val tasks: List<TaskItem> = RiseDefaults.tasks,
    val moodPicked: String? = null,
    val moodCalendar: Map<Int, String> = emptyMap(),
    val urgeThisWeek: Int = 0,
    val urgeWeekly: List<Int> = emptyList(),
    val checkedInToday: Boolean = false,
    val showCheckin: Boolean = false,
    val journalText: String = "",
    val reasons: List<String> = RiseDefaults.reasons,
    val reminders: Map<String, Boolean> = RiseDefaults.reminderState,
    val panic: Panic = Panic.NONE,
    val game: Game = Game.NONE,
    val bubbles: List<Bubble> = emptyList(),
    val bubbleSeed: Int = 0,
    val gameScore: Int = 0,
    val groundStep: Int = 0,
    val groundFilled: Int = 0,
    val groundDone: Boolean = false,
    val tapScore: Int = 0,
    val tapTarget: Int = 0,
    val tapTime: Int = 20,
    val tapRunning: Boolean = false,
    val tapOver: Boolean = false,
    val tapRecord: Int = 0,
    val player: PlayerContent? = null,
    val playerTotal: Int = 1,
    val playerLeft: Int = 0,
    val playerPlaying: Boolean = false,
    val playerDone: Boolean = false,
    val bored: Boolean = false,
    val showSettings: Boolean = false,
    val toast: String? = null,
    // ----- Inspiratie / routines -----
    val activeRoutineId: String = PERSONAL_ROUTINE,
    val addedRoutineIds: List<String> = emptyList(),
    val routineTaskDone: Map<Int, Boolean> = emptyMap(),
    val inspoCategory: RoutineCategory? = null,
    val openRoutine: String? = null,
    val openSelfCheck: String? = null,
) {
    val doneCount: Int get() = tasks.count { it.done }
    val taskTotal: Int get() = tasks.size

    /** Chips shown on Home: Persoonlijk first, then any added catalogue routines. */
    val homeChips: List<RoutineChip> get() =
        listOf(RoutineChip(PERSONAL_ROUTINE, "Persoonlijk")) +
            addedRoutineIds.mapNotNull { id ->
                RoutineCatalog.routines.find { it.id == id }?.let { RoutineChip(it.id, it.name) }
            }

    /** Tasks for the active chip: Persoonlijk = the seeded [tasks]; else the catalogue routine's steps. */
    val activeRoutineTasks: List<TaskItem> get() =
        if (activeRoutineId == PERSONAL_ROUTINE) tasks
        else RoutineCatalog.routines.find { it.id == activeRoutineId }?.let { r ->
            r.steps.mapIndexed { i, step ->
                val tid = routineTaskId(r.id, i)
                TaskItem(tid, step.time, step.label, step.icon, routineTaskDone[tid] ?: false)
            }
        } ?: tasks

    val activeDoneCount: Int get() = activeRoutineTasks.count { it.done }
    val activeTaskTotal: Int get() = activeRoutineTasks.size
    val streak: Int get() = soberSince
        ?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt().coerceAtLeast(0) }
        ?: 0
    val urgePct: Int get() = 92
    val displayName: String get() = userName.trim().ifBlank { "maat" }
    val plantGenerations: Int get() = maxOf(2, minOf(6, 2 + streak / 3))
}
