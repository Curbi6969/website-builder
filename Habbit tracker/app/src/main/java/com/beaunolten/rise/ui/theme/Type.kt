package com.beaunolten.rise.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.beaunolten.rise.R

// Variable fonts: register the weights we use by pinning the `wght` axis.
// variationSettings take effect on API 26+; on 24/25 the font renders at its
// default instance (a graceful, acceptable fallback).
@OptIn(ExperimentalTextApi::class)
private fun fredokaFont(weight: FontWeight) = Font(
    resId = R.font.fredoka,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

@OptIn(ExperimentalTextApi::class)
private fun nunitoFont(weight: FontWeight) = Font(
    resId = R.font.nunito,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Fredoka — rounded display/headings (400/500/600/700). */
val Fredoka = FontFamily(
    fredokaFont(FontWeight.Normal),
    fredokaFont(FontWeight.Medium),
    fredokaFont(FontWeight.SemiBold),
    fredokaFont(FontWeight.Bold),
)

/** Nunito — body (400/600/700/800/900). */
val Nunito = FontFamily(
    nunitoFont(FontWeight.Normal),
    nunitoFont(FontWeight.SemiBold),
    nunitoFont(FontWeight.Bold),
    nunitoFont(FontWeight.ExtraBold),
    nunitoFont(FontWeight.Black),
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
