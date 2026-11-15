package vn.id.tozydev.tusu.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Text that a ViewModel can keep in its `UiState` and that is turned into a localized string only
 * where a locale is available: in composition ([asString]) or in a coroutine ([resolve]).
 */
@Immutable
sealed interface UiText {

    /** User content or an already formatted string; shown as is. */
    data class Dynamic(val value: String) : UiText

    /** A string resource; [args] may be plain values or other [UiText]s. */
    data class Resource(val resource: StringResource, val args: List<Any> = emptyList()) : UiText

    /** A plural resource picked by [quantity]; [args] follow the same rules as in [Resource]. */
    data class Plural(
        val resource: PluralStringResource,
        val quantity: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    companion object {
        fun of(value: String): UiText = Dynamic(value)

        fun of(resource: StringResource, vararg args: Any): UiText =
            Resource(resource, args.toList())

        fun plural(resource: PluralStringResource, quantity: Int, vararg args: Any): UiText =
            Plural(resource, quantity, args.toList())
    }
}

/** Resolves this text in composition. */
@Composable
fun UiText.asString(): String =
    when (this) {
        is UiText.Dynamic -> value
        is UiText.Resource ->
            stringResource(
                resource,
                *args.map { if (it is UiText) it.asString() else it }.toTypedArray(),
            )
        is UiText.Plural ->
            pluralStringResource(
                resource,
                quantity,
                *args.map { if (it is UiText) it.asString() else it }.toTypedArray(),
            )
    }

/** Resolves this text outside composition, for example for a snackbar. */
suspend fun UiText.resolve(): String =
    when (this) {
        is UiText.Dynamic -> value
        is UiText.Resource -> getString(resource, *args.resolveAll())
        is UiText.Plural -> getPluralString(resource, quantity, *args.resolveAll())
    }

private suspend fun List<Any>.resolveAll(): Array<Any> =
    Array(size) { index -> this[index].let { if (it is UiText) it.resolve() else it } }
