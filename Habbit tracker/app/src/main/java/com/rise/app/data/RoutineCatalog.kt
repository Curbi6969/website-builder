package com.rise.app.data

import com.rise.app.R

/**
 * Static catalogue for the Inspiratie tab — prebuilt routines and non-diagnostic
 * self-checks. Mirrors the pattern of [RiseDefaults]: all Dutch copy lives here,
 * verbatim. Content is grounded in evidence-based methods (Atomic Habits / habit
 * stacking & implementation intentions, HALT, urge surfing, Kristin Neff's
 * zelfcompassie). See docs/superpowers/specs/2026-06-27-inspiratie-tab-design.md §3, §7.
 *
 * [illustration] is a drawable res id; 0 until the real art is wired in (Task 8).
 */
enum class RoutineCategory(val label: String) {
    OCHTEND("Ochtend"), DRANG("Drang"), ZELFZORG("Zelfzorg"), SLAAP("Slaap")
}

enum class InspoAccent { GREEN, LAVENDER, TEAL, YELLOW, CORAL, BERRY }

data class RoutineStep(val time: String, val label: String, val icon: String)

data class Routine(
    val id: String,
    val name: String,
    val category: RoutineCategory,
    val accent: InspoAccent,
    val illustration: Int,
    val why: String,
    val steps: List<RoutineStep>,
)

enum class CheckKind { LIKERT_1_5, YES_NO, SCALE_0_10 }

data class SelfCheckQuestion(val text: String, val kind: CheckKind)

data class SelfCheck(
    val id: String,
    val name: String,
    val category: RoutineCategory,
    val accent: InspoAccent,
    val illustration: Int,
    val intro: String,
    val questions: List<SelfCheckQuestion>,
    /** True when the outcome warrants showing the crisis/113 card. */
    val heavy: (answers: List<Int>) -> Boolean,
    /** Supportive, non-diagnostic message. May reference today's mood key. */
    val feedback: (answers: List<Int>, mood: String?) -> String,
)

object RoutineCatalog {

    val routines = listOf(
        Routine(
            id = "ochtend",
            name = "Ochtendroutine — Sterke start",
            category = RoutineCategory.OCHTEND,
            accent = InspoAccent.GREEN,
            illustration = R.drawable.inspo_ochtend,
            why = "Koppel kleine stappen aan wakker worden. Een vaste cue (opstaan) plus een " +
                "concrete intentie (implementatie-intentie) maakt een gewoonte veel waarschijnlijker.",
            steps = listOf(
                RoutineStep("07:30", "Glas water bij opstaan", "💧"),
                RoutineStep("07:35", "1 minuut ademhaling", "🌬️"),
                RoutineStep("07:40", "Noem 1 ding waar je dankbaar voor bent", "🙏"),
                RoutineStep("07:45", "Zet je intentie voor vandaag", "✨"),
            ),
        ),
        Routine(
            id = "drang",
            name = "Drang de baas — HALT & golf",
            category = RoutineCategory.DRANG,
            accent = InspoAccent.TEAL,
            illustration = R.drawable.inspo_drang,
            why = "Veel drang is een onvervulde basisbehoefte (HALT). Vul die eerst, en surf dan " +
                "de golf: drang piekt en zakt altijd weer — hij gaat vanzelf voorbij.",
            steps = listOf(
                RoutineStep("", "Check HALT: honger, boos, eenzaam, moe?", "🔍"),
                RoutineStep("", "Drink water of eet iets kleins", "🍎"),
                RoutineStep("", "Surf de golf — 8 min", "🌊"),
                RoutineStep("", "App iemand of loop naar buiten", "🚶"),
            ),
        ),
        Routine(
            id = "zelfcompassie",
            name = "Zelfcompassie — Wees zacht voor jezelf",
            category = RoutineCategory.ZELFZORG,
            accent = InspoAccent.LAVENDER,
            illustration = R.drawable.inspo_zelfcompassie,
            why = "De drie bouwstenen van zelfcompassie (Kristin Neff): mindfulness, gedeelde " +
                "menselijkheid en vriendelijkheid. Zachter én effectiever dan zelfkritiek.",
            steps = listOf(
                RoutineStep("", "Hand op je hart, 3 diepe ademhalingen", "💚"),
                RoutineStep("", "Zeg: dit is een moeilijk moment", "🌧️"),
                RoutineStep("", "Zeg: moeilijk hebben hoort bij mens-zijn", "🤝"),
                RoutineStep("", "Vraag: wat heb ik nu nodig?", "🌱"),
            ),
        ),
        Routine(
            id = "avond",
            name = "Avond wind-down — Zacht afsluiten",
            category = RoutineCategory.SLAAP,
            accent = InspoAccent.YELLOW,
            illustration = R.drawable.inspo_avond,
            why = "Schermrust, dankbaarheid en reflectie kalmeren je systeem voor de nacht. " +
                "Betere slaap betekent overdag meer veerkracht tegen drang.",
            steps = listOf(
                RoutineStep("21:00", "Scherm weg, 30 min voor bed", "📵"),
                RoutineStep("21:15", "Schrijf 3 dingen die goed gingen", "✍️"),
                RoutineStep("21:30", "Avond check-in", "🌙"),
                RoutineStep("21:45", "Slaap & loslaten meditatie", "😴"),
            ),
        ),
        Routine(
            id = "dankbaarheid",
            name = "Dankbaarheid — 3 goede dingen",
            category = RoutineCategory.ZELFZORG,
            accent = InspoAccent.CORAL,
            illustration = R.drawable.inspo_dankbaarheid,
            why = "Dagelijks 3 concrete dingen opschrijven traint je brein om het goede te zien — " +
                "dat verbetert je stemming en je motivatie om door te gaan.",
            steps = listOf(
                RoutineStep("", "Schrijf 3 concrete dingen op", "✍️"),
                RoutineStep("", "Waarom maakte het je blij?", "💭"),
                RoutineStep("", "Stuur 1 iemand een bedankje", "💌"),
            ),
        ),
        Routine(
            id = "1procent",
            name = "1% beter — Mini-gewoontes",
            category = RoutineCategory.OCHTEND,
            accent = InspoAccent.GREEN,
            illustration = R.drawable.inspo_1procent,
            why = "Maak het zó klein dat falen onmogelijk is en koppel het aan een bestaande " +
                "gewoonte (habit stacking). 1% beter per dag is na een jaar een enorme sprong.",
            steps = listOf(
                RoutineStep("", "Kies 1 mini-gewoonte (max 2 min)", "🎯"),
                RoutineStep("", "Koppel het aan iets dat je al doet", "🔗"),
                RoutineStep("", "Doe het direct na je anker", "▶️"),
                RoutineStep("", "Vink af, vier de win", "🎉"),
            ),
        ),
    )

