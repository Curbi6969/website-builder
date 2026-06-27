package com.rise.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rise.app.R

// Static weight instances generated from the variable Fredoka/Nunito fonts.
// One file per weight keeps rendering reliable across devices (the variable
// `variationSettings` path silently fell back to the system font).

/** Fredoka, rounded display/headings. */
val Fredoka = FontFamily(
    Font(R.font.fredoka_regular, FontWeight.Normal),
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold),
)

/** Nunito, body. */
val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black),
)

/** Material3 typography defaulting unstyled Text to Nunito / Fredoka headings. */
val RiseTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Fredoka, fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold),
        headlineSmall = headlineSmall.copy(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
        bodyLarge = bodyLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold),
        bodyMedium = bodyMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
        labelMedium = labelMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    )
}
