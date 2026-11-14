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
 * Builds the app-wide image loader. The disk cache lives under [appDir] rather than the platform
 * cache folder, so one code path serves Android and the JVM alike.
 */
fun newAppImageLoader(context: PlatformContext, appDir: Path): ImageLoader =
    ImageLoader.Builder(context)
        .components { addPlatformFileSupport() }
        .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.25).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(Path(appDir, IMAGE_CACHE_DIR_NAME).toString().toPath())
                .build()
        }
        .crossfade(true)
        .build()

private const val IMAGE_CACHE_DIR_NAME = "image_cache"
