package vn.io.tozyworks.tusu.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.tozyworks.tusu.data.db.TagDao
import vn.io.tozyworks.tusu.data.db.toModel
import vn.io.tozyworks.tusu.domain.model.Tag
import vn.io.tozyworks.tusu.domain.repository.TagRepository

@Inject
@ContributesBinding(AppScope::class, binding = binding<TagRepository>())
class TagRepositoryImpl(val tagDao: TagDao) : TagRepository {
    override fun getTagsFlow(): Flow<List<Tag>> {
        return tagDao.getAllAsFlow().map { entities -> entities.map { it.toModel() } }
    }
}
