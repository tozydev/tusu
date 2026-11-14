package vn.id.tozydev.tusu.data.db.entry

import androidx.room3.Embedded
import androidx.room3.Relation
import vn.id.tozydev.tusu.data.db.label.EntryLabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelEntity

/** A link row with its label; the label is null only if the foreign key is broken. */
data class EntryLabelWithLabel(
    @Embedded val link: EntryLabelEntity,
    @Relation(parentColumns = ["label_id"], entityColumns = ["id"]) val label: LabelEntity?,
)
