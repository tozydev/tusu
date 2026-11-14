package vn.id.tozydev.tusu.data.db.entry

import androidx.room3.Embedded
import androidx.room3.Relation
import vn.id.tozydev.tusu.data.db.label.EntryLabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelEntity
import vn.id.tozydev.tusu.data.db.media.MediaEntity

/**
 * An entry with its media and label links. Room observes all three tables for queries that return
 * this type, so a change to any of them re-emits the entry.
 */
data class EntryWithRelations(
    @Embedded val entry: EntryEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["entry_id"]) val media: List<MediaEntity>,
    // The element type is a projection, so the entity has to be named explicitly.
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["entry_id"],
        entity = EntryLabelEntity::class,
    )
    val labelLinks: List<EntryLabelWithLabel>,
)

/** A link row with its label; the label is null only if the foreign key is broken. */
data class EntryLabelWithLabel(
    @Embedded val link: EntryLabelEntity,
    @Relation(parentColumns = ["label_id"], entityColumns = ["id"]) val label: LabelEntity?,
)
