package vn.id.tozydev.tusu.ui.feature.entryeditor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.mohamedrejeb.richeditor.model.HeadingStyle
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.Tag
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_delete
import vn.id.tozydev.tusu.generated.resources.action_done
import vn.id.tozydev.tusu.generated.resources.action_edit
import vn.id.tozydev.tusu.generated.resources.cd_back
import vn.id.tozydev.tusu.generated.resources.cd_more_menu
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_add_media
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_add_tag
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_bold
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_bullet_list
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_code
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_h1
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_h2
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_h3
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_italic
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_ordered_list
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_quote
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_redo
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_select_emoji
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_strikethrough
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_timestamp
import vn.id.tozydev.tusu.generated.resources.entry_editor_cd_undo
import vn.id.tozydev.tusu.generated.resources.entry_editor_prompt
import vn.id.tozydev.tusu.generated.resources.ic_add_24px
import vn.id.tozydev.tusu.generated.resources.ic_add_photo_alternate_24px
import vn.id.tozydev.tusu.generated.resources.ic_add_reaction_24px
import vn.id.tozydev.tusu.generated.resources.ic_arrow_back_24px
import vn.id.tozydev.tusu.generated.resources.ic_code_24px
import vn.id.tozydev.tusu.generated.resources.ic_delete_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_bold_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_h1_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_h2_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_h3_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_italic_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_list_bulleted_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_list_numbered_rtl_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_quote_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_strikethrough_24px
import vn.id.tozydev.tusu.generated.resources.ic_more_vert_24px
import vn.id.tozydev.tusu.generated.resources.ic_redo_24px
import vn.id.tozydev.tusu.generated.resources.ic_schedule_24px
import vn.id.tozydev.tusu.generated.resources.ic_sell_24px
import vn.id.tozydev.tusu.generated.resources.ic_undo_24px
import vn.id.tozydev.tusu.ui.component.DatePickerModal
import vn.id.tozydev.tusu.ui.component.TimePickerModal
import vn.id.tozydev.tusu.ui.feature.entryeditor.components.EmojiPickerModal
import vn.id.tozydev.tusu.ui.feature.entryeditor.components.EntryMediaBrowser
import vn.id.tozydev.tusu.ui.feature.entryeditor.components.EntryMediaEditor
import vn.id.tozydev.tusu.ui.feature.entryeditor.components.MediaPickerModal
import vn.id.tozydev.tusu.ui.feature.entryeditor.components.TagPickerModal
import vn.id.tozydev.tusu.ui.formatter.DateTimeFormatter

