package vn.id.tozydev.tusu.ui.feature.settings.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.domain.repository.BackupRepository

sealed interface SettingsBackupState {
    data object Idle : SettingsBackupState
    data object Exporting : SettingsBackupState
    data object Importing : SettingsBackupState
    data object ExportSuccess : SettingsBackupState
    data object ImportSuccess : SettingsBackupState
    data class Error(val message: String) : SettingsBackupState
}

@ViewModelKey
@ContributesIntoMap(AppScope::class)
class SettingsBackupViewModel(
    private val backupRepository: BackupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsBackupState>(SettingsBackupState.Idle)
    val uiState: StateFlow<SettingsBackupState> = _uiState.asStateFlow()

    fun exportBackup(destFile: PlatformFile) {
        viewModelScope.launch {
            _uiState.update { SettingsBackupState.Exporting }
            runCatching {
                backupRepository.exportBackup(destFile)
            }.onSuccess {
                _uiState.update { SettingsBackupState.ExportSuccess }
            }.onFailure { e ->
                _uiState.update { SettingsBackupState.Error(e.message ?: "Unknown export error") }
            }
        }
    }

    fun importBackup(srcFile: PlatformFile) {
        viewModelScope.launch {
            _uiState.update { SettingsBackupState.Importing }
            runCatching {
                backupRepository.importBackup(srcFile)
            }.onSuccess {
                _uiState.update { SettingsBackupState.ImportSuccess }
            }.onFailure { e ->
                _uiState.update { SettingsBackupState.Error(e.message ?: "Unknown import error") }
            }
        }
    }

    fun resetState() {
        _uiState.update { SettingsBackupState.Idle }
    }
}
