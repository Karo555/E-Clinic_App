package com.example.e_clinic_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
/**
 * Defines the typography styles used in the e-clinic application's theme.
 *
 * This file provides a set of text styles based on Material 3 design principles. These styles
 * are used throughout the application to ensure a consistent and modern text appearance.
 *
 * Typography includes:
 * - `bodyLarge`: The default style for large body text, with a normal font weight and size of 16sp.
 * - Additional styles (e.g., `titleLarge`, `labelSmall`) can be customized as needed.
 *
 * The typography styles can be overridden to match the application's branding or design requirements.
 */
// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)