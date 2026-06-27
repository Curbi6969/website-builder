package com.rise.app.data

import androidx.compose.ui.graphics.Color
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.Orange
import com.rise.app.ui.theme.Purple
import com.rise.app.ui.theme.Teal

/** All seeded content from the design's renderVals(), reproduced verbatim. */
object RiseDefaults {

    val tasks = listOf(
        TaskItem(1, "08:00", "Ochtend check-in", "☀️", true),
        TaskItem(2, "12:30", "15 min wandelen", "🚶", true),
        TaskItem(3, "17:00", "10-min meditatie", "🧘", false),
        TaskItem(4, "21:30", "Avond check-in", "🌙", false),
    )

    val courses = listOf(
        Course("Ademruimte", "Kalmeer in 5 minuten", "5 min", 5, "🌬️", Color(0xFFE6F2F6)),
        Course("Slaap & loslaten", "Avondroutine, val makkelijk in slaap", "12 min", 12, "🌙", Color(0xFFE9E2FB)),
        Course("Ochtendkracht", "Begin de dag met intentie", "7 min", 7, "☀️", Color(0xFFFFF1D6)),
        Course("Zelfcompassie", "Geen schaamte. Je doet het goed.", "10 min", 10, "💚", Color(0xFFE3F2E9)),
        Course("Focus reset", "Verveling ombuigen naar rust", "6 min", 6, "🎯", Color(0xFFE6F2F6)),
    )

    val moods = listOf(
        MoodDef("great", "😄", "Top", Color(0xFF9AE6B4), "Mooi, geniet ervan!"),
        MoodDef("good", "🙂", "Goed", Color(0xFFC8EFA0), "Fijn dat het lekker gaat."),
        MoodDef("ok", "😐", "Oké", Color(0xFFFBE08A), "Oké is helemaal prima."),
        MoodDef("down", "😟", "Matig", Color(0xFFFFC48A), "Je voelt het, dat is moedig."),
        MoodDef("rough", "😣", "Zwaar", Color(0xFFFFAA9E), "Zware dag mag. Ik ben er."),
    )

    val reminders = listOf(
        ReminderDef("morning", "☀️", "Ochtend check-in", "Sterke start", true),
        ReminderDef("evening", "🌙", "Avond check-in", "Reflecteer & sluit af", true),
        ReminderDef("daytime", "💬", "Steun-pings overdag", "Willekeurige positieve berichten", false),
        ReminderDef("boredom", "🎯", "Bij verveling", "Seintje als je lang stil bent", false),
    )

    val reminderState: Map<String, Boolean> =
        reminders.associate { it.key to true }

    val reasons = listOf(
        "Ik wil ’s ochtends wakker worden zonder schuldgevoel.",
        "Ik wil echte connectie met mensen, niet met een scherm.",
        "Ik wil mijn tijd en energie terug voor wat ik echt wil.",
    )

    val triggers = listOf(
        TriggerDef("Verveling", "😪", 42, Orange),
        TriggerDef("’s Avonds laat", "🌙", 28, Purple),
        TriggerDef("’s Ochtends", "☀️", 18, Teal),
        TriggerDef("Stress / down", "💭", 12, Green),
    )

    /** Urges-beaten weekly bar heights (%) wk1..wk4. */
    val weeks = listOf("wk1" to 60, "wk2" to 72, "wk3" to 85, "wk4" to 95)

    val groundSteps = listOf(
        GroundStep(5, "dingen die je ZIET", "👀"),
        GroundStep(4, "dingen die je HOORT", "👂"),
        GroundStep(3, "dingen die je VOELT", "✋"),
        GroundStep(2, "dingen die je RUIKT", "👃"),
        GroundStep(1, "goede eigenschap van jezelf", "❤️"),
    )

    val activities = listOf(
        ActivityDef("💪", "10 push-ups nu"),
        ActivityDef("🚶", "Loop naar buiten"),
        ActivityDef("📞", "App een vriend"),
        ActivityDef("🎧", "Zet je favo nummer"),
        ActivityDef("🚿", "Koude douche"),
        ActivityDef("🧹", "Ruim 5 min op"),
    )

    val affirmations = listOf(
        "Goed bezig, man! 💪",
        "Trots op je. Echt waar.",
        "Dat is een win. Pak de volgende.",
        "Jij koos jezelf. Sterk.",
        "Zo bouw je je streak 🌱",
        "Yes! Weer een stap vooruit.",
    )

    val bubbleColors = listOf(
        Color(0xFF7EE0FF), Color(0xFFA8E6CF), Color(0xFFFFD3B6),
        Color(0xFFFFAAA5), Color(0xFFC3B0FF), Color(0xFF9AE6B4),
    )

    // Mood calendar (June 2026): 2 leading blanks; days 1..25 seeded faces; 26..30 plain.
    val weekdayLabels = listOf("M", "D", "W", "D", "V", "Z", "Z")
    val moodCalColors = listOf(
        Color(0xFF9AE6B4), Color(0xFFC8EFA0), Color(0xFFFBE08A), Color(0xFFFFC48A), Color(0xFFFFAA9E),
    )
    val moodCalFaces = listOf("😄", "🙂", "😐", "😟", "😣")
    val moodCalSeed = listOf(2, 1, 0, 2, 1, 3, 0, 1, 1, 0, 2, 4, 1, 0, 0, 1, 2, 1, 0, 3, 1, 0, 0, 1, 2)

    val urgeSurf = PlayerContent("Urge surfen", "Rij de golf uit, hij zakt altijd.", "🌊", 8)
}
