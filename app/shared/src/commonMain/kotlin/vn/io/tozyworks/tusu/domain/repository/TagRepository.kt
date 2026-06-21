package vn.io.tozyworks.tusu.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.tozyworks.tusu.domain.model.Tag

interface TagRepository {
    fun getTagsFlow(): Flow<List<Tag>>
}
