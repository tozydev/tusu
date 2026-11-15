package vn.id.tozydev.tusu.ui.format

/**
 * Localized words and patterns used by [AppDateTimeFormatter].
 *
 * Patterns may contain `{weekday}`, `{day}`, `{month}` (number), `{monthShort}`, `{monthName}`,
 * `{year}` and `{time}`. [weekdays] starts on Monday.
 */
data class DateTimeNames(
    val weekdays: List<String>,
    val monthsShort: List<String>,
    val monthsFull: List<String>,
    val today: String,
    val yesterday: String,
    val dayHeaderPattern: String,
    val dayHeaderWithYearPattern: String,
    val monthTitlePattern: String,
    val fullDateTimePattern: String,
) {
    init {
        require(weekdays.size == 7) { "Expected 7 weekday names, got ${weekdays.size}" }
        require(monthsShort.size == 12) { "Expected 12 short month names, got ${monthsShort.size}" }
        require(monthsFull.size == 12) { "Expected 12 month names, got ${monthsFull.size}" }
    }
}
