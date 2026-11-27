package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_remove_location
import vn.id.tozydev.tusu.generated.resources.ic_location_on_24px
import vn.id.tozydev.tusu.generated.resources.ic_my_location_24px
import vn.id.tozydev.tusu.generated.resources.location_name_apply
import vn.id.tozydev.tusu.generated.resources.location_name_label
import vn.id.tozydev.tusu.generated.resources.location_recent
import vn.id.tozydev.tusu.generated.resources.location_sheet_title
import vn.id.tozydev.tusu.generated.resources.location_use_current
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * Sets the place of an entry: the current location, a free-typed name without coordinates, or a
 * recent place. Picking anything closes the sheet through [onDismiss].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    current: EntryLocation?,
    recent: List<EntryLocation>,
    onUseCurrent: () -> Unit,
    onPick: (EntryLocation?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier, dragHandle = null) {
        LocationPickerContent(
            current = current,
            recent = recent,
            onUseCurrent = {
                onUseCurrent()
                onDismiss()
            },
            onPick = {
                onPick(it)
                onDismiss()
            },
        )
    }
}

@Composable
private fun LocationPickerContent(
    current: EntryLocation?,
    recent: List<EntryLocation>,
    onUseCurrent: () -> Unit,
    onPick: (EntryLocation?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.location_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        ListItem(
            headlineContent = { Text(stringResource(Res.string.location_use_current)) },
            leadingContent = {
                Icon(painterResource(Res.drawable.ic_my_location_24px), contentDescription = null)
            },
            modifier = Modifier.clickable(onClick = onUseCurrent),
        )
        FreeNameField(
            initial = current?.takeIf { it.coordinates == null }?.name.orEmpty(),
            onApply = { onPick(EntryLocation(name = it)) },
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        RecentPlaces(recent = recent.filter { it != current }, onPick = onPick)
        if (current != null) {
            TextButton(
                onClick = { onPick(null) },
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                Text(stringResource(Res.string.composer_remove_location))
            }
        }
    }
}

/** A name such as "Nhà ngoại", stored without coordinates or address. */
@Composable
private fun FreeNameField(
    initial: String,
    onApply: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf(initial) }
    val trimmed = name.trim()
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(Res.string.location_name_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(onDone = { if (trimmed.isNotEmpty()) onApply(trimmed) }),
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { onApply(trimmed) }, enabled = trimmed.isNotEmpty()) {
            Text(stringResource(Res.string.location_name_apply))
        }
    }
}

@Composable
private fun RecentPlaces(
    recent: List<EntryLocation>,
    onPick: (EntryLocation) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (recent.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.location_recent),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )
        recent.forEach { place ->
            ListItem(
                headlineContent = { Text(place.name, maxLines = 1) },
                supportingContent = place.address?.let { { Text(it, maxLines = 1) } },
                leadingContent = {
                    Icon(
                        painterResource(Res.drawable.ic_location_on_24px),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable { onPick(place) },
            )
        }
    }
}

@Preview
@Composable
private fun LocationPickerContentPreview() {
    AppTheme {
        LocationPickerContent(
            current = EntryLocation(name = "Nhà ngoại"),
            recent =
                listOf(
                    EntryLocation(name = "Hồ Gươm", address = "Hoàn Kiếm, Hà Nội"),
                    EntryLocation(name = "Cà phê Lê Văn Sỹ", address = "Quận 3, TP.HCM"),
                ),
            onUseCurrent = {},
            onPick = {},
        )
    }
}
