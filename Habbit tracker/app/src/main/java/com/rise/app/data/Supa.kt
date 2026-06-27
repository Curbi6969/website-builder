package com.rise.app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * App-wide Supabase client. The publishable key is safe to ship — row-level security
 * on every table is what actually protects user data. Auth session persists automatically
 * on Android (supabase-kt auto-initializes from the app context).
 */
val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://msopebagktidmtefrxmt.supabase.co",
    supabaseKey = "sb_publishable_8Yj1my1GXzknbdInBka_Hg_Aro19mLJ",
) {
    install(Auth) {
        // Magic-link emails redirect back into the app via rise://login,
        // caught by MainActivity.handleDeeplinks. The 6-digit code stays as
        // a cross-device fallback.
        scheme = "rise"
        host = "login"
    }
    install(Postgrest)
}
