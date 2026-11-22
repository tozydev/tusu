package vn.id.tozydev.tusu.ui.component

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.SettingsError
import vn.id.tozydev.tusu.domain.model.AppSettings
import vn.id.tozydev.tusu.domain.repository.SettingsRepository
import vn.id.tozydev.tusu.ui.format.toUiText

private class FakeSettingsRepository(var failure: SettingsError? = null) : SettingsRepository {
    private val state = MutableStateFlow(AppSettings())
    override val settings: Flow<AppSettings> = state

    override suspend fun update(
        transform: (AppSettings) -> AppSettings,
    ): Outcome<Unit, SettingsError> {
        failure?.let {
            return Outcome.Failure(it)
        }
        state.value = transform(state.value)
        return Outcome.Success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RecentEmojisViewModelTest :
    FunSpec(
        {
            beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
            afterTest { Dispatchers.resetMain() }

            test("a picked emoji becomes the first recent one") {
                runTest {
                    val viewModel = RecentEmojisViewModel(FakeSettingsRepository(), AppMessages())
                    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                        viewModel.recent.collect {}
                    }

                    viewModel.onPicked("🌙")
                    viewModel.onPicked("☀️")

                    viewModel.recent.value shouldBe listOf("☀️", "🌙")
                }
            }

            test("a failed write is reported to the user") {
                runTest {
                    val messages = AppMessages()
                    val viewModel =
                        RecentEmojisViewModel(
                            FakeSettingsRepository(failure = SettingsError.StorageFailed),
                            messages,
                        )

                    viewModel.onPicked("🌙")

                    // The channel buffers, so the message is waiting even with no collector yet.
                    messages.messages.first().text shouldBe SettingsError.StorageFailed.toUiText()
                    viewModel.recent.value.shouldBeEmpty()
                }
            }
        },
    )
