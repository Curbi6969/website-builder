package com.beaunolten.rise.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beaunolten.rise.data.Bubble
import com.beaunolten.rise.data.Game
import com.beaunolten.rise.data.MoodDef
import com.beaunolten.rise.data.Panic
import com.beaunolten.rise.data.PlayerContent
import com.beaunolten.rise.data.RiseDefaults
import com.beaunolten.rise.data.RisePreferences
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.data.Tab
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random

class RiseViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = RisePreferences(app)

    private val _state = MutableStateFlow(RiseUiState())
    val state: StateFlow<RiseUiState> = _state.asStateFlow()

    private var toastJob: Job? = null
    private var playerJob: Job? = null
    private var tapTimerJob: Job? = null
    private var tapTargetJob: Job? = null

    init {
        spawnBubbles()
        viewModelScope.launch {
            val p = prefs.load()
            _state.update { s ->
                s.copy(
                    userName = p.name ?: s.userName,
                    reasons = p.reasons ?: s.reasons,
                    reminders = if (p.reminders.isEmpty()) s.reminders else s.reminders + p.reminders,
                    tapRecord = p.tapRecord ?: s.tapRecord,
                )
            }
        }
    }

    // ----- navigation -----
    fun goTab(tab: Tab) = _state.update { it.copy(tab = tab) }

    // ----- tasks -----
    fun toggleTask(id: Int) {
        var becameDone = false
        _state.update { s ->
            s.copy(tasks = s.tasks.map { t ->
                if (t.id == id) {
                    becameDone = !t.done
                    t.copy(done = !t.done)
                } else t
            })
        }
        if (becameDone) affirm(rndAffirm())
    }

    // ----- mood / journal -----
    fun pickMood(m: MoodDef) {
        _state.update { it.copy(moodPicked = m.key) }
        affirm("Genoteerd. ${m.note}")
    }

    fun setJournal(text: String) = _state.update { it.copy(journalText = text) }

    fun saveJournal() {
        _state.update { it.copy(journalText = "") }
        affirm("Bewaard in je boomhut ✨")
    }

    // ----- settings -----
    fun openSettings() = _state.update { it.copy(showSettings = true) }
    fun closeSettings() = _state.update { it.copy(showSettings = false) }

    fun setName(value: String) {
        _state.update { it.copy(userName = value) }
        viewModelScope.launch { prefs.setName(value) }
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
        viewModelScope.launch { prefs.setReasons(list) }
    }

    fun toggleReminder(key: String) {
        val newVal = !(_state.value.reminders[key] ?: false)
        _state.update { it.copy(reminders = it.reminders + (key to newVal)) }
        viewModelScope.launch { prefs.setReminder(key, newVal) }
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
        stopTap()
        _state.update { it.copy(panic = Panic.DONE, game = Game.NONE) }
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
        viewModelScope.launch { prefs.setTapRecord(_state.value.tapRecord) }
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
