package vn.id.tozydev.tusu.ui.feature.entryeditor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Tag
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.back_desc
import vn.id.tozydev.tusu.generated.resources.create_new_tag
import vn.id.tozydev.tusu.generated.resources.enter_tag_name_placeholder
import vn.id.tozydev.tusu.generated.resources.ic_add_24px
import vn.id.tozydev.tusu.generated.resources.ic_arrow_back_24px
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.generated.resources.ic_tag_24px

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TagPickerModal(
    allTags: List<Tag>,
    selectedTags: List<Tag>,
    onTagSelect: (Uuid) -> Unit,
    onTagDeselect: (Uuid) -> Unit,
    onCreateTag: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchFieldState = rememberTextFieldState()
    val filteredTags by
        remember(allTags) {
            derivedStateOf {
                val searchQuery = searchFieldState.text
                if (searchQuery.isBlank()) {
                    allTags
                } else {
                    allTags.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
            }
        }
    val tagNameNotExists by
        remember(allTags) {
            derivedStateOf {
                val searchQuery = searchFieldState.text
                if (searchQuery.isEmpty()) {
                    false
                } else {
                    allTags.none { it.name.contentEquals(searchQuery, ignoreCase = true) }
                }
            }
        }
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        OutlinedTextField(
                            state = searchFieldState,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(stringResource(Res.string.enter_tag_name_placeholder))
                            },
                            trailingIcon = {
                                if (searchFieldState.text.isNotBlank()) {
                                    IconButton(onClick = { searchFieldState.clearText() }) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_close_24px),
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                            lineLimits = TextFieldLineLimits.SingleLine,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                ),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back_24px),
                                contentDescription = stringResource(Res.string.back_desc),
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    if (tagNameNotExists) {
                        item {
                            ListItem(
                                onClick = {
                                    onCreateTag(searchFieldState.text.toString())
                                    searchFieldState.clearText()
                                },
                                leadingContent = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_add_24px),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                },
                            ) {
                                Text(
                                    stringResource(
                                        Res.string.create_new_tag,
                                        searchFieldState.text,
                                    )
                                )
                            }
                        }
                    }
                    itemsIndexed(filteredTags) { index, tag ->
                        val checked = tag in selectedTags
                        SegmentedListItem(
                            checked = checked,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    onTagSelect(tag.id)
                                } else {
                                    onTagDeselect(tag.id)
                                }
                            },
                            shapes = ListItemDefaults.segmentedShapes(index, filteredTags.size),
                            colors = colors,
                            leadingContent = {
                                Icon(painterResource(Res.drawable.ic_tag_24px), null)
                            },
                            trailingContent = {
                                Checkbox(checked = checked, onCheckedChange = null)
                            },
                        ) {
                            Text(text = tag.name)
                        }
                    }
                }
            }
        }
    }
}
