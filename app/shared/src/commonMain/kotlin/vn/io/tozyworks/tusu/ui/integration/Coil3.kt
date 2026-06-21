package vn.io.tozyworks.tusu.ui.integration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.map.Mapper
import coil3.request.crossfade
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.resolve

@Composable
@ReadOnlyComposable
fun InitCoil3(appDir: PlatformFile) {
    setSingletonImageLoaderFactory { ctx ->
        ImageLoader.Builder(ctx)
            .crossfade(true)
            .components {
                add(getRelativeMediaPathToAbsoluteMapper(appDir))
            }
            .build()
    }
}

private fun getRelativeMediaPathToAbsoluteMapper(appDir: PlatformFile) =
    Mapper<String, String> { data, _ ->
        // todo magic constants
        if (data.startsWith("./media/")) {
            appDir.resolve(data).absolutePath()
        } else {
            null
        }
    }
