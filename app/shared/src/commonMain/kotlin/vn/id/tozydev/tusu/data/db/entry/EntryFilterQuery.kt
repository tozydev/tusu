package vn.id.tozydev.tusu.data.db.entry

import androidx.room3.RoomRawQuery
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.EntryFilter
import vn.id.tozydev.tusu.text.foldDiacritics

/** SQL with `?` placeholders and the text value bound to each, in order. */
internal class EntryFilterSql(val sql: String, val args: List<String>)

/**
 * Compiles [EntryFilter] to parameterised SQL. Every user value is bound, never concatenated, so
 * input can not change the statement. Criteria are ANDed; the label ids of one group are ORed.
 */
internal fun EntryFilter.toEntryFilterSql(): EntryFilterSql {
    val clauses = mutableListOf("e.deleted_at IS NULL")
    val args = mutableListOf<String>()

    ftsMatchQuery(query)?.let { match ->
        clauses += "e.id IN (SELECT entry_id FROM entries_fts WHERE entries_fts MATCH ?)"
        args += match
    }
    if (mediaTypes.isNotEmpty()) {
        clauses +=
            "EXISTS (SELECT 1 FROM media m WHERE m.entry_id = e.id AND m.deleted_at IS NULL " +
                "AND m.type IN (${placeholders(mediaTypes.size)}))"
        args += mediaTypes.map { it.name }
    }
    if (hasLocation) clauses += "e.place_name IS NOT NULL"
    if (emojis.isNotEmpty()) {
        clauses += "e.emoji IN (${placeholders(emojis.size)})"
        args += emojis
    }
    // A group with no selection does not filter, so it contributes no clause.
    for (ids in labelIdsByGroup.values) {
        if (ids.isEmpty()) continue
        clauses +=
            "EXISTS (SELECT 1 FROM entry_labels el JOIN labels l ON l.id = el.label_id " +
                "WHERE el.entry_id = e.id AND el.deleted_at IS NULL AND l.deleted_at IS NULL " +
                "AND el.label_id IN (${placeholders(ids.size)}))"
        args += ids.map(Uuid::toString)
    }
    dateRange?.let { range ->
        // local_date is stored as ISO text, so text comparison orders the days correctly.
        clauses += "e.local_date BETWEEN ? AND ?"
        args += range.start.toString()
        args += range.endInclusive.toString()
    }

    val sql =
        "SELECT e.* FROM entries e WHERE ${clauses.joinToString(" AND ")} " +
            "ORDER BY e.occurred_at DESC, e.id DESC"
    return EntryFilterSql(sql, args)
}

/** The filter as the raw query Room runs for search paging. */
internal fun EntryFilter.toRoomRawQuery(): RoomRawQuery {
    val compiled = toEntryFilterSql()
    return RoomRawQuery(sql = compiled.sql) { statement ->
        compiled.args.forEachIndexed { index, value -> statement.bindText(index + 1, value) }
    }
}

/**
 * The FTS5 MATCH expression for [query]: each token is folded, quoted and made a prefix, so every
 * token must match and the user can never type FTS operators. Null when no token has a letter or
 * digit, so the search adds no text clause.
 */
internal fun ftsMatchQuery(query: String): String? {
    val tokens =
        query.foldDiacritics().split(WHITESPACE).filter { token ->
            token.any { it.isLetterOrDigit() }
        }
    if (tokens.isEmpty()) return null
    return tokens.joinToString(separator = " ") { token ->
        // A double quote inside an FTS5 string is escaped by doubling it.
        "\"" + token.replace("\"", "\"\"") + "\"*"
    }
}

private fun placeholders(count: Int): String = List(count) { "?" }.joinToString(separator = ",")

private val WHITESPACE = Regex("\\s+")
