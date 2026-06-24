package vn.io.tozydev.tusu.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.tozydev.tusu.data.db.TagDao
import vn.io.tozydev.tusu.data.db.TagEntity
import vn.io.tozydev.tusu.data.db.toModel
import vn.io.tozydev.tusu.domain.model.Tag
import vn.io.tozydev.tusu.domain.repository.TagRepository

@Inject
@ContributesBinding(AppScope::class, binding = binding<TagRepository>())
class TagRepositoryImpl(val tagDao: TagDao) : TagRepository {
    override fun getTagsFlow(): Flow<List<Tag>> {
        return tagDao.getAllAsFlow().map { entities -> entities.map { it.toModel() } }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createTag(name: String): Tag {
        val entity = TagEntity(id = Uuid.generateV7(), name = name)
        tagDao.insert(entity)
        return entity.toModel()
    }
}
