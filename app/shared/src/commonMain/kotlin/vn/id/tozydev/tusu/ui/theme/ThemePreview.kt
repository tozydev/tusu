package vn.id.tozydev.tusu.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.domain.model.ThemeMode

@Composable
private fun ThemeSample(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Headline", style = MaterialTheme.typography.headlineMedium)
            Text("Title", style = MaterialTheme.typography.titleMedium)
            Text("Body text", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                with(MaterialTheme.colorScheme) {
                    listOf(primary, secondary, tertiary, primaryContainer, surfaceContainerHigh)
                        .forEach { Swatch(it) }
                }
            }
        }
    }
}

@Composable
private fun Swatch(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp).background(color, CircleShape))
}

@Preview
@Composable
private fun ThemeLightPreview() {
    AppTheme(themeMode = ThemeMode.LIGHT) { ThemeSample() }
}

@Preview
@Composable
private fun ThemeDarkPreview() {
    AppTheme(themeMode = ThemeMode.DARK) { ThemeSample() }
}
