package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_add_menu
import vn.id.tozydev.tusu.generated.resources.ic_add_24px
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * The "+" button shown while the keyboard is hidden; it opens the attach actions as a menu. Picking
 * one closes the menu first.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ComposerFabMenu(actions: List<AttachAction>, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    // Back closes the open menu before it closes the composer.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = expanded,
        onBackCompleted = { expanded = false },
    )
    val label = stringResource(Res.string.composer_add_menu)
    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it },
                modifier = Modifier.semantics { contentDescription = label },
            ) {
                Icon(
                    painter =
                        painterResource(
                            if (checkedProgress > 0.5f) Res.drawable.ic_close_24px
                            else Res.drawable.ic_add_24px,
                        ),
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        actions.forEach { action ->
            FloatingActionButtonMenuItem(
                onClick = {
                    expanded = false
                    action.onClick()
                },
                icon = { Icon(painterResource(action.icon), contentDescription = null) },
                text = { Text(stringResource(action.label)) },
            )
        }
    }
}

@Preview
@Composable
private fun ComposerFabMenuPreview() {
    AppTheme {
        Box(Modifier.fillMaxSize()) {
            ComposerFabMenu(
                actions =
                    attachActions(
                        onTakePhoto = {},
                        onPickPhotos = {},
                        onRecord = {},
                        onAddLocation = {},
                    ),
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}
