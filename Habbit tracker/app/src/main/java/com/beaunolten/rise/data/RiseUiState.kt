package com.beaunolten.rise.data

import androidx.compose.ui.graphics.Color

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
    val userName: String = "Beau",
    val tasks: List<TaskItem> = RiseDefaults.tasks,
    val moodPicked: String? = null,
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
) {
    val doneCount: Int get() = tasks.count { it.done }
    val taskTotal: Int get() = tasks.size
    val streak: Int get() = 14
    val urgePct: Int get() = 92
    val displayName: String get() = userName.trim().ifBlank { "maat" }
    val plantGenerations: Int get() = maxOf(2, minOf(6, 2 + streak / 3))
}
