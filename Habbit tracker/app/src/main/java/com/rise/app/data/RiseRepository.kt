package com.rise.app.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.OffsetDateTime

// ----- row models (snake_case columns ↔ camelCase Kotlin) -----

@Serializable
private data class ProfileRow(
    val name: String = "",
    @SerialName("sober_since") val soberSince: String? = null,
    @SerialName("hero_style") val heroStyle: String = "plant",
    val onboarded: Boolean = false,
    @SerialName("active_routine") val activeRoutine: String = "personal",
)

@Serializable
private data class TriggerRow(
    @SerialName("user_id") val userId: String,
    val key: String,
    val position: Int,
)

@Serializable
private data class ReasonRow(
    @SerialName("user_id") val userId: String,
    val text: String,
    val position: Int,
)

@Serializable
private data class ReminderRow(
    @SerialName("user_id") val userId: String,
    val key: String,
    val enabled: Boolean,
)

@Serializable
private data class GameRow(
    @SerialName("user_id") val userId: String,
    @SerialName("tap_record") val tapRecord: Int,
)

@Serializable
private data class JournalRow(
    @SerialName("user_id") val userId: String,
    val text: String,
)

@Serializable
private data class TaskRow(
    @SerialName("user_id") val userId: String,
    val date: String,
    @SerialName("task_id") val taskId: Int,
    val done: Boolean,
    @SerialName("routine_id") val routineId: String = "personal",
)

@Serializable
private data class UserRoutineRow(
    @SerialName("user_id") val userId: String,
    @SerialName("routine_id") val routineId: String,
    val position: Int,
)

@Serializable
private data class MoodRow(
    @SerialName("user_id") val userId: String,
    val date: String,
    @SerialName("mood_key") val moodKey: String,
)

@Serializable
private data class UrgeRow(
    @SerialName("user_id") val userId: String,
    val method: String? = null,
    val trigger: String? = null,
    val beaten: Boolean = true,
)

@Serializable
private data class UrgeReadRow(
    @SerialName("created_at") val createdAt: String,
    val trigger: String? = null,
)

@Serializable
private data class CheckinRow(
    @SerialName("user_id") val userId: String,
    val date: String,
    val success: Boolean,
    val trigger: String? = null,
)

/** Everything loaded from Supabase at app start. */
data class RemoteState(
    val name: String,
    val soberSince: LocalDate,
    val heroStyle: HeroStyle,
    val reasons: List<String>,
    val reminders: Map<String, Boolean>,
    val tapRecord: Int,
    val todayTasksDone: Map<Int, Boolean>,
    val todayMood: String?,
    val moodCalendar: Map<Int, String>,
    val urgeThisWeek: Int,
    val urgeWeekly: List<Int>,
    val checkedInToday: Boolean,
    val addedRoutineIds: List<String>,
    val activeRoutineId: String,
    val routineTaskDone: Map<Int, Boolean>,
)

/**
 * Reads and writes the user's data in Supabase. Every call is scoped to the
 * authenticated user (RLS enforces `auth.uid() = user_id`), so the user id is
 * attached to every insert. The app is only reachable behind a session, so the
 * id is always present here.
 */
class RiseRepository {

    private val uid: String? get() = supabase.auth.currentUserOrNull()?.id

    /** Loads the whole user profile + settings; seeds design defaults on first run. */
    suspend fun load(): RemoteState? {
        val uid = uid ?: return null

        val profile = supabase.from("profiles")
            .select { filter { eq("id", uid) } }
            .decodeSingleOrNull<ProfileRow>()

        // Establish a sober-start date the first time so the streak can be real.
        val soberSince = profile?.soberSince ?: LocalDate.now().toString().also { today ->
            supabase.from("profiles").update({ set("sober_since", today) }) {
                filter { eq("id", uid) }
            }
        }

        var reasons = supabase.from("reasons")
            .select {
                filter { eq("user_id", uid) }
                order("position", Order.ASCENDING)
            }
            .decodeList<ReasonRow>()
            .map { it.text }
        if (reasons.isEmpty()) {
            saveReasons(RiseDefaults.reasons)
            reasons = RiseDefaults.reasons
        }

        var reminders = supabase.from("reminders")
            .select { filter { eq("user_id", uid) } }
            .decodeList<ReminderRow>()
            .associate { it.key to it.enabled }
        if (reminders.isEmpty()) {
            RiseDefaults.reminderState.forEach { (k, v) -> setReminder(k, v) }
            reminders = RiseDefaults.reminderState
        }

        val tapRecord = supabase.from("game_records")
            .select { filter { eq("user_id", uid) } }
            .decodeSingleOrNull<GameRow>()
            ?.tapRecord ?: 0

        val today = LocalDate.now()
        val todayStr = today.toString()

        val todayTasksDone = supabase.from("task_completions")
            .select {
                filter { eq("user_id", uid); eq("date", todayStr) }
            }
            .decodeList<TaskRow>()
            .associate { it.taskId to it.done }

        val monthStart = today.withDayOfMonth(1).toString()
        val moodRows = supabase.from("mood_logs")
            .select {
                filter { eq("user_id", uid); gte("date", monthStart) }
            }
            .decodeList<MoodRow>()
        val moodCalendar = moodRows.associate { LocalDate.parse(it.date).dayOfMonth to it.moodKey }
        val todayMood = moodRows.firstOrNull { it.date == todayStr }?.moodKey

        val urgeDates = supabase.from("urge_events")
            .select {
                filter { eq("user_id", uid); eq("beaten", true) }
            }
            .decodeList<UrgeReadRow>()
            .map { OffsetDateTime.parse(it.createdAt).toLocalDate() }
        val urgeWeekly = weeklyCounts(urgeDates, today)

        val checkedInToday = supabase.from("daily_checkins")
            .select { filter { eq("user_id", uid); eq("date", todayStr) } }
            .decodeSingleOrNull<CheckinRow>() != null

        val addedRoutineIds = supabase.from("user_routines")
            .select {
                filter { eq("user_id", uid) }
                order("position", Order.ASCENDING)
            }
            .decodeList<UserRoutineRow>()
            .map { it.routineId }

        return RemoteState(
            name = profile?.name.orEmpty(),
            soberSince = LocalDate.parse(soberSince),
            heroStyle = if (profile?.heroStyle == "number") HeroStyle.NUMBER else HeroStyle.PLANT,
            reasons = reasons,
            reminders = reminders,
            tapRecord = tapRecord,
            todayTasksDone = todayTasksDone,
            todayMood = todayMood,
            moodCalendar = moodCalendar,
            urgeThisWeek = urgeWeekly.last(),
            urgeWeekly = urgeWeekly,
            checkedInToday = checkedInToday,
            addedRoutineIds = addedRoutineIds,
            activeRoutineId = profile?.activeRoutine ?: "personal",
            routineTaskDone = todayTasksDone,
        )
    }

