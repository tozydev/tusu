package vn.id.tozydev.tusu.data.db.label

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import kotlin.time.Clock
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.LabelSelection

/**
 * Seeds the default label groups and labels when the database is created. It runs only on creation,
 * so deleting every group later never brings the defaults back.
 *
 * The rows are written with plain SQL, because Room's DAOs are not usable inside `onCreate`. The
 * values use the same storage format as the entities: ids as text, instants as epoch millis,
 * booleans as 0 or 1, and enums by name.
 */
class DefaultLabelSeedCallback(
    private val clock: Clock,
    private val defaultNames: suspend () -> DefaultLabelNames,
) : RoomDatabase.Callback() {
    override suspend fun onCreate(connection: SQLiteConnection) {
        val names = defaultNames()
        val now = clock.now().toEpochMilliseconds()
        val kindId = Uuid.random()

        insertGroup(connection, kindId, names.kindGroup, KIND_COLOR, LabelSelection.MULTI, 0, now)
        insertGroup(
            connection,
            Uuid.random(),
            names.peopleGroup,
            PEOPLE_COLOR,
            LabelSelection.MULTI,
            1,
            now,
        )
        insertGroup(
            connection,
            Uuid.random(),
            names.tripsGroup,
            TRIPS_COLOR,
            LabelSelection.SINGLE,
            2,
            now,
        )

        val kindLabels =
            listOf(names.achievement, names.lesson, names.reflection, names.memory, names.gratitude)
        kindLabels.forEachIndexed { index, name ->
            insertLabel(connection, Uuid.random(), kindId, name, index, now)
        }
    }

    private fun insertGroup(
        connection: SQLiteConnection,
        id: Uuid,
        name: String,
        color: Int,
        selection: LabelSelection,
        sortOrder: Int,
        now: Long,
    ) {
        connection
            .prepare(
                "INSERT INTO label_groups (id, name, color, selection, show_in_composer, " +
                    "is_default, sort_order, updated_at, deleted_at) " +
                    "VALUES (?, ?, ?, ?, 1, 1, ?, ?, NULL)",
            )
            .use { statement ->
                statement.bindText(1, id.toString())
                statement.bindText(2, name)
                statement.bindLong(3, color.toLong())
                statement.bindText(4, selection.name)
                statement.bindLong(5, sortOrder.toLong())
                statement.bindLong(6, now)
                statement.step()
            }
    }

    private fun insertLabel(
        connection: SQLiteConnection,
        id: Uuid,
        groupId: Uuid,
        name: String,
        sortOrder: Int,
        now: Long,
    ) {
        connection
            .prepare(
                "INSERT INTO labels (id, group_id, name, is_hidden, is_default, sort_order, " +
                    "updated_at, deleted_at) VALUES (?, ?, ?, 0, 1, ?, ?, NULL)",
            )
            .use { statement ->
                statement.bindText(1, id.toString())
                statement.bindText(2, groupId.toString())
                statement.bindText(3, name)
                statement.bindLong(4, sortOrder.toLong())
                statement.bindLong(5, now)
                statement.step()
            }
    }

    private companion object {
        // ARGB colours of the seeded groups; distinct from each other.
        val KIND_COLOR = 0xFF4F7CAC.toInt()
        val PEOPLE_COLOR = 0xFFC2708A.toInt()
        val TRIPS_COLOR = 0xFF5E9E6E.toInt()
    }
}