    val selfChecks = listOf(
        SelfCheck(
            id = "halt",
            name = "HALT-check",
            category = RoutineCategory.DRANG,
            accent = InspoAccent.TEAL,
            illustration = R.drawable.inspo_halt,
            intro = "Vier korte vragen. Geen score — gewoon checken wat je nu nodig hebt.",
            questions = listOf(
                SelfCheckQuestion("Heb je honger?", CheckKind.YES_NO),
                SelfCheckQuestion("Ben je boos of geïrriteerd?", CheckKind.YES_NO),
                SelfCheckQuestion("Voel je je eenzaam?", CheckKind.YES_NO),
                SelfCheckQuestion("Ben je moe?", CheckKind.YES_NO),
            ),
            heavy = { ans -> ans.count { it == 1 } >= 3 },
            feedback = { ans, _ ->
                val needs = buildList {
                    if (ans.getOrNull(0) == 1) add("Eet iets — ook iets kleins helpt al. 🍎")
                    if (ans.getOrNull(1) == 1) add("Je bent boos. Adem 3x lang uit, of beweeg het eruit. 🌬️")
                    if (ans.getOrNull(2) == 1) add("Eenzaam? App of bel iemand — één bericht is genoeg. 💬")
                    if (ans.getOrNull(3) == 1) add("Je bent moe. Rust mag — leg even alles neer. 😴")
                }
                if (needs.isEmpty())
                    "Mooi — je basis is op orde. Komt de drang toch? Onthoud: de golf zakt altijd weer. 🌊"
                else
                    "Dit heb je nu nodig:\n\n" + needs.joinToString("\n") +
                        "\n\nVeel drang is gewoon een onvervulde basisbehoefte. Zorg hier eerst voor."
            },
        ),
        SelfCheck(
            id = "zelfcompassie_check",
            name = "Zelfcompassie-check",
            category = RoutineCategory.ZELFZORG,
            accent = InspoAccent.LAVENDER,
            illustration = R.drawable.inspo_zelfcompassie_check,
            intro = "Vijf stellingen. Hoe meer iets klopt, hoe hoger. Er is geen goed of fout.",
            questions = listOf(
                SelfCheckQuestion("Als ik het moeilijk heb, ben ik vriendelijk voor mezelf.", CheckKind.LIKERT_1_5),
                SelfCheckQuestion("Ik accepteer dat fouten maken bij het leven hoort.", CheckKind.LIKERT_1_5),
                SelfCheckQuestion("Als ik faal, probeer ik het in perspectief te zien.", CheckKind.LIKERT_1_5),
                SelfCheckQuestion("Ik gun mezelf rust als het zwaar is.", CheckKind.LIKERT_1_5),
                SelfCheckQuestion("Ik praat tegen mezelf zoals tegen een goede vriend.", CheckKind.LIKERT_1_5),
            ),
            heavy = { ans -> (if (ans.isEmpty()) 5.0 else ans.average()) <= 1.5 },
            feedback = { ans, _ ->
                val avg = if (ans.isEmpty()) 0.0 else ans.average()
                when {
                    avg >= 4 ->
                        "Je bent al behoorlijk zacht voor jezelf — dat is een kracht, geen luxe. " +
                            "Blijf zo tegen jezelf praten als tegen een vriend. 💚"
                    avg >= 2.5 ->
                        "Je bent soms streng voor jezelf — heel menselijk. Zelfcompassie heeft " +
                            "dezelfde voordelen als zelfwaardering, maar zonder de nadelen. Probeer eens " +
                            "de Zelfcompassie-routine. 🌱"
                    else ->
                        "Je bent vaak hard voor jezelf. Dat hoeft niet. Moeilijk hebben hoort bij " +
                            "mens-zijn — je bent niet de enige. Wees net zo mild voor jezelf als voor " +
                            "iemand van wie je houdt. 💚"
                }
            },
        ),
        SelfCheck(
            id = "drang_check",
            name = "Drang-check",
            category = RoutineCategory.DRANG,
            accent = InspoAccent.TEAL,
            illustration = R.drawable.inspo_drang_check,
            intro = "Hoe sterk is je drang op dit moment?",
            questions = listOf(
                SelfCheckQuestion("Sleep om je drang aan te geven (0 = niets, 10 = heel sterk).", CheckKind.SCALE_0_10),
            ),
            heavy = { ans -> (ans.firstOrNull() ?: 0) >= 8 },
            feedback = { ans, _ ->
                when (val v = ans.firstOrNull() ?: 0) {
                    in 8..10 ->
                        "Sterke drang nu. Dat is zwaar — én tijdelijk. De golf piekt en zakt altijd " +
                            "weer, meestal binnen 20 minuten. Wil je 'm samen uitrijden? 🌊"
                    in 4..7 ->
                        "Er is drang, maar je staat er nog boven. Goed moment voor een korte actie: " +
                            "water, een blokje om, of iemand appen. 🚶"
                    else ->
                        "Lichte of geen drang — fijn. Dit is precies het moment om je routine te doen " +
                            "terwijl het rustig is. 🌱"
                }
            },
        ),
        SelfCheck(
            id = "stemming",
            name = "Stemming-check",
            category = RoutineCategory.ZELFZORG,
            accent = InspoAccent.BERRY,
            illustration = R.drawable.inspo_stemming,
            intro = "Even checken hoe je je nu voelt.",
            questions = listOf(
                SelfCheckQuestion("Hoe voel je je op dit moment? (1 = zwaar, 5 = top)", CheckKind.LIKERT_1_5),
            ),
            heavy = { ans -> (ans.firstOrNull() ?: 3) <= 1 },
            feedback = { ans, mood ->
                val moodLine = mood?.let { m ->
                    val nl = mapOf(
                        "great" to "Top", "good" to "Goed", "ok" to "Oké",
                        "down" to "Matig", "rough" to "Zwaar",
                    )[m]
                    if (nl != null) "Je gaf vandaag '$nl' aan in Stemming. " else ""
                } ?: ""
                when (val v = ans.firstOrNull() ?: 3) {
                    in 4..5 -> moodLine + "Fijn dat het goed gaat — geniet ervan en onthoud wat hieraan bijdroeg. 🌞"
                    3 -> moodLine + "Oké is helemaal prima. Niet elke dag hoeft top te zijn. 🌿"
                    else -> moodLine + "Zware dag mag er zijn. Wees zacht voor jezelf — wat heb je nu nodig? 💚"
                }
            },
        ),
    )
}
