package com.rise.app.notif

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/** Schedules the daily "is het je gelukt?" check-in notification. */
object Reminders {

    const val CHANNEL = "daily_checkin"
    private const val PREFS = "rise_reminders"
    private const val KEY_TIME = "time"
    private const val REQ = 100

    fun ensureChannel(ctx: Context) {
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(CHANNEL) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL, "Dagelijkse check-in", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Een seintje per dag om je voortgang te bevestigen."
                },
            )
        }
    }

    /** Stores the time and (re)schedules a daily repeating alarm at HH:mm. */
    fun schedule(ctx: Context, hhmm: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_TIME, hhmm).apply()
        ensureChannel(ctx)
        val parts = hhmm.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return
        val m = parts.getOrNull(1)?.toIntOrNull() ?: return
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent(ctx))
    }

    /** Re-arms the alarm after a reboot or app update, if a time was set. */
    fun rescheduleFromPrefs(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TIME, null)?.let { schedule(ctx, it) }
    }

    private fun pendingIntent(ctx: Context): PendingIntent {
        val intent = Intent(ctx, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            ctx, REQ, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
