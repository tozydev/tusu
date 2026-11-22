package vn.id.tozydev.tusu.ui.feature.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.domain.error.LabelError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.domain.repository.LabelRepository
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.label_group_single_notice
import vn.id.tozydev.tusu.ui.component.AppMessages
import vn.id.tozydev.tusu.ui.component.UiMessage
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.format.toUiText

/** The label screen: every group and label, and their edits. Failures are posted as messages. */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(LabelsViewModel::class)
@Inject
class LabelsViewModel(
    private val labelRepository: LabelRepository,
    private val appMessages: AppMessages,
) : ViewModel() {
    val uiState: StateFlow<LabelsUiState> =
        combine(labelRepository.observeGroups(), labelRepository.observeLabels()) { groups, labels
                ->
                LabelsUiState(
                    isLoading = false,
                    groups = groups.filter { it.deletedAt == null }.sortedBy { it.sortOrder },
                    labels = labels.filter { it.deletedAt == null },
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                LabelsUiState(),
            )

    fun onCreateGroup(name: String, color: Int, selection: LabelSelection) = write {
        labelRepository.createGroup(name.trim(), color, selection)
    }

    /**
     * Saves the group. Turning a multiple-choice group into single choice leaves existing entries
     * as they are, so the user is told how many still hold several of its labels.
     */
    fun onUpdateGroup(group: LabelGroup) {
        val before = uiState.value.groups.firstOrNull { it.id == group.id }
        val becomesSingle =
            before?.selection == LabelSelection.MULTI && group.selection == LabelSelection.SINGLE
        viewModelScope.launch {
            val outcome = labelRepository.updateGroup(group.copy(name = group.name.trim()))
            if (outcome is Outcome.Failure) {
                appMessages.post(UiMessage(outcome.error.toUiText()))
                return@launch
            }
            if (!becomesSingle) return@launch
            when (val count = labelRepository.countEntriesWithSeveralLabels(group.id)) {
                is Outcome.Failure -> appMessages.post(UiMessage(count.error.toUiText()))
                is Outcome.Success ->
                    if (count.value > 0) {
                        appMessages.post(
                            UiMessage(
                                UiText.plural(
                                    Res.plurals.label_group_single_notice,
                                    count.value,
                                    count.value,
                                ),
                            ),
                        )
                    }
            }
        }
    }

    /** The repository moves the group's labels to "Khác", so no entry loses a label. */
    fun onDeleteGroup(id: Uuid) = write { labelRepository.deleteGroup(id) }

    fun onCreateLabel(name: String, groupId: Uuid?) = write {
        labelRepository.createLabel(name.trim(), groupId)
    }

    /** Renames, moves to another group (null for "Khác") or hides the label. */
    fun onUpdateLabel(label: Label) = write {
        labelRepository.updateLabel(label.copy(name = label.name.trim()))
    }

    fun onDeleteLabel(id: Uuid) = write { labelRepository.deleteLabel(id) }

    private fun write(block: suspend () -> Outcome<*, LabelError>) {
        viewModelScope.launch {
            val outcome = block()
            if (outcome is Outcome.Failure) appMessages.post(UiMessage(outcome.error.toUiText()))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
