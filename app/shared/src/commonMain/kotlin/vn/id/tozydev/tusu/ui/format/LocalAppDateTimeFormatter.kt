package vn.id.tozydev.tusu.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.date_months_full
import vn.id.tozydev.tusu.generated.resources.date_months_short
import vn.id.tozydev.tusu.generated.resources.date_pattern_day_header
import vn.id.tozydev.tusu.generated.resources.date_pattern_day_header_with_year
import vn.id.tozydev.tusu.generated.resources.date_pattern_full_date_time
import vn.id.tozydev.tusu.generated.resources.date_pattern_month_title
import vn.id.tozydev.tusu.generated.resources.date_today
import vn.id.tozydev.tusu.generated.resources.date_weekdays
import vn.id.tozydev.tusu.generated.resources.date_yesterday

val LocalAppDateTimeFormatter =
    compositionLocalOf<AppDateTimeFormatter> {
        error("No AppDateTimeFormatter provided; wrap the content in ProvideAppDateTimeFormatter")
    }

/** Builds a formatter from the strings of the current app locale. */
@Composable
fun rememberAppDateTimeFormatter(): AppDateTimeFormatter {
    val names =
        DateTimeNames(
            weekdays = stringArrayResource(Res.array.date_weekdays),
            monthsShort = stringArrayResource(Res.array.date_months_short),
            monthsFull = stringArrayResource(Res.array.date_months_full),
            today = stringResource(Res.string.date_today),
            yesterday = stringResource(Res.string.date_yesterday),
            dayHeaderPattern = stringResource(Res.string.date_pattern_day_header),
            dayHeaderWithYearPattern = stringResource(Res.string.date_pattern_day_header_with_year),
            monthTitlePattern = stringResource(Res.string.date_pattern_month_title),
            fullDateTimePattern = stringResource(Res.string.date_pattern_full_date_time),
        )
    return remember(names) { AppDateTimeFormatter(names) }
}

@Composable
fun ProvideAppDateTimeFormatter(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppDateTimeFormatter provides rememberAppDateTimeFormatter(),
        content = content,
    )
}
