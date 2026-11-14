package vn.id.tozydev.tusu.data.media

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import kotlin.uuid.Uuid
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

/** Runs [block] with a fresh media folder and deletes it afterwards, even when [block] fails. */
private suspend fun <R> withMediaRoot(block: suspend (Path, File) -> R): R {
    val dir = Files.createTempDirectory("tusu-media-test").toFile()
    try {
        return block(Path(dir.absolutePath), dir)
    } finally {
        dir.deleteRecursively()
    }
}

private fun textSource(text: String): RawSource =
    Buffer().also { it.write(text.encodeToByteArray()) }

private fun readText(path: Path): String =
    SystemFileSystem.source(path).buffered().use { it.readByteArray() }.decodeToString()

/** Fails on the first read, as a source over a disconnected device would. */
private class FailingSource(private val onClose: () -> Unit = {}) : RawSource {
    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long =
        throw IOException("device gone")

    override fun close() = onClose()
}

private fun File.regularFiles(): List<File> = walkTopDown().filter { it.isFile }.toList()

class MediaStoreTest :
    FunSpec(
        {
            test("bytes written under a relative path read back from the resolved path") {
                withMediaRoot { root, _ ->
                    val store = MediaStore(root)
                    val relativePath = store.newRelativePath(Uuid.random(), "JPG")

                    store.write(relativePath, textSource("hello")) shouldBe Outcome.Success(Unit)

                    store.exists(relativePath) shouldBe true
                    readText(store.resolve(relativePath)) shouldBe "hello"
                }
            }

            test("delete removes the file and a second delete of it still succeeds") {
                withMediaRoot { root, _ ->
                    val store = MediaStore(root)
                    val relativePath = store.newRelativePath(Uuid.random(), "jpg")
                    store.write(relativePath, textSource("x"))

                    store.delete(relativePath) shouldBe Outcome.Success(Unit)

                    store.exists(relativePath) shouldBe false
                    store.delete(relativePath) shouldBe Outcome.Success(Unit)
                }
            }

            test("delete of a file that never existed succeeds") {
                withMediaRoot { root, _ ->
                    val store = MediaStore(root)

                    store.delete("ab/cd/missing.jpg") shouldBe Outcome.Success(Unit)
                }
            }

            test("new paths are sharded by the first two bytes of the id and keep the extension") {
                withMediaRoot { root, _ ->
                    val store = MediaStore(root)
                    val id = Uuid.parse("0f1e2d3c-4b5a-6978-8695-a4b3c2d1e0f9")

                    store.newRelativePath(id, "JPG") shouldBe
                        "0f/1e/0f1e2d3c-4b5a-6978-8695-a4b3c2d1e0f9.jpg"
                }
            }

            test("extension keeps only letters and digits, lower-cased") {
                withMediaRoot { root, _ ->
                    val store = MediaStore(root)
                    val id = Uuid.parse("0f1e2d3c-4b5a-6978-8695-a4b3c2d1e0f9")

                    store.newRelativePath(id, "M4A!").endsWith("-a4b3c2d1e0f9.m4a") shouldBe true
                }
            }

            test("an extension without letters or digits is a caller bug") {
                withMediaRoot { root, _ ->
                    val store = MediaStore(root)

                    shouldThrow<IllegalArgumentException> {
                        store.newRelativePath(Uuid.random(), "..")
                    }
                }
            }

            test("paths that leave the media folder are rejected") {
                withMediaRoot { root, _ ->
                    val store = MediaStore(root)

                    shouldThrow<IllegalArgumentException> { store.resolve("../outside.jpg") }
                    shouldThrow<IllegalArgumentException> { store.resolve("ab/../../outside.jpg") }
                    shouldThrow<IllegalArgumentException> { store.resolve("/etc/passwd") }
                    shouldThrow<IllegalArgumentException> {
                        store.write("../outside.jpg", textSource("x"))
                    }
                    shouldThrow<IllegalArgumentException> { store.delete("../outside.jpg") }
                }
            }

            test("a failure while reading the source is FileUnreadable and leaves no file") {
                withMediaRoot { root, dir ->
                    val store = MediaStore(root)
                    val relativePath = store.newRelativePath(Uuid.random(), "jpg")

                    store.write(relativePath, FailingSource()) shouldBe
                        Outcome.Failure(MediaError.FileUnreadable)

                    store.exists(relativePath) shouldBe false
                    dir.regularFiles().shouldBeEmpty()
                }
            }

            test("the source is closed after a successful write and after a failed one") {
                withMediaRoot { root, _ ->
                    val store = MediaStore(root)
                    var closed = 0
                    val ok =
                        object : RawSource by textSource("ok") {
                            override fun close() {
                                closed++
                            }
                        }
                    store.write(store.newRelativePath(Uuid.random(), "jpg"), ok)
                    store.write(
                        store.newRelativePath(Uuid.random(), "jpg"),
                        FailingSource(onClose = { closed++ }),
                    )

                    closed shouldBe 2
                }
            }

            test("a failure to move the file into place is StorageFailed and leaves no temp file") {
                withMediaRoot { root, dir ->
                    val store = MediaStore(root)
                    val relativePath = store.newRelativePath(Uuid.random(), "jpg")
                    // A directory under the final name makes the move fail on every platform.
                    File(dir, relativePath).mkdirs()

                    store.write(relativePath, textSource("x")) shouldBe
                        Outcome.Failure(MediaError.StorageFailed)

                    dir.regularFiles().shouldBeEmpty()
                }
            }
        },
    )
