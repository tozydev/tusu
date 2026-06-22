package vn.io.tozyworks.tusu.ui.feature.entryeditor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.compose.AsyncImage
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.io.tozyworks.tusu.domain.model.Media
import vn.io.tozyworks.tusu.domain.model.Tag
import vn.io.tozyworks.tusu.generated.resources.Res
import vn.io.tozyworks.tusu.generated.resources.add_media
import vn.io.tozyworks.tusu.generated.resources.add_media_desc
import vn.io.tozyworks.tusu.generated.resources.back_desc
import vn.io.tozyworks.tusu.generated.resources.done_btn
import vn.io.tozyworks.tusu.generated.resources.edit_btn
import vn.io.tozyworks.tusu.generated.resources.entry_content_prompt
import vn.io.tozyworks.tusu.generated.resources.ic_add_photo_alternate_24px
import vn.io.tozyworks.tusu.generated.resources.ic_add_reaction_24px
import vn.io.tozyworks.tusu.generated.resources.ic_arrow_back_24px
import vn.io.tozyworks.tusu.generated.resources.ic_close_24px
import vn.io.tozyworks.tusu.generated.resources.ic_more_vert_24px
import vn.io.tozyworks.tusu.generated.resources.ic_schedule_24px
import vn.io.tozyworks.tusu.generated.resources.ic_sell_24px
import vn.io.tozyworks.tusu.generated.resources.more_menu_desc
import vn.io.tozyworks.tusu.generated.resources.remove_media_desc
import vn.io.tozyworks.tusu.generated.resources.select_emoji_desc
import vn.io.tozyworks.tusu.ui.component.DatePickerModal
import vn.io.tozyworks.tusu.ui.component.TimePickerModal
import vn.io.tozyworks.tusu.ui.feature.entryeditor.components.EmojiPickerModal
import vn.io.tozyworks.tusu.ui.feature.entryeditor.components.EntryMediaBrowser
import vn.io.tozyworks.tusu.ui.formatter.DateTimeFormatter

context(dateTimeFormatter: DateTimeFormatter)
@Composable
fun EntryEditorScreen(
    viewModel: EntryEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isContentFocus by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.observeContentChanges()
    }

    val handleBack =
        remember(uiState, onNavigateBack) {
            {
                if ((uiState as? EntryEditorUiState.Loaded)?.mode == EntryEditorMode.Edit) {
                    viewModel.setEditorMode(EntryEditorMode.ReadOnly)
                } else {
                    viewModel.onExitEditor()
                    onNavigateBack()
                }
            }
        }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = handleBack,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            val loadedUiState = uiState as? EntryEditorUiState.Loaded
            EntryEditorTopBar(
                editorMode = loadedUiState?.mode ?: EntryEditorMode.ReadOnly,
                emoji = loadedUiState?.emoji,
                onEmojiSelected = viewModel::setEmoji,
                onNavigateBack = onNavigateBack,
                onModeSwitch = viewModel::setEditorMode,
                onOpenMoreMenu = { /*TODO*/ },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
            when (uiState) {
                is EntryEditorUiState.Error -> {
                    Column(
                        modifier = modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            (uiState as EntryEditorUiState.Error).message.asString(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                is EntryEditorUiState.Loaded -> {
                    val loadedUiState = uiState as EntryEditorUiState.Loaded
                    EntryEditorContent(
                        editorMode = loadedUiState.mode,
                        recordedAt = loadedUiState.recordedAt,
                        recordedDate = loadedUiState.recordedDate,
                        onRecordedDateSelect = viewModel::setRecordedDate,
                        recordedTime = loadedUiState.recordedTime,
                        onRecordedTimeSelect = viewModel::setRecordedTime,
                        tags = loadedUiState.tags,
                        contentState = viewModel.contentState,
                        onContentBlur = viewModel::saveContent,
                        onContentFocusChange = { isContentFocus = it },
                        media = loadedUiState.media,
                        onRemoveMedia = viewModel::onRemoveMedia,
                    )
                }
                EntryEditorUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                }
            }
        }
    }
}