    /** Counts of dates per week for the last 4 weeks (Mon-start), oldest → current. */
    private fun weeklyCounts(dates: List<LocalDate>, today: LocalDate): List<Int> {
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        return (0..3).map { w ->
            val start = monday.minusWeeks((3 - w).toLong())
            val end = start.plusWeeks(1)
            dates.count { !it.isBefore(start) && it.isBefore(end) }
        }
    }

    suspend fun setName(value: String) {
        val uid = uid ?: return
        supabase.from("profiles").update({ set("name", value) }) { filter { eq("id", uid) } }
    }

    suspend fun resetSoberSince() {
        val uid = uid ?: return
        supabase.from("profiles").update({ set("sober_since", LocalDate.now().toString()) }) {
            filter { eq("id", uid) }
        }
    }

    /** Reasons are a small ordered list — replace the set on every change. */
    suspend fun saveReasons(list: List<String>) {
        val uid = uid ?: return
        supabase.from("reasons").delete { filter { eq("user_id", uid) } }
        if (list.isNotEmpty()) {
            supabase.from("reasons").insert(list.mapIndexed { i, t -> ReasonRow(uid, t, i) })
        }
    }

    suspend fun setReminder(key: String, enabled: Boolean) {
        val uid = uid ?: return
        supabase.from("reminders").upsert(ReminderRow(uid, key, enabled))
    }

    suspend fun setTapRecord(value: Int) {
        val uid = uid ?: return
        supabase.from("game_records").upsert(GameRow(uid, value))
    }

    suspend fun addJournal(text: String) {
        val uid = uid ?: return
        supabase.from("journal_entries").insert(JournalRow(uid, text))
    }

    suspend fun setTaskDone(taskId: Int, done: Boolean, routineId: String = "personal") {
        val uid = uid ?: return
        supabase.from("task_completions")
            .upsert(TaskRow(uid, LocalDate.now().toString(), taskId, done, routineId)) {
                onConflict = "user_id,date,task_id"
            }
    }

    suspend fun addRoutine(routineId: String, position: Int) {
        val uid = uid ?: return
        supabase.from("user_routines")
            .upsert(UserRoutineRow(uid, routineId, position)) { onConflict = "user_id,routine_id" }
    }

    suspend fun removeRoutine(routineId: String) {
        val uid = uid ?: return
        supabase.from("user_routines").delete { filter { eq("user_id", uid); eq("routine_id", routineId) } }
    }

    suspend fun setActiveRoutine(routineId: String) {
        val uid = uid ?: return
        supabase.from("profiles").update({ set("active_routine", routineId) }) { filter { eq("id", uid) } }
    }

    suspend fun setMood(moodKey: String) {
        val uid = uid ?: return
        supabase.from("mood_logs")
            .upsert(MoodRow(uid, LocalDate.now().toString(), moodKey)) {
                onConflict = "user_id,date"
            }
    }

    suspend fun addUrge(method: String?, trigger: String?) {
        val uid = uid ?: return
        supabase.from("urge_events").insert(UrgeRow(uid, method, trigger, true))
    }

    suspend fun isOnboarded(): Boolean {
        val uid = uid ?: return true
        return supabase.from("profiles")
            .select { filter { eq("id", uid) } }
            .decodeSingleOrNull<ProfileRow>()
            ?.onboarded ?: false
    }

    /** Persists the whole wizard in one go and flips the onboarded flag. */
    suspend fun saveOnboarding(
        name: String,
        focus: String?,
        soberSince: LocalDate,
        reasons: List<String>,
        triggers: List<String>,
        reminderTime: String?,
    ) {
        val uid = uid ?: return
        supabase.from("profiles").update({
            set("name", name)
            set("focus", focus)
            set("sober_since", soberSince.toString())
            set("reminder_time", reminderTime)
            set("onboarded", true)
        }) { filter { eq("id", uid) } }
        saveReasons(reasons)
        supabase.from("user_triggers").delete { filter { eq("user_id", uid) } }
        if (triggers.isNotEmpty()) {
            supabase.from("user_triggers").insert(triggers.mapIndexed { i, k -> TriggerRow(uid, k, i) })
        }
    }

    suspend fun setCheckin(success: Boolean, trigger: String?) {
        val uid = uid ?: return
        supabase.from("daily_checkins")
            .upsert(CheckinRow(uid, LocalDate.now().toString(), success, trigger)) {
                onConflict = "user_id,date"
            }
    }
}
