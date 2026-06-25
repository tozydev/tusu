package vn.id.tozydev.tusu.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class EntryWithRelations(
    @Embedded val entry: EntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy =
            Junction(
                value = EntryTagCrossRef::class,
                parentColumn = "entry_id",
                entityColumn = "tag_id",
            ),
    )
    val tags: List<TagEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id",
    )
    val media: List<MediaEntity>,
)