context(dateTimeFormatter: DateTimeFormatter)
@Composable
fun EntryEditorScreen(
    viewModel: EntryEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val contentFocusRequester = remember { FocusRequester() }
    var isContentFocus by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.observeContentChanges()
    }

    LaunchedEffect((uiState as? EntryEditorUiState.Loaded)?.mode) {
        if ((uiState as? EntryEditorUiState.Loaded)?.mode == EntryEditorMode.Edit) {
            contentFocusRequester.requestFocus()
            delay(200.milliseconds)
            keyboardController?.show()
        }
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

    var showMediaPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            val loadedUiState = uiState as? EntryEditorUiState.Loaded
            EntryEditorTopBar(
                editorMode = loadedUiState?.mode ?: EntryEditorMode.ReadOnly,
                emoji = loadedUiState?.emoji,
                onEmojiSelected = viewModel::setEmoji,
                onNavigateBack = handleBack,
                onModeSwitch = viewModel::setEditorMode,
                onDeleteEntry = {
                    viewModel.deleteEntry()
                    onNavigateBack()
                },
            )
        },
        bottomBar = {
            val loadedUiState = uiState as? EntryEditorUiState.Loaded
            if (loadedUiState?.mode == EntryEditorMode.Edit) {
                EntryEditorBottomBar(
                    isContentFocus = isContentFocus,
                    contentState = viewModel.contentState,
                    onAddMedia = { showMediaPicker = true },
                    onOpenTagPicker = { showTagPicker = true },
                    recordedAt = loadedUiState.recordedAt,
                )
            }
        },
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        top = innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        bottom = 0.dp,
                    )
                    .consumeWindowInsets(innerPadding)
        ) {
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
                        modifier =
                            Modifier.padding(
                                bottom = innerPadding.calculateBottomPadding() + 16.dp
                            ),
                        editorMode = loadedUiState.mode,
                        recordedAt = loadedUiState.recordedAt,
                        recordedDate = loadedUiState.recordedDate,
                        onRecordedDateSelect = viewModel::setRecordedDate,
                        recordedTime = loadedUiState.recordedTime,
                        onRecordedTimeSelect = viewModel::setRecordedTime,
                        tags = loadedUiState.tags,
                        onOpenTagPicker = { showTagPicker = true },
                        contentState = viewModel.contentState,
                        contentFocusRequester = contentFocusRequester,
                        onContentBlur = viewModel::saveContent,
                        onContentFocusChange = { isContentFocus = it },
                        media = loadedUiState.media,
                        onRemoveMedia = viewModel::removeMedia,
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

    if (showMediaPicker) {
        MediaPickerModal(
            onDismiss = { showMediaPicker = false },
            onSelectMedia = {
                showMediaPicker = false
                viewModel.addMedia(it)
            },
        )
    }

    val loadedState = uiState as? EntryEditorUiState.Loaded
    if (showTagPicker && loadedState != null) {
        TagPickerModal(
            allTags = allTags,
            selectedTags = loadedState.tags,
            onTagSelect = viewModel::selectTag,
            onTagDeselect = viewModel::deselectTag,
            onCreateTag = viewModel::createAndSelectTag,
            onDismiss = { showTagPicker = false },
        )
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
    onOpenTagPicker: () -> Unit,
    contentState: RichTextState,
    contentFocusRequester: FocusRequester,
    onContentBlur: () -> Unit,
    onContentFocusChange: (Boolean) -> Unit,
    media: List<Media>,
    onRemoveMedia: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var viewportHeight by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(contentState.selection, viewportHeight, isFocused, textLayoutResult) {
        if (isFocused) {
            val layoutResult = textLayoutResult
            val selection = contentState.selection
            if (
                layoutResult != null &&
                    selection.collapsed &&
                    selection.start in 0..layoutResult.layoutInput.text.length
            ) {
                val cursorRect = layoutResult.getCursorRect(selection.start)
                bringIntoViewRequester.bringIntoView(cursorRect)
            }
        }
    }

    val formattedShortMonth =
        remember(recordedDate) { dateTimeFormatter.formatShortMonth(recordedDate) }
    val formattedTime = remember(recordedAt) { dateTimeFormatter.formatTime(recordedAt) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    viewportHeight = coordinates.size.height
                }
                .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (media.isNotEmpty()) {
            if (editorMode.isReadOnly) {
                EntryMediaBrowser(media)
            } else {
                EntryMediaEditor(
                    mediaList = media,
                    onRemoveMedia = onRemoveMedia,
                )
            }
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
                            onClick = onOpenTagPicker,
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
                            onClick = onOpenTagPicker,
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

                    if (editorMode == EntryEditorMode.Edit) {
                        Surface(
                            onClick = onOpenTagPicker,
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
                                    painter = painterResource(Res.drawable.ic_add_24px),
                                    contentDescription =
                                        stringResource(Res.string.entry_editor_cd_add_tag),
                                    modifier = Modifier.size(16.dp),
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
                Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .focusRequester(contentFocusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        onContentFocusChange(focusState.isFocused)
                        if (!focusState.isFocused) {
                            onContentBlur()
                        }
                    },
            textStyle =
                MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
            readOnly = editorMode.isReadOnly,
            minLines = 6,
            onTextLayout = { textLayoutResult = it },
            decorationBox = { innerContent ->
                innerContent()
                if (contentState.annotatedString.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.entry_editor_prompt),
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
private fun EntryEditorTopBar(
    editorMode: EntryEditorMode,
    emoji: String?,
    onEmojiSelected: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    onModeSwitch: (EntryEditorMode) -> Unit,
    onDeleteEntry: () -> Unit,
) {
    var showEmojiModal by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }
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
                        contentDescription = stringResource(Res.string.cd_back),
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
                            contentDescription =
                                stringResource(Res.string.entry_editor_cd_select_emoji),
                        )
                    }
                }
            }
        },
        actions = {
            when (editorMode) {
                EntryEditorMode.ReadOnly -> {
                    Button(onClick = { onModeSwitch(EntryEditorMode.Edit) }) {
                        Text(stringResource(Res.string.action_edit))
                    }
                }
                EntryEditorMode.Edit -> {
                    Button(onClick = { onModeSwitch(EntryEditorMode.ReadOnly) }) {
                        Text(stringResource(Res.string.action_done))
                    }
                }
            }

            IconButton(onClick = { moreMenuExpanded = true }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_more_vert_24px),
                    contentDescription = stringResource(Res.string.cd_more_menu),
                )
            }

            MoreDropdownMenu(
                expanded = moreMenuExpanded,
                onDismiss = { moreMenuExpanded = false },
                onDeleteEntry = onDeleteEntry,
            )
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

