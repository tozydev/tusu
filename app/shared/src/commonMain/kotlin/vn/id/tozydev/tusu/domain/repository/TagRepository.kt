package vn.id.tozydev.tusu.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.id.tozydev.tusu.domain.model.Tag

interface TagRepository {
    fun getTagsFlow(): Flow<List<Tag>>

    suspend fun createTag(name: String): Tag
}
