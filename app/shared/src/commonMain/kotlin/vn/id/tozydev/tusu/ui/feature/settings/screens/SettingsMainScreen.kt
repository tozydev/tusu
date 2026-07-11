package vn.id.tozydev.tusu.ui.feature.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.expressive.SettingsGroup
import com.alorma.compose.settings.ui.expressive.SettingsMenuLink
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.ic_backup_24px
import vn.id.tozydev.tusu.generated.resources.ic_info_24px
import vn.id.tozydev.tusu.generated.resources.ic_palette_24px
import vn.id.tozydev.tusu.generated.resources.settings_about_subtitle
import vn.id.tozydev.tusu.generated.resources.settings_about_title
import vn.id.tozydev.tusu.generated.resources.settings_appearance_subtitle
import vn.id.tozydev.tusu.generated.resources.settings_appearance_title
import vn.id.tozydev.tusu.generated.resources.settings_backup_restore_subtitle
import vn.id.tozydev.tusu.generated.resources.settings_backup_restore_title
import vn.id.tozydev.tusu.ui.model.UiText

private data class SettingsItem(
    val title: UiText,
    val subtitle: UiText,
    val iconRes: DrawableResource,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsMainScreen(
    onNavigateToAppearance: () -> Unit,
    onNavigateToBackupAndRestore: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settingsItems = remember {
        setOf(
            SettingsItem(
                title = UiText(Res.string.settings_appearance_title),
                subtitle = UiText(Res.string.settings_appearance_subtitle),
                iconRes = Res.drawable.ic_palette_24px,
                onClick = onNavigateToAppearance,
            ),
            SettingsItem(
                title = UiText(Res.string.settings_backup_restore_title),
                subtitle = UiText(Res.string.settings_backup_restore_subtitle),
                iconRes = Res.drawable.ic_backup_24px,
                onClick = onNavigateToBackupAndRestore,
            ),
            SettingsItem(
                title = UiText(Res.string.settings_about_title),
                // TODO replace with actual values
                subtitle = UiText(Res.string.settings_about_subtitle, "Tusu", "dev"),
                iconRes = Res.drawable.ic_info_24px,
                onClick = onNavigateToAbout,
            ),
        )
    }

    val menuLinkColors =
        ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )

    SettingsGroup(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        settingsItems.forEachIndexed { index, item ->
            SettingsMenuLink(
                title = { Text(item.title.asString(), fontWeight = FontWeight.SemiBold) },
                subtitle = { Text(item.subtitle.asString()) },
                onClick = item.onClick,
                icon = { Icon(painterResource(item.iconRes), null) },
                shapes = ListItemDefaults.segmentedShapes(index, settingsItems.size),
                colors = menuLinkColors,
            )
        }
    }
}
