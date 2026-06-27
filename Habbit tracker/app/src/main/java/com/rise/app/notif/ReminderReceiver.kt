package com.rise.app.notif

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rise.app.MainActivity
import com.rise.app.R

/** Posts the daily check-in notification; tapping opens the check-in. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        Reminders.ensureChannel(ctx)

        val tap = Intent(ctx, MainActivity::class.java).apply {
            putExtra("open_checkin", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            ctx, 101, tap,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notif = NotificationCompat.Builder(ctx, Reminders.CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Is het je vandaag gelukt?")
            .setContentText("Tik om je dag te bevestigen 🌱")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (allowed) NotificationManagerCompat.from(ctx).notify(1, notif)
    }
}
