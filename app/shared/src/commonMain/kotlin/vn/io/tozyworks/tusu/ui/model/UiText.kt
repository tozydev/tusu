package vn.io.tozyworks.tusu.ui.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    @JvmInline value class Dynamic(val value: String) : UiText

    data class Resource(val resource: StringResource, val args: List<Any> = emptyList()) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is Dynamic -> value
            is Resource -> stringResource(resource, *args.toTypedArray())
        }
    }
}

fun UiText(string: String): UiText = UiText.Dynamic(string)

fun UiText(resource: StringResource, vararg args: Any): UiText =
    UiText.Resource(resource, args.toList())
