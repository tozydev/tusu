package vn.id.tozydev.tusu.ui.platform

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import kotlinx.io.files.Path
import okio.Path.Companion.toPath

/**
 * Builds the app-wide image loader. The disk cache lives under [cacheDir], which the platform may
 * clear and leaves out of backups.
 */
fun newAppImageLoader(context: PlatformContext, cacheDir: Path): ImageLoader =
    ImageLoader.Builder(context)
        .components { addPlatformFileSupport() }
        .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.25).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(Path(cacheDir, IMAGE_CACHE_DIR_NAME).toString().toPath())
                .build()
        }
        .crossfade(true)
        .build()

private const val IMAGE_CACHE_DIR_NAME = "image_cache"
