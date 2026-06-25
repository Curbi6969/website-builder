package com.beaunolten.rise.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "rise")

/** Snapshot of persisted fields; null means "never saved, use default". */
data class Persisted(
    val name: String?,
    val reasons: List<String>?,
    val reminders: Map<String, Boolean>,
    val tapRecord: Int?,
)

/** Minimal persistence — name, reasons, reminder toggles, tap-game record. */
class RisePreferences(private val context: Context) {

    private val keyName = stringPreferencesKey("user_name")
    private val keyReasons = stringPreferencesKey("reasons")
    private val keyTapRecord = intPreferencesKey("tap_record")
    private fun keyReminder(k: String) = booleanPreferencesKey("rem_$k")

    // Unit-separator control char — won't collide with user-typed reason text.
    private val reasonSep = ""

    suspend fun load(): Persisted {
        val p = context.dataStore.data.first()
        val reasonsRaw = p[keyReasons]
        val reminders = RiseDefaults.reminders
            .mapNotNull { def -> p[keyReminder(def.key)]?.let { def.key to it } }
            .toMap()
        return Persisted(
            name = p[keyName],
            reasons = reasonsRaw?.split(reasonSep)?.filter { it.isNotEmpty() },
            reminders = reminders,
            tapRecord = p[keyTapRecord],
        )
    }

    suspend fun setName(value: String) {
        context.dataStore.edit { it[keyName] = value }
    }

    suspend fun setReasons(list: List<String>) {
        context.dataStore.edit { it[keyReasons] = list.joinToString(reasonSep) }
    }

    suspend fun setReminder(key: String, on: Boolean) {
        context.dataStore.edit { it[keyReminder(key)] = on }
    }

    suspend fun setTapRecord(value: Int) {
        context.dataStore.edit { it[keyTapRecord] = value }
    }
}
