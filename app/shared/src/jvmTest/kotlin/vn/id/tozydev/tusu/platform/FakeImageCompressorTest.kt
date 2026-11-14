package vn.id.tozydev.tusu.platform

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

class FakeImageCompressorTest :
    FunSpec(
        {
            val compressor = FakeImageCompressor()
            lateinit var dir: File

            beforeTest { dir = Files.createTempDirectory("tusu-compressor").toFile() }
            afterTest { dir.deleteRecursively() }

            test("copies the source to the destination and reports a size within the limit") {
                val source = Path(dir.path, "in.jpg")
                val destination = Path(dir.path, "out.jpg")
                SystemFileSystem.sink(source).buffered().use { it.write(byteArrayOf(1, 2, 3)) }

                val result = compressor.compress(source, destination, maxLongEdgePx = 2048)

                result shouldBe Outcome.Success(ImageSize(width = 1600, height = 1200))
                SystemFileSystem.source(destination).buffered().use { it.readByteArray() } shouldBe
                    byteArrayOf(1, 2, 3)
            }

            test("shrinks the reported size to the long edge limit") {
                val source = Path(dir.path, "in.jpg")
                SystemFileSystem.sink(source).buffered().use { it.write(byteArrayOf(1)) }

                val result = compressor.compress(source, Path(dir.path, "out.jpg"), 800)

                result shouldBe Outcome.Success(ImageSize(width = 800, height = 600))
            }

            test("missing source is FileUnreadable") {
                val result =
                    compressor.compress(Path(dir.path, "missing.jpg"), Path(dir.path, "out.jpg"))

                result shouldBe Outcome.Failure(MediaError.FileUnreadable)
            }
        },
    )
