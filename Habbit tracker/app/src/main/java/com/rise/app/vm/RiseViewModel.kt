package com.rise.app.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rise.app.data.Bubble
import com.rise.app.data.Game
import com.rise.app.data.MoodDef
import com.rise.app.data.Panic
import com.rise.app.data.PERSONAL_ROUTINE
import com.rise.app.data.PlayerContent
import com.rise.app.data.RiseDefaults
import com.rise.app.data.RoutineCategory
import com.rise.app.data.RiseRepository
import com.rise.app.data.RiseUiState
import com.rise.app.data.Tab
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.random.Random

class RiseViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = RiseRepository()

    private val _state = MutableStateFlow(RiseUiState())
    val state: StateFlow<RiseUiState> = _state.asStateFlow()

    private var toastJob: Job? = null
    private var playerJob: Job? = null
    private var tapTimerJob: Job? = null
    private var tapTargetJob: Job? = null

    init {
        spawnBubbles()
        viewModelScope.launch {
            runCatching { repo.load() }
                .onSuccess { r ->
                    if (r != null) _state.update { s ->
                        s.copy(
                            userName = r.name,
                            soberSince = r.soberSince,
                            heroStyle = r.heroStyle,
                            reasons = r.reasons,
                            reminders = s.reminders + r.reminders,
                            tapRecord = r.tapRecord,
                            tasks = s.tasks.map { t -> t.copy(done = r.todayTasksDone[t.id] ?: false) },
                            moodPicked = r.todayMood ?: s.moodPicked,
                            moodCalendar = r.moodCalendar,
                            urgeThisWeek = r.urgeThisWeek,
                            urgeWeekly = r.urgeWeekly,
                            checkedInToday = r.checkedInToday,
                            addedRoutineIds = r.addedRoutineIds,
                            activeRoutineId = r.activeRoutineId,
                            routineTaskDone = r.routineTaskDone,
                        )
                    }
                }
                .onFailure { Log.w("Rise", "Kon data niet laden uit Supabase", it) }
        }
    }

    /** Fire-and-forget Supabase write; a network hiccup is logged, never fatal. */
    private fun write(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure { Log.w("Rise", "Supabase-write faalde", it) }
        }
    }

    // ----- navigation -----
    fun goTab(tab: Tab) = _state.update { it.copy(tab = tab) }

    // ----- tasks -----
    fun toggleTask(id: Int) {
        var newDone = false
        val routineId = _state.value.activeRoutineId
        if (routineId == PERSONAL_ROUTINE) {
            _state.update { s ->
                s.copy(tasks = s.tasks.map { t ->
                    if (t.id == id) { newDone = !t.done; t.copy(done = newDone) } else t
                })
            }
        } else {
            _state.update { s ->
                newDone = !(s.routineTaskDone[id] ?: false)
                s.copy(routineTaskDone = s.routineTaskDone + (id to newDone))
            }
        }
        write { repo.setTaskDone(id, newDone, routineId) }
        if (newDone) affirm(rndAffirm())
    }

    // ----- routines (Inspiratie) -----
    fun setActiveRoutine(id: String) {
        _state.update { it.copy(activeRoutineId = id) }
        write { repo.setActiveRoutine(id) }
    }

    fun addRoutine(catalogId: String) {
        var position = 0
        _state.update {
            if (it.addedRoutineIds.contains(catalogId)) it
            else {
                position = it.addedRoutineIds.size
                it.copy(addedRoutineIds = it.addedRoutineIds + catalogId)
            }
        }
        write { repo.addRoutine(catalogId, position) }
    }

    fun removeRoutine(id: String) {
        var resetActive = false
        _state.update {
            resetActive = it.activeRoutineId == id
            it.copy(
                addedRoutineIds = it.addedRoutineIds - id,
                activeRoutineId = if (resetActive) PERSONAL_ROUTINE else it.activeRoutineId,
            )
        }
        write { repo.removeRoutine(id) }
        if (resetActive) write { repo.setActiveRoutine(PERSONAL_ROUTINE) }
    }

    /** Swipe-to-delete a task from the active list (session-only; not yet persisted). */
    fun removeTask(id: Int) {
        _state.update { it.copy(hiddenTaskIds = it.hiddenTaskIds + id) }
        affirm("Taak verwijderd")
    }

    /** Rename a routine chip (session-only; not yet persisted). */
    fun renameRoutine(id: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        _state.update { it.copy(routineNames = it.routineNames + (id to trimmed)) }
        affirm("Naam opgeslagen")
    }

    fun setInspoCategory(cat: RoutineCategory?) = _state.update { it.copy(inspoCategory = cat) }
    fun openRoutine(id: String) = _state.update { it.copy(openRoutine = id) }
    fun closeRoutine() = _state.update { it.copy(openRoutine = null) }
    fun openSelfCheck(id: String) = _state.update { it.copy(openSelfCheck = id) }
    fun closeSelfCheck() = _state.update { it.copy(openSelfCheck = null) }

    // ----- mood / journal -----
    fun pickMood(m: MoodDef) {
        _state.update {
            it.copy(
                moodPicked = m.key,
                moodCalendar = it.moodCalendar + (LocalDate.now().dayOfMonth to m.key),
            )
        }
        write { repo.setMood(m.key) }
        affirm("Genoteerd. ${m.note}")
    }

    fun setJournal(text: String) = _state.update { it.copy(journalText = text) }

    fun saveJournal() {
        val text = _state.value.journalText.trim()
        _state.update { it.copy(journalText = "") }
        if (text.isNotEmpty()) write { repo.addJournal(text) }
        affirm("Bewaard in je boomhut ✨")
    }

    // ----- daily check-in -----
    fun openCheckin() = _state.update { it.copy(showCheckin = true) }
    fun closeCheckin() = _state.update { it.copy(showCheckin = false) }

    fun submitCheckin(success: Boolean, trigger: String?) {
        _state.update { it.copy(checkedInToday = true, showCheckin = false) }
        write { repo.setCheckin(success, trigger) }
        affirm(if (success) "Sterk. Weer een dag bevestigd 🌱" else "Eerlijk zijn is moedig. Morgen weer, maat.")
    }

    // ----- settings -----
    fun openSettings() = _state.update { it.copy(showSettings = true) }
    fun closeSettings() = _state.update { it.copy(showSettings = false) }

    fun resetStreak() {
        val today = LocalDate.now()
        _state.update { it.copy(soberSince = today, showSettings = false) }
        write { repo.resetSoberSince() }
        affirm("Nieuwe start vanaf vandaag. Je kunt dit. 🌱")
    }

    fun setName(value: String) {
        _state.update { it.copy(userName = value) }
        write { repo.setName(value) }
    }

    fun addReason() {
        _state.update { it.copy(reasons = it.reasons + "") }
        persistReasons()
    }

    fun editReason(index: Int, value: String) {
        _state.update { s ->
            s.copy(reasons = s.reasons.toMutableList().also { if (index in it.indices) it[index] = value })
        }
        persistReasons()
    }

    fun removeReason(index: Int) {
        _state.update { s -> s.copy(reasons = s.reasons.filterIndexed { i, _ -> i != index }) }
        persistReasons()
    }

    private fun persistReasons() {
        val list = _state.value.reasons
        write { repo.saveReasons(list) }
    }

    fun toggleReminder(key: String) {
        val newVal = !(_state.value.reminders[key] ?: false)
        _state.update { it.copy(reminders = it.reminders + (key to newVal)) }
        write { repo.setReminder(key, newVal) }
    }

    // ----- panic -----
    fun openPanic() = _state.update { it.copy(panic = Panic.MENU) }
    fun closePanic() {
        stopTap()
        _state.update { it.copy(panic = Panic.NONE, game = Game.NONE) }
    }
    fun panicGo(target: Panic) = _state.update { it.copy(panic = target) }
    fun panicBack() = _state.update { it.copy(panic = Panic.MENU) }
    fun panicBeat() {
        val method = when (_state.value.panic) {
            Panic.BREATHING -> "breathing"
            Panic.WATER -> "water"
            Panic.REASONS -> "reasons"
            Panic.GAME -> "game"
            else -> "menu"
        }
        stopTap()
        _state.update {
            it.copy(panic = Panic.DONE, game = Game.NONE, urgeThisWeek = it.urgeThisWeek + 1)
        }
        write { repo.addUrge(method, null) }
    }
    fun panicOpenGames() {
        stopTap()
        _state.update { it.copy(panic = Panic.GAME, game = Game.NONE) }
    }

    // ----- games -----
    fun pickBubbles() {
        spawnBubbles()
        _state.update { it.copy(game = Game.BUBBLES, gameScore = 0) }
    }
    fun pickGround() {
        resetGround()
        _state.update { it.copy(game = Game.GROUND) }
    }
    fun pickTap() {
        startTap()
        _state.update { it.copy(game = Game.TAP) }
    }
    fun gameBack() {
        stopTap()
        _state.update { it.copy(game = Game.NONE) }
    }

    fun spawnBubbles() {
        _state.update { s ->
            var seed = s.bubbleSeed
            val arr = (0 until 7).map {
                seed += 1
                mkBubble(seed)
            }
            s.copy(bubbles = arr, bubbleSeed = seed)
        }
    }

    private fun mkBubble(id: Int): Bubble {
        val size = 42 + Random.nextInt(34)
        return Bubble(
            id = id,
            size = size,
            x = Random.nextInt((300 - size).coerceAtLeast(1)),
            y = Random.nextInt((320 - size).coerceAtLeast(1)),
            color = RiseDefaults.bubbleColors[Random.nextInt(RiseDefaults.bubbleColors.size)],
        )
    }

    fun popBubble(id: Int) {
        _state.update { s ->
            var seed = s.bubbleSeed + 1
            val left = s.bubbles.filter { it.id != id } + mkBubble(seed)
            s.copy(bubbles = left, bubbleSeed = seed, gameScore = s.gameScore + 1)
        }
    }

    // grounding
    fun tapGround(n: Int) = _state.update { s ->
        if (s.groundFilled >= n) s else s.copy(groundFilled = s.groundFilled + 1)
    }
    fun nextGround(total: Int) = _state.update { s ->
        if (s.groundStep >= total - 1) s.copy(groundDone = true)
        else s.copy(groundStep = s.groundStep + 1, groundFilled = 0)
    }
    private fun resetGround() =
        _state.update { it.copy(groundStep = 0, groundFilled = 0, groundDone = false) }

    // tap-green game
    fun startTap() {
        stopTap()
        _state.update {
            it.copy(tapScore = 0, tapTime = 20, tapRunning = true, tapOver = false, tapTarget = Random.nextInt(9))
        }
        tapTargetJob = viewModelScope.launch {
            while (isActive) {
                delay(900)
                _state.update { it.copy(tapTarget = Random.nextInt(9)) }
            }
        }
        tapTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                tickTap()
            }
        }
    }

    private fun tickTap() {
        val cur = _state.value
        if (cur.tapTime <= 1) endTap()
        else _state.update { it.copy(tapTime = it.tapTime - 1) }
    }

    private fun endTap() {
        tapTimerJob?.cancel()
        tapTargetJob?.cancel()
        _state.update { s ->
            val rec = maxOf(s.tapRecord, s.tapScore)
            s.copy(tapRunning = false, tapOver = true, tapTime = 0, tapRecord = rec)
        }
        write { repo.setTapRecord(_state.value.tapRecord) }
    }

    fun hitTap(index: Int) {
        _state.update { s ->
            if (!s.tapRunning || index != s.tapTarget) return@update s
            var t = Random.nextInt(9)
            while (t == s.tapTarget) t = Random.nextInt(9)
            s.copy(tapScore = s.tapScore + 1, tapTarget = t)
        }
    }

    fun stopTap() {
        tapTimerJob?.cancel()
        tapTargetJob?.cancel()
    }

    // ----- bored -----
    fun openBored() = _state.update { it.copy(bored = true) }
    fun closeBored() = _state.update { it.copy(bored = false) }
    fun doGoal() {
        _state.update { it.copy(bored = false) }
        affirm("Top! Klein doel = grote win 💪")
    }
    fun doActivity() {
        _state.update { it.copy(bored = false) }
        affirm(rndAffirm())
    }
    fun goJournalFromBored() = _state.update { it.copy(bored = false, tab = Tab.MOOD) }

    // ----- player -----
    fun openUrgeSurf() = openPlayer(RiseDefaults.urgeSurf)

    fun openPlayer(content: PlayerContent) {
        playerJob?.cancel()
        val total = content.min.coerceAtLeast(1) * 60
        _state.update {
            it.copy(player = content, playerTotal = total, playerLeft = total, playerPlaying = true, playerDone = false)
        }
        playerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val s = _state.value
                if (!s.playerPlaying) continue
                if (s.playerLeft <= 1) {
                    _state.update { it.copy(playerLeft = 0, playerPlaying = false, playerDone = true) }
                    break
                }
                _state.update { it.copy(playerLeft = it.playerLeft - 1) }
            }
        }
    }

    fun togglePlay() = _state.update { it.copy(playerPlaying = !it.playerPlaying) }

    fun closePlayer() {
        playerJob?.cancel()
        _state.update { it.copy(player = null, playerPlaying = false) }
    }

    // ----- toast -----
    fun affirm(msg: String) {
        toastJob?.cancel()
        _state.update { it.copy(toast = msg) }
        toastJob = viewModelScope.launch {
            delay(2400)
            _state.update { it.copy(toast = null) }
        }
    }

    private fun rndAffirm(): String =
        RiseDefaults.affirmations[Random.nextInt(RiseDefaults.affirmations.size)]
}
