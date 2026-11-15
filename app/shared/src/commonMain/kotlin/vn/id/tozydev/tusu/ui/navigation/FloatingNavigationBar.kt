package vn.id.tozydev.tusu.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.nav_main_description
import vn.id.tozydev.tusu.ui.theme.AppTheme
import vn.id.tozydev.tusu.ui.theme.PillShape

internal val FloatingBarHeight = 64.dp
internal val FloatingBarShadow = 6.dp

/** The navigation pill with the three tabs, followed by the [NewEntryButton]. */
@Composable
internal fun FloatingNavigationBar(
    selectedTab: AppRoute.TopLevel,
    onTabSelect: (AppRoute.TopLevel) -> Unit,
    onNewEntryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationPill(selectedTab = selectedTab, onTabSelect = onTabSelect)
        NewEntryButton(onClick = onNewEntryClick)
    }
}

@Composable
private fun NavigationPill(
    selectedTab: AppRoute.TopLevel,
    onTabSelect: (AppRoute.TopLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.nav_main_description)
    Surface(
        modifier = modifier.semantics { contentDescription = description },
        shape = PillShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = FloatingBarShadow,
    ) {
        Row(
            modifier = Modifier.height(FloatingBarHeight).padding(8.dp).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (tab in TopLevelTab.entries) {
                NavigationTabItem(
                    selected = tab.route == selectedTab,
                    icon = tab.icon,
                    label = stringResource(tab.label),
                    onClick = { onTabSelect(tab.route) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun FloatingNavigationBarPreview() {
    AppTheme {
        FloatingNavigationBar(
            selectedTab = AppRoute.Timeline,
            onTabSelect = {},
            onNewEntryClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun FloatingNavigationBarMediaPreview() {
    AppTheme {
        FloatingNavigationBar(
            selectedTab = AppRoute.Media,
            onTabSelect = {},
            onNewEntryClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
