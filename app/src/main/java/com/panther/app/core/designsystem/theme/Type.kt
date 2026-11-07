package com.panther.app.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.panther.app.R

val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val BaseTypography = Typography()

val PantherTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = VazirmatnFontFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = VazirmatnFontFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = VazirmatnFontFamily),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = VazirmatnFontFamily),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = VazirmatnFontFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = VazirmatnFontFamily),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = VazirmatnFontFamily),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = VazirmatnFontFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = VazirmatnFontFamily),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = VazirmatnFontFamily),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = VazirmatnFontFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = VazirmatnFontFamily),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = VazirmatnFontFamily),
)
