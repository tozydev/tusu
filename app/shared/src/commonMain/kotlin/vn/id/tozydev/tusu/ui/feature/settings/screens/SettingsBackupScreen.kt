package vn.id.tozydev.tusu.ui.feature.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.expressive.SettingsGroup
import com.alorma.compose.settings.ui.expressive.SettingsMenuLink
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_cancel
import vn.id.tozydev.tusu.generated.resources.ic_download_24px
import vn.id.tozydev.tusu.generated.resources.ic_save_24px
import vn.id.tozydev.tusu.generated.resources.ic_upload_24px
import vn.id.tozydev.tusu.generated.resources.ic_upload_file_24px
import vn.id.tozydev.tusu.generated.resources.settings_backup_export_action
import vn.id.tozydev.tusu.generated.resources.settings_backup_export_desc
import vn.id.tozydev.tusu.generated.resources.settings_backup_export_title
import vn.id.tozydev.tusu.generated.resources.settings_backup_exporting
import vn.id.tozydev.tusu.generated.resources.settings_backup_restore_action
import vn.id.tozydev.tusu.generated.resources.settings_backup_restore_dialog_desc
import vn.id.tozydev.tusu.generated.resources.settings_backup_restore_subtitle
import vn.id.tozydev.tusu.generated.resources.settings_backup_restore_title
import vn.id.tozydev.tusu.generated.resources.settings_backup_restoring
import vn.id.tozydev.tusu.generated.resources.settings_backup_select_file

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsBackupScreen(
    viewModel: SettingsBackupViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRestoreWarning by remember { mutableStateOf(false) }
    var selectedRestoreFile by remember { mutableStateOf<PlatformFile?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher =
        rememberFileSaverLauncher(dialogSettings = FileKitDialogSettings.createDefault()) { file ->
            if (file != null) {
                viewModel.exportBackup(file)
            }
        }

    val importLauncher =
        rememberFilePickerLauncher(type = FileKitType.File("zip")) { file ->
            if (file != null) {
                selectedRestoreFile = file
                showRestoreWarning = true
            }
        }

    LaunchedEffect(uiState) {
        when (uiState) {
            SettingsBackupState.ExportSuccess -> {
                snackbarHostState.showSnackbar("Backup exported successfully!")
                viewModel.resetState()
            }
            SettingsBackupState.ImportSuccess -> {
                snackbarHostState.showSnackbar("Backup restored successfully!")
                viewModel.resetState()
            }
            is SettingsBackupState.Error -> {
                val errorMsg = (uiState as SettingsBackupState.Error).message
                snackbarHostState.showSnackbar("Error: $errorMsg")
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val menuLinkColors =
        ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        val isExporting = uiState is SettingsBackupState.Exporting
        val isImporting = uiState is SettingsBackupState.Importing

        SettingsGroup(
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            SettingsMenuLink(
                title = {
                    Text(
                        stringResource(Res.string.settings_backup_export_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                subtitle = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(Res.string.settings_backup_export_desc))
                        Button(
                            onClick = {
                                if (!isExporting && !isImporting) {
                                    exportLauncher.launch(
                                        suggestedName = "tusu_backup",
                                        defaultExtension = "zip",
                                        allowedExtensions = setOf("zip"),
                                    )
                                }
                            },
                            enabled = !isExporting,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (isExporting) {
                                LoadingIndicator(
                                    modifier =
                                        Modifier.size(ButtonDefaults.SmallIconSize)
                                            .padding(end = 4.dp)
                                )
                                Text(stringResource(Res.string.settings_backup_exporting))
                            } else {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_save_24px),
                                    contentDescription = null,
                                    modifier =
                                        Modifier.size(ButtonDefaults.SmallIconSize)
                                            .padding(end = 4.dp),
                                )
                                Text(stringResource(Res.string.settings_backup_export_action))
                            }
                        }
                    }
                },
                onClick = {},
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_upload_24px),
                        contentDescription = null,
                    )
                },
                shapes =
                    ListItemDefaults.segmentedShapes(
                        0,
                        2,
                    ),
                colors = menuLinkColors,
            )

            SettingsMenuLink(
                title = {
                    Text(
                        stringResource(Res.string.settings_backup_restore_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                subtitle = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(Res.string.settings_backup_restore_subtitle))
                        OutlinedButton(
                            onClick = {
                                if (!isExporting && !isImporting) {
                                    importLauncher.launch()
                                }
                            },
                            enabled = !isImporting,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (isImporting) {
                                LoadingIndicator(
                                    modifier =
                                        Modifier.size(ButtonDefaults.SmallIconSize)
                                            .padding(end = 4.dp)
                                )
                                Text(stringResource(Res.string.settings_backup_restoring))
                            } else {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_upload_file_24px),
                                    contentDescription = null,
                                    modifier =
                                        Modifier.size(ButtonDefaults.SmallIconSize)
                                            .padding(end = 4.dp),
                                )
                                Text(stringResource(Res.string.settings_backup_select_file))
                            }
                        }
                    }
                },
                onClick = {},
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_download_24px),
                        contentDescription = null,
                    )
                },
                shapes = ListItemDefaults.segmentedShapes(1, 2),
                colors = menuLinkColors,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SnackbarHost(hostState = snackbarHostState)
    }

    if (showRestoreWarning) {
        AlertDialog(
            onDismissRequest = {
                showRestoreWarning = false
                selectedRestoreFile = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreWarning = false
                        selectedRestoreFile?.let { viewModel.importBackup(it) }
                        selectedRestoreFile = null
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                ) {
                    Text(stringResource(Res.string.settings_backup_restore_action))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreWarning = false
                        selectedRestoreFile = null
                    }
                ) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
            title = { Text(stringResource(Res.string.settings_backup_restore_action)) },
            text = { Text(stringResource(Res.string.settings_backup_restore_dialog_desc)) },
        )
    }
}