@Composable
private fun MoreDropdownMenu(
    expanded: Boolean = false,
    onDismiss: () -> Unit,
    onDeleteEntry: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.action_delete)) },
            leadingIcon = {
                Icon(painterResource(Res.drawable.ic_delete_24px), contentDescription = null)
            },
            onClick = onDeleteEntry,
            colors =
                MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error,
                ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
context(dateTimeFormatter: DateTimeFormatter)
@Composable
private fun EntryEditorBottomBar(
    isContentFocus: Boolean,
    contentState: RichTextState,
    onAddMedia: () -> Unit,
    onOpenTagPicker: () -> Unit,
    recordedAt: Instant,
    modifier: Modifier = Modifier,
) {
    FlexibleBottomAppBar(
        modifier = modifier.fillMaxWidth().imePadding(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        expandedHeight = 52.dp,
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item(key = "add_media") {
                IconButton(
                    onClick = onAddMedia,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add_photo_alternate_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_add_media),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            item(key = "add_tag") {
                IconButton(
                    onClick = onOpenTagPicker,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_sell_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_add_tag),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            if (isContentFocus) {
                item(key = "h1") {
                    RichTextStyleButton(
                        onClick = {
                            if (contentState.currentHeadingStyle != HeadingStyle.H1) {
                                contentState.setHeadingStyle(HeadingStyle.H1)
                            } else {
                                contentState.setHeadingStyle(HeadingStyle.Normal)
                            }
                        },
                        isSelected = contentState.currentHeadingStyle == HeadingStyle.H1,
                        icon = painterResource(Res.drawable.ic_format_h1_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_h1),
                    )
                }

                item(key = "h2") {
                    RichTextStyleButton(
                        onClick = {
                            if (contentState.currentHeadingStyle != HeadingStyle.H2) {
                                contentState.setHeadingStyle(HeadingStyle.H2)
                            } else {
                                contentState.setHeadingStyle(HeadingStyle.Normal)
                            }
                        },
                        isSelected = contentState.currentHeadingStyle == HeadingStyle.H2,
                        icon = painterResource(Res.drawable.ic_format_h2_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_h2),
                    )
                }

                item(key = "h3") {
                    RichTextStyleButton(
                        onClick = {
                            if (contentState.currentHeadingStyle != HeadingStyle.H3) {
                                contentState.setHeadingStyle(HeadingStyle.H3)
                            } else {
                                contentState.setHeadingStyle(HeadingStyle.Normal)
                            }
                        },
                        isSelected = contentState.currentHeadingStyle == HeadingStyle.H3,
                        icon = painterResource(Res.drawable.ic_format_h3_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_h3),
                    )
                }

                item(key = "bold") {
                    RichTextStyleButton(
                        onClick = {
                            contentState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        },
                        isSelected = contentState.currentSpanStyle.fontWeight == FontWeight.Bold,
                        icon = painterResource(Res.drawable.ic_format_bold_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_bold),
                    )
                }

                item(key = "italic") {
                    RichTextStyleButton(
                        onClick = {
                            contentState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        },
                        isSelected = contentState.currentSpanStyle.fontStyle == FontStyle.Italic,
                        icon = painterResource(Res.drawable.ic_format_italic_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_italic),
                    )
                }

                item(key = "strikethrough") {
                    RichTextStyleButton(
                        onClick = {
                            contentState.toggleSpanStyle(
                                SpanStyle(textDecoration = TextDecoration.LineThrough)
                            )
                        },
                        isSelected =
                            contentState.currentSpanStyle.textDecoration?.contains(
                                TextDecoration.LineThrough
                            ) == true,
                        icon = painterResource(Res.drawable.ic_format_strikethrough_24px),
                        contentDescription =
                            stringResource(Res.string.entry_editor_cd_strikethrough),
                    )
                }

                item(key = "bullet_list") {
                    RichTextStyleButton(
                        onClick = { contentState.toggleUnorderedList() },
                        isSelected = contentState.isUnorderedList,
                        icon = painterResource(Res.drawable.ic_format_list_bulleted_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_bullet_list),
                    )
                }

                item(key = "ordered_list") {
                    RichTextStyleButton(
                        onClick = { contentState.toggleOrderedList() },
                        isSelected = contentState.isOrderedList,
                        icon = painterResource(Res.drawable.ic_format_list_numbered_rtl_24px),
                        contentDescription =
                            stringResource(Res.string.entry_editor_cd_ordered_list),
                    )
                }

                item(key = "quote") {
                    RichTextStyleButton(
                        onClick = {
                            val current = contentState.toMarkdown()
                            contentState.setMarkdown("$current\n> ")
                        },
                        isSelected = false,
                        icon = painterResource(Res.drawable.ic_format_quote_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_quote),
                    )
                }

                item(key = "code") {
                    RichTextStyleButton(
                        onClick = { contentState.toggleCodeSpan() },
                        isSelected = contentState.isCodeSpan,
                        icon = painterResource(Res.drawable.ic_code_24px),
                        contentDescription = stringResource(Res.string.entry_editor_cd_code),
                    )
                }

                item(key = "timestamp") {
                    IconButton(
                        onClick = {
                            val timeStr = dateTimeFormatter.formatTime(recordedAt)
                            contentState.setMarkdown(contentState.toMarkdown() + " [$timeStr] ")
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_schedule_24px),
                            contentDescription =
                                stringResource(Res.string.entry_editor_cd_timestamp),
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { contentState.history.undo() },
                enabled = contentState.history.canUndo,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_undo_24px),
                    contentDescription = stringResource(Res.string.entry_editor_cd_undo),
                    modifier = Modifier.size(20.dp),
                    tint =
                        if (contentState.history.canUndo) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
            }

            IconButton(
                onClick = { contentState.history.redo() },
                enabled = contentState.history.canRedo,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_redo_24px),
                    contentDescription = stringResource(Res.string.entry_editor_cd_redo),
                    modifier = Modifier.size(20.dp),
                    tint =
                        if (contentState.history.canRedo) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
            }
        }
    }
}

@Composable
private fun RichTextStyleButton(
    onClick: () -> Unit,
    isSelected: Boolean,
    icon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor =
                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                    else Color.Transparent,
                contentColor =
                    if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        modifier = modifier.size(40.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
        )
    }
}
