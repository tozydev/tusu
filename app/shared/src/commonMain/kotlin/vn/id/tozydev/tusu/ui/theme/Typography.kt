package vn.id.tozydev.tusu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

private fun TextStyle.weight(weight: FontWeight) = copy(fontWeight = weight)

/** Material 3 defaults with the heavier title and label weights the design uses. */
val AppTypography: Typography =
    Typography().run {
        copy(
            displayLarge = displayLarge.weight(FontWeight.ExtraBold),
            displayMedium = displayMedium.weight(FontWeight.ExtraBold),
            displaySmall = displaySmall.weight(FontWeight.ExtraBold),
            headlineLarge = headlineLarge.weight(FontWeight.ExtraBold),
            headlineMedium = headlineMedium.weight(FontWeight.ExtraBold),
            headlineSmall = headlineSmall.weight(FontWeight.Bold),
            titleLarge = titleLarge.weight(FontWeight.Bold),
            titleMedium = titleMedium.weight(FontWeight.Bold),
            titleSmall = titleSmall.weight(FontWeight.Bold),
            labelLarge = labelLarge.weight(FontWeight.Bold),
            labelMedium = labelMedium.weight(FontWeight.Bold),
            labelSmall = labelSmall.weight(FontWeight.Bold),
        )
    }
