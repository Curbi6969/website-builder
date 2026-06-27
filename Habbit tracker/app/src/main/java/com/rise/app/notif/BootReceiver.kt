package com.rise.app.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Re-arms the daily reminder after a reboot (repeating alarms don't survive one). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) Reminders.rescheduleFromPrefs(ctx)
    }
}
