package vn.io.tozydev.tusu.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.tozydev.tusu.domain.model.Tag

interface TagRepository {
    fun getTagsFlow(): Flow<List<Tag>>
}
