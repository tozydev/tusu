package vn.id.tozydev.tusu.data.db.label

import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup

fun LabelGroupEntity.toDomain(): LabelGroup =
    LabelGroup(
        id = id,
        name = name,
        color = color,
        selection = selection,
        showInComposer = showInComposer,
        isDefault = isDefault,
        sortOrder = sortOrder,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

fun LabelGroup.toEntity(): LabelGroupEntity =
    LabelGroupEntity(
        id = id,
        name = name,
        color = color,
        selection = selection,
        showInComposer = showInComposer,
        isDefault = isDefault,
        sortOrder = sortOrder,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

fun LabelEntity.toDomain(): Label =
    Label(
        id = id,
        groupId = groupId,
        name = name,
        isHidden = isHidden,
        isDefault = isDefault,
        sortOrder = sortOrder,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

fun Label.toEntity(): LabelEntity =
    LabelEntity(
        id = id,
        groupId = groupId,
        name = name,
        isHidden = isHidden,
        isDefault = isDefault,
        sortOrder = sortOrder,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