context(dateTimeFormatter: DateTimeFormatter)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EntryEditorContent(
    editorMode: EntryEditorMode,
    recordedAt: Instant,
    recordedDate: LocalDate,
    onRecordedDateSelect: (epochMillis: Long) -> Unit,
    recordedTime: LocalTime,
    onRecordedTimeSelect: (LocalTime) -> Unit,
    tags: List<Tag>,
    contentState: RichTextState,
    onContentBlur: () -> Unit,
    onContentFocusChange: (Boolean) -> Unit,
    media: List<Media>,
    onRemoveMedia: (Uuid) -> Unit,
) {
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val formattedShortMonth =
        remember(recordedDate) { dateTimeFormatter.formatShortMonth(recordedDate) }
    val formattedTime = remember(recordedAt) { dateTimeFormatter.formatTime(recordedAt) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (editorMode.isReadOnly) {
            EntryMediaBrowser(media)
        } else {
            EntryMediaEditor(
                mediaList = media,
                onAddMedia = { /*TODO*/ },
                onRemoveMedia = onRemoveMedia,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                onClick = { showDatePicker = true },
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = recordedDate.day.toString(),
                        style = MaterialTheme.typography.displayMediumEmphasized,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.alignBy(FirstBaseline),
                    )
                    Column(modifier = Modifier.alignBy(LastBaseline)) {
                        Text(
                            text = formattedShortMonth.asString().uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = recordedDate.year.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Surface(
                        onClick = { showTimePicker = true },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.height(28.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp).fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_schedule_24px),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }

                    if (tags.isEmpty()) {
                        Surface(
                            onClick = {},
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(28.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_sell_24px),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    tags.forEach {
                        Surface(
                            onClick = {},
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(28.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "#${it.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }
            }
        }

        BasicRichTextEditor(
            state = contentState,
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).onFocusChanged { focusState ->
                    onContentFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        onContentBlur()
                    }
                },
            textStyle = MaterialTheme.typography.bodyLarge,
            readOnly = editorMode.isReadOnly,
            minLines = 6,
            decorationBox = { innerContent ->
                innerContent()
                if (contentState.annotatedString.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.entry_content_prompt),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            },
        )
    }

    when {
        showDatePicker -> {
            DatePickerModal(
                onConfirm = { epochMillis ->
                    epochMillis?.let { onRecordedDateSelect(it) }
                    showDatePicker = false
                },
                onDismissRequest = { showDatePicker = false },
                initialSelectedDateMillis = recordedAt.toEpochMilliseconds(),
            )
        }
        showTimePicker -> {
            TimePickerModal(
                onConfirm = { hour, minute ->
                    onRecordedTimeSelect(LocalTime(hour, minute))
                    showTimePicker = false
                },
                onDismissRequest = { showTimePicker = false },
                initialHour = recordedTime.hour,
                initialMinute = recordedTime.minute,
            )
        }
    }
}

@Composable
private fun EntryMediaEditor(
    mediaList: List<Media>,
    onAddMedia: () -> Unit,
    onRemoveMedia: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(mediaList, { it.id }) { media ->
            Box(
                modifier =
                    Modifier.size(width = 186.dp, height = 220.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
            ) {
                AsyncImage(
                    model = media.path,
                    contentDescription = null,
                    modifier = Modifier.size(width = 186.dp, height = 220.dp),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier =
                        Modifier.align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                            .clickable { onRemoveMedia(media.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close_24px),
                        contentDescription =
                            stringResource(Res.string.remove_media_desc, media.id.toString()),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        item {
            Box(
                modifier =
                    Modifier.size(width = 186.dp, height = 220.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { onAddMedia() },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add_photo_alternate_24px),
                        contentDescription = stringResource(Res.string.add_media_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                    Text(
                        text = stringResource(Res.string.add_media),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryEditorTopBar(
    editorMode: EntryEditorMode,
    emoji: String?,
    onEmojiSelected: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    onModeSwitch: (EntryEditorMode) -> Unit,
    onOpenMoreMenu: () -> Unit,
) {
    var showEmojiModal by remember { mutableStateOf(false) }
    TopAppBar(
        title = {},
        navigationIcon = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = onNavigateBack,
                    colors =
                        IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back_24px),
                        contentDescription = stringResource(Res.string.back_desc),
                    )
                }

                FilledIconButton(
                    onClick = { showEmojiModal = true },
                    colors =
                        IconButtonDefaults.filledIconButtonColors(
                            containerColor =
                                if (emoji == null) MaterialTheme.colorScheme.surfaceContainerLow
                                else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                ) {
                    if (emoji != null) {
                        Text(emoji, fontSize = 20.sp)
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add_reaction_24px),
                            contentDescription = stringResource(Res.string.select_emoji_desc),
                        )
                    }
                }
            }
        },
        actions = {
            when (editorMode) {
                EntryEditorMode.ReadOnly -> {
                    Button(onClick = { onModeSwitch(EntryEditorMode.Edit) }) {
                        Text(stringResource(Res.string.edit_btn))
                    }
                }
                EntryEditorMode.Edit -> {
                    Button(onClick = { onModeSwitch(EntryEditorMode.ReadOnly) }) {
                        Text(stringResource(Res.string.done_btn))
                    }
                }
            }

            IconButton(onClick = onOpenMoreMenu) {
                Icon(
                    painter = painterResource(Res.drawable.ic_more_vert_24px),
                    contentDescription = stringResource(Res.string.more_menu_desc),
                )
            }
        },
    )

    if (showEmojiModal) {
        EmojiPickerModal(
            onEmojiSelected = {
                onEmojiSelected(it)
                showEmojiModal = false
            },
            onDismiss = { showEmojiModal = false },
            initialEmoji = emoji,
        )
    }
}
