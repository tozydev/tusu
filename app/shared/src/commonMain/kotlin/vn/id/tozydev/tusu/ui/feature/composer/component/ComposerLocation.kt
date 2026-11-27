package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_add_location
import vn.id.tozydev.tusu.generated.resources.composer_remove_location
import vn.id.tozydev.tusu.generated.resources.composer_suggested_location
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.generated.resources.ic_location_on_24px
import vn.id.tozydev.tusu.generated.resources.location_locating
import vn.id.tozydev.tusu.ui.component.CompactMapTile
import vn.id.tozydev.tusu.ui.component.LocationCard
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * The place of the entry under the emoji row: a compact card that opens the location sheet, or the
 * "Add location" button, with a suggested place next to it when the setting only suggests.
 */
@Composable
fun ComposerLocation(
    location: EntryLocation?,
    isLocating: Boolean,
    suggestedLocation: EntryLocation?,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onAcceptSuggestion: (EntryLocation) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        location != null ->
            LocationCard(
                name = location.name,
                address = location.address,
                mapSnapshotPath = location.mapSnapshotPath,
                hasCoordinates = location.coordinates != null,
                tileSize = CompactMapTile,
                onClick = onOpen,
                trailing = {
                    IconButton(onClick = onRemove) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close_24px),
                            contentDescription =
                                stringResource(Res.string.composer_remove_location),
                        )
                    }
                },
                modifier = modifier.padding(vertical = 4.dp),
            )
        isLocating -> LocatingRow(modifier)
        else ->
            FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (suggestedLocation != null) {
                    LocationChip(
                        text =
                            stringResource(
                                Res.string.composer_suggested_location,
                                suggestedLocation.name,
                            ),
                        onClick = { onAcceptSuggestion(suggestedLocation) },
                    )
                }
                LocationChip(
                    text = stringResource(Res.string.composer_add_location),
                    onClick = onOpen,
                )
            }
    }
}

@Composable
private fun LocationChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, maxLines = 1) },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.ic_location_on_24px),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun LocatingRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Text(
            text = stringResource(Res.string.location_locating),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun ComposerLocationCardPreview() {
    AppTheme {
        ComposerLocation(
            location =
                EntryLocation(
                    name = "Cà phê Lê Văn Sỹ",
                    address = "Phường 13, Quận 3, TP.HCM",
                    coordinates = GeoPoint(10.78, 106.67),
                ),
            isLocating = false,
            suggestedLocation = null,
            onOpen = {},
            onRemove = {},
            onAcceptSuggestion = {},
        )
    }
}

@Preview
@Composable
private fun ComposerLocationAddPreview() {
    AppTheme {
        ComposerLocation(
            location = null,
            isLocating = false,
            suggestedLocation = EntryLocation(name = "Quận 3"),
            onOpen = {},
            onRemove = {},
            onAcceptSuggestion = {},
        )
    }
}

@Preview
@Composable
private fun ComposerLocationLocatingPreview() {
    AppTheme {
        ComposerLocation(
            location = null,
            isLocating = true,
            suggestedLocation = null,
            onOpen = {},
            onRemove = {},
            onAcceptSuggestion = {},
        )
    }
}
