package com.rise.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.rise.app.data.supabase
import com.rise.app.notif.Reminders
import com.rise.app.ui.auth.AuthGate
import com.rise.app.ui.theme.RiseTheme
import io.github.jan.supabase.auth.handleDeeplinks

/** Bridges a notification tap into Compose so the check-in opens. */
object CheckinBus {
    var pending by mutableStateOf(false)
}

class MainActivity : ComponentActivity() {

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAuthDeeplink(intent)
        handleCheckinIntent(intent)
        Reminders.rescheduleFromPrefs(this)
        requestNotifPermission()
        setContent {
            RiseTheme {
                AuthGate()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthDeeplink(intent)
        handleCheckinIntent(intent)
    }

    private fun handleCheckinIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("open_checkin", false) == true) CheckinBus.pending = true
    }

    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * A clicked magic link may arrive without a usable session (e.g. a mail provider
     * pre-opened the link and burned the one-time token). handleDeeplinks throws in
     * that case, swallow it so the app never crashes; the 6-digit code path remains.
     */
    private fun handleAuthDeeplink(intent: Intent?) {
        intent ?: return
        try {
            supabase.handleDeeplinks(intent)
        } catch (e: Exception) {
            Log.w("Rise", "Negeer ongeldige auth-deeplink", e)
        }
    }
}
